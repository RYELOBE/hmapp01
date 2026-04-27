<template>
  <a-layout class="layout">
    <!-- 头部（根据门户配置动态渲染） -->
    <a-layout-header
      class="header"
      :style="headerStyle"
    >
      <div class="header__left">
        <a-button
          v-if="authStore.isLoggedIn && showSider"
          type="text"
          class="header__trigger"
          :style="{ color: isLightTheme ? '#fff' : '#4e5969' }"
          @click="collapsed = !collapsed"
        >
          <template #icon>
            <icon-menu-fold v-if="!collapsed" />
            <icon-menu-unfold v-else />
          </template>
        </a-button>
        <a-button
          v-if="authStore.isLoggedIn && hideSider"
          type="text"
          class="header__trigger"
          :style="{ color: isLightTheme ? '#fff' : '#4e5969' }"
          @click="$router.push('/')"
        >
          <template #icon><icon-left /></template>
        </a-button>
        <div
          class="brand"
          :style="{ color: isLightTheme ? '#fff' : '#336ad8', cursor: 'pointer' }"
          @click="$router.push('/')"
        >
          {{ brandName }}
        </div>
      </div>
      <a-space v-if="authStore.isLoggedIn" size="medium">
        <a-tag :color="isLightTheme ? 'rgba(255,255,255,0.2)' : 'arcoblue'">
          <span :style="{ color: isLightTheme ? '#fff' : undefined }">{{ roleLabel }}</span>
        </a-tag>
        <a-button
          type="text"
          status="danger"
          :style="{ color: isLightTheme ? 'rgba(255,255,255,0.9)' : undefined }"
          @click="logout"
        >退出</a-button>
      </a-space>
    </a-layout-header>

    <!-- 主体 -->
    <a-layout class="body">
      <!-- 左侧菜单（从后端门户配置控制是否显示） -->
      <a-layout-sider
        v-if="authStore.isLoggedIn && showSider"
        class="sider"
        :width="220"
        :collapsed-width="48"
        :collapsed="collapsed"
        collapsible
        breakpoint="lg"
        @collapse-breakpoint="collapsed = true"
      >
        <!-- 优先使用后端菜单树 -->
        <a-menu
          v-if="myMenuTree.length > 0"
          :selected-keys="[activeMenuKey]"
          @menu-item-click="handleMenuClick"
          :style="{ width: collapsed ? '48px' : '220px' }"
        >
          <a-menu-item key="home">
            <template #icon><icon-home /></template>
            <span v-if="!collapsed">首页</span>
          </a-menu-item>
          <a-menu-item
            v-for="item in myMenuTree"
            :key="item.path"
          >
            <template #icon><icon-apps /></template>
            <span v-if="!collapsed">{{ item.menuName }}</span>
          </a-menu-item>
        </a-menu>
        <!-- 回退：使用子应用列表 -->
        <a-menu
          v-else
          :selected-keys="[activeMenuKey]"
          @menu-item-click="handleMenuClick"
          :style="{ width: collapsed ? '48px' : '220px' }"
        >
          <a-menu-item key="home">
            <template #icon><icon-home /></template>
            <span v-if="!collapsed">首页</span>
          </a-menu-item>
          <a-menu-item
            v-for="app in authorizedApps"
            :key="app.pathPrefix"
          >
            <template #icon><icon-apps /></template>
            <span v-if="!collapsed">{{ app.title }}</span>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>

      <!-- 内容区 -->
      <a-layout-content class="content">
        <div class="frameapp-main">
          <router-view v-show="!$route.meta.isMinProject" />
          <div id="TO_FRAME_WINDOW" />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
  <AiAssistant v-if="authStore.isLoggedIn" />
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import AiAssistant from "../../components/AiAssistant.vue";
import { useAuthStore } from "../../stores/auth";
import framePinia from "../../minFrame/pinia/framePinia";
import { roleLabels } from "@campus/common/roles";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const frame = framePinia();

const collapsed = ref(false);

/** 当前用户有权限的子应用列表 */
const authorizedApps = computed(() => frame.authorizedApps || []);

/** 当前子应用的门户配置 */
const portalConfig = computed(() => frame.currentPortalConfig || {});

/** 当前路由匹配的子应用配置 */
const currentApp = computed(() => {
  if (route.path === '/' || route.name === 'home') return null;
  return authorizedApps.value.find(
    app => route.path.startsWith(app.pathPrefix)
  ) || null;
});

/** 是否隐藏侧边栏（子应用配置全屏 或 门户配置关闭左侧菜单） */
const hideSider = computed(() => {
  return currentApp.value?.hideShellMenu === true;
});

/** 是否显示侧边栏 */
const showSider = computed(() => {
  if (hideSider.value) return false;
  if (authorizedApps.value.length === 0) return false;
  // 如果当前在子应用页面，检查该子应用对应的门户配置
  if (currentApp.value) {
    return portalConfig.value.showLeftMenu !== false;
  }
  return true;
});

/** 主题是否为浅色（文字用白色） */
const isLightTheme = computed(() => {
  return portalConfig.value?.theme?.textTheme === 'light';
});

/** 头部动态样式 */
const headerStyle = computed(() => {
  const bg = portalConfig.value?.theme?.background;
  if (bg) return { background: bg };
  return {};
});

/** 品牌名称（优先使用门户配置） */
const brandName = computed(() => {
  const logoConfig = portalConfig.value?.logoConfig;
  if (logoConfig?.showLogoTitle && logoConfig.systemName) {
    return logoConfig.systemName;
  }
  return 'CampusTrade';
});

/** 后端菜单树（从 resource_menu + 角色过滤获取），递归过滤当前子应用菜单 */
const myMenuTree = computed(() => {
  const tree = frame.myMenuTree || [];
  if (currentApp.value) {
    const appCode = currentApp.value.appCode;
    function filterByApp(nodes) {
      return nodes.filter(item => item.appCode === appCode).map(item => ({
        ...item,
        children: item.children ? filterByApp(item.children) : [],
      }));
    }
    return filterByApp(tree);
  }
  return tree;
});

/** 菜单选中高亮 */
const activeMenuKey = computed(() => {
  if (route.path === '/' || route.name === 'home') return 'home';
  // 先匹配后端菜单
  const menuMatch = myMenuTree.value.find(item => route.path.startsWith(item.path));
  if (menuMatch) return menuMatch.path;
  // 回退匹配子应用 pathPrefix
  const appMatch = authorizedApps.value.find(
    app => route.path.startsWith(app.pathPrefix)
  );
  return appMatch ? appMatch.pathPrefix : 'home';
});

const roleLabel = computed(() => {
  try {
    const roles = authStore?.roles;
    const role = Array.isArray(roles) && roles.length > 0 ? roles[0] : null;
    if (!role) return "未登录";
    return roleLabels[role] || role;
  } catch {
    return "未登录";
  }
});

function handleMenuClick(key) {
  if (key === 'home') {
    router.push('/');
  } else {
    router.push(key);
  }
}

function logout() {
  authStore.logout();
  router.push("/login");
}

// 路由变化时，加载对应子应用的门户配置
let lastPortalCode = null;
watch(() => route.path, async (path) => {
  if (!authStore.isLoggedIn) return;
  const app = authorizedApps.value.find(a => path.startsWith(a.pathPrefix));
  if (app) {
    if (app.portalCode && app.portalCode !== lastPortalCode) {
      lastPortalCode = app.portalCode;
      await frame.loadPortalConfig(app.portalCode);
    }
  } else {
    lastPortalCode = null;
    frame.currentPortalConfig = null;
  }
}, { immediate: true });

// 登录后加载菜单树
watch(() => authStore.isLoggedIn, async (loggedIn) => {
  if (loggedIn) {
    await frame.loadMyMenuTree();
  }
}, { immediate: true });
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: transparent;
}

.header {
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.header__left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header__trigger {
  font-size: 18px;
}

.brand {
  font-size: 20px;
  font-weight: 700;
}

.body {
  margin-top: 56px;
  min-height: calc(100vh - 56px);
}

.sider {
  background: #fff;
  border-right: 1px solid #e5e6eb;
}

.content {
  min-height: calc(100vh - 56px);
}

.frameapp-main {
  width: 100%;
  min-height: calc(100vh - 56px);
  position: relative;
}

#TO_FRAME_WINDOW {
  width: 100%;
  min-height: calc(100vh - 56px);
}
</style>
