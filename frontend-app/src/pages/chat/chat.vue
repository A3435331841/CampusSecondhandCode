<template>
  <view class="chat-page">
    <!-- 商品信息卡片 -->
    <view class="product-card" v-if="product" @click="goProductDetail">
      <image class="product-card-img" :src="product.cover" mode="aspectFill"></image>
      <view class="product-card-info">
        <text class="product-card-title">{{ product.title }}</text>
        <text class="product-card-price">¥{{ product.price }}</text>
      </view>
      <view class="product-card-arrow">
        <text class="arrow-icon">›</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="msg-list" scroll-y :scroll-into-view="scrollAnchor" scroll-with-animation>
      <view class="msg-padding"></view>
      <view v-for="(item, index) in messages" :key="index" :id="`msg-${index}`">
        <!-- 时间分割线 -->
        <view class="time-divider" v-if="showTimeDivider(index)">
          <text class="time-divider-text">{{ formatTimeFull(item.timestamp) }}</text>
        </view>
        <!-- 消息气泡 -->
        <view class="msg-row" :class="{ self: item.self }">
          <image
            v-if="!item.self"
            class="avatar"
            :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${toUserId}`"
            mode="aspectFill"
          ></image>
          <view class="bubble-wrap">
            <view class="bubble" :class="{ 'bubble-self': item.self }">
              <text class="bubble-text">{{ item.content }}</text>
            </view>
          </view>
          <image
            v-if="item.self"
            class="avatar"
            :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUserId}`"
            mode="aspectFill"
          ></image>
        </view>
      </view>
      <view class="msg-padding-bottom"></view>
    </scroll-view>

    <!-- 连接提示 -->
    <view class="connect-tip" v-if="!connected && messages.length > 0">
      <text class="connect-tip-text">连接中...</text>
    </view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <view class="input-wrap">
        <input
          v-model="inputText"
          class="input"
          type="text"
          placeholder="输入消息..."
          placeholder-class="input-placeholder"
          confirm-type="send"
          @confirm="sendMessage"
        />
      </view>
      <view class="send-btn" :class="{ disabled: !inputText.trim() }" @click="sendMessage">
        <text class="send-icon">➤</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, getCurrentInstance } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { API_ORIGIN, normalizeImage } from '@/utils/config'

const API_WS = API_ORIGIN.replace(/^http/, 'ws') + '/ws/chat'
const { proxy } = getCurrentInstance()
const messages = ref([])
const inputText = ref('')
const scrollAnchor = ref('')
const connected = ref(false)
const toUserId = ref('')
const productId = ref('')
const product = ref(null)
const currentUserId = ref('')

const loadProduct = async () => {
  if (!productId.value) return
  try {
    const res = await proxy.$request({
      url: `/product/detail/${productId.value}`,
      method: 'GET'
    })
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
  try {
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
    scrollToBottom()
  } catch {}
}

const scrollToBottom = async () => {
  await nextTick()
  if (messages.value.length > 0) {
    scrollAnchor.value = ''
    await nextTick()
    scrollAnchor.value = `msg-${messages.value.length - 1}`
  }
}

const parseUserId = () => {
  try {
    const raw = uni.getStorageSync('user_info')
    currentUserId.value = raw ? JSON.parse(raw).userId : ''
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

  uni.connectSocket({
    url: `${API_WS}?token=${token}`,
    success: () => {},
    fail: () => {
      connected.value = false
      uni.showToast({ title: '聊天连接失败', icon: 'none' })
    }
  })

  uni.onSocketOpen(() => {
    connected.value = true
  })

  uni.onSocketClose(() => {
    connected.value = false
  })

  uni.onSocketError(() => {
    connected.value = false
  })

  uni.onSocketMessage(async (res) => {
    try {
      const msg = JSON.parse(res.data || '{}')
      if (!msg.content) return
      if (msg.productId && String(msg.productId) !== String(productId.value)) return
      if (msg.type === 'system' || msg.type === 'ack') return
      messages.value.push({
        ...msg,
        self: String(msg.fromUserId) === String(currentUserId.value)
      })
      scrollToBottom()
    } catch {}
  })
}

const sendMessage = () => {
  const text = inputText.value.trim()
  if (!text) return
  if (!connected.value) {
    uni.showToast({ title: '正在连接，请稍后重试', icon: 'none' })
    return
  }
  uni.sendSocketMessage({
    data: JSON.stringify({
      toUserId: Number(toUserId.value),
      productId: Number(productId.value),
      content: text,
      type: 'text'
    }),
    fail: () => {
      uni.showToast({ title: '发送失败', icon: 'none' })
    }
  })
  inputText.value = ''
}

const showTimeDivider = (index) => {
  if (index === 0) return true
  const prev = messages.value[index - 1]
  const curr = messages.value[index]
  if (!prev.timestamp || !curr.timestamp) return false
  return curr.timestamp - prev.timestamp > 5 * 60 * 1000
}

const formatTimeFull = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${hh}:${mm}`
  const MM = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${MM}-${dd} ${hh}:${mm}`
}

const goProductDetail = () => {
  if (productId.value) {
    uni.navigateTo({ url: `/pages/detail/detail?id=${productId.value}` })
  }
}

onLoad((options) => {
  toUserId.value = options.toUserId || ''
  productId.value = options.productId || ''
  const nickname = options.nickname ? decodeURIComponent(options.nickname) : ''
  uni.setNavigationBarTitle({ title: nickname || `用户${toUserId.value}` })
  parseUserId()
  loadProduct()
  loadHistory()
  connectSocket()
})

onUnload(() => {
  try { uni.closeSocket() } catch {}
  if (typeof uni.offSocketOpen === 'function') uni.offSocketOpen()
  if (typeof uni.offSocketClose === 'function') uni.offSocketClose()
  if (typeof uni.offSocketError === 'function') uni.offSocketError()
  if (typeof uni.offSocketMessage === 'function') uni.offSocketMessage()
})
</script>

<style lang="scss">
page {
  background-color: #F0F2F5;
}

.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 商品信息卡片 */
.product-card {
  display: flex;
  align-items: center;
  padding: 20rpx 28rpx;
  background: #fff;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.product-card-img {
  width: 88rpx;
  height: 88rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
  flex-shrink: 0;
  margin-right: 20rpx;
}

.product-card-info {
  flex: 1;
  min-width: 0;
}

.product-card-title {
  display: block;
  font-size: 28rpx;
  color: #1A1A1A;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6rpx;
}

.product-card-price {
  display: block;
  font-size: 28rpx;
  color: #FF6633;
  font-weight: 600;
}

.product-card-arrow {
  flex-shrink: 0;
  margin-left: 12rpx;
}

.arrow-icon {
  font-size: 36rpx;
  color: #ccc;
}

/* 消息列表 */
.msg-list {
  flex: 1;
  padding: 0 24rpx;
  box-sizing: border-box;
}

.msg-padding { height: 24rpx; }
.msg-padding-bottom { height: 24rpx; }

/* 时间分割线 */
.time-divider {
  display: flex;
  justify-content: center;
  padding: 24rpx 0 16rpx;
}

.time-divider-text {
  font-size: 22rpx;
  color: #999;
  background: rgba(0, 0, 0, 0.06);
  padding: 4rpx 20rpx;
  border-radius: 16rpx;
}

/* 消息行 */
.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 28rpx;

  &.self {
    flex-direction: row-reverse;

    .bubble {
      background: linear-gradient(135deg, #1890ff, #40a9ff);
      border-radius: 32rpx 8rpx 32rpx 32rpx;
    }

    .bubble-text {
      color: #fff;
    }
  }
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
}

.bubble-wrap {
  max-width: 65%;
  margin: 0 16rpx;
}

.bubble {
  padding: 22rpx 28rpx;
  border-radius: 8rpx 32rpx 32rpx 32rpx;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.bubble-text {
  font-size: 30rpx;
  line-height: 1.6;
  color: #333;
  word-break: break-all;
}

/* 连接提示 */
.connect-tip {
  display: flex;
  justify-content: center;
  padding: 8rpx 0;
}

.connect-tip-text {
  font-size: 22rpx;
  color: #faad14;
  background: #fffbe6;
  padding: 6rpx 24rpx;
  border-radius: 20rpx;
}

/* 输入栏 */
.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.input-wrap {
  flex: 1;
  background: #F5F6F8;
  border-radius: 40rpx;
  padding: 0 4rpx;
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
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(24, 144, 255, 0.35);
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
