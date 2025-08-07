<template>
  <div class="template">
    <div class="page-header">
      <h1>通用JSON模板</h1>
      <p>管理带占位符的通用JSON模板</p>
    </div>
    
    <a-card title="模板列表">
      <template #extra>
        <a-space>
          <a-button type="primary">
            <PlusOutlined />
            新增模板
          </a-button>
          <a-button>
            <ReloadOutlined />
            刷新
          </a-button>
        </a-space>
      </template>
      
      <a-empty description="通用JSON模板管理功能开发中..." />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.template {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
