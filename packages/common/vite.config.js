import { defineConfig } from "vite";

export default defineConfig({
  build: {
    lib: {
      entry: {
        index: "./src/index.js",
        roles: "./src/roles.js",
        theme: "./src/theme.js",
        constants: "./src/constants.js",
        utils: "./src/utils.js",
      },
      formats: ["es"],
    },
    outDir: "dist",
    emptyOutDir: true,
  },
});
