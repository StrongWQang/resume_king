import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'LandingPage',
    component: () => import('../views/LandingPageView.vue')
  },
  {
    path: '/editor',
    name: 'ResumeEditor',
    component: () => import('../views/ResumeEditorView.vue')
  },
  {
    path: '/resume-square',
    name: 'ResumeSquare',
    component: () => import('../views/ResumeSquareView.vue')
  },
  {
    path: '/user/resumes',
    name: 'UserResumes',
    component: () => import('../views/UserResumeView.vue'),
    meta: {
      requiresUserAuth: true
    }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfileView.vue'),
    meta: {
      requiresUserAuth: true
    }
  },
  {
    path: '/user/settings',
    name: 'UserSettings',
    component: () => import('../views/UserSettingsView.vue'),
    meta: {
      requiresUserAuth: true
    }
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
  // 检查管理员权限
  if (to.meta.requiresAuth) {
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      ElMessage.error('请先登录管理员账号')
      next('/')
      return
    }
  }
  
  // 检查用户登录权限
  if (to.meta.requiresUserAuth) {
    const userToken = localStorage.getItem('userToken')
    if (!userToken) {
      ElMessage.error('请先登录用户账号')
      next('/')
      return
    }
  }
  
  next()
})

export default router