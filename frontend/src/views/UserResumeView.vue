<template>
  <div class="user-resume-view">
    <div class="page-header">
      <h1>我的简历</h1>
      <p>管理您的所有简历</p>
    </div>

    <div class="page-content">
      <!-- 统计信息 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ userResumes.length }}</div>
            <div class="stat-label">简历总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ draftCount }}</div>
            <div class="stat-label">草稿</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ publishedCount }}</div>
            <div class="stat-label">已发布</div>
          </div>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="createNewResume">
          <el-icon><Plus /></el-icon>
          新建简历
        </el-button>
        <div class="toolbar-right">
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="0" />
            <el-option label="已发布" value="1" />
          </el-select>
          <el-button @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 简历列表 -->
      <div class="resume-list" v-loading="loading">
        <div v-if="filteredResumes.length === 0" class="empty-state">
          <el-empty description="暂无简历">
            <el-button type="primary" @click="createNewResume">创建第一份简历</el-button>
          </el-empty>
        </div>
        <div v-else class="resume-grid">
          <div 
            v-for="resume in filteredResumes" 
            :key="resume.id" 
            class="resume-card"
            @click="editResume(resume)"
          >
            <div class="resume-preview">
              <div class="resume-thumbnail">
                <el-icon size="40"><Document /></el-icon>
              </div>
            </div>
            <div class="resume-info">
              <h3 class="resume-title">{{ resume.title || '未命名简历' }}</h3>
              <div class="resume-meta">
                <span class="status" :class="getStatusClass(resume.status)">
                  {{ getStatusText(resume.status) }}
                </span>
                <span class="date">{{ formatDate(resume.updateTime) }}</span>
              </div>
              <div class="resume-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ resume.likeCount || 0 }}
                </span>
              </div>
            </div>
            <div class="resume-actions">
              <el-button-group>
                <el-button size="small" @click.stop="editResume(resume)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" @click.stop="previewResume(resume)">
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click.stop="deleteResume(resume)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  Edit, 
  Check, 
  Plus, 
  Refresh, 
  View, 
  Delete 
} from '@element-plus/icons-vue'

interface Resume {
  id: string
  title: string
  status: number
  createTime: string
  updateTime: string
  likeCount: number
  content: string
}

const router = useRouter()
const loading = ref(false)
const userResumes = ref<Resume[]>([])
const statusFilter = ref('')

// 计算属性
const filteredResumes = computed(() => {
  if (!statusFilter.value) return userResumes.value
  return userResumes.value.filter(resume => resume.status.toString() === statusFilter.value)
})

const draftCount = computed(() => {
  return userResumes.value.filter(resume => resume.status === 0).length
})

const publishedCount = computed(() => {
  return userResumes.value.filter(resume => resume.status === 1).length
})

// 获取用户简历列表
const fetchUserResumes = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      router.push('/')
      return
    }

    const response = await fetch('/api/resumes/user/list', {
      headers: {
        'Authorization': token
      }
    })

    const result = await response.json()
    if (result.success) {
      userResumes.value = result.resumes || []
    } else {
      ElMessage.error(result.message || '获取简历列表失败')
    }
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchUserResumes()
}

// 创建新简历
const createNewResume = () => {
  router.push('/')
}

// 编辑简历
const editResume = (resume: Resume) => {
  // 跳转到编辑页面并加载该简历
  router.push(`/?resumeId=${resume.id}`)
}

// 预览简历
const previewResume = (resume: Resume) => {
  // 在新窗口中打开预览
  window.open(`/preview/${resume.id}`, '_blank')
}

// 删除简历
const deleteResume = async (resume: Resume) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除简历"${resume.title || '未命名简历'}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    const response = await fetch(`/api/resumes/${resume.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': token
      }
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success('删除成功')
      fetchUserResumes() // 重新获取列表
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除简历失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态相关方法
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '草稿'
    case 1: return '已发布'
    case 2: return '已归档'
    default: return '未知'
  }
}

const getStatusClass = (status: number) => {
  switch (status) {
    case 0: return 'draft'
    case 1: return 'published'
    case 2: return 'archived'
    default: return 'unknown'
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserResumes()
})
</script>

<style scoped>
.user-resume-view {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f0f9ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  color: #409eff;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.resume-list {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.resume-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resume-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.resume-preview {
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.resume-thumbnail {
  color: #999;
}

.resume-info {
  margin-bottom: 16px;
}

.resume-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status.draft {
  background: #fff7e6;
  color: #fa8c16;
}

.status.published {
  background: #f6ffed;
  color: #52c41a;
}

.status.archived {
  background: #f5f5f5;
  color: #999;
}

.date {
  color: #666;
  font-size: 12px;
}

.resume-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 12px;
}

.resume-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.resume-actions .el-button-group {
  width: 100%;
}

.resume-actions .el-button {
  flex: 1;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .resume-grid {
    grid-template-columns: 1fr;
  }
}
</style> 