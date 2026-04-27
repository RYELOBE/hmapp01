import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import qiankun from "vite-plugin-qiankun";
import federation from "@originjs/vite-plugin-federation";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [
    vue(),
    qiankun("ops", {
      useDevMode: true,
    }),
    federation({
      name: "ops",
      filename: "remoteEntry.js",
      remotes: {
        commonprovide: {
          external: "http://localhost:7199/assets/remoteEntry.js",
          from: "vite",
        },
      },
      shared: {
        vue: { singleton: true },
        pinia: { singleton: true },
        "vue-router": { singleton: true },
        "@arco-design/web-vue": { singleton: false },
        axios: { singleton: true },
      },
    }),
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
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  base: "/",
  build: {
    target: "esnext",
    minify: false,
  },
});
