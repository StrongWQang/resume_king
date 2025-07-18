<template>
  <Header v-if="showHeader" />
  <router-view />
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/layout/Header.vue'
import { useSettingsStore } from './store/settings'

const route = useRoute()
const showHeader = computed(() => {
  // 在官网首页隐藏Header
  return route.name !== 'LandingPage'
})

const settingsStore = useSettingsStore()

onMounted(() => {
  settingsStore.initializeSettings()
})
</script>

<style>
/* 全局样式重置 - 彻底去掉白边 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
}

body {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

#app {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

:root {
  --primary-bg: #ffffff;
  --primary-text: #333333;
  --secondary-bg: #f5f5f5;
  --border-color: #e8e8e8;
}

:root[data-theme='dark'] {
  --primary-bg: #1a1a1a;
  --primary-text: #ffffff;
  --secondary-bg: #2a2a2a;
  --border-color: #3a3a3a;
}

body {
  background-color: var(--primary-bg);
  color: var(--primary-text);
}

.el-card,
.el-dialog,
.el-menu {
  background-color: var(--primary-bg) !important;
  color: var(--primary-text) !important;
  border-color: var(--border-color) !important;
}

.el-input__inner,
.el-textarea__inner {
  background-color: var(--secondary-bg) !important;
  color: var(--primary-text) !important;
}
</style>