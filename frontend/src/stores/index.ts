import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// 导出所有store
export { useSystemStore } from './system'
export { useChartConfigStore } from './chartConfig'
export { useJoltSpecStore } from './joltSpec'
export { useMappingStore } from './mapping'
export { useTemplateStore } from './template'
export { useTransformationStore } from './transformation'
export { useVirtualDatabaseStore } from './virtualDatabase'
