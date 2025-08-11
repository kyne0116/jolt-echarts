<template>
  <div class="simple-data-crud-container">
    <div class="page-header">
      <h2>🗄️ 简化数据管理</h2>
      <p class="page-description">对40个固定字段的UniversalChartDataView进行基本的增删改查操作</p>
    </div>

    <a-card title="数据记录管理" class="main-card">
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

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input
              v-model:value="searchKeyword"
              placeholder="搜索标题或描述"
              @change="handleSearch"
            >
              <template #prefix>
                <SearchOutlined />
              </template>
            </a-input>
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="filterChartType"
              placeholder="筛选图表类型"
              @change="handleFilter"
              allowClear
            >
              <a-select-option value="line">折线图</a-select-option>
              <a-select-option value="bar">柱状图</a-select-option>
              <a-select-option value="pie">饼图</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="filterChannel"
              placeholder="筛选渠道"
              @change="handleFilter"
              allowClear
            >
              <a-select-option value="Email">Email</a-select-option>
              <a-select-option value="Social Media">Social Media</a-select-option>
              <a-select-option value="Direct">Direct</a-select-option>
              <a-select-option value="Search">Search</a-select-option>
              <a-select-option value="Referral">Referral</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-button @click="resetFilters">
              <ClearOutlined />
              重置筛选
            </a-button>
          </a-col>
        </a-row>
      </div>

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
          showTotal: (total) => `共 ${total} 条记录`,
          onChange: handlePageChange,
          onShowSizeChange: handlePageSizeChange
        }"
        :scroll="{ x: 'max-content' }"
        row-key="id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a-tooltip :title="record.description">
              <span>{{ record.title }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'chartType'">
            <a-tag :color="getChartTypeColor(record.chartType)">
              {{ getChartTypeName(record.chartType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'amount'">
            <span>¥{{ record.amount?.toFixed(2) || '0.00' }}</span>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            <span>{{ formatDateTime(record.createdAt) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewRecord(record)">
                <EyeOutlined />
                查看
              </a-button>
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

      <!-- 批量操作 -->
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <a-space>
          <span>已选择 {{ selectedRowKeys.length }} 条记录</span>
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
      width="800px"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        layout="vertical"
      >
        <a-row :gutter="16">
          <!-- 基础信息字段 -->
          <a-col :span="12">
            <a-form-item label="标题" name="title">
              <a-input v-model:value="editForm.title" placeholder="请输入标题" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图表类型" name="chartType">
              <a-select v-model:value="editForm.chartType" placeholder="选择图表类型">
                <a-select-option value="line">折线图</a-select-option>
                <a-select-option value="bar">柱状图</a-select-option>
                <a-select-option value="pie">饼图</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="渠道名称" name="channelName">
              <a-select v-model:value="editForm.channelName" placeholder="选择渠道">
                <a-select-option value="Email">Email</a-select-option>
                <a-select-option value="Social Media">Social Media</a-select-option>
                <a-select-option value="Direct">Direct</a-select-option>
                <a-select-option value="Search">Search</a-select-option>
                <a-select-option value="Referral">Referral</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="产品名称" name="productName">
              <a-input v-model:value="editForm.productName" placeholder="请输入产品名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="地区" name="region">
              <a-select v-model:value="editForm.region" placeholder="选择地区">
                <a-select-option value="华北">华北</a-select-option>
                <a-select-option value="华东">华东</a-select-option>
                <a-select-option value="华南">华南</a-select-option>
                <a-select-option value="华中">华中</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="部门" name="department">
              <a-select v-model:value="editForm.department" placeholder="选择部门">
                <a-select-option value="销售部">销售部</a-select-option>
                <a-select-option value="市场部">市场部</a-select-option>
                <a-select-option value="技术部">技术部</a-select-option>
                <a-select-option value="客服部">客服部</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <!-- 数值字段 -->
          <a-col :span="12">
            <a-form-item label="转化数量" name="conversionCount">
              <a-input-number v-model:value="editForm.conversionCount" placeholder="转化数量" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="点击数量" name="clickCount">
              <a-input-number v-model:value="editForm.clickCount" placeholder="点击数量" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="金额" name="amount">
              <a-input-number v-model:value="editForm.amount" placeholder="金额" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数量" name="quantity">
              <a-input-number v-model:value="editForm.quantity" placeholder="数量" style="width: 100%" />
            </a-form-item>
          </a-col>
          
          <a-col :span="24">
            <a-form-item label="描述" name="description">
              <a-textarea v-model:value="editForm.description" placeholder="请输入描述" :rows="3" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 查看记录模态框 -->
    <a-modal
      v-model:open="viewModalVisible"
      title="查看记录详情"
      :footer="null"
      width="800px"
    >
      <a-descriptions v-if="viewingRecord" title="记录信息" bordered>
        <a-descriptions-item label="ID">{{ viewingRecord.id }}</a-descriptions-item>
        <a-descriptions-item label="标题">{{ viewingRecord.title }}</a-descriptions-item>
        <a-descriptions-item label="图表类型">{{ getChartTypeName(viewingRecord.chartType) }}</a-descriptions-item>
        <a-descriptions-item label="渠道名称">{{ viewingRecord.channelName }}</a-descriptions-item>
        <a-descriptions-item label="产品名称">{{ viewingRecord.productName }}</a-descriptions-item>
        <a-descriptions-item label="地区">{{ viewingRecord.region }}</a-descriptions-item>
        <a-descriptions-item label="部门">{{ viewingRecord.department }}</a-descriptions-item>
        <a-descriptions-item label="转化数量">{{ viewingRecord.conversionCount }}</a-descriptions-item>
        <a-descriptions-item label="点击数量">{{ viewingRecord.clickCount }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ viewingRecord.amount?.toFixed(2) || '0.00' }}</a-descriptions-item>
        <a-descriptions-item label="数量">{{ viewingRecord.quantity }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(viewingRecord.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ formatDateTime(viewingRecord.updatedAt) }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="3">{{ viewingRecord.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 字段信息模态框 -->
    <a-modal
      v-model:open="fieldsInfoModalVisible"
      title="字段信息"
      :footer="null"
      width="600px"
    >
      <div v-if="fieldsInfo">
        <a-collapse>
          <a-collapse-panel v-for="(fields, group) in fieldsInfo" :key="group" :header="group">
            <a-tag v-for="field in fields" :key="field" style="margin: 4px;">
              {{ field }}
            </a-tag>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    ClearOutlined,
    CopyOutlined,
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    InfoCircleOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'

// ==================== 响应式数据 ====================

// 数据列表相关
const dataList = ref([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索和筛选
const searchKeyword = ref('')
const filterChartType = ref('')
const filterChannel = ref('')

// 选择相关
const selectedRowKeys = ref([])
const batchDeleting = ref(false)

// 编辑相关
const editModalVisible = ref(false)
const editingRecord = ref(null)
const saving = ref(false)
const editForm = reactive({
  title: '',
  chartType: '',
  channelName: '',
  productName: '',
  region: '',
  department: '',
  conversionCount: null,
  clickCount: null,
  amount: null,
  quantity: null,
  description: ''
})

// 查看相关
const viewModalVisible = ref(false)
const viewingRecord = ref(null)

// 字段信息相关
const fieldsInfoModalVisible = ref(false)
const fieldsInfo = ref(null)

// 其他操作
const regenerating = ref(false)
const clearing = ref(false)

// ==================== 表格配置 ====================

// 表格列定义
const tableColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 150,
    ellipsis: true
  },
  {
    title: '图表类型',
    dataIndex: 'chartType',
    key: 'chartType',
    width: 100
  },
  {
    title: '渠道名称',
    dataIndex: 'channelName',
    key: 'channelName',
    width: 120
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 120
  },
  {
    title: '地区',
    dataIndex: 'region',
    key: 'region',
    width: 80
  },
  {
    title: '部门',
    dataIndex: 'department',
    key: 'department',
    width: 100
  },
  {
    title: '转化数量',
    dataIndex: 'conversionCount',
    key: 'conversionCount',
    width: 100
  },
  {
    title: '点击数量',
    dataIndex: 'clickCount',
    key: 'clickCount',
    width: 100
  },
  {
    title: '金额',
    dataIndex: 'amount',
    key: 'amount',
    width: 120
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 80
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  }
}))

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
})

// ==================== 方法定义 ====================

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/chart/simple-data/records?page=${currentPage.value - 1}&size=${pageSize.value}`)
    const result = await response.json()

    if (result.success) {
      dataList.value = result.data.records || []
      totalCount.value = result.data.totalCount || 0
      console.log('✅ [简化CRUD] 加载数据成功:', dataList.value.length)
    } else {
      message.error('加载数据失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [简化CRUD] 加载数据失败:', error)
    message.error('加载数据失败')
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

// 搜索处理
const handleSearch = () => {
  // 这里可以实现搜索逻辑
  console.log('搜索关键词:', searchKeyword.value)
  // 暂时重新加载数据
  loadData()
}

// 筛选处理
const handleFilter = () => {
  console.log('筛选条件:', { chartType: filterChartType.value, channel: filterChannel.value })
  // 这里可以实现筛选逻辑
  loadData()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterChartType.value = ''
  filterChannel.value = ''
  loadData()
}

// 显示添加模态框
const showAddModal = () => {
  editingRecord.value = null
  resetEditForm()
  editModalVisible.value = true
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    title: '',
    chartType: '',
    channelName: '',
    productName: '',
    region: '',
    department: '',
    conversionCount: null,
    clickCount: null,
    amount: null,
    quantity: null,
    description: ''
  })
}

// 编辑记录
const editRecord = (record: any) => {
  editingRecord.value = record
  Object.assign(editForm, {
    title: record.title || '',
    chartType: record.chartType || '',
    channelName: record.channelName || '',
    productName: record.productName || '',
    region: record.region || '',
    department: record.department || '',
    conversionCount: record.conversionCount,
    clickCount: record.clickCount,
    amount: record.amount,
    quantity: record.quantity,
    description: record.description || ''
  })
  editModalVisible.value = true
}

// 保存记录
const handleSaveRecord = async () => {
  saving.value = true
  try {
    const url = editingRecord.value
      ? `/api/chart/simple-data/records/${editingRecord.value.id}`
      : '/api/chart/simple-data/records'

    const method = editingRecord.value ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(editForm)
    })

    const result = await response.json()

    if (result.success) {
      message.success(editingRecord.value ? '记录更新成功' : '记录创建成功')
      editModalVisible.value = false
      loadData()
    } else {
      message.error('保存失败: ' + result.message)
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

// 查看记录
const viewRecord = (record: any) => {
  viewingRecord.value = record
  viewModalVisible.value = true
}

// 复制记录
const duplicateRecord = (record: any) => {
  editingRecord.value = null
  Object.assign(editForm, {
    title: (record.title || '') + ' (副本)',
    chartType: record.chartType || '',
    channelName: record.channelName || '',
    productName: record.productName || '',
    region: record.region || '',
    department: record.department || '',
    conversionCount: record.conversionCount,
    clickCount: record.clickCount,
    amount: record.amount,
    quantity: record.quantity,
    description: record.description || ''
  })
  editModalVisible.value = true
}

// 删除记录
const deleteRecord = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除记录 "${record.title}" 吗？`,
    onOk: async () => {
      try {
        const response = await fetch(`/api/chart/simple-data/records/${record.id}`, {
          method: 'DELETE'
        })

        const result = await response.json()

        if (result.success) {
          message.success('记录删除成功')
          loadData()
        } else {
          message.error('删除失败: ' + result.message)
        }
      } catch (error) {
        console.error('删除记录失败:', error)
        message.error('删除记录失败')
      }
    }
  })
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

        if (result.success) {
          message.success(`成功删除 ${result.data.deletedCount} 条记录`)
          selectedRowKeys.value = []
          loadData()
        } else {
          message.error('批量删除失败: ' + result.message)
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
    const result = await response.json()

    if (result.success) {
      fieldsInfo.value = result.data
      fieldsInfoModalVisible.value = true
    } else {
      message.error('获取字段信息失败: ' + result.message)
    }
  } catch (error) {
    console.error('获取字段信息失败:', error)
    message.error('获取字段信息失败')
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

        if (result.success) {
          message.success('示例数据重新生成成功')
          loadData()
        } else {
          message.error('重新生成失败: ' + result.message)
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

        if (result.success) {
          message.success('所有数据已清空')
          loadData()
        } else {
          message.error('清空失败: ' + result.message)
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

// 获取图表类型颜色
const getChartTypeColor = (chartType: string) => {
  const colorMap: Record<string, string> = {
    'line': 'blue',
    'bar': 'green',
    'pie': 'orange'
  }
  return colorMap[chartType] || 'default'
}

// 获取图表类型名称
const getChartTypeName = (chartType: string) => {
  const nameMap: Record<string, string> = {
    'line': '折线图',
    'bar': '柱状图',
    'pie': '饼图'
  }
  return nameMap[chartType] || chartType
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.simple-data-crud-container {
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

.main-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.main-card .ant-card-head {
  border-bottom: 2px solid #f0f0f0;
}

.main-card .ant-card-head-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.search-section {
  margin-bottom: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.batch-actions {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f0f8ff;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
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

.ant-tag {
  border-radius: 4px;
  font-weight: 500;
}

.ant-modal-header {
  border-bottom: 2px solid #f0f0f0;
}

.ant-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.ant-form-item-label > label {
  font-weight: 600;
  color: #262626;
}

.ant-descriptions-title {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.ant-collapse {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
}

.ant-collapse-header {
  font-weight: 600;
  color: #262626;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .simple-data-crud-container {
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
  .simple-data-crud-container {
    padding: 12px;
  }

  .search-section .ant-row {
    flex-direction: column;
  }

  .search-section .ant-col {
    width: 100%;
    margin-bottom: 8px;
  }

  .ant-table {
    font-size: 12px;
  }
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
