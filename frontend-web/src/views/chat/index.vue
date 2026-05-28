<template>
  <div class="page-layout chat-page">
    <AppHeader />
    <main class="main-content chat-main">
      <!-- Product Card -->
      <div class="chat-product" v-if="product" @click="$router.push(`/detail/${product.id}`)">
        <img :src="product.cover" class="chat-product-img" />
        <div class="chat-product-info">
          <p class="chat-product-title">{{ product.title }}</p>
          <p class="chat-product-price">¥{{ product.price }}</p>
        </div>
        <el-icon class="chat-product-arrow"><ArrowRight /></el-icon>
      </div>

      <!-- Chat Title -->
      <div class="chat-title-bar">
        <span class="chat-peer-name">{{ peerNickname || ('用户' + toUserId) }}</span>
        <span class="connect-status" :class="{ online: connected }">{{ connected ? '在线' : '连接中...' }}</span>
      </div>

      <!-- Messages -->
      <div class="messages-area" ref="messagesRef">
        <template v-for="(item, index) in messages" :key="index">
          <div class="time-divider" v-if="showTimeDivider(index)">
            <span>{{ formatTimeFull(item.timestamp) }}</span>
          </div>
          <div class="msg-row" :class="{ self: item.self }">
            <el-avatar v-if="!item.self" :size="36" :src="peerAvatar" />
            <div class="bubble-wrap">
              <div class="bubble" :class="{ 'bubble-self': item.self }">{{ item.content }}</div>
            </div>
            <el-avatar v-if="item.self" :size="36" :src="myAvatar" />
          </div>
        </template>
        <div v-if="messages.length === 0 && !loading" class="empty-chat">
          <p>暂无消息，发送一条开始聊天吧</p>
        </div>
      </div>

      <!-- Input -->
      <div class="input-bar">
        <div class="input-bar-inner">
          <el-input
            v-model="inputText"
            placeholder="输入消息..."
            class="msg-input"
            @keyup.enter="sendMessage"
          />
          <el-button
            type="primary"
            circle
            class="send-btn"
            :disabled="!inputText.trim() || !connected"
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
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { getChatHistory } from '@/api/chat'
import { getProductDetail } from '@/api/product'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const toUserId = ref(route.query.toUserId || '')
const productId = ref(route.query.productId || '')
const peerNickname = ref(route.query.nickname || '')

const messages = ref([])
const inputText = ref('')
const connected = ref(false)
const loading = ref(false)
const product = ref(null)
const messagesRef = ref(null)

let ws = null

const currentUserId = computed(() => user.userInfo?.userId)

const myAvatar = computed(() =>
  `https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUserId.value}`
)

const peerAvatar = computed(() =>
  `https://api.dicebear.com/7.x/avataaars/svg?seed=${toUserId.value}`
)

const normalizeImage = (url) => {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const loadProduct = async () => {
  if (!productId.value) return
  try {
    const res = await getProductDetail(productId.value)
    if (res) {
      const imgs = (res.images || '').split(',').filter(Boolean)
      product.value = {
        id: res.id,
        title: res.title,
        price: res.price,
        cover: normalizeImage(imgs[0])
      }
    }
  } catch {}
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getChatHistory({
      toUserId: Number(toUserId.value),
      productId: Number(productId.value),
      current: 1,
      size: 100
    })
    messages.value = (res?.records || []).map(msg => ({
      ...msg,
      self: String(msg.fromUserId) === String(currentUserId.value)
    }))
    scrollToBottom()
  } catch {}
  loading.value = false
}

const connectWebSocket = () => {
  const token = localStorage.getItem('user_token')
  if (!token) return

  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${location.host}/ws/chat?token=${token}`

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    connected.value = true
  }

  ws.onclose = () => {
    connected.value = false
  }

  ws.onerror = () => {
    connected.value = false
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (!msg.content) return
      if (msg.productId && String(msg.productId) !== String(productId.value)) return
      if (msg.type === 'system' || msg.type === 'ack') return
      messages.value.push({
        ...msg,
        self: String(msg.fromUserId) === String(currentUserId.value)
      })
      scrollToBottom()
    } catch {}
  }
}

const sendMessage = () => {
  const text = inputText.value.trim()
  if (!text || !connected.value || !ws) return

  ws.send(JSON.stringify({
    toUserId: Number(toUserId.value),
    productId: Number(productId.value),
    content: text,
    type: 'text'
  }))
  inputText.value = ''
}

const showTimeDivider = (index) => {
  if (index === 0) return true
  const prev = messages.value[index - 1]
  const curr = messages.value[index]
  if (!prev.timestamp || !curr.timestamp) return false
  return new Date(curr.timestamp) - new Date(prev.timestamp) > 5 * 60 * 1000
}

const formatTimeFull = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${hh}:${mm}`
  return `${d.getMonth() + 1}-${d.getDate()} ${hh}:${mm}`
}

onMounted(() => {
  loadProduct()
  loadHistory()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
    ws = null
  }
})
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background: #F0F2F5;
}

.chat-main {
  padding-top: var(--header-height);
  display: flex;
  flex-direction: column;
  height: 100vh;
}

/* Product card */
.chat-product {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s;
}

.chat-product:hover {
  background: #fafafa;
}

.chat-product-img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  background: #f0f0f0;
  flex-shrink: 0;
  margin-right: 14px;
}

.chat-product-info {
  flex: 1;
  min-width: 0;
}

.chat-product-title {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.chat-product-price {
  font-size: 15px;
  color: var(--accent);
  font-weight: 600;
}

.chat-product-arrow {
  color: #ccc;
  font-size: 16px;
  flex-shrink: 0;
}

/* Title bar */
.chat-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.chat-peer-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.connect-status {
  font-size: 12px;
  color: #faad14;
  background: #fffbe6;
  padding: 2px 10px;
  border-radius: 10px;
}

.connect-status.online {
  color: #52c41a;
  background: #f6ffed;
}

/* Messages area */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
}

.empty-chat {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 80px 0;
}

/* Time divider */
.time-divider {
  text-align: center;
  padding: 16px 0 12px;
}

.time-divider span {
  font-size: 12px;
  color: #999;
  background: rgba(0, 0, 0, 0.06);
  padding: 3px 12px;
  border-radius: 10px;
}

/* Message row */
.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.bubble-wrap {
  max-width: 65%;
}

.bubble {
  padding: 12px 16px;
  border-radius: 6px 18px 18px 18px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-all;
  background: #fff;
  color: #333;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.bubble-self {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  border-radius: 18px 6px 18px 18px;
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

.msg-input :deep(.el-input__wrapper) {
  border-radius: 50px;
}

.send-btn {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .messages-area {
    padding: 16px;
  }

  .bubble-wrap {
    max-width: 80%;
  }
}
</style>
