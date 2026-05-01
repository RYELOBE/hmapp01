import { http } from "commonprovide/http";

// ── 审核 API ──────────────────────────────
export function getReviewQueue(params = {}) {
  return http.get("/ops/reviews", { params });
}

export function approveItem(itemId) {
  return http.post(`/ops/reviews/${itemId}/approve`);
}

export function rejectItem(itemId, reason) {
  return http.post(`/ops/reviews/${itemId}/reject`, { reason });
}

export function getItemDetail(itemId) {
  return http.get(`/items/${itemId}`);
}

// ── 运营统计 API ──────────────────────────────
export function getStatistics() {
  return http.get("/ops/statistics");
}

// ── 订单监控 API ──────────────────────────────
export function getOpsOrders(params = {}) {
  return http.get("/ops/orders", { params });
}

// ── 供方管理 API ──────────────────────────────
export function getVendors(params = {}) {
  return http.get("/ops/vendors", { params });
}

// ── 需方管理 API ──────────────────────────────
export function getBuyers(params = {}) {
  return http.get("/ops/buyers", { params });
}

// ── 子应用管理 API ──────────────────────────────
export function getAppRegisters() {
  return http.get("/frame/registers");
}

export function saveAppRegister(data) {
  return http.post("/frame/registers", data);
}

export function deleteAppRegister(appCode) {
  return http.delete(`/frame/registers/${appCode}`);
}

// ── 门户配置 API ──────────────────────────────
export function getPortalConfigs() {
  return http.get("/portal/configs");
}

export function getPortalConfig(portalCode) {
  return http.get(`/portal/configs/${portalCode}`);
}

export function savePortalConfig(data) {
  return http.post("/portal/configs", data);
}

export function deletePortalConfig(portalCode) {
  return http.delete(`/portal/configs/${portalCode}`);
}

// ── 资源菜单 API ──────────────────────────────
export function getResourceMenus() {
  return http.get("/resource/menus");
}

export function getResourceMenuTree() {
  return http.get("/resource/menu-tree");
}

export function saveResourceMenu(data) {
  return http.post("/resource/menus", data);
}

export function deleteResourceMenu(id) {
  return http.delete(`/resource/menus/${id}`);
}

// ── 角色管理 API ──────────────────────────────
export function getAllRoles() {
  return http.get("/resource/roles");
}

export function getRoleResources(roleCode) {
  return http.get(`/resource/roles/${roleCode}/resources`);
}

export function saveRoleResources(roleCode, resourceIds) {
  return http.post(`/resource/roles/${roleCode}/resources`, { resourceIds });
}
