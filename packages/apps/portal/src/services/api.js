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

// ── 文件上传 ──────────────────────────────
export function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  return http.post("/upload/image", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}
