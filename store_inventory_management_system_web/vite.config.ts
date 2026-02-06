import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      "/api/system": {
        target: "http://127.0.0.1:8000",
        changeOrigin: true,
      },
    },
  },
  css: { devSourcemap: true },
});
