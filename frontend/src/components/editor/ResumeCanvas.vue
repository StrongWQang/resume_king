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
          :class="{ 
            'selected': store.selectedComponentId === component.id,
            'draggable-hover': isDraggableHovered && store.selectedComponentId === component.id
          }"
          :style="{
            left: `${component.x}px`,
            top: `${component.y}px`,
            width: `${component.width}px`,
            height: `${component.height}px`
          }"
          @mousedown.stop="handleComponentMouseDown($event, component)"
          @mousemove="handleDraggableHover($event, true)"
          @mouseleave="handleDraggableHover($event, false)"
        >
          <!-- 添加拖动提示区域 -->
          <div class="drag-handle top"></div>
          <div class="drag-handle right"></div>
          <div class="drag-handle bottom"></div>
          <div class="drag-handle left"></div>
          
          <div 
            ref="textContentRef"
            class="text-content-wrapper"
            :class="{ 'wrapper-draggable-hover': isWrapperHovered }"
            @mousedown.stop="handleTextWrapperMouseDown($event, component)"
            @mousemove="handleWrapperHover($event, true)"
            @mouseleave="handleWrapperHover($event, false)"
          >
            <div 
              class="text-content" 
              contenteditable="true" 
              v-html="component.content"
              :style="{
                fontSize: `${component.fontSize}px`,
                fontFamily: component.fontFamily,
                color: component.color,
                fontWeight: component.fontWeight,
                lineHeight: component.lineHeight,
                textAlign: component.textAlign,
                whiteSpace: 'pre-wrap'
              }"
              @mousedown.stop="handleTextContentMouseDown($event, component)"
              @input="handleTextInput($event, component)"
              @keydown="handleTextKeyDown($event, component)"
              @mouseup="handleTextSelect($event, component)"
              @compositionstart="handleCompositionStart"
              @compositionend="handleCompositionEnd($event, component)"
              @blur="handleTextBlur($event, component)"
            >
            </div>
          </div>
          <!-- 添加调整大小的手柄 -->
          <div
            v-if="store.selectedComponentId === component.id"
            class="resize-handle"
            @mousedown.stop="handleResizeStart"
          ></div>
          <!-- 添加AI优化按钮 -->
          <el-button
            v-if="store.selectedComponentId === component.id && hasSelectedText"
            class="ai-optimize-btn"
            type="primary"
            size="small"
            @click="showOptimizeDialog"
          >
            AI优化
          </el-button>
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
          @mousedown.stop="handleComponentMouseDown($event, component)"
        >
          <div 
            class="divider-line"
            :style="{
              borderColor: component.color || '#dcdfe6',
              borderWidth: component.thickness ? `${component.thickness}px` : '1px'
            }"
          ></div>
          <!-- 添加调整大小的手柄 -->
          <div
            v-if="store.selectedComponentId === component.id"
            class="resize-handle"
            @mousedown.stop="handleResizeStart"
          ></div>
        </div>
      </template>
    </div>
  </div>
  <!-- 添加AI优化对话框 -->
  <AIOptimizeDialog
    v-model:visible="optimizeDialogVisible"
    :selected-text="selectedText"
    @apply="handleOptimizedText"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useResumeStore } from '../../store/resume'
import ImageComponent from '../common/ImageComponent.vue'
import AIOptimizeDialog from '../common/AIOptimizeDialog.vue'

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
  
  // 检查是否点击了任何组件
  const clickedComponent = store.components.find(component => {
    return x >= component.x && 
           x <= component.x + component.width && 
           y >= component.y && 
           y <= component.y + component.height
  })
  
  if (clickedComponent) {
    store.selectComponent(clickedComponent.id)
  } else {
    store.selectComponent(null)
  }
  
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

// 新增：测量文本内容实际宽高
function measureTextSize(component: any) {
  // 创建隐藏div用于测量
  const div = document.createElement('div')
  div.style.position = 'absolute'
  div.style.visibility = 'hidden'
  div.style.whiteSpace = 'pre-wrap'
  div.style.fontSize = (component.fontSize || 14) + 'px'
  div.style.fontFamily = component.fontFamily || 'Microsoft YaHei'
  div.style.fontWeight = component.fontWeight || 400
  div.style.lineHeight = component.lineHeight || 1.5
  div.style.textAlign = component.textAlign || 'left'
  div.style.width = 'auto'
  div.style.height = 'auto'
  div.style.padding = '0'
  div.innerText = component.content || ''
  document.body.appendChild(div)
  const width = Math.ceil(div.offsetWidth)
  const height = Math.ceil(div.offsetHeight)
  document.body.removeChild(div)
  return { width, height }
}

// 在 handleTextInput 和 handleSizeChange 里调用 measureTextSize
const handleTextInput = (event: Event, component: any) => {
  // 不同步内容到 store，只让 DOM 自己维护
  if (isComposing.value) return // 输入法输入中，不处理
  const target = event.target as HTMLElement;
  // 只做宽高自适应
  if (component.type.startsWith('text-')) {
    const { width, height } = measureTextSize({ ...component, content: target.innerText });
    component.width = Math.max(component.width, width);
    component.height = Math.max(component.height, height);
  }
}

// 添加文本键盘事件处理函数
const handleTextKeyDown = (event: KeyboardEvent, component: any) => {
  if (isComposing.value) return // 输入法输入中，不处理
  const target = event.target as HTMLElement;
  const text = target.textContent || '';
  const selection = window.getSelection();
  if (!selection) return;
  
  const range = selection.getRangeAt(0);
  const cursorPosition = range.startOffset;

  if (event.key === 'Delete' || event.key === 'Backspace') {
    // 如果文本内容为空，则删除整个组件
    if (text.trim() === '') {
      event.preventDefault();
      store.deleteSelectedComponent();
      renderCanvas();
      return;
    }

    // 处理退格键删除项目符号的情况
    if (event.key === 'Backspace') {
      const lines = text.split('\n');
      const currentLineIndex = text.substring(0, cursorPosition).split('\n').length - 1;
      const currentLine = lines[currentLineIndex];
      const currentLineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1;

      // 如果当前行以"• "开头且光标在"• "后面或其中
      if (currentLine.startsWith('• ') && cursorPosition - currentLineStart <= 2) {
        event.preventDefault();
        lines[currentLineIndex] = currentLine.substring(2); // 移除"• "
        const newText = lines.join('\n');
        target.textContent = newText;
        
        // 设置光标位置到行首
        const newRange = document.createRange();
        newRange.setStart(target.firstChild || target, currentLineStart);
        newRange.collapse(true);
        selection.removeAllRanges();
        selection.addRange(newRange);
        
        // 更新组件内容
        component.content = newText;
        store.updateSelectedComponent();
        return;
      }
    }
  } else if (event.key === 'Enter') {
    event.preventDefault(); // 阻止默认的换行行为
    
    const textBefore = text.substring(0, cursorPosition);
    const textAfter = text.substring(cursorPosition);
    const newText = textBefore + '\n• ' + textAfter;
    target.textContent = newText;
    
    // 设置光标位置到新行的项目符号后
    const newPosition = cursorPosition + 3; // \n• 的长度是3
    const newRange = document.createRange();
    newRange.setStart(target.firstChild || target, newPosition);
    newRange.collapse(true);
    selection.removeAllRanges();
    selection.addRange(newRange);
    
    // 更新组件内容
    component.content = newText;
    store.updateSelectedComponent();
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

// 添加新的响应式变量
const hasSelectedText = ref(false)
const selectedText = ref('')
const optimizeDialogVisible = ref(false)
const currentTextComponent = ref<any>(null)

// 添加文本选择处理函数
const handleTextSelect = (event: MouseEvent, component: any) => {
  const selection = window.getSelection()
  if (selection && selection.toString().trim()) {
    hasSelectedText.value = true
    selectedText.value = selection.toString()
    currentTextComponent.value = component
  } else {
    hasSelectedText.value = false
    selectedText.value = ''
    currentTextComponent.value = null
  }
}

// 显示优化对话框
const showOptimizeDialog = () => {
  optimizeDialogVisible.value = true
}

// 处理优化后的文本
const handleOptimizedText = (optimizedText: string) => {
  if (currentTextComponent.value && selectedText.value) {
    const content = currentTextComponent.value.content || '';
    // 创建临时div解析HTML
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    // 获取纯文本
    const plainText = tempDiv.innerText;
    const selText = selectedText.value;
    // 找到选中文本在纯文本中的位置
    const idx = plainText.indexOf(selText);
    if (idx !== -1) {
      // 用于遍历和替换的字符计数
      let charCount = 0;
      let replaced = false;
      function replaceInNode(node: Node) {
        if (replaced) return;
        if (node.nodeType === Node.TEXT_NODE) {
          const nodeText = node.nodeValue || '';
          if (!replaced && charCount + nodeText.length >= idx + selText.length && charCount <= idx) {
            const start = idx - charCount;
            const end = start + selText.length;
            const before = nodeText.slice(0, start);
            const after = nodeText.slice(end);
            node.nodeValue = before + optimizedText + after;
            replaced = true;
          }
          charCount += nodeText.length;
        } else if (node.nodeType === Node.ELEMENT_NODE) {
          for (let i = 0; i < node.childNodes.length; i++) {
            replaceInNode(node.childNodes[i]);
            if (replaced) break;
          }
        }
      }
      replaceInNode(tempDiv);
      // 关键：直接在store.components中替换内容
      const idxInStore = store.components.findIndex(c => c.id === currentTextComponent.value.id);
      if (idxInStore !== -1) {
        store.components[idxInStore].content = tempDiv.innerHTML;
        store.updateSelectedComponent();
      }
    }
    // 重置状态
    hasSelectedText.value = false;
    selectedText.value = '';
    currentTextComponent.value = null;
  }
}

// 修改事件处理函数
const handleComponentMouseDown = (event: MouseEvent, component: any) => {
  // 如果点击的是组件的根元素（空白区域），则启动拖动
  if (event.target === event.currentTarget) {
    store.selectComponent(component.id)
    isDragging = true
    startX = event.clientX
    startY = event.clientY
  }
}

const handleTextWrapperMouseDown = (event: MouseEvent, component: any) => {
  // 如果点击的是文本包装器（不是文本内容），则启动拖动
  if (event.target === event.currentTarget) {
    store.selectComponent(component.id)
    isDragging = true
    startX = event.clientX
    startY = event.clientY
    event.preventDefault() // 阻止默认行为
  }
}

const handleTextContentMouseDown = (event: MouseEvent, component: any) => {
  // 点击文本内容区域时，只选中组件但不启动拖动
  store.selectComponent(component.id)
  event.stopPropagation() // 阻止事件冒泡
}

// 添加悬停状态变量
const isDraggableHovered = ref(false)
const isWrapperHovered = ref(false)

// 处理边框区域悬停
const handleDraggableHover = (event: MouseEvent, isHovering: boolean) => {
  if (event.target === event.currentTarget) {
    isDraggableHovered.value = isHovering
  }
}

// 处理包装器区域悬停
const handleWrapperHover = (event: MouseEvent, isHovering: boolean) => {
  if (event.target === event.currentTarget) {
    isWrapperHovered.value = isHovering
  }
}

const isComposing = ref(false)

const handleCompositionStart = () => {
  isComposing.value = true
}

const handleCompositionEnd = (event: CompositionEvent, component: any) => {
  isComposing.value = false
  handleTextBlur(event as unknown as FocusEvent, component)
}

const handleTextBlur = (event: FocusEvent, component: any) => {
  const target = event.target as HTMLElement
  component.content = target.innerHTML
  store.updateSelectedComponent()
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

/* 拖动提示区域样式 */
.drag-handle {
  position: absolute;
  background-color: transparent;
  transition: background-color 0.2s ease;
}

.drag-handle.top {
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  cursor: move;
}

.drag-handle.right {
  top: 0;
  right: 0;
  bottom: 0;
  width: 4px;
  cursor: move;
}

.drag-handle.bottom {
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  cursor: move;
}

.drag-handle.left {
  top: 0;
  left: 0;
  bottom: 0;
  width: 4px;
  cursor: move;
}

/* 悬停效果 */
.text-component.draggable-hover .drag-handle {
  background-color: rgba(76, 175, 80, 0.3);
}

.text-component.draggable-hover {
  border-color: #4caf50;
}

.text-content-wrapper {
  width: 100%;
  height: 100%;
  cursor: move;
  position: relative;
  border: 1px solid transparent;
  transition: border-color 0.2s ease;
}

.text-content-wrapper.wrapper-draggable-hover {
  border-color: rgba(76, 175, 80, 0.3);
  background-color: rgba(76, 175, 80, 0.05);
}

.text-content {
  width: 100%;
  height: 100%;
  padding: 0;
  outline: none;
  word-break: break-word;
  cursor: text;
  user-select: text;
  position: relative;
  z-index: 1;
  line-height: 1;
  display: block;
  vertical-align: top;
  white-space: pre-wrap;
}

/* 美化项目符号 */
.text-content[data-has-bullets="true"] {
  padding-left: 1.5em;
}

.text-content[data-has-bullets="true"]::before {
  content: "•";
  position: absolute;
  left: 0.5em;
  color: #666;
}

.text-content:hover {
  cursor: text;
}

.text-component:hover {
  border-color: #4caf50;
}

.text-component.selected {
  border-color: #4caf50;
  border-width: 2px;
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
  z-index: 2;
  pointer-events: auto;
}

.resize-handle:hover {
  background-color: #2e7d32;
}

/* 对齐辅助线样式 */
.alignment-line {
  display: none;
}

.ai-optimize-btn {
  position: absolute;
  right: -70px;
  top: 0;
  z-index: 1000;
}
</style>