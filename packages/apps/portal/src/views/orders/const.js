// ── 订单状态配置 ──────────────────────────
export const ORDER_STATUS = [
  { value: "", label: "全部" },
  { value: "PENDING_PAY", label: "待付款" },
  { value: "PENDING_SHIP", label: "待发货" },
  { value: "SHIPPED", label: "待收货" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
];

export const STATUS_MAP = {
  PENDING_PAY: { label: "待付款", color: "orange" },
  PENDING_SHIP: { label: "待发货", color: "blue" },
  SHIPPED: { label: "待收货", color: "cyan" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "gray" },
};
