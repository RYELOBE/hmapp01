<template>
  <div>
    <!-- 浮动按钮 -->
    <button
      class="ai-assistant-trigger"
      @click="visible = true"
    >
      <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M12 8v4M12 16h.01"></path>
      </svg>
    </button>

    <!-- 对话窗口 -->
    <div v-if="visible" class="ai-assistant-overlay" @click="visible = false"></div>
    <div v-if="visible" class="ai-assistant-panel">
      <!-- 头部 -->
      <div class="ai-assistant-header">
        <div class="ai-assistant-title">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <path d="M12 8v4M12 16h.01"></path>
          </svg>
          <span>AI 助手</span>
        </div>
        <button class="ai-assistant-close" @click="visible = false">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <!-- 主体内容 -->
      <div class="ai-assistant-body">
        <!-- 会话列表侧边栏 -->
        <div class="ai-assistant-sessions">
          <button class="ai-new-session-btn" @click="createNewSession">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            新对话
          </button>
          <div class="ai-session-list">
            <div
              v-for="session in sessions"
              :key="session.sessionId"
              :class="['ai-session-item', { active: currentSessionId === session.sessionId }]"
              @click="loadSession(session.sessionId)"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              <span>{{ getSessionTitle(session) }}</span>
            </div>
          </div>
        </div>

        <!-- 对话区域 -->
        <div class="ai-assistant-chat">
          <!-- 预设问题 -->
          <div class="ai-presets" v-if="messages.length === 0">
            <div class="ai-preset-title">试试问这些问题：</div>
            <div class="ai-preset-list">
              <button
                v-for="preset in presets"
                :key="preset"
                class="ai-preset-btn"
                @click="sendPresetMessage(preset)"
              >
                {{ preset }}
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="ai-messages" ref="messagesContainer">
            <div v-for="(message, index) in messages" :key="index" :class="['ai-message', message.role]">
              <div class="ai-message-avatar">
                <svg v-if="message.role === 'assistant'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <path d="M12 8v4M12 16h.01"></path>
                </svg>
                <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
              <div class="ai-message-content">
                <div
                  v-if="message.role === 'assistant'"
                  class="ai-markdown"
                  v-html="renderMarkdown(message.content)"
                ></div>
                <div v-else class="ai-text">{{ message.content }}</div>
              </div>
            </div>
            <div v-if="sending" class="ai-message assistant">
              <div class="ai-message-avatar">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <path d="M12 8v4M12 16h.01"></path>
                </svg>
              </div>
              <div class="ai-message-content">
                <div class="ai-typing">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="ai-input-area">
            <textarea
              v-model="draft"
              placeholder="输入你的问题..."
              @keydown.enter.prevent="sendMessage"
              rows="1"
              ref="textareaRef"
            ></textarea>
            <button
              class="ai-send-btn"
              :disabled="!draft.trim() || sending"
              @click="sendMessage"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { chatWithAssistant, fetchAiPresets, fetchRecentSessions, fetchSessionMessages } from 'commonprovide/ai-sdk';
import { getCurrentUser } from 'commonprovide/auth-sdk';

// 状态
const visible = ref(false);
const draft = ref('');
const sending = ref(false);
const currentSessionId = ref('');
const messages = ref([]);
const sessions = ref([]);
const presets = ref([]);
const messagesContainer = ref(null);
const textareaRef = ref(null);

// 初始化 markdown-it
let md = null;
onMounted(() => {
  // 动态导入 markdown-it
  import('markdown-it').then(module => {
    md = module.default();
  });
  loadPresets();
  loadSessions();
});

// 加载预设问题
async function loadPresets() {
  try {
    const result = await fetchAiPresets();
    presets.value = result.presets || [];
  } catch (error) {
    console.warn('加载预设问题失败:', error.message);
  }
}

// 加载会话列表
async function loadSessions() {
  try {
    const result = await fetchRecentSessions();
    sessions.value = result.sessions || [];
  } catch (error) {
    console.warn('加载会话列表失败:', error.message);
  }
}

// 创建新会话
function createNewSession() {
  currentSessionId.value = '';
  messages.value = [];
}

// 加载会话历史
async function loadSession(sessionId) {
  currentSessionId.value = sessionId;
  try {
    const result = await fetchSessionMessages(sessionId);
    messages.value = result.messages || [];
    scrollToBottom();
  } catch (error) {
    console.error('加载会话失败:', error.message);
  }
}

// 获取会话标题
function getSessionTitle(session) {
  return session.title || `会话 ${session.sessionId.slice(-6)}`;
}

// 发送预设问题
function sendPresetMessage(preset) {
  draft.value = preset;
  sendMessage();
}

// 发送消息
async function sendMessage() {
  if (!draft.value.trim() || sending.value) return;

  const user = getCurrentUser();
  const content = draft.value.trim();
  
  // 添加用户消息
  messages.value.push({ role: 'user', content });
  draft.value = '';
  sending.value = true;
  
  await nextTick();
  scrollToBottom();
  autoResizeTextarea();

  try {
    const result = await chatWithAssistant({
      sessionId: currentSessionId.value || null,
      role: user?.roles?.[0] || 'BUYER',
      message: content,
    });
    
    currentSessionId.value = result.sessionId;
    messages.value.push({
      role: 'assistant',
      content: result.answer,
      references: result.references || [],
    });
    
    // 重新加载会话列表
    loadSessions();
  } catch (error) {
    console.error('AI助手对话失败:', error.message);
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用，请稍后重试。',
      references: [],
    });
  } finally {
    sending.value = false;
    await nextTick();
    scrollToBottom();
  }
}

// Markdown 渲染
function renderMarkdown(content) {
  if (!md) return content;
  return md.render(content);
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}

// 自动调整 textarea 高度
function autoResizeTextarea() {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto';
      textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px';
    }
  });
}

// 监听输入变化自动调整高度
watch(draft, () => {
  autoResizeTextarea();
});
</script>

<style scoped>
/* 浮动按钮 */
.ai-assistant-trigger {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.ai-assistant-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

/* 遮罩 */
.ai-assistant-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1001;
}

/* 对话面板 */
.ai-assistant-panel {
  position: fixed;
  right: 96px;
  bottom: 24px;
  width: 420px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  z-index: 1002;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.ai-assistant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ai-assistant-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.ai-assistant-close {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.ai-assistant-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 主体 */
.ai-assistant-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 会话列表 */
.ai-assistant-sessions {
  width: 140px;
  background: #f8f9fa;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.ai-new-session-btn {
  margin: 12px;
  padding: 10px 12px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #495057;
  transition: all 0.2s;
}

.ai-new-session-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.ai-session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 12px;
}

.ai-session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #495057;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.ai-session-item:hover {
  background: white;
}

.ai-session-item.active {
  background: white;
  color: #667eea;
  font-weight: 500;
}

/* 对话区域 */
.ai-assistant-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 预设问题 */
.ai-presets {
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
}

.ai-preset-title {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 12px;
}

.ai-preset-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-preset-btn {
  text-align: left;
  padding: 10px 14px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #495057;
  transition: all 0.2s;
}

.ai-preset-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 消息列表 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息 */
.ai-message {
  display: flex;
  gap: 12px;
  max-width: 100%;
}

.ai-message.user {
  flex-direction: row-reverse;
}

.ai-message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-message.assistant .ai-message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ai-message.user .ai-message-avatar {
  background: #e9ecef;
  color: #495057;
}

.ai-message-content {
  max-width: 260px;
}

.ai-message.assistant .ai-message-content {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 12px 12px 12px 4px;
}

.ai-message.user .ai-message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 16px;
  border-radius: 12px 12px 4px 12px;
}

/* Markdown 样式 */
.ai-markdown {
  font-size: 14px;
  line-height: 1.6;
}

.ai-markdown :first-child {
  margin-top: 0;
}

.ai-markdown :last-child {
  margin-bottom: 0;
}

.ai-markdown p {
  margin: 8px 0;
}

.ai-markdown strong {
  font-weight: 600;
}

.ai-markdown ul,
.ai-markdown ol {
  padding-left: 20px;
  margin: 8px 0;
}

.ai-markdown li {
  margin: 4px 0;
}

.ai-markdown blockquote {
  border-left: 3px solid #667eea;
  padding-left: 12px;
  margin: 8px 0;
  color: #667eea;
}

.ai-markdown code {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.ai-text {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

/* 输入区域 */
.ai-input-area {
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.ai-input-area textarea {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  font-size: 14px;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
  line-height: 1.5;
  max-height: 120px;
}

.ai-input-area textarea:focus {
  border-color: #667eea;
}

.ai-send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, opacity 0.2s;
  flex-shrink: 0;
}

.ai-send-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

.ai-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 打字动画 */
.ai-typing {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.ai-typing span {
  width: 8px;
  height: 8px;
  background: #6c757d;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.ai-typing span:nth-child(1) {
  animation-delay: 0s;
}

.ai-typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-6px);
    opacity: 1;
  }
}
</style>
