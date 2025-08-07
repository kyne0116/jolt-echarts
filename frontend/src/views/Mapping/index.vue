<template>
  <div class="mapping-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>占位符映射管理</h1>
      <p>管理占位符与虚拟数据库表列的映射关系</p>
    </div>
    
    <!-- 工具栏 -->
    <a-card class="toolbar" title="映射关系概览">
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <PlusOutlined />
            新增映射
          </a-button>
          <a-button @click="refreshMappings">
            <ReloadOutlined />
            刷新
          </a-button>
          <a-button @click="exportMappings">
            <ExportOutlined />
            导出
          </a-button>
        </a-space>
      </template>
      
      <div class="toolbar-content">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="图表类型" :value="chartTypes.length" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="总映射数" :value="totalMappings" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="占位符数" :value="totalPlaceholders" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="数据表数" :value="uniqueTables.length" />
          </a-col>
        </a-row>
      </div>
    </a-card>
    
    <!-- 图表类型选择 -->
    <a-card class="chart-selector" title="选择图表类型">
      <a-radio-group
        v-model:value="selectedChartId"
        button-style="solid"
        size="large"
        @change="handleChartChange"
      >
        <a-radio-button
          v-for="chart in chartTypes"
          :key="chart.id"
          :value="chart.id"
        >
          <component :is="chart.icon" />
          {{ chart.name }}
        </a-radio-button>
      </a-radio-group>
    </a-card>
    
    <!-- 映射关系表格 -->
    <a-card class="mapping-table" :title="`${currentChartName} - 映射关系`">
      <template #extra>
        <a-space>
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索占位符或表名"
            style="width: 200px"
            @search="handleSearch"
          />
          <a-button
            type="primary"
            ghost
            @click="showBatchEditModal"
            :disabled="selectedRowKeys.length === 0"
          >
            批量编辑
          </a-button>
        </a-space>
      </template>
      
      <a-table
        :columns="tableColumns"
        :data-source="filteredMappings"
        :row-selection="rowSelection"
        :loading="loading"
        :pagination="paginationConfig"
        row-key="placeholderName"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'placeholderName'">
            <a-tag color="blue" class="placeholder-tag">
              {{ record.placeholderName }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'dataType'">
            <a-tag :color="getDataTypeColor(record.dataType)">
              {{ record.dataType }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'queryConditions'">
            <a-tooltip v-if="record.queryConditions" :title="JSON.stringify(record.queryConditions, null, 2)">
              <a-tag color="orange">有条件</a-tag>
            </a-tooltip>
            <span v-else class="text-muted">无</span>
          </template>
          
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="editMapping(record)">
                <EditOutlined />
                编辑
              </a-button>
              <a-button type="link" size="small" @click="testMapping(record)">
                <PlayCircleOutlined />
                测试
              </a-button>
              <a-popconfirm
                title="确定要删除这个映射关系吗？"
                @confirm="deleteMapping(record)"
              >
                <a-button type="link" size="small" danger>
                  <DeleteOutlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 映射关系可视化 -->
    <a-card class="mapping-visualization" title="映射关系可视化">
      <div ref="visualizationContainer" class="visualization-container">
        <!-- 这里可以添加映射关系的可视化图表 -->
        <a-empty description="映射关系可视化图表（待实现）" />
      </div>
    </a-card>
    
    <!-- 创建/编辑映射模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="占位符名称" name="placeholderName">
          <a-input
            v-model:value="formData.placeholderName"
            placeholder="例如: ${chart_title}"
            :disabled="isEditMode"
          />
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据表名" name="tableName">
              <a-select
                v-model:value="formData.tableName"
                placeholder="选择数据表"
                show-search
                :filter-option="filterOption"
                :get-popup-container="(triggerNode) => triggerNode.parentNode"
              >
                <a-select-option
                  v-for="table in availableTables"
                  :key="table"
                  :value="table"
                >
                  {{ table }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="列名" name="columnName">
              <a-input
                v-model:value="formData.columnName"
                placeholder="例如: title"
              />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据类型" name="dataType">
              <a-select
                v-model:value="formData.dataType"
                placeholder="选择数据类型"
                :get-popup-container="(triggerNode) => triggerNode.parentNode"
              >
                <a-select-option value="string">字符串</a-select-option>
                <a-select-option value="number">数字</a-select-option>
                <a-select-option value="array">数组</a-select-option>
                <a-select-option value="object">对象</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="聚合类型" name="aggregationType">
              <a-select
                v-model:value="formData.aggregationType"
                placeholder="选择聚合类型（可选）"
                allow-clear
              >
                <a-select-option value="SUM">求和</a-select-option>
                <a-select-option value="AVG">平均值</a-select-option>
                <a-select-option value="COUNT">计数</a-select-option>
                <a-select-option value="MAX">最大值</a-select-option>
                <a-select-option value="MIN">最小值</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="查询条件" name="queryConditions">
          <a-textarea
            v-model:value="queryConditionsText"
            placeholder="JSON格式的查询条件（可选）"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 测试结果模态框 -->
    <a-modal
      v-model:open="testModalVisible"
      title="映射测试结果"
      width="800px"
      :footer="null"
    >
      <div class="test-result">
        <h4>测试映射: {{ testingMapping?.placeholderName }}</h4>
        <a-descriptions bordered size="small">
          <a-descriptions-item label="数据表">{{ testingMapping?.tableName }}</a-descriptions-item>
          <a-descriptions-item label="列名">{{ testingMapping?.columnName }}</a-descriptions-item>
          <a-descriptions-item label="数据类型">{{ testingMapping?.dataType }}</a-descriptions-item>
        </a-descriptions>
        
        <h4 style="margin-top: 16px;">模拟查询结果:</h4>
        <vue-json-pretty
          v-if="testResult"
          :data="testResult"
          :show-length="true"
          :show-line="true"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { twoStageApi } from '@/api'
import type { FieldMapping } from '@/types'
import {
    BarChartOutlined,
    DeleteOutlined,
    EditOutlined,
    ExportOutlined,
    LineChartOutlined,
    PieChartOutlined,
    PlayCircleOutlined,
    PlusOutlined,
    ReloadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import VueJsonPretty from 'vue-json-pretty'

// 响应式状态
const selectedChartId = ref('stacked_line_chart')
const searchKeyword = ref('')
const loading = ref(false)
const modalVisible = ref(false)
const testModalVisible = ref(false)
const selectedRowKeys = ref<string[]>([])

const mappingsData = ref<Record<string, FieldMapping>>({})
const testingMapping = ref<FieldMapping | null>(null)
const testResult = ref<any>(null)

// 表单相关
const formRef = ref()
const formData = reactive<Partial<FieldMapping>>({})
const queryConditionsText = ref('')
const isEditMode = ref(false)

// 图表类型配置
const chartTypes = [
  { id: 'stacked_line_chart', name: '堆叠折线图', icon: LineChartOutlined },
  { id: 'basic_bar_chart', name: '基础柱状图', icon: BarChartOutlined },
  { id: 'pie_chart', name: '饼图', icon: PieChartOutlined }
]

// 可用数据表
const availableTables = [
  'marketing_data',
  'chart_config',
  'sales_data',
  'user_behavior',
  'product_info'
]

// 计算属性
const currentChartName = computed(() => {
  const chart = chartTypes.find(c => c.id === selectedChartId.value)
  return chart?.name || '未知图表'
})

const filteredMappings = computed(() => {
  const mappings = Object.values(mappingsData.value)
  if (!searchKeyword.value) return mappings
  
  return mappings.filter(mapping =>
    mapping.placeholderName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    mapping.tableName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    mapping.columnName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const totalMappings = computed(() => Object.keys(mappingsData.value).length)

const totalPlaceholders = computed(() => {
  return new Set(Object.values(mappingsData.value).map(m => m.placeholderName)).size
})

const uniqueTables = computed(() => {
  return [...new Set(Object.values(mappingsData.value).map(m => m.tableName))]
})

const modalTitle = computed(() => isEditMode.value ? '编辑映射关系' : '新增映射关系')

// 表格配置
const tableColumns = [
  {
    title: '占位符名称',
    dataIndex: 'placeholderName',
    key: 'placeholderName',
    width: 200,
    fixed: 'left'
  },
  {
    title: '数据表',
    dataIndex: 'tableName',
    key: 'tableName',
    width: 150
  },
  {
    title: '列名',
    dataIndex: 'columnName',
    key: 'columnName',
    width: 120
  },
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'dataType',
    width: 100
  },
  {
    title: '聚合类型',
    dataIndex: 'aggregationType',
    key: 'aggregationType',
    width: 100
  },
  {
    title: '查询条件',
    dataIndex: 'queryConditions',
    key: 'queryConditions',
    width: 120
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}

const paginationConfig = {
  current: 1,
  pageSize: 10,
  total: computed(() => filteredMappings.value.length),
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
}

// 表单验证规则
const formRules = {
  placeholderName: [
    { required: true, message: '请输入占位符名称' },
    { pattern: /^\$\{[^}]+\}$/, message: '占位符格式应为 ${variable_name}' }
  ],
  tableName: [
    { required: true, message: '请选择数据表' }
  ],
  columnName: [
    { required: true, message: '请输入列名' }
  ],
  dataType: [
    { required: true, message: '请选择数据类型' }
  ]
}

// 方法
const loadMappings = async () => {
  loading.value = true
  try {
    const response = await twoStageApi.getMappings(selectedChartId.value)
    mappingsData.value = response.mappings || {}
    console.log('映射关系加载成功:', response)
  } catch (error: any) {
    message.error(`加载映射关系失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

const handleChartChange = () => {
  loadMappings()
}

const refreshMappings = () => {
  loadMappings()
  message.success('映射关系已刷新')
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
}

const getDataTypeColor = (dataType: string) => {
  const colors: Record<string, string> = {
    string: 'green',
    number: 'blue',
    array: 'orange',
    object: 'purple'
  }
  return colors[dataType] || 'default'
}

const showCreateModal = () => {
  isEditMode.value = false
  resetForm()
  modalVisible.value = true
}

const editMapping = (record: FieldMapping) => {
  isEditMode.value = true
  Object.assign(formData, record)
  queryConditionsText.value = record.queryConditions 
    ? JSON.stringify(record.queryConditions, null, 2) 
    : ''
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(formData, {
    placeholderName: '',
    tableName: '',
    columnName: '',
    dataType: '',
    aggregationType: '',
    queryConditions: undefined
  })
  queryConditionsText.value = ''
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    
    // 解析查询条件
    let queryConditions = undefined
    if (queryConditionsText.value.trim()) {
      try {
        queryConditions = JSON.parse(queryConditionsText.value)
      } catch (error) {
        message.error('查询条件JSON格式不正确')
        return
      }
    }
    
    const mappingData: FieldMapping = {
      ...formData as FieldMapping,
      queryConditions
    }
    
    // 更新本地数据（实际项目中应该调用API）
    mappingsData.value[mappingData.placeholderName] = mappingData
    
    modalVisible.value = false
    message.success(isEditMode.value ? '映射关系更新成功' : '映射关系创建成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const deleteMapping = (record: FieldMapping) => {
  delete mappingsData.value[record.placeholderName]
  message.success('映射关系删除成功')
}

const testMapping = async (record: FieldMapping) => {
  testingMapping.value = record
  
  // 模拟测试结果
  testResult.value = {
    placeholder: record.placeholderName,
    mockData: generateMockData(record),
    queryInfo: {
      table: record.tableName,
      column: record.columnName,
      conditions: record.queryConditions
    }
  }
  
  testModalVisible.value = true
}

const generateMockData = (mapping: FieldMapping) => {
  switch (mapping.dataType) {
    case 'string':
      return `Mock_${mapping.columnName}_Value`
    case 'number':
      return Math.floor(Math.random() * 1000)
    case 'array':
      return [1, 2, 3, 4, 5].map(i => Math.floor(Math.random() * 100))
    case 'object':
      return { key: 'value', timestamp: new Date().toISOString() }
    default:
      return null
  }
}

const showBatchEditModal = () => {
  message.info('批量编辑功能待实现')
}

const exportMappings = () => {
  const data = JSON.stringify(mappingsData.value, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `mappings-${selectedChartId.value}-${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)
  message.success('映射关系已导出')
}

const filterOption = (input: string, option: any) => {
  return option.value.toLowerCase().includes(input.toLowerCase())
}

// 生命周期
onMounted(() => {
  loadMappings()
})
</script>

<style scoped>
.mapping-management {
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

.toolbar,
.chart-selector,
.mapping-table,
.mapping-visualization {
  margin-bottom: 24px;
}

.toolbar-content {
  margin-top: 16px;
}

.placeholder-tag {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.text-muted {
  color: #999;
}

.visualization-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.test-result h4 {
  margin: 16px 0 8px 0;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mapping-management {
    padding: 16px;
  }
  
  .toolbar-content .ant-row {
    text-align: center;
  }
  
  .chart-selector .ant-radio-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .chart-selector .ant-radio-button-wrapper {
    text-align: center;
  }
}
</style>
