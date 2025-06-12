<template>
  <div class="divider-component" :class="type">
    <div class="divider-line"></div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

const props = defineProps<{
  type: 'divider-solid' | 'divider-dashed' | 'divider-dotted' | 'divider-gradient'
}>()
</script>

<style scoped>
.divider-component {
  width: 100%;
  padding: 10px 0;
}

.divider-line {
  height: 1px;
  width: 100%;
}

/* 实线样式 */
.divider-solid .divider-line {
  border-bottom: 1px solid #dcdfe6;
}

/* 虚线样式 */
.divider-dashed .divider-line {
  border-bottom: 1px dashed #dcdfe6;
}

/* 点线样式 */
.divider-dotted .divider-line {
  border-bottom: 1px dotted #dcdfe6;
}

/* 渐变线样式 */
.divider-gradient .divider-line {
  height: 2px;
  background: linear-gradient(to right, transparent, #409eff, transparent);
  border: none;
}
</style> 