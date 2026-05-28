<template>
  <view class="msg-page">
    <!-- Tab Switcher -->
    <view class="msg-tabs">
      <view
        class="msg-tab"
        :class="{ active: activeTab === 'chat' }"
        @click="activeTab = 'chat'"
      >
        <text class="msg-tab-text">聊天</text>
        <view v-if="activeTab === 'chat'" class="tab-line"></view>
      </view>
      <view
        class="msg-tab"
        :class="{ active: activeTab === 'notice' }"
        @click="activeTab = 'notice'"
      >
        <text class="msg-tab-text">通知</text>
        <view v-if="activeTab === 'notice'" class="tab-line"></view>
      </view>
    </view>

    <!-- Chat Sessions -->
    <view class="chat-list" v-if="activeTab === 'chat'">
      <view v-if="loading" class="loading-wrap">
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="sessions.length === 0" class="empty-wrap">
        <text class="empty-icon">💬</text>
        <text class="empty-title">暂无聊天记录</text>
        <text class="empty-sub">浏览商品并联系卖家开始聊天吧</text>
        <view class="empty-btn" @click="goHome">
          <text class="empty-btn-text">去逛逛</text>
        </view>
      </view>

      <view v-else class="session-list">
        <view
          class="session-item"
          v-for="(item, idx) in sessions"
          :key="item.sessionKey"
          @click="goChat(idx)"
        >
          <view class="session-avatar-wrap">
            <image
              class="session-avatar"
              :src="item.peerAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.peerUserId}`"
              mode="aspectFill"
            ></image>
          </view>
          <view class="session-content">
            <view class="session-top">
              <text class="session-name">{{ item.peerNickname || ('用户' + item.peerUserId) }}</text>
              <text class="session-time">{{ formatTime(item.lastMessageTime) }}</text>
            </view>
            <view class="session-bottom">
              <text class="session-msg">{{ item.lastMessage || '暂无消息' }}</text>
              <text class="session-product" v-if="getProductName(item.productId)">{{ getProductName(item.productId) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Notifications -->
    <view class="notice-list" v-if="activeTab === 'notice'">
      <view v-if="notices.length === 0" class="empty-wrap">
        <text class="empty-icon">🔔</text>
        <text class="empty-title">暂无通知</text>
        <text class="empty-sub">订单状态变更、评论回复等消息将在这里显示</text>
      </view>

      <view v-else class="notice-items">
        <view
          class="notice-item"
          v-for="item in notices"
          :key="item.id"
          @click="handleNotice(item)"
        >
          <view class="notice-icon-wrap" :class="item.type">
            <text class="notice-type-icon">{{ noticeIcon(item.type) }}</text>
          </view>
          <view class="notice-content">
            <text class="notice-title">{{ item.title }}</text>
            <text class="notice-desc">{{ item.content }}</text>
            <text class="notice-time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
      </view>
    </view>

    <custom-tabbar :current="3" />
  </view>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import customTabbar from '@/components/custom-tabbar.vue'

const { proxy } = getCurrentInstance()
const activeTab = ref('chat')
const loading = ref(false)
const sessions = ref([])
const notices = ref([])
const productNames = ref({})

const loadSessions = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    sessions.value = []
    return
  }
  loading.value = true
  try {
    const res = await proxy.$request({
      url: '/chat/sessions',
      method: 'GET'
    })
    sessions.value = res?.records || res || []
    const productIds = [...new Set(sessions.value.map(s => s.productId).filter(Boolean))]
    const map = {}
    await Promise.all(productIds.map(async (pid) => {
      try {
        const p = await proxy.$request({ url: `/product/detail/${pid}`, method: 'GET' })
        if (p) map[pid] = p.title
      } catch {}
    }))
    productNames.value = map
  } catch {
    sessions.value = []
  } finally {
    loading.value = false
  }
}

const getProductName = (pid) => productNames.value[pid] || ''

onShow(() => {
  loadSessions()
})

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${hh}:${mm}`
  const diff = now - d
  if (diff < 7 * 24 * 3600 * 1000) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[d.getDay()]
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const noticeIcon = (type) => {
  const map = { order: '📦', comment: '💬', system: '📢' }
  return map[type] || '🔔'
}

const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

const goChat = (idx) => {
  const item = sessions.value[idx]
  if (!item) return
  uni.navigateTo({
    url: `/pages/chat/chat?toUserId=${item.peerUserId}&productId=${item.productId || ''}&nickname=${encodeURIComponent(item.peerNickname || '')}`
  })
}

const handleNotice = (item) => {
  if (item.type === 'order' && item.targetId) {
    uni.navigateTo({ url: `/pages/my-buy/my-buy` })
  } else if (item.type === 'comment' && item.targetId) {
    uni.navigateTo({ url: `/pages/detail/detail?id=${item.targetId}` })
  }
}
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.msg-page {
  min-height: 100vh;
}

/* Tabs */
.msg-tabs {
  display: flex;
  background: #fff;
  height: 96rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 50;
}

.msg-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.msg-tab-text {
  font-size: 30rpx;
  color: #999;
  font-weight: 500;
  transition: color 0.2s;
}

.msg-tab.active .msg-tab-text {
  color: #1A1A1A;
  font-weight: 700;
}

.tab-line {
  position: absolute;
  bottom: 0;
  width: 60rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: linear-gradient(135deg, #00B578, #00D68F);
}

/* Loading */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* Empty State */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-bottom: 40rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #00B578, #00D68F);
  border-radius: 40rpx;
  padding: 20rpx 60rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 181, 120, 0.25);
}

.empty-btn-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}

/* Session List */
.session-list {
  padding: 16rpx 0;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  margin-bottom: 2rpx;
}

.session-avatar-wrap {
  position: relative;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.session-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.unread-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 36rpx;
  height: 36rpx;
  border-radius: 18rpx;
  background: #ff4d4f;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  border: 4rpx solid #fff;
}

.unread-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.session-name {
  font-size: 30rpx;
  color: #1A1A1A;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 24rpx;
  color: #ccc;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.session-bottom {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.session-msg {
  font-size: 26rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.session-product {
  font-size: 22rpx;
  color: #00B578;
  background: rgba(0, 181, 120, 0.08);
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
  flex-shrink: 0;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Notice Items */
.notice-items {
  padding: 16rpx 0;
}

.notice-item {
  display: flex;
  padding: 28rpx 32rpx;
  background: #fff;
  margin-bottom: 2rpx;
}

.notice-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
  background: #f0f5ff;
}

.notice-icon-wrap.order {
  background: #fff7e6;
}

.notice-icon-wrap.comment {
  background: #e6fffb;
}

.notice-icon-wrap.system {
  background: #f0f5ff;
}

.notice-type-icon {
  font-size: 40rpx;
}

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-title {
  display: block;
  font-size: 28rpx;
  color: #1A1A1A;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.notice-desc {
  display: block;
  font-size: 26rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8rpx;
}

.notice-time {
  font-size: 22rpx;
  color: #ccc;
}
</style>
