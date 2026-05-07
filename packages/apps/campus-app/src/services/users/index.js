import http from "../core/http";

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