import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { JoltSpec } from '@/types'

export const useJoltSpecStore = defineStore('joltSpec', () => {
  const joltSpecs = ref<JoltSpec[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const loadJoltSpecs = async () => {
    loading.value = true
    try {
      // 模拟API调用
      joltSpecs.value = []
    } catch (err: any) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    joltSpecs,
    loading,
    error,
    loadJoltSpecs
  }
})
