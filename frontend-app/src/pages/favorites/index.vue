<template>
  <view class="favorites-page">
    <view v-if="loading" class="loading-box">
      <text class="loading-text">加载中...</text>
    </view>
    <view v-else-if="list.length === 0" class="empty-box">
      <text class="empty-icon">🤍</text>
      <text class="empty-text">暂无收藏商品</text>
      <view class="empty-btn" @click="goHome">
        <text class="empty-btn-text">去逛逛</text>
      </view>
    </view>
    <view v-else class="fav-list">
      <view class="fav-card" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
        <image :src="normalizeImageUrl(item.images)" class="fav-img" mode="aspectFill"></image>
        <view class="fav-info">
          <text class="fav-title">{{ item.title }}</text>
          <text class="fav-price">¥{{ item.price }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { normalizeImage } from '@/utils/config'

const { proxy } = getCurrentInstance()
const list = ref([])
const loading = ref(true)

const normalizeImageUrl = (url) => {
  if (!url) return normalizeImage(null)
  return normalizeImage(url.split(',')[0].trim())
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await proxy.$request({ url: '/favorite/list', method: 'GET' })
    list.value = res || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}

const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

onShow(loadFavorites)
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.favorites-page {
  min-height: 100vh;
  padding: 24rpx;
}

.loading-box {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border-radius: 40rpx;
  padding: 20rpx 60rpx;
}

.empty-btn-text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

/* 2-column grid */
.fav-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}

.fav-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.07);
  display: flex;
  flex-direction: column;
}

.fav-img {
  width: 100%;
  height: 240rpx;
  background-color: #f5f5f5;
}

.fav-info {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.fav-title {
  font-size: 26rpx;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
}

.fav-price {
  font-size: 30rpx;
  font-weight: 700;
  color: #FF6600;
}
</style>
