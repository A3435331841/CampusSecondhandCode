<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <h2 class="page-title">消息</h2>

        <!-- Tabs -->
        <div class="tab-bar">
          <button class="tab-btn" :class="{ active: activeTab === 'chat' }" @click="activeTab = 'chat'">聊天</button>
          <button class="tab-btn" :class="{ active: activeTab === 'notice' }" @click="activeTab = 'notice'">通知</button>
        </div>

        <!-- Chat Sessions -->
        <div v-if="activeTab === 'chat'">
          <div v-if="loading" class="loading-wrap">
            <el-skeleton :rows="5" animated />
          </div>

          <el-empty v-else-if="sessions.length === 0" description="暂无聊天记录">
            <el-button type="primary" round @click="$router.push('/')">去逛逛</el-button>
          </el-empty>

          <div class="session-list" v-else>
            <div class="session-item" v-for="item in sessions" :key="item.sessionKey" @click="goChat(item)">
              <el-avatar
                :size="48"
                :src="item.peerAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.peerUserId}`"
                class="session-avatar"
              />
              <div class="session-content">
                <div class="session-top">
                  <span class="session-name">{{ item.peerNickname || ('用户' + item.peerUserId) }}</span>
                  <span class="session-time">{{ formatTime(item.lastMessageTime) }}</span>
                </div>
                <div class="session-bottom">
                  <span class="session-msg">{{ item.lastMessage || '暂无消息' }}</span>
                  <el-tag v-if="productNames[item.productId]" size="small" type="success" class="session-product">
                    {{ productNames[item.productId] }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Notice Tab -->
        <div v-if="activeTab === 'notice'">
          <el-empty description="暂无通知" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { getChatSessions } from '@/api/chat'
import { getProductDetail } from '@/api/product'

const router = useRouter()
const activeTab = ref('chat')
const loading = ref(false)
const sessions = ref([])
const productNames = ref({})

const loadSessions = async () => {
  loading.value = true
  try {
    const res = await getChatSessions({ current: 1, size: 50 })
    sessions.value = res?.records || res || []

    const productIds = [...new Set(sessions.value.map(s => s.productId).filter(Boolean))]
    const map = {}
    await Promise.all(productIds.map(async (pid) => {
      try {
        const p = await getProductDetail(pid)
        if (p) map[pid] = p.title
      } catch {}
    }))
    productNames.value = map
  } catch {
    sessions.value = []
  } finally {
    loading.value = false
  }
}

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${hh}:${mm}`
  const diff = now - d
  if (diff < 7 * 24 * 3600 * 1000) {
    return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const goChat = (item) => {
  router.push({
    path: '/chat',
    query: {
      toUserId: item.peerUserId,
      productId: item.productId || '',
      nickname: item.peerNickname || ''
    }
  })
}

onMounted(loadSessions)
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
  max-width: 800px;
  margin: 0 auto;
  padding: 28px 24px 56px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
}

/* Tabs */
.tab-bar {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.tab-btn {
  flex: 1;
  height: 48px;
  border: none;
  background: transparent;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: var(--primary);
  color: #fff;
  font-weight: 600;
}

.loading-wrap {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow);
}

/* Session list */
.session-list {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow);
}

.session-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.session-item:last-child {
  border-bottom: none;
}

.session-item:hover {
  background: #fafafa;
}

.session-avatar {
  flex-shrink: 0;
  margin-right: 16px;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #ccc;
  flex-shrink: 0;
  margin-left: 12px;
}

.session-bottom {
  display: flex;
  align-items: center;
  gap: 8px;
}

.session-msg {
  font-size: 14px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.session-product {
  flex-shrink: 0;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
