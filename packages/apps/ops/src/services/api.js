import { opsHttp as http } from "commonprovide/http";

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

export function getVendorDetail(vendorId) {
  return http.get(`/ops/vendors/${vendorId}`);
}

// ── 需方管理 API ──────────────────────────────
export function getBuyers(params = {}) {
  return http.get("/ops/buyers", { params });
}

export function getBuyerDetail(buyerId) {
  return http.get(`/ops/buyers/${buyerId}`);
}

// ── 用户管理 API ──────────────────────────────
export function getUsers(params = {}) {
  return http.get("/ops/users", { params });
}

export function getUserDetail(userId) {
  return http.get(`/ops/users/${userId}`);
}

export function updateUserStatus(userId, status) {
  return http.put(`/ops/users/${userId}/status`, { status });
}

export function updateUserRole(userId, role) {
  return http.put(`/ops/users/${userId}/roles`, { roles: [role] });
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

export function createRole(data) {
  return http.post("/resource/roles", data);
}

export function updateRole(roleCode, data) {
  return http.put(`/resource/roles/${roleCode}`, data);
}

export function deleteRole(roleCode) {
  return http.delete(`/resource/roles/${roleCode}`);
}

export function updateRoleStatus(roleCode, status) {
  return http.put(`/resource/roles/${roleCode}/status`, { status });
}

// ── 资源管理 API ──────────────────────────────
export function getResourceMenus(params = {}) {
  return http.get("/resource/menus", { params });
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

export function getResourceDetail(id) {
  return http.get(`/resource/menus/${id}`);
}

export function updateResourceMenu(id, data) {
  return http.put(`/resource/menus/${id}`, data);
}

export function getResourceFunctions(menuId) {
  return http.get(`/resource/menus/${menuId}/functions`);
}

export function saveResourceFunction(data) {
  return http.post("/resource/functions", data);
}

export function deleteResourceFunction(id) {
  return http.delete(`/resource/functions/${id}`);
}

export function updateResourceFunction(id, data) {
  return http.put(`/resource/functions/${id}`, data);
}

// ── 导航菜单 API ──────────────────────────────
export function getMyNavigation() {
  return http.get("/resource/my-menus");
}
