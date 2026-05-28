<template>
  <view class="ai-page">
    <!-- Header -->
    <view class="ai-header">
      <text class="header-title">AI 智能找货</text>
    </view>

    <!-- Chat Area -->
    <scroll-view class="chat-area" scroll-y :scroll-into-view="scrollAnchor" scroll-with-animation>
      <view class="chat-padding"></view>

      <!-- Welcome Message -->
      <view class="msg-row ai" v-if="chatMessages.length === 0 && !loading">
        <view class="ai-avatar-wrap">
          <text class="ai-avatar-icon">🤖</text>
        </view>
        <view class="bubble-wrap">
          <view class="bubble ai-bubble">
            <text class="bubble-text">你好！我是AI小助手，告诉我你想要什么，我帮你找~</text>
          </view>
          <!-- Quick Tags -->
          <view class="quick-tags">
            <text class="tags-label">试试这些：</text>
            <view class="tags-row">
              <view class="tag" v-for="tag in quickTags" :key="tag" @click="sendQuickTag(tag)">
                <text class="tag-text">{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Chat Messages -->
      <view v-for="(msg, index) in chatMessages" :key="index" :id="`ai-msg-${index}`">
        <!-- User Message -->
        <view class="msg-row user" v-if="msg.role === 'user'">
          <view class="bubble-wrap">
            <view class="bubble user-bubble">
              <text class="bubble-text">{{ msg.content }}</text>
            </view>
          </view>
        </view>

        <!-- AI Message -->
        <view class="msg-row ai" v-if="msg.role === 'ai'">
          <view class="ai-avatar-wrap">
            <text class="ai-avatar-icon">🤖</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble ai-bubble">
              <text class="bubble-text">{{ msg.content }}</text>
            </view>
            <!-- Product Cards -->
            <view class="product-cards" v-if="msg.products && msg.products.length > 0">
              <view class="product-card" v-for="p in msg.products" :key="p.id" @click="goDetail(p.id)">
                <image :src="p.image" class="product-img" mode="aspectFill"></image>
                <view class="product-info">
                  <text class="product-title">{{ p.title }}</text>
                  <text class="product-price">¥{{ p.price }}</text>
                </view>
              </view>
              <view class="products-total" v-if="msg.total > 3">
                <text class="products-total-text">共找到 {{ msg.total }} 件，已展示前3件</text>
              </view>
            </view>
            <!-- Suggestion Tags after AI response -->
            <view class="quick-tags" v-if="msg.showTags">
              <view class="tags-row">
                <view class="tag" v-for="tag in followUpTags" :key="tag" @click="sendQuickTag(tag)">
                  <text class="tag-text">{{ tag }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Loading -->
      <view class="msg-row ai" v-if="loading">
        <view class="ai-avatar-wrap">
          <text class="ai-avatar-icon">🤖</text>
        </view>
        <view class="bubble-wrap">
          <view class="bubble ai-bubble">
            <view class="thinking-dots">
              <view class="dot dot1"></view>
              <view class="dot dot2"></view>
              <view class="dot dot3"></view>
            </view>
          </view>
        </view>
      </view>

      <view class="chat-padding-bottom"></view>
    </scroll-view>

    <!-- Input Bar -->
    <view class="input-bar">
      <view class="input-wrap">
        <input
          v-model="inputText"
          class="input"
          type="text"
          placeholder="描述你想要的商品..."
          placeholder-class="input-placeholder"
          confirm-type="send"
          @confirm="sendMessage"
        />
      </view>
      <view class="send-btn" :class="{ disabled: !inputText.trim() || loading }" @click="sendMessage">
        <text class="send-icon">➤</text>
      </view>
    </view>

    <custom-tabbar :current="1" />
  </view>
</template>

<script setup>
import { getCurrentInstance, nextTick, ref } from 'vue'
import { normalizeImage as normalizeImageUrl } from '@/utils/config'
import customTabbar from '@/components/custom-tabbar.vue'

const { proxy } = getCurrentInstance()
const inputText = ref('')
const loading = ref(false)
const scrollAnchor = ref('')
const chatMessages = ref([])
const conversationHistory = ref([])

const quickTags = ['考研资料', '笔记本电脑', '自行车', '台灯', '充电器', '高数教材']
const followUpTags = ['换个推荐', '便宜一点的', '更多选择']

const scrollToBottom = async () => {
  await nextTick()
  if (chatMessages.value.length > 0) {
    scrollAnchor.value = ''
    await nextTick()
    scrollAnchor.value = `ai-msg-${chatMessages.value.length - 1}`
  }
}

const formatProducts = (list) => {
  return (Array.isArray(list) ? list : []).slice(0, 3).map(item => ({
    id: item.id,
    title: item.title,
    price: item.price,
    image: item.images ? normalizeImageUrl(item.images.split(',')[0].trim()) : ''
  }))
}

const sendQuickTag = (tag) => {
  inputText.value = tag
  sendMessage()
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  chatMessages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  conversationHistory.value.push({ role: 'user', content: text })

  try {
    const res = await proxy.$request({
      url: '/ai/chat',
      method: 'POST',
      data: {
        message: text,
        history: conversationHistory.value
      }
    })

    const reply = res?.reply || '让我帮你找找看~'
    const products = formatProducts(res?.products || [])
    const total = res?.total || 0
    const isLast = chatMessages.value.filter(m => m.role === 'ai').length < 1

    chatMessages.value.push({
      role: 'ai',
      content: reply,
      products,
      total,
      showTags: products.length > 0
    })

    conversationHistory.value.push({ role: 'assistant', content: reply })
  } catch {
    chatMessages.value.push({
      role: 'ai',
      content: '网络不太稳定，请再试一次~',
      products: [],
      total: 0,
      showTags: false
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const goDetail = (id) => {
  if (!id) return
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}
</script>

<style lang="scss">
page {
  background-color: #F0F2F5;
}

.ai-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Header */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  z-index: 10;
}

.header-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #fff;
}

/* Chat Area */
.chat-area {
  flex: 1;
  padding: 0 24rpx;
  padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
}

.chat-padding { height: 24rpx; }
.chat-padding-bottom { height: 24rpx; }

/* Message Row */
.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 28rpx;

  &.user {
    flex-direction: row-reverse;
  }
}

.ai-avatar-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16rpx;
}

.ai-avatar-icon {
  font-size: 36rpx;
}

.bubble-wrap {
  max-width: 80%;
}

.bubble {
  padding: 24rpx 28rpx;
  border-radius: 8rpx 32rpx 32rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.ai-bubble {
  background: #fff;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 32rpx 8rpx 32rpx 32rpx;
}

.user .bubble-text {
  color: #fff;
}

.ai .bubble-text {
  color: #333;
}

.bubble-text {
  font-size: 30rpx;
  line-height: 1.6;
  word-break: break-all;
}

/* Thinking Dots */
.thinking-dots {
  display: flex;
  gap: 12rpx;
  padding: 8rpx 0;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #667eea;
  animation: bounce 1.4s ease-in-out infinite;
}

.dot2 { animation-delay: 0.16s; }
.dot3 { animation-delay: 0.32s; }

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Quick Tags */
.quick-tags {
  margin-top: 20rpx;
}

.tags-label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 12rpx;
  display: block;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag {
  background: #F0EDFF;
  border-radius: 24rpx;
  padding: 10rpx 24rpx;
}

.tag-text {
  font-size: 24rpx;
  color: #667eea;
}

/* Product Cards inside chat */
.product-cards {
  margin-top: 16rpx;
}

.product-card {
  display: flex;
  align-items: center;
  background: #FAFAFA;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
  border: 1rpx solid #f0f0f0;
}

.product-img {
  width: 140rpx;
  height: 140rpx;
  flex-shrink: 0;
  background: #eee;
}

.product-info {
  flex: 1;
  padding: 16rpx 20rpx;
  min-width: 0;
}

.product-title {
  display: block;
  font-size: 26rpx;
  color: #1A1A1A;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.product-price {
  display: block;
  font-size: 30rpx;
  color: #FF6633;
  font-weight: bold;
}

.products-total {
  text-align: center;
  padding: 8rpx 0;
}

.products-total-text {
  font-size: 22rpx;
  color: #999;
}

/* Input Bar */
.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx calc(160rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.04);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.input-wrap {
  flex: 1;
  background: #F5F6F8;
  border-radius: 40rpx;
}

.input {
  height: 80rpx;
  padding: 0 28rpx;
  font-size: 30rpx;
  color: #333;
}

.input-placeholder {
  color: #bbb;
}

.send-btn {
  margin-left: 16rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.35);
  flex-shrink: 0;
  transition: opacity 0.2s;

  &.disabled {
    opacity: 0.5;
  }
}

.send-icon {
  font-size: 36rpx;
  color: #fff;
}
</style>
