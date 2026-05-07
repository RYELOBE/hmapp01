import { opsHttp as http } from "../../services/http";

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
