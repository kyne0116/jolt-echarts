<template>
  <div class="jolt-spec">
    <div class="page-header">
      <h1>Jolt规范管理</h1>
      <p>管理每个图表类型对应的Jolt转换规范文件</p>
    </div>
    
    <a-card title="Jolt规范列表">
      <template #extra>
        <a-space>
          <a-button type="primary">
            <PlusOutlined />
            新增规范
          </a-button>
          <a-button>
            <ReloadOutlined />
            刷新
          </a-button>
        </a-space>
      </template>
      
      <a-empty description="Jolt规范管理功能开发中..." />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.jolt-spec {
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
