<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>仪表板</h1>
      <p>ECharts动态数据流架构管理平台概览</p>
    </div>
    
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stats-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="图表配置"
            :value="stats.chartConfigs"
            :prefix="h(BarChartOutlined)"
            :value-style="{ color: '#3f8600' }"
          />
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="Jolt规范"
            :value="stats.joltSpecs"
            :prefix="h(CodeOutlined)"
            :value-style="{ color: '#cf1322' }"
          />
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="映射关系"
            :value="stats.mappings"
            :prefix="h(LinkOutlined)"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="虚拟表"
            :value="stats.virtualTables"
            :prefix="h(DatabaseOutlined)"
            :value-style="{ color: '#722ed1' }"
          />
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 主要功能区域 -->
    <a-row :gutter="[16, 16]" class="main-content">
      <!-- 系统状态 -->
      <a-col :xs="24" :lg="12">
        <a-card title="系统状态" class="status-card">
          <template #extra>
            <a-button
              type="link"
              size="small"
              :loading="systemStore.loading"
              @click="systemStore.checkHealth"
            >
              <ReloadOutlined />
              刷新
            </a-button>
          </template>
          
          <div class="status-content">
            <div class="status-item">
              <span class="status-label">后端连接:</span>
              <a-badge
                :status="systemStore.isHealthy ? 'success' : 'error'"
                :text="systemStore.connectionStatus"
              />
            </div>
            
            <div class="status-item">
              <span class="status-label">系统版本:</span>
              <span>{{ systemStore.status.version }}</span>
            </div>
            
            <div class="status-item">
              <span class="status-label">最后心跳:</span>
              <span>{{ lastHeartbeatText }}</span>
            </div>
            
            <div class="status-item">
              <span class="status-label">支持功能:</span>
              <div class="features">
                <a-tag
                  v-for="feature in systemStore.status.features"
                  :key="feature"
                  color="blue"
                  class="feature-tag"
                >
                  {{ feature }}
                </a-tag>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      
      <!-- 快速操作 -->
      <a-col :xs="24" :lg="12">
        <a-card title="快速操作" class="quick-actions-card">
          <div class="quick-actions">
            <a-button
              type="primary"
              size="large"
              class="action-button"
              @click="$router.push('/transformation')"
            >
              <SwapOutlined />
              开始转换演示
            </a-button>
            
            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/chart-config')"
            >
              <BarChartOutlined />
              管理图表配置
            </a-button>
            
            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/mapping')"
            >
              <LinkOutlined />
              配置映射关系
            </a-button>
            
            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/virtual-database')"
            >
              <DatabaseOutlined />
              管理虚拟数据
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 最近活动和图表预览 -->
    <a-row :gutter="[16, 16]" class="bottom-content">
      <!-- 最近活动 -->
      <a-col :xs="24" :lg="8">
        <a-card title="最近活动" class="activity-card">
          <a-timeline class="activity-timeline">
            <a-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :color="activity.type === 'success' ? 'green' : activity.type === 'error' ? 'red' : 'blue'"
            >
              <div class="activity-item">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ formatTime(activity.timestamp) }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
      
      <!-- 示例图表预览 -->
      <a-col :xs="24" :lg="16">
        <a-card title="示例图表预览" class="chart-preview-card">
          <template #extra>
            <a-select
              v-model:value="selectedChartType"
              style="width: 150px"
              @change="updatePreviewChart"
            >
              <a-select-option value="line">折线图</a-select-option>
              <a-select-option value="bar">柱状图</a-select-option>
              <a-select-option value="pie">饼图</a-select-option>
            </a-select>
          </template>
          
          <div ref="chartContainer" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, h } from 'vue'
import { useSystemStore } from '@/stores'
import {
  BarChartOutlined,
  CodeOutlined,
  LinkOutlined,
  DatabaseOutlined,
  SwapOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const systemStore = useSystemStore()

// 响应式状态
const stats = ref({
  chartConfigs: 5,
  joltSpecs: 3,
  mappings: 14,
  virtualTables: 2
})

const selectedChartType = ref('line')
const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const recentActivities = ref([
  {
    id: 1,
    title: '系统启动成功',
    timestamp: new Date().toISOString(),
    type: 'success'
  },
  {
    id: 2,
    title: '加载图表配置',
    timestamp: new Date(Date.now() - 60000).toISOString(),
    type: 'info'
  },
  {
    id: 3,
    title: '初始化映射关系',
    timestamp: new Date(Date.now() - 120000).toISOString(),
    type: 'info'
  }
])

// 计算属性
const lastHeartbeatText = computed(() => {
  if (!systemStore.status.lastHeartbeat) return '无'
  return dayjs(systemStore.status.lastHeartbeat).format('YYYY-MM-DD HH:mm:ss')
})

// 方法
const formatTime = (timestamp: string) => {
  return dayjs(timestamp).format('MM-DD HH:mm')
}

const initChart = () => {
  if (!chartContainer.value) return
  
  chartInstance = echarts.init(chartContainer.value)
  updatePreviewChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
}

const updatePreviewChart = () => {
  if (!chartInstance) return
  
  const options = getChartOptions(selectedChartType.value)
  chartInstance.setOption(options)
}

const getChartOptions = (type: string) => {
  const baseData = {
    categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series1: [120, 132, 101, 134, 90, 230, 210],
    series2: [220, 182, 191, 234, 290, 330, 310]
  }
  
  switch (type) {
    case 'line':
      return {
        title: { text: '示例折线图' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['邮件营销', '联盟广告'] },
        xAxis: { type: 'category', data: baseData.categories },
        yAxis: { type: 'value' },
        series: [
          {
            name: '邮件营销',
            type: 'line',
            data: baseData.series1
          },
          {
            name: '联盟广告',
            type: 'line',
            data: baseData.series2
          }
        ]
      }
    
    case 'bar':
      return {
        title: { text: '示例柱状图' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['邮件营销', '联盟广告'] },
        xAxis: { type: 'category', data: baseData.categories },
        yAxis: { type: 'value' },
        series: [
          {
            name: '邮件营销',
            type: 'bar',
            data: baseData.series1
          },
          {
            name: '联盟广告',
            type: 'bar',
            data: baseData.series2
          }
        ]
      }
    
    case 'pie':
      return {
        title: { text: '示例饼图' },
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: '50%',
            data: [
              { value: 1048, name: '搜索引擎' },
              { value: 735, name: '直接访问' },
              { value: 580, name: '邮件营销' },
              { value: 484, name: '联盟广告' },
              { value: 300, name: '视频广告' }
            ]
          }
        ]
      }
    
    default:
      return {}
  }
}

// 生命周期
onMounted(async () => {
  await nextTick()
  initChart()
})
</script>

<style scoped>
.dashboard {
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

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.main-content {
  margin-bottom: 24px;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-label {
  font-weight: 500;
  color: #666;
}

.features {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.feature-tag {
  margin: 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-button {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.bottom-content {
  margin-bottom: 24px;
}

.activity-timeline {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-title {
  font-weight: 500;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
