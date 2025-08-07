import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { MappingRelationship } from '@/types'

export const useMappingStore = defineStore('mapping', () => {
  const mappings = ref<Record<string, MappingRelationship>>({})
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadMappings = async (chartId: string) => {
    loading.value = true
    try {
      // 模拟API调用
      mappings.value[chartId] = {
        chartId,
        mappings: {},
        mappingCount: 0
      }
    } catch (err: any) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    mappings,
    loading,
    error,
    loadMappings
  }
})
