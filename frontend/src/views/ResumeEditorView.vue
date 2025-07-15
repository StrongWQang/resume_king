<template>
  <div class="app-container">
    <div class="main-content">
      <LeftPanel />
      <ResumeCanvas />
      <RightPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useResumeStore } from '../store/resume'
import LeftPanel from '../components/layout/LeftPanel.vue'
import RightPanel from '../components/layout/RightPanel.vue'
import ResumeCanvas from '../components/editor/ResumeCanvas.vue'
import { useRoute } from 'vue-router';

const store = useResumeStore()

// 在组件加载时处理模板ID
onMounted(async () => {
  const route = useRoute();
  const templateId = route.query.template as string;
  
  if (templateId) {
    try {
      // 清除本地存储的上次编辑的简历ID，避免影响模板应用
      localStorage.removeItem('lastResumeId');
      // 使用字符串形式的模板ID
      await store.loadResume(templateId);
    } catch (error) {
      console.error('加载模板失败:', error);
    }
  } else {
    // 从本地存储加载上次编辑的简历ID
    const lastResumeId = localStorage.getItem('lastResumeId');
    if (lastResumeId) {
      try {
        await store.loadResume(lastResumeId);
      } catch (error) {
        console.error('加载上次编辑的简历失败:', error);
      }
    }
  }
});
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