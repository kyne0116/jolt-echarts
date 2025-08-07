import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { VirtualTable } from '@/types'

export const useVirtualDatabaseStore = defineStore('virtualDatabase', () => {
  const tables = ref<VirtualTable[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadTables = async () => {
    loading.value = true
    try {
      // 模拟API调用
      tables.value = []
    } catch (err: any) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    tables,
    loading,
    error,
    loadTables
  }
})
