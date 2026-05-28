<template>
  <view class="order-page">
    <view v-if="orders.length === 0 && !loading" class="empty-wrap">
      <u-empty mode="list" text="暂无买到的订单"></u-empty>
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
        <view class="action-row">
          <u-button
            v-if="item.status === 0"
            size="mini"
            text="取消订单"
            plain
            @click="cancelOrder(item.orderNo)"
          ></u-button>
          <u-button
            v-if="item.status === 0"
            size="mini"
            type="primary"
            text="确认收货"
            @click="confirmOrder(item.orderNo)"
          ></u-button>
          <u-button
            v-if="item.status === 1 && item.buyerRated !== 1"
            size="mini"
            type="success"
            text="评价卖家"
            @click="submitReview(item.orderNo)"
          ></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'

import { normalizeImage } from '@/utils/config'

const { proxy } = getCurrentInstance()
const orders = ref([])
const loading = ref(false)

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
    title: '确认操作',
    content: '确定取消该订单？',
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
    title: '确认操作',
    content: '确认已线下收货？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await proxy.$request({ url: `/order/confirm?orderNo=${encodeURIComponent(orderNo)}`, method: 'POST' })
      uni.showToast({ title: '已确认收货', icon: 'none' })
      loadOrders()
    }
  })
}

const submitReview = (orderNo) => {
  uni.showModal({
    title: '提交评价',
    content: '为卖家提交5星好评？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await proxy.$request({
        url: '/review/create',
        method: 'POST',
        data: {
          orderNo,
          score: 5,
          content: '交易顺利，卖家沟通很好'
        }
      })
      uni.showToast({ title: '评价已提交', icon: 'none' })
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
page { background: #F7F8FA; }

.order-page {
  padding: 24rpx;
}

.empty-wrap {
  margin-top: 200rpx;
}

.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.07);
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.order-no {
  font-size: 24rpx;
  color: #999;
}

.product-row {
  display: flex;
  align-items: flex-start;
}

.cover {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 28rpx;
  color: #222;
  font-weight: 600;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.amount {
  font-size: 30rpx;
  color: #FF6600;
  font-weight: 700;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}
</style>
