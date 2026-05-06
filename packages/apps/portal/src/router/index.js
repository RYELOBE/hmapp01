import { createRouter, createWebHistory } from "vue-router";
import { getCurrentUser } from "commonprovide/auth-sdk";
import { hasAnyRole } from "@campus/common/roles";

const Layout = () => import(/* webpackChunkName: "portal-layout" */ "../views/Layout.vue");
const Login = () => import(/* webpackChunkName: "portal-login" */ "../views/Login.vue");
const ForbiddenView = () => import(/* webpackChunkName: "portal-forbidden" */ "../views/ForbiddenView.vue");
const Home = () => import(/* webpackChunkName: "portal-home" */ "../views/home/index.vue");
const ItemDetail = () => import(/* webpackChunkName: "portal-item-detail" */ "../views/ItemDetail.vue");
const SellerPublish = () => import(/* webpackChunkName: "portal-publish" */ "../views/seller/PublishItem.vue");
const SellerItems = () => import(/* webpackChunkName: "portal-seller-items" */ "../views/seller/MyItems.vue");
const Orders = () => import(/* webpackChunkName: "portal-orders" */ "../views/orders/index.vue");
const OrderConfirm = () => import(/* webpackChunkName: "portal-order-confirm" */ "../views/orders/OrderConfirm.vue");

const routes = [
  { path: "/", redirect: "/home" },
  { path: "/login", component: Login, meta: { hideMenu: true } },
  { path: "/register", name: "Register", component: () => import(/* webpackChunkName: "portal-register" */ "../views/Register.vue"), meta: { hideMenu: true } },
  { path: "/forbidden", component: ForbiddenView, meta: { hideMenu: true } },
  {
    path: "/",
    component: Layout,
    children: [
      { path: "home", component: Home },
      { path: "buyer/home", redirect: "/home", meta: { roles: ["BUYER"] } },
      { path: "buyer/items", component: Home, meta: { roles: ["BUYER"], title: "淘好物" } },
      { path: "buyer/orders", component: Orders, meta: { roles: ["BUYER"] } },
      { path: "item/:id", component: ItemDetail },
      { path: "orders/confirm/:id", component: OrderConfirm, meta: { roles: ["BUYER"] } },
      { path: "seller/publish", component: SellerPublish, meta: { roles: ["SELLER"] } },
      { path: "seller/items", component: SellerItems, meta: { roles: ["SELLER"] } },
      { path: "seller/orders", component: Orders, meta: { roles: ["SELLER"] } },
      { path: "orders", component: Orders, meta: { roles: ["BUYER", "SELLER"] } },
      { path: "addresses", component: () => import(/* webpackChunkName: "portal-addresses" */ "../views/AddressList.vue"), meta: { roles: ["BUYER", "SELLER"] } },
  { path: "cart", component: () => import(/* webpackChunkName: "portal-cart" */ "../views/Cart.vue"), meta: { roles: ["BUYER"] } },
  { path: "favorites", component: () => import(/* webpackChunkName: "portal-favorites" */ "../views/Favorites.vue"), meta: { roles: ["BUYER"] } },
  { path: "review/:orderId", component: () => import(/* webpackChunkName: "portal-review" */ "../views/ReviewSubmit.vue"), meta: { roles: ["BUYER"] } },
  { path: "seller/stats", component: () => import(/* webpackChunkName: "portal-seller-stats" */ "../views/SellerStats.vue"), meta: { roles: ["SELLER"] } },
    ],
  },
];

export function createPortalRouter(poweredByQiankun = false) {
  const router = createRouter({
    history: createWebHistory(poweredByQiankun ? "/portal" : "/"),
    routes,
  });
  router.beforeEach((to) => {
    const user = getCurrentUser();
    if (to.path === "/login") {
      if (user) return "/home";
      return true;
    }
    if (!user) return "/login";
    const roles = user.roles || [];
    if (to.meta.roles && !hasAnyRole(roles, to.meta.roles)) {
      return "/forbidden";
    }
    return true;
  });
  return router;
}
