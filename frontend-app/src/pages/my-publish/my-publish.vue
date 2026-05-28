<template>
  <view class="my-publish-container">
    <view v-if="list.length === 0 && !loading" class="empty-wrap">
      <u-empty mode="data" text="暂未发布商品"></u-empty>
    </view>
    <view v-else class="list-wrap">
      <view class="item-card" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
        <image class="cover" :src="getCover(item.images)" mode="aspectFill"></image>
        <view class="content">
          <text class="title">{{ item.title }}</text>
          <text class="desc">{{ item.description || '暂无描述' }}</text>
          <view class="meta-row">
            <text class="price">¥{{ item.price }}</text>
            <u-tag size="mini" plain :text="statusText(item.status)" :type="statusType(item.status)"></u-tag>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { normalizeImage } from '@/utils/config'

const { proxy } = getCurrentInstance()
const list = ref([])
const loading = ref(false)

const getCover = (images) => {
  if (!images) return normalizeImage(null)
  return normalizeImage(images.split(',')[0].trim())
}

const statusText = (status) => {
  return ({ 0: '待审核', 1: '在售', 2: '下架', 3: '已售出', 4: '已驳回' }[status] || '未知')
}

const statusType = (status) => {
  return ({ 0: 'warning', 1: 'success', 2: 'error', 3: 'info', 4: 'error' }[status] || 'info')
}

const loadMyProducts = async () => {
  loading.value = true
  try {
    const res = await proxy.$request({
      url: '/product/my',
      method: 'GET',
      data: { current: 1, size: 50 }
    })
    list.value = res?.records || []
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}

onLoad(loadMyProducts)
onPullDownRefresh(loadMyProducts)
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.my-publish-container {
  padding: 24rpx;
}

.empty-wrap {
  margin-top: 200rpx;
}

.item-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.07);

  .cover {
    width: 160rpx;
    height: 160rpx;
    background: #f5f5f5;
    flex-shrink: 0;
  }

  .content {
    flex: 1;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .title {
      font-size: 28rpx;
      color: #222;
      font-weight: 600;
      margin-bottom: 8rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .desc {
      font-size: 24rpx;
      color: #999;
      line-height: 1.4;
      margin-bottom: 16rpx;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meta-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;

      .price {
        color: #FF6600;
        font-size: 30rpx;
        font-weight: 700;
      }
    }
  }
}
</style>
