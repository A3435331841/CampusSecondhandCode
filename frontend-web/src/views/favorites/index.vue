<template>
  <div class="page-layout">
    <AppHeader />
    <main class="main-content">
      <div class="content-wrap">
        <h2 class="page-title">我的收藏</h2>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="4" animated />
        </div>

        <el-empty v-else-if="list.length === 0" description="还没有收藏" />

        <div v-else class="fav-grid">
          <div
            v-for="item in list"
            :key="item.id"
            class="fav-card"
            @click="$router.push(`/detail/${item.productId}`)"
          >
            <img :src="normalizeImage(item.productImage)" class="fav-img" @error="e => e.target.src = FALLBACK" />
            <div class="fav-info">
              <p class="fav-title">{{ item.productTitle }}</p>
              <span class="fav-price">¥{{ item.productPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { getFavoriteList } from '@/api/favorite'

const FALLBACK = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'

const list = ref([])
const loading = ref(false)

const normalizeImage = (url) => {
  if (!url) return FALLBACK
  const first = url.split(',')[0].trim()
  if (/^https?:\/\//.test(first)) return first
  return first.startsWith('/') ? first : `/${first}`
}

onMounted(async () => {
  loading.value = true
  try {
    list.value = await getFavoriteList() || []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-layout { min-height: 100vh; background: var(--bg); }
.main-content { padding-top: var(--header-height); }
.content-wrap { max-width: 1000px; margin: 0 auto; padding: 28px 24px 56px; }

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.loading-wrap { padding: 40px 0; }

.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

@media (max-width: 768px) {
  .fav-grid { grid-template-columns: repeat(2, 1fr); }
}

.fav-card {
  background: #fff;
  border-radius: var(--radius-card, 14px);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.fav-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.fav-img {
  width: 100%;
  aspect-ratio: 4/3;
  object-fit: cover;
  background: #f0f0f0;
}

.fav-info { padding: 14px 16px 16px; }
.fav-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fav-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}
</style>
