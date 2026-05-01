<template>
  <div class="address-list-page">
    <div class="page-header">
      <a-button @click="$router.back()" type="text">
        <template #icon><icon-arrow-left /></template>
        返回
      </a-button>
      <h2 class="page-title">收货地址</h2>
      <a-button type="primary" @click="openAddressForm()">
        <template #icon><icon-plus /></template>
        新增地址
      </a-button>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="addresses.length > 0" class="address-grid">
        <div
          v-for="address in addresses"
          :key="address.id"
          class="address-card"
          :class="{ 'address-card--default': address.isDefault }"
        >
          <div v-if="address.isDefault" class="address-card__badge">默认地址</div>

          <div class="address-card__info">
            <div class="address-card__receiver">
              <span class="receiver-name">{{ address.receiverName }}</span>
              <span class="receiver-phone">{{ formatPhone(address.receiverPhone) }}</span>
            </div>
            <div class="address-card__detail">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
            </div>
          </div>

          <div class="address-card__actions">
            <a-button type="text" size="small" @click="openAddressForm(address)">
              <template #icon><icon-edit /></template>
              编辑
            </a-button>
            <a-button
              v-if="!address.isDefault"
              type="text"
              size="small"
              @click="handleSetDefault(address)"
            >
              <template #icon><icon-star /></template>
              设为默认
            </a-button>
            <a-button
              v-else
              type="text"
              size="small"
              status="warning"
              disabled
            >
              <template #icon><icon-check /></template>
              默认地址
            </a-button>
            <a-popconfirm
              content="确定要删除此收货地址吗？"
              @ok="handleDelete(address)"
            >
              <a-button type="text" size="small" status="danger">
                <template #icon><icon-delete /></template>
                删除
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>

      <a-empty v-else-if="!loading" description="暂无收货地址">
        <template #image>
          <icon-home size="48" />
        </template>
        <a-button type="primary" @click="openAddressForm()">添加收货地址</a-button>
      </a-empty>
    </a-spin>

    <!-- 地址编辑弹窗 -->
    <a-modal
      v-model:visible="formVisible"
      :title="editingAddress ? '编辑收货地址' : '新增收货地址'"
      @before-ok="handleSubmit"
      @cancel="formVisible = false"
      :width="520"
    >
      <a-form :model="form" layout="vertical" ref="formRef">
        <a-form-item field="receiverName" label="收货人" required>
          <a-input v-model="form.receiverName" placeholder="请输入收货人姓名" :max-length="20" />
        </a-form-item>

        <a-form-item field="receiverPhone" label="联系电话" required>
          <a-input v-model="form.receiverPhone" placeholder="请输入手机号码" :max-length="11" />
        </a-form-item>

        <a-form-item field="province" label="所在地区" required>
          <a-cascader
            v-model="form.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            allow-search
            :style="{ width: '100%' }"
          />
        </a-form-item>

        <a-form-item field="detailAddress" label="详细地址" required>
          <a-textarea
            v-model="form.detailAddress"
            placeholder="请输入详细地址，如街道、门牌号等"
            :max-length="200"
            :rows="2"
          />
        </a-form-item>

        <a-form-item field="postalCode" label="邮政编码">
          <a-input v-model="form.postalCode" placeholder="请输入邮政编码（选填）" :max-length="6" />
        </a-form-item>

        <a-form-item field="isDefault" no-label>
          <a-checkbox v-model="form.isDefault">设为默认收货地址</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconPlus,
  IconEdit,
  IconStar,
  IconCheck,
  IconDelete,
  IconHome,
} from "@arco-design/web-vue/es/icon";
import {
  getAddressList,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
} from "../../services/api";

const loading = ref(false);
const addresses = ref([]);
const formVisible = ref(false);
const editingAddress = ref(null);

const form = ref({
  receiverName: "",
  receiverPhone: "",
  region: [],
  detailAddress: "",
  postalCode: "",
  isDefault: false,
});

const regionOptions = [
  {
    value: "北京市",
    label: "北京市",
    children: [
      { value: "市辖区", label: "市辖区", children: [
        { value: "海淀区", label: "海淀区" },
        { value: "朝阳区", label: "朝阳区" },
        { value: "东城区", label: "东城区" },
        { value: "西城区", label: "西城区" },
        { value: "丰台区", label: "丰台区" },
        { value: "石景山区", label: "石景山区" },
        { value: "通州区", label: "通州区" },
        { value: "昌平区", label: "昌平区" },
      ]},
    ],
  },
  {
    value: "上海市",
    label: "上海市",
    children: [
      { value: "市辖区", label: "市辖区", children: [
        { value: "黄浦区", label: "黄浦区" },
        { value: "徐汇区", label: "徐汇区" },
        { value: "静安区", label: "静安区" },
        { value: "浦东新区", label: "浦东新区" },
        { value: "闵行区", label: "闵行区" },
        { value: "杨浦区", label: "杨浦区" },
      ]},
    ],
  },
  {
    value: "广东省",
    label: "广东省",
    children: [
      { value: "广州市", label: "广州市", children: [
        { value: "天河区", label: "天河区" },
        { value: "海珠区", label: "海珠区" },
        { value: "越秀区", label: "越秀区" },
        { value: "白云区", label: "白云区" },
        { value: "番禺区", label: "番禺区" },
      ]},
      { value: "深圳市", label: "深圳市", children: [
        { value: "南山区", label: "南山区" },
        { value: "福田区", label: "福田区" },
        { value: "罗湖区", label: "罗湖区" },
        { value: "宝安区", label: "宝安区" },
        { value: "龙岗区", label: "龙岗区" },
      ]},
    ],
  },
  {
    value: "浙江省",
    label: "浙江省",
    children: [
      { value: "杭州市", label: "杭州市", children: [
        { value: "西湖区", label: "西湖区" },
        { value: "上城区", label: "上城区" },
        { value: "拱墅区", label: "拱墅区" },
        { value: "滨江区", label: "滨江区" },
      ]},
      { value: "宁波市", label: "宁波市", children: [
        { value: "海曙区", label: "海曙区" },
        { value: "江北区", label: "江北区" },
      ]},
    ],
  },
  {
    value: "江苏省",
    label: "江苏省",
    children: [
      { value: "南京市", label: "南京市", children: [
        { value: "玄武区", label: "玄武区" },
        { value: "秦淮区", label: "秦淮区" },
        { value: "鼓楼区", label: "鼓楼区" },
        { value: "栖霞区", label: "栖霞区" },
      ]},
      { value: "苏州市", label: "苏州市", children: [
        { value: "姑苏区", label: "姑苏区" },
        { value: "虎丘区", label: "虎丘区" },
        { value: "工业园区", label: "工业园区" },
      ]},
    ],
  },
];

function formatPhone(phone) {
  if (!phone) return "";
  return phone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
}

async function loadAddresses() {
  loading.value = true;
  try {
    const res = await getAddressList();
    addresses.value = res || [];
  } catch (e) {
    Message.error(e.message || "加载收货地址失败");
  } finally {
    loading.value = false;
  }
}

function openAddressForm(address = null) {
  editingAddress.value = address;
  if (address) {
    const region = [address.province, address.city, address.district].filter(Boolean);
    form.value = {
      receiverName: address.receiverName,
      receiverPhone: address.receiverPhone,
      region: region,
      detailAddress: address.detailAddress,
      postalCode: address.postalCode || "",
      isDefault: address.isDefault,
    };
  } else {
    form.value = {
      receiverName: "",
      receiverPhone: "",
      region: [],
      detailAddress: "",
      postalCode: "",
      isDefault: false,
    };
  }
  formVisible.value = true;
}

async function handleSubmit(done) {
  if (!form.value.receiverName) {
    Message.error("请输入收货人姓名");
    done(false);
    return;
  }
  if (!form.value.receiverPhone) {
    Message.error("请输入联系电话");
    done(false);
    return;
  }
  if (!form.value.region || form.value.region.length === 0) {
    Message.error("请选择所在地区");
    done(false);
    return;
  }
  if (!form.value.detailAddress) {
    Message.error("请输入详细地址");
    done(false);
    return;
  }

  try {
    const data = {
      receiverName: form.value.receiverName,
      receiverPhone: form.value.receiverPhone,
      province: form.value.region[0] || "",
      city: form.value.region[1] || "",
      district: form.value.region[2] || "",
      detailAddress: form.value.detailAddress,
      postalCode: form.value.postalCode,
      isDefault: form.value.isDefault,
    };

    if (editingAddress.value) {
      await updateAddress(editingAddress.value.id, data);
      Message.success("修改成功");
    } else {
      await createAddress(data);
      Message.success("添加成功");
    }

    formVisible.value = false;
    await loadAddresses();
    done(true);
  } catch (e) {
    Message.error(e.message || "操作失败");
    done(false);
  }
}

async function handleSetDefault(address) {
  try {
    await setDefaultAddress(address.id);
    Message.success("设置默认地址成功");
    await loadAddresses();
  } catch (e) {
    Message.error(e.message || "设置失败");
  }
}

async function handleDelete(address) {
  try {
    await deleteAddress(address.id);
    Message.success("删除成功");
    await loadAddresses();
  } catch (e) {
    Message.error(e.message || "删除失败");
  }
}

onMounted(loadAddresses);
</script>

<style lang="scss" scoped>
.address-list-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%);
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
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.06);

  .page-title {
    flex: 1;
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.address-card {
  position: relative;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;

  &:hover {
    border-color: #7c3aed;
    box-shadow: 0 4px 12px rgba(124, 58, 237, 0.1);
  }

  &--default {
    border-color: #7c3aed;
    background: linear-gradient(135deg, rgba(124, 58, 237, 0.03) 0%, white 100%);
  }

  &__badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 4px 10px;
    background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
    color: white;
    font-size: 12px;
    border-radius: 20px;
  }

  &__info {
    margin-bottom: 16px;
  }

  &__receiver {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .receiver-name {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }

    .receiver-phone {
      font-size: 14px;
      color: #6b7280;
    }
  }

  &__detail {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.5;
  }

  &__actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}

:deep(.arco-empty) {
  padding: 60px 20px;
  background: white;
  border-radius: 12px;

  .arco-empty-icon {
    color: #d1d5db;
  }
}

:deep(.arco-form-item-label) {
  font-weight: 500;
}

:deep(.arco-modal) {
  border-radius: 16px;
  overflow: hidden;
}
</style>
