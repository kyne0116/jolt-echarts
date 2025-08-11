package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.example.chart.service.PlaceholderManager;
import com.example.chart.service.PlaceholderMappingManager;
import com.example.chart.service.TwoStageTransformationService;
import com.example.chart.service.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
            TwoStageTransformationService.TransformationResult stage1Result = 
                transformationService.executeStage1Transformation(chartId, template);

            if (!stage1Result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.error("TRANSFORMATION_FAILED", "转换失败: " + stage1Result.getMessage()));
            }

            // 3. 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(stage1Result.getResult());

            // 4. 获取当前映射配置
            Map<String, PlaceholderMappingManager.FieldMapping> currentMappings = mappingManager.getMappings(chartId);

            // 5. 生成默认映射建议
            Map<String, PlaceholderMappingManager.FieldMapping> suggestedMappings = 
                mappingManager.generateDefaultMappings(chartId, placeholders);

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
            Map<String, Map<String, Object>> mappingsData = (Map<String, Map<String, Object>>) requestBody.get("mappings");

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
     * 执行映射并预览结果
     */
    @PostMapping("/{chartId}/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewMapping(
            @PathVariable String chartId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        logger.info("👁️ [占位符映射] 预览映射结果: {}", chartId);

        try {
            // 1. 获取模板并执行第一阶段转换
            Map<String, Object> template = templateService.getTemplateByChartId(chartId);
            TwoStageTransformationService.TransformationResult stage1Result = 
                transformationService.executeStage1Transformation(chartId, template);

            if (!stage1Result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.error("TRANSFORMATION_FAILED", "转换失败: " + stage1Result.getMessage()));
            }

            // 2. 执行映射
            PlaceholderMappingManager.MappingResult mappingResult = 
                mappingManager.executeMapping(chartId, stage1Result.getResult());

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappingSuccess", mappingResult.isSuccess());
            response.put("mappingMessage", mappingResult.getMessage());
            response.put("mappedData", mappingResult.getData());
            response.put("unmappedPlaceholders", mappingResult.getUnmappedPlaceholders());
            response.put("originalTemplate", stage1Result.getResult());

            if (mappingResult.isSuccess()) {
                logger.info("✅ [占位符映射] 映射预览成功");
            } else {
                logger.warn("⚠️ [占位符映射] 映射预览部分失败: {}", mappingResult.getMessage());
            }

            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [占位符映射] 预览映射失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "预览映射失败: " + e.getMessage()));
        }
    }

    /**
     * 获取可用的数据库字段列表
     */
    @GetMapping("/available-fields")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableFields() {
        logger.info("📊 [占位符映射] 获取可用字段列表");

        try {
            // 定义可用字段及其信息
            List<Map<String, Object>> fields = Arrays.asList(
                createFieldInfo("id", "number", "主键ID", "唯一标识"),
                createFieldInfo("year", "string", "年份", "时间维度"),
                createFieldInfo("month", "string", "月份", "时间维度"),
                createFieldInfo("date", "string", "日期", "时间维度"),
                createFieldInfo("category", "string", "业务分类", "分类维度"),
                createFieldInfo("channel", "string", "销售渠道", "分类维度"),
                createFieldInfo("product", "string", "产品名称", "分类维度"),
                createFieldInfo("region", "string", "地理区域", "分类维度"),
                createFieldInfo("amount", "number", "金额", "数值字段"),
                createFieldInfo("quantity", "number", "数量", "数值字段"),
                createFieldInfo("percentage", "number", "百分比", "数值字段"),
                createFieldInfo("salesman", "string", "销售人员", "分类维度")
            );

            // 聚合类型选项
            List<Map<String, Object>> aggregationTypes = Arrays.asList(
                createAggregationInfo("none", "无聚合", "直接使用原始值"),
                createAggregationInfo("sum", "求和", "对数值字段求和"),
                createAggregationInfo("avg", "平均值", "计算数值字段平均值"),
                createAggregationInfo("count", "计数", "统计记录数量"),
                createAggregationInfo("max", "最大值", "获取数值字段最大值"),
                createAggregationInfo("min", "最小值", "获取数值字段最小值"),
                createAggregationInfo("list", "列表", "返回所有值的数组")
            );

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
    private Map<String, Object> createFieldInfo(String name, String type, String label, String group) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("type", type);
        field.put("label", label);
        field.put("group", group);
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
}
