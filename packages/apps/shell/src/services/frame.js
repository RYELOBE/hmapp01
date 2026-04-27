import { http } from "commonprovide/http";

export function getRegisters() {
  return http.get("/frame/registers");
}

export function getAuthRoutes() {
  return http.get("/frame/routes");
}

/** 获取门户配置（公开接口） */
export function getPortalConfig(portalCode) {
  return http.get(`/portal/configs/${portalCode}`);
}

/** 获取当前用户的菜单树 */
export function getMyMenuTree() {
  return http.get("/resource/my-menus");
}
