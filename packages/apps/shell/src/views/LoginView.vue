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
    // 登录成功后直接跳转到门户首页
    const redirect = router.currentRoute.value.query.redirect;
    if (redirect && redirect !== '/') {
      router.push(redirect);
    } else {
      router.push('/portal/home');
    }
  }
}
</script>
