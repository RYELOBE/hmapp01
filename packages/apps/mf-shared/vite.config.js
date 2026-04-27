import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import federation from "@originjs/vite-plugin-federation";

export default defineConfig({
  plugins: [
    vue(),
    federation({
      name: "commonprovide",
      filename: "remoteEntry.js",
      exposes: {
        "./auth-sdk": "./src/exposes/auth-sdk.js",
        "./http": "./src/exposes/http.js",
        "./ai-sdk": "./src/exposes/ai-sdk.js",
        "./login": "./src/exposes/login.js",
        "./status-tag": "./src/exposes/status-tag.js",
        "./PageContainer": "./src/exposes/PageContainer.js",
        "./PageHeader": "./src/exposes/PageHeader.js",
        "./ImageUploader": "./src/exposes/ImageUploader.js",
        "./ConfirmDialog": "./src/exposes/ConfirmDialog.js",
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
  server: {
    port: 7199,
    cors: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  build: {
    target: "esnext",
    outDir: "dist",
    minify: false,
  },
  // 确保 preview 模式也正确提供 remoteEntry.js
  preview: {
    port: 7199,
    cors: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
});
