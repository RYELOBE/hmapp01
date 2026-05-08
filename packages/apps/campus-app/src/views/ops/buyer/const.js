// ── 需方列表表格列 ──────────────────────────
export const BUYER_COLUMNS = [
  { title: "用户名", dataIndex: "username", width: 140, slotName: "username" },
  { title: "订单总数", dataIndex: "totalOrders", width: 100 },
  { title: "已付款", dataIndex: "paidOrders", width: 100 },
  { title: "已完成", dataIndex: "completedOrders", width: 100 },
  { title: "总消费", dataIndex: "totalSpent", width: 120, slotName: "totalSpent" },
  { title: "状态", dataIndex: "status", width: 100, slotName: "status" },
  { title: "注册时间", dataIndex: "createdAt", width: 160 },
  { title: "操作", dataIndex: "operations", width: 220, slotName: "operations", fixed: "right" },
];
