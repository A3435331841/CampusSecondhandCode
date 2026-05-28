<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <h2 class="page-title">我的订单</h2>

        <div class="tab-bar">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-btn"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</button>
        </div>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="4" animated />
        </div>

        <el-empty v-else-if="orders.length === 0" description="暂无订单" />

        <div v-else class="order-list">
          <div v-for="item in orders" :key="item.orderNo" class="order-card">
            <div class="order-header">
              <span class="order-no">{{ item.orderNo }}</span>
              <el-tag :type="statusType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
            </div>
            <div class="order-body" @click="$router.push(`/detail/${item.productId}`)">
              <img :src="normalizeImage(item.productImage)" class="order-img" @error="e => e.target.src = FALLBACK" />
              <div class="order-info">
                <p class="order-title">{{ item.productTitle || '商品已下架' }}</p>
                <p class="order-amount">¥{{ item.totalAmount }}</p>
              </div>
            </div>
            <div class="order-actions" v-if="activeTab === 'buy'">
              <el-button v-if="item.status === 0" size="small" @click="handleCancel(item.orderNo)">取消订单</el-button>
              <el-button v-if="item.status === 0" size="small" type="primary" @click="handleConfirm(item.orderNo)">确认收货</el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { getMyBuyOrders, getMySellOrders, cancelOrder, confirmOrder } from '@/api/order'

const FALLBACK = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'

const tabs = [
  { key: 'buy', label: '我买到的' },
  { key: 'sell', label: '我卖出的' }
]

const activeTab = ref('buy')
const orders = ref([])
const loading = ref(false)

const normalizeImage = (url) => {
  if (!url) return FALLBACK
  if (/^https?:\/\//.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const statusText = (s) => ({ 0: '待交接', 1: '已完成', 2: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info')

const loadOrders = async () => {
  loading.value = true
  try {
    const fn = activeTab.value === 'buy' ? getMyBuyOrders : getMySellOrders
    const res = await fn({ current: 1, size: 50 })
    orders.value = res?.records || []
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = (key) => {
  activeTab.value = key
  loadOrders()
}

const handleCancel = async (orderNo) => {
  await ElMessageBox.confirm('确定取消该订单？', '确认')
  await cancelOrder(orderNo)
  ElMessage.success('订单已取消')
  loadOrders()
}

const handleConfirm = async (orderNo) => {
  await ElMessageBox.confirm('确认已线下收货？', '确认')
  await confirmOrder(orderNo)
  ElMessage.success('已确认收货')
  loadOrders()
}

onMounted(loadOrders)
</script>

<style scoped>
.page-layout { min-height: 100vh; background: var(--bg); }
.main-content { padding-top: var(--header-height); }
.content-wrap { max-width: 800px; margin: 0 auto; padding: 28px 24px 56px; }

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.tab-bar {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  background: none;
  border: none;
  font-size: 15px;
  font-weight: 500;
  color: #999;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}

.tab-btn.active {
  color: var(--text-primary);
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 30%;
  width: 40%;
  height: 3px;
  background: var(--accent);
  border-radius: 2px;
}

.loading-wrap { padding: 40px 0; }

.order-list { display: flex; flex-direction: column; gap: 16px; }

.order-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.order-no { font-size: 13px; color: #999; }

.order-body {
  display: flex;
  gap: 16px;
  cursor: pointer;
}

.order-img {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  background: #f5f5f5;
  flex-shrink: 0;
}

.order-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.order-title { font-size: 15px; font-weight: 500; color: var(--text-primary); margin-bottom: 8px; }
.order-amount { font-size: 18px; font-weight: 700; color: var(--accent); }

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}
</style>
