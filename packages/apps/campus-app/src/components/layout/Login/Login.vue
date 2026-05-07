<template>
  <a-modal
    v-if="props.modal"
    :visible="visible"
    :title="null"
    :footer="false"
    :closable="true"
    :mask-closable="false"
    :width="900"
    :centered="true"
    class="login-modal"
    @cancel="handleClose"
  >
    <div class="login-modal-content">
      <div class="login-modal-brand">
        <div class="brand-bg"></div>
        <div class="brand-info">
          <h3 class="brand-title">{{ loginConfig.name || 'Campus Trade' }}</h3>
          <p class="brand-subtitle">校园二手交易平台</p>
          <div class="brand-features">
            <div class="feature-item">
              <icon-safe />
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <icon-thunderbolt />
              <span>快速交易</span>
            </div>
            <div class="feature-item">
              <icon-heart-fill />
              <span>校园专属</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-modal-form">
        <LoginContent
          :login-config="loginConfig"
          :app-type="appType"
          @login-success="handleLoginSuccess"
        />
      </div>
    </div>
  </a-modal>

  <div v-else-if="props.type === 'full-screen'" class="full-screen-login">
    <div class="full-screen-login-inner">
      <div class="login-brand">
        <div class="brand-bg"></div>
        <div class="brand-content">
          <div class="brand-logo">
            <icon-apps />
          </div>
          <h1 class="brand-title">{{ loginConfig.name || 'Campus Trade' }}</h1>
          <p class="brand-subtitle">校园二手交易平台</p>
          <div class="brand-stats">
            <div class="stat-item">
              <span class="stat-number">10K+</span>
              <span class="stat-label">活跃用户</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">50K+</span>
              <span class="stat-label">成功交易</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">99%</span>
              <span class="stat-label">好评率</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-form-section">
        <LoginContent
          class="login-form-wrapper"
          :login-config="loginConfig"
          :app-type="appType"
          @login-success="loginSuccess"
        />
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
import { IconSafe, IconThunderbolt, IconHeartFill, IconApps } from "@arco-design/web-vue/es/icon";

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
    display: flex;
    width: 100%;
    height: 100%;
    min-height: 780px;

    .login-brand {
      flex: 0 0 40%;
      background-color: var(--color-primary-light-1, #f0f5fe);
      position: relative;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;

      .brand-bg {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: radial-gradient(circle at top, #ddecff 0%, #f8fbff 48%, #ffffff 100%);
        opacity: 0.8;
      }

      .brand-content {
        position: relative;
        z-index: 1;
        text-align: center;
        padding: 40px;

        .brand-logo {
          width: 80px;
          height: 80px;
          margin: 0 auto 24px;
          background: linear-gradient(135deg, var(--color-primary, #165DFF) 0%, #36bffa 100%);
          border-radius: var(--border-radius-medium, 8px);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 40px;
          color: #fff;
          box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3);
        }

        .brand-title {
          font-size: 32px;
          font-weight: 700;
          color: var(--color-primary, #165DFF);
          margin: 0 0 12px;
          line-height: 1.3;
        }

        .brand-subtitle {
          font-size: 16px;
          color: var(--color-text-3, #86909c);
          margin: 0 0 40px;
        }

        .brand-stats {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 32px;

          .stat-item {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .stat-number {
              font-size: 28px;
              font-weight: 700;
              color: var(--color-primary, #165DFF);
            }

            .stat-label {
              font-size: 13px;
              color: var(--color-text-3, #86909c);
            }
          }

          .stat-divider {
            width: 1px;
            height: 40px;
            background: var(--color-border-2, #e5e6eb);
          }
        }
      }
    }

    .login-form-section {
      flex: 0 0 60%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;
      background: #fff;
      box-shadow: -10px 0 30px rgba(0, 0, 0, 0.05);

      .login-form-wrapper {
        width: 100%;
        max-width: 420px;
      }
    }
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
  border-radius: var(--border-radius-medium, 8px);
  border: 1px solid #d9e7ff;
  box-shadow: 0 16px 30px rgba(69, 120, 218, 0.12);
}

.login-modal-content {
  display: flex;
  min-height: 500px;
  padding: 0;
  margin: -16px -24px;

  .login-modal-brand {
    flex: 0 0 40%;
    background: linear-gradient(135deg, var(--color-primary, #165DFF) 0%, #36bffa 100%);
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .brand-bg {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      opacity: 0.1;
      background-image:
        radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 50%);
    }

    .brand-info {
      position: relative;
      z-index: 1;
      text-align: center;
      color: #fff;

      .brand-title {
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 12px;
        text-shadow: 0 2px 4px rgba(0,0,0,0.1);
      }

      .brand-subtitle {
        font-size: 14px;
        margin: 0 0 48px;
        opacity: 0.9;
      }

      .brand-features {
        display: flex;
        flex-direction: column;
        gap: 20px;

        .feature-item {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 14px;
          font-weight: 500;

          .arco-icon {
            font-size: 20px;
          }
        }
      }
    }
  }

  .login-modal-form {
    flex: 0 0 60%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
  }
}
</style>
