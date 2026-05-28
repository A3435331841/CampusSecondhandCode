<template>
  <div class="page-layout ai-page">
    <AppHeader />
    <main class="main-content ai-main">
      <!-- Banner -->
      <div class="ai-banner">
        <span class="banner-emoji">🔮</span>
        <h2 class="banner-title">AI 智能找货</h2>
        <p class="banner-sub">告诉我你想要什么，AI帮你精准匹配</p>
      </div>

      <!-- Chat Area -->
      <div class="chat-container" ref="chatRef">
        <!-- Welcome -->
        <div class="msg-row ai" v-if="chatMessages.length === 0 && !loading">
          <div class="ai-avatar"><span>🤖</span></div>
          <div class="bubble-wrap">
            <div class="bubble ai-bubble">你好！我是AI小助手，告诉我你想要什么，我帮你找~</div>
            <div class="quick-tags">
              <span class="tags-label">试试这些：</span>
              <div class="tags-row">
                <span class="tag" v-for="tag in quickTags" :key="tag" @click="sendQuickTag(tag)">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Messages -->
        <template v-for="(msg, index) in chatMessages" :key="index">
          <div class="msg-row user" v-if="msg.role === 'user'">
            <div class="bubble-wrap">
              <div class="bubble user-bubble">{{ msg.content }}</div>
            </div>
          </div>

          <div class="msg-row ai" v-if="msg.role === 'ai'">
            <div class="ai-avatar"><span>🤖</span></div>
            <div class="bubble-wrap">
              <div class="bubble ai-bubble">{{ msg.content }}</div>
              <div class="product-cards" v-if="msg.products?.length">
                <div class="product-card" v-for="p in msg.products" :key="p.id" @click="$router.push(`/detail/${p.id}`)">
                  <img :src="p.image" class="product-img" @error="e => e.target.src = FALLBACK_IMG" />
                  <div class="product-info">
                    <p class="product-title">{{ p.title }}</p>
                    <p class="product-price">¥{{ p.price }}</p>
                  </div>
                </div>
                <p class="products-total" v-if="msg.total > 3">共找到 {{ msg.total }} 件，已展示前3件</p>
              </div>
              <div class="quick-tags" v-if="msg.showTags">
                <div class="tags-row">
                  <span class="tag" v-for="tag in followUpTags" :key="tag" @click="sendQuickTag(tag)">{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- Loading -->
        <div class="msg-row ai" v-if="loading">
          <div class="ai-avatar"><span>🤖</span></div>
          <div class="bubble-wrap">
            <div class="bubble ai-bubble">
              <div class="thinking-dots">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Bar -->
      <div class="input-bar">
        <div class="input-bar-inner">
          <el-input
            v-model="inputText"
            placeholder="描述你想要的商品..."
            class="chat-input"
            @keyup.enter="sendMessage"
            clearable
          />
          <el-button
            type="primary"
            circle
            class="send-btn"
            :disabled="!inputText.trim() || loading"
            @click="sendMessage"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { aiChat } from '@/api/ai'

const router = useRouter()
const inputText = ref('')
const loading = ref(false)
const chatRef = ref(null)
const chatMessages = ref([])
const conversationHistory = ref([])

const quickTags = ['考研资料', '笔记本电脑', '自行车', '台灯', '充电器', '高数教材']
const followUpTags = ['换个推荐', '便宜一点的', '更多选择']

const FALLBACK_IMG = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'

const normalizeImage = (url) => {
  if (!url) return FALLBACK_IMG
  if (/^https?:\/\//.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const formatProducts = (list) => {
  return (Array.isArray(list) ? list : []).slice(0, 3).map(item => ({
    id: item.id,
    title: item.title,
    price: item.price,
    image: normalizeImage(item.images?.split(',')[0]?.trim())
  }))
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatRef.value) {
    chatRef.value.scrollTop = chatRef.value.scrollHeight
  }
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
    const res = await aiChat({ message: text, history: conversationHistory.value })
    const reply = res?.reply || '让我帮你找找看~'
    const products = formatProducts(res?.products || [])
    const total = res?.total || 0

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
</script>

<style scoped>
.ai-page {
  min-height: 100vh;
  background: #F0F2F5;
}

.ai-main {
  padding-top: var(--header-height);
  display: flex;
  flex-direction: column;
  height: 100vh;
}

/* Banner */
.ai-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 28px 24px 20px;
  text-align: center;
  flex-shrink: 0;
}

.banner-emoji {
  font-size: 36px;
}

.banner-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 8px 0 4px;
}

.banner-sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* Chat container */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
}

/* Message row */
.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
  font-size: 20px;
}

.msg-row.user .ai-avatar {
  margin-right: 0;
  margin-left: 12px;
}

.bubble-wrap {
  max-width: 75%;
}

.bubble {
  padding: 14px 18px;
  border-radius: 6px 20px 20px 20px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-all;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

.ai-bubble {
  background: #fff;
  color: #333;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 20px 6px 20px 20px;
}

/* Thinking dots */
.thinking-dots {
  display: flex;
  gap: 6px;
  padding: 4px 0;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: bounce 1.4s ease-in-out infinite;
}

.dot:nth-child(2) { animation-delay: 0.16s; }
.dot:nth-child(3) { animation-delay: 0.32s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* Quick tags */
.quick-tags {
  margin-top: 12px;
}

.tags-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: block;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #F0EDFF;
  border-radius: 16px;
  padding: 6px 14px;
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  transition: background 0.2s;
}

.tag:hover {
  background: #E0DBFF;
}

/* Product cards */
.product-cards {
  margin-top: 12px;
}

.product-card {
  display: flex;
  align-items: center;
  background: #FAFAFA;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: border-color 0.2s;
}

.product-card:hover {
  border-color: #667eea;
}

.product-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  flex-shrink: 0;
  background: #eee;
}

.product-info {
  flex: 1;
  padding: 10px 14px;
  min-width: 0;
}

.product-title {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.product-price {
  font-size: 16px;
  color: var(--accent);
  font-weight: 700;
}

.products-total {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px 0;
}

/* Input bar */
.input-bar {
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
  padding: 14px 24px;
}

.input-bar-inner {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 50px;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  flex-shrink: 0;
}

.send-btn:hover {
  opacity: 0.9;
}

.send-btn.is-disabled {
  opacity: 0.5;
}

@media (max-width: 768px) {
  .chat-container {
    padding: 16px;
  }

  .bubble-wrap {
    max-width: 85%;
  }
}
</style>
