import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/minio': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/minio/, ''),
      },
    },
  },
  build: {
    // 使用esbuild压缩
    minify: 'esbuild',
    // 生成 source map
    sourcemap: false,
    // 压缩资源
    assetsInlineLimit: 4096,
  },
})
