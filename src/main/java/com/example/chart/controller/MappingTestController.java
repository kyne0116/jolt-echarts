package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.example.chart.service.PlaceholderMappingManager;
import com.example.chart.service.TwoStageTransformationService;
import com.example.chart.service.TemplateService;
import com.example.chart.service.SimpleUniversalDataCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 映射功能测试控制器
 * 用于验证三种折线图的完整转换流程
 */
@RestController
@RequestMapping("/api/test/mapping")
@CrossOrigin(origins = "*")
public class MappingTestController {

    private static final Logger logger = LoggerFactory.getLogger(MappingTestController.class);

    @Autowired
    private PlaceholderMappingManager mappingManager;

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SimpleUniversalDataCrudService dataService;

    /**
     * 测试所有三种折线图的映射功能
     */
    @GetMapping("/test-all-line-charts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testAllLineCharts() {
        logger.info("🧪 [映射测试] 开始测试所有折线图类型的映射功能");

        Map<String, Object> testResults = new HashMap<>();
        List<String> chartTypes = Arrays.asList("basic_line_chart", "smooth_line_chart", "stacked_line_chart");
        
        for (String chartType : chartTypes) {
            try {
                Map<String, Object> result = testSingleChartMapping(chartType);
                testResults.put(chartType, result);
                logger.info("✅ [映射测试] {} 测试完成", chartType);
            } catch (Exception e) {
                logger.error("❌ [映射测试] {} 测试失败: {}", chartType, e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", e.getMessage());
                testResults.put(chartType, errorResult);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("testResults", testResults);
        response.put("totalTests", chartTypes.size());
        response.put("successCount", testResults.values().stream()
                .mapToInt(result -> ((Map<String, Object>) result).get("success").equals(true) ? 1 : 0)
                .sum());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 测试单个图表类型的映射功能
     */
    @GetMapping("/test-chart/{chartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testSingleChart(@PathVariable String chartId) {
        logger.info("🧪 [映射测试] 测试图表类型: {}", chartId);

        try {
            Map<String, Object> result = testSingleChartMapping(chartId);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            logger.error("❌ [映射测试] 测试失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("TEST_FAILED", "测试失败: " + e.getMessage()));
        }
    }

    /**
     * 获取虚拟数据库统计信息
     */
    @GetMapping("/database-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseStats() {
        logger.info("📊 [映射测试] 获取虚拟数据库统计信息");

        try {
            var allData = dataService.findAll();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecords", allData.size());
            
            // 按年份统计
            Map<String, Long> yearStats = allData.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            data -> data.getYear(),
                            java.util.stream.Collectors.counting()));
            stats.put("yearDistribution", yearStats);
            
            // 按月份统计
            Map<String, Long> monthStats = allData.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            data -> data.getMonth(),
                            java.util.stream.Collectors.counting()));
            stats.put("monthDistribution", monthStats);
            
            // 按渠道统计
            Map<String, Long> channelStats = allData.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            data -> data.getChannel(),
                            java.util.stream.Collectors.counting()));
            stats.put("channelDistribution", channelStats);
            
            // 按产品统计
            Map<String, Long> productStats = allData.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            data -> data.getProduct(),
                            java.util.stream.Collectors.counting()));
            stats.put("productDistribution", productStats);

            return ResponseEntity.ok(ApiResponse.ok(stats));
        } catch (Exception e) {
            logger.error("❌ [映射测试] 获取数据库统计失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("STATS_FAILED", "获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取映射配置信息
     */
    @GetMapping("/mapping-config/{chartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMappingConfig(@PathVariable String chartId) {
        logger.info("🔍 [映射测试] 获取映射配置: {}", chartId);

        try {
            Map<String, PlaceholderMappingManager.FieldMapping> mappings = mappingManager.getMappings(chartId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappings", mappings);
            response.put("mappingCount", mappings.size());
            response.put("hasConfig", !mappings.isEmpty());

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            logger.error("❌ [映射测试] 获取映射配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("CONFIG_FAILED", "获取映射配置失败: " + e.getMessage()));
        }
    }

    /**
     * 执行单个图表的完整测试流程
     */
    private Map<String, Object> testSingleChartMapping(String chartId) {
        Map<String, Object> result = new HashMap<>();
        
        // 步骤1：获取模板
        logger.info("📋 [映射测试] 步骤1：获取模板 - {}", chartId);
        Map<String, Object> template = templateService.getTemplateByChartId(chartId);
        result.put("step1_template", template);
        
        // 步骤2：执行第一阶段转换
        logger.info("🔄 [映射测试] 步骤2：第一阶段转换 - {}", chartId);
        TwoStageTransformationService.TransformationResult stage1Result = 
                transformationService.executeStage1Transformation(chartId, template);
        result.put("step2_stage1", Map.of(
                "success", stage1Result.isSuccess(),
                "message", stage1Result.getMessage(),
                "result", stage1Result.getResult()
        ));
        
        if (!stage1Result.isSuccess()) {
            result.put("success", false);
            result.put("error", "第一阶段转换失败: " + stage1Result.getMessage());
            return result;
        }
        
        // 步骤3：执行映射和第二阶段转换
        logger.info("🔗 [映射测试] 步骤3：映射和第二阶段转换 - {}", chartId);
        PlaceholderMappingManager.MappingResult mappingResult = 
                mappingManager.executeMapping(chartId, stage1Result.getResult());
        result.put("step3_mapping", Map.of(
                "success", mappingResult.isSuccess(),
                "message", mappingResult.getMessage(),
                "mappedData", mappingResult.getData().get("mappedData"),
                "finalResult", mappingResult.getData().get("result"),
                "unmappedPlaceholders", mappingResult.getUnmappedPlaceholders()
        ));
        
        // 步骤4：验证结果
        logger.info("✅ [映射测试] 步骤4：验证结果 - {}", chartId);
        boolean isValid = validateMappingResult(mappingResult);
        result.put("step4_validation", Map.of(
                "isValid", isValid,
                "hasRealData", hasRealDataFromDatabase(mappingResult),
                "placeholdersCovered", mappingResult.getUnmappedPlaceholders().isEmpty()
        ));
        
        result.put("success", stage1Result.isSuccess() && mappingResult.isSuccess() && isValid);
        result.put("chartId", chartId);
        result.put("timestamp", new Date());
        
        return result;
    }
    
    /**
     * 验证映射结果的有效性
     */
    private boolean validateMappingResult(PlaceholderMappingManager.MappingResult mappingResult) {
        if (!mappingResult.isSuccess()) {
            return false;
        }
        
        Object finalResult = mappingResult.getData().get("result");
        if (finalResult == null) {
            return false;
        }
        
        // 检查是否包含ECharts必要的结构
        if (finalResult instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) finalResult;
            return resultMap.containsKey("title") || resultMap.containsKey("xAxis") || resultMap.containsKey("series");
        }
        
        return false;
    }
    
    /**
     * 检查映射结果是否包含来自数据库的真实数据
     */
    private boolean hasRealDataFromDatabase(PlaceholderMappingManager.MappingResult mappingResult) {
        Object mappedData = mappingResult.getData().get("mappedData");
        if (mappedData instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) mappedData;
            // 检查是否有非空的数据值
            return dataMap.values().stream().anyMatch(value -> 
                    value != null && !value.toString().isEmpty() && !value.toString().equals("null"));
        }
        return false;
    }
}
