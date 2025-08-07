package com.example.chart.controller;

import com.example.chart.service.TwoStageTransformationService;
import com.example.chart.service.PlaceholderManager;
import com.example.chart.service.MappingRelationshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证完整的两阶段转换流程
     */
    @GetMapping("/validate/{chartId}")
    public ResponseEntity<Map<String, Object>> validateTwoStageTransformation(@PathVariable String chartId) {
        try {
            TwoStageTransformationService.TransformationResult result = 
                transformationService.validateFullProcess(chartId);
            
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
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取带占位符的通用JSON模板
     */
    @GetMapping("/template/{chartId}")
    public ResponseEntity<Map<String, Object>> getUniversalTemplate(@PathVariable String chartId) {
        try {
            Map<String, Object> template = transformationService.createUniversalTemplateWithPlaceholders();
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(template);
            
            Map<String, Object> response = new HashMap<>();
            response.put("template", template);
            response.put("placeholders", placeholders);
            response.put("placeholderCount", placeholders.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 执行第一阶段转换（结构转换）
     */
    @PostMapping("/stage1")
    public ResponseEntity<Map<String, Object>> executeStage1Transformation(@RequestBody Map<String, Object> universalTemplate) {
        try {
            TwoStageTransformationService.TransformationResult result = 
                transformationService.executeStage1Transformation(universalTemplate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("echartsStructure", result.getResult());
            response.put("preservedPlaceholders", result.getPlaceholders());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 执行第二阶段转换（数据回填）
     */
    @PostMapping("/stage2/{chartId}")
    public ResponseEntity<Map<String, Object>> executeStage2Transformation(
            @PathVariable String chartId, 
            @RequestBody Map<String, Object> echartsTemplate) {
        try {
            TwoStageTransformationService.TransformationResult result = 
                transformationService.executeStage2Transformation(chartId, echartsTemplate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("finalEChartsConfig", result.getResult());
            response.put("queryResults", result.getQueryResults());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取映射关系信息
     */
    @GetMapping("/mappings/{chartId}")
    public ResponseEntity<Map<String, Object>> getMappingInfo(@PathVariable String chartId) {
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
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 测试占位符管理功能
     */
    @PostMapping("/placeholder/test")
    public ResponseEntity<Map<String, Object>> testPlaceholderManager(@RequestBody Map<String, Object> testData) {
        try {
            // 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(testData);
            
            // 创建示例替换值
            Map<String, Object> sampleValues = placeholderManager.createSamplePlaceholderValues();
            
            // 执行替换
            Object replacedData = placeholderManager.replacePlaceholdersInJson(testData, sampleValues);
            
            // 验证占位符
            java.util.List<String> missingPlaceholders = placeholderManager.validatePlaceholders(testData, sampleValues);
            
            Map<String, Object> response = new HashMap<>();
            response.put("originalData", testData);
            response.put("extractedPlaceholders", placeholders);
            response.put("sampleValues", sampleValues);
            response.put("replacedData", replacedData);
            response.put("missingPlaceholders", missingPlaceholders);
            response.put("validationPassed", missingPlaceholders.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Two Stage Transformation Service");
        response.put("features", java.util.Arrays.asList(
            "占位符管理", "映射关系管理", "两阶段转换", "数据回填"
        ));
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
