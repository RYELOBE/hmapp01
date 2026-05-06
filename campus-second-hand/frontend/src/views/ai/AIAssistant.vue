<template>
  <div class="ai-assistant-page">
    <div class="page-container">
      <div class="left-panel">
        <div class="ai-intro-card">
          <div class="ai-header">
            <div class="ai-avatar">
              <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="aiGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#165DFF"/>
                    <stop offset="100%" style="stop-color:#4080FF"/>
                  </linearGradient>
                </defs>
                <circle cx="50" cy="50" r="45" fill="url(#aiGradient)"/>
                <circle cx="50" cy="40" r="18" fill="white"/>
                <rect x="32" y="62" width="36" height="25" rx="5" fill="white"/>
                <circle cx="42" cy="38" r="4" fill="#165DFF"/>
                <circle cx="58" cy="38" r="4" fill="#165DFF"/>
                <path d="M 40 48 Q 50 54 60 48" stroke="#165DFF" stroke-width="2" fill="none"/>
              </svg>
            </div>
            <div class="ai-info">
              <h2 class="ai-name">校园AI助手</h2>
              <p class="ai-status">
                <span class="status-dot"></span>
                在线服务中
              </p>
            </div>
          </div>
          
          <div class="ai-description">
            <p>您好！我是校园AI助手，可以帮助您：</p>
            <ul class="feature-list">
              <li>
                <icon-robot />
                <span>商品咨询与推荐</span>
              </li>
              <li>
                <icon-mall />
                <span>交易流程指导</span>
              </li>
              <li>
                <icon-safe />
                <span>校园生活帮助</span>
              </li>
              <li>
                <icon-service />
                <span>问题解答支持</span>
              </li>
            </ul>
          </div>

          <div class="quick-actions">
            <h3 class="section-title">
              <icon-apps />
              快捷入口
            </h3>
            <div class="action-buttons">
              <a-button type="outline" class="action-btn" @click="handleActionClick('product')">
                <template #icon><icon-shopping-cart /></template>
                商品咨询
              </a-button>
              <a-button type="outline" class="action-btn" @click="handleActionClick('recommend')">
                <template #icon><icon-thumb-up /></template>
                智能推荐
              </a-button>
              <a-button type="outline" class="action-btn" @click="handleActionClick('campus')">
                <template #icon><icon-bookmark /></template>
                校园助手
              </a-button>
              <a-button type="outline" class="action-btn" @click="handleActionClick('help')">
                <template #icon><icon-question-circle /></template>
                交易帮助
              </a-button>
            </div>
          </div>

          <div class="tips-section">
            <h3 class="section-title">
              <icon-light />
              使用提示
            </h3>
            <div class="tips-content">
              <p>💡 可以直接输入商品名称进行搜索</p>
              <p>💡 描述您的需求获得个性化推荐</p>
              <p>💬 支持中文对话交流</p>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="chat-container">
          <div class="chat-header">
            <div class="chat-title">
              <icon-message />
              <span>智能对话</span>
            </div>
            <a-button type="text" status="warning" @click="clearChat">
              <template #icon><icon-delete /></template>
              清空对话
            </a-button>
          </div>

          <div class="chat-messages" ref="messageContainer">
            <div v-if="messages.length === 0" class="empty-state">
              <div class="empty-illustration">
                <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="100" cy="100" r="80" fill="#EEF2FF" stroke="#165DFF" stroke-width="2"/>
                  <circle cx="100" cy="80" r="30" fill="#165DFF"/>
                  <rect x="70" y="115" width="60" height="40" rx="8" fill="#165DFF"/>
                  <circle cx="90" cy="75" r="5" fill="white"/>
                  <circle cx="110" cy="75" r="5" fill="white"/>
                  <path d="M 88 90 Q 100 98 112 90" stroke="white" stroke-width="2" fill="none"/>
                </svg>
              </div>
              <p class="empty-title">开始与AI助手对话</p>
              <p class="empty-subtitle">点击下方快捷问题快速开始</p>
            </div>

            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message-wrapper', msg.role === 'user' ? 'user-message' : 'ai-message']"
            >
              <div v-if="msg.role === 'ai'" class="avatar ai-avatar">
                <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="50" cy="50" r="45" fill="#165DFF"/>
                  <circle cx="50" cy="40" r="18" fill="white"/>
                  <rect x="32" y="62" width="36" height="25" rx="5" fill="white"/>
                </svg>
              </div>
              
              <div class="message-content">
                <div class="message-bubble" :class="{ 'user-bubble': msg.role === 'user' }">
                  {{ msg.content }}
                </div>
                <div class="message-time">{{ msg.time }}</div>
              </div>

              <div v-if="msg.role === 'user'" class="avatar user-avatar">
                <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="50" cy="50" r="45" fill="#00D084"/>
                  <circle cx="50" cy="40" r="18" fill="white"/>
                  <rect x="32" y="62" width="36" height="25" rx="5" fill="white"/>
                </svg>
              </div>
            </div>

            <div v-if="isTyping" class="message-wrapper ai-message">
              <div class="avatar ai-avatar">
                <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="50" cy="50" r="45" fill="#165DFF"/>
                  <circle cx="50" cy="40" r="18" fill="white"/>
                  <rect x="32" y="62" width="36" height="25" rx="5" fill="white"/>
                </svg>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="quick-questions">
            <span class="quick-label">快捷问题：</span>
            <div class="quick-buttons">
              <a-tag
                v-for="q in quickQuestions"
                :key="q"
                class="quick-tag"
                @click="sendQuickQuestion(q)"
              >
                {{ q }}
              </a-tag>
            </div>
          </div>

          <div class="input-area">
            <a-textarea
              v-model="inputText"
              placeholder="输入您的问题..."
              class="chat-input"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              @keydown.enter.exact.prevent="handleSend"
              ref="inputRef"
            />
            <div class="input-actions">
              <a-button
                type="primary"
                :disabled="!inputText.trim() || isTyping"
                :loading="isTyping"
                @click="handleSend"
              >
                <template #icon><icon-send /></template>
                发送
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'

const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const messageContainer = ref(null)
const inputRef = ref(null)

const quickQuestions = [
  '这本书还有吗？',
  '推荐一些电子产品',
  '如何发布商品？',
  '有什么优惠活动？'
]

const getCurrentTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

const generateAIResponse = (userMessage) => {
  const msg = userMessage.toLowerCase()
  
  if (msg.includes('有') && (msg.includes('吗') || msg.includes('没') || msg.includes('还'))) {
    return '您好！关于商品的库存情况，建议您直接在商品详情页面查看实时信息，或者告诉我具体的商品名称，我可以帮您查询相关商品的详细信息。如果您看到心仪的商品，建议尽快下单锁定哦！😊'
  }
  
  if (msg.includes('推荐') || msg.includes('想要') || msg.includes('需要')) {
    if (msg.includes('电子') || msg.includes('手机') || msg.includes('电脑')) {
      return '为您推荐以下热门电子产品：\n\n📱 iPhone 14 Pro Max - ¥6999\n  99新，带原装充电器\n\n💻 MacBook Air M1 - ¥4999\n  轻薄便携，学生首选\n\n🎮 Switch OLED - ¥1899\n  娱乐学习两不误\n\n🎧 AirPods Pro 2代 - ¥1599\n  降噪效果好\n\n点击商品可以直接查看详情哦！'
    }
    if (msg.includes('书') || msg.includes('教材')) {
      return '为您推荐热门图书教材：\n\n📚 高等数学教材全套 - ¥89\n  九成新，包含习题解答\n\n📖 大学英语系列 - ¥45\n  包含1-4册\n\n📕 经济学原理 - ¥65\n  考研必备教材\n\n需要了解更多信息可以随时问我！'
    }
    return '根据您的需求，为您推荐以下热门商品：\n\n🔥 戴森V12吸尘器 - ¥2599\n  校园宿舍清洁神器\n\n🎧 AirPods Pro - ¥1599\n  学习娱乐好帮手\n\n📱 iPhone 14 - ¥5499\n  性价比超高\n\n您可以告诉我更具体的需求，比如商品类型、品牌等，我会给出更精准的推荐！'
  }
  
  if (msg.includes('发布') || msg.includes('卖') || msg.includes('上架')) {
    return '发布商品非常简单！\n\n📝 发布步骤：\n1. 点击右上角"发布商品"按钮\n2. 填写商品信息（名称、价格、描述）\n3. 上传商品图片\n4. 选择商品分类\n5. 点击确认发布即可\n\n💡 小提示：\n• 图片清晰能提高成交率\n• 价格合理更易售出\n• 详细的描述能减少咨询量\n\n需要我帮您发布商品吗？'
  }
  
  if (msg.includes('优惠') || msg.includes('活动') || msg.includes('折扣') || msg.includes('便宜')) {
    return '🎉 当前优惠活动：\n\n1️⃣ 新用户首单立减\n   首单满100减10元\n\n2️⃣ 学生认证专享\n   认证学生额外9折\n\n3️⃣ 限时秒杀\n   每日10点、20点限时特价\n\n4️⃣ 邀请好友\n   邀请1位好友双方各得5元优惠券\n\n5️⃣ 积分兑换\n   100积分抵1元\n\n请问我还有什么可以帮您的？'
  }
  
  if (msg.includes('安全') || msg.includes('骗') || msg.includes('放心')) {
    return '🛡️ 校园二手交易安全保障：\n\n✅ 实名认证\n   所有用户需完成学生认证\n\n✅ 交易担保\n   平台托管资金，交易成功才放行\n\n✅ 评价系统\n   真实用户评价，诚信交易\n\n✅ 客服支持\n   7×24小时在线客服\n\n⚠️ 温馨提示：\n• 请使用平台内交易，不要私下转账\n• 见面交易选择校园内公共场所\n• 保留聊天记录作为凭证\n\n祝您交易愉快！'
  }
  
  if (msg.includes('你好') || msg.includes('您好') || msg.includes('嗨') || msg.includes('hi') || msg.includes('hello')) {
    return '您好！👋 欢迎来到校园二手交易平台！\n\n我是您的AI助手，可以帮您：\n\n🔍 搜索和咨询商品\n💡 获取个性化推荐\n📝 了解交易流程\n🎁 查看优惠活动\n❓ 解答各类问题\n\n请问有什么可以帮您的？'
  }
  
  if (msg.includes('联系') || msg.includes('客服') || msg.includes('人工')) {
    return '📞 联系客服方式：\n\n1️⃣ 在线客服\n   点击右下角"在线客服"图标\n\n2️⃣ 电话热线\n   400-888-8888\n   工作时间：9:00-21:00\n\n3️⃣ 邮件反馈\n   support@campus-secondhand.com\n\n4️⃣ 校园代理点\n   各校区均有线下服务点\n\n您也可以直接在这里问我，我会尽力帮您解答！'
  }
  
  if (msg.includes('付款') || msg.includes('支付') || msg.includes('钱')) {
    return '💳 支付方式说明：\n\n支持的支付方式：\n\n✅ 微信支付\n✅ 支付宝\n✅ 银行卡支付\n✅ 校园卡支付（部分校区）\n\n⏰ 支付安全提示：\n• 请在平台内完成支付\n• 不要点击陌生链接\n• 支付前核对订单信息\n\n💰 退款说明：\n交易完成后如有问题，可在7天内申请退款，我们会尽快处理。'
  }
  
  if (msg.includes('配送') || msg.includes('快递') || msg.includes('送货') || msg.includes('自提')) {
    return '🚚 配送方式：\n\n1️⃣ 校内自提\n   免费，约定时间地点见面交易\n\n2️⃣ 校内配送\n   ¥2-5，1小时内送达\n\n3️⃣ 校外快递\n   按快递公司标准收费\n\n💡 温馨提示：\n• 建议选择校内自提，安全便捷\n• 见面交易选择人流较多的公共场所\n• 如有校园电动车配送服务，会更快捷\n\n您可以根据实际情况选择合适的配送方式！'
  }
  
  return '感谢您的提问！😊\n\n根据您的需求，我可以为您提供以下帮助：\n\n📱 商品咨询\n   告诉我想买的商品类型\n\n💡 智能推荐\n   描述您的预算和需求\n\n📝 发布指导\n   了解如何快速发布商品\n\n🔍 交易帮助\n   获取交易流程和安全保障\n\n请告诉我具体想了解什么，或者试试点击快捷问题？'
}

const handleSend = () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  messages.value.push({
    role: 'user',
    content: text,
    time: getCurrentTime()
  })
  
  inputText.value = ''
  scrollToBottom()
  
  isTyping.value = true
  
  setTimeout(() => {
    isTyping.value = false
    
    const response = generateAIResponse(text)
    messages.value.push({
      role: 'ai',
      content: response,
      time: getCurrentTime()
    })
    
    scrollToBottom()
  }, 1000 + Math.random() * 1000)
}

const sendQuickQuestion = (question) => {
  inputText.value = question
  handleSend()
}

const clearChat = () => {
  messages.value = []
}

const handleActionClick = (type) => {
  const actions = {
    product: '我想买一个电子产品，请给我推荐一些热门商品',
    recommend: '请根据我的需求推荐一些适合的商品',
    campus: '校园内有什么二手交易需要注意的事项吗？',
    help: '请告诉我如何在平台安全交易'
  }
  
  if (actions[type]) {
    inputText.value = actions[type]
    handleSend()
  }
}

onMounted(() => {
  if (inputRef.value) {
    const textarea = inputRef.value.$el?.querySelector('textarea')
    if (textarea) {
      textarea.focus()
    }
  }
})
</script>

<style scoped>
.ai-assistant-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #F2F3F5 0%, #E5E6EB 100%);
  padding: 24px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  height: calc(100vh - 48px);
}

.left-panel {
  display: flex;
  flex-direction: column;
}

.ai-intro-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  flex: 1;
  overflow-y: auto;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E5E6EB;
  margin-bottom: 20px;
}

.ai-avatar svg {
  width: 56px;
  height: 56px;
}

.ai-name {
  font-size: 20px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 6px 0;
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4B4B4B;
  margin: 0;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #00D084;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.ai-description {
  margin-bottom: 24px;
}

.ai-description p {
  font-size: 14px;
  color: #4B4B4B;
  margin: 0 0 16px 0;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 14px;
  color: #1D2129;
  border-bottom: 1px dashed #E5E6EB;
}

.feature-list li:last-child {
  border-bottom: none;
}

.feature-list li :deep(.arco-icon) {
  color: #165DFF;
  font-size: 18px;
}

.quick-actions {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 16px 0;
}

.section-title :deep(.arco-icon) {
  color: #165DFF;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.action-btn {
  justify-content: flex-start;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  border-color: #165DFF;
  color: #165DFF;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #EEF2FF;
  transform: translateY(-2px);
}

.tips-section {
  background: #F7F8FA;
  border-radius: 12px;
  padding: 16px;
}

.tips-content p {
  font-size: 13px;
  color: #4B4B4B;
  margin: 8px 0;
  line-height: 1.6;
}

.right-panel {
  display: flex;
  flex-direction: column;
}

.chat-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.08);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #E5E6EB;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  color: white;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
}

.chat-header :deep(.arco-btn-text) {
  color: white;
}

.chat-header :deep(.arco-btn-text:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #F7F8FA;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-illustration svg {
  width: 120px;
  height: 120px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 8px 0;
}

.empty-subtitle {
  font-size: 14px;
  color: #86909C;
  margin: 0;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-wrapper.user-message {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar svg {
  width: 100%;
  height: 100%;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  font-size: 14px;
  line-height: 1.6;
  color: #1D2129;
  white-space: pre-wrap;
}

.message-bubble.user-bubble {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #86909C;
  margin-top: 6px;
  padding: 0 4px;
}

.user-message .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #165DFF;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.quick-questions {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #E5E6EB;
}

.quick-label {
  font-size: 13px;
  color: #86909C;
  margin-bottom: 10px;
  display: block;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 16px;
  background: #F2F3F5;
  border: 1px solid #E5E6EB;
  color: #4B4B4B;
  font-size: 13px;
  transition: all 0.3s ease;
}

.quick-tag:hover {
  background: #EEF2FF;
  border-color: #165DFF;
  color: #165DFF;
  transform: translateY(-2px);
}

.input-area {
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #E5E6EB;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  border-radius: 10px;
}

.chat-input :deep(.arco-textarea) {
  border-radius: 10px;
}

.chat-input :deep(.arco-textarea-wrapper) {
  border-radius: 10px;
  border-color: #E5E6EB;
}

.chat-input :deep(.arco-textarea-wrapper:focus-within) {
  border-color: #165DFF;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.input-actions :deep(.arco-btn-primary) {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border: none;
  border-radius: 10px;
  padding: 0 20px;
  height: 32px;
}

@media (max-width: 1024px) {
  .page-container {
    grid-template-columns: 1fr;
  }
  
  .left-panel {
    max-height: 400px;
  }
}
</style>
