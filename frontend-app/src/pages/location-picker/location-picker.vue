<template>
  <view class="picker-page">
    <view class="search-card">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索面交地点"
        confirm-type="search"
        @confirm="searchLocations"
      />
      <view class="search-btn" @click="searchLocations">搜索</view>
    </view>

    <view class="tip-text">优先使用腾讯地图结果，异常时提供校园常用面交点</view>

    <view class="result-list">
      <view class="result-item" v-for="(item, index) in suggestions" :key="index" @click="selectLocation(item)">
        <text class="place-name">{{ item.placeName }}</text>
        <text class="address">{{ item.address }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const { proxy } = getCurrentInstance()
const keyword = ref('')
const suggestions = ref([])
const currentLat = ref(null)
const currentLng = ref(null)

const loadCurrentLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: ({ latitude, longitude }) => {
      currentLat.value = latitude
      currentLng.value = longitude
    }
  })
}

const searchLocations = async () => {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入地点关键词', icon: 'none' })
    return
  }
  try {
    const params = {
      keyword: keyword.value.trim()
    }
    if (currentLat.value != null && currentLng.value != null) {
      params.lat = currentLat.value
      params.lng = currentLng.value
    }
    const result = await proxy.$request({
      url: '/location/search',
      method: 'GET',
      data: params
    })
    suggestions.value = result || []
  } catch (error) {
    uni.showToast({ title: error?.message || '地点搜索失败', icon: 'none' })
  }
}

const selectLocation = (item) => {
  uni.$emit('pickLocation', item)
  uni.navigateBack()
}

onLoad(() => {
  loadCurrentLocation()
})
</script>

<style lang="scss">
page {
  background: #F7F8FA;
}

.picker-page {
  padding: 14px;
}

.search-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  align-items: center;
}

.search-input {
  flex: 1;
  height: 36px;
  background: #f7f8fa;
  border-radius: 18px;
  padding: 0 12px;
  font-size: 14px;
}

.search-btn {
  margin-left: 10px;
  background: #1890ff;
  color: #fff;
  border-radius: 18px;
  padding: 8px 14px;
  font-size: 13px;
}

.tip-text {
  margin: 12px 2px;
  font-size: 12px;
  color: #888;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-item {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}

.place-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.address {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #777;
}
</style>
