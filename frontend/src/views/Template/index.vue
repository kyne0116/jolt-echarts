<template>
  <div class="template-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <FileTextOutlined class="title-icon" />
            模板管理中心
          </h1>
          <p class="page-subtitle">管理ECharts图表的通用JSON模板，支持14种图表类型和JOLT转换规范</p>
        </div>
        <div class="header-actions">
          <a-space size="middle">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索模板名称或图表类型"
              style="width: 300px"
              @search="handleSearch"
            >
              <template #enterButton>
                <SearchOutlined />
              </template>
            </a-input-search>
            <a-button type="primary" @click="showCreateModal">
              <PlusOutlined />
              新建模板
            </a-button>
            <a-button @click="refreshData">
              <ReloadOutlined />
              刷新
            </a-button>
          </a-space>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧面板：模板分类树 -->
      <div class="left-panel">
        <a-card title="模板分类" size="small" class="category-card">
          <template #extra>
            <a-badge :count="totalTemplatesCount" show-zero color="#52c41a">
              <span class="total-label">总计</span>
            </a-badge>
          </template>
          
          <a-tree
            v-model:expandedKeys="expandedKeys"
            v-model:selectedKeys="selectedCategoryKeys"
            :tree-data="categoryTreeData"
            :show-line="{ showLeafIcon: false }"
            @select="handleCategorySelect"
            class="category-tree"
          >
            <template #title="{ key, title, count, status, children }">
              <div class="tree-node-title">
                <span class="node-title">{{ title }}</span>
                <div class="node-info" v-if="!children">
                  <a-tag :color="getStatusColor(status)" size="small">
                    {{ getStatusText(status) }}
                  </a-tag>
                </div>
                <div class="node-count" v-else>
                  <a-badge :count="count" :number-style="{ backgroundColor: '#f0f0f0', color: '#666' }" />
                </div>
              </div>
            </template>
          </a-tree>
        </a-card>
      </div>

      <!-- 中左面板：模板列表 -->
      <div class="center-left-panel">
        <a-card title="模板列表" size="small" class="template-list-card">
          <template #extra>
            <a-space>
              <a-select
                v-model:value="sortBy"
                style="width: 120px"
                size="small"
                @change="handleSortChange"
              >
                <a-select-option value="name">按名称</a-select-option>
                <a-select-option value="chartType">按类型</a-select-option>
                <a-select-option value="updateTime">按时间</a-select-option>
              </a-select>
              <a-button type="text" size="small" @click="toggleViewMode">
                <UnorderedListOutlined v-if="viewMode === 'card'" />
                <AppstoreOutlined v-else />
              </a-button>
            </a-space>
          </template>

          <div class="template-list-content">
            <a-spin :spinning="loading">
              <!-- 列表视图 -->
              <div v-if="viewMode === 'list'" class="list-view">
                <a-list
                  :data-source="filteredTemplates"
                  :pagination="listPagination"
                  size="small"
                >
                  <template #renderItem="{ item }">
                    <a-list-item
                      :class="{ 'active-template': selectedTemplate?.chartId === item.chartId }"
                      @click="selectTemplate(item)"
                      class="template-list-item"
                    >
                      <a-list-item-meta>
                        <template #title>
                          <div class="item-title">
                            <span class="template-name">{{ item.name }}</span>
                            <a-tag :color="getTemplateTypeColor(item.chartType)" size="small">
                              {{ getTemplateTypeName(item.chartType) }}
                            </a-tag>
                          </div>
                        </template>
                        <template #description>
                          <div class="item-description">
                            <div class="description-text">{{ item.description }}</div>
                            <div class="meta-info">
                              <span class="chart-id">ID: {{ item.chartId }}</span>
                              <span class="update-time">更新时间: {{ formatTime(item.updatedAt) }}</span>
                            </div>
                          </div>
                        </template>
                        <template #avatar>
                          <div class="template-avatar">
                            <component :is="getChartIcon(item.chartType)" class="chart-icon" />
                          </div>
                        </template>
                      </a-list-item-meta>
                      
                      <template #actions>
                        <a-space>
                          <a-tooltip title="编辑">
                            <a-button type="text" size="small" @click.stop="editTemplate(item)">
                              <EditOutlined />
                            </a-button>
                          </a-tooltip>
                          <a-tooltip title="复制">
                            <a-button type="text" size="small" @click.stop="duplicateTemplate(item)">
                              <CopyOutlined />
                            </a-button>
                          </a-tooltip>
                          <a-tooltip title="导出">
                            <a-button type="text" size="small" @click.stop="exportTemplate(item)">
                              <DownloadOutlined />
                            </a-button>
                          </a-tooltip>
                          <a-popconfirm
                            title="确定要删除这个模板吗？"
                            @confirm="deleteTemplate(item.chartId)"
                            @click.stop
                          >
                            <a-button type="text" size="small" danger>
                              <DeleteOutlined />
                            </a-button>
                          </a-popconfirm>
                        </a-space>
                      </template>
                    </a-list-item>
                  </template>
                </a-list>
              </div>

              <!-- 卡片视图 -->
              <div v-else class="card-view">
                <a-row :gutter="[16, 16]">
                  <a-col
                    v-for="template in paginatedTemplates"
                    :key="template.chartId"
                    :xs="24"
                    :sm="12"
                    :lg="8"
                    :xl="6"
                  >
                    <a-card
                      :class="{ 'active-template-card': selectedTemplate?.chartId === template.chartId }"
                      @click="selectTemplate(template)"
                      class="template-card"
                      hoverable
                      size="small"
                    >
                      <template #title>
                        <div class="card-title">
                          <component :is="getChartIcon(template.chartType)" class="card-icon" />
                          <span class="card-name">{{ template.name }}</span>
                        </div>
                      </template>
                      <template #extra>
                        <a-dropdown trigger="click">
                          <a-button type="text" size="small">
                            <MoreOutlined />
                          </a-button>
                          <template #overlay>
                            <a-menu @click="handleCardMenuClick($event, template)">
                              <a-menu-item key="edit">
                                <EditOutlined />
                                编辑
                              </a-menu-item>
                              <a-menu-item key="duplicate">
                                <CopyOutlined />
                                复制
                              </a-menu-item>
                              <a-menu-item key="export">
                                <DownloadOutlined />
                                导出
                              </a-menu-item>
                              <a-menu-divider />
                              <a-menu-item key="delete" class="danger-menu-item">
                                <DeleteOutlined />
                                删除
                              </a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </template>
                      
                      <div class="card-content">
                        <a-tag :color="getTemplateTypeColor(template.chartType)" class="type-tag">
                          {{ getTemplateTypeName(template.chartType) }}
                        </a-tag>
                        <p class="card-description">{{ template.description }}</p>
                        <div class="card-meta">
                          <small class="meta-item">{{ template.chartId }}</small>
                          <small class="meta-item">{{ formatTime(template.updatedAt) }}</small>
                        </div>
                      </div>
                    </a-card>
                  </a-col>
                </a-row>

                <!-- 卡片视图分页 -->
                <div class="card-pagination">
                  <a-pagination
                    v-model:current="cardPage"
                    v-model:page-size="cardPageSize"
                    :total="filteredTemplates.length"
                    :show-size-changer="true"
                    :show-quick-jumper="true"
                    :show-total="(total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`"
                    size="small"
                  />
                </div>
              </div>
            </a-spin>
          </div>
        </a-card>
      </div>

      <!-- 中右面板：模板编辑器 -->
      <div class="center-right-panel">
        <a-card title="模板编辑器" size="small" class="editor-card">
          <template #extra>
            <a-space>
              <a-button size="small" @click="formatTemplate" :disabled="!selectedTemplate">
                <FormatPainterOutlined />
                格式化
              </a-button>
              <a-button size="small" @click="validateTemplate" :disabled="!selectedTemplate">
                <CheckCircleOutlined />
                验证
              </a-button>
              <a-button type="primary" size="small" @click="saveTemplate" :loading="saving" :disabled="!selectedTemplate">
                <SaveOutlined />
                保存
              </a-button>
            </a-space>
          </template>

          <div class="editor-content">
            <div v-if="selectedTemplate" class="template-editor">
              <!-- 基本信息编辑 -->
              <a-form layout="vertical" class="template-form">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-form-item label="模板名称">
                      <a-input v-model:value="selectedTemplate.name" placeholder="请输入模板名称" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="图表类型">
                      <a-select v-model:value="selectedTemplate.chartType" placeholder="请选择图表类型">
                        <a-select-optgroup v-for="category in chartTypeOptions" :key="category.type" :label="category.name">
                          <a-select-option v-for="chart in category.charts" :key="chart.id" :value="chart.id">
                            {{ chart.name }} ({{ chart.id }})
                          </a-select-option>
                        </a-select-optgroup>
                      </a-select>
                    </a-form-item>
                  </a-col>
                </a-row>
                
                <a-form-item label="描述">
                  <a-textarea
                    v-model:value="selectedTemplate.description"
                    placeholder="请输入模板描述"
                    :rows="2"
                  />
                </a-form-item>
              </a-form>

              <!-- JSON编辑器 -->
              <a-form-item label="模板内容" class="json-editor-form-item">
                <div class="json-editor-container">
                  <a-textarea
                    v-model:value="templateContent"
                    placeholder="请输入JSON模板内容"
                    :rows="25"
                    class="json-editor"
                    spellcheck="false"
                  />
                </div>
              </a-form-item>
            </div>
            
            <div v-else class="empty-editor">
              <a-empty description="请选择一个模板进行编辑">
                <template #image>
                  <FileTextOutlined style="font-size: 64px; color: #d9d9d9;" />
                </template>
              </a-empty>
            </div>
          </div>
        </a-card>
      </div>

      <!-- 右侧面板：预览和工具 -->
      <div class="right-panel">
        <a-card title="模板分析" size="small" class="analysis-card">
          <template #extra>
            <a-space>
              <a-button size="small" @click="analyzeTemplate" :disabled="!selectedTemplate">
                <ScanOutlined />
                分析
              </a-button>
              <a-button size="small" @click="previewTemplate" :disabled="!selectedTemplate">
                <EyeOutlined />
                预览
              </a-button>
            </a-space>
          </template>

          <div class="analysis-content">
            <div v-if="selectedTemplate && analysisResult" class="analysis-result">
              <!-- 基本信息 -->
              <a-descriptions title="基本信息" :column="1" size="small" bordered>
                <a-descriptions-item label="模板名称">
                  {{ selectedTemplate.name }}
                </a-descriptions-item>
                <a-descriptions-item label="图表类型">
                  <a-tag :color="getTemplateTypeColor(selectedTemplate.chartType)">
                    {{ getTemplateTypeName(selectedTemplate.chartType) }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="模板类别">
                  {{ getTemplateCategoryName(selectedTemplate.chartType) }}
                </a-descriptions-item>
                <a-descriptions-item label="实现状态">
                  <a-tag :color="getStatusColor(getImplementationStatus(selectedTemplate.chartType))">
                    {{ getStatusText(getImplementationStatus(selectedTemplate.chartType)) }}
                  </a-tag>
                </a-descriptions-item>
              </a-descriptions>

              <!-- 占位符信息 -->
              <a-divider orientation="left" orientation-margin="0" style="margin: 16px 0 12px 0;">
                <span style="font-size: 14px; font-weight: 500;">占位符信息</span>
              </a-divider>
              
              <div class="placeholder-info">
                <div class="placeholder-summary">
                  <a-statistic
                    title="占位符总数"
                    :value="analysisResult.placeholders?.length || 0"
                    :value-style="{ fontSize: '18px' }"
                  />
                </div>
                
                <div v-if="analysisResult.placeholders && analysisResult.placeholders.length > 0" class="placeholder-list">
                  <a-list
                    :data-source="analysisResult.placeholders"
                    size="small"
                    :pagination="{ pageSize: 6, size: 'small' }"
                  >
                    <template #renderItem="{ item }">
                      <a-list-item class="placeholder-item">
                        <a-typography-text code class="placeholder-text">
                          {{ item }}
                        </a-typography-text>
                      </a-list-item>
                    </template>
                  </a-list>
                </div>
              </div>

              <!-- 结构信息 -->
              <a-divider orientation="left" orientation-margin="0" style="margin: 16px 0 12px 0;">
                <span style="font-size: 14px; font-weight: 500;">结构信息</span>
              </a-divider>
              
              <a-descriptions :column="1" size="small" bordered>
                <a-descriptions-item label="JSON有效性">
                  <a-tag :color="analysisResult.isValidJson ? 'green' : 'red'">
                    {{ analysisResult.isValidJson ? '有效' : '无效' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="字段总数">
                  {{ analysisResult.fieldCount || 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="嵌套层级">
                  {{ analysisResult.maxDepth || 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内容大小">
                  {{ formatFileSize(analysisResult.contentSize || 0) }}
                </a-descriptions-item>
              </a-descriptions>
            </div>
            
            <div v-else-if="selectedTemplate" class="no-analysis">
              <a-empty description="点击'分析'按钮分析模板结构" image-style="height: 100px;">
                <template #image>
                  <ScanOutlined style="font-size: 48px; color: #d9d9d9;" />
                </template>
              </a-empty>
            </div>
            
            <div v-else class="empty-analysis">
              <a-empty description="请选择一个模板查看分析结果" image-style="height: 100px;" />
            </div>
          </div>
        </a-card>

        <!-- 工具面板 -->
        <a-card title="工具箱" size="small" class="tools-card" style="margin-top: 16px;">
          <div class="tools-content">
            <a-space direction="vertical" style="width: 100%;" size="middle">
              <!-- 导入工具 -->
              <div class="tool-section">
                <h4 class="tool-title">
                  <ImportOutlined />
                  导入工具
                </h4>
                <a-space direction="vertical" style="width: 100%;" size="small">
                  <a-button block size="small" @click="importFromJolt" :disabled="!selectedTemplate">
                    从JOLT规范导入
                  </a-button>
                  <a-button block size="small" @click="importFromExample" :disabled="!selectedTemplate">
                    从ECharts示例导入
                  </a-button>
                  <a-upload
                    :before-upload="handleFileImport"
                    :show-upload-list="false"
                    accept=".json"
                  >
                    <a-button block size="small">
                      从本地文件导入
                    </a-button>
                  </a-upload>
                </a-space>
              </div>

              <!-- 导出工具 -->
              <div class="tool-section">
                <h4 class="tool-title">
                  <ExportOutlined />
                  导出工具
                </h4>
                <a-space direction="vertical" style="width: 100%;" size="small">
                  <a-button block size="small" @click="exportAsJson" :disabled="!selectedTemplate">
                    导出为JSON文件
                  </a-button>
                  <a-button block size="small" @click="exportAsTemplate" :disabled="!selectedTemplate">
                    导出为模板文件
                  </a-button>
                </a-space>
              </div>

              <!-- 其他工具 -->
              <div class="tool-section">
                <h4 class="tool-title">
                  <ToolOutlined />
                  其他工具
                </h4>
                <a-space direction="vertical" style="width: 100%;" size="small">
                  <a-button block size="small" @click="generateDefaultTemplate">
                    生成默认模板
                  </a-button>
                  <a-button block size="small" @click="optimizeTemplate" :disabled="!selectedTemplate">
                    优化模板结构
                  </a-button>
                  <a-button block size="small" @click="compareTemplates" :disabled="filteredTemplates.length < 2">
                    模板对比
                  </a-button>
                </a-space>
              </div>
            </a-space>
          </div>
        </a-card>
      </div>
    </div>

    <!-- 新建模板模态框 -->
    <a-modal
      v-model:open="createModalVisible"
      title="新建模板"
      width="600px"
      @ok="handleCreateTemplate"
      @cancel="handleCreateCancel"
      :confirm-loading="creating"
    >
      <a-form ref="createFormRef" :model="newTemplate" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="模板名称"
              name="name"
              :rules="[{ required: true, message: '请输入模板名称' }]"
            >
              <a-input v-model:value="newTemplate.name" placeholder="请输入模板名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="图表ID"
              name="chartId"
              :rules="[
                { required: true, message: '请输入图表ID' },
                { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
              ]"
            >
              <a-input v-model:value="newTemplate.chartId" placeholder="如: my_line_chart" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item
          label="图表类型"
          name="chartType"
          :rules="[{ required: true, message: '请选择图表类型' }]"
        >
          <a-select v-model:value="newTemplate.chartType" placeholder="请选择图表类型">
            <a-select-optgroup v-for="category in chartTypeOptions" :key="category.type" :label="category.name">
              <a-select-option v-for="chart in category.charts" :key="chart.id" :value="chart.id">
                {{ chart.name }} ({{ chart.id }})
              </a-select-option>
            </a-select-optgroup>
          </a-select>
        </a-form-item>
        
        <a-form-item label="描述">
          <a-textarea
            v-model:value="newTemplate.description"
            placeholder="请输入模板描述"
            :rows="3"
          />
        </a-form-item>
        
        <a-form-item label="初始内容">
          <a-radio-group v-model:value="newTemplate.initType">
            <a-radio value="empty">空模板</a-radio>
            <a-radio value="default">默认模板</a-radio>
            <a-radio value="copy">复制现有模板</a-radio>
          </a-radio-group>
        </a-form-item>
        
        <a-form-item v-if="newTemplate.initType === 'copy'" label="复制源">
          <a-select v-model:value="newTemplate.copySource" placeholder="选择要复制的模板">
            <a-select-option v-for="template in templateList" :key="template.chartId" :value="template.chartId">
              {{ template.name }} ({{ template.chartId }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览模态框 -->
    <a-modal
      v-model:open="previewModalVisible"
      title="模板预览"
      width="1000px"
      :footer="null"
      class="preview-modal"
    >
      <div v-if="previewData" class="preview-content">
        <a-tabs>
          <a-tab-pane key="formatted" tab="格式化预览">
            <pre class="formatted-preview">{{ previewData.formatted }}</pre>
          </a-tab-pane>
          <a-tab-pane key="tree" tab="结构树">
            <a-tree
              :tree-data="previewData.tree"
              :show-line="true"
              :default-expand-all="true"
              class="structure-tree"
            />
          </a-tab-pane>
          <a-tab-pane key="placeholders" tab="占位符">
            <a-list
              :data-source="previewData.placeholders"
              size="small"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-typography-text code>{{ item }}</a-typography-text>
                </a-list-item>
              </template>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  CheckCircleOutlined,
  CopyOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ExportOutlined,
  EyeOutlined,
  FileTextOutlined,
  FormatPainterOutlined,
  ImportOutlined,
  MoreOutlined,
  PlusOutlined,
  ReloadOutlined,
  SaveOutlined,
  ScanOutlined,
  SearchOutlined,
  ToolOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { templateApi } from '@/api'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const creating = ref(false)
const searchKeyword = ref('')
const selectedCategoryKeys = ref<string[]>([])
const expandedKeys = ref<string[]>(['cartesian', 'pie', 'radar', 'gauge'])
const selectedTemplate = ref<any>(null)
const templateList = ref<any[]>([])
const viewMode = ref<'list' | 'card'>('list')
const sortBy = ref<'name' | 'chartType' | 'updateTime'>('name')

// 分页
const listPagination = reactive({
  current: 1,
  pageSize: 10,
  size: 'small' as const,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

const cardPage = ref(1)
const cardPageSize = ref(12)

// 模态框状态
const createModalVisible = ref(false)
const previewModalVisible = ref(false)
const previewData = ref<any>(null)
const analysisResult = ref<any>(null)

// 表单引用和数据
const createFormRef = ref()
const newTemplate = reactive({
  name: '',
  chartId: '',
  chartType: '',
  description: '',
  initType: 'empty',
  copySource: ''
})

// 计算属性
const templateContent = computed({
  get: () => {
    if (!selectedTemplate.value || !selectedTemplate.value.template) {
      return ''
    }
    return typeof selectedTemplate.value.template === 'string' 
      ? selectedTemplate.value.template
      : JSON.stringify(selectedTemplate.value.template, null, 2)
  },
  set: (value: string) => {
    if (selectedTemplate.value) {
      try {
        selectedTemplate.value.template = JSON.parse(value)
      } catch {
        selectedTemplate.value.template = value
      }
    }
  }
})

// 图表类型选项
const chartTypeOptions = ref([
  {
    type: 'cartesian',
    name: 'CARTESIAN (直角坐标系)',
    charts: [
      { id: 'basic_line_chart', name: '基础折线图' },
      { id: 'smooth_line_chart', name: '平滑折线图' },
      { id: 'stacked_line_chart', name: '堆叠折线图' },
      { id: 'basic_bar_chart', name: '基础柱状图' },
      { id: 'stacked_bar_chart', name: '堆叠柱状图' },
      { id: 'basic_area_chart', name: '基础面积图' }
    ]
  },
  {
    type: 'pie',
    name: 'PIE (饼图类)',
    charts: [
      { id: 'basic_pie_chart', name: '基础饼图' },
      { id: 'doughnut_chart', name: '环形图' },
      { id: 'rose_chart', name: '玫瑰图' },
      { id: 'pie_chart', name: '饼图(兼容)' }
    ]
  },
  {
    type: 'radar',
    name: 'RADAR (雷达图类)',
    charts: [
      { id: 'basic_radar_chart', name: '基础雷达图' },
      { id: 'filled_radar_chart', name: '填充雷达图' }
    ]
  },
  {
    type: 'gauge',
    name: 'GAUGE (仪表盘类)',
    charts: [
      { id: 'basic_gauge_chart', name: '基础仪表盘' },
      { id: 'progress_gauge_chart', name: '进度仪表盘' },
      { id: 'grade_gauge_chart', name: '等级仪表盘' }
    ]
  }
])

// 分类树数据
const categoryTreeData = computed(() => [
  {
    title: 'CARTESIAN (直角坐标系)',
    key: 'cartesian',
    count: templateList.value.filter(t => ['basic_line_chart', 'smooth_line_chart', 'stacked_line_chart', 'basic_bar_chart', 'stacked_bar_chart', 'basic_area_chart'].includes(t.chartType)).length,
    children: [
      { title: '基础折线图', key: 'basic_line_chart', status: 'completed' },
      { title: '平滑折线图', key: 'smooth_line_chart', status: 'completed' },
      { title: '堆叠折线图', key: 'stacked_line_chart', status: 'completed' },
      { title: '基础柱状图', key: 'basic_bar_chart', status: 'completed' },
      { title: '堆叠柱状图', key: 'stacked_bar_chart', status: 'completed' },
      { title: '基础面积图', key: 'basic_area_chart', status: 'pending' }
    ]
  },
  {
    title: 'PIE (饼图类)',
    key: 'pie',
    count: templateList.value.filter(t => ['basic_pie_chart', 'doughnut_chart', 'rose_chart', 'pie_chart'].includes(t.chartType)).length,
    children: [
      { title: '基础饼图', key: 'basic_pie_chart', status: 'development' },
      { title: '环形图', key: 'doughnut_chart', status: 'development' },
      { title: '玫瑰图', key: 'rose_chart', status: 'pending' },
      { title: '饼图(兼容)', key: 'pie_chart', status: 'completed' }
    ]
  },
  {
    title: 'RADAR (雷达图类)',
    key: 'radar',
    count: templateList.value.filter(t => ['basic_radar_chart', 'filled_radar_chart'].includes(t.chartType)).length,
    children: [
      { title: '基础雷达图', key: 'basic_radar_chart', status: 'development' },
      { title: '填充雷达图', key: 'filled_radar_chart', status: 'pending' }
    ]
  },
  {
    title: 'GAUGE (仪表盘类)',
    key: 'gauge',
    count: templateList.value.filter(t => ['basic_gauge_chart', 'progress_gauge_chart', 'grade_gauge_chart'].includes(t.chartType)).length,
    children: [
      { title: '基础仪表盘', key: 'basic_gauge_chart', status: 'development' },
      { title: '进度仪表盘', key: 'progress_gauge_chart', status: 'development' },
      { title: '等级仪表盘', key: 'grade_gauge_chart', status: 'development' }
    ]
  }
])

// 过滤后的模板列表
const filteredTemplates = computed(() => {
  let list = [...templateList.value]
  
  // 分类过滤
  if (selectedCategoryKeys.value.length > 0) {
    const selectedKey = selectedCategoryKeys.value[0]
    if (selectedKey && selectedKey !== 'cartesian' && selectedKey !== 'pie' && selectedKey !== 'radar' && selectedKey !== 'gauge') {
      list = list.filter(t => t.chartType === selectedKey)
    } else if (['cartesian', 'pie', 'radar', 'gauge'].includes(selectedKey)) {
      const chartTypes = chartTypeOptions.value.find(c => c.type === selectedKey)?.charts.map(c => c.id) || []
      list = list.filter(t => chartTypes.includes(t.chartType))
    }
  }
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(t => 
      t.name.toLowerCase().includes(keyword) ||
      t.chartType.toLowerCase().includes(keyword) ||
      t.description.toLowerCase().includes(keyword)
    )
  }
  
  // 排序
  list.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'chartType':
        return a.chartType.localeCompare(b.chartType)
      case 'updateTime':
        return new Date(b.updatedAt || b.updateTime || 0).getTime() - new Date(a.updatedAt || a.updateTime || 0).getTime()
      default:
        return 0
    }
  })
  
  return list
})

// 卡片视图分页数据
const paginatedTemplates = computed(() => {
  const start = (cardPage.value - 1) * cardPageSize.value
  const end = start + cardPageSize.value
  return filteredTemplates.value.slice(start, end)
})

// 模板总数
const totalTemplatesCount = computed(() => templateList.value.length)

// 工具方法
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    completed: '#52c41a',
    development: '#1890ff',
    pending: '#faad14'
  }
  return colorMap[status] || '#d9d9d9'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    completed: '完整实现',
    development: '开发中',
    pending: '待开发'
  }
  return textMap[status] || '未知'
}

const getTemplateTypeColor = (chartType: string) => {
  const type = getTemplateCategory(chartType)
  const colorMap: Record<string, string> = {
    cartesian: '#1890ff',
    pie: '#fa8c16',
    radar: '#722ed1',
    gauge: '#13c2c2'
  }
  return colorMap[type] || '#d9d9d9'
}

const getTemplateTypeName = (chartType: string) => {
  const nameMap: Record<string, string> = {
    basic_line_chart: '基础折线图',
    smooth_line_chart: '平滑折线图',
    stacked_line_chart: '堆叠折线图',
    basic_bar_chart: '基础柱状图',
    stacked_bar_chart: '堆叠柱状图',
    basic_area_chart: '基础面积图',
    basic_pie_chart: '基础饼图',
    doughnut_chart: '环形图',
    rose_chart: '玫瑰图',
    pie_chart: '饼图',
    basic_radar_chart: '基础雷达图',
    filled_radar_chart: '填充雷达图',
    basic_gauge_chart: '基础仪表盘',
    progress_gauge_chart: '进度仪表盘',
    grade_gauge_chart: '等级仪表盘'
  }
  return nameMap[chartType] || chartType
}

const getTemplateCategory = (chartType: string) => {
  const categoryMap: Record<string, string> = {
    basic_line_chart: 'cartesian',
    smooth_line_chart: 'cartesian',
    stacked_line_chart: 'cartesian',
    basic_bar_chart: 'cartesian',
    stacked_bar_chart: 'cartesian',
    basic_area_chart: 'cartesian',
    basic_pie_chart: 'pie',
    doughnut_chart: 'pie',
    rose_chart: 'pie',
    pie_chart: 'pie',
    basic_radar_chart: 'radar',
    filled_radar_chart: 'radar',
    basic_gauge_chart: 'gauge',
    progress_gauge_chart: 'gauge',
    grade_gauge_chart: 'gauge'
  }
  return categoryMap[chartType] || 'unknown'
}

const getTemplateCategoryName = (chartType: string) => {
  const category = getTemplateCategory(chartType)
  const nameMap: Record<string, string> = {
    cartesian: 'CARTESIAN (直角坐标系)',
    pie: 'PIE (饼图类)',
    radar: 'RADAR (雷达图类)',
    gauge: 'GAUGE (仪表盘类)'
  }
  return nameMap[category] || '未知类别'
}

const getImplementationStatus = (chartType: string) => {
  const statusMap: Record<string, string> = {
    basic_line_chart: 'completed',
    smooth_line_chart: 'completed',
    stacked_line_chart: 'completed',
    basic_bar_chart: 'completed',
    stacked_bar_chart: 'completed',
    basic_area_chart: 'pending',
    basic_pie_chart: 'development',
    doughnut_chart: 'development',
    rose_chart: 'pending',
    pie_chart: 'completed',
    basic_radar_chart: 'development',
    filled_radar_chart: 'pending',
    basic_gauge_chart: 'development',
    progress_gauge_chart: 'development',
    grade_gauge_chart: 'development'
  }
  return statusMap[chartType] || 'pending'
}

const getChartIcon = (chartType: string) => {
  // 根据图表类型返回对应的图标组件名
  const iconMap: Record<string, string> = {
    basic_line_chart: 'LineChartOutlined',
    smooth_line_chart: 'LineChartOutlined',
    stacked_line_chart: 'LineChartOutlined',
    basic_bar_chart: 'BarChartOutlined',
    stacked_bar_chart: 'BarChartOutlined',
    basic_area_chart: 'AreaChartOutlined',
    basic_pie_chart: 'PieChartOutlined',
    doughnut_chart: 'PieChartOutlined',
    rose_chart: 'PieChartOutlined',
    pie_chart: 'PieChartOutlined',
    basic_radar_chart: 'RadarChartOutlined',
    filled_radar_chart: 'RadarChartOutlined',
    basic_gauge_chart: 'DashboardOutlined',
    progress_gauge_chart: 'DashboardOutlined',
    grade_gauge_chart: 'DashboardOutlined'
  }
  return iconMap[chartType] || 'FileTextOutlined'
}

const formatTime = (time: any) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString()
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 事件处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
}

const handleCategorySelect = (keys: string[]) => {
  selectedCategoryKeys.value = keys
}

const handleSortChange = () => {
  // 排序逻辑已在计算属性中实现
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'list' ? 'card' : 'list'
}

const handleCardMenuClick = ({ key }: { key: string }, template: any) => {
  switch (key) {
    case 'edit':
      editTemplate(template)
      break
    case 'duplicate':
      duplicateTemplate(template)
      break
    case 'export':
      exportTemplate(template)
      break
    case 'delete':
      deleteTemplate(template.chartId)
      break
  }
}

const selectTemplate = (template: any) => {
  selectedTemplate.value = { ...template }
  analysisResult.value = null // 重置分析结果
}

const editTemplate = (template: any) => {
  selectedTemplate.value = { ...template }
}

const duplicateTemplate = (template: any) => {
  const newId = `${template.chartId}_copy_${Date.now()}`
  const duplicated = {
    ...template,
    chartId: newId,
    name: `${template.name} (副本)`,
    updatedAt: new Date().toISOString()
  }
  createTemplate(duplicated)
}

const exportTemplate = async (template: any) => {
  try {
    const content = JSON.stringify(template, null, 2)
    const blob = new Blob([content], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${template.chartId}_template.json`
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  }
}

const deleteTemplate = async (chartId: string) => {
  try {
    await templateApi.delete(chartId)
    templateList.value = templateList.value.filter(t => t.chartId !== chartId)
    if (selectedTemplate.value?.chartId === chartId) {
      selectedTemplate.value = null
    }
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

const showCreateModal = () => {
  createModalVisible.value = true
  Object.assign(newTemplate, {
    name: '',
    chartId: '',
    chartType: '',
    description: '',
    initType: 'empty',
    copySource: ''
  })
}

const handleCreateTemplate = async () => {
  try {
    await createFormRef.value.validate()
    creating.value = true
    
    const template: any = {
      chartId: newTemplate.chartId,
      name: newTemplate.name,
      description: newTemplate.description,
      chartType: newTemplate.chartType,
      template: getInitialTemplate()
    }
    
    await createTemplate(template)
    createModalVisible.value = false
    message.success('模板创建成功')
  } catch (error) {
    console.error('创建模板失败:', error)
  } finally {
    creating.value = false
  }
}

const handleCreateCancel = () => {
  createModalVisible.value = false
}

const getInitialTemplate = () => {
  switch (newTemplate.initType) {
    case 'empty':
      return {}
    case 'default':
      return getDefaultTemplateForChartType(newTemplate.chartType)
    case 'copy':
      const source = templateList.value.find(t => t.chartId === newTemplate.copySource)
      return source ? { ...source.template } : {}
    default:
      return {}
  }
}

const getDefaultTemplateForChartType = (chartType: string) => {
  const defaultTemplates: Record<string, any> = {
    basic_line_chart: {
      chartMeta: {
        title: "${chart_title}",
        type: "line"
      },
      categories: "${categories}",
      series: [
        {
          name: "${series_1_name}",
          data: "${series_1_data}",
          type: "line"
        }
      ]
    },
    basic_bar_chart: {
      chartMeta: {
        title: "${chart_title}",
        type: "bar"
      },
      categories: "${categories}",
      series: [
        {
          name: "${series_1_name}",
          data: "${series_1_data}",
          type: "bar"
        }
      ]
    },
    basic_pie_chart: {
      chartMeta: {
        title: "${chart_title}",
        type: "pie"
      },
      data: "${pie_data}"
    }
  }
  return defaultTemplates[chartType] || {}
}

const createTemplate = async (template: any) => {
  try {
    const created = await templateApi.create(template)
    templateList.value.push(created)
    selectedTemplate.value = { ...created }
  } catch (error) {
    message.error('创建模板失败')
    throw error
  }
}

const formatTemplate = () => {
  if (!selectedTemplate.value) return
  try {
    const parsed = typeof selectedTemplate.value.template === 'string' 
      ? JSON.parse(selectedTemplate.value.template)
      : selectedTemplate.value.template
    selectedTemplate.value.template = parsed
    message.success('格式化成功')
  } catch (error) {
    message.error('JSON格式错误，无法格式化')
  }
}

const validateTemplate = () => {
  if (!selectedTemplate.value) return
  try {
    const content = templateContent.value
    JSON.parse(content)
    message.success('JSON格式验证通过')
  } catch (error) {
    message.error('JSON格式错误')
  }
}

const saveTemplate = async () => {
  if (!selectedTemplate.value) return
  saving.value = true
  try {
    const updated = await templateApi.update(selectedTemplate.value.chartId, selectedTemplate.value)
    const index = templateList.value.findIndex(t => t.chartId === selectedTemplate.value.chartId)
    if (index !== -1) {
      templateList.value[index] = { ...updated }
      selectedTemplate.value = { ...updated }
    }
    message.success('保存成功')
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const analyzeTemplate = () => {
  if (!selectedTemplate.value) return
  
  try {
    const content = templateContent.value
    const parsed = JSON.parse(content)
    
    // 提取占位符
    const placeholders = extractPlaceholders(content)
    
    // 分析结构
    const fieldCount = countFields(parsed)
    const maxDepth = getMaxDepth(parsed)
    const contentSize = new Blob([content]).size
    
    analysisResult.value = {
      placeholders,
      isValidJson: true,
      fieldCount,
      maxDepth,
      contentSize
    }
    
    message.success('模板分析完成')
  } catch (error) {
    analysisResult.value = {
      placeholders: [],
      isValidJson: false,
      fieldCount: 0,
      maxDepth: 0,
      contentSize: 0
    }
    message.error('JSON格式错误，分析失败')
  }
}

const extractPlaceholders = (content: string): string[] => {
  const regex = /\$\{([^}]+)\}/g
  const matches = content.match(regex)
  return matches ? [...new Set(matches)] : []
}

const countFields = (obj: any, count = 0): number => {
  if (typeof obj === 'object' && obj !== null) {
    if (Array.isArray(obj)) {
      obj.forEach(item => {
        count = countFields(item, count)
      })
    } else {
      count += Object.keys(obj).length
      Object.values(obj).forEach(value => {
        count = countFields(value, count)
      })
    }
  }
  return count
}

const getMaxDepth = (obj: any, depth = 1): number => {
  if (typeof obj !== 'object' || obj === null) {
    return depth
  }
  
  if (Array.isArray(obj)) {
    return Math.max(...obj.map(item => getMaxDepth(item, depth + 1)), depth)
  } else {
    const depths = Object.values(obj).map(value => getMaxDepth(value, depth + 1))
    return depths.length > 0 ? Math.max(...depths) : depth
  }
}

const previewTemplate = () => {
  if (!selectedTemplate.value) return
  
  try {
    const content = templateContent.value
    const parsed = JSON.parse(content)
    
    previewData.value = {
      formatted: JSON.stringify(parsed, null, 2),
      tree: convertToTreeData(parsed),
      placeholders: extractPlaceholders(content)
    }
    
    previewModalVisible.value = true
  } catch (error) {
    message.error('JSON格式错误，无法预览')
  }
}

const convertToTreeData = (obj: any, parentKey = 'root'): any[] => {
  if (typeof obj !== 'object' || obj === null) {
    return [{
      title: `${parentKey}: ${obj}`,
      key: `${parentKey}_${Math.random()}`
    }]
  }
  
  if (Array.isArray(obj)) {
    return obj.map((item, index) => ({
      title: `[${index}]`,
      key: `${parentKey}_${index}`,
      children: convertToTreeData(item, `${parentKey}[${index}]`)
    }))
  }
  
  return Object.entries(obj).map(([key, value]) => ({
    title: key,
    key: `${parentKey}_${key}`,
    children: convertToTreeData(value, key)
  }))
}

const refreshData = async () => {
  await loadTemplateList()
}

const loadTemplateList = async () => {
  loading.value = true
  try {
    const list = await templateApi.list()
    templateList.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载模板列表失败:', error)
    // 使用模拟数据
    templateList.value = getMockTemplateList()
    message.warning('使用模拟数据，请检查后端服务')
  } finally {
    loading.value = false
  }
}

const getMockTemplateList = () => [
  {
    chartId: 'stacked_line_chart',
    name: '堆叠折线图模板',
    chartType: 'stacked_line_chart',
    description: '用于展示多系列数据的堆叠折线图模板',
    template: {
      chartMeta: {
        title: "${chart_title}",
        type: "line"
      },
      categories: "${categories}",
      series: [
        {
          name: "${series_1_name}",
          data: "${series_1_data}",
          type: "line",
          stack: "${stack_group}"
        }
      ]
    },
    updatedAt: new Date().toISOString()
  },
  {
    chartId: 'basic_bar_chart',
    name: '基础柱状图模板',
    chartType: 'basic_bar_chart',
    description: '用于展示分类数据的基础柱状图模板',
    template: {
      chartMeta: {
        title: "${chart_title}",
        type: "bar"
      },
      categories: "${categories}",
      series: [
        {
          name: "${series_1_name}",
          data: "${series_1_data}",
          type: "bar"
        }
      ]
    },
    updatedAt: new Date().toISOString()
  }
]

// 工具方法实现
const importFromJolt = async () => {
  if (!selectedTemplate.value) return
  try {
    const result = await templateApi.importFromJolt(selectedTemplate.value.chartId)
    selectedTemplate.value.template = result
    message.success('从JOLT规范导入成功')
  } catch (error) {
    message.error('导入失败')
  }
}

const importFromExample = async () => {
  if (!selectedTemplate.value) return
  try {
    const result = await templateApi.importFromExample(selectedTemplate.value.chartId)
    selectedTemplate.value.template = result
    message.success('从ECharts示例导入成功')
  } catch (error) {
    message.error('导入失败')
  }
}

const handleFileImport = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const content = e.target?.result as string
      const parsed = JSON.parse(content)
      if (selectedTemplate.value) {
        selectedTemplate.value.template = parsed
        message.success('文件导入成功')
      }
    } catch (error) {
      message.error('文件格式错误')
    }
  }
  reader.readAsText(file)
  return false // 阻止默认上传
}

const exportAsJson = () => {
  exportTemplate(selectedTemplate.value)
}

const exportAsTemplate = async () => {
  if (!selectedTemplate.value) return
  try {
    const result = await templateApi.exportTemplate(selectedTemplate.value.chartId)
    const content = JSON.stringify(result, null, 2)
    const blob = new Blob([content], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${selectedTemplate.value.chartId}_export.json`
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  }
}

const generateDefaultTemplate = () => {
  const defaultTemplate = {
    chartId: `new_template_${Date.now()}`,
    name: '新建默认模板',
    chartType: 'basic_line_chart',
    description: '系统生成的默认模板',
    template: getDefaultTemplateForChartType('basic_line_chart')
  }
  showCreateModal()
  Object.assign(newTemplate, defaultTemplate)
}

const optimizeTemplate = () => {
  if (!selectedTemplate.value) return
  try {
    // 简单的优化：移除空字段，格式化JSON
    const content = templateContent.value
    const parsed = JSON.parse(content)
    const optimized = removeEmptyFields(parsed)
    selectedTemplate.value.template = optimized
    message.success('模板优化完成')
  } catch (error) {
    message.error('优化失败')
  }
}

const removeEmptyFields = (obj: any): any => {
  if (typeof obj !== 'object' || obj === null) {
    return obj
  }
  
  if (Array.isArray(obj)) {
    return obj.map(removeEmptyFields).filter(item => item !== null && item !== undefined)
  }
  
  const result: any = {}
  for (const [key, value] of Object.entries(obj)) {
    if (value !== null && value !== undefined && value !== '') {
      result[key] = removeEmptyFields(value)
    }
  }
  return result
}

const compareTemplates = () => {
  message.info('模板对比功能开发中...')
}

// 监听器
watch(selectedTemplate, (newVal) => {
  if (newVal) {
    analysisResult.value = null
  }
}, { deep: true })

// 生命周期
onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped>
.template-management-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.page-header {
  background: white;
  padding: 24px;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1600px;
  margin: 0 auto;
}

.title-section {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #1890ff;
}

.page-subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.header-actions {
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  min-height: 0;
}

.left-panel {
  width: 280px;
  flex-shrink: 0;
}

.center-left-panel {
  width: 380px;
  flex-shrink: 0;
}

.center-right-panel {
  flex: 1;
  min-width: 500px;
}

.right-panel {
  width: 320px;
  flex-shrink: 0;
}

.category-card,
.template-list-card,
.editor-card,
.analysis-card,
.tools-card {
  height: 100%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.category-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 57px);
  overflow-y: auto;
}

.category-tree {
  height: 100%;
}

.tree-node-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-title {
  flex: 1;
  font-size: 13px;
}

.node-info,
.node-count {
  flex-shrink: 0;
}

.template-list-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 57px);
}

.template-list-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-view {
  flex: 1;
  overflow: hidden;
}

.list-view .ant-list {
  height: 100%;
}

.template-list-item {
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 8px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.template-list-item:hover {
  background: #fafafa;
  border-color: #d9d9d9;
}

.active-template {
  background: #f0f8ff !important;
  border-color: #1890ff !important;
}

.item-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.item-description {
  margin-top: 4px;
}

.description-text {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  line-height: 1.4;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
}

.chart-id {
  font-family: 'Monaco', 'Menlo', monospace;
}

.template-avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  background: #f0f8ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-icon {
  font-size: 20px;
  color: #1890ff;
}

.card-view {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.template-card {
  height: 140px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e8e8;
}

.template-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24,144,255,0.1);
}

.active-template-card {
  border-color: #1890ff !important;
  box-shadow: 0 4px 12px rgba(24,144,255,0.15) !important;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  font-size: 16px;
  color: #1890ff;
}

.card-name {
  font-size: 13px;
  font-weight: 500;
  color: #262626;
}

.card-content {
  height: calc(100% - 20px);
  display: flex;
  flex-direction: column;
}

.type-tag {
  margin-bottom: 8px;
  font-size: 11px;
}

.card-description {
  flex: 1;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.meta-item {
  color: #999;
  font-size: 11px;
}

.card-pagination {
  margin-top: 16px;
  text-align: center;
}

.editor-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 57px);
}

.editor-content {
  height: 100%;
}

.template-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.template-form {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.json-editor-form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 0 !important;
}

.json-editor-form-item .ant-form-item-control {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.json-editor-container {
  flex: 1;
  display: flex;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
}

.json-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  border: none;
  background: transparent;
  resize: none;
  flex: 1;
}

.json-editor:focus {
  border: none;
  box-shadow: none;
}

.empty-editor {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analysis-card .ant-card-body,
.tools-card .ant-card-body {
  padding: 16px;
  height: calc(100% - 57px);
  overflow-y: auto;
}

.analysis-content {
  height: 100%;
}

.placeholder-info {
  margin-top: 12px;
}

.placeholder-summary {
  margin-bottom: 16px;
}

.placeholder-list {
  max-height: 200px;
  overflow-y: auto;
}

.placeholder-item {
  padding: 4px 0 !important;
}

.placeholder-text {
  font-size: 11px !important;
  background: #f0f8ff !important;
  color: #1890ff !important;
}

.no-analysis,
.empty-analysis {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tools-content {
  height: 100%;
}

.tool-section {
  padding: 12px 0;
}

.tool-section:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.tool-title {
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 500;
  color: #262626;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-modal .ant-modal-body {
  padding: 24px;
}

.preview-content {
  max-height: 600px;
  overflow: auto;
}

.formatted-preview {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 500px;
  overflow: auto;
  margin: 0;
  white-space: pre-wrap;
}

.structure-tree {
  max-height: 500px;
  overflow: auto;
}

.danger-menu-item {
  color: #ff4d4f !important;
}

.total-label {
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .main-content {
    flex-wrap: wrap;
  }
  
  .left-panel,
  .right-panel {
    width: 100%;
    order: 3;
  }
  
  .center-left-panel {
    width: 50%;
  }
  
  .center-right-panel {
    width: 50%;
  }
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .left-panel,
  .center-left-panel,
  .center-right-panel,
  .right-panel {
    width: 100%;
    height: 400px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>