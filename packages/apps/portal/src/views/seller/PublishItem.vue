<template>
  <div class="publish-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">{{ editingItem ? '编辑商品' : '发布闲置' }}</h2>
    </div>

    <div class="publish-content">
      <a-card title="Step 1: 基本信息" :bordered="false" class="step-card" :class="{ 'step-card--active': currentStep >= 1 }">
        <a-form ref="formRef1" :model="form" layout="vertical" :rules="rules1">
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item field="title" label="商品标题" required>
                <a-input
                  v-model="form.title"
                  placeholder="请输入商品标题，如：九成新 Cherry轴机械键盘"
                  :max-length="60"
                  show-word-limit
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item field="category" label="商品分类" required>
                <a-select v-model="form.category" placeholder="请选择分类">
                  <a-option v-for="cat in CATEGORY_OPTIONS" :key="cat.value" :value="cat.value">
                    {{ cat.label }}
                  </a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item field="conditionLevel" label="商品成色">
                <a-radio-group v-model="form.conditionLevel" type="button">
                  <a-radio v-for="opt in CONDITION_OPTIONS" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item field="price" label="售价 (¥)" required>
                <a-input-number
                  v-model="form.price"
                  :min="0.01"
                  :max="99999"
                  :precision="2"
                  placeholder="0.00"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item field="originalPrice" label="原价 (¥)">
                <a-input-number
                  v-model="form.originalPrice"
                  :min="0"
                  :max="999999"
                  :precision="2"
                  placeholder="选填"
                  style="width: 100%"
                >
                  <template #prefix>¥</template>
                </a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item field="quantity" label="数量" required>
                <a-input-number
                  v-model="form.quantity"
                  :min="1"
                  :max="999"
                  placeholder="1"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <div class="step-actions">
            <a-button type="primary" @click="goToStep(2)">下一步</a-button>
          </div>
        </a-form>
      </a-card>

      <a-card title="Step 2: 商品图片" :bordered="false" class="step-card" :class="{ 'step-card--active': currentStep >= 2 }">
        <div class="upload-section">
          <p class="upload-tip">支持 jpg/png/webp 格式，单张不超过 5MB，最多上传 9 张图片。第一张将作为主图。</p>

          <div class="image-grid">
            <div
              v-for="(img, index) in form.imageUrls"
              :key="index"
              class="image-item"
            >
              <img :src="img" class="preview-image" />
              <div v-if="index === 0" class="main-badge">主图</div>
              <button class="remove-btn" @click="removeImage(index)">
                <icon-close />
              </button>
            </div>

            <div
              v-if="form.imageUrls.length < 9"
              class="upload-trigger"
              @click="triggerUpload"
            >
              <icon-plus />
              <span>上传图片</span>
            </div>
          </div>

          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            multiple
            style="display: none"
            @change="handleFileChange"
          />
        </div>

        <div class="step-actions">
          <a-button @click="currentStep = 1">上一步</a-button>
          <a-button type="primary" @click="goToStep(3)">下一步</a-button>
        </div>
      </a-card>

      <a-card title="Step 3: 详细描述" :bordered="false" class="step-card" :class="{ 'step-card--active': currentStep >= 3 }">
        <a-form :model="form" layout="vertical">
          <a-form-item field="description" label="详细描述">
            <a-textarea
              v-model="form.description"
              placeholder="请详细描述商品的 brand/型号、购买时间、使用情况、转手原因等信息..."
              :max-length="2000"
              show-word-limit
              :auto-size="{ minRows: 6, maxRows: 12 }"
            />
          </a-form-item>
        </a-form>

        <div class="submit-actions">
          <a-button @click="currentStep = 2">上一步</a-button>
          <a-button @click="handleSaveDraft" :loading="saving">保存草稿</a-button>
          <a-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            {{ editingItem ? '提交修改' : '提交审核' }}
          </a-button>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconPlus,
  IconClose,
} from "@arco-design/web-vue/es/icon";
import { publishItem, updateItem, uploadImage, getItemDetail } from "../../services/api";

const router = useRouter();
const route = useRoute();

const currentStep = ref(1);
const formRef1 = ref(null);
const fileInput = ref(null);
const submitting = ref(false);
const saving = ref(false);
const editingItem = ref(null);

const CATEGORY_OPTIONS = [
  { value: "electronics", label: "📱 数码电子" },
  { value: "textbooks", label: "📚 教材书籍" },
  { value: "clothing", label: "👕 服饰鞋包" },
  { value: "daily", label: "🧴 生活用品" },
  { value: "sports", label: "⚽ 运动户外" },
  { value: "furniture", label: "🪑 家居家具" },
  { value: "transport", label: "🚲 出行工具" },
  { value: "others", label: "📦 其他物品" },
];

const CONDITION_OPTIONS = [
  { value: "brand_new", label: "全新" },
  { value: "like_new_99", label: "99新" },
  { value: "like_new_95", label: "95新" },
  { value: "good_90", label: "9成新" },
  { value: "fair_80", label: "8成新" },
  { value: "poor_70", label: "7成新及以下" },
];

const rules1 = {
  title: [{ required: true, message: "请输入商品标题" }],
  category: [{ required: true, message: "请选择分类" }],
  price: [{ required: true, message: "请输入价格" }, { type: "number", min: 0.01, message: "价格必须大于0" }],
  quantity: [{ required: true, message: "请输入数量" }, { type: "number", min: 1, message: "数量至少为1" }],
};

const form = reactive({
  title: "",
  category: "",
  conditionLevel: "good_90",
  price: null,
  originalPrice: null,
  quantity: 1,
  imageUrls: [],
  description: "",
});

async function loadEditingItem() {
  const id = route.query.id;
  if (!id) return;

  try {
    const res = await getItemDetail(id);
    editingItem.value = res;
    Object.assign(form, {
      title: res.title || "",
      category: res.category || "",
      conditionLevel: res.conditionLevel || "good_90",
      price: res.price || null,
      originalPrice: res.originalPrice || null,
      quantity: res.quantity || 1,
      imageUrls: res.imageUrls || [],
      description: res.description || "",
    });
  } catch (e) {
    Message.error("加载商品信息失败");
  }
}

function goToStep(step) {
  if (step === 2) {
    formRef1.value?.validate().then(() => {
      currentStep.value = step;
    }).catch(() => {
      Message.warning("请先完善基本信息");
    });
  } else {
    currentStep.value = step;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

function triggerUpload() {
  fileInput.value?.click();
}

async function handleFileChange(e) {
  const files = Array.from(e.target.files);
  if (files.length === 0) return;

  const validFiles = files.filter((f) => f.size <= 5 * 1024 * 1024);
  if (validFiles.length !== files.length) {
    Message.warning("部分图片超过5MB限制，已自动过滤");
  }

  for (const file of validFiles) {
    if (form.imageUrls.length >= 9) {
      Message.warning("最多上传9张图片");
      break;
    }

    try {
      const res = await uploadImage(file);
      if (res?.url) {
        form.imageUrls.push(res.url);
      } else if (typeof res === 'string') {
        form.imageUrls.push(res);
      }
    } catch (err) {
      console.error("上传失败:", err);
    }
  }

  e.target.value = "";
}

function removeImage(index) {
  form.imageUrls.splice(index, 1);
}

async function handleSaveDraft() {
  saving.value = true;
  try {
    const data = { ...form, reviewStatus: "DRAFT" };
    if (editingItem.value) {
      await updateItem(editingItem.value.id, data);
      Message.success("草稿已保存");
    } else {
      await publishItem(data);
      Message.success("草稿已保存");
      router.push("/seller/items");
    }
  } catch (e) {
    Message.error(e.message || "保存失败");
  } finally {
    saving.value = false;
  }
}

async function handleSubmit() {
  submitting.value = true;
  try {
    await formRef1.value?.validate();

    const data = {
      ...form,
      reviewStatus: route.query.reaudit ? "PENDING_REVIEW" : "PENDING_REVIEW",
    };

    if (editingItem.value) {
      await updateItem(editingItem.value.id, data);
      Message.success("修改成功，已重新进入审核");
    } else {
      await publishItem(data);
      Message.success("发布成功，已进入审核队列！");
    }

    router.push("/seller/items");
  } catch (e) {
    if (e.message) Message.error(e.message);
  } finally {
    submitting.value = false;
  }
}

onMounted(loadEditingItem);
</script>

<style lang="scss" scoped>
.publish-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f6f8 0%, #ffffff 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
  }
}

.publish-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.step-card {
  border-radius: 12px;
  opacity: 0.6;
  transition: all 0.3s;

  &--active {
    opacity: 1;
  }

  :deep(.arco-card-head-title) {
    font-size: 15px;
    font-weight: 600;
  }

  :deep(.arco-card-body) {
    padding: 24px;
  }
}

.upload-tip {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 16px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #f0f0f0;

  .preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .main-badge {
    position: absolute;
    top: 6px;
    left: 6px;
    padding: 2px 8px;
    background: #165dff;
    color: white;
    font-size: 11px;
    border-radius: 4px;
  }

  .remove-btn {
    position: absolute;
    top: 6px;
    right: 6px;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: rgba(245, 63, 63, 0.9);
    color: white;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;

    &:hover { background: #f53f3f; }
  }
}

.upload-trigger {
  aspect-ratio: 1;
  border: 2px dashed #d9d9d9;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #86909c;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    border-color: #165dff;
    color: #165dff;
    background: #f0f5ff;
  }

  :deep(.arco-icon) { font-size: 28px; }
}

.step-actions,
.submit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  margin-top: 20px;
}
</style>
