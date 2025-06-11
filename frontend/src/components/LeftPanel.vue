<template>
  <div class="left-panel">
    <div class="panel-title">组件</div>
    <div class="component-list">
      <div
        v-for="item in components"
        :key="item.type"
        class="component-item"
        draggable="true"
        @dragstart="handleDragStart($event, item)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document, Picture, List, User } from '@element-plus/icons-vue'

const components = [
  { type: 'text', label: '文本框', icon: Document },
  { type: 'image', label: '图片', icon: Picture },
  { type: 'list', label: '列表', icon: List },
  { type: 'profile', label: '个人信息', icon: User }
]

const handleDragStart = (event: DragEvent, item: any) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('componentType', item.type)
  }
}
</script>

<style scoped>
.left-panel {
  width: 200px;
  border-right: 1px solid #eee;
  padding: 20px;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.component-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.component-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: move;
}

.component-item:hover {
  background-color: #f5f7fa;
}
</style> 