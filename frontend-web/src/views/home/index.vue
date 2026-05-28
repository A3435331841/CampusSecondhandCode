<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <!-- 分类栏 -->
        <CategoryBar v-model="activeCategoryId" :categories="categories" @update:modelValue="onCategoryChange" />

        <!-- 商品区标题 -->
        <div class="section-header">
          <h2 class="section-title">
            <span v-if="searchKeyword">搜索「{{ searchKeyword }}」</span>
            <span v-else>最新发布</span>
          </h2>
          <span v-if="searchKeyword && products.length > 0" class="result-count">共 {{ products.length }} 件</span>
        </div>

        <!-- 加载骨架 -->
        <div v-if="loading" class="product-grid skeleton-grid">
          <div v-for="n in 8" :key="n" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" class="skel-img" />
                <div class="skel-body">
                  <el-skeleton-item variant="text" style="width:90%" />
                  <el-skeleton-item variant="text" style="width:60%;margin-top:8px" />
                  <el-skeleton-item variant="text" style="width:40%;margin-top:8px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 空态 -->
        <div v-else-if="products.length === 0" class="empty-state">
          <div class="empty-icon">🔍</div>
          <p class="empty-title">没有找到相关商品</p>
          <p class="empty-sub">换个关键词试试，或者浏览其他分类</p>
        </div>

        <!-- 商品网格 -->
        <div v-else class="product-grid" ref="gridRef">
          <ProductCard v-for="p in products" :key="p.id" :product="p" />
        </div>
      </div>
    </main>

    <LoginPrompt v-model="showLoginPrompt" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { gsap } from 'gsap'
import AppHeader from '@/components/AppHeader.vue'
import CategoryBar from '@/components/CategoryBar.vue'
import ProductCard from '@/components/ProductCard.vue'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { getProductList } from '@/api/product'
import { getCategoryList } from '@/api/category'

const route = useRoute()
const categories = ref([])
const products = ref([])
const loading = ref(false)
const activeCategoryId = ref(null)
const searchKeyword = ref(route.query.keyword || '')
const showLoginPrompt = ref(false)
const gridRef = ref(null)

const categoryUi = {
  1: '💻', 2: '📚', 3: '👟', 4: '🚲', 5: '🏠',
  6: '📦', 7: '🏀', 8: '💄', 9: '🍪', 10: '🎸',
  11: '🛋️', 12: '🎫'
}

const FALLBACK_IMG = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'

const normalizeImage = (url) => {
  if (!url) return FALLBACK_IMG
  if (/^https?:\/\//.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const mapProduct = (item, catMap) => ({
  id: item.id,
  title: item.title,
  price: item.price,
  image: normalizeImage(item.images?.split(',')[0]?.trim()),
  seller: `卖家${item.sellerId}`,
  avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.sellerId}`,
  categoryName: catMap[item.categoryId] || '其他'
})

const animateGrid = async () => {
  await nextTick()
  const cards = document.querySelectorAll('.product-grid .product-card')
  if (!cards.length) return
  gsap.fromTo(cards,
    { autoAlpha: 0, y: 28, scale: 0.97 },
    { autoAlpha: 1, y: 0, scale: 1, duration: 0.38, stagger: 0.045, ease: 'power2.out', clearProps: 'transform' }
  )
}

const loadCategories = async () => {
  const res = await getCategoryList()
  categories.value = (res || []).map(item => ({
    ...item,
    icon: categoryUi[item.id] || '📦'
  }))
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = { current: 1, size: 40, status: 1 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const res = await getProductList(params)
    const catMap = Object.fromEntries(categories.value.map(c => [c.id, c.name]))
    products.value = (res?.records || []).map(item => mapProduct(item, catMap))
    animateGrid()
  } finally {
    loading.value = false
  }
}

const onCategoryChange = () => {
  searchKeyword.value = ''
  loadProducts()
}

watch(() => route.query.keyword, (kw) => {
  searchKeyword.value = kw || ''
  activeCategoryId.value = null
  loadProducts()
})

onMounted(async () => {
  await loadCategories()
  await loadProducts()
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
  padding: 24px 24px 56px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 20px 0 18px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.3px;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 24px;
  height: 3px;
  background: linear-gradient(90deg, var(--accent), transparent);
  border-radius: 2px;
}

.result-count {
  font-size: 14px;
  color: var(--text-secondary);
  background: #f0f0f0;
  padding: 2px 12px;
  border-radius: 50px;
}

.product-grid,
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .product-grid, .skeleton-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid, .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
}

@media (max-width: 480px) {
  .content-wrap {
    padding: 16px 12px 32px;
  }
}

/* 骨架卡 */
.skeleton-card {
  background: #fff;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.skel-img {
  width: 100%;
  height: 180px;
}

.skel-body {
  padding: 14px 16px 16px;
}

/* 空态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
}

.empty-sub {
  font-size: 14px;
  color: var(--text-secondary);
}
</style>
