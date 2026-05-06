<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑收货地址' : '新增收货地址'"
    @before-ok="handleSubmit"
    @cancel="handleCancel"
    :width="520"
    :mask-closable="false"
  >
    <a-form :model="form" layout="vertical" ref="formRef">
      <a-form-item field="receiverName" label="收货人" required>
        <a-input
          v-model="form.receiverName"
          placeholder="请输入收货人姓名"
          :max-length="20"
        />
      </a-form-item>

      <a-form-item field="receiverPhone" label="联系电话" required>
        <a-input
          v-model="form.receiverPhone"
          placeholder="请输入手机号码"
          :max-length="11"
        />
      </a-form-item>

      <a-form-item field="region" label="所在地区" required>
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
        <a-input
          v-model="form.postalCode"
          placeholder="请输入邮政编码（选填）"
          :max-length="6"
        />
      </a-form-item>

      <a-form-item field="isDefault" no-label>
        <a-checkbox v-model="form.isDefault">设为默认收货地址</a-checkbox>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { Message } from '@arco-design/web-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  address: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'submit', 'cancel']);

const formRef = ref(null);
const form = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  postalCode: '',
  isDefault: false
});

const isEdit = computed(() => !!props.address);

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initForm();
  }
});

function initForm() {
  if (props.address) {
    form.receiverName = props.address.receiverName || '';
    form.receiverPhone = props.address.receiverPhone || '';
    form.region = [props.address.province, props.address.city, props.address.district].filter(Boolean);
    form.detailAddress = props.address.detailAddress || '';
    form.postalCode = props.address.postalCode || '';
    form.isDefault = props.address.isDefault || false;
  } else {
    form.receiverName = '';
    form.receiverPhone = '';
    form.region = [];
    form.detailAddress = '';
    form.postalCode = '';
    form.isDefault = false;
  }
}

function validate() {
  if (!form.receiverName) {
    Message.error('请输入收货人姓名');
    return false;
  }
  if (!form.receiverPhone) {
    Message.error('请输入联系电话');
    return false;
  }
  if (!form.region || form.region.length === 0) {
    Message.error('请选择所在地区');
    return false;
  }
  if (!form.detailAddress) {
    Message.error('请输入详细地址');
    return false;
  }
  return true;
}

function handleSubmit(done) {
  if (!validate()) {
    done(false);
    return;
  }

  const data = {
    receiverName: form.receiverName,
    receiverPhone: form.receiverPhone,
    province: form.region[0] || '',
    city: form.region[1] || '',
    district: form.region[2] || '',
    detailAddress: form.detailAddress,
    postalCode: form.postalCode,
    isDefault: form.isDefault
  };

  if (props.address?.id) {
    data.id = props.address.id;
  }

  emit('submit', data);
  done(true);
}

function handleCancel() {
  emit('update:visible', false);
  emit('cancel');
}

const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '市辖区',
        label: '市辖区',
        children: [
          { value: '海淀区', label: '海淀区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '市辖区',
        label: '市辖区',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '静安区', label: '静安区' },
          { value: '浦东新区', label: '浦东新区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '海珠区', label: '海珠区' },
          { value: '越秀区', label: '越秀区' },
          { value: '白云区', label: '白云区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' }
        ]
      }
    ]
  },
  {
    value: '浙江省',
    label: '浙江省',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [
          { value: '西湖区', label: '西湖区' },
          { value: '上城区', label: '上城区' },
          { value: '拱墅区', label: '拱墅区' }
        ]
      }
    ]
  }
];
</script>

<style lang="scss" scoped>
:deep(.arco-form-item-label) {
  font-weight: 500;
}

:deep(.arco-modal) {
  border-radius: 16px;
  overflow: hidden;
}
</style>
