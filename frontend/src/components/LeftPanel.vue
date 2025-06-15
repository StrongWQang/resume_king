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
            <el-dropdown-item command="default">默认模板</el-dropdown-item>
            <el-dropdown-item command="modern">简约现代风格</el-dropdown-item>
            <el-dropdown-item command="creative">创意设计风格</el-dropdown-item>
            <el-dropdown-item command="professional">专业商务风格</el-dropdown-item>
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
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, Picture, ArrowDown, Minus } from '@element-plus/icons-vue'
import { useResumeStore } from '../store/resume'
import { 
  createDefaultTemplate,
  createModernTemplate,
  createCreativeTemplate,
  createProfessionalTemplate 
} from '../template'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useResumeStore()
const expandedItems = ref<string[]>([])

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

const handleTemplateSelect = async (command: string) => {
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
  
  let templateComponents
  switch (command) {
    case 'default':
      templateComponents = createDefaultTemplate()
      break
    case 'modern':
      templateComponents = createModernTemplate()
      break
    case 'creative':
      templateComponents = createCreativeTemplate()
      break
    case 'professional':
      templateComponents = createProfessionalTemplate()
      break
    default:
      templateComponents = createDefaultTemplate()
  }
  
  // 设置到简历中
  store.setComponents(templateComponents)
  
  ElMessage.success('已应用简历模板')
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
</style> 