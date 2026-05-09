export const ORDER_STATUS_OPTIONS = [
  { value: "", label: "全部" },
  { value: "PENDING_PAYMENT", label: "待付款" },
  { value: "PAID", label: "待发货" },
  { value: "SHIPPED", label: "待收货" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
  { value: "REFUNDING", label: "退款中" },
  { value: "REFUNDED", label: "已退款" },
];

export const ORDER_STATUS_MAP = {
  PENDING_PAYMENT: { label: "待付款", color: "orange" },
  PAID: { label: "待发货", color: "blue" },
  SHIPPED: { label: "待收货", color: "cyan" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "gray" },
  REFUNDING: { label: "退款中", color: "red" },
  REFUNDED: { label: "已退款", color: "gray" },
};

export const ORDER_COLUMNS = [
  { title: "订单号", dataIndex: "orderNo", width: 140, slotName: "orderNo" },
  { title: "商品", dataIndex: "itemTitle", width: 200, slotName: "itemInfo" },
  { title: "买家", dataIndex: "buyerName", width: 100, slotName: "buyerName" },
  { title: "卖家", dataIndex: "sellerName", width: 100, slotName: "sellerName" },
  { title: "金额", dataIndex: "totalAmount", width: 120, slotName: "amount" },
  { title: "状态", dataIndex: "status", width: 110, slotName: "status" },
  { title: "下单时间", dataIndex: "createdAt", width: 160, slotName: "createdAt" },
  { title: "操作", dataIndex: "actions", width: 100, slotName: "actions" },
];
