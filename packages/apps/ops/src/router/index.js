import { createRouter, createWebHistory } from "vue-router";
import { getOpsCurrentUser as getCurrentUser } from "commonprovide/auth-sdk";
import { hasAnyRole } from "@campus/common/roles";

const Layout = () => import(/* webpackChunkName: "ops-layout" */ "../views/Layout.vue");
const Login = () => import(/* webpackChunkName: "ops-login" */ "../views/Login.vue");
const ForbiddenView = () => import(/* webpackChunkName: "ops-forbidden" */ "../views/ForbiddenView.vue");
const Dashboard = () => import(/* webpackChunkName: "ops-dashboard" */ "../views/dashboard/index.vue");
const Reviews = () => import(/* webpackChunkName: "ops-reviews" */ "../views/reviews/index.vue");
const ReviewDetail = () => import(/* webpackChunkName: "ops-review-detail" */ "../views/ReviewDetail.vue");
const Orders = () => import(/* webpackChunkName: "ops-orders" */ "../views/orders/index.vue");
const Vendor = () => import(/* webpackChunkName: "ops-vendor" */ "../views/vendor/index.vue");
const Buyer = () => import(/* webpackChunkName: "ops-buyers" */ "../views/buyer/index.vue");
const AppRegisterView = () => import(/* webpackChunkName: "ops-apps" */ "../views/AppRegisterView.vue");
const PortalDesign = () => import(/* webpackChunkName: "ops-portal-design" */ "../views/PortalDesign.vue");
const RoleManage = () => import(/* webpackChunkName: "ops-roles" */ "../views/RoleManage.vue");
const RouteManage = () => import(/* webpackChunkName: "ops-routes" */ "../views/RouteManage.vue");
const ResourceManage = () => import(/* webpackChunkName: "ops-resources" */ "../views/ResourceManage.vue");
const UserManage = () => import(/* webpackChunkName: "ops-users" */ "../views/UserManage.vue");
const VendorManage = () => import(/* webpackChunkName: "ops-vendors" */ "../views/VendorManage.vue");
const BuyerManage = () => import(/* webpackChunkName: "ops-buyer-manage" */ "../views/BuyerManage.vue");

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
    ],
  },
];

export function createOpsRouter(poweredByQiankun = false) {
  const router = createRouter({
    history: createWebHistory(poweredByQiankun ? "/ops" : "/"),
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
