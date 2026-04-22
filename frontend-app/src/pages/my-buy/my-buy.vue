<template>
  <view class="my-buy-container">
    <view v-if="orders.length === 0 && !loading" class="empty-wrap">
      <u-empty mode="list" text="暂无购买订单"></u-empty>
    </view>
    <view v-else>
      <view class="order-card" v-for="item in orders" :key="item.orderNo">
        <view class="top-row">
          <text class="order-no">订单号：{{ item.orderNo }}</text>
          <u-tag size="mini" plain :text="statusText(item.status)" :type="statusType(item.status)"></u-tag>
        </view>
        <view class="product-row" @click="goDetail(item.productId)">
          <image class="cover" :src="normalizeImage(item.productImage)" mode="aspectFill"></image>
          <view class="info">
            <text class="title">{{ item.productTitle || '商品已下架' }}</text>
            <text class="meta">数量 x{{ item.buyCount || 1 }}</text>
            <text class="amount">实付 ¥{{ item.totalAmount }}</text>
          </view>
        </view>
        <view class="action-row" v-if="item.status === 0">
          <u-button size="mini" text="取消订单" plain @click="cancelOrder(item.orderNo)"></u-button>
          <u-button size="mini" type="primary" text="确认收货" @click="confirmOrder(item.orderNo)"></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'

const { proxy } = getCurrentInstance()
const API_ORIGIN = 'http://localhost:8080'
const orders = ref([])
const loading = ref(false)

const normalizeImage = (url) => {
  if (!url) return 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'
  if (/^https?:\/\//.test(url)) return url
  return `${API_ORIGIN}${url}`
}

const statusText = (status) => ({ 0: '待交接', 1: '已完成', 2: '已取消' }[status] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'info' }[status] || 'info')

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await proxy.$request({
      url: '/order/my/buy',
      method: 'GET',
      data: { current: 1, size: 50 }
    })
    orders.value = res?.records || []
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

const cancelOrder = (orderNo) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await proxy.$request({ url: `/order/cancel?orderNo=${encodeURIComponent(orderNo)}`, method: 'POST' })
      uni.showToast({ title: '订单已取消', icon: 'none' })
      loadOrders()
    }
  })
}

const confirmOrder = (orderNo) => {
  uni.showModal({
    title: '确认收货',
    content: '确认已线下完成交易并收货？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await proxy.$request({ url: `/order/confirm?orderNo=${encodeURIComponent(orderNo)}`, method: 'POST' })
      uni.showToast({ title: '确认收货成功', icon: 'none' })
      loadOrders()
    }
  })
}

const goDetail = (id) => {
  if (!id) return
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}

onLoad(loadOrders)
onPullDownRefresh(loadOrders)
</script>

<style lang="scss">
page { background: #f3f4f6; }

.my-buy-container {
  padding: 12px;
}

.empty-wrap {
  margin-top: 120px;
}

.order-card {
  background: #fff;
  border-radius: 10px;
  padding: 10px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .top-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .order-no {
      font-size: 12px;
      color: #666;
    }
  }

  .product-row {
    display: flex;
    .cover {
      width: 72px;
      height: 72px;
      border-radius: 6px;
      background: #f5f5f5;
      margin-right: 10px;
    }
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      .title {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }
      .meta {
        font-size: 12px;
        color: #999;
        margin-bottom: 6px;
      }
      .amount {
        font-size: 15px;
        color: #ff4d4f;
        font-weight: 700;
      }
    }
  }

  .action-row {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 10px;
  }
}
</style>
