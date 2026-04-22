<template>
  <view class="home-container">
    <!-- 1. 顶部搜索区 -->
    <view class="search-header">
      <view class="search-box">
        <text class="icon-search">🔍</text>
        <input
          type="text"
          v-model="keyword"
          placeholder="搜搜同学们的闲置..."
          class="search-input"
          confirm-type="search"
          @confirm="handleSearch"
        />
        <text v-if="keyword" class="clear-btn" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- 2. 轮播图区 -->
    <view class="banner-section">
      <swiper class="swiper" circular autoplay :interval="3000" indicator-dots indicator-active-color="#1890FF">
        <swiper-item v-for="(item, index) in banners" :key="index">
          <image :src="item" class="banner-img" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
    </view>

    <!-- 3. 分类金刚区 (Grid) -->
    <view class="category-grid">
      <view class="grid-item" v-for="(item, index) in categories" :key="index" @click="handleCategoryClick(item)">
        <view class="icon-wrap" :style="{ backgroundColor: item.color }">
          <text class="emoji-icon">{{ item.icon }}</text>
        </view>
        <text class="category-name">{{ item.name }}</text>
      </view>
    </view>

    <!-- 4. 最新发布推荐 (瀑布流双列) -->
    <view class="recommend-section">
      <view class="section-title">
        <text class="title-text">✨ 最新发布</text>
      </view>
      <view class="product-list">
        <!-- 商品卡片 -->
        <view class="product-card" v-for="(item, index) in products" :key="index" @click="goToDetail(item.id)">
          <image :src="item.image" class="product-img" mode="aspectFill"></image>
          <view class="product-info">
            <text class="product-title">{{ item.title }}</text>
            <view class="price-wrap">
              <text class="currency">¥</text>
              <text class="price">{{ item.price }}</text>
            </view>
            <view class="seller-info">
              <image :src="item.avatar" class="avatar"></image>
              <text class="seller-name">{{ item.seller }}</text>
              <text class="tag">{{ item.condition }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const API_ORIGIN = 'http://localhost:8080'

const normalizeImageUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return `${API_ORIGIN}${url}`
}

// 搜索关键词
const keyword = ref('')

const handleSearch = () => {
  fetchProducts(keyword.value)
}

const clearSearch = () => {
  keyword.value = ''
  fetchProducts()
}

// 模拟轮播图数据
const banners = ref([
  'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&q=80&w=800&h=400',
  'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=800&h=400',
  'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=800&h=400'
])

// 分类数据（id 与后端 category_id 对应）
const categories = ref([
  { id: 1, name: '数码产品', icon: '📱', color: '#E6F7FF' },
  { id: 2, name: '书籍教材', icon: '📚', color: '#F6FFED' },
  { id: 3, name: '衣物鞋帽', icon: '👗', color: '#FFF0F6' },
  { id: 4, name: '代步工具', icon: '🚲', color: '#FFF7E6' },
  { id: 5, name: '生活日用', icon: '🧻', color: '#E6FFFB' },
  { id: 6, name: '其他闲置', icon: '📦', color: '#F5F5F5' }
])

// 处理分类点击 - 按分类筛选
const handleCategoryClick = (item) => {
  keyword.value = ''
  fetchProducts('', item.id)
}

// 跳转到商品详情页
const goToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${id}`
  })
}

// 商品列表（初始为空，将从后端获取）
const products = ref([])

// 从后端获取真实商品数据
const fetchProducts = async (kw = '', categoryId = null) => {
  try {
    const params = { current: 1, size: 20, status: 1 }
    if (kw) params.keyword = kw
    if (categoryId) params.categoryId = categoryId

    const res = await proxy.$request({ url: '/product/list', method: 'GET', data: params })

    if (res && res.records) {
      products.value = res.records.map(item => ({
        id: item.id,
        title: item.title,
        price: item.price,
        // 取第一张图，如无则用默认占位图
        image: item.images
          ? normalizeImageUrl(item.images.split(',')[0].trim())
          : 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80',
        seller: '同学_' + item.sellerId,
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + item.sellerId,
        condition: '二手'
      }))
    } else {
      products.value = []
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 页面加载时请求数据
onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss">
page {
  background-color: #F3F4F6;
}

.home-container {
  padding-bottom: 20px;
}

/* 顶部搜索区 */
.search-header {
  background-color: #1890FF;
  padding: 10px 15px 15px;
  position: sticky;
  top: 0;
  z-index: 99;
  
  .search-box {
    background-color: #ffffff;
    border-radius: 20px;
    height: 36px;
    display: flex;
    align-items: center;
    padding: 0 15px;
    
    .icon-search {
      font-size: 16px;
      margin-right: 8px;
    }
    
    .search-input {
      flex: 1;
      font-size: 14px;
      color: #333;
    }

    .clear-btn {
      font-size: 14px;
      color: #999;
      padding: 0 4px;
    }
  }
}

/* 轮播图 */
.banner-section {
  padding: 10px 15px;
  background-color: #fff;
  
  .swiper {
    height: 140px;
    border-radius: 12px;
    overflow: hidden;
    transform: translateY(0); /* 修复部分机型圆角失效 */
    
    .banner-img {
      width: 100%;
      height: 100%;
    }
  }
}

/* 金刚区 */
.category-grid {
  display: flex;
  flex-wrap: wrap;
  background-color: #fff;
  padding: 15px 5px;
  margin-bottom: 12px;
  
  .grid-item {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 15px;
    
    .icon-wrap {
      width: 44px;
      height: 44px;
      border-radius: 16px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 6px;
      
      .emoji-icon {
        font-size: 24px;
      }
    }
    
    .category-name {
      font-size: 12px;
      color: #333;
    }
  }
}

/* 最新发布推荐 */
.recommend-section {
  padding: 0 10px;
  
  .section-title {
    padding: 10px 5px;
    margin-bottom: 5px;
    
    .title-text {
      font-size: 16px;
      font-weight: bold;
      color: #333;
    }
  }
  
  .product-list {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    
    .product-card {
      width: 48%;
      background-color: #fff;
      border-radius: 10px;
      margin-bottom: 15px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      
      .product-img {
        width: 100%;
        height: 160px;
        background-color: #f5f5f5;
      }
      
      .product-info {
        padding: 10px;
        
        .product-title {
          font-size: 13px;
          color: #333;
          line-height: 1.4;
          height: 36px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          margin-bottom: 8px;
        }
        
        .price-wrap {
          color: #FF4D4F;
          font-weight: bold;
          margin-bottom: 8px;
          
          .currency {
            font-size: 12px;
            margin-right: 2px;
          }
          .price {
            font-size: 16px;
          }
        }
        
        .seller-info {
          display: flex;
          align-items: center;
          
          .avatar {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            margin-right: 6px;
            background-color: #eee;
          }
          
          .seller-name {
            font-size: 11px;
            color: #999;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .tag {
            font-size: 10px;
            color: #1890FF;
            background-color: #E6F7FF;
            padding: 2px 4px;
            border-radius: 4px;
          }
        }
      }
    }
  }
}
</style>
