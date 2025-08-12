<template>
  <div class="chart-selector">
    <a-card title="图表选择" size="small" class="selector-card">
      <div class="selector-content">
        <a-row :gutter="12">
          <a-col :span="12">
            <div class="selector-item">
              <label class="selector-label">图表分类</label>
              <a-select
                v-model:value="selectedTemplateType"
                placeholder="请选择图表分类"
                @change="handleTemplateTypeChange"
                style="width: 100%"
                size="small"
                :loading="categoriesLoading"
              >
                <a-select-option
                  v-for="category in directoryCategories"
                  :key="category"
                  :value="category"
                >
                  {{ category }}
                </a-select-option>
              </a-select>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="selector-item">
              <label class="selector-label">具体图表</label>
              <a-select
                v-model:value="selectedChartFile"
                placeholder="请选择具体图表"
                @change="handleChartFileChange"
                style="width: 100%"
                size="small"
                :disabled="!selectedTemplateType"
                :loading="chartsLoading"
              >
                <a-select-option
                  v-for="chart in availableCharts"
                  :key="chart.id"
                  :value="chart.filePath"
                >
                  {{ chart.name }}
                </a-select-option>
              </a-select>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-card>


  </div>
</template>

<script setup lang="ts">
import { twoStageApi } from '@/api';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref, watch } from 'vue';

// Props
interface Props {
  autoLoad?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoLoad: true
})

// Emits
const emit = defineEmits<{
  chartSelected: [chartInfo: {
    chartId: string
    templateType: string
    chartFile: string
    joltSpecFile: string
  }]
  chartCleared: []
}>()

// 响应式数据
const categoriesLoading = ref(false)
const chartsLoading = ref(false)

const selectedTemplateType = ref<string>('')
const selectedChartFile = ref<string>('')
const availableCharts = ref<Array<{id: string, name: string, filePath: string}>>([])

const echartsDirectoryStructure = ref<Record<string, Array<{fileName: string, displayName: string, filePath: string}>>>({})
const directoryCategories = ref<string[]>([])

// 计算属性
const currentChartId = computed(() => {
  if (!selectedTemplateType.value || !selectedChartFile.value) return ''

  // 优先从availableCharts中查找对应的图表ID
  const selectedChart = availableCharts.value.find(chart => chart.filePath === selectedChartFile.value)
  if (selectedChart && selectedChart.id) {
    return selectedChart.id
  }

  // 回退方案：从文件路径生成ID
  const fileName = selectedChartFile.value.split('/').pop()?.replace('.json', '') || ''
  return fileName.toLowerCase().replace(/[^a-z0-9]/g, '_')
})

const joltSpecFile = computed(() => {
  if (!currentChartId.value) return ''
  
  // 根据图表ID映射到JOLT规范文件
  const chartToSpecMapping: Record<string, string> = {
    'basic_line_chart': 'line-chart-placeholder.json',
    'smooth_line_chart': 'line-chart-placeholder.json',
    'stacked_line_chart': 'line-chart-stacked.json',
    'basic_bar_chart': 'bar-chart-placeholder.json',
    'stacked_bar_chart': 'bar-chart-placeholder.json',
    'basic_pie_chart': 'pie-chart-placeholder.json',
    'doughnut_chart': 'pie-chart-placeholder.json',
    'basic_radar_chart': 'radar-chart-placeholder.json',
    'basic_gauge_chart': 'gauge-chart-placeholder.json'
  }
  
  return chartToSpecMapping[currentChartId.value] || 'line-chart-placeholder.json'
})

// 监听图表选择变化
watch([currentChartId, selectedTemplateType, selectedChartFile, joltSpecFile],
  ([chartId, templateType, chartFile, joltSpec]) => {
    if (chartId && templateType && chartFile) {
      // 获取选中图表的中文名称
      const selectedChart = availableCharts.value.find(chart => chart.filePath === chartFile)
      const chartName = selectedChart ? selectedChart.name : getChartDisplayName(chartFile)

      emit('chartSelected', {
        chartId,
        templateType,
        chartFile,
        chartName,
        joltSpecFile: joltSpec
      })
    } else {
      emit('chartCleared')
    }
  }
)

// 获取图表显示名称的辅助方法
const getChartDisplayName = (chartFile: string) => {
  if (!chartFile) return '未知图表'
  const fileName = chartFile.split('/').pop()?.replace('.json', '') || ''
  return fileName.replace(/[-_]/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// 方法
const loadEChartsDirectory = async () => {
  categoriesLoading.value = true
  try {
    console.log('📂 [图表选择器] 开始加载ECharts目录结构...')
    const response = await twoStageApi.scanEChartsDirectory()
    console.log('📦 [图表选择器] API响应数据:', response)

    if (response && response.directoryStructure) {
      echartsDirectoryStructure.value = response.directoryStructure
      directoryCategories.value = Object.keys(response.directoryStructure)

      console.log('✅ [图表选择器] ECharts目录结构加载成功:', {
        categories: directoryCategories.value,
        totalFiles: response.totalFiles
      })

      if (props.autoLoad) {
        await initializeDefaultSelections()
      }

      message.success(`已加载 ${directoryCategories.value.length} 个图表分类`)
    } else {
      throw new Error('目录结构数据格式不正确')
    }
  } catch (error: any) {
    console.error('❌ [图表选择器] 加载目录结构失败:', error)
    
    // 回退到默认分类
    directoryCategories.value = ['折线图', '柱状图', '饼图', '雷达图', '仪表盘']
    message.warning('已切换到本地默认分类')
    
    if (props.autoLoad) {
      await initializeDefaultSelections()
    }
  } finally {
    categoriesLoading.value = false
  }
}

const initializeDefaultSelections = async () => {
  try {
    console.log('🎯 [图表选择器] 开始初始化默认选择...')
    
    if (directoryCategories.value.length === 0) {
      console.warn('⚠️ [图表选择器] 没有可用的图表分类')
      return
    }

    // 优先选择折线图分类
    const preferredCategory = directoryCategories.value.find(cat => 
      cat.includes('折线') || cat.includes('line')
    ) || directoryCategories.value[0]

    selectedTemplateType.value = preferredCategory
    await handleTemplateTypeChange(preferredCategory)

  } catch (error) {
    console.error('❌ [图表选择器] 初始化默认选择失败:', error)
  }
}

const handleTemplateTypeChange = async (categoryName: string) => {
  console.log('🔄 [图表选择器] 图表分类切换:', categoryName)

  chartsLoading.value = true
  try {
    // 从目录结构中获取对应分类的文件列表
    const categoryFiles = echartsDirectoryStructure.value[categoryName] || []

    // 转换为下拉框需要的格式，确保包含完整的图表信息
    availableCharts.value = categoryFiles.map(file => {
      // 从文件路径生成图表ID
      const fileName = file.fileName.replace('.json', '')
      const chartId = fileName.toLowerCase().replace(/[^a-z0-9]/g, '_')

      return {
        id: chartId,
        name: file.displayName || fileName,
        filePath: file.filePath,
        fileName: file.fileName
      }
    })

    // 清空之前的选择
    selectedChartFile.value = ''

    if (availableCharts.value.length === 0) {
      console.warn(`⚠️ [图表选择器] 分类 ${categoryName} 下暂无可用的图表文件`)

      // 生成默认的图表选项，参考两阶段转换的逻辑
      const defaultCharts = generateDefaultChartsForCategory(categoryName)
      availableCharts.value = defaultCharts
    }

    // 自动选择第一个图表
    if (availableCharts.value.length > 0 && props.autoLoad) {
      const firstChart = availableCharts.value[0]
      selectedChartFile.value = firstChart.filePath
    }

    console.log('📋 [图表选择器] 可用图表文件:', availableCharts.value)
    message.success(`已切换到 ${categoryName}，可选择 ${availableCharts.value.length} 个图表`)

  } catch (error: any) {
    console.error('❌ [图表选择器] 图表分类切换失败:', error)
    message.error(`图表分类切换失败: ${error.message || '未知错误'}`)

    // 错误恢复
    selectedTemplateType.value = ''
    selectedChartFile.value = ''
    availableCharts.value = []
  } finally {
    chartsLoading.value = false
  }
}

// 为分类生成默认图表选项
const generateDefaultChartsForCategory = (categoryName: string) => {
  const defaultChartMappings: Record<string, Array<{id: string, name: string, fileName: string}>> = {
    '折线图': [
      { id: 'basic_line_chart', name: '基础折线图', fileName: 'basic-line-chart.json' },
      { id: 'smooth_line_chart', name: '平滑折线图', fileName: 'smooth-line-chart.json' },
      { id: 'stacked_line_chart', name: '堆叠折线图', fileName: 'stacked-line-chart.json' }
    ],
    '柱状图': [
      { id: 'basic_bar_chart', name: '基础柱状图', fileName: 'basic-bar-chart.json' },
      { id: 'stacked_bar_chart', name: '堆叠柱状图', fileName: 'stacked-bar-chart.json' }
    ],
    '饼图': [
      { id: 'basic_pie_chart', name: '基础饼图', fileName: 'basic-pie-chart.json' },
      { id: 'doughnut_chart', name: '圆环图', fileName: 'doughnut-chart.json' }
    ],
    '雷达图': [
      { id: 'basic_radar_chart', name: '基础雷达图', fileName: 'basic-radar-chart.json' }
    ],
    '仪表盘': [
      { id: 'basic_gauge_chart', name: '基础仪表盘', fileName: 'basic-gauge-chart.json' }
    ]
  }

  const charts = defaultChartMappings[categoryName] || [
    { id: 'default_chart', name: `默认${categoryName}`, fileName: 'default-chart.json' }
  ]

  return charts.map(chart => ({
    id: chart.id,
    name: chart.name,
    filePath: `${categoryName}/${chart.fileName}`,
    fileName: chart.fileName
  }))
}

const handleChartFileChange = (filePath: string) => {
  console.log('📊 [图表选择器] 具体图表切换:', filePath)
  selectedChartFile.value = filePath
}

// 公开方法
const clearSelection = () => {
  selectedTemplateType.value = ''
  selectedChartFile.value = ''
  availableCharts.value = []
}

const setSelection = (templateType: string, chartFile: string) => {
  selectedTemplateType.value = templateType
  handleTemplateTypeChange(templateType).then(() => {
    selectedChartFile.value = chartFile
  })
}

// 暴露方法给父组件
defineExpose({
  clearSelection,
  setSelection,
  currentChartId,
  selectedTemplateType,
  selectedChartFile
})

// 生命周期
onMounted(async () => {
  if (props.autoLoad) {
    await loadEChartsDirectory()
  }
})
</script>

<style scoped>
.chart-selector {
  width: 100%;
}

.selector-card,
.info-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.selector-content {
  padding: 8px 0;
}

.selector-item {
  margin-bottom: 8px;
}

.selector-label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #262626;
  font-size: 12px;
}

.info-content {
  padding: 8px 0;
}

.info-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80px;
}
</style>
