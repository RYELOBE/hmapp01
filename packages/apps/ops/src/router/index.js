import { createRouter, createWebHistory } from "vue-router";
import Layout from "../views/Layout.vue";
import Login from "../views/Login.vue";
import ForbiddenView from "../views/ForbiddenView.vue";
import Dashboard from "../views/dashboard/index.vue";
import Reviews from "../views/reviews/index.vue";
import ReviewDetail from "../views/ReviewDetail.vue";
import Orders from "../views/orders/index.vue";
import Vendor from "../views/vendor/index.vue";
import Buyer from "../views/buyer/index.vue";
import AppRegisterView from "../views/AppRegisterView.vue";
import PortalDesign from "../views/PortalDesign.vue";
import RoleManage from "../views/RoleManage.vue";
import RouteManage from "../views/RouteManage.vue";
import ResourceManage from "../views/ResourceManage.vue";
import UserManage from "../views/UserManage.vue";
import UserManage2 from "../views/UserManage2.vue";
import VendorManage from "../views/VendorManage.vue";
import BuyerManage from "../views/BuyerManage.vue";
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
      { path: "ops/dashboard", component: Dashboard, meta: { roles: ["OPS"] } },
      { path: "ops/reviews", component: Reviews, meta: { roles: ["OPS"] } },
      { path: "ops/reviews/:id", component: ReviewDetail, meta: { roles: ["OPS"] } },
      { path: "ops/orders", component: Orders, meta: { roles: ["OPS"] } },
      { path: "ops/vendor", component: Vendor, meta: { roles: ["OPS"] } },
      { path: "ops/buyer", component: Buyer, meta: { roles: ["OPS"] } },
      { path: "ops/user-manage", component: UserManage, meta: { roles: ["OPS"] } },
      { path: "ops/vendor-manage", component: VendorManage, meta: { roles: ["OPS"] } },
      { path: "ops/buyer-manage", component: BuyerManage, meta: { roles: ["OPS"] } },
      { path: "ops/app-register", component: AppRegisterView, meta: { roles: ["OPS"] } },
      { path: "ops/portal-design", component: PortalDesign, meta: { roles: ["OPS"] } },
      { path: "ops/role-manage", component: RoleManage, meta: { roles: ["OPS"] } },
      { path: "ops/route-manage", component: RouteManage, meta: { roles: ["OPS"] } },
      { path: "ops/resource-manage", component: ResourceManage, meta: { roles: ["OPS"] } },
      { path: "ops/user-manage-2", component: UserManage2, meta: { roles: ["OPS"] } },
    ],
  },
];

export function createOpsRouter(poweredByQiankun = false) {
  const router = createRouter({
    history: createWebHistory("/"),
    routes,
  });
  router.beforeEach((to) => {
    const user = getCurrentUser();
    if (to.path === "/login") {
      if (user) return "/ops/dashboard";
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
