<template>
  <div class="review-detail">
    <a-spin :loading="loading" style="width: 100%">
      <div v-if="item" class="detail-content">
        <a-card title="商品图片" :bordered="false" class="section-card">
          <template #extra>
            <a-tag color="arcoblue">{{ imageList.length }} 张</a-tag>
          </template>
          <a-image-preview-group infinite>
            <div class="image-gallery">
              <a-image
                v-for="(url, index) in imageList"
                :key="index"
                :src="url"
                width="100"
                height="100"
                fit="cover"
                class="gallery-image"
              />
              <div v-if="imageList.length === 0" class="image-empty">
                <icon-image />
                <span>暂无图片</span>
              </div>
            </div>
          </a-image-preview-group>
        </a-card>

        <a-card title="商品信息" :bordered="false" class="section-card">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="商品名称" :span="2">
              {{ item.title }}
            </a-descriptions-item>
            <a-descriptions-item label="商品价格">
              <a-typography-text type="danger" strong>
                ¥{{ item.price }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="原价" v-if="item.originalPrice">
              <a-typography-text delete type="secondary">
                ¥{{ item.originalPrice }}
              </a-typography-text>
            </a-descriptions-item>
            <a-descriptions-item label="商品分类">
              <a-tag>{{ item.category || '未分类' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="商品成色">
              <StatusTag :status="item.conditionLevel" type="condition" />
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="商品描述" :bordered="false" class="section-card">
          <a-typography-paragraph class="description-text">
            {{ item.description || '暂无描述' }}
          </a-typography-paragraph>
        </a-card>

        <a-card title="卖家信息" :bordered="false" class="section-card">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="卖家名称">
              {{ item.sellerName || '未知' }}
            </a-descriptions-item>
            <a-descriptions-item label="联系电话" v-if="item.sellerPhone">
              {{ item.sellerPhone }}
            </a-descriptions-item>
            <a-descriptions-item label="所在校区">
              {{ item.campus || '未设置' }}
            </a-descriptions-item>
            <a-descriptions-item label="发布时间">
              {{ formatDate(item.createdAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="审核信息" :bordered="false" class="section-card">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="审核状态">
              <StatusTag :status="item.reviewStatus" />
            </a-descriptions-item>
            <a-descriptions-item label="提交时间">
              {{ formatDate(item.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="驳回原因" :span="2" v-if="item.rejectReason">
              <a-typography-text type="danger">
                {{ item.rejectReason }}
              </a-typography-text>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card v-if="item.reviewStatus === 'PENDING_REVIEW'" title="审核操作" :bordered="false" class="section-card">
          <a-form :model="form" layout="vertical">
            <a-form-item label="驳回原因" field="rejectReason">
              <a-textarea
                v-model="form.rejectReason"
                placeholder="请输入驳回原因（可选）"
                :max-length="200"
                show-word-limit
                :rows="3"
              />
            </a-form-item>
            <a-form-item label="快速驳回原因">
              <a-space>
                <a-tag
                  v-for="reason in quickRejectReasons"
                  :key="reason"
                  class="reject-tag"
                  style="cursor: pointer"
                  @click="form.rejectReason = reason"
                >
                  {{ reason }}
                </a-tag>
              </a-space>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" status="success" size="large" @click="handleApprove" :loading="submitting">
                  <template #icon><icon-check /></template>
                  审核通过
                </a-button>
                <a-button type="primary" status="danger" size="large" @click="handleReject" :loading="submitting">
                  <template #icon><icon-close /></template>
                  驳回
                </a-button>
                <a-button size="large" @click="$emit('cancel')">
                  取消
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>

        <a-alert v-else type="info" class="alert-box">
          该商品已处理，无法再次审核
        </a-alert>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconCheck, IconClose, IconImage } from "@arco-design/web-vue/es/icon";
import StatusTag from "commonprovide/status-tag";
import { approveItem, rejectItem } from "../services/api";

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["approve", "reject", "cancel"]);

const loading = ref(false);
const submitting = ref(false);

const form = reactive({
  rejectReason: "",
});

const quickRejectReasons = [
  "图片不规范",
  "价格异常",
  "商品违规",
  "信息不完整",
  "重复发布",
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

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function handleApprove() {
  submitting.value = true;
  try {
    await approveItem(props.item.id);
    Message.success("审核已通过");
    emit("approve");
  } catch (error) {
    Message.error(error.message || "操作失败");
  } finally {
    submitting.value = false;
  }
}

async function handleReject() {
  if (!form.rejectReason.trim()) {
    Message.warning("请输入驳回原因");
    return;
  }
  submitting.value = true;
  try {
    await rejectItem(props.item.id, form.rejectReason);
    Message.success("已驳回");
    emit("reject");
  } catch (error) {
    Message.error(error.message || "操作失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="scss" scoped>
.review-detail {
  padding: 0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  :deep(.arco-card-head) {
    min-height: 40px;
    padding: 12px 16px;

    .arco-card-head-title {
      font-size: 14px;
      font-weight: 600;
    }
  }

  :deep(.arco-card-body) {
    padding: 16px;
  }
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .gallery-image {
    border-radius: 6px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.05);
    }
  }

  .image-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    background: #f7f8fa;
    border-radius: 6px;
    color: #86909c;
    font-size: 12px;
    gap: 8px;
  }
}

.description-text {
  margin: 0;
  color: #4e5969;
  line-height: 1.8;
  white-space: pre-wrap;
}

.reject-tag {
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: scale(1.05);
  }
}

.alert-box {
  margin-top: 16px;
}
</style>
