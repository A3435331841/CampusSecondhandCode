<template>
  <view class="detail-container">
    <!-- 商品轮播图 -->
    <swiper class="product-swiper" circular indicator-dots indicator-active-color="#1890FF">
      <swiper-item v-for="(img, index) in product.images" :key="index">
        <image :src="img" class="swiper-img" mode="aspectFill" @click="previewImage(index)"></image>
      </swiper-item>
    </swiper>

    <!-- 价格与标题区 -->
    <view class="info-section">
      <view class="price-row">
        <view class="price-left">
          <text class="currency">¥</text>
          <text class="price">{{ product.price }}</text>
          <text class="original-price">原价 ¥{{ product.originalPrice }}</text>
        </view>
        <view class="condition-tag">
          <u-tag :text="product.condition" type="primary" size="mini" plain></u-tag>
        </view>
      </view>
      <view class="title-row">
        <text class="title">{{ product.title }}</text>
      </view>
      <text class="desc">{{ product.desc }}</text>
      <view class="meta-row">
        <text>发布于 2小时前</text>
        <text>128次浏览</text>
      </view>
    </view>

    <!-- 卖家信息卡片 -->
    <view class="seller-card">
      <image :src="product.seller.avatar" class="avatar"></image>
      <view class="seller-info">
        <view class="name-row">
          <text class="name">{{ product.seller.name }}</text>
          <u-tag text="已实名" type="success" size="mini" plain plainFill></u-tag>
        </view>
        <text class="credit">信用极好 | 历史成交 12 笔</text>
      </view>
      <u-button size="small" shape="circle" plain type="primary" text="关注"></u-button>
    </view>

    <!-- 留言区 -->
    <view class="comment-section">
      <view class="section-header">全部留言 (2)</view>
      <view class="comment-item" v-for="i in 2" :key="i">
        <image src="https://api.dicebear.com/7.x/avataaars/svg?seed=User1" class="c-avatar"></image>
        <view class="c-content">
          <text class="c-name">同学A</text>
          <text class="c-text">还能再便宜点吗？诚心要</text>
          <text class="c-time">1小时前</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-icon">
        <u-icon name="heart" size="24" color="#333"></u-icon>
        <text>收藏</text>
      </view>
      <view class="action-icon">
        <u-icon name="chat" size="24" color="#333"></u-icon>
        <text>留言</text>
      </view>
      <view class="btn-group">
        <view class="btn chat-btn">聊一聊</view>
        <view class="btn buy-btn" @click="handleBuy">立即购买</view>
      </view>
    </view>
    
    <u-toast ref="uToast"></u-toast>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const { proxy } = getCurrentInstance()
const uToast = ref(null)

// 商品详情数据，初始为空
const product = ref({
  id: '',
  title: '',
  desc: '',
  price: '',
  originalPrice: '',
  condition: '全新', // 暂时写死
  images: [
    'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80' // 默认占位图
  ],
  seller: {
    name: '加载中...',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=loading'
  }
})

// 获取商品详情
const fetchProductDetail = async (id) => {
  try {
    const res = await proxy.$request({
      url: `/product/detail/${id}`,
      method: 'GET'
    })
    
    if (res) {
      product.value = {
        id: res.id,
        title: res.title,
        desc: res.description,
        price: res.price,
        originalPrice: (res.price * 1.2).toFixed(2), // 模拟原价
        condition: '全新',
        images: [
          'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80'
        ],
        seller: {
          name: '同学_' + res.sellerId,
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + res.sellerId
        }
      }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 页面加载时获取通过 URL 传过来的商品 ID
onLoad((options) => {
  if (options.id) {
    fetchProductDetail(options.id)
  }
})

// 预览图片
const previewImage = (index) => {
  uni.previewImage({
    current: index,
    urls: product.value.images
  })
}

// 立即购买
const handleBuy = () => {
  if (uToast.value) {
    uToast.value.show({
      type: 'loading',
      message: '正在生成订单...',
      duration: 1000,
      complete() {
        uToast.value.show({ type: 'success', message: '下单成功，跳转支付' })
      }
    })
  } else {
    uni.showLoading({ title: '正在生成订单...' })
    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({ title: '下单成功，跳转支付', icon: 'success' })
    }, 1000)
  }
}
</script>

<style lang="scss">
page {
  background-color: #F3F4F6;
}

.detail-container {
  padding-bottom: 100px;
}

.product-swiper {
  height: 375px; /* 1:1 比例 */
  background-color: #fff;
  
  .swiper-img {
    width: 100%;
    height: 100%;
  }
}

.info-section {
  background-color: #fff;
  padding: 15px;
  margin-bottom: 10px;
  
  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    
    .price-left {
      color: #FF4D4F;
      display: flex;
      align-items: baseline;
      
      .currency { font-size: 16px; font-weight: bold; }
      .price { font-size: 28px; font-weight: bold; margin-right: 10px; }
      .original-price { font-size: 12px; color: #999; text-decoration: line-through; }
    }
  }
  
  .title-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 10px;
    
    .title {
      font-size: 16px;
      font-weight: bold;
      color: #333;
      line-height: 1.4;
      flex: 1;
    }
  }
  
  .desc {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    display: block;
    margin-bottom: 15px;
  }
  
  .meta-row {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #999;
  }
}

.seller-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 15px;
  margin-bottom: 10px;
  
  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    margin-right: 12px;
    background-color: #f5f5f5;
  }
  
  .seller-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .name-row {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
      
      .name {
        font-size: 15px;
        font-weight: bold;
        color: #333;
        margin-right: 8px;
      }
    }
    
    .credit {
      font-size: 12px;
      color: #999;
    }
  }
}

.comment-section {
  background-color: #fff;
  padding: 15px;
  
  .section-header {
    font-size: 15px;
    font-weight: bold;
    color: #333;
    margin-bottom: 15px;
  }
  
  .comment-item {
    display: flex;
    margin-bottom: 15px;
    
    .c-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      margin-right: 10px;
      background-color: #f5f5f5;
    }
    
    .c-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      border-bottom: 1px solid #f5f5f5;
      padding-bottom: 15px;
      
      .c-name { font-size: 13px; color: #666; margin-bottom: 4px; }
      .c-text { font-size: 14px; color: #333; margin-bottom: 6px; }
      .c-time { font-size: 11px; color: #999; }
    }
    
    &:last-child .c-content {
      border-bottom: none;
      padding-bottom: 0;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 15px;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
  
  .action-icon {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 20px;
    
    text {
      font-size: 10px;
      color: #666;
      margin-top: 2px;
    }
  }
  
  .btn-group {
    flex: 1;
    display: flex;
    height: 40px;
    border-radius: 20px;
    overflow: hidden;
    margin-left: 10px;
    
    .btn {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 15px;
      font-weight: bold;
      color: #fff;
    }
    
    .chat-btn { background-color: #FFB040; }
    .buy-btn { background-color: #1890FF; }
  }
}
</style>