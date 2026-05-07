import http from "../core/http";

export async function getStatistics() {
  return await http.get("/ops/stats");
}

export async function getReviewQueue(params = {}) {
  return await http.get("/ops/reviews/queue", { params });
}

export async function reviewItem(id, data) {
  return await http.post(`/ops/reviews/${id}`, data);
}

export async function approveItem(id, data) {
  return await http.post(`/ops/reviews/${id}/approve`, data);
}

export async function rejectItem(id, data) {
  return await http.post(`/ops/reviews/${id}/reject`, data);
}

export async function getOpsOrders(params = {}) {
  return await http.get("/ops/orders", { params });
}

export async function getOpsReviewDetail(id) {
  return await http.get(`/ops/reviews/${id}`);
}

export async function getUsers(params = {}) {
  return await http.get("/ops/users", { params });
}

export async function getUserDetail(id) {
  return await http.get(`/ops/users/${id}`);
}

export async function updateUserRole(id, role) {
  return await http.put(`/ops/users/${id}/role`, { role });
}

export async function updateUserStatus(id, status) {
  return await http.put(`/ops/users/${id}/status`, { status });
}

export async function getSellers(params = {}) {
  return await http.get("/ops/sellers", { params });
}

export async function getBuyers(params = {}) {
  return await http.get("/ops/buyers", { params });
}

export async function getBuyerDetail(id) {
  return await http.get(`/ops/buyers/${id}`);
}

export async function getVendors(params = {}) {
  return await http.get("/ops/vendors", { params });
}

export async function getVendorDetail(id) {
  return await http.get(`/ops/vendors/${id}`);
}

export async function getApps(params = {}) {
  return await http.get("/ops/apps", { params });
}

export async function createApp(data) {
  return await http.post("/ops/apps", data);
}

export async function updateApp(id, data) {
  return await http.put(`/ops/apps/${id}`, data);
}

export async function deleteApp(id) {
  return await http.delete(`/ops/apps/${id}`);
}

export async function getRoles(params = {}) {
  return await http.get("/ops/roles", { params });
}

export const getAllRoles = getRoles;

export async function createRole(data) {
  return await http.post("/ops/roles", data);
}

export async function updateRole(id, data) {
  return await http.put(`/ops/roles/${id}`, data);
}

export async function deleteRole(id) {
  return await http.delete(`/ops/roles/${id}`);
}

export async function updateRoleStatus(id, status) {
  return await http.put(`/ops/roles/${id}/status`, { status });
}

export async function getRoleResources(id) {
  return await http.get(`/ops/roles/${id}/resources`);
}

export async function saveRoleResources(id, resources) {
  return await http.put(`/ops/roles/${id}/resources`, { resources });
}