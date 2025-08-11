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
            chartTypeNames.put("ring_chart", "环形图");
            chartTypeNames.put("nested_pie_chart", "嵌套饼图");
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
            chartCategories.put("ring_chart", "饼图");
            chartCategories.put("nested_pie_chart", "饼图");
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
            // 检查映射关系是否存在（不再自动初始化）
            Map<String, Object> mappings = mappingService.getChartMappings(chartId);
            if (mappings.isEmpty()) {
                logger.error("❌ [映射信息] 映射关系未初始化: {}", chartId);
                throw new IllegalStateException("映射关系未初始化，请先调用 /mappings/reload 接口");
            }

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
                            String filePath = categoryName + "/" + fileName;
                            String chartId = generateChartIdFromFilePath(filePath);

                            Map<String, String> fileInfo = new HashMap<>();
                            fileInfo.put("fileName", fileName);
                            fileInfo.put("displayName", displayName);
                            fileInfo.put("filePath", filePath);
                            fileInfo.put("chartId", chartId);
                            fileInfo.put("status", getChartImplementationStatus(chartId));
                            files.add(fileInfo);
                            
                            logger.trace("📄 [目录扫描] 添加文件: {} -> {} (chartId: {})", fileName, displayName, chartId);
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
     * 根据文件路径生成chartId
     */
    private String generateChartIdFromFilePath(String filePath) {
        Map<String, String> pathToIdMapping = new HashMap<>();
        
        // 映射关系
        pathToIdMapping.put("折线图/基础折线图.json", "basic_line_chart");
        pathToIdMapping.put("折线图/基础平滑折线图.json", "smooth_line_chart");
        pathToIdMapping.put("折线图/折线图堆叠.json", "stacked_line_chart");
        pathToIdMapping.put("柱状图/基础柱状图.json", "basic_bar_chart");
        pathToIdMapping.put("柱状图/堆叠柱状图.json", "stacked_bar_chart");
        pathToIdMapping.put("饼图/富文本标签.json", "basic_pie_chart");
        pathToIdMapping.put("饼图/圆角环形图.json", "ring_chart");
        pathToIdMapping.put("饼图/嵌套饼图.json", "nested_pie_chart");
        pathToIdMapping.put("雷达图/基础雷达图.json", "basic_radar_chart");
        pathToIdMapping.put("仪表盘/基础仪表盘.json", "basic_gauge_chart");
        pathToIdMapping.put("仪表盘/进度仪表盘.json", "progress_gauge_chart");
        pathToIdMapping.put("仪表盘/等级仪表盘.json", "grade_gauge_chart");
        
        return pathToIdMapping.getOrDefault(filePath, filePath.replace(".json", "").replaceAll("[/\\\\]", "_"));
    }

    /**
     * 获取图表实现状态
     */
    private String getChartImplementationStatus(String chartId) {
        java.util.Set<String> implementedCharts = java.util.Set.of(
            "stacked_line_chart", "basic_bar_chart", "stacked_bar_chart", 
            "basic_line_chart", "smooth_line_chart", "basic_pie_chart", 
            "ring_chart", "nested_pie_chart", "basic_radar_chart", "basic_gauge_chart",
            "progress_gauge_chart", "grade_gauge_chart"
        );
        
        java.util.Set<String> plannedCharts = java.util.Set.of(
            "basic_area_chart", "rose_chart", "filled_radar_chart"
        );
        
        if (implementedCharts.contains(chartId)) {
            return "implemented";
        } else if (plannedCharts.contains(chartId)) {
            return "planned";
        } else {
            return "unknown";
        }
    }

    /**
     * 获取图表分类列表（错误恢复时使用）
     */
    @GetMapping("/categories")
    public ResponseEntity<com.example.api.ApiResponse<Map<String, Object>>> getCategories() {
        logger.info("📁 [分类获取] 获取图表分类列表");
        
        try {
            Map<String, Object> response = new HashMap<>();
            java.util.List<Map<String, String>> categories = new java.util.ArrayList<>();
            
            // 定义分类信息
            String[][] categoryData = {
                {"折线图", "Line Chart", "LineChartOutlined"},
                {"柱状图", "Bar Chart", "BarChartOutlined"},
                {"饼图", "Pie Chart", "PieChartOutlined"},
                {"雷达图", "Radar Chart", "RadarChartOutlined"},
                {"仪表盘", "Gauge Chart", "DashboardOutlined"}
            };
            
            for (String[] data : categoryData) {
                Map<String, String> category = new HashMap<>();
                category.put("name", data[0]);
                category.put("displayName", data[1]);
                category.put("iconName", data[2]);
                categories.add(category);
            }
            
            response.put("categories", categories);
            response.put("totalCategories", categories.size());
            
            logger.info("✅ [分类获取] 返回 {} 个分类", categories.size());
            return ResponseEntity.ok(com.example.api.ApiResponse.ok(response));
            
        } catch (Exception e) {
            logger.error("❌ [分类获取] 获取分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(com.example.api.ApiResponse.error("CATEGORY_ERROR", e.getMessage()));
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
