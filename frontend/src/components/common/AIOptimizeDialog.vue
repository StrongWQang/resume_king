<template>
  <el-dialog
    v-model="dialogVisible"
    title="AI优化简历内容"
    width="50%"
    :before-close="handleClose"
  >
    <div class="optimize-content">
      <div class="original-text">
        <h4>原文内容：</h4>
        <p>{{ originalText }}</p>
      </div>
      <div class="optimized-text">
        <h4>优化内容：</h4>
        <div v-if="loading" class="loading-container">
          <el-icon class="loading-icon"><loading /></el-icon>
          <span>AI正在优化内容...</span>
        </div>
        <template v-else>
          <p>{{ optimizedText }}</p>
        </template>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleApply" :disabled="loading">
          应用优化
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps<{
  visible: boolean
  selectedText: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply', value: string): void
}>()

const dialogVisible = ref(false)
const originalText = ref('')
const optimizedText = ref('')
const loading = ref(false)

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    originalText.value = props.selectedText
    optimizeText()
  }
})

watch(() => dialogVisible.value, (newVal) => {
  emit('update:visible', newVal)
})

const optimizeText = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/optimize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        text: originalText.value
      })
    })
    
    if (!response.ok) {
      throw new Error('优化失败')
    }
    
    const data = await response.json()
    optimizedText.value = data.optimizedText
  } catch (error) {
    ElMessage.error('内容优化失败，请重试')
    console.error('优化失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  optimizedText.value = ''
}

const handleApply = () => {
  emit('apply', optimizedText.value)
  handleClose()
}
</script>

<style scoped>
.optimize-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.original-text,
.optimized-text {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.original-text h4,
.optimized-text h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.original-text p,
.optimized-text p {
  margin: 0;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.loading-container {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909399;
}

.loading-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>