<template>
  <div class="virtual-database-container">
    <div class="page-header">
      <h2>虚拟数据库管理</h2>
      <p class="page-description">管理虚拟数据源和数据表结构</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：数据源列表 -->
      <a-col :span="8">
        <a-card title="数据源" class="datasource-card">
          <template #extra>
            <a-button type="primary" size="small" @click="showAddDatasourceModal">
              <PlusOutlined />
              新增
            </a-button>
          </template>
          
          <a-tree
            :tree-data="datasourceTree"
            :selected-keys="selectedKeys"
            @select="onSelectNode"
            :field-names="{ children: 'children', title: 'title', key: 'key' }"
          >
            <template #title="{ title, type, icon }">
              <span class="tree-node">
                <component :is="icon" v-if="icon" style="margin-right: 4px;" />
                {{ title }}
              </span>
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 右侧：详情和编辑 -->
      <a-col :span="16">
        <a-card :title="currentTitle" class="detail-card">
          <template #extra>
            <a-space>
              <a-button v-if="selectedNode?.type === 'table'" @click="showAddColumnModal">
                <PlusOutlined />
                添加字段
              </a-button>
              <a-button @click="refreshData">
                <ReloadOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <!-- 数据源详情 -->
          <div v-if="selectedNode?.type === 'datasource'" class="datasource-detail">
            <a-descriptions title="数据源信息" bordered>
              <a-descriptions-item label="名称">{{ selectedNode.name }}</a-descriptions-item>
              <a-descriptions-item label="类型">{{ selectedNode.dbType }}</a-descriptions-item>
              <a-descriptions-item label="连接状态">
                <a-tag :color="selectedNode.status === 'connected' ? 'green' : 'red'">
                  {{ selectedNode.status === 'connected' ? '已连接' : '未连接' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="描述" :span="3">{{ selectedNode.description }}</a-descriptions-item>
            </a-descriptions>
            
            <a-divider>数据表列表</a-divider>
            
            <a-table
              :columns="tableColumns"
              :data-source="selectedNode.tables || []"
              :pagination="false"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="viewTable(record)">
                      查看
                    </a-button>
                    <a-button type="link" size="small" @click="editTable(record)">
                      编辑
                    </a-button>
                    <a-popconfirm
                      title="确定要删除这个表吗？"
                      @confirm="deleteTable(record.id)"
                    >
                      <a-button type="link" size="small" danger>
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 数据表详情 -->
          <div v-else-if="selectedNode?.type === 'table'" class="table-detail">
            <a-descriptions title="数据表信息" bordered>
              <a-descriptions-item label="表名">{{ selectedNode.name }}</a-descriptions-item>
              <a-descriptions-item label="记录数">{{ selectedNode.rowCount || 0 }}</a-descriptions-item>
              <a-descriptions-item label="描述" :span="2">{{ selectedNode.description }}</a-descriptions-item>
            </a-descriptions>
            
            <a-divider>字段列表</a-divider>
            
            <a-table
              :columns="columnColumns"
              :data-source="selectedNode.columns || []"
              :pagination="false"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'isPrimaryKey'">
                  <a-tag v-if="record.isPrimaryKey" color="gold">主键</a-tag>
                </template>
                <template v-else-if="column.key === 'isNullable'">
                  <a-tag :color="record.isNullable ? 'default' : 'red'">
                    {{ record.isNullable ? '可空' : '非空' }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="editColumn(record)">
                      编辑
                    </a-button>
                    <a-popconfirm
                      title="确定要删除这个字段吗？"
                      @confirm="deleteColumn(record.id)"
                    >
                      <a-button type="link" size="small" danger>
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <a-empty description="请选择一个数据源或数据表查看详情" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增数据源模态框 -->
    <a-modal
      v-model:open="datasourceModalVisible"
      title="新增数据源"
      @ok="handleAddDatasource"
      @cancel="handleCancel"
    >
      <a-form ref="datasourceFormRef" :model="newDatasource" layout="vertical">
        <a-form-item label="数据源名称" name="name" :rules="[{ required: true, message: '请输入数据源名称' }]">
          <a-input v-model:value="newDatasource.name" />
        </a-form-item>
        <a-form-item label="数据库类型" name="dbType" :rules="[{ required: true, message: '请选择数据库类型' }]">
          <a-select v-model:value="newDatasource.dbType">
            <a-select-option value="mysql">MySQL</a-select-option>
            <a-select-option value="postgresql">PostgreSQL</a-select-option>
            <a-select-option value="oracle">Oracle</a-select-option>
            <a-select-option value="sqlserver">SQL Server</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="newDatasource.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 新增字段模态框 -->
    <a-modal
      v-model:open="columnModalVisible"
      title="新增字段"
      @ok="handleAddColumn"
      @cancel="handleCancel"
    >
      <a-form ref="columnFormRef" :model="newColumn" layout="vertical">
        <a-form-item label="字段名" name="name" :rules="[{ required: true, message: '请输入字段名' }]">
          <a-input v-model:value="newColumn.name" />
        </a-form-item>
        <a-form-item label="数据类型" name="dataType" :rules="[{ required: true, message: '请选择数据类型' }]">
          <a-select v-model:value="newColumn.dataType">
            <a-select-option value="VARCHAR">VARCHAR</a-select-option>
            <a-select-option value="INT">INT</a-select-option>
            <a-select-option value="BIGINT">BIGINT</a-select-option>
            <a-select-option value="DECIMAL">DECIMAL</a-select-option>
            <a-select-option value="DATE">DATE</a-select-option>
            <a-select-option value="DATETIME">DATETIME</a-select-option>
            <a-select-option value="TEXT">TEXT</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item>
              <a-checkbox v-model:checked="newColumn.isPrimaryKey">主键</a-checkbox>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item>
              <a-checkbox v-model:checked="newColumn.isNullable">可空</a-checkbox>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="描述">
          <a-textarea v-model:value="newColumn.description" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    DatabaseOutlined,
    PlusOutlined,
    ReloadOutlined,
    TableOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'

// 响应式数据
const datasourceModalVisible = ref(false)
const columnModalVisible = ref(false)
const selectedKeys = ref<string[]>([])
const selectedNode = ref<any>(null)
const datasourceFormRef = ref()
const columnFormRef = ref()

const newDatasource = reactive({
  name: '',
  dbType: '',
  description: '',
})

const newColumn = reactive({
  name: '',
  dataType: '',
  isPrimaryKey: false,
  isNullable: true,
  description: '',
})

// 模拟数据
const datasourceTree = ref([
  {
    key: 'ds1',
    title: '销售数据库',
    type: 'datasource',
    icon: DatabaseOutlined,
    name: '销售数据库',
    dbType: 'MySQL',
    status: 'connected',
    description: '存储销售相关数据的数据库',
    tables: [
      {
        id: 1,
        name: 'sales_data',
        description: '销售数据表',
        rowCount: 1000,
      },
      {
        id: 2,
        name: 'customer_info',
        description: '客户信息表',
        rowCount: 500,
      },
    ],
    children: [
      {
        key: 'ds1_table1',
        title: 'sales_data',
        type: 'table',
        icon: TableOutlined,
        name: 'sales_data',
        description: '销售数据表',
        rowCount: 1000,
        columns: [
          {
            id: 1,
            name: 'id',
            dataType: 'BIGINT',
            isPrimaryKey: true,
            isNullable: false,
            description: '主键ID',
          },
          {
            id: 2,
            name: 'title',
            dataType: 'VARCHAR',
            isPrimaryKey: false,
            isNullable: false,
            description: '标题',
          },
          {
            id: 3,
            name: 'monthly_sales',
            dataType: 'DECIMAL',
            isPrimaryKey: false,
            isNullable: true,
            description: '月度销售额',
          },
        ],
      },
    ],
  },
])

// 表格列配置
const tableColumns = [
  { title: '表名', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '记录数', dataIndex: 'rowCount', key: 'rowCount' },
  { title: '操作', key: 'action', width: 150 },
]

const columnColumns = [
  { title: '字段名', dataIndex: 'name', key: 'name' },
  { title: '数据类型', dataIndex: 'dataType', key: 'dataType' },
  { title: '主键', key: 'isPrimaryKey' },
  { title: '可空', key: 'isNullable' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '操作', key: 'action', width: 120 },
]

// 计算属性
const currentTitle = computed(() => {
  if (!selectedNode.value) return '详情'
  if (selectedNode.value.type === 'datasource') return `数据源: ${selectedNode.value.name}`
  if (selectedNode.value.type === 'table') return `数据表: ${selectedNode.value.name}`
  return '详情'
})

// 方法
const onSelectNode = (keys: string[], { node }: any) => {
  selectedKeys.value = keys
  selectedNode.value = node
}

const showAddDatasourceModal = () => {
  datasourceModalVisible.value = true
  Object.assign(newDatasource, {
    name: '',
    dbType: '',
    description: '',
  })
}

const showAddColumnModal = () => {
  columnModalVisible.value = true
  Object.assign(newColumn, {
    name: '',
    dataType: '',
    isPrimaryKey: false,
    isNullable: true,
    description: '',
  })
}

const handleAddDatasource = async () => {
  try {
    await datasourceFormRef.value.validate()
    message.success('数据源创建成功')
    datasourceModalVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleAddColumn = async () => {
  try {
    await columnFormRef.value.validate()
    message.success('字段添加成功')
    columnModalVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  datasourceModalVisible.value = false
  columnModalVisible.value = false
}

const viewTable = (table: any) => {
  message.info(`查看表: ${table.name}`)
}

const editTable = (table: any) => {
  message.info(`编辑表: ${table.name}`)
}

const deleteTable = (id: number) => {
  message.success('删除成功')
}

const editColumn = (column: any) => {
  message.info(`编辑字段: ${column.name}`)
}

const deleteColumn = (id: number) => {
  message.success('删除成功')
}

const refreshData = () => {
  message.success('数据已刷新')
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.virtual-database-container {
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

.datasource-card,
.detail-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tree-node {
  display: flex;
  align-items: center;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.datasource-detail,
.table-detail {
  min-height: 400px;
}
</style>
