<template>
  <view class="profile-container">
    <!-- 用户信息头部 -->
    <view class="user-header" @click="handleAvatarClick">
      <image class="avatar" :src="userInfo.avatar || defaultAvatar"></image>
      <view class="user-info">
        <text class="nickname">{{ userInfo.nickname || '未登录' }}</text>
        <view class="tags" v-if="isLoggedIn">
          <u-tag text="已登录" type="success" size="mini" plain plainFill></u-tag>
        </view>
        <view class="tags" v-else>
          <text class="login-tip">点击登录账号 →</text>
        </view>
      </view>
      <u-icon name="arrow-right" color="#999" size="18"></u-icon>
    </view>

    <!-- 订单状态区 -->
    <view class="order-section">
      <view class="section-title">
        <text>我的交易</text>
        <view class="more">
          <text>全部</text>
          <u-icon name="arrow-right" size="12"></u-icon>
        </view>
      </view>
      <view class="status-grid">
        <view class="status-item" @click="goMyPublish">
          <u-icon name="bag" size="28" color="#1890FF"></u-icon>
          <text class="text">我发布的</text>
        </view>
        <view class="status-item">
          <u-icon name="shopping-cart" size="28" color="#52c41a"></u-icon>
          <text class="text">我买到的</text>
        </view>
        <view class="status-item">
          <u-icon name="rmb-circle" size="28" color="#fa8c16"></u-icon>
          <text class="text">我卖出的</text>
        </view>
        <view class="status-item">
          <u-icon name="star" size="28" color="#eb2f96"></u-icon>
          <text class="text">我的收藏</text>
        </view>
      </view>
    </view>

    <!-- 服务列表区 -->
    <view class="service-list">
      <u-cell-group :border="false">
        <u-cell icon="map" title="地址管理" isLink></u-cell>
        <u-cell icon="server-man" title="联系客服" isLink></u-cell>
        <u-cell icon="setting" title="系统设置" isLink></u-cell>
      </u-cell-group>
    </view>

    <!-- 退出登录/去登录 按钮 -->
    <view class="logout-btn" v-if="isLoggedIn">
      <u-button type="error" text="退出登录" plain shape="circle" @click="handleLogout"></u-button>
    </view>
    <view class="logout-btn" v-else>
      <u-button type="primary" text="立即登录" shape="circle" @click="goLogin"></u-button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const { proxy } = getCurrentInstance()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const userInfo = ref({})
const isLoggedIn = computed(() => !!uni.getStorageSync('user_token'))

// 每次页面显示时刷新用户信息
onShow(() => {
  try {
    const info = uni.getStorageSync('user_info')
    userInfo.value = info ? JSON.parse(info) : {}
  } catch {
    userInfo.value = {}
  }
})

const handleAvatarClick = () => {
  if (!isLoggedIn.value) goLogin()
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

const goMyPublish = () => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/my-publish/my-publish' })
}

const handleLogout = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await proxy.$request({ url: '/auth/logout', method: 'POST' })
        } catch {}
        uni.removeStorageSync('user_token')
        uni.removeStorageSync('user_info')
        userInfo.value = {}
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss">
page { background-color: #F3F4F6; }

.profile-container { padding: 15px; }

.user-header {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 20px 15px;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);

  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    margin-right: 15px;
    background-color: #f5f5f5;
  }

  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .nickname {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }

    .login-tip {
      font-size: 13px;
      color: #1890FF;
    }
  }
}

.order-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 15px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
    padding-bottom: 10px;
    border-bottom: 1px solid #f5f5f5;

    .more {
      display: flex;
      align-items: center;
      font-size: 12px;
      font-weight: normal;
      color: #999;
    }
  }

  .status-grid {
    display: flex;
    justify-content: space-around;
    padding: 5px 0;

    .status-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .text {
        font-size: 12px;
        color: #666;
        margin-top: 8px;
      }
    }
  }
}

.service-list {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.logout-btn { padding: 0 20px; }
</style>
