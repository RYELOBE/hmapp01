export const ORDER_STATUS_OPTIONS = [
  { value: "", label: "全部" },
  { value: "PENDING_PAY", label: "待付款" },
  { value: "PENDING_SHIP", label: "待发货" },
  { value: "SHIPPED", label: "待收货" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
];

export const ORDER_STATUS_MAP = {
  PENDING_PAY: { label: "待付款", color: "orange" },
  PENDING_SHIP: { label: "待发货", color: "blue" },
  SHIPPED: { label: "待收货", color: "cyan" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "gray" },
};

export const ORDER_COLUMNS = [
  { title: "订单号", dataIndex: "id", width: 100 },
  { title: "商品", dataIndex: "itemTitle", width: 200 },
  { title: "买家", dataIndex: "buyerName", width: 100 },
  { title: "卖家", dataIndex: "sellerName", width: 100 },
  { title: "金额", dataIndex: "amount", width: 100 },
  { title: "状态", dataIndex: "status", width: 100 },
  { title: "下单时间", dataIndex: "createdAt", width: 160 },
];
