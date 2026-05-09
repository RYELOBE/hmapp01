<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <section class="profile-header">
      <div class="user-card">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="$refs.avatarUploader?.querySelector('input')?.click()">
            <a-avatar :size="100" :image-url="avatarUrl || user?.avatar" class="user-avatar">
              {{ userInitial }}
            </a-avatar>
            <div class="avatar-overlay" v-if="!uploadingAvatar">
              <icon-camera size="20" />
              <span>更换头像</span>
            </div>
            <a-spin v-else :loading="uploadingAvatar" class="avatar-loading" />
          </div>
          
          <!-- 隐藏的上传组件 -->
          <div ref="avatarUploader" style="display: none;">
            <ImageUploader
              v-model="avatarUrl"
              :limit="1"
              upload-url="/api/upload"
              @update:modelValue="handleAvatarChange"
            />
          </div>
          
          <h2 class="user-name">{{ user?.nickname || user?.username || '用户' }}</h2>
          <p class="user-roles">
            <a-tag 
              v-for="role in (user?.roles || [])" 
              :key="role" 
              :color="getRoleColor(role)"
              class="role-tag"
            >
              {{ getRoleName(role) }}
            </a-tag>
          </p>
        </div>

        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ stats.totalOrders || 0 }}</span>
            <span class="stat-label">订单数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.totalItems || 0 }}</span>
            <span class="stat-label">商品数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.favorites || 0 }}</span>
            <span class="stat-label">收藏数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.joinDays || 0 }}</span>
            <span class="stat-label">加入天数</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-actions">
      <div class="action-cards">
        <div class="action-card" @click="$router.push('/portal/orders')">
          <icon-file size="28" />
          <span>我的订单</span>
        </div>
        <div class="action-card" @click="$router.push('/portal/favorites')">
          <icon-star-fill size="28" />
          <span>我的收藏</span>
        </div>
        <div class="action-card" v-if="isSeller" @click="$router.push('/portal/seller/items')">
          <icon-apps size="28" />
          <span>我的商品</span>
        </div>
        <div class="action-card" v-if="isSeller" @click="$router.push('/portal/seller/publish')">
          <icon-plus-circle size="28" />
          <span>发布商品</span>
        </div>
        <div class="action-card" @click="$router.push('/portal/addresses')">
          <icon-location size="28" />
          <span>收货地址</span>
        </div>
      </div>
    </section>

    <!-- 个人信息编辑 -->
    <section class="info-section">
      <a-card title="基本信息" :bordered="false" class="info-card">
        <a-form
          :model="profileForm"
          layout="vertical"
          class="profile-form"
          @submit="handleSaveProfile"
        >
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="用户名">
                <div style="display: flex; gap: 8px;">
                  <a-input
                    v-model="profileForm.username"
                    disabled
                    placeholder="用户名不可直接修改"
                    style="flex: 1;"
                  />
                  <a-button type="outline" size="small" @click="showChangeUsername = true">
                    申请修改
                  </a-button>
                </div>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="昵称">
                <a-input
                  v-model="profileForm.nickname"
                  placeholder="设置您的昵称"
                  allow-clear
                >
                  <template #prefix>
                    <icon-user style="color: #86909C;" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="手机号码">
                <a-input
                  v-model="profileForm.phone"
                  placeholder="绑定手机号"
                  allow-clear
                >
                  <template #prefix>
                    <icon-phone style="color: #86909C;" />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="邮箱地址">
                <div style="display: flex; gap: 8px;">
                  <a-input
                    v-model="profileForm.email"
                    placeholder="绑定邮箱（可选）"
                    allow-clear
                    style="flex: 1;"
                  >
                    <template #prefix>
                      <icon-email style="color: #86909C;" />
                    </template>
                  </a-input>
                  <a-button type="outline" size="small" @click="showChangeEmail = true">
                    {{ profileForm.email ? '更换' : '绑定' }}
                  </a-button>
                </div>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="所属校区">
                <a-select
                  v-model="profileForm.campus"
                  placeholder="选择您的校区"
                  allow-clear
                >
                  <a-option value="主校区">主校区</a-option>
                  <a-option value="东校区">东校区</a-option>
                  <a-option value="西校区">西校区</a-option>
                  <a-option value="南校区">南校区</a-option>
                  <a-option value="北校区">北校区</a-option>
                  <a-option value="新校区">新校区</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label=" ">
                <div style="padding-top: 8px; color: #86909C; font-size: 13px;">
                  <icon-info-circle style="margin-right: 4px;" />
                  校区信息用于推荐同校商品
                </div>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="个人简介">
            <a-textarea 
              v-model="profileForm.bio" 
              placeholder="介绍一下自己吧..."
              :max-length="200"
              show-word-limit
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="saving">
                保存修改
              </a-button>
              <a-button @click="resetForm">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </section>

    <!-- 安全设置 -->
    <section class="security-section">
      <a-card title="安全设置" :bordered="false" class="security-card">
        <div class="security-items">
          <div class="security-item">
            <div class="security-info">
              <icon-lock size="20" />
              <div>
                <h4>修改密码</h4>
                <p>定期更换密码可以保护账号安全</p>
              </div>
            </div>
            <a-button type="outline" @click="showChangePassword = true">
              修改
            </a-button>
          </div>

          <div class="security-item">
            <div class="security-info">
              <icon-mobile size="20" />
              <div>
                <h4>手机绑定</h4>
                <p>{{ profileForm.phone ? `已绑定: ${maskPhone(profileForm.phone)}` : '未绑定手机号' }}</p>
              </div>
            </div>
            <a-button type="outline" @click="openBindPhone">
              {{ profileForm.phone ? '更换' : '绑定' }}
            </a-button>
          </div>
        </div>
      </a-card>
    </section>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:visible="showChangePassword"
      title="修改密码"
      :footer="false"
      width="480px"
    >
      <a-form
        :model="passwordForm"
        layout="vertical"
        @submit="handleChangePassword"
      >
        <a-form-item field="oldPassword" label="当前密码" :rules="[{ required: true, message: '请输入当前密码' }]">
          <a-input-password v-model="passwordForm.oldPassword" placeholder="请输入当前密码" />
        </a-form-item>
        
        <a-form-item field="newPassword" label="新密码" :rules="[
          { required: true, message: '请输入新密码' },
          { minLength: 6, message: '密码至少6位' }
        ]">
          <a-input-password v-model="passwordForm.newPassword" placeholder="请输入新密码（至少6位）" />
        </a-form-item>
        
        <a-form-item field="confirmPassword" label="确认密码" :rules="[
          { required: true, message: '请确认新密码' },
          { validator: (value, callback) => {
              if (value !== passwordForm.newPassword) {
                callback('两次密码不一致');
              } else {
                callback();
              }
            }
          }
        ]">
          <a-input-password v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>

        <div class="modal-actions">
          <a-button @click="showChangePassword = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="changingPwd">确认修改</a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 手机绑定/更换弹窗 -->
    <a-modal
      v-model:visible="showBindPhone"
      :title="profileForm.phone ? '更换手机号' : '绑定手机号'"
      :footer="false"
      width="480px"
    >
      <a-form
        :model="phoneForm"
        layout="vertical"
        @submit="handleBindPhone"
      >
        <a-alert v-if="profileForm.phone" type="warning" style="margin-bottom: 16px;">
          当前已绑定手机：{{ maskPhone(profileForm.phone) }}
        </a-alert>

        <a-form-item field="phone" label="手机号码" :rules="[
          { required: true, message: '请输入手机号码' },
          { match: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式' }
        ]">
          <a-input v-model="phoneForm.phone" placeholder="请输入11位手机号" :max-length="11">
            <template #prefix>
              <icon-mobile style="color: #86909C;" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="verifyCode" label="验证码" :rules="[
          { required: true, message: '请输入验证码' },
          { minLength: 6, message: '验证码为6位数字' }
        ]">
          <div style="display: flex; gap: 12px;">
            <a-input
              v-model="phoneForm.verifyCode"
              placeholder="请输入6位验证码"
              :max-length="6"
              style="flex: 1;"
            />
            <a-button
              type="outline"
              :disabled="phoneCountdown > 0"
              @click="sendVerifyCode"
              style="min-width: 120px;"
            >
              {{ phoneCountdown > 0 ? `${phoneCountdown}s后重发` : '发送验证码' }}
            </a-button>
          </div>
        </a-form-item>

        <div class="modal-actions">
          <a-button @click="showBindPhone = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="bindingPhone">{{ profileForm.phone ? '确认更换' : '确认绑定' }}</a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 邮箱绑定/更换弹窗 -->
    <a-modal
      v-model:visible="showChangeEmail"
      :title="profileForm.email ? '更换邮箱' : '绑定邮箱'"
      :footer="false"
      width="480px"
    >
      <a-form
        :model="emailForm"
        layout="vertical"
        @submit="handleChangeEmail"
      >
        <a-alert v-if="profileForm.email" type="warning" style="margin-bottom: 16px;">
          当前已绑定邮箱：{{ profileForm.email }}
        </a-alert>

        <a-form-item field="email" label="邮箱地址" :rules="[
          { required: true, message: '请输入邮箱地址' },
          { type: 'email', message: '请输入正确的邮箱格式' }
        ]">
          <a-input v-model="emailForm.email" placeholder="请输入邮箱地址">
            <template #prefix>
              <icon-email style="color: #86909C;" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="verifyCode" label="验证码" :rules="[
          { required: true, message: '请输入验证码' },
          { minLength: 6, message: '验证码为6位数字' }
        ]">
          <div style="display: flex; gap: 12px;">
            <a-input
              v-model="emailForm.verifyCode"
              placeholder="请输入6位验证码"
              :max-length="6"
              style="flex: 1;"
            />
            <a-button
              type="outline"
              :disabled="emailCountdown > 0"
              @click="sendEmailVerifyCode"
              style="min-width: 120px;"
            >
              {{ emailCountdown > 0 ? `${emailCountdown}s后重发` : '发送验证码' }}
            </a-button>
          </div>
        </a-form-item>

        <div class="modal-actions">
          <a-button @click="showChangeEmail = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="changingEmail">{{ profileForm.email ? '确认更换' : '确认绑定' }}</a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 用户名修改申请弹窗 -->
    <a-modal
      v-model:visible="showChangeUsername"
      title="申请修改用户名"
      :footer="false"
      width="480px"
    >
      <a-alert type="info" style="margin-bottom: 16px;">
        <template #icon><icon-info-circle /></template>
        用户名修改需要运营审核，审核通过后生效。当前用户名：{{ profileForm.username }}
      </a-alert>

      <a-form
        :model="usernameForm"
        layout="vertical"
        @submit="handleSubmitUsername"
      >
        <a-form-item field="newUsername" label="新用户名" :rules="[
          { required: true, message: '请输入新用户名' },
          { minLength: 3, message: '用户名至少3个字符' },
          { maxLength: 20, message: '用户名不超过20个字符' },
          { match: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
        ]">
          <a-input v-model="usernameForm.newUsername" placeholder="请输入新的用户名（英文/数字）" :max-length="20" />
        </a-form-item>

        <a-form-item field="reason" label="修改原因" :rules="[
          { required: true, message: '请填写修改原因' }
        ]">
          <a-textarea
            v-model="usernameForm.reason"
            placeholder="请说明为什么要修改用户名..."
            :max-length="200"
            show-word-limit
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>

        <div class="modal-actions">
          <a-button @click="showChangeUsername = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="submittingUsername">提交申请</a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useAuthStore } from "../../stores/auth";
import { http } from "../../services/http";
import { Message } from "@arco-design/web-vue";
import {
  IconUser,
  IconPhone,
  IconEmail,
  IconLock,
  IconMobile,
  IconFile,
  IconStarFill,
  IconApps,
  IconPlusCircle,
  IconLocation,
  IconCamera,
  IconInfoCircle,
} from "@arco-design/web-vue/es/icon";
import ImageUploader from "../../components/form/ImageUploader/ImageUploader.vue";

const authStore = useAuthStore();
const saving = ref(false);
const changingPwd = ref(false);
const showChangePassword = ref(false);
const showBindPhone = ref(false);  // 手机绑定弹窗
const showChangeEmail = ref(false);  // 邮箱修改弹窗
const showChangeUsername = ref(false);  // 用户名修改弹窗
const bindingPhone = ref(false);   // 绑定中状态
const changingEmail = ref(false);   // 邮箱修改中
const submittingUsername = ref(false);  // 用户名申请中
const phoneCountdown = ref(0);     // 验证码倒计时
const emailCountdown = ref(0);     // 邮箱验证码倒计时
let phoneTimer = null;             // 倒计时定时器
let emailTimer = null;             // 邮箱倒计时定时器
const uploadingAvatar = ref(false);
const avatarUrl = ref('');

// 用户数据
const user = computed(() => authStore.user);

const userInitial = computed(() => {
  const name = user.value?.nickname || user.value?.username || 'U';
  return name.charAt(0).toUpperCase();
});

const isSeller = computed(() => {
  return user.value?.roles?.includes('SELLER');
});

// 统计数据（从API获取）
const stats = reactive({
  totalOrders: 0,
  totalItems: 0,
  favorites: 0,
  joinDays: 0,
});

// 个人信息表单
const profileForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  bio: '',
  campus: '',
});

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 手机绑定表单
const phoneForm = reactive({
  phone: '',
  verifyCode: '',
});

// 邮箱绑定/更换表单
const emailForm = reactive({
  email: '',
  verifyCode: '',
});

// 用户名修改申请表单
const usernameForm = reactive({
  newUsername: '',
  reason: '',
});

// 初始化页面数据
onMounted(async () => {
  try {
    // 从后端获取最新用户信息
    const response = await http.get('/auth/current');
    if (response.data) {
      const userData = response.data;

      // 更新 authStore
      authStore.user = userData;
      localStorage.setItem('user', JSON.stringify(userData));

      // 填充表单
      profileForm.username = userData.username || '';
      profileForm.nickname = userData.nickname || '';
      profileForm.phone = userData.phone || '';
      profileForm.email = userData.email || '';
      profileForm.bio = userData.bio || '';
      profileForm.campus = userData.campus || '';

      // 初始化头像URL
      avatarUrl.value = userData.avatar || '';

      // 计算加入天数
      if (userData.createdAt) {
        const created = new Date(userData.createdAt);
        const now = new Date();
        stats.joinDays = Math.floor((now - created) / (1000 * 60 * 60 * 24));
      }

      // 获取统计数据（TODO: 实现真实统计API）
      await fetchUserStats();
    }
  } catch (error) {
    console.error('[Profile] 加载用户信息失败:', error);
    Message.error('加载用户信息失败');
  }
});

// 获取用户统计数据
async function fetchUserStats() {
  try {
    // TODO: 调用真实的统计API，例如：
    // const statsData = await http.get('/user/stats');
    // stats.totalOrders = statsData.totalOrders || 0;
    // stats.totalItems = statsData.totalItems || 0;
    // stats.favorites = statsData.favorites || 0;

    // 临时：如果后端没有统计接口，可以保持为0或使用Mock
    console.log('[Profile] 统计数据接口待实现');
  } catch (error) {
    console.error('[Profile] 获取统计失败:', error);
  }
}

function getRoleName(role) {
  const roleMap = {
    'BUYER': '买家',
    'SELLER': '卖家',
    'OPS': '运营',
    'ADMIN': '管理员',
  };
  return roleMap[role] || role;
}

function getRoleColor(role) {
  const colorMap = {
    'BUYER': 'arcoblue',
    'SELLER': 'orangered',
    'OPS': 'purple',
    'ADMIN': 'red',
  };
  return colorMap[role] || 'gray';
}

function maskPhone(phone) {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

async function handleSaveProfile() {
  saving.value = true;
  try {
    const result = await http.put('/auth/profile', {
      nickname: profileForm.nickname || undefined,
      email: profileForm.email || undefined,
      bio: profileForm.bio || undefined,
      phone: profileForm.phone || undefined,
      campus: profileForm.campus || undefined,
    });
    
    // 更新本地store
    if (result.data) {
      authStore.user = result.data;
      localStorage.setItem('user', JSON.stringify(result.data));
    }
    
    Message.success('个人信息保存成功！');
  } catch (error) {
    console.error('[Profile] 保存失败:', error);
    Message.error(error.message || '保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

function resetForm() {
  if (user.value) {
    profileForm.nickname = user.value.nickname || '';
    profileForm.phone = user.value.phone || '';
    profileForm.email = user.value.email || '';
    profileForm.bio = user.value.bio || '';
    profileForm.campus = user.value.campus || '';
  }
  Message.info('已重置为原始信息');
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    Message.warning('请填写完整信息');
    return;
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    Message.error('两次输入的密码不一致');
    return;
  }
  
  changingPwd.value = true;
  try {
    await http.put('/auth/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    
    Message.success('密码修改成功！');
    showChangePassword.value = false;
    
    // 清空表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (error) {
    console.error('[Password] 修改失败:', error);
    Message.error(error.message || '密码修改失败，请检查原密码是否正确');
  } finally {
    changingPwd.value = false;
  }
}

// 打开手机绑定弹窗
function openBindPhone() {
  phoneForm.phone = '';
  phoneForm.verifyCode = '';
  showBindPhone.value = true;
}

// 发送验证码
async function sendVerifyCode() {
  if (!phoneForm.phone) {
    Message.warning('请先输入手机号码');
    return;
  }

  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(phoneForm.phone)) {
    Message.error('请输入正确的手机号格式');
    return;
  }

  try {
    // TODO: 调用发送验证码接口
    // await http.post('/auth/sms/send', { phone: phoneForm.phone });

    // 模拟：直接开始倒计时（实际应等待接口成功）
    Message.success(`验证码已发送至 ${maskPhone(phoneForm.phone)}`);

    // 开始60秒倒计时
    phoneCountdown.value = 60;
    if (phoneTimer) clearInterval(phoneTimer);

    phoneTimer = setInterval(() => {
      phoneCountdown.value--;
      if (phoneCountdown.value <= 0) {
        clearInterval(phoneTimer);
        phoneTimer = null;
      }
    }, 1000);
  } catch (error) {
    console.error('[Phone] 发送验证码失败:', error);
    Message.error(error.message || '发送验证码失败');
  }
}

// 绑定/更换手机号
async function handleBindPhone() {
  if (!phoneForm.phone || !phoneForm.verifyCode) {
    Message.warning('请填写完整信息');
    return;
  }

  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!phoneRegex.test(phoneForm.phone)) {
    Message.error('请输入正确的手机号格式');
    return;
  }

  bindingPhone.value = true;

  try {
    // TODO: 验证验证码并绑定手机
    // await http.post('/auth/sms/verify', { phone: phoneForm.phone, code: phoneForm.verifyCode });
    // 然后更新 profile

    // 直接通过 profile 接口更新手机号（简化流程）
    await http.put('/auth/profile', {
      nickname: profileForm.nickname || undefined,
      email: profileForm.email || undefined,
      bio: profileForm.bio || undefined,
      campus: profileForm.campus || undefined,
      phone: phoneForm.phone,
    });

    // 更新本地数据
    profileForm.phone = phoneForm.phone;
    if (authStore.user) {
      authStore.user.phone = phoneForm.phone;
      localStorage.setItem('user', JSON.stringify(authStore.user));
    }

    Message.success(profileForm.phone ? '手机号更换成功！' : '手机号绑定成功！');
    showBindPhone.value = false;

    // 清空表单
    phoneForm.phone = '';
    phoneForm.verifyCode = '';
  } catch (error) {
    console.error('[Phone] 绑定失败:', error);
    if (error.message?.includes('验证码')) {
      Message.error('验证码错误或已过期');
    } else {
      Message.error(error.message || '绑定失败，请稍后重试');
    }
  } finally {
    bindingPhone.value = false;
  }
}

// 发送邮箱验证码
async function sendEmailVerifyCode() {
  if (!emailForm.email) {
    Message.warning('请先输入邮箱地址');
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(emailForm.email)) {
    Message.error('请输入正确的邮箱格式');
    return;
  }

  try {
    // TODO: 调用发送邮箱验证码接口
    // await http.post('/auth/email/send', { email: emailForm.email });

    Message.success(`验证码已发送至 ${emailForm.email}`);

    // 开始60秒倒计时
    emailCountdown.value = 60;
    if (emailTimer) clearInterval(emailTimer);

    emailTimer = setInterval(() => {
      emailCountdown.value--;
      if (emailCountdown.value <= 0) {
        clearInterval(emailTimer);
        emailTimer = null;
      }
    }, 1000);
  } catch (error) {
    console.error('[Email] 发送验证码失败:', error);
    Message.error(error.message || '发送验证码失败');
  }
}

// 绑定/更换邮箱
async function handleChangeEmail() {
  if (!emailForm.email || !emailForm.verifyCode) {
    Message.warning('请填写完整信息');
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(emailForm.email)) {
    Message.error('请输入正确的邮箱格式');
    return;
  }

  changingEmail.value = true;

  try {
    // TODO: 验证邮箱验证码
    // await http.post('/auth/email/verify', { email: emailForm.email, code: emailForm.verifyCode });

    // 通过 profile 接口更新邮箱
    await http.put('/auth/profile', {
      nickname: profileForm.nickname || undefined,
      phone: profileForm.phone || undefined,
      bio: profileForm.bio || undefined,
      campus: profileForm.campus || undefined,
      email: emailForm.email,
    });

    // 更新本地数据
    profileForm.email = emailForm.email;
    if (authStore.user) {
      authStore.user.email = emailForm.email;
      localStorage.setItem('user', JSON.stringify(authStore.user));
    }

    Message.success(profileForm.email ? '邮箱更换成功！' : '邮箱绑定成功！');
    showChangeEmail.value = false;

    // 清空表单
    emailForm.email = '';
    emailForm.verifyCode = '';
  } catch (error) {
    console.error('[Email] 绑定失败:', error);
    if (error.message?.includes('验证码')) {
      Message.error('验证码错误或已过期');
    } else {
      Message.error(error.message || '操作失败，请稍后重试');
    }
  } finally {
    changingEmail.value = false;
  }
}

// 提交用户名修改申请
async function handleSubmitUsername() {
  if (!usernameForm.newUsername || !usernameForm.reason) {
    Message.warning('请填写完整信息');
    return;
  }

  if (usernameForm.newUsername.length < 3 || usernameForm.newUsername.length > 20) {
    Message.error('用户名长度应在3-20个字符之间');
    return;
  }

  const usernameRegex = /^[a-zA-Z0-9_]+$/;
  if (!usernameRegex.test(usernameForm.newUsername)) {
    Message.error('用户名只能包含字母、数字和下划线');
    return;
  }

  submittingUsername.value = true;

  try {
    // TODO: 提交用户名修改申请（需要运营审核）
    // await http.post('/auth/username/change-request', {
    //   newUsername: usernameForm.newUsername,
    //   reason: usernameForm.reason
    // });

    // 模拟：直接提示成功（实际应等待审核）
    Message.loading('正在提交申请...');

    setTimeout(() => {
      Message.success({
        content: '修改申请已提交！',
        duration: 5000,
      });
      Message.info('运营人员将在1-3个工作日内审核，审核通过后生效。');

      showChangeUsername.value = false;

      // 清空表单
      usernameForm.newUsername = '';
      usernameForm.reason = '';
    }, 1500);

  } catch (error) {
    console.error('[Username] 提交申请失败:', error);
    Message.error(error.message || '提交失败，请稍后重试');
  } finally {
    submittingUsername.value = false;
  }
}

async function handleAvatarChange(urls) {
  if (!urls) return;
  if (Array.isArray(urls) && urls.length === 0) return;
  if (typeof urls === 'string' && urls.trim() === '') return;

  const newAvatarUrl = Array.isArray(urls) ? urls[0] : urls;
  uploadingAvatar.value = true;
  
  try {
    const result = await http.put('/auth/avatar', { avatar: newAvatarUrl });
    
    // 更新本地store
    if (authStore.user) {
      authStore.user.avatar = newAvatarUrl;
      localStorage.setItem('user', JSON.stringify(authStore.user));
    }
    
    avatarUrl.value = newAvatarUrl;
    Message.success('头像更新成功！');
  } catch (error) {
    console.error('[Avatar] 上传失败:', error);
    Message.error('头像更新失败');
  } finally {
    uploadingAvatar.value = false;
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 40px;
}

// ========== 用户信息卡片 ==========
.profile-header {
  background: linear-gradient(135deg, var(--primary-blue, #165DFF) 0%, var(--primary-blue-light, #4080FF) 100%);
  border-radius: 16px;
  padding: 48px 40px;
  color: white;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.25);
}

.user-card {
  display: flex;
  gap: 60px;
  align-items: center;
}

.avatar-section {
  text-align: center;
  
  .avatar-wrapper {
    position: relative;
    display: inline-block;
    cursor: pointer;
    margin-bottom: 16px;
    
    &:hover {
      .avatar-overlay {
        opacity: 1;
      }
    }
  }
  
  .user-avatar {
    font-size: 42px !important;
    font-weight: 700;
    background: rgba(255, 255, 255, 0.95) !important;
    color: var(--primary-blue, #165DFF) !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    border: 4px solid rgba(255, 255, 255, 0.3);
  }
  
  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    color: #fff;
    opacity: 0;
    transition: all 0.3s ease;
    
    span {
      font-size: 12px;
    }
  }
  
  .avatar-loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .user-name {
    margin: 0 0 10px 0;
    font-size: 26px;
    font-weight: 700;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .user-roles {
    margin: 0;
    
    .role-tag {
      margin: 0 4px;
      
      :deep(.arco-tag) {
        border-color: rgba(255, 255, 255, 0.5);
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
      }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  text-align: center;

  .stat-value {
    display: block;
    font-size: 32px;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    opacity: 0.85;
    font-weight: 400;
  }
}

// ========== 快捷操作 ==========
.quick-actions {
  .action-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 24px 16px;
    background: var(--bg-white, #FFFFFF);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 2px solid transparent;
    color: var(--text-primary, #1D2129);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
      border-color: var(--primary-blue-light, #4080FF);
      color: var(--primary-blue, #165DFF);
    }

    span {
      font-size: 14px;
      font-weight: 600;
    }
  }
}

// ========== 信息卡片 ==========
.info-section,
.security-section {
  .info-card,
  .security-card {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    :deep(.arco-card-header-title) {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary, #1D2129);
    }
  }
}

.profile-form {
  margin-top: 20px;

  :deep(.arco-input-wrapper),
  :deep(.arco-textarea-wrapper) {
    border-radius: 8px;
  }
}

// ========== 安全设置 ==========
.security-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--bg-gray-light, #FAFBFC);
  border-radius: 10px;
  transition: all 0.2s ease;

  &:hover {
    background: #F2F3F5;
  }

  .security-info {
    display: flex;
    gap: 16px;
    align-items: center;
    color: var(--text-secondary, #4E5969);

    h4 {
      margin: 0 0 4px 0;
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary, #1D2129);
    }

    p {
      margin: 0;
      font-size: 13px;
    }
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

// ========== 响应式设计 ==========
@media (max-width: 768px) {
  .profile-header {
    padding: 32px 20px;
  }

  .user-card {
    flex-direction: column;
    gap: 32px;
    text-align: center;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    padding: 20px;
  }

  .stat-item .stat-value {
    font-size: 26px;
  }

  .quick-actions .action-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
