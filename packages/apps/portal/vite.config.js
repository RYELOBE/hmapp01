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
    qiankun("portal", {
      useDevMode: true,
    }),
    federation({
      name: "portal",
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
      "@": path.resolve(__dirname, "src"), // 添加 @ 别名指向 src
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'], // 确保 .vue 能被解析
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', '@arco-design/web-vue', 'axios', 'pinia'], // 预构建依赖
  },
  server: {
    host: "localhost",
    port: 7101,
    cors: true,
    fs: {
      strict: false, // 允许访问项目外文件（解决导入路径问题）
    },
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
