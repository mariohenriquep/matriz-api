import { randomBytes } from 'node:crypto'
import { defineConfig, devices } from '@playwright/test'
import { BD_E2E, PASSWORD_DEMO, PORTA_API, PORTA_WEB, lerEnv } from './ambiente.mjs'

const env = lerEnv()
const portaBd = env.POSTGRES_PORTA ?? '5433'

/**
 * Testes E2E da Matriz (Ficha 04, Ex. 4). O Playwright arranca o backend e o frontend e espera que respondam;
 * antes, preparar-bd.mjs cria de novo a base de dados matriz_e2e. Precisa do PostgreSQL do docker compose a correr.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: `http://localhost:${PORTA_WEB}`,
    // Só se guardam evidências quando um teste falha, para diagnóstico
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command: 'node preparar-bd.mjs && cd ../backend && ./mvnw -q spring-boot:run',
      url: `http://localhost:${PORTA_API}/actuator/health`,
      timeout: 240_000,
      reuseExistingServer: false,
      stdout: 'ignore',
      stderr: 'pipe',
      env: {
        MATRIZ_DB_URL: `jdbc:postgresql://localhost:${portaBd}/${BD_E2E}`,
        MATRIZ_PORTA: String(PORTA_API),
        MATRIZ_CORS_ORIGENS: `http://localhost:${PORTA_WEB}`,
        MATRIZ_SEED_ATIVO: 'true',
        MATRIZ_SEED_PASSWORD: PASSWORD_DEMO,
        // Segredo novo a cada execução: os tokens só valem durante os testes
        MATRIZ_JWT_SEGREDO: randomBytes(48).toString('base64'),
      },
    },
    {
      command: `npm --prefix ../frontend run dev -- --port ${PORTA_WEB}`,
      url: `http://localhost:${PORTA_WEB}`,
      timeout: 60_000,
      reuseExistingServer: false,
      env: { VITE_API_URL: `http://localhost:${PORTA_API}/api/v1` },
    },
  ],
})
