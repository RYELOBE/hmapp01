// @campus/common — 纯工具共享包（不依赖 Vue 运行时）
export { roleLabels, ALL_ROLES, hasAnyRole, hasRole } from "./roles.js";
export { theme } from "./theme.js";
export {
  ITEM_STATUS,
  ORDER_STATUS,
  STATUS_MAP,
  CATEGORIES,
  ITEM_CONDITION,
  PAYMENT_METHODS,
  USER_TYPES,
  TRADE_METHODS,
  STORAGE_KEYS,
} from "./constants.js";
export {
  safeJsonParse,
  formatPrice,
  simpleId,
  debounce,
  throttle,
  formatDate,
  deepClone,
  formatFileSize,
  validatePhone,
  validateEmail,
  toThousands,
} from "./utils.js";
