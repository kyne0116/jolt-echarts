package com.example.chart.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 两阶段转换服务
 * 实现从通用JSON模板到最终ECharts配置的两阶段转换流程
 */
@Service
public class TwoStageTransformationService {

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private PlaceholderMappingManager mappingManager;

    @Autowired
    private MappingRelationshipService mappingService;

    @Autowired
    private SmartTransformationEngine smartEngine;

    @Autowired
    private ChartRegistryService chartRegistryService;

    @Autowired
    private TemplateService templateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 转换结果类
     */
    public static class TransformationResult {
        private boolean success;
        private String message;
        private Object result;
        private Set<String> placeholders;
        private Map<String, Object> queryResults;
        private String usedJoltSpec; // 新增：记录使用的Jolt规范文件名

        public TransformationResult(boolean success, String message, Object result) {
            this.success = success;
            this.message = message;
            this.result = result;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Set<String> getPlaceholders() {
            return placeholders;
        }

        public void setPlaceholders(Set<String> placeholders) {
            this.placeholders = placeholders;
        }

        public Map<String, Object> getQueryResults() {
            return queryResults;
        }

        public void setQueryResults(Map<String, Object> queryResults) {
            this.queryResults = queryResults;
        }

        public String getUsedJoltSpec() {
            return usedJoltSpec;
        }

        public void setUsedJoltSpec(String usedJoltSpec) {
            this.usedJoltSpec = usedJoltSpec;
        }
    }

    /**
     * 完整的两阶段转换流程
     */
    public TransformationResult executeFullTransformation(String chartId, Map<String, Object> universalTemplate) {
        try {
            System.out.println("=== 开始两阶段转换流程 ===");

            // 第一阶段：结构转换（保持占位符）
            System.out.println("第一阶段：执行结构转换...");
            TransformationResult stage1Result = executeStage1Transformation(universalTemplate);

            if (!stage1Result.isSuccess()) {
                return stage1Result;
            }

            // 第二阶段：数据回填（替换占位符）
            System.out.println("第二阶段：执行数据回填...");
            TransformationResult stage2Result = executeStage2Transformation(chartId, stage1Result.getResult());

            if (stage2Result.isSuccess()) {
                System.out.println("✅ 两阶段转换完成");
            }

            return stage2Result;

        } catch (Exception e) {
            System.err.println("两阶段转换失败: " + e.getMessage());
            return new TransformationResult(false, "转换失败: " + e.getMessage(), null);
        }
    }

    /**
     * 第一阶段：结构转换（保持占位符）
     */
    public TransformationResult executeStage1Transformation(String chartId, Map<String, Object> universalTemplate) {
        try {
            System.out.println("=== 第一阶段转换开始（分类模板语义转换）===");
            System.out.println("图表类型: " + chartId);

            // 提取模板中的占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(universalTemplate);
            System.out.println("发现占位符: " + placeholders);

            // 使用新的分类模板转换引擎
            Map<String, Object> structuralResult = smartEngine.semanticTransformWithCategory(chartId,
                    universalTemplate);
            System.out.println("使用分类模板转换引擎，根据图表类型自动适配");

            // 验证转换后占位符是否保持
            Set<String> afterPlaceholders = placeholderManager.extractPlaceholdersFromJson(structuralResult);
            System.out.println("转换后占位符: " + afterPlaceholders);

            TransformationResult result = new TransformationResult(true, "第一阶段转换成功", structuralResult);
            result.setPlaceholders(afterPlaceholders);
            result.setUsedJoltSpec("CategoryTemplateEngine"); // 使用分类模板引擎

            System.out.println("=== 第一阶段转换完成 ===");
            return result;

        } catch (Exception e) {
            System.err.println("第一阶段转换失败: " + e.getMessage());
            e.printStackTrace();
            return new TransformationResult(false, "第一阶段转换失败: " + e.getMessage(), null);
        }
    }

    /**
     * 第一阶段：结构转换（保持占位符）- 兼容旧版本API
     * 
     * @deprecated 请使用 executeStage1Transformation(String chartId, Map<String,
     *             Object> universalTemplate)
     */
    @Deprecated
    public TransformationResult executeStage1Transformation(Map<String, Object> universalTemplate) {
        System.out.println("⚠️ 使用了已废弃的API，默认使用堆叠折线图规范");
        return executeStage1Transformation("stacked_line_chart", universalTemplate);
    }

    /**
     * 第二阶段：数据回填（替换占位符）
     */
    public TransformationResult executeStage2Transformation(String chartId, Object echartsTemplate) {
        try {
            System.out.println("=== 第二阶段转换开始（数据回填）===");
            System.out.println("图表类型: " + chartId);

            // 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(echartsTemplate);
            System.out.println("发现占位符: " + placeholders);

            if (placeholders.isEmpty()) {
                System.out.println("⚠️ 未发现占位符，直接返回原始配置");
                return new TransformationResult(true, "无需数据回填", echartsTemplate);
            }

            // 优先尝试使用新的映射管理器
            try {
                PlaceholderMappingManager.MappingResult mappingResult = mappingManager.executeMapping(chartId,
                        echartsTemplate);

                if (mappingResult.isSuccess()) {
                    System.out.println("✅ 使用映射管理器成功执行数据回填");
                    Object finalResult = mappingResult.getData().get("result");

                    TransformationResult result = new TransformationResult(true, "第二阶段转换成功（使用映射管理器）", finalResult);
                    result.setQueryResults((Map<String, Object>) mappingResult.getData().get("mappedData"));
                    return result;
                } else {
                    System.out.println("⚠️ 映射管理器执行失败，回退到传统方式: " + mappingResult.getMessage());
                }
            } catch (Exception e) {
                System.out.println("⚠️ 映射管理器异常，回退到传统方式: " + e.getMessage());
            }

            // 回退到传统的映射服务和注册表
            // 检查注册表中是否有激活的映射版本（暂时跳过新映射服务以避免循环依赖）
            var registry = chartRegistryService.get(chartId);
            if (registry.isPresent() && registry.get().getActiveMappingVersion() != null) {
                System.out.println("📋 检测到注册表中的激活映射版本: " + registry.get().getActiveMappingVersion() + "，但暂时使用旧映射服务");
            }

            // 回退到旧的映射服务
            System.out.println("📋 回退到旧映射服务");

            // 初始化映射关系（关键步骤！）
            mappingService.initializeSampleMappings();
            System.out.println("✅ 映射关系初始化完成");

            System.out.println("需要替换的占位符: " + placeholders);

            // 验证映射关系
            List<String> missingMappings = mappingService.validateMappings(chartId, placeholders);
            if (!missingMappings.isEmpty()) {
                return new TransformationResult(false, "缺少映射关系: " + missingMappings, null);
            }

            // 模拟数据库查询
            Map<String, Object> queryResults = mappingService.simulateDataQuery(chartId, placeholders);
            System.out.println("查询结果: " + queryResults.keySet());

            // 验证转换前数据
            if (!validateTransformationData(echartsTemplate, queryResults)) {
                return new TransformationResult(false, "转换前数据验证失败", null);
            }

            // 替换占位符
            Object finalResult = placeholderManager.replacePlaceholdersInJson(echartsTemplate, queryResults);

            TransformationResult result = new TransformationResult(true, "第二阶段转换成功", finalResult);
            result.setQueryResults(queryResults);

            return result;

        } catch (Exception e) {
            System.err.println("第二阶段转换失败: " + e.getMessage());
            return new TransformationResult(false, "第二阶段转换失败: " + e.getMessage(), null);
        }
    }

    /**
     * 使用Jolt进行结构转换（保持占位符）- 根据图表类型动态选择规范
     */
    private Map<String, Object> transformStructureWithJolt(String chartId, Map<String, Object> universalTemplate)
            throws IOException {
        // 根据图表类型获取对应的Jolt规范文件
        String joltSpecFile = getJoltSpecFileByChartId(chartId);
        System.out.println("使用Jolt规范文件: " + joltSpecFile);

        // 加载对应的Jolt规范
        ClassPathResource resource = new ClassPathResource("jolt-specs/" + joltSpecFile);
        if (!resource.exists()) {
            throw new IOException("Jolt规范文件不存在: jolt-specs/" + joltSpecFile);
        }

        String joltSpecContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 解析Jolt规范
        List<Object> joltSpec = JsonUtils.jsonToList(joltSpecContent);
        Chainr chainr = Chainr.fromSpec(joltSpec);

        // 执行Jolt转换
        Object transformedObj = chainr.transform(universalTemplate);

        return (Map<String, Object>) transformedObj;
    }

    /**
     * 使用Jolt进行结构转换（保持占位符）- 兼容旧版本
     * 
     * @deprecated 请使用 transformStructureWithJolt(String chartId, Map<String,
     *             Object> universalTemplate)
     */
    @Deprecated
    private Map<String, Object> transformStructureWithJolt(Map<String, Object> universalTemplate) throws IOException {
        System.out.println("⚠️ 使用了已废弃的transformStructureWithJolt方法");
        return transformStructureWithJolt("stacked_line_chart", universalTemplate);
    }

    /**
     * 验证转换前数据
     */
    private boolean validateTransformationData(Object template, Map<String, Object> queryResults) {
        try {
            // 1. 验证模板不为空
            if (template == null) {
                System.err.println("❌ 模板数据为空");
                return false;
            }

            // 2. 验证查询结果不为空
            if (queryResults == null || queryResults.isEmpty()) {
                System.err.println("❌ 查询结果为空");
                return false;
            }

            // 3. 验证模板是否为有效JSON
            String templateJson = objectMapper.writeValueAsString(template);
            if (templateJson.length() < 10) { // 基本长度检查
                System.err.println("❌ 模板JSON过短，可能无效");
                return false;
            }

            // 4. 验证查询结果中的数据类型
            for (Map.Entry<String, Object> entry : queryResults.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    System.out.println("⚠️ 占位符 " + key + " 的值为null");
                    continue;
                }

                // 检查复杂数据结构
                if (value instanceof Map || value instanceof List) {
                    try {
                        objectMapper.writeValueAsString(value);
                    } catch (Exception e) {
                        System.err.println("❌ 占位符 " + key + " 的值无法序列化: " + e.getMessage());
                        return false;
                    }
                }
            }

            System.out.println("✅ 转换前数据验证通过");
            return true;

        } catch (Exception e) {
            System.err.println("❌ 数据验证过程中出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据图表类型ID获取对应的Jolt规范文件名
     */
    private String getJoltSpecFileByChartId(String chartId) {
        Map<String, String> chartToSpecMapping = new HashMap<>();

        // 已实现的图表类型 (5种) - 有完整的JOLT SPEC文件
        chartToSpecMapping.put("stacked_line_chart", "line-chart-stacked.json");
        chartToSpecMapping.put("basic_bar_chart", "bar-chart-placeholder.json");
        chartToSpecMapping.put("stacked_bar_chart", "bar-chart-placeholder.json");
        chartToSpecMapping.put("basic_line_chart", "line-chart-placeholder.json");
        chartToSpecMapping.put("smooth_line_chart", "line-chart-placeholder.json");

        // 部分实现的图表类型 - 使用新创建的JOLT SPEC文件
        chartToSpecMapping.put("basic_pie_chart", "pie-chart-placeholder.json");
        chartToSpecMapping.put("doughnut_chart", "pie-chart-placeholder.json");
        chartToSpecMapping.put("basic_radar_chart", "radar-chart-placeholder.json");
        chartToSpecMapping.put("filled_radar_chart", "radar-chart-placeholder.json");
        chartToSpecMapping.put("basic_gauge_chart", "gauge-chart-placeholder.json");
        chartToSpecMapping.put("progress_gauge_chart", "gauge-chart-placeholder.json");
        chartToSpecMapping.put("grade_gauge_chart", "gauge-chart-placeholder.json");

        // 兼容版本
        chartToSpecMapping.put("pie_chart", "pie-chart-placeholder.json");

        String specFile = chartToSpecMapping.get(chartId);
        if (specFile == null) {
            System.out.println("⚠️ 未找到图表类型 " + chartId + " 对应的Jolt规范，使用默认的折线图规范");
            return "line-chart-placeholder.json";
        }

        System.out.println("📋 图表类型 " + chartId + " 使用JOLT规范: " + specFile);
        return specFile;
    }

    /**
     * 获取转换流程的详细信息
     */
    public Map<String, Object> getTransformationInfo(String chartId) {
        Map<String, Object> info = new HashMap<>();

        // 模板信息：优先从模板服务获取；在测试环境或未注入时回退到内置创建
        Map<String, Object> template;
        if (templateService != null) {
            template = templateService.getTemplateByChartId(chartId);
        } else {
            // 如果templateService未注入，返回空模板
            template = new HashMap<>();
        }
        Set<String> templatePlaceholders = placeholderManager.extractPlaceholdersFromJson(template);

        info.put("templatePlaceholders", templatePlaceholders);
        info.put("templatePlaceholderCount", templatePlaceholders.size());

        // 映射关系信息
        Map<String, Object> mappings = mappingService.getChartMappings(chartId);
        info.put("availableMappings", mappings.keySet());
        info.put("mappingCount", mappings.size());

        // 验证信息
        List<String> missingMappings = mappingService.validateMappings(chartId, templatePlaceholders);
        info.put("missingMappings", missingMappings);
        info.put("mappingComplete", missingMappings.isEmpty());

        return info;
    }
}
