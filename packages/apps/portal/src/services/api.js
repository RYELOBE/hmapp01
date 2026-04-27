import { http } from "commonprovide/http";

export function getItems(params = {}) {
  return http.get("/items", { params });
}

export function getItemDetail(id) {
  return http.get(`/items/${id}`);
}

export function publishItem(payload) {
  return http.post("/items", payload);
}

export function getMyOrders() {
  return http.get("/orders/mine");
}
