<template>
  <div class="ops-layout" :class="{ 'sidebar-collapsed': collapsed }">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="logo-area">
        <router-link to="/ops/dashboard" class="logo-link">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="20" cy="20" r="20" fill="#165DFF"/>
              <path d="M20 10L26 18H14L20 10Z" fill="white"/>
              <rect x="14" y="19" width="12" height="11" rx="2" fill="white"/>
            </svg>
          </div>
          <span class="logo-text" v-show="!collapsed">运营后台</span>
        </router-link>
      </div>

      <nav class="nav-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: isActiveRoute(item.path) }"
          :title="collapsed ? item.label : ''"
        >
          <component :is="item.icon" class="menu-icon" />
          <span class="menu-text">{{ item.label }}</span>
          <a-badge
            v-if="item.badge && item.badge > 0"
            :count="item.badge"
            :max-count="99"
            class="menu-badge"
          />
        </router-link>
      </nav>

      <button class="collapse-btn" @click="toggleCollapse" :title="collapsed ? '展开侧边栏' : '折叠侧边栏'">
        <icon-right v-if="collapsed" />
        <icon-left v-else />
      </button>
    </aside>

    <main class="main-content">
      <header class="content-header">
        <a-breadcrumb v-if="breadcrumbs.length > 0">
          <a-breadcrumb-item v-for="(crumb, index) in breadcrumbs" :key="index">
            {{ crumb }}
          </a-breadcrumb-item>
        </a-breadcrumb>
      </header>

      <div class="content-body">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <div
      v-if="collapsed && showMobileOverlay"
      class="mobile-overlay"
      @click="toggleCollapse"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  IconDashboard,
  IconStorage,
  IconUserGroup,
  IconFile,
  IconList,
  IconStar,
  IconCheckCircle,
  IconSettings,
  IconMenu,
  IconRight,
  IconLeft
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const collapsed = ref(false)
const showMobileOverlay = ref(false)

const pendingCounts = ref({
  items: 12,
  reviews: 8,
  circle: 5,
})

const menuItems = computed(() => [
  {
    label: '工作台',
    path: '/ops/dashboard',
    icon: IconDashboard,
  },
  {
    label: '商品管理',
    path: '/ops/vendor',
    icon: IconStorage,
  },
  {
    label: '用户管理',
    path: '/ops/user-manage',
    icon: IconUserGroup,
  },
  {
    label: '订单管理',
    path: '/ops/orders',
    icon: IconFile,
  },
  {
    label: '买家管理',
    path: '/ops/buyer-manage',
    icon: IconList,
  },
  {
    label: '评价管理',
    path: '/ops/reviews',
    icon: IconStar,
  },
  {
    label: '审批工作台',
    path: '/ops/review',
    icon: IconCheckCircle,
    badge: pendingCounts.value.items + pendingCounts.value.reviews + pendingCounts.value.circle,
  },
  {
    label: '设置',
    path: '#',
    icon: IconSettings,
  },
])

const breadcrumbs = computed(() => {
  const crumbs = []
  const matched = route.matched

  if (matched.length > 0) {
    matched.forEach((record) => {
      if (record.meta?.title) {
        crumbs.push(record.meta.title)
      }
    })

    if (crumbs.length === 0 && route.name) {
      const nameMap = {
        opsDashboard: '运营中心',
        opsReviews: '商品审核',
        opsReviewDetail: '审核详情',
        opsOrders: '订单监控',
        opsVendor: '卖家管理',
        opsBuyer: '买家管理',
        opsUserManage: '用户管理',
        opsReview: '审批工作台',
        opsRoleManage: '角色管理',
      }
      if (nameMap[route.name]) {
        crumbs.push('运营中心', nameMap[route.name])
      }
    }
  }

  return crumbs
})

function isActiveRoute(path) {
  if (!path || path === '#') return false
  return route.path === path || route.path.startsWith(path + '/')
}

function toggleCollapse() {
  collapsed.value = !collapsed.value

  if (window.innerWidth < 1024) {
    showMobileOverlay.value = !showMobileOverlay.value
  }
}

function handleResize() {
  if (window.innerWidth < 1024 && !collapsed.value) {
    collapsed.value = true
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.ops-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f6f7;
}

.sidebar {
  width: 240px;
  min-height: 100vh;
  background: linear-gradient(180deg, #1D2129 0%, #2A2F36 100%);
  color: #FFFFFF;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: relative;
  z-index: 100;

  &.collapsed {
    width: 64px;

    .logo-text {
      display: none;
    }

    .menu-text {
      display: none;
    }

    .menu-badge {
      position: absolute;
      top: 4px;
      right: 4px;
    }

    .collapse-btn {
      left: 50%;
      transform: translateX(-50%);
    }
  }
}

.logo-area {
  padding: 0 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 16px;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #FFFFFF;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.85;
  }
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
  }
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 12px;
}

.menu-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.65);
  border-radius: 8px;
  text-decoration: none;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #FFFFFF;
  }

  &.active {
    background: #165DFF;
    color: #FFFFFF;
    border-left: 3px solid #FFFFFF;

    .menu-icon {
      color: #FFFFFF;
    }
  }

  .menu-icon {
    font-size: 20px;
    flex-shrink: 0;
    transition: color 0.2s;
  }

  .menu-text {
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    flex: 1;
  }

  .menu-badge {
    flex-shrink: 0;

    :deep(.arco-badge-number) {
      background-color: #F53F3F;
      font-size: 11px;
    }
  }
}

.collapse-btn {
  position: absolute;
  bottom: 20px;
  left: 20px;
  width: calc(100% - 40px);
  padding: 10px;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.65);
  font-size: 18px;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
    color: #FFFFFF;
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-x: hidden;
}

.content-header {
  background: #FFFFFF;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e6eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  :deep(.arco-breadcrumb) {
    font-size: 13px;
  }
}

.content-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.mobile-overlay {
  display: none;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media screen and (max-width: 1023px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 1000;
    box-shadow: 4px 0 16px rgba(0, 0, 0, 0.15);

    &:not(.collapsed) {
      width: 240px;
    }

    &.collapsed {
      width: 64px;
    }
  }

  .mobile-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
  }

  .main-content {
    margin-left: 0;
  }

  .content-body {
    padding: 16px;
  }
}

@media screen and (max-width: 767px) {
  .content-header {
    padding: 12px 16px;
  }

  .content-body {
    padding: 12px;
  }
}
</style>
