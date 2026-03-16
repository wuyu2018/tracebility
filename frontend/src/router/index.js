import { createRouter, createWebHistory } from 'vue-router'

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
  },
  {
    path: '/ToolsStandalone',
    name: 'ToolsStandalone',
    component: () => import('../views/ToolsStandalone.vue'),
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router