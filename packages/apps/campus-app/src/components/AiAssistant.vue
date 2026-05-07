<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <transition name="bounce">
      <div
        v-if="!isOpen"
        class="ai-fab"
        @click="toggleChat"
        :class="{ 'has-notification': unreadCount > 0 }"
      >
        <div class="fab-icon">
          <icon-robot size="28" />
        </div>
        <span v-if="unreadCount > 0" class="fab-badge">{{ unreadCount }}</span>
      </div>
    </transition>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar">🤖</div>
            <div class="header-info">
              <h3>校园AI助手</h3>
              <span class="status-dot"></span>
              <span class="status-text">在线</span>
            </div>
          </div>
          <div class="header-actions">
            <a-tooltip content="历史会话">
              <button class="action-btn" @click="showHistory = !showHistory">
                <icon-history />
              </button>
            </a-tooltip>
            <a-tooltip content="最小化">
              <button class="action-btn" @click="toggleChat">
                <icon-minus />
              </button>
            </a-tooltip>
          </div>
        </div>

        <div class="chat-body">
          <!-- 历史会话侧边栏 -->
          <transition name="slide-left">
            <div v-if="showHistory" class="history-sidebar">
              <div class="history-header">
                <h4>历史会话</h4>
                <button class="close-btn" @click="showHistory = false">
                  <icon-close />
                </button>
              </div>
              <div class="history-list">
                <div
                  v-for="session in sessions"
                  :key="session.id"
                  class="history-item"
                  :class="{ active: currentSessionId === session.id }"
                  @click="loadSession(session.id)"
                >
                  <div class="item-title">{{ session.title || "新对话" }}</div>
                  <div class="item-time">
                    {{ formatTime(session.updatedAt) }}
                  </div>
                </div>
                <div v-if="sessions.length === 0" class="empty-state">
                  <icon-empty style="font-size: 48px; color: #c9cdd4" />
                  <p>暂无历史会话</p>
                </div>
              </div>
            </div>
          </transition>

          <!-- 聊天消息区 -->
          <div class="messages-container" ref="messagesContainer">
            <!-- 欢迎消息 -->
            <div v-if="messages.length === 0" class="welcome-message">
              <div class="welcome-icon">👋</div>
              <h3>你好！我是校园AI助手</h3>
              <p>我可以帮你：</p>
              <ul class="feature-list">
                <li>🔍 搜索商品和分类信息</li>
                <li>💰 了解平台交易规则</li>
                <li>📦 发布商品指导</li>
                <li>❓ 解答常见问题</li>
              </ul>

              <!-- 快捷问题 -->
              <div class="quick-actions">
                <p class="quick-label">试试问我：</p>
                <div class="quick-chips">
                  <span
                    v-for="(preset, index) in presets"
                    :key="index"
                    class="chip"
                    @click="sendQuickMessage(preset)"
                  >
                    {{ preset }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 消息列表 -->
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-wrapper"
              :class="msg.role"
            >
              <div class="avatar">
                {{ msg.role === "user" ? "👤" : "🤖" }}
              </div>
              <div class="message-content">
                <div class="bubble" v-html="formatMessage(msg.content)"></div>
                <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
              </div>
            </div>

            <!-- AI正在输入 -->
            <div v-if="isTyping" class="message-wrapper assistant">
              <div class="avatar">🤖</div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-footer">
          <div class="input-area">
            <textarea
              v-model="inputMessage"
              placeholder="输入你的问题..."
              rows="1"
              @keydown.enter.exact.prevent="sendMessage"
              @input="autoResize"
              ref="inputRef"
            ></textarea>
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || isTyping"
              @click="sendMessage"
            >
              <icon-send />
            </button>
          </div>
          <div class="footer-hint">
            AI助手基于大语言模型，回答仅供参考，请以实际为准
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconRobot,
  IconHistory,
  IconMinus,
  IconClose,
  IconEmpty,
  IconSend,
} from "@arco-design/web-vue/es/icon";
import {
  chatWithAssistant,
  fetchRecentSessions,
  fetchSessionMessages,
  fetchAiPresets,
} from "../shared-components/ai-sdk";

// 状态管理
const isOpen = ref(false);
const showHistory = ref(false);
const inputMessage = ref("");
const messages = ref([]);
const sessions = ref([]);
const currentSessionId = ref(null);
const isTyping = ref(false);
const unreadCount = ref(0);
const messagesContainer = ref(null);
const inputRef = ref(null);
const presets = ref([]);

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    unreadCount.value = 0;
    loadSessions();
    loadPresets();
    nextTick(() => {
      scrollToBottom();
      inputRef.value?.focus();
    });
  }
};

// 发送消息
const sendMessage = async () => {
  const text = inputMessage.value.trim();
  if (!text || isTyping.value) return;

  // 添加用户消息
  messages.value.push({
    role: "user",
    content: text,
    timestamp: new Date().toISOString(),
  });

  inputMessage.value = "";
  autoResize();
  scrollToBottom();

  // 显示输入状态
  isTyping.value = true;

  try {
    console.log("[AI Debug] 开始发送消息...");
    console.log(
      "[AI Debug] 当前Token:",
      localStorage.getItem("token") || "无Token",
    );
    console.log(
      "[AI Debug] 当前用户:",
      localStorage.getItem("user") || "无用户信息",
    );

    const response = await chatWithAssistant(
      { message: text, sessionId: currentSessionId.value },
      {
        isHideErrorMsg: true,
        isTokenNotAuth: true,
        isAllowDuplicate: true,
      },
    );

    console.log("[AI Debug] API调用成功:", response);

    // 解析响应（后端返回 {code:200/500, data:{reply, sessionId, references}, message}）
    const data = response.data || response;

    // 检查是否为错误响应
    if (data.code && data.code !== 200) {
      const errorMsg = data.message || "AI服务暂时不可用，请稍后再试";
      console.error("[AI Debug] AI服务错误:", errorMsg);

      // 显示红色错误提示
      Message.error(errorMsg);
      return;
    }

    const aiReply = data.reply || data.answer;
    console.log("[AI Debug] AI回复内容:", aiReply);

    // 添加AI回复
    messages.value.push({
      role: "assistant",
      content: aiReply || "抱歉，我暂时无法回答这个问题。",
      timestamp: new Date().toISOString(),
    });

    // 更新会话ID
    if (data.sessionId) {
      currentSessionId.value = data.sessionId;
    }
  } catch (error) {
    console.error("[AI Debug] 发送失败，完整错误:", error);
    console.error("[AI Debug] 错误状态码:", error?.status);
    console.error("[AI Debug] 错误代码:", error?.code);
    console.error("[AI Debug] 是否后端不可用:", error?.isBackendUnavailable);

    // 🔧 判断是否是认证错误（但不强制登出）
    const isAuthError =
      error?.status === 401 || error?.code === 401 || error?.code === 402;

    if (isAuthError) {
      console.warn("[AI Chat] 认证已过期，但保持当前会话");
      // 显示友好提示而不是强制登出
      messages.value.push({
        role: "assistant",
        content:
          "⚠️ 会话已过期，如需继续对话请重新登录。\n\n你可以继续浏览商品和使用其他功能。",
        timestamp: new Date().toISOString(),
      });
    } else if (error?.isBackendUnavailable) {
      // 后端不可用时使用Mock回复
      messages.value.push({
        role: "assistant",
        content: getMockResponse(text),
        timestamp: new Date().toISOString(),
      });
    } else {
      // 其他错误也使用Mock降级
      console.warn("[AI Chat] API调用失败，使用Mock模式");
      messages.value.push({
        role: "assistant",
        content: getMockResponse(text),
        timestamp: new Date().toISOString(),
      });
    }
  } finally {
    isTyping.value = false;
    scrollToBottom();
  }
};

// 快捷问题发送
const sendQuickMessage = (text) => {
  inputMessage.value = text;
  sendMessage();
};

// 加载历史会话
const loadSessions = async () => {
  try {
    const data = await fetchRecentSessions(
      {},
      {
        isHideErrorMsg: true,
        isTokenNotAuth: true,
      },
    );
    sessions.value = data || [];
  } catch (error) {
    console.warn("[AI] 加载会话失败:", error);
    sessions.value = [];
  }
};

// 加载会话消息
const loadSession = async (sessionId) => {
  currentSessionId.value = sessionId;
  showHistory.value = false;

  try {
    const data = await fetchSessionMessages(
      sessionId,
      {},
      {
        isHideErrorMsg: true,
        isTokenNotAuth: true,
      },
    );
    messages.value = data?.messages || [];
  } catch (error) {
    console.warn("[AI] 加载消息失败:", error);
    messages.value = [];
  }

  scrollToBottom();
};

// 加载预设问题
const loadPresets = async () => {
  try {
    const data = await fetchAiPresets(
      {},
      {
        isHideErrorMsg: true,
        isTokenNotAuth: true,
      },
    );
    presets.value = data?.presets || [
      "如何发布商品？",
      "交易流程是什么？",
      "如何联系卖家？",
      "平台手续费多少？",
    ];
  } catch (error) {
    presets.value = [
      "如何发布商品？",
      "交易流程是什么？",
      "如何联系卖家？",
      "平台手续费多少？",
    ];
  }
};

// Mock响应（当API不可用时）
const getMockResponse = (question) => {
  const q = question.toLowerCase();

  if (q.includes("发布") || q.includes("卖")) {
    return "发布商品很简单！点击导航栏的「立即发布」或进入「卖家中心→发布商品」，填写商品信息、上传图片、设置价格即可。记得详细描述商品成色和使用情况哦~ 📦";
  }

  if (q.includes("交易") || q.includes("购买") || q.includes("买")) {
    return "购买流程：\n1️⃣ 浏览商品，找到心仪的宝贝\n2️⃣ 点击「立即购买」或「加入购物车」\n3️⃣ 填写收货地址\n4️⃣ 选择支付方式完成支付\n5️⃣ 等待卖家发货\n6️⃣ 确认收货并评价 💯";
  }

  if (q.includes("联系") || q.includes("客服")) {
    return "你可以通过以下方式联系我们：\n• 在线客服：点击右下角本窗口\n• 客服中心：导航栏「客服中心」\n• 邮箱：support@campustrade.com\n• 工作时间：周一至周五 9:00-18:00";
  }

  if (q.includes("费用") || q.includes("手续费") || q.includes("价格")) {
    return "平台收费说明：\n• **买家**：免费浏览和购买\n• **卖家**：成交后收取2%服务费（最低1元）\n• **免佣活动**：新用户首月免服务费！\n\n具体规则请查看「帮助中心→费用说明」页面 💰";
  }

  return `感谢你的提问！关于"${question}"的问题，我建议你：\n\n1. 查看「客服中心」获取详细信息\n2. 联系在线人工客服获得帮助\n3. 查阅常见问题FAQ\n\n还有什么我可以帮你的吗？😊`;
};

// 格式化消息（支持Markdown）
const formatMessage = (content) => {
  if (!content) return "";

  // 简单的Markdown转换
  return content
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\n/g, "<br>")
    .replace(/`(.*?)`/g, "<code>$1</code>");
};

// 时间格式化
const formatTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;

  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;

  return date.toLocaleString("zh-CN", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// 自动调整输入框高度
const autoResize = (e) => {
  const textarea = e?.target || inputRef.value;
  if (textarea) {
    textarea.style.height = "auto";
    textarea.style.height = Math.min(textarea.scrollHeight, 120) + "px";
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

onMounted(() => {
  // 可以在这里初始化一些逻辑
});
</script>

<style lang="scss" scoped>
.ai-assistant {
  position: fixed;
  z-index: 9999;
  bottom: 100px;
  right: 15px;
}

// ========== 悬浮按钮 ==========
.ai-fab {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  animation: pulse 2s infinite;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 12px 32px rgba(22, 93, 255, 0.45);
  }

  &:active {
    transform: scale(0.95);
  }

  .fab-icon {
    color: white;
    animation: float 3s ease-in-out infinite;
  }

  .fab-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    background: #f53f3f;
    color: white;
    font-size: 11px;
    font-weight: 700;
    min-width: 20px;
    height: 20px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 5px;
    border: 2px solid white;
  }
}

@keyframes pulse {
  0%,
  100% {
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35);
  }
  50% {
    box-shadow: 0 8px 32px rgba(22, 93, 255, 0.55);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

// ========== 聊天窗口 ==========
.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 400px;
  height: 600px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 16px 48px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: windowAppear 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes windowAppear {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 头部
.chat-header {
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  color: white;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .ai-avatar {
    width: 42px;
    height: 42px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    backdrop-filter: blur(10px);
  }

  .header-info {
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
    }

    .status-dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      background: #52c41a;
      border-radius: 50%;
      margin-right: 4px;
      animation: blink 2s infinite;
    }

    .status-text {
      font-size: 12px;
      opacity: 0.9;
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      width: 32px;
      height: 32px;
      border: none;
      background: rgba(255, 255, 255, 0.15);
      color: white;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(255, 255, 255, 0.25);
      }
    }
  }
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

// 主体区域
.chat-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  position: relative;
}

// 历史侧边栏
.history-sidebar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 240px;
  background: #fafbfc;
  border-right: 1px solid #e5e6eb;
  z-index: 10;
  display: flex;
  flex-direction: column;

  .history-header {
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #e5e6eb;

    h4 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
    }

    .close-btn {
      background: none;
      border: none;
      cursor: pointer;
      padding: 4px;
      border-radius: 4px;

      &:hover {
        background: #e5e6eb;
      }
    }
  }

  .history-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    .history-item {
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      margin-bottom: 4px;

      &:hover {
        background: #e8f3ff;
      }

      &.active {
        background: #d6e9ff;

        .item-title {
          color: #165dff;
          font-weight: 600;
        }
      }

      .item-title {
        font-size: 14px;
        color: #1d2129;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .item-time {
        font-size: 12px;
        color: #86909c;
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #86909c;

      p {
        margin: 12px 0 0 0;
        font-size: 14px;
      }
    }
  }
}

// 消息容器
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c9cdd4;
    border-radius: 3px;
  }
}

// 欢迎消息
.welcome-message {
  text-align: center;
  padding: 30px 20px;

  .welcome-icon {
    font-size: 56px;
    margin-bottom: 16px;
    animation: wave 2s ease-in-out infinite;
  }

  @keyframes wave {
    0%,
    100% {
      transform: rotate(0deg);
    }
    25% {
      transform: rotate(15deg);
    }
    75% {
      transform: rotate(-10deg);
    }
  }

  h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    color: #1d2129;
  }

  p {
    margin: 0 0 16px 0;
    color: #4e5969;
    font-size: 14px;
  }

  .feature-list {
    text-align: left;
    max-width: 280px;
    margin: 0 auto 24px;
    padding-left: 20px;
    color: #4e5969;
    font-size: 13px;
    line-height: 1.8;

    li {
      margin-bottom: 4px;
    }
  }

  .quick-actions {
    .quick-label {
      font-size: 13px;
      color: #86909c;
      margin-bottom: 12px;
    }

    .quick-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;

      .chip {
        padding: 8px 16px;
        background: #f2f3f5;
        border-radius: 20px;
        font-size: 13px;
        color: #4e5969;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #e8f3ff;
          color: #165dff;
          transform: translateY(-2px);
        }
      }
    }
  }
}

// 消息样式
.message-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-out;

  &.user {
    flex-direction: row-reverse;

    .bubble {
      background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
      color: white;
      border-radius: 18px 18px 4px 18px;
    }
  }

  &.assistant {
    .bubble {
      background: #f2f3f5;
      color: #1d2129;
      border-radius: 18px 18px 18px 4px;
    }
  }

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #fafbfc;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }

  .message-content {
    max-width: 75%;
  }

  .bubble {
    padding: 12px 16px;
    line-height: 1.6;
    font-size: 14px;
    word-wrap: break-word;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    code {
      background: rgba(0, 0, 0, 0.06);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 13px;
    }
  }

  .msg-time {
    display: block;
    font-size: 11px;
    color: #86909c;
    margin-top: 6px;
    padding: 0 4px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 输入指示器
.typing-indicator {
  display: inline-flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f2f3f5;
  border-radius: 18px 18px 18px 4px;

  span {
    width: 8px;
    height: 8px;
    background: #86909c;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

// 底部输入区
.chat-footer {
  border-top: 1px solid #e5e6eb;
  padding: 16px;
  background: #ffffff;
  flex-shrink: 0;

  .input-area {
    display: flex;
    gap: 10px;
    align-items: center;
    background: #f7f8fa;
    border-radius: 12px;
    padding: 8px 12px;
    border: 2px solid transparent;
    transition: all 0.2s;

    &:focus-within {
      border-color: #165dff;
      background: #ffffff;
      box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
    }

    textarea {
      flex: 1;
      border: none;
      outline: none;
      resize: none;
      font-size: 14px;
      line-height: 1.5;
      background: transparent;
      font-family: inherit;
      max-height: 120px;

      &::placeholder {
        color: #86909c;
      }
    }

    .send-btn {
      width: 40px;
      height: 40px;
      border: none;
      background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
      color: white;
      border-radius: 10px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      flex-shrink: 0;

      &:hover:not(:disabled) {
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .footer-hint {
    text-align: center;
    font-size: 11px;
    color: #c9cdd4;
    margin-top: 8px;
  }
}

// 动画
.bounce-enter-active,
.bounce-leave-active {
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
.bounce-enter-from,
.bounce-leave-to {
  transform: scale(0);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.25s ease;
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}

// 响应式
@media (max-width: 768px) {
  .ai-assistant {
    bottom: 16px;
    right: 16px;
  }

  .chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
    bottom: 76px;
    right: -16px;
  }

  .history-sidebar {
    width: 200px;
  }
}
</style>
