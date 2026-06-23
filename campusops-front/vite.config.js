import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3017,
    proxy: {
      '/campusops': {
        target: 'http://127.0.0.1:3016',
        changeOrigin: true,
      },
    },
  }
})
