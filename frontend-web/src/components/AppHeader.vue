<template>
  <header class="app-header" :class="{ scrolled: isScrolled }">
    <div class="header-inner">
      <div class="logo" @click="$router.push('/')">
        <span class="logo-icon">🏫</span>
        <span class="logo-text">校园二手</span>
      </div>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="search-btn" @click="handleSearch">搜索</el-button>
      </div>

      <div class="nav-links">
        <router-link to="/ai" class="nav-link ai-link">
          <el-icon><MagicStick /></el-icon>
          <span class="nav-label">AI找货</span>
        </router-link>
        <router-link v-if="user.isLoggedIn" to="/publish" class="nav-link">
          <el-icon><Plus /></el-icon>
          <span class="nav-label">发布</span>
        </router-link>
        <router-link v-if="user.isLoggedIn" to="/messages" class="nav-link">
          <el-icon><ChatDotRound /></el-icon>
          <span class="nav-label">消息</span>
        </router-link>
      </div>

      <div class="header-actions">
        <template v-if="user.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="34" :src="avatarUrl" class="user-avatar" />
              <span class="username">{{ user.userInfo?.nickname }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">我的主页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else-if="user.isGuest">
          <span class="guest-tag">游客</span>
          <el-button type="primary" size="small" round @click="$router.push('/login')">登录</el-button>
        </template>
        <template v-else>
          <el-button type="primary" round @click="$router.push('/login')">登录</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const keyword = ref(route.query.keyword || '')
const isScrolled = ref(false)

const avatarUrl = computed(() =>
  user.userInfo?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.userInfo?.userId}`
)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => window.addEventListener('scroll', handleScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

const handleSearch = () => {
  router.push({ path: '/', query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {} })
}

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    try { await logout() } catch {}
    user.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--border);
  z-index: 100;
  box-shadow: var(--shadow-sm);
  transition: height var(--transition-base), background var(--transition-base), box-shadow var(--transition-base);
  will-change: height;
}

.app-header.scrolled {
  height: 54px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.10);
  border-bottom-color: transparent;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
  text-decoration: none;
}

.logo-icon {
  font-size: 24px;
  transition: transform var(--transition-base);
}

.logo:hover .logo-icon {
  transform: rotate(-8deg) scale(1.1);
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.search-bar {
  flex: 1;
  display: flex;
  gap: 8px;
  max-width: 560px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 50px;
  transition: box-shadow var(--transition-base);
}

.search-input :deep(.el-input__wrapper):hover {
  box-shadow: 0 0 0 1px var(--primary) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary) inset, 0 0 0 3px rgba(24, 144, 255, 0.12) !important;
}

.search-btn {
  border-radius: 50px;
  padding: 0 20px;
}

.header-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 50px;
  border: 1.5px solid var(--border);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.user-info:hover {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.08);
}

.user-avatar {
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  color: #aaa;
}

.guest-tag {
  font-size: 13px;
  color: var(--text-secondary);
  background: #F7F8FA;
  padding: 4px 12px;
  border-radius: 50px;
  border: 1px solid var(--border);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 50px;
  font-size: 14px;
  font-weight: 500;
  color: #555;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.nav-link:hover {
  background: #f0f7ff;
  color: var(--primary);
}

.nav-link.ai-link {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  color: #667eea;
}

.nav-link.ai-link:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.18), rgba(118, 75, 162, 0.18));
}

.nav-label {
  white-space: nowrap;
}

@media (max-width: 768px) {
  .nav-label {
    display: none;
  }
  .nav-link {
    padding: 6px 10px;
  }
}
</style>
