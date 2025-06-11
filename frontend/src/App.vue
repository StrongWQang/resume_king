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
import Header from './components/Header.vue'
import LeftPanel from './components/LeftPanel.vue'
import RightPanel from './components/RightPanel.vue'
import ResumeCanvas from './components/ResumeCanvas.vue'
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