import { defineStore } from "pinia";
import { getRegisters, getAuthRoutes, getPortalConfig, getMyMenuTree } from "../../services/frame";
import { createConfigs, createRoutes } from "../index";

const framePinia = defineStore("framePinia", {
  state: () => ({
    registConfigs: null,
    /** 当前用户有权限访问的子应用列表（供侧边栏菜单渲染） [{appCode, title, pathPrefix, roles, portalCode}] */
    authorizedApps: [],
    routePermissionMap: {},
    resetMark: "",
    loaded: false,
    configsLoaded: false,
    /** 当前子应用的门户配置（主题、Logo、布局等） */
    currentPortalConfig: null,
    /** 当前用户有权限的菜单树（从后端按角色过滤） */
    myMenuTree: [],
    /** ToAppMain 挂载的 TO_FRAME_WINDOW DOM 引用 */
    toEL: "#TO_FRAME_WINDOW",
  }),
  actions: {
    /** 加载子应用注册配置（公开接口，无需登录） */
    async getRegistConfigs() {
      if (this.configsLoaded) return;
      try {
        const result = await getRegisters();
        const data = result.apps || [];
        this.registConfigs = createConfigs(data);
        this.configsLoaded = true;
      } catch (e) {
        console.error("[framePinia] getRegistConfigs failed:", e);
        this.registConfigs = [];
        this.configsLoaded = true;
      }
    },

    /** 加载权限路由（需登录），401 时静默处理 */
    async getRegistRoutes() {
      try {
        const result = await getAuthRoutes();
        const resources = result.resources || [];
        // 保存完整的授权子应用列表，供侧边栏菜单使用
        this.authorizedApps = resources;
        this.routePermissionMap = resources.reduce((acc, item) => {
          acc[item.pathPrefix] = item.roles || [];
          return acc;
        }, {});
        createRoutes(resources);
        this.loaded = true;
        this.resetMark = String(Date.now());
      } catch (e) {
        console.warn("[framePinia] getRegistRoutes failed (user may not be logged in):", e);
      }
    },

    /** 加载当前子应用的门户配置 */
    async loadPortalConfig(portalCode) {
      if (!portalCode) return;
      try {
        const result = await getPortalConfig(portalCode);
        const portal = result.portal;
        if (portal?.configJson) {
          this.currentPortalConfig = JSON.parse(portal.configJson);
        } else {
          this.currentPortalConfig = null;
        }
      } catch (e) {
        console.warn("[framePinia] loadPortalConfig failed:", e);
        this.currentPortalConfig = null;
      }
    },

    /** 加载当前用户的菜单树 */
    async loadMyMenuTree() {
      try {
        const result = await getMyMenuTree();
        this.myMenuTree = result.tree || [];
      } catch (e) {
        console.warn("[framePinia] loadMyMenuTree failed:", e);
        this.myMenuTree = [];
      }
    },

    /** 公开初始化方法：仅加载 configs 并启动 qiankun，不加载权限路由 */
    async initFrame() {
      await this.getRegistConfigs();
    },

    /** 完整初始化：configs + 权限路由 */
    async initFrameWithRoutes() {
      await this.getRegistConfigs();
      await this.getRegistRoutes();
    }
  }
});

export default framePinia;
