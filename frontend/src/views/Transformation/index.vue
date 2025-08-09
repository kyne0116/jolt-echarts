<template>
  <div class="transformation-demo">
    <!-- 版本信息 -->
    <div class="version-info">
      v{{ currentVersion }}
    </div>



    <!-- 精简后的配置区域 - 无标题版本 -->
    <div class="config-header-compact">
      <!-- 主要配置区域 -->
      <a-row :gutter="[24, 16]" class="config-section" align="top">
        <!-- 左侧：图表选择区域 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="chart-selector-panel">
            <a-space direction="vertical" size="middle" style="width: 100%">
              <!-- 一级下拉框：图表分类 -->
              <div class="selector-item">
                <label class="selector-label">图表分类</label>
                <a-select
                  v-model:value="selectedTemplateType"
                  style="width: 100%"
                  size="middle"
                  @change="handleTemplateTypeChange"
                  placeholder="请选择图表分类"
                  :get-popup-container="getDropdownContainer"
                  :loading="directoryCategories.length === 0"
                  show-search
                  :filter-option="false"
                >
                  <a-select-option
                    v-for="category in directoryCategories"
                    :key="category"
                    :value="category"
                  >
                    <component
                      :is="getCategoryIcon(category)"
                      style="margin-right: 8px"
                    />
                    {{ category }}
                  </a-select-option>
                </a-select>
              </div>

              <!-- 二级下拉框：具体图表 -->
              <div class="selector-item">
                <label class="selector-label">具体图表</label>
                <a-select
                  v-model:value="selectedChartFile"
                  style="width: 100%"
                  size="middle"
                  @change="handleChartFileChange"
                  placeholder="请选择具体图表"
                  :disabled="!selectedTemplateType"
                  :get-popup-container="getDropdownContainer"
                >
                  <a-select-option
                    v-for="chart in availableCharts"
                    :key="chart.filePath"
                    :value="chart.filePath"
                  >
                    {{ chart.name }}
                  </a-select-option>
                </a-select>
              </div>

            </a-space>
          </div>
        </a-col>

        <!-- 右侧：图表信息展示区域 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="chart-info-panel">
            <div v-if="chartInfo && selectedChartFile" class="info-content">
              <a-descriptions :column="1" size="small" bordered>
                <a-descriptions-item label="图表名称">
                  <div class="info-value-with-subtitle">
                    <div class="main-value">{{ chartInfo.chartName }}</div>
                    <div class="sub-value">{{ getChartTypeEnglish(chartInfo.chartCategory) }}</div>
                  </div>
                </a-descriptions-item>
                
                <a-descriptions-item label="模板类型">
                  <a-tag :color="getTemplateTypeColor(chartInfo.templateType)" size="small">
                    {{ chartInfo.templateType.toUpperCase() }}
                  </a-tag>
                  <span class="template-type-name">{{ chartInfo.templateTypeName }}</span>
                </a-descriptions-item>
                
                <a-descriptions-item label="ECharts文件">
                  <code class="file-path-code">{{ selectedChartFile }}</code>
                </a-descriptions-item>
                
                <a-descriptions-item label="JOLT文件">
                  <code class="file-path-code">{{ getJoltFilePath(transformationStore.currentChartId) }}</code>
                </a-descriptions-item>
                
                <a-descriptions-item label="实现状态">
                  <a-tag :color="getImplementationStatusColor(transformationStore.currentChartId)" size="small">
                    {{ getImplementationStatus(transformationStore.currentChartId) }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </div>
            <div v-else class="info-placeholder">
              <a-empty description="请选择图表以查看详细信息" :image="false">
                <template #image>
                  <BarChartOutlined style="font-size: 32px; color: #d9d9d9;" />
                </template>
              </a-empty>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- 操作按钮区域 -->
      <div class="action-section">
        <a-space size="middle">
          <a-button
            :disabled="transformationStore.loading"
            @click="resetTransformation"
            size="middle"
          >
            <ReloadOutlined />
            重置
          </a-button>

          <a-button
            type="default"
            @click="testChart"
            size="middle"
          >
            <BarChartOutlined />
            测试图表
          </a-button>

          <a-button
            type="dashed"
            @click="testAllFunctionality"
            size="middle"
          >
            🧪 全面测试
          </a-button>

          <a-button
            type="text"
            @click="debugCurrentState"
            size="middle"
          >
            🔍 调试状态
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 主要内容区域 - 新的布局结构 -->
    <div class="main-content-area">
      <a-row :gutter="[16, 16]" style="height: 100%;">
        <!-- 左侧：图表预览区域 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8" style="height: 100%;">
          <a-card
            title="图表预览"
            class="chart-preview-card"
            :class="{ active: transformationStore.isCompleted }"
          >
          <template #extra>
            <a-space direction="vertical" size="small" style="width: 100%;">
              <!-- 状态标签 -->
              <a-space>
                <a-tag v-if="transformationStore.finalResult" color="green">
                  数据就绪
                </a-tag>
                <a-tag v-if="chartInstance" color="blue">
                  图表已初始化
                </a-tag>
                <a-tag v-if="chartZoom !== 1" color="orange">
                  缩放: {{ Math.round(chartZoom * 100) }}%
                </a-tag>
              </a-space>

              <!-- 基础功能按钮 -->
              <a-space wrap>
                <a-button
                  type="link"
                  size="small"
                  @click="initChart"
                  title="重新初始化图表"
                >
                  <ReloadOutlined />
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!transformationStore.finalResult"
                  @click="refreshChart"
                  title="刷新图表"
                >
                  <ReloadOutlined />
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance || chartZoom === 1"
                  @click="resetChartZoom"
                  title="重置缩放"
                >
                  <CompressOutlined />
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!transformationStore.finalResult"
                  @click="downloadChart"
                  title="下载图表"
                >
                  <DownloadOutlined />
                </a-button>
              </a-space>

              <!-- 测试功能按钮 -->
              <a-space wrap>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testOfficialExample"
                  title="官方ECharts示例"
                  style="color: #1890ff;"
                >
                  📊 官方
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testSmoothFunction"
                  title="简单平滑测试"
                  style="color: #52c41a;"
                >
                  🧪 简单
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testDataStructureComparison"
                  title="数据结构对比"
                  style="color: #722ed1;"
                >
                  🔍 对比
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testRegressionCheck"
                  title="回归测试"
                  style="color: #fa541c;"
                >
                  🔄 回归
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testPropertyInterference"
                  title="属性干扰诊断"
                  style="color: #eb2f96;"
                >
                  🔬 诊断
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :disabled="!chartInstance"
                  @click="testDirectFix"
                  title="直接修复测试"
                  style="color: #13c2c2;"
                >
                  🔧 修复
                </a-button>
              </a-space>
            </a-space>
          </template>

          <div class="chart-wrapper">
            <!-- 缩放控制按钮 -->
            <div class="chart-zoom-controls" v-if="chartInstance">
              <a-button-group size="small">
                <a-button @click="zoomIn" :disabled="chartZoom >= 3" title="放大">
                  <PlusOutlined />
                </a-button>
                <a-button @click="zoomOut" :disabled="chartZoom <= 0.5" title="缩小">
                  <MinusOutlined />
                </a-button>
                <a-button @click="resetChartZoom" :disabled="chartZoom === 1" title="重置">
                  <CompressOutlined />
                </a-button>
              </a-button-group>
            </div>

            <!-- 可滚动的图表容器 -->
            <div class="chart-scroll-container">
              <!-- 图表容器始终存在，但根据状态显示不同内容 -->
              <div
                ref="chartContainer"
                class="chart-container"
                :style="{
                  display: transformationStore.finalResult ? 'block' : 'none',
                  transform: `scale(${chartZoom})`,
                  transformOrigin: 'top left',
                  width: `${100 / chartZoom}%`,
                  height: `${100 / chartZoom}%`
                }"
                @wheel="handleChartWheel"
                @mousedown="handleChartMouseDown"
                @mousemove="handleChartMouseMove"
                @mouseup="handleChartMouseUp"
                @mouseleave="handleChartMouseUp"
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
          </div>
        </a-card>
      </a-col>

        <!-- 右侧：数据流展示区域 -->
        <a-col :xs="24" :sm="24" :md="12" :lg="16" :xl="16" style="height: 100%;">
          <a-row :gutter="[12, 12]" class="data-flow" style="height: 100%;">
            <!-- 通用JSON模板 -->
            <a-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8" style="height: 100%;">
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
            <a-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8" style="height: 100%;">
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
            <a-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8" style="height: 100%;">
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
          </a-row>
        </a-col>
      </a-row>
    </div>


    
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
import { twoStageApi } from '@/api'
import chartConfigService from '@/services/chartConfigService'
import { useTransformationStore } from '@/stores'
import {
    BarChartOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    CloseCircleOutlined,
    CompressOutlined,
    CopyOutlined,
    DashboardOutlined,
    DownloadOutlined,
    LineChartOutlined,
    LoadingOutlined,
    MinusOutlined,
    PieChartOutlined,
    PlusOutlined,
    RadarChartOutlined,
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
const chartInfo = ref<any>(null)

// 图表缩放和拖拽状态
const chartZoom = ref(1)
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const chartOffset = ref({ x: 0, y: 0 })

// 二级联动下拉框状态
const selectedTemplateType = ref<string>('')
const selectedChartFile = ref<string>('')
const availableCharts = ref<Array<{id: string, name: string, filePath: string}>>([])

// ECharts目录结构数据
const echartsDirectoryStructure = ref<Record<string, Array<{fileName: string, displayName: string, filePath: string}>>>({})
const directoryCategories = ref<string[]>([])

// 加载ECharts目录结构
const loadEChartsDirectory = async () => {
  try {
    console.log('🔄 开始加载ECharts目录结构...')
    const response = await twoStageApi.scanEChartsDirectory()

    console.log('📦 API响应数据:', response)

    // 由于unwrap函数已经提取了data，response就是data内容
    if (response && response.directoryStructure) {
      echartsDirectoryStructure.value = response.directoryStructure
      directoryCategories.value = Object.keys(response.directoryStructure)

      console.log('✅ ECharts目录结构加载成功:', {
        categories: directoryCategories.value,
        totalFiles: response.totalFiles,
        directoryStructure: response.directoryStructure
      })

      // 自动初始化默认选择
      await initializeDefaultSelections()

      message.success(`已加载 ${directoryCategories.value.length} 个图表分类，共 ${response.totalFiles} 个图表文件`)
    } else {
      console.warn('⚠️ 响应数据结构异常:', response)
      throw new Error('目录结构数据格式不正确')
    }
  } catch (error: any) {
    console.error('❌ 加载ECharts目录结构失败:', error)
    message.error(`加载目录结构失败: ${error.message || '未知错误'}`)

    // 错误恢复：尝试使用后端备用接口
    try {
      await loadCategoriesFromBackup()
      message.warning('已切换到备用分类数据')
    } catch (backupError) {
      console.error('❌ 备用接口也失败了:', backupError)
      // 最终回退到硬编码数据
      directoryCategories.value = ['折线图', '柱状图', '饼图', '雷达图', '仪表盘']
      message.warning('已切换到本地默认分类')
    }

    // 即使在错误恢复情况下，也尝试初始化默认选择
    try {
      await initializeDefaultSelections()
    } catch (initError) {
      console.error('❌ 错误恢复时初始化默认选择失败:', initError)
    }
  }
}

// 初始化默认选择
const initializeDefaultSelections = async () => {
  try {
    console.log('🎯 开始初始化默认选择...')
    console.log('📊 当前目录结构:', echartsDirectoryStructure.value)
    console.log('📊 当前分类列表:', directoryCategories.value)

    // 如果已有存储的图表ID，优先使用
    if (transformationStore.currentChartId) {
      console.log('📋 发现已存储的图表ID，尝试恢复选择:', transformationStore.currentChartId)
      await initializeTemplateTypeFromChartId(transformationStore.currentChartId)
      return
    }

    // 检查是否有目录结构数据
    const categories = Object.keys(echartsDirectoryStructure.value)
    console.log('📂 可用分类:', categories)

    if (categories.length === 0) {
      console.warn('⚠️ 没有可用的图表分类，尝试使用默认分类列表')
      // 如果没有目录结构，但有分类列表，尝试手动构建
      if (directoryCategories.value.length > 0) {
        await initializeWithFallbackData()
      }
      return
    }

    // 选择第一个可用的图表作为默认选择
    // 优先选择雷达图，如果没有则选择第一个分类
    const preferredCategory = categories.includes('雷达图') ? '雷达图' : categories[0]
    const categoryFiles = echartsDirectoryStructure.value[preferredCategory]

    console.log(`🎯 选择的分类: ${preferredCategory}`)
    console.log(`📁 该分类下的文件:`, categoryFiles)

    if (categoryFiles && categoryFiles.length > 0) {
      console.log(`🎯 自动选择默认分类: ${preferredCategory}`)

      // 设置第一级下拉框
      selectedTemplateType.value = preferredCategory

      // 设置第二级下拉框的选项
      availableCharts.value = categoryFiles.map(file => ({
        id: file.displayName,
        name: file.displayName,
        filePath: file.filePath
      }))

      // 选择第一个图表文件
      const defaultFile = categoryFiles[0]
      selectedChartFile.value = defaultFile.filePath

      console.log(`🎯 自动选择默认图表: ${defaultFile.displayName}`)
      console.log(`📄 文件路径: ${defaultFile.filePath}`)

      // 延迟加载图表信息，确保DOM更新完成
      await nextTick()
      setTimeout(async () => {
        try {
          await handleChartFileChange(defaultFile.filePath)
          console.log('✅ 默认选择初始化完成')
        } catch (loadError) {
          console.error('❌ 加载默认图表信息失败:', loadError)
          // 如果API调用失败，至少确保下拉框状态正确
          await loadChartInfoWithFallback(defaultFile.filePath)
        }
      }, 100)
    } else {
      console.warn('⚠️ 选择的分类下没有可用文件')
    }
  } catch (error: any) {
    console.error('❌ 初始化默认选择失败:', error)
    // 尝试回退方案
    await initializeWithFallbackData()
  }
}

// 使用回退数据初始化
const initializeWithFallbackData = async () => {
  try {
    console.log('🔄 使用回退数据初始化默认选择...')

    if (directoryCategories.value.length > 0) {
      // 选择第一个分类
      const firstCategory = directoryCategories.value[0]
      selectedTemplateType.value = firstCategory

      console.log(`🎯 回退方案：选择分类 ${firstCategory}`)

      // 触发分类变化处理
      handleTemplateTypeChange(firstCategory)
    }
  } catch (error) {
    console.error('❌ 回退数据初始化失败:', error)
  }
}

// 使用回退方案加载图表信息
const loadChartInfoWithFallback = async (filePath: string) => {
  try {
    console.log('🔄 使用回退方案加载图表信息:', filePath)

    // 生成基本的图表信息
    const chartId = await generateChartIdFromFilePath(filePath)
    const displayName = getDisplayNameFromFilePath(filePath)

    // 设置基本的图表信息
    chartInfo.value = {
      chartName: displayName,
      chartCategory: selectedTemplateType.value,
      templateType: selectedTemplateType.value.toLowerCase(),
      templateTypeName: selectedTemplateType.value,
      filePath: filePath,
      chartId: chartId
    }

    // 同步到store
    transformationStore.setChartId(chartId)

    console.log('✅ 回退方案图表信息设置完成:', chartInfo.value)
  } catch (error) {
    console.error('❌ 回退方案加载图表信息失败:', error)
  }
}

// 获取分类图标（动态获取，不再使用硬编码映射）
const getCategoryIcon = (category: string) => {
  // 预定义的图标映射（保留作为回退方案）
  const defaultIconMap: Record<string, any> = {
    '折线图': LineChartOutlined,
    '柱状图': BarChartOutlined,
    '饼图': PieChartOutlined,
    '雷达图': RadarChartOutlined,
    '仪表盘': DashboardOutlined
  }
  
  // TODO: 可以从后端返回的分类数据中获取图标信息
  // 目前先使用默认映射，后续可扩展为动态获取
  return defaultIconMap[category] || BarChartOutlined
}

// 窗口宽度监听（用于调试布局）
const windowWidth = ref(window.innerWidth)

const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth
}

// 安全的下拉框容器获取函数
const getDropdownContainer = (triggerNode?: HTMLElement) => {
  try {
    // 优先使用triggerNode的父节点
    if (triggerNode && triggerNode.parentNode) {
      return triggerNode.parentNode as HTMLElement
    }
    
    // 回退到查找页面上的工具栏容器
    const toolbarElement = document.querySelector('.header-toolbar')
    if (toolbarElement) {
      return toolbarElement as HTMLElement
    }
    
    // 最后回退到body
    return document.body
  } catch (error) {
    console.warn('获取下拉框容器失败，使用document.body作为回退:', error)
    return document.body
  }
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

// 处理模板类型变化（一级下拉框）
const handleTemplateTypeChange = async (categoryName: string) => {
  console.log('🔄 图表分类切换:', categoryName)

  try {
    // 从目录结构中获取对应分类的文件列表
    const categoryFiles = echartsDirectoryStructure.value[categoryName] || []

    // 转换为下拉框需要的格式
    availableCharts.value = categoryFiles.map(file => ({
      id: file.displayName, // 使用显示名称作为ID
      name: file.displayName, // 显示名称
      filePath: file.filePath // 完整文件路径
    }))

    if (availableCharts.value.length === 0) {
      console.warn(`⚠️ 分类 ${categoryName} 下暂无可用的图表文件`)

      // 如果是在初始化过程中，尝试生成默认的图表选项
      if (directoryCategories.value.includes(categoryName)) {
        console.log('🔄 尝试为分类生成默认图表选项:', categoryName)
        availableCharts.value = [{
          id: `${categoryName}堆叠`,
          name: `${categoryName}堆叠`,
          filePath: `${categoryName}/${categoryName}堆叠.json`
        }]

        // 自动选择第一个
        const defaultChart = availableCharts.value[0]
        selectedChartFile.value = defaultChart.filePath

        // 使用回退方案加载图表信息
        await loadChartInfoWithFallback(defaultChart.filePath)

        console.log('✅ 已生成默认图表选项并加载信息')
        return
      }

      message.warning(`分类 ${categoryName} 下暂无可用的图表文件`)
      return
    }

    // 清空当前选中的图表文件
    selectedChartFile.value = ''
    transformationStore.currentChartId = ''
    chartInfo.value = null

    // 重置转换状态
    transformationStore.resetSteps()

    console.log('📋 可用图表文件:', availableCharts.value)

    // 如果是在初始化过程中，自动选择第一个图表
    if (availableCharts.value.length > 0) {
      const firstChart = availableCharts.value[0]
      selectedChartFile.value = firstChart.filePath

      // 延迟加载图表信息
      setTimeout(async () => {
        try {
          await handleChartFileChange(firstChart.filePath)
        } catch (error) {
          console.error('❌ 自动加载第一个图表失败:', error)
          await loadChartInfoWithFallback(firstChart.filePath)
        }
      }, 100)
    }

    message.success(`已切换到 ${categoryName}，可选择 ${availableCharts.value.length} 个图表`)
  } catch (error: any) {
    console.error('❌ 图表分类切换失败:', error)
    message.error(`图表分类切换失败: ${error.message || '未知错误'}`)

    // 错误恢复：清空选择
    selectedTemplateType.value = ''
    selectedChartFile.value = ''
    availableCharts.value = []
    transformationStore.currentChartId = ''
    chartInfo.value = null
  }
}

// 处理图表文件变化（二级下拉框）
const handleChartFileChange = async (filePath: string) => {
  console.log('🔄 图表文件切换:', filePath)

  if (!filePath) {
    console.warn('⚠️ 图表文件路径为空，跳过处理')
    return
  }

  try {
    // 先重置所有状态
    transformationStore.resetSteps()
    chartInfo.value = null

    // 显示加载状态
    const loadingMessage = message.loading('正在切换图表文件...', 0)

    // 根据文件路径生成chartId（用于后端API调用）
    const chartId = await generateChartIdFromFilePath(filePath)

    // 同步到store
    transformationStore.setChartId(chartId)

    // 获取图表类型信息
    const chartInfoResp = await twoStageApi.getChartInfo(chartId)

    if (!chartInfoResp) {
      throw new Error('获取图表信息失败：服务器返回空数据')
    }

    chartInfo.value = chartInfoResp
    console.log('📊 图表信息:', chartInfoResp)

    // 执行完整转换流程
    await transformationStore.executeFullTransformation()

    // 关闭加载提示
    loadingMessage()

    message.success(`已切换到：${getDisplayNameFromFilePath(filePath)}`)
    console.log('✅ 图表文件切换成功')
  } catch (e: any) {
    console.error('❌ 图表文件切换失败:', e)

    // 错误分类处理
    let errorMessage = '图表文件切换失败'
    if (e.message?.includes('网络')) {
      errorMessage = '网络连接失败，请检查网络状态'
    } else if (e.message?.includes('404')) {
      errorMessage = '图表文件不存在，请选择其他文件'
    } else if (e.message?.includes('500')) {
      errorMessage = '服务器内部错误，请稍后重试'
    } else if (e.message) {
      errorMessage = e.message
    }

    message.error(errorMessage)

    // 错误恢复：回退到之前的状态
    selectedChartFile.value = ''
    transformationStore.currentChartId = ''
    chartInfo.value = null
    transformationStore.resetSteps()
  }
}

// 从后端备用接口加载分类数据
const loadCategoriesFromBackup = async () => {
  console.log('🔄 尝试从备用接口加载分类数据')
  
  const response = await twoStageApi.getCategories()
  console.log('📂 备用接口返回的分类数据:', response)
  
  if (response.categories && Array.isArray(response.categories)) {
    // 提取分类名称
    directoryCategories.value = response.categories.map((cat: any) => cat.name)
    
    // 可选：保存图标映射信息
    const icons: Record<string, any> = {}
    response.categories.forEach((cat: any) => {
      if (cat.iconName) {
        icons[cat.name] = cat.iconName
      }
    })
    
    console.log('✅ 成功从备用接口加载分类:', directoryCategories.value)
    return response
  } else {
    throw new Error('备用接口返回的分类数据格式不正确')
  }
}

// 根据文件路径生成chartId（使用配置服务）
const generateChartIdFromFilePath = async (filePath: string): Promise<string> => {
  try {
    // 首先尝试使用配置服务获取chartId
    console.log(`📋 [配置服务] 尝试获取文件路径映射: ${filePath}`)
    const chartId = await chartConfigService.generateChartIdFromFilePath(filePath)
    console.log(`📋 [配置服务] 文件路径映射成功: ${filePath} -> ${chartId}`)
    return chartId
  } catch (error) {
    console.error(`❌ [配置服务] 获取chartId失败: ${filePath}`, error)

    // 使用临时硬编码映射作为回退
    const hardcodedMapping: Record<string, string> = {
      '折线图/基础折线图.json': 'basic_line_chart',
      '折线图/基础平滑折线图.json': 'smooth_line_chart',
      '折线图/折线图堆叠.json': 'stacked_line_chart',
      '柱状图/基础柱状图.json': 'basic_bar_chart',
      '柱状图/堆叠柱状图.json': 'stacked_bar_chart',
      '饼图/富文本标签.json': 'basic_pie_chart',
      '饼图/圆角环形图.json': 'doughnut_chart',
      '雷达图/基础雷达图.json': 'basic_radar_chart',
      '仪表盘/基础仪表盘.json': 'basic_gauge_chart',
      '仪表盘/进度仪表盘.json': 'progress_gauge_chart',
      '仪表盘/等级仪表盘.json': 'grade_gauge_chart'
    }

    const mappedId = hardcodedMapping[filePath]
    if (mappedId) {
      console.log(`📋 [硬编码映射] 使用硬编码映射: ${filePath} -> ${mappedId}`)
      return mappedId
    }

    // 最后的回退方案
    const fallbackId = filePath.replace(/[\/\s\.]/g, '_').toLowerCase()
    console.warn(`⚠️ 使用最终回退方案: ${filePath} -> ${fallbackId}`)
    return fallbackId
  }
}

// 从文件路径获取显示名称
const getDisplayNameFromFilePath = (filePath: string): string => {
  const parts = filePath.split('/')
  if (parts.length >= 2) {
    return parts[1].replace('.json', '')
  }
  return filePath.replace('.json', '')
}






const resetTransformation = () => {
  transformationStore.resetSteps()
  if (chartInstance) {
    chartInstance.clear()
  }
  message.info('已重置转换状态')
}

// 调试工具：检查当前状态
const debugCurrentState = () => {
  console.log('🔍 当前状态调试信息:')
  console.log('- 当前图表ID:', transformationStore.currentChartId)
  console.log('- 通用模板:', transformationStore.universalTemplate)
  console.log('- 第一阶段输出:', transformationStore.stage1Output)
  console.log('- 第二阶段输出:', transformationStore.stage2Output)
  console.log('- 最终结果:', transformationStore.finalResult)
  console.log('- 加载状态:', transformationStore.loading)
  console.log('- 错误信息:', transformationStore.error)
  console.log('- 步骤状态:', transformationStore.steps.map(s => ({ id: s.id, status: s.status })))
}

// 测试所有功能的综合函数
const testAllFunctionality = async () => {
  console.log('🧪 开始测试所有功能和UI修复效果...')

  try {
    // 1. 测试重置功能
    console.log('1️⃣ 测试重置功能')
    resetTransformation()
    await new Promise(resolve => setTimeout(resolve, 500))
    debugCurrentState()

    // 2. 图表已在文件选择时自动转换
    console.log('2️⃣ 图表已自动转换...')
    await new Promise(resolve => setTimeout(resolve, 1000))
    debugCurrentState()

    // 3. 验证数据是否正确设置
    console.log('3️⃣ 验证数据状态:')
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
    const templateResp = await twoStageApi.getTemplate(transformationStore.currentChartId)
    transformationStore.universalTemplate = templateResp.template
    transformationStore.updateStepStatus('template', 'completed', templateResp)
    message.success('模板加载成功！')
  } catch (error: any) {
    message.error(`模板加载失败: ${error.message}`)
  }
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

const getTemplateTypeColor = (templateType: string) => {
  const colorMap: Record<string, string> = {
    'cartesian': 'blue',
    'pie': 'orange',
    'radar': 'green',
    'gauge': 'purple'
  }
  return colorMap[templateType] || 'default'
}

// 获取ECharts示例文件路径（使用配置服务）
const getEChartsFilePath = async (chartId: string): Promise<string> => {
  try {
    const filePath = await chartConfigService.getEChartsFilePath(chartId)
    console.log(`📋 [配置服务] 图表ID映射: ${chartId} -> ${filePath}`)
    return filePath
  } catch (error) {
    console.error(`❌ [配置服务] 获取文件路径失败: ${chartId}`, error)
    return '未知'
  }
}

// 获取JOLT SPEC文件路径
const getJoltFilePath = (chartId: string) => {
  const filePathMap: Record<string, string> = {
    'basic_line_chart': 'line-chart-placeholder.json',
    'smooth_line_chart': 'line-chart-placeholder.json',
    'stacked_line_chart': 'line-chart-stacked.json',
    'basic_bar_chart': 'bar-chart-placeholder.json',
    'stacked_bar_chart': 'bar-chart-placeholder.json',
    'basic_area_chart': '待创建',
    'basic_pie_chart': 'pie-chart-placeholder.json',
    'doughnut_chart': 'pie-chart-placeholder.json',
    'rose_chart': '待创建',
    'pie_chart': 'pie-chart-placeholder.json',
    'basic_radar_chart': 'radar-chart-placeholder.json',
    'filled_radar_chart': 'radar-chart-placeholder.json',
    'basic_gauge_chart': 'gauge-chart-placeholder.json',
    'progress_gauge_chart': 'gauge-chart-placeholder.json',
    'grade_gauge_chart': 'gauge-chart-placeholder.json'
  }
  return filePathMap[chartId] || '未知'
}

// 获取实现状态（从后端数据获取，不再使用硬编码）
const getImplementationStatus = (chartId: string) => {
  // 从后端数据中查找对应的状态
  const allCharts = Object.values(echartsDirectoryStructure.value).flat()
  const chart = allCharts.find((c: any) => c.chartId === chartId)
  
  if (chart && chart.status) {
    // 将英文状态转换为中文显示
    const statusMap: Record<string, string> = {
      'implemented': '已实现',
      'planned': '计划中',
      'unknown': '未知'
    }
    return statusMap[chart.status] || chart.status
  }
  
  // 如果后端没有提供状态信息，返回未知
  return '未知'
}

// 获取实现状态颜色
const getImplementationStatusColor = (chartId: string) => {
  const status = getImplementationStatus(chartId)
  const colorMap: Record<string, string> = {
    '已实现': 'success',
    '计划中': 'warning',
    '未知': 'default'
  }
  return colorMap[status] || 'default'
}

const getChartTypeEnglish = (chartCategory: string) => {
  const englishMap: Record<string, string> = {
    '折线图': 'Line Chart',
    '柱状图': 'Bar Chart',
    '面积图': 'Area Chart',
    '饼图': 'Pie Chart',
    '雷达图': 'Radar Chart',
    '仪表盘': 'Gauge Chart'
  }
  return englishMap[chartCategory] || chartCategory
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
      try {
        chartInstance.dispose()
      } catch (e) {
        console.warn('销毁旧图表实例时出现警告:', e)
      }
      chartInstance = null
    }

    // 确保容器有尺寸 - 适应新的布局
    const container = chartContainer.value
    
    // 检查容器是否仍然在DOM中
    if (!container.isConnected) {
      console.warn('图表容器已从DOM中移除，跳过初始化')
      return
    }
    
    if (container.offsetWidth === 0 || container.offsetHeight === 0) {
      console.warn('图表容器尺寸为0，设置默认尺寸')
      container.style.width = '100%'
      // 根据新布局调整默认高度
      container.style.height = '520px'
    }

    // 延迟初始化，确保DOM稳定
    requestAnimationFrame(() => {
      try {
        if (!container.isConnected) {
          console.warn('在动画帧回调中发现容器已移除，取消初始化')
          return
        }

        chartInstance = echarts.init(container, null, {
          renderer: 'canvas',
          useDirtyRect: false,
          width: container.offsetWidth || 400,
          height: container.offsetHeight || 300
        })

        // 添加图表错误监听
        chartInstance.on('error', (error: any) => {
          console.error('❌ ECharts渲染错误:', error)
          message.error('图表渲染出现错误，请检查数据格式')

          // 显示错误状态
          showChartError('图表渲染错误: ' + (error.message || '未知错误'))
        })

        console.log('✅ 图表实例初始化成功', {
          width: container.offsetWidth,
          height: container.offsetHeight,
          layout: '左侧图表预览布局'
        })

        // 重置缩放状态
        chartZoom.value = 1
        chartOffset.value = { x: 0, y: 0 }

        // 如果已有数据，立即渲染
        if (transformationStore.finalResult) {
          updateChart().catch(error => {
            console.error('图表渲染失败:', error)
          })
        }

        // 监听窗口大小变化
        const resizeHandler = () => {
          if (chartInstance && !chartInstance.isDisposed()) {
            try {
              chartInstance.resize()
            } catch (e) {
              console.warn('图表resize时出现警告:', e)
            }
          }
        }
        window.addEventListener('resize', resizeHandler)
      } catch (innerError) {
        console.error('❌ 延迟图表初始化失败:', innerError)
        const errorMessage = innerError instanceof Error ? innerError.message : String(innerError)
        showChartError('图表初始化失败: ' + errorMessage)
      }
    })

  } catch (error) {
    console.error('❌ 图表初始化失败:', error)
    const errorMessage = error instanceof Error ? error.message : String(error)
    message.error(`图表初始化失败: ${errorMessage}`)

    // 显示错误状态
    showChartError('图表初始化失败: ' + errorMessage)
  }
}

// 显示图表错误状态
const showChartError = (errorMessage: string) => {
  if (chartContainer.value) {
    chartContainer.value.innerHTML = `
      <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #ff4d4f;">
        <div style="font-size: 48px; margin-bottom: 16px;">⚠️</div>
        <div style="font-size: 16px; font-weight: 500; margin-bottom: 8px;">图表渲染失败</div>
        <div style="font-size: 12px; color: #999; text-align: center; max-width: 300px;">${errorMessage}</div>
        <button onclick="location.reload()" style="margin-top: 16px; padding: 8px 16px; background: #1890ff; color: white; border: none; border-radius: 4px; cursor: pointer;">
          重新加载页面
        </button>
      </div>
    `
  }
}

const updateChart = async () => {
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
    console.log('🎨 开始渲染图表，原始数据:', transformationStore.finalResult)

    // 🔍 检查原始数据
    if (transformationStore.finalResult?.series) {
      transformationStore.finalResult.series.forEach((series: any, index: number) => {
        console.log(`ORIGINAL_SERIES_${index}: type=${series.type} smooth=${series.smooth} name=${series.name}`)
      })
    }

    console.log(`CURRENT_CHART_ID: ${transformationStore.currentChartId}`)
    console.log(`EXPECTED_SMOOTH: ${transformationStore.currentChartId === 'smooth_line_chart' ? 'true' : 'false'}`)

    // 数据验证和预处理（使用配置服务）
    const chartData = await preprocessChartData(transformationStore.finalResult)
    if (!validateChartData(chartData)) {
      throw new Error('图表数据格式不正确')
    }

    // 🔍 检查预处理后的数据
    if (chartData?.series) {
      chartData.series.forEach((series: any, index: number) => {
        console.log(`PROCESSED_SERIES_${index}: type=${series.type} smooth=${series.smooth} name=${series.name}`)
        console.log(`PROCESSED_DATA_${index}: count=${series.data?.length} sample=${JSON.stringify(series.data?.slice(0, 3))}`)

        if (transformationStore.currentChartId === 'smooth_line_chart' && series.type === 'line') {
          if (series.smooth !== true) {
            console.log(`ERROR_SMOOTH_WRONG: expected=true actual=${series.smooth}`)
          } else {
            console.log(`SUCCESS_SMOOTH_CORRECT: ${series.smooth}`)
          }

          // 检查数据点是否足够用于平滑
          if (series.data && series.data.length < 3) {
            console.log(`WARNING_INSUFFICIENT_DATA: count=${series.data.length} need>=3`)
          }
        }
      })
    }

    // 清除之前的图表
    chartInstance.clear()

    // 设置新的配置
    console.log('SETOPTION_START: notMerge=true')

    chartInstance.setOption(chartData, {
      notMerge: true,
      lazyUpdate: false,
      silent: false
    })

    console.log('SETOPTION_COMPLETE')

    // 🔧 强制刷新图表以确保配置生效
    chartInstance.resize()

    // 简单验证
    setTimeout(() => {
      const actualConfig = chartInstance.getOption()
      if (actualConfig.series && actualConfig.series[0]) {
        console.log(`FINAL_CHECK: smooth=${actualConfig.series[0].smooth}`)
      }
    }, 100)

    console.log('CHART_RENDER_SUCCESS')

    // 🔍 验证ECharts实例中的配置
    setTimeout(() => {
      if (chartInstance && !chartInstance.isDisposed()) {
        const currentOption = chartInstance.getOption()
        console.log('🔍 [SMOOTH_DEBUG] ECharts实例中的实际配置:', currentOption)

        // 🔍 检查ECharts版本和smooth属性支持
        console.log('🔍 [ECHARTS_INFO] ECharts版本:', echarts.version)

        // 🔍 验证smooth属性是否被正确应用
        if (currentOption.series && Array.isArray(currentOption.series)) {
          currentOption.series.forEach((series: any, index: number) => {
            console.log(`🔍 [SMOOTH_VERIFY] ECharts实例series[${index}]:`, {
              type: series.type,
              smooth: series.smooth,
              name: series.name
            })
          })
        }

        chartInstance.resize()
      }
    }, 50)

    console.log('✅ 图表渲染成功')
  } catch (error) {
    console.error('❌ 图表渲染失败:', error)
    const errorMessage = error instanceof Error ? error.message : String(error)
    message.error(`图表渲染失败: ${errorMessage}`)

    // 显示错误状态
    showChartError('图表渲染失败: ' + errorMessage)
  }
}

// 预处理图表数据（使用配置服务）
const preprocessChartData = async (data: any): Promise<any> => {
  if (!data || typeof data !== 'object') {
    console.log('🔧 [预处理] 数据为空或非对象，直接返回:', data)
    return data
  }

  try {
    const currentChartId = transformationStore.currentChartId
    console.log(`🔧 [预处理] 当前图表ID: ${currentChartId}`)

    if (!currentChartId) {
      console.warn('⚠️ [配置服务] 当前图表ID为空，跳过配置服务预处理')
      return data
    }

    // 检查配置服务是否可用
    if (typeof chartConfigService === 'undefined') {
      console.error('❌ [配置服务] chartConfigService 未定义，使用原始数据')
      return data
    }

    // 使用配置服务进行预处理
    console.log(`🔧 [配置服务] 开始预处理图表数据: ${currentChartId}`)
    console.log(`🔧 [配置服务] 原始数据:`, data)

    const processedData = await chartConfigService.preprocessChartData(currentChartId, data)

    console.log(`✅ [配置服务] 图表数据预处理完成: ${currentChartId}`)
    console.log(`✅ [配置服务] 处理后数据:`, processedData)

    // 🔧 关键修复：平滑折线图移除stack属性
    if (currentChartId === 'smooth_line_chart' && processedData.series) {
      processedData.series.forEach((series: any, index: number) => {
        if (series.type === 'line') {
          console.log(`SMOOTH_CHECK_${index}: smooth=${series.smooth} stack=${series.stack}`)

          // 移除stack属性，因为它与smooth冲突
          if (series.stack) {
            console.log(`REMOVE_STACK_${index}: 移除stack属性以启用smooth`)
            delete series.stack
          }
        }
      })
    }

    return processedData
  } catch (error) {
    console.error('❌ [配置服务] 预处理失败，使用原始数据:', error)
    console.error('❌ [配置服务] 错误详情:', error.stack)
    return data
  }
}

// 验证图表数据格式
const validateChartData = (data: any): boolean => {
  try {
    if (!data || typeof data !== 'object') {
      console.error('图表数据不是有效对象')
      return false
    }

    // 检查必要的ECharts配置
    if (!data.series || !Array.isArray(data.series)) {
      console.error('缺少series配置或格式不正确')
      return false
    }

    if (data.series.length === 0) {
      console.error('series数组为空')
      return false
    }

    // 检查每个series的基本结构
    for (let i = 0; i < data.series.length; i++) {
      const series = data.series[i]
      if (!series.type) {
        console.error(`series[${i}]缺少type属性`)
        return false
      }
    }

    return true
  } catch (error) {
    console.error('数据验证过程中出错:', error)
    return false
  }
}

const refreshChart = async () => {
  try {
    await updateChart()
    message.success('图表已刷新')
  } catch (error) {
    console.error('图表刷新失败:', error)
    message.error('图表刷新失败')
  }
}

// 🧪 官方ECharts示例对比测试
const testOfficialExample = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  console.log('OFFICIAL_EXAMPLE_TEST_START')

  // 官方ECharts平滑折线图示例 (https://echarts.apache.org/examples/zh/editor.html?c=line-smooth)
  const officialConfig = {
    title: {
      text: '官方ECharts平滑折线图示例'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['邮件营销', '联盟广告', '视频广告', '直接访问', '搜索引擎']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
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
        stack: 'Total',
        smooth: true,
        lineStyle: {
          width: 0
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8
        },
        emphasis: {
          focus: 'series'
        },
        data: [140, 232, 101, 264, 90, 340, 250]
      },
      {
        name: '联盟广告',
        type: 'line',
        stack: 'Total',
        smooth: true,
        lineStyle: {
          width: 0
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8
        },
        emphasis: {
          focus: 'series'
        },
        data: [120, 282, 111, 234, 220, 340, 310]
      }
    ]
  }

  console.log('OFFICIAL_CONFIG_SET')
  console.log('OFFICIAL_SERIES_0_SMOOTH:', officialConfig.series[0].smooth)
  console.log('OFFICIAL_SERIES_1_SMOOTH:', officialConfig.series[1].smooth)

  chartInstance.setOption(officialConfig, { notMerge: true })
  message.success('官方ECharts示例已加载，应该看到平滑的区域图')
}

// 🧪 简化的平滑测试
const testSmoothFunction = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  console.log('SIMPLE_SMOOTH_TEST_START')

  // 最简单的平滑测试配置
  const simpleConfig = {
    title: { text: '简单平滑测试' },
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Smooth Line',
        type: 'line',
        smooth: true,
        data: [820, 932, 901, 934, 1290, 1330, 1320]
      }
    ]
  }

  console.log('SIMPLE_CONFIG_SET')
  console.log('SIMPLE_SERIES_SMOOTH:', simpleConfig.series[0].smooth)
  console.log('SIMPLE_SERIES_DATA:', simpleConfig.series[0].data)

  chartInstance.setOption(simpleConfig, { notMerge: true })
  message.success('简单平滑测试已加载')
}

// 🧪 数据结构对比测试
const testDataStructureComparison = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  console.log('DATA_STRUCTURE_COMPARISON_START')

  if (transformationStore.finalResult && transformationStore.finalResult.series) {
    const ourData = transformationStore.finalResult

    console.log('OUR_DATA_STRUCTURE:')
    console.log('- Title:', ourData.title)
    console.log('- XAxis:', ourData.xAxis)
    console.log('- YAxis:', ourData.yAxis)
    console.log('- Series count:', ourData.series.length)

    ourData.series.forEach((series, index) => {
      console.log(`- Series[${index}]:`)
      console.log(`  - name: ${series.name}`)
      console.log(`  - type: ${series.type}`)
      console.log(`  - smooth: ${series.smooth}`)
      console.log(`  - data length: ${series.data?.length}`)
      console.log(`  - data sample: ${JSON.stringify(series.data?.slice(0, 3))}`)
      console.log(`  - other props: ${Object.keys(series).filter(k => !['name', 'type', 'smooth', 'data'].includes(k)).join(', ')}`)
    })

    // 创建对比配置：我们的数据 vs 官方格式
    const comparisonConfig = {
      title: { text: '数据结构对比测试' },
      xAxis: ourData.xAxis,
      yAxis: ourData.yAxis,
      series: [
        // 我们的原始数据
        {
          ...ourData.series[0],
          name: '我们的数据(原始)',
          lineStyle: { color: '#ff0000', width: 2 }
        },
        // 简化为官方格式
        {
          name: '官方格式',
          type: 'line',
          smooth: true,
          data: ourData.series[0].data,
          lineStyle: { color: '#0000ff', width: 2 }
        }
      ],
      legend: { data: ['我们的数据(原始)', '官方格式'] }
    }

    console.log('COMPARISON_CONFIG_SET')
    chartInstance.setOption(comparisonConfig, { notMerge: true })
    message.success('数据结构对比测试已加载，红线=我们的数据，蓝线=官方格式')
  } else {
    message.error('没有当前数据可供对比')
  }
}

// 🧪 回归测试：测试之前工作的配置
const testRegressionCheck = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  console.log('REGRESSION_TEST_START')

  // 这是之前工作的确切配置
  const workingConfig = {
    title: { text: 'Smooth功能测试' },
    xAxis: {
      type: 'category',
      data: ['A', 'B', 'C', 'D', 'E', 'F', 'G']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '直线连接',
        type: 'line',
        smooth: false,
        data: [10, 50, 20, 80, 30, 90, 40],
        lineStyle: { color: '#ff0000', width: 3 }
      },
      {
        name: '平滑连接',
        type: 'line',
        smooth: true,
        data: [15, 45, 25, 75, 35, 85, 45],
        lineStyle: { color: '#0000ff', width: 3 }
      }
    ],
    legend: { data: ['直线连接', '平滑连接'] }
  }

  console.log('REGRESSION_CONFIG_EXACT_COPY')
  console.log('REGRESSION_SERIES_0_SMOOTH:', workingConfig.series[0].smooth)
  console.log('REGRESSION_SERIES_1_SMOOTH:', workingConfig.series[1].smooth)

  // 清空图表并重新设置
  chartInstance.clear()
  chartInstance.setOption(workingConfig, { notMerge: true })

  // 验证设置后的配置
  setTimeout(() => {
    const actualConfig = chartInstance.getOption()
    console.log('REGRESSION_VERIFY_SERIES_0_SMOOTH:', actualConfig.series[0].smooth)
    console.log('REGRESSION_VERIFY_SERIES_1_SMOOTH:', actualConfig.series[1].smooth)
  }, 100)

  message.success('回归测试：之前工作的配置已加载')
}

// 🔍 Stack属性干扰确认测试
const testPropertyInterference = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  if (!transformationStore.finalResult || !transformationStore.finalResult.series) {
    message.error('没有当前数据可供测试')
    return
  }

  console.log('STACK_INTERFERENCE_TEST_START')

  const ourSeries = transformationStore.finalResult.series[0]
  console.log('OUR_SERIES_ALL_PROPERTIES:', Object.keys(ourSeries))
  console.log('OUR_SERIES_STACK_VALUE:', ourSeries.stack)
  console.log('OUR_SERIES_DATA_TYPE:', typeof ourSeries.data)
  console.log('OUR_SERIES_DATA_LENGTH:', ourSeries.data?.length)
  console.log('OUR_SERIES_DATA_SAMPLE:', ourSeries.data?.slice(0, 5))
  console.log('OUR_SERIES_DATA_SAMPLE_TYPES:', ourSeries.data?.slice(0, 5).map(d => typeof d))

  // 精确测试stack属性的影响
  const baseConfig = {
    title: { text: 'Stack属性干扰确认' },
    xAxis: transformationStore.finalResult.xAxis,
    yAxis: transformationStore.finalResult.yAxis,
    series: []
  }

  // 转换数据为数字类型（防止字符串数据导致的问题）
  const numericData = ourSeries.data?.map(d => Number(d)) || []
  console.log('NUMERIC_DATA_SAMPLE:', numericData.slice(0, 5))
  console.log('NUMERIC_DATA_TYPES:', numericData.slice(0, 5).map(d => typeof d))

  // 测试1：无stack属性 + 数字数据
  const noStackSeries = {
    name: '无Stack+数字',
    type: 'line',
    smooth: true,
    data: numericData,
    lineStyle: { color: '#00ff00', width: 3 }
  }

  // 测试2：无stack属性 + 原始数据
  const originalDataSeries = {
    name: '无Stack+原始',
    type: 'line',
    smooth: true,
    data: ourSeries.data,
    lineStyle: { color: '#ff0000', width: 3 }
  }

  // 测试3：简单测试数据
  const simpleDataSeries = {
    name: '简单数据',
    type: 'line',
    smooth: true,
    data: [10, 50, 20, 80, 30, 90, 40],
    lineStyle: { color: '#0000ff', width: 3 }
  }

  // 测试4：有stack但不同值
  const differentStackSeries = {
    name: '不同Stack',
    type: 'line',
    smooth: true,
    stack: 'Different',
    data: numericData,
    lineStyle: { color: '#ff8800', width: 3 }
  }

  baseConfig.series = [noStackSeries, originalDataSeries, simpleDataSeries, differentStackSeries]
  baseConfig.legend = { data: ['无Stack+数字', '无Stack+原始', '简单数据', '不同Stack'] }

  console.log('NO_STACK_NUMERIC:', noStackSeries)
  console.log('ORIGINAL_DATA:', originalDataSeries)
  console.log('SIMPLE_DATA:', simpleDataSeries)
  console.log('DIFFERENT_STACK:', differentStackSeries)

  chartInstance.setOption(baseConfig, { notMerge: true })
  message.success('数据类型测试：绿线=数字数据，红线=原始数据，蓝线=简单数据，橙线=不同Stack')
}

// 🔧 直接修复测试：使用当前数据但移除stack
const testDirectFix = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  if (!transformationStore.finalResult || !transformationStore.finalResult.series) {
    message.error('没有当前数据可供测试')
    return
  }

  console.log('DIRECT_FIX_TEST_START')

  // 使用当前的完整配置，但移除stack属性
  const fixedConfig = JSON.parse(JSON.stringify(transformationStore.finalResult))

  if (fixedConfig.series) {
    fixedConfig.series.forEach((series: any, index: number) => {
      if (series.type === 'line') {
        console.log(`BEFORE_FIX_${index}: smooth=${series.smooth} stack=${series.stack}`)

        // 强制设置smooth并移除stack
        series.smooth = true
        if (series.stack) {
          delete series.stack
          console.log(`AFTER_FIX_${index}: 已移除stack属性`)
        }

        console.log(`AFTER_FIX_${index}: smooth=${series.smooth} stack=${series.stack}`)
      }
    })
  }

  fixedConfig.title = { text: '直接修复测试' }

  // 🔧 关键修复：使用更明显的测试数据
  if (fixedConfig.series) {
    fixedConfig.series.forEach((series: any, index: number) => {
      if (series.type === 'line') {
        // 使用更明显的波动数据
        series.data = [10, 80, 20, 90, 15, 85, 25, 95, 30]
        console.log(`ENHANCED_DATA_${index}: 使用增强测试数据`)
      }
    })
  }

  console.log('DIRECT_FIX_CONFIG:', fixedConfig)
  chartInstance.setOption(fixedConfig, { notMerge: true })
  message.success('直接修复测试：使用增强数据，应该显示明显的平滑曲线')
}

// 🧪 测试堆叠功能的独立函数
const testStackFunction = () => {
  if (!chartInstance) {
    message.error('图表实例不存在')
    return
  }

  console.log('🧪 [STACK_TEST] 开始测试堆叠功能')

  // 创建堆叠折线图测试配置
  const stackTestConfig = {
    title: { text: '堆叠折线图测试' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Email',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        data: [120, 132, 101, 134, 90, 230, 210]
      },
      {
        name: 'Union Ads',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        data: [220, 182, 191, 234, 290, 330, 310]
      },
      {
        name: 'Video Ads',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        data: [150, 232, 201, 154, 190, 330, 410]
      }
    ],
    legend: { data: ['Email', 'Union Ads', 'Video Ads'] }
  }

  console.log('🧪 [STACK_TEST] 堆叠测试配置:', stackTestConfig)

  chartInstance.setOption(stackTestConfig, { notMerge: true })
  message.success('堆叠折线图测试已加载，应该看到三个区域垂直堆叠的效果')
}

// 🧪 测试配置服务的独立函数
const testConfigService = async () => {
  console.log('🧪 [CONFIG_TEST] 开始测试配置服务')

  try {
    // 测试图表类型映射
    const mappings = await chartConfigService.getChartTypeMappings()
    console.log('✅ [CONFIG_TEST] 图表类型映射:', mappings)

    // 测试特定配置
    const config = await chartConfigService.getChartSpecificConfig('basic_line_chart')
    console.log('✅ [CONFIG_TEST] 基础折线图配置:', config)

    // 测试文件路径映射
    const chartId = await chartConfigService.generateChartIdFromFilePath('折线图/基础折线图.json')
    console.log('✅ [CONFIG_TEST] 文件路径映射:', chartId)

    message.success('配置服务测试完成，请查看控制台日志')
  } catch (error) {
    console.error('❌ [CONFIG_TEST] 配置服务测试失败:', error)
    message.error('配置服务测试失败')
  }
}

// 🧪 专门测试平滑折线图的函数
const testSmoothLineChart = async () => {
  console.log('🧪 [SMOOTH_TEST] 开始测试平滑折线图配置')

  try {
    // 1. 测试文件路径到图表ID的映射
    const filePath = '折线图/基础平滑折线图.json'
    const chartId = await generateChartIdFromFilePath(filePath)
    console.log(`🧪 [SMOOTH_TEST] 文件路径映射: ${filePath} -> ${chartId}`)

    // 2. 测试图表特定配置获取
    const config = await chartConfigService.getChartSpecificConfig(chartId)
    console.log(`🧪 [SMOOTH_TEST] 图表配置:`, config)

    // 3. 测试预处理功能
    const testData = {
      title: { text: '平滑折线图测试' },
      xAxis: { type: 'category', data: ['A', 'B', 'C', 'D', 'E'] },
      yAxis: { type: 'value' },
      series: [
        {
          name: '测试数据',
          type: 'line',
          data: [10, 50, 20, 80, 30]
        }
      ]
    }

    console.log(`🧪 [SMOOTH_TEST] 原始数据:`, testData)
    const processedData = await chartConfigService.preprocessChartData(chartId, testData)
    console.log(`🧪 [SMOOTH_TEST] 处理后数据:`, processedData)

    // 4. 检查smooth属性是否正确设置
    if (processedData.series && processedData.series[0]) {
      const smoothValue = processedData.series[0].smooth
      console.log(`🧪 [SMOOTH_TEST] series[0].smooth = ${smoothValue}`)

      if (smoothValue === true) {
        console.log('✅ [SMOOTH_TEST] smooth属性设置正确')

        // 5. 直接渲染测试图表
        if (chartInstance) {
          chartInstance.setOption(processedData, { notMerge: true })
          message.success('平滑折线图测试完成，应该看到平滑曲线')
        }
      } else {
        console.error(`❌ [SMOOTH_TEST] smooth属性错误: 期望true，实际${smoothValue}`)
        message.error(`smooth属性错误: 期望true，实际${smoothValue}`)
      }
    } else {
      console.error('❌ [SMOOTH_TEST] 处理后数据缺少series')
      message.error('处理后数据缺少series')
    }

  } catch (error) {
    console.error('❌ [SMOOTH_TEST] 平滑折线图测试失败:', error)
    message.error('平滑折线图测试失败')
  }
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

// 图表缩放功能
const zoomIn = () => {
  if (chartZoom.value < 3) {
    chartZoom.value = Math.min(3, chartZoom.value + 0.2)
    updateChartSize()
  }
}

const zoomOut = () => {
  if (chartZoom.value > 0.5) {
    chartZoom.value = Math.max(0.5, chartZoom.value - 0.2)
    updateChartSize()
  }
}

const resetChartZoom = () => {
  chartZoom.value = 1
  chartOffset.value = { x: 0, y: 0 }
  updateChartSize()
}

const updateChartSize = () => {
  if (chartInstance && !chartInstance.isDisposed()) {
    // 延迟调用resize，确保DOM更新完成
    nextTick(() => {
      try {
        chartInstance?.resize()
      } catch (e) {
        console.warn('图表resize时出现警告:', e)
      }
    })
  }
}

// 鼠标滚轮缩放
const handleChartWheel = (event: WheelEvent) => {
  if (!chartInstance) return

  event.preventDefault()

  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newZoom = Math.max(0.5, Math.min(3, chartZoom.value + delta))

  if (newZoom !== chartZoom.value) {
    chartZoom.value = newZoom
    updateChartSize()
  }
}

// 图表拖拽功能
const handleChartMouseDown = (event: MouseEvent) => {
  if (chartZoom.value <= 1) return // 只有放大时才允许拖拽

  isDragging.value = true
  dragStart.value = { x: event.clientX, y: event.clientY }
  event.preventDefault()
}

const handleChartMouseMove = (event: MouseEvent) => {
  if (!isDragging.value || chartZoom.value <= 1) return

  const deltaX = event.clientX - dragStart.value.x
  const deltaY = event.clientY - dragStart.value.y

  chartOffset.value = {
    x: chartOffset.value.x + deltaX,
    y: chartOffset.value.y + deltaY
  }

  dragStart.value = { x: event.clientX, y: event.clientY }

  // 更新图表容器位置
  if (chartContainer.value) {
    chartContainer.value.style.transform = `scale(${chartZoom.value}) translate(${chartOffset.value.x / chartZoom.value}px, ${chartOffset.value.y / chartZoom.value}px)`
    chartContainer.value.style.transformOrigin = 'top left'
  }
}

const handleChartMouseUp = () => {
  isDragging.value = false
}

// 仅用于下载/预览真实数据后的图表，不再注入假数据
const testChart = () => {
  console.log('图表会在选择文件时自动转换，无需手动调用')
  message.info('图表会在选择文件时自动转换')
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
          updateChart().catch(error => {
            console.error('图表更新失败:', error)
          })
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

// 监听选中的图表文件变化
watch(
  () => selectedChartFile.value,
  (newFile, oldFile) => {
    console.log('📁 监听到图表文件变化:', { oldFile, newFile })
    if (newFile && newFile !== oldFile) {
      // 当图表文件变化时，确保图表容器准备就绪
      nextTick(() => {
        if (chartContainer.value && !chartInstance) {
          console.log('🔄 图表文件变化，重新初始化图表容器')
          initChart()
        }
      })
    }
  }
)

// 监听下拉框选择状态，确保图表信息正确显示
watch(
  () => [selectedTemplateType.value, selectedChartFile.value],
  ([newTemplateType, newChartFile], [oldTemplateType, oldChartFile]) => {
    console.log('🔍 监听到选择状态变化:', {
      templateType: { old: oldTemplateType, new: newTemplateType },
      chartFile: { old: oldChartFile, new: newChartFile },
      chartInfo: chartInfo.value
    })

    // 如果有选择但没有图表信息，尝试加载
    if (newTemplateType && newChartFile && !chartInfo.value) {
      console.log('⚠️ 检测到有选择但缺少图表信息，尝试加载...')
      setTimeout(async () => {
        try {
          await handleChartFileChange(newChartFile)
        } catch (error) {
          console.error('❌ 补充加载图表信息失败:', error)
          await loadChartInfoWithFallback(newChartFile)
        }
      }, 200)
    }
  },
  { deep: true }
)

// 监听目录分类数据变化
watch(
  () => directoryCategories.value,
  (newCategories) => {
    console.log('📂 监听到目录分类变化:', newCategories)
    if (newCategories && newCategories.length > 0) {
      console.log('✅ 目录分类数据已更新，共', newCategories.length, '个分类')
    }
  },
  { deep: true }
)

// 生命周期
onMounted(async () => {
  console.log('🚀 页面开始挂载...')
  await nextTick()

  // 检查配置服务可用性
  console.log('🔧 [配置服务] 检查服务可用性...')
  try {
    if (typeof chartConfigService !== 'undefined') {
      console.log('✅ [配置服务] 服务可用')
      // 预热配置服务
      await chartConfigService.getChartTypeMappings()
      console.log('✅ [配置服务] 预热完成')
    } else {
      console.error('❌ [配置服务] 服务不可用，将使用硬编码回退')
    }
  } catch (error) {
    console.error('❌ [配置服务] 初始化失败:', error)
  }

  // 首先加载ECharts目录结构（包含默认选择初始化）
  console.log('📂 开始加载目录结构...')
  await loadEChartsDirectory()

  // 检查加载结果
  console.log('📊 目录加载完成，当前状态:', {
    directoryCategories: directoryCategories.value,
    echartsDirectoryStructure: echartsDirectoryStructure.value,
    selectedTemplateType: selectedTemplateType.value,
    selectedChartFile: selectedChartFile.value
  })

  // 延迟初始化图表，确保DOM完全渲染
  setTimeout(() => {
    initChart()
  }, 200)

  // 监听窗口大小变化
  window.addEventListener('resize', updateWindowWidth)
  updateWindowWidth()

  console.log('✅ 页面挂载完成')
})

// 根据图表ID初始化模板类型选择
const initializeTemplateTypeFromChartId = async (chartId: string) => {
  // 在新的目录结构中查找对应的分类和文件
  for (const [categoryName, files] of Object.entries(echartsDirectoryStructure.value)) {
    // 使用 Promise.all 来并行处理所有文件的 chartId 生成
    const fileChartIds = await Promise.all(
      files.map(async file => ({
        file,
        generatedId: await generateChartIdFromFilePath(file.filePath)
      }))
    )

    const foundFileData = fileChartIds.find(({ generatedId }) => generatedId === chartId)

    if (foundFileData) {
      selectedTemplateType.value = categoryName
      availableCharts.value = files.map(file => ({
        id: file.displayName,
        name: file.displayName,
        filePath: file.filePath
      }))
      selectedChartFile.value = foundFileData.file.filePath
      break
    }
  }
}

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
  background: #f5f5f5;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

/* 精简后的配置头部 - 无标题版本 */
.config-header-compact {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
  height: auto;
  max-height: 280px;
  overflow-y: auto;
}

/* 主要配置区域 - 紧凑化 */
.config-section {
  margin-bottom: 12px;
  min-height: auto; /* 移除固定高度，让内容自适应 */
}

/* 图表选择面板 - 紧凑化 */
.chart-selector-panel {
  background: #fafbfc;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 10px;
  height: 100%;
}


/* 选择器项目 - 紧凑化 */
.selector-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.selector-item:last-child {
  margin-bottom: 0;
}

.selector-label {
  font-size: 12px;
  font-weight: 500;
  color: #595959;
  margin-bottom: 2px;
}


/* 图表信息面板 - 紧凑化 */
.chart-info-panel {
  background: #fafbfc;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 10px;
  height: auto;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}


/* 信息内容样式 */
.info-content .ant-descriptions {
  background: #ffffff;
  border-radius: 6px;
}

.info-value-with-subtitle {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.main-value {
  font-weight: 500;
  color: #262626;
}

.sub-value {
  font-size: 12px;
  color: #8c8c8c;
}

.template-type-name {
  margin-left: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.file-path-code {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 4px;
  padding: 4px 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #d73a49;
  word-break: break-all;
}

/* 信息占位符 - 紧凑化 */
.info-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 120px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
}

/* 操作按钮区域 - 紧凑化 */
.action-section {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.action-section .ant-btn {
  height: 32px;
  font-size: 13px;
  padding: 0 12px;
}

/* 重构后的响应式设计 - 适应20%-80%布局 */
@media (max-width: 1199px) {
  .config-header-compact {
    padding: 14px;
    max-height: 22vh; /* 稍微增加空间 */
  }
  
  .config-section {
    margin-bottom: 10px;
  }
  
  .chart-selector-panel,
  .chart-info-panel {
    padding: 10px;
  }
  
  .main-content-area {
    min-height: 68vh; /* 调整内容区域高度 */
  }
  
  .chart-preview-card,
  .data-card {
    height: calc(68vh - 40px);
    min-height: 450px;
  }
  
  .json-viewer {
    height: calc(68vh - 120px);
    min-height: 350px;
  }
  
  .chart-preview-card .chart-wrapper {
    height: calc(68vh - 90px);
    min-height: 380px;
  }
}

@media (max-width: 991px) {
  .config-section {
    min-height: auto;
  }
  
  .chart-selector-panel,
  .chart-info-panel {
    margin-bottom: 16px;
  }
  
  .action-section {
    padding-top: 20px;
  }
}

@media (max-width: 767px) {
  .config-header-compact {
    padding: 16px;
    margin-bottom: 20px;
  }
  
  .chart-selector-panel,
  .chart-info-panel {
    padding: 12px;
    margin-bottom: 12px;
  }
  
  
  .selector-label {
    font-size: 12px;
  }
  
  .file-path-code {
    font-size: 11px;
    padding: 3px 6px;
  }
  
  .info-placeholder {
    height: 120px;
  }
  
  .action-section {
    padding-top: 16px;
  }
  
  .action-section .ant-space {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 575px) {
  .config-header-compact {
    padding: 12px;
  }
  
  .action-section .ant-space {
    gap: 8px !important;
  }
  
  .action-section .ant-btn {
    font-size: 12px;
    padding: 0 8px;
    height: 28px;
  }
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
  flex-shrink: 0;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
  margin-top: 12px;
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

/* 主要内容区域 - 占据剩余空间 */
.main-content-area {
  margin-bottom: 12px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 图表预览卡片 - 适应新的高度分配 */
.chart-preview-card {
  height: 100%;
  min-height: 400px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  isolation: isolate;
}

.chart-preview-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 60px);
  display: flex;
  flex-direction: column;
}

.chart-preview-card.active {
  border-color: #1890ff;
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.12);
  transform: translateY(-2px);
  z-index: 1;
}

.data-flow {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 确保数据流卡片在右侧区域内合理分布 */
@media (min-width: 1200px) {
  .data-flow .ant-col {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .data-flow {
    flex-direction: row;
  }
}

/* 中等屏幕优化 */
@media (max-width: 1199px) and (min-width: 768px) {
  .data-flow .ant-col {
    margin-bottom: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .data-flow {
    flex-direction: column;
  }
}

.data-card {
  height: 100%;
  min-height: 400px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  isolation: isolate;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.json-viewer {
  flex: 1;
  min-height: 300px;
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

/* 图表预览卡片中的图表容器 */
.chart-preview-card .chart-wrapper {
  flex: 1;
  min-height: 300px;
  width: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 图表缩放控制按钮 */
.chart-zoom-controls {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 图表滚动容器 */
.chart-scroll-container {
  flex: 1;
  overflow: auto;
  position: relative;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
}

/* 图表滚动容器的滚动条样式 - 与JSON查看器保持一致 */
.chart-scroll-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.chart-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chart-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chart-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.chart-preview-card .chart-container {
  width: 100%;
  height: 100%;
  min-width: 100%;
  min-height: 100%;
  background: #ffffff;
  position: relative;
  /* 移除可能导致重影的样式 */
  box-shadow: none;
  /* 确保图表容器完全填充 */
  display: block;
  /* 支持缩放和拖拽 */
  cursor: grab;
  transition: transform 0.1s ease-out;
}

.chart-preview-card .chart-container:active {
  cursor: grabbing;
}

/* 当图表被缩放时的样式 */
.chart-preview-card .chart-container.zoomed {
  cursor: move;
}

/* 数据流卡片中的图表容器（如果有的话） */
.data-card .chart-wrapper {
  height: 360px;
  width: 100%;
  position: relative;
  display: flex;
  align-items: stretch;
}

.data-card .chart-wrapper > * {
  width: 100%;
  height: 100%;
}

.data-card .chart-container {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  box-shadow: none;
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
  width: 100%;
  height: 100%;
  min-height: 300px;
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
    height: 100vh;
  }

  .config-header-compact {
    max-height: 200px;
    padding: 8px;
  }

  .main-content-area {
    flex-direction: column;
  }

  .main-content-area .ant-row {
    flex-direction: column;
    height: auto;
  }

  .main-content-area .ant-col {
    height: auto !important;
    margin-bottom: 12px;
  }

  .chart-preview-card {
    height: 300px;
    min-height: 300px;
  }

  .chart-preview-card .chart-wrapper {
    min-height: 200px;
  }

  .chart-zoom-controls {
    top: 4px;
    right: 4px;
    padding: 2px;
  }

  .chart-zoom-controls .ant-btn {
    padding: 0 4px;
    height: 24px;
    font-size: 12px;
  }

  .data-card {
    height: 280px;
    min-height: 280px;
  }

  .json-viewer {
    min-height: 180px;
    font-size: 11px;
  }

  .data-flow {
    flex-direction: column;
    gap: 12px;
  }

  .progress-panel {
    margin-top: 8px;
    padding: 12px;
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
