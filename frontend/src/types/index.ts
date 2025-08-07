// 基础类型定义
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
  error?: string
}

// 占位符相关类型
export interface PlaceholderInfo {
  name: string
  description: string
  dataType: 'string' | 'number' | 'array' | 'object'
  required: boolean
  defaultValue?: any
}

// 映射关系类型
export interface FieldMapping {
  placeholderName: string
  tableName: string
  columnName: string
  dataType: string
  aggregationType?: string
  queryConditions?: Record<string, any>
}

export interface MappingRelationship {
  chartId: string
  mappings: Record<string, FieldMapping>
  mappingCount: number
}

// 图表配置类型
export interface ChartConfig {
  id: string
  name: string
  type: 'line' | 'bar' | 'pie' | 'radar' | 'gauge'
  category: string
  description: string
  echartsConfig: Record<string, any>
  universalTemplate: Record<string, any>
  joltSpec: any[]
  placeholders: string[]
  createdAt: string
  updatedAt: string
}

// Jolt规范类型
export interface JoltSpec {
  id: string
  chartType: string
  name: string
  description: string
  spec: any[]
  inputExample: Record<string, any>
  outputExample: Record<string, any>
  createdAt: string
  updatedAt: string
}

// 通用JSON模板类型
export interface UniversalTemplate {
  id: string
  chartType: string
  name: string
  description: string
  template: Record<string, any>
  placeholders: PlaceholderInfo[]
  createdAt: string
  updatedAt: string
}

// 转换结果类型
export interface TransformationResult {
  success: boolean
  message: string
  result?: Record<string, any>
  placeholders?: string[]
  queryResults?: Record<string, any>
  stage1Output?: Record<string, any>
  stage2Output?: Record<string, any>
  executionTime?: number
}

// 虚拟数据库类型
export interface VirtualTable {
  name: string
  description: string
  columns: VirtualColumn[]
  data: Record<string, any>[]
  createdAt: string
  updatedAt: string
}

export interface VirtualColumn {
  name: string
  type: 'string' | 'number' | 'boolean' | 'date' | 'json'
  nullable: boolean
  defaultValue?: any
  description?: string
}

// 转换流程步骤类型
export interface TransformationStep {
  id: string
  name: string
  description: string
  status: 'pending' | 'running' | 'completed' | 'error'
  input?: Record<string, any>
  output?: Record<string, any>
  error?: string
  duration?: number
}

// 系统状态类型
export interface SystemStatus {
  backendConnected: boolean
  lastHeartbeat: string
  version: string
  features: string[]
}

// 菜单项类型
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path: string
  children?: MenuItem[]
}

// 表单验证规则类型
export interface ValidationRule {
  required?: boolean
  message?: string
  pattern?: RegExp
  validator?: (rule: any, value: any) => Promise<void>
}

// 编辑器配置类型
export interface EditorConfig {
  language: 'json' | 'javascript' | 'typescript'
  theme: 'vs-dark' | 'vs-light'
  readOnly: boolean
  minimap: boolean
  wordWrap: 'on' | 'off'
}

// 图表预览配置类型
export interface ChartPreviewConfig {
  width: number
  height: number
  theme: 'default' | 'dark' | 'light'
  responsive: boolean
}

// 导入导出配置类型
export interface ImportExportConfig {
  format: 'json' | 'csv' | 'excel'
  includeMetadata: boolean
  compression: boolean
}

// 分页配置类型
export interface PaginationConfig {
  current: number
  pageSize: number
  total: number
  showSizeChanger: boolean
  showQuickJumper: boolean
  showTotal: (total: number, range: [number, number]) => string
}

// 搜索过滤配置类型
export interface SearchFilterConfig {
  keyword: string
  type: string[]
  dateRange: [string, string] | null
  status: string[]
}

// 操作日志类型
export interface OperationLog {
  id: string
  action: string
  target: string
  targetId: string
  details: Record<string, any>
  userId: string
  timestamp: string
  result: 'success' | 'error'
  error?: string
}
