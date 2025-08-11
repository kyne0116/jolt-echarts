<template>
  <div class="universal-data-view-container">
    <div class="page-header">
      <h2>🗄️ UniversalChartDataView 数据管理</h2>
      <p class="page-description">管理模拟数据的字段定义和数据记录，支持动态字段扩展</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：功能导航 -->
      <a-col :span="6">
        <a-card title="数据管理" class="navigation-card">
          <a-menu 
            v-model:selectedKeys="selectedMenuKeys" 
            mode="inline"
            @click="onMenuClick"
          >
            <a-menu-item key="field-management">
              <template #icon><SettingOutlined /></template>
              字段管理
            </a-menu-item>
            <a-menu-item key="data-records">
              <template #icon><TableOutlined /></template>
              数据记录
            </a-menu-item>
            <a-menu-item key="data-import">
              <template #icon><ImportOutlined /></template>
              数据导入
            </a-menu-item>
            <a-menu-item key="data-export">
              <template #icon><ExportOutlined /></template>
              数据导出
            </a-menu-item>
            <a-menu-item key="statistics">
              <template #icon><BarChartOutlined /></template>
              统计信息
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>

      <!-- 右侧：主要内容区域 -->
      <a-col :span="18">
        <!-- 字段管理界面 -->
        <a-card 
          v-if="currentView === 'field-management'" 
          title="字段管理" 
          class="content-card"
        >
          <template #extra>
            <a-space>
              <a-button type="primary" @click="showAddFieldModal">
                <PlusOutlined />
                添加字段
              </a-button>
              <a-button @click="refreshFieldData">
                <ReloadOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <!-- 字段管理表格 -->
          <a-table
            :columns="fieldColumns"
            :data-source="fieldList"
            :loading="fieldLoading"
            :pagination="{ pageSize: 10, showSizeChanger: true, showQuickJumper: true }"
            row-key="fieldName"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fieldType'">
                <a-tag :color="getFieldTypeColor(record.dataType)">
                  {{ record.dataType?.displayName || record.dataType }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'required'">
                <a-tag :color="record.required ? 'red' : 'default'">
                  {{ record.required ? '必填' : '可选' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'fieldGroup'">
                <a-tag color="blue">{{ record.fieldGroup || '未分组' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="editField(record)">
                    <EditOutlined />
                    编辑
                  </a-button>
                  <a-button type="link" size="small" danger @click="deleteField(record)">
                    <DeleteOutlined />
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 数据记录管理界面 -->
        <a-card 
          v-else-if="currentView === 'data-records'" 
          title="数据记录管理" 
          class="content-card"
        >
          <template #extra>
            <a-space>
              <a-button type="primary" @click="showAddRecordModal">
                <PlusOutlined />
                添加记录
              </a-button>
              <a-button @click="showBatchEditModal">
                <EditOutlined />
                批量编辑
              </a-button>
              <a-button @click="refreshRecordData">
                <ReloadOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <!-- 数据记录表格 -->
          <a-table
            :columns="recordColumns"
            :data-source="recordList"
            :loading="recordLoading"
            :pagination="{ 
              pageSize: 20, 
              showSizeChanger: true, 
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条记录`
            }"
            :scroll="{ x: 'max-content' }"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="editRecord(record)">
                    <EditOutlined />
                    编辑
                  </a-button>
                  <a-button type="link" size="small" @click="duplicateRecord(record)">
                    <CopyOutlined />
                    复制
                  </a-button>
                  <a-button type="link" size="small" danger @click="deleteRecord(record)">
                    <DeleteOutlined />
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 数据导入界面 -->
        <a-card 
          v-else-if="currentView === 'data-import'" 
          title="数据导入" 
          class="content-card"
        >
          <a-upload-dragger
            v-model:fileList="importFileList"
            :before-upload="beforeUpload"
            @change="handleImportFileChange"
            accept=".json,.csv,.xlsx"
            :multiple="false"
          >
            <p class="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持 JSON、CSV、Excel 格式文件。单次上传一个文件。
            </p>
          </a-upload-dragger>

          <a-divider />

          <div v-if="importPreviewData.length > 0">
            <h3>导入预览</h3>
            <a-table
              :columns="importPreviewColumns"
              :data-source="importPreviewData"
              :pagination="{ pageSize: 5 }"
              size="small"
            />
            
            <div style="margin-top: 16px; text-align: right;">
              <a-space>
                <a-button @click="clearImportPreview">清空预览</a-button>
                <a-button type="primary" @click="confirmImport" :loading="importLoading">
                  确认导入 {{ importPreviewData.length }} 条记录
                </a-button>
              </a-space>
            </div>
          </div>
        </a-card>

        <!-- 数据导出界面 -->
        <a-card 
          v-else-if="currentView === 'data-export'" 
          title="数据导出" 
          class="content-card"
        >
          <a-form layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="导出格式">
                  <a-select v-model:value="exportFormat" placeholder="选择导出格式">
                    <a-select-option value="json">JSON 格式</a-select-option>
                    <a-select-option value="csv">CSV 格式</a-select-option>
                    <a-select-option value="xlsx">Excel 格式</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="导出范围">
                  <a-select v-model:value="exportRange" placeholder="选择导出范围">
                    <a-select-option value="all">全部数据</a-select-option>
                    <a-select-option value="current">当前页数据</a-select-option>
                    <a-select-option value="selected">选中数据</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            
            <a-form-item label="包含字段">
              <a-checkbox-group v-model:value="exportFields" :options="exportFieldOptions" />
            </a-form-item>
            
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="exportData" :loading="exportLoading">
                  <DownloadOutlined />
                  导出数据
                </a-button>
                <a-button @click="resetExportForm">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>

        <!-- 统计信息界面 -->
        <a-card 
          v-else-if="currentView === 'statistics'" 
          title="统计信息" 
          class="content-card"
        >
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic title="总字段数" :value="statisticsData.totalFields" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="基础字段" :value="statisticsData.baseFields" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="动态字段" :value="statisticsData.dynamicFields" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="数据记录" :value="statisticsData.totalRecords" />
            </a-col>
          </a-row>

          <a-divider />

          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="字段类型分布" size="small">
                <div ref="fieldTypeChart" style="height: 300px;"></div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="字段分组分布" size="small">
                <div ref="fieldGroupChart" style="height: 300px;"></div>
              </a-card>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 添加字段模态框 -->
    <a-modal
      v-model:open="addFieldModalVisible"
      title="添加字段"
      @ok="handleAddField"
      @cancel="cancelAddField"
      :confirm-loading="addFieldLoading"
    >
      <a-form
        ref="addFieldFormRef"
        :model="addFieldForm"
        :rules="addFieldRules"
        layout="vertical"
      >
        <a-form-item label="字段名称" name="fieldName">
          <a-input v-model:value="addFieldForm.fieldName" placeholder="请输入字段名称" />
        </a-form-item>
        <a-form-item label="显示名称" name="displayName">
          <a-input v-model:value="addFieldForm.displayName" placeholder="请输入显示名称" />
        </a-form-item>
        <a-form-item label="数据类型" name="dataType">
          <a-select v-model:value="addFieldForm.dataType" placeholder="选择数据类型">
            <a-select-option value="STRING">字符串</a-select-option>
            <a-select-option value="NUMBER">数字</a-select-option>
            <a-select-option value="BOOLEAN">布尔值</a-select-option>
            <a-select-option value="DATE">日期</a-select-option>
            <a-select-option value="ARRAY">数组</a-select-option>
            <a-select-option value="OBJECT">对象</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="字段分组" name="fieldGroup">
          <a-input v-model:value="addFieldForm.fieldGroup" placeholder="请输入字段分组" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="addFieldForm.description" placeholder="请输入字段描述" />
        </a-form-item>
        <a-form-item name="required">
          <a-checkbox v-model:checked="addFieldForm.required">必填字段</a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 添加记录模态框 -->
    <a-modal
      v-model:open="addRecordModalVisible"
      title="添加数据记录"
      @ok="handleAddRecord"
      @cancel="cancelAddRecord"
      :confirm-loading="addRecordLoading"
      width="800px"
    >
      <a-form
        ref="addRecordFormRef"
        :model="addRecordForm"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12" v-for="field in availableFields" :key="field.fieldName">
            <a-form-item :label="field.displayName" :name="field.fieldName">
              <a-input 
                v-if="field.dataType === 'STRING'"
                v-model:value="addRecordForm[field.fieldName]" 
                :placeholder="`请输入${field.displayName}`" 
              />
              <a-input-number 
                v-else-if="field.dataType === 'NUMBER'"
                v-model:value="addRecordForm[field.fieldName]" 
                :placeholder="`请输入${field.displayName}`"
                style="width: 100%"
              />
              <a-switch 
                v-else-if="field.dataType === 'BOOLEAN'"
                v-model:checked="addRecordForm[field.fieldName]"
              />
              <a-date-picker 
                v-else-if="field.dataType === 'DATE'"
                v-model:value="addRecordForm[field.fieldName]"
                style="width: 100%"
              />
              <a-textarea 
                v-else
                v-model:value="addRecordForm[field.fieldName]" 
                :placeholder="`请输入${field.displayName}`"
                :rows="2"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    BarChartOutlined,
    CopyOutlined,
    DeleteOutlined,
    DownloadOutlined,
    EditOutlined,
    ExportOutlined,
    ImportOutlined,
    InboxOutlined,
    PlusOutlined,
    ReloadOutlined,
    SettingOutlined,
    TableOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { nextTick, onMounted, reactive, ref } from 'vue'

// ==================== 响应式数据 ====================

// 当前视图
const currentView = ref('field-management')
const selectedMenuKeys = ref(['field-management'])

// 字段管理相关
const fieldList = ref([])
const fieldLoading = ref(false)
const addFieldModalVisible = ref(false)
const addFieldLoading = ref(false)
const addFieldForm = reactive({
  fieldName: '',
  displayName: '',
  dataType: '',
  fieldGroup: '',
  description: '',
  required: false
})

// 数据记录管理相关
const recordList = ref([])
const recordLoading = ref(false)
const addRecordModalVisible = ref(false)
const addRecordLoading = ref(false)
const addRecordForm = reactive({})

// 数据导入相关
const importFileList = ref([])
const importPreviewData = ref([])
const importPreviewColumns = ref([])
const importLoading = ref(false)

// 数据导出相关
const exportFormat = ref('json')
const exportRange = ref('all')
const exportFields = ref([])
const exportFieldOptions = ref([])
const exportLoading = ref(false)

// 统计信息相关
const statisticsData = reactive({
  totalFields: 0,
  baseFields: 0,
  dynamicFields: 0,
  totalRecords: 0
})

// 可用字段列表
const availableFields = ref([])

// ==================== 表格列定义 ====================

// 字段管理表格列
const fieldColumns = [
  {
    title: '字段名称',
    dataIndex: 'fieldName',
    key: 'fieldName',
    width: 150
  },
  {
    title: '显示名称',
    dataIndex: 'displayName',
    key: 'displayName',
    width: 150
  },
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'fieldType',
    width: 120
  },
  {
    title: '是否必填',
    dataIndex: 'required',
    key: 'required',
    width: 100
  },
  {
    title: '字段分组',
    dataIndex: 'fieldGroup',
    key: 'fieldGroup',
    width: 120
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
]

// 数据记录表格列（动态生成）
const recordColumns = ref([])

// 表单验证规则
const addFieldRules = {
  fieldName: [
    { required: true, message: '请输入字段名称' },
    { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '字段名称只能包含字母、数字和下划线，且不能以数字开头' }
  ],
  displayName: [
    { required: true, message: '请输入显示名称' }
  ],
  dataType: [
    { required: true, message: '请选择数据类型' }
  ]
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadFieldData()
  loadRecordData()
  loadStatisticsData()
})

// ==================== 方法定义 ====================

// 菜单点击事件
const onMenuClick = ({ key }: { key: string }) => {
  currentView.value = key
  selectedMenuKeys.value = [key]

  // 根据选择的菜单加载对应数据
  switch (key) {
    case 'field-management':
      loadFieldData()
      break
    case 'data-records':
      loadRecordData()
      break
    case 'statistics':
      loadStatisticsData()
      nextTick(() => {
        renderCharts()
      })
      break
  }
}

// 加载字段数据
const loadFieldData = async () => {
  fieldLoading.value = true
  try {
    // 调用API获取字段列表
    const response = await fetch('/api/chart/dynamic-mapping/available-fields')
    const result = await response.json()

    if (result.success) {
      // 合并基础字段和动态字段
      const baseFields = Object.entries(result.data.baseFields).flatMap(([group, fields]) =>
        (fields as string[]).map(fieldName => ({
          fieldName,
          displayName: fieldName,
          dataType: 'STRING',
          fieldGroup: group,
          description: `基础字段: ${fieldName}`,
          required: false,
          isBaseField: true
        }))
      )

      const dynamicFields = Object.values(result.data.dynamicFields).map((field: any) => ({
        ...field,
        isBaseField: false
      }))

      fieldList.value = [...baseFields, ...dynamicFields]
      availableFields.value = fieldList.value

      console.log('✅ [字段管理] 加载字段数据成功:', fieldList.value.length)
    } else {
      message.error('加载字段数据失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [字段管理] 加载字段数据失败:', error)
    message.error('加载字段数据失败')
  } finally {
    fieldLoading.value = false
  }
}

// 加载数据记录
const loadRecordData = async () => {
  recordLoading.value = true
  try {
    // 调用API获取数据记录
    const response = await fetch('/api/chart/data/universal-view')
    const result = await response.json()

    if (result.success) {
      recordList.value = result.data || []

      // 动态生成表格列
      if (recordList.value.length > 0) {
        const firstRecord = recordList.value[0]
        recordColumns.value = Object.keys(firstRecord).map(key => ({
          title: key,
          dataIndex: key,
          key: key,
          width: 120,
          ellipsis: true
        }))

        // 添加操作列
        recordColumns.value.push({
          title: '操作',
          key: 'action',
          width: 180,
          fixed: 'right'
        })
      }

      console.log('✅ [数据记录] 加载数据记录成功:', recordList.value.length)
    } else {
      message.error('加载数据记录失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [数据记录] 加载数据记录失败:', error)
    message.error('加载数据记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 加载统计信息
const loadStatisticsData = async () => {
  try {
    const response = await fetch('/api/chart/dynamic-mapping/stats')
    const result = await response.json()

    if (result.success) {
      const fieldStats = result.data.fieldStats
      statisticsData.totalFields = fieldStats.totalDefinitions + fieldStats.baseFieldCount
      statisticsData.baseFields = fieldStats.baseFieldCount
      statisticsData.dynamicFields = fieldStats.totalDefinitions
      statisticsData.totalRecords = recordList.value.length

      console.log('✅ [统计信息] 加载统计数据成功')
    }
  } catch (error) {
    console.error('❌ [统计信息] 加载统计数据失败:', error)
  }
}

// 获取字段类型颜色
const getFieldTypeColor = (dataType: string) => {
  const colorMap: Record<string, string> = {
    'STRING': 'blue',
    'NUMBER': 'green',
    'BOOLEAN': 'orange',
    'DATE': 'purple',
    'ARRAY': 'cyan',
    'OBJECT': 'magenta'
  }
  return colorMap[dataType] || 'default'
}

// 显示添加字段模态框
const showAddFieldModal = () => {
  addFieldModalVisible.value = true
  // 重置表单
  Object.assign(addFieldForm, {
    fieldName: '',
    displayName: '',
    dataType: '',
    fieldGroup: '',
    description: '',
    required: false
  })
}

// 处理添加字段
const handleAddField = async () => {
  addFieldLoading.value = true
  try {
    // 这里应该调用API添加字段
    console.log('添加字段:', addFieldForm)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('字段添加成功')
    addFieldModalVisible.value = false
    loadFieldData() // 重新加载数据
  } catch (error) {
    console.error('添加字段失败:', error)
    message.error('添加字段失败')
  } finally {
    addFieldLoading.value = false
  }
}

// 取消添加字段
const cancelAddField = () => {
  addFieldModalVisible.value = false
}

// 编辑字段
const editField = (record: any) => {
  console.log('编辑字段:', record)
  message.info('编辑字段功能开发中')
}

// 删除字段
const deleteField = (record: any) => {
  console.log('删除字段:', record)
  message.info('删除字段功能开发中')
}

// 刷新字段数据
const refreshFieldData = () => {
  loadFieldData()
}

// 显示添加记录模态框
const showAddRecordModal = () => {
  addRecordModalVisible.value = true
  // 重置表单
  Object.keys(addRecordForm).forEach(key => {
    delete addRecordForm[key]
  })
}

// 处理添加记录
const handleAddRecord = async () => {
  addRecordLoading.value = true
  try {
    console.log('添加记录:', addRecordForm)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('数据记录添加成功')
    addRecordModalVisible.value = false
    loadRecordData() // 重新加载数据
  } catch (error) {
    console.error('添加记录失败:', error)
    message.error('添加记录失败')
  } finally {
    addRecordLoading.value = false
  }
}

// 取消添加记录
const cancelAddRecord = () => {
  addRecordModalVisible.value = false
}

// 编辑记录
const editRecord = (record: any) => {
  console.log('编辑记录:', record)
  message.info('编辑记录功能开发中')
}

// 复制记录
const duplicateRecord = (record: any) => {
  console.log('复制记录:', record)
  message.info('复制记录功能开发中')
}

// 删除记录
const deleteRecord = (record: any) => {
  console.log('删除记录:', record)
  message.info('删除记录功能开发中')
}

// 显示批量编辑模态框
const showBatchEditModal = () => {
  message.info('批量编辑功能开发中')
}

// 刷新记录数据
const refreshRecordData = () => {
  loadRecordData()
}

// 文件上传前处理
const beforeUpload = (file: File) => {
  const isValidType = ['application/json', 'text/csv', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'].includes(file.type)
  if (!isValidType) {
    message.error('只支持 JSON、CSV、Excel 格式文件')
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB')
  }
  return isValidType && isLt10M
}

// 处理导入文件变化
const handleImportFileChange = (info: any) => {
  console.log('文件变化:', info)
  message.info('文件导入功能开发中')
}

// 清空导入预览
const clearImportPreview = () => {
  importPreviewData.value = []
  importPreviewColumns.value = []
  importFileList.value = []
}

// 确认导入
const confirmImport = async () => {
  importLoading.value = true
  try {
    // 模拟导入过程
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success(`成功导入 ${importPreviewData.value.length} 条记录`)
    clearImportPreview()
    loadRecordData() // 重新加载数据
  } catch (error) {
    console.error('导入失败:', error)
    message.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

// 导出数据
const exportData = async () => {
  exportLoading.value = true
  try {
    console.log('导出配置:', {
      format: exportFormat.value,
      range: exportRange.value,
      fields: exportFields.value
    })

    // 模拟导出过程
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('数据导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 重置导出表单
const resetExportForm = () => {
  exportFormat.value = 'json'
  exportRange.value = 'all'
  exportFields.value = []
}

// 渲染统计图表
const renderCharts = () => {
  // 字段类型分布图表
  const fieldTypeChart = echarts.init(document.querySelector('.field-type-chart'))
  const fieldTypeOption = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 20, name: '字符串' },
        { value: 8, name: '数字' },
        { value: 4, name: '数组' },
        { value: 3, name: '布尔值' },
        { value: 2, name: '日期' },
        { value: 3, name: '对象' }
      ]
    }]
  }
  fieldTypeChart.setOption(fieldTypeOption)

  // 字段分组分布图表
  const fieldGroupChart = echarts.init(document.querySelector('.field-group-chart'))
  const fieldGroupOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['基础信息', '时间维度', '分类数据', '数值字段', '配置字段', '业务扩展']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      type: 'bar',
      data: [8, 8, 8, 8, 8, 4]
    }]
  }
  fieldGroupChart.setOption(fieldGroupOption)
}
</script>

<style scoped>
.universal-data-view-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h2 {
  margin: 0;
  color: #1890ff;
  font-size: 28px;
  font-weight: 600;
}

.page-description {
  margin: 8px 0 0 0;
  color: #666;
  font-size: 16px;
}

.navigation-card {
  height: fit-content;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.navigation-card .ant-menu {
  border: none;
}

.navigation-card .ant-menu-item {
  margin: 4px 0;
  border-radius: 6px;
}

.content-card {
  min-height: 600px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-card .ant-card-head {
  border-bottom: 2px solid #f0f0f0;
}

.content-card .ant-card-head-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.ant-table {
  background: white;
  border-radius: 8px;
}

.ant-table-thead > tr > th {
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
}

.ant-statistic {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.ant-statistic-title {
  color: #666;
  font-size: 14px;
}

.ant-statistic-content {
  color: #1890ff;
  font-weight: 600;
}

.ant-upload-dragger {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.ant-upload-dragger:hover {
  border-color: #1890ff;
  background: #f0f8ff;
}

.ant-upload-drag-icon {
  font-size: 48px;
  color: #1890ff;
}

.ant-upload-text {
  font-size: 16px;
  color: #666;
  margin: 16px 0 8px 0;
}

.ant-upload-hint {
  color: #999;
  font-size: 14px;
}

.ant-form-item-label > label {
  font-weight: 600;
  color: #262626;
}

.ant-modal-header {
  border-bottom: 2px solid #f0f0f0;
}

.ant-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.ant-tag {
  border-radius: 4px;
  font-weight: 500;
}

.ant-btn {
  border-radius: 6px;
  font-weight: 500;
}

.ant-btn-primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.ant-btn-primary:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.4);
}

.ant-divider {
  border-color: #e8e8e8;
  margin: 24px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .universal-data-view-container {
    padding: 16px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .universal-data-view-container {
    padding: 12px;
  }

  .ant-col:first-child {
    margin-bottom: 16px;
  }

  .content-card {
    min-height: auto;
  }
}

/* 图表容器样式 */
.field-type-chart,
.field-group-chart {
  width: 100%;
  height: 300px;
}

/* 加载状态样式 */
.ant-spin-container {
  min-height: 200px;
}

/* 空状态样式 */
.ant-empty {
  padding: 40px 0;
}

.ant-empty-description {
  color: #999;
  font-size: 14px;
}
</style>
