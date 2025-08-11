import { message } from "ant-design-vue";
import type { AxiosInstance } from "axios";
import axios from "axios";

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: "/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加请求时间戳
    if (config.params) {
      config.params._t = Date.now();
    } else {
      config.params = { _t: Date.now() };
    }

    console.log(
      `🚀 API请求: ${config.method?.toUpperCase()} ${config.url}`,
      config.data || config.params
    );
    return config;
  },
  (error) => {
    console.error("❌ 请求拦截器错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API响应: ${response.config.url}`, response.data);
    return response;
  },
  (error) => {
    console.error("❌ API错误:", error);

    let errorMessage = "请求失败";

    if (error.response) {
      // 服务器响应错误
      const { status, data } = error.response;
      switch (status) {
        case 400:
          errorMessage = data?.message || "请求参数错误";
          break;
        case 401:
          errorMessage = "未授权访问";
          break;
        case 403:
          errorMessage = "禁止访问";
          break;
        case 404:
          errorMessage = "请求的资源不存在";
          break;
        case 500:
          errorMessage = "服务器内部错误";
          break;
        default:
          errorMessage = data?.message || `请求失败 (${status})`;
      }
    } else if (error.request) {
      // 网络错误
      errorMessage = "网络连接失败，请检查网络设置";
    } else {
      // 其他错误
      errorMessage = error.message || "未知错误";
    }

    message.error(errorMessage);
    return Promise.reject(error);
  }
);

// 通用API请求方法
export const request = {
  get: <T = any>(url: string, params?: any): Promise<T> => {
    return api.get(url, { params }).then((res) => res.data);
  },

  post: <T = any>(url: string, data?: any): Promise<T> => {
    return api.post(url, data).then((res) => res.data);
  },

  put: <T = any>(url: string, data?: any): Promise<T> => {
    return api.put(url, data).then((res) => res.data);
  },

  delete: <T = any>(url: string, params?: any): Promise<T> => {
    return api.delete(url, { params }).then((res) => res.data);
  },

  patch: <T = any>(url: string, data?: any): Promise<T> => {
    return api.patch(url, data).then((res) => res.data);
  },
};

// 统一解包后端ApiResponse的data
const unwrap = (resp: any) =>
  resp && typeof resp === "object" && "data" in resp ? resp.data : resp;

// 两阶段转换API（已适配ApiResponse包装）
export const twoStageApi = {
  // 健康检查
  health: () => request.get("/chart/two-stage/health").then(unwrap),

  // 完整转换验证
  validate: (chartId: string) =>
    request.get(`/chart/two-stage/validate/${chartId}`).then(unwrap),

  // 获取图表类型信息
  getChartInfo: (chartId: string) =>
    request.get(`/chart/two-stage/chart-info/${chartId}`).then(unwrap),

  // 获取通用模板
  getTemplate: (chartId: string) =>
    request.get(`/chart/two-stage/template/${chartId}`).then(unwrap),

  // 第一阶段转换
  stage1Transform: (chartId: string, template: any) =>
    request.post(`/chart/two-stage/stage1/${chartId}`, template).then(unwrap),

  // 第二阶段转换
  stage2Transform: (chartId: string, echartsTemplate: any) =>
    request
      .post(`/chart/two-stage/stage2/${chartId}`, echartsTemplate)
      .then(unwrap),

  // 获取映射关系
  getMappings: (chartId: string) =>
    request.get(`/chart/two-stage/mappings/${chartId}`).then(unwrap),

  // 占位符测试
  testPlaceholder: (testData: any) =>
    request.post("/chart/two-stage/placeholder/test", testData).then(unwrap),

  // 扫描ECharts目录结构
  scanEChartsDirectory: () =>
    request.get("/chart/two-stage/echarts-directory").then(unwrap),

  // 获取图表分类列表（错误恢复用）
  getCategories: () => request.get("/chart/two-stage/categories").then(unwrap),
};

// 图表验证API（原有的）
export const chartValidationApi = {
  // 堆叠折线图验证
  validateStackedLine: () => request.get("/chart/validation/stacked-line"),

  // 获取Mock数据
  getMockData: () => request.get("/chart/validation/mock-data"),

  // 健康检查
  health: () => request.get("/chart/validation/health"),
};

// 新增API：占位符目录、映射管理、注册表、数据源
export const placeholderCatalogApi = {
  // 生成占位符目录
  generate: (
    chartId: string,
    body?: { templateVersion?: string; specVersion?: string }
  ) =>
    request
      .post(`/charts/${chartId}/placeholder-catalog/generate`, body)
      .then(unwrap),
};

export const mappingApi = {
  // 获取激活映射
  getActive: (chartId: string) =>
    request.get(`/charts/${chartId}/mappings/active`).then(unwrap),

  // 保存映射草稿
  save: (chartId: string, mapping: any) =>
    request.put(`/charts/${chartId}/mappings`, mapping).then(unwrap),

  // 校验映射
  validate: (
    chartId: string,
    body: { mapping: any; templateVersion?: string; specVersion?: string }
  ) => request.post(`/charts/${chartId}/mappings/validate`, body).then(unwrap),

  // Dry-run 预览
  dryRun: (chartId: string, mapping: any) =>
    request.post(`/charts/${chartId}/dry-run`, mapping).then(unwrap),
};

export const chartRegistryApi = {
  // 列出所有图表
  list: () => request.get("/charts").then(unwrap),

  // 获取图表注册信息
  get: (chartId: string) =>
    request.get(`/charts/${chartId}/registry`).then(unwrap),

  // 激活版本
  activate: (
    chartId: string,
    body: {
      templateVersion?: string;
      specVersion?: string;
      mappingVersion?: string;
    }
  ) => request.put(`/charts/${chartId}/registry/activate`, body).then(unwrap),
};

export const dataSourceApi = {
  // 获取数据源schema
  schema: () => request.get("/datasource/schema").then(unwrap),

  // 预览表数据
  preview: (table: string, limit = 20) =>
    request.get("/datasource/preview", { table, limit }).then(unwrap),

  // 查询预览
  queryPreview: (body: any) =>
    request.post("/datasource/query/preview", body).then(unwrap),
};

// 占位符映射管理API
export const placeholderMappingApi = {
  // 获取图表的占位符列表
  getPlaceholders: (chartId: string) =>
    request
      .get(`/chart/placeholder-mapping/${chartId}/placeholders`)
      .then(unwrap),

  // 配置图表的占位符映射关系
  configureMappings: (chartId: string, mappings: any) =>
    request
      .post(`/chart/placeholder-mapping/${chartId}/mappings`, { mappings })
      .then(unwrap),

  // 获取图表的映射配置
  getMappings: (chartId: string) =>
    request.get(`/chart/placeholder-mapping/${chartId}/mappings`).then(unwrap),

  // 获取可用的数据库字段列表
  getAvailableFields: () =>
    request.get("/chart/placeholder-mapping/available-fields").then(unwrap),

  // 删除图表的映射配置
  deleteMappings: (chartId: string) =>
    request
      .delete(`/chart/placeholder-mapping/${chartId}/mappings`)
      .then(unwrap),

  // 根据图表ID获取对应的JOLT规范文件内容
  getJoltSpecContent: async (chartId: string) => {
    // 首先获取图表ID对应的JOLT规范文件名
    const joltSpecFileMap: Record<string, string> = {
      basic_line_chart: "line-chart-placeholder.json",
      smooth_line_chart: "line-chart-placeholder.json",
      stacked_line_chart: "line-chart-stacked.json",
      basic_bar_chart: "bar-chart-placeholder.json",
      stacked_bar_chart: "bar-chart-placeholder.json",
      basic_pie_chart: "pie-chart-placeholder.json",
      doughnut_chart: "pie-chart-placeholder.json",
      pie_chart: "pie-chart-placeholder.json",
      basic_radar_chart: "radar-chart-placeholder.json",
      filled_radar_chart: "radar-chart-placeholder.json",
      basic_gauge_chart: "gauge-chart-placeholder.json",
      progress_gauge_chart: "gauge-chart-placeholder.json",
      grade_gauge_chart: "gauge-chart-placeholder.json",
    };

    const filename = joltSpecFileMap[chartId] || "line-chart-placeholder.json";
    const response = await request
      .get(`/file-template-tests/jolt-specs/${filename}`)
      .then(unwrap);
    return response.content;
  },

  // 获取所有映射配置列表
  getAllMappings: () =>
    request.get("/chart/placeholder-mapping/list").then(unwrap),

  // 复制映射配置
  copyMappings: (sourceChartId: string, targetChartId: string) =>
    request
      .post(`/chart/placeholder-mapping/${sourceChartId}/copy/${targetChartId}`)
      .then(unwrap),

  // 批量删除映射配置
  batchDeleteMappings: (chartIds: string[]) =>
    request
      .delete("/chart/placeholder-mapping/batch", { data: { chartIds } })
      .then(unwrap),

  // 智能推荐映射配置
  generateDefaultMappings: (chartId: string, placeholders: string[]) =>
    request
      .post(`/chart/placeholder-mapping/${chartId}/generate-defaults`, {
        placeholders,
      })
      .then(unwrap),
};

// 图表配置API
export const chartConfigApi = {
  // 获取图表类型映射配置
  getChartTypes: () => request.get("/chart/config/chart-types").then(unwrap),

  // 获取图表特定配置
  getChartSpecificConfig: (chartId: string) =>
    request.get(`/chart/config/chart-specific/${chartId}`).then(unwrap),

  // 获取图表预处理规则
  getPreprocessingRules: () =>
    request.get("/chart/config/preprocessing-rules").then(unwrap),

  // 获取文件路径到图表ID的映射关系
  getFilePathMappings: () =>
    request.get("/chart/config/file-path-mappings").then(unwrap),

  // 根据文件路径获取图表ID
  getChartIdByFilePath: (filePath: string) =>
    request.get("/chart/config/chart-id-by-path", { filePath }).then(unwrap),

  // 获取图表测试数据
  getTestData: (chartType: string) =>
    request.get(`/chart/config/test-data/${chartType}`).then(unwrap),
};

// 模板管理API
export const templateApi = {
  // 获取所有模板列表
  list: () => request.get("/templates").then(unwrap),

  // 根据chartId获取模板
  getByChartId: (chartId: string) =>
    request.get(`/templates/${chartId}`).then(unwrap),

  // 创建新模板
  create: (template: any) => request.post("/templates", template).then(unwrap),

  // 更新模板
  update: (chartId: string, template: any) =>
    request.put(`/templates/${chartId}`, template).then(unwrap),

  // 删除模板
  delete: (chartId: string) =>
    request.delete(`/templates/${chartId}`).then(unwrap),

  // 获取支持的图表类型
  getSupportedChartTypes: () =>
    request.get("/chart/config/chart-types").then(unwrap),

  // 获取模板分类信息
  getTemplateCategories: () =>
    request.get("/chart/config/template-categories").then(unwrap),

  // 从JOLT文件导入模板
  importFromJolt: (chartId: string) =>
    request.post(`/templates/import-jolt/${chartId}`).then(unwrap),

  // 从ECharts示例导入模板
  importFromExample: (chartId: string) =>
    request.post(`/templates/import-example/${chartId}`).then(unwrap),

  // 导出模板为JSON
  exportTemplate: (chartId: string) =>
    request.get(`/templates/${chartId}/export`).then(unwrap),

  // 验证模板格式
  validateTemplate: (template: any) =>
    request.post("/templates/validate", template).then(unwrap),

  // 提取占位符
  extractPlaceholders: (template: any) =>
    request.post("/templates/extract-placeholders", template).then(unwrap),
};

export default api;
