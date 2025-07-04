<template>
  <div
    class="image-component"
    :class="{ 'selected': isSelected }"
    :style="{
      left: `${component.x}px`,
      top: `${component.y}px`,
      width: `${component.width}px`,
      height: `${component.height}px`
    }"
    @mousedown="handleMouseDown"
    @click="handleClick"
  >
    <!-- 添加一个覆盖整个区域的透明层，确保点击事件能被捕获 -->
    <div class="click-overlay" @mousedown="handleMouseDown" @click="handleClick"></div>
    
    <div class="image-wrapper">
      <ImageUploader
        v-if="!component.imageUrl"
        ref="uploaderRef"
        :component-id="component.id"
        @image-uploaded="handleImageUploaded"
      />
      <div v-else class="image-container">
        <GlobalImage
          :src="component.imageUrl"
          :alt="component.imageAlt || ''"
          width="100%"
          height="100%"
          object-fit="contain"
        />
      </div>
    </div>
    
    <div
      v-if="isSelected"
      class="resize-handle"
      @mousedown.stop="handleResizeStart"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useResumeStore } from '../store/resume'
import ImageUploader from './ImageUploader.vue'
import GlobalImage from './GlobalImage.vue'
import { imageLoader } from '../utils/imageLoader'

const props = defineProps<{
  component: {
    id: string
    type: string
    x: number
    y: number
    width: number
    height: number
    imageUrl?: string
    imageAlt?: string
  }
}>()

const store = useResumeStore()
const isSelected = ref(false)
const isResizing = ref(false)
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)
const startWidth = ref(0)
const startHeight = ref(0)
const uploaderRef = ref<InstanceType<typeof ImageUploader> | null>(null)
const imageLoadErrors = ref(new Set<string>())

// 监听选中状态变化
watch(() => store.selectedComponentId, (newId) => {
  isSelected.value = newId === props.component.id
  console.log(`组件 ${props.component.id} 选中状态:`, isSelected.value)
})

const handleClick = (e: MouseEvent) => {
  console.log('图片组件被点击:', props.component.id)
  
  // 检查点击的是否是上传区域
  const target = e.target as HTMLElement
  if (target.closest('.upload-placeholder') || target.closest('.el-upload')) {
    console.log('点击了上传区域，不处理选中逻辑')
    return
  }
  
  // 确保组件被选中
  store.selectComponent(props.component.id)
  isSelected.value = true
  
  // 阻止事件冒泡，避免触发画布的取消选中
  e.stopPropagation()
}

const handleMouseDown = (e: MouseEvent) => {
  console.log('图片组件 mousedown:', props.component.id)
  
  // 检查点击的是否是上传区域
  const target = e.target as HTMLElement
  if (target.closest('.upload-placeholder') || target.closest('.el-upload')) {
    console.log('点击了上传区域，跳过拖动逻辑')
    return
  }
  
  // 确保组件被选中
  store.selectComponent(props.component.id)
  isSelected.value = true
  
  // 开始拖动
  isDragging.value = true
  startX.value = e.clientX
  startY.value = e.clientY
  
  console.log('开始拖动，起始位置:', startX.value, startY.value)
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  
  // 阻止事件冒泡，避免触发 Canvas 的拖动逻辑
  e.stopPropagation()
  e.preventDefault()
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  
  const dx = e.clientX - startX.value
  const dy = e.clientY - startY.value
  // 直接更新组件位置（不使用 store 方法，直接修改）
  const newX = props.component.x + dx
  const newY = props.component.y + dy
  
  // 边界检查（可选）
  const canvasWidth = 595
  const canvasHeight = 842
  const constrainedX = Math.max(0, Math.min(newX, canvasWidth - props.component.width))
  const constrainedY = Math.max(0, Math.min(newY, canvasHeight - props.component.height))
  
  // 更新组件位置
  if (store.updateComponentPosition) {
    store.updateComponentPosition(props.component.id, constrainedX - props.component.x, constrainedY - props.component.y)
  } else {
    // 如果 store 方法不存在，直接修改组件属性
    const component = store.components.find(c => c.id === props.component.id)
    if (component) {
      component.x = constrainedX
      component.y = constrainedY
    }
  }
  
  // 更新起始位置
  startX.value = e.clientX
  startY.value = e.clientY
}

const handleMouseUp = () => {
  console.log('结束拖动')
  isDragging.value = false
  
  // 移除全局事件监听
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

const handleResizeStart = (e: MouseEvent) => {
  console.log('开始调整大小')
  e.preventDefault()
  e.stopPropagation()
  
  isResizing.value = true
  startX.value = e.clientX
  startY.value = e.clientY
  startWidth.value = props.component.width
  startHeight.value = props.component.height
  
  if (store.startResizing) {
    store.startResizing(props.component.id)
  }
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', handleResizeEnd)
}

const handleResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  
  const dx = e.clientX - startX.value
  const dy = e.clientY - startY.value
  
  const aspectRatio = props.component.aspectRatio || 1
  let newWidth = startWidth.value + dx
  let newHeight = newWidth / aspectRatio
  
  if (store.updateComponentSize) {
    store.updateComponentSize(props.component.id, newWidth, newHeight)
  } else {
    // 如果 store 方法不存在，直接修改组件属性
    const component = store.components.find(c => c.id === props.component.id)
    if (component) {
      component.width = newWidth
      component.height = newHeight
    }
  }
}

const handleResizeEnd = () => {
  console.log('结束调整大小')
  isResizing.value = false
  
  if (store.stopResizing) {
    store.stopResizing(props.component.id)
  }
  
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', handleResizeEnd)
}

const handleImageUploaded = (url: string, width: number, height: number) => {
  store.setComponentImage(props.component.id, url, width, height)
}

const triggerFileSelect = async () => {
  console.log('ImageComponent: triggerFileSelect called')
  
  let retryCount = 0
  const maxRetries = 3
  
  const tryTriggerUpload = async () => {
    await nextTick()
    if (uploaderRef.value) {
      console.log('ImageComponent: calling uploader triggerFileSelect')
      await uploaderRef.value.triggerFileSelect()
    } else {
      console.error(`ImageComponent: uploaderRef is null (attempt ${retryCount + 1}/${maxRetries})`)
      if (retryCount < maxRetries) {
        retryCount++
        await new Promise(resolve => setTimeout(resolve, 100))
        await tryTriggerUpload()
      }
    }
  }
  
  await tryTriggerUpload()
}

// 添加获取代理图片URL的方法
const getProxyImageUrl = (url: string) => {
  return imageLoader.getProxyImageUrl(url)
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  const imgUrl = img.src
  
  if (imageLoadErrors.value.has(imgUrl)) {
    return
  }
  
  imageLoadErrors.value.add(imgUrl)
  
  console.error('图片加载失败:', {
    src: img.src,
    alt: img.alt,
    naturalWidth: img.naturalWidth,
    naturalHeight: img.naturalHeight
  })
  
  // 使用全局默认图片
  img.src = '/src/template/default.png'
  
  // 添加错误样式
  img.style.border = '1px solid #ff4d4f'
  img.style.backgroundColor = '#fff2f0'
}

onMounted(() => {
  isSelected.value = store.selectedComponentId === props.component.id
})

onUnmounted(() => {
  // 清理所有事件监听器
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', handleResizeEnd)
})

defineExpose({
  triggerFileSelect
})
</script>

<style scoped>
.image-component {
  position: absolute;
  cursor: move;
  border: 1px solid transparent;
  min-width: 1px;
  min-height: 1px;
  user-select: none;
  touch-action: none;
  z-index: 1;
  /* 确保整个区域都可以接收点击事件 */
  background-color: rgba(0, 0, 0, 0.01);
}

.image-component:hover {
  border-color: #409eff;
}

.image-component.selected {
  border-color: #409eff;
  border-width: 2px;
}

/* 透明覆盖层，确保整个区域都可点击 */
.click-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  cursor: move;
  background-color: transparent;
  pointer-events: auto;
}

.image-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  pointer-events: auto;
}

.image-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  pointer-events: auto;
}

.image {
  display: block;
  max-width: 100%;
  max-height: 100%;
  pointer-events: none;
}

.resize-handle {
  position: absolute;
  right: -5px;
  bottom: -5px;
  width: 10px;
  height: 10px;
  background-color: #409eff;
  border-radius: 50%;
  cursor: se-resize;
  z-index: 10;
  pointer-events: auto;
}

.resize-handle:hover {
  background-color: #66b1ff;
}

/* 上传组件的特殊处理 */
:deep(.el-upload) {
  width: 100%;
  height: 100%;
  pointer-events: auto;
  z-index: 5;
  position: relative;
}

:deep(.upload-placeholder) {
  width: 100%;
  height: 100%;
  pointer-events: auto;
}
</style> 