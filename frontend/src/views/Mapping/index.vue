<template>
  <div class="mapping-container">
    <div class="page-header">
      <h2>占位符映射管理</h2>
      <p class="page-description">管理图表模板中占位符与数据源的映射关系</p>
    </div>

    <a-card title="映射关系管理" class="mapping-card">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="selectedChartType"
            style="width: 160px"
            placeholder="选择图表类型"
            @change="loadMappings"
          >
            <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
            <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
            <a-select-option value="pie_chart">饼图</a-select-option>
          </a-select>
          <a-button type="primary" @click="showAddModal" :disabled="!selectedChartType">
            <PlusOutlined />
            新增映射
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="mappingList"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'placeholder'">
            <a-tag color="blue">${{ record.placeholder }}</a-tag>
          </template>
          <template v-else-if="column.key === 'dataSource'">
            <a-tag color="green">{{ record.tableName }}.{{ record.columnName }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'">
              {{ record.status === 'active' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editMapping(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="testMapping(record)">
                测试
              </a-button>
              <a-popconfirm
                title="确定要删除这个映射吗？"
                @confirm="deleteMapping(record.id)"
              >
                <a-button type="link" size="small" danger>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑映射模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑映射' : '新增映射'"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="占位符名称" name="placeholder">
          <a-input 
            v-model:value="formData.placeholder" 
            placeholder="例如: chart_title, series_data_1"
            addon-before="${"
            addon-after="}"
          />
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据表名" name="tableName">
              <a-select 
                v-model:value="formData.tableName" 
                placeholder="请选择数据表"
                @change="loadColumns"
              >
                <a-select-option value="sales_data">销售数据表</a-select-option>
                <a-select-option value="user_stats">用户统计表</a-select-option>
                <a-select-option value="product_info">产品信息表</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="字段名" name="columnName">
              <a-select 
                v-model:value="formData.columnName" 
                placeholder="请选择字段"
                :disabled="!formData.tableName"
              >
                <a-select-option 
                  v-for="column in availableColumns" 
                  :key="column.value" 
                  :value="column.value"
                >
                  {{ column.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="数据类型" name="dataType">
          <a-select v-model:value="formData.dataType" placeholder="请选择数据类型">
            <a-select-option value="string">字符串</a-select-option>
            <a-select-option value="number">数字</a-select-option>
            <a-select-option value="array">数组</a-select-option>
            <a-select-option value="object">对象</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="请输入映射描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 测试映射模态框 -->
    <a-modal
      v-model:open="testModalVisible"
      title="测试映射"
      width="800px"
      :footer="null"
    >
      <div v-if="testResult">
        <a-descriptions title="映射信息" bordered size="small">
          <a-descriptions-item label="占位符">${{ testResult.placeholder }}</a-descriptions-item>
          <a-descriptions-item label="数据源">{{ testResult.tableName }}.{{ testResult.columnName }}</a-descriptions-item>
          <a-descriptions-item label="数据类型">{{ testResult.dataType }}</a-descriptions-item>
        </a-descriptions>
        
        <a-divider>测试结果</a-divider>
        
        <a-alert
          :type="testResult.success ? 'success' : 'error'"
          :message="testResult.success ? '映射测试成功' : '映射测试失败'"
          :description="testResult.message"
          show-icon
        />
        
        <div v-if="testResult.sampleData" style="margin-top: 16px;">
          <h4>示例数据：</h4>
          <pre class="sample-data">{{ JSON.stringify(testResult.sampleData, null, 2) }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'

// 响应式数据
const loading = ref(false)
const modalVisible = ref(false)
const testModalVisible = ref(false)
const isEdit = ref(false)
const selectedChartType = ref('')
const mappingList = ref<any[]>([])
const availableColumns = ref<any[]>([])
const testResult = ref<any>(null)
const formRef = ref()

// 表格列配置
const columns = [
  {
    title: '占位符',
    dataIndex: 'placeholder',
    key: 'placeholder',
  },
  {
    title: '数据源',
    key: 'dataSource',
  },
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'dataType',
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
]

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
})

// 表单数据
const formData = reactive({
  id: null,
  placeholder: '',
  tableName: '',
  columnName: '',
  dataType: '',
  description: '',
  status: 'active',
})

// 表单验证规则
const rules = {
  placeholder: [{ required: true, message: '请输入占位符名称', trigger: 'blur' }],
  tableName: [{ required: true, message: '请选择数据表', trigger: 'change' }],
  columnName: [{ required: true, message: '请选择字段', trigger: 'change' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
}

// 方法
const loadMappings = async () => {
  if (!selectedChartType.value) return
  
  loading.value = true
  try {
    // 模拟数据
    const mockData = {
      stacked_line_chart: [
        {
          id: 1,
          placeholder: 'chart_title',
          tableName: 'sales_data',
          columnName: 'title',
          dataType: 'string',
          description: '图表标题',
          status: 'active',
        },
        {
          id: 2,
          placeholder: 'series_data_1',
          tableName: 'sales_data',
          columnName: 'monthly_sales',
          dataType: 'array',
          description: '第一个系列数据',
          status: 'active',
        },
      ],
      basic_bar_chart: [
        {
          id: 3,
          placeholder: 'chart_title',
          tableName: 'user_stats',
          columnName: 'report_title',
          dataType: 'string',
          description: '图表标题',
          status: 'active',
        },
      ],
      pie_chart: [
        {
          id: 4,
          placeholder: 'chart_title',
          tableName: 'product_info',
          columnName: 'category_title',
          dataType: 'string',
          description: '图表标题',
          status: 'active',
        },
      ],
    }
    
    mappingList.value = (mockData as any)[selectedChartType.value] || []
    pagination.total = mappingList.value.length
  } catch (error) {
    message.error('加载映射列表失败')
  } finally {
    loading.value = false
  }
}

const loadColumns = (tableName: string) => {
  const columnMap: Record<string, any[]> = {
    sales_data: [
      { value: 'title', label: '标题' },
      { value: 'monthly_sales', label: '月度销售额' },
      { value: 'quarterly_sales', label: '季度销售额' },
    ],
    user_stats: [
      { value: 'report_title', label: '报告标题' },
      { value: 'user_count', label: '用户数量' },
      { value: 'active_users', label: '活跃用户' },
    ],
    product_info: [
      { value: 'category_title', label: '分类标题' },
      { value: 'product_name', label: '产品名称' },
      { value: 'price', label: '价格' },
    ],
  }
  
  availableColumns.value = columnMap[tableName] || []
  formData.columnName = ''
}

const showAddModal = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

const editMapping = (record: any) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(formData, record)
  loadColumns(record.tableName)
}

const deleteMapping = async (id: number) => {
  try {
    mappingList.value = mappingList.value.filter(item => item.id !== id)
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

const testMapping = (record: any) => {
  testResult.value = {
    ...record,
    success: true,
    message: '映射配置正确，数据获取成功',
    sampleData: {
      placeholder: record.placeholder,
      value: '示例数据值',
      type: record.dataType,
    },
  }
  testModalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      const index = mappingList.value.findIndex(item => item.id === formData.id)
      if (index !== -1) {
        mappingList.value[index] = { ...formData }
      }
      message.success('更新成功')
    } else {
      const newId = Math.max(...mappingList.value.map(item => item.id), 0) + 1
      mappingList.value.push({ ...formData, id: newId })
      message.success('创建成功')
    }
    
    modalVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    placeholder: '',
    tableName: '',
    columnName: '',
    dataType: '',
    description: '',
    status: 'active',
  })
  availableColumns.value = []
  formRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  // 初始化时不加载数据，等用户选择图表类型
})
</script>

<style scoped>
.mapping-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.mapping-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sample-data {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  overflow-x: auto;
}
</style>
