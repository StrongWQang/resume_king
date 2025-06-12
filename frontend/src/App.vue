<template>
  <div class="app-container">
    <Header />
    <div class="main-content">
      <LeftPanel />
      <ResumeCanvas />
      <RightPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useResumeStore } from './store/resume'
import Header from './components/Header.vue'
import LeftPanel from './components/LeftPanel.vue'
import RightPanel from './components/RightPanel.vue'
import ResumeCanvas from './components/ResumeCanvas.vue'

const store = useResumeStore()

onMounted(async () => {
  // 从本地存储加载数据
  store.loadFromLocalStorage()
  
  // 从 localStorage 获取上次编辑的简历 ID
  const lastResumeId = localStorage.getItem('lastResumeId')
  if (lastResumeId) {
    try {
      await store.loadResume(lastResumeId)
    } catch (error) {
      console.error('加载上次编辑的简历失败:', error)
    }
  }
})
</script>

<style scoped>
.app-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}
</style> 