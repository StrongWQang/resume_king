<template>
  <header class="header">
    <div class="logo">简历制作</div>
    <div class="actions">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="handleLoad">加载</el-button>
      <el-button type="success" @click="handleExport">导出PDF</el-button>
      <el-button @click="handlePreview">预览</el-button>
      <el-button type="warning" @click="handleExportTemplate">导出模板</el-button>
      <el-button type="info" @click="handleImportTemplate">导入模板</el-button>
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

    <!-- 导入模板对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入模板" width="30%">
      <el-upload
        class="upload-demo"
        drag
        action="/api/templates/import"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImportUpload"
        accept=".json"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 json 文件
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useResumeStore } from '../store/resume'
import { UploadFilled } from '@element-plus/icons-vue'

const store = useResumeStore()
const loadDialogVisible = ref(false)
const resumeId = ref('')
const importDialogVisible = ref(false)

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
    // 确保所有组件数据都是最新的
    store.updateSelectedComponent()
    
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
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleExportTemplate = async () => {
  try {
    const response = await fetch('/api/templates/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(store.components)
    })
    
    if (!response.ok) {
      throw new Error('导出模板失败')
    }
    
    const templateData = await response.json()
    const blob = new Blob([JSON.stringify(templateData, null, 2)], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'resume-template.json'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
    ElMessage.success('模板导出成功')
  } catch (error) {
    ElMessage.error('模板导出失败')
  }
}

const handlePreview = () => {
  // TODO: 实现预览功能
  ElMessage.info('预览功能开发中')
}

const handleImportTemplate = () => {
  importDialogVisible.value = true
}

const handleImportSuccess = (response: any) => {
  try {
    if (!Array.isArray(response)) {
      throw new Error('无效的模板数据格式')
    }
    
    // 验证每个组件的数据结构
    const isValid = response.every(component => 
      component.id && 
      component.type && 
      typeof component.x === 'number' && 
      typeof component.y === 'number' && 
      typeof component.width === 'number' && 
      typeof component.height === 'number'
    )
    
    if (!isValid) {
      throw new Error('模板数据格式不正确')
    }
    
    store.setComponents(response)
    importDialogVisible.value = false
    ElMessage.success('模板导入成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板导入失败')
  }
}

const handleImportError = (error: any) => {
  console.error('导入错误:', error)
  ElMessage.error('模板导入失败，请检查文件格式是否正确')
}

const beforeImportUpload = (file: File) => {
  const isJSON = file.type === 'application/json'
  if (!isJSON) {
    ElMessage.error('只能上传 JSON 文件！')
  }
  return isJSON
}

// 添加新简历按钮处理函数
const handleNewResume = async () => {
  try {
    const id = await store.saveResume()
    ElMessage.success(`新简历创建成功，ID：${id}`)
  } catch (error) {
    ElMessage.error('创建新简历失败')
  }
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

.upload-demo {
  width: 100%;
}
</style> 