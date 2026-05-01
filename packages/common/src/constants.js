/** 商品状态映射 */
export const ITEM_STATUS = {
  PENDING: { label: "待审核", color: "orange" },
  PENDING_REVIEW: { label: "待审核", color: "orange" },
  APPROVED: { label: "已上架", color: "green" },
  REJECTED: { label: "已驳回", color: "red" },
  OFF_SHELF: { label: "已下架", color: "gray" },
};

/** 订单状态映射 */
export const ORDER_STATUS = {
  PENDING_PAYMENT: { label: "待支付", color: "blue" },
  PAID: { label: "已支付", color: "cyan" },
  SHIPPED: { label: "已发货", color: "purple" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "gray" },
  REFUNDING: { label: "退款中", color: "orange" },
  REFUNDED: { label: "已退款", color: "red" },
};

/** 订单状态选项卡（用于前端显示） */
export const ORDER_STATUS_TABS = [
  { value: "", label: "全部" },
  { value: "PENDING_PAYMENT", label: "待支付" },
  { value: "PAID", label: "已支付" },
  { value: "SHIPPED", label: "已发货" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
  { value: "REFUNDING", label: "退款中" },
  { value: "REFUNDED", label: "已退款" },
];

/** 合并所有状态映射（供 StatusTag 使用） */
export const STATUS_MAP = {
  ...ITEM_STATUS,
  ...ORDER_STATUS,
};

/** 物品分类 */
export const CATEGORIES = {
  ELECTRONICS: { label: "电子产品", value: "electronics", icon: "device" },
  TEXTBOOKS: { label: "教材书籍", value: "textbooks", icon: "book" },
  DAILY_NECESSITIES: { label: "生活用品", value: "daily", icon: "house" },
  CLOTHING: { label: "服装配饰", value: "clothing", icon: "shirt" },
  SPORTS: { label: "运动器材", value: "sports", icon: "fitness" },
  FURNITURE: { label: "二手家具", value: "furniture", icon: "table" },
  TRANSPORTATION: { label: "出行工具", value: "transport", icon: "car" },
  OTHERS: { label: "其他物品", value: "others", icon: "more" },
};

/** 物品成色 */
export const ITEM_CONDITION = {
  BRAND_NEW: { label: "全新", value: "brand_new", color: "green" },
  LIKE_NEW: { label: "九成新", value: "like_new", color: "blue" },
  GOOD: { label: "八成新", value: "good", color: "cyan" },
  FAIR: { label: "七成新", value: "fair", color: "orange" },
  POOR: { label: "六成新及以下", value: "poor", color: "red" },
};

/** 支付方式 */
export const PAYMENT_METHODS = {
  WECHAT: { label: "微信支付", value: "wechat", icon: "wechat" },
  ALIPAY: { label: "支付宝", value: "alipay", icon: "alipay" },
  CAMPUS_CARD: { label: "校园卡", value: "campus_card", icon: "card" },
  CASH_ON_DELIVERY: { label: "当面交易", value: "cash", icon: "money" },
};

/** 用户类型 */
export const USER_TYPES = {
  STUDENT: { label: "学生", value: "student" },
  TEACHER: { label: "教师", value: "teacher" },
  STAFF: { label: "教职工", value: "staff" },
};

/** 交易方式 */
export const TRADE_METHODS = {
  FACE_TO_FACE: { label: "当面交易", value: "face_to_face" },
  CAMPUS_EXPRESS: { label: "校园快递", value: "campus_express" },
  SELF_PICKUP: { label: "自行取货", value: "self_pickup" },
};

/** localStorage 存储键名 */
export const STORAGE_KEYS = {
  TOKEN: "campus_market_token",
  USER: "campus_market_user",
  OPS_TOKEN: "campus_ops_token",
  OPS_USER: "campus_ops_user",
  OPS_PORTAL_CONFIG: "campus_ops_portal_config",
};
