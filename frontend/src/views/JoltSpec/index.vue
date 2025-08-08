<template>
  <div class="jolt-spec-container">
    <div class="page-header">
      <h2>Jolt规范管理</h2>
      <p class="page-description">管理和编辑Jolt转换规范文件</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：规范列表 -->
      <a-col :span="8">
        <a-card title="规范列表" class="spec-list-card">
          <template #extra>
            <a-button type="primary" size="small" @click="showAddModal">
              <PlusOutlined />
              新增
            </a-button>
          </template>
          
          <a-list
            :data-source="specList"
            :loading="loading"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'active-spec': selectedSpec?.id === item.id }"
                @click="selectSpec(item)"
                style="cursor: pointer;"
              >
                <a-list-item-meta>
                  <template #title>
                    <div class="spec-title">
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
                  <a-button type="link" size="small" @click.stop="editSpec(item)">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除吗？"
                    @confirm="deleteSpec(item.id)"
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

      <!-- 右侧：规范编辑器 -->
      <a-col :span="16">
        <a-card title="Jolt规范编辑器" class="editor-card">
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
              <a-button type="primary" @click="saveSpec" :loading="saving">
                <SaveOutlined />
                保存
              </a-button>
            </a-space>
          </template>

          <div v-if="selectedSpec" class="editor-content">
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="规范名称">
                    <a-input v-model:value="selectedSpec.name" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="图表类型">
                    <a-select v-model:value="selectedSpec.chartType">
                      <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
                      <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
                      <a-select-option value="pie_chart">饼图</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>
              
              <a-form-item label="描述">
                <a-textarea v-model:value="selectedSpec.description" :rows="2" />
              </a-form-item>
              
              <a-form-item label="Jolt规范内容">
                <a-textarea
                  v-model:value="selectedSpec.content"
                  :rows="20"
                  placeholder="请输入Jolt转换规范的JSON内容"
                  class="json-editor"
                />
              </a-form-item>
            </a-form>
          </div>
          
          <div v-else class="empty-state">
            <a-empty description="请选择一个规范进行编辑" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增规范模态框 -->
    <a-modal
      v-model:open="modalVisible"
      title="新增Jolt规范"
      @ok="handleAddSpec"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="newSpec" layout="vertical">
        <a-form-item label="规范名称" name="name" :rules="[{ required: true, message: '请输入规范名称' }]">
          <a-input v-model:value="newSpec.name" />
        </a-form-item>
        <a-form-item label="图表类型" name="chartType" :rules="[{ required: true, message: '请选择图表类型' }]">
          <a-select v-model:value="newSpec.chartType">
            <a-select-option value="stacked_line_chart">堆叠折线图</a-select-option>
            <a-select-option value="basic_bar_chart">基础柱状图</a-select-option>
            <a-select-option value="pie_chart">饼图</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="newSpec.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {
    CheckCircleOutlined,
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
const specList = ref<any[]>([])
const selectedSpec = ref<any>(null)
const formRef = ref()

const newSpec = reactive({
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

const loadSpecList = async () => {
  loading.value = true
  try {
    // 模拟数据
    specList.value = [
      {
        id: 1,
        name: 'line-chart-placeholder',
        chartType: 'stacked_line_chart',
        description: '堆叠折线图占位符转换规范',
        content: JSON.stringify([
          {
            "operation": "shift",
            "spec": {
              "chartTitle": "title.text",
              "series": {
                "*": {
                  "seriesName": "series[&1].name",
                  "values": "series[&1].data"
                }
              }
            }
          }
        ], null, 2),
      },
      {
        id: 2,
        name: 'bar-chart-placeholder',
        chartType: 'basic_bar_chart',
        description: '基础柱状图占位符转换规范',
        content: JSON.stringify([
          {
            "operation": "shift",
            "spec": {
              "chartTitle": "title.text",
              "xAxis": "xAxis.data",
              "series": {
                "*": {
                  "seriesName": "series[&1].name",
                  "values": "series[&1].data"
                }
              }
            }
          }
        ], null, 2),
      },
    ]
  } catch (error) {
    message.error('加载规范列表失败')
  } finally {
    loading.value = false
  }
}

const selectSpec = (spec: any) => {
  selectedSpec.value = { ...spec }
}

const editSpec = (spec: any) => {
  selectedSpec.value = { ...spec }
}

const deleteSpec = async (id: number) => {
  try {
    specList.value = specList.value.filter(item => item.id !== id)
    if (selectedSpec.value?.id === id) {
      selectedSpec.value = null
    }
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

const showAddModal = () => {
  modalVisible.value = true
  Object.assign(newSpec, {
    name: '',
    chartType: '',
    description: '',
  })
}

const handleAddSpec = async () => {
  try {
    await formRef.value.validate()
    const newId = Math.max(...specList.value.map(item => item.id)) + 1
    const spec = {
      id: newId,
      ...newSpec,
      content: '[]',
    }
    specList.value.push(spec)
    selectedSpec.value = { ...spec }
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
  if (!selectedSpec.value?.content) return
  try {
    const parsed = JSON.parse(selectedSpec.value.content)
    selectedSpec.value.content = JSON.stringify(parsed, null, 2)
    message.success('格式化成功')
  } catch (error) {
    message.error('JSON格式错误，无法格式化')
  }
}

const validateJson = () => {
  if (!selectedSpec.value?.content) return
  try {
    JSON.parse(selectedSpec.value.content)
    message.success('JSON格式验证通过')
  } catch (error) {
    message.error('JSON格式错误')
  }
}

const saveSpec = async () => {
  if (!selectedSpec.value) return
  saving.value = true
  try {
    // 这里应该调用API保存
    const index = specList.value.findIndex(item => item.id === selectedSpec.value.id)
    if (index !== -1) {
      specList.value[index] = { ...selectedSpec.value }
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
  loadSpecList()
})
</script>

<style scoped>
.jolt-spec-container {
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

.spec-list-card,
.editor-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.active-spec {
  background-color: #f0f8ff;
}

.spec-title {
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
</style>
