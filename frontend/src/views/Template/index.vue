<template>
  <div class="template-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>通用模板管理</h2>
      <p class="page-description">管理8个通用模板文件，支持30种图表类型</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索模板"
          style="width: 250px"
          @search="handleSearch"
        />
        <a-select
          v-model:value="categoryFilter"
          placeholder="筛选分类"
          style="width: 150px"
          allowClear
          @change="handleFilter"
        >
          <a-select-option value="CARTESIAN">直角坐标系</a-select-option>
          <a-select-option value="PIE">饼图类</a-select-option>
          <a-select-option value="TREE">层次图类</a-select-option>
          <a-select-option value="RADAR">雷达图类</a-select-option>
          <a-select-option value="GAUGE">仪表盘类</a-select-option>
        </a-select>
        <a-button @click="refreshData" :loading="loading">
          <ReloadOutlined />
          刷新
        </a-button>
        <a-button type="primary" @click="showUploadModal">
          <UploadOutlined />
          上传模板
        </a-button>
      </a-space>
    </div>

    <!-- 简单统计 -->
    <div class="stats-summary">
      <a-space>
        <a-tag color="blue">总计: {{ stats.totalTemplates || 8 }}</a-tag>
        <a-tag color="green">可用: {{ stats.availableTemplates || 0 }}</a-tag>
        <a-tag color="red">缺失: {{ stats.missingTemplates || 0 }}</a-tag>
        <a-tag color="purple">图表类型: {{ stats.totalSupportedChartTypes || 30 }}</a-tag>
      </a-space>
    </div>

    <!-- 主表格 -->
    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="filteredData"
        :loading="loading"
        :pagination="pagination"
        row-key="templateKey"
        size="middle"
        bordered
        :scroll="{ x: 1200 }"
      >
        <!-- 模板名称列 -->
        <template #templateName="{ record }">
          <div class="template-title">{{ record.templateName }}</div>
        </template>

        <!-- 模板键列 -->
        <template #templateKey="{ record }">
          <code class="template-key">{{ record.templateKey }}</code>
        </template>

        <!-- 文件名列 -->
        <template #fileName="{ record }">
          <code class="template-file">{{ record.fileName }}</code>
        </template>

        <!-- 分类列 -->
        <template #category="{ record }">
          <a-tag :color="getCategoryColor(record.templateCategory)">
            {{ getCategoryName(record.templateCategory) }}
          </a-tag>
        </template>

        <!-- 状态列 -->
        <template #status="{ record }">
          <a-tag v-if="record.fileExists" color="success">
            <CheckOutlined />
            可用
          </a-tag>
          <a-tag v-else color="error">
            <CloseOutlined />
            缺失
          </a-tag>
        </template>


        <!-- 文件信息列 -->
        <template #fileInfo="{ record }">
          <div v-if="record.fileExists && record.fileInfo">
            <div class="file-size">{{ formatFileSize(record.fileInfo.size) }}</div>
            <div class="file-time">{{ formatTime(record.fileInfo.lastModified) }}</div>
          </div>
          <div v-else class="no-file">
            <MinusOutlined />
            无文件
          </div>
        </template>

        <!-- 操作列 -->
        <template #actions="{ record }">
          <a-space size="small">
            <a-upload
              :show-upload-list="false"
              :before-upload="(file) => handleUpload(file, record.templateKey)"
              accept=".json"
            >
              <a-button type="primary" size="small">
                <UploadOutlined />
                上传
              </a-button>
            </a-upload>
            <a-button 
              size="small" 
              :disabled="!record.fileExists"
              @click="handleDownload(record.templateKey)"
            >
              <DownloadOutlined />
              下载
            </a-button>
            <a-button 
              size="small"
              :disabled="!record.fileExists"
              @click="handlePreview(record.templateKey)"
            >
              <EyeOutlined />
              预览
            </a-button>
            <a-popconfirm 
              title="确定删除此模板？" 
              @confirm="handleDelete(record.templateKey)"
            >
              <a-button 
                size="small" 
                danger
                :disabled="!record.fileExists"
              >
                <DeleteOutlined />
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="模板预览"
      width="800px"
      :footer="null"
      centered
    >
      <div v-if="previewData">
        <a-descriptions size="small" bordered :column="2" style="margin-bottom: 16px">
          <a-descriptions-item label="模板名称">{{ previewData.displayName }}</a-descriptions-item>
          <a-descriptions-item label="分类">{{ previewData.category }}</a-descriptions-item>
          <a-descriptions-item label="文件名">{{ previewData.fileName }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ formatTime(previewData.timestamp) }}</a-descriptions-item>
        </a-descriptions>
        <div class="json-preview">
          <pre>{{ formatJson(previewData.content) }}</pre>
        </div>
      </div>
    </a-modal>

    <!-- 支持图表类型模态框 -->
    <a-modal
      v-model:open="chartTypesVisible"
      title="支持的图表类型"
      width="600px"
      :footer="null"
      centered
    >
      <div v-if="selectedTemplate">
        <div class="template-header">
          <h4>{{ selectedTemplate.templateName }}</h4>
          <a-tag :color="getCategoryColor(selectedTemplate.templateCategory)">
            {{ selectedTemplate.templateCategory }}
          </a-tag>
        </div>
        <a-list
          :data-source="selectedTemplate.supportedChartTypes || []"
          size="small"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>{{ getChartTypeName(item) }}</template>
                <template #description>
                  <a-tag size="small">{{ item }}</a-tag>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </a-modal>

    <!-- 上传模态框 -->
    <a-modal
      v-model:open="uploadVisible"
      title="上传模板文件"
      width="500px"
      @ok="handleUploadConfirm"
      @cancel="uploadVisible = false"
    >
      <a-upload-dragger
        v-model:fileList="uploadFileList"
        accept=".json"
        :before-upload="() => false"
        @change="handleUploadChange"
      >
        <p class="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽上传JSON模板文件</p>
      </a-upload-dragger>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
  ReloadOutlined,
  UploadOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteOutlined,
  CheckOutlined,
  CloseOutlined,
  MinusOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, ref } from 'vue'
import { universalTemplateApi } from '@/api'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const categoryFilter = ref('')
const tableData = ref<any[]>([])
const stats = ref<any>({})
const previewVisible = ref(false)
const previewData = ref<any>(null)
const chartTypesVisible = ref(false)
const selectedTemplate = ref<any>(null)
const uploadVisible = ref(false)
const uploadFileList = ref<any[]>([])

// 分页配置
const pagination = {
  pageSize: 10,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 个模板`
}

// 表格列配置
const columns = [
  {
    title: '模板名称',
    dataIndex: 'templateName',
    key: 'templateName',
    width: 180,
    slots: { customRender: 'templateName' }
  },
  {
    title: '模板键',
    dataIndex: 'templateKey',
    key: 'templateKey',
    width: 160,
    slots: { customRender: 'templateKey' }
  },
  {
    title: '文件名',
    dataIndex: 'fileName',
    key: 'fileName',
    width: 180,
    slots: { customRender: 'fileName' }
  },
  {
    title: '分类',
    dataIndex: 'templateCategory',
    key: 'templateCategory',
    width: 100,
    slots: { customRender: 'category' },
    align: 'center'
  },
  {
    title: '状态',
    dataIndex: 'fileExists',
    key: 'fileExists',
    width: 80,
    slots: { customRender: 'status' },
    align: 'center'
  },
  {
    title: '文件信息',
    dataIndex: 'fileInfo',
    key: 'fileInfo',
    width: 120,
    slots: { customRender: 'fileInfo' }
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    slots: { customRender: 'actions' },
    fixed: 'right'
  }
]

// 过滤数据
const filteredData = computed(() => {
  let data = tableData.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(item =>
      item.templateName?.toLowerCase().includes(keyword) ||
      item.templateKey?.toLowerCase().includes(keyword) ||
      item.templateCategory?.toLowerCase().includes(keyword)
    )
  }
  
  if (categoryFilter.value) {
    data = data.filter(item => item.templateCategory === categoryFilter.value)
  }
  
  return data
})

// 工具方法
const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    'CARTESIAN': 'blue',
    'PIE': 'orange',
    'TREE': 'purple',
    'RADAR': 'cyan',
    'GAUGE': 'magenta'
  }
  return colors[category] || 'default'
}

const getCategoryName = (category: string) => {
  const names: Record<string, string> = {
    'CARTESIAN': '直角坐标系',
    'PIE': '饼图类',
    'TREE': '层次图类',
    'RADAR': '雷达图类',
    'GAUGE': '仪表盘类'
  }
  return names[category] || category
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  return new Date(timestamp).toLocaleString('zh-CN')
}

const formatJson = (jsonData: any) => {
  if (typeof jsonData === 'string') {
    try {
      return JSON.stringify(JSON.parse(jsonData), null, 2)
    } catch {
      return jsonData
    }
  }
  return JSON.stringify(jsonData, null, 2)
}

const getChartTypeName = (chartType: string) => {
  const nameMap: Record<string, string> = {
    basic_line_chart: '基础折线图',
    smooth_line_chart: '平滑折线图',
    stacked_line_chart: '堆叠折线图',
    step_line_chart: '阶梯折线图',
    basic_bar_chart: '基础柱状图',
    stacked_bar_chart: '堆叠柱状图',
    horizontal_bar_chart: '水平柱状图',
    grouped_bar_chart: '分组柱状图',
    basic_area_chart: '基础面积图',
    stacked_area_chart: '堆叠面积图',
    scatter_chart: '散点图',
    bubble_chart: '气泡图',
    basic_pie_chart: '基础饼图',
    doughnut_chart: '环形图',
    rose_chart: '玫瑰图',
    pie_chart: '饼图',
    nested_pie_chart: '嵌套饼图',
    sunburst_chart: '旭日图',
    treemap_chart: '矩形树图',
    funnel_chart: '漏斗图',
    basic_radar_chart: '基础雷达图',
    filled_radar_chart: '填充雷达图',
    polar_chart: '极坐标图',
    radar_multiple_chart: '多系列雷达图',
    basic_gauge_chart: '基础仪表盘',
    progress_gauge_chart: '进度仪表盘',
    grade_gauge_chart: '等级仪表盘',
    speedometer_chart: '速度表图',
    thermometer_chart: '温度计图',
    ring_progress_chart: '环形进度图'
  }
  return nameMap[chartType] || chartType
}

// API调用方法
const refreshData = async () => {
  loading.value = true
  try {
    const response = await universalTemplateApi.getTable()
    tableData.value = response.data || []
    stats.value = response.statistics || {}
    message.success('数据加载成功')
  } catch (error) {
    message.error('数据加载失败')
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = (value: string) => {
  searchKeyword.value = value
}

const handleFilter = (value: string) => {
  categoryFilter.value = value
}

const handleUpload = async (file: File, templateKey: string) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    await universalTemplateApi.upload(templateKey, formData)
    message.success(`模板 ${templateKey} 上传成功`)
    refreshData()
  } catch (error) {
    message.error(`上传失败`)
  }
  return false
}

const handleDownload = async (templateKey: string) => {
  try {
    const downloadUrl = universalTemplateApi.downloadUrl(templateKey)
    const response = await fetch(downloadUrl)
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${templateKey}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败')
  }
}

const handlePreview = async (templateKey: string) => {
  try {
    const response = await universalTemplateApi.preview(templateKey)
    previewData.value = response
    previewVisible.value = true
  } catch (error) {
    message.error('预览失败')
  }
}

const handleDelete = async (templateKey: string) => {
  try {
    await universalTemplateApi.delete(templateKey)
    message.success('删除成功')
    refreshData()
  } catch (error) {
    message.error('删除失败')
  }
}

const showChartTypes = (template: any) => {
  selectedTemplate.value = template
  chartTypesVisible.value = true
}

const showUploadModal = () => {
  uploadVisible.value = true
  uploadFileList.value = []
}

const handleUploadChange = (info: any) => {
  uploadFileList.value = info.fileList
}

const handleUploadConfirm = async () => {
  if (uploadFileList.value.length === 0) {
    message.warning('请选择文件')
    return
  }

  try {
    for (const fileItem of uploadFileList.value) {
      const file = fileItem.originFileObj
      const fileName = file.name.replace('.json', '')
      const formData = new FormData()
      formData.append('file', file)
      await universalTemplateApi.upload(fileName, formData)
    }
    message.success('批量上传成功')
    uploadVisible.value = false
    uploadFileList.value = []
    refreshData()
  } catch (error) {
    message.error('上传失败')
  }
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.template-management {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header h2 {
  margin: 0 0 8px;
  color: #1890ff;
  font-size: 24px;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.toolbar {
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stats-summary {
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.template-title {
  font-weight: bold;
  color: #1890ff;
  font-size: 14px;
}

.template-key {
  color: #666;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #e8e8e8;
}

.template-file {
  color: #666;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  background: #f0f2ff;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #d4e3ff;
}


.file-size {
  font-weight: bold;
  color: #1890ff;
  font-size: 12px;
}

.file-time {
  color: #666;
  font-size: 11px;
}

.no-file {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.json-preview {
  background: #f5f5f5;
  border-radius: 4px;
  max-height: 400px;
  overflow: auto;
}

.json-preview pre {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  padding: 16px;
  background: #2d3748;
  color: #e2e8f0;
  border-radius: 4px;
}

.template-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.template-header h4 {
  margin: 0;
  color: #1890ff;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .template-management {
    padding: 12px;
  }
  
  .toolbar {
    padding: 12px;
  }
  
  .page-header {
    padding: 16px;
  }
}
</style>