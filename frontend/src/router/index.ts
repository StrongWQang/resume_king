import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    component: () => import('../views/ServerMonitorView.vue'),
    meta: {
      requiresAuth: true
    }
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

// 添加路由守卫
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    // 检查是否有管理员token
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      ElMessage.error('请先登录管理员账号')
      next('/')
      return
    }
  }
  next()
})

export default router