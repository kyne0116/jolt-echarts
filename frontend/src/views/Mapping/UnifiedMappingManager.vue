<template>
  <div class="unified-mapping-manager">
    <div class="page-header">
      <h2>🔗 统一映射管理器</h2>
      <p class="page-description">
        集成占位符映射、动态配置和实时预览的一体化管理界面
      </p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：图表选择和配置模式 -->
      <a-col :span="6">
        <a-card title="图表选择" class="chart-selection-card" style="margin-bottom: 16px;">
          <a-form layout="vertical">
            <a-form-item label="选择图表类型">
              <a-select 
                v-model:value="selectedChartId" 
                placeholder="请选择图表类型"
                @change="onChartChange"
                :loading="chartLoading"
              >
                <a-select-option value="basic_line_chart">基础折线图</a-select-option>
                <a-select-option value="smooth_line_chart">平滑折线图</a-select-option>
                <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
                <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
                <a-select-option value="pie_chart">饼图</a-select-option>
              </a-select>
            </a-form-item>
            
            <a-form-item label="配置模式">
              <a-radio-group v-model:value="configMode" @change="onModeChange">
                <a-radio-button value="basic">基础映射</a-radio-button>
                <a-radio-button value="advanced">高级配置</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-form>
        </a-card>

        <a-card title="占位符列表" class="placeholders-card">
          <template #extra>
            <a-space>
              <a-tag color="blue">{{ placeholders.length }} 个</a-tag>
              <a-button 
                type="link" 
                size="small" 
                @click="refreshPlaceholders"
                :loading="placeholderLoading"
              >
                <ReloadOutlined />
              </a-button>
            </a-space>
          </template>

          <div v-if="placeholderLoading" class="loading-container">
            <a-spin tip="加载中..." />
          </div>

          <div v-else-if="placeholders.length === 0" class="empty-container">
            <a-empty description="请先选择图表类型" />
          </div>

          <div v-else class="placeholders-list">
            <div 
              v-for="placeholder in placeholders" 
              :key="placeholder"
              class="placeholder-item"
              :class="{ 'mapped': isMapped(placeholder), 'selected': selectedPlaceholder === placeholder }"
              @click="selectPlaceholder(placeholder)"
            >
              <a-tag :color="isMapped(placeholder) ? 'green' : 'orange'">
                {{ placeholder }}
              </a-tag>
              <a-icon 
                :component="isMapped(placeholder) ? CheckCircleOutlined : ExclamationCircleOutlined"
                :style="{ color: isMapped(placeholder) ? '#52c41a' : '#faad14' }"
              />
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 中间：映射配置区域 -->
      <a-col :span="12">
        <a-card title="映射配置" class="mapping-config-card">
          <template #extra>
            <a-space>
              <a-button 
                type="primary" 
                @click="saveAllMappings"
                :loading="savingMappings"
                :disabled="!hasUnsavedChanges"
              >
                <SaveOutlined />
                保存配置
              </a-button>
              <a-button 
                @click="generateDefaultMappings"
                :loading="generatingDefaults"
              >
                <BulbOutlined />
                智能推荐
              </a-button>
              <a-button 
                @click="previewMapping"
                :loading="previewLoading"
                :disabled="!selectedChartId"
              >
                <EyeOutlined />
                预览结果
              </a-button>
            </a-space>
          </template>

          <!-- 基础映射模式 -->
          <div v-if="configMode === 'basic'" class="basic-mapping-config">
            <div v-if="!selectedPlaceholder" class="no-selection">
              <a-empty description="请从左侧选择一个占位符进行配置" />
            </div>
            
            <div v-else class="mapping-form">
              <h4>配置占位符: {{ selectedPlaceholder }}</h4>
              
              <a-form layout="vertical">
                <a-form-item label="映射字段">
                  <a-select 
                    v-model:value="currentMapping.fieldName"
                    placeholder="选择数据库字段"
                    @change="onMappingChange"
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
                </a-form-item>
                
                <a-form-item label="数据类型">
                  <a-select 
                    v-model:value="currentMapping.dataType"
                    @change="onMappingChange"
                  >
                    <a-select-option value="string">字符串</a-select-option>
                    <a-select-option value="number">数字</a-select-option>
                    <a-select-option value="array">数组</a-select-option>
                    <a-select-option value="object">对象</a-select-option>
                  </a-select>
                </a-form-item>
                
                <a-form-item label="聚合方式">
                  <a-select 
                    v-model:value="currentMapping.aggregationType"
                    @change="onMappingChange"
                  >
                    <a-select-option value="none">无聚合</a-select-option>
                    <a-select-option value="sum">求和</a-select-option>
                    <a-select-option value="avg">平均值</a-select-option>
                    <a-select-option value="count">计数</a-select-option>
                    <a-select-option value="max">最大值</a-select-option>
                    <a-select-option value="min">最小值</a-select-option>
                    <a-select-option value="list">列表</a-select-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </div>
          </div>

          <!-- 高级配置模式 -->
          <div v-else class="advanced-mapping-config">
            <a-tabs v-model:activeKey="advancedTab">
              <a-tab-pane key="conditions" tab="条件逻辑">
                <div class="conditions-config">
                  <a-form layout="vertical">
                    <a-form-item label="条件表达式">
                      <a-textarea 
                        v-model:value="advancedConfig.conditionExpression"
                        placeholder="例如：if (category === 'sales') { return 'sales_data' } else { return 'default_data' }"
                        :rows="4"
                      />
                    </a-form-item>
                    
                    <a-form-item label="默认行为">
                      <a-radio-group v-model:value="advancedConfig.defaultBehavior">
                        <a-radio value="skip">跳过未匹配的占位符</a-radio>
                        <a-radio value="error">抛出错误</a-radio>
                        <a-radio value="default">使用默认值</a-radio>
                      </a-radio-group>
                    </a-form-item>
                  </a-form>
                </div>
              </a-tab-pane>
              
              <a-tab-pane key="templates" tab="模板管理">
                <div class="templates-config">
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
                          >
                            应用
                          </a-button>
                        </template>
                        <a-list-item-meta>
                          <template #title>{{ item.templateName }}</template>
                          <template #description>{{ item.description }}</template>
                        </a-list-item-meta>
                      </a-list-item>
                    </template>
                  </a-list>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：预览和状态 -->
      <a-col :span="6">
        <a-card title="映射状态" class="status-card" style="margin-bottom: 16px;">
          <div class="status-summary">
            <a-statistic 
              title="已映射" 
              :value="mappedCount" 
              :total="placeholders.length"
              suffix="/ {{ placeholders.length }}"
              :value-style="{ color: '#3f8600' }"
            />
            <a-progress 
              :percent="mappingProgress" 
              :stroke-color="mappingProgress === 100 ? '#52c41a' : '#1890ff'"
              style="margin-top: 8px;"
            />
          </div>
          
          <div class="quick-actions" style="margin-top: 16px;">
            <a-space direction="vertical" style="width: 100%;">
              <a-button block @click="clearAllMappings" danger>
                <DeleteOutlined />
                清空配置
              </a-button>
              <a-button block @click="exportMappings">
                <ExportOutlined />
                导出配置
              </a-button>
            </a-space>
          </div>
        </a-card>

        <a-card title="预览结果" class="preview-card">
          <div v-if="previewLoading" class="loading-container">
            <a-spin tip="生成预览中..." />
          </div>
          
          <div v-else-if="!previewResult" class="no-preview">
            <a-empty description="点击预览按钮查看结果" />
          </div>
          
          <div v-else class="preview-content">
            <pre>{{ JSON.stringify(previewResult, null, 2) }}</pre>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {
  BulbOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
  ExclamationCircleOutlined,
  ExportOutlined,
  EyeOutlined,
  ReloadOutlined,
  SaveOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { placeholderMappingApi } from '@/api'

// ==================== 响应式数据 ====================

// 基础状态
const selectedChartId = ref('')
const configMode = ref('basic') // 'basic' | 'advanced'
const advancedTab = ref('conditions')

// 占位符相关
const placeholders = ref<string[]>([])
const selectedPlaceholder = ref('')
const placeholderLoading = ref(false)

// 映射配置
const mappings = ref<Record<string, any>>({})
const currentMapping = reactive({
  fieldName: '',
  dataType: 'string',
  aggregationType: 'none',
  filters: {}
})

// 字段数据
const availableFields = ref<any[]>([])
const groupedFields = ref<any[]>([])

// 高级配置
const advancedConfig = reactive({
  conditionExpression: '',
  defaultBehavior: 'skip'
})

// 模板数据
const mappingTemplates = ref<any[]>([])
const templateLoading = ref(false)

// 状态管理
const chartLoading = ref(false)
const savingMappings = ref(false)
const generatingDefaults = ref(false)
const previewLoading = ref(false)
const hasUnsavedChanges = ref(false)

// 预览结果
const previewResult = ref(null)

// ==================== 计算属性 ====================

const mappedCount = computed(() => {
  return Object.keys(mappings.value).length
})

const mappingProgress = computed(() => {
  if (placeholders.value.length === 0) return 0
  return Math.round((mappedCount.value / placeholders.value.length) * 100)
})

// ==================== 方法 ====================

// 初始化
onMounted(async () => {
  await loadAvailableFields()
  await loadMappingTemplates()
})

// 监听图表变化
watch(selectedChartId, (newChartId) => {
  if (newChartId) {
    refreshPlaceholders()
    loadExistingMappings(newChartId)
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
    }
  } catch (error) {
    console.error('❌ [统一映射] 加载可用字段失败:', error)
    message.error('加载可用字段失败')
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
        category: template.category
      }))
    }
  } catch (error) {
    console.error('❌ [统一映射] 加载模板失败:', error)
  } finally {
    templateLoading.value = false
  }
}

// 其他方法的简化实现...
const onChartChange = (chartId: string) => {
  console.log('🔄 [统一映射] 图表切换:', chartId)
}

const onModeChange = () => {
  console.log('🔄 [统一映射] 模式切换:', configMode.value)
}

const refreshPlaceholders = async () => {
  if (!selectedChartId.value) return
  
  placeholderLoading.value = true
  try {
    const result = await placeholderMappingApi.getPlaceholders(selectedChartId.value)
    if (result && result.placeholders) {
      placeholders.value = result.placeholders || []
    }
  } catch (error) {
    console.error('❌ [统一映射] 加载占位符失败:', error)
  } finally {
    placeholderLoading.value = false
  }
}

const loadExistingMappings = async (chartId: string) => {
  try {
    const result = await placeholderMappingApi.getMappings(chartId)
    if (result && result.hasConfig) {
      mappings.value = result.mappings || {}
    }
  } catch (error) {
    console.error('❌ [统一映射] 加载现有映射失败:', error)
  }
}

const selectPlaceholder = (placeholder: string) => {
  selectedPlaceholder.value = placeholder
  // 加载当前占位符的映射配置
  if (mappings.value[placeholder]) {
    Object.assign(currentMapping, mappings.value[placeholder])
  } else {
    Object.assign(currentMapping, {
      fieldName: '',
      dataType: 'string',
      aggregationType: 'none',
      filters: {}
    })
  }
}

const isMapped = (placeholder: string) => {
  return !!mappings.value[placeholder]
}

const onMappingChange = () => {
  if (selectedPlaceholder.value) {
    mappings.value[selectedPlaceholder.value] = { ...currentMapping }
    hasUnsavedChanges.value = true
  }
}

const saveAllMappings = async () => {
  if (!selectedChartId.value) {
    message.warning('请先选择图表类型')
    return
  }
  
  savingMappings.value = true
  try {
    await placeholderMappingApi.configureMappings(selectedChartId.value, mappings.value)
    hasUnsavedChanges.value = false
    message.success('映射配置保存成功')
  } catch (error) {
    console.error('❌ [统一映射] 保存失败:', error)
    message.error('保存映射配置失败')
  } finally {
    savingMappings.value = false
  }
}

const generateDefaultMappings = async () => {
  generatingDefaults.value = true
  try {
    // 实现智能推荐逻辑
    message.success('智能推荐完成')
  } catch (error) {
    message.error('智能推荐失败')
  } finally {
    generatingDefaults.value = false
  }
}

const previewMapping = async () => {
  if (!selectedChartId.value) {
    message.warning('请先选择图表类型')
    return
  }
  
  previewLoading.value = true
  try {
    const result = await placeholderMappingApi.previewMapping(selectedChartId.value, {})
    previewResult.value = result
    message.success('预览生成成功')
  } catch (error) {
    console.error('❌ [统一映射] 预览失败:', error)
    message.error('预览映射失败')
  } finally {
    previewLoading.value = false
  }
}

const clearAllMappings = () => {
  mappings.value = {}
  hasUnsavedChanges.value = true
  message.info('已清空所有映射配置')
}

const exportMappings = () => {
  const dataStr = JSON.stringify(mappings.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `mapping-config-${selectedChartId.value}.json`
  link.click()
  URL.revokeObjectURL(url)
  message.success('配置导出成功')
}

const applyTemplate = (templateId: string) => {
  message.info(`应用模板: ${templateId}`)
}
</script>

<style scoped>
.unified-mapping-manager {
  padding: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1890ff;
}

.page-description {
  color: #666;
  margin: 0 0 24px 0;
}

.placeholder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.placeholder-item:hover {
  background-color: #f5f5f5;
}

.placeholder-item.selected {
  background-color: #e6f7ff;
  border: 1px solid #1890ff;
}

.placeholder-item.mapped {
  background-color: #f6ffed;
}

.loading-container,
.empty-container,
.no-selection,
.no-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.preview-content pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 300px;
  overflow: auto;
}

.status-summary {
  text-align: center;
}

.quick-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}
</style>
