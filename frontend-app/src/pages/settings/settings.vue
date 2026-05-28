<template>
  <view class="settings-page">
    <view class="settings-card">
      <view class="settings-item" @click="clearCache">
        <view class="settings-left">
          <text class="settings-icon">🗑️</text>
          <text class="settings-text">清除缓存</text>
        </view>
        <text class="settings-value">{{ cacheSize }}</text>
      </view>
      <view class="settings-item" @click="showAbout">
        <view class="settings-left">
          <text class="settings-icon">ℹ️</text>
          <text class="settings-text">关于我们</text>
        </view>
        <text class="settings-arrow">›</text>
      </view>
      <view class="settings-item">
        <view class="settings-left">
          <text class="settings-icon">📱</text>
          <text class="settings-text">当前版本</text>
        </view>
        <text class="settings-value">v1.0.0</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const cacheSize = ref('计算中...')

onMounted(() => {
  uni.getStorageInfo({
    success: (res) => {
      cacheSize.value = (res.currentSize / 1024).toFixed(1) + ' MB'
    },
    fail: () => {
      cacheSize.value = '未知'
    }
  })
})

const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '清除缓存将清空本地数据（不影响登录状态），确定继续？',
    success: ({ confirm }) => {
      if (!confirm) return
      const token = uni.getStorageSync('user_token')
      const userInfo = uni.getStorageSync('user_info')
      uni.clearStorageSync()
      if (token) uni.setStorageSync('user_token', token)
      if (userInfo) uni.setStorageSync('user_info', userInfo)
      cacheSize.value = '0.0 MB'
      uni.showToast({ title: '缓存已清除', icon: 'success' })
    }
  })
}

const showAbout = () => {
  uni.showModal({
    title: '校园二手交易平台',
    content: '一个面向高校学生的二手闲置交易平台，支持商品发布、浏览、收藏、即时聊天和面交定位。',
    showCancel: false,
    confirmText: '知道了'
  })
}
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.settings-page {
  padding: 24rpx;
}

.settings-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.settings-left {
  display: flex;
  align-items: center;
}

.settings-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.settings-text {
  font-size: 28rpx;
  color: #1A1A1A;
}

.settings-value {
  font-size: 26rpx;
  color: #999;
}

.settings-arrow {
  font-size: 28rpx;
  color: #ccc;
}
</style>
