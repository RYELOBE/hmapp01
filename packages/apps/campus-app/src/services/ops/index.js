import http from "../core/http";

// 后端响应: { code:200, data: { code:200, data: {...} } }
// core/http 拦截器返回 response.data，即 { code, data }
function unwrap(res) {
  if (res?.data?.data !== undefined) return res.data.data;
  if (res?.data !== undefined) return res.data;
  return res;
}

// ── 字典接口 ──────────────────────────
export async function getDictOptions() {
  const res = await http.get("/dict/options");
  return unwrap(res);
}

// ── 统计接口 ──────────────────────────
export async function getBriefStats() {
  return unwrap(await http.get("/ops/stats/brief"));
}

export async function getStatistics() {
  return unwrap(await http.get("/ops/statistics"));
}

// ── 商品审核 ──────────────────────────
export async function getReviewQueue(params = {}) {
  return unwrap(await http.post("/ops/reviews", params));
}

export async function approveItem(id, data) {
  return unwrap(await http.post(`/ops/reviews/${id}/approve`, data));
}

export async function rejectItem(id, data) {
  return unwrap(await http.post(`/ops/reviews/${id}/reject`, data));
}

// ── 商品管理（走 OPS 接口，不需要 SELLER 角色） ──────────────────────────
export async function getOpsItems(params = {}) {
  return unwrap(await http.post("/items/list", params));
}

export async function offlineItem(id) {
  return unwrap(await http.post(`/ops/items/${id}/off-shelf`));
}

export async function deleteItem(id) {
  return unwrap(await http.delete(`/ops/items/${id}`));
}

// ── 订单管理 ──────────────────────────
export async function getOpsOrders(params = {}) {
  return unwrap(await http.post("/ops/orders", params));
}

// ── 卖家管理 ──────────────────────────
export async function getVendors(params = {}) {
  return unwrap(await http.post("/ops/vendors", params));
}

export async function getVendorDetail(id) {
  return unwrap(await http.get(`/ops/vendors/${id}`));
}

// 卖家商品列表（分页）
export async function getVendorItems(vendorId, params = {}) {
  return unwrap(await http.post("/items/list", { ...params, sellerId: vendorId }));
}

// ── 买家管理 ──────────────────────────
export async function getBuyers(params = {}) {
  return unwrap(await http.post("/ops/buyers", params));
}

export async function getBuyerDetail(id) {
  return unwrap(await http.get(`/ops/buyers/${id}`));
}

// ── 用户管理 ──────────────────────────
export async function getUsers(params = {}) {
  return unwrap(await http.post("/ops/users", params));
}

export async function getUserDetail(id) {
  return unwrap(await http.get(`/ops/users/${id}`));
}

export async function updateUserRole(id, role) {
  return unwrap(await http.put(`/ops/users/${id}/roles`, { roles: [role] }));
}

export async function updateUserStatus(id, status) {
  return unwrap(await http.put(`/ops/users/${id}/status`, { status }));
}

// ── 待审核数量 ──────────────────────────
export async function getPendingCounts() {
  return unwrap(await http.get("/ops/pending-counts"));
}
