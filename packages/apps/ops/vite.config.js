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
    },
  },
  server: {
    host: "localhost",
    port: 7102,
    cors: true,
  },
  base: "/",
  build: {
    target: "esnext",
  },
});
