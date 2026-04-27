<template>
  <div>
    <a-button
      class="assistant-trigger"
      type="primary"
      shape="round"
      @click="visible = true"
    >
      AI 小助手
    </a-button>
    <a-drawer
      :visible="visible"
      :width="420"
      title="校园二手 AI 助手"
      @cancel="visible = false"
    >
      <template #footer>
        <a-space>
          <a-button @click="clearDraft">清空输入</a-button>
          <a-button type="primary" :loading="sending" @click="sendMessage"
            >发送</a-button
          >
        </a-space>
      </template>
      <a-space direction="vertical" fill>
        <a-select
          v-model="selectedPreset"
          placeholder="选择预设问题"
          allow-clear
          @change="applyPreset"
        >
          <a-option v-for="preset in presets" :key="preset" :value="preset">{{
            preset
          }}</a-option>
        </a-select>
        <div class="chat-history">
          <div v-for="(item, index) in messages" :key="index" class="chat-row">
            <a-typography-text type="secondary">{{
              item.role === "user" ? "我" : "助手"
            }}</a-typography-text>
            <div class="chat-bubble">{{ item.content }}</div>
            <a-typography-text v-if="item.references?.length" class="ref">
              来源：{{ item.references.join(" / ") }}
            </a-typography-text>
          </div>
        </div>
        <a-textarea
          v-model="draft"
          placeholder="输入你的问题..."
          :auto-size="{ minRows: 3, maxRows: 6 }"
        />
      </a-space>
    </a-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { chatWithAssistant, fetchAiPresets } from "commonprovide/ai-sdk";
import { getCurrentUser } from "commonprovide/auth-sdk";

const visible = ref(false);
const draft = ref("");
const sending = ref(false);
const sessionId = ref("");
const messages = ref([]);
const presets = ref([]);
const selectedPreset = ref("");

async function loadPresets() {
  try {
    const result = await fetchAiPresets();
    presets.value = result.presets || [];
  } catch (error) {
    presets.value = [];
    console.warn('加载预设问题失败:', error.message);
  }
}

function clearDraft() {
  draft.value = "";
}

function applyPreset(value) {
  if (value) {
    // 确保 value 是一个字符串
    if (typeof value === 'object') {
      // 如果 value 是一个对象，尝试获取它的某个属性作为预设问题的内容
      draft.value = value.content || JSON.stringify(value);
    } else {
      // 如果 value 是一个字符串，直接将其设置到 draft 中
      draft.value = value;
    }
  }
}

async function sendMessage() {
  if (!draft.value || (typeof draft.value === 'string' && !draft.value.trim())) {
    return;
  }
  const user = getCurrentUser();
  // 确保 content 是一个字符串
  let content = draft.value;
  if (typeof content !== 'string') {
    content = content.content || JSON.stringify(content);
  } else {
    content = content.trim();
  }
  messages.value.push({ role: "user", content });
  draft.value = "";
  sending.value = true;
  try {
    const result = await chatWithAssistant({
      sessionId: sessionId.value || null,
      role: user?.roles?.[0] || "BUYER",
      message: content,
    });
    sessionId.value = result.sessionId;
    messages.value.push({
      role: "assistant",
      content: result.answer,
      references: result.references || [],
    });
  } catch (error) {
    console.error('AI助手对话失败:', error.message);
    messages.value.push({
      role: "assistant",
      content: `助手暂时不可用，请稍后重试。\n错误信息: ${error.message}`,
      references: [],
    });
  } finally {
    sending.value = false;
  }
}

onMounted(loadPresets);
</script>

<style scoped>
.assistant-trigger {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1001;
  box-shadow: 0 12px 28px rgba(68, 127, 255, 0.28);
}

.chat-history {
  height: 420px;
  overflow: auto;
  border: 1px solid #d7e5ff;
  border-radius: 8px;
  background: #f8fbff;
  padding: 12px;
}

.chat-row {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-bubble {
  padding: 8px 10px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dce8ff;
  white-space: pre-wrap;
}

.ref {
  font-size: 12px;
}
</style>
