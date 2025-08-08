<template>
  <div class="chart-config-container">
    <div class="page-header">
      <h2>图表配置管理</h2>
      <p class="page-description">管理和配置各种图表类型的参数设置</p>
    </div>

    <a-card title="图表配置列表" class="config-card">
      <template #extra>
        <a-button type="primary" @click="showAddModal">
          <PlusOutlined />
          新增配置
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="configList"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'chartType'">
            <a-tag :color="getChartTypeColor(record.chartType)">
              {{ getChartTypeName(record.chartType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'red'">
              {{ record.status === 'active' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editConfig(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="viewConfig(record)">
                查看
              </a-button>
              <a-popconfirm
                title="确定要删除这个配置吗？"
                @confirm="deleteConfig(record.id)"
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

    <!-- 添加/编辑配置模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑配置' : '新增配置'"
      width="800px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="配置名称" name="name">
              <a-input v-model:value="formData.name" placeholder="请输入配置名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="图表类型" name="chartType">
              <a-select v-model:value="formData.chartType" placeholder="请选择图表类型">
                <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
                <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
                <a-select-option value="pie_chart">饼图</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="配置描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="请输入配置描述" :rows="3" />
        </a-form-item>
        
        <a-form-item label="配置参数" name="config">
          <a-textarea 
            v-model:value="formData.config" 
            placeholder="请输入JSON格式的配置参数" 
            :rows="8"
          />
        </a-form-item>
      </a-form>
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
const isEdit = ref(false)
const configList = ref<any[]>([])
const formRef = ref()

// 表格列配置
const columns = [
  {
    title: '配置名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '图表类型',
    dataIndex: 'chartType',
    key: 'chartType',
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
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
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
  name: '',
  chartType: '',
  description: '',
  config: '',
  status: 'active',
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  chartType: [{ required: true, message: '请选择图表类型', trigger: 'change' }],
  config: [{ required: true, message: '请输入配置参数', trigger: 'blur' }],
}

// 方法
const getChartTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    stacked_line_chart: 'blue',
    basic_bar_chart: 'green',
    pie_chart: 'orange',
  }
  return colorMap[type] || 'default'
}

const getChartTypeName = (type: string) => {
  const nameMap: Record<string, string> = {
    stacked_line_chart: '堆叠折线图',
    basic_bar_chart: '基础柱状图',
    pie_chart: '饼图',
  }
  return nameMap[type] || type
}

const loadConfigList = async () => {
  loading.value = true
  try {
    // 模拟数据
    configList.value = [
      {
        id: 1,
        name: '默认堆叠折线图配置',
        chartType: 'stacked_line_chart',
        description: '用于销售数据展示的堆叠折线图配置',
        status: 'active',
        createTime: '2024-01-01 10:00:00',
      },
      {
        id: 2,
        name: '基础柱状图配置',
        chartType: 'basic_bar_chart',
        description: '用于统计数据展示的基础柱状图配置',
        status: 'active',
        createTime: '2024-01-02 11:00:00',
      },
    ]
    pagination.total = configList.value.length
  } catch (error) {
    message.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const showAddModal = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

const editConfig = (record: any) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(formData, record)
}

const viewConfig = (record: any) => {
  message.info(`查看配置: ${record.name}`)
}

const deleteConfig = async (id: number) => {
  try {
    message.success('删除成功')
    await loadConfigList()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    // 这里应该调用API保存数据
    message.success(isEdit.value ? '更新成功' : '创建成功')
    modalVisible.value = false
    await loadConfigList()
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
    name: '',
    chartType: '',
    description: '',
    config: '',
    status: 'active',
  })
  formRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadConfigList()
})
</script>

<style scoped>
.chart-config-container {
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

.config-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
