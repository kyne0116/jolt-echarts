<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🔗 占位符映射管理系统</h1>
      <p class="page-subtitle">智能数据映射 · 零代码配置 · 实时预览</p>
      <div class="feature-highlights">
        <a-tag color="blue">虚拟数据库集成</a-tag>
        <a-tag color="green">两阶段转换</a-tag>
        <a-tag color="gold">智能推荐</a-tag>
        <a-tag color="purple">实时同步</a-tag>
      </div>
    </div>
    
    <!-- 核心功能统计 -->
    <a-row :gutter="[16, 16]" class="stats-cards">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card featured-stat">
          <a-statistic
            title="活跃映射配置"
            :value="stats.activeMappings"
            :prefix="h(LinkOutlined)"
            :value-style="{ color: '#1890ff', fontWeight: 'bold' }"
            suffix="个"
          />
          <div class="stat-description">已配置的占位符映射关系</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="虚拟数据记录"
            :value="stats.dataRecords"
            :prefix="h(DatabaseOutlined)"
            :value-style="{ color: '#52c41a' }"
            suffix="条"
          />
          <div class="stat-description">统一数据视图中的记录数</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="支持图表类型"
            :value="stats.chartTypes"
            :prefix="h(SwapOutlined)"
            :value-style="{ color: '#722ed1' }"
            suffix="种"
          />
          <div class="stat-description">可进行映射配置的图表类型</div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card">
          <a-statistic
            title="转换成功率"
            :value="stats.successRate"
            :value-style="{ color: '#fa8c16' }"
            suffix="%"
          />
          <div class="stat-description">映射转换的成功率</div>
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
      
      <!-- 核心功能入口 -->
      <a-col :xs="24" :lg="12">
        <a-card title="🚀 核心功能入口" class="quick-actions-card">
          <div class="quick-actions">
            <a-button
              type="primary"
              size="large"
              class="action-button featured-action"
              @click="$router.push('/mapping')"
            >
              <LinkOutlined />
              占位符映射管理
              <a-tag color="gold" size="small" class="action-tag">核心</a-tag>
            </a-button>

            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/virtual-database')"
            >
              <DatabaseOutlined />
              虚拟数据库管理
            </a-button>

            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/transformation')"
            >
              <SwapOutlined />
              两阶段转换演示
            </a-button>

            <a-button
              size="large"
              class="action-button"
              @click="$router.push('/template')"
            >
              <FileTextOutlined />
              模板管理
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 最近活动和图表预览 -->
    <a-row :gutter="[16, 16]" class="bottom-content">
      <!-- 映射管理活动 -->
      <a-col :xs="24" :lg="8">
        <a-card title="📋 映射管理活动" class="activity-card">
          <template #extra>
            <a-button type="link" size="small" @click="refreshActivities">
              <ReloadOutlined />
              刷新
            </a-button>
          </template>
          <a-timeline class="activity-timeline">
            <a-timeline-item
              v-for="activity in mappingActivities"
              :key="activity.id"
              :color="activity.type === 'success' ? 'green' : activity.type === 'error' ? 'red' : 'blue'"
            >
              <div class="activity-item">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-description">{{ activity.description }}</div>
                <div class="activity-time">{{ formatTime(activity.timestamp) }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
      
      <!-- 映射效果预览 -->
      <a-col :xs="24" :lg="16">
        <a-card title="📊 映射效果预览" class="chart-preview-card">
          <template #extra>
            <a-space>
              <a-select
                v-model:value="selectedChartType"
                style="width: 180px"
                @change="updatePreviewChart"
              >
                <a-select-option value="stacked_line">堆叠折线图</a-select-option>
                <a-select-option value="basic_bar">基础柱状图</a-select-option>
                <a-select-option value="pie">饼图</a-select-option>
              </a-select>
              <a-button type="primary" size="small" @click="$router.push('/mapping')">
                配置映射
              </a-button>
            </a-space>
          </template>

          <div class="chart-preview-content">
            <div class="preview-description">
              <a-alert
                message="映射演示"
                :description="getChartDescription(selectedChartType)"
                type="info"
                show-icon
                class="preview-alert"
              />
            </div>
            <div ref="chartContainer" class="chart-container"></div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { useSystemStore } from '@/stores'
import {
    DatabaseOutlined,
    FileTextOutlined,
    LinkOutlined,
    ReloadOutlined,
    SwapOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { computed, h, nextTick, onMounted, ref } from 'vue'

const systemStore = useSystemStore()

// 响应式状态
const stats = ref({
  activeMappings: 12,
  dataRecords: 350,
  chartTypes: 5,
  successRate: 98.5
})

const selectedChartType = ref('stacked_line')
const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const mappingActivities = ref([
  {
    id: 1,
    title: '堆叠折线图映射配置成功',
    description: '配置了7个占位符的映射关系',
    timestamp: new Date().toISOString(),
    type: 'success'
  },
  {
    id: 2,
    title: '虚拟数据库同步完成',
    description: '同步了350条数据记录',
    timestamp: new Date(Date.now() - 300000).toISOString(),
    type: 'success'
  },
  {
    id: 3,
    title: '智能推荐映射生成',
    description: '为饼图生成了默认映射配置',
    timestamp: new Date(Date.now() - 600000).toISOString(),
    type: 'info'
  },
  {
    id: 4,
    title: '映射预览测试通过',
    description: '基础柱状图映射验证成功',
    timestamp: new Date(Date.now() - 900000).toISOString(),
    type: 'success'
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

const refreshActivities = () => {
  // 模拟刷新活动数据
  console.log('刷新映射管理活动')
}

const getChartDescription = (chartType: string) => {
  const descriptions = {
    'stacked_line': '展示多个数据系列的堆叠效果，通过占位符映射实现不同渠道数据的动态绑定',
    'basic_bar': '基础柱状图展示，演示单一数据系列的映射配置和数据聚合效果',
    'pie': '饼图数据映射，展示分类数据的占位符替换和百分比计算'
  }
  return descriptions[chartType] || '选择图表类型查看映射说明'
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
  const mappingData = {
    categories: ['1月', '2月', '3月', '4月', '5月'],
    emailData: [12500, 13200, 11800, 14500, 15200],
    socialData: [8900, 9500, 8200, 10200, 11100],
    directData: [15600, 16200, 14800, 17100, 18300]
  }

  switch (type) {
    case 'stacked_line':
      return {
        title: { text: '营销渠道分析（映射演示）' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['Email', 'Social Media', 'Direct'] },
        xAxis: { type: 'category', data: mappingData.categories },
        yAxis: { type: 'value' },
        series: [
          {
            name: 'Email',
            type: 'line',
            stack: '营销渠道',
            data: mappingData.emailData,
            smooth: true
          },
          {
            name: 'Social Media',
            type: 'line',
            stack: '营销渠道',
            data: mappingData.socialData,
            smooth: true
          },
          {
            name: 'Direct',
            type: 'line',
            stack: '营销渠道',
            data: mappingData.directData,
            smooth: true
          }
        ]
      }

    case 'basic_bar':
      return {
        title: { text: '月度销售额（映射演示）' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: mappingData.categories },
        yAxis: { type: 'value' },
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: mappingData.emailData,
            itemStyle: {
              color: '#1890ff'
            }
          }
        ]
      }

    case 'pie':
      return {
        title: { text: '渠道占比（映射演示）' },
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [
          {
            name: '渠道占比',
            type: 'pie',
            radius: '60%',
            data: [
              { value: 65200, name: 'Email营销' },
              { value: 45900, name: 'Social Media' },
              { value: 82000, name: 'Direct访问' },
              { value: 28500, name: 'Search引擎' },
              { value: 19800, name: 'Video广告' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
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
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #1890ff, #722ed1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

.feature-highlights {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.featured-stat {
  border: 2px solid #1890ff;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.05), rgba(24, 144, 255, 0.02));
}

.stat-description {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
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
  position: relative;
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.featured-action {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border: none;
  color: white;
  font-weight: 600;
}

.featured-action:hover {
  background: linear-gradient(135deg, #40a9ff, #1890ff);
  color: white;
}

.action-tag {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 10px;
  transform: scale(0.8);
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
  font-weight: 600;
  color: #262626;
}

.activity-description {
  font-size: 13px;
  color: #666;
  margin: 2px 0;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.chart-preview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-description {
  margin-bottom: 16px;
}

.preview-alert {
  border-radius: 8px;
}

.chart-container {
  height: 320px;
  width: 100%;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
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
