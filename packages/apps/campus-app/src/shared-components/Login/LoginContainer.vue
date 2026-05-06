<template>
  <div class="login-container">
    <slot></slot>
  </div>
</template>

<script setup>
const emits = defineEmits(["close"]);

function handleClose() {
  emits("close");
}
</script>

<style scoped>
.login-container {
  position: relative;
}
</style>
