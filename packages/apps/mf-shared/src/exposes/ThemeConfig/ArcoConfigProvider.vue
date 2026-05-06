<template>
  <a-config-provider :locale="zhCN" :theme="arcoTheme">
    <slot></slot>
  </a-config-provider>
</template>

<script setup>
import { computed } from "vue";
import zhCN from "@arco-design/web-vue/es/locale/lang/zh-cn";
import { theme as arcoThemeUtils } from "@arco-design/web-vue";

const props = defineProps({
  primaryColor: {
    type: String,
    default: "#165DFF"
  }
});

const arcoTheme = computed(() => {
  return {
    ...arcoThemeUtils,
    token: {
      colorPrimary: props.primaryColor,
      colorPrimaryHover: "#0E42D2",
      colorPrimaryActive: "#092DC2",
      colorPrimaryBg: "#E8F3FF",
      colorPrimaryBorder: "#99BFFF",

      colorSuccess: "#00B42A",
      colorSuccessLight: "#53D13D",
      colorSuccessBg: "#E8FFEA",

      colorWarning: "#FF7D00",
      colorWarningLight: "#FF9A2E",
      colorWarningBg: "#FFF7E8",

      colorDanger: "#F53F3F",
      colorDangerLight: "#F87171",
      colorDangerBg: "#FFECE8",

      borderRadius: 8,

      fontSizeHeading1: 24,
      fontSizeHeading2: 20,
      fontSizeHeading3: 16,
      fontSizeBody: 14,

      colorText1: "#1D2129",
      colorText2: "#4E5969",
      colorText3: "#86909C",
      colorText4: "#C9CDD4",

      colorBgContainer: "#FFFFFF",
      colorBgLayout: "#F5F6F7",
      colorBorder: "#E5E6EB",
      colorFill1: "#F2F3F5",
      colorFill2: "#F7F8FA"
    }
  };
});
</script>

<style scoped>
</style>
