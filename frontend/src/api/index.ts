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

export default api;
