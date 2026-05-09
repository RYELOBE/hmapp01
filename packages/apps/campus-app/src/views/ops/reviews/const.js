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

export const CATEGORY_MAP = {
  ELECTRONICS: { label: "数码产品", color: "blue" },
  BOOKS: { label: "图书教材", color: "green" },
  CLOTHING: { label: "服饰鞋包", color: "purple" },
  DAILY: { label: "生活用品", color: "cyan" },
  SPORTS: { label: "运动户外", color: "orange" },
  FOOD: { label: "食品饮料", color: "lime" },
  BEAUTY: { label: "美妆个护", color: "pink" },
  OTHER: { label: "其他", color: "gray" },
};

export const REVIEW_COLUMNS = [
  { title: "商品ID", dataIndex: "id", width: 80 },
  { title: "商品名", dataIndex: "title", width: 200, slotName: "title" },
  { title: "卖家", dataIndex: "sellerName", width: 120 },
  { title: "价格", dataIndex: "price", width: 100, slotName: "price" },
  { title: "分类", dataIndex: "category", width: 120, slotName: "category" },
  { title: "审核状态", dataIndex: "reviewStatus", width: 120, slotName: "reviewStatus" },
  { title: "提交时间", dataIndex: "createdAt", width: 160, slotName: "createdAt" },
  { title: "操作", dataIndex: "actions", width: 160, slotName: "actions" },
];
