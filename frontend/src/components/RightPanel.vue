<template>
  <div class="right-panel">
    <div class="panel-title">属性</div>
    <template v-if="selectedComponent">
      <el-form label-position="top">
        <el-form-item label="位置">
          <div class="position-inputs">
            <el-input-number v-model="selectedComponent.x" :min="0" @change="handlePositionChange" />
            <el-input-number v-model="selectedComponent.y" :min="0" @change="handlePositionChange" />
          </div>
        </el-form-item>
        
        <el-form-item label="尺寸">
          <div class="size-inputs">
            <el-input-number v-model="selectedComponent.width" :min="50" @change="handleSizeChange" />
            <el-input-number v-model="selectedComponent.height" :min="50" @change="handleSizeChange" />
          </div>
        </el-form-item>
        
        <el-form-item label="文本内容" v-if="selectedComponent.type === 'text'">
          <el-input
            v-model="selectedComponent.content"
            type="textarea"
            :rows="4"
            @change="handleContentChange"
          />
        </el-form-item>
        
        <el-form-item label="字体">
          <el-select v-model="selectedComponent.fontFamily" @change="handleStyleChange">
            <el-option label="宋体" value="SimSun" />
            <el-option label="黑体" value="SimHei" />
            <el-option label="微软雅黑" value="Microsoft YaHei" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="字号">
          <el-input-number
            v-model="selectedComponent.fontSize"
            :min="12"
            :max="72"
            @change="handleStyleChange"
          />
        </el-form-item>
        
        <el-form-item label="颜色">
          <el-color-picker v-model="selectedComponent.color" @change="handleStyleChange" />
        </el-form-item>
      </el-form>
    </template>
    <div v-else class="no-selection">
      请选择一个组件
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useResumeStore } from '../store/resume'

const store = useResumeStore()

const selectedComponent = computed(() => store.selectedComponent)

const handlePositionChange = () => {
  store.updateSelectedComponent()
}

const handleSizeChange = () => {
  store.updateSelectedComponent()
}

const handleContentChange = () => {
  store.updateSelectedComponent()
}

const handleStyleChange = () => {
  store.updateSelectedComponent()
}
</script>

<style scoped>
.right-panel {
  width: 300px;
  border-left: 1px solid #eee;
  padding: 20px;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
}

.position-inputs,
.size-inputs {
  display: flex;
  gap: 10px;
}

.no-selection {
  color: #999;
  text-align: center;
  margin-top: 20px;
}
</style> 