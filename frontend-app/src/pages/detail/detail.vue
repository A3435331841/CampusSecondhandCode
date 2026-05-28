<template>
  <view class="detail-container">
    <!-- 图片轮播 -->
    <view class="swiper-wrap">
      <swiper class="product-swiper" circular :indicator-dots="false" @change="onSwiperChange">
        <swiper-item v-for="(img, index) in product.images" :key="index">
          <image :src="img" class="swiper-img" mode="aspectFill" @click="previewImage(index)" @error="onImgError(index)"></image>
        </swiper-item>
      </swiper>
      <view class="img-counter">
        <text class="img-counter-text">{{ currentImgIdx + 1 }}/{{ product.images.length }}</text>
      </view>
    </view>

    <!-- 商品信息区 -->
    <view class="info-card">
      <text class="detail-price">¥{{ product.price }}</text>
      <text class="detail-title">{{ product.title }}</text>
      <text class="detail-desc" v-if="product.description">{{ product.description }}</text>
    </view>

    <!-- 卖家卡片 -->
    <view class="seller-card">
      <image class="seller-avatar" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${product.sellerId}`" mode="aspectFill"></image>
      <view class="seller-info">
        <text class="seller-nickname">卖家{{ product.sellerId }}</text>
        <view class="seller-credit">
          <text class="credit-label">信用良好</text>
        </view>
      </view>
      <view class="chat-btn-inline" @click="goChat">
        <text class="chat-btn-text">聊一聊</text>
      </view>
    </view>

    <!-- 面交地点 -->
    <view class="location-card" v-if="product.pickupPlaceName || product.pickupAddress">
      <view class="location-row">
        <text class="location-icon">📍</text>
        <view class="location-info">
          <text class="location-name">{{ product.pickupPlaceName || '未设置' }}</text>
          <text class="location-address">{{ product.pickupAddress || '' }}</text>
        </view>
      </view>
    </view>

    <!-- 评论区 -->
    <view class="comment-card">
      <view class="comment-header">
        <text class="comment-title">留言({{ product.commentCount || comments.length }})</text>
      </view>

      <view class="comment-input-row">
        <input v-model="commentText" class="comment-input" placeholder="写留言..." />
        <view class="comment-submit" @click="submitComment">
          <text class="submit-text">发送</text>
        </view>
      </view>

      <view class="comment-list">
        <view class="comment-item" v-for="item in comments" :key="item.id">
          <image class="comment-avatar" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`" mode="aspectFill"></image>
          <view class="comment-body">
            <view class="comment-meta">
              <text class="comment-user">用户{{ item.userId }}</text>
              <text class="comment-time" v-if="item.createTime">{{ item.createTime }}</text>
            </view>
            <text class="comment-content">{{ item.content }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部固定栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-icon-btn" :class="{ active: favorited }" @click="toggleFavorite">
          <text class="bar-icon">{{ favorited ? '❤️' : '🤍' }}</text>
          <text class="bar-icon-label">收藏</text>
        </view>
        <view class="bar-icon-btn" @click="scrollToComment">
          <text class="bar-icon">💬</text>
          <text class="bar-icon-label">评论</text>
        </view>
      </view>
      <view class="bar-right">
        <view class="want-btn" @click="handleBuy">
          <text class="want-btn-text">我想要</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { normalizeImage as normalizeImageUrl, FALLBACK_IMG } from '@/utils/config'

const { proxy } = getCurrentInstance()
const currentImgIdx = ref(0)

const onImgError = (index) => {
  product.value.images[index] = FALLBACK_IMG
}

const product = ref({
  id: '',
  title: '',
  description: '',
  price: '',
  images: ['https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80'],
  pickupPlaceName: '',
  pickupAddress: '',
  sellerId: '',
  favoriteCount: 0,
  commentCount: 0
})
const favorited = ref(false)
const comments = ref([])
const commentText = ref('')

const onSwiperChange = (e) => {
  currentImgIdx.value = e.detail.current
}

const scrollToComment = () => {
  // 简单滚动提示
  uni.pageScrollTo({ selector: '.comment-card', duration: 300 })
}

const fetchProductDetail = async (id) => {
  const res = await proxy.$request({
    url: `/product/detail/${id}`,
    method: 'GET'
  })

  product.value = {
    id: res.id,
    title: res.title,
    description: res.description,
    price: res.price,
    images: res.images
      ? res.images.split(',').map(img => normalizeImageUrl(img.trim())).filter(Boolean)
      : ['https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80'],
    pickupPlaceName: res.pickupPlaceName,
    pickupAddress: res.pickupAddress,
    sellerId: res.sellerId,
    favoriteCount: res.favoriteCount || 0,
    commentCount: res.commentCount || 0
  }
}

const loadFavoriteStatus = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token || !product.value.id) {
    favorited.value = false
    return
  }
  try {
    const result = await proxy.$request({
      url: '/favorite/status',
      method: 'GET',
      data: { productId: product.value.id }
    })
    favorited.value = !!result?.favorited
  } catch {
    favorited.value = false
  }
}

const loadComments = async () => {
  if (!product.value.id) {
    comments.value = []
    return
  }
  const result = await proxy.$request({
    url: '/comment/list',
    method: 'GET',
    data: { productId: product.value.id }
  })
  comments.value = result || []
}

const previewImage = (index) => {
  uni.previewImage({
    current: index,
    urls: product.value.images
  })
}

const goChat = () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/chat/chat?toUserId=${product.value.sellerId}&productId=${product.value.id}&title=${encodeURIComponent(product.value.title || '聊天')}`
  })
}

const handleBuy = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  const orderNo = await proxy.$request({
    url: '/order/create',
    method: 'POST',
    data: { productId: product.value.id, buyCount: 1 }
  })
  uni.showToast({ title: `下单成功：${orderNo}`, icon: 'none' })
}

const toggleFavorite = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  await proxy.$request({
    url: favorited.value ? '/favorite/remove' : '/favorite/add',
    method: 'POST',
    data: { productId: product.value.id }
  })
  favorited.value = !favorited.value
  product.value.favoriteCount += favorited.value ? 1 : -1
}

const submitComment = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  const content = commentText.value.trim()
  if (!content) {
    uni.showToast({ title: '请输入留言内容', icon: 'none' })
    return
  }
  await proxy.$request({
    url: '/comment/add',
    method: 'POST',
    data: { productId: product.value.id, content }
  })
  product.value.commentCount++
  comments.value.push({ id: Date.now(), userId: '我', content })
  commentText.value = ''
}

onLoad((options) => {
  if (options.id) {
    fetchProductDetail(options.id).then(async () => {
      await loadFavoriteStatus()
      await loadComments()
    })
  }
})
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.detail-container {
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 图片轮播 */
.swiper-wrap {
  position: relative;
  width: 100%;
}

.product-swiper {
  height: 600rpx;
  width: 100%;
  background-color: #000;
}

.swiper-img {
  width: 100%;
  height: 100%;
}

.img-counter {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 20rpx;
  padding: 6rpx 18rpx;
  backdrop-filter: blur(4px);
}

.img-counter-text {
  font-size: 24rpx;
  color: #1A1A1A;
  font-weight: 500;
}

/* 商品信息卡片 */
.info-card {
  background: #FFFFFF;
  padding: 24rpx;
  margin: 24rpx;
  border-radius: 16rpx;
}

.detail-price {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #FF6633;
  margin-bottom: 16rpx;
}

.detail-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #1A1A1A;
  line-height: 1.5;
}

.detail-desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-top: 12rpx;
}

/* 卖家卡片 */
.seller-card {
  background: #FFFFFF;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
}

.seller-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #f5f5f5;
}

.seller-info {
  flex: 1;
}

.seller-nickname {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 6rpx;
}

.seller-credit {
  display: inline-flex;
}

.credit-label {
  font-size: 22rpx;
  color: #00B578;
  background: rgba(0, 181, 120, 0.08);
  border-radius: 6rpx;
  padding: 4rpx 10rpx;
}

.chat-btn-inline {
  background: #fff;
  border: 2rpx solid #00B578;
  border-radius: 24rpx;
  padding: 12rpx 28rpx;
}

.chat-btn-text {
  font-size: 26rpx;
  color: #00B578;
  font-weight: 600;
}

/* 面交地点 */
.location-card {
  background: #FFFFFF;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.location-row {
  display: flex;
  align-items: center;
}

.location-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.location-info {
  flex: 1;
}

.location-name {
  display: block;
  font-size: 28rpx;
  color: #1A1A1A;
  font-weight: 500;
  margin-bottom: 4rpx;
}

.location-address {
  display: block;
  font-size: 24rpx;
  color: #999;
}

/* 评论区 */
.comment-card {
  background: #FFFFFF;
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.comment-header {
  margin-bottom: 20rpx;
}

.comment-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1A1A1A;
}

.comment-input-row {
  display: flex;
  margin-bottom: 24rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  background: #F7F8FA;
  border-radius: 36rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
}

.comment-submit {
  margin-left: 16rpx;
  width: 120rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #00B578, #00D68F);
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-text {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}

.comment-item {
  display: flex;
  padding: 20rpx 0;
  border-top: 1rpx solid #F7F8FA;
}

.comment-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background-color: #f5f5f5;
}

.comment-body {
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.comment-user {
  font-size: 24rpx;
  color: #1A1A1A;
  font-weight: 500;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 16rpx;
}

.comment-content {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
}

/* 底部固定栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  padding: 0 24rpx env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
}

.bar-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 40rpx;
}

.bar-icon {
  font-size: 36rpx;
}

.bar-icon-label {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
}

.bar-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.want-btn {
  background: linear-gradient(135deg, #00B578, #00D68F);
  border-radius: 50rpx;
  height: 88rpx;
  padding: 0 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 181, 120, 0.25);
}

.want-btn-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: bold;
}
</style>
