module.exports = {
  plugins: {
    "postcss-px-to-viewport": {
      viewportWidth: 1920,
      unitPrecision: 5,
      viewportUnit: "vw",
      selectorBlackList: [/^\.arco-/],
      minPixelValue: 1,
      mediaQuery: false,
      exclude: [/node_modules\/@arco-design/],
    },
  },
};
