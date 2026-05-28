<template>
  <view class="tabbar-placeholder"></view>
  <view class="tabbar-wrap">
    <view class="tabbar">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tabbar-item"
        :class="{ active: current === index }"
        @click="switchTab(index)"
      >
        <view v-if="tab.raised" class="raised-btn">
          <text class="raised-icon">+</text>
        </view>
        <view v-else class="tab-inner">
          <view class="icon-box">
            <text class="tab-icon">{{ current === index ? tab.activeIcon : tab.icon }}</text>
          </view>
          <text class="tab-label" :class="{ 'label-active': current === index }">{{ tab.text }}</text>
          <view v-if="current === index" class="active-dot"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  current: { type: Number, default: 0 }
})

const tabs = [
  { text: '首页', icon: '🏠', activeIcon: '🏠', path: '/pages/index/index' },
  { text: 'AI', icon: '🔮', activeIcon: '🔮', path: '/pages/ai-search/ai-search' },
  { text: '发布', icon: '+', activeIcon: '+', path: '/pages/publish/publish', raised: true },
  { text: '消息', icon: '💬', activeIcon: '💬', path: '/pages/messages/messages' },
  { text: '我的', icon: '👤', activeIcon: '👤', path: '/pages/profile/profile' }
]

const switchTab = (index) => {
  if (index === props.current) return
  uni.switchTab({ url: tabs[index].path })
}
</script>

<style lang="scss" scoped>
.tabbar-placeholder {
  height: calc(120rpx + env(safe-area-inset-bottom));
}

.tabbar-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.tabbar {
  display: flex;
  align-items: flex-end;
  height: calc(110rpx + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.06);
  border-radius: 28rpx 28rpx 0 0;
}

.tabbar-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 110rpx;
  position: relative;
}

.tab-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon-box {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
}

.tab-icon {
  font-size: 42rpx;
  line-height: 1;
  transition: transform 0.2s ease;
}

.tabbar-item.active .tab-icon {
  transform: scale(1.15);
}

.tab-label {
  font-size: 20rpx;
  color: #999;
  line-height: 1;
  transition: color 0.2s ease;
}

.label-active {
  color: #00B578;
  font-weight: 600;
}

.active-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #00B578;
  margin-top: 4rpx;
}

/* Center raised button */
.raised-btn {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #00B578, #00D68F);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 181, 120, 0.35);
  position: relative;
}

.raised-btn::after {
  content: '发布';
  position: absolute;
  bottom: -32rpx;
  font-size: 20rpx;
  color: #00B578;
  font-weight: 600;
  white-space: nowrap;
}

.raised-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
  margin-top: -4rpx;
}
</style>
