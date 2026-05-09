import { createRouter, createWebHistory, RouterView } from "vue-router";
import { h } from "vue";
import { useAuthStore } from "../stores/auth";
import { hasAnyRole } from "@campus/common/roles";

const LoginView = () =>
  import(/* webpackChunkName: "login" */ "../views/LoginView.vue");
const ForbiddenView = () =>
  import(/* webpackChunkName: "forbidden" */ "../views/ForbiddenView.vue");
const ShellHomeView = () =>
  import(/* webpackChunkName: "shell-home" */ "../views/ShellHomeView.vue");
const AboutUs = () =>
  import(/* webpackChunkName: "about-us" */ "../views/shared/AboutUs.vue");
const LayoutView = () =>
  import(/* webpackChunkName: "layout" */ "../views/container/Layout.vue");

const PortalLayout = () =>
  import(/* webpackChunkName: "portal-layout" */ "../views/portal/Layout.vue");
const PortalHome = () =>
  import(
    /* webpackChunkName: "portal-home" */ "../views/portal/home/index.vue"
  );
const BuyerItems = () =>
  import(
    /* webpackChunkName: "buyer-items" */ "../views/portal/buyer/Items.vue"
  );
const ItemDetail = () =>
  import(
    /* webpackChunkName: "portal-item-detail" */ "../views/portal/ItemDetail.vue"
  );
const SellerPublish = () =>
  import(
    /* webpackChunkName: "portal-publish" */ "../views/portal/seller/PublishItem.vue"
  );
const SellerItems = () =>
  import(
    /* webpackChunkName: "portal-seller-items" */ "../views/portal/seller/MyItems.vue"
  );
const Orders = () =>
  import(
    /* webpackChunkName: "portal-orders" */ "../views/portal/orders/index.vue"
  );
const OrderConfirm = () =>
  import(
    /* webpackChunkName: "portal-order-confirm" */ "../views/portal/orders/OrderConfirm.vue"
  );
const ProfileView = () =>
  import(
    /* webpackChunkName: "portal-profile" */ "../views/portal/Profile.vue"
  );

const OpsLayout = () =>
  import(/* webpackChunkName: "ops-layout" */ "../views/ops/Layout.vue");
const OpsDashboard = () =>
  import(
    /* webpackChunkName: "ops-dashboard" */ "../views/ops/dashboard/EnhancedDashboard.vue"
  );
const OpsReview = () =>
  import(
    /* webpackChunkName: "ops-reviews" */ "../views/ops/reviews/index.vue"
  );
const OpsApprovalWorkspace = () =>
  import(
    /* webpackChunkName: "ops-approval-workspace" */ "../views/ops/review/ApprovalWorkspace.vue"
  );
const OpsOrders = () =>
  import(/* webpackChunkName: "ops-orders" */ "../views/ops/orders/index.vue");
const OpsVendor = () =>
  import(/* webpackChunkName: "ops-vendor" */ "../views/ops/VendorManage.vue");
const OpsBuyer = () =>
  import(/* webpackChunkName: "ops-buyers" */ "../views/ops/BuyerManage.vue");

const RouteView = {
  name: "RouteView",
  render() {
    return h(RouterView);
  },
};

const routes = [
  {
    path: "/",
    redirect: "/portal/home",
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
  },
  {
    path: "/forbidden",
    name: "forbidden",
    component: ForbiddenView,
  },

  // ========== 门户路由 (独立Layout - 白色导航栏) ==========
  {
    path: "/portal",
    name: "portalRoot",
    component: PortalLayout,
    redirect: "/portal/home",
    children: [
      {
        path: "home",
        name: "portalHome",
        component: PortalHome,
        meta: { title: "校园二手交易平台" },
      },
      {
        path: "buyer/home",
        redirect: "/portal/home",
        meta: { roles: ["BUYER"] },
      },
      {
        path: "buyer/items",
        name: "buyerItems",
        component: BuyerItems,
        meta: { title: "全部商品" },
      },
      {
        path: "buyer/orders",
        component: Orders,
        meta: { roles: ["BUYER"] },
      },
      {
        path: "item/:id",
        name: "itemDetail",
        component: ItemDetail,
      },
      {
        path: "orders/confirm/:id",
        component: OrderConfirm,
        meta: { roles: ["BUYER"] },
      },
      {
        path: "seller/publish",
        component: SellerPublish,
        meta: { roles: ["SELLER"] },
      },
      {
        path: "seller/items",
        component: SellerItems,
        meta: { roles: ["SELLER"] },
      },
      {
        path: "seller/orders",
        component: Orders,
        meta: { roles: ["SELLER"] },
      },
      {
        path: "orders",
        component: Orders,
        meta: { roles: ["BUYER", "SELLER"] },
      },
      {
        path: "addresses",
        component: () => import("../views/portal/AddressList.vue"),
        meta: { roles: ["BUYER", "SELLER"] },
      },
      {
        path: "profile",
        name: "portalProfile",
        component: ProfileView,
        meta: { title: "个人中心" },
      },
      {
        path: "cart",
        component: () => import("../views/portal/Cart.vue"),
        meta: { roles: ["BUYER"] },
      },
      {
        path: "favorites",
        component: () => import("../views/portal/Favorites.vue"),
        meta: { roles: ["BUYER"] },
      },
      {
        path: "my-reviews",
        name: "portalMyReviews",
        component: () => import("../views/portal/MyReviews.vue"),
        meta: { title: "我的评价", roles: ["BUYER"] },
      },
      {
        path: "review/:orderId",
        component: () => import("../views/portal/ReviewSubmit.vue"),
        meta: { roles: ["BUYER"] },
      },
      {
        path: "seller/stats",
        component: () => import("../views/portal/SellerStats.vue"),
        meta: { roles: ["SELLER"] },
      },
      // ========== 校园圈子路由 ==========
      {
        path: "circle",
        name: "circleHome",
        component: () =>
          import(
            /* webpackChunkName: "circle-home" */ "../views/portal/circle/CircleHome.vue"
          ),
        meta: { title: "校园圈子" },
      },
      {
        path: "circle/publish",
        component: () =>
          import(
            /* webpackChunkName: "circle-publish" */ "../views/portal/circle/CirclePublish.vue"
          ),
        meta: { requiresAuth: true, title: "发布动态" },
      },
      {
        path: "circle/:id",
        name: "circleDetail",
        component: () =>
          import(
            /* webpackChunkName: "circle-detail" */ "../views/portal/circle/CircleDetail.vue"
          ),
        meta: { title: "帖子详情" },
      },
      // ========== 消息中心路由 ==========
      {
        path: "messages",
        name: "messageList",
        component: () =>
          import(
            /* webpackChunkName: "message-list" */ "../views/portal/MessageList.vue"
          ),
        meta: { roles: ["BUYER", "SELLER"], title: "消息中心" },
      },
      // ========== 关于我们路由 ==========
      {
        path: "about",
        name: "about",
        component: AboutUs,
        meta: { title: "关于我们" },
      },
    ],
  },

  // ========== 运营后台路由 (独立Layout - 深色侧边栏) ==========
  {
    path: "/ops",
    name: "opsRoot",
    component: OpsLayout,
    redirect: "/ops/dashboard",
    children: [
      {
        path: "dashboard",
        component: OpsDashboard,
        meta: { roles: ["OPS"], title: "工作台" },
      },
      // 用户管理
      {
        path: "users",
        component: RouteView,
        meta: { roles: ["OPS"], title: "用户管理" },
        redirect: "/ops/users/vendor-manage",
        children: [
          {
            path: "vendor-manage",
            component: () => import("../views/ops/VendorManage.vue"),
            meta: { roles: ["OPS"], title: "卖家管理" },
          },
          {
            path: "buyer-manage",
            component: () => import("../views/ops/BuyerManage.vue"),
            meta: { roles: ["OPS"], title: "买家管理" },
          },
          {
            path: "user-manage",
            component: () => import("../views/ops/UserManage.vue"),
            meta: { roles: ["OPS"], title: "用户管理" },
          },
        ],
      },
      // 订单管理
      {
        path: "orders",
        component: RouteView,
        meta: { roles: ["OPS"], title: "订单管理" },
        redirect: "/ops/orders/list",
        children: [
          {
            path: "list",
            component: OpsOrders,
            meta: { roles: ["OPS"], title: "订单管理" },
          },
          {
            path: "review",
            component: () => import("../views/ops/orders/OrderReview.vue"),
            meta: { roles: ["OPS"], title: "订单审核" },
          },
        ],
      },
      // 商品管理
      {
        path: "items",
        component: RouteView,
        meta: { roles: ["OPS"], title: "商品管理" },
        redirect: "/ops/items/manage",
        children: [
          {
            path: "manage",
            component: () => import("../views/ops/items/ItemManage.vue"),
            meta: { roles: ["OPS"], title: "商品管理" },
          },
          {
            path: "review",
            component: OpsReview,
            meta: { roles: ["OPS"], title: "商品审核" },
          },
        ],
      },
      // 圈子管理
      {
        path: "circles",
        component: RouteView,
        meta: { roles: ["OPS"], title: "圈子管理" },
        redirect: "/ops/circles/manage",
        children: [
          {
            path: "manage",
            component: () => import("../views/ops/circle/CircleManage.vue"),
            meta: { roles: ["OPS"], title: "圈子管理" },
          },
          {
            path: "review",
            component: () => import("../views/ops/circle/CircleReview.vue"),
            meta: { roles: ["OPS"], title: "圈子审核" },
          },
        ],
      },
      // 评价管理
      {
        path: "reviews",
        component: RouteView,
        meta: { roles: ["OPS"], title: "评价管理" },
        redirect: "/ops/reviews/manage",
        children: [
          {
            path: "manage",
            component: () => import("../views/ops/review-manage/ReviewManage.vue"),
            meta: { roles: ["OPS"], title: "评价管理" },
          },
          {
            path: "audit",
            component: () => import("../views/ops/review-manage/ReviewAudit.vue"),
            meta: { roles: ["OPS"], title: "评价审核" },
          },
        ],
      },
      // 消息
      {
        path: "messages",
        component: RouteView,
        meta: { roles: ["OPS"], title: "消息" },
        redirect: "/ops/messages/center",
        children: [
          {
            path: "center",
            component: () => import("../views/ops/messages/MessageCenter.vue"),
            meta: { roles: ["OPS"], title: "消息中心" },
          },
        ],
      },
      // 兼容旧路由
      { path: "review", redirect: "/ops/items/review" },
      { path: "item-review", redirect: "/ops/items/review" },
      { path: "vendor", redirect: "/ops/users/vendor-manage" },
      { path: "vendor-manage", redirect: "/ops/users/vendor-manage" },
      { path: "buyer-manage", redirect: "/ops/users/buyer-manage" },
      { path: "user-manage", redirect: "/ops/users/user-manage" },
      { path: "order-review", redirect: "/ops/orders/review" },
      { path: "circle-manage", redirect: "/ops/circles/manage" },
      { path: "circle-review", redirect: "/ops/circles/review" },
      { path: "review-manage", redirect: "/ops/reviews/manage" },
      { path: "review-audit", redirect: "/ops/reviews/audit" },
    ],
  },

  // ========== 其他页面 ==========
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const whiteList = ["/login", "/forbidden"];

router.beforeEach(async (to) => {
  const authStore = useAuthStore();

  if (!authStore.user && !authStore.token) {
    authStore.hydrate();
  }

  console.log('[Router Guard] 目标路径:', to.path);
  console.log('[Router Guard] 是否登录:', authStore.isLoggedIn);
  console.log('[Router Guard] 用户角色:', JSON.stringify(authStore.roles));
  console.log('[Router Guard] 需要的角色:', to.meta.roles ? JSON.stringify(to.meta.roles) : '无');

  if (!authStore.isLoggedIn) {
    if (whiteList.includes(to.path)) {
      return true;
    }
    const requiresAuth = to.matched.some(
      (record) => record.meta.requiresAuth || record.meta.roles,
    );
    if (requiresAuth) {
      return { name: "login", query: { redirect: to.fullPath } };
    }
    return true;
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRoles = Array.isArray(authStore.roles) ? authStore.roles : [];
    const requiredRoles = Array.isArray(to.meta.roles) ? to.meta.roles : [];
    
    console.log('[Router Guard] 角色检查 - 用户角色:', userRoles);
    console.log('[Router Guard] 角色检查 - 需要角色:', requiredRoles);
    
    if (!hasAnyRole(userRoles, requiredRoles)) {
      console.warn('[Router Guard] ❌ 角色不匹配！跳转到 /forbidden');
      console.warn('[Router Guard] 用户信息:', authStore.user);
      
      return { name: "forbidden" };
    }
    console.log('[Router Guard] ✅ 角色匹配通过');
  }

  return true;
});

router.afterEach((to) => {
  document.title = to.meta?.title || "CampusTrade";
});

export default router;
