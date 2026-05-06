<template>
  <div class="review-detail">
    <a-spin :loading="loading" style="width: 100%">
      <div v-if="item" class="detail-content">
        <a-card title="商品信息" :bordered="false" class="section-card">
          <template #extra>
            <a-tag :color="getStatusColor(item.reviewStatus)" size="small">
              {{ getStatusLabel(item.reviewStatus) }}
            </a-tag>
          </template>

          <div class="image-gallery-section">
            <div class="section-label">商品图片</div>
            <a-image-preview-group infinite>
              <div class="image-gallery">
                <a-image
                  v-for="(url, index) in imageList"
                  :key="index"
                  :src="url"
                  width="120"
                  height="120"
                  fit="cover"
                  loading="lazy"
                  class="gallery-image"
                />
                <div v-if="imageList.length === 0" class="image-empty">
                  <icon-image />
                  <span>暂无图片</span>
                </div>
              </div>
            </a-image-preview-group>
          </div>

          <a-descriptions :column="2" bordered size="medium" class="info-descriptions">
            <a-descriptions-item label="商品标题" :span="2">
              <h3 style="margin: 0; font-size: 16px; font-weight: 600; color: #1d2129;">
                {{ item.title }}
              </h3>
            </a-descriptions-item>
            <a-descriptions-item label="当前价格">
              <a-typography-text type="danger" strong style="font-size: 18px;">
                ¥{{ formatPrice(item.price) }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="原价" v-if="item.originalPrice">
              <a-typography-text delete type="secondary">
                ¥{{ formatPrice(item.originalPrice) }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="商品分类">
              <a-tag size="small">{{ item.category || '未分类' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="商品成色">
              <a-tag size="small" :color="getConditionColor(item.conditionLevel)">
                {{ getConditionLabel(item.conditionLevel) }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>

          <div class="description-section">
            <div class="section-label">商品描述</div>
            <div class="description-content" v-html="item.description || '暂无描述'"></div>
          </div>
        </a-card>

        <a-card title="卖家信息" :bordered="false" class="section-card">
          <div class="seller-info-header">
            <a-avatar :size="48" :style="{ backgroundColor: '#165dff' }">
              {{ (item.sellerName || '卖')[0] }}
            </a-avatar>
            <div class="seller-basic">
              <h4 style="margin: 0; font-size: 16px; font-weight: 600;">{{ item.sellerName || '未知卖家' }}</h4>
              <span style="font-size: 13px; color: #86909c;">ID: {{ item.sellerId || '-' }}</span>
            </div>
          </div>

          <a-descriptions :column="2" bordered size="medium" style="margin-top: 16px;">
            <a-descriptions-item label="注册时间">
              {{ formatDate(item.sellerCreatedAt) || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="历史发布数">
              <a-badge :text="item.sellerTotalItems || 0" />
            </a-descriptions-item>
            <a-descriptions-item label="通过率" v-if="item.sellerApprovalRate !== undefined">
              <a-progress
                :percent="item.sellerApprovalRate"
                :size="'small'"
                :status="item.sellerApprovalRate >= 80 ? 'success' : 'warning'"
              />
            </a-descriptions-item>
            <a-descriptions-item label="操作">
              <a-button type="text" size="small" @click="viewSellerProfile">
                查看卖家主页
              </a-button>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="审核记录" :bordered="false" class="section-card">
          <a-timeline v-if="reviewHistory && reviewHistory.length > 0">
            <a-timeline-item
              v-for="(record, index) in reviewHistory"
              :key="index"
              :dot-color="getTimelineColor(record.action)"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ record.actionLabel }}</div>
                <div class="timeline-time">{{ formatDateTime(record.createdAt) }}</div>
                <div class="timeline-operator" v-if="record.operator">操作人：{{ record.operator }}</div>
                <div class="timeline-reason" v-if="record.reason">
                  原因：{{ record.reason }}
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无审核记录" />
        </a-card>

        <a-collapse v-if="showReviewRules" class="rules-collapse" :default-active-key="['1']">
          <a-collapse-item header="审核规则提示" key="1">
            <div class="rules-list">
              <div class="rule-category">
                <h5>✅ 信息完整性检查</h5>
                <ul>
                  <li>商品标题清晰准确</li>
                  <li>价格填写合理</li>
                  <li>分类选择正确</li>
                  <li>成色描述真实</li>
                </ul>
              </div>
              <div class="rule-category">
                <h5>📸 图片规范性检查</h5>
                <ul>
                  <li>图片清晰度良好</li>
                  <li>图片数量充足（至少3张）</li>
                  <li>展示商品全貌和细节</li>
                  <li>无水印或无关内容</li>
                </ul>
              </div>
              <div class="rule-category">
                <h5>💰 价格合理性参考</h5>
                <ul>
                  <li>参考同类商品市场价</li>
                  <li>考虑成色折旧程度</li>
                  <li>警惕异常低价或高价</li>
                </ul>
              </div>
              <div class="rule-category warning">
                <h5>⚠️ 违禁品警告标识</h5>
                <ul>
                  <li>禁止发布违禁物品</li>
                  <li>禁止虚假宣传信息</li>
                  <li>禁止侵权商品</li>
                </ul>
              </div>
            </div>
          </a-collapse-item>
        </a-collapse>
      </div>

      <a-empty v-else description="暂无数据" />
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import { IconImage } from "@arco-design/web-vue/es/icon";
import { approveItem, rejectItem, getItemDetail } from "../../services/api";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["approve", "reject", "cancel", "view-seller"]);

const loading = ref(false);
const approving = ref(false);
const rejecting = ref(false);
const showRejectForm = ref(false);
const showReviewRules = computed(() => props.item?.reviewStatus === 'PENDING_REVIEW');

const form = reactive({
  rejectReason: "",
  rejectReasonType: "",
});

const quickRejectReasons = [
  { value: "incomplete_info", label: "信息不完整" },
  { value: "invalid_image", label: "图片不规范" },
  { value: "abnormal_price", label: "价格异常" },
  { value: "violation", label: "商品违规（违禁品/虚假信息）" },
  { value: "other", label: "其他（需填写具体原因）" },
];

const imageList = computed(() => {
  if (!props.item) return [];
  const urls = props.item.imageUrls || props.item.images || [];
  if (typeof urls === "string") {
    try {
      return JSON.parse(urls);
    } catch {
      return urls ? [urls] : [];
    }
  }
  return Array.isArray(urls) ? urls : [];
});

const reviewHistory = computed(() => {
  if (!props.item) return [];
  const history = props.item.reviewHistory || [];

  if (props.item.createdAt && history.length === 0) {
    history.unshift({
      action: 'submit',
      actionLabel: '提交审核',
      createdAt: props.item.createdAt,
      operator: props.item.sellerName,
    });
  }

  if (props.item.reviewStatus === 'APPROVED') {
    history.push({
      action: 'approved',
      actionLabel: '审核通过',
      createdAt: props.item.updatedAt,
      reason: '',
    });
  }

  if (props.item.reviewStatus === 'REJECTED') {
    history.push({
      action: 'rejected',
      actionLabel: '审核拒绝',
      createdAt: props.item.updatedAt,
      reason: props.item.rejectReason,
    });
  }

  return history;
});

function formatPrice(price) {
  if (!price && price !== 0) return "0.00";
  return Number(price).toFixed(2);
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

function formatDateTime(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function getStatusColor(status) {
  const colors = {
    PENDING_REVIEW: "orangered",
    APPROVED: "green",
    REJECTED: "red",
  };
  return colors[status] || "gray";
}

function getStatusLabel(status) {
  const labels = {
    PENDING_REVIEW: "待审核",
    APPROVED: "已通过",
    REJECTED: "已拒绝",
  };
  return labels[status] || status;
}

function getConditionColor(condition) {
  const colors = {
    NEW: "green",
    LIKE_NEW: "cyan",
    GOOD: "blue",
    FAIR: "orange",
    POOR: "red",
  };
  return colors[condition] || "gray";
}

function getConditionLabel(condition) {
  const labels = {
    NEW: "全新",
    LIKE_NEW: "几乎全新",
    GOOD: "良好",
    FAIR: "一般",
    POOR: "较差",
  };
  return labels[condition] || condition;
}

function getTimelineColor(action) {
  const colors = {
    submit: '#165DFF',
    approved: '#00B42A',
    rejected: '#F53F3F',
  };
  return colors[action] || '#86909C';
}

function viewSellerProfile() {
  emit('view-seller', props.item);
}

async function handleApprove() {
  approving.value = true;
  try {
    await approveItem(props.item.id);
    Message.success("审核已通过");
    emit("approve");
  } catch (error) {
    Message.error(error.message || "操作失败");
  } finally {
    approving.value = false;
  }
}

async function handleReject() {
  if (!form.rejectReason.trim()) {
    Message.warning("请输入或选择驳回原因");
    return;
  }

  rejecting.value = true;
  try {
    await rejectItem(props.item.id, form.rejectReason);
    Message.success("已驳回");
    showRejectForm.value = false;
    emit("reject");
  } catch (error) {
    Message.error(error.message || "操作失败");
  } finally {
    rejecting.value = false;
  }
}

function openRejectForm() {
  showRejectForm.value = true;
  form.rejectReason = "";
  form.rejectReasonType = "";
}

function cancelReject() {
  showRejectForm.value = false;
  form.rejectReason = "";
  form.rejectReasonType = "";
}

function selectQuickReason(reason) {
  form.rejectReasonType = reason.value;
  if (reason.value === 'other') {
    form.rejectReason = '';
  } else {
    form.rejectReason = reason.label;
  }
}

defineExpose({
  handleApprove,
  handleReject,
  openRejectForm,
  cancelReject,
  showRejectForm,
  approving,
  rejecting,
  form,
});
</script>

<style lang="scss" scoped>
.review-detail {
  padding: 0;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  border-radius: 8px;

  :deep(.arco-card-head) {
    min-height: 44px;
    padding: 12px 20px;
    border-bottom: 1px solid #e5e6eb;

    .arco-card-head-title {
      font-size: 15px;
      font-weight: 600;
      color: #1d2129;
    }
  }

  :deep(.arco-card-body) {
    padding: 20px;
  }
}

.image-gallery-section {
  margin-bottom: 20px;

  .section-label {
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
    margin-bottom: 12px;
  }
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .gallery-image {
    border-radius: 6px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }

  .image-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 120px;
    height: 120px;
    background: #f7f8fa;
    border-radius: 6px;
    color: #86909c;
    font-size: 12px;
    gap: 8px;
  }
}

.info-descriptions {
  :deep(.arco-descriptions-item-label) {
    background: #fafafa;
    font-weight: 500;
  }
}

.description-section {
  margin-top: 20px;

  .section-label {
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
    margin-bottom: 12px;
  }

  .description-content {
    font-size: 14px;
    line-height: 1.8;
    color: #4e5969;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.seller-info-header {
  display: flex;
  align-items: center;
  gap: 16px;

  .seller-basic {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

.timeline-content {
  .timeline-title {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
  }

  .timeline-time {
    font-size: 12px;
    color: #86909c;
    margin-top: 4px;
  }

  .timeline-operator {
    font-size: 13px;
    color: #4e5969;
    margin-top: 4px;
  }

  .timeline-reason {
    font-size: 13px;
    color: #f53f3f;
    margin-top: 4px;
    padding: 8px 12px;
    background: #fff1f0;
    border-radius: 4px;
  }
}

.rules-collapse {
  border-radius: 8px;

  :deep(.arco-collapse-item-header) {
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.arco-collapse-item-content) {
    padding: 16px 20px;
  }
}

.rules-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.rule-category {
  h5 {
    margin: 0 0 8px 0;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
  }

  ul {
    margin: 0;
    padding-left: 18px;
    list-style: disc;

    li {
      font-size: 13px;
      color: #4e5969;
      line-height: 1.8;
    }
  }

  &.warning {
    h5 {
      color: #f53f3f;
    }

    ul {
      li {
        color: #f53f3f;
      }
    }
  }
}
</style>
