<template>
  <view class="chat-page">
    <scroll-view class="msg-list" scroll-y :scroll-into-view="scrollIntoView">
      <view
        class="msg-item"
        v-for="(item, index) in messages"
        :key="index"
        :id="`msg-${index}`"
        :class="{ self: item.self }"
      >
        <view class="bubble">{{ item.content }}</view>
        <text class="time">{{ formatTime(item.timestamp) }}</text>
      </view>
    </scroll-view>

    <view class="input-bar">
      <input
        v-model="inputText"
        class="input"
        type="text"
        placeholder="输入消息..."
        confirm-type="send"
        @confirm="sendMessage"
      />
      <view class="send-btn" @click="sendMessage">发送</view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, getCurrentInstance } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'

const API_WS = 'ws://localhost:8080/ws/chat'
const { proxy } = getCurrentInstance()
const messages = ref([])
const inputText = ref('')
const scrollIntoView = ref('')
const connected = ref(false)
const toUserId = ref('')
const productId = ref('')
const currentUserId = ref('')
let socketTask = null

const loadHistory = async () => {
  const res = await proxy.$request({
    url: '/chat/history',
    method: 'GET',
    data: {
      toUserId: Number(toUserId.value),
      productId: Number(productId.value),
      current: 1,
      size: 100
    }
  })
  messages.value = (res?.records || []).map((msg) => ({
    ...msg,
    self: String(msg.fromUserId) === String(currentUserId.value)
  }))
  await scrollBottom()
}

const scrollBottom = async () => {
  await nextTick()
  if (messages.value.length > 0) {
    scrollIntoView.value = `msg-${messages.value.length - 1}`
  }
}

const parseUserId = () => {
  try {
    const userInfo = uni.getStorageSync('user_info')
    currentUserId.value = userInfo ? JSON.parse(userInfo).userId : ''
  } catch {
    currentUserId.value = ''
  }
}

const connectSocket = () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  socketTask = uni.connectSocket({ url: `${API_WS}?token=${token}` })
  socketTask.onOpen(() => {
    connected.value = true
  })
  socketTask.onClose(() => {
    connected.value = false
  })
  socketTask.onError(() => {
    connected.value = false
    uni.showToast({ title: '聊天连接失败', icon: 'none' })
  })
  socketTask.onMessage(async (res) => {
    try {
      const msg = JSON.parse(res.data || '{}')
      if (!msg.content) return
      if (msg.productId && String(msg.productId) !== String(productId.value)) return
      if (msg.type === 'system') return
      messages.value.push({
        ...msg,
        self: String(msg.fromUserId) === String(currentUserId.value)
      })
      await scrollBottom()
    } catch {}
  })
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text) return
  if (!connected.value || !socketTask) {
    uni.showToast({ title: '连接未就绪，请稍后', icon: 'none' })
    return
  }
  socketTask.send({
    data: JSON.stringify({
      toUserId: Number(toUserId.value),
      productId: Number(productId.value),
      content: text,
      type: 'text'
    })
  })
  inputText.value = ''
}

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

onLoad((options) => {
  toUserId.value = options.toUserId || ''
  productId.value = options.productId || ''
  const title = options.title ? decodeURIComponent(options.title) : '聊天'
  uni.setNavigationBarTitle({ title })
  parseUserId()
  loadHistory()
  connectSocket()
})

onUnload(() => {
  if (socketTask) {
    socketTask.close({})
    socketTask = null
  }
})
</script>

<style lang="scss">
page {
  background-color: #f3f4f6;
}

.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.msg-list {
  flex: 1;
  padding: 12px;
  box-sizing: border-box;
}

.msg-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 12px;

  &.self {
    align-items: flex-end;
    .bubble {
      background: #1890ff;
      color: #fff;
    }
  }

  .bubble {
    max-width: 70%;
    padding: 8px 12px;
    border-radius: 8px;
    background: #fff;
    color: #333;
    font-size: 14px;
    line-height: 1.5;
    word-break: break-all;
  }

  .time {
    font-size: 11px;
    color: #999;
    margin-top: 4px;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 8px 10px calc(8px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid #eee;

  .input {
    flex: 1;
    height: 36px;
    background: #f5f5f5;
    border-radius: 18px;
    padding: 0 12px;
    font-size: 14px;
  }

  .send-btn {
    margin-left: 8px;
    width: 56px;
    height: 36px;
    border-radius: 18px;
    background: #1890ff;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
  }
}
</style>
