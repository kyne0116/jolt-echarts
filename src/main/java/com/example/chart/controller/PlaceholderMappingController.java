package com.example.chart.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.ApiResponse;
import com.example.chart.service.PlaceholderManager;
import com.example.chart.service.PlaceholderMappingManager;
import com.example.chart.service.TemplateService;
import com.example.chart.service.TwoStageTransformationService;

/**
 * 占位符映射管理控制器
 * 提供占位符与虚拟数据库字段的动态映射配置API
 */
@RestController
@RequestMapping("/api/chart/placeholder-mapping")
@CrossOrigin(origins = "*")
public class PlaceholderMappingController {

    private static final Logger logger = LoggerFactory.getLogger(PlaceholderMappingController.class);

    @Autowired
    private PlaceholderMappingManager mappingManager;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private TemplateService templateService;

    /**
     * 获取图表的占位符列表
     */
    @GetMapping("/{chartId}/placeholders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlaceholders(@PathVariable String chartId) {
        logger.info("🔍 [占位符映射] 获取图表占位符: {}", chartId);

        try {
            // 1. 获取模板
            Map<String, Object> template = templateService.getTemplateByChartId(chartId);
            if (template == null) {
                return ResponseEntity.ok(ApiResponse.error("TEMPLATE_NOT_FOUND", "模板不存在"));
            }

            // 2. 执行第一阶段转换获取带占位符的结构
            TwoStageTransformationService.TransformationResult stage1Result = transformationService
                    .executeStage1Transformation(chartId, template);

            if (!stage1Result.isSuccess()) {
                return ResponseEntity
                        .ok(ApiResponse.error("TRANSFORMATION_FAILED", "转换失败: " + stage1Result.getMessage()));
            }

            // 3. 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(stage1Result.getResult());

            // 4. 获取当前映射配置
            Map<String, PlaceholderMappingManager.FieldMapping> currentMappings = mappingManager.getMappings(chartId);

            // 5. 生成默认映射建议
            Map<String, PlaceholderMappingManager.FieldMapping> suggestedMappings = mappingManager
                    .generateDefaultMappings(chartId, placeholders);

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("placeholders", new ArrayList<>(placeholders));
            response.put("placeholderCount", placeholders.size());
            response.put("currentMappings", currentMappings);
            response.put("suggestedMappings", suggestedMappings);
            response.put("stage1Output", stage1Result.getResult());

            logger.info("✅ [占位符映射] 成功获取 {} 个占位符", placeholders.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 获取占位符失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取占位符失败: " + e.getMessage()));
        }
    }

    /**
     * 配置图表的占位符映射关系
     */
    @PostMapping("/{chartId}/mappings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> configureMappings(
            @PathVariable String chartId,
            @RequestBody Map<String, Object> requestBody) {
        logger.info("⚙️ [占位符映射] 配置映射关系: {}", chartId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> mappingsData = (Map<String, Map<String, Object>>) requestBody
                    .get("mappings");

            if (mappingsData == null || mappingsData.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("INVALID_REQUEST", "映射配置不能为空"));
            }

            // 转换为FieldMapping对象
            Map<String, PlaceholderMappingManager.FieldMapping> mappings = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : mappingsData.entrySet()) {
                String placeholder = entry.getKey();
                Map<String, Object> mappingData = entry.getValue();

                PlaceholderMappingManager.FieldMapping mapping = new PlaceholderMappingManager.FieldMapping();
                mapping.setFieldName((String) mappingData.get("fieldName"));
                mapping.setDataType((String) mappingData.get("dataType"));
                mapping.setAggregationType((String) mappingData.getOrDefault("aggregationType", "none"));

                @SuppressWarnings("unchecked")
                Map<String, Object> filters = (Map<String, Object>) mappingData.get("filters");
                mapping.setFilters(filters != null ? filters : new HashMap<>());

                mapping.setTransformExpression((String) mappingData.get("transformExpression"));

                mappings.put(placeholder, mapping);
            }

            // 保存映射配置
            mappingManager.configureMappings(chartId, mappings);

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappingCount", mappings.size());
            response.put("success", true);
            response.put("message", "映射配置保存成功");

            logger.info("✅ [占位符映射] 成功配置 {} 个映射关系", mappings.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 配置映射关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "配置映射关系失败: " + e.getMessage()));
        }
    }

    /**
     * 获取图表的映射配置
     */
    @GetMapping("/{chartId}/mappings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMappings(@PathVariable String chartId) {
        logger.info("📋 [占位符映射] 获取映射配置: {}", chartId);

        try {
            Map<String, PlaceholderMappingManager.FieldMapping> mappings = mappingManager.getMappings(chartId);

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappings", mappings);
            response.put("mappingCount", mappings.size());
            response.put("hasConfig", !mappings.isEmpty());

            logger.info("✅ [占位符映射] 成功获取 {} 个映射配置", mappings.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 获取映射配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取映射配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取可用的数据库字段列表
     */
    @GetMapping("/available-fields")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableFields() {
        logger.info("📊 [占位符映射] 获取可用字段列表");

        try {
            // 定义可用字段及其信息 - 与UniversalChartDataView的12个核心字段完全对应
            List<Map<String, Object>> fields = Arrays.asList(
                    createFieldInfo("id", "number", "主键ID", "唯一标识", "主键ID - 唯一标识"),
                    createFieldInfo("year", "string", "年份", "时间维度", "年份 - 用于时间维度分析，示例: 2024, 2023"),
                    createFieldInfo("month", "string", "月份", "时间维度", "月份 - 用于月度趋势分析，示例: 1月, 2月, 12月"),
                    createFieldInfo("date", "string", "日期", "时间维度", "具体日期 - 用于日期维度分析，示例: 2024-01-15"),
                    createFieldInfo("category", "string", "业务分类", "分类维度", "业务分类 - 如产品类别、部门等，示例: 电子产品, 服装, 食品"),
                    createFieldInfo("channel", "string", "销售渠道", "分类维度",
                            "销售渠道 - 用于渠道分析，示例: Email, Social Media, Direct"),
                    createFieldInfo("product", "string", "产品名称", "分类维度", "具体产品名称，示例: 产品A, 产品B, 产品C"),
                    createFieldInfo("region", "string", "地理区域", "分类维度", "地理区域 - 用于地域分析，示例: 华北, 华东, 华南, 华中"),
                    createFieldInfo("amount", "number", "金额", "数值字段", "金额数据 - 核心财务指标，示例: 12500.50, 8900.00"),
                    createFieldInfo("quantity", "number", "数量", "数值字段", "数量数据 - 销量等计数指标，示例: 100, 250, 50"),
                    createFieldInfo("percentage", "number", "百分比", "数值字段", "百分比数据 - 占比分析，示例: 25.5, 60.2, 15.8"),
                    createFieldInfo("salesman", "string", "销售人员", "分类维度", "销售人员 - 用于人员绩效分析，示例: 张三, 李四, 王五"));

            // 聚合类型选项
            List<Map<String, Object>> aggregationTypes = Arrays.asList(
                    createAggregationInfo("none", "无聚合", "直接使用原始值"),
                    createAggregationInfo("sum", "求和", "对数值字段求和"),
                    createAggregationInfo("avg", "平均值", "计算数值字段平均值"),
                    createAggregationInfo("count", "计数", "统计记录数量"),
                    createAggregationInfo("max", "最大值", "获取数值字段最大值"),
                    createAggregationInfo("min", "最小值", "获取数值字段最小值"),
                    createAggregationInfo("list", "列表", "返回所有值的数组"));

            Map<String, Object> response = new HashMap<>();
            response.put("fields", fields);
            response.put("aggregationTypes", aggregationTypes);
            response.put("fieldCount", fields.size());

            logger.info("✅ [占位符映射] 成功获取 {} 个可用字段", fields.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 获取可用字段失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取可用字段失败: " + e.getMessage()));
        }
    }

    /**
     * 创建字段信息
     */
    private Map<String, Object> createFieldInfo(String name, String type, String label, String group,
            String description) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("type", type);
        field.put("label", label);
        field.put("group", group);
        field.put("description", description);
        return field;
    }

    /**
     * 创建聚合类型信息
     */
    private Map<String, Object> createAggregationInfo(String value, String label, String description) {
        Map<String, Object> aggregation = new HashMap<>();
        aggregation.put("value", value);
        aggregation.put("label", label);
        aggregation.put("description", description);
        return aggregation;
    }

    /**
     * 删除图表的映射配置
     */
    @DeleteMapping("/{chartId}/mappings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteMappings(@PathVariable String chartId) {
        logger.info("🗑️ [占位符映射] 删除映射配置: {}", chartId);

        try {
            mappingManager.configureMappings(chartId, new HashMap<>());

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("success", true);
            response.put("message", "映射配置已删除");

            logger.info("✅ [占位符映射] 成功删除映射配置");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 删除映射配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "删除映射配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有映射配置列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMappings() {
        logger.info("📋 [占位符映射] 获取所有映射配置列表");

        try {
            List<Map<String, Object>> mappingList = mappingManager.getAllMappingConfigurations();

            Map<String, Object> response = new HashMap<>();
            response.put("mappings", mappingList);
            response.put("totalCount", mappingList.size());

            logger.info("✅ [占位符映射] 成功获取 {} 个映射配置", mappingList.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 获取映射配置列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取映射配置列表失败: " + e.getMessage()));
        }
    }

    /**
     * 复制映射配置到其他图表类型
     */
    @PostMapping("/{sourceChartId}/copy/{targetChartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> copyMappings(
            @PathVariable String sourceChartId,
            @PathVariable String targetChartId) {
        logger.info("📋 [占位符映射] 复制映射配置: {} -> {}", sourceChartId, targetChartId);

        try {
            boolean success = mappingManager.copyMappings(sourceChartId, targetChartId);

            Map<String, Object> response = new HashMap<>();
            response.put("sourceChartId", sourceChartId);
            response.put("targetChartId", targetChartId);
            response.put("success", success);
            response.put("message", success ? "映射配置复制成功" : "源图表没有映射配置");

            logger.info("✅ [占位符映射] 映射配置复制完成: {}", success);
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 复制映射配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "复制映射配置失败: " + e.getMessage()));
        }
    }

    /**
     * 测试API - 获取映射存储状态
     */
    @GetMapping("/debug/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDebugStatus() {
        logger.info("🔍 [占位符映射] 获取调试状态信息");

        try {
            Map<String, Object> debugInfo = new HashMap<>();

            // 获取映射存储的基本信息
            List<Map<String, Object>> mappingList = mappingManager.getAllMappingConfigurations();
            debugInfo.put("totalMappings", mappingList.size());
            debugInfo.put("mappingDetails", mappingList);

            // 测试单个图表的信息生成
            String testChartId = "basic_line_chart";
            Map<String, Object> testChart = new HashMap<>();
            testChart.put("chartId", testChartId);
            testChart.put("chartType", getChartTypeFromTestId(testChartId));
            testChart.put("chartName", getChartNameFromTestId(testChartId));
            testChart.put("universalTemplate", getUniversalTemplateFromTestId(testChartId));
            debugInfo.put("testChart", testChart);

            logger.info("✅ [占位符映射] 调试信息获取成功");
            return ResponseEntity.ok(ApiResponse.ok(debugInfo));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 获取调试信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取调试信息失败: " + e.getMessage()));
        }
    }

    // 测试用的辅助方法
    private String getChartTypeFromTestId(String chartId) {
        if (chartId.contains("line"))
            return "折线图";
        if (chartId.contains("bar"))
            return "柱状图";
        if (chartId.contains("pie") || chartId.contains("doughnut"))
            return "饼图";
        if (chartId.contains("radar"))
            return "雷达图";
        if (chartId.contains("gauge"))
            return "仪表盘";
        return "未知类型";
    }

    private String getChartNameFromTestId(String chartId) {
        Map<String, String> chartNames = new HashMap<>();
        chartNames.put("basic_line_chart", "基础折线图");
        chartNames.put("smooth_line_chart", "平滑折线图");
        chartNames.put("stacked_line_chart", "堆叠折线图");
        return chartNames.getOrDefault(chartId, "未知图表");
    }

    private String getUniversalTemplateFromTestId(String chartId) {
        // 使用与PlaceholderMappingManager相同的逻辑
        String chartType = getChartTypeFromTestId(chartId);
        return mappingManager.getUniversalTemplateByCategory(chartType);
    }

    /**
     * 批量删除映射配置
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteMappings(
            @RequestBody Map<String, Object> requestBody) {
        logger.info("🗑️ [占位符映射] 批量删除映射配置");

        try {
            @SuppressWarnings("unchecked")
            List<String> chartIds = (List<String>) requestBody.get("chartIds");

            if (chartIds == null || chartIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("INVALID_PARAMS", "图表ID列表不能为空"));
            }

            int deletedCount = mappingManager.batchDeleteMappings(chartIds);

            Map<String, Object> response = new HashMap<>();
            response.put("deletedCount", deletedCount);
            response.put("requestedCount", chartIds.size());
            response.put("message", String.format("成功删除 %d 个映射配置", deletedCount));

            logger.info("✅ [占位符映射] 批量删除完成: {}/{}", deletedCount, chartIds.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 批量删除映射配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "批量删除映射配置失败: " + e.getMessage()));
        }
    }

    /**
     * 智能推荐映射配置
     */
    @PostMapping("/{chartId}/generate-defaults")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateDefaultMappings(
            @PathVariable String chartId,
            @RequestBody Map<String, Object> requestBody) {
        logger.info("🧠 [占位符映射] 智能推荐映射配置: {}", chartId);

        try {
            @SuppressWarnings("unchecked")
            List<String> placeholders = (List<String>) requestBody.get("placeholders");

            if (placeholders == null || placeholders.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("INVALID_PARAMS", "占位符列表不能为空"));
            }

            Map<String, PlaceholderMappingManager.FieldMapping> defaultMappings = mappingManager
                    .generateDefaultMappings(chartId, new HashSet<>(placeholders));

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappings", defaultMappings);
            response.put("generatedCount", defaultMappings.size());
            response.put("message", String.format("成功生成 %d 个智能推荐映射", defaultMappings.size()));

            logger.info("✅ [占位符映射] 智能推荐完成: {}", defaultMappings.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 智能推荐失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "智能推荐失败: " + e.getMessage()));
        }
    }
}
