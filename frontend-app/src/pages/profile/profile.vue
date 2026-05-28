<template>
  <view class="profile-page">
    <!-- 顶部绿色渐变区域 -->
    <view class="profile-header">
      <view class="header-content" @click="handleAvatarClick">
        <image class="profile-avatar" :src="userInfo.avatar || defaultAvatar" mode="aspectFill"></image>
        <text class="profile-nickname">{{ userInfo.nickname || '未登录' }}</text>
        <view class="credit-badge" v-if="userInfo.verifyStatus === 'VERIFIED'">
          <text class="credit-text">已认证学生</text>
        </view>
        <view class="credit-badge unverified" v-else-if="isLoggedIn">
          <text class="credit-text">{{ userInfo.verifyStatus || '未认证' }}</text>
        </view>
        <text class="login-hint" v-if="!isLoggedIn">点击登录</text>
      </view>
    </view>

    <!-- 功能网格 -->
    <view class="func-grid-card">
      <view class="func-item" @click="goMyPublish">
        <text class="func-icon">📦</text>
        <text class="func-label">我发布的</text>
      </view>
      <view class="func-item" @click="goMyBuy">
        <text class="func-icon">🛒</text>
        <text class="func-label">我买到的</text>
      </view>
      <view class="func-item" @click="goMySell">
        <text class="func-icon">💰</text>
        <text class="func-label">我卖出的</text>
      </view>
      <view class="func-item" @click="toggleFavorite">
        <text class="func-icon">❤️</text>
        <text class="func-label">我的收藏</text>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-card">
      <view class="menu-item" @click="goVerify" v-if="isLoggedIn && userInfo.verifyStatus !== 'VERIFIED'">
        <view class="menu-left">
          <text class="menu-icon">🎓</text>
          <text class="menu-text">实名认证</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" v-if="userInfo.verifyStatus === 'VERIFIED'">
        <view class="menu-left">
          <text class="menu-icon">🎓</text>
          <text class="menu-text">实名认证</text>
        </view>
        <view class="verified-tag">
          <text class="verified-tag-text">已完成</text>
        </view>
      </view>
      <view class="menu-item" @click="goSettings">
        <view class="menu-left">
          <text class="menu-icon">⚙️</text>
          <text class="menu-text">设置</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="action-card" v-if="isLoggedIn">
      <view class="logout-action" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
    <view class="action-card" v-else>
      <view class="login-action" @click="goLogin">
        <text class="login-text">立即登录</text>
      </view>
    </view>

    <custom-tabbar :current="4" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import customTabbar from '@/components/custom-tabbar.vue'

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
const userInfo = ref({})
const isLoggedIn = computed(() => !!uni.getStorageSync('user_token'))

const refreshUserInfo = () => {
  try {
    const raw = uni.getStorageSync('user_info')
    userInfo.value = raw ? JSON.parse(raw) : {}
  } catch {
    userInfo.value = {}
  }
}

onShow(refreshUserInfo)

const handleAvatarClick = () => {
  if (!isLoggedIn.value) {
    goLogin()
  }
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

const goVerify = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/verify/verify' })
}

const goMyPublish = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-publish/my-publish' })
}

const goMyBuy = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-buy/my-buy' })
}

const goMySell = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-sell/my-sell' })
}

const goSettings = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/settings/settings' })
}

const toggleFavorite = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/favorites/index' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定退出当前账号？',
    success: async ({ confirm }) => {
      if (!confirm) return
      uni.removeStorageSync('user_token')
      uni.removeStorageSync('user_info')
      userInfo.value = {}
      uni.showToast({ title: '已退出登录', icon: 'none' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/login' })
      }, 500)
    }
  })
}
</script>

<style lang="scss">
page {
  background-color: var(--bg, #F7F8FA);
}

.profile-page {
  min-height: 100vh;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

/* 顶部绿色渐变 */
.profile-header {
  background: linear-gradient(180deg, #00B578 0%, #00D68F 100%);
  height: 280rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20rpx;
}

.profile-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 3rpx solid #fff;
  margin-bottom: 16rpx;
  background-color: #f5f5f5;
}

.profile-nickname {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.credit-badge {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 20rpx;
  padding: 6rpx 20rpx;
}

.credit-badge.unverified {
  background: rgba(255, 255, 255, 0.15);
}

.credit-text {
  font-size: 22rpx;
  color: #fff;
}

.login-hint {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 功能网格 */
.func-grid-card {
  background: var(--card-bg, #fff);
  margin: -40rpx 24rpx 0;
  border-radius: 16rpx;
  padding: 36rpx 0;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.func-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.func-icon {
  font-size: 40rpx;
  margin-bottom: 12rpx;
}

.func-label {
  font-size: 24rpx;
  color: var(--text-secondary, #666);
}

/* 菜单列表 */
.menu-card {
  background: var(--card-bg, #fff);
  margin: 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.menu-text {
  font-size: 28rpx;
  color: var(--text-primary, #1A1A1A);
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}

.verified-tag {
  background: rgba(0, 181, 120, 0.1);
  border-radius: 8rpx;
  padding: 4rpx 16rpx;
}

.verified-tag-text {
  font-size: 22rpx;
  color: #00B578;
}

/* 退出/登录按钮 */
.action-card {
  margin: 24rpx;
}

.logout-action {
  background: var(--card-bg, #fff);
  border-radius: 16rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.logout-text {
  font-size: 28rpx;
  color: #ff4d4f;
  font-weight: 500;
}

.login-action {
  background: linear-gradient(135deg, #00B578, #00D68F);
  border-radius: 16rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 181, 120, 0.3);
}

.login-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: bold;
}
</style>
