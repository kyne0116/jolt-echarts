<template>
  <div class="data-manager">
    <!-- 查询条件 -->
    <a-card class="filter-card">
      <template #title>
        <span style="color: #1890ff; font-weight: 600;">查询条件：</span>
      </template>
      <a-row :gutter="16">
        <a-col :span="6">
          <a-select
            v-model:value="filters.year"
            placeholder="选择年份"
            style="width: 100%"
            allowClear
            @change="loadData"
          >
            <a-select-option value="2023">2023年</a-select-option>
            <a-select-option value="2024">2024年</a-select-option>
            <a-select-option value="2025">2025年</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="filters.region"
            placeholder="选择地区"
            style="width: 100%"
            allowClear
            @change="loadData"
          >
            <a-select-option value="华北">华北</a-select-option>
            <a-select-option value="华东">华东</a-select-option>
            <a-select-option value="华南">华南</a-select-option>
            <a-select-option value="华中">华中</a-select-option>
            <a-select-option value="西北">西北</a-select-option>
            <a-select-option value="西南">西南</a-select-option>
            <a-select-option value="东北">东北</a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <!-- 数据表格 -->
    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1000 }"
        size="small"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <!-- 分类标签 -->
          <template v-if="column.key === 'category'">
            <a-tag :color="getCategoryColor(record.category)">
              {{ record.category }}
            </a-tag>
          </template>
          
          <!-- 金额格式化 -->
          <template v-else-if="column.key === 'amount'">
            <span class="amount">¥{{ formatNumber(record.amount) }}</span>
          </template>
          
          <!-- 百分比格式化 -->
          <template v-else-if="column.key === 'percentage'">
            <span>{{ formatPercentage(record.percentage) }}</span>
          </template>
          
          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button
                type="link"
                size="small"
                @click="editRecord(record)"
                title="编辑"
              >
                <EditOutlined />
              </a-button>
              <a-popconfirm
                title="确定要删除这条记录吗？"
                @confirm="deleteRecord(record)"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  title="删除"
                >
                  <DeleteOutlined />
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑记录"
      @ok="saveEdit"
      @cancel="cancelEdit"
      :confirm-loading="saving"
      width="800px"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="年份" name="year">
              <a-input v-model:value="editForm.year" placeholder="如：2024" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="月份" name="month">
              <a-input v-model:value="editForm.month" placeholder="如：01" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="日期" name="date">
              <a-date-picker
                v-model:value="editForm.dateValue"
                style="width: 100%"
                format="YYYY-MM-DD"
                @change="onDateChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分类" name="category">
              <a-select v-model:value="editForm.category" placeholder="选择分类">
                <a-select-option value="电子产品">电子产品</a-select-option>
                <a-select-option value="服装">服装</a-select-option>
                <a-select-option value="食品">食品</a-select-option>
                <a-select-option value="家居">家居</a-select-option>
                <a-select-option value="图书">图书</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="渠道" name="channel">
              <a-select v-model:value="editForm.channel" placeholder="选择渠道">
                <a-select-option value="线上">线上</a-select-option>
                <a-select-option value="线下">线下</a-select-option>
                <a-select-option value="移动端">移动端</a-select-option>
                <a-select-option value="电话销售">电话销售</a-select-option>
                <a-select-option value="直销">直销</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="产品" name="product">
              <a-input v-model:value="editForm.product" placeholder="请输入产品名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地区" name="region">
              <a-select v-model:value="editForm.region" placeholder="选择地区">
                <a-select-option value="华北">华北</a-select-option>
                <a-select-option value="华东">华东</a-select-option>
                <a-select-option value="华南">华南</a-select-option>
                <a-select-option value="华中">华中</a-select-option>
                <a-select-option value="西北">西北</a-select-option>
                <a-select-option value="西南">西南</a-select-option>
                <a-select-option value="东北">东北</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="销售员" name="salesman">
              <a-input v-model:value="editForm.salesman" placeholder="请输入销售员姓名" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="金额" name="amount">
              <a-input-number
                v-model:value="editForm.amount"
                placeholder="金额"
                style="width: 100%"
                :precision="2"
                :min="0"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="数量" name="quantity">
              <a-input-number
                v-model:value="editForm.quantity"
                placeholder="数量"
                style="width: 100%"
                :min="0"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="百分比" name="percentage">
              <a-input-number
                v-model:value="editForm.percentage"
                placeholder="百分比"
                style="width: 100%"
                :min="0"
                :max="100"
                :precision="1"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import dayjs, { Dayjs } from 'dayjs'

// 接口定义
interface DataRecord {
  id: number
  year: string
  month: string
  date: string
  category: string
  channel: string
  product: string
  region: string
  amount: number
  quantity: number
  percentage: number
  salesman: string
}

// 响应式数据
const loading = ref(false)
const dataSource = ref<DataRecord[]>([])
const filters = reactive({
  year: '',
  region: ''
})

// 编辑相关
const editModalVisible = ref(false)
const saving = ref(false)
const editFormRef = ref()
const currentEditRecord = ref<DataRecord | null>(null)

const editForm = reactive({
  id: 0,
  year: '',
  month: '',
  date: '',
  dateValue: null as Dayjs | null,
  category: '',
  channel: '',
  product: '',
  region: '',
  amount: null as number | null,
  quantity: null as number | null,
  percentage: null as number | null,
  salesman: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
  onChange: (page: number, size: number) => {
    pagination.current = page
    pagination.pageSize = size
    loadData()
  },
  onShowSizeChange: (current: number, size: number) => {
    pagination.current = 1
    pagination.pageSize = size
    loadData()
  }
})

// 表格列定义
const columns = [
  {
    title: '年份',
    dataIndex: 'year',
    key: 'year',
    width: 60,
    align: 'center'
  },
  {
    title: '月份',
    dataIndex: 'month',
    key: 'month',
    width: 50,
    align: 'center'
  },
  {
    title: '日期',
    dataIndex: 'date',
    key: 'date',
    width: 90,
    align: 'center'
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: 70,
    align: 'center'
  },
  {
    title: '渠道',
    dataIndex: 'channel',
    key: 'channel',
    width: 60,
    align: 'center'
  },
  {
    title: '产品',
    dataIndex: 'product',
    key: 'product',
    width: 100,
    align: 'center'
  },
  {
    title: '地区',
    dataIndex: 'region',
    key: 'region',
    width: 50,
    align: 'center'
  },
  {
    title: '金额',
    dataIndex: 'amount',
    key: 'amount',
    width: 80,
    align: 'center'
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 50,
    align: 'center'
  },
  {
    title: '百分比',
    dataIndex: 'percentage',
    key: 'percentage',
    width: 70,
    align: 'center'
  },
  {
    title: '销售员',
    dataIndex: 'salesman',
    key: 'salesman',
    width: 70,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 80,
    align: 'center',
    fixed: 'right'
  }
]

// 表单验证规则
const formRules = {
  year: [
    { required: true, message: '请输入年份', trigger: 'blur' }
  ],
  month: [
    { required: true, message: '请输入月份', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  product: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择地区', trigger: 'change' }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: (pagination.current - 1).toString(),
      size: pagination.pageSize.toString()
    })

    if (filters.year) params.append('year', filters.year)
    if (filters.region) params.append('region', filters.region)

    const response = await fetch(`/api/chart/simple-data/records?${params}`)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 'OK') {
      dataSource.value = result.data.records || []
      pagination.total = result.data.totalCount || 0
    } else {
      throw new Error(result.message || '获取数据失败')
    }
  } catch (error: any) {
    message.error('加载数据失败: ' + error.message)
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 编辑记录
const editRecord = (record: DataRecord) => {
  currentEditRecord.value = record
  
  // 填充表单数据
  editForm.id = record.id
  editForm.year = record.year
  editForm.month = record.month
  editForm.date = record.date
  editForm.dateValue = record.date ? dayjs(record.date) : null
  editForm.category = record.category
  editForm.channel = record.channel
  editForm.product = record.product
  editForm.region = record.region
  editForm.amount = record.amount
  editForm.quantity = record.quantity
  editForm.percentage = record.percentage
  editForm.salesman = record.salesman
  
  editModalVisible.value = true
}

// 日期选择器变化
const onDateChange = (date: Dayjs | null) => {
  editForm.date = date ? date.format('YYYY-MM-DD') : ''
}

// 保存编辑
const saveEdit = async () => {
  try {
    await editFormRef.value.validate()
    saving.value = true

    const updateData = {
      id: editForm.id,
      year: editForm.year,
      month: editForm.month,
      date: editForm.date,
      category: editForm.category,
      channel: editForm.channel,
      product: editForm.product,
      region: editForm.region,
      amount: editForm.amount,
      quantity: editForm.quantity,
      percentage: editForm.percentage,
      salesman: editForm.salesman
    }

    const response = await fetch(`/api/chart/simple-data/records/${editForm.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })

    const result = await response.json()
    
    if (result.code === 'OK') {
      message.success('更新成功')
      editModalVisible.value = false
      loadData() // 重新加载数据
    } else {
      throw new Error(result.message || '更新失败')
    }
  } catch (error: any) {
    if (error.errorFields) {
      message.error('请填写必填项')
    } else {
      message.error('更新失败: ' + error.message)
      console.error('更新失败:', error)
    }
  } finally {
    saving.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  editModalVisible.value = false
  currentEditRecord.value = null
  // 重置表单
  Object.assign(editForm, {
    id: 0,
    year: '',
    month: '',
    date: '',
    dateValue: null,
    category: '',
    channel: '',
    product: '',
    region: '',
    amount: null,
    quantity: null,
    percentage: null,
    salesman: ''
  })
}

// 删除记录
const deleteRecord = async (record: DataRecord) => {
  try {
    const response = await fetch(`/api/chart/simple-data/records/${record.id}`, {
      method: 'DELETE'
    })

    const result = await response.json()
    
    if (result.code === 'OK') {
      message.success('删除成功')
      loadData() // 重新加载数据
    } else {
      throw new Error(result.message || '删除失败')
    }
  } catch (error: any) {
    message.error('删除失败: ' + error.message)
    console.error('删除失败:', error)
  }
}

// 工具函数
const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    '电子产品': 'blue',
    '服装': 'green', 
    '食品': 'orange',
    '家居': 'purple',
    '图书': 'cyan'
  }
  return colors[category] || 'default'
}

const formatNumber = (num: number) => {
  return num?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) || '0.00'
}

const formatPercentage = (num: number) => {
  return `${num?.toFixed(1) || 0}%`
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.data-manager {
  padding: 16px;
  height: 100vh;
  overflow-y: auto;
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  flex: 1;
}

.amount {
  color: #52c41a;
  font-weight: 500;
}

/* 表格样式优化 */
:deep(.ant-table-small .ant-table-tbody > tr > td) {
  padding: 4px 8px !important;
}

:deep(.ant-table-small .ant-table-thead > tr > th) {
  padding: 8px 8px !important;
  font-weight: 600;
}

:deep(.ant-table) {
  font-size: 12px;
}

:deep(.ant-tag) {
  margin: 0;
  font-size: 11px;
}

:deep(.ant-btn-link) {
  padding: 0 4px;
}
</style>