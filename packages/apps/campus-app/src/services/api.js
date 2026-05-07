import http from "./core/http";

export async function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  return await http.post("/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

export async function getItems(params = {}) {
  return await http.get("/items", { params });
}

export async function getItemDetail(id) {
  return await http.get(`/items/${id}`);
}

export async function publishItem(data) {
  return await http.post("/items", data);
}

export async function updateItem(id, data) {
  return await http.put(`/items/${id}`, data);
}

export async function offShelfItem(id) {
  return await http.post(`/items/${id}/off-shelf`);
}

export async function deleteItem(id) {
  return await http.delete(`/items/${id}`);
}

export async function getMyItems(params = {}) {
  return await http.get("/items/mine", { params });
}

export async function getSellerOverview() {
  return await http.get("/seller/overview");
}

export async function getSellerTrend(days = 7) {
  return await http.get("/seller/trend", { params: { days } });
}

export async function getSellerRanking(params = {}) {
  return await http.get("/seller/ranking", { params });
}

export async function getOrders(params = {}) {
  return await http.get("/orders", { params });
}

export async function getOrderDetail(id) {
  return await http.get(`/orders/${id}`);
}

export async function createOrder(data) {
  return await http.post("/orders", data);
}

export async function confirmOrder(id, data) {
  return await http.post(`/orders/${id}/confirm`, data);
}

export async function cancelOrder(id) {
  return await http.post(`/orders/${id}/cancel`);
}

export async function getMyOrders(params = {}) {
  return await http.get("/orders/mine", { params });
}

export async function payOrder(id) {
  return await http.post(`/orders/${id}/pay`);
}

export async function shipOrder(id, data) {
  return await http.post(`/orders/${id}/ship`, data);
}

export async function requestRefund(id) {
  return await http.post(`/orders/${id}/refund`);
}

export async function approveRefund(id) {
  return await http.post(`/orders/${id}/refund/approve`);
}

export async function rejectRefund(id) {
  return await http.post(`/orders/${id}/refund/reject`);
}

export async function addToCart(itemId, quantity = 1) {
  return await http.post("/cart", { itemId, quantity });
}

export async function getCart() {
  return await http.get("/cart");
}

export const getCartList = getCart;

export async function updateCartItem(id, quantity) {
  return await http.put(`/cart/${id}`, { quantity });
}

export const updateCartQuantity = updateCartItem;

export async function updateCartSelected(id, selected) {
  return await http.put(`/cart/${id}/select`, { selected });
}

export async function deleteCartItem(id) {
  return await http.delete(`/cart/${id}`);
}

export const removeCartItem = deleteCartItem;

export async function addFavorite(itemId) {
  return await http.post(`/favorites/${itemId}`);
}

export async function removeFavorite(itemId) {
  return await http.delete(`/favorites/${itemId}`);
}

export async function checkFavorite(itemId) {
  return await http.get(`/favorites/check/${itemId}`);
}

export async function getFavorites(params = {}) {
  return await http.get("/favorites", { params });
}

export const getFavoriteList = getFavorites;

export async function getAddresses(params = {}) {
  return await http.get("/addresses", { params });
}

export const getAddressList = getAddresses;

export async function getDefaultAddress() {
  return await http.get("/addresses/default");
}

export async function setDefaultAddress(id) {
  return await http.put(`/addresses/${id}/default`);
}

export async function createAddress(data) {
  return await http.post("/addresses", data);
}

export async function updateAddress(id, data) {
  return await http.put(`/addresses/${id}`, data);
}

export async function deleteAddress(id) {
  return await http.delete(`/addresses/${id}`);
}

export async function submitReview(orderId, data) {
  return await http.post("/reviews", { orderId, ...data });
}

export async function createReview(orderId, data) {
  return await http.post("/reviews", { orderId, ...data });
}

export async function getReview(orderId) {
  return await http.get(`/reviews/${orderId}`);
}

export async function register(data) {
  return await http.post("/auth/register", data);
}

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

export default http;
