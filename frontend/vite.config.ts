import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Em desenvolvimento, os pedidos a /api seguem para o backend (evita configurar CORS).
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
