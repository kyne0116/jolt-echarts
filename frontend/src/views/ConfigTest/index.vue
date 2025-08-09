<template>
  <div class="config-test-container">
    <a-card title="图表配置服务测试" style="margin: 20px;">
      <a-space direction="vertical" style="width: 100%;">
        
        <!-- 测试按钮 -->
        <a-space>
          <a-button type="primary" @click="testChartTypes" :loading="loading">
            测试图表类型映射
          </a-button>
          <a-button @click="testSpecificConfig" :loading="loading">
            测试特定配置
          </a-button>
          <a-button @click="testPreprocessing" :loading="loading">
            测试预处理
          </a-button>
          <a-button @click="clearResults">清除结果</a-button>
        </a-space>

        <!-- 结果显示 -->
        <a-card v-if="results.length > 0" title="测试结果" size="small">
          <div v-for="(result, index) in results" :key="index" style="margin-bottom: 16px;">
            <a-tag :color="result.success ? 'green' : 'red'">
              {{ result.success ? '成功' : '失败' }}
            </a-tag>
            <strong>{{ result.title }}</strong>
            <pre style="background: #f5f5f5; padding: 8px; margin-top: 8px; border-radius: 4px;">{{ result.data }}</pre>
          </div>
        </a-card>

      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import chartConfigService from '@/services/chartConfigService'

const loading = ref(false)
const results = ref<Array<{
  title: string
  success: boolean
  data: string
}>>([])

// 测试图表类型映射
const testChartTypes = async () => {
  loading.value = true
  try {
    const mappings = await chartConfigService.getChartTypeMappings()
    results.value.push({
      title: '图表类型映射测试',
      success: true,
      data: JSON.stringify(mappings, null, 2)
    })
    message.success('图表类型映射测试成功')
  } catch (error) {
    results.value.push({
      title: '图表类型映射测试',
      success: false,
      data: String(error)
    })
    message.error('图表类型映射测试失败')
  } finally {
    loading.value = false
  }
}

// 测试特定配置
const testSpecificConfig = async () => {
  loading.value = true
  try {
    const configs = await Promise.all([
      chartConfigService.getChartSpecificConfig('basic_line_chart'),
      chartConfigService.getChartSpecificConfig('smooth_line_chart'),
      chartConfigService.getChartSpecificConfig('stacked_line_chart')
    ])
    
    results.value.push({
      title: '特定配置测试',
      success: true,
      data: JSON.stringify({
        basic_line_chart: configs[0],
        smooth_line_chart: configs[1],
        stacked_line_chart: configs[2]
      }, null, 2)
    })
    message.success('特定配置测试成功')
  } catch (error) {
    results.value.push({
      title: '特定配置测试',
      success: false,
      data: String(error)
    })
    message.error('特定配置测试失败')
  } finally {
    loading.value = false
  }
}

// 测试预处理
const testPreprocessing = async () => {
  loading.value = true
  try {
    const testData = {
      series: [
        { type: 'line', data: [1, 2, 3, 4, 5], name: 'Test Series' }
      ]
    }
    
    const processed = await chartConfigService.preprocessChartData('smooth_line_chart', testData)
    
    results.value.push({
      title: '预处理测试',
      success: true,
      data: JSON.stringify({
        original: testData,
        processed: processed
      }, null, 2)
    })
    message.success('预处理测试成功')
  } catch (error) {
    results.value.push({
      title: '预处理测试',
      success: false,
      data: String(error)
    })
    message.error('预处理测试失败')
  } finally {
    loading.value = false
  }
}

// 清除结果
const clearResults = () => {
  results.value = []
  chartConfigService.clearCache()
  message.info('结果已清除，缓存已重置')
}
</script>

<style scoped>
.config-test-container {
  min-height: 100vh;
  background: #f0f2f5;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style>
