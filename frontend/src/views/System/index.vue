<template>
  <div class="system">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>系统配置和管理功能</p>
    </div>
    
    <a-card title="系统信息">
      <a-descriptions bordered>
        <a-descriptions-item label="系统版本">{{ systemStore.status.version }}</a-descriptions-item>
        <a-descriptions-item label="后端连接状态">
          <a-badge
            :status="systemStore.isHealthy ? 'success' : 'error'"
            :text="systemStore.connectionStatus"
          />
        </a-descriptions-item>
        <a-descriptions-item label="最后心跳">{{ lastHeartbeatText }}</a-descriptions-item>
        <a-descriptions-item label="支持功能" :span="3">
          <a-space wrap>
            <a-tag
              v-for="feature in systemStore.status.features"
              :key="feature"
              color="blue"
            >
              {{ feature }}
            </a-tag>
          </a-space>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSystemStore } from '@/stores'
import dayjs from 'dayjs'

const systemStore = useSystemStore()

const lastHeartbeatText = computed(() => {
  if (!systemStore.status.lastHeartbeat) return '无'
  return dayjs(systemStore.status.lastHeartbeat).format('YYYY-MM-DD HH:mm:ss')
})
</script>

<style scoped>
.system {
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
