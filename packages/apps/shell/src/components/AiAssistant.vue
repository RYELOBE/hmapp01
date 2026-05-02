<template>
  <div>
    <a-button shape="circle" size="large" class="ai-assistant-trigger" @click="visible = true">
      <template #icon><icon-question-circle /></template>
    </a-button>

    <!-- 对话窗口 -->
    <div v-if="visible" class="ai-assistant-overlay" @click="visible = false"></div>
    <div v-if="visible" class="ai-assistant-panel">
      <!-- 头部 -->
      <div class="ai-assistant-header">
        <div class="ai-assistant-title">
          <icon-question-circle />
          <span>AI 助手</span>
        </div>
        <a-button type="text" class="ai-assistant-close" @click="visible = false">
          <template #icon><icon-close /></template>
        </a-button>
      </div>

      <!-- 主体内容 -->
      <div class="ai-assistant-body">
        <!-- 会话列表侧边栏 -->
        <div class="ai-assistant-sessions">
          <a-button type="primary" long class="ai-new-session-btn" @click="createNewSession">
            <template #icon><icon-plus /></template>
            新对话
          </a-button>
          <a-menu v-model:selected-keys="selectedKeys" @menu-item-click="onMenuItemClick" class="ai-session-menu">
            <a-menu-item v-for="session in sessions" :key="session.sessionId">
              <template #icon><icon-message /></template>
              {{ getSessionTitle(session) }}
            </a-menu-item>
          </a-menu>
        </div>

        <!-- 对话区域 -->
        <div class="ai-assistant-chat">
          <!-- 预设问题 -->
          <div class="ai-presets" v-if="messages.length === 0">
            <div class="ai-preset-title">试试问这些问题：</div>
            <div class="ai-preset-list">
              <a-button
                v-for="preset in presets"
                :key="preset"
                type="outline"
                long
                class="ai-preset-btn"
                @click="sendPresetMessage(preset)"
              >
                {{ preset }}
              </a-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="ai-messages" ref="messagesContainer">
            <div v-for="(message, index) in messages" :key="index" :class="['ai-message', message.role]">
              <div class="ai-message-avatar">
                <icon-question-circle v-if="message.role === 'assistant'" />
                <icon-user v-else />
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
                <icon-question-circle />
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
            <a-textarea
              v-model="draft"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              placeholder="输入你的问题..."
              class="ai-input-textarea"
              @keydown.enter.prevent="sendMessage"
            />
            <a-button
              type="primary"
              shape="circle"
              class="ai-send-btn"
              :disabled="!draft.trim() || sending"
              @click="sendMessage"
            >
              <template #icon><icon-right /></template>
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import MarkdownIt from 'markdown-it';
import { IconQuestionCircle, IconClose, IconPlus, IconMessage, IconUser, IconRight } from '@arco-design/web-vue/es/icon';
import { chatWithAssistant, fetchAiPresets, fetchRecentSessions, fetchSessionMessages } from 'commonprovide/ai-sdk';
import { getCurrentUser } from 'commonprovide/auth-sdk';

const md = new MarkdownIt({ html: false, breaks: true, linkify: true });

const visible = ref(false);
const draft = ref('');
const sending = ref(false);
const currentSessionId = ref('');
const messages = ref([]);
const sessions = ref([]);
const presets = ref([]);
const selectedKeys = ref([]);
const messagesContainer = ref(null);

watch(currentSessionId, (id) => {
  selectedKeys.value = id ? [id] : [];
});

onMounted(() => {
  loadPresets();
  loadSessions();
});

async function loadPresets() {
  try {
    const result = await fetchAiPresets();
    presets.value = result.presets || [];
  } catch (error) {
    console.warn('加载预设问题失败:', error.message);
  }
}

async function loadSessions() {
  try {
    const result = await fetchRecentSessions();
    sessions.value = result.sessions || [];
  } catch (error) {
    console.warn('加载会话列表失败:', error.message);
  }
}

function createNewSession() {
  currentSessionId.value = '';
  messages.value = [];
}

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

function onMenuItemClick(key) {
  loadSession(key);
}

function getSessionTitle(session) {
  return session.title || `会话 ${session.sessionId.slice(-6)}`;
}

function sendPresetMessage(preset) {
  draft.value = preset;
  sendMessage();
}

async function sendMessage() {
  if (!draft.value.trim() || sending.value) return;

  const user = getCurrentUser();
  const content = draft.value.trim();

  messages.value.push({ role: 'user', content });
  draft.value = '';
  sending.value = true;

  await nextTick();
  scrollToBottom();

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

function renderMarkdown(content) {
  return md.render(content || '');
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}
</script>

<style scoped>
.ai-assistant-trigger {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border: none;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  z-index: 1000;
  transition: transform 0.2s, box-shadow 0.2s;
}

.ai-assistant-trigger:deep(.arco-btn) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.ai-assistant-trigger:deep(.arco-btn:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.ai-assistant-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.ai-assistant-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1001;
}

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
  color: white;
}

.ai-assistant-close:deep(.arco-btn) {
  color: white;
}

.ai-assistant-close:hover:deep(.arco-btn) {
  background: rgba(255, 255, 255, 0.2);
}

.ai-assistant-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.ai-assistant-sessions {
  width: 140px;
  background: #f8f9fa;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-new-session-btn {
  margin: 12px;
  flex-shrink: 0;
}

.ai-session-menu {
  flex: 1;
  overflow-y: auto;
  border: none;
  background: transparent;
  padding: 0 12px 12px;
}

.ai-session-menu:deep(.arco-menu-inner) {
  padding: 0;
}

.ai-session-menu:deep(.arco-menu-item) {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #495057;
  margin-bottom: 4px;
  line-height: 1.4;
}

.ai-session-menu:deep(.arco-menu-item:hover) {
  background: white;
}

.ai-session-menu:deep(.arco-menu-selected) {
  background: white;
  color: #667eea;
  font-weight: 500;
}

.ai-assistant-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
}

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
  font-size: 13px;
  color: #495057;
}

.ai-preset-btn:deep(.arco-btn) {
  justify-content: flex-start;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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

.ai-markdown {
  font-size: 14px;
  line-height: 1.6;
}

.ai-markdown:deep(*:first-child) {
  margin-top: 0;
}

.ai-markdown:deep(*:last-child) {
  margin-bottom: 0;
}

.ai-markdown:deep(p) {
  margin: 8px 0;
}

.ai-markdown:deep(strong) {
  font-weight: 600;
}

.ai-markdown:deep(ul),
.ai-markdown:deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.ai-markdown:deep(li) {
  margin: 4px 0;
}

.ai-markdown:deep(blockquote) {
  border-left: 3px solid #667eea;
  padding-left: 12px;
  margin: 8px 0;
  color: #667eea;
}

.ai-markdown:deep(code) {
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

.ai-input-area {
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.ai-input-textarea {
  flex: 1;
}

.ai-input-textarea:deep(.arco-textarea) {
  border-radius: 20px;
  font-size: 14px;
  font-family: inherit;
}

.ai-input-textarea:deep(.arco-textarea:focus) {
  border-color: #667eea;
}

.ai-send-btn {
  flex-shrink: 0;
  border: none;
  transition: transform 0.2s, opacity 0.2s;
}

.ai-send-btn:deep(.arco-btn) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.ai-send-btn:deep(.arco-btn:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.ai-send-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

.ai-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ai-send-btn:disabled:deep(.arco-btn) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

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
