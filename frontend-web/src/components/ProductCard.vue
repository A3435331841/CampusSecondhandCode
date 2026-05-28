<template>
  <div class="product-card" @click="$router.push(`/detail/${product.id}`)">
    <div class="card-img-wrap">
      <img :src="product.image" :alt="product.title" class="card-img" loading="lazy" @error="e => e.target.src = 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=400&q=80'" />
      <div class="card-overlay"></div>
      <span class="card-tag">{{ product.categoryName }}</span>
    </div>
    <div class="card-info">
      <p class="card-title">{{ product.title }}</p>
      <div class="card-price-row">
        <span class="card-price">¥{{ product.price }}</span>
      </div>
      <div class="card-seller">
        <img :src="product.avatar" class="seller-avatar" />
        <span class="seller-name">{{ product.seller }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: var(--radius-card);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  will-change: transform;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
}

.card-img-wrap {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
  background: #f0f0f0;
}

.card-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover .card-img {
  transform: scale(1.08);
}

/* 底部渐变遮罩 */
.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.35) 0%, transparent 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
  pointer-events: none;
}

.product-card:hover .card-overlay {
  opacity: 1;
}

.card-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 11px;
  color: var(--primary);
  background: rgba(255, 255, 255, 0.9);
  padding: 3px 10px;
  border-radius: 50px;
  font-weight: 500;
  backdrop-filter: blur(4px);
  transition: transform var(--transition-fast);
}

.product-card:hover .card-tag {
  transform: translateY(-2px);
}

.card-info {
  padding: 14px 16px 16px;
}

.card-title {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 0.1px;
}

.card-price-row {
  padding-left: 10px;
  border-left: 3px solid var(--accent);
  margin-bottom: 10px;
  line-height: 1;
}

.card-price {
  font-size: 19px;
  font-weight: 700;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.card-seller {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 1.5px solid #e8e8e8;
}

.seller-name {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
