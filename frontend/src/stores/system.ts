import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { twoStageApi } from '@/api'
import type { SystemStatus } from '@/types'

export const useSystemStore = defineStore('system', () => {
  // 状态
  const status = ref<SystemStatus>({
    backendConnected: false,
    lastHeartbeat: '',
    version: '1.0.0',
    features: []
  })
  
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // 计算属性
  const isHealthy = computed(() => status.value.backendConnected)
  const connectionStatus = computed(() => 
    status.value.backendConnected ? '已连接' : '连接失败'
  )
  
  // 方法
  const checkHealth = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await twoStageApi.health()
      
      status.value = {
        backendConnected: true,
        lastHeartbeat: new Date().toISOString(),
        version: response.version || '1.0.0',
        features: response.features || []
      }
      
      console.log('✅ 系统健康检查成功:', response)
    } catch (err: any) {
      status.value.backendConnected = false
      error.value = err.message || '健康检查失败'
      console.error('❌ 系统健康检查失败:', err)
    } finally {
      loading.value = false
    }
  }
  
  const startHeartbeat = () => {
    // 立即执行一次健康检查
    checkHealth()
    
    // 每30秒执行一次健康检查
    setInterval(() => {
      checkHealth()
    }, 30000)
  }
  
  const resetError = () => {
    error.value = null
  }
  
  return {
    // 状态
    status,
    loading,
    error,
    
    // 计算属性
    isHealthy,
    connectionStatus,
    
    // 方法
    checkHealth,
    startHeartbeat,
    resetError
  }
})
