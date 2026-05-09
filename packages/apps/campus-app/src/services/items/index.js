import http from "../core/http";

export async function getItems(params = {}) {
  return await http.post("/items/list", params);
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

export async function uploadImage(file) {
  const rawFile = file?.file || file?.originFile || file;
  if (!rawFile) {
    throw new Error("请选择要上传的图片");
  }
  const formData = new FormData();
  formData.append("file", rawFile, rawFile.name || file?.name || "image.png");
  return await http.post("/upload", formData);
}
