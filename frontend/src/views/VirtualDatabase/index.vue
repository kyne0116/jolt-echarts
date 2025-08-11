<template>
  <div class="virtual-database-container">
    <!-- 数据管理主界面 -->
    <UniversalDataManager />
  </div>
</template>

<script setup lang="ts">
import UniversalDataManager from './UniversalDataManager.vue';
</script>

<style scoped>
.virtual-database-container {
  /* 简化容器样式，让子组件完全控制布局 */
  width: 100%;
  height: 100vh;
  overflow: hidden;
  position: relative;
}
</style>
