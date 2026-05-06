<template>
  <div class="ai-assistant" :class="{ 'ai-assistant--open': visible }">
    <!-- 悬浮触发按钮 -->
    <div
      class="ai-assistant__trigger"
      role="button"
      tabindex="0"
      aria-label="打开 AI 助手"
      @click="togglePanel"
      @keydown.enter="togglePanel"
      @keydown.space.prevent="togglePanel"
    >
      <a-badge :count="unreadCount" :max-count="99">
        <div class="ai-trigger-btn">
          <icon-message />
        </div>
      </a-badge>
    </div>

    <!-- 对话窗口 -->
    <Transition name="ai-panel">
      <div v-if="visible" class="ai-assistant__panel">
        <!-- 头部 -->
        <header class="ai-panel__header">
          <div class="ai-header-left">
            <a-avatar :size="32" class="ai-avatar">
              <template #icon><icon-robot /></template>
            </a-avatar>
            <span class="ai-title">AI 助手</span>
          </div>
          <div class="ai-header-right">
            <a-button type="text" size="small" class="ai-header-btn" @click="minimizePanel">
              <template #icon><icon-minus /></template>
            </a-button>
            <a-button type="text" size="small" class="ai-header-btn" @click="closePanel">
              <template #icon><icon-close /></template>
            </a-button>
          </div>
        </header>

        <!-- 消息列表区域 -->
        <div class="ai-panel__body" ref="messagesContainer">
          <!-- 空状态 -->
          <div v-if="messages.length === 0 && !sending" class="ai-empty-state">
            <a-avatar :size="56" class="ai-empty-avatar">
              <template #icon><icon-robot /></template>
            </a-avatar>
            <p class="ai-empty-text">您好！我是校园二手平台的 AI 助手 😊</p>
            <div class="ai-quick-questions">
              <button
                v-for="(q, idx) in quickQuestions"
                :key="idx"
                class="ai-quick-btn"
                @click="sendQuickQuestion(q)"
              >{{ q }}</button>
            </div>
          </div>

          <!-- 消息列表 -->
          <template v-else>
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['ai-message', `ai-message--${msg.role}`]"
            >
              <a-avatar v-if="msg.role === 'assistant'" :size="24" class="ai-msg-avatar">
                <template #icon><icon-robot /></template>
              </a-avatar>
              <div class="ai-message__content">
                <div
                  v-if="msg.role === 'assistant'"
                  class="ai-bubble ai-bubble--assistant"
                  v-html="renderMarkdown(msg.content)"
                ></div>
                <div v-else class="ai-bubble ai-bubble--user">{{ msg.content }}</div>
              </div>
              <a-avatar v-if="msg.role === 'user'" :size="24" class="ai-msg-avatar">
                <template #icon><icon-user /></template>
              </a-avatar>
            </div>

            <!-- 加载状态：打字动画 -->
            <div v-if="sending" class="ai-message ai-message--assistant">
              <a-avatar :size="24" class="ai-msg-avatar">
                <template #icon><icon-robot /></template>
              </a-avatar>
              <div class="ai-message__content">
                <div class="ai-bubble ai-bubble--typing">
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 输入区域 -->
        <footer class="ai-panel__footer">
          <!-- 快捷问题标签 -->
          <div v-if="messages.length > 0 && !sending" class="ai-shortcuts">
            <button
              v-for="(q, idx) in quickQuestions.slice(0, 3)"
              :key="'sc-' + idx"
              class="ai-shortcut-tag"
              @click="sendQuickQuestion(q)"
            >{{ q }}</button>
          </div>

          <div class="ai-input-wrapper">
            <a-textarea
              ref="inputRef"
              v-model="draft"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              placeholder="输入您的问题..."
              :max-length="500"
              show-word-limit
              class="ai-input"
              @keydown.enter.exact.prevent="handleSend"
              @focus="isInputFocused = true"
              @blur="isInputFocused = false"
            />
            <a-button
              type="primary"
              shape="circle"
              :size="32"
              class="ai-send-btn"
              :disabled="!draft.trim() || sending"
              @click="handleSend"
            >
              <template #icon><icon-send /></template>
            </a-button>
          </div>

          <!-- 功能按钮 -->
          <div class="ai-footer-actions">
            <a-button type="text" size="mini" @click="clearMessages">
              <template #icon><icon-delete /></template>
              清空对话
            </a-button>
          </div>
        </footer>
      </div>
    </Transition>

    <!-- 遮罩层 -->
    <Transition name="ai-overlay">
      <div v-if="visible && isMobile" class="ai-assistant__overlay" @click="closePanel"></div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import MarkdownIt from 'markdown-it';
import {
  IconMessage,
  IconRobot,
  IconMinus,
  IconClose,
  IconUser,
  IconSend,
  IconDelete,
} from '@arco-design/web-vue/es/icon';
import { chatWithAssistant, fetchAiPresets, fetchRecentSessions, fetchSessionMessages } from 'commonprovide/ai-sdk';
import { getCurrentUser } from 'commonprovide/auth-sdk';

const md = new MarkdownIt({ html: false, breaks: true, linkify: true });

const visible = ref(false);
const draft = ref('');
const sending = ref(false);
const isInputFocused = ref(false);
const currentSessionId = ref('');
const messages = ref([]);
const sessions = ref([]);
const presets = ref([]);
const messagesContainer = ref(null);
const inputRef = ref(null);
const unreadCount = ref(0);

const isMobile = computed(() => {
  if (typeof window === 'undefined') return false;
  return window.innerWidth < 768;
});

const quickQuestions = [
  '商品如何发布？',
  '如何申请退款？',
  '平台有哪些分类？',
  '卖家多久需要发货？',
  '平台收取手续费吗？',
];

onMounted(() => {
  loadPresets();
  loadSessions();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

function handleResize() {
  if (isMobile.value && visible.value) {
    // 移动端适配逻辑
  }
}

function togglePanel() {
  visible.value ? closePanel() : openPanel();
}

function openPanel() {
  visible.value = true;
  unreadCount.value = 0;
  nextTick(() => {
    inputRef.value?.focus();
    scrollToBottom();
  });
}

function closePanel() {
  visible.value = false;
}

function minimizePanel() {
  visible.value = false;
}

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

function clearMessages() {
  currentSessionId.value = '';
  messages.value = [];
}

function sendQuickQuestion(question) {
  draft.value = question;
  handleSend();
}

async function handleSend() {
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
      messagesContainer.value.scrollTo({
        top: messagesContainer.value.scrollHeight,
        behavior: 'smooth',
      });
    }
  });
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: var(--z-tooltip, 700);
}

/* ══════════════════════════════════════
   触发按钮
   ══════════════════════════════════════ */

.ai-assistant__trigger {
  cursor: pointer;
  outline: none;
}

.ai-trigger-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.ai-trigger-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
}

.ai-trigger-btn:active {
  animation: pulse 0.3s ease;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(0.92); }
  100% { transform: scale(1); }
}

/* ══════════════════════════════════════
   对话面板
   ══════════════════════════════════════ */

.ai-assistant__panel {
  position: fixed;
  right: 24px;
  bottom: 80px;
  width: 380px;
  height: 520px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9999;
}

/* 头部 */
.ai-panel__header {
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--border-color-light, #F2F3F5);
  flex-shrink: 0;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-avatar {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  flex-shrink: 0;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-1, #1D2129);
}

.ai-header-right {
  display: flex;
  gap: 4px;
}

.ai-header-btn {
  color: var(--text-color-3, #86909C);
}

.ai-header-btn:hover {
  color: var(--text-color-1, #1D2129);
  background: var(--fill-color-1, #F2F3F5);
}

/* 消息区域 */
.ai-panel__body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #FAFBFC;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 空状态 */
.ai-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}

.ai-empty-avatar {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  margin-bottom: 16px;
  flex-shrink: 0;
}

.ai-empty-text {
  font-size: 15px;
  color: var(--text-color-1, #1D2129);
  margin: 0 0 24px;
  line-height: 1.6;
}

.ai-quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.ai-quick-btn {
  padding: 10px 16px;
  background: #FFFFFF;
  border: 1px solid var(--border-color, #E5E6EB);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-color-2, #4E5969);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.ai-quick-btn:hover {
  border-color: #165DFF;
  color: #165DFF;
  background: var(--color-primary-bg, #E8F3FF);
}

/* 消息气泡 */
.ai-message {
  display: flex;
  gap: 8px;
  max-width: 100%;
  animation: fadeInUp 0.15s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message--user {
  flex-direction: row-reverse;
}

.ai-msg-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}

.ai-message--assistant .ai-msg-avatar {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  color: white;
}

.ai-message--user .ai-msg-avatar {
  background: var(--fill-color-1, #F2F3F5);
  color: var(--text-color-3, #86909C);
}

.ai-message__content {
  max-width: 75%;
  min-width: 0;
}

/* 用户消息气泡 */
.ai-bubble--user {
  background: #165DFF;
  color: white;
  padding: 10px 14px;
  border-radius: 12px 12px 4px 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

/* AI 回复气泡 */
.ai-bubble--assistant {
  background: #F2F3F5;
  color: var(--text-color-1, #1D2129);
  padding: 10px 14px;
  border-radius: 12px 12px 12px 4px;
  font-size: 14px;
  line-height: 1.6;
}

.ai-bubble--assistant :deep(p) {
  margin: 4px 0;
}

.ai-bubble--assistant :deep(p:first-child) {
  margin-top: 0;
}

.ai-bubble--assistant :deep(p:last-child) {
  margin-bottom: 0;
}

.ai-bubble--assistant :deep(strong) {
  font-weight: 600;
}

.ai-bubble--assistant :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 13px;
}

/* 打字动画 */
.ai-bubble--typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 18px;
}

.typing-dot {
  width: 7px;
  height: 7px;
  background: var(--text-color-3, #86909C);
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  40% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

/* 底部输入区 */
.ai-panel__footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color-light, #F2F3F5);
  background: #FFFFFF;
  flex-shrink: 0;
}

.ai-shortcuts {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}

.ai-shortcuts::-webkit-scrollbar {
  display: none;
}

.ai-shortcut-tag {
  padding: 4px 12px;
  background: var(--fill-color-1, #F2F3F5);
  border: none;
  border-radius: 16px;
  font-size: 12px;
  color: var(--text-color-2, #4E5969);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.ai-shortcut-tag:hover {
  background: var(--color-primary-bg, #E8F3FF);
  color: #165DFF;
}

.ai-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.ai-input {
  flex: 1;
}

.ai-input :deep(.arco-textarea-wrapper) {
  border-radius: 24px;
  border: 1px solid var(--border-color, #E5E6EB);
  transition: border-color 0.2s ease;
}

.ai-input :deep(.arco-textarea-wrapper:focus-within) {
  border-color: #165DFF;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.ai-send-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  transition: transform 0.15s ease;
}

.ai-send-btn:not(:disabled):active {
  transform: scale(0.95);
}

.ai-footer-actions {
  margin-top: 8px;
  text-align: center;
}

/* 遮罩层 */
.ai-assistant__overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 9998;
}

/* ══════════════════════════════════════
   过渡动画
   ══════════════════════════════════════ */

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: transform 0.2s ease-out, opacity 0.2s ease-out;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.ai-overlay-enter-active,
.ai-overlay-leave-active {
  transition: opacity 0.2s ease;
}

.ai-overlay-enter-from,
.ai-overlay-leave-to {
  opacity: 0;
}

/* ══════════════════════════════════════
   响应式 - 移动端全屏模式
   ══════════════════════════════════════ */

@media (max-width: 768px) {
  .ai-assistant {
    right: 0;
    bottom: 0;
  }

  .ai-assistant__panel {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    border-radius: 0;
  }

  .ai-trigger-btn {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
}
</style>