package com.example.chart.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chart.service.MappingRelationshipService;
import com.example.chart.service.PlaceholderManager;
import com.example.chart.service.TemplateService;
import com.example.chart.service.TwoStageTransformationService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 两阶段转换控制器
 * 提供API接口验证占位符系统和两阶段转换流程
 */
@RestController
@RequestMapping("/api/chart/two-stage")
public class TwoStageTransformationController {

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private MappingRelationshipService mappingService;

    @Autowired
    private TemplateService templateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证完整的两阶段转换流程
     */
    @GetMapping("/validate/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> validateTwoStageTransformation(
            @PathVariable String chartId) {
        try {
            TwoStageTransformationService.TransformationResult result = transformationService
                    .validateFullProcess(chartId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("finalEChartsConfig", result.getResult());

            if (result.getPlaceholders() != null) {
                response.put("processedPlaceholders", result.getPlaceholders());
            }

            if (result.getQueryResults() != null) {
                response.put("queryResults", result.getQueryResults());
            }

            // 添加转换流程信息
            Map<String, Object> transformationInfo = transformationService.getTransformationInfo(chartId);
            response.put("transformationInfo", transformationInfo);

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 获取带占位符的通用JSON模板
     */
    @GetMapping("/template/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> getUniversalTemplate(
            @PathVariable String chartId) {
        try {
            Map<String, Object> template = templateService.getTemplateByChartId(chartId);
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(template);

            Map<String, Object> response = new HashMap<>();
            response.put("template", template);
            response.put("placeholders", placeholders);
            response.put("placeholderCount", placeholders.size());

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 执行第一阶段转换（结构转换）
     */
    @PostMapping("/stage1/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> executeStage1Transformation(
            @PathVariable String chartId,
            @RequestBody Map<String, Object> universalTemplate) {
        try {
            System.out.println("执行第一阶段转换，图表类型: " + chartId);

            TwoStageTransformationService.TransformationResult result = transformationService
                    .executeStage1Transformation(chartId, universalTemplate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("echartsStructure", result.getResult());
            response.put("preservedPlaceholders", result.getPlaceholders());
            response.put("chartId", chartId);
            response.put("usedJoltSpec", result.getUsedJoltSpec());

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            System.err.println("第一阶段转换失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "第一阶段转换失败: " + e.getMessage());
            errorResponse.put("chartId", chartId);
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 执行第二阶段转换（数据回填）
     */
    @PostMapping("/stage2/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> executeStage2Transformation(
            @PathVariable String chartId,
            @RequestBody Map<String, Object> echartsTemplate) {
        try {
            TwoStageTransformationService.TransformationResult result = transformationService
                    .executeStage2Transformation(chartId, echartsTemplate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("finalEChartsConfig", result.getResult());
            response.put("queryResults", result.getQueryResults());

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 获取映射关系信息
     */
    @GetMapping("/mappings/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> getMappingInfo(
            @PathVariable String chartId) {
        try {
            // 初始化映射关系（如果还没有）
            if (mappingService.getChartMappings(chartId).isEmpty()) {
                mappingService.initializeSampleMappings();
            }

            Map<String, Object> mappings = mappingService.getChartMappings(chartId);
            Map<String, Integer> summary = mappingService.getMappingsSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappings", mappings);
            response.put("mappingCount", mappings.size());
            response.put("allChartsSummary", summary);

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 测试占位符管理功能
     */
    @PostMapping("/placeholder/test")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> testPlaceholderManager(
            @RequestBody Map<String, Object> testData) {
        try {
            // 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(testData);

            // 创建示例替换值
            Map<String, Object> sampleValues = placeholderManager.createSamplePlaceholderValues();

            // 执行替换
            Object replacedData = placeholderManager.replacePlaceholdersInJson(testData, sampleValues);

            // 验证占位符
            java.util.List<String> missingPlaceholders = placeholderManager.validatePlaceholders(testData,
                    sampleValues);

            Map<String, Object> response = new HashMap<>();
            response.put("originalData", testData);
            response.put("extractedPlaceholders", placeholders);
            response.put("sampleValues", sampleValues);
            response.put("replacedData", replacedData);
            response.put("missingPlaceholders", missingPlaceholders);
            response.put("validationPassed", missingPlaceholders.isEmpty());

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Two Stage Transformation Service");
        response.put("features", java.util.Arrays.asList(
                "占位符管理", "映射关系管理", "两阶段转换", "数据回填"));
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));
    }
}
