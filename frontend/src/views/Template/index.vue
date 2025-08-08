<template>
  <div class="template-container">
    <div class="page-header">
      <h2>通用JSON模板</h2>
      <p class="page-description">管理和编辑图表的通用JSON模板</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：模板列表 -->
      <a-col :span="8">
        <a-card title="模板列表" class="template-list-card">
          <template #extra>
            <a-button type="primary" size="small" @click="showAddModal">
              <PlusOutlined />
              新增
            </a-button>
          </template>
          
          <a-list
            :data-source="templateList"
            :loading="loading"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'active-template': selectedTemplate?.id === item.id }"
                @click="selectTemplate(item)"
                style="cursor: pointer;"
              >
                <a-list-item-meta>
                  <template #title>
                    <div class="template-title">
                      {{ item.name }}
                      <a-tag size="small" :color="getTypeColor(item.chartType)">
                        {{ getTypeName(item.chartType) }}
                      </a-tag>
                    </div>
                  </template>
                  <template #description>
                    {{ item.description }}
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small" @click.stop="editTemplate(item)">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除吗？"
                    @confirm="deleteTemplate(item.id)"
                    @click.stop
                  >
                    <a-button type="link" size="small" danger>
                      删除
                    </a-button>
                  </a-popconfirm>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧：模板编辑器 -->
      <a-col :span="16">
        <a-card title="模板编辑器" class="editor-card">
          <template #extra>
            <a-space>
              <a-button @click="formatJson">
                <FormatPainterOutlined />
                格式化
              </a-button>
              <a-button @click="validateJson">
                <CheckCircleOutlined />
                验证
              </a-button>
              <a-button @click="previewTemplate">
                <EyeOutlined />
                预览
              </a-button>
              <a-button type="primary" @click="saveTemplate" :loading="saving">
                <SaveOutlined />
                保存
              </a-button>
            </a-space>
          </template>

          <div v-if="selectedTemplate" class="editor-content">
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="模板名称">
                    <a-input v-model:value="selectedTemplate.name" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="图表类型">
                    <a-select v-model:value="selectedTemplate.chartType">
                      <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
                      <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
                      <a-select-option value="pie_chart">饼图</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>
              
              <a-form-item label="描述">
                <a-textarea v-model:value="selectedTemplate.description" :rows="2" />
              </a-form-item>
              
              <a-form-item label="模板内容">
                <a-textarea
                  v-model:value="selectedTemplate.content"
                  :rows="20"
                  placeholder="请输入JSON模板内容"
                  class="json-editor"
                />
              </a-form-item>
            </a-form>
          </div>
          
          <div v-else class="empty-state">
            <a-empty description="请选择一个模板进行编辑" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增模板模态框 -->
    <a-modal
      v-model:open="modalVisible"
      title="新增模板"
      @ok="handleAddTemplate"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="newTemplate" layout="vertical">
        <a-form-item label="模板名称" name="name" :rules="[{ required: true, message: '请输入模板名称' }]">
          <a-input v-model:value="newTemplate.name" />
        </a-form-item>
        <a-form-item label="图表类型" name="chartType" :rules="[{ required: true, message: '请选择图表类型' }]">
          <a-select v-model:value="newTemplate.chartType">
            <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
            <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
            <a-select-option value="pie_chart">饼图</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="newTemplate.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="模板预览"
      width="800px"
      :footer="null"
    >
      <div v-if="previewData">
        <a-descriptions title="模板信息" bordered size="small">
          <a-descriptions-item label="名称">{{ previewData.name }}</a-descriptions-item>
          <a-descriptions-item label="类型">{{ getTypeName(previewData.chartType) }}</a-descriptions-item>
          <a-descriptions-item label="描述">{{ previewData.description }}</a-descriptions-item>
        </a-descriptions>
        
        <a-divider>模板内容</a-divider>
        
        <pre class="preview-content">{{ previewData.content }}</pre>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    CheckCircleOutlined,
    EyeOutlined,
    FormatPainterOutlined,
    PlusOutlined,
    SaveOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const previewVisible = ref(false)
const templateList = ref<any[]>([])
const selectedTemplate = ref<any>(null)
const previewData = ref<any>(null)
const formRef = ref()

const newTemplate = reactive({
  name: '',
  chartType: '',
  description: '',
})

// 方法
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    stacked_line_chart: 'blue',
    basic_bar_chart: 'green',
    pie_chart: 'orange',
  }
  return colorMap[type] || 'default'
}

const getTypeName = (type: string) => {
  const nameMap: Record<string, string> = {
    stacked_line_chart: '堆叠折线图',
    basic_bar_chart: '基础柱状图',
    pie_chart: '饼图',
  }
  return nameMap[type] || type
}

const loadTemplateList = async () => {
  loading.value = true
  try {
    // 模拟数据
    templateList.value = [
      {
        id: 1,
        name: '堆叠折线图模板',
        chartType: 'stacked_line_chart',
        description: '用于展示多系列数据的堆叠折线图模板',
        content: JSON.stringify({
          chartTitle: "${chart_title}",
          chartType: "${chart_type}",
          series: [
            {
              seriesName: "${series_name_1}",
              seriesType: "${chart_type}",
              values: "${series_data_1}",
              stackGroup: "${stack_group}",
              seriesId: "series_1"
            }
          ],
          categoryField: "${category_field}"
        }, null, 2),
      },
      {
        id: 2,
        name: '基础柱状图模板',
        chartType: 'basic_bar_chart',
        description: '用于展示分类数据的基础柱状图模板',
        content: JSON.stringify({
          chartTitle: "${chart_title}",
          chartType: "${chart_type}",
          series: [
            {
              seriesName: "${series_name_1}",
              seriesType: "${chart_type}",
              values: "${series_data_1}",
              seriesId: "series_1"
            }
          ],
          categoryField: "${category_field}"
        }, null, 2),
      },
    ]
  } catch (error) {
    message.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

const selectTemplate = (template: any) => {
  selectedTemplate.value = { ...template }
}

const editTemplate = (template: any) => {
  selectedTemplate.value = { ...template }
}

const deleteTemplate = async (id: number) => {
  try {
    templateList.value = templateList.value.filter(item => item.id !== id)
    if (selectedTemplate.value?.id === id) {
      selectedTemplate.value = null
    }
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

const showAddModal = () => {
  modalVisible.value = true
  Object.assign(newTemplate, {
    name: '',
    chartType: '',
    description: '',
  })
}

const handleAddTemplate = async () => {
  try {
    await formRef.value.validate()
    const newId = Math.max(...templateList.value.map(item => item.id)) + 1
    const template = {
      id: newId,
      ...newTemplate,
      content: '{}',
    }
    templateList.value.push(template)
    selectedTemplate.value = { ...template }
    modalVisible.value = false
    message.success('创建成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

const formatJson = () => {
  if (!selectedTemplate.value?.content) return
  try {
    const parsed = JSON.parse(selectedTemplate.value.content)
    selectedTemplate.value.content = JSON.stringify(parsed, null, 2)
    message.success('格式化成功')
  } catch (error) {
    message.error('JSON格式错误，无法格式化')
  }
}

const validateJson = () => {
  if (!selectedTemplate.value?.content) return
  try {
    JSON.parse(selectedTemplate.value.content)
    message.success('JSON格式验证通过')
  } catch (error) {
    message.error('JSON格式错误')
  }
}

const previewTemplate = () => {
  if (!selectedTemplate.value) return
  previewData.value = { ...selectedTemplate.value }
  previewVisible.value = true
}

const saveTemplate = async () => {
  if (!selectedTemplate.value) return
  saving.value = true
  try {
    // 这里应该调用API保存
    const index = templateList.value.findIndex(item => item.id === selectedTemplate.value.id)
    if (index !== -1) {
      templateList.value[index] = { ...selectedTemplate.value }
    }
    message.success('保存成功')
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 生命周期
onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped>
.template-container {
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

.template-list-card,
.editor-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.active-template {
  background-color: #f0f8ff;
}

.template-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.json-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.editor-content {
  min-height: 500px;
}

.preview-content {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  overflow-x: auto;
  max-height: 400px;
}
</style>
