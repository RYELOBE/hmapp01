<template>
  <a-modal
    v-if="props.modal"
    :visible="visible"
    :title="null"
    :footer="false"
    :closable="true"
    :mask-closable="false"
    :width="420"
    :centered="true"
    class="login-modal"
    @cancel="handleClose"
  >
    <div class="login-modal-content">
      <div class="login-modal-header">
        <h3 class="login-modal-title">{{ loginConfig.name || 'Campus Trade' }}</h3>
        <p class="login-modal-subtitle">校园二手交易平台</p>
      </div>
      <LoginContent
        :login-config="loginConfig"
        :app-type="appType"
        @login-success="handleLoginSuccess"
      />
    </div>
  </a-modal>

  <div v-else-if="props.type === 'full-screen'" class="full-screen-login">
    <div class="full-screen-login-inner">
      <div class="login-bg"></div>
      <div class="login-content-outer">
        <div class="logo-box">
          <a-space :size="8">
            <span class="name">Campus Trade</span>
          </a-space>
        </div>
        <LoginContent
          class="login-content"
          :login-config="loginConfig"
          :app-type="appType"
          @login-success="loginSuccess"
        >
        </LoginContent>
        <div class="gray-font">
          <span>校园二手交易平台</span>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="login-wrap">
    <a-card class="login-card" title="校园二手交易平台">
      <LoginContent :login-config="loginConfig" :app-type="appType" @login-success="loginSuccess" />
    </a-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import LoginContent from "./LoginContent.vue";

const props = defineProps({
  type: {
    type: String,
    default: () => "full-screen"
  },
  modal: {
    type: Boolean,
    default: false
  },
  visible: {
    type: Boolean,
    default: false
  },
  loginConfig: {
    type: Object,
    default: () => ({
      logo: "",
      name: "Campus Trade",
      bg: ""
    })
  },
  appType: {
    type: String,
    default: ""
  }
});

const emits = defineEmits(["login-success", "update:visible", "close"]);

function loginSuccess() {
  emits("login-success");
}

function handleLoginSuccess() {
  emits("login-success");
  if (props.modal) {
    emits("update:visible", false);
    emits("close");
  }
}

function handleClose() {
  emits("update:visible", false);
  emits("close");
}
</script>

<style lang="scss" scoped>
.full-screen-login {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 1999;
  overflow: auto;
  background-color: #fff;

  .full-screen-login-inner {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;
    min-height: 780px;

    .login-bg {
      flex: 1 1 auto;
      background-color: #f0f5fe;
      background-repeat: no-repeat;
      background-size: cover;
      background-image: radial-gradient(circle at top, #ddecff 0%, #f8fbff 48%, #ffffff 100%);
    }

    .login-content-outer {
      flex: none;
      width: 480px;
      position: relative;
      background: #fff;
      box-shadow: -10px 0 30px rgba(0, 0, 0, 0.05);

      .login-content {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
      }
    }
  }

  .logo-box {
    padding: 16px 24px 0 24px;

    .name {
      color: #336ad8;
      font-size: 20px;
      font-weight: bold;
      line-height: 24px;
    }
  }

  .gray-font {
    text-align: center;
    font-size: 14px;
    line-height: 18px;
    color: #666;
    position: absolute;
    width: 100%;
    left: 0;
    bottom: 32px;
  }
}

.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at top, #ddecff 0%, #f8fbff 48%, #ffffff 100%);
}

.login-card {
  width: 420px;
  border-radius: 8px;
  border: 1px solid #d9e7ff;
  box-shadow: 0 16px 30px rgba(69, 120, 218, 0.12);
}

.login-modal-content {
  padding: 8px;
}

.login-modal-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-modal-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #336ad8;
}

.login-modal-subtitle {
  margin: 0;
  font-size: 14px;
  color: #86909c;
}
</style>
