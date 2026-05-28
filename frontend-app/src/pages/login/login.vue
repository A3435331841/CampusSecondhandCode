<template>
  <view class="login-page">
    <!-- 顶部绿色渐变区域 -->
    <view class="header-bg">
      <view class="logo-area">
        <view class="logo-circle">
          <text class="logo-icon">🎓</text>
        </view>
        <text class="logo-title">校园二手</text>
        <text class="logo-sub">让闲置流转起来</text>
      </view>
    </view>

    <!-- 登录卡片 -->
    <view class="form-card">
      <!-- 登录方式切换 -->
      <view class="tab-row">
        <view class="tab-item" :class="{ active: loginMode === 'wechat' }" @click="loginMode = 'wechat'">
          <text>微信登录</text>
        </view>
        <view class="tab-item" :class="{ active: loginMode === 'account' }" @click="loginMode = 'account'">
          <text>账号登录</text>
        </view>
      </view>

      <!-- 微信登录 -->
      <view v-if="loginMode === 'wechat'">
        <view class="login-copy">
          <text class="copy-title">使用微信小程序授权登录</text>
          <text class="copy-desc">
            授权后将自动获取平台账号、头像、昵称和实名状态。
          </text>
        </view>

        <view class="login-btn" :class="{ loading: loading }" @click="handleWechatLogin">
          <text v-if="!loading">微信一键登录</text>
          <text v-else>登录中...</text>
        </view>
      </view>

      <!-- 账号密码登录 -->
      <view v-else>
        <view class="input-group">
          <input class="login-input" v-model="username" placeholder="请输入用户名" placeholder-class="placeholder" />
        </view>
        <view class="input-group">
          <input class="login-input" v-model="password" type="password" placeholder="请输入密码" placeholder-class="placeholder" />
        </view>

        <view class="login-btn" :class="{ loading: loading }" @click="handleAccountLogin">
          <text v-if="!loading">登录</text>
          <text v-else>登录中...</text>
        </view>

        <view class="hint-text">
          <text>测试账号：user001 / 123456</text>
        </view>
      </view>

      <view class="agreement-text">
        <text>登录即表示同意</text>
        <text class="agreement-link">《用户服务协议》</text>
        <text>和</text>
        <text class="agreement-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const loginMode = ref('account')
const username = ref('')
const password = ref('')

const saveLoginData = (data) => {
  uni.setStorageSync('user_token', data.token)
  uni.setStorageSync('user_info', JSON.stringify({
    userId: data.userId,
    nickname: data.nickname,
    avatar: data.avatar,
    role: data.role,
    verifyStatus: data.verifyStatus
  }))
  uni.showToast({ title: `欢迎，${data.nickname}`, icon: 'success' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' })
  }, 1000)
}

const handleAccountLogin = async () => {
  if (loading.value) return
  if (!username.value.trim() || !password.value.trim()) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await proxy.$request({
      url: '/auth/login',
      method: 'POST',
      data: { username: username.value.trim(), password: password.value.trim() }
    })
    saveLoginData(data)
  } catch (error) {
    // error toast shown by request interceptor
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    if (!loginRes.code) throw new Error('未获取到微信登录凭证，请重新登录')

    const data = await proxy.$request({
      url: '/auth/wechat/login',
      method: 'POST',
      data: { code: loginRes.code }
    })
    saveLoginData(data)
  } catch (error) {
    const message = error && error.message ? error.message : '微信登录失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
page {
  background-color: #00B578;
}

.login-page {
  min-height: 100vh;
  background-color: var(--bg, #F7F8FA);
}

.header-bg {
  background: linear-gradient(180deg, #00B578 0%, #00D68F 100%);
  height: 400rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0 0 40rpx 40rpx;
  padding-top: env(safe-area-inset-top);

  .logo-area {
    display: flex;
    flex-direction: column;
    align-items: center;

    .logo-circle {
      width: 80rpx;
      height: 80rpx;
      background: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24rpx;
    }

    .logo-icon {
      font-size: 44rpx;
    }

    .logo-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
      margin-bottom: 8rpx;
    }

    .logo-sub {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

.form-card {
  background: var(--card-bg, #fff);
  border-radius: 24rpx 24rpx 0 0;
  margin-top: -40rpx;
  padding: 48rpx;
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
  min-height: 700rpx;
  position: relative;
  z-index: 10;

  .tab-row {
    display: flex;
    margin-bottom: 48rpx;
    border-bottom: 2rpx solid #f0f0f0;
  }

  .tab-item {
    flex: 1;
    text-align: center;
    padding-bottom: 20rpx;
    font-size: 32rpx;
    color: var(--text-hint, #999);
    font-weight: 500;
    position: relative;

    &.active {
      color: var(--text-primary, #1A1A1A);
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: -2rpx;
        left: 30%;
        width: 40%;
        height: 4rpx;
        background: #00B578;
        border-radius: 2rpx;
      }
    }
  }

  .input-group {
    margin-bottom: 28rpx;
  }

  .login-input {
    width: 100%;
    height: 96rpx;
    background: var(--bg, #F7F8FA);
    border-radius: 48rpx;
    padding: 0 32rpx;
    font-size: 30rpx;
    color: var(--text-primary, #1A1A1A);
    box-sizing: border-box;
  }

  .placeholder {
    color: #ccc;
  }

  .login-copy {
    background: var(--bg, #F7F8FA);
    border-radius: 24rpx;
    padding: 36rpx 32rpx;
    margin-bottom: 48rpx;

    .copy-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: var(--text-primary, #1A1A1A);
    }

    .copy-desc {
      display: block;
      margin-top: 16rpx;
      font-size: 26rpx;
      line-height: 1.6;
      color: var(--text-secondary, #666);
    }
  }

  .login-btn {
    height: 96rpx;
    background: linear-gradient(135deg, #00B578, #00D68F);
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    letter-spacing: 2rpx;
    box-shadow: 0 8rpx 30rpx rgba(0, 181, 120, 0.35);

    &.loading {
      opacity: 0.7;
    }
  }

  .hint-text {
    margin-top: 40rpx;
    text-align: center;
    font-size: 24rpx;
    line-height: 1.6;
    color: var(--text-hint, #999);
  }

  .agreement-text {
    margin-top: 48rpx;
    text-align: center;
    font-size: 24rpx;
    color: var(--text-hint, #999);
    line-height: 1.8;
  }

  .agreement-link {
    color: #00B578;
  }
}
</style>
