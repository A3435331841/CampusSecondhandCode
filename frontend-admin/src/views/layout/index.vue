<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="isCollapse ? 24 : 28"><Shop /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">校园二手</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="sidebar-menu"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/product">
          <el-icon><Goods /></el-icon>
          <template #title>商品管理</template>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><CollectionTag /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/verification">
          <el-icon><Postcard /></el-icon>
          <template #title>认证信息</template>
        </el-menu-item>
        <el-menu-item index="/comment">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
      </el-menu>
      <div class="collapse-btn" @click="isCollapse = !isCollapse">
        <el-icon :size="16">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header" height="64px">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="adminUser.avatar || fallbackAvatar" />
              <span class="name">{{ adminUser.nickname || '管理员' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Fold, Expand, SwitchButton, Shop } from '@element-plus/icons-vue'
import { logout } from '../../api/auth.js'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const fallbackAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const adminUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('admin_user') || '{}')
  } catch {
    return {}
  }
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logout()
    } catch {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  overflow: hidden;

  .aside {
    background-color: #001529;
    display: flex;
    flex-direction: column;
    transition: width 0.2s;
    overflow: hidden;

    .logo-area {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 0 16px;
      background: linear-gradient(180deg, #1d3a6e 0%, #001f4d 100%);
      border-bottom: 1px solid rgba(255, 255, 255, 0.06);

      .logo-icon {
        color: #40a9ff;
        display: flex;
        align-items: center;
        justify-content: center;
        filter: drop-shadow(0 0 6px rgba(64, 169, 255, 0.5));
      }

      .logo-text {
        color: #40a9ff;
        font-size: 18px;
        font-weight: 700;
        white-space: nowrap;
        letter-spacing: 2px;
        text-shadow: 0 0 12px rgba(64, 169, 255, 0.4);
      }
    }

    .sidebar-menu {
      flex: 1;
      border-right: none;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 8px 0;

      &::-webkit-scrollbar {
        width: 0;
      }

      :deep(.el-menu-item) {
        height: 46px;
        line-height: 46px;
        margin: 2px 10px;
        border-radius: 8px;
        position: relative;
        transition: background 0.18s, color 0.18s;

        &:hover {
          background-color: rgba(64, 169, 255, 0.12) !important;
          color: rgba(255, 255, 255, 0.9) !important;
        }

        &.is-active {
          background: linear-gradient(90deg, rgba(24, 144, 255, 0.2), rgba(24, 144, 255, 0.04)) !important;
          color: #40a9ff !important;
          font-weight: 600;

          &::before {
            content: '';
            position: absolute;
            left: -10px;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 60%;
            background: #1890ff;
            border-radius: 0 3px 3px 0;
          }
        }
      }
    }

    .collapse-btn {
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.65);
      cursor: pointer;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
      transition: color 0.2s;

      &:hover {
        color: #fff;
      }
    }
  }

  .main-container {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .header {
    background-color: #fff;
    box-shadow: 0 1px 0 #f0f0f0, 0 2px 8px rgba(0, 21, 41, 0.04);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    z-index: 10;
  }

  .header-left {
    display: flex;
    align-items: center;
  }

  .header-right {
    display: flex;
    align-items: center;
  }

  .user-dropdown {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 6px;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }

    .name {
      margin-left: 8px;
      color: rgba(0, 0, 0, 0.85);
      font-size: 14px;
    }
  }

  .main-content {
    background-color: #f0f2f5;
    padding: 24px;
    overflow-y: auto;
  }
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.25s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
