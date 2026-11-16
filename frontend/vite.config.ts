import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// O frontend chama a API diretamente (VITE_API_URL); o backend autoriza esta origem por CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/testes/preparar.ts'],
    // npm run test:cobertura: relatório lcov em coverage/, lido pelo SonarQube Cloud na CI
    coverage: {
      provider: 'v8',
      reporter: ['text-summary', 'lcov'],
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/**/*.test.{ts,tsx}', 'src/testes/**', 'src/main.tsx', 'src/**/*.d.ts'],
    },
  },
})
