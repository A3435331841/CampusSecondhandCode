<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo-area">
        <div class="logo-icon">🏫</div>
        <div class="title">校园二手交易 · 管理后台</div>
        <div class="subtitle">Campus Secondhand Admin</div>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
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
      <div class="hint">默认账号：admin / 123456</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth.js'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

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
  background: linear-gradient(135deg, #1890FF 0%, #36cfc9 100%);

  .login-box {
    width: 420px;
    padding: 40px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);

    .logo-area {
      text-align: center;
      margin-bottom: 32px;

      .logo-icon {
        font-size: 48px;
        margin-bottom: 10px;
      }

      .title {
        font-size: 22px;
        font-weight: bold;
        color: #333;
        margin-bottom: 4px;
      }

      .subtitle {
        font-size: 13px;
        color: #aaa;
      }
    }

    .login-btn {
      width: 100%;
      margin-top: 8px;
      height: 44px;
      font-size: 16px;
    }

    .hint {
      text-align: center;
      font-size: 12px;
      color: #bbb;
      margin-top: 16px;
    }
  }
}
</style>
