import axios from "axios";
import { STORAGE_KEYS } from "@campus/common";

const API_BASE_URL = "/api";

const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

instance.interceptors.response.use((response) => {
  return response.data;
}, (error) => {
  if (error.response?.status === 401) {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "/login";
  }
  return Promise.reject(error);
});

export async function getItems(params = {}) {
  return await instance.get("/items", { params });
}

export async function getItemDetail(id) {
  return await instance.get(`/items/${id}`);
}

export async function publishItem(data) {
  return await instance.post("/items", data);
}

export async function updateItem(id, data) {
  return await instance.put(`/items/${id}`, data);
}

export async function offShelfItem(id) {
  return await instance.put(`/items/${id}/off-shelf`);
}

export async function deleteItem(id) {
  return await instance.delete(`/items/${id}`);
}

export async function getMyItems(params = {}) {
  return await instance.get("/items/my", { params });
}

export async function getOrders(params = {}) {
  return await instance.get("/orders", { params });
}

export async function getOrderDetail(id) {
  return await instance.get(`/orders/${id}`);
}

export async function createOrder(data) {
  return await instance.post("/orders", data);
}

export async function confirmOrder(id, data) {
  return await instance.post(`/orders/${id}/confirm`, data);
}

export async function cancelOrder(id) {
  return await instance.post(`/orders/${id}/cancel`);
}

export async function getMyOrders(params = {}) {
  return await instance.get("/orders/my", { params });
}

export async function payOrder(id, data) {
  return await instance.post(`/orders/${id}/pay`, data);
}

export async function shipOrder(id, data) {
  return await instance.post(`/orders/${id}/ship`, data);
}

export async function requestRefund(id, data) {
  return await instance.post(`/orders/${id}/refund`, data);
}

export async function approveRefund(id) {
  return await instance.post(`/orders/${id}/refund/approve`);
}

export async function rejectRefund(id, data) {
  return await instance.post(`/orders/${id}/refund/reject`, data);
}

export async function addToCart(itemId, quantity = 1) {
  return await instance.post("/cart", { itemId, quantity });
}

export async function getCart() {
  return await instance.get("/cart");
}

export async function getCartList() {
  return await instance.get("/cart");
}

export async function updateCartItem(id, quantity) {
  return await instance.put(`/cart/${id}`, { quantity });
}

export async function updateCartQuantity(id, quantity) {
  return await instance.put(`/cart/${id}`, { quantity });
}

export async function updateCartSelected(id, selected) {
  return await instance.put(`/cart/${id}/select`, { selected });
}

export async function removeCartItem(id) {
  return await instance.delete(`/cart/${id}`);
}

export async function deleteCartItem(id) {
  return await instance.delete(`/cart/${id}`);
}

export async function addFavorite(itemId) {
  return await instance.post("/favorites", { itemId });
}

export async function removeFavorite(itemId) {
  return await instance.delete(`/favorites/${itemId}`);
}

export async function checkFavorite(itemId) {
  return await instance.get(`/favorites/check/${itemId}`);
}

export async function getFavorites(params = {}) {
  return await instance.get("/favorites", { params });
}

export async function getFavoriteList(params = {}) {
  return await instance.get("/favorites", { params });
}

export async function getAddresses(params = {}) {
  return await instance.get("/addresses", { params });
}

export async function getAddressList(params = {}) {
  return await instance.get("/addresses", { params });
}

export async function getDefaultAddress() {
  return await instance.get("/addresses/default");
}

export async function setDefaultAddress(id) {
  return await instance.put(`/addresses/${id}/default`);
}

export async function createAddress(data) {
  return await instance.post("/addresses", data);
}

export async function updateAddress(id, data) {
  return await instance.put(`/addresses/${id}`, data);
}

export async function deleteAddress(id) {
  return await instance.delete(`/addresses/${id}`);
}

export async function submitReview(orderId, data) {
  return await instance.post(`/reviews/submit/${orderId}`, data);
}

export async function getReview(orderId) {
  return await instance.get(`/reviews/${orderId}`);
}

export async function register(data) {
  return await instance.post("/auth/register", data);
}

export async function getStatistics() {
  return await instance.get("/ops/stats");
}

export async function getReviewQueue(params = {}) {
  return await instance.get("/ops/reviews/queue", { params });
}

export async function reviewItem(id, data) {
  return await instance.post(`/ops/reviews/${id}`, data);
}

export async function approveItem(id, data) {
  return await instance.post(`/ops/reviews/${id}/approve`, data);
}

export async function rejectItem(id, data) {
  return await instance.post(`/ops/reviews/${id}/reject`, data);
}

export async function getOpsOrders(params = {}) {
  return await instance.get("/ops/orders", { params });
}

export async function getOpsReviewDetail(id) {
  return await instance.get(`/ops/reviews/${id}`);
}

export async function getUsers(params = {}) {
  return await instance.get("/ops/users", { params });
}

export async function updateUserStatus(id, status) {
  return await instance.put(`/ops/users/${id}/status`, { status });
}

export async function getSellers(params = {}) {
  return await instance.get("/ops/sellers", { params });
}

export async function getBuyers(params = {}) {
  return await instance.get("/ops/buyers", { params });
}

export async function getApps(params = {}) {
  return await instance.get("/ops/apps", { params });
}

export async function createApp(data) {
  return await instance.post("/ops/apps", data);
}

export async function updateApp(id, data) {
  return await instance.put(`/ops/apps/${id}`, data);
}

export async function deleteApp(id) {
  return await instance.delete(`/ops/apps/${id}`);
}

export async function getPortalConfig(portalCode) {
  return await instance.get(`/ops/portal/${portalCode}`);
}

export async function updatePortalConfig(portalCode, data) {
  return await instance.put(`/ops/portal/${portalCode}`, data);
}

export async function getRoles(params = {}) {
  return await instance.get("/ops/roles", { params });
}

export async function createRole(data) {
  return await instance.post("/ops/roles", data);
}

export async function updateRole(id, data) {
  return await instance.put(`/ops/roles/${id}`, data);
}

export async function deleteRole(id) {
  return await instance.delete(`/ops/roles/${id}`);
}

export async function getResources(params = {}) {
  return await instance.get("/ops/resources", { params });
}

export async function createResource(data) {
  return await instance.post("/ops/resources", data);
}

export async function updateResource(id, data) {
  return await instance.put(`/ops/resources/${id}`, data);
}

export async function deleteResource(id) {
  return await instance.delete(`/ops/resources/${id}`);
}

export async function getRoutes(params = {}) {
  return await instance.get("/ops/routes", { params });
}

export async function createRoute(data) {
  return await instance.post("/ops/routes", data);
}

export async function updateRoute(id, data) {
  return await instance.put(`/ops/routes/${id}`, data);
}

export async function deleteRoute(id) {
  return await instance.delete(`/ops/routes/${id}`);
}

export default instance;