import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'DataBoard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'Odometer' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'product',
        name: 'ProductManage',
        component: () => import('../views/product/index.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'category',
        name: 'CategoryManage',
        component: () => import('../views/category/index.vue'),
        meta: { title: '分类管理', icon: 'CollectionTag' }
      },
      {
        path: 'verification',
        name: 'StudentVerifyManage',
        component: () => import('../views/verification/index.vue'),
        meta: { title: '认证信息', icon: 'Postcard' }
      },
      {
        path: 'comment',
        name: 'CommentManage',
        component: () => import('../views/comment/index.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
