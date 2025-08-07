import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UniversalTemplate } from '@/types'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref<UniversalTemplate[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadTemplates = async () => {
    loading.value = true
    try {
      // 模拟API调用
      templates.value = []
    } catch (err: any) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    templates,
    loading,
    error,
    loadTemplates
  }
})
