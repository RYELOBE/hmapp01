// ── 供方列表表格列 ──────────────────────────
export const VENDOR_COLUMNS = [
  { title: "用户名", dataIndex: "username", width: 140, slotName: "username" },
  { title: "商品总数", dataIndex: "totalItems", width: 100 },
  { title: "在售数", dataIndex: "activeItems", width: 100, slotName: "activeItems" },
  { title: "已售数", dataIndex: "soldItems", width: 100 },
  { title: "待审核", dataIndex: "pendingItems", width: 100, slotName: "pendingItems" },
  { title: "状态", dataIndex: "status", width: 100, slotName: "status" },
  { title: "注册时间", dataIndex: "createdAt", width: 160 },
  { title: "操作", dataIndex: "operations", width: 260, slotName: "operations", fixed: "right" },
];
