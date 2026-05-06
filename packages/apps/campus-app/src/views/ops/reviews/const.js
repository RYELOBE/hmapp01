export const REVIEW_STATUS_OPTIONS = [
  { value: "", label: "全部" },
  { value: "PENDING_REVIEW", label: "待审核" },
  { value: "APPROVED", label: "已通过" },
  { value: "REJECTED", label: "已驳回" },
];

export const REVIEW_STATUS_MAP = {
  PENDING_REVIEW: { label: "待审核", color: "orange" },
  APPROVED: { label: "已通过", color: "green" },
  REJECTED: { label: "已驳回", color: "red" },
};

export const REVIEW_COLUMNS = [
  { title: "商品ID", dataIndex: "id", width: 80 },
  { title: "商品名", dataIndex: "title", width: 200 },
  { title: "卖家", dataIndex: "sellerName", width: 120 },
  { title: "价格", dataIndex: "price", width: 100 },
  { title: "分类", dataIndex: "category", width: 100 },
  { title: "审核状态", dataIndex: "reviewStatus", width: 100 },
  { title: "提交时间", dataIndex: "createdAt", width: 160 },
  { title: "操作", dataIndex: "actions", width: 160 },
];
