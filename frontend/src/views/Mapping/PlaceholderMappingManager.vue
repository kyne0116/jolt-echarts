<template>
  <div class="placeholder-mapping-manager">
    <div class="page-header">
      <h2>🔗 占位符映射管理器</h2>
      <p class="page-description">
        建立虚拟数据库与图表模板之间的动态映射关系，实现数据的实时同步和转换
      </p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：图表选择和配置 -->
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
                <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
                <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
                <a-select-option value="pie_chart">饼图</a-select-option>
                <a-select-option value="radar_chart">雷达图</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>

        <a-card title="占位符列表" class="placeholders-card">
          <template #extra>
            <a-space>
              <a-tag color="blue">{{ placeholders.length }} 个占位符</a-tag>
              <a-button 
                type="link" 
                size="small" 
                @click="refreshPlaceholders"
                :loading="placeholderLoading"
              >
                <ReloadOutlined />
                刷新
              </a-button>
            </a-space>
          </template>

          <div v-if="placeholderLoading" class="loading-container">
            <a-spin tip="加载占位符中..." />
          </div>

          <div v-else-if="placeholders.length === 0" class="empty-container">
            <a-empty description="请先选择图表类型" />
          </div>

          <div v-else class="placeholders-list">
            <div 
              v-for="placeholder in placeholders" 
              :key="placeholder"
              class="placeholder-item"
              :class="{ 'mapped': isMapped(placeholder), 'unmapped': !isMapped(placeholder) }"
              @click="selectPlaceholder(placeholder)"
            >
              <div class="placeholder-name">
                <a-tag :color="isMapped(placeholder) ? 'green' : 'orange'">
                  {{ placeholder }}
                </a-tag>
              </div>
              <div class="placeholder-status">
                <a-icon 
                  :component="isMapped(placeholder) ? CheckCircleOutlined : ExclamationCircleOutlined"
                  :style="{ color: isMapped(placeholder) ? '#52c41a' : '#faad14' }"
                />
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 中间：映射配置 -->
      <a-col :span="8">
        <a-card title="映射配置" class="mapping-config-card">
          <template #extra>
            <a-space>
              <a-button 
                type="primary" 
                size="small" 
                @click="saveAllMappings"
                :loading="savingMappings"
                :disabled="!hasUnsavedChanges"
              >
                <SaveOutlined />
                保存配置
              </a-button>
              <a-button 
                size="small" 
                @click="generateDefaultMappings"
                :loading="generatingDefaults"
              >
                <BulbOutlined />
                智能推荐
              </a-button>
            </a-space>
          </template>

          <div v-if="!selectedPlaceholder" class="empty-config">
            <a-empty description="请选择一个占位符进行配置" />
          </div>

          <div v-else class="mapping-form">
            <a-form layout="vertical" :model="currentMapping">
              <a-form-item label="占位符">
                <a-input :value="selectedPlaceholder" disabled />
              </a-form-item>

              <a-form-item label="映射字段" required>
                <a-select 
                  v-model:value="currentMapping.fieldName"
                  placeholder="选择数据库字段"
                  @change="onFieldChange"
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
                      {{ field.label }} ({{ field.type }})
                    </a-select-option>
                  </a-select-opt-group>
                </a-select>
              </a-form-item>

              <a-form-item label="数据类型">
                <a-select v-model:value="currentMapping.dataType" placeholder="选择数据类型">
                  <a-select-option value="string">字符串</a-select-option>
                  <a-select-option value="number">数值</a-select-option>
                  <a-select-option value="array">数组</a-select-option>
                  <a-select-option value="object">对象</a-select-option>
                  <a-select-option value="boolean">布尔值</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="聚合方式">
                <a-select v-model:value="currentMapping.aggregationType" placeholder="选择聚合方式">
                  <a-select-option value="none">无聚合</a-select-option>
                  <a-select-option value="sum">求和</a-select-option>
                  <a-select-option value="avg">平均值</a-select-option>
                  <a-select-option value="count">计数</a-select-option>
                  <a-select-option value="max">最大值</a-select-option>
                  <a-select-option value="min">最小值</a-select-option>
                  <a-select-option value="list">列表</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="过滤条件">
                <div class="filters-container">
                  <div 
                    v-for="(filter, index) in currentMapping.filters" 
                    :key="index"
                    class="filter-item"
                  >
                    <a-input-group compact>
                      <a-select v-model:value="filter.field" style="width: 40%">
                        <a-select-option 
                          v-for="field in availableFields" 
                          :key="field.name"
                          :value="field.name"
                        >
                          {{ field.label }}
                        </a-select-option>
                      </a-select>
                      <a-select v-model:value="filter.operator" style="width: 20%">
                        <a-select-option value="=">=</a-select-option>
                        <a-select-option value="!=">!=</a-select-option>
                        <a-select-option value=">">></a-select-option>
                        <a-select-option value="<"><</a-select-option>
                      </a-select>
                      <a-input v-model:value="filter.value" style="width: 30%" />
                      <a-button 
                        type="text" 
                        danger 
                        style="width: 10%"
                        @click="removeFilter(index)"
                      >
                        <DeleteOutlined />
                      </a-button>
                    </a-input-group>
                  </div>
                  <a-button 
                    type="dashed" 
                    block 
                    @click="addFilter"
                    style="margin-top: 8px;"
                  >
                    <PlusOutlined />
                    添加过滤条件
                  </a-button>
                </div>
              </a-form-item>

              <a-form-item>
                <a-space>
                  <a-button 
                    type="primary" 
                    @click="saveMappingConfig"
                    :loading="savingMapping"
                  >
                    保存映射
                  </a-button>
                  <a-button @click="resetMappingConfig">
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：预览和测试 -->
      <a-col :span="8">
        <a-card title="映射预览" class="preview-card">
          <template #extra>
            <a-space>
              <a-button 
                type="primary" 
                @click="previewMapping"
                :loading="previewLoading"
                :disabled="!selectedChartId"
              >
                <EyeOutlined />
                预览结果
              </a-button>
              <a-button 
                @click="testMapping"
                :loading="testLoading"
                :disabled="!selectedChartId"
              >
                <PlayCircleOutlined />
                测试映射
              </a-button>
            </a-space>
          </template>

          <div v-if="previewResult" class="preview-content">
            <a-tabs v-model:activeKey="previewTab">
              <a-tab-pane key="summary" tab="映射摘要">
                <div class="mapping-summary">
                  <a-descriptions size="small" :column="1">
                    <a-descriptions-item label="图表类型">
                      {{ selectedChartId }}
                    </a-descriptions-item>
                    <a-descriptions-item label="占位符总数">
                      {{ placeholders.length }}
                    </a-descriptions-item>
                    <a-descriptions-item label="已映射">
                      {{ mappedCount }}
                    </a-descriptions-item>
                    <a-descriptions-item label="未映射">
                      {{ unmappedCount }}
                    </a-descriptions-item>
                    <a-descriptions-item label="映射状态">
                      <a-tag :color="isAllMapped ? 'green' : 'orange'">
                        {{ isAllMapped ? '完整' : '不完整' }}
                      </a-tag>
                    </a-descriptions-item>
                  </a-descriptions>
                </div>
              </a-tab-pane>

              <a-tab-pane key="data" tab="映射数据">
                <div class="mapped-data">
                  <pre class="json-preview">{{ JSON.stringify(previewResult.mappedData, null, 2) }}</pre>
                </div>
              </a-tab-pane>

              <a-tab-pane key="result" tab="最终结果">
                <div class="final-result">
                  <pre class="json-preview">{{ JSON.stringify(previewResult.result, null, 2) }}</pre>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>

          <div v-else class="empty-preview">
            <a-empty description="点击预览按钮查看映射结果" />
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { placeholderMappingApi } from '@/api'
import {
    BulbOutlined,
    CheckCircleOutlined,
    DeleteOutlined,
    ExclamationCircleOutlined,
    EyeOutlined,
    PlayCircleOutlined,
    PlusOutlined,
    ReloadOutlined,
    SaveOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'

// ==================== 响应式数据 ====================

// 图表选择
const selectedChartId = ref('')
const chartLoading = ref(false)

// 占位符管理
const placeholders = ref<string[]>([])
const placeholderLoading = ref(false)
const selectedPlaceholder = ref('')

// 映射配置
const mappings = ref<Record<string, any>>({})
const currentMapping = reactive({
  fieldName: '',
  dataType: 'string',
  aggregationType: 'none',
  filters: [] as Array<{ field: string; operator: string; value: string }>,
  transformExpression: ''
})

// 字段数据
const availableFields = ref<any[]>([])
const groupedFields = ref<any[]>([])

// 状态管理
const savingMapping = ref(false)
const savingMappings = ref(false)
const generatingDefaults = ref(false)
const previewLoading = ref(false)
const testLoading = ref(false)
const hasUnsavedChanges = ref(false)

// 预览结果
const previewResult = ref<any>(null)
const previewTab = ref('summary')

// ==================== 计算属性 ====================

const mappedCount = computed(() => {
  return Object.keys(mappings.value).length
})

const unmappedCount = computed(() => {
  return placeholders.value.length - mappedCount.value
})

const isAllMapped = computed(() => {
  return placeholders.value.length > 0 && unmappedCount.value === 0
})

// ==================== 方法 ====================

// 初始化
onMounted(async () => {
  await loadAvailableFields()
})

// 监听图表变化
watch(selectedChartId, (newChartId) => {
  if (newChartId) {
    refreshPlaceholders()
  }
})

// 加载可用字段
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
    } else {
      message.error('加载可用字段失败: 数据格式错误')
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载可用字段失败:', error)
    message.error('加载可用字段失败: ' + (error.message || '网络错误'))
  }
}

// 图表变化处理
const onChartChange = async (chartId: string) => {
  console.log('🔄 [映射管理] 图表切换:', chartId)
  selectedPlaceholder.value = ''
  mappings.value = {}
  previewResult.value = null
  hasUnsavedChanges.value = false
  
  if (chartId) {
    await Promise.all([
      refreshPlaceholders(),
      loadExistingMappings(chartId)
    ])
  }
}

// 刷新占位符
const refreshPlaceholders = async () => {
  if (!selectedChartId.value) return

  placeholderLoading.value = true
  try {
    const result = await placeholderMappingApi.getPlaceholders(selectedChartId.value)

    if (result && result.placeholders) {
      placeholders.value = result.placeholders || []
      console.log('✅ [映射管理] 加载占位符成功:', placeholders.value.length)
    } else {
      message.error('加载占位符失败: 数据格式错误')
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载占位符失败:', error)
    message.error('加载占位符失败: ' + (error.message || '网络错误'))
  } finally {
    placeholderLoading.value = false
  }
}

// 加载现有映射配置
const loadExistingMappings = async (chartId: string) => {
  try {
    const result = await placeholderMappingApi.getMappings(chartId)

    if (result && result.hasConfig) {
      mappings.value = result.mappings || {}
      console.log('✅ [映射管理] 加载现有映射配置:', Object.keys(mappings.value).length)
    }
  } catch (error) {
    console.error('❌ [映射管理] 加载现有映射配置失败:', error)
  }
}

// 检查是否已映射
const isMapped = (placeholder: string) => {
  return !!mappings.value[placeholder]
}

// 选择占位符
const selectPlaceholder = (placeholder: string) => {
  selectedPlaceholder.value = placeholder
  
  // 加载现有配置
  const existingMapping = mappings.value[placeholder]
  if (existingMapping) {
    Object.assign(currentMapping, existingMapping)
    // 确保filters是数组
    if (!Array.isArray(currentMapping.filters)) {
      currentMapping.filters = []
    }
  } else {
    // 重置配置
    resetMappingConfig()
  }
}

// 字段变化处理
const onFieldChange = (fieldName: string) => {
  const field = availableFields.value.find(f => f.name === fieldName)
  if (field) {
    currentMapping.dataType = field.type
    hasUnsavedChanges.value = true
  }
}

// 添加过滤条件
const addFilter = () => {
  currentMapping.filters.push({
    field: '',
    operator: '=',
    value: ''
  })
  hasUnsavedChanges.value = true
}

// 移除过滤条件
const removeFilter = (index: number) => {
  currentMapping.filters.splice(index, 1)
  hasUnsavedChanges.value = true
}

// 保存单个映射配置
const saveMappingConfig = () => {
  if (!selectedPlaceholder.value || !currentMapping.fieldName) {
    message.warning('请完善映射配置')
    return
  }
  
  // 转换filters格式
  const filtersObj = currentMapping.filters.reduce((acc, filter) => {
    if (filter.field && filter.value) {
      acc[filter.field] = filter.value
    }
    return acc
  }, {} as Record<string, any>)
  
  mappings.value[selectedPlaceholder.value] = {
    fieldName: currentMapping.fieldName,
    dataType: currentMapping.dataType,
    aggregationType: currentMapping.aggregationType,
    filters: filtersObj,
    transformExpression: currentMapping.transformExpression
  }
  
  hasUnsavedChanges.value = true
  message.success('映射配置已保存到本地')
}

// 重置映射配置
const resetMappingConfig = () => {
  Object.assign(currentMapping, {
    fieldName: '',
    dataType: 'string',
    aggregationType: 'none',
    filters: [],
    transformExpression: ''
  })
}

// 保存所有映射配置
const saveAllMappings = async () => {
  if (!selectedChartId.value) {
    message.warning('请先选择图表类型')
    return
  }

  savingMappings.value = true
  try {
    const result = await placeholderMappingApi.configureMappings(selectedChartId.value, mappings.value)

    if (result && result.success) {
      hasUnsavedChanges.value = false
      message.success('映射配置保存成功')
    } else {
      message.error('保存失败: ' + (result?.message || '未知错误'))
    }
  } catch (error) {
    console.error('❌ [映射管理] 保存映射配置失败:', error)
    message.error('保存映射配置失败: ' + (error.message || '网络错误'))
  } finally {
    savingMappings.value = false
  }
}

// 生成默认映射
const generateDefaultMappings = async () => {
  if (!selectedChartId.value || placeholders.value.length === 0) {
    message.warning('请先选择图表类型并加载占位符')
    return
  }
  
  generatingDefaults.value = true
  try {
    // 这里可以调用后端API生成智能推荐
    // 暂时使用前端逻辑
    const defaultMappings: Record<string, any> = {}
    
    placeholders.value.forEach(placeholder => {
      const variableName = placeholder.replace(/^\$\{|\}$/g, '').toLowerCase()
      
      let fieldMapping = { fieldName: 'category', dataType: 'string', aggregationType: 'none', filters: {} }
      
      if (variableName.includes('category') || variableName.includes('categories')) {
        fieldMapping = { fieldName: 'category', dataType: 'array', aggregationType: 'list', filters: {} }
      } else if (variableName.includes('amount') || variableName.includes('value')) {
        fieldMapping = { fieldName: 'amount', dataType: 'number', aggregationType: 'sum', filters: {} }
      } else if (variableName.includes('quantity') || variableName.includes('count')) {
        fieldMapping = { fieldName: 'quantity', dataType: 'number', aggregationType: 'sum', filters: {} }
      } else if (variableName.includes('region')) {
        fieldMapping = { fieldName: 'region', dataType: 'string', aggregationType: 'none', filters: {} }
      } else if (variableName.includes('product')) {
        fieldMapping = { fieldName: 'product', dataType: 'string', aggregationType: 'none', filters: {} }
      } else if (variableName.includes('channel')) {
        fieldMapping = { fieldName: 'channel', dataType: 'string', aggregationType: 'none', filters: {} }
      }
      
      defaultMappings[placeholder] = fieldMapping
    })
    
    mappings.value = { ...mappings.value, ...defaultMappings }
    hasUnsavedChanges.value = true
    message.success(`已生成 ${Object.keys(defaultMappings).length} 个默认映射`)
    
  } catch (error) {
    console.error('❌ [映射管理] 生成默认映射失败:', error)
    message.error('生成默认映射失败')
  } finally {
    generatingDefaults.value = false
  }
}

// 预览映射结果
const previewMapping = async () => {
  if (!selectedChartId.value) {
    message.warning('请先选择图表类型')
    return
  }

  previewLoading.value = true
  try {
    const result = await placeholderMappingApi.previewMapping(selectedChartId.value, {})

    if (result) {
      previewResult.value = result
      message.success('预览生成成功')
    } else {
      message.error('预览失败: 数据格式错误')
    }
  } catch (error) {
    console.error('❌ [映射管理] 预览映射失败:', error)
    message.error('预览映射失败: ' + (error.message || '网络错误'))
  } finally {
    previewLoading.value = false
  }
}

// 测试映射
const testMapping = async () => {
  if (!selectedChartId.value) {
    message.warning('请先选择图表类型')
    return
  }
  
  testLoading.value = true
  try {
    // 先保存当前配置
    await saveAllMappings()
    
    // 然后预览
    await previewMapping()
    
    message.success('映射测试完成')
  } catch (error) {
    console.error('❌ [映射管理] 测试映射失败:', error)
    message.error('测试映射失败')
  } finally {
    testLoading.value = false
  }
}
</script>

<style scoped>
.placeholder-mapping-manager {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1890ff;
}

.page-description {
  color: #666;
  margin: 0;
}

.chart-selection-card,
.placeholders-card,
.mapping-config-card,
.preview-card {
  height: fit-content;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.placeholders-list {
  max-height: 400px;
  overflow-y: auto;
}

.placeholder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.placeholder-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.placeholder-item.mapped {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.placeholder-item.unmapped {
  border-color: #faad14;
  background-color: #fffbe6;
}

.empty-config,
.empty-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.filters-container {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}

.filter-item {
  margin-bottom: 8px;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.preview-content {
  max-height: 500px;
  overflow-y: auto;
}

.json-preview {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  max-height: 300px;
  overflow: auto;
}

.mapping-summary {
  padding: 16px;
}
</style>
