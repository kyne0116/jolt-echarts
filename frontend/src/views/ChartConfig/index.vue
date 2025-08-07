<template>
  <div class="chart-config">
    <div class="page-header">
      <h1>图表配置管理</h1>
      <p>管理所有ECharts图表类型的配置文件</p>
    </div>
    
    <a-card title="图表配置列表">
      <template #extra>
        <a-space>
          <a-button type="primary">
            <PlusOutlined />
            新增配置
          </a-button>
          <a-button>
            <ReloadOutlined />
            刷新
          </a-button>
        </a-space>
      </template>
      
      <a-table
        :columns="columns"
        :data-source="chartConfigs"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeName(record.type) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small">编辑</a-button>
              <a-button type="link" size="small">预览</a-button>
              <a-button type="link" size="small" danger>删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'

const chartConfigs = ref([
  {
    id: '1',
    name: '堆叠折线图',
    type: 'line',
    description: '用于展示多个数据系列的趋势变化',
    createdAt: '2024-01-01'
  },
  {
    id: '2',
    name: '基础柱状图',
    type: 'bar',
    description: '用于比较不同类别的数据',
    createdAt: '2024-01-02'
  },
  {
    id: '3',
    name: '饼图',
    type: 'pie',
    description: '用于展示数据的占比关系',
    createdAt: '2024-01-03'
  }
])

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'actions' }
]

const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    line: 'blue',
    bar: 'green',
    pie: 'orange',
    radar: 'purple',
    gauge: 'red'
  }
  return colors[type] || 'default'
}

const getTypeName = (type: string) => {
  const names: Record<string, string> = {
    line: '折线图',
    bar: '柱状图',
    pie: '饼图',
    radar: '雷达图',
    gauge: '仪表盘'
  }
  return names[type] || type
}
</script>

<style scoped>
.chart-config {
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
