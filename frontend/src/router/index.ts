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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router