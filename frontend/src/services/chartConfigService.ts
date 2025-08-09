import { chartConfigApi } from "@/api";

/**
 * 图表配置服务
 * 从后端获取图表配置信息，替代前端硬编码
 */

// 图表类型映射接口
export interface ChartTypeMappings {
  pathToChartId: Record<string, string>;
  chartIdToPath: Record<string, string>;
  categories: Record<string, string[]>;
  totalCharts: number;
}

// 图表特定配置接口
export interface ChartSpecificConfig {
  chartId: string;
  smooth?: boolean;
  stack?: string | null;
  areaStyle?: object | null;
  boundaryGap?: boolean;
  radius?: string | string[];
  center?: string[];
  min?: number;
  max?: number;
  description: string;
  timestamp: number;
}

// 预处理规则接口
export interface PreprocessingRules {
  [chartId: string]: {
    requiresAreaStyle?: boolean;
    stackValue?: string;
    smooth?: boolean;
  };
}

class ChartConfigService {
  private chartTypeMappings: ChartTypeMappings | null = null;
  private preprocessingRules: PreprocessingRules | null = null;
  private chartConfigs: Map<string, ChartSpecificConfig> = new Map();

  /**
   * 获取图表类型映射配置
   */
  async getChartTypeMappings(): Promise<ChartTypeMappings> {
    if (this.chartTypeMappings) {
      return this.chartTypeMappings;
    }

    try {
      console.log("📋 [配置服务] 从后端获取图表类型映射");
      this.chartTypeMappings = await chartConfigApi.getChartTypes();
      console.log(
        "✅ [配置服务] 图表类型映射获取成功:",
        this.chartTypeMappings
      );
      return this.chartTypeMappings!;
    } catch (error) {
      console.error("❌ [配置服务] 获取图表类型映射失败:", error);
      throw error;
    }
  }

  /**
   * 根据文件路径生成图表ID
   */
  async generateChartIdFromFilePath(filePath: string): Promise<string> {
    const mappings = await this.getChartTypeMappings();
    const chartId = mappings.pathToChartId[filePath];

    if (chartId) {
      console.log(`📋 [配置服务] 文件路径映射: ${filePath} -> ${chartId}`);
      return chartId;
    }

    // 如果没有找到映射，使用默认转换逻辑
    const fallbackId = filePath.replace(/[\/\s\.]/g, "_").toLowerCase();
    console.warn(
      `⚠️ [配置服务] 未找到文件路径映射，使用默认转换: ${filePath} -> ${fallbackId}`
    );
    return fallbackId;
  }

  /**
   * 根据图表ID获取文件路径
   */
  async getEChartsFilePath(chartId: string): Promise<string> {
    const mappings = await this.getChartTypeMappings();
    const filePath = mappings.chartIdToPath[chartId];

    if (filePath) {
      console.log(`📋 [配置服务] 图表ID映射: ${chartId} -> ${filePath}`);
      return filePath;
    }

    console.warn(`⚠️ [配置服务] 未找到图表ID映射: ${chartId}`);
    return "未知";
  }

  /**
   * 获取图表分类信息
   */
  async getChartCategories(): Promise<Record<string, string[]>> {
    const mappings = await this.getChartTypeMappings();
    return mappings.categories;
  }

  /**
   * 获取图表特定配置
   */
  async getChartSpecificConfig(chartId: string): Promise<ChartSpecificConfig> {
    // 检查缓存
    if (this.chartConfigs.has(chartId)) {
      return this.chartConfigs.get(chartId)!;
    }

    try {
      console.log(`📋 [配置服务] 获取图表特定配置: ${chartId}`);
      const config = await chartConfigApi.getChartSpecificConfig(chartId);

      // 缓存配置
      this.chartConfigs.set(chartId, config);
      console.log(`✅ [配置服务] 图表配置获取成功: ${chartId}`, config);
      return config;
    } catch (error) {
      console.error(`❌ [配置服务] 获取图表配置失败: ${chartId}`, error);
      throw error;
    }
  }

  /**
   * 获取预处理规则
   */
  async getPreprocessingRules(): Promise<PreprocessingRules> {
    if (this.preprocessingRules) {
      return this.preprocessingRules;
    }

    try {
      console.log("📋 [配置服务] 获取预处理规则");
      this.preprocessingRules = await chartConfigApi.getPreprocessingRules();
      console.log("✅ [配置服务] 预处理规则获取成功:", this.preprocessingRules);
      return this.preprocessingRules!;
    } catch (error) {
      console.error("❌ [配置服务] 获取预处理规则失败:", error);
      throw error;
    }
  }

  /**
   * 根据图表ID和配置规则预处理图表数据
   */
  async preprocessChartData(chartId: string, data: any): Promise<any> {
    try {
      const config = await this.getChartSpecificConfig(chartId);

      console.log(`🔧 [配置服务] 开始预处理图表数据: ${chartId}`);

      // 深拷贝数据
      const processedData = JSON.parse(JSON.stringify(data));

      // 应用图表特定配置
      if (processedData.series && Array.isArray(processedData.series)) {
        processedData.series.forEach((series: any, index: number) => {
          // 饼图特殊处理
          if (chartId.includes("pie") || chartId.includes("doughnut")) {
            // 确保饼图类型正确
            series.type = "pie";

            // 应用饼图特定配置
            if (config.radius !== undefined) {
              series.radius = config.radius;
            }
            if (config.center !== undefined) {
              series.center = config.center;
            }

            // 圆环图特殊配置
            if (chartId === "doughnut_chart") {
              series.avoidLabelOverlap = false;
              series.label = {
                show: false,
                position: "center",
              };
              series.emphasis = {
                label: {
                  show: true,
                  fontSize: 30,
                  fontWeight: "bold",
                },
              };
              series.labelLine = {
                show: false,
              };
            }
          } else {
            // 折线图/柱状图处理
            // 应用smooth配置
            if (config.smooth !== undefined) {
              series.smooth = config.smooth;

              // 🔧 关键修复：如果启用smooth，移除stack属性（两者冲突）
              if (config.smooth === true && series.stack) {
                delete series.stack;
              }
            }

            // 应用stack配置（仅在不启用smooth时）
            if (config.stack !== undefined && config.smooth !== true) {
              series.stack = config.stack;
            }

            // 应用areaStyle配置
            if (config.areaStyle !== undefined && config.areaStyle !== null) {
              series.areaStyle = config.areaStyle;
              console.log(`🔧 [配置服务] series[${index}] 添加areaStyle`);
            }
          }
        });
      }

      console.log(`✅ [配置服务] 图表数据预处理完成: ${chartId}`);
      return processedData;
    } catch (error) {
      console.error(`❌ [配置服务] 图表数据预处理失败: ${chartId}`, error);
      return data; // 返回原始数据作为fallback
    }
  }

  /**
   * 清除缓存
   */
  clearCache(): void {
    this.chartTypeMappings = null;
    this.preprocessingRules = null;
    this.chartConfigs.clear();
    console.log("🧹 [配置服务] 缓存已清除");
  }
}

// 导出单例实例
export const chartConfigService = new ChartConfigService();
export default chartConfigService;
