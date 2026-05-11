<template>
  <div class="notification-debug">
    <h2>🔍 通知系统调试面板</h2>

    <div class="debug-section">
      <h3>1. 数据库状态检查</h3>
      <a-button type="primary" @click="checkNotificationTable" :loading="checkingTable">
        检查 notification 表是否存在
      </a-button>
      <div v-if="tableStatus" :class="['status-msg', tableStatus.exists ? 'success' : 'error']">
        {{ tableStatus.message }}
      </div>
    </div>

    <div class="debug-section">
      <h3>2. 查看所有通知记录</h3>
      <a-button type="primary" @click="fetchAllNotifications" :loading="fetchingAll">
        获取所有通知（不分用户）
      </a-button>
      <div v-if="allNotifications.length > 0" class="notifications-list">
        <div v-for="(notif, index) in allNotifications" :key="index" class="notif-item">
          <strong>#{{ notif.id }}</strong> |
          类型: {{ notif.type }} |
          接收者: {{ notif.receiverId || '全体' }} |
          已读: {{ notif.isRead }} |
          标题: {{ notif.title }}
        </div>
      </div>
    </div>

    <div class="debug-section">
      <h3>3. 当前用户的通知</h3>
      <a-button type="primary" @click="fetchMyNotifications" :loading="fetchingMine">
        获取当前登录用户的通知
      </a-button>
      <div v-if="myNotifications.length > 0" class="notifications-list">
        <p>共找到 {{ myNotifications.length }} 条通知</p>
        <div v-for="(notif, index) in myNotifications" :key="index" :class="['notif-item', { unread: !notif.isRead }]">
          <strong>{{ notif.title }}</strong><br/>
          <small>{{ notif.content }}</small><br/>
          <span :class="['badge', notif.isRead ? 'read' : 'unread']">
            {{ notif.isRead ? '已读' : '未读' }}
          </span>
          <span class="meta">| 创建时间: {{ formatTime(notif.createdAt) }}</span>
        </div>
      </div>
      <a-empty v-else-if="!fetchingMine" description="当前用户暂无通知" />
    </div>

    <div class="debug-section">
      <h3>4. 手动发送测试通知</h3>
      <a-input v-model="testReceiverId" placeholder="接收者用户ID (留空=全体)" style="width: 200px; margin-right: 8px;" />
      <a-button type="outline" @click="sendTestNotification" :loading="sendingTest">
        发送测试通知
      </a-button>
      <div v-if="testResult" :class="['status-msg', testResult.success ? 'success' : 'error']">
        {{ testResult.message }}
      </div>
    </div>

    <div class="debug-section">
      <h3>5. 未读数量测试</h3>
      <a-button type="outline" @click="checkUnreadCount" :loading="checkingUnread">
        查询当前用户未读数量
      </a-button>
      <div v-if="unreadCountInfo !== null" class="count-display">
        当前未读数量: <strong :class="{ 'text-red': unreadCountInfo > 0 }">{{ unreadCountInfo }}</strong>
      </div>
    </div>

    <div class="debug-info">
      <h4>💡 使用说明：</h4>
      <ol>
        <li>先用账号A发布一个帖子</li>
        <li>切换到账号B，评论该帖子</li>
        <li>切换回账号A，点击"获取当前用户的通知"</li>
        <li>如果看到新评论通知，说明推送成功！</li>
      </ol>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import http from '../../services/core/http';

const checkingTable = ref(false);
const fetchingAll = ref(false);
const fetchingMine = ref(false);
const sendingTest = ref(false);
const checkingUnread = ref(false);

const tableStatus = ref(null);
const allNotifications = ref([]);
const myNotifications = ref([]);
const testReceiverId = ref('');
const testResult = ref(null);
const unreadCountInfo = ref(null);

async function checkNotificationTable() {
  checkingTable.value = true;
  tableStatus.value = null;
  try {
    const res = await http.get('/notifications/debug/table-exists');
    tableStatus.value = {
      exists: res.data?.exists || false,
      message: res.data?.exists
        ? '✅ notification 表存在'
        : '❌ notification 表不存在'
    };
  } catch (e) {
    tableStatus.value = {
      exists: false,
      message: `❌ 检查失败: ${e.message}`
    };
  } finally {
    checkingTable.value = false;
  }
}

async function fetchAllNotifications() {
  fetchingAll.value = true;
  try {
    const res = await http.get('/notifications/debug/all');
    console.log('[Debug] 所有通知:', res.data);
    allNotifications.value = Array.isArray(res.data) ? res.data : [];
    Message.success(`找到 ${allNotifications.value.length} 条通知`);
  } catch (e) {
    Message.error('获取失败: ' + e.message);
  } finally {
    fetchingAll.value = false;
  }
}

async function fetchMyNotifications() {
  fetchingMine.value = true;
  try {
    const res = await http.get('/notifications?limit=50');
    console.log('[Debug] 我的通知:', res.data);
    myNotifications.value = Array.isArray(res.data)
      ? res.data.map(n => ({
          id: n.id,
          title: n.title,
          content: n.content,
          type: n.type,
          isRead: n.isRead,
          receiverId: n.receiverId,
          senderName: n.senderName,
          createdAt: n.createdAt
        }))
      : [];
    Message.success(`当前用户有 ${myNotifications.value.length} 条通知`);
  } catch (e) {
    Message.error('获取失败: ' + e.message);
  } finally {
    fetchingMine.value = false;
  }
}

async function sendTestNotification() {
  sendingTest.value = true;
  testResult.value = null;
  try {
    const payload = {
      title: '测试通知',
      content: '这是一条手动发送的测试通知 - ' + new Date().toLocaleString(),
      type: 'SYSTEM',
      receiverId: testReceiverId.value ? Number(testReceiverId.value) : null,
      senderId: 1,
      senderName: '系统管理员',
      businessType: 'TEST',
      priority: 'HIGH'
    };

    await http.post('/notifications/send', payload);
    testResult.value = {
      success: true,
      message: '✅ 测试通知发送成功！请刷新列表查看'
    };

    // 自动刷新我的通知
    setTimeout(() => fetchMyNotifications(), 500);
  } catch (e) {
    testResult.value = {
      success: false,
      message: '❌ 发送失败: ' + e.message
    };
  } finally {
    sendingTest.value = false;
  }
}

async function checkUnreadCount() {
  checkingUnread.value = true;
  unreadCountInfo.value = null;
  try {
    const res = await http.get('/notifications/unread-count');
    console.log('[Debug] 未读数量:', res.data);
    unreadCountInfo.value = res.data?.data?.count ?? res.data?.count ?? 0;
    Message.info(`未读数量: ${unreadCountInfo.value}`);
  } catch (e) {
    Message.error('查询失败: ' + e.message);
  } finally {
    checkingUnread.value = false;
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('zh-CN');
}
</script>

<style lang="scss" scoped>
.notification-debug {
  max-width: 1000px;
  margin: 24px auto;
  padding: 32px;
  background: white;
  border-radius: 12px;

  h2 {
    margin-bottom: 24px;
    color: #165DFF;
  }

  .debug-section {
    margin-bottom: 28px;
    padding: 20px;
    background: #F7F8FA;
    border-radius: 8px;

    h3 {
      margin: 0 0 16px 0;
      font-size: 16px;
      color: #1D2129;
    }
  }

  .status-msg {
    margin-top: 12px;
    padding: 10px 16px;
    border-radius: 6px;
    font-weight: 500;

    &.success {
      background: #E8FFEC;
      color: #00B42A;
      border: 1px solid #00B42A33;
    }

    &.error {
      background: #FFECE8;
      color: #F53F3F;
      border: 1px solid #F53F3F33;
    }
  }

  .notifications-list {
    margin-top: 16px;

    .notif-item {
      padding: 14px 18px;
      margin-bottom: 10px;
      background: white;
      border-radius: 6px;
      border-left: 4px solid #E5E6EB;
      transition: all 0.25s ease;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      &.unread {
        border-left-color: #165DFF;
        background: #FAFCFF;
      }

      strong {
        color: #1D2129;
      }

      small {
        display: block;
        margin: 6px 0;
        color: #86909C;
        line-height: 1.5;
      }

      .badge {
        display: inline-block;
        padding: 2px 10px;
        border-radius: 10px;
        font-size: 12px;
        font-weight: 600;

        &.read {
          background: #F2F3F5;
          color: #86909C;
        }

        &.unread {
          background: #165DFF;
          color: white;
        }
      }

      .meta {
        margin-left: 8px;
        color: #C9CDD4;
        font-size: 13px;
      }
    }
  }

  .count-display {
    margin-top: 12px;
    padding: 16px;
    background: white;
    border-radius: 6px;
    text-align: center;
    font-size: 20px;

    strong {
      color: #165DFF;

      &.text-red {
        color: #F53F3F;
        font-size: 28px;
      }
    }
  }

  .debug-info {
    margin-top: 32px;
    padding: 20px;
    background: #FFF7E8;
    border-radius: 8px;
    border: 1px solid #FF7D0033;

    h4 {
      margin: 0 0 12px 0;
      color: #FF7D00;
    }

    ol {
      margin: 0;
      padding-left: 20px;
      color: #4E5969;

      li {
        margin-bottom: 8px;
        line-height: 1.6;
      }
    }
  }
}
</style>
