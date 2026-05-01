import { createRouter, createWebHistory } from "vue-router";
import Layout from "../views/Layout.vue";
import Login from "../views/Login.vue";
import ForbiddenView from "../views/ForbiddenView.vue";
import Home from "../views/home/index.vue";
import ItemDetail from "../views/ItemDetail.vue";
import SellerPublish from "../views/seller/PublishItem.vue";
import SellerItems from "../views/seller/MyItems.vue";
import Orders from "../views/orders/index.vue";
import OrderConfirm from "../views/orders/OrderConfirm.vue";
import { getCurrentUser } from "commonprovide/auth-sdk";
import { hasAnyRole } from "@campus/common/roles";

const routes = [
  { path: "/", redirect: "/home" },
  { path: "/login", component: Login, meta: { hideMenu: true } },
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
      { path: "addresses", component: () => import("../views/AddressList.vue"), meta: { roles: ["BUYER", "SELLER"] } },
      { path: "cart", component: () => import("../views/Cart.vue"), meta: { roles: ["BUYER"] } },
      { path: "favorites", component: () => import("../views/Favorites.vue"), meta: { roles: ["BUYER"] } },
      { path: "review/:orderId", component: () => import("../views/ReviewSubmit.vue"), meta: { roles: ["BUYER"] } },
      { path: "seller/stats", component: () => import("../views/SellerStats.vue"), meta: { roles: ["SELLER"] } },
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
