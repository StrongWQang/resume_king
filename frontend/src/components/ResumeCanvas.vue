<template>
  <div class="canvas-container">
    <div
      ref="canvasRef"
      class="resume-canvas"
      @dragover.prevent
      @drop="handleDrop"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp"
      @keydown="handleKeyDown"
      @click="handleCanvasClick"
      tabindex="0"
    >
      <template v-for="component in store.components" :key="component.id">
        <!-- 图片组件 -->
        <ImageComponent
          v-if="component.type === 'image'"
          :component="component"
          :ref="el => { if (el) imageRefs[component.id] = el }"
        />
        <!-- 文本组件 -->
        <div
          v-else-if="component.type.startsWith('text-')"
          class="text-component"
          :class="{ 'selected': store.selectedComponentId === component.id }"
          :style="{
            left: `${component.x}px`,
            top: `${component.y}px`,
            width: `${component.width}px`,
            height: `${component.height}px`
          }"
          @mousedown.stop="handleMouseDown"
        >
          <div 
            class="text-content" 
            contenteditable="true" 
            :style="{
              fontSize: `${component.fontSize}px`,
              fontFamily: component.fontFamily,
              color: component.color,
              fontWeight: component.fontWeight,
              lineHeight: component.lineHeight,
              textAlign: component.textAlign,
              whiteSpace: 'pre-wrap'
            }"
            @input="handleTextInput($event, component)"
            @keydown="handleTextKeyDown($event, component)"
          >
            {{ component.content }}
          </div>
          <!-- 添加调整大小的手柄 -->
          <div
            v-if="store.selectedComponentId === component.id"
            class="resize-handle"
            @mousedown.stop="handleResizeStart"
          ></div>
        </div>
        <!-- 分隔线组件 -->
        <div
          v-else-if="component.type.startsWith('divider-')"
          class="divider-component"
          :class="component.type"
          :style="{
            left: `${component.x}px`,
            top: `${component.y}px`,
            width: `${component.width}px`,
            padding: `${component.padding || 10}px 0`
          }"
          @mousedown.stop="handleMouseDown"
        >
          <div 
            class="divider-line"
            :style="{
              borderColor: component.color || '#dcdfe6',
              borderWidth: component.thickness ? `${component.thickness}px` : '1px'
            }"
          ></div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useResumeStore } from '../store/resume'
import ImageComponent from './ImageComponent.vue'

const canvasRef = ref<HTMLDivElement | null>(null)
const store = useResumeStore()
const imageRefs = ref<Record<string, any>>({})
let isDragging = false
let isResizing = false
let startX = 0
let startY = 0
let resizeDirection = ''
let initialWidth = 0
let initialHeight = 0
let initialX = 0
let initialY = 0
let resizeStartX = 0
let resizeStartY = 0

// 定义调整大小的手柄位置
const resizeHandles = {
  nw: 'northwest', // 左上
  ne: 'northeast', // 右上
  sw: 'southwest', // 左下
  se: 'southeast', // 右下
  n: 'north',      // 上
  s: 'south',      // 下
  w: 'west',       // 左
  e: 'east'        // 右
}

const handleSize = 8 // 调整大小的手柄尺寸
const handleColor = '#1890ff' // 手柄颜色
const handleHoverColor = '#40a9ff' // 手柄悬停颜色
const minSize = 50 // 最小尺寸

// 添加手柄状态
const hoveredHandle = ref('')

// 修改对齐辅助线相关变量
const alignmentLines = ref<{ x: number[], y: number[] }>({ x: [], y: [] })
const alignmentThreshold = 5 // 对齐阈值（像素）

// 检查鼠标是否在手柄上
const isOverHandle = (x: number, y: number, handleX: number, handleY: number) => {
  return x >= handleX && x <= handleX + handleSize && y >= handleY && y <= handleY + handleSize
}

// 获取手柄位置
const getHandlePositions = (component: any) => {
  const handleSize = 8
  return {
    nw: { x: component.x, y: component.y },
    ne: { x: component.x + component.width - handleSize, y: component.y },
    sw: { x: component.x, y: component.y + component.height - handleSize },
    se: { x: component.x + component.width - handleSize, y: component.y + component.height - handleSize },
    n: { x: component.x + component.width / 2 - handleSize / 2, y: component.y },
    s: { x: component.x + component.width / 2 - handleSize / 2, y: component.y + component.height - handleSize },
    w: { x: component.x, y: component.y + component.height / 2 - handleSize / 2 },
    e: { x: component.x + component.width - handleSize, y: component.y + component.height / 2 - handleSize / 2 }
  }
}

// 检查是否在边界线上
const isOnEdge = (x: number, y: number, component: any) => {
  const threshold = 5 // 边界检测阈值
  const handleSize = 8
  
  // 检查是否在四个角的手柄上
  const handlePositions = getHandlePositions(component)
  for (const [direction, pos] of Object.entries(handlePositions)) {
    if (isOverHandle(x, y, pos.x, pos.y)) {
      return direction
    }
  }
  
  // 检查是否在边界线上
  if (Math.abs(x - component.x) <= threshold) return 'w'
  if (Math.abs(x - (component.x + component.width)) <= threshold) return 'e'
  if (Math.abs(y - component.y) <= threshold) return 'n'
  if (Math.abs(y - (component.y + component.height)) <= threshold) return 's'
  
  return ''
}

onMounted(() => {
  if (canvasRef.value) {
    // 确保 canvas 可以获得焦点
    canvasRef.value.focus()
  }
})

const handleDrop = async (event: DragEvent) => {
  if (!event.dataTransfer) return
  
  const rect = canvasRef.value?.getBoundingClientRect()
  
  if (rect) {
    const x = event.clientX - rect.left
    const y = event.clientY - rect.top
    
    // 尝试解析拖拽数据
    let dragData
    try {
      const jsonData = event.dataTransfer.getData('application/json')
      if (jsonData) {
        dragData = JSON.parse(jsonData)
      }
    } catch (e) {
      console.log('不是JSON数据，使用普通组件类型')
    }
    
    // 添加新组件到 store
    const newComponent = {
      id: Date.now().toString(),
      type: dragData?.type || event.dataTransfer.getData('componentType'),
      x,
      y,
      width: dragData?.type === 'image' ? 200 : (event.dataTransfer.getData('componentType').startsWith('divider-') ? 400 : 120),
      height: dragData?.type === 'image' ? 200 : (event.dataTransfer.getData('componentType').startsWith('divider-') ? 20 : 30),
      content: event.dataTransfer.getData('componentType').startsWith('text-') ? getDefaultTextContent(event.dataTransfer.getData('componentType')) : undefined,
      // 添加文本相关属性
      fontSize: event.dataTransfer.getData('componentType').startsWith('text-') ? getDefaultFontSize(event.dataTransfer.getData('componentType')) : undefined,
      fontFamily: event.dataTransfer.getData('componentType').startsWith('text-') ? 'Microsoft YaHei' : undefined,
      color: event.dataTransfer.getData('componentType').startsWith('text-') ? '#333333' : undefined,
      fontWeight: event.dataTransfer.getData('componentType').startsWith('text-') ? getDefaultFontWeight(event.dataTransfer.getData('componentType')) : undefined,
      lineHeight: event.dataTransfer.getData('componentType').startsWith('text-') ? getDefaultLineHeight(event.dataTransfer.getData('componentType')) : undefined,
      textAlign: event.dataTransfer.getData('componentType').startsWith('text-') ? 'left' : undefined,
      // 如果是图片组件，添加图片URL
      imageUrl: dragData?.imageUrl,
      imageAlt: dragData?.imageAlt
    }
    
    store.addComponent(newComponent)
    
    // 如果是图片组件且没有图片URL，自动触发文件选择
    if (newComponent.type === 'image' && !newComponent.imageUrl) {
      // 添加重试机制
      let retryCount = 0
      const maxRetries = 3
      
      const tryTriggerFileSelect = async () => {
        await nextTick()
        const imageComponent = imageRefs.value[newComponent.id]
        if (imageComponent) {
          console.log('Triggering file select for component:', newComponent.id)
          await imageComponent.triggerFileSelect()
        } else {
          console.error(`Image component not found: ${newComponent.id} (attempt ${retryCount + 1}/${maxRetries})`)
          if (retryCount < maxRetries) {
            retryCount++
            setTimeout(tryTriggerFileSelect, 100) // 100ms 后重试
          }
        }
      }
      
      await tryTriggerFileSelect()
    }
  }
}

// 获取默认文本内容
const getDefaultTextContent = (type: string) => {
  switch (type) {
    case 'text-basic':
      return '请输入文本内容'
    case 'text-title':
      return '标题文本'
    case 'text-paragraph':
      return '段落文本'
    default:
      return '请输入文本内容'
  }
}

// 获取默认字体大小
const getDefaultFontSize = (type: string) => {
  switch (type) {
    case 'text-basic':
      return 14
    case 'text-title':
      return 20
    case 'text-paragraph':
      return 14
    default:
      return 14
  }
}

// 获取默认字体粗细
const getDefaultFontWeight = (type: string) => {
  switch (type) {
    case 'text-basic':
      return 400
    case 'text-title':
      return 600
    case 'text-paragraph':
      return 400
    default:
      return 400
  }
}

// 获取默认行高
const getDefaultLineHeight = (type: string) => {
  switch (type) {
    case 'text-basic':
      return 1.5
    case 'text-title':
      return 1.2
    case 'text-paragraph':
      return 1.8
    default:
      return 1.5
  }
}

const handleMouseDown = (event: MouseEvent) => {
  if (!canvasRef.value) return
  
  const rect = canvasRef.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  
  // 首先检查是否点击了任何组件
  const clickedComponent = store.components.find(component => {
    return x >= component.x && 
           x <= component.x + component.width && 
           y >= component.y && 
           y <= component.y + component.height
  })
  
  if (clickedComponent) {
    store.selectComponent(clickedComponent.id)
    
    // 如果不是在调整大小，则开始拖动
    if (!isResizing) {
      isDragging = true
      startX = event.clientX
      startY = event.clientY
    }
  } else {
    store.selectComponent(null)
  }
  
  // 重新渲染画布以显示选中状态
  renderCanvas()
}

// 移除计算对齐线函数
const calculateAlignmentLines = (component: any) => {
  return { x: [], y: [] }
}

// 移除检查对齐函数
const checkAlignment = (value: number, lines: number[]) => {
  return null
}

const handleMouseMove = (event: MouseEvent) => {
  if (!canvasRef.value) return
  
  const rect = canvasRef.value.getBoundingClientRect()
  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top
  
  if (isDragging && store.selectedComponent) {
    // 拖动逻辑
    const dx = event.clientX - startX
    const dy = event.clientY - startY
    
    // 计算新的位置
    let newX = store.selectedComponent.x + dx
    let newY = store.selectedComponent.y + dy
    
    // 更新组件位置
    store.selectedComponent.x = newX
    store.selectedComponent.y = newY
    
    // 更新起始位置，用于下一次相对移动
    startX = event.clientX
    startY = event.clientY
    
    renderCanvas()
  }
}

const handleMouseUp = () => {
  isDragging = false
}

// 添加文本输入处理函数
const handleTextInput = (event: Event, component: any) => {
  const target = event.target as HTMLElement
  let content = target.textContent || ''
  
  // 处理•符号，将其转换为换行
  if (content.includes('•')) {
    content = content.split('•').map((line, index) => {
      // 第一行不需要添加•符号
      return index === 0 ? line.trim() : '•' + line.trim()
    }).join('\n')
    
    // 更新组件内容
    component.content = content
    target.textContent = content
  } else {
    component.content = content
  }
  
  store.updateSelectedComponent()
}

// 添加文本键盘事件处理函数
const handleTextKeyDown = (event: KeyboardEvent, component: any) => {
  if (event.key === 'Delete' || event.key === 'Backspace') {
    const target = event.target as HTMLElement
    const text = target.textContent || ''
    
    // 如果文本内容为空，则删除整个组件
    if (text.trim() === '') {
      event.preventDefault() // 阻止默认的删除行为
      store.deleteSelectedComponent()
      renderCanvas()
    }
  }
}

// 修改原有的 handleKeyDown 函数
const handleKeyDown = (event: KeyboardEvent) => {
  // 如果当前选中的是文本组件，且正在编辑文本，则不处理删除操作
  if (store.selectedComponent?.type.startsWith('text-') && 
      document.activeElement?.classList.contains('text-content')) {
    return
  }
  
  // 处理复制快捷键
  if ((event.metaKey || event.ctrlKey) && event.key === 'c') {
    event.preventDefault()
    if (store.selectedComponent) {
      handleCopyComponent()
    }
    return
  }

  // 处理撤销快捷键
  if ((event.metaKey || event.ctrlKey) && event.key === 'z') {
    event.preventDefault()
    if (event.shiftKey) {
      // Command/Ctrl + Shift + Z 重做
      store.redo()
    } else {
      // Command/Ctrl + Z 撤销
      store.undo()
    }
    return
  }
  
  if (event.key === 'Delete' || event.key === 'Backspace') {
    // 检查是否是文本组件且有内容
    if (store.selectedComponent?.type.startsWith('text-') && 
        store.selectedComponent.content && 
        store.selectedComponent.content.trim() !== '') {
      // 如果文本组件有内容，阻止删除
      event.preventDefault()
      return
    }
    
    store.deleteSelectedComponent()
    renderCanvas()
  }
}

// 添加复制组件的方法
const handleCopyComponent = () => {
  if (!store.selectedComponent) return
  
  // 创建组件的深拷贝
  const copiedComponent = JSON.parse(JSON.stringify(store.selectedComponent))
  
  // 生成新的ID
  copiedComponent.id = Date.now().toString()
  
  // 稍微偏移位置，避免完全重叠
  copiedComponent.x += 10
  copiedComponent.y += 10
  
  // 添加到store中
  store.addComponent(copiedComponent)
  
  // 选中新复制的组件
  store.selectComponent(copiedComponent.id)
  
  // 重新渲染画布
  renderCanvas()
}

const handleCanvasClick = (event: MouseEvent) => {
  if (!canvasRef.value) return
  
  const rect = canvasRef.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  
  // 检查是否点击了任何组件
  const clickedComponent = store.components.find(component => {
    return x >= component.x && 
           x <= component.x + component.width && 
           y >= component.y && 
           y <= component.y + component.height
  })
  
  // 如果没有点击到组件，取消选中状态
  if (!clickedComponent) {
    store.selectComponent(null)
    renderCanvas()
  }
}

const renderCanvas = () => {
  // 由于我们不再使用 canvas，这个函数可以留空或移除
  // 如果需要，可以在这里添加其他渲染逻辑
}

// 监听 store 变化
store.$subscribe(() => {
  renderCanvas()
})

// 添加调整大小的处理函数
const handleResizeStart = (event: MouseEvent) => {
  if (!store.selectedComponent) return
  
  isResizing = true
  resizeStartX = event.clientX
  resizeStartY = event.clientY
  initialWidth = store.selectedComponent.width
  initialHeight = store.selectedComponent.height
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleResizeMove)
  document.addEventListener('mouseup', handleResizeEnd)
}

const handleResizeMove = (event: MouseEvent) => {
  if (!isResizing || !store.selectedComponent) return
  
  const dx = event.clientX - resizeStartX
  const dy = event.clientY - resizeStartY
  
  // 计算新的宽度和高度
  const newWidth = Math.max(100, initialWidth + dx)
  const newHeight = Math.max(30, initialHeight + dy)
  
  // 更新组件尺寸
  store.selectedComponent.width = newWidth
  store.selectedComponent.height = newHeight
  
  // 触发重新渲染
  renderCanvas()
}

const handleResizeEnd = () => {
  isResizing = false
  
  // 移除全局事件监听
  document.removeEventListener('mousemove', handleResizeMove)
  document.removeEventListener('mouseup', handleResizeEnd)
}
</script>

<style scoped>
.canvas-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: #f1f8e9;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
}

.resume-canvas {
  width: 595px;
  height: 842px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(76, 175, 80, 0.1);
  position: relative;
  transform-origin: top left;
  transform: scale(1);
}

.text-component {
  position: absolute;
  cursor: move;
  border: 1px solid transparent;
  min-width: 100px;
  min-height: 30px;
  user-select: none;
  touch-action: none;
  z-index: 1;
  background-color: rgba(76, 175, 80, 0.01);
}

.text-component:hover {
  border-color: #4caf50;
}

.text-component.selected {
  border-color: #4caf50;
  border-width: 2px;
}

.text-content {
  width: 100%;
  height: 100%;
  padding: 5px;
  outline: none;
  word-break: break-word;
  pointer-events: auto;
}

.divider-component {
  position: absolute;
  cursor: move;
  border: 1px solid transparent;
  min-width: 100px;
}

.divider-component:hover {
  border-color: #4caf50;
}

.divider-line {
  height: 1px;
  width: 100%;
}

/* 实线样式 */
.divider-solid .divider-line {
  border-bottom-style: solid;
  border-color: #4caf50;
}

/* 虚线样式 */
.divider-dashed .divider-line {
  border-bottom-style: dashed;
  border-color: #4caf50;
}

/* 点线样式 */
.divider-dotted .divider-line {
  border-bottom-style: dotted;
  border-color: #4caf50;
}

/* 渐变线样式 */
.divider-gradient .divider-line {
  height: 2px;
  background: linear-gradient(to right, transparent, #4caf50, transparent);
  border: none;
}

/* 调整大小的手柄样式 */
.resize-handle {
  position: absolute;
  right: -5px;
  bottom: -5px;
  width: 10px;
  height: 10px;
  background-color: #4caf50;
  border-radius: 50%;
  cursor: se-resize;
  z-index: 10;
  pointer-events: auto;
}

.resize-handle:hover {
  background-color: #2e7d32;
}

/* 对齐辅助线样式 */
.alignment-line {
  display: none;
}
</style> 