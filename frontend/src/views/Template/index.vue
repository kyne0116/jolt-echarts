<template>
  <div class="template-file-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <DatabaseOutlined class="title-icon" />
            模板文件管理
          </h1>
          <p class="page-subtitle">管理ECharts图表模板的JSON文件和JOLT转换规范文件</p>
        </div>
        <div class="header-actions">
          <a-space size="middle">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索图表类型或名称"
              style="width: 300px"
              @search="handleSearch"
            >
              <template #enterButton>
                <SearchOutlined />
              </template>
            </a-input-search>
            <a-select
              v-model:value="statusFilter"
              placeholder="筛选状态"
              style="width: 120px"
              allowClear
              @change="handleStatusFilter"
            >
              <a-select-option value="completed">完整实现</a-select-option>
              <a-select-option value="development">开发中</a-select-option>
              <a-select-option value="pending">待开发</a-select-option>
            </a-select>
            <a-select
              v-model:value="categoryFilter"
              placeholder="筛选分类"
              style="width: 120px"
              allowClear
              @change="handleCategoryFilter"
            >
              <a-select-option value="CARTESIAN">直角坐标系</a-select-option>
              <a-select-option value="PIE">饼图类</a-select-option>
              <a-select-option value="RADAR">雷达图类</a-select-option>
              <a-select-option value="GAUGE">仪表盘类</a-select-option>
            </a-select>
            <a-button @click="refreshData" :loading="loading">
              <ReloadOutlined />
              刷新
            </a-button>
          </a-space>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card size="small">
            <a-statistic 
              title="图表类型总数" 
              :value="stats.total" 
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <AppstoreOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic 
              title="完整实现" 
              :value="stats.completed" 
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <CheckCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic 
              title="开发中" 
              :value="stats.development" 
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small">
            <a-statistic 
              title="待开发" 
              :value="stats.pending" 
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <ExclamationCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <a-card>
        <a-table
          :columns="columns"
          :data-source="filteredData"
          :pagination="pagination"
          :loading="loading"
          row-key="chartType"
          size="middle"
          :scroll="{ x: 1200 }"
        >
          <!-- 图表类型列 -->
          <template #chartType="{ record }">
            <div class="chart-type-cell">
              <a-tag :color="getCategoryColor(record.category)" size="small">
                {{ record.category }}
              </a-tag>
              <div>
                <div class="chart-name">{{ record.chartName }}</div>
                <div class="chart-id">{{ record.chartType }}</div>
              </div>
            </div>
          </template>

          <!-- 实现状态列 -->
          <template #status="{ record }">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- JSON模板文件列 -->
          <template #jsonTemplate="{ record }">
            <div class="file-cell">
              <div class="file-info">
                <span class="file-name">{{ record.jsonTemplate?.fileName || '未设置' }}</span>
                <div v-if="record.jsonTemplate?.exists" class="file-meta">
                  <span class="file-size">{{ formatFileSize(record.jsonTemplate?.size) }}</span>
                  <span class="file-time">{{ formatTime(record.jsonTemplate?.lastModified) }}</span>
                </div>
                <span v-else class="file-missing">文件不存在</span>
              </div>
              <div class="file-actions">
                <a-space size="small">
                  <a-tooltip title="上传文件">
                    <a-upload
                      :show-upload-list="false"
                      :before-upload="(file) => handleJsonUpload(file, record.chartType)"
                      accept=".json"
                    >
                      <a-button type="text" size="small">
                        <UploadOutlined />
                      </a-button>
                    </a-upload>
                  </a-tooltip>
                  <a-tooltip title="下载文件">
                    <a-button 
                      type="text" 
                      size="small" 
                      :disabled="!record.jsonTemplate?.exists"
                      @click="handleJsonDownload(record.chartType)"
                    >
                      <DownloadOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="预览内容">
                    <a-button 
                      type="text" 
                      size="small"
                      :disabled="!record.jsonTemplate?.exists"
                      @click="handleJsonPreview(record.chartType)"
                    >
                      <EyeOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-popconfirm 
                    title="确定删除此文件?" 
                    @confirm="handleJsonDelete(record.chartType)"
                  >
                    <a-button 
                      type="text" 
                      size="small" 
                      danger
                      :disabled="!record.jsonTemplate?.exists"
                    >
                      <DeleteOutlined />
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </div>
            </div>
          </template>

          <!-- JOLT规范文件列 -->
          <template #joltSpec="{ record }">
            <div class="file-cell">
              <div class="file-info">
                <span class="file-name">{{ record.joltSpec?.fileName || '未设置' }}</span>
                <div v-if="record.joltSpec?.exists" class="file-meta">
                  <span class="file-size">{{ formatFileSize(record.joltSpec?.size) }}</span>
                  <span class="file-time">{{ formatTime(record.joltSpec?.lastModified) }}</span>
                </div>
                <span v-else class="file-missing">文件不存在</span>
              </div>
              <div class="file-actions">
                <a-space size="small">
                  <a-tooltip title="上传文件">
                    <a-upload
                      :show-upload-list="false"
                      :before-upload="(file) => handleJoltUpload(file, record.chartType)"
                      accept=".json"
                    >
                      <a-button type="text" size="small">
                        <UploadOutlined />
                      </a-button>
                    </a-upload>
                  </a-tooltip>
                  <a-tooltip title="下载文件">
                    <a-button 
                      type="text" 
                      size="small"
                      :disabled="!record.joltSpec?.exists"
                      @click="handleJoltDownload(record.chartType)"
                    >
                      <DownloadOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="预览内容">
                    <a-button 
                      type="text" 
                      size="small"
                      :disabled="!record.joltSpec?.exists"
                      @click="handleJoltPreview(record.chartType)"
                    >
                      <EyeOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-popconfirm 
                    title="确定删除此文件?" 
                    @confirm="handleJoltDelete(record.chartType)"
                  >
                    <a-button 
                      type="text" 
                      size="small" 
                      danger
                      :disabled="!record.joltSpec?.exists"
                    >
                      <DeleteOutlined />
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </div>
            </div>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 文件预览模态框 -->
    <a-modal
      v-model:open="previewModalVisible"
      :title="`文件预览 - ${previewData?.fileName}`"
      width="800px"
      :footer="null"
    >
      <div v-if="previewData" class="preview-content">
        <div class="preview-meta">
          <a-descriptions size="small" :column="2" bordered>
            <a-descriptions-item label="文件名">{{ previewData.fileName }}</a-descriptions-item>
            <a-descriptions-item label="文件大小">{{ formatFileSize(previewData.size) }}</a-descriptions-item>
            <a-descriptions-item label="修改时间" :span="2">
              {{ formatTime(previewData.lastModified) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
        <a-divider />
        <div class="preview-json">
          <pre class="json-content">{{ previewData.rawContent }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  DownloadOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  ReloadOutlined,
  SearchOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, ref } from 'vue'
import { request } from '@/api'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const tableData = ref<any[]>([])
const previewModalVisible = ref(false)
const previewData = ref<any>(null)

// 表格列配置
const columns = [
  {
    title: '图表类型',
    dataIndex: 'chartType',
    key: 'chartType',
    width: 200,
    slots: { customRender: 'chartType' },
    sorter: (a: any, b: any) => a.chartName.localeCompare(b.chartName)
  },
  {
    title: '实现状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    slots: { customRender: 'status' },
    filters: [
      { text: '完整实现', value: 'completed' },
      { text: '开发中', value: 'development' },
      { text: '待开发', value: 'pending' }
    ],
    onFilter: (value: string, record: any) => record.status === value
  },
  {
    title: 'JSON模板文件',
    dataIndex: 'jsonTemplate',
    key: 'jsonTemplate',
    width: 300,
    slots: { customRender: 'jsonTemplate' }
  },
  {
    title: 'JOLT规范文件',
    dataIndex: 'joltSpec',
    key: 'joltSpec',
    width: 300,
    slots: { customRender: 'joltSpec' }
  }
]

// 分页配置
const pagination = {
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
}

// 计算属性
const filteredData = computed(() => {
  let data = [...tableData.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(item =>
      item.chartName.toLowerCase().includes(keyword) ||
      item.chartType.toLowerCase().includes(keyword)
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    data = data.filter(item => item.status === statusFilter.value)
  }
  
  // 分类过滤
  if (categoryFilter.value) {
    data = data.filter(item => item.category === categoryFilter.value)
  }
  
  return data
})

const stats = computed(() => {
  const total = tableData.value.length
  const completed = tableData.value.filter(item => item.status === 'completed').length
  const development = tableData.value.filter(item => item.status === 'development').length
  const pending = tableData.value.filter(item => item.status === 'pending').length
  
  return { total, completed, development, pending }
})

// 工具方法
const getCategoryColor = (category: string) => {
  const colorMap: Record<string, string> = {
    CARTESIAN: '#1890ff',
    PIE: '#fa8c16',
    RADAR: '#722ed1',
    GAUGE: '#13c2c2'
  }
  return colorMap[category] || '#d9d9d9'
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    completed: '#52c41a',
    development: '#1890ff',
    pending: '#faad14'
  }
  return colorMap[status] || '#d9d9d9'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    completed: '完整实现',
    development: '开发中',
    pending: '待开发'
  }
  return textMap[status] || '未知'
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  return new Date(timestamp).toLocaleString()
}

// 事件处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
}

const handleStatusFilter = () => {
  // 过滤逻辑已在计算属性中实现
}

const handleCategoryFilter = () => {
  // 过滤逻辑已在计算属性中实现
}

const refreshData = async () => {
  loading.value = true
  try {
    const response = await request.get('/template-files/table')
    tableData.value = response.data || []
    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据加载失败')
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

// JSON模板文件操作
const handleJsonUpload = async (file: File, chartType: string) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('chartType', chartType)
    
    await request.post('/template-files/json-template/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    message.success('JSON模板文件上传成功')
    refreshData()
  } catch (error) {
    message.error('JSON模板文件上传失败')
  }
  return false // 阻止默认上传
}

const handleJsonDownload = async (chartType: string) => {
  try {
    const response = await fetch(`/api/template-files/json-template/download?chartType=${chartType}`)
    if (response.ok) {
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      const contentDisposition = response.headers.get('content-disposition')
      const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `${chartType}-template.json`
      a.href = url
      a.download = fileName
      a.click()
      window.URL.revokeObjectURL(url)
      message.success('文件下载成功')
    } else {
      message.error('文件下载失败')
    }
  } catch (error) {
    message.error('文件下载失败')
  }
}

const handleJsonPreview = async (chartType: string) => {
  try {
    const response = await request.get(`/template-files/json-template/preview?chartType=${chartType}`)
    previewData.value = response
    previewModalVisible.value = true
  } catch (error) {
    message.error('文件预览失败')
  }
}

const handleJsonDelete = async (chartType: string) => {
  try {
    await request.delete(`/template-files/json-template?chartType=${chartType}`)
    message.success('JSON模板文件删除成功')
    refreshData()
  } catch (error) {
    message.error('JSON模板文件删除失败')
  }
}

// JOLT规范文件操作
const handleJoltUpload = async (file: File, chartType: string) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('chartType', chartType)
    
    await request.post('/template-files/jolt-spec/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    message.success('JOLT规范文件上传成功')
    refreshData()
  } catch (error) {
    message.error('JOLT规范文件上传失败')
  }
  return false // 阻止默认上传
}

const handleJoltDownload = async (chartType: string) => {
  try {
    const response = await fetch(`/api/template-files/jolt-spec/download?chartType=${chartType}`)
    if (response.ok) {
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      const contentDisposition = response.headers.get('content-disposition')
      const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `${chartType}-spec.json`
      a.href = url
      a.download = fileName
      a.click()
      window.URL.revokeObjectURL(url)
      message.success('文件下载成功')
    } else {
      message.error('文件下载失败')
    }
  } catch (error) {
    message.error('文件下载失败')
  }
}

const handleJoltPreview = async (chartType: string) => {
  try {
    const response = await request.get(`/template-files/jolt-spec/preview?chartType=${chartType}`)
    previewData.value = response
    previewModalVisible.value = true
  } catch (error) {
    message.error('文件预览失败')
  }
}

const handleJoltDelete = async (chartType: string) => {
  try {
    await request.delete(`/template-files/jolt-spec?chartType=${chartType}`)
    message.success('JOLT规范文件删除成功')
    refreshData()
  } catch (error) {
    message.error('JOLT规范文件删除失败')
  }
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.template-file-management {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.page-header {
  background: white;
  padding: 24px;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1600px;
  margin: 0 auto;
}

.title-section {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #1890ff;
}

.page-subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.header-actions {
  flex-shrink: 0;
}

.stats-cards {
  padding: 16px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
}

.table-container {
  flex: 1;
  padding: 0 16px 16px 16px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  min-height: 0;
}

.chart-type-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-name {
  font-weight: 500;
  color: #262626;
  font-size: 14px;
}

.chart-id {
  font-size: 12px;
  color: #999;
  font-family: 'Monaco', 'Menlo', monospace;
}

.file-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: block;
  font-weight: 500;
  color: #262626;
  font-size: 13px;
  font-family: 'Monaco', 'Menlo', monospace;
  word-break: break-all;
}

.file-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.file-size,
.file-time {
  font-size: 11px;
  color: #999;
}

.file-missing {
  color: #ff4d4f;
  font-size: 12px;
  font-style: italic;
}

.file-actions {
  flex-shrink: 0;
}

.preview-content {
  max-height: 600px;
  overflow: hidden;
}

.preview-meta {
  margin-bottom: 16px;
}

.preview-json {
  max-height: 450px;
  overflow: auto;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: #fafafa;
}

.json-content {
  padding: 16px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #262626;
  background: transparent;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .stats-cards .ant-col {
    margin-bottom: 16px;
  }
  
  .file-cell {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .file-actions {
    justify-content: center;
  }
}

/* 表格样式优化 */
:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>