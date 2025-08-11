package com.example.chart.controller;

import com.example.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 动态映射配置控制器
 * 提供动态映射模板和配置管理功能
 */
@RestController
@RequestMapping("/api/chart/dynamic-mapping")
@CrossOrigin(origins = "*")
public class DynamicMappingController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMappingController.class);

    /**
     * 获取映射模板列表
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplates() {
        logger.info("📋 [动态映射] 获取映射模板列表");

        try {
            // 创建预定义的映射模板
            Map<String, Object> templates = new HashMap<>();
            
            // 基础映射模板
            Map<String, Object> basicTemplate = new HashMap<>();
            basicTemplate.put("id", "basic_mapping");
            basicTemplate.put("name", "基础映射模板");
            basicTemplate.put("description", "适用于简单的字段映射场景");
            basicTemplate.put("category", "基础");
            basicTemplate.put("rules", Arrays.asList(
                createMappingRule("${chart_title}", "category", "string", "none", "图表标题映射"),
                createMappingRule("${categories}", "month", "array", "list", "分类数据映射"),
                createMappingRule("${series_data}", "amount", "array", "list", "系列数据映射")
            ));
            templates.put("basic", basicTemplate);
            
            // 条件映射模板
            Map<String, Object> conditionalTemplate = new HashMap<>();
            conditionalTemplate.put("id", "conditional_mapping");
            conditionalTemplate.put("name", "条件映射模板");
            conditionalTemplate.put("description", "支持条件判断的高级映射");
            conditionalTemplate.put("category", "高级");
            conditionalTemplate.put("rules", Arrays.asList(
                createConditionalRule("${dynamic_data}", "if (category === 'sales') { return amount } else { return quantity }", "条件数据映射"),
                createConditionalRule("${chart_type}", "if (dataCount > 10) { return 'bar' } else { return 'line' }", "动态图表类型")
            ));
            templates.put("conditional", conditionalTemplate);
            
            // 聚合映射模板
            Map<String, Object> aggregationTemplate = new HashMap<>();
            aggregationTemplate.put("id", "aggregation_mapping");
            aggregationTemplate.put("name", "聚合映射模板");
            aggregationTemplate.put("description", "包含各种聚合函数的映射配置");
            aggregationTemplate.put("category", "聚合");
            aggregationTemplate.put("rules", Arrays.asList(
                createMappingRule("${total_amount}", "amount", "number", "sum", "总金额聚合"),
                createMappingRule("${avg_quantity}", "quantity", "number", "avg", "平均数量"),
                createMappingRule("${max_percentage}", "percentage", "number", "max", "最大百分比"),
                createMappingRule("${unique_regions}", "region", "array", "list", "地区列表")
            ));
            templates.put("aggregation", aggregationTemplate);

            Map<String, Object> response = new HashMap<>();
            response.put("templates", templates);
            response.put("templateCount", templates.size());
            response.put("categories", Arrays.asList("基础", "高级", "聚合"));

            logger.info("✅ [动态映射] 成功获取 {} 个映射模板", templates.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [动态映射] 获取映射模板失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取映射模板失败: " + e.getMessage()));
        }
    }

    /**
     * 获取图表的动态映射配置
     */
    @GetMapping("/{chartId}/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartConfig(@PathVariable String chartId) {
        logger.info("🔍 [动态映射] 获取图表动态配置: {}", chartId);

        try {
            // 模拟获取图表的动态映射配置
            Map<String, Object> config = new HashMap<>();
            config.put("chartId", chartId);
            config.put("templateId", "basic_mapping");
            config.put("customRules", new ArrayList<>());
            config.put("conditions", new HashMap<>());
            config.put("lastModified", new Date());

            Map<String, Object> response = new HashMap<>();
            response.put("config", config);
            response.put("hasConfig", true);

            logger.info("✅ [动态映射] 成功获取图表配置");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [动态映射] 获取图表配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取图表配置失败: " + e.getMessage()));
        }
    }

    /**
     * 保存图表的动态映射配置
     */
    @PostMapping("/{chartId}/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveChartConfig(
            @PathVariable String chartId,
            @RequestBody Map<String, Object> configData) {
        logger.info("💾 [动态映射] 保存图表动态配置: {}", chartId);

        try {
            // 模拟保存配置
            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("success", true);
            response.put("message", "动态映射配置保存成功");
            response.put("timestamp", new Date());

            logger.info("✅ [动态映射] 配置保存成功");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [动态映射] 保存配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "保存配置失败: " + e.getMessage()));
        }
    }

    /**
     * 验证动态映射配置
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateConfig(@RequestBody Map<String, Object> configData) {
        logger.info("🔍 [动态映射] 验证映射配置");

        try {
            // 模拟配置验证
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("isValid", true);
            validationResult.put("errors", new ArrayList<>());
            validationResult.put("warnings", new ArrayList<>());
            validationResult.put("suggestions", Arrays.asList(
                "建议添加错误处理逻辑",
                "考虑优化聚合性能"
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("validation", validationResult);
            response.put("timestamp", new Date());

            logger.info("✅ [动态映射] 配置验证完成");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [动态映射] 配置验证失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "配置验证失败: " + e.getMessage()));
        }
    }

    /**
     * 创建映射规则
     */
    private Map<String, Object> createMappingRule(String placeholder, String field, String dataType, String aggregation, String description) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("placeholder", placeholder);
        rule.put("field", field);
        rule.put("dataType", dataType);
        rule.put("aggregation", aggregation);
        rule.put("description", description);
        rule.put("filters", new HashMap<>());
        return rule;
    }

    /**
     * 创建条件映射规则
     */
    private Map<String, Object> createConditionalRule(String placeholder, String expression, String description) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("placeholder", placeholder);
        rule.put("type", "conditional");
        rule.put("expression", expression);
        rule.put("description", description);
        rule.put("fallback", "null");
        return rule;
    }
}
