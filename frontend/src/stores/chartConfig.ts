import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChartConfig } from '@/types'

export const useChartConfigStore = defineStore('chartConfig', () => {
  const chartConfigs = ref<ChartConfig[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadChartConfigs = async () => {
    loading.value = true
    try {
      // 模拟API调用
      chartConfigs.value = [
        {
          id: '1',
          name: '堆叠折线图',
          type: 'line',
          category: '折线图',
          description: '用于展示多个数据系列的趋势变化',
          echartsConfig: {},
          universalTemplate: {},
          joltSpec: [],
          placeholders: [],
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01'
        }
      ]
    } catch (err: any) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    chartConfigs,
    loading,
    error,
    loadChartConfigs
  }
})
