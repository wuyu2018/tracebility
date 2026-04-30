import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  {
    path: '/verify',
    name: 'Verify',
    component: () => import('../views/Verify.vue'),
  },
  {
    path: '/purchase',
    name: 'Purchase',
    component: () => import('../views/Purchase.vue'),
  },
  {
    path: '/intro',
    name: 'Intro',
    component: () => import('../views/Intro.vue'),
  },
  {
    path: '/complaint',
    name: 'Complaint',
    component: () => import('../views/Complaint.vue'),
  },
  {
    path: '/getAllComplaintInfo',
    name: 'getAllComplaintInfo',
    component: () => import('../views/getAllComplaintInfo.vue'),
    meta: { requiresAuth: true } // 需要认证
  },
  {
    path: '/ToolsStandalone',
    name: 'ToolsStandalone',
    component: () => import('../views/ToolsStandalone.vue'),
  },
  {
    path: '/Admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要认证
  const requiresAuth = to.meta.requiresAuth || false
  
  if (requiresAuth) {
    const auth = isAuthenticated()
    
    if (!auth) {
      // 未登录，重定向到登录页
      next({
        path: '/ToolsStandalone',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  // 如果已登录且访问登录页，重定向到管理后台
  if (to.path === '/Admin') {
    const auth = isAuthenticated()
    if (auth) {
      next({ path: '/ToolsStandalone' })
      return
    }
  }
  
  next()
})

export default router
