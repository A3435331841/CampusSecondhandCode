<template>
  <div class="login-page">
    <!-- 背景装饰圆 -->
    <div class="bg-circle bg-circle-1"></div>
    <div class="bg-circle bg-circle-2"></div>
    <div class="bg-circle bg-circle-3"></div>

    <div class="login-card">
      <div class="card-logo">
        <span class="logo-emoji">🏫</span>
        <h1 class="logo-title">校园二手交易</h1>
        <p class="logo-sub">校园闲置流转平台</p>
      </div>

      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item class="form-item">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
            autocomplete="username"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item class="form-item">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            autocomplete="current-password"
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="divider">
        <span>或者</span>
      </div>

      <button class="guest-btn" @click="handleGuestEntry">
        <span class="guest-icon">👀</span>
        游客浏览（只读）
      </button>

      <p class="guest-tip">游客可浏览商品和查看详情，购买 / 收藏 / 留言需登录</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(form.username.trim(), form.password.trim())
    user.setUser(data)
    ElMessage.success(`欢迎，${data.nickname}`)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch {
    // error shown by request interceptor
  } finally {
    loading.value = false
  }
}

const handleGuestEntry = () => {
  user.setGuest()
  router.push('/')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f60d4 0%, #1890ff 40%, #36cfc9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.07);
  pointer-events: none;
}

.bg-circle-1 {
  width: 500px;
  height: 500px;
  top: -180px;
  right: -100px;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
  background: rgba(255, 255, 255, 0.05);
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  bottom: 120px;
  right: 10%;
  background: rgba(255, 255, 255, 0.04);
}

/* 卡片入场动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(36px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-card {
  background: #fff;
  border-radius: 28px;
  padding: 52px 44px 44px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.18);
  animation: slideUp 0.52s cubic-bezier(0.16, 1, 0.3, 1) both;
  position: relative;
  z-index: 1;
}

.card-logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo-emoji {
  font-size: 52px;
  display: block;
  margin-bottom: 14px;
  animation: slideUp 0.52s cubic-bezier(0.16, 1, 0.3, 1) 0.08s both;
}

.logo-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.logo-sub {
  font-size: 14px;
  color: #aaa;
}

.form-item {
  margin-bottom: 16px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 14px;
  padding: 4px 16px;
  transition: box-shadow var(--transition-base);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary) inset, 0 0 0 4px rgba(24, 144, 255, 0.10) !important;
}

.login-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #FF6600, #FF8533);
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  box-shadow: 0 8px 24px rgba(255, 102, 0, 0.35);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.login-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #e55a00, #f07a2e);
  border: none;
  transform: translateY(-1px);
  box-shadow: 0 12px 32px rgba(255, 102, 0, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 28px 0;
  color: #ccc;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #f0f0f0;
}

.guest-btn {
  width: 100%;
  height: 48px;
  background: #F7F8FA;
  border: 1.5px solid #e8e8e8;
  border-radius: 14px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background var(--transition-fast), border-color var(--transition-fast), transform var(--transition-fast);
}

.guest-btn:hover {
  background: #eef2ff;
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-1px);
}

.guest-btn:active {
  transform: scale(0.98);
}

.guest-icon {
  font-size: 18px;
}

.guest-tip {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: #bbb;
  line-height: 1.6;
}
</style>
