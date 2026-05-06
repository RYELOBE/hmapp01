import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/user/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/product/list',
    name: 'ProductList',
    component: () => import('@/views/user/ProductList.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/user/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/product/publish',
    name: 'PublishProduct',
    component: () => import('@/views/user/PublishProduct.vue'),
    meta: { title: '发布商品', requireAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/user/Cart.vue'),
    meta: { title: '购物车', requireAuth: true }
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('@/views/user/Order.vue'),
    meta: { title: '我的订单', requireAuth: true }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/UserProfile.vue'),
    meta: { title: '个人中心', requireAuth: true }
  },
  {
    path: '/user/favorites',
    name: 'UserFavorites',
    component: () => import('@/views/user/UserFavorites.vue'),
    meta: { title: '我的收藏', requireAuth: true }
  },
  {
    path: '/ai/assistant',
    name: 'AIAssistant',
    component: () => import('@/views/ai/AIAssistant.vue'),
    meta: { title: 'AI助手' }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'products',
        name: 'ProductManagement',
        component: () => import('@/views/admin/ProductManagement.vue'),
        meta: { title: '商品管理', requiresAdmin: true }
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('@/views/admin/OrderManagement.vue'),
        meta: { title: '订单管理', requiresAdmin: true }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: { title: '分类管理', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title 
    ? `${to.meta.title} - 校园二手交易平台` 
    : '校园二手交易平台'
  
  const userStore = useUserStore()
  
  if (to.meta.requireAuth && !userStore.isLoggedIn()) {
    next('/login')
  } else if (to.meta.requiresAdmin) {
    if (!userStore.isLoggedIn() || userStore.userInfo?.role !== 'admin') {
      next('/home')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
