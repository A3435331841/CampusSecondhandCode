<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap" v-if="product">
        <!-- 面包屑 -->
        <el-breadcrumb class="breadcrumb" separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>商品详情</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="detail-layout reveal" ref="detailRef">
          <!-- 左侧图片 -->
          <div class="img-section">
            <el-carousel height="440px" indicator-position="outside" class="product-carousel">
              <el-carousel-item v-for="(img, i) in product.images" :key="i">
                <img :src="img" class="carousel-img" @click="previewImages(i)" @error="e => e.target.src = FALLBACK_IMG" />
              </el-carousel-item>
            </el-carousel>
          </div>

          <!-- 右侧信息 -->
          <div class="info-section">
            <div class="price-block">
              <span class="price-label">售价</span>
              <span class="price">¥{{ product.price }}</span>
            </div>
            <h1 class="product-title">{{ product.title }}</h1>
            <p class="product-desc" v-if="product.description">{{ product.description }}</p>

            <!-- 面交地点 -->
            <div class="location-block" v-if="product.pickupPlaceName">
              <el-icon class="location-pin"><Location /></el-icon>
              <div class="location-text">
                <span class="location-name">{{ product.pickupPlaceName }}</span>
                <span class="location-addr" v-if="product.pickupAddress">{{ product.pickupAddress }}</span>
              </div>
            </div>

            <!-- 卖家 -->
            <div class="seller-block">
              <el-avatar :size="48" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${product.sellerId}`" class="seller-avatar-img" />
              <div class="seller-meta">
                <span class="seller-name">卖家{{ product.sellerId }}</span>
                <el-tag type="success" size="small" round>信用良好</el-tag>
              </div>
              <el-button round @click="requireLogin('chat')" class="chat-btn">💬 聊一聊</el-button>
            </div>

            <!-- 操作按钮 -->
            <div class="action-row">
              <button
                class="fav-btn"
                :class="{ favorited }"
                @click="toggleFavorite"
              >
                <span>{{ favorited ? '❤️' : '🤍' }}</span>
                {{ favorited ? '已收藏' : '收藏' }}
                <span class="fav-count">{{ product.favoriteCount }}</span>
              </button>
              <button class="buy-btn" @click="requireLogin('buy')">
                我想要
              </button>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section reveal" ref="commentRef">
          <h2 class="comment-title">
            <span>留言</span>
            <span class="comment-count">{{ comments.length }}</span>
          </h2>
          <div class="comment-input-row">
            <el-input
              v-model="commentText"
              placeholder="写下你的留言..."
              maxlength="200"
              show-word-limit
              class="comment-input"
              @keyup.enter="submitComment"
            />
            <el-button type="primary" round @click="submitComment" class="send-btn">发送</el-button>
          </div>
          <div class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <el-avatar :size="38" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${c.userId}`" />
              <div class="comment-body">
                <span class="comment-user">用户{{ c.userId }}</span>
                <p class="comment-content">{{ c.content }}</p>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="还没有留言，来说点什么吧" :image-size="80" />
          </div>
        </div>
      </div>

      <div v-else class="loading-wrap">
        <el-skeleton :rows="8" animated />
      </div>
    </main>

    <LoginPrompt v-model="showLoginPrompt" :message="loginPromptMsg" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { getProductDetail } from '@/api/product'
import { getCommentList, addComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const user = useUserStore()
const product = ref(null)
const comments = ref([])
const favorited = ref(false)
const commentText = ref('')
const showLoginPrompt = ref(false)
const loginPromptMsg = ref('该功能需要登录后使用')
const detailRef = ref(null)
const commentRef = ref(null)

const FALLBACK_IMG = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80'

const normalizeImage = (url) => {
  if (!url) return FALLBACK_IMG
  if (/^https?:\/\//.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const useReveal = (el) => {
  if (!el) return
  const observer = new IntersectionObserver(
    ([entry]) => { if (entry.isIntersecting) { el.classList.add('visible'); observer.disconnect() } },
    { threshold: 0.08 }
  )
  observer.observe(el)
}

const requireLogin = (action) => {
  if (!user.isLoggedIn) {
    loginPromptMsg.value = action === 'chat' ? '联系卖家需要登录' : action === 'buy' ? '购买商品需要登录' : '该功能需要登录'
    showLoginPrompt.value = true
    return false
  }
  return true
}

const previewImages = (idx) => {
  window.open(product.value.images[idx], '_blank')
}

const toggleFavorite = async () => {
  if (!requireLogin('favorite')) return
  favorited.value = !favorited.value
  product.value.favoriteCount += favorited.value ? 1 : -1
  ElMessage.success(favorited.value ? '已收藏' : '已取消收藏')
}

const submitComment = async () => {
  if (!requireLogin('comment')) return
  const content = commentText.value.trim()
  if (!content) { ElMessage.warning('请输入留言内容'); return }
  try {
    await addComment(product.value.id, content)
    comments.value.unshift({ id: Date.now(), userId: user.userInfo?.userId || '我', content })
    commentText.value = ''
    ElMessage.success('留言成功')
  } catch {}
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getProductDetail(id)
    product.value = {
      ...res,
      images: res.images
        ? res.images.split(',').map(u => normalizeImage(u.trim())).filter(Boolean)
        : [normalizeImage(null)],
      favoriteCount: res.favoriteCount || 0,
      commentCount: res.commentCount || 0
    }
    const coms = await getCommentList(id)
    comments.value = coms || []
    // 挂载后延一帧再绑定 observer，确保 DOM 已渲染
    setTimeout(() => {
      useReveal(detailRef.value)
      useReveal(commentRef.value)
    }, 50)
  } catch {}
})
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  background: var(--bg);
}

.main-content {
  padding-top: var(--header-height);
}

.content-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.loading-wrap {
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 24px;
}

.breadcrumb {
  margin-bottom: 24px;
}

/* 滚动入场 */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.reveal.visible {
  opacity: 1;
  transform: none;
}

.detail-layout {
  display: flex;
  gap: 48px;
  margin-bottom: 40px;
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow);
}

.img-section {
  flex: 0 0 54%;
}

.product-carousel :deep(.el-carousel__container) {
  border-radius: 14px;
  overflow: hidden;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  cursor: zoom-in;
  background: #f5f5f5;
  transition: transform 0.3s ease;
}

.carousel-img:hover {
  transform: scale(1.02);
}

.info-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.price-block {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 14px;
}

.price-label {
  font-size: 13px;
  color: #aaa;
  font-weight: 400;
}

.price {
  font-size: 40px;
  font-weight: 700;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.45;
  margin-bottom: 14px;
  letter-spacing: 0.2px;
}

.product-desc {
  font-size: 15px;
  color: #666;
  line-height: 1.7;
  margin-bottom: 20px;
  background: #fafafa;
  padding: 14px 16px;
  border-radius: 12px;
  border-left: 3px solid #e8e8e8;
}

.location-block {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
  color: #666;
  background: #f0f7ff;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid rgba(24, 144, 255, 0.12);
}

.location-pin {
  color: var(--primary);
  margin-top: 1px;
  flex-shrink: 0;
}

.location-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.location-name {
  font-weight: 500;
  color: #333;
}

.location-addr {
  color: #999;
  font-size: 13px;
}

.seller-block {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: #f9fafb;
  border-radius: 16px;
  margin-bottom: 24px;
  border: 1px solid var(--border);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.seller-block:hover {
  border-color: rgba(24, 144, 255, 0.3);
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.06);
}

.seller-avatar-img {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.seller-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.chat-btn {
  font-weight: 500;
}

/* 操作按钮行 */
.action-row {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 8px;
}

.fav-btn {
  flex: 1;
  height: 52px;
  background: #fff;
  border: 1.5px solid #e8e8e8;
  border-radius: var(--radius-btn);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all var(--transition-fast);
}

.fav-btn:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.08);
}

.fav-btn.favorited {
  background: #fff1f0;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.fav-count {
  font-size: 12px;
  color: inherit;
  opacity: 0.7;
}

.buy-btn {
  flex: 2;
  height: 52px;
  background: linear-gradient(135deg, #FF6600, #FF8533);
  border: none;
  border-radius: var(--radius-btn);
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  letter-spacing: 1px;
  box-shadow: 0 8px 24px rgba(255, 102, 0, 0.3);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.buy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(255, 102, 0, 0.4);
}

.buy-btn:active {
  transform: scale(0.97);
  box-shadow: 0 4px 12px rgba(255, 102, 0, 0.3);
}

/* 评论区 */
.comment-section {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow);
}

.comment-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-count {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: var(--primary);
  padding: 1px 10px;
  border-radius: 50px;
}

.comment-input-row {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
}

.comment-input :deep(.el-input__wrapper) {
  border-radius: 50px;
}

.send-btn {
  flex-shrink: 0;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 12px;
  border-radius: 12px;
  transition: background var(--transition-fast);
}

.comment-item:not(:last-child) {
  border-bottom: 1px solid var(--border);
}

.comment-item:hover {
  background: #fafafa;
}

.comment-body {
  flex: 1;
}

.comment-user {
  font-size: 13px;
  color: var(--text-secondary);
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
}

.comment-content {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.55;
}

@media (max-width: 768px) {
  .detail-layout {
    flex-direction: column;
    padding: 20px;
  }

  .img-section {
    flex: none;
  }

  .action-row {
    flex-wrap: wrap;
  }
}
</style>
