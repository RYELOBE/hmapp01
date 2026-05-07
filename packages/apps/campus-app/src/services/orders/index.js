import http from "../core/http";

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