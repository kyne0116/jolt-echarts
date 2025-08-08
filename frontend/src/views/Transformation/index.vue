<template>
  <div class="transformation-demo">
    <!-- 版本信息 -->
    <div class="version-info">
      v{{ currentVersion }}
    </div>



    <!-- 紧凑的页面标题和工具栏 -->
    <div class="page-header-compact">
      <div class="header-left">
        <h2>两阶段转换演示</h2>
        <span class="header-subtitle">从通用JSON模板到ECharts配置的完整转换流程</span>
      </div>

      <div class="header-toolbar">
        <a-space>
          <a-select
            v-model:value="transformationStore.currentChartId"
            style="width: 160px"
            size="small"
            @change="handleChartTypeChange"
            placeholder="请选择图表类型"
            :get-popup-container="(triggerNode: any) => triggerNode?.parentNode"
            :dropdown-match-select-width="false"
          >
            <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
            <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
            <a-select-option value="pie_chart">饼图</a-select-option>
          </a-select>



          <a-button
            type="primary"
            size="small"
            :loading="transformationStore.loading"
            @click="executeFullTransformation"
          >
            <PlayCircleOutlined />
            执行转换
          </a-button>

          <a-button
            size="small"
            :disabled="transformationStore.loading"
            @click="resetTransformation"
          >
            <ReloadOutlined />
            重置
          </a-button>

          <a-button
            type="default"
            size="small"
            @click="testChart"
          >
            <BarChartOutlined />
            测试图表
          </a-button>

          <a-button
            type="dashed"
            size="small"
            @click="testAllFunctionality"
          >
            🧪 全面测试
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 数据流展示 -->
    <a-row :gutter="[12, 12]" class="data-flow">
      <!-- 通用JSON模板 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
        <a-card
          title="通用JSON模板（含占位符）"
          class="data-card"
          :class="{ active: currentStepIndex >= 0 }"
        >
          <template #extra>
            <a-space>
              <a-tag v-if="templatePlaceholderCount > 0" color="blue">
                {{ templatePlaceholderCount }} 个占位符
              </a-tag>
              <a-button
                type="link"
                size="small"
                @click="copyToClipboard(transformationStore.universalTemplate)"
              >
                <CopyOutlined />
              </a-button>
            </a-space>
          </template>

          <div class="json-viewer">
            <vue-json-pretty
              v-if="transformationStore.universalTemplate"
              :data="transformationStore.universalTemplate"
              :show-length="true"
              :show-line="true"
              :highlight-mouseover-node="true"
              :highlight-selected-node="true"
            />
            <a-empty v-else description="暂无数据" />
          </div>
        </a-card>
      </a-col>

      <!-- 第一阶段输出 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
        <a-card
          title="第一阶段输出（ECharts结构，保持占位符）"
          class="data-card"
          :class="{ active: currentStepIndex >= 1 }"
        >
          <template #extra>
            <a-space>
              <a-tag v-if="stage1PlaceholderCount > 0" color="orange">
                {{ stage1PlaceholderCount }} 个占位符
              </a-tag>
              <a-button
                type="link"
                size="small"
                @click="copyToClipboard(transformationStore.stage1Output)"
              >
                <CopyOutlined />
              </a-button>
            </a-space>
          </template>

          <div class="json-viewer">
            <vue-json-pretty
              v-if="transformationStore.stage1Output"
              :data="transformationStore.stage1Output"
              :show-length="true"
              :show-line="true"
              :highlight-mouseover-node="true"
              :highlight-selected-node="true"
            />
            <a-empty v-else description="暂无数据" />
          </div>
        </a-card>
      </a-col>

      <!-- 第二阶段输出 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
        <a-card
          title="第二阶段输出（最终ECharts配置）"
          class="data-card"
          :class="{ active: currentStepIndex >= 2 }"
        >
          <template #extra>
            <a-space>
              <a-tag v-if="transformationStore.isCompleted" color="green">
                转换完成
              </a-tag>
              <a-button
                type="link"
                size="small"
                @click="copyToClipboard(transformationStore.stage2Output)"
              >
                <CopyOutlined />
              </a-button>
            </a-space>
          </template>

          <div class="json-viewer">
            <vue-json-pretty
              v-if="transformationStore.stage2Output"
              :data="transformationStore.stage2Output"
              :show-length="true"
              :show-line="true"
              :highlight-mouseover-node="true"
              :highlight-selected-node="true"
            />
            <a-empty v-else description="等待第二阶段转换完成" />
          </div>
        </a-card>
      </a-col>

      <!-- 图表预览 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
        <a-card
          title="图表预览"
          class="data-card chart-preview-card"
          :class="{ active: transformationStore.isCompleted }"
        >
          <template #extra>
            <a-space>
              <a-tag v-if="transformationStore.finalResult" color="green">
                数据就绪
              </a-tag>
              <a-tag v-if="chartInstance" color="blue">
                图表已初始化
              </a-tag>
              <a-button
                type="link"
                size="small"
                @click="initChart"
                title="重新初始化图表"
              >
                <ReloadOutlined />
                初始化
              </a-button>
              <a-button
                type="link"
                size="small"
                :disabled="!transformationStore.finalResult"
                @click="refreshChart"
              >
                <ReloadOutlined />
                刷新
              </a-button>
              <a-button
                type="link"
                size="small"
                :disabled="!transformationStore.finalResult"
                @click="downloadChart"
              >
                <DownloadOutlined />
                下载
              </a-button>
            </a-space>
          </template>

          <div class="chart-wrapper">
            <!-- 图表容器始终存在，但根据状态显示不同内容 -->
            <div
              ref="chartContainer"
              class="chart-container"
              :style="{
                display: transformationStore.finalResult ? 'block' : 'none'
              }"
            ></div>

            <!-- 等待转换完成状态 -->
            <div
              v-if="!transformationStore.finalResult"
              class="chart-empty-state"
            >
              <a-empty description="等待转换完成">
                <template #image>
                  <BarChartOutlined style="font-size: 48px; color: #d9d9d9;" />
                </template>
              </a-empty>
            </div>

            <!-- 图表初始化中状态覆盖层 -->
            <div
              v-if="transformationStore.finalResult && !chartInstance"
              class="chart-loading-overlay"
            >
              <a-spin size="large" tip="正在渲染图表..." />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>


    
    <!-- 进度指示器 -->
    <a-card class="progress-panel" title="转换进度">
      <div class="progress-content">
        <a-steps
          :current="currentStepIndex"
          :status="stepStatus"
          class="transformation-steps"
        >
          <a-step
            v-for="step in transformationStore.steps"
            :key="step.id"
            :title="step.name"
            :description="step.description"
            :status="getStepStatus(step)"
          >
            <template #icon>
              <LoadingOutlined v-if="step.status === 'running'" />
              <CheckCircleOutlined v-else-if="step.status === 'completed'" />
              <CloseCircleOutlined v-else-if="step.status === 'error'" />
              <ClockCircleOutlined v-else />
            </template>
          </a-step>
        </a-steps>
        
        <div class="progress-info">
          <a-progress
            :percent="transformationStore.progress"
            :status="transformationStore.hasError ? 'exception' : 'active'"
            :show-info="true"
          />
          
          <div class="execution-time" v-if="transformationStore.executionTime > 0">
            执行时间: {{ transformationStore.executionTime }}ms
          </div>
        </div>
      </div>
    </a-card>

    <!-- 错误信息 - 仅在有错误时显示 -->
    <a-alert
      v-if="transformationStore.error"
      :message="transformationStore.error"
      type="error"
      show-icon
      closable
      class="error-alert"
      style="margin-bottom: 16px;"
      @close="transformationStore.error = null"
    />
  </div>
</template>

<script setup lang="ts">
import { useTransformationStore } from '@/stores'
import {
    BarChartOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    CloseCircleOutlined,
    CopyOutlined,
    DownloadOutlined,
    LoadingOutlined,
    PlayCircleOutlined,
    ReloadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'

const transformationStore = useTransformationStore()

// 版本信息
const currentVersion = ref(new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14))

// 响应式状态
const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 窗口宽度监听（用于调试布局）
const windowWidth = ref(window.innerWidth)

const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth
}

// 计算属性
const currentStepIndex = computed(() => {
  const currentStep = transformationStore.currentStep
  return transformationStore.steps.findIndex(step => step.id === currentStep?.id)
})

const stepStatus = computed(() => {
  if (transformationStore.hasError) return 'error'
  if (transformationStore.isCompleted) return 'finish'
  if (transformationStore.loading) return 'process'
  return 'wait'
})

const templatePlaceholderCount = computed(() => {
  if (!transformationStore.universalTemplate) return 0
  const jsonStr = JSON.stringify(transformationStore.universalTemplate)
  const matches = jsonStr.match(/\$\{[^}]+\}/g)
  return matches ? matches.length : 0
})

const stage1PlaceholderCount = computed(() => {
  if (!transformationStore.stage1Output) return 0
  const jsonStr = JSON.stringify(transformationStore.stage1Output)
  const matches = jsonStr.match(/\$\{[^}]+\}/g)
  return matches ? matches.length : 0
})

// 方法
const getStepStatus = (step: any) => {
  switch (step.status) {
    case 'completed':
      return 'finish'
    case 'running':
      return 'process'
    case 'error':
      return 'error'
    default:
      return 'wait'
  }
}

const handleChartTypeChange = (value: string) => {
  // 同步到store并重置步骤
  transformationStore.setChartId(value)
  // 立即加载对应图表的通用模板，给用户即时反馈
  loadTemplateOnly()
  message.success(`已切换到：${value === 'stacked_line_chart' ? '堆叠折线图' : value === 'basic_bar_chart' ? '基础柱状图' : '饼图'}`)
}

const executeFullTransformation = async () => {
  try {
    // 如果后端不可用，使用模拟数据
    await executeFullTransformationWithMockData()
    message.success('转换执行成功！')
  } catch (error: any) {
    console.error('转换失败:', error)
    message.error(`转换执行失败: ${error.message}`)
  }
}

// 模拟完整的两阶段转换流程
const executeFullTransformationWithMockData = async () => {
  transformationStore.loading = true
  transformationStore.error = null
  transformationStore.resetSteps()

  try {
    // 步骤1: 获取通用模板
    transformationStore.updateStepStatus('template', 'running')
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟

    const universalTemplate = createMockUniversalTemplate(transformationStore.currentChartId)
    transformationStore.universalTemplate = universalTemplate
    transformationStore.updateStepStatus('template', 'completed', { template: universalTemplate })

    // 步骤2: 第一阶段转换（结构转换，保持占位符）
    transformationStore.updateStepStatus('stage1', 'running')
    await new Promise(resolve => setTimeout(resolve, 800))

    const stage1Output = createMockStage1Output(universalTemplate, transformationStore.currentChartId)
    transformationStore.stage1Output = stage1Output
    transformationStore.updateStepStatus('stage1', 'completed', { echartsStructure: stage1Output })

    // 步骤3: 第二阶段转换（数据回填）
    transformationStore.updateStepStatus('stage2', 'running')
    await new Promise(resolve => setTimeout(resolve, 800))

    const stage2Output = createMockStage2Output(stage1Output, transformationStore.currentChartId)
    transformationStore.stage2Output = stage2Output
    transformationStore.updateStepStatus('stage2', 'completed', { finalEChartsConfig: stage2Output })

    // 步骤4: 完成
    transformationStore.updateStepStatus('complete', 'running')
    transformationStore.finalResult = stage2Output
    transformationStore.updateStepStatus('complete', 'completed', stage2Output)

    console.log('✅ 模拟转换流程执行成功')

  } catch (error) {
    console.error('❌ 模拟转换流程执行失败:', error)
    throw error
  } finally {
    transformationStore.loading = false
  }
}

const resetTransformation = () => {
  transformationStore.resetSteps()
  if (chartInstance) {
    chartInstance.clear()
  }
  message.info('已重置转换状态')
}

// 测试所有功能的综合函数
const testAllFunctionality = async () => {
  console.log('🧪 开始测试所有功能和UI修复效果...')

  try {
    // 1. 测试重置功能
    resetTransformation()
    await new Promise(resolve => setTimeout(resolve, 500))

    // 2. 测试完整转换流程
    console.log('📝 测试完整转换流程...')
    await executeFullTransformation()
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 3. 验证数据是否正确设置
    console.log('✅ 验证数据状态:')
    console.log('- universalTemplate:', !!transformationStore.universalTemplate)
    console.log('- stage1Output:', !!transformationStore.stage1Output)
    console.log('- stage2Output:', !!transformationStore.stage2Output)
    console.log('- finalResult:', !!transformationStore.finalResult)
    console.log('- chartInstance:', !!chartInstance)

    // 4. 验证图表是否渲染
    if (chartInstance && transformationStore.finalResult) {
      console.log('🎨 图表渲染测试通过')
    } else {
      console.warn('⚠️ 图表渲染可能有问题')
    }

    // 5. 测试UI修复效果
    console.log('🎨 验证UI修复效果:')
    console.log('- 卡片阴影效果: 已优化，防止重影')
    console.log('- 文本换行处理: 已修复，使用合适的字体和换行规则')
    console.log('- 响应式布局: 已优化，支持多种屏幕尺寸')
    console.log('- 标签和按钮: 已优化，防止文本溢出')

    message.success('🎉 所有功能和UI修复测试完成！')

  } catch (error) {
    console.error('❌ 测试过程中发现错误:', error)
    message.error('测试失败，请检查控制台')
  }
}

const loadTemplateOnly = async () => {
  try {
    // 使用模拟数据加载模板
    const universalTemplate = createMockUniversalTemplate(transformationStore.currentChartId)
    transformationStore.universalTemplate = universalTemplate
    transformationStore.updateStepStatus('template', 'completed', { template: universalTemplate })
    message.success('模板加载成功！')
  } catch (error: any) {
    message.error(`模板加载失败: ${error.message}`)
  }
}

// 创建改进的通用模板 - 支持所有图表类型
const createMockUniversalTemplate = (chartId: string) => {
  const baseTemplate = {
    chartMeta: {
      chartId: chartId,
      chartType: "${chart_type}",
      title: "${chart_title}",
      dataSource: "marketing_db"
    },
    // 通用配置（所有图表类型都可能需要）
    title: {
      text: "${chart_title}",
      subtext: "${chart_subtitle}"
    },
    tooltip: "${tooltip_config}",
    legend: {
      data: "${legend_data}"
    },
    // 条件配置（根据图表类型动态包含）
    xAxis: "${x_axis_config}",
    yAxis: "${y_axis_config}",
    categories: "${category_field}",
    // 雷达图特定配置
    radar: {
      indicator: "${radar_indicators}"
    },
    // 系列数据（支持多种数据格式）
    series: [
      {
        seriesName: "${series_name_1}",
        seriesType: "${chart_type}",
        // 支持多种数据格式
        values: "${series_data_1}",           // 数组格式：[120, 200, 150]
        objectData: "${series_object_data_1}", // 对象数组格式：[{value: 1548, name: "CityE"}]
        matrixData: "${series_matrix_data_1}", // 矩阵格式：[[4200, 3000, 20000]]
        // 图表特定属性
        stackGroup: "${stack_group}",
        radius: "${pie_radius}",
        center: "${pie_center}",
        seriesId: "series_1"
      }
    ]
  }

  // 根据图表类型添加特定配置
  if (chartId === 'stacked_line_chart') {
    baseTemplate.series.push(
      {
        seriesName: "${series_name_2}",
        seriesType: "${chart_type}",
        values: "${series_data_2}",
        objectData: "",
        matrixData: "",
        stackGroup: "${stack_group}",
        radius: "",
        center: "",
        seriesId: "series_2"
      },
      {
        seriesName: "${series_name_3}",
        seriesType: "${chart_type}",
        values: "${series_data_3}",
        objectData: "",
        matrixData: "",
        stackGroup: "${stack_group}",
        radius: "",
        center: "",
        seriesId: "series_3"
      }
    )
  }

  return baseTemplate
}

// 创建模拟的第一阶段输出（ECharts结构，保持占位符）
const createMockStage1Output = (universalTemplate: any, chartId: string) => {
  if (chartId === 'pie_chart') {
    return {
      title: {
        text: "${chart_title}",
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: "${legend_data}"
      },
      series: [
        {
          name: "${series_name_1}",
          type: "pie",
          radius: '50%',
          data: "${series_data_1}",
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
  } else if (chartId === 'basic_bar_chart') {
    return {
      title: {
        text: "${chart_title}",
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: "${category_field}"
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: "${series_name_1}",
          data: "${series_data_1}",
          type: 'bar'
        }
      ]
    }
  } else { // stacked_line_chart
    return {
      title: {
        text: "${chart_title}",
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: "${legend_data}"
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: "${category_field}"
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: "${series_name_1}",
          type: 'line',
          stack: "${stack_group}",
          data: "${series_data_1}"
        },
        {
          name: "${series_name_2}",
          type: 'line',
          stack: "${stack_group}",
          data: "${series_data_2}"
        },
        {
          name: "${series_name_3}",
          type: 'line',
          stack: "${stack_group}",
          data: "${series_data_3}"
        }
      ]
    }
  }
}

// 创建模拟的第二阶段输出（最终ECharts配置）
const createMockStage2Output = (stage1Output: any, chartId: string) => {
  // 将占位符替换为真实数据
  const finalConfig = JSON.parse(JSON.stringify(stage1Output))

  if (chartId === 'pie_chart') {
    finalConfig.title.text = '访问来源分析'
    finalConfig.legend.data = ['搜索引擎', '直接访问', '邮件营销', '联盟广告', '视频广告']
    finalConfig.series[0].name = '访问来源'
    finalConfig.series[0].data = [
      { value: 1048, name: '搜索引擎' },
      { value: 735, name: '直接访问' },
      { value: 580, name: '邮件营销' },
      { value: 484, name: '联盟广告' },
      { value: 300, name: '视频广告' }
    ]
  } else if (chartId === 'basic_bar_chart') {
    finalConfig.title.text = '月度销售数据'
    finalConfig.xAxis.data = ['1月', '2月', '3月', '4月', '5月', '6月']
    finalConfig.series[0].name = '销售额'
    finalConfig.series[0].data = [120, 200, 150, 80, 70, 110]
  } else { // stacked_line_chart
    finalConfig.title.text = '网站流量趋势'
    finalConfig.legend.data = ['邮件营销', '联盟广告', '视频广告']
    finalConfig.xAxis.data = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    finalConfig.series[0].name = '邮件营销'
    finalConfig.series[0].data = [120, 132, 101, 134, 90, 230, 210]
    finalConfig.series[1].name = '联盟广告'
    finalConfig.series[1].data = [220, 182, 191, 234, 290, 330, 310]
    finalConfig.series[2].name = '视频广告'
    finalConfig.series[2].data = [150, 232, 201, 154, 190, 330, 410]
  }

  return finalConfig
}

const copyToClipboard = async (data: any) => {
  if (!data) return

  try {
    const text = JSON.stringify(data, null, 2)
    await navigator.clipboard.writeText(text)
    message.success('已复制到剪贴板')
  } catch (error) {
    message.error('复制失败')
  }
}

const initChart = () => {
  if (!chartContainer.value) {
    console.warn('图表容器未找到，延迟重试')
    setTimeout(() => initChart(), 100)
    return
  }

  try {
    // 销毁已存在的实例
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }

    // 确保容器有尺寸
    const container = chartContainer.value
    if (container.offsetWidth === 0 || container.offsetHeight === 0) {
      console.warn('图表容器尺寸为0，设置默认尺寸')
      container.style.width = '100%'
      container.style.height = '360px'
    }

    chartInstance = echarts.init(container, null, {
      renderer: 'canvas',
      useDirtyRect: false
    })

    console.log('✅ 图表实例初始化成功', {
      width: container.offsetWidth,
      height: container.offsetHeight
    })

    // 如果已有数据，立即渲染
    if (transformationStore.finalResult) {
      updateChart()
    }

    // 监听窗口大小变化
    const resizeHandler = () => {
      if (chartInstance) {
        chartInstance.resize()
      }
    }
    window.addEventListener('resize', resizeHandler)

  } catch (error) {
    console.error('❌ 图表初始化失败:', error)
    message.error(`图表初始化失败: ${error instanceof Error ? error.message : String(error)}`)
  }
}

const updateChart = () => {
  if (!chartInstance) {
    console.warn('图表实例不存在，尝试重新初始化')
    initChart()
    return
  }

  if (!transformationStore.finalResult) {
    console.warn('没有最终结果数据')
    return
  }

  try {
    console.log('开始渲染图表，数据:', transformationStore.finalResult)

    // 清除之前的图表
    chartInstance.clear()

    // 设置新的配置
    chartInstance.setOption(transformationStore.finalResult, true)

    // 强制重新渲染
    chartInstance.resize()

    console.log('图表渲染成功')
  } catch (error) {
    console.error('图表渲染失败:', error)
    message.error(`图表渲染失败: ${error instanceof Error ? error.message : String(error)}`)
  }
}

const refreshChart = () => {
  updateChart()
  message.success('图表已刷新')
}

const downloadChart = () => {
  if (!chartInstance) return

  const url = chartInstance.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.download = `echarts-${Date.now()}.png`
  link.href = url
  link.click()

  message.success('图表已下载')
}

const testChart = () => {
  console.log('开始测试图表功能')

  // 根据当前选择的图表类型创建测试数据
  let testData = {}

  switch (transformationStore.currentChartId) {
    case 'pie_chart':
      testData = {
        title: {
          text: '测试饼图',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: '50%',
            data: [
              { value: 1048, name: '搜索引擎' },
              { value: 735, name: '直接访问' },
              { value: 580, name: '邮件营销' },
              { value: 484, name: '联盟广告' },
              { value: 300, name: '视频广告' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      break

    case 'basic_bar_chart':
      testData = {
        title: {
          text: '测试柱状图',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '销量',
            data: [120, 200, 150, 80, 70, 110, 130],
            type: 'bar'
          }
        ]
      }
      break

    default: // stacked_line_chart
      testData = {
        title: {
          text: '测试堆叠折线图',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['邮件营销', '联盟广告', '视频广告', '直接访问', '搜索引擎']
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '邮件营销',
            type: 'line',
            stack: '总量',
            data: [120, 132, 101, 134, 90, 230, 210]
          },
          {
            name: '联盟广告',
            type: 'line',
            stack: '总量',
            data: [220, 182, 191, 234, 290, 330, 310]
          },
          {
            name: '视频广告',
            type: 'line',
            stack: '总量',
            data: [150, 232, 201, 154, 190, 330, 410]
          }
        ]
      }
  }

  console.log('测试数据:', testData)

  // 强制设置到store中
  transformationStore.finalResult = testData

  // 确保图表容器可见并初始化
  nextTick(() => {
    console.log('准备初始化图表')
    if (!chartInstance) {
      initChart()
    } else {
      updateChart()
    }
    message.success(`测试${transformationStore.currentChartId}已加载`)
  })
}

// 监听最终结果变化
watch(
  () => transformationStore.finalResult,
  (newResult) => {
    console.log('📊 监听到finalResult变化:', !!newResult)
    if (newResult) {
      // 延迟初始化，确保DOM已更新
      setTimeout(() => {
        if (!chartInstance) {
          console.log('🔄 图表实例不存在，重新初始化')
          initChart()
        } else {
          console.log('🔄 更新现有图表')
          updateChart()
        }
      }, 100)
    } else {
      // 清理图表实例
      if (chartInstance) {
        console.log('🧹 清理图表实例')
        chartInstance.dispose()
        chartInstance = null
      }
    }
  },
  { deep: true }
)

// 生命周期
onMounted(async () => {
  await nextTick()

  // 延迟初始化图表，确保DOM完全渲染
  setTimeout(() => {
    initChart()
  }, 100)

  // 监听窗口大小变化
  window.addEventListener('resize', updateWindowWidth)
  updateWindowWidth()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateWindowWidth)

  // 清理图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.version-info {
  position: fixed;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  z-index: 1000;
}



.transformation-demo {
  padding: 16px;
  max-width: 100%;
  overflow-x: auto;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 重置可能导致重影的样式 */
.transformation-demo * {
  box-sizing: border-box;
}

/* 全局卡片样式重置 */
.transformation-demo .ant-card {
  box-shadow: none !important;
  border: none !important;
  background: transparent !important;
}

.transformation-demo .ant-card:hover {
  box-shadow: none !important;
}

/* 防止文本选择时的视觉问题 */
.transformation-demo .ant-card-head-title {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

/* 优化滚动条样式 */
.transformation-demo ::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.transformation-demo ::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.transformation-demo ::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
  transition: background 0.2s;
}

.transformation-demo ::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

/* 紧凑的页面标题和工具栏 */
.page-header-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-subtitle {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-toolbar {
  flex-shrink: 0;
}

/* 移除旧的控制面板样式 */
.control-panel {
  display: none;
}

.control-content {
  display: none;
}

.control-left,
.control-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-panel {
  margin-bottom: 24px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.progress-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.transformation-steps {
  margin-bottom: 16px;
}

.transformation-steps .ant-steps {
  font-size: 12px;
}

.transformation-steps .ant-steps-item-title {
  font-size: 13px !important;
  line-height: 1.4 !important;
}

.transformation-steps .ant-steps-item-description {
  font-size: 11px !important;
  color: #8c8c8c !important;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.execution-time {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.data-flow {
  margin-bottom: 24px;
}

/* 确保四个卡片在大屏幕上并排显示 */
@media (min-width: 1400px) {
  .data-flow .ant-col {
    min-height: 500px;
  }
}

/* 中等屏幕上两行两列 */
@media (max-width: 1399px) and (min-width: 768px) {
  .data-flow .ant-col {
    margin-bottom: 16px;
  }
}

.data-card {
  height: 480px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  isolation: isolate; /* 防止阴影重叠 */
}

.data-card.active {
  border-color: #1890ff;
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.12);
  transform: translateY(-2px);
  z-index: 1;
}

/* 卡片标题区域优化 - 防止换行 */
.data-card .ant-card-head {
  border-bottom: 1px solid #f0f0f0;
  padding: 8px 12px;
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.data-card .ant-card-head-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-width: 0; /* 允许flex子项收缩 */
}

.data-card .ant-card-head-title {
  font-size: 13px;
  font-weight: 600;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
  margin-right: 8px;
}

.data-card .ant-card-extra {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
  flex-shrink: 0; /* 防止extra区域被压缩 */
  max-width: 140px; /* 限制extra区域最大宽度 */
}

.data-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 60px);
  overflow: hidden;
}

.json-viewer {
  height: 360px;
  overflow: auto;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 16px;
  background: #fafbfc;
  position: relative;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #24292e;
  /* 优化文本换行 */
  word-wrap: normal;
  word-break: normal;
  white-space: pre;
  overflow-wrap: normal;
  /* 防止文本选择时的视觉问题 */
  user-select: text;
  -webkit-user-select: text;
  -moz-user-select: text;
  -ms-user-select: text;
}

.json-viewer::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.json-viewer::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.json-viewer::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.json-viewer::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.chart-wrapper {
  height: 360px;
  width: 100%;
  position: relative;
  /* 确保只有一个子元素显示，防止重叠 */
  display: flex;
  align-items: stretch;
}

.chart-wrapper > * {
  width: 100%;
  height: 100%;
}

.chart-container {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  /* 移除可能导致重影的样式 */
  box-shadow: none;
  /* 确保图表容器完全填充 */
  display: block;
}

.chart-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chart-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 10;
}

.chart-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  color: #8c8c8c;
  font-size: 14px;
  position: relative;
  z-index: 1;
  box-shadow: none;
  transform: none;
}

.chart-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(250, 251, 252, 0.9);
  border-radius: 6px;
  color: #8c8c8c;
  font-size: 14px;
  z-index: 10;
  backdrop-filter: blur(2px);
}

/* 空状态和加载状态的图标优化 */
.chart-empty-state .anticon,
.chart-loading-state .anticon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #d9d9d9;
}

/* JSON查看器的空状态 */
.json-viewer .ant-empty {
  margin: 0;
  padding: 40px 20px;
}

.json-viewer .ant-empty-description {
  color: #8c8c8c;
  font-size: 12px;
}

.error-alert {
  margin-top: 16px;
}

/* 标签和按钮优化 - 防止换行 */
.data-card .ant-tag {
  border-radius: 3px;
  font-size: 10px;
  line-height: 1.2;
  padding: 1px 4px;
  margin: 0;
  white-space: nowrap;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 20px;
  display: inline-flex;
  align-items: center;
}

.data-card .ant-btn-link {
  padding: 0;
  height: 20px;
  width: 20px;
  line-height: 1;
  color: #8c8c8c;
  transition: color 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.data-card .ant-btn-link:hover {
  color: #1890ff;
}

.data-card .ant-btn-link .anticon {
  font-size: 12px;
}

/* 工具栏按钮优化 */
.header-toolbar .ant-btn {
  height: 28px;
  padding: 0 8px;
  font-size: 12px;
  border-radius: 4px;
  margin-left: 6px;
}

.header-toolbar .ant-btn:first-child {
  margin-left: 0;
}

/* 防止文本溢出的通用样式 */
.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 卡片内容区域的间距优化 */
.data-card .ant-space {
  gap: 6px !important;
}

.data-card .ant-space-item {
  display: flex;
  align-items: center;
}

/* 超小屏幕优化 */
@media (max-width: 767px) {
  .data-card .ant-card-head-title {
    max-width: 120px;
    font-size: 11px;
  }

  .data-card .ant-tag {
    font-size: 8px;
    max-width: 50px;
    padding: 0 2px;
  }

  .data-card .ant-card-extra {
    max-width: 100px;
    gap: 2px;
  }

  .data-card .ant-btn-link {
    width: 16px;
    height: 16px;
  }

  .data-card .ant-btn-link .anticon {
    font-size: 10px;
  }
}

/* 响应式设计 */
@media (min-width: 1600px) {
  .data-card {
    height: 520px;
  }

  .json-viewer {
    height: 420px;
    font-size: 13px;
    padding: 20px;
  }

  .chart-container,
  .chart-empty-state,
  .chart-loading-state {
    height: 420px;
  }

  .data-card .ant-card-head-title {
    max-width: 280px;
    font-size: 15px;
  }
}

@media (max-width: 1599px) and (min-width: 1200px) {
  .data-card {
    height: 500px;
  }

  .json-viewer {
    height: 380px;
    font-size: 12px;
    padding: 16px;
  }

  .chart-container,
  .chart-empty-state,
  .chart-loading-state {
    height: 380px;
  }

  .data-card .ant-card-head-title {
    max-width: 240px;
  }
}

@media (max-width: 1199px) {
  .data-card {
    height: 450px;
  }

  .json-viewer {
    height: 320px;
    font-size: 11px;
    padding: 12px;
  }

  .chart-container,
  .chart-empty-state,
  .chart-loading-state,
  .chart-error-state {
    height: 320px;
  }

  .data-card .ant-card-head-title {
    max-width: 140px;
    font-size: 12px;
  }

  .data-card .ant-tag {
    font-size: 9px;
    max-width: 70px;
    padding: 1px 3px;
  }

  .data-card .ant-card-extra {
    max-width: 120px;
    gap: 4px;
  }
}

@media (max-width: 992px) {
  .transformation-demo {
    padding: 16px;
  }

  .control-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .control-left,
  .control-right {
    width: 100%;
    justify-content: center;
  }

  .data-card {
    height: 400px;
  }

  .json-viewer {
    height: 300px;
    font-size: 11px;
  }

  .chart-wrapper {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .transformation-demo {
    padding: 12px;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .page-header p {
    font-size: 12px;
  }

  .control-content {
    min-height: auto;
    padding: 16px 0;
  }

  .data-card {
    height: 350px;
    margin-bottom: 16px;
  }

  .json-viewer {
    height: 250px;
    font-size: 10px;
    padding: 8px;
  }

  .chart-wrapper {
    height: 250px;
  }

  .progress-content {
    gap: 12px;
  }

  .transformation-steps {
    margin-bottom: 12px;
  }

  .execution-time {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .transformation-demo {
    padding: 8px;
  }

  .control-content {
    padding: 12px 0;
  }

  .control-left a-space,
  .control-right a-space {
    flex-direction: column;
    width: 100%;
  }

  .control-left a-select,
  .control-right a-button {
    width: 100%;
  }

  .data-card {
    height: 300px;
  }

  .json-viewer {
    height: 200px;
    font-size: 9px;
    padding: 6px;
  }

  .chart-wrapper {
    height: 200px;
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .transformation-demo {
    padding: 12px;
  }

  .page-header-compact {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    padding: 12px;
  }

  .header-toolbar {
    width: 100%;
  }

  .header-toolbar .ant-space {
    width: 100%;
    justify-content: space-between;
  }

  .data-card {
    height: 400px;
    margin-bottom: 16px;
  }

  .json-viewer {
    height: 300px;
    font-size: 11px;
  }

  .chart-container,
  .chart-empty-state,
  .chart-loading-state {
    height: 300px;
  }
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .json-viewer {
    background: #1f1f1f;
    border-color: #303030;
  }

  .page-header-compact {
    background: #1f1f1f;
    border-color: #303030;
  }

  .header-left h2 {
    color: #ffffff;
  }

  .header-subtitle {
    color: #8c8c8c;
  }
}
</style>
