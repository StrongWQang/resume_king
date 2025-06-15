<template>
  <div class="left-panel">
    <div class="panel-title">组件</div>
    <div class="template-button">
      <el-dropdown @command="handleTemplateSelect" style="width: 100%">
        <el-button type="primary" size="large" style="width: 100%">
          使用简历模板
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item 
              v-for="template in templates" 
              :key="template.id"
              :command="template.id"
              @mouseenter="handlePreview(template.id, $event)"
              @mouseleave="closePreview"
            >
              {{ template.name }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="component-list">
      <div
        v-for="item in components"
        :key="item.type"
        class="component-item"
      >
        <div 
          class="component-header"
          draggable="true"
          @dragstart="handleDragStart($event, item)"
          @click="toggleSubList(item.type)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <el-icon class="arrow-icon" :class="{ 'is-expanded': expandedItems.includes(item.type) }">
            <ArrowDown />
          </el-icon>
        </div>
        <div 
          v-if="expandedItems.includes(item.type)"
          class="sub-list"
        >
          <div
            v-for="subItem in item.subItems"
            :key="subItem.type"
            class="sub-item"
            draggable="true"
            @dragstart="handleDragStart($event, subItem)"
          >
            <el-icon><component :is="subItem.icon" /></el-icon>
            <span>{{ subItem.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 预览弹窗 -->
  <div 
    v-if="previewVisible" 
    class="preview-popup"
    :style="previewPosition"
  >
    <div class="preview-content" v-if="previewData">
      <div 
        v-for="(component, index) in previewData" 
        :key="component.id"
        class="preview-component"
        :style="{
          position: 'absolute',
          left: (component.x * 0.3) + 'px',
          top: (component.y * 0.3) + 'px',
          width: (component.width * 0.3) + 'px',
          height: (component.height * 0.3) + 'px',
          color: component.color,
          fontSize: (component.fontSize * 0.3) + 'px',
          textAlign: component.textAlign,
          fontFamily: component.fontFamily,
          lineHeight: component.lineHeight,
          fontWeight: component.fontWeight
        }"
      >
        <template v-if="component.type === 'text-title' || component.type === 'text-basic'">
          {{ component.content }}
        </template>
        <template v-else-if="component.type === 'divider-solid'">
          <div 
            class="divider"
            :style="{
              width: '100%',
              height: component.thickness + 'px',
              backgroundColor: component.color,
              margin: (component.padding * 0.3) + 'px 0'
            }"
          ></div>
        </template>
        <template v-else-if="component.type === 'image'">
          <img 
            :src="component.imageUrl" 
            :alt="component.alt || ''"
            class="preview-image"
            :style="{
              width: '100%',
              height: '100%',
              objectFit: component.objectFit || 'contain'
            }"
            @error="handleImageError"
          />
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Document, Picture, ArrowDown, Minus } from '@element-plus/icons-vue'
import { useResumeStore } from '../store/resume'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const store = useResumeStore()
const expandedItems = ref<string[]>([])
const previewVisible = ref(false)
const previewData = ref(null)
const previewPosition = ref({
  top: '0px',
  left: '0px'
})
const imageLoadErrors = ref(new Set())

// 存储所有模板数据
const templatesData = ref<Record<string, any>>({})

const components = [
  { 
    type: 'text', 
    label: '文本框', 
    icon: Document,
    subItems: [
      { type: 'text-basic', label: '基础文本', icon: Document },
      { type: 'text-title', label: '标题文本', icon: Document },
      { type: 'text-paragraph', label: '段落文本', icon: Document }
    ]
  },
  { 
    type: 'image', 
    label: '图片', 
    icon: Picture,
  },
  // {
  //   type: 'list',
  //   label: '列表',
  //   icon: List,
  //   subItems: [
  //     { type: 'list-bullet', label: '项目符号列表', icon: List },
  //     { type: 'list-number', label: '数字列表', icon: List }
  //   ]
  // },
  // {
  //   type: 'profile',
  //   label: '个人信息',
  //   icon: User,
  //   subItems: [
  //     { type: 'profile-basic', label: '基础信息', icon: User },
  //     { type: 'profile-contact', label: '联系方式', icon: User }
  //   ]
  // },
  {
    type: 'divider',
    label: '分隔线',
    icon: Minus,
    subItems: [
      { type: 'divider-solid', label: '实线', icon: Minus },
      { type: 'divider-dashed', label: '虚线', icon: Minus },
      { type: 'divider-dotted', label: '点线', icon: Minus },
      { type: 'divider-gradient', label: '渐变线', icon: Minus }
    ]
  }
]

const toggleSubList = (type: string) => {
  const index = expandedItems.value.indexOf(type)
  if (index === -1) {
    expandedItems.value.push(type)
  } else {
    expandedItems.value.splice(index, 1)
  }
}

const handleDragStart = (event: DragEvent, item: any) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('componentType', item.type)
  }
}

const handleTemplateSelect = async (templateId: string) => {
  // 检查是否已有内容
  if (store.components.length > 0) {
    try {
      await ElMessageBox.confirm(
        '当前简历已有内容，使用模板将覆盖现有内容，是否继续？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      // 用户点击取消
      return
    }
  }
  
  // 从已加载的模板数据中获取对应模板
  const templateComponents = templatesData.value[templateId]
  if (!templateComponents) {
    ElMessage.error('模板数据不存在')
    return
  }
  
  // 设置到简历中
  store.setComponents(templateComponents)
  
  ElMessage.success('已应用简历模板')
}

const templates = [
  { id: '354907c7-3c1d-4471-821c-11565924bb73', name: '默认模板' },
  { id: 'd5ec40df-3ee3-47e6-95e0-f7c251961899', name: '简约现代风格' },
  { id: 'c90222fa-e4ec-4e2f-8cda-881d1fa0c8b6', name: '创意设计风格' },
  { id: '32de6bc0-d07c-43bd-85b6-ac00020295e4', name: '专业商务风格' }
]

// 处理图片URL
const getImageUrl = (src: string) => {
  if (!src) return ''
  // 如果已经是完整的URL，使用代理URL
  if (src.startsWith('http://') || src.startsWith('https://')) {
    return `/api/proxy/image?url=${encodeURIComponent(src)}`
  }
  // 添加日志记录
  console.log('处理图片URL:', src)
  const fullUrl = `${src}`
  console.log('完整图片URL:', fullUrl)
  return fullUrl
}

// 处理模板数据中的图片URL
const processTemplateData = (data: any[]) => {
  console.log('处理模板数据:', data)
  return data.map(component => {
    if (component.type === 'image' && component.imageUrl) {
      console.log('处理图片组件:', component)
      const processedComponent = {
        ...component,
        imageUrl: getImageUrl(component.imageUrl)
      }
      console.log('处理后的图片组件:', processedComponent)
      return processedComponent
    }
    return component
  })
}

// 初始化加载所有模板数据
const loadTemplatesData = async () => {
  try {
    const promises = templates.map(template => 
      axios.get(`/api/resumes/${template.id}`)
        .then(response => {
          // 处理模板数据中的图片URL
          templatesData.value[template.id] = processTemplateData(response.data)
        })
    )
    await Promise.all(promises)
    console.log('所有模板数据加载完成')
  } catch (error) {
    console.error('加载模板数据失败:', error)
    ElMessage.error('加载模板数据失败')
  }
}

// 组件挂载时加载模板数据
onMounted(() => {
  loadTemplatesData()
})

const handlePreview = (templateId: string, event: MouseEvent) => {
  // 直接从本地数据获取预览内容
  previewData.value = templatesData.value[templateId]
  previewVisible.value = true
  
  // 计算预览框位置
  const dropdownItem = event.currentTarget as HTMLElement
  const rect = dropdownItem.getBoundingClientRect()
  previewPosition.value = {
    top: rect.top + 'px',
    left: (rect.right + 10) + 'px'
  }
}

const closePreview = () => {
  previewVisible.value = false
  previewData.value = null
  imageLoadErrors.value.clear()
}

const getComponentType = (type: string) => {
  // 根据组件类型返回对应的组件
  const componentMap = {
    'text-basic': 'TextBasic',
    'text-title': 'TextTitle',
    'text-paragraph': 'TextParagraph',
    'image': 'ImageComponent',
    'divider-solid': 'DividerSolid',
    'divider-dashed': 'DividerDashed',
    'divider-dotted': 'DividerDotted',
    'divider-gradient': 'DividerGradient'
  }
  return componentMap[type] || 'div'
}

// 处理图片加载错误
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
</script>

<style scoped>
.left-panel {
  width: 200px;
  border-right: 1px solid #e8f5e9;
  padding: 20px;
  background-color: #ffffff;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #2e7d32;
}

.template-button {
  margin-bottom: 20px;
}

.template-button :deep(.el-button--primary) {
  background-color: #4caf50;
  border-color: #4caf50;
}

.template-button :deep(.el-button--primary:hover) {
  background-color: #43a047;
  border-color: #43a047;
}

.component-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.component-item {
  border: 1px solid #e8f5e9;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.component-item:hover {
  box-shadow: 0 2px 12px 0 rgba(76, 175, 80, 0.1);
}

.component-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  cursor: pointer;
  background-color: #ffffff;
  transition: all 0.3s ease;
}

.component-header:hover {
  background-color: #f1f8e9;
}

.arrow-icon {
  margin-left: auto;
  transition: transform 0.3s;
  color: #4caf50;
}

.arrow-icon.is-expanded {
  transform: rotate(180deg);
}

.sub-list {
  border-top: 1px solid #e8f5e9;
  background-color: #fafafa;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px 10px 32px;
  cursor: move;
  transition: all 0.3s ease;
}

.sub-item:hover {
  background-color: #f1f8e9;
}

.sub-item .el-icon {
  color: #4caf50;
}

.component-header .el-icon {
  color: #4caf50;
}

.preview-popup {
  position: fixed;
  z-index: 2000;
  background: white;
  border: 1px solid #e8f5e9;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 10px;
}

.preview-content {
  position: relative;
  width: 180px;
  height: 240px;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.preview-component {
  position: absolute;
  white-space: pre-wrap;
  word-break: break-word;
  transform-origin: top left;
}

.divider {
  width: 100%;
  background-color: currentColor;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 2px;
  background-color: #f5f5f5; /* 添加背景色，在图片加载时显示 */
}
</style> 