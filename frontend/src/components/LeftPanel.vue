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
          <div v-if="item.type === 'company-logo'" class="logo-search-container">
            <el-input
              v-model="logoSearchKeyword"
              placeholder="搜索公司名称"
              clearable
              size="small"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div
            v-for="subItem in item.subItems"
            :key="subItem.type + (subItem.logoUrl || '')"
            class="sub-item"
            draggable="true"
            @dragstart="handleLogoDragStart($event, subItem)"
          >
            <template v-if="subItem.type === 'company-logo-item'">
              <div class="logo-preview">
                <img 
                  :src="subItem.logoUrl" 
                  :alt="subItem.label"
                  class="logo-preview-image"
                  @error="handleImageError"
                />
              </div>
            </template>
            <template v-else>
              <el-icon><component :is="subItem.icon" /></el-icon>
            </template>
            <span>{{ subItem.label }}</span>
          </div>
          <div v-if="item.type === 'company-logo' && item.subItems.length === 0" class="no-data">
            暂无数据
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

  <!-- Logo选择对话框 -->
  <el-dialog
    v-model="logoDialogVisible"
    title="选择公司Logo"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="logo-search">
      <el-input
        v-model="logoSearchKeyword"
        placeholder="搜索公司名称"
        clearable
        @clear="handleLogoSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <div v-loading="logoLoading" class="logo-grid">
      <template v-if="companyLogos.length > 0">
        <div
          v-for="logo in companyLogos"
          :key="logo.id"
          class="logo-item"
          draggable="true"
          @dragstart="handleLogoDragStart($event, logo)"
        >
          <div class="logo-image-container">
            <img
              :src="logo.url"
              :alt="logo.name"
              class="logo-image"
              @error="handleImageError"
              @load="handleImageLoad"
            />
          </div>
          <div class="logo-name">{{ logo.name }}</div>
        </div>
      </template>
      <div v-else class="no-data">
        暂无数据
      </div>
    </div>
    
    <div class="logo-pagination">
      <el-pagination
        v-model:current-page="logoCurrentPage"
        :page-size="logoPageSize"
        :total="totalLogos"
        @current-change="fetchCompanyLogos"
        layout="prev, pager, next"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { Document, Picture, ArrowDown, Minus, Search } from '@element-plus/icons-vue'
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

// 存储所有公司logo数据
const allCompanyLogos = ref<any[]>([])
const logoSearchKeyword = ref('')
const logoLoading = ref(false)

// 根据搜索关键词过滤logo
const filteredCompanyLogos = computed(() => {
  if (!logoSearchKeyword.value) {
    return allCompanyLogos.value
  }
  
  const keyword = logoSearchKeyword.value.toLowerCase()
  return allCompanyLogos.value.filter(logo => 
    logo.name.toLowerCase().includes(keyword)
  )
})

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
  {
    type: 'company-logo',
    label: '公司Logo',
    icon: Picture,
    subItems: [] // 将在获取数据后动态填充
  },
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
    if (item.type === 'company-logo') {
      // 如果是公司logo组件，阻止默认拖拽
      event.preventDefault()
      return
    }
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
  console.log('处理图片URL:', src)
  // 直接返回原始URL
  return src
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

// 组件挂载时加载模板数据和logo数据
onMounted(() => {
  loadTemplatesData()
  fetchAllCompanyLogos() // 加载所有logo数据
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
    alt: img.alt
  })
  
  // 使用默认图片
  img.src = '/src/assets/default-logo.png'
  
  // 添加错误样式
  img.style.border = '1px solid #ff4d4f'
  img.style.backgroundColor = '#fff2f0'
}

// 处理图片加载成功
const handleImageLoad = (e: Event) => {
  const img = e.target as HTMLImageElement
  console.log('图片加载成功:', img.src)
}

// 添加公司logo相关的状态
const companyLogos = ref([])
const logoCurrentPage = ref(1)
const logoPageSize = 9
const logoDialogVisible = ref(false)
const totalLogos = ref(0)  // 添加总数状态

// 处理logo数据中的图片URL
const processLogoData = (data: any[]) => {
  console.log('处理前的logo数据:', data)
  const processed = data.map(logo => ({
    ...logo,
    url: getImageUrl(logo.url)
  }))
  console.log('处理后的logo数据:', processed)
  return processed
}

// 获取公司logo列表
const fetchCompanyLogos = async () => {
  try {
    logoLoading.value = true
    const response = await axios.get('/api/logo', {
      params: {
        name: logoSearchKeyword.value,
        page: logoCurrentPage.value,
        size: logoPageSize
      }
    })
    const { records, total } = response.data
    console.log('获取到的logo数据:', records)
    // 处理logo数据中的图片URL
    companyLogos.value = processLogoData(records)
    // 更新公司logo组件的子项
    const companyLogoComponent = components.find(c => c.type === 'company-logo')
    if (companyLogoComponent) {
      companyLogoComponent.subItems = records.map(logo => ({
        type: 'company-logo-item',
        label: logo.name,
        icon: Picture,
        logoUrl: logo.url,
        isCompanyLogo: true // 标记这是公司logo
      }))
    }
    totalLogos.value = total
  } catch (error) {
    console.error('获取公司logo失败:', error)
    ElMessage.error('获取公司logo失败')
  } finally {
    logoLoading.value = false
  }
}

// 处理logo搜索
const handleLogoSearch = () => {
  // 重置页码
  logoCurrentPage.value = 1
  // 获取公司logo列表
  fetchCompanyLogos()
}

// 处理logo拖拽
const handleLogoDragStart = (event: DragEvent, item: any) => {
  if (event.dataTransfer) {
    if (item.type === 'company-logo-item') {
      // 设置拖拽数据
      const dragData = {
        type: 'image',
        imageUrl: item.logoUrl,
        imageAlt: item.label,
        isCompanyLogo: true // 标记这是公司logo
      }
      event.dataTransfer.setData('application/json', JSON.stringify(dragData))
      // 设置拖拽效果
      event.dataTransfer.effectAllowed = 'copy'
      
      // 创建固定大小的预览图
      const img = new Image()
      img.src = item.logoUrl
      img.onload = () => {
        // 创建一个canvas来调整图片大小
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        const size = 100 // 设置统一的预览图大小
        
        // 计算缩放比例，保持宽高比
        const scale = Math.min(size / img.width, size / img.height)
        const width = img.width * scale
        const height = img.height * scale
        
        // 设置canvas大小
        canvas.width = size
        canvas.height = size
        
        // 在canvas中央绘制图片
        if (ctx) {
          ctx.fillStyle = '#ffffff' // 设置白色背景
          ctx.fillRect(0, 0, size, size)
          ctx.drawImage(
            img,
            (size - width) / 2,
            (size - height) / 2,
            width,
            height
          )
        }
        
        // 使用canvas作为拖拽预览图
        event.dataTransfer.setDragImage(canvas, size / 2, size / 2)
      }
    } else {
      event.dataTransfer.setData('componentType', item.type)
    }
  }
}

// 打开logo选择对话框
const openLogoDialog = () => {
  logoDialogVisible.value = true
  console.log('打开logo对话框')
  fetchCompanyLogos()
}

// 获取所有公司logo数据
const fetchAllCompanyLogos = async () => {
  try {
    logoLoading.value = true
    // 获取第一页数据
    const response = await axios.get('/api/logo', {
      params: {
        page: 1,
        size: 1000 // 设置一个较大的值以获取所有数据
      }
    })
    const { records } = response.data
    // 处理logo数据中的图片URL
    allCompanyLogos.value = processLogoData(records)
    // 更新公司logo组件的子项
    updateCompanyLogoSubItems()
  } catch (error) {
    console.error('获取公司logo失败:', error)
    ElMessage.error('获取公司logo失败')
  } finally {
    logoLoading.value = false
  }
}

// 更新公司logo组件的子项
const updateCompanyLogoSubItems = () => {
  const companyLogoComponent = components.find(c => c.type === 'company-logo')
  if (companyLogoComponent) {
    companyLogoComponent.subItems = filteredCompanyLogos.value.map(logo => ({
      type: 'company-logo-item',
      label: logo.name,
      icon: Picture,
      logoUrl: logo.url,
      isCompanyLogo: true
    }))
  }
}

// 监听搜索关键词变化
watch(logoSearchKeyword, () => {
  updateCompanyLogoSubItems()
})
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
  max-height: 300px;
  overflow-y: auto;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px 10px 32px;
  cursor: move;
  transition: all 0.3s ease;
  user-select: none;
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

.logo-search {
  margin-bottom: 20px;
}

.logo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.logo-item {
  aspect-ratio: 1;
  border: 1px solid #e8f5e9;
  border-radius: 4px;
  padding: 8px;
  cursor: move;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}

.logo-item:hover {
  box-shadow: 0 2px 12px 0 rgba(76, 175, 80, 0.1);
  border-color: #4caf50;
}

.logo-item:hover .logo-image {
  transform: scale(1.05);
}

.logo-image-container {
  width: 100%;
  height: 80%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.logo-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: all 0.3s ease;
}

.logo-name {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.logo-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.no-data {
  padding: 12px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.logo-grid {
  min-height: 200px;
}

.logo-preview {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.logo-preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 添加拖拽时的样式 */
.sub-item[draggable="true"]:active {
  opacity: 0.7;
  cursor: grabbing;
}

.logo-search-container {
  padding: 8px 12px;
  border-bottom: 1px solid #e8f5e9;
  position: sticky;
  top: 0;
  background-color: #fafafa;
  z-index: 1;
}

.logo-search-container :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
}

.logo-search-container :deep(.el-input__inner) {
  height: 28px;
  line-height: 28px;
}

.sub-list::-webkit-scrollbar {
  width: 6px;
}

.sub-list::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.sub-list::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style> 