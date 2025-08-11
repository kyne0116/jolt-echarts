<template>
  <div class="mapping-container">
    <!-- 上方：图表选择 -->
    <div class="top-section">
      <ChartSelector
        @chart-selected="onChartSelected"
        @chart-cleared="onChartCleared"
        ref="chartSelectorRef"
      />
    </div>

    <!-- 下方：映射配置列表 -->
    <div class="bottom-section">
      <a-card title="映射配置列表" class="mapping-list-card">
        <template #extra>
          <a-space>
            <a-button
              size="small"
              @click="refreshMappingList"
              :loading="listLoading"
            >
              <ReloadOutlined />
              刷新
            </a-button>
            <a-button
              size="small"
              @click="showBatchDeleteModal"
              :disabled="selectedMappings.length === 0"
              danger
            >
              <DeleteOutlined />
              批量删除
            </a-button>
          </a-space>
        </template>

        <div v-if="listLoading" class="loading-container">
          <a-spin tip="加载配置列表中..." />
        </div>

        <div v-else-if="mappingList.length === 0" class="empty-container">
          <a-empty description="暂无映射配置" />
        </div>

        <div v-else class="mapping-list-content">
          <a-table
            :data-source="mappingList"
            :columns="listColumns"
            :pagination="paginationConfig"
            :row-selection="{ selectedRowKeys: selectedMappings, onChange: onSelectionChange }"
            size="small"
            row-key="chartId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'chartType'">
                <a-tag color="blue">{{ record.chartType }}</a-tag>
              </template>

              <template v-if="column.key === 'chartName'">
                <a-tag color="purple">{{ record.chartName }}</a-tag>
              </template>

              <template v-if="column.key === 'universalTemplate'">
                <a-tag color="orange">{{ record.universalTemplate }}</a-tag>
              </template>

              <template v-if="column.key === 'joltSpecFile'">
                <a-tag color="cyan">{{ record.joltSpecFile }}</a-tag>
              </template>

              <template v-if="column.key === 'placeholderCount'">
                <a-badge
                  :count="record.placeholderCount || record.mappingCount || 0"
                  :number-style="{ backgroundColor: '#52c41a' }"
                />
              </template>

              <template v-if="column.key === 'actions'">
                <a-space size="small">
                  <a-button
                    type="link"
                    size="small"
                    @click="viewMappingDetail(record)"
                  >
                    查看
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="configureMapping(record)"
                  >
                    配置映射
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="showConfigGuide(record)"
                  >
                    配置指南
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="deleteMapping(record)"
                    danger
                  >
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </a-card>
    </div>

    <!-- 映射配置模态框 -->
    <a-modal
      v-model:open="mappingModalVisible"
      width="1000px"
      :footer="null"
      :mask="false"
      :keyboard="false"
      :destroy-on-close="false"
      :closable="true"
      :wrap-class-name="'mapping-modal-wrap'"
      :body-style="{ padding: '16px', maxHeight: '70vh', overflow: 'hidden' }"
      class="mapping-config-modal"
      :style="{ top: mappingModalPosition.top + 'px', left: mappingModalPosition.left + 'px' }"
      @click="bringToFront('mapping')"
    >
      <template #title>
        <div
          class="draggable-title"
          @mousedown="startDrag($event, 'mapping')"
        >
          配置映射 - {{ selectedChart.chartId }}
        </div>
      </template>

      <div v-if="selectedChart.chartId">
        <!-- 占位符映射配置 -->
        <div class="modal-mapping-content">
          <div v-if="placeholdersLoading" class="loading-container">
            <a-spin tip="加载占位符中..." />
          </div>

          <div v-else-if="placeholders.length === 0" class="empty-container">
            <a-empty description="该图表暂无占位符" />
          </div>

          <div v-else class="placeholders-mapping">
            <div class="mapping-header">
              <h4>占位符映射配置 ({{ placeholders.length }} 个)</h4>
              <a-progress
                :percent="mappingProgress"
                :stroke-color="mappingProgress === 100 ? '#52c41a' : '#1890ff'"
                size="small"
              />
            </div>

            <div class="mapping-list-container">
              <!-- 表头 -->
              <div class="mapping-table-header">
                <a-row :gutter="12">
                  <a-col :span="6">
                    <strong>占位符</strong>
                  </a-col>
                  <a-col :span="8">
                    <strong>数据库字段</strong>
                  </a-col>
                  <a-col :span="5">
                    <strong>数据类型</strong>
                  </a-col>
                  <a-col :span="5">
                    <strong>聚合方式</strong>
                  </a-col>
                </a-row>
              </div>

              <div class="mapping-list">
                <div
                  v-for="placeholder in placeholders"
                  :key="placeholder"
                  class="mapping-item"
                >
                  <a-row :gutter="12" align="middle">
                    <a-col :span="6">
                      <div class="placeholder-info">
                        <a-tag :color="isMapped(placeholder) ? 'green' : 'orange'">
                          {{ placeholder }}
                        </a-tag>
                        <span class="mapping-status">
                          <a-icon
                            :component="isMapped(placeholder) ? CheckCircleOutlined : ExclamationCircleOutlined"
                            :style="{ color: isMapped(placeholder) ? '#52c41a' : '#faad14' }"
                          />
                        </span>
                      </div>
                    </a-col>
                    <a-col :span="8">
                      <a-select
                        :value="mappingConfigs[placeholder]?.fieldName"
                        placeholder="选择字段"
                        size="small"
                        style="width: 100%; min-width: 200px;"
                        @change="(value) => updateMappingField(placeholder, 'fieldName', value)"
                      >
                        <a-select-opt-group
                          v-for="group in groupedFields"
                          :key="group.name"
                          :label="group.name"
                        >
                          <a-select-option
                            v-for="field in group.fields"
                            :key="field.name"
                            :value="field.name"
                          >
                            {{ field.label }} ({{ field.name }})
                          </a-select-option>
                        </a-select-opt-group>
                      </a-select>
                    </a-col>
                    <a-col :span="5">
                      <a-select
                        :value="mappingConfigs[placeholder]?.dataType"
                        placeholder="数据类型"
                        size="small"
                        style="width: 100%;"
                        @change="(value) => updateMappingField(placeholder, 'dataType', value)"
                      >
                        <a-select-option value="string">字符串</a-select-option>
                        <a-select-option value="number">数字</a-select-option>
                        <a-select-option value="array">数组</a-select-option>
                        <a-select-option value="object">对象</a-select-option>
                      </a-select>
                    </a-col>
                    <a-col :span="5">
                      <a-select
                        :value="mappingConfigs[placeholder]?.aggregationType"
                        placeholder="聚合方式"
                        size="small"
                        style="width: 100%;"
                        @change="(value) => updateMappingField(placeholder, 'aggregationType', value)"
                      >
                        <a-select-option value="none">无聚合</a-select-option>
                        <a-select-option value="sum">求和</a-select-option>
                        <a-select-option value="avg">平均值</a-select-option>
                        <a-select-option value="count">计数</a-select-option>
                        <a-select-option value="max">最大值</a-select-option>
                        <a-select-option value="min">最小值</a-select-option>
                        <a-select-option value="list">列表</a-select-option>
                      </a-select>
                    </a-col>
                  </a-row>

                  <!-- 操作按钮行 -->
                  <div class="mapping-actions" style="margin-top: 8px; text-align: right;">
                    <a-space size="small">
                      <a-button
                        type="text"
                        size="small"
                        @click="clearMapping(placeholder)"
                        :disabled="!isMapped(placeholder)"
                        title="清除映射"
                      >
                        <DeleteOutlined />
                      </a-button>
                      <a-button
                        type="text"
                        size="small"
                        @click="generateSmartMapping(placeholder)"
                        :loading="generatingMappings"
                        title="智能推荐"
                      >
                        <BulbOutlined />
                      </a-button>
                    </a-space>
                  </div>
                </div>
              </div>
            </div>

            <!-- 模态框操作按钮 -->
            <div class="modal-actions" style="margin-top: 24px; text-align: right;">
              <a-space>
                <a-button @click="mappingModalVisible = false">
                  取消
                </a-button>
                <a-button
                  @click="generateSmartMappings"
                  :loading="generatingMappings"
                >
                  <BulbOutlined />
                  智能推荐
                </a-button>
                <a-button
                  type="primary"
                  @click="saveMappingConfig"
                  :loading="savingConfig"
                >
                  <SaveOutlined />
                  保存配置
                </a-button>
              </a-space>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 映射详情查看模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="映射配置详情"
      width="800px"
      :footer="null"
    >
      <div v-if="selectedMappingDetail">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="图表ID">{{ selectedMappingDetail.chartId }}</a-descriptions-item>
          <a-descriptions-item label="图表类型">{{ selectedMappingDetail.chartType }}</a-descriptions-item>
          <a-descriptions-item label="图表名称">{{ selectedMappingDetail.chartName }}</a-descriptions-item>
          <a-descriptions-item label="通用模板">{{ selectedMappingDetail.universalTemplate }}</a-descriptions-item>
          <a-descriptions-item label="JOLT规范">{{ selectedMappingDetail.joltSpecFile }}</a-descriptions-item>
          <a-descriptions-item label="映射数量">{{ selectedMappingDetail.mappingCount }} 个</a-descriptions-item>
        </a-descriptions>

        <h4 style="margin: 16px 0 8px 0;">映射详情</h4>
        <a-table
          :data-source="selectedMappingDetail.mappingDetails"
          :columns="detailColumns"
          :pagination="false"
          size="small"
        />
      </div>
    </a-modal>

    <!-- 批量删除确认模态框 -->
    <a-modal
      v-model:open="batchDeleteModalVisible"
      title="批量删除确认"
      @ok="confirmBatchDelete"
      :confirm-loading="batchDeleting"
    >
      <p>确定要删除选中的 {{ selectedMappings.length }} 个映射配置吗？</p>
      <p style="color: #ff4d4f;">此操作不可撤销！</p>
    </a-modal>

    <!-- 配置指南模态框 -->
    <a-modal
      v-model:open="configGuideModalVisible"
      width="1200px"
      :footer="null"
      :mask="false"
      :keyboard="false"
      :destroy-on-close="false"
      :closable="true"
      :wrap-class-name="'guide-modal-wrap'"
      class="config-guide-modal"
      :style="{ top: guideModalPosition.top + 'px', left: guideModalPosition.left + 'px' }"
      @click="bringToFront('guide')"
    >
      <template #title>
        <div
          class="draggable-title"
          @mousedown="startDrag($event, 'guide')"
        >
          配置指南 - {{ selectedGuideRecord?.chartName || '未知图表' }}
        </div>
      </template>

      <div v-if="selectedGuideRecord" class="guide-content">
        <a-row :gutter="16">
          <!-- 卡片1 - 官方完整实例 -->
          <a-col :span="12">
            <a-card title="官方完整实例" class="guide-card">
              <template #extra>
                <a-tag color="blue">{{ selectedGuideRecord.chartType }}</a-tag>
              </template>
              <div class="guide-section">
                <h4>ECharts配置示例</h4>
                <div class="code-block">
                  <pre><code>{{ getOfficialExample(selectedGuideRecord) }}</code></pre>
                </div>
                <div class="structure-description">
                  <h5>结构说明</h5>
                  <ul>
                    <li v-for="desc in getStructureDescription(selectedGuideRecord)" :key="desc">
                      {{ desc }}
                    </li>
                  </ul>
                </div>
              </div>
            </a-card>
          </a-col>

          <!-- 卡片2 - 转换结构说明 -->
          <a-col :span="12">
            <a-card title="转换结构说明" class="guide-card">
              <template #extra>
                <a-tag color="orange">{{ selectedGuideRecord.universalTemplate }}</a-tag>
              </template>
              <div class="guide-section">
                <h4>JOLT转换涉及的数据结构</h4>
                <div class="transformation-info">
                  <h5>转换前数据结构</h5>
                  <div class="code-block">
                    <pre><code>{{ getTransformationBefore(selectedGuideRecord) }}</code></pre>
                  </div>

                  <h5>转换后数据结构</h5>
                  <div class="code-block">
                    <pre><code>{{ getTransformationAfter(selectedGuideRecord) }}</code></pre>
                  </div>

                  <h5>占位符变量列表</h5>
                  <div class="placeholder-list">
                    <a-tag
                      v-for="placeholder in getPlaceholderList(selectedGuideRecord)"
                      :key="placeholder.name"
                      :color="placeholder.color"
                      style="margin: 2px;"
                    >
                      {{ placeholder.name }} - {{ placeholder.description }}
                    </a-tag>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-modal>

  </div>
</template>

<script setup lang="ts">
import { placeholderMappingApi } from '@/api'
import ChartSelector from '@/components/ChartSelector.vue'
import {
    BulbOutlined,
    CheckCircleOutlined,
    DeleteOutlined,
    ExclamationCircleOutlined,
    ReloadOutlined,
    SaveOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'

// 响应式数据
const chartSelectorRef = ref()
const selectedChart = reactive({
  chartId: '',
  templateType: '',
  chartFile: '',
  chartName: '',
  joltSpecFile: ''
})

// 占位符和映射配置
const placeholders = ref<string[]>([])
const placeholdersLoading = ref(false)
const mappingConfigs = reactive<Record<string, any>>({})
const hasUnsavedChanges = ref(false)

// 可用字段
const availableFields = ref<any[]>([])
const groupedFields = ref<any[]>([])

// 映射配置列表
const mappingList = ref<any[]>([])
const listLoading = ref(false)
const selectedMappings = ref<string[]>([])

// 状态管理
const savingConfig = ref(false)
const generatingMappings = ref(false)
const batchDeleting = ref(false)

// 模态框状态
const mappingModalVisible = ref(false)
const detailModalVisible = ref(false)
const batchDeleteModalVisible = ref(false)
const configGuideModalVisible = ref(false)
const selectedMappingDetail = ref(null)
const selectedGuideRecord = ref(null)

// 拖拽状态
const mappingModalPosition = ref({ top: 50, left: 100 })
const guideModalPosition = ref({ top: 80, left: 200 })
const isDragging = ref(false)
const dragTarget = ref('')

// 分页配置 - 与数据管理页面保持一致
const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) =>
    `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
  onChange: (page: number, size: number) => {
    paginationConfig.current = page
    paginationConfig.pageSize = size
    // 这里可以添加分页数据加载逻辑，当前是静态数据所以不需要
  },
  onShowSizeChange: (current: number, size: number) => {
    paginationConfig.current = 1
    paginationConfig.pageSize = size
    // 这里可以添加分页数据加载逻辑，当前是静态数据所以不需要
  }
})

// 计算属性
const mappingProgress = computed(() => {
  if (placeholders.value.length === 0) return 0
  const mappedCount = placeholders.value.filter(p => isMapped(p)).length
  return Math.round((mappedCount / placeholders.value.length) * 100)
})

// 表格列定义 - 使用关联信息中的字段，优化列宽避免滚动条
const listColumns = [
  { title: '图表ID', dataIndex: 'chartId', key: 'chartId', width: 120 },
  { title: '图表类型', dataIndex: 'chartType', key: 'chartType', width: 100 },
  { title: '图表名称', dataIndex: 'chartName', key: 'chartName', width: 120 },
  { title: '通用JSON模板', dataIndex: 'universalTemplate', key: 'universalTemplate', width: 140 },
  { title: 'JOLT转换规范', dataIndex: 'joltSpecFile', key: 'joltSpecFile', width: 160 },
  { title: '占位符数量', dataIndex: 'placeholderCount', key: 'placeholderCount', width: 100, align: 'center' },
  { title: '操作', key: 'actions', width: 240, align: 'center' }
]

const detailColumns = [
  { title: '占位符', dataIndex: 'placeholder', key: 'placeholder' },
  { title: '映射字段', dataIndex: 'fieldName', key: 'fieldName' },
  { title: '数据类型', dataIndex: 'dataType', key: 'dataType' },
  { title: '聚合方式', dataIndex: 'aggregationType', key: 'aggregationType' }
]

// 方法
const onChartSelected = async (chartInfo: any) => {
  Object.assign(selectedChart, chartInfo)
  console.log('📊 [映射管理] 选择图表:', chartInfo)

  // 加载占位符和现有映射配置
  await Promise.all([
    loadPlaceholders(chartInfo.chartId),
    loadExistingMappings(chartInfo.chartId)
  ])
}

// 获取图表显示名称
const getChartDisplayName = (chartFile: string) => {
  if (!chartFile) return '未知图表'
  const fileName = chartFile.split('/').pop()?.replace('.json', '') || ''

  // 根据文件名返回中文名称
  const chartNameMappings: Record<string, string> = {
    'basic-line-chart': '基础折线图',
    'smooth-line-chart': '平滑折线图',
    'stacked-line-chart': '堆叠折线图',
    'basic-bar-chart': '基础柱状图',
    'stacked-bar-chart': '堆叠柱状图',
    'basic-pie-chart': '基础饼图',
    'doughnut-chart': '圆环图',
    'basic-radar-chart': '基础雷达图',
    'basic-gauge-chart': '基础仪表盘'
  }

  return chartNameMappings[fileName] || fileName.replace(/[-_]/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// 获取通用模板类型 - 根据图表类型（一级分类）确定
const getUniversalTemplate = (chartId: string) => {
  // 根据图表类型（templateType）确定通用模板分类
  const templateType = selectedChart.templateType

  if (!templateType) return '通用模板'

  // 四大模板分类
  const templateMappings: Record<string, string> = {
    '折线图': '折线图通用模板',
    '柱状图': '柱状图通用模板',
    '饼图': '饼图通用模板',
    '雷达图': '雷达图通用模板',
    '仪表盘': '仪表盘通用模板'
  }

  return templateMappings[templateType] || '通用模板'
}

// 打开映射配置模态框
const openMappingModal = () => {
  if (!selectedChart.chartId) {
    message.warning('请先选择图表')
    return
  }
  mappingModalVisible.value = true
}

const onChartCleared = () => {
  Object.assign(selectedChart, { chartId: '', templateType: '', chartFile: '', chartName: '', joltSpecFile: '' })
  placeholders.value = []
  Object.keys(mappingConfigs).forEach(key => delete mappingConfigs[key])
  previewResult.value = null
  hasUnsavedChanges.value = false
}

const loadAvailableFields = async () => {
  try {
    const result = await placeholderMappingApi.getAvailableFields()

    if (result && result.fields) {
      availableFields.value = result.fields

      // 按组分类字段
      const groups = availableFields.value.reduce((acc, field) => {
        const group = acc.find(g => g.name === field.group)
        if (group) {
          group.fields.push(field)
        } else {
          acc.push({ name: field.group, fields: [field] })
        }
        return acc
      }, [] as any[])

      groupedFields.value = groups
      console.log('✅ [映射管理] 加载可用字段成功:', availableFields.value.length)
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载可用字段失败:', error)
    message.error('加载可用字段失败')
  }
}

const loadPlaceholders = async (chartId: string) => {
  placeholdersLoading.value = true
  try {
    const result = await placeholderMappingApi.getPlaceholders(chartId)

    if (result && result.placeholders) {
      placeholders.value = result.placeholders

      // 初始化映射配置
      placeholders.value.forEach(placeholder => {
        if (!mappingConfigs[placeholder]) {
          mappingConfigs[placeholder] = {
            fieldName: '',
            dataType: 'string',
            aggregationType: 'none',
            filters: {}
          }
        }
      })

      console.log('✅ [映射管理] 加载占位符成功:', placeholders.value.length)
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载占位符失败:', error)
    message.error('加载占位符失败')
  } finally {
    placeholdersLoading.value = false
  }
}

const loadExistingMappings = async (chartId: string) => {
  try {
    const result = await placeholderMappingApi.getMappings(chartId)

    if (result && result.hasConfig && result.mappings) {
      Object.assign(mappingConfigs, result.mappings)
      console.log('✅ [映射管理] 加载现有映射配置成功')
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载现有映射配置失败:', error)
  }
}

const isMapped = (placeholder: string) => {
  const config = mappingConfigs[placeholder]
  return config && config.fieldName
}

const updateMappingField = (placeholder: string, field: string, value: any) => {
  if (!mappingConfigs[placeholder]) {
    mappingConfigs[placeholder] = {
      fieldName: '',
      dataType: 'string',
      aggregationType: 'none',
      filters: {}
    }
  }

  mappingConfigs[placeholder][field] = value
  hasUnsavedChanges.value = true
  console.log('🔄 [映射管理] 映射配置变更:', placeholder, field, value)
}

const onMappingChange = (placeholder: string) => {
  hasUnsavedChanges.value = true
  console.log('🔄 [映射管理] 映射配置变更:', placeholder, mappingConfigs[placeholder])
}

const clearMapping = (placeholder: string) => {
  if (mappingConfigs[placeholder]) {
    mappingConfigs[placeholder] = {
      fieldName: '',
      dataType: 'string',
      aggregationType: 'none',
      filters: {}
    }
    hasUnsavedChanges.value = true
  }
}

const saveMappingConfig = async () => {
  if (!selectedChart.chartId) {
    message.warning('请先选择图表')
    return
  }

  savingConfig.value = true
  try {
    // 过滤出已配置的映射
    const validMappings = Object.fromEntries(
      Object.entries(mappingConfigs).filter(([_, config]: [string, any]) => config.fieldName)
    )

    await placeholderMappingApi.configureMappings(selectedChart.chartId, validMappings)
    hasUnsavedChanges.value = false
    message.success('映射配置保存成功')

    // 刷新映射列表
    await refreshMappingList()
  } catch (error) {
    console.error('❌ [映射管理] 保存映射配置失败:', error)
    message.error('保存映射配置失败')
  } finally {
    savingConfig.value = false
  }
}

const generateSmartMappings = async () => {
  if (!selectedChart.chartId || placeholders.value.length === 0) {
    message.warning('请先选择图表并加载占位符')
    return
  }

  generatingMappings.value = true
  try {
    const result = await placeholderMappingApi.generateDefaultMappings(
      selectedChart.chartId,
      placeholders.value
    )

    if (result && result.mappings) {
      Object.assign(mappingConfigs, result.mappings)
      hasUnsavedChanges.value = true
      message.success('智能推荐完成')
    }
  } catch (error) {
    console.error('❌ [映射管理] 智能推荐失败:', error)
    message.error('智能推荐失败')
  } finally {
    generatingMappings.value = false
  }
}

// 为单个占位符生成智能推荐
const generateSmartMapping = (placeholder: string) => {
  // 基于占位符名称的智能推荐逻辑
  const lowerName = placeholder.toLowerCase()

  let recommendedMapping = {
    fieldName: '',
    dataType: 'string',
    aggregationType: 'none',
    filters: {}
  }

  if (lowerName.includes('title')) {
    recommendedMapping = { fieldName: 'category', dataType: 'string', aggregationType: 'none', filters: {} }
  } else if (lowerName.includes('categories') || lowerName.includes('category')) {
    recommendedMapping = { fieldName: 'month', dataType: 'array', aggregationType: 'list', filters: {} }
  } else if (lowerName.includes('data') || lowerName.includes('value')) {
    recommendedMapping = { fieldName: 'amount', dataType: 'array', aggregationType: 'list', filters: {} }
  } else if (lowerName.includes('name')) {
    recommendedMapping = { fieldName: 'product', dataType: 'string', aggregationType: 'none', filters: {} }
  } else if (lowerName.includes('series')) {
    if (lowerName.includes('data')) {
      recommendedMapping = { fieldName: 'amount', dataType: 'array', aggregationType: 'list', filters: {} }
    } else {
      recommendedMapping = { fieldName: 'product', dataType: 'string', aggregationType: 'none', filters: {} }
    }
  }

  if (recommendedMapping.fieldName) {
    mappingConfigs[placeholder] = recommendedMapping
    hasUnsavedChanges.value = true
    message.success(`已为 ${placeholder} 推荐映射到 ${recommendedMapping.fieldName}`)
  } else {
    message.info(`无法为 ${placeholder} 生成推荐，请手动配置`)
  }
}



const refreshMappingList = async () => {
  listLoading.value = true
  try {
    console.log('🔄 [映射管理] 开始刷新映射列表...')
    const result = await placeholderMappingApi.getAllMappings()
    console.log('📊 [映射管理] API响应数据:', result)

    if (result && result.mappings) {
      mappingList.value = result.mappings
      paginationConfig.total = result.totalCount || result.mappings.length
      console.log('✅ [映射管理] 刷新映射列表成功，共 ' + result.totalCount + ' 条记录')
      console.log('📋 [映射管理] 映射列表数据详情:', result.mappings)

      // 检查每条记录的字段
      result.mappings.forEach((item: any, index: number) => {
        console.log(`📝 [映射管理] 记录${index + 1}:`, {
          chartId: item.chartId,
          chartType: item.chartType,
          chartName: item.chartName,
          universalTemplate: item.universalTemplate,
          joltSpecFile: item.joltSpecFile,
          placeholderCount: item.placeholderCount,
          mappingCount: item.mappingCount
        })
      })
    } else {
      console.warn('⚠️ [映射管理] API响应数据格式异常:', result)
    }
  } catch (error) {
    console.error('❌ [映射管理] 刷新映射列表失败:', error)
    message.error('刷新映射列表失败')
  } finally {
    listLoading.value = false
  }
}

const onSelectionChange = (selectedRowKeys: string[]) => {
  selectedMappings.value = selectedRowKeys
}

const viewMappingDetail = (record: any) => {
  selectedMappingDetail.value = record
  detailModalVisible.value = true
}



const copyMapping = async (record: any) => {
  // 这里可以实现复制到剪贴板或复制到其他图表的功能
  message.info('复制功能开发中...')
}

// 配置映射（从列表操作）
const configureMapping = (record: any) => {
  console.log('🔧 [映射管理] 打开配置映射模态框:', record.chartId)

  // 设置选中的图表信息
  Object.assign(selectedChart, {
    chartId: record.chartId,
    templateType: record.chartType,
    chartFile: '', // 这里可能需要从record中获取
    chartName: record.chartName,
    joltSpecFile: record.joltSpecFile
  })

  // 加载占位符和现有映射配置
  Promise.all([
    loadPlaceholders(record.chartId),
    loadExistingMappings(record.chartId)
  ]).then(() => {
    mappingModalVisible.value = true
    console.log('✅ [映射管理] 配置映射模态框已打开')
  })
}



// 显示配置指南
const showConfigGuide = (record: any) => {
  console.log('📖 [映射管理] 打开配置指南模态框:', record.chartId)
  selectedGuideRecord.value = record
  configGuideModalVisible.value = true
  console.log('✅ [映射管理] 配置指南模态框已打开')
}

// 拖拽功能实现
const startDrag = (event: MouseEvent, modalType: string) => {
  event.preventDefault()
  event.stopPropagation()

  isDragging.value = true
  dragTarget.value = modalType

  console.log('🖱️ [拖拽] 开始拖拽:', modalType)

  const startX = event.clientX
  const startY = event.clientY

  const currentPosition = modalType === 'mapping' ? mappingModalPosition.value : guideModalPosition.value
  const startTop = currentPosition.top
  const startLeft = currentPosition.left

  console.log('📍 [拖拽] 初始位置:', { startTop, startLeft, startX, startY })

  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging.value) return

    const deltaX = e.clientX - startX
    const deltaY = e.clientY - startY

    // 获取窗口尺寸
    const windowWidth = window.innerWidth
    const windowHeight = window.innerHeight

    // 模态框尺寸（更保守的估算）
    const modalWidth = modalType === 'mapping' ? 1000 : 1200
    const modalHeight = 500

    // 计算新位置，确保不超出边界
    const newTop = Math.max(0, Math.min(windowHeight - modalHeight, startTop + deltaY))
    const newLeft = Math.max(0, Math.min(windowWidth - modalWidth, startLeft + deltaX))

    const newPosition = {
      top: newTop,
      left: newLeft
    }

    // 更新位置
    if (modalType === 'mapping') {
      mappingModalPosition.value = newPosition
    } else {
      guideModalPosition.value = newPosition
    }

    // 调试信息
    if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
      console.log('🔄 [拖拽] 位置更新:', {
        modalType,
        delta: { deltaX, deltaY },
        newPosition,
        window: { windowWidth, windowHeight }
      })
    }
  }

  const handleMouseUp = () => {
    console.log('🛑 [拖拽] 结束拖拽:', modalType)
    isDragging.value = false
    dragTarget.value = ''
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)

    // 确保最终位置正确应用
    const finalPosition = modalType === 'mapping' ? mappingModalPosition.value : guideModalPosition.value
    console.log('📍 [拖拽] 最终位置:', finalPosition)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 模态框点击置顶
const bringToFront = (modalType: string) => {
  const mappingModal = document.querySelector('.mapping-config-modal .ant-modal')
  const guideModal = document.querySelector('.config-guide-modal .ant-modal')

  if (modalType === 'mapping' && mappingModal) {
    (mappingModal as HTMLElement).style.zIndex = '1002'
    if (guideModal) (guideModal as HTMLElement).style.zIndex = '1001'
  } else if (modalType === 'guide' && guideModal) {
    (guideModal as HTMLElement).style.zIndex = '1002'
    if (mappingModal) (mappingModal as HTMLElement).style.zIndex = '1001'
  }
}

const deleteMapping = async (record: any) => {
  try {
    await placeholderMappingApi.deleteMappings(record.chartId)
    message.success('删除成功')
    await refreshMappingList()
  } catch (error) {
    console.error('❌ [映射管理] 删除失败:', error)
    message.error('删除失败')
  }
}

const showBatchDeleteModal = () => {
  batchDeleteModalVisible.value = true
}

const confirmBatchDelete = async () => {
  batchDeleting.value = true
  try {
    await placeholderMappingApi.batchDeleteMappings(selectedMappings.value)
    message.success(`成功删除 ${selectedMappings.value.length} 个配置`)
    selectedMappings.value = []
    batchDeleteModalVisible.value = false
    await refreshMappingList()
  } catch (error) {
    console.error('❌ [映射管理] 批量删除失败:', error)
    message.error('批量删除失败')
  } finally {
    batchDeleting.value = false
  }
}

// 配置指南内容生成方法
const getOfficialExample = (record: any) => {
  const examples = {
    '折线图': `{
  "title": {
    "text": "\${chart_title}"
  },
  "tooltip": {
    "trigger": "axis"
  },
  "legend": {
    "data": ["\${series_1_name}"]
  },
  "xAxis": {
    "type": "category",
    "data": \${categories}
  },
  "yAxis": {
    "type": "value"
  },
  "series": [{
    "name": "\${series_1_name}",
    "type": "line",
    "data": \${series_1_data}
  }]
}`,
    '柱状图': `{
  "title": {
    "text": "\${chart_title}"
  },
  "tooltip": {
    "trigger": "axis"
  },
  "xAxis": {
    "type": "category",
    "data": \${categories}
  },
  "yAxis": {
    "type": "value"
  },
  "series": [{
    "name": "\${series_1_name}",
    "type": "bar",
    "data": \${series_1_data}
  }]
}`,
    '饼图': `{
  "title": {
    "text": "\${chart_title}",
    "left": "center"
  },
  "tooltip": {
    "trigger": "item"
  },
  "legend": {
    "orient": "vertical",
    "left": "left"
  },
  "series": [{
    "name": "\${chart_title}",
    "type": "pie",
    "radius": "50%",
    "data": \${pie_data},
    "emphasis": {
      "itemStyle": {
        "shadowBlur": 10,
        "shadowOffsetX": 0,
        "shadowColor": "rgba(0, 0, 0, 0.5)"
      }
    }
  }]
}`,
    '雷达图': `{
  "title": {
    "text": "\${chart_title}"
  },
  "tooltip": {},
  "legend": {
    "data": ["数据系列"]
  },
  "radar": {
    "indicator": \${radar_indicators}
  },
  "series": [{
    "name": "数据对比",
    "type": "radar",
    "data": \${radar_data}
  }]
}`
  }

  return examples[record.chartType] || '暂无示例'
}

const getStructureDescription = (record: any) => {
  const descriptions = {
    '折线图': [
      'title: 图表标题配置，支持文本和样式设置',
      'xAxis: X轴配置，通常为类目轴，显示分类数据',
      'yAxis: Y轴配置，通常为数值轴，显示数值范围',
      'series: 数据系列配置，包含数据点和样式',
      'tooltip: 提示框配置，鼠标悬停时显示详细信息'
    ],
    '柱状图': [
      'title: 图表标题配置',
      'xAxis: X轴类目配置，显示分类标签',
      'yAxis: Y轴数值配置，显示数值刻度',
      'series: 柱状数据系列，type设置为"bar"',
      'tooltip: 交互提示配置'
    ],
    '饼图': [
      'title: 图表标题，通常居中显示',
      'series: 饼图数据系列，type设置为"pie"',
      'radius: 饼图半径设置，支持百分比或像素值',
      'data: 饼图数据，包含name和value字段',
      'legend: 图例配置，显示数据项说明'
    ],
    '雷达图': [
      'radar: 雷达图坐标系配置',
      'indicator: 雷达图指标配置，定义各个维度',
      'series: 雷达图数据系列，type设置为"radar"',
      'data: 雷达图数据，多维度数值数组'
    ]
  }

  return descriptions[record.chartType] || ['暂无描述']
}

const getTransformationBefore = (record: any) => {
  return `{
  "virtualDatabase": {
    "chartData": [
      {
        "id": 1,
        "year": "2024",
        "month": "1月",
        "category": "销售数据",
        "channel": "线上",
        "product": "产品A",
        "amount": 12500.50,
        "quantity": 100
      }
    ]
  }
}`
}

const getTransformationAfter = (record: any) => {
  const transformations = {
    '折线图': `{
  "title": { "text": "销售数据" },
  "xAxis": { "data": ["1月", "2月", "3月"] },
  "series": [{
    "name": "产品A",
    "data": [12500.50, 13200.00, 11800.00]
  }]
}`,
    '柱状图': `{
  "title": { "text": "销售数据" },
  "xAxis": { "data": ["1月", "2月", "3月"] },
  "series": [{
    "name": "产品A",
    "type": "bar",
    "data": [12500.50, 13200.00, 11800.00]
  }]
}`,
    '饼图': `{
  "title": { "text": "销售数据" },
  "series": [{
    "type": "pie",
    "data": [
      { "name": "产品A", "value": 12500.50 },
      { "name": "产品B", "value": 13200.00 }
    ]
  }]
}`,
    '雷达图': `{
  "title": { "text": "销售数据" },
  "radar": {
    "indicator": [
      { "name": "销量", "max": 100 },
      { "name": "利润", "max": 100 }
    ]
  },
  "series": [{
    "type": "radar",
    "data": [{ "value": [80, 90] }]
  }]
}`
  }

  return transformations[record.chartType] || '暂无转换示例'
}

const getPlaceholderList = (record: any) => {
  const placeholders = {
    '折线图': [
      { name: '${chart_title}', description: '图表标题', color: 'blue' },
      { name: '${categories}', description: 'X轴分类数据', color: 'green' },
      { name: '${series_1_name}', description: '系列名称', color: 'orange' },
      { name: '${series_1_data}', description: '系列数据', color: 'purple' }
    ],
    '柱状图': [
      { name: '${chart_title}', description: '图表标题', color: 'blue' },
      { name: '${categories}', description: 'X轴分类数据', color: 'green' },
      { name: '${series_1_name}', description: '系列名称', color: 'orange' },
      { name: '${series_1_data}', description: '系列数据', color: 'purple' }
    ],
    '饼图': [
      { name: '${chart_title}', description: '图表标题', color: 'blue' },
      { name: '${pie_data}', description: '饼图数据', color: 'red' }
    ],
    '雷达图': [
      { name: '${chart_title}', description: '图表标题', color: 'blue' },
      { name: '${radar_indicators}', description: '雷达图指标', color: 'cyan' },
      { name: '${radar_data}', description: '雷达图数据', color: 'magenta' }
    ]
  }

  return placeholders[record.chartType] || []
}

// 监听模态框状态变化
watch(mappingModalVisible, (newVal) => {
  console.log('👁️ [模态框] 配置映射模态框状态变化:', newVal)
})

watch(configGuideModalVisible, (newVal) => {
  console.log('👁️ [模态框] 配置指南模态框状态变化:', newVal)
})

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadAvailableFields(),
    refreshMappingList()
  ])

  console.log('🚀 [映射管理] 页面初始化完成')
  console.log('📊 [映射管理] 初始模态框位置:', {
    mapping: mappingModalPosition.value,
    guide: guideModalPosition.value
  })
})
</script>

<style scoped>
.mapping-container {
  padding: 16px;
  background: none;
  min-height: auto;
  position: static;
  z-index: auto;
}

/* 上下布局样式 - 完全透明平铺模式 */
.top-section {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.bottom-section {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 图表选择器在上下布局中的样式调整 */
.top-section :deep(.ant-card) {
  box-shadow: none;
  border: 1px solid #f0f0f0;
}

.mapping-list-card {
  border-radius: 8px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 模态框内的映射配置样式 */
.modal-mapping-content {
  height: 100%;
  overflow: hidden;
}

.mapping-header {
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.mapping-header h4 {
  margin: 0 0 8px 0;
  color: #262626;
}

.mapping-list-container {
  height: calc(60vh - 120px);
  overflow: hidden;
}

.mapping-table-header {
  background: #fafafa;
  padding: 8px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px 6px 0 0;
  margin-bottom: 0;
}

.mapping-list {
  max-height: calc(60vh - 180px);
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-top: none;
  border-radius: 0 0 6px 6px;
  padding: 8px;
}

.mapping-item {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  transition: all 0.2s;
}

.mapping-item:hover {
  border-color: #1890ff;
  background: #f6ffed;
}

.placeholder-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mapping-status {
  font-size: 16px;
  margin-left: 8px;
}

.mapping-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

/* 确保下拉框有足够的宽度 */
:deep(.ant-select) {
  min-width: 120px;
}

:deep(.ant-select-selector) {
  min-height: 28px;
}

/* 字段选择下拉框特殊样式 */
.mapping-item .ant-col:nth-child(2) :deep(.ant-select) {
  min-width: 200px;
}

.preview-content {
  max-height: 400px;
  overflow: auto;
}

.preview-content pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.mapping-list-content {
  padding: 16px;
}

/* 表格样式优化 */
:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f6ffed;
}

/* 标签样式优化 */
:deep(.ant-tag) {
  margin: 2px;
  border-radius: 4px;
}

/* 描述列表样式 */
:deep(.ant-descriptions-item-label) {
  font-weight: 600;
  color: #262626;
}

/* 模态框操作按钮区域 */
.modal-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

/* 模态框拖拽样式 */
.mapping-config-modal {
  z-index: 1001;
}

.config-guide-modal {
  z-index: 1001;
}

/* 模态框定位样式 - 支持拖拽 */
.mapping-config-modal :deep(.ant-modal),
.config-guide-modal :deep(.ant-modal) {
  max-width: 90vw;
  position: fixed !important;
  margin: 0 !important;
  transform: none !important;
  top: auto !important;
  left: auto !important;
}

/* 模态框包装器样式 */
.mapping-modal-wrap :deep(.ant-modal-wrap),
.guide-modal-wrap :deep(.ant-modal-wrap) {
  position: static !important;
  overflow: visible !important;
}

/* 隐藏遮罩层 */
.mapping-config-modal :deep(.ant-modal-mask),
.config-guide-modal :deep(.ant-modal-mask),
.mapping-modal-wrap :deep(.ant-modal-mask),
.guide-modal-wrap :deep(.ant-modal-mask) {
  display: none !important;
}

/* 确保模态框可以正确定位 */
.mapping-config-modal,
.config-guide-modal {
  position: relative;
}

.mapping-config-modal :deep(.ant-modal-content),
.config-guide-modal :deep(.ant-modal-content) {
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
}

.draggable-title {
  cursor: move;
  user-select: none;
  padding: 4px 0;
  font-weight: 600;
  color: #262626;
}

.draggable-title:hover {
  color: #1890ff;
}

.draggable-title:active {
  cursor: grabbing;
}

.guide-content {
  max-height: 70vh;
  overflow-y: auto;
}

.guide-card {
  height: 100%;
  border-radius: 8px;
}

.guide-section {
  padding: 8px 0;
}

.guide-section h4 {
  margin: 0 0 12px 0;
  color: #1890ff;
  font-size: 14px;
  font-weight: 600;
}

.guide-section h5 {
  margin: 16px 0 8px 0;
  color: #262626;
  font-size: 13px;
  font-weight: 600;
}

.code-block {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  margin: 8px 0;
  overflow-x: auto;
}

.code-block pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #333;
}

.structure-description ul {
  margin: 8px 0;
  padding-left: 20px;
}

.structure-description li {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
}

.transformation-info {
  padding: 8px 0;
}

.placeholder-list {
  margin: 8px 0;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
}

/* 确保配置指南模态框可以独立拖拽 */
.config-guide-modal :deep(.ant-modal-content) {
  position: relative;
  cursor: move;
}

.config-guide-modal :deep(.ant-modal-header) {
  cursor: move;
  user-select: none;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .mapping-container {
    padding: 16px;
  }

  .top-section {
    padding: 16px;
  }

  .modal-mapping-content {
    max-height: 400px;
  }

  .config-guide-modal :deep(.ant-modal) {
    width: 95vw !important;
    max-width: none;
  }

  .guide-content {
    max-height: 60vh;
  }

  /* 小屏幕下表格列宽调整 */
  :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 12px;
  }

  :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  .top-section {
    padding: 12px;
    margin-bottom: 16px;
  }

  .config-guide-modal :deep(.ant-modal) {
    width: 98vw !important;
    margin: 10px auto;
  }

  .guide-content {
    max-height: 50vh;
  }

  .code-block {
    font-size: 10px;
  }

  /* 移动端表格优化 */
  :deep(.ant-table) {
    font-size: 11px;
  }

  :deep(.ant-btn) {
    padding: 2px 6px;
    font-size: 11px;
  }
}
</style>