import { createRouter, createWebHistory } from "vue-router";
import Layout from "../views/Layout.vue";
import Login from "../views/Login.vue";
import ForbiddenView from "../views/ForbiddenView.vue";
import BuyerHome from "../views/BuyerHome.vue";
import ItemList from "../views/ItemList.vue";
import ItemDetail from "../views/ItemDetail.vue";
import SellerCenter from "../views/SellerCenter.vue";
import PublishItem from "../views/PublishItem.vue";
import MyItems from "../views/MyItems.vue";
import MyOrders from "../views/MyOrders.vue";
import { getCurrentUser } from "commonprovide/auth-sdk";
import { hasAnyRole } from "@campus/common/roles";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: Login, meta: { hideMenu: true } },
  { path: "/forbidden", component: ForbiddenView, meta: { hideMenu: true } },
  {
    path: "/",
    component: Layout,
    children: [
      { path: "buyer/home", component: BuyerHome, meta: { roles: ["BUYER"] } },
      { path: "buyer/items", component: ItemList, meta: { roles: ["BUYER"] } },
      { path: "buyer/items/:id", component: ItemDetail, meta: { roles: ["BUYER"] } },
      { path: "seller/center", component: SellerCenter, meta: { roles: ["SELLER"] } },
      { path: "seller/publish", component: PublishItem, meta: { roles: ["SELLER"] } },
      { path: "seller/items", component: MyItems, meta: { roles: ["SELLER"] } },
      { path: "orders", component: MyOrders, meta: { roles: ["BUYER", "SELLER"] } }
    ]
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
      if (user) {
        return "/buyer/home";
      }
      return true;
    }
    if (!user) {
      return "/login";
    }
    const roles = user.roles || [];
    if (to.meta.roles && !hasAnyRole(roles, to.meta.roles)) {
      return "/forbidden";
    }
    return true;
  });
  return router;
}
