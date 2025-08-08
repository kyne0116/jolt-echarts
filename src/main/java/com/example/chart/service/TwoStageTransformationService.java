package com.example.chart.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private MappingRelationshipService mappingService;

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
            System.out.println("=== 第一阶段转换开始 ===");
            System.out.println("图表类型: " + chartId);

            // 提取模板中的占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(universalTemplate);
            System.out.println("发现占位符: " + placeholders);

            // 使用Jolt进行结构转换（根据图表类型选择规范）
            Map<String, Object> structuralResult = transformStructureWithJolt(chartId, universalTemplate);

            // 验证转换后占位符是否保持
            Set<String> afterPlaceholders = placeholderManager.extractPlaceholdersFromJson(structuralResult);
            System.out.println("转换后占位符: " + afterPlaceholders);

            TransformationResult result = new TransformationResult(true, "第一阶段转换成功", structuralResult);
            result.setPlaceholders(afterPlaceholders);
            result.setUsedJoltSpec(getJoltSpecFileByChartId(chartId));

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
            // 优先使用新的映射服务和注册表
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

            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(echartsTemplate);
            System.out.println("需要替换的占位符: " + placeholders);

            // 验证映射关系
            List<String> missingMappings = mappingService.validateMappings(chartId, placeholders);
            if (!missingMappings.isEmpty()) {
                return new TransformationResult(false, "缺少映射关系: " + missingMappings, null);
            }

            // 模拟数据库查询
            Map<String, Object> queryResults = mappingService.simulateDataQuery(chartId, placeholders);
            System.out.println("查询结果: " + queryResults.keySet());

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
     * 根据图表类型ID获取对应的Jolt规范文件名
     */
    private String getJoltSpecFileByChartId(String chartId) {
        Map<String, String> chartToSpecMapping = new HashMap<>();
        chartToSpecMapping.put("stacked_line_chart", "line-chart-placeholder.json");
        chartToSpecMapping.put("basic_bar_chart", "bar-chart-placeholder.json");
        chartToSpecMapping.put("pie_chart", "pie-chart-placeholder.json");

        String specFile = chartToSpecMapping.get(chartId);
        if (specFile == null) {
            System.out.println("⚠️ 未找到图表类型 " + chartId + " 对应的Jolt规范，使用默认的折线图规范");
            return "line-chart-placeholder.json";
        }

        return specFile;
    }

    /**
     * 创建带占位符的通用JSON模板
     */
    public Map<String, Object> createUniversalTemplateWithPlaceholders() {
        Map<String, Object> template = new HashMap<>();

        // 图表元数据
        Map<String, Object> chartMeta = new HashMap<>();
        chartMeta.put("chartId", "stacked_line_chart");
        chartMeta.put("chartType", "${chart_type}");
        chartMeta.put("title", "${chart_title}");
        chartMeta.put("dataSource", "marketing_db");
        template.put("chartMeta", chartMeta);

        // 类别数据
        template.put("categories", "${category_field}");

        // 系列数据
        List<Map<String, Object>> series = new ArrayList<>();

        // 创建5个系列的模板
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("seriesId", "series_" + i);
            seriesItem.put("seriesName", "${series_name_" + i + "}");
            seriesItem.put("seriesType", "${chart_type}");
            seriesItem.put("stackGroup", "${stack_group}");
            seriesItem.put("values", "${series_data_" + i + "}");
            series.add(seriesItem);
        }

        template.put("series", series);

        // 样式配置
        Map<String, Object> styleConfig = new HashMap<>();
        styleConfig.put("showLegend", true);
        styleConfig.put("showTooltip", true);
        styleConfig.put("showGrid", true);
        template.put("styleConfig", styleConfig);

        return template;
    }

    /**
     * 验证完整转换流程
     */
    public TransformationResult validateFullProcess(String chartId) {
        try {
            System.out.println("=== 验证完整转换流程 ===");

            // 初始化映射关系
            mappingService.initializeSampleMappings();

            // 创建带占位符的模板
            Map<String, Object> template = createUniversalTemplateWithPlaceholders();
            System.out.println("创建模板完成，包含占位符: " +
                    placeholderManager.extractPlaceholdersFromJson(template));

            // 执行完整转换
            TransformationResult result = executeFullTransformation(chartId, template);

            if (result.isSuccess()) {
                // 验证最终结果
                Set<String> remainingPlaceholders = placeholderManager.extractPlaceholdersFromJson(result.getResult());
                if (remainingPlaceholders.isEmpty()) {
                    result.setMessage("✅ 完整转换验证成功，所有占位符已替换");
                } else {
                    result.setMessage("⚠️ 转换完成但仍有未替换的占位符: " + remainingPlaceholders);
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("验证过程失败: " + e.getMessage());
            return new TransformationResult(false, "验证失败: " + e.getMessage(), null);
        }
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
            // 兼容旧测试用例
            template = createUniversalTemplateWithPlaceholders();
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
