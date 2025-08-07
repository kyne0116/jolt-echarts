<template>
  <div class="virtual-database">
    <div class="page-header">
      <h1>虚拟数据库管理</h1>
      <p>模拟数据库表结构的管理界面</p>
    </div>
    
    <a-card title="虚拟数据表">
      <template #extra>
        <a-space>
          <a-button type="primary">
            <PlusOutlined />
            新增数据表
          </a-button>
          <a-button>
            <ReloadOutlined />
            刷新
          </a-button>
        </a-space>
      </template>
      
      <a-empty description="虚拟数据库管理功能开发中..." />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.virtual-database {
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
