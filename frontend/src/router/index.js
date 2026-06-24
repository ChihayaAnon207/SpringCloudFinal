import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/public'
  },
  {
    path: '/public',
    name: 'PublicBlogs',
    component: () => import('@/views/PublicBlogs.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my',
    name: 'PersonalBlog',
    component: () => import('@/views/PersonalBlog.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'UserBlog',
    component: () => import('@/views/UserBlog.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/:id',
    name: 'BlogDetail',
    component: () => import('@/views/BlogDetail.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/public')
  } else {
    next()
  }
})

export default router
