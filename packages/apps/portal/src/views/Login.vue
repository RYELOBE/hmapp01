<template>
  <Login type="full-screen" @login-success="loginSuccess" />
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Login } from "commonprovide/login";

const router = useRouter();

function loginSuccess() {
  if (!window.__POWERED_BY_QIANKUN__ || import.meta.env.DEV) {
    router.push('/buyer/home');
  } else {
    router.push('/');
  }
}
</script>