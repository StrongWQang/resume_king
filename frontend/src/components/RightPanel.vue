<template>
  <div class="right-panel">
    <div class="panel-title">属性</div>
    <template v-if="selectedComponent">
      <el-form label-position="top">
        <el-form-item label="位置">
          <div class="position-inputs">
            <el-input-number 
              v-model="selectedComponent.x" 
              :min="0" 
              :precision="0"
              :controls="false"
              @change="handlePositionChange" 
            />
            <el-input-number 
              v-model="selectedComponent.y" 
              :min="0" 
              :precision="0"
              :controls="false"
              @change="handlePositionChange" 
            />
          </div>
        </el-form-item>

        <el-form-item label="尺寸">
          <div class="size-inputs">
            <el-input-number 
              v-model="selectedComponent.width" 
              :min="1" 
              :precision="0"
              :controls="false"
              @change="handleSizeChange" 
            />
            <el-input-number 
              v-model="selectedComponent.height" 
              :min="1" 
              :precision="0"
              :controls="false"
              @change="handleSizeChange" 
            />
          </div>
        </el-form-item>

        <!-- 文本组件属性 -->
        <template v-if="selectedComponent.type.startsWith('text-')">
          <el-form-item label="文本内容">
            <el-input
                v-model="selectedComponent.content"
                type="textarea"
                :rows="4"
                @change="handleContentChange"
            />
          </el-form-item>

          <el-form-item label="字体">
            <div class="font-controls">
              <el-select 
                v-model="selectedComponent.fontFamily" 
                @change="handleStyleChange"
                :value="selectedComponent.fontFamily"
              >
                <el-option label="微软雅黑" value="Microsoft YaHei" />
                <el-option label="宋体" value="SimSun" />
                <el-option label="黑体" value="SimHei" />
                <el-option label="楷体" value="KaiTi" />
                <el-option label="仿宋" value="FangSong" />
                <el-option label="华文黑体" value="STHeiti" />
                <el-option label="华文楷体" value="STKaiti" />
                <el-option label="华文宋体" value="STSong" />
                <el-option label="华文仿宋" value="STFangsong" />
                <el-option label="华文中宋" value="STZhongsong" />
                <el-option label="华文琥珀" value="STHupo" />
                <el-option label="华文新魏" value="STXinwei" />
                <el-option label="华文隶书" value="STLiti" />
                <el-option label="幼圆" value="YouYuan" />
                <el-option label="方正姚体" value="FZYaoti" />
              </el-select>
              <el-button 
                type="primary" 
                size="small" 
                @click="applyFontToAll"
                :disabled="!selectedComponent"
              >
                应用到全部文本
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="字号">
            <el-input-number
                v-model="selectedComponent.fontSize"
                :min="1"
                :max="72"
                @change="handleStyleChange"
            />
          </el-form-item>

          <el-form-item label="字体粗细">
            <el-select 
              v-model="selectedComponent.fontWeight" 
              @change="handleStyleChange"
            >
              <el-option label="正常" :value="400" />
              <el-option label="加粗" :value="600" />
              <el-option label="更粗" :value="700" />
            </el-select>
          </el-form-item>

          <el-form-item label="行高">
            <el-slider
              v-model="selectedComponent.lineHeight"
              :min="1"
              :max="3"
              :step="0.1"
              @change="handleStyleChange"
            />
          </el-form-item>

          <el-form-item label="对齐方式">
            <el-radio-group 
              v-model="selectedComponent.textAlign" 
              @change="handleStyleChange"
            >
              <el-radio-button label="left">左对齐</el-radio-button>
              <el-radio-button label="center">居中</el-radio-button>
              <el-radio-button label="right">右对齐</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="颜色">
            <el-color-picker v-model="selectedComponent.color" @change="handleStyleChange" />
          </el-form-item>
        </template>

        <!-- 分隔线组件属性 -->
        <template v-if="selectedComponent.type.startsWith('divider-')">
          <el-form-item label="分隔线样式">
            <el-select 
              v-model="selectedComponent.type" 
              @change="handleDividerStyleChange"
            >
              <el-option label="实线" value="divider-solid" />
              <el-option label="虚线" value="divider-dashed" />
              <el-option label="点线" value="divider-dotted" />
              <el-option label="渐变线" value="divider-gradient" />
            </el-select>
          </el-form-item>

          <el-form-item label="线条颜色">
            <el-color-picker 
              v-model="selectedComponent.color" 
              @change="handleDividerStyleChange"
              :disabled="selectedComponent.type === 'divider-gradient'"
            />
          </el-form-item>

          <el-form-item label="线条粗细">
            <el-slider
              v-model="selectedComponent.thickness"
              :min="1"
              :max="5"
              :step="1"
              :disabled="selectedComponent.type === 'divider-gradient'"
              @change="handleDividerStyleChange"
            />
          </el-form-item>

          <el-form-item label="上下间距">
            <el-slider
              v-model="selectedComponent.padding"
              :min="0"
              :max="30"
              :step="1"
              @change="handleDividerStyleChange"
            />
          </el-form-item>
        </template>
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
import { ElMessage } from 'element-plus'

const store = useResumeStore()

const selectedComponent = computed(() => store.selectedComponent)

const handlePositionChange = () => {
  if (selectedComponent.value) {
    // 确保位置值为整数
    selectedComponent.value.x = Math.round(selectedComponent.value.x)
    selectedComponent.value.y = Math.round(selectedComponent.value.y)
    store.updateSelectedComponent()
  }
}

const handleSizeChange = () => {
  if (selectedComponent.value) {
    // 确保尺寸为整数且不小于1
    selectedComponent.value.width = Math.max(1, Math.round(selectedComponent.value.width))
    selectedComponent.value.height = Math.max(1, Math.round(selectedComponent.value.height))
    store.updateSelectedComponent()
  }
}

const handleContentChange = () => {
  store.updateSelectedComponent()
}

const handleStyleChange = () => {
  if (selectedComponent.value) {
    store.updateSelectedComponent()
  }
}

const handleDividerStyleChange = () => {
  if (selectedComponent.value) {
    // 如果是渐变线，重置颜色和粗细
    if (selectedComponent.value.type === 'divider-gradient') {
      selectedComponent.value.color = undefined
      selectedComponent.value.thickness = undefined
    }
    store.updateSelectedComponent()
  }
}

const applyFontToAll = () => {
  if (!selectedComponent.value) return
  
  const currentFont = selectedComponent.value.fontFamily
  const currentFontSize = selectedComponent.value.fontSize
  const currentColor = selectedComponent.value.color
  
  // 更新所有文本组件的字体样式
  store.components.forEach(component => {
    if (component.type === 'text') {
      component.fontFamily = currentFont
      component.fontSize = currentFontSize
      component.color = currentColor
    }
  })
  
  store.updateSelectedComponent()
  ElMessage.success('已将所有文本组件更新为当前字体样式')
}
</script>

<style scoped>
.right-panel {
  width: 300px;
  border-left: 1px solid #eee;
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  position: sticky;
  top: 0;
  background-color: white;
  padding: 10px 0;
  z-index: 1;
}

.position-inputs,
.size-inputs {
  display: flex;
  gap: 10px;
}

.position-inputs :deep(.el-input-number),
.size-inputs :deep(.el-input-number) {
  width: 100%;
}

.position-inputs :deep(.el-input__wrapper),
.size-inputs :deep(.el-input__wrapper) {
  padding: 0 8px;
}

.position-inputs :deep(.el-input__inner),
.size-inputs :deep(.el-input__inner) {
  text-align: center;
}

.font-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.no-selection {
  color: #999;
  text-align: center;
  margin-top: 20px;
}

/* 添加滚动条样式 */
.right-panel::-webkit-scrollbar {
  width: 6px;
}

.right-panel::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.right-panel::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* 确保表单项有足够的间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 确保输入框和选择器有合适的宽度 */
:deep(.el-input),
:deep(.el-select) {
  width: 100%;
}

/* 确保颜色选择器有合适的宽度 */
:deep(.el-color-picker) {
  width: 100%;
}

/* 确保滑块有合适的宽度 */
:deep(.el-slider) {
  width: 100%;
}

/* 确保文本域有合适的宽度和高度 */
:deep(.el-textarea__inner) {
  min-height: 80px;
}

/* 确保单选按钮组有合适的宽度 */
:deep(.el-radio-group) {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

/* 确保数字输入框有合适的宽度 */
:deep(.el-input-number) {
  width: 100%;
}
</style>