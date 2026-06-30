import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  base: process.env.NODE_ENV === 'production' ? '/springbootZLpxsG/client/dist/' : '/',
  plugins: [vue()],
  resolve: {
    alias: {
      "@": "/src",
    },
  },
  server: {
    proxy: {
      '/springbootZLpxsG': {
        target: 'http://127.0.0.1:9999/springbootZLpxsG',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/springbootZLpxsG/, '')
      }
    }
  },
});
