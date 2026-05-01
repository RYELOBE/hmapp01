<template>
  <div class="publish-page">
    <div class="publish-page__header">
      <h2>发布闲置</h2>
      <p>填写商品信息，审核通过后将展示给全校同学</p>
    </div>

    <a-form :model="form" layout="vertical" ref="formRef" class="publish-page__form">
      <!-- 图片上传 -->
      <a-form-item label="商品图片（最多6张）" field="imageUrls" :rules="[{ required: true, message: '请至少上传一张图片' }]">
        <a-upload
          list-type="picture-card"
          :file-list="fileList"
          :limit="6"
          :custom-request="handleUpload"
          accept="image/*"
          image-preview
          :multiple="true"
          @success="onUploadSuccess"
          @remove="onRemove"
        />
      </a-form-item>

      <!-- 标题 -->
      <a-form-item label="商品名称" field="title" :rules="[{ required: true, message: '请输入商品名称' }]">
        <a-input v-model="form.title" placeholder="如：九成新机械键盘 Cherry轴" :max-length="50" show-word-limit />
      </a-form-item>

      <!-- 价格 + 分类 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="价格（元）" field="price" :rules="[{ required: true, message: '请输入价格' }]">
            <a-input-number v-model="form.price" :min="0.01" :precision="2" placeholder="0.00" style="width: 100%">
              <template #prefix>¥</template>
            </a-input-number>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="分类" field="category" :rules="[{ required: true, message: '请选择分类' }]">
            <a-select v-model="form.category" placeholder="选择分类">
              <a-option v-for="cat in CATEGORY_OPTIONS" :key="cat.value" :value="cat.value">{{ cat.label }}</a-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 成色 -->
      <a-form-item label="成色" field="conditionLevel">
        <a-radio-group v-model="form.conditionLevel" type="button">
          <a-radio v-for="opt in CONDITION_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 描述 -->
      <a-form-item label="详细描述" field="description">
        <a-textarea
          v-model="form.description"
          placeholder="描述一下商品的使用情况、购买时间、出手原因等，让买家更了解你的宝贝..."
          :max-length="500"
          show-word-limit
          :auto-size="{ minRows: 4, maxRows: 8 }"
        />
      </a-form-item>

      <!-- 提交 -->
      <a-form-item>
        <a-space>
          <a-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交审核</a-button>
          <a-button size="large" @click="handleReset">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { publishItem, uploadImage } from "../../services/api";

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const fileList = ref([]);

const CATEGORY_OPTIONS = [
  { value: "digital", label: "📱 数码" },
  { value: "book", label: "📚 教材" },
  { value: "clothing", label: "👕 服饰" },
  { value: "daily", label: "🧴 生活" },
  { value: "sport", label: "⚽ 运动" },
  { value: "instrument", label: "🎸 乐器" },
  { value: "other", label: "📦 其他" },
];

const CONDITION_OPTIONS = [
  { value: "new", label: "全新" },
  { value: "99", label: "99新" },
  { value: "95", label: "95新" },
  { value: "9", label: "9成新" },
  { value: "8", label: "8成新" },
  { value: "7", label: "7成新" },
];

const form = reactive({
  title: "",
  price: null,
  category: "",
  conditionLevel: "9",
  description: "",
  imageUrls: [],
});

async function handleUpload({ file, onSuccess, onError }) {
  try {
    const res = await uploadImage(file);
    form.imageUrls.push(res.url || res);
    onSuccess(res);
  } catch (e) {
    onError(e);
    Message.error("图片上传失败");
  }
}

function onUploadSuccess(fileItem) {
  // handled in handleUpload
}

function onRemove(fileItem) {
  const idx = fileList.value.findIndex(f => f.uid === fileItem.uid);
  if (idx > -1) {
    form.imageUrls.splice(idx, 1);
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate();
  if (valid) return;

  submitting.value = true;
  try {
    await publishItem({
      ...form,
      imageUrls: form.imageUrls,
    });
    Message.success("发布成功，已进入审核队列！");
    router.push("/seller/items");
  } catch (e) {
    Message.error(e.message || "发布失败");
  } finally {
    submitting.value = false;
  }
}

function handleReset() {
  formRef.value?.resetFields();
  form.imageUrls = [];
  fileList.value = [];
}
</script>

<style lang="scss" scoped>
.publish-page {
  background: #fff;
  border-radius: 16px;
  padding: 32px;

  &__header {
    margin-bottom: 28px;
    h2 {
      margin: 0 0 6px;
      font-size: 22px;
      font-weight: 700;
      color: #1d2129;
    }
    p {
      margin: 0;
      font-size: 14px;
      color: #86909c;
    }
  }

  &__form {
    max-width: 680px;
  }
}
</style>
