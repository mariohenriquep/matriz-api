// Configuração partilhada pelos testes E2E. Portas próprias (8081 e 5174) para poderem correr com o
// ambiente de desenvolvimento (8080 e 5173) ligado, e base de dados própria (matriz_e2e).
import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

export const BD_E2E = 'matriz_e2e'
export const PORTA_API = 8081
export const PORTA_WEB = 5174
// Password das contas do seed na base de dados de E2E. Só existe nessa base de dados, que é apagada a cada execução.
export const PASSWORD_DEMO = 'e2e-matriz-demo'

/**
 * Lê o .env da raiz do repositório (o mesmo do docker compose e do backend), se existir.
 * @returns {Record<string, string | undefined>}
 */
export function lerEnv() {
  const caminho = fileURLToPath(new URL('../.env', import.meta.url))
  /** @type {Record<string, string>} */
  const env = {}
  if (existsSync(caminho)) {
    for (const linha of readFileSync(caminho, 'utf8').split('\n')) {
      const m = linha.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*?)\s*$/)
      if (m) env[m[1]] = m[2]
    }
  }
  return { ...env, ...process.env }
}
