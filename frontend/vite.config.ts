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
  },
})
