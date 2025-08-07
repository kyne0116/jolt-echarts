<template>
  <div id="app">
    <h1>ECharts动态数据流管理平台</h1>
    <p>系统状态: {{ status }}</p>
    <button @click="testConnection">测试连接</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const status = ref('正在初始化...')

const testConnection = () => {
  status.value = '连接测试中...'
  
  // 简单的测试
  setTimeout(() => {
    status.value = '测试完成'
  }, 1000)
}

// 初始化
setTimeout(() => {
  status.value = '就绪'
}, 500)
</script>

<style>
#app {
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  color: #1890ff;
}

button {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #40a9ff;
}
</style>
