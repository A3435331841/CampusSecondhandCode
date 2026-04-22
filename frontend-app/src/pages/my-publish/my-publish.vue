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

const { proxy } = getCurrentInstance()
const list = ref([])
const loading = ref(false)
const API_ORIGIN = 'http://localhost:8080'

const normalizeImageUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return `${API_ORIGIN}${url}`
}

const getCover = (images) => {
  if (!images) {
    return 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'
  }
  return normalizeImageUrl(images.split(',')[0].trim())
}

const statusText = (status) => {
  return ({ 0: '待审核', 1: '在售', 2: '下架', 3: '已售出' }[status] || '未知')
}

const statusType = (status) => {
  return ({ 0: 'warning', 1: 'success', 2: 'error', 3: 'info' }[status] || 'info')
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
  background-color: #f3f4f6;
}

.my-publish-container {
  padding: 12px;
}

.empty-wrap {
  margin-top: 100px;
}

.item-card {
  display: flex;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .cover {
    width: 96px;
    height: 96px;
    background: #f5f5f5;
    flex-shrink: 0;
  }

  .content {
    flex: 1;
    padding: 10px;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .title {
      font-size: 14px;
      color: #333;
      font-weight: 600;
      margin-bottom: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .desc {
      font-size: 12px;
      color: #999;
      line-height: 1.4;
      margin-bottom: 8px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meta-row {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price {
        color: #ff4d4f;
        font-size: 15px;
        font-weight: 700;
      }
    }
  }
}
</style>
