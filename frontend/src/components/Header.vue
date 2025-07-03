<template>
  <header class="header">
    <div class="logo" @click="handleLogoClick" title="github链接 点击后跳转">
      <img src="/logo-large.jpg" alt="Resume King Logo" class="logo-image" />
      <span class="logo-text">Resume_King 简历王</span>
    </div>
    <div class="actions">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button type="danger" @click="handleClear">清空</el-button>
      <el-dropdown @command="handleImportCommand">
        <el-button type="primary">
          导入简历
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="load">根据ID加载简历</el-dropdown-item>
            <el-dropdown-item command="import">导入模板JSON</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-dropdown @command="handleExportCommand">
        <el-button type="success">
          导出简历
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="pdf">导出PDF</el-dropdown-item>
            <el-dropdown-item command="template">导出模板JSON</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResumeStore } from '../store/resume'
import { UploadFilled, ArrowDown } from '@element-plus/icons-vue'
import { exportPDF } from '../utils/export'

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
    
    // 导出PDF
    const pdfUrl = await exportPDF(store.components)
    
    // 下载PDF
    const a = document.createElement('a')
    a.href = pdfUrl
    a.download = 'resume.pdf'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(pdfUrl)
    
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

// 添加导入命令处理函数
const handleImportCommand = (command: string) => {
  switch (command) {
    case 'load':
      handleLoad()
      break
    case 'import':
      handleImportTemplate()
      break
  }
}

// 修改导出命令处理函数
const handleExportCommand = (command: string) => {
  switch (command) {
    case 'pdf':
      handleExport()
      break
    case 'template':
      handleExportTemplate()
      break
  }
}

// 添加清空功能
const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清空当前编辑区吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      store.setComponents([])
      ElMessage.success('已清空编辑区')
    })
    .catch(() => {
      ElMessage.info('已取消清空操作')
    })
}

const handleLogoClick = () => {
  window.open('https://github.com/StrongWQang/resume_king', '_blank')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(76, 175, 80, 0.08);
  border-bottom: 1px solid #e8f5e9;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #2e7d32;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 15px;
  border-radius: 6px;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-image {
  height: 40px;
  width: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.logo:hover {
  transform: scale(1.05);
  color: #4caf50;
  background-color: #f1f8e9;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
}

.logo-text {
  white-space: nowrap;
}

.actions {
  display: flex;
  gap: 12px;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background-color: #4caf50;
  border-color: #4caf50;
}

:deep(.el-button--primary:hover) {
  background-color: #2e7d32;
  border-color: #2e7d32;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

:deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #e74c3c;
  color: #e74c3c;
}

:deep(.el-button--danger:hover) {
  background-color: #e74c3c;
  border-color: #e74c3c;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(231, 76, 60, 0.2);
}

:deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #4caf50;
  color: #4caf50;
}

:deep(.el-button--success:hover) {
  background-color: #4caf50;
  border-color: #4caf50;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

:deep(.el-button:not(.el-button--primary):not(.el-button--danger):not(.el-button--success)) {
  background-color: #ffffff;
  border-color: #e8f5e9;
  color: #4caf50;
}

:deep(.el-button:not(.el-button--primary):not(.el-button--danger):not(.el-button--success):hover) {
  color: #2e7d32;
  border-color: #4caf50;
  background-color: #f1f8e9;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
}

:deep(.el-dropdown-menu) {
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(76, 175, 80, 0.1);
  border: 1px solid #e8f5e9;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f1f8e9;
  color: #4caf50;
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

:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.1);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e8f5e9;
  padding: 15px 20px;
}

:deep(.el-dialog__title) {
  color: #2e7d32;
  font-weight: 600;
}

:deep(.el-upload-dragger) {
  border: 2px dashed #e8f5e9;
  background-color: #ffffff;
  transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: #4caf50;
  background-color: #f1f8e9;
}

:deep(.el-upload__text) {
  color: #4caf50;
}

:deep(.el-upload__tip) {
  color: #2e7d32;
}
</style> 