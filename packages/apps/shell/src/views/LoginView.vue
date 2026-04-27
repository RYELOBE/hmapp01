<template>
  <Login type="full-screen" @login-success="handleLoginSuccess" />
</template>

<script setup>
import { useRouter } from "vue-router";
import { Login } from "commonprovide/login";
import { getToken } from "commonprovide/auth-sdk";

const router = useRouter();

function handleLoginSuccess() {
  if (getToken()) {
    router.push("/");
  }
}
</script>
