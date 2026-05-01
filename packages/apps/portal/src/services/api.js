import { http } from "commonprovide/http";

// ── 商品接口 ──────────────────────────────
export function getItems(params = {}) {
  return http.get("/items", { params });
}

export function getItemDetail(id) {
  return http.get(`/items/${id}`);
}

export function publishItem(payload) {
  return http.post("/items", payload);
}

export function updateItem(id, payload) {
  return http.put(`/items/${id}`, payload);
}

export function deleteItem(id) {
  return http.delete(`/items/${id}`);
}

export function offShelfItem(id) {
  return http.post(`/items/${id}/off-shelf`);
}

export function getMyItems(params = {}) {
  return http.get("/items/mine", { params });
}

// ── 订单接口 ──────────────────────────────
export function getMyOrders(params = {}) {
  return http.get("/orders/mine", { params });
}

export function getOrderDetail(id) {
  return http.get(`/orders/${id}`);
}

export function createOrder(data) {
  return http.post("/orders", data);
}

export function payOrder(orderId) {
  return http.post(`/orders/${orderId}/pay`);
}

export function confirmOrder(orderId) {
  return http.post(`/orders/${orderId}/confirm`);
}

export function shipOrder(orderId, data) {
  return http.post(`/orders/${orderId}/ship`, data);
}

export function cancelOrder(orderId) {
  return http.post(`/orders/${orderId}/cancel`);
}

export function requestRefund(orderId) {
  return http.post(`/orders/${orderId}/refund`);
}

export function approveRefund(orderId) {
  return http.post(`/orders/${orderId}/refund/approve`);
}

export function rejectRefund(orderId) {
  return http.post(`/orders/${orderId}/refund/reject`);
}

// ── 收货地址接口 ──────────────────────────────
export function getAddressList() {
  return http.get("/addresses");
}

export function getDefaultAddress() {
  return http.get("/addresses/default");
}

export function getAddress(id) {
  return http.get(`/addresses/${id}`);
}

export function createAddress(data) {
  return http.post("/addresses", data);
}

export function updateAddress(id, data) {
  return http.put(`/addresses/${id}`, data);
}

export function deleteAddress(id) {
  return http.delete(`/addresses/${id}`);
}

export function setDefaultAddress(id) {
  return http.put(`/addresses/${id}/default`);
}

// ── 购物车接口 ──────────────────────────────
export function getCartList() {
  return http.get("/cart");
}

export function getCartCount() {
  return http.get("/cart/count");
}

export function addToCart(itemId, quantity) {
  return http.post("/cart", { itemId, quantity });
}

export function updateCartQuantity(cartId, quantity) {
  return http.put(`/cart/${cartId}`, { quantity });
}

export function updateCartSelected(cartId, selected) {
  return http.put(`/cart/${cartId}/select`, { selected });
}

export function batchUpdateCartSelected(cartIds, selected) {
  return http.put("/cart/batch/select", { cartIds, selected });
}

export function deleteCartItem(cartId) {
  return http.delete(`/cart/${cartId}`);
}

export function clearCart() {
  return http.delete("/cart/clear");
}

// ── 收藏接口 ──────────────────────────────
export function getFavoriteList() {
  return http.get("/favorites");
}

export function getFavoriteCount() {
  return http.get("/favorites/count");
}

export function checkFavorite(itemId) {
  return http.get(`/favorites/check/${itemId}`);
}

export function addFavorite(itemId) {
  return http.post(`/favorites/${itemId}`);
}

export function removeFavorite(itemId) {
  return http.delete(`/favorites/${itemId}`);
}

// ── 评价接口 ──────────────────────────────
export function getItemReviews(itemId, params = {}) {
  return http.get(`/reviews/item/${itemId}`, { params });
}

export function getItemReviewStats(itemId) {
  return http.get(`/reviews/item/${itemId}/stats`);
}

export function getOrderReview(orderId) {
  return http.get(`/reviews/order/${orderId}`);
}

export function createReview(data) {
  return http.post("/reviews", data);
}

export function replyReview(reviewId, content) {
  return http.post(`/reviews/${reviewId}/reply`, { content });
}

// ── 卖家统计接口 ──────────────────────────────
export function getSellerOverview() {
  return http.get("/seller/stats/overview");
}

export function getSellerTrend(days = 7) {
  return http.get("/seller/stats/trend", { params: { days } });
}

export function getSellerRanking(limit = 10) {
  return http.get("/seller/stats/ranking", { params: { limit } });
}

// ── 文件上传 ──────────────────────────────
export function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  return http.post("/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}
