<template>
  <div class="page-container">
    <div class="universal-data-manager">
      <a-card class="main-card">
      <template #title>
        <a-row :gutter="16" align="middle">
          <a-col>
            <span style="font-weight: 600; color: #1890ff; margin-right: 16px;">查询条件：</span>
          </a-col>
          <a-col>
            <a-select
              v-model:value="selectedYear"
              placeholder="选择年份"
              style="width: 120px"
              @change="handleYearChange"
            >
              <a-select-option value="2023">2023年</a-select-option>
              <a-select-option value="2024">2024年</a-select-option>
              <a-select-option value="2025">2025年</a-select-option>
            </a-select>
          </a-col>
          <a-col>
            <a-select
              v-model:value="selectedRegion"
              placeholder="选择地区"
              style="width: 120px"
              @change="handleRegionChange"
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
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <PlusOutlined />
            添加记录
          </a-button>
          <a-button @click="showFieldsInfo">
            <InfoCircleOutlined />
            字段信息
          </a-button>
          <a-button @click="regenerateSampleData" :loading="regenerating">
            <ReloadOutlined />
            重新生成示例数据
          </a-button>
          <a-button danger @click="clearAllData" :loading="clearing">
            <DeleteOutlined />
            清空所有数据
          </a-button>
        </a-space>
      </template>



      <!-- 数据表格 -->
      <a-table
        :columns="tableColumns"
        :data-source="dataList"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: totalCount,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条 (第 ${currentPage}/${Math.ceil(total / pageSize)} 页)`,
          onChange: handlePageChange,
          onShowSizeChange: handlePageSizeChange,
          pageSizeOptions: ['10', '20', '50'],
          size: 'small',
          position: ['bottomCenter']
        }"
        :scroll="{ x: 2000, y: 420 }"
        :sticky="true"
        row-key="id"
        :row-selection="rowSelection"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'product'">
            <span class="table-title">{{ record.product }}</span>
          </template>
          <template v-else-if="column.key === 'category'">
            <a-tag :color="getCategoryColor(record.category)">
              {{ record.category }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'amount'">
            <span class="amount-text">¥{{ formatNumber(record.amount) }}</span>
          </template>
          <template v-else-if="column.key === 'percentage'">
            <span>{{ formatPercentage(record.percentage) }}</span>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            <span>{{ formatDateTime(record.createdAt) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <div style="background: yellow; padding: 5px; border: 2px solid red;">
              <span>操作按钮测试</span>
              <a-button type="primary" size="small">测试</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <!-- 批量操作区域 -->
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <a-space>
          <span class="selected-info">已选择 {{ selectedRowKeys.length }} 条记录</span>
          <a-button type="primary" danger @click="batchDelete" :loading="batchDeleting">
            <DeleteOutlined />
            批量删除
          </a-button>
          <a-button @click="clearSelection">
            取消选择
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- 添加/编辑记录模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="editingRecord ? '编辑记录' : '添加记录'"
      @ok="handleSaveRecord"
      @cancel="cancelEdit"
      :confirm-loading="saving"
      width="1000px"
      :mask-closable="false"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        layout="vertical"
        :rules="formRules"
      >
        <!-- 简化的单页表单 - 12个核心字段 -->
        <a-row :gutter="16">
          <!-- 时间维度字段 -->
          <a-col :span="8">
            <a-form-item label="年份" name="year">
              <a-input v-model:value="editForm.year" placeholder="如：2024" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="月份" name="month">
              <a-input v-model:value="editForm.month" placeholder="如：01" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="日期" name="date">
              <a-input v-model:value="editForm.date" placeholder="如：2024-01-15" />
            </a-form-item>
          </a-col>

          <!-- 业务分类字段 -->
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

          <!-- 数值字段 -->
          <a-col :span="12">
            <a-form-item label="金额" name="amount">
              <a-input-number v-model:value="editForm.amount" placeholder="金额" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="数量" name="quantity">
              <a-input-number v-model:value="editForm.quantity" placeholder="数量" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="百分比" name="percentage">
              <a-input-number v-model:value="editForm.percentage" placeholder="百分比" :min="0" :max="100" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 查看记录详情模态框 -->
    <a-modal
      v-model:open="viewModalVisible"
      title="记录详情"
      :footer="null"
      width="800px"
    >
      <a-descriptions v-if="viewingRecord" title="记录信息" bordered>
        <a-descriptions-item label="ID">{{ viewingRecord.id }}</a-descriptions-item>
        <a-descriptions-item label="年份">{{ viewingRecord.year }}</a-descriptions-item>
        <a-descriptions-item label="月份">{{ viewingRecord.month }}</a-descriptions-item>
        <a-descriptions-item label="日期">{{ viewingRecord.date }}</a-descriptions-item>
        <a-descriptions-item label="分类">{{ viewingRecord.category }}</a-descriptions-item>
        <a-descriptions-item label="渠道">{{ viewingRecord.channel }}</a-descriptions-item>
        <a-descriptions-item label="产品">{{ viewingRecord.product }}</a-descriptions-item>
        <a-descriptions-item label="地区">{{ viewingRecord.region }}</a-descriptions-item>
        <a-descriptions-item label="销售员">{{ viewingRecord.salesman }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ formatNumber(viewingRecord.amount) }}</a-descriptions-item>
        <a-descriptions-item label="数量">{{ viewingRecord.quantity }}</a-descriptions-item>
        <a-descriptions-item label="百分比">{{ formatPercentage(viewingRecord.percentage) }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(viewingRecord.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ formatDateTime(viewingRecord.updatedAt) }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
    CopyOutlined,
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    InfoCircleOutlined,
    PlusOutlined,
    ReloadOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'

// ==================== 类型定义 ====================

interface UniversalChartDataView {
  id?: number
  year?: string
  month?: string
  date?: string
  category?: string
  channel?: string
  product?: string
  region?: string
  amount?: number
  quantity?: number
  percentage?: number
  salesman?: string
  createdAt?: string
  updatedAt?: string
}

// ==================== 响应式数据 ====================

// 数据列表相关
const dataList = ref<UniversalChartDataView[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 查询条件
const selectedYear = ref('')
const selectedRegion = ref('')

// 选择相关
const selectedRowKeys = ref<number[]>([])
const batchDeleting = ref(false)

// 编辑相关
const editModalVisible = ref(false)
const editingRecord = ref<UniversalChartDataView | null>(null)
const saving = ref(false)
const activeTabKey = ref('basic')
const editFormRef = ref()

const editForm = reactive<UniversalChartDataView>({
  year: '',
  month: '',
  date: '',
  category: '',
  channel: '',
  product: '',
  region: '',
  amount: undefined,
  quantity: undefined,
  percentage: undefined,
  salesman: ''
})

// 查看相关
const viewModalVisible = ref(false)
const viewingRecord = ref<UniversalChartDataView | null>(null)

// 其他操作
const regenerating = ref(false)
const clearing = ref(false)

// ==================== 表格配置 ====================

// 表格列定义 - 暂时移除所有固定列，先确保操作列能显示
const tableColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    align: 'center'
  },
  {
    title: '年份',
    dataIndex: 'year',
    key: 'year',
    width: 80,
    align: 'center'
  },
  {
    title: '月份',
    dataIndex: 'month',
    key: 'month',
    width: 80,
    align: 'center'
  },
  {
    title: '日期',
    dataIndex: 'date',
    key: 'date',
    width: 120,
    align: 'center'
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: 100,
    align: 'center'
  },
  {
    title: '渠道',
    dataIndex: 'channel',
    key: 'channel',
    width: 100,
    align: 'center'
  },
  {
    title: '产品',
    dataIndex: 'product',
    key: 'product',
    width: 150,
    align: 'center'
  },
  {
    title: '地区',
    dataIndex: 'region',
    key: 'region',
    width: 80,
    align: 'center'
  },
  {
    title: '金额',
    dataIndex: 'amount',
    key: 'amount',
    width: 120,
    align: 'center'
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 80,
    align: 'center'
  },
  {
    title: '百分比',
    dataIndex: 'percentage',
    key: 'percentage',
    width: 100,
    align: 'center'
  },
  {
    title: '销售员',
    dataIndex: 'salesman',
    key: 'salesman',
    width: 100,
    align: 'center'
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    align: 'center',
    className: 'action-column'
  }
]

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
  }
}))

// 表单验证规则
const formRules = {
  year: [
    { required: true, message: '请输入年份', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('🔍 表格列定义：', tableColumns)
  console.log('🔍 操作列配置：', tableColumns.find(col => col.key === 'action'))
  loadData()
})

// ==================== 方法定义 ====================

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: (currentPage.value - 1).toString(),
      size: pageSize.value.toString()
    })

    if (selectedYear.value) {
      params.append('year', selectedYear.value)
    }
    if (selectedRegion.value) {
      params.append('region', selectedRegion.value)
    }

    const response = await fetch(`/api/chart/simple-data/records?${params}`)

    console.log('🔍 [调试] API响应状态:', response.status, response.statusText)
    console.log('🔍 [调试] API响应头:', Object.fromEntries(response.headers.entries()))

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const result = await response.json()
    console.log('🔍 [调试] API响应数据:', result)

    // 后端返回格式: {code: 'OK', message: '', data: {...}}
    // 检查 code 是否为 'OK' 表示成功
    if (result.code === 'OK') {
      dataList.value = result.data.records || []
      totalCount.value = result.data.totalCount || 0
      console.log('✅ [数据管理] 加载数据成功:', dataList.value.length)
      console.log('🔍 [数据结构] 第一条记录:', dataList.value[0])
      console.log('🔍 [数据字段] 所有字段名:', dataList.value[0] ? Object.keys(dataList.value[0]) : [])
    } else {
      const errorMsg = result.message || '未知错误'
      console.error('❌ [数据管理] API返回错误:', errorMsg)
      message.error('加载数据失败: ' + errorMsg)
    }
  } catch (error: any) {
    console.error('❌ [数据管理] 加载数据失败:', error)
    if (error.message && error.message.includes('fetch')) {
      message.error('网络连接失败，请检查后端服务是否启动')
    } else {
      message.error('加载数据失败: ' + (error.message || error))
    }
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

// 页面大小变化
const handlePageSizeChange = (current: number, size: number) => {
  currentPage.value = 1
  pageSize.value = size
  loadData()
}

// 查询条件处理
const handleYearChange = () => {
  currentPage.value = 1
  loadData()
}

const handleRegionChange = () => {
  currentPage.value = 1
  loadData()
}

// 显示添加模态框
const showAddModal = () => {
  editingRecord.value = null
  resetEditForm()
  editModalVisible.value = true
  activeTabKey.value = 'basic'
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    year: '',
    month: '',
    date: '',
    category: '',
    channel: '',
    product: '',
    region: '',
    amount: undefined,
    quantity: undefined,
    percentage: undefined,
    salesman: ''
  })
}

// 编辑记录
const editRecord = (record: UniversalChartDataView) => {
  editingRecord.value = record
  Object.assign(editForm, {
    ...record
  })
  editModalVisible.value = true
  activeTabKey.value = 'basic'
}

// 查看记录
const viewRecord = (record: UniversalChartDataView) => {
  viewingRecord.value = record
  viewModalVisible.value = true
}

// 复制记录
const duplicateRecord = (record: UniversalChartDataView) => {
  editingRecord.value = null
  Object.assign(editForm, {
    ...record,
    id: undefined,
    product: (record.product || '') + ' (副本)'
  })
  editModalVisible.value = true
  activeTabKey.value = 'basic'
}

// 保存记录
const handleSaveRecord = async () => {
  try {
    await editFormRef.value.validate()

    saving.value = true

    const formData = { ...editForm }

    const url = editingRecord.value
      ? `/api/chart/simple-data/records/${editingRecord.value.id}`
      : '/api/chart/simple-data/records'

    const method = editingRecord.value ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(formData)
    })

    const result = await response.json()

    // 后端返回格式: {code: 'OK', message: '', data: {...}}
    if (result.code === 'OK') {
      message.success(editingRecord.value ? '记录更新成功' : '记录创建成功')
      editModalVisible.value = false
      loadData()
    } else {
      message.error('保存失败: ' + (result.message || '未知错误'))
    }
  } catch (error) {
    console.error('保存记录失败:', error)
    message.error('保存记录失败')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  editModalVisible.value = false
  resetEditForm()
}

// 删除记录
const deleteRecord = async (record: UniversalChartDataView) => {
  try {
    const response = await fetch(`/api/chart/simple-data/records/${record.id}`, {
      method: 'DELETE'
    })

    const result = await response.json()

    // 后端返回格式: {code: 'OK', message: '', data: {...}}
    if (result.code === 'OK') {
      message.success('记录删除成功')
      loadData()
    } else {
      message.error('删除失败: ' + (result.message || '未知错误'))
    }
  } catch (error) {
    console.error('删除记录失败:', error)
    message.error('删除记录失败')
  }
}

// 批量删除
const batchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    onOk: async () => {
      batchDeleting.value = true
      try {
        const response = await fetch('/api/chart/simple-data/records/batch', {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(selectedRowKeys.value)
        })

        const result = await response.json()

        // 后端返回格式: {code: 'OK', message: '', data: {...}}
        if (result.code === 'OK') {
          message.success(`成功删除 ${result.data.deletedCount} 条记录`)
          selectedRowKeys.value = []
          loadData()
        } else {
          message.error('批量删除失败: ' + (result.message || '未知错误'))
        }
      } catch (error) {
        console.error('批量删除失败:', error)
        message.error('批量删除失败')
      } finally {
        batchDeleting.value = false
      }
    }
  })
}

// 清除选择
const clearSelection = () => {
  selectedRowKeys.value = []
}

// 显示字段信息
const showFieldsInfo = async () => {
  try {
    const response = await fetch('/api/chart/simple-data/fields-info')

    console.log('🔍 [调试] 字段信息API响应状态:', response.status)

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const result = await response.json()
    console.log('🔍 [调试] 字段信息API响应:', result)

    // 后端返回格式: {code: 'OK', message: '', data: {...}}
    if (result.code === 'OK') {
      const fieldsInfo = result.data
      let content = ''

      Object.entries(fieldsInfo).forEach(([group, fields]) => {
        content += `<h4>${group}</h4><p>${(fields as string[]).join(', ')}</p>`
      })

      Modal.info({
        title: '字段信息说明',
        content: content,
        width: 800
      })
    } else {
      const errorMsg = result.message || '未知错误'
      message.error('获取字段信息失败: ' + errorMsg)
    }
  } catch (error: any) {
    console.error('获取字段信息失败:', error)
    if (error.message && error.message.includes('fetch')) {
      message.error('网络连接失败，请检查后端服务是否启动')
    } else {
      message.error('获取字段信息失败: ' + (error.message || error))
    }
  }
}

// 重新生成示例数据
const regenerateSampleData = () => {
  Modal.confirm({
    title: '确认重新生成',
    content: '这将清空现有数据并重新生成示例数据，确定继续吗？',
    onOk: async () => {
      regenerating.value = true
      try {
        const response = await fetch('/api/chart/simple-data/regenerate-sample-data', {
          method: 'POST'
        })

        const result = await response.json()

        // 后端返回格式: {code: 'OK', message: '', data: {...}}
        if (result.code === 'OK') {
          message.success('示例数据重新生成成功')
          loadData()
        } else {
          message.error('重新生成失败: ' + (result.message || '未知错误'))
        }
      } catch (error) {
        console.error('重新生成失败:', error)
        message.error('重新生成失败')
      } finally {
        regenerating.value = false
      }
    }
  })
}

// 清空所有数据
const clearAllData = () => {
  Modal.confirm({
    title: '确认清空',
    content: '这将删除所有数据记录，确定继续吗？',
    onOk: async () => {
      clearing.value = true
      try {
        const response = await fetch('/api/chart/simple-data/records/all', {
          method: 'DELETE'
        })

        const result = await response.json()

        // 后端返回格式: {code: 'OK', message: '', data: {...}}
        if (result.code === 'OK') {
          message.success('所有数据已清空')
          loadData()
        } else {
          message.error('清空失败: ' + (result.message || '未知错误'))
        }
      } catch (error) {
        console.error('清空数据失败:', error)
        message.error('清空数据失败')
      } finally {
        clearing.value = false
      }
    }
  })
}

// ==================== 辅助方法 ====================

// 获取分类颜色
const getCategoryColor = (category: string) => {
  const colorMap: Record<string, string> = {
    '电子产品': 'blue',
    '服装': 'green',
    '食品': 'orange',
    '家居': 'purple',
    '图书': 'cyan'
  }
  return colorMap[category] || 'default'
}

// 格式化数字
const formatNumber = (num: number | undefined) => {
  if (num === undefined || num === null) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 格式化百分比
const formatPercentage = (num: number | undefined) => {
  if (num === undefined || num === null) return '0%'
  return `${num.toFixed(1)}%`
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
/* ==================== 页面容器布局 ==================== */
.page-container {
  /* 确保页面容器占满整个视窗 */
  width: 100vw;
  height: 100vh;
  /* 启用垂直滚动，允许水平溢出以显示固定列 */
  overflow-y: auto;
  overflow-x: auto;
  /* 重置定位，避免遮罩效果 */
  position: relative;
  z-index: 1;
  /* 背景色 */
  background-color: #f5f5f5;
  /* 左对齐，顶部对齐 - 移除内边距 */
  padding: 0;
  /* 盒模型 */
  box-sizing: border-box;
}

.universal-data-manager {
  /* 内容容器 - 左对齐，顶部对齐 */
  width: 100%;
  max-width: none;
  margin: 0;
  /* 最小高度确保内容可见 */
  min-height: 100vh;
  /* 相对定位 */
  position: relative;
  /* 背景透明 */
  background: transparent;
  /* 左对齐，顶部对齐 - 添加少量内边距 */
  padding: 8px;
  box-sizing: border-box;
}

/* ==================== 卡片样式 ==================== */
.main-card {
  /* 卡片样式 - 左对齐，顶部对齐 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: white;
  /* 确保卡片内容可滚动 */
  overflow: visible;
  /* 最小高度 - 确保分页器可见 */
  min-height: calc(100vh - 16px);
  max-height: calc(100vh - 16px);
  display: flex;
  flex-direction: column;
  /* 左对齐 */
  margin: 0;
}

.main-card .ant-card-head {
  border-bottom: 2px solid #f0f0f0;
  background: white;
  /* 确保头部区域可见且有足够空间 */
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /* 确保正确的层级 */
  position: relative;
  z-index: 10;
}

.main-card .ant-card-head-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  /* 确保标题区域不会被挤压 */
  flex-shrink: 0;
}

/* 确保右侧操作区域可见 */
.main-card .ant-card-extra {
  /* 右侧额外内容区域 */
  display: flex !important;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
  /* 确保按钮组可见 */
  visibility: visible !important;
  opacity: 1 !important;
}

/* 确保按钮组正常显示 */
.main-card .ant-card-extra .ant-space {
  display: flex !important;
  gap: 8px;
  flex-wrap: wrap;
}

.main-card .ant-card-body {
  /* 卡片内容区域 - 减少内边距 */
  padding: 12px;
  overflow: visible;
  flex: 1;
  display: flex;
  flex-direction: column;
  /* 确保有足够宽度显示固定列 */
  min-width: 0;
  width: 100%;
}

/* 表格容器占剩余空间 */
.ant-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  /* 确保表格容器有足够宽度 */
  min-width: 100%;
  overflow-x: auto;
}



/* ==================== 批量操作区域 ==================== */
.batch-actions {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f0f8ff;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
}

.selected-info {
  font-weight: 500;
  color: #1890ff;
}

/* ==================== 表格样式 ==================== */
.table-title {
  font-weight: 500;
  color: #262626;
}

.amount-text {
  font-weight: 500;
  color: #52c41a;
}

/* 表格行高度压缩 */
.ant-table-small .ant-table-tbody > tr > td {
  padding: 4px 8px !important;
  line-height: 1.2 !important;
}

.ant-table-small .ant-table-thead > tr > th {
  padding: 6px 8px !important;
  line-height: 1.3 !important;
}

/* 表格内文字紧凑显示 */
.ant-table-small {
  font-size: 13px;
}

.ant-table-small .ant-tag {
  margin: 0;
  padding: 1px 6px;
  font-size: 12px;
  line-height: 1.3;
}

.ant-table-small .ant-btn {
  padding: 1px 4px;
  font-size: 11px;
  height: 22px;
  line-height: 1.2;
}

/* ==================== 表格滚动和布局 ==================== */
.ant-table {
  background: white;
  border-radius: 8px;
  /* 确保表格能够正确显示固定列 */
  overflow: visible;
  width: 100%;
}

.ant-table-wrapper {
  /* 表格包装器支持水平滚动 */
  overflow-x: auto;
  overflow-y: visible;
  /* 确保有足够空间显示固定列 */
  width: 100%;
  position: relative;
}

.ant-table-container {
  /* 表格容器支持水平滚动 */
  overflow-x: auto;
  overflow-y: visible;
  /* 最小宽度确保所有列都能显示 */
  min-width: 2000px;
}

.ant-table-content {
  /* 表格内容区域 */
  overflow-x: auto;
  overflow-y: visible;
  position: relative;
}

.ant-table-tbody {
  /* 表格主体 */
  overflow: visible;
  position: relative;
}

.ant-table-thead > tr > th {
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
  /* 固定表头，防止滚动时消失 */
  position: sticky;
  top: 0;
  z-index: 2;
}

/* ==================== 按钮样式 ==================== */
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

/* ==================== 标签样式 ==================== */
.ant-tag {
  border-radius: 4px;
  font-weight: 500;
}

/* ==================== 模态框样式 ==================== */
.ant-modal {
  /* 确保模态框在正确的层级 */
  z-index: 1000;
}

.ant-modal-header {
  border-bottom: 2px solid #f0f0f0;
}

.ant-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.ant-modal-body {
  /* 模态框内容支持滚动 */
  max-height: 70vh;
  overflow-y: auto;
}

/* ==================== 表单样式 ==================== */
.ant-form-item-label > label {
  font-weight: 600;
  color: #262626;
}

.ant-tabs-card > .ant-tabs-content {
  margin-top: 16px;
}

.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab {
  border-radius: 6px 6px 0 0;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1400px) {
  .page-container {
    padding: 0;
  }

  .universal-data-manager {
    padding: 8px;
  }

  /* 确保中等屏幕下右侧操作区域可见 */
  .main-card .ant-card-extra .ant-space {
    gap: 6px;
  }
}

@media (max-width: 1200px) {
  .page-container {
    padding: 0;
  }

  .universal-data-manager {
    padding: 8px;
    min-height: 100vh;
  }

  .main-card .ant-card-body {
    padding: 16px;
  }

  /* 小屏幕下确保操作按钮可见 */
  .main-card .ant-card-extra .ant-space {
    gap: 4px;
  }

  .main-card .ant-card-extra .ant-btn {
    font-size: 12px;
    padding: 4px 8px;
    height: auto;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 0;
    height: 100vh;
  }

  .universal-data-manager {
    padding: 4px;
    min-height: 100vh;
  }

  .ant-table {
    font-size: 12px;
  }

  .main-card .ant-card-head-title {
    font-size: 16px;
  }

  .main-card .ant-card-body {
    padding: 12px;
  }

  /* 移动端确保右侧操作区域换行显示 */
  .main-card .ant-card-head {
    flex-wrap: wrap;
    min-height: auto;
    padding: 12px 16px;
  }

  .main-card .ant-card-extra {
    width: 100%;
    margin-top: 12px;
    margin-left: 0;
    justify-content: center;
  }

  .main-card .ant-card-extra .ant-space {
    flex-wrap: wrap;
    justify-content: center;
    gap: 6px;
  }

  .main-card .ant-card-extra .ant-btn {
    font-size: 11px;
    padding: 2px 6px;
    height: 28px;
  }

  .ant-modal {
    margin: 0;
    max-width: 100vw;
    height: 100vh;
  }

  .ant-modal-body {
    max-height: calc(100vh - 120px);
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 0;
  }

  .universal-data-manager {
    padding: 2px;
  }

  .ant-table {
    font-size: 11px;
  }

  /* 超小屏幕下的操作区域 */
  .main-card .ant-card-extra .ant-btn {
    font-size: 10px;
    padding: 1px 4px;
    height: 24px;
  }

  .main-card .ant-card-extra .ant-space {
    gap: 4px;
  }
}

/* ==================== 加载和空状态样式 ==================== */
.ant-spin-container {
  min-height: 200px;
}

.ant-empty {
  padding: 40px 0;
}

.ant-empty-description {
  color: #999;
  font-size: 14px;
}

/* ==================== 滚动条样式 ==================== */
.page-container::-webkit-scrollbar {
  width: 8px;
}

.page-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.page-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.page-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.ant-table-wrapper::-webkit-scrollbar {
  height: 8px;
}

.ant-table-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.ant-table-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.ant-table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* ==================== 修复层级问题 ==================== */
.ant-modal-mask {
  z-index: 1000;
}

.ant-modal-wrap {
  z-index: 1000;
}

.ant-message {
  z-index: 1010;
}

.ant-notification {
  z-index: 1010;
}

/* ==================== 全局重置和优化 ==================== */
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow-x: hidden;
}

#app {
  height: 100%;
  overflow: visible;
}

/* ==================== 确保内容可见性 ==================== */
.page-container > * {
  position: relative;
  z-index: 1;
}

/* ==================== 表格特殊优化 ==================== */
.ant-table-fixed-left,
.ant-table-fixed-right {
  z-index: 3;
}

.ant-table-thead th.ant-table-column-has-sorters:hover {
  background-color: #f5f5f5;
}

/* 简化固定列样式 */
.ant-table-fixed-left {
  border-right: 1px solid #e8e8e8;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
}

.ant-table-fixed-right {
  border-left: 1px solid #e8e8e8;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.05);
  background-color: white;
}

/* 强力调试：给操作列添加非常明显的样式 */
.ant-table-thead > tr > th:last-child {
  background-color: #ff0000 !important;
  color: white !important;
  border: 3px solid #000000 !important;
  font-size: 16px !important;
  font-weight: bold !important;
}

.ant-table-tbody > tr > td:last-child {
  background-color: #ff0000 !important;
  color: white !important;
  border: 3px solid #000000 !important;
  min-width: 200px !important;
}

/* 确保操作列内容可见 */
.ant-table-tbody > tr > td:last-child * {
  color: white !important;
}

/* 通过类名强制显示操作列 */
.action-column {
  background-color: #00ff00 !important;
  border: 5px solid #0000ff !important;
  min-width: 200px !important;
  max-width: 200px !important;
  width: 200px !important;
  display: table-cell !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 操作列按钮样式优化 */
.ant-table-small .ant-table-tbody .ant-btn-link {
  padding: 2px 4px;
  height: 20px;
  font-size: 12px;
  line-height: 1;
  border: none;
}

.ant-table-small .ant-table-tbody .ant-btn-link:hover {
  background-color: rgba(24, 144, 255, 0.1);
  border-radius: 3px;
}

.ant-table-small .ant-table-tbody .ant-btn-link.ant-btn-dangerous:hover {
  background-color: rgba(255, 77, 79, 0.1);
}

/* ==================== 分页器样式 ==================== */
.ant-pagination {
  margin-top: 8px !important;
  margin-bottom: 0 !important;
  text-align: center;
  padding: 8px 0;
}

.ant-pagination.ant-pagination-small {
  font-size: 12px;
}

.ant-pagination.ant-pagination-small .ant-pagination-item {
  min-width: 24px;
  height: 24px;
  line-height: 24px;
  margin: 0 2px;
}

.ant-pagination.ant-pagination-small .ant-pagination-item a {
  padding: 0 4px;
}

.ant-pagination.ant-pagination-small .ant-pagination-prev,
.ant-pagination.ant-pagination-small .ant-pagination-next {
  min-width: 24px;
  height: 24px;
  line-height: 24px;
}

.ant-pagination-total-text {
  font-size: 12px;
  color: #666;
  margin-right: 8px;
}

/* ==================== 加载遮罩优化 ==================== */
.ant-spin-nested-loading > div > .ant-spin {
  z-index: 4;
}

/* ==================== 确保模态框正常显示 ==================== */
.ant-modal-root {
  position: relative;
  z-index: 1000;
}
</style>
