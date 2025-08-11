<template>
  <div class="mapping-page">
    <!-- 图表选择区域 -->
    <div class="chart-selection-section">
      <a-card class="selection-card" size="small">
        <template #title>
          <span class="section-title">📊 图表选择</span>
        </template>
        <ChartSelector
          ref="chartSelectorRef"
          @chart-selected="onChartSelected"
          class="chart-selector"
        />
      </a-card>
    </div>

    <!-- 映射配置列表区域 -->
    <div class="mapping-list-section">
      <a-card class="list-card" size="small">
        <template #title>
          <span class="section-title">🔗 映射配置列表</span>
        </template>
        <template #extra>
          <a-space>
            <a-button
              type="primary"
              size="small"
              @click="refreshList"
              :loading="loading"
            >
              <ReloadOutlined />
              刷新
            </a-button>
          </a-space>
        </template>

        <a-table
          :dataSource="mappingList"
          :columns="tableColumns"
          :pagination="paginationConfig"
          :loading="loading"
          size="small"
          row-key="chartId"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'actions'">
              <a-space size="small">
                <a-button type="link" size="small" @click="viewMapping(record)">
                  查看
                </a-button>
                <a-button type="link" size="small" @click="configureMapping(record)">
                  配置映射
                </a-button>
                <a-button type="link" size="small" @click="showConfigGuide(record)">
                  配置指南
                </a-button>
                <a-button type="link" size="small" danger @click="deleteMapping(record)">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 配置映射独立浮动窗口 -->
    <div
      v-if="mappingModalVisible"
      class="mapping-floating-window"
      :style="mappingWindowStyle"
      @click="bringToFront('mapping')"
    >
      <!-- 窗口标题栏 -->
      <div
        class="window-header"
        @mousedown="startDrag($event, 'mapping')"
      >
        <div class="window-title">
          <span class="title-icon">🔧</span>
          <span class="title-text">{{ mappingModalTitle }}</span>
        </div>
        <div class="window-controls">
          <button
            class="control-btn close-btn"
            @click="mappingModalVisible = false"
            title="关闭"
          >
            ✕
          </button>
        </div>
      </div>

      <!-- 窗口内容 -->
      <div class="window-content">
        <div class="modal-content">
          <div v-if="selectedRecord">
          <!-- 图表信息展示 -->
          <a-descriptions title="图表信息" :column="2" size="small" bordered>
            <a-descriptions-item label="图表ID">{{ selectedRecord.chartId }}</a-descriptions-item>
            <a-descriptions-item label="图表类型">{{ selectedRecord.chartType }}</a-descriptions-item>
            <a-descriptions-item label="图表名称">{{ selectedRecord.chartName }}</a-descriptions-item>
            <a-descriptions-item label="通用模板">{{ selectedRecord.universalTemplate }}</a-descriptions-item>
          </a-descriptions>

          <!-- JOLT规范文件内容展示 -->
          <div class="jolt-spec-section" style="margin-top: 16px;">
            <h4>JOLT规范文件内容</h4>
            <div v-if="joltSpecLoading" class="loading-container">
              <a-spin tip="加载JOLT规范文件中..." />
            </div>
            <div v-else-if="joltSpecContent" class="jolt-spec-content">
              <div class="code-container">
                <pre class="code-block json-highlight"><code>{{ JSON.stringify(joltSpecContent, null, 2) }}</code></pre>
              </div>
            </div>
            <div v-else class="no-jolt-spec">
              <a-empty description="未找到对应的JOLT规范文件" />
            </div>
          </div>

          <!-- 占位符映射配置 -->
          <div class="mapping-config-section">
            <h4>占位符映射配置</h4>
            <div v-if="placeholdersLoading" class="loading-container">
              <a-spin tip="加载占位符中..." />
            </div>
            <div v-else>
              <div class="mapping-header">
                <a-row :gutter="12">
                  <a-col :span="6"><strong>占位符</strong></a-col>
                  <a-col :span="6"><strong>映射字段</strong></a-col>
                  <a-col :span="5"><strong>数据类型</strong></a-col>
                  <a-col :span="5"><strong>聚合方式</strong></a-col>
                  <a-col :span="2"><strong>操作</strong></a-col>
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
                      </div>
                    </a-col>
                    <a-col :span="6">
                      <a-select
                        :value="getMappingFieldName(placeholder)"
                        @update:value="updateMappingFieldName(placeholder, $event)"
                        placeholder="选择字段"
                        size="small"
                        style="width: 100%"
                        @change="onMappingChange(placeholder)"
                      >
                        <a-select-option
                          v-for="field in availableFields"
                          :key="field.name"
                          :value="field.name"
                        >
                          {{ field.displayName }}
                        </a-select-option>
                      </a-select>
                    </a-col>
                    <a-col :span="5">
                      <a-select
                        :value="getMappingDataType(placeholder)"
                        @update:value="updateMappingDataType(placeholder, $event)"
                        placeholder="数据类型"
                        size="small"
                        style="width: 100%"
                      >
                        <a-select-option value="string">字符串</a-select-option>
                        <a-select-option value="number">数字</a-select-option>
                        <a-select-option value="array">数组</a-select-option>
                        <a-select-option value="object">对象</a-select-option>
                      </a-select>
                    </a-col>
                    <a-col :span="5">
                      <a-select
                        :value="getMappingAggregationType(placeholder)"
                        @update:value="updateMappingAggregationType(placeholder, $event)"
                        placeholder="聚合方式"
                        size="small"
                        style="width: 100%"
                      >
                        <a-select-option value="none">无</a-select-option>
                        <a-select-option value="sum">求和</a-select-option>
                        <a-select-option value="avg">平均值</a-select-option>
                        <a-select-option value="count">计数</a-select-option>
                        <a-select-option value="max">最大值</a-select-option>
                        <a-select-option value="min">最小值</a-select-option>
                        <a-select-option value="list">列表</a-select-option>
                      </a-select>
                    </a-col>
                    <a-col :span="2">
                      <a-button
                        type="link"
                        size="small"
                        danger
                        @click="clearMapping(placeholder)"
                      >
                        清除
                      </a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>
            </div>
          </div>

          <!-- 映射进度 -->
          <div class="mapping-progress">
            <a-progress
              :percent="mappingProgress"
              :status="mappingProgress === 100 ? 'success' : 'active'"
              :format="(percent) => `${percent}% (${mappedCount}/${placeholders.length})`"
            />
          </div>

          <!-- 窗口底部操作栏 -->
          <div class="window-footer">
            <div class="footer-actions">
              <button
                class="action-btn cancel-btn"
                @click="mappingModalVisible = false"
              >
                取消
              </button>
              <button
                class="action-btn primary-btn"
                @click="saveMappingConfig"
                :disabled="savingConfig || mappingProgress === 0"
              >
                <span v-if="savingConfig" class="loading-icon">⏳</span>
                <span v-else>💾</span>
                {{ savingConfig ? '保存中...' : '保存配置' }}
              </button>
            </div>
          </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 配置指南独立浮动窗口 -->
    <div
      v-if="guideModalVisible"
      class="guide-floating-window"
      :style="guideWindowStyle"
      @click="bringToFront('guide')"
    >
      <!-- 窗口标题栏 -->
      <div
        class="window-header"
        @mousedown="startDrag($event, 'guide')"
      >
        <div class="window-title">
          <span class="title-icon">📖</span>
          <span class="title-text">配置指南 - {{ selectedRecord?.chartName || '未知图表' }}</span>
        </div>
        <div class="window-controls">
          <button
            class="control-btn close-btn"
            @click="guideModalVisible = false"
            title="关闭"
          >
            ✕
          </button>
        </div>
      </div>

      <!-- 窗口内容 -->
      <div class="window-content">
        <div v-if="selectedRecord" class="guide-content">
          <div class="guide-panels">
            <!-- 面板1 - 官方完整实例 -->
            <div class="guide-panel">
              <div class="panel-header">
                <h4 class="panel-title">
                  <span class="panel-icon">📊</span>
                  官方完整实例
                </h4>
                <a-tag color="blue" size="small">{{ selectedRecord.chartType }}</a-tag>
              </div>
              <div class="panel-content">
                <div class="section">
                  <h5>ECharts配置示例</h5>
                  <div class="code-container">
                    <pre class="code-block"><code>{{ getOfficialExample() }}</code></pre>
                  </div>
                </div>
                <div class="section">
                  <h5>结构说明</h5>
                  <ul class="description-list">
                    <li v-for="desc in getStructureDescription()" :key="desc">
                      {{ desc }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <!-- 面板2 - 转换结构说明 -->
            <div class="guide-panel">
              <div class="panel-header">
                <h4 class="panel-title">
                  <span class="panel-icon">🔄</span>
                  转换结构说明
                </h4>
                <a-tag color="orange" size="small">{{ selectedRecord.universalTemplate }}</a-tag>
              </div>
              <div class="panel-content">
                <div class="section">
                  <h5>JOLT转换涉及的数据结构</h5>

                  <h6>转换前数据结构</h6>
                  <div class="code-container">
                    <pre class="code-block"><code>{{ getTransformationBefore() }}</code></pre>
                  </div>

                  <h6>转换后数据结构</h6>
                  <div class="code-container">
                    <pre class="code-block"><code>{{ getTransformationAfter() }}</code></pre>
                  </div>

                  <h6>占位符变量列表</h6>
                  <div class="placeholder-tags">
                    <a-tag
                      v-for="placeholder in getPlaceholderList()"
                      :key="placeholder.name"
                      :color="placeholder.color"
                      size="small"
                      style="margin: 2px;"
                    >
                      {{ placeholder.name }} - {{ placeholder.description }}
                    </a-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { placeholderMappingApi } from '@/api'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'

// 接口类型定义
interface MappingRecord {
  chartId: string
  chartType: string
  chartName: string
  universalTemplate: string
  joltSpecFile: string
  placeholderCount: number
}

interface SelectedChart {
  chartId: string
  templateType: string
  chartFile: string
  chartName: string
  joltSpecFile: string
}

// 响应式数据
const chartSelectorRef = ref()
const loading = ref(false)
const mappingList = ref<MappingRecord[]>([])
const selectedRecord = ref<MappingRecord | null>(null)

// 选中的图表信息
const selectedChart = reactive<SelectedChart>({
  chartId: '',
  templateType: '',
  chartFile: '',
  chartName: '',
  joltSpecFile: ''
})

// 模态框状态
const mappingModalVisible = ref(false)
const guideModalVisible = ref(false)

// 拖拽状态
const isDragging = ref(false)
const dragTarget = ref('')
const mappingModalPosition = ref({ top: 100, left: 150 })
const guideModalPosition = ref({ top: 150, left: 300 })

// 映射配置相关状态
const placeholders = ref<string[]>([])
const placeholdersLoading = ref(false)
const mappingConfigs = reactive<Record<string, any>>({})
const savingConfig = ref(false)
const availableFields = ref<any[]>([])

// JOLT规范文件相关状态
const joltSpecContent = ref<any>(null)
const joltSpecLoading = ref(false)

// 计算属性
const mappedCount = computed(() => {
  return placeholders.value.filter(p => isMapped(p)).length
})

const mappingProgress = computed(() => {
  if (placeholders.value.length === 0) return 0
  return Math.round((mappedCount.value / placeholders.value.length) * 100)
})

// 表格列定义
const tableColumns = [
  { title: '图表ID', dataIndex: 'chartId', key: 'chartId', width: 120 },
  { title: '图表类型', dataIndex: 'chartType', key: 'chartType', width: 100 },
  { title: '图表名称', dataIndex: 'chartName', key: 'chartName', width: 150 },
  { title: '通用JSON模板', dataIndex: 'universalTemplate', key: 'universalTemplate', width: 180 },
  { title: 'JOLT转换规范', dataIndex: 'joltSpecFile', key: 'joltSpecFile', width: 150 },
  { title: '占位符数量', dataIndex: 'placeholderCount', key: 'placeholderCount', width: 100 },
  { title: '操作', key: 'actions', width: 280, fixed: 'right' }
]

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
  },
  onShowSizeChange: (current: number, size: number) => {
    paginationConfig.current = 1
    paginationConfig.pageSize = size
  }
})

// 计算属性
const mappingModalTitle = computed(() =>
  selectedRecord.value ? `配置映射 - ${selectedRecord.value.chartName}` : '配置映射'
)

const guideModalTitle = computed(() =>
  selectedRecord.value ? `配置指南 - ${selectedRecord.value.chartName}` : '配置指南'
)

// 配置映射独立窗口样式
const mappingWindowStyle = computed(() => ({
  top: `${mappingModalPosition.value.top}px`,
  left: `${mappingModalPosition.value.left}px`,
  zIndex: isDragging.value && dragTarget.value === 'mapping' ? 2000 : 1001
}))

// 配置指南独立窗口样式
const guideWindowStyle = computed(() => ({
  top: `${guideModalPosition.value.top}px`,
  left: `${guideModalPosition.value.left}px`,
  zIndex: isDragging.value && dragTarget.value === 'guide' ? 2000 : 1002
}))



// 事件处理方法
const onChartSelected = (chartInfo: any) => {
  console.log('📊 [映射管理] 图表选择变化:', chartInfo)
  Object.assign(selectedChart, chartInfo)
}

const refreshList = async () => {
  console.log('🔄 [映射管理] 刷新映射列表')
  loading.value = true

  try {
    const result = await placeholderMappingApi.getAllMappings()
    console.log('📋 [映射管理] API响应数据:', result)

    if (result && result.mappings) {
      mappingList.value = result.mappings
      paginationConfig.total = result.totalCount || result.mappings.length
      console.log('✅ [映射管理] 刷新成功，共', result.totalCount, '条记录')
    } else {
      console.warn('⚠️ [映射管理] API响应数据格式异常:', result)
      message.warning('数据格式异常')
    }
  } catch (error) {
    console.error('❌ [映射管理] 刷新失败:', error)
    message.error('刷新失败')
  } finally {
    loading.value = false
  }
}

const viewMapping = (record: MappingRecord) => {
  console.log('👁️ [映射管理] 查看映射:', record.chartId)
  selectedRecord.value = record
  message.info(`查看映射: ${record.chartName}`)
}

const configureMapping = async (record: MappingRecord) => {
  console.log('🔧 [映射管理] 配置映射:', record.chartId)
  selectedRecord.value = record

  // 加载占位符和现有映射配置
  await Promise.all([
    loadPlaceholders(record.chartId),
    loadExistingMappings(record.chartId),
    loadAvailableFields(),
    loadJoltSpecContent(record.chartId)
  ])

  mappingModalVisible.value = true
  console.log('✅ [映射管理] 配置映射模态框已打开')
}

const showConfigGuide = (record: MappingRecord) => {
  console.log('📖 [映射管理] 显示配置指南:', record.chartId)
  selectedRecord.value = record
  guideModalVisible.value = true
  console.log('✅ [映射管理] 配置指南模态框已打开')
}

const deleteMapping = (record: MappingRecord) => {
  console.log('🗑️ [映射管理] 删除映射:', record.chartId)
  message.info(`删除映射: ${record.chartName}`)
}

// 映射配置相关方法
const loadPlaceholders = async (chartId: string) => {
  placeholdersLoading.value = true
  try {
    const result = await placeholderMappingApi.getPlaceholders(chartId)
    placeholders.value = result.placeholders || []

    // 初始化映射配置对象
    placeholders.value.forEach(placeholder => {
      if (!mappingConfigs[placeholder]) {
        mappingConfigs[placeholder] = {
          fieldName: '',
          dataType: '',
          aggregationType: ''
        }
      }
    })

    console.log('📋 [映射管理] 加载占位符成功:', placeholders.value)
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
    Object.assign(mappingConfigs, result.mappings || {})
    console.log('📋 [映射管理] 加载现有映射成功:', mappingConfigs)
  } catch (error) {
    console.error('❌ [映射管理] 加载现有映射失败:', error)
  }
}

const loadAvailableFields = async () => {
  try {
    const result = await placeholderMappingApi.getAvailableFields()
    availableFields.value = result.fields || []
    console.log('📋 [映射管理] 加载可用字段成功:', availableFields.value)
  } catch (error) {
    console.error('❌ [映射管理] 加载可用字段失败:', error)
  }
}

// 加载JOLT规范文件内容
const loadJoltSpecContent = async (chartId: string) => {
  if (!chartId) return

  joltSpecLoading.value = true
  try {
    const content = await placeholderMappingApi.getJoltSpecContent(chartId)
    joltSpecContent.value = content
    console.log('✅ [映射管理] JOLT规范文件加载成功:', chartId)
  } catch (error) {
    console.error('❌ [映射管理] 加载JOLT规范文件失败:', error)
    message.error('加载JOLT规范文件失败')
    joltSpecContent.value = null
  } finally {
    joltSpecLoading.value = false
  }
}

const isMapped = (placeholder: string) => {
  return mappingConfigs[placeholder]?.fieldName
}

// 映射配置的getter和setter方法
const getMappingFieldName = (placeholder: string) => {
  return mappingConfigs[placeholder]?.fieldName || ''
}

const updateMappingFieldName = (placeholder: string, value: string) => {
  if (!mappingConfigs[placeholder]) {
    mappingConfigs[placeholder] = {}
  }
  mappingConfigs[placeholder].fieldName = value
  onMappingChange(placeholder)
}

const getMappingDataType = (placeholder: string) => {
  return mappingConfigs[placeholder]?.dataType || ''
}

const updateMappingDataType = (placeholder: string, value: string) => {
  if (!mappingConfigs[placeholder]) {
    mappingConfigs[placeholder] = {}
  }
  mappingConfigs[placeholder].dataType = value
  onMappingChange(placeholder)
}

const getMappingAggregationType = (placeholder: string) => {
  return mappingConfigs[placeholder]?.aggregationType || ''
}

const updateMappingAggregationType = (placeholder: string, value: string) => {
  if (!mappingConfigs[placeholder]) {
    mappingConfigs[placeholder] = {}
  }
  mappingConfigs[placeholder].aggregationType = value
  onMappingChange(placeholder)
}

const onMappingChange = (placeholder: string) => {
  console.log('🔄 [映射管理] 映射变化:', placeholder, mappingConfigs[placeholder])
}

const clearMapping = (placeholder: string) => {
  delete mappingConfigs[placeholder]
  console.log('🗑️ [映射管理] 清除映射:', placeholder)
}

const saveMappingConfig = async () => {
  if (!selectedRecord.value) return

  savingConfig.value = true
  try {
    // 过滤出已配置的映射
    const validMappings = Object.fromEntries(
      Object.entries(mappingConfigs).filter(([_, config]: [string, any]) => config.fieldName)
    )

    await placeholderMappingApi.configureMappings(selectedRecord.value.chartId, validMappings)
    message.success('映射配置保存成功')
    mappingModalVisible.value = false
    await refreshList()
  } catch (error) {
    console.error('❌ [映射管理] 保存映射配置失败:', error)
    message.error('保存映射配置失败')
  } finally {
    savingConfig.value = false
  }
}

// 拖拽功能实现
const startDrag = (event: MouseEvent, modalType: string) => {
  event.preventDefault()
  event.stopPropagation()

  isDragging.value = true
  dragTarget.value = modalType

  // 添加拖拽状态的视觉反馈
  document.body.classList.add('dragging')

  console.log('🖱️ [拖拽] 开始拖拽:', modalType, '初始位置:', event.clientX, event.clientY)

  const startX = event.clientX
  const startY = event.clientY

  const currentPosition = modalType === 'mapping' ? mappingModalPosition.value : guideModalPosition.value
  const startTop = currentPosition.top
  const startLeft = currentPosition.left

  console.log('📍 [拖拽] 模态框当前位置:', { startTop, startLeft })

  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging.value) return

    const deltaX = e.clientX - startX
    const deltaY = e.clientY - startY

    // 获取视口尺寸
    const viewportWidth = window.innerWidth
    const viewportHeight = window.innerHeight

    // 获取实际窗口尺寸
    const windowElement = document.querySelector(
      modalType === 'mapping' ? '.mapping-floating-window' : '.guide-floating-window'
    ) as HTMLElement

    let elementWidth = modalType === 'mapping' ? 1000 : 900  // 配置指南窗口优化为900px
    let elementHeight = 500

    if (windowElement) {
      const rect = windowElement.getBoundingClientRect()
      elementWidth = rect.width
      elementHeight = rect.height
    }

    // 计算新位置 - 允许更灵活的边界
    // 左边界：允许拖拽到负坐标，但保留至少100px可见区域
    const minLeft = -(elementWidth - 100)
    // 右边界：允许拖拽到视口右侧，但保留至少100px可见区域
    const maxLeft = viewportWidth - 100
    // 上边界：不能超出视口顶部
    const minTop = 0
    // 下边界：允许拖拽到视口底部，但保留至少50px可见区域
    const maxTop = viewportHeight - 50

    const newTop = Math.max(minTop, Math.min(maxTop, startTop + deltaY))
    const newLeft = Math.max(minLeft, Math.min(maxLeft, startLeft + deltaX))

    const newPosition = { top: newTop, left: newLeft }

    // 更新位置
    if (modalType === 'mapping') {
      mappingModalPosition.value = newPosition
    } else {
      guideModalPosition.value = newPosition
    }

    // 调试信息
    if (Math.abs(deltaX) % 30 === 0 || Math.abs(deltaY) % 30 === 0) {
      console.log('🔄 [拖拽] 位置更新:', {
        modalType,
        delta: { deltaX, deltaY },
        newPosition,
        boundaries: {
          viewport: { width: viewportWidth, height: viewportHeight },
          element: { width: elementWidth, height: elementHeight },
          limits: { minLeft, maxLeft, minTop, maxTop }
        }
      })
    }
  }

  const handleMouseUp = () => {
    const finalPosition = modalType === 'mapping' ? mappingModalPosition.value : guideModalPosition.value
    console.log('🛑 [拖拽] 结束拖拽:', modalType, '最终位置:', finalPosition)

    // 移除拖拽状态的视觉反馈
    document.body.classList.remove('dragging')

    isDragging.value = false
    dragTarget.value = ''
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 窗口点击置顶功能
const bringToFront = (windowType: string) => {
  console.log('🔝 [窗口] 置顶窗口:', windowType)

  const mappingWindow = document.querySelector('.mapping-floating-window')
  const guideWindow = document.querySelector('.guide-floating-window')

  if (windowType === 'mapping' && mappingWindow) {
    (mappingWindow as HTMLElement).style.zIndex = '1003'
    if (guideWindow) {
      (guideWindow as HTMLElement).style.zIndex = '1002'
    }
  } else if (windowType === 'guide' && guideWindow) {
    (guideWindow as HTMLElement).style.zIndex = '1003'
    if (mappingWindow) {
      (mappingWindow as HTMLElement).style.zIndex = '1001'
    }
  }
}

// 配置指南内容生成
const getOfficialExample = () => {
  if (!selectedRecord.value) return '请选择图表'

  const examples: Record<string, string> = {
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
}`,
    '仪表盘': `{
  "title": {
    "text": "\${chart_title}"
  },
  "series": [{
    "name": "仪表盘",
    "type": "gauge",
    "detail": { "formatter": "{value}%" },
    "data": [{
      "value": \${gauge_value},
      "name": "完成率"
    }]
  }]
}`
  }

  return examples[selectedRecord.value.chartType] || '暂无示例'
}

const getStructureDescription = () => {
  if (!selectedRecord.value) return []

  const descriptions: Record<string, string[]> = {
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
    ],
    '仪表盘': [
      'series: 仪表盘数据系列，type设置为"gauge"',
      'data: 仪表盘数据，包含value和name',
      'detail: 详细信息配置，如数值格式化',
      'min/max: 仪表盘数值范围设置'
    ]
  }

  return descriptions[selectedRecord.value.chartType] || ['暂无描述']
}

const getTransformationBefore = () => {
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

const getTransformationAfter = () => {
  if (!selectedRecord.value) return '请选择图表'

  const transformations: Record<string, string> = {
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
}`,
    '仪表盘': `{
  "title": { "text": "完成率" },
  "series": [{
    "type": "gauge",
    "data": [{ "value": 75, "name": "完成率" }]
  }]
}`
  }

  return transformations[selectedRecord.value.chartType] || '暂无转换示例'
}

const getPlaceholderList = () => {
  if (!selectedRecord.value) return []

  const placeholders: Record<string, Array<{name: string, description: string, color: string}>> = {
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
    ],
    '仪表盘': [
      { name: '${chart_title}', description: '图表标题', color: 'blue' },
      { name: '${gauge_value}', description: '仪表盘数值', color: 'gold' }
    ]
  }

  return placeholders[selectedRecord.value.chartType] || []
}





// 监听模态框状态变化
watch(mappingModalVisible, (newVal) => {
  console.log('👁️ [模态框] 配置映射模态框状态变化:', newVal)
  if (newVal) {
    console.log('📊 [模态框] 配置映射模态框已打开，当前记录:', selectedRecord.value)
  }
})

watch(guideModalVisible, (newVal) => {
  console.log('👁️ [模态框] 配置指南模态框状态变化:', newVal)
  if (newVal) {
    console.log('📖 [模态框] 配置指南模态框已打开，当前记录:', selectedRecord.value)
  }
})

// 生命周期
onMounted(async () => {
  console.log('🚀 [映射管理] 页面挂载完成')
  console.log('📊 [映射管理] 初始模态框位置:', {
    mapping: mappingModalPosition.value,
    guide: guideModalPosition.value
  })
  await refreshList()
})

console.log('🚀 [映射管理] 页面组件初始化')
</script>

<style scoped>
/* 页面基础样式 - 完全透明背景，不遮挡底层内容 */
.mapping-page {
  padding: 12px;
  background: none;
  min-height: auto;
  position: static;
}

/* 区域卡片样式 - 极高透明度，最小化视觉干扰 */
.selection-card,
.list-card {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.01);
  margin-bottom: 12px;
  backdrop-filter: blur(2px);
}

.selection-card :deep(.ant-card-body),
.list-card :deep(.ant-card-body) {
  background: transparent;
  padding: 12px;
}

.selection-card :deep(.ant-card-head),
.list-card :deep(.ant-card-head) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 标题样式 */
.section-title {
  font-weight: 600;
  color: #1890ff;
  font-size: 14px;
}

/* 图表选择器样式 */
.chart-selector {
  background: transparent;
}

/* 拖拽标题样式 */
.draggable-title {
  cursor: move;
  user-select: none;
  padding: 4px 0;
  font-weight: 600;
  color: #262626;
  transition: all 0.2s;
  border-radius: 4px;
  padding: 8px 12px;
  margin: -8px -12px;
}

.draggable-title:hover {
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.1);
}

.draggable-title:active {
  cursor: grabbing;
  background-color: rgba(24, 144, 255, 0.2);
  transform: scale(0.98);
}

/* 拖拽时的全局样式 */
body.dragging {
  cursor: grabbing !important;
  user-select: none !important;
}

body.dragging * {
  cursor: grabbing !important;
}

/* 配置映射独立浮动窗口样式 */
.mapping-floating-window {
  position: fixed;
  width: 1000px; /* 保持原有宽度 */
  max-height: 80vh;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8e8e8;
  overflow: hidden;
  z-index: 1001;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 配置指南独立浮动窗口样式 */
.guide-floating-window {
  position: fixed;
  width: 900px; /* 优化后的宽度 */
  max-height: 80vh;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8e8e8;
  overflow: hidden;
  z-index: 1002;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 窗口标题栏 */
.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: move;
  user-select: none;
  border-bottom: 1px solid #e8e8e8;
}

.window-header:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.window-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 14px;
}

.title-icon {
  margin-right: 8px;
  font-size: 16px;
}

.title-text {
  color: white;
}

.window-controls {
  display: flex;
  gap: 8px;
}

.control-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.close-btn:hover {
  background: #ff4757;
}

/* 窗口内容区域 */
.window-content {
  max-height: calc(80vh - 60px);
  overflow-y: auto;
  padding: 16px;
}

.guide-content {
  height: 100%;
}

.guide-panels {
  display: flex;
  gap: 16px;
  height: 100%;
}

.guide-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.panel-title {
  display: flex;
  align-items: center;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.panel-icon {
  margin-right: 8px;
  font-size: 16px;
}

.panel-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.section {
  margin-bottom: 20px;
}

.section:last-child {
  margin-bottom: 0;
}

.section h5 {
  margin: 0 0 8px 0;
  font-size: 13px;
  font-weight: 600;
  color: #1890ff;
}

.section h6 {
  margin: 12px 0 6px 0;
  font-size: 12px;
  font-weight: 600;
  color: #595959;
}

.code-container {
  margin: 8px 0;
}

.code-block {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 11px;
  line-height: 1.4;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.description-list {
  margin: 8px 0;
  padding-left: 16px;
}

.description-list li {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.placeholder-tags {
  margin: 8px 0;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
  max-height: 120px;
  overflow-y: auto;
}

/* 窗口底部操作栏样式 */
.window-footer {
  border-top: 1px solid #e8e8e8;
  padding: 12px 16px;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 100px;
  justify-content: center;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.cancel-btn:hover {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.primary-btn {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: 1px solid #1890ff;
}

.primary-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

.primary-btn:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  border-color: #d9d9d9;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 模态框样式 - 确保正确定位和无遮罩 */
.mapping-modal :deep(.ant-modal),
.guide-modal :deep(.ant-modal) {
  position: fixed !important;
  margin: 0 !important;
  transform: none !important;
  top: auto !important;
  left: auto !important;
  max-width: none !important;
  width: auto !important;
}

/* 确保模态框可以超出视口边界 */
.mapping-modal :deep(.ant-modal),
.guide-modal :deep(.ant-modal) {
  overflow: visible !important;
}

/* 模态框内容区域样式 */
.mapping-modal :deep(.ant-modal-content),
.guide-modal :deep(.ant-modal-content) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: visible;
}

/* 模态框包装器样式 */
.mapping-modal-wrap,
.guide-modal-wrap {
  position: static !important;
  pointer-events: none;
  z-index: auto !important;
}

.mapping-modal-wrap :deep(.ant-modal),
.guide-modal-wrap :deep(.ant-modal) {
  pointer-events: auto;
}

/* 配置映射模态框z-index */
.mapping-modal-wrap {
  z-index: 1001 !important;
}

.mapping-modal-wrap :deep(.ant-modal) {
  z-index: 1001 !important;
}

/* 完全隐藏遮罩层 */
.mapping-modal :deep(.ant-modal-mask),
.guide-modal :deep(.ant-modal-mask),
.mapping-modal-wrap :deep(.ant-modal-mask),
.guide-modal-wrap :deep(.ant-modal-mask) {
  display: none !important;
}

.mapping-modal :deep(.ant-modal-wrap),
.guide-modal :deep(.ant-modal-wrap) {
  position: static !important;
  overflow: visible !important;
  pointer-events: none;
}

.mapping-modal :deep(.ant-modal-wrap .ant-modal),
.guide-modal :deep(.ant-modal-wrap .ant-modal) {
  pointer-events: auto;
}

/* 模态框内容样式 */
.modal-content {
  max-height: 70vh;
  overflow-y: auto;
}

/* 映射配置样式 */
.mapping-config-section {
  margin-top: 16px;
}

.mapping-config-section h4 {
  margin: 16px 0 12px 0;
  color: #1890ff;
  font-weight: 600;
}

.mapping-header {
  background: #fafafa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  border: 1px solid #e8e8e8;
}

.mapping-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.mapping-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.mapping-item:hover {
  background-color: #f9f9f9;
}

.mapping-item:last-child {
  border-bottom: none;
}

.placeholder-info {
  display: flex;
  align-items: center;
}

.mapping-progress {
  margin-top: 16px;
  padding: 12px;
  background: #f6f8fa;
  border-radius: 4px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

/* 配置指南样式 */
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

/* JOLT规范文件内容样式 */
.jolt-spec-section {
  margin-top: 16px;
}

.jolt-spec-section h4 {
  margin-bottom: 12px;
  color: #1890ff;
  font-weight: 600;
}

.jolt-spec-content .code-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.jolt-spec-content .code-block {
  margin: 0;
  border: none;
  border-radius: 0;
  background: #fafafa;
}

.json-highlight {
  background: #fafafa;
  color: #333;
}

.no-jolt-spec {
  padding: 20px;
  text-align: center;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
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

.code-example,
.transformation-info .code-block {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 表格样式优化 */
:deep(.ant-table-thead > tr > th) {
  background: rgba(250, 250, 250, 0.8);
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(246, 255, 237, 0.8);
}

/* 按钮样式优化 */
:deep(.ant-btn-link) {
  padding: 2px 4px;
  height: auto;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .mapping-page {
    padding: 8px;
  }

  .selection-card,
  .list-card {
    margin-bottom: 8px;
  }

  .modal-content {
    max-height: 50vh;
  }

  /* 浮动窗口响应式 */
  .mapping-floating-window {
    width: 95vw;
    max-width: 900px;
  }

  .guide-floating-window {
    width: 95vw;
    max-width: 800px;
  }

  .guide-panels {
    flex-direction: column;
    gap: 12px;
  }

  .code-block {
    font-size: 10px;
    max-height: 150px;
  }
}

@media (max-width: 768px) {
  .mapping-floating-window,
  .guide-floating-window {
    width: 98vw;
    max-height: 90vh;
  }

  .window-header {
    padding: 8px 12px;
  }

  .title-text {
    font-size: 12px;
  }

  .panel-content {
    padding: 12px;
  }

  .code-block {
    font-size: 9px;
    padding: 8px;
  }
}
</style>