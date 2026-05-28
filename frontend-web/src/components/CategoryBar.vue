<template>
  <div class="category-wrap">
    <div class="category-bar" ref="barRef">
      <div
        v-for="(item, index) in allItems"
        :key="item.id ?? 'all'"
        :ref="el => { if (el) itemRefs[index] = el }"
        class="category-item"
        :class="{ active: item.id === modelValue }"
        @click="handleClick(item.id)"
      >
        <span class="cat-icon">{{ item.icon }}</span>
        <span class="cat-name">{{ item.name }}</span>
      </div>
      <!-- 滑动活跃指示器 -->
      <div class="slide-indicator" :style="indicatorStyle"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  modelValue: { type: Number, default: null }
})
const emit = defineEmits(['update:modelValue'])

const barRef = ref(null)
const itemRefs = ref([])
const indicatorStyle = ref({ width: '0px', transform: 'translateX(0px)', opacity: 0 })

const allItems = computed(() => [
  { id: null, icon: '🏷️', name: '全部' },
  ...props.categories
])

const updateIndicator = async () => {
  await nextTick()
  const activeIndex = allItems.value.findIndex(item => item.id === props.modelValue)
  const el = itemRefs.value[activeIndex]
  if (!el || !barRef.value) return
  const barRect = barRef.value.getBoundingClientRect()
  const elRect = el.getBoundingClientRect()
  const scrollLeft = barRef.value.scrollLeft
  const left = elRect.left - barRect.left + scrollLeft
  indicatorStyle.value = {
    width: `${elRect.width}px`,
    transform: `translateX(${left}px)`,
    opacity: 1
  }
}

const handleClick = (id) => {
  emit('update:modelValue', id)
}

watch(() => props.modelValue, updateIndicator)
watch(() => props.categories, () => nextTick(updateIndicator), { deep: true })
onMounted(updateIndicator)
</script>

<style scoped>
.category-wrap {
  background: #fff;
  border-radius: var(--radius-card);
  padding: 12px 16px;
  box-shadow: var(--shadow-sm);
  margin-bottom: 4px;
}

.category-bar {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding-bottom: 6px;
  scrollbar-width: none;
  position: relative;
}

.category-bar::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 10px 18px;
  border-radius: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition: background var(--transition-fast), color var(--transition-fast), transform var(--transition-fast);
  position: relative;
}

.category-item:hover {
  background: #f0f7ff;
  transform: translateY(-1px);
}

.category-item.active {
  background: #EDF5FF;
}

.category-item:active {
  transform: scale(0.95);
}

.cat-icon {
  font-size: 22px;
  transition: transform var(--transition-fast);
}

.category-item.active .cat-icon {
  transform: scale(1.15);
}

.cat-name {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.category-item.active .cat-name {
  color: var(--primary);
  font-weight: 600;
}

/* 底部滑动指示器 */
.slide-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), #36cfc9);
  border-radius: 2px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), width 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.2s;
  pointer-events: none;
}
</style>
