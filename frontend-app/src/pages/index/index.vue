<template>
  <view class="home-container">
    <!-- 固定顶部搜索栏 -->
    <view class="search-header">
      <view class="search-header-inner">
        <!-- 左侧Logo -->
        <view class="logo-circle">
          <text class="logo-text">闲</text>
        </view>
        <!-- 中间搜索框 -->
        <view class="search-box" @click="focusSearch">
          <text class="search-icon">&#x1F50D;</text>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索你想要的宝贝"
            class="search-input"
            confirm-type="search"
            @confirm="handleSearch"
          />
          <text v-if="keyword" class="clear-btn" @click.stop="clearSearch">✕</text>
        </view>
        <!-- 右侧AI按钮 -->
        <view class="ai-btn" @click="goAiSearch">
          <text class="ai-btn-text">AI</text>
        </view>
      </view>
    </view>

    <!-- 分类导航条 -->
    <view class="category-bar">
      <scroll-view scroll-x class="category-scroll" :show-scrollbar="false">
        <view class="category-tab-list">
          <view
            class="category-tab-item"
            :class="{ active: selectedCategoryId === null }"
            @click="handleTabClick(null)"
          >
            <text class="category-tab-text">推荐</text>
            <view v-if="selectedCategoryId === null" class="tab-indicator"></view>
          </view>
          <view
            v-for="item in categories"
            :key="item.id"
            class="category-tab-item"
            :class="{ active: selectedCategoryId === item.id }"
            @click="handleTabClick(item.id)"
          >
            <text class="category-tab-text">{{ item.name }}</text>
            <view v-if="selectedCategoryId === item.id" class="tab-indicator"></view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 搜索结果提示 -->
    <view v-if="searchMode && products.length > 0" class="search-result-hint">
      <text class="hint-text">找到 {{ products.length }} 件商品</text>
    </view>

    <!-- 空搜索结果 -->
    <view v-if="searchMode && products.length === 0" class="empty-search">
      <text class="empty-text">未找到相关商品</text>
    </view>

    <!-- 商品瀑布流 -->
    <view class="waterfall-wrapper" v-if="products.length > 0">
      <view class="waterfall">
        <view class="waterfall-col">
          <view class="goods-card" v-for="item in leftCol" :key="item.id" @click="goToDetail(item.id)">
            <image :src="item.image" class="goods-img" mode="widthFix"></image>
            <view class="goods-info">
              <text class="goods-title">{{ item.title }}</text>
              <view class="goods-bottom">
                <text class="goods-price">¥{{ item.price }}</text>
              </view>
              <view class="goods-seller">
                <image :src="item.avatar" class="seller-avatar" mode="aspectFill"></image>
                <text class="seller-name">{{ item.seller }}</text>
                <view class="want-btn">
                  <text class="want-icon">♡</text>
                  <text class="want-count">{{ item.wantCount || 0 }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view class="waterfall-col">
          <view class="goods-card" v-for="item in rightCol" :key="item.id" @click="goToDetail(item.id)">
            <image :src="item.image" class="goods-img" mode="widthFix"></image>
            <view class="goods-info">
              <text class="goods-title">{{ item.title }}</text>
              <view class="goods-bottom">
                <text class="goods-price">¥{{ item.price }}</text>
              </view>
              <view class="goods-seller">
                <image :src="item.avatar" class="seller-avatar" mode="aspectFill"></image>
                <text class="seller-name">{{ item.seller }}</text>
                <view class="want-btn">
                  <text class="want-icon">♡</text>
                  <text class="want-count">{{ item.wantCount || 0 }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <custom-tabbar :current="0" />
  </view>
</template>

<script setup>
import { computed, getCurrentInstance, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { normalizeImage as normalizeImageUrl } from '@/utils/config'
import customTabbar from '@/components/custom-tabbar.vue'

const { proxy } = getCurrentInstance()

const keyword = ref('')
const searchMode = ref(false)
const products = ref([])
const recommendedProducts = ref([])
const categories = ref([])
const selectedCategoryId = ref(null)

const goAiSearch = () => {
  uni.switchTab({ url: '/pages/ai-search/ai-search' })
}

const categoryUi = {
  1: { icon: '💻', color: '#E6F7FF' },
  2: { icon: '📚', color: '#F6FFED' },
  3: { icon: '👟', color: '#FFF0F6' },
  4: { icon: '🚲', color: '#FFF7E6' },
  5: { icon: '🏠', color: '#E6FFFB' },
  6: { icon: '📦', color: '#F5F5F5' },
  7: { icon: '🏀', color: '#F0F5FF' },
  8: { icon: '💄', color: '#FFF0F5' },
  9: { icon: '🍪', color: '#FFFBE6' },
  10: { icon: '🎸', color: '#F9F0FF' },
  11: { icon: '🛋️', color: '#FCF4E8' },
  12: { icon: '🎫', color: '#E6F9F0' }
}

const banners = ref([
  'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&q=80&w=800&h=400',
  'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=800&h=400',
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=800&h=400'
])

// 瀑布流分列
const leftCol = computed(() => products.value.filter((_, i) => i % 2 === 0))
const rightCol = computed(() => products.value.filter((_, i) => i % 2 === 1))
const recLeftCol = computed(() => recommendedProducts.value.filter((_, i) => i % 2 === 0))
const recRightCol = computed(() => recommendedProducts.value.filter((_, i) => i % 2 === 1))


const loadCategories = async () => {
  const res = await proxy.$request({ url: '/category/list', method: 'GET' })
  categories.value = (res || []).map(item => ({
    ...item,
    icon: categoryUi[item.id]?.icon || '📦',
    color: categoryUi[item.id]?.color || '#F5F5F5'
  }))
}

const fetchProducts = async (kw = '', categoryId = null) => {
  try {
    const params = { current: 1, size: 20, status: 1 }
    if (kw) params.keyword = kw
    if (categoryId) params.categoryId = categoryId

    const res = await proxy.$request({ url: '/product/list', method: 'GET', data: params })
    const categoryMap = Object.fromEntries(categories.value.map(item => [item.id, item.name]))
    products.value = (res?.records || []).map(item => ({
      ...mapProduct(item, categoryMap)
    }))
  } catch (error) {
    console.error('Failed to load products:', error)
  }
}

const fetchRecommendations = async () => {
  const token = uni.getStorageSync('user_token')
  if (!token) {
    recommendedProducts.value = []
    return
  }
  try {
    const categoryMap = Object.fromEntries(categories.value.map(item => [item.id, item.name]))
    const res = await proxy.$request({ url: '/recommend/list', method: 'GET', data: { size: 10 } })
    recommendedProducts.value = (res || []).map(item => ({
      ...mapProduct(item, categoryMap)
    }))
  } catch (error) {
    recommendedProducts.value = []
    console.error('Failed to load recommendations:', error)
  }
}

const mapProduct = (item, categoryMap) => ({
  id: item.id,
  title: item.title,
  price: item.price,
  image: item.images
    ? normalizeImageUrl(item.images.split(',')[0].trim())
    : 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80',
  seller: `卖家${item.sellerId}`,
  avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.sellerId}`,
  categoryName: categoryMap[item.categoryId] || '其他',
  wantCount: item.wantCount || Math.floor(Math.random() * 20)
})

const handleSearch = () => {
  searchMode.value = !!keyword.value.trim()
  selectedCategoryId.value = null
  fetchProducts(keyword.value)
}
const clearSearch = () => {
  keyword.value = ''
  searchMode.value = false
  selectedCategoryId.value = null
  fetchProducts()
}
const focusSearch = () => {
  // placeholder for future focus logic
}
const handleTabClick = (categoryId) => {
  selectedCategoryId.value = categoryId
  keyword.value = ''
  searchMode.value = false
  if (categoryId === null) {
    fetchProducts()
  } else {
    fetchProducts('', categoryId)
  }
}
const handleCategoryClick = (item) => {
  handleTabClick(item.id)
}
const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}

onShow(async () => {
  await loadCategories()
  await fetchProducts()
  await fetchRecommendations()
})
</script>

<style lang="scss">
page {
  background-color: #F7F8FA;
}

.home-container {
  padding-bottom: 24rpx;
}

/* ========== 搜索栏 ========== */
.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
  height: 88rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0.04);
}

.search-header-inner {
  display: flex;
  align-items: center;
  width: 100%;
}

.logo-circle {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background-color: var(--primary, #00B578);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16rpx;
}

.logo-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 700;
}

.search-box {
  flex: 1;
  background-color: #F5F5F5;
  border-radius: 32rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.search-icon {
  font-size: 24rpx;
  margin-right: 12rpx;
  opacity: 0.5;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-primary, #1A1A1A);
  height: 64rpx;
  line-height: 64rpx;
}

.clear-btn {
  font-size: 24rpx;
  color: #999;
  padding: 10rpx;
}

.ai-btn {
  margin-left: 16rpx;
  background: linear-gradient(135deg, var(--primary, #00B578), var(--primary-light, #00D68F));
  border-radius: 20rpx;
  padding: 10rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-btn-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 700;
  letter-spacing: 1rpx;
}

/* ========== 分类导航条 ========== */
.category-bar {
  background-color: #fff;
  height: 80rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.category-scroll {
  height: 80rpx;
  white-space: nowrap;
}

.category-tab-list {
  display: inline-flex;
  align-items: center;
  height: 80rpx;
  padding: 0 24rpx;
}

.category-tab-item {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  margin-right: 48rpx;

  .category-tab-text {
    font-size: 28rpx;
    color: #666666;
    line-height: 80rpx;
  }

  &.active .category-tab-text {
    color: var(--primary, #00B578);
    font-weight: 600;
  }

  .tab-indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 40rpx;
    height: 4rpx;
    border-radius: 2rpx;
    background-color: var(--primary, #00B578);
  }
}

/* ========== 搜索结果提示 ========== */
.search-result-hint {
  padding: 20rpx 32rpx;
  background-color: #F7F8FA;

  .hint-text {
    font-size: 26rpx;
    color: var(--text-secondary, #666666);
  }
}

/* ========== 空状态 ========== */
.empty-search {
  text-align: center;
  padding: 120rpx 40rpx;

  .empty-text {
    font-size: 28rpx;
    color: var(--text-hint, #999999);
  }
}

/* ========== 瀑布流 ========== */
.waterfall-wrapper {
  padding: 16rpx;
  background-color: #F7F8FA;
}

.waterfall {
  display: flex;
  gap: 16rpx;
}

.waterfall-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.goods-card {
  background-color: var(--card-bg, #FFFFFF);
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow, 0 2rpx 12rpx rgba(0, 0, 0, 0.06));
}

.goods-img {
  width: 100%;
  display: block;
  border-radius: 16rpx 16rpx 0 0;
  background-color: #F0F0F0;
}

.goods-info {
  padding: 16rpx;
}

.goods-title {
  font-size: 28rpx;
  color: var(--text-primary, #1A1A1A);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.goods-bottom {
  margin-bottom: 12rpx;
}

.goods-price {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--accent, #FF6633);
}

.goods-seller {
  display: flex;
  align-items: center;
}

.seller-avatar {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  margin-right: 8rpx;
  background-color: #E8E8E8;
  flex-shrink: 0;
}

.seller-name {
  font-size: 24rpx;
  color: var(--text-hint, #999999);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.want-btn {
  display: flex;
  align-items: center;
  flex-shrink: 0;

  .want-icon {
    font-size: 24rpx;
    color: #ccc;
    margin-right: 4rpx;
  }

  .want-count {
    font-size: 22rpx;
    color: var(--text-hint, #999999);
  }
}

</style>
