package com.example.chart.controller;

import com.example.chart.service.StackedLineChartValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 图表验证控制器
 * 提供API接口来验证图表转换流程
 */
@RestController
@RequestMapping("/api/chart/validation")
public class ChartValidationController {

    @Autowired
    private StackedLineChartValidationService validationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证堆叠折线图转换流程
     */
    @GetMapping("/stacked-line")
    public ResponseEntity<Map<String, Object>> validateStackedLineChart() {
        try {
            StackedLineChartValidationService.ValidationResult result = 
                validationService.validateStackedLineChart();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isValid());
            response.put("message", result.getMessage());
            response.put("generatedConfig", result.getEchartsConfig());
            
            // 加载原始配置进行对比
            ClassPathResource originalResource = new ClassPathResource("echarts/折线图/折线图堆叠.json");
            String originalContent = new String(originalResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> originalConfig = objectMapper.readValue(originalContent, Map.class);
            response.put("originalConfig", originalConfig);
            
            // 结构对比分析
            Map<String, Object> comparison = compareConfigs(originalConfig, result.getEchartsConfig());
            response.put("comparison", comparison);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取Mock数据示例
     */
    @GetMapping("/mock-data")
    public ResponseEntity<Map<String, Object>> getMockData() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("mockDatabaseResults", validationService.getMockDatabaseResults());
            response.put("universalFormat", validationService.convertToUniversalFormat(
                validationService.getMockDatabaseResults()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 对比原始配置和生成配置的结构
     */
    private Map<String, Object> compareConfigs(Map<String, Object> original, Map<String, Object> generated) {
        Map<String, Object> comparison = new HashMap<>();
        
        // 检查主要字段
        String[] mainFields = {"title", "tooltip", "legend", "xAxis", "yAxis", "series"};
        Map<String, Boolean> fieldComparison = new HashMap<>();
        
        for (String field : mainFields) {
            boolean originalHas = original.containsKey(field);
            boolean generatedHas = generated != null && generated.containsKey(field);
            fieldComparison.put(field, originalHas && generatedHas);
        }
        
        comparison.put("fieldComparison", fieldComparison);
        
        // 检查系列数量
        if (original.containsKey("series") && generated != null && generated.containsKey("series")) {
            int originalSeriesCount = ((java.util.List<?>) original.get("series")).size();
            int generatedSeriesCount = ((java.util.List<?>) generated.get("series")).size();
            
            comparison.put("seriesCount", Map.of(
                "original", originalSeriesCount,
                "generated", generatedSeriesCount,
                "match", originalSeriesCount == generatedSeriesCount
            ));
        }
        
        // 检查类别数据
        if (original.containsKey("xAxis") && generated != null && generated.containsKey("xAxis")) {
            Map<String, Object> originalXAxis = (Map<String, Object>) original.get("xAxis");
            Map<String, Object> generatedXAxis = (Map<String, Object>) generated.get("xAxis");
            
            if (originalXAxis.containsKey("data") && generatedXAxis.containsKey("data")) {
                java.util.List<?> originalData = (java.util.List<?>) originalXAxis.get("data");
                java.util.List<?> generatedData = (java.util.List<?>) generatedXAxis.get("data");
                
                comparison.put("categoryData", Map.of(
                    "originalCount", originalData.size(),
                    "generatedCount", generatedData.size(),
                    "match", originalData.equals(generatedData)
                ));
            }
        }
        
        return comparison;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Chart Validation Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
