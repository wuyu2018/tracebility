import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'
import { hasRoutePermission, getDefaultHomeByRole } from '../utils/auth'

function hasValidToken() {
  if (!isAuthenticated()) return false

  const expiresIn = Number(localStorage.getItem('expiresIn') || '0')
  const loginAt = Number(localStorage.getItem('loginAt') || '0')
  if (!expiresIn || !loginAt) return true

  return Date.now() < loginAt + expiresIn
}

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
    path: '/complaint',
    name: 'Complaint',
    component: () => import('../views/Complaint.vue'),
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
  },
  {
    path: '/',
    name: 'AdminLayout',
    component: () => import('../views/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'materials/varieties',
        name: 'MaterialVarieties',
        component: () => import('../views/materials/Varieties.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'materials/purchases',
        name: 'MaterialPurchases',
        component: () => import('../views/materials/Purchases.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'production/batches',
        name: 'ProductionBatches',
        component: () => import('../views/production/Batches.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'production/inspections',
        name: 'ProductionInspections',
        component: () => import('../views/production/Inspections.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'circulation/storage',
        name: 'CirculationStorage',
        component: () => import('../views/circulation/Storage.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'circulation/transport',
        name: 'CirculationTransport',
        component: () => import('../views/circulation/Transport.vue'),
        meta: { agentTypes: ['CIRCULATION', 'SALES'] },
      },
      {
        path: 'sales/records',
        name: 'SalesRecords',
        component: () => import('../views/sales/Records.vue'),
        meta: { agentTypes: ['CIRCULATION', 'SALES'] },
      },
      {
        path: 'qrcodes',
        name: 'Qrcodes',
        component: () => import('../views/Qrcodes.vue'),
        meta: { agentTypes: ['PRODUCTION'] },
      },
      {
        path: 'complaints',
        name: 'Complaints',
        component: () => import('../views/Complaints.vue'),
      },
      {
        path: 'blockchain',
        name: 'Blockchain',
        component: () => import('../views/Blockchain.vue'),
      },
      {
        path: 'agents',
        name: 'Agents',
        component: () => import('../views/Agents.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth || false
  
  if (requiresAuth) {
    if (!hasValidToken()) {
      localStorage.removeItem('token')
      localStorage.removeItem('tokenType')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('agentType')
      localStorage.removeItem('expiresIn')
      localStorage.removeItem('loginAt')
      next({
        path: '/admin',
        query: { redirect: to.fullPath },
      })
      return
    }
  }
  
  if (to.path === '/admin' && hasValidToken()) {
    next({ path: getDefaultHomeByRole() })
    return
  }

  if (requiresAuth) {
    const allowedAgentTypes = to.meta.agentTypes || []
    if (!hasRoutePermission(allowedAgentTypes)) {
      next({ path: getDefaultHomeByRole() })
      return
    }
  }
  
  next()
})

export default router
