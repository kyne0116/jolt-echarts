<template>
  <div class="test-chart-page">
    <h2>ECharts 测试页面</h2>
    
    <a-card title="图表测试" style="margin-bottom: 20px;">
      <template #extra>
        <a-space>
          <a-button @click="initTestChart" type="primary">初始化图表</a-button>
          <a-button @click="updateTestChart">更新数据</a-button>
          <a-button @click="clearChart">清除图表</a-button>
        </a-space>
      </template>
      
      <div class="chart-info">
        <p>图表状态: <a-tag :color="chartInstance ? 'green' : 'red'">{{ chartInstance ? '已初始化' : '未初始化' }}</a-tag></p>
        <p>容器状态: <a-tag :color="chartContainer ? 'green' : 'red'">{{ chartContainer ? '已找到' : '未找到' }}</a-tag></p>
      </div>
      
      <div ref="chartContainer" class="test-chart-container"></div>
    </a-card>
    
    <a-card title="测试数据">
      <pre>{{ JSON.stringify(testData, null, 2) }}</pre>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'

const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const testData = ref({
  title: {
    text: '测试图表'
  },
  tooltip: {},
  xAxis: {
    data: ['衬衫', '羊毛衫', '雪纺衫', '裤子', '高跟鞋', '袜子']
  },
  yAxis: {},
  series: [{
    name: '销量',
    type: 'bar',
    data: [5, 20, 36, 10, 10, 20]
  }]
})

const initTestChart = () => {
  if (!chartContainer.value) {
    message.error('图表容器未找到')
    return
  }
  
  try {
    if (chartInstance) {
      chartInstance.dispose()
    }
    
    chartInstance = echarts.init(chartContainer.value)
    chartInstance.setOption(testData.value)
    
    message.success('图表初始化成功')
  } catch (error) {
    console.error('图表初始化失败:', error)
    message.error('图表初始化失败')
  }
}

const updateTestChart = () => {
  if (!chartInstance) {
    message.error('请先初始化图表')
    return
  }
  
  // 更新数据
  const newData = {
    ...testData.value,
    series: [{
      ...testData.value.series[0],
      data: testData.value.series[0].data.map(() => Math.floor(Math.random() * 50))
    }]
  }
  
  testData.value = newData
  chartInstance.setOption(newData)
  message.success('图表数据已更新')
}

const clearChart = () => {
  if (chartInstance) {
    chartInstance.clear()
    message.success('图表已清除')
  }
}

onMounted(async () => {
  await nextTick()
  setTimeout(() => {
    initTestChart()
  }, 100)
})
</script>

<style scoped>
.test-chart-page {
  padding: 20px;
}

.test-chart-container {
  height: 400px;
  width: 100%;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.chart-info {
  margin-bottom: 16px;
}

.chart-info p {
  margin: 8px 0;
}
</style>
