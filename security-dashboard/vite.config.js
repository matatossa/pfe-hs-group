import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Proxy Eureka API calls to avoid CORS — browser sees same origin
      '/eureka-proxy': {
        target: 'http://localhost:8761',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/eureka-proxy/, ''),
      },
      // Proxy normalization service (avoids CORS in production-like setups)
      '/norm-proxy': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/norm-proxy/, ''),
      },
      // Proxy RSS collector service
      '/rss-proxy': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/rss-proxy/, ''),
      },
    },
  },
})
