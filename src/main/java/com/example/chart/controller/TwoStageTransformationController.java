package com.example.chart.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chart.model.TemplateType;
import com.example.chart.service.MappingRelationshipService;
import com.example.chart.service.PlaceholderManager;
import com.example.chart.service.TemplateService;
import com.example.chart.service.TwoStageTransformationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 两阶段转换控制器
 * 提供API接口验证占位符系统和两阶段转换流程
 */
@RestController
@RequestMapping("/api/chart/two-stage")
public class TwoStageTransformationController {

    private static final Logger logger = LoggerFactory.getLogger(TwoStageTransformationController.class);

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
//    @GetMapping("/validate/{chartId}")
//    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> validateTwoStageTransformation(
//            @PathVariable String chartId) {
//        logger.info("🔍 [验证转换] 开始验证图表: {}", chartId);
//        long startTime = System.currentTimeMillis();
//
//        try {
//            logger.debug("📊 [验证转换] 调用验证服务，chartId: {}", chartId);
//
//            TwoStageTransformationService.TransformationResult result = transformationService
//                    .validateFullProcess(chartId);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", result.isSuccess());
//            response.put("message", result.getMessage());
//            response.put("finalEChartsConfig", result.getResult());
//
//            if (result.getPlaceholders() != null) {
//                response.put("processedPlaceholders", result.getPlaceholders());
//                logger.debug("🏷️ [验证转换] 处理的占位符数量: {}", result.getPlaceholders().size());
//            }
//
//            if (result.getQueryResults() != null) {
//                response.put("queryResults", result.getQueryResults());
//                logger.debug("📋 [验证转换] 查询结果包含 {} 项数据", result.getQueryResults().size());
//            }
//
//            // 添加转换流程信息
//            Map<String, Object> transformationInfo = transformationService.getTransformationInfo(chartId);
//            response.put("transformationInfo", transformationInfo);
//
//            long duration = System.currentTimeMillis() - startTime;
//            logger.info("✅ [验证转换] 验证完成，耗时: {}ms, 成功: {}, 图表: {}",
//                       duration, result.isSuccess(), chartId);
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("📤 [验证转换] 响应数据: success={}, message={}, placeholders={}, queryResults={}",
//                           result.isSuccess(), result.getMessage(),
//                           result.getPlaceholders() != null ? result.getPlaceholders().size() : 0,
//                           result.getQueryResults() != null ? result.getQueryResults().size() : 0);
//            }
//
//            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));
//
//        } catch (Exception e) {
//            long duration = System.currentTimeMillis() - startTime;
//            logger.error("❌ [验证转换] 验证失败，耗时: {}ms, 图表: {}, 错误: {}", duration, chartId, e.getMessage(), e);
//
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("error", e.getMessage());
//            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
//        }
//    }

    /**
     * 获取图表类型信息
     */
    @GetMapping("/chart-info/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> getChartInfo(
            @PathVariable String chartId) {
        logger.info("📊 [获取图表信息] 开始获取图表信息: {}", chartId);
        
        try {
            // 推断模板类型
            TemplateType templateType = TemplateType.inferFromChartType(chartId);
            logger.debug("📝 [获取图表信息] 推断的模板类型: {} -> {}", chartId, templateType);

            // 获取图表类型名称映射（与echarts目录下的JSON文件名一一对应）
            Map<String, String> chartTypeNames = new HashMap<>();
            chartTypeNames.put("basic_line_chart", "基础折线图");
            chartTypeNames.put("smooth_line_chart", "基础平滑折线图");
            chartTypeNames.put("stacked_line_chart", "折线图堆叠");
            chartTypeNames.put("basic_bar_chart", "基础柱状图");
            chartTypeNames.put("stacked_bar_chart", "堆叠柱状图");
            chartTypeNames.put("basic_pie_chart", "富文本标签");
            chartTypeNames.put("doughnut_chart", "圆角环形图");
            chartTypeNames.put("basic_radar_chart", "基础雷达图");
            chartTypeNames.put("basic_gauge_chart", "基础仪表盘");
            chartTypeNames.put("progress_gauge_chart", "进度仪表盘");
            chartTypeNames.put("grade_gauge_chart", "等级仪表盘");

            // 获取图表类别映射（与echarts目录下的子目录名一一对应）
            Map<String, String> chartCategories = new HashMap<>();
            chartCategories.put("basic_line_chart", "折线图");
            chartCategories.put("smooth_line_chart", "折线图");
            chartCategories.put("stacked_line_chart", "折线图");
            chartCategories.put("basic_bar_chart", "柱状图");
            chartCategories.put("stacked_bar_chart", "柱状图");
            chartCategories.put("basic_pie_chart", "饼图");
            chartCategories.put("doughnut_chart", "饼图");
            chartCategories.put("basic_radar_chart", "雷达图");
            chartCategories.put("basic_gauge_chart", "仪表盘");
            chartCategories.put("progress_gauge_chart", "仪表盘");
            chartCategories.put("grade_gauge_chart", "仪表盘");

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("chartName", chartTypeNames.getOrDefault(chartId, chartId));
            response.put("chartCategory", chartCategories.getOrDefault(chartId, "未知"));
            response.put("templateType", templateType.getCode());
            response.put("templateTypeName", templateType.getName());
            response.put("templateTypeDescription", templateType.getDescription());
            response.put("supportedChartTypes", templateType.getSupportedChartTypes());
            
            logger.info("✅ [获取图表信息] 获取成功: {} -> {}/{}, 模板类型: {}", 
                       chartId, chartTypeNames.getOrDefault(chartId, chartId), 
                       chartCategories.getOrDefault(chartId, "未知"), templateType.getCode());
            
            if (logger.isDebugEnabled()) {
                logger.debug("📤 [获取图表信息] 返回数据: chartName={}, category={}, templateType={}, supportedTypes={}", 
                           chartTypeNames.getOrDefault(chartId, chartId),
                           chartCategories.getOrDefault(chartId, "未知"),
                           templateType.getCode(),
                           templateType.getSupportedChartTypes().length);
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [获取图表信息] 获取失败: {}, 错误: {}", chartId, e.getMessage(), e);
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 获取带占位符的通用JSON模板
     */
    @GetMapping("/template/{chartId}")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> getUniversalTemplate(
            @PathVariable String chartId) {
        logger.info("🏷️ [获取模板] 开始获取通用模板: {}", chartId);
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("🔍 [获取模板] 调用分类模板服务获取模板: {}", chartId);
            logger.info("📎 [获取模板] 正在调用 templateService.getCategoryTemplateByChartId({})", chartId);
            
            // 使用新的分类模板
            Map<String, Object> template = templateService.getCategoryTemplateByChartId(chartId);
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(template);
            
            logger.info("🏷️ [获取模板] 模板提取成功，占位符数量: {}", placeholders.size());
            if (logger.isTraceEnabled()) {
                logger.trace("🏷️ [获取模板] 所有占位符: {}", placeholders);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("template", template);
            response.put("placeholders", placeholders);
            response.put("placeholderCount", placeholders.size());
            response.put("templateType", "category"); // 标识使用分类模板
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ [获取模板] 模板获取成功，耗时: {}ms, 图表: {}, 占位符: {}个", 
                       duration, chartId, placeholders.size());
            
            if (logger.isDebugEnabled()) {
                logger.debug("📤 [获取模板] 模板结构预览: templateType={}, size={}KB, placeholders={}", 
                           "category", 
                           objectMapper.writeValueAsString(template).length() / 1024.0,
                           placeholders.size());
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [获取模板] 获取失败，耗时: {}ms, 图表: {}, 错误: {}", duration, chartId, e.getMessage(), e);
            
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
        logger.info("🔄 [阶段1转换] 开始第一阶段转换: {}", chartId);
        long startTime = System.currentTimeMillis();
        
        try {
            // 输入数据日志
            if (logger.isDebugEnabled()) {
                Set<String> inputPlaceholders = placeholderManager.extractPlaceholdersFromJson(universalTemplate);
                int templateSize = objectMapper.writeValueAsString(universalTemplate).length();
                logger.info("📥 [阶段1转换] 输入数据: 模板大小={}KB, 占位符数量={}", 
                           templateSize / 1024.0, inputPlaceholders.size());
                
                if (logger.isTraceEnabled()) {
                    logger.trace("📥 [阶段1转换] 输入模板内容: {}", 
                               objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(universalTemplate));
                    logger.trace("📥 [阶段1转换] 输入占位符: {}", inputPlaceholders);
                }
            }
            
            System.out.println("🔄 执行第一阶段转换，图表类型: " + chartId);
            logger.info("📎 [阶段1转换] 正在调用 transformationService.executeStage1Transformation({})", chartId);

            TwoStageTransformationService.TransformationResult result = transformationService
                    .executeStage1Transformation(chartId, universalTemplate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("echartsStructure", result.getResult());
            response.put("preservedPlaceholders", result.getPlaceholders());
            response.put("chartId", chartId);
            response.put("usedJoltSpec", result.getUsedJoltSpec());
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (result.isSuccess()) {
                logger.info("✅ [阶段1转换] 第一阶段转换成功，耗时: {}ms, 图表: {}, 保留占位符: {}个", 
                           duration, chartId, result.getPlaceholders() != null ? result.getPlaceholders().size() : 0);
                
                // 输出数据日志
                if (logger.isDebugEnabled() && result.getResult() != null) {
                    int outputSize = objectMapper.writeValueAsString(result.getResult()).length();
                    logger.info("📤 [阶段1转换] 输出数据: 结构大小={}KB, Jolt规范={}, 保留占位符={}个", 
                               outputSize / 1024.0, result.getUsedJoltSpec(), 
                               result.getPlaceholders() != null ? result.getPlaceholders().size() : 0);
                    
                    if (logger.isTraceEnabled()) {
                        logger.trace("📤 [阶段1转换] 输出结构: {}", 
                                   objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getResult()));
                        if (result.getPlaceholders() != null) {
                            logger.trace("📤 [阶段1转换] 保留占位符: {}", result.getPlaceholders());
                        }
                    }
                }
            } else {
                logger.warn("⚠️ [阶段1转换] 第一阶段转换失败，耗时: {}ms, 图表: {}, 错误: {}", 
                           duration, chartId, result.getMessage());
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [阶段1转换] 第一阶段转换异常，耗时: {}ms, 图表: {}, 错误: {}", duration, chartId, e.getMessage(), e);
            
            System.err.println("❌ 第一阶段转换失败: " + e.getMessage());
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
        logger.info("⚙️ [阶段2转换] 开始第二阶段转换: {}", chartId);
        long startTime = System.currentTimeMillis();
        
        try {
            // 输入数据日志
            if (logger.isDebugEnabled()) {
                Set<String> inputPlaceholders = placeholderManager.extractPlaceholdersFromJson(echartsTemplate);
                int templateSize = objectMapper.writeValueAsString(echartsTemplate).length();
                logger.info("📥 [阶段2转换] 输入数据: ECharts模板大小={}KB, 待替换占位符={}个", 
                           templateSize / 1024.0, inputPlaceholders.size());
                
                if (logger.isTraceEnabled()) {
                    logger.trace("📥 [阶段2转换] 输入ECharts模板: {}", 
                               objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(echartsTemplate));
                    logger.trace("📥 [阶段2转换] 待替换占位符: {}", inputPlaceholders);
                }
            }
            
            logger.info("📎 [阶段2转换] 正在调用 transformationService.executeStage2Transformation({})", chartId);
            
            TwoStageTransformationService.TransformationResult result = transformationService
                    .executeStage2Transformation(chartId, echartsTemplate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("finalEChartsConfig", result.getResult());
            response.put("queryResults", result.getQueryResults());
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (result.isSuccess()) {
                logger.info("✅ [阶段2转换] 第二阶段转换成功，耗时: {}ms, 图表: {}, 查询结果: {}项", 
                           duration, chartId, 
                           result.getQueryResults() != null ? result.getQueryResults().size() : 0);
                
                // 输出数据日志
                if (logger.isDebugEnabled() && result.getResult() != null) {
                    int outputSize = objectMapper.writeValueAsString(result.getResult()).length();
                    Set<String> remainingPlaceholders = placeholderManager.extractPlaceholdersFromJson(result.getResult());
                    
                    logger.info("📤 [阶段2转换] 输出数据: 最终配置大小={}KB, 剩余占位符={}个, 查询结果={}项", 
                               outputSize / 1024.0, remainingPlaceholders.size(), 
                               result.getQueryResults() != null ? result.getQueryResults().size() : 0);
                    
                    if (remainingPlaceholders.size() > 0) {
                        logger.warn("⚠️ [阶段2转换] 注意：仍有未替换的占位符: {}", remainingPlaceholders);
                    }
                    
                    if (logger.isTraceEnabled()) {
                        logger.trace("📤 [阶段2转换] 最终ECharts配置: {}", 
                                   objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getResult()));
                        if (result.getQueryResults() != null) {
                            logger.trace("📤 [阶段2转换] 查询结果数据: {}", 
                                       objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getQueryResults()));
                        }
                    }
                }
            } else {
                logger.warn("⚠️ [阶段2转换] 第二阶段转换失败，耗时: {}ms, 图表: {}, 错误: {}", 
                           duration, chartId, result.getMessage());
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [阶段2转换] 第二阶段转换异常，耗时: {}ms, 图表: {}, 错误: {}", duration, chartId, e.getMessage(), e);
            
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
        logger.info("🗺️ [映射信息] 获取映射关系信息: {}", chartId);
        
        try {
            // 初始化映射关系（如果还没有）
            if (mappingService.getChartMappings(chartId).isEmpty()) {
                logger.debug("🔄 [映射信息] 初始化映射关系: {}", chartId);
                mappingService.initializeSampleMappings();
            }

            Map<String, Object> mappings = mappingService.getChartMappings(chartId);
            Map<String, Integer> summary = mappingService.getMappingsSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("chartId", chartId);
            response.put("mappings", mappings);
            response.put("mappingCount", mappings.size());
            response.put("allChartsSummary", summary);
            
            logger.info("✅ [映射信息] 获取成功: {} -> {} 个映射, 总统计: {} 个图表", 
                       chartId, mappings.size(), summary.size());
            
            if (logger.isDebugEnabled()) {
                logger.debug("📤 [映射信息] 映射列表: {}", mappings.keySet());
                logger.debug("📤 [映射信息] 所有图表统计: {}", summary);
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [映射信息] 获取失败: {}, 错误: {}", chartId, e.getMessage(), e);
            
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
        logger.info("🧪 [占位符测试] 开始测试占位符管理功能");
        long startTime = System.currentTimeMillis();
        
        try {
            // 输入数据日志
            if (logger.isDebugEnabled()) {
                int dataSize = objectMapper.writeValueAsString(testData).length();
                logger.debug("📥 [占位符测试] 输入数据大小: {}KB", dataSize / 1024.0);
                
                if (logger.isTraceEnabled()) {
                    logger.trace("📥 [占位符测试] 输入数据: {}", 
                               objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(testData));
                }
            }
            
            // 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(testData);
            logger.debug("🏷️ [占位符测试] 提取到 {} 个占位符", placeholders.size());

            // 创建示例替换值
            Map<String, Object> sampleValues = placeholderManager.createSamplePlaceholderValues();
            logger.debug("🎲 [占位符测试] 生成 {} 个示例替换值", sampleValues.size());

            // 执行替换
            Object replacedData = placeholderManager.replacePlaceholdersInJson(testData, sampleValues);

            // 验证占位符
            java.util.List<String> missingPlaceholders = placeholderManager.validatePlaceholders(testData,
                    sampleValues);
            
            boolean validationPassed = missingPlaceholders.isEmpty();
            logger.debug("✅ [占位符测试] 验证结果: {}, 缺失占位符: {}个", 
                       validationPassed ? "通过" : "失败", missingPlaceholders.size());
            
            if (!validationPassed && logger.isDebugEnabled()) {
                logger.debug("⚠️ [占位符测试] 缺失的占位符: {}", missingPlaceholders);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("originalData", testData);
            response.put("extractedPlaceholders", placeholders);
            response.put("sampleValues", sampleValues);
            response.put("replacedData", replacedData);
            response.put("missingPlaceholders", missingPlaceholders);
            response.put("validationPassed", validationPassed);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ [占位符测试] 测试完成，耗时: {}ms, 占位符: {}个, 验证: {}", 
                       duration, placeholders.size(), validationPassed ? "通过" : "失败");
            
            // 输出数据日志
            if (logger.isTraceEnabled()) {
                logger.trace("📤 [占位符测试] 替换后数据: {}", 
                           objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(replacedData));
                logger.trace("📤 [占位符测试] 示例替换值: {}", 
                           objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleValues));
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [占位符测试] 测试异常，耗时: {}ms, 错误: {}", duration, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 重新加载映射关系
     */
    @PostMapping("/mappings/reload")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> reloadMappings() {
        logger.info("🔄 [映射重载] 开始重新加载映射关系");
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean wasInitialized = mappingService.isInitialized();
            logger.debug("📊 [映射重载] 重载前状态: {}", wasInitialized ? "已初始化" : "未初始化");
            
            mappingService.reloadMappings();
            
            boolean isNowInitialized = mappingService.isInitialized();
            long duration = System.currentTimeMillis() - startTime;
            
            response.put("message", "映射关系重新加载成功");
            response.put("timestamp", System.currentTimeMillis());
            response.put("initialized", isNowInitialized);
            response.put("duration", duration);
            
            logger.info("✅ [映射重载] 重载成功，耗时: {}ms, 状态: {} -> {}", 
                       duration, 
                       wasInitialized ? "已初始化" : "未初始化",
                       isNowInitialized ? "已初始化" : "未初始化");
            
            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [映射重载] 重载失败，耗时: {}ms, 错误: {}", duration, e.getMessage(), e);
            
            response.put("message", "映射关系重新加载失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            response.put("duration", duration);
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("RELOAD_FAILED", e.getMessage()));
        }
    }

    /**
     * 扫描ECharts目录结构
     */
    @GetMapping("/echarts-directory")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> scanEChartsDirectory() {
        logger.info("📁 [目录扫描] 开始扫描ECharts目录结构");
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, java.util.List<Map<String, String>>> directoryStructure = new HashMap<>();

            // 扫描echarts目录
            java.io.File echartsDir = new java.io.File("src/main/resources/echarts");
            logger.debug("📂 [目录扫描] 扫描路径: {}", echartsDir.getAbsolutePath());
            
            if (!echartsDir.exists() || !echartsDir.isDirectory()) {
                logger.error("❌ [目录扫描] ECharts目录不存在: {}", echartsDir.getAbsolutePath());
                response.put("error", "ECharts目录不存在");
                return ResponseEntity.status(404)
                        .body(com.example.api.ApiResponse.error("DIRECTORY_NOT_FOUND", "ECharts目录不存在"));
            }

            java.io.File[] categories = echartsDir.listFiles(java.io.File::isDirectory);
            if (categories != null) {
                logger.debug("📁 [目录扫描] 发现 {} 个类别目录", categories.length);
                
                for (java.io.File category : categories) {
                    String categoryName = category.getName();
                    java.util.List<Map<String, String>> files = new java.util.ArrayList<>();

                    java.io.File[] jsonFiles = category.listFiles((dir, name) -> name.endsWith(".json"));
                    if (jsonFiles != null) {
                        logger.debug("📄 [目录扫描] 类别 '{}' 包含 {} 个JSON文件", categoryName, jsonFiles.length);
                        
                        for (java.io.File jsonFile : jsonFiles) {
                            String fileName = jsonFile.getName();
                            String displayName = fileName.replace(".json", "");

                            Map<String, String> fileInfo = new HashMap<>();
                            fileInfo.put("fileName", fileName);
                            fileInfo.put("displayName", displayName);
                            fileInfo.put("filePath", categoryName + "/" + fileName);
                            files.add(fileInfo);
                            
                            logger.trace("📄 [目录扫描] 添加文件: {} -> {}", fileName, displayName);
                        }
                    } else {
                        logger.debug("📁 [目录扫描] 类别 '{}' 不包含JSON文件", categoryName);
                    }

                    directoryStructure.put(categoryName, files);
                }
            } else {
                logger.warn("⚠️ [目录扫描] 无法读取类别目录");
            }

            response.put("directoryStructure", directoryStructure);
            response.put("categories", directoryStructure.keySet());
            response.put("totalCategories", directoryStructure.size());

            int totalFiles = directoryStructure.values().stream()
                    .mapToInt(java.util.List::size)
                    .sum();
            response.put("totalFiles", totalFiles);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ [目录扫描] 扫描完成，耗时: {}ms, 类别: {}个, 文件: {}个", 
                       duration, directoryStructure.size(), totalFiles);
            
            if (logger.isDebugEnabled()) {
                logger.debug("📤 [目录扫描] 扫描结果概要:");
                for (Map.Entry<String, java.util.List<Map<String, String>>> entry : directoryStructure.entrySet()) {
                    logger.debug("📁   类别 '{}': {} 个文件", entry.getKey(), entry.getValue().size());
                }
            }

            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [目录扫描] 扫描失败，耗时: {}ms, 错误: {}", duration, e.getMessage(), e);
            
            System.err.println("扫描ECharts目录失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("SCAN_ERROR", e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> health() {
        logger.debug("🌡️ [健康检查] 收到健康检查请求");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Two Stage Transformation Service");
        response.put("features", java.util.Arrays.asList(
                "占位符管理", "映射关系管理", "两阶段转换", "数据回填", "目录扫描"));
        response.put("timestamp", System.currentTimeMillis());
        
        logger.debug("✅ [健康检查] 服务状态: 正常");
        return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));
    }
}
