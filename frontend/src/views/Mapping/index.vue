<template>
  <div class="mapping-container">
    <div class="page-header">
      <h2>占位符映射管理</h2>
      <p class="page-description">管理图表模板中占位符与数据源的映射关系</p>

      <a-tabs v-model:activeKey="activeTab" class="mapping-tabs">
        <a-tab-pane key="advanced" tab="高级映射管理器">
          <PlaceholderMappingManager />
        </a-tab-pane>
        <a-tab-pane key="dynamic" tab="动态映射配置">
          <DynamicMappingConfig />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DynamicMappingConfig from './DynamicMappingConfig.vue'
import PlaceholderMappingManager from './PlaceholderMappingManager.vue'

const activeTab = ref('advanced')
</script>

<style scoped>
.mapping-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1890ff;
}

.page-description {
  color: #666;
  margin: 0 0 16px 0;
}

.mapping-tabs {
  margin-top: 16px;
}
</style>