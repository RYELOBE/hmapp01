import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      "@campus/common": path.resolve(__dirname, "../../common/src"),
      "@": path.resolve(__dirname, "src"),
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', '@arco-design/web-vue', 'axios', 'pinia'],
  },
  server: {
    host: "localhost",
    port: 7101,
    cors: true,
  },
  base: "/",
  build: {
    target: "esnext",
  },
});
