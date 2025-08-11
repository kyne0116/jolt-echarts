<template>
  <div class="dynamic-mapping-container">
    <div class="page-header">
      <h2>🔗 动态占位符映射配置</h2>
      <p class="page-description">可视化配置占位符与字段的映射关系，支持实时预览和模板管理</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：图表选择和模板管理 -->
      <a-col :span="8">
        <a-card title="图表选择" class="chart-selection-card" style="margin-bottom: 16px;">
          <a-form layout="vertical">
            <a-form-item label="选择图表类型">
              <a-select 
                v-model:value="selectedChartId" 
                placeholder="请选择图表类型"
                @change="onChartChange"
                :loading="chartLoading"
              >
                <a-select-option 
                  v-for="chart in chartTypes" 
                  :key="chart.id" 
                  :value="chart.id"
                >
                  {{ chart.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>

        <a-card title="映射模板" class="template-card">
          <template #extra>
            <a-space>
              <a-button size="small" @click="showSaveTemplateModal">
                <SaveOutlined />
                保存模板
              </a-button>
              <a-button size="small" @click="refreshTemplates">
                <ReloadOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <a-list 
            :data-source="mappingTemplates" 
            size="small"
            :loading="templateLoading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <template #actions>
                  <a-button 
                    type="link" 
                    size="small" 
                    @click="applyTemplate(item.templateId)"
                    :loading="applyingTemplate === item.templateId"
                  >
                    应用
                  </a-button>
                  <a-button 
                    type="link" 
                    size="small" 
                    danger 
                    @click="deleteTemplate(item.templateId)"
                  >
                    删除
                  </a-button>
                </template>
                <a-list-item-meta>
                  <template #title>
                    <span>{{ item.templateName }}</span>
                    <a-tag v-if="item.systemTemplate" color="blue" size="small" style="margin-left: 8px;">
                      系统
                    </a-tag>
                  </template>
                  <template #description>
                    <div>{{ item.description }}</div>
                    <div style="margin-top: 4px;">
                      <a-tag size="small">{{ item.mappingCount }} 个映射</a-tag>
                      <a-tag size="small" color="green">使用 {{ item.usageCount }} 次</a-tag>
                    </div>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 中间：映射配置区域 -->
      <a-col :span="10">
        <a-card title="映射配置" class="mapping-config-card">
          <template #extra>
            <a-space>
              <a-button type="primary" @click="saveMapping" :loading="savingMapping">
                <SaveOutlined />
                保存配置
              </a-button>
              <a-button @click="previewMapping" :loading="previewLoading">
                <EyeOutlined />
                预览效果
              </a-button>
              <a-button @click="resetMapping">
                <UndoOutlined />
                重置
              </a-button>
            </a-space>
          </template>

          <div v-if="!selectedChartId" class="empty-state">
            <a-empty description="请先选择图表类型" />
          </div>

          <div v-else class="mapping-content">
            <!-- 占位符列表 -->
            <div class="placeholder-section">
              <h3>占位符映射</h3>
              <a-table
                :columns="mappingColumns"
                :data-source="placeholderMappings"
                :pagination="false"
                size="small"
                row-key="placeholder"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'fieldMapping'">
                    <a-select
                      v-model:value="record.fieldName"
                      placeholder="选择字段"
                      style="width: 100%"
                      @change="updateMapping(record.placeholder, $event)"
                      :loading="fieldLoading"
                      show-search
                      :filter-option="filterFieldOption"
                    >
                      <a-select-opt-group 
                        v-for="(fields, group) in groupedFields" 
                        :key="group" 
                        :label="group"
                      >
                        <a-select-option 
                          v-for="field in fields" 
                          :key="field.fieldName" 
                          :value="field.fieldName"
                        >
                          <span>{{ field.displayName }}</span>
                          <a-tag 
                            :color="getFieldTypeColor(field.dataType)" 
                            size="small" 
                            style="margin-left: 8px;"
                          >
                            {{ field.dataType?.displayName || field.dataType }}
                          </a-tag>
                        </a-select-option>
                      </a-select-opt-group>
                    </a-select>
                  </template>
                  <template v-else-if="column.key === 'preview'">
                    <a-button 
                      type="link" 
                      size="small" 
                      @click="previewSingleMapping(record)"
                      :disabled="!record.fieldName"
                    >
                      预览
                    </a-button>
                  </template>
                </template>
              </a-table>
            </div>

            <!-- 拖拽映射区域 -->
            <a-divider>拖拽映射</a-divider>
            <div class="drag-mapping-section">
              <a-row :gutter="16">
                <a-col :span="12">
                  <div class="drag-source">
                    <h4>可用字段</h4>
                    <div class="field-list">
                      <div
                        v-for="field in availableFields"
                        :key="field.fieldName"
                        class="field-item"
                        draggable="true"
                        @dragstart="onDragStart($event, field)"
                      >
                        <span>{{ field.displayName }}</span>
                        <a-tag 
                          :color="getFieldTypeColor(field.dataType)" 
                          size="small"
                        >
                          {{ field.dataType?.displayName || field.dataType }}
                        </a-tag>
                      </div>
                    </div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="drag-target">
                    <h4>占位符区域</h4>
                    <div class="placeholder-list">
                      <div
                        v-for="placeholder in placeholderList"
                        :key="placeholder"
                        class="placeholder-item"
                        :class="{ 'has-mapping': getMappingForPlaceholder(placeholder) }"
                        @dragover.prevent
                        @drop="onDrop($event, placeholder)"
                      >
                        <div class="placeholder-name">{{ placeholder }}</div>
                        <div class="mapping-info">
                          <span v-if="getMappingForPlaceholder(placeholder)">
                            → {{ getFieldDisplayName(getMappingForPlaceholder(placeholder)) }}
                          </span>
                          <span v-else class="no-mapping">拖拽字段到此处</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </a-col>
              </a-row>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：实时预览 -->
      <a-col :span="6">
        <a-card title="实时预览" class="preview-card">
          <template #extra>
            <a-button size="small" @click="refreshPreview">
              <ReloadOutlined />
              刷新
            </a-button>
          </template>

          <div v-if="!selectedChartId" class="empty-state">
            <a-empty description="请先选择图表类型" />
          </div>

          <div v-else-if="previewLoading" class="loading-state">
            <a-spin size="large" />
            <p>生成预览中...</p>
          </div>

          <div v-else class="preview-content">
            <!-- 映射状态概览 -->
            <div class="mapping-status">
              <a-statistic 
                title="映射完成度" 
                :value="mappingCompletionRate" 
                suffix="%" 
                :value-style="{ color: mappingCompletionRate === 100 ? '#3f8600' : '#cf1322' }"
              />
              <a-progress 
                :percent="mappingCompletionRate" 
                :stroke-color="mappingCompletionRate === 100 ? '#52c41a' : '#ff4d4f'"
                style="margin-top: 8px;"
              />
            </div>

            <a-divider />

            <!-- 预览数据 -->
            <div class="preview-data">
              <h4>预览数据</h4>
              <a-table
                :columns="previewColumns"
                :data-source="previewData"
                :pagination="false"
                size="small"
                :scroll="{ y: 200 }"
              />
            </div>

            <a-divider />

            <!-- 图表预览 -->
            <div class="chart-preview">
              <h4>图表预览</h4>
              <div ref="previewChart" class="preview-chart-container"></div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 保存模板模态框 -->
    <a-modal
      v-model:open="saveTemplateModalVisible"
      title="保存映射模板"
      @ok="handleSaveTemplate"
      @cancel="cancelSaveTemplate"
      :confirm-loading="saveTemplateLoading"
    >
      <a-form
        ref="saveTemplateFormRef"
        :model="saveTemplateForm"
        :rules="saveTemplateRules"
        layout="vertical"
      >
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="saveTemplateForm.templateName" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="模板描述" name="description">
          <a-textarea v-model:value="saveTemplateForm.description" placeholder="请输入模板描述" :rows="3" />
        </a-form-item>
        <a-form-item label="模板分类" name="category">
          <a-input v-model:value="saveTemplateForm.category" placeholder="请输入模板分类" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览详情模态框 -->
    <a-modal
      v-model:open="previewDetailModalVisible"
      title="映射预览详情"
      :footer="null"
      width="800px"
    >
      <div class="preview-detail-content">
        <a-descriptions title="映射信息" bordered>
          <a-descriptions-item label="图表类型">{{ selectedChartId }}</a-descriptions-item>
          <a-descriptions-item label="映射数量">{{ Object.keys(currentMappingConfig).length }}</a-descriptions-item>
          <a-descriptions-item label="完成度">{{ mappingCompletionRate }}%</a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <h3>详细映射关系</h3>
        <a-table
          :columns="detailMappingColumns"
          :data-source="detailMappingData"
          :pagination="false"
          size="small"
        />

        <a-divider />

        <h3>预览图表</h3>
        <div ref="detailPreviewChart" style="height: 400px;"></div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    EyeOutlined,
    ReloadOutlined,
    SaveOutlined,
    UndoOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'

// ==================== 响应式数据 ====================

// 图表选择相关
const selectedChartId = ref('')
const chartTypes = ref([])
const chartLoading = ref(false)

// 映射配置相关
const placeholderMappings = ref([])
const currentMappingConfig = ref({})
const savingMapping = ref(false)

// 字段数据相关
const availableFields = ref([])
const groupedFields = ref({})
const fieldLoading = ref(false)

// 模板管理相关
const mappingTemplates = ref([])
const templateLoading = ref(false)
const applyingTemplate = ref('')

// 预览相关
const previewLoading = ref(false)
const previewData = ref([])
const previewColumns = ref([])
const previewChart = ref(null)

// 模态框相关
const saveTemplateModalVisible = ref(false)
const saveTemplateLoading = ref(false)
const saveTemplateForm = reactive({
  templateName: '',
  description: '',
  category: ''
})

const previewDetailModalVisible = ref(false)
const detailMappingData = ref([])

// 拖拽相关
const draggedField = ref(null)

// ==================== 计算属性 ====================

// 占位符列表
const placeholderList = computed(() => {
  return placeholderMappings.value.map(item => item.placeholder)
})

// 映射完成度
const mappingCompletionRate = computed(() => {
  const total = placeholderMappings.value.length
  const completed = placeholderMappings.value.filter(item => item.fieldName).length
  return total > 0 ? Math.round((completed / total) * 100) : 0
})

// ==================== 表格列定义 ====================

// 映射配置表格列
const mappingColumns = [
  {
    title: '占位符',
    dataIndex: 'placeholder',
    key: 'placeholder',
    width: 180
  },
  {
    title: '映射字段',
    key: 'fieldMapping',
    width: 200
  },
  {
    title: '操作',
    key: 'preview',
    width: 80
  }
]

// 详细映射表格列
const detailMappingColumns = [
  {
    title: '占位符',
    dataIndex: 'placeholder',
    key: 'placeholder'
  },
  {
    title: '字段名',
    dataIndex: 'fieldName',
    key: 'fieldName'
  },
  {
    title: '字段类型',
    dataIndex: 'fieldType',
    key: 'fieldType'
  },
  {
    title: '预览值',
    dataIndex: 'previewValue',
    key: 'previewValue'
  }
]

// 表单验证规则
const saveTemplateRules = {
  templateName: [
    { required: true, message: '请输入模板名称' }
  ]
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadChartTypes()
  loadAvailableFields()
  loadMappingTemplates()
})

// 监听图表选择变化
watch(selectedChartId, (newChartId) => {
  if (newChartId) {
    loadChartMappingConfig(newChartId)
  }
})

// ==================== 方法定义 ====================

// 加载图表类型
const loadChartTypes = async () => {
  chartLoading.value = true
  try {
    const response = await fetch('/api/chart/configurable/chart-types')
    const result = await response.json()

    if (result.success) {
      const pathToChartId = result.data.pathToChartId
      chartTypes.value = Object.entries(pathToChartId).map(([path, id]) => ({
        id: id as string,
        name: path.replace('.json', '').replace('/', ' - ')
      }))

      console.log('✅ [映射配置] 加载图表类型成功:', chartTypes.value.length)
    } else {
      message.error('加载图表类型失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 加载图表类型失败:', error)
    message.error('加载图表类型失败')
  } finally {
    chartLoading.value = false
  }
}

// 加载可用字段
const loadAvailableFields = async () => {
  fieldLoading.value = true
  try {
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
          isBaseField: true
        }))
      )

      const dynamicFields = Object.values(result.data.dynamicFields).map((field: any) => ({
        ...field,
        isBaseField: false
      }))

      availableFields.value = [...baseFields, ...dynamicFields]

      // 按分组整理字段
      groupedFields.value = availableFields.value.reduce((groups, field) => {
        const group = field.fieldGroup || '未分组'
        if (!groups[group]) {
          groups[group] = []
        }
        groups[group].push(field)
        return groups
      }, {})

      console.log('✅ [映射配置] 加载可用字段成功:', availableFields.value.length)
    } else {
      message.error('加载可用字段失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 加载可用字段失败:', error)
    message.error('加载可用字段失败')
  } finally {
    fieldLoading.value = false
  }
}

// 加载映射模板
const loadMappingTemplates = async () => {
  templateLoading.value = true
  try {
    const response = await fetch('/api/chart/dynamic-mapping/templates')
    const result = await response.json()

    if (result.success && result.data && result.data.templates) {
      mappingTemplates.value = Object.values(result.data.templates).map((template: any) => ({
        templateId: template.id,
        templateName: template.name,
        description: template.description,
        category: template.category,
        mappingCount: template.rules ? template.rules.length : 0,
        usageCount: Math.floor(Math.random() * 50), // 模拟使用次数
        systemTemplate: true,
        rules: template.rules || []
      }))
      console.log('✅ [映射配置] 加载映射模板成功:', mappingTemplates.value.length)
    } else {
      message.error('加载映射模板失败: ' + (result.message || '数据格式错误'))
    }
  } catch (error) {
    console.error('❌ [映射配置] 加载映射模板失败:', error)
    message.error('加载映射模板失败: ' + (error.message || '网络错误'))
  } finally {
    templateLoading.value = false
  }
}

// 图表变化事件
const onChartChange = (chartId: string) => {
  console.log('📊 [映射配置] 选择图表:', chartId)
  loadChartMappingConfig(chartId)
}

// 加载图表映射配置
const loadChartMappingConfig = async (chartId: string) => {
  try {
    const response = await fetch(`/api/chart/dynamic-mapping/config/${chartId}`)
    const result = await response.json()

    if (result.success) {
      if (result.data.hasConfig) {
        // 已有配置
        currentMappingConfig.value = result.data.mappings || {}
        console.log('✅ [映射配置] 加载已有配置:', Object.keys(currentMappingConfig.value).length)
      } else {
        // 无配置，生成默认占位符
        currentMappingConfig.value = {}
        console.log('⚠️ [映射配置] 图表无配置，生成默认占位符')
      }

      // 生成占位符映射列表
      generatePlaceholderMappings(chartId)

      // 自动预览
      if (Object.keys(currentMappingConfig.value).length > 0) {
        previewMapping()
      }
    } else {
      message.error('加载映射配置失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 加载映射配置失败:', error)
    message.error('加载映射配置失败')
  }
}

// 生成占位符映射列表
const generatePlaceholderMappings = (chartId: string) => {
  // 根据图表类型生成默认占位符
  const defaultPlaceholders = getDefaultPlaceholders(chartId)

  placeholderMappings.value = defaultPlaceholders.map(placeholder => ({
    placeholder,
    fieldName: currentMappingConfig.value[placeholder] || '',
    description: getPlaceholderDescription(placeholder)
  }))
}

// 获取默认占位符
const getDefaultPlaceholders = (chartId: string) => {
  const commonPlaceholders = [
    '${chart_title}',
    '${categories}',
    '${series_1_name}',
    '${series_1_data}',
    '${series_2_name}',
    '${series_2_data}',
    '${series_3_name}',
    '${series_3_data}'
  ]

  if (chartId.includes('stacked')) {
    return [
      ...commonPlaceholders,
      '${series_4_name}',
      '${series_4_data}',
      '${series_5_name}',
      '${series_5_data}',
      '${stack_group}'
    ]
  }

  if (chartId.includes('pie')) {
    return [
      '${chart_title}',
      '${series_1_name}',
      '${series_1_data}',
      '${radius}',
      '${center}'
    ]
  }

  return commonPlaceholders
}

// 获取占位符描述
const getPlaceholderDescription = (placeholder: string) => {
  const descriptions = {
    '${chart_title}': '图表标题',
    '${categories}': '分类轴数据',
    '${series_1_name}': '第一个系列名称',
    '${series_1_data}': '第一个系列数据',
    '${series_2_name}': '第二个系列名称',
    '${series_2_data}': '第二个系列数据',
    '${series_3_name}': '第三个系列名称',
    '${series_3_data}': '第三个系列数据',
    '${series_4_name}': '第四个系列名称',
    '${series_4_data}': '第四个系列数据',
    '${series_5_name}': '第五个系列名称',
    '${series_5_data}': '第五个系列数据',
    '${stack_group}': '堆叠分组',
    '${radius}': '半径设置',
    '${center}': '中心位置'
  }
  return descriptions[placeholder] || placeholder
}

// 更新映射
const updateMapping = (placeholder: string, fieldName: string) => {
  currentMappingConfig.value[placeholder] = fieldName

  // 更新占位符映射列表
  const mapping = placeholderMappings.value.find(item => item.placeholder === placeholder)
  if (mapping) {
    mapping.fieldName = fieldName
  }

  console.log('🔄 [映射配置] 更新映射:', placeholder, '->', fieldName)

  // 自动预览
  if (fieldName) {
    previewMapping()
  }
}

// 字段过滤
const filterFieldOption = (input: string, option: any) => {
  return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
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

// 拖拽开始
const onDragStart = (event: DragEvent, field: any) => {
  draggedField.value = field
  event.dataTransfer?.setData('text/plain', field.fieldName)
}

// 拖拽放置
const onDrop = (event: DragEvent, placeholder: string) => {
  event.preventDefault()
  const fieldName = event.dataTransfer?.getData('text/plain')
  if (fieldName && draggedField.value) {
    updateMapping(placeholder, fieldName)
    message.success(`已将 ${draggedField.value.displayName} 映射到 ${placeholder}`)
  }
  draggedField.value = null
}

// 获取占位符的映射
const getMappingForPlaceholder = (placeholder: string) => {
  return currentMappingConfig.value[placeholder]
}

// 获取字段显示名称
const getFieldDisplayName = (fieldName: string) => {
  const field = availableFields.value.find(f => f.fieldName === fieldName)
  return field ? field.displayName : fieldName
}

// 保存映射配置
const saveMapping = async () => {
  if (!selectedChartId.value) {
    message.error('请先选择图表类型')
    return
  }

  savingMapping.value = true
  try {
    const mappingConfig = {
      chartId: selectedChartId.value,
      configName: `${selectedChartId.value} 映射配置`,
      description: '用户自定义映射配置',
      mappings: currentMappingConfig.value,
      active: true
    }

    const response = await fetch(`/api/chart/dynamic-mapping/config/${selectedChartId.value}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(mappingConfig)
    })

    const result = await response.json()

    if (result.success) {
      message.success('映射配置保存成功')
      console.log('✅ [映射配置] 保存成功')
    } else {
      message.error('保存失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 保存失败:', error)
    message.error('保存映射配置失败')
  } finally {
    savingMapping.value = false
  }
}

// 预览映射效果
const previewMapping = async () => {
  if (!selectedChartId.value || Object.keys(currentMappingConfig.value).length === 0) {
    return
  }

  previewLoading.value = true
  try {
    const response = await fetch(`/api/chart/dynamic-mapping/config/${selectedChartId.value}/preview`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(currentMappingConfig.value)
    })

    const result = await response.json()

    if (result.success) {
      previewData.value = Object.entries(result.data.previewData).map(([key, value]) => ({
        placeholder: key,
        value: Array.isArray(value) ? value.join(', ') : String(value)
      }))

      previewColumns.value = [
        { title: '占位符', dataIndex: 'placeholder', key: 'placeholder' },
        { title: '预览值', dataIndex: 'value', key: 'value' }
      ]

      // 渲染预览图表
      nextTick(() => {
        renderPreviewChart(result.data.previewData)
      })

      console.log('✅ [映射配置] 预览生成成功')
    } else {
      message.error('预览生成失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 预览生成失败:', error)
    message.error('预览生成失败')
  } finally {
    previewLoading.value = false
  }
}

// 渲染预览图表
const renderPreviewChart = (previewData: any) => {
  if (!previewChart.value) return

  const chart = echarts.init(previewChart.value)

  // 根据图表类型生成配置
  const option = generateChartOption(previewData)

  chart.setOption(option)
}

// 生成图表配置
const generateChartOption = (data: any) => {
  // 基础配置
  const option: any = {
    title: {
      text: data['${chart_title}'] || '预览图表',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: 30
    }
  }

  if (selectedChartId.value.includes('pie')) {
    // 饼图配置
    option.series = [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 335, name: '示例数据1' },
        { value: 310, name: '示例数据2' },
        { value: 234, name: '示例数据3' }
      ]
    }]
  } else {
    // 折线图/柱状图配置
    option.xAxis = {
      type: 'category',
      data: data['${categories}'] || ['类别1', '类别2', '类别3', '类别4', '类别5']
    }
    option.yAxis = {
      type: 'value'
    }
    option.series = [
      {
        name: data['${series_1_name}'] || '系列1',
        type: selectedChartId.value.includes('bar') ? 'bar' : 'line',
        data: data['${series_1_data}'] || [120, 200, 150, 80, 70]
      }
    ]

    if (data['${series_2_name}']) {
      option.series.push({
        name: data['${series_2_name}'],
        type: selectedChartId.value.includes('bar') ? 'bar' : 'line',
        data: data['${series_2_data}'] || [220, 182, 191, 234, 290]
      })
    }
  }

  return option
}

// 应用模板
const applyTemplate = async (templateId: string) => {
  if (!selectedChartId.value) {
    message.error('请先选择图表类型')
    return
  }

  applyingTemplate.value = templateId
  try {
    const response = await fetch(`/api/chart/dynamic-mapping/config/${selectedChartId.value}/apply-template/${templateId}`, {
      method: 'POST'
    })

    const result = await response.json()

    if (result.success) {
      message.success('模板应用成功')
      // 重新加载配置
      loadChartMappingConfig(selectedChartId.value)
    } else {
      message.error('应用模板失败: ' + result.message)
    }
  } catch (error) {
    console.error('❌ [映射配置] 应用模板失败:', error)
    message.error('应用模板失败')
  } finally {
    applyingTemplate.value = ''
  }
}

// 显示保存模板模态框
const showSaveTemplateModal = () => {
  if (Object.keys(currentMappingConfig.value).length === 0) {
    message.error('请先配置映射关系')
    return
  }

  saveTemplateModalVisible.value = true
  // 重置表单
  Object.assign(saveTemplateForm, {
    templateName: '',
    description: '',
    category: ''
  })
}

// 处理保存模板
const handleSaveTemplate = async () => {
  saveTemplateLoading.value = true
  try {
    // 这里应该调用API保存模板
    console.log('保存模板:', saveTemplateForm, currentMappingConfig.value)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('模板保存成功')
    saveTemplateModalVisible.value = false
    loadMappingTemplates() // 重新加载模板列表
  } catch (error) {
    console.error('保存模板失败:', error)
    message.error('保存模板失败')
  } finally {
    saveTemplateLoading.value = false
  }
}

// 取消保存模板
const cancelSaveTemplate = () => {
  saveTemplateModalVisible.value = false
}

// 删除模板
const deleteTemplate = (templateId: string) => {
  console.log('删除模板:', templateId)
  message.info('删除模板功能开发中')
}

// 刷新模板
const refreshTemplates = () => {
  loadMappingTemplates()
}

// 重置映射
const resetMapping = () => {
  currentMappingConfig.value = {}
  placeholderMappings.value.forEach(item => {
    item.fieldName = ''
  })
  previewData.value = []
  message.info('映射配置已重置')
}

// 刷新预览
const refreshPreview = () => {
  previewMapping()
}

// 预览单个映射
const previewSingleMapping = (record: any) => {
  console.log('预览单个映射:', record)
  message.info('单个映射预览功能开发中')
}
</script>

<style scoped>
.dynamic-mapping-container {
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

.chart-selection-card,
.template-card,
.mapping-config-card,
.preview-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.chart-selection-card .ant-card-head,
.template-card .ant-card-head,
.mapping-config-card .ant-card-head,
.preview-card .ant-card-head {
  border-bottom: 2px solid #f0f0f0;
}

.chart-selection-card .ant-card-head-title,
.template-card .ant-card-head-title,
.mapping-config-card .ant-card-head-title,
.preview-card .ant-card-head-title {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.mapping-content {
  padding: 16px 0;
}

.placeholder-section h3 {
  margin-bottom: 16px;
  color: #262626;
  font-size: 16px;
  font-weight: 600;
}

.drag-mapping-section {
  margin-top: 24px;
}

.drag-source,
.drag-target {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
  min-height: 300px;
}

.drag-source h4,
.drag-target h4 {
  margin: 0 0 16px 0;
  color: #262626;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.field-list,
.placeholder-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.3s;
}

.field-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
  transform: translateY(-1px);
}

.field-item:active {
  cursor: grabbing;
}

.placeholder-item {
  padding: 12px;
  background: white;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  transition: all 0.3s;
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.placeholder-item:hover {
  border-color: #1890ff;
  background: #f0f8ff;
}

.placeholder-item.has-mapping {
  border-color: #52c41a;
  border-style: solid;
  background: #f6ffed;
}

.placeholder-name {
  font-weight: 600;
  color: #262626;
  font-size: 12px;
  margin-bottom: 4px;
}

.mapping-info {
  font-size: 12px;
  color: #666;
}

.no-mapping {
  color: #999;
  font-style: italic;
}

.mapping-status {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 8px;
  margin-bottom: 16px;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
}

.preview-data h4,
.chart-preview h4 {
  margin: 16px 0 8px 0;
  color: #262626;
  font-size: 14px;
  font-weight: 600;
}

.preview-chart-container {
  height: 200px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.loading-state {
  text-align: center;
  padding: 40px 0;
}

.loading-state p {
  margin-top: 16px;
  color: #666;
}

.preview-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.ant-table {
  background: white;
  border-radius: 6px;
}

.ant-table-thead > tr > th {
  background-color: #fafafa;
  font-weight: 600;
  color: #262626;
}

.ant-list-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.ant-list-item:last-child {
  border-bottom: none;
}

.ant-list-item-meta-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.ant-list-item-meta-description {
  font-size: 12px;
  color: #666;
}

.ant-select {
  width: 100%;
}

.ant-select-selector {
  border-radius: 6px;
}

.ant-tag {
  border-radius: 4px;
  font-weight: 500;
  font-size: 11px;
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

.ant-statistic {
  padding: 0;
}

.ant-statistic-title {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.ant-statistic-content {
  font-weight: 600;
}

.ant-progress {
  margin: 0;
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

.ant-divider {
  border-color: #e8e8e8;
  margin: 16px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dynamic-mapping-container {
    padding: 16px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }

  .drag-source,
  .drag-target {
    min-height: 200px;
  }
}

@media (max-width: 768px) {
  .dynamic-mapping-container {
    padding: 12px;
  }

  .ant-col {
    margin-bottom: 16px;
  }

  .drag-mapping-section .ant-row {
    flex-direction: column;
  }

  .drag-mapping-section .ant-col {
    width: 100%;
  }

  .preview-chart-container {
    height: 150px;
  }
}

/* 拖拽动画效果 */
.field-item.dragging {
  opacity: 0.5;
  transform: rotate(5deg);
}

.placeholder-item.drag-over {
  border-color: #52c41a;
  background: #f6ffed;
  transform: scale(1.02);
}

/* 滚动条样式 */
.preview-content::-webkit-scrollbar,
.preview-detail-content::-webkit-scrollbar {
  width: 6px;
}

.preview-content::-webkit-scrollbar-track,
.preview-detail-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.preview-content::-webkit-scrollbar-thumb,
.preview-detail-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.preview-content::-webkit-scrollbar-thumb:hover,
.preview-detail-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
