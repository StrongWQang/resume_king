<template>
  <div class="global-image" :class="{ 'loading': isLoading, 'error': hasError }">
    <img
      v-if="!hasError"
      ref="imgRef"
      :src="proxyUrl"
      :alt="alt"
      crossorigin="anonymous"
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
import { ref, computed, onMounted, watch } from 'vue'
import { Loading, Warning } from '@element-plus/icons-vue'
import { imageLoader } from "../../utils/imageLoader";

const props = defineProps<{
  src: string
  alt?: string
  width?: string | number
  height?: string | number
  objectFit?: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down'
}>()

const imgRef = ref<HTMLImageElement>()
const isLoading = ref(true)
const hasError = ref(false)
const imageLoadErrors = ref(new Set<string>())

const proxyUrl = computed(() => {
  if (!props.src) return ''
  
  // 开发环境下添加调试信息
  if (process.env.NODE_ENV === 'development') {
    console.log('=== GlobalImage URL处理 ===')
    console.log('原始src:', props.src)
    console.log('清理后URL:', imageLoader.cleanUrl(props.src))
    console.log('最终代理URL:', imageLoader.getProxyImageUrl(props.src))
    console.log('=== 处理完成 ===')
  }
  
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

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  const imgUrl = img.src
  
  // 避免重复处理同一个错误
  if (imageLoadErrors.value.has(imgUrl)) {
    return
  }
  
  imageLoadErrors.value.add(imgUrl)
  
  console.error('图片加载失败:', {
    src: img.src,
    alt: img.alt,
    originalSrc: props.src,
    cleanedUrl: imageLoader.cleanUrl(props.src)
  })
  
  // 使用imageLoader的错误处理方法
  imageLoader.handleImageError(img, '/logo-large.jpg')
  
  // 如果默认图片也失败了，显示错误状态
  if (img.src.includes('/logo-large.jpg')) {
    hasError.value = true
    isLoading.value = false
  }
}

// 监听src变化，重置状态
watch(() => props.src, () => {
  isLoading.value = true
  hasError.value = false
  imageLoadErrors.value.clear()
})

onMounted(() => {
  if (!props.src) {
    isLoading.value = false
    hasError.value = true
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