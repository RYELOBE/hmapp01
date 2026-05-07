<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <section class="profile-header">
      <div class="user-card">
        <div class="avatar-section">
          <a-avatar :size="100" class="user-avatar">
            {{ userInitial }}
          </a-avatar>
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
                <a-input 
                  v-model="profileForm.username" 
                  disabled 
                  placeholder="用户名不可修改"
                />
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
                <a-input 
                  v-model="profileForm.email" 
                  placeholder="绑定邮箱（可选）"
                  allow-clear
                >
                  <template #prefix>
                    <icon-email style="color: #86909C;" />
                  </template>
                </a-input>
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
            <a-button type="outline" :disabled="!profileForm.phone">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useAuthStore } from "../../stores/auth";
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
} from "@arco-design/web-vue/es/icon";

const authStore = useAuthStore();
const saving = ref(false);
const changingPwd = ref(false);
const showChangePassword = ref(false);

// 用户数据
const user = computed(() => authStore.user);

const userInitial = computed(() => {
  const name = user.value?.nickname || user.value?.username || 'U';
  return name.charAt(0).toUpperCase();
});

const isSeller = computed(() => {
  return user.value?.roles?.includes('SELLER');
});

// 统计数据（Mock数据，实际应从API获取）
const stats = reactive({
  totalOrders: 12,
  totalItems: 5,
  favorites: 23,
  joinDays: 45,
});

// 个人信息表单
const profileForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  bio: '',
});

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 初始化表单数据
onMounted(() => {
  if (user.value) {
    profileForm.username = user.value.username || '';
    profileForm.nickname = user.value.nickname || '';
    profileForm.phone = user.value.phone || '';
    profileForm.email = user.value.email || '';
    profileForm.bio = user.value.bio || '';
    
    // Mock：计算加入天数
    if (user.value.createdAt) {
      const created = new Date(user.value.createdAt);
      const now = new Date();
      stats.joinDays = Math.floor((now - created) / (1000 * 60 * 60 * 24));
    }
  }
});

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
    // TODO: 调用API保存用户信息
    await new Promise(resolve => setTimeout(resolve, 800)); // 模拟API调用
    
    // 更新本地store
    authStore.user = {
      ...authStore.user,
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      email: profileForm.email,
      bio: profileForm.bio,
    };
    
    // 保存到localStorage
    localStorage.setItem('user', JSON.stringify(authStore.user));
    
    Message.success('个人信息保存成功！');
  } catch (error) {
    console.error('[Profile] 保存失败:', error);
    Message.error('保存失败，请稍后重试');
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
  }
  Message.info('已重置为原始信息');
}

async function handleChangePassword() {
  changingPwd.value = true;
  try {
    // TODO: 调用API修改密码
    await new Promise(resolve => setTimeout(resolve, 800));
    
    Message.success('密码修改成功！');
    showChangePassword.value = false;
    
    // 清空表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (error) {
    console.error('[Password] 修改失败:', error);
    Message.error('密码修改失败，请检查原密码是否正确');
  } finally {
    changingPwd.value = false;
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
  
  .user-avatar {
    font-size: 42px !important;
    font-weight: 700;
    background: rgba(255, 255, 255, 0.95) !important;
    color: var(--primary-blue, #165DFF) !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    margin-bottom: 16px;
    border: 4px solid rgba(255, 255, 255, 0.3);
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
