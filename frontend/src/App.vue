<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  console.log('🎉 ECharts动态数据流管理平台已加载')
})
</script>

<style>
#app {
  height: 100vh;
  overflow: hidden;
}
</style>
