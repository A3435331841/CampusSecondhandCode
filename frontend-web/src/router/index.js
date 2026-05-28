import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/views/home/index.vue')
  },
  {
    path: '/detail/:id',
    component: () => import('@/views/detail/index.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    component: () => import('@/views/orders/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    component: () => import('@/views/favorites/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/publish',
    component: () => import('@/views/publish/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai',
    component: () => import('@/views/ai/index.vue')
  },
  {
    path: '/messages',
    component: () => import('@/views/messages/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    component: () => import('@/views/chat/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/verify',
    component: () => import('@/views/verify/index.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const user = useUserStore()
  if (to.meta.requiresAuth && !user.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
