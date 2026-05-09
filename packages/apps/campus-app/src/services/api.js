import http from "./core/http";

export * from "./items";
export * from "./orders";
export * from "./users";
export * from "./ops";
export * from "./auth";
export * from "./ai";

export async function getSellerOverview() {
  return await http.get("/seller/overview");
}

export async function getSellerTrend(days = 7) {
  return await http.get("/seller/trend", { params: { days } });
}

export async function getSellerRanking(params = {}) {
  return await http.get("/seller/ranking", { params });
}

export async function submitReview(orderId, data) {
  return await http.post("/reviews", { orderId, ...data });
}

export async function createReview(orderId, data) {
  return await http.post("/reviews", { orderId, ...data });
}

export async function getReview(orderId) {
  return await http.get(`/reviews/order/${orderId}`);
}

export async function getDictOptions() {
  return await http.get("/dict/options");
}

export async function getHotItems(limit = 8) {
  return await http.get(`/items/hot?limit=${limit}`);
}

export async function trackView(itemId) {
  return await http.post(`/items/${itemId}/track-view`);
}

export async function trackClick(itemId) {
  return await http.post(`/items/${itemId}/track-click`);
}

export async function trackFavorite(itemId, isAdd) {
  return await http.post(`/items/${itemId}/track-favorite?isAdd=${isAdd}`);
}