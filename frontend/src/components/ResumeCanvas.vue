<template>
  <div class="canvas-container">
    <canvas
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
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useResumeStore } from '../store/resume'

const canvasRef = ref<HTMLCanvasElement | null>(null)
const store = useResumeStore()
let ctx: CanvasRenderingContext2D | null = null
let isDragging = false
let isResizing = false
let startX = 0
let startY = 0
let resizeDirection = ''
let initialWidth = 0
let initialHeight = 0
let initialX = 0
let initialY = 0

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

// 添加对齐辅助线相关变量
const alignmentLines = ref<{ x: number[], y: number[] }>({ x: [], y: [] })
const alignmentThreshold = 5 // 对齐阈值

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
    ctx = canvasRef.value.getContext('2d')
    initCanvas()
    // 确保 canvas 可以获得焦点
    canvasRef.value.focus()
  }
})

const initCanvas = () => {
  if (!canvasRef.value || !ctx) return
  
  // 设置画布大小为 A4 纸张大小（像素）
  canvasRef.value.width = 595 * 2  // A4 宽度（像素）
  canvasRef.value.height = 842 * 2  // A4 高度（像素）
  
  // 设置画布背景为白色
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height)
}

const handleDrop = (event: DragEvent) => {
  if (!event.dataTransfer) return
  
  const componentType = event.dataTransfer.getData('componentType')
  const rect = canvasRef.value?.getBoundingClientRect()
  
  if (rect) {
    const x = event.clientX - rect.left
    const y = event.clientY - rect.top
    
    // 添加新组件到 store
    store.addComponent({
      type: componentType,
      x,
      y,
      width: 200,
      height: 100,
      content: componentType === 'text' ? '请输入文本内容' : undefined
    })
    
    // 重新渲染画布
    renderCanvas()
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
    
    // 检查是否点击了调整大小的手柄或边界线
    const edge = isOnEdge(x, y, clickedComponent)
    if (edge) {
      isResizing = true
      resizeDirection = edge
      initialWidth = clickedComponent.width
      initialHeight = clickedComponent.height
      initialX = clickedComponent.x
      initialY = clickedComponent.y
    } else {
      isDragging = true
    }
  } else {
    store.selectComponent(null)
  }
  
  startX = event.clientX
  startY = event.clientY
  
  // 重新渲染画布以显示选中状态
  renderCanvas()
}

// 计算对齐线
const calculateAlignmentLines = (component: any) => {
  const lines = { x: [] as number[], y: [] as number[] }
  
  // 获取其他组件的边界
  store.components.forEach(other => {
    if (other.id === component.id) return
    
    // 水平对齐线
    lines.x.push(other.x) // 左边界
    lines.x.push(other.x + other.width) // 右边界
    lines.x.push(other.x + other.width / 2) // 中心线
    
    // 垂直对齐线
    lines.y.push(other.y) // 上边界
    lines.y.push(other.y + other.height) // 下边界
    lines.y.push(other.y + other.height / 2) // 中心线
  })
  
  return lines
}

// 检查是否需要对齐
const checkAlignment = (value: number, lines: number[]) => {
  for (const line of lines) {
    if (Math.abs(value - line) <= alignmentThreshold) {
      return line
    }
  }
  return null
}

const handleMouseMove = (event: MouseEvent) => {
  if (!canvasRef.value) return
  
  const rect = canvasRef.value.getBoundingClientRect()
  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top
  
  // 更新光标样式和手柄悬停状态
  if (store.selectedComponent && !isDragging && !isResizing) {
    const edge = isOnEdge(mouseX, mouseY, store.selectedComponent)
    hoveredHandle.value = edge
  }
  
  if (!isDragging && !isResizing) return
  
  // 计算从鼠标按下开始的总位移
  const dx = event.clientX - startX
  const dy = event.clientY - startY
  
  if (isResizing && store.selectedComponent) {
    const component = store.selectedComponent
    const alignmentLines = calculateAlignmentLines(component)
    
    // 初始化新的位置和尺寸
    let newX = component.x
    let newY = component.y
    let newWidth = component.width
    let newHeight = component.height
    
    // 处理水平方向的调整
    if (resizeDirection.includes('e')) {
      // 右边界或右上角、右下角
      const alignWidth = checkAlignment(initialX + Math.max(minSize, initialWidth + dx), alignmentLines.x)
      newWidth = alignWidth !== null ? alignWidth - initialX : Math.max(minSize, initialWidth + dx)
    }
    if (resizeDirection.includes('w')) {
      // 左边界或左上角、左下角
      newX = initialX + dx
      const alignX = checkAlignment(newX, alignmentLines.x)
      if (alignX !== null) newX = alignX
      newWidth = Math.max(minSize, initialWidth - (newX - initialX))
    }
    
    // 处理垂直方向的调整
    if (resizeDirection.includes('s')) {
      // 下边界或左下角、右下角
      const alignHeight = checkAlignment(initialY + Math.max(minSize, initialHeight + dy), alignmentLines.y)
      newHeight = alignHeight !== null ? alignHeight - initialY : Math.max(minSize, initialHeight + dy)
    }
    if (resizeDirection.includes('n')) {
      // 上边界或左上角、右上角
      newY = initialY + dy
      const alignY = checkAlignment(newY, alignmentLines.y)
      if (alignY !== null) newY = alignY
      newHeight = Math.max(minSize, initialHeight - (newY - initialY))
    }
    
    // 更新组件状态
    component.x = newX
    component.y = newY
    component.width = newWidth
    component.height = newHeight
    
  } else if (isDragging) {
    // 拖动逻辑
    store.updateSelectedComponentPosition(dx, dy)
    // 更新起始位置，用于下一次相对移动
    startX = event.clientX
    startY = event.clientY
  }
  
  renderCanvas()
}

const handleMouseUp = () => {
  isDragging = false
  isResizing = false
  resizeDirection = ''
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Delete' || event.key === 'Backspace') {
    store.deleteSelectedComponent()
    renderCanvas()
  }
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
  if (!ctx || !canvasRef.value) return
  
  // 清除画布
  ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  
  // 绘制背景
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  
  // 绘制所有组件
  store.components.forEach(component => {
    if (!ctx) return
    
    // 绘制组件边框
    if (component.id === store.selectedComponentId) {
      ctx.strokeStyle = '#1890ff'
      ctx.lineWidth = 2
      ctx.strokeRect(component.x, component.y, component.width, component.height)
    }
    
    // 如果是选中的组件，绘制调整大小的手柄
    if (component.id === store.selectedComponentId) {
      const handlePositions = getHandlePositions(component)
      
      // 绘制所有手柄
      for (const [direction, pos] of Object.entries(handlePositions)) {
        ctx.fillStyle = direction === hoveredHandle.value ? handleHoverColor : handleColor
        ctx.fillRect(pos.x, pos.y, handleSize, handleSize)
      }
      
      // 绘制对齐线
      if (isResizing && ctx) {
        const lines = calculateAlignmentLines(component)
        ctx.strokeStyle = '#1890ff'
        ctx.lineWidth = 1
        ctx.setLineDash([5, 5])
        
        // 绘制水平对齐线
        lines.x.forEach(x => {
          if (ctx) {
            ctx.beginPath()
            ctx.moveTo(x, 0)
            ctx.lineTo(x, canvasRef.value!.height)
            ctx.stroke()
          }
        })
        
        // 绘制垂直对齐线
        lines.y.forEach(y => {
          if (ctx) {
            ctx.beginPath()
            ctx.moveTo(0, y)
            ctx.lineTo(canvasRef.value!.width, y)
            ctx.stroke()
          }
        })
        
        if (ctx) {
          ctx.setLineDash([])
        }
      }
    }
    
    // 如果是文本组件，绘制文本内容
    if (component.type === 'text' && component.content) {
      ctx.save() // 保存当前画布状态

      // 设置文本样式
      const fontSize = component.fontSize || 16
      const fontFamily = component.fontFamily || 'SimSun'
      const padding = 10 // 内边距
      const lineHeight = fontSize * 1.2 // 行高
      ctx.font = `${fontSize}px ${fontFamily}`
      ctx.fillStyle = component.color || '#000000'
      ctx.textBaseline = 'top' // 设置文本基线为顶部

      // 创建剪裁区域
      ctx.beginPath()
      ctx.rect(component.x, component.y, component.width, component.height)
      ctx.clip()

      // 文本换行与绘制逻辑
      const words = component.content.split(' ')
      let line = ''
      let y = component.y + padding
      const maxWidth = component.width - (padding * 2)

      for (let n = 0; n < words.length; n++) {
        const testLine = line + words[n] + ' '
        const metrics = ctx.measureText(testLine)
        const testWidth = metrics.width

        // 检查是否需要换行
        if (testWidth > maxWidth && n > 0) {
          ctx.fillText(line, component.x + padding, y)
          line = words[n] + ' '
          y += lineHeight
        } else {
          line = testLine
        }

        // 检查是否超出垂直边界
        if (y + lineHeight > component.y + component.height - padding) {
          break
        }
      }

      // 绘制最后一行
      ctx.fillText(line, component.x + padding, y)

      ctx.restore() // 恢复画布状态
    }
  })
}

// 监听 store 变化
store.$subscribe(() => {
  renderCanvas()
})
</script>

<style scoped>
.canvas-container {
  flex: 1;
  overflow: auto;
  background-color: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.resume-canvas {
  background-color: white;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  outline: none; /* 移除焦点时的边框 */
}
</style> 