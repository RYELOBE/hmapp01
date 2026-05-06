import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { hasAnyRole } from "@campus/common/roles";

const LoginView = () => import(/* webpackChunkName: "login" */ "../views/LoginView.vue");
const ForbiddenView = () => import(/* webpackChunkName: "forbidden" */ "../views/ForbiddenView.vue");
const ShellHomeView = () => import(/* webpackChunkName: "shell-home" */ "../views/ShellHomeView.vue");
const LayoutView = () => import(/* webpackChunkName: "layout" */ "../views/container/Layout.vue");

const PortalLayout = () => import(/* webpackChunkName: "portal-layout" */ "../views/portal/Layout.vue");
const PortalHome = () => import(/* webpackChunkName: "portal-home" */ "../views/portal/home/index.vue");
const ItemDetail = () => import(/* webpackChunkName: "portal-item-detail" */ "../views/portal/ItemDetail.vue");
const SellerPublish = () => import(/* webpackChunkName: "portal-publish" */ "../views/portal/seller/PublishItem.vue");
const SellerItems = () => import(/* webpackChunkName: "portal-seller-items" */ "../views/portal/seller/MyItems.vue");
const Orders = () => import(/* webpackChunkName: "portal-orders" */ "../views/portal/orders/index.vue");
const OrderConfirm = () => import(/* webpackChunkName: "portal-order-confirm" */ "../views/portal/orders/OrderConfirm.vue");

const OpsLayout = () => import(/* webpackChunkName: "ops-layout" */ "../views/ops/Layout.vue");
const OpsDashboard = () => import(/* webpackChunkName: "ops-dashboard" */ "../views/ops/dashboard/index.vue");
const OpsReviews = () => import(/* webpackChunkName: "ops-reviews" */ "../views/ops/reviews/index.vue");
const OpsReviewDetail = () => import(/* webpackChunkName: "ops-review-detail" */ "../views/ops/ReviewDetail.vue");
const OpsOrders = () => import(/* webpackChunkName: "ops-orders" */ "../views/ops/orders/index.vue");
const OpsVendor = () => import(/* webpackChunkName: "ops-vendor" */ "../views/ops/vendor/index.vue");
const OpsBuyer = () => import(/* webpackChunkName: "ops-buyers" */ "../views/ops/buyer/index.vue");

const routes = [
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
  {
    path: "/",
    name: "frameRoot",
    component: LayoutView,
    children: [
      {
        path: "",
        name: "home",
        component: ShellHomeView,
        meta: { requiresAuth: true },
      },
      {
        path: "portal",
        name: "portalRoot",
        component: PortalLayout,
        children: [
          { path: "home", component: PortalHome, meta: { requiresAuth: true } },
          { path: "buyer/home", redirect: "/portal/home", meta: { roles: ["BUYER"] } },
          { path: "buyer/items", component: PortalHome, meta: { roles: ["BUYER"], title: "淘好物" } },
          { path: "buyer/orders", component: Orders, meta: { roles: ["BUYER"] } },
          { path: "item/:id", component: ItemDetail, meta: { requiresAuth: true } },
          { path: "orders/confirm/:id", component: OrderConfirm, meta: { roles: ["BUYER"] } },
          { path: "seller/publish", component: SellerPublish, meta: { roles: ["SELLER"] } },
          { path: "seller/items", component: SellerItems, meta: { roles: ["SELLER"] } },
          { path: "seller/orders", component: Orders, meta: { roles: ["SELLER"] } },
          { path: "orders", component: Orders, meta: { roles: ["BUYER", "SELLER"] } },
          { path: "addresses", component: () => import("../views/portal/AddressList.vue"), meta: { roles: ["BUYER", "SELLER"] } },
          { path: "cart", component: () => import("../views/portal/Cart.vue"), meta: { roles: ["BUYER"] } },
          { path: "favorites", component: () => import("../views/portal/Favorites.vue"), meta: { roles: ["BUYER"] } },
          { path: "review/:orderId", component: () => import("../views/portal/ReviewSubmit.vue"), meta: { roles: ["BUYER"] } },
          { path: "seller/stats", component: () => import("../views/portal/SellerStats.vue"), meta: { roles: ["SELLER"] } },
        ],
      },
      {
        path: "ops",
        name: "opsRoot",
        component: OpsLayout,
        children: [
          { path: "dashboard", component: OpsDashboard, meta: { roles: ["OPS"] } },
          { path: "reviews", component: OpsReviews, meta: { roles: ["OPS"] } },
          { path: "reviews/:id", component: OpsReviewDetail, meta: { roles: ["OPS"] } },
          { path: "orders", component: OpsOrders, meta: { roles: ["OPS"] } },
          { path: "vendor", component: OpsVendor, meta: { roles: ["OPS"] } },
          { path: "buyer", component: OpsBuyer, meta: { roles: ["OPS"] } },
          { path: "user-manage", component: () => import("../views/ops/UserManage.vue"), meta: { roles: ["OPS"] } },
          { path: "vendor-manage", component: () => import("../views/ops/VendorManage.vue"), meta: { roles: ["OPS"] } },
          { path: "buyer-manage", component: () => import("../views/ops/BuyerManage.vue"), meta: { roles: ["OPS"] } },
          { path: "app-register", component: () => import("../views/ops/AppRegisterView.vue"), meta: { roles: ["OPS"] } },
          { path: "portal-design", component: () => import("../views/ops/PortalDesign.vue"), meta: { roles: ["OPS"] } },
          { path: "role-manage", component: () => import("../views/ops/RoleManage.vue"), meta: { roles: ["OPS"] } },
          { path: "route-manage", component: () => import("../views/ops/RouteManage.vue"), meta: { roles: ["OPS"] } },
          { path: "resource-manage", component: () => import("../views/ops/ResourceManage.vue"), meta: { roles: ["OPS"] } },
        ],
      },
    ],
  },
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

  if (!authStore.isLoggedIn) {
    if (whiteList.includes(to.path)) {
      return true;
    }
    const requiresAuth = to.matched.some(record =>
      record.meta.requiresAuth || record.meta.roles
    );
    if (requiresAuth) {
      return { name: "login", query: { redirect: to.fullPath } };
    }
    return true;
  }

  if (to.meta.roles && !hasAnyRole(authStore.roles, to.meta.roles)) {
    return { name: "forbidden" };
  }

  return true;
});

router.afterEach((to) => {
  document.title = to.meta?.title || 'CampusTrade';
});

export default router;