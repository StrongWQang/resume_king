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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router