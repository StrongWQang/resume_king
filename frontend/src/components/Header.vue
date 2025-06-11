<template>
  <header class="header">
    <div class="logo">简历制作</div>
    <div class="actions">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="handleLoad">加载</el-button>
      <el-button type="success" @click="handleExport">导出PDF</el-button>
      <el-button @click="handlePreview">预览</el-button>
    </div>

    <!-- 加载简历对话框 -->
    <el-dialog v-model="loadDialogVisible" title="加载简历" width="30%">
      <el-input v-model="resumeId" placeholder="请输入简历ID" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="loadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmLoad">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useResumeStore } from '../store/resume'

const store = useResumeStore()
const loadDialogVisible = ref(false)
const resumeId = ref('')

const handleSave = async () => {
  try {
    const id = await store.saveResume()
    ElMessage.success(`保存成功，简历ID：${id}`)
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleLoad = () => {
  loadDialogVisible.value = true
}

const confirmLoad = async () => {
  if (!resumeId.value) {
    ElMessage.warning('请输入简历ID')
    return
  }
  try {
    await store.loadResume(resumeId.value)
    loadDialogVisible.value = false
    ElMessage.success('加载成功')
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const handleExport = async () => {
  try {
    const response = await fetch('/api/resumes/download-pdf', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(store.components)
    })
    
    if (!response.ok) {
      throw new Error('导出失败')
    }
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'resume.pdf'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handlePreview = () => {
  // TODO: 实现预览功能
  ElMessage.info('预览功能开发中')
}
</script>

<style scoped>
.header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style> 