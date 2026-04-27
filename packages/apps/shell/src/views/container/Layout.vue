<template>
  <AppMain />
  <ToAppMain v-if="authStore.isLoggedIn" />
</template>

<script setup>
import AppMain from "./AppMain.vue";
import ToAppMain from "./ToAppMain.vue";
import { useAuthStore } from "../../stores/auth";

const authStore = useAuthStore();
</script>
