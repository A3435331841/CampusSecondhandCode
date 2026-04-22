<template>
  <view class="login-page">
    <!-- 顶部波浪背景 -->
    <view class="header-bg">
      <view class="logo-area">
        <text class="logo-emoji">🏫</text>
        <text class="logo-title">校园二手交易</text>
        <text class="logo-sub">Campus Secondhand</text>
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="form-card">
      <view class="form-title">欢迎登录</view>

      <view class="input-group">
        <view class="input-item">
          <text class="input-label">账号</text>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入账号"
            class="input-field"
          />
        </view>
        <view class="divider-line"></view>
        <view class="input-item">
          <text class="input-label">密码</text>
          <input
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            placeholder="请输入密码"
            class="input-field"
          />
          <text class="pwd-toggle" @click="showPwd = !showPwd">
            {{ showPwd ? '🙈' : '👁️' }}
          </text>
        </view>
      </view>

      <view class="login-btn" @click="handleLogin" :class="{ loading: loading }">
        <text v-if="!loading">登 录</text>
        <text v-else>登录中...</text>
      </view>

      <view class="hint-text">
        <text>测试账号：user001 / 123456</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showPwd = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!form.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const data = await proxy.$request({
      url: '/auth/login',
      method: 'POST',
      data: { username: form.username, password: form.password }
    })

    // 保存登录信息到本地
    uni.setStorageSync('user_token', data.token)
    uni.setStorageSync('user_info', JSON.stringify({
      userId: data.userId,
      nickname: data.nickname,
      avatar: data.avatar
    }))

    uni.showToast({ title: `欢迎，${data.nickname}！`, icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1000)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
page {
  background-color: #1890FF;
}

.login-page {
  min-height: 100vh;
  background-color: #F3F4F6;
}

.header-bg {
  background: linear-gradient(135deg, #1890FF 0%, #36cfc9 100%);
  padding: 60px 0 80px;
  display: flex;
  justify-content: center;

  .logo-area {
    display: flex;
    flex-direction: column;
    align-items: center;

    .logo-emoji {
      font-size: 56px;
      margin-bottom: 12px;
    }

    .logo-title {
      font-size: 24px;
      font-weight: bold;
      color: #fff;
      margin-bottom: 4px;
    }

    .logo-sub {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.form-card {
  background: #fff;
  border-radius: 24px 24px 0 0;
  margin-top: -30px;
  padding: 30px 24px 40px;
  min-height: 400px;

  .form-title {
    font-size: 20px;
    font-weight: bold;
    color: #333;
    margin-bottom: 24px;
  }

  .input-group {
    background: #F7F8FA;
    border-radius: 12px;
    padding: 0 16px;
    margin-bottom: 24px;

    .input-item {
      display: flex;
      align-items: center;
      height: 54px;

      .input-label {
        font-size: 14px;
        color: #555;
        width: 44px;
        flex-shrink: 0;
      }

      .input-field {
        flex: 1;
        font-size: 15px;
        color: #333;
        background: transparent;
        border: none;
        outline: none;
        height: 100%;
      }

      .pwd-toggle {
        font-size: 18px;
        padding: 0 4px;
      }
    }

    .divider-line {
      height: 1px;
      background: #EBEBEB;
    }
  }

  .login-btn {
    height: 50px;
    background: linear-gradient(90deg, #1890FF, #36cfc9);
    border-radius: 25px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 17px;
    font-weight: bold;
    letter-spacing: 2px;
    box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);

    &.loading {
      opacity: 0.7;
    }
  }

  .hint-text {
    margin-top: 20px;
    text-align: center;
    font-size: 12px;
    color: #bbb;
  }
}
</style>
