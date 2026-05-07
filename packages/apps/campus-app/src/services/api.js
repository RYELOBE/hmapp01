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
  return await http.get(`/reviews/${orderId}`);
}