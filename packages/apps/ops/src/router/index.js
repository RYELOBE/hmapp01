import { createRouter, createWebHistory } from "vue-router";
import Layout from "../views/Layout.vue";
import Login from "../views/Login.vue";
import ForbiddenView from "../views/ForbiddenView.vue";
import OpsDashboard from "../views/OpsDashboard.vue";
import ReviewList from "../views/ReviewList.vue";
import ReviewDetail from "../views/ReviewDetail.vue";
import OrderMonitor from "../views/OrderMonitor.vue";
import AppRegisterView from "../views/AppRegisterView.vue";
import PortalDesign from "../views/PortalDesign.vue";
import RoleManage from "../views/RoleManage.vue";
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
      { path: "ops/dashboard", component: OpsDashboard, meta: { roles: ["OPS"] } },
      { path: "ops/reviews", component: ReviewList, meta: { roles: ["OPS"] } },
      { path: "ops/reviews/:id", component: ReviewDetail, meta: { roles: ["OPS"] } },
      { path: "ops/orders", component: OrderMonitor, meta: { roles: ["OPS"] } },
      { path: "ops/app-register", component: AppRegisterView, meta: { roles: ["OPS"] } },
      { path: "ops/portal-design", component: PortalDesign, meta: { roles: ["OPS"] } },
      { path: "ops/role-manage", component: RoleManage, meta: { roles: ["OPS"] } }
    ]
  },
];

export function createOpsRouter(poweredByQiankun = false) {
  const router = createRouter({
    history: createWebHistory(poweredByQiankun ? "/ops" : "/"),
    routes
  });
  router.beforeEach((to) => {
    const user = getCurrentUser();
    if (to.path === "/login") {
      if (user) {
        return "/ops/dashboard";
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
