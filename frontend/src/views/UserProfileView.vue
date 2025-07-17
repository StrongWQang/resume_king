<template>
  <div class="user-profile-view">
    <div class="page-header">
      <h1>个人资料</h1>
      <p>管理您的个人信息</p>
    </div>

    <div class="page-content">
      <div class="profile-card">
        <div class="profile-header">
          <el-avatar :size="80" :src="userInfo.avatar">
            {{ getAvatarText(userInfo) }}
          </el-avatar>
          <div class="profile-info">
            <h2>{{ userInfo.nickname || userInfo.username || '未知用户' }}</h2>
            <p>{{ userInfo.email || '暂无邮箱' }}</p>
            <div class="user-stats">
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                {{ resumeCount }} 份简历
              </span>
              <span class="stat-item">
                <el-icon><Calendar /></el-icon>
                注册于 {{ formatDate(userInfo.createTime) }}
              </span>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="profile-form">
          <h3>基本信息</h3>
          <el-form 
            :model="formData" 
            :rules="rules" 
            ref="formRef"
            label-width="100px"
          >
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="loading">
                更新资料
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const userInfo = ref<any>({})
const resumeCount = ref(0)
const formRef = ref()

const formData = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  nickname: [
    { min: 1, max: 50, message: '昵称长度在1到50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      router.push('/')
      return
    }

    const response = await fetch('/api/user/info', {
      headers: {
        'Authorization': token
      }
    })

    const result = await response.json()
    if (result.success) {
      userInfo.value = result.user
      resumeCount.value = result.resumeCount || 0
      
      // 填充表单数据
      formData.nickname = result.user.nickname || ''
      formData.email = result.user.email || ''
      formData.phone = result.user.phone || ''
    } else {
      ElMessage.error(result.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户资料
const updateProfile = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  loading.value = true
  
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    const response = await fetch('/api/user/profile', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify(formData)
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success('更新成功')
      
      // 更新本地存储的用户信息
      userInfo.value = result.user
      localStorage.setItem('userInfo', JSON.stringify(result.user))
      
      // 重新获取用户信息
      fetchUserInfo()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户资料失败:', error)
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.nickname = userInfo.value.nickname || ''
  formData.email = userInfo.value.email || ''
  formData.phone = userInfo.value.phone || ''
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取头像文本
const getAvatarText = (user: any) => {
  if (user.avatar) {
    return '' // 如果有头像，则不显示文本
  }
  if (user.nickname) {
    return user.nickname.charAt(0)
  }
  if (user.username) {
    return user.username.charAt(0)
  }
  return 'U' // 默认显示
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-profile-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  background: white;
  padding: 24px;
  border-bottom: 1px solid #e8e8e8;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.page-content {
  padding: 24px;
}

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.profile-info p {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.user-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
}

.profile-form {
  margin-top: 24px;
}

.profile-form h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.el-form {
  max-width: 600px;
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
}
</style> 