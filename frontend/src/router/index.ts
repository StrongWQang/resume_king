import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'ResumeEditor',
    component: () => import('../views/ResumeEditorView.vue')
  },
  {
    path: '/resume-square',
    name: 'ResumeSquare',
    component: () => import('../views/ResumeSquareView.vue')
  },
  {
    path: '/server-monitor',
    name: 'ServerMonitor',
    component: () => import('../views/ServerMonitorView.vue')
  },
  {
    path: '/approval-management',
    name: 'ApprovalManagement',
    component: () => import('../views/ApprovalManagementView.vue'),
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router