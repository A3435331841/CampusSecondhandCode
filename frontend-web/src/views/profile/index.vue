<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <div class="profile-card">
          <div class="profile-header">
            <el-avatar :size="80" :src="avatarUrl" />
            <div class="profile-info">
              <h2 class="nickname">{{ user.userInfo?.nickname }}</h2>
              <el-tag v-if="user.userInfo?.verifyStatus === 'VERIFIED'" type="success" size="small">已认证学生</el-tag>
              <el-tag v-else type="warning" size="small">未认证</el-tag>
            </div>
          </div>

          <el-divider />

          <div class="profile-menu">
            <div class="menu-item" @click="$router.push('/publish')">
              <span class="menu-icon">📦</span>
              <span class="menu-label">我发布的</span>
              <span class="menu-arrow">›</span>
            </div>
            <div class="menu-item" @click="$router.push('/orders')">
              <span class="menu-icon">🛒</span>
              <span class="menu-label">我的订单</span>
              <span class="menu-arrow">›</span>
            </div>
            <div class="menu-item" @click="$router.push('/favorites')">
              <span class="menu-icon">❤️</span>
              <span class="menu-label">我的收藏</span>
              <span class="menu-arrow">›</span>
            </div>
            <div class="menu-item" @click="$router.push('/messages')">
              <span class="menu-icon">💬</span>
              <span class="menu-label">我的消息</span>
              <span class="menu-arrow">›</span>
            </div>
            <div class="menu-item" v-if="user.userInfo?.verifyStatus !== 'VERIFIED'" @click="$router.push('/verify')">
              <span class="menu-icon">🎓</span>
              <span class="menu-label">学生认证</span>
              <span class="menu-arrow">›</span>
            </div>
          </div>

          <el-divider />

          <div class="profile-actions">
            <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'

const router = useRouter()
const user = useUserStore()

const avatarUrl = computed(() =>
  user.userInfo?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.userInfo?.userId}`
)

const handleLogout = async () => {
  try { await logout() } catch {}
  user.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  background: var(--bg);
}

.main-content {
  padding-top: var(--header-height);
}

.content-wrap {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 24px;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nickname {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
}

.profile-menu {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px 4px;
  cursor: pointer;
  border-radius: 10px;
  transition: background 0.2s;
}

.menu-item:hover { background: #f5f7fa; }

.menu-icon { font-size: 20px; margin-right: 14px; }
.menu-label { flex: 1; font-size: 15px; font-weight: 500; color: var(--text-primary); }
.menu-arrow { font-size: 20px; color: #ccc; }

.profile-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
