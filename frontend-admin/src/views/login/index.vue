<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-circle">
            <el-icon :size="32" color="#1890ff"><Shop /></el-icon>
          </div>
        </div>
        <h1 class="title">校园二手交易管理平台</h1>
        <p class="subtitle">Campus Secondhand Trading Admin</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="管理员账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="登录密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            size="large"
            @click="handleLogin"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>校园二手交易平台 &copy; 2025</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop } from '@element-plus/icons-vue'
import { login } from '../../api/auth.js'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await login({ username: loginForm.username, password: loginForm.password })
      if (data.role !== 'admin') {
        ElMessage.error('请使用管理员账号登录')
        return
      }
      // 存储 token 和用户信息
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_user', JSON.stringify({
        userId: data.userId,
        nickname: data.nickname,
        avatar: data.avatar,
        role: data.role
      }))
      ElMessage.success('登录成功，欢迎回来！')
      router.push('/')
    } catch (e) {
      // 错误已由 request.js 拦截器统一处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(24, 144, 255, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(24, 144, 255, 0.04) 0%, transparent 50%);

  .login-card {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      .logo-wrapper {
        margin-bottom: 16px;

        .logo-circle {
          width: 56px;
          height: 56px;
          margin: 0 auto;
          border-radius: 50%;
          background: #e6f7ff;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .title {
        font-size: 20px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        margin-bottom: 8px;
      }

      .subtitle {
        font-size: 13px;
        color: rgba(0, 0, 0, 0.45);
      }
    }

    .login-form {
      .form-options {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .login-btn {
        width: 100%;
        height: 44px;
        font-size: 16px;
        border-radius: 4px;
        background-color: #1890ff;
        border-color: #1890ff;

        &:hover {
          background-color: #40a9ff;
          border-color: #40a9ff;
        }

        &:active {
          background-color: #096dd9;
          border-color: #096dd9;
        }
      }
    }

    .login-footer {
      text-align: center;
      margin-top: 24px;
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}
</style>
