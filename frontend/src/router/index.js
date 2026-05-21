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
      },
      {
        path: 'materials/varieties',
        name: 'MaterialVarieties',
        component: () => import('../views/materials/Varieties.vue'),
      },
      {
        path: 'materials/purchases',
        name: 'MaterialPurchases',
        component: () => import('../views/materials/Purchases.vue'),
      },
      {
        path: 'production/batches',
        name: 'ProductionBatches',
        component: () => import('../views/production/Batches.vue'),
      },
      {
        path: 'production/inspections',
        name: 'ProductionInspections',
        component: () => import('../views/production/Inspections.vue'),
      },
      {
        path: 'circulation/storage',
        name: 'CirculationStorage',
        component: () => import('../views/circulation/Storage.vue'),
      },
      {
        path: 'circulation/transport',
        name: 'CirculationTransport',
        component: () => import('../views/circulation/Transport.vue'),
      },
      {
        path: 'sales/records',
        name: 'SalesRecords',
        component: () => import('../views/sales/Records.vue'),
      },
      {
        path: 'qrcodes',
        name: 'Qrcodes',
        component: () => import('../views/Qrcodes.vue'),
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
    if (!isAuthenticated()) {
      next({
        path: '/admin',
        query: { redirect: to.fullPath },
      })
      return
    }
  }
  
  if (to.path === '/admin' && isAuthenticated()) {
    next({ path: '/dashboard' })
    return
  }
  
  next()
})

export default router
