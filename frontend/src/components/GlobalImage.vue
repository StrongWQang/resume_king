<template>
  <div class="global-image" :class="{ 'loading': isLoading, 'error': hasError }">
    <img
      v-if="!hasError"
      :src="proxyUrl"
      :alt="alt"
      :style="imageStyle"
      @load="handleImageLoad"
      @error="handleImageError"
    />
    <div v-if="isLoading" class="loading-placeholder">
      <el-icon class="loading-icon"><Loading /></el-icon>
    </div>
    <div v-if="hasError" class="error-placeholder">
      <el-icon class="error-icon"><Warning /></el-icon>
      <span>加载失败</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Loading, Warning } from '@element-plus/icons-vue'
import { imageLoader } from '../utils/imageLoader'

const props = defineProps<{
  src: string
  alt?: string
  width?: string | number
  height?: string | number
  objectFit?: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down'
}>()

const isLoading = ref(true)
const hasError = ref(false)
const retryCount = ref(0)
const maxRetries = 3

const proxyUrl = computed(() => {
  return imageLoader.getProxyImageUrl(props.src)
})

const imageStyle = computed(() => {
  return {
    width: typeof props.width === 'number' ? `${props.width}px` : props.width,
    height: typeof props.height === 'number' ? `${props.height}px` : props.height,
    objectFit: props.objectFit || 'contain'
  }
})

const handleImageLoad = () => {
  isLoading.value = false
  hasError.value = false
}

const handleImageError = async () => {
  if (retryCount.value < maxRetries) {
    retryCount.value++
    // 重试前检查图片是否可访问
    const isAccessible = await imageLoader.checkImageAccess(props.src)
    if (isAccessible) {
      // 如果可访问，强制重新加载
      const timestamp = new Date().getTime()
      const img = document.createElement('img')
      img.src = `${proxyUrl.value}${proxyUrl.value.includes('?') ? '&' : '?'}t=${timestamp}`
    } else {
      hasError.value = true
      isLoading.value = false
    }
  } else {
    hasError.value = true
    isLoading.value = false
  }
}

onMounted(async () => {
  try {
    await imageLoader.preloadImage(props.src)
    handleImageLoad()
  } catch (error) {
    handleImageError()
  }
})
</script>

<style scoped>
.global-image {
  position: relative;
  display: inline-block;
  overflow: hidden;
}

.loading-placeholder,
.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.loading-icon {
  font-size: 24px;
  color: #409eff;
  animation: rotate 1s linear infinite;
}

.error-icon {
  font-size: 24px;
  color: #f56c6c;
  margin-bottom: 8px;
}

.error-placeholder span {
  font-size: 14px;
  color: #909399;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 