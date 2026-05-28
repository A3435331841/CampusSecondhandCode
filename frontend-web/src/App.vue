<template>
  <router-view v-slot="{ Component }">
    <Transition name="page" mode="out-in">
      <component :is="Component" />
    </Transition>
  </router-view>
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #F7F8FA;
  color: #222;
}

:root {
  --primary: #1890ff;
  --accent: #FF6600;
  --success: #52c41a;
  --warning: #faad14;
  --bg: #F7F8FA;
  --card-bg: #fff;
  --text-primary: #222;
  --text-secondary: #999;
  --border: #EFEFEF;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.10);
  --shadow-hover: 0 16px 40px rgba(0, 0, 0, 0.13);
  --header-height: 64px;
  --radius-card: 16px;
  --radius-btn: 50px;
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-base: 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.38s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 路由切换过渡 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
