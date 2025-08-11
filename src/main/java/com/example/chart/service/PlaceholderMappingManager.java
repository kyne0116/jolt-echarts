package com.example.chart.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chart.model.UniversalChartDataView;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 占位符映射管理器
 * 负责虚拟数据库与转换模块之间的动态映射关系管理
 */
@Service
public class PlaceholderMappingManager {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Autowired
    private SimpleUniversalDataCrudService dataService;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private TemplateService templateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 映射关系存储：chartId -> (placeholder -> fieldMapping)
    private final Map<String, Map<String, FieldMapping>> mappingStore = new ConcurrentHashMap<>();

    /**
     * 初始化预置映射关系
     */
    @PostConstruct
    public void initializePresetMappings() {
        System.out.println("🔧 [映射管理] 开始初始化预置映射关系...");

        // 初始化折线图的映射关系
        initializeBasicLineChartMappings();
        initializeSmoothLineChartMappings();
        initializeStackedLineChartMappings();

        // 初始化柱状图的映射关系
        initializeBasicBarChartMappings();
        initializeStackedBarChartMappings();

        // 初始化饼图的映射关系
        initializeBasicPieChartMappings();
        initializeRingChartMappings();
        initializeNestedPieChartMappings();

        // 初始化雷达图的映射关系
        initializeBasicRadarChartMappings();

        // 初始化仪表盘的映射关系
        initializeBasicGaugeChartMappings();

        // 初始化更多图表类型以达到12条数据
        initializeAreaChartMappings();
        initializeScatterChartMappings();
        initializeHeatmapChartMappings();

        System.out.println("✅ [映射管理] 预置映射关系初始化完成，共配置 " + mappingStore.size() + " 种图表类型");

        // 验证模板类型映射关系
        validateTemplateTypeMappings();
    }

    /**
     * 验证模板类型映射关系是否符合系统设计
     */
    private void validateTemplateTypeMappings() {
        System.out.println("🔍 [映射管理] 开始验证模板类型映射关系...");

        // 测试所有已初始化的图表类型
        for (String chartId : mappingStore.keySet()) {
            String chartType = getChartTypeFromId(chartId);
            String universalTemplate = getUniversalTemplateByCategory(chartType);

            System.out.println("📊 [映射验证] " + chartId +
                    " -> 图表分类: " + chartType +
                    " -> 通用模板: " + universalTemplate);
        }

        // 验证四大模板类型的正确性
        System.out.println("🎯 [映射验证] 四大模板类型验证:");
        System.out.println("   折线图 -> " + getUniversalTemplateByCategory("折线图"));
        System.out.println("   柱状图 -> " + getUniversalTemplateByCategory("柱状图"));
        System.out.println("   饼图 -> " + getUniversalTemplateByCategory("饼图"));
        System.out.println("   雷达图 -> " + getUniversalTemplateByCategory("雷达图"));
        System.out.println("   仪表盘 -> " + getUniversalTemplateByCategory("仪表盘"));

        System.out.println("✅ [映射管理] 模板类型映射关系验证完成");
    }

    /**
     * 字段映射配置
     */
    public static class FieldMapping {
        private String fieldName; // 数据库字段名
        private String dataType; // 数据类型
        private String aggregationType; // 聚合类型：sum, avg, count, max, min
        private Map<String, Object> filters; // 过滤条件
        private String transformExpression; // 转换表达式

        public FieldMapping() {
        }

        public FieldMapping(String fieldName, String dataType) {
            this.fieldName = fieldName;
            this.dataType = dataType;
            this.aggregationType = "none";
            this.filters = new HashMap<>();
        }

        // Getters and Setters
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getAggregationType() {
            return aggregationType;
        }

        public void setAggregationType(String aggregationType) {
            this.aggregationType = aggregationType;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public String getTransformExpression() {
            return transformExpression;
        }

        public void setTransformExpression(String transformExpression) {
            this.transformExpression = transformExpression;
        }
    }

    /**
     * 映射结果
     */
    public static class MappingResult {
        private boolean success;
        private String message;
        private Map<String, Object> data;
        private List<String> unmappedPlaceholders;

        public MappingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.data = new HashMap<>();
            this.unmappedPlaceholders = new ArrayList<>();
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

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public List<String> getUnmappedPlaceholders() {
            return unmappedPlaceholders;
        }

        public void setUnmappedPlaceholders(List<String> unmappedPlaceholders) {
            this.unmappedPlaceholders = unmappedPlaceholders;
        }
    }

    /**
     * 配置图表的占位符映射关系
     */
    public void configureMappings(String chartId, Map<String, FieldMapping> mappings) {
        mappingStore.put(chartId, new HashMap<>(mappings));
        System.out.println("✅ [映射管理] 配置图表 " + chartId + " 的映射关系，共 " + mappings.size() + " 个映射");
    }

    /**
     * 获取图表的映射配置
     */
    public Map<String, FieldMapping> getMappings(String chartId) {
        return mappingStore.getOrDefault(chartId, new HashMap<>());
    }

    /**
     * 根据映射关系动态查询数据并替换占位符
     */
    public MappingResult executeMapping(String chartId, Object templateWithPlaceholders) {
        try {
            // 1. 提取模板中的占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(templateWithPlaceholders);
            System.out.println("🔍 [映射执行] 发现占位符: " + placeholders);

            // 2. 获取映射配置
            Map<String, FieldMapping> mappings = getMappings(chartId);
            if (mappings.isEmpty()) {
                return new MappingResult(false, "图表 " + chartId + " 未配置映射关系");
            }

            // 3. 执行数据查询和映射
            Map<String, Object> mappedData = new HashMap<>();
            List<String> unmappedPlaceholders = new ArrayList<>();

            for (String placeholder : placeholders) {
                FieldMapping mapping = mappings.get(placeholder);

                if (mapping != null) {
                    Object value = queryDataByMapping(mapping);
                    mappedData.put(placeholder, value);
                    System.out.println("✅ [映射执行] " + placeholder + " -> " + value);
                } else {
                    unmappedPlaceholders.add(placeholder);
                    System.out.println("⚠️ [映射执行] 未找到映射: " + placeholder);
                }
            }

            // 4. 替换占位符
            Object result = placeholderManager.replacePlaceholdersInJson(templateWithPlaceholders, mappedData);

            MappingResult mappingResult = new MappingResult(true, "映射执行成功");
            mappingResult.setData(Map.of("result", result, "mappedData", mappedData));
            mappingResult.setUnmappedPlaceholders(unmappedPlaceholders);

            return mappingResult;

        } catch (Exception e) {
            System.err.println("❌ [映射执行] 执行失败: " + e.getMessage());
            return new MappingResult(false, "映射执行失败: " + e.getMessage());
        }
    }

    /**
     * 根据字段映射查询数据
     */
    private Object queryDataByMapping(FieldMapping mapping) {
        try {
            List<UniversalChartDataView> allData = dataService.findAll();

            // 应用过滤条件
            List<UniversalChartDataView> filteredData = applyFilters(allData, mapping.getFilters());

            // 提取字段值
            List<Object> fieldValues = extractFieldValues(filteredData, mapping.getFieldName());

            // 应用聚合
            return applyAggregation(fieldValues, mapping.getAggregationType(), mapping.getDataType());

        } catch (Exception e) {
            System.err.println("❌ [数据查询] 查询失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 应用过滤条件
     */
    private List<UniversalChartDataView> applyFilters(List<UniversalChartDataView> data, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return data;
        }

        return data.stream().filter(item -> {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                Object fieldValue = getFieldValue(item, filter.getKey());
                if (!Objects.equals(fieldValue, filter.getValue())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 提取字段值
     */
    private List<Object> extractFieldValues(List<UniversalChartDataView> data, String fieldName) {
        return data.stream()
                .map(item -> getFieldValue(item, fieldName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取对象字段值（反射方式）
     */
    private Object getFieldValue(UniversalChartDataView item, String fieldName) {
        try {
            switch (fieldName.toLowerCase()) {
                case "id":
                    return item.getId();
                case "year":
                    return item.getYear();
                case "month":
                    return item.getMonth();
                case "date":
                    return item.getDate();
                case "category":
                    return item.getCategory();
                case "channel":
                    return item.getChannel();
                case "product":
                    return item.getProduct();
                case "region":
                    return item.getRegion();
                case "amount":
                    return item.getAmount();
                case "quantity":
                    return item.getQuantity();
                case "percentage":
                    return item.getPercentage();
                case "salesman":
                    return item.getSalesman();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 应用聚合函数
     */
    private Object applyAggregation(List<Object> values, String aggregationType, String dataType) {
        if (values.isEmpty()) {
            return getDefaultValue(dataType);
        }

        switch (aggregationType.toLowerCase()) {
            case "sum":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .sum();

            case "avg":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .average()
                        .orElse(0.0);

            case "count":
                return values.size();

            case "max":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .max()
                        .orElse(0.0);

            case "min":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .min()
                        .orElse(0.0);

            case "list":
                return values;

            case "none":
            default:
                return values.isEmpty() ? getDefaultValue(dataType) : values.get(0);
        }
    }

    /**
     * 获取数据类型的默认值
     */
    private Object getDefaultValue(String dataType) {
        switch (dataType.toLowerCase()) {
            case "number":
                return 0;
            case "string":
                return "";
            case "boolean":
                return false;
            case "array":
                return new ArrayList<>();
            case "object":
                return new HashMap<>();
            default:
                return null;
        }
    }

    /**
     * 生成默认映射配置
     */
    public Map<String, FieldMapping> generateDefaultMappings(String chartId, Set<String> placeholders) {
        Map<String, FieldMapping> defaultMappings = new HashMap<>();

        for (String placeholder : placeholders) {
            String variableName = placeholderManager.getVariableName(placeholder);
            if (variableName != null) {
                FieldMapping mapping = inferMappingFromPlaceholder(variableName);
                defaultMappings.put(placeholder, mapping);
            }
        }

        return defaultMappings;
    }

    /**
     * 根据占位符名称推断映射配置
     */
    private FieldMapping inferMappingFromPlaceholder(String variableName) {
        String lowerName = variableName.toLowerCase();

        if (lowerName.contains("category") || lowerName.contains("categories")) {
            FieldMapping mapping = new FieldMapping("category", "array");
            mapping.setAggregationType("list");
            return mapping;
        } else if (lowerName.contains("amount") || lowerName.contains("value")) {
            return new FieldMapping("amount", "number");
        } else if (lowerName.contains("quantity") || lowerName.contains("count")) {
            return new FieldMapping("quantity", "number");
        } else if (lowerName.contains("region")) {
            return new FieldMapping("region", "string");
        } else if (lowerName.contains("product")) {
            return new FieldMapping("product", "string");
        } else if (lowerName.contains("channel")) {
            return new FieldMapping("channel", "string");
        } else {
            // 默认映射到第一个字符串字段
            return new FieldMapping("category", "string");
        }
    }

    /**
     * 初始化基础折线图的映射关系
     */
    private void initializeBasicLineChartMappings() {
        String chartId = "basic_line_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        titleMapping.getFilters().put("category", "基础折线图演示");
        mappings.put("${chart_title}", titleMapping);

        // X轴分类数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 系列名称映射
        FieldMapping seriesNameMapping = new FieldMapping("product", "string");
        seriesNameMapping.setAggregationType("none");
        seriesNameMapping.getFilters().put("product", "产品A");
        mappings.put("${series_1_name}", seriesNameMapping);

        // 系列数据映射
        FieldMapping seriesDataMapping = new FieldMapping("amount", "array");
        seriesDataMapping.setAggregationType("list");
        seriesDataMapping.getFilters().put("product", "产品A");
        mappings.put("${series_1_data}", seriesDataMapping);

        // 图表类型映射
        FieldMapping chartTypeMapping = new FieldMapping("category", "string");
        chartTypeMapping.setAggregationType("none");
        mappings.put("${chart_type}", chartTypeMapping);

        configureMappings(chartId, mappings);
        System.out.println("✅ [映射管理] 基础折线图映射关系配置完成");
    }

    /**
     * 初始化平滑折线图的映射关系
     */
    private void initializeSmoothLineChartMappings() {
        String chartId = "smooth_line_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        titleMapping.getFilters().put("category", "平滑折线图演示");
        mappings.put("${chart_title}", titleMapping);

        // X轴分类数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 系列名称映射
        FieldMapping seriesNameMapping = new FieldMapping("channel", "string");
        seriesNameMapping.setAggregationType("none");
        seriesNameMapping.getFilters().put("channel", "线上渠道");
        mappings.put("${series_1_name}", seriesNameMapping);

        // 系列数据映射
        FieldMapping seriesDataMapping = new FieldMapping("quantity", "array");
        seriesDataMapping.setAggregationType("list");
        seriesDataMapping.getFilters().put("channel", "线上渠道");
        mappings.put("${series_1_data}", seriesDataMapping);

        // 平滑样式映射
        FieldMapping smoothMapping = new FieldMapping("category", "string");
        smoothMapping.setAggregationType("none");
        mappings.put("${smooth_style}", smoothMapping);

        configureMappings(chartId, mappings);
        System.out.println("✅ [映射管理] 平滑折线图映射关系配置完成");
    }

    /**
     * 初始化堆叠折线图的映射关系
     */
    private void initializeStackedLineChartMappings() {
        String chartId = "stacked_line_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        titleMapping.getFilters().put("category", "营销渠道分析");
        mappings.put("${chart_title}", titleMapping);

        // X轴分类数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 第一个系列：Email营销
        FieldMapping series1NameMapping = new FieldMapping("channel", "string");
        series1NameMapping.setAggregationType("none");
        series1NameMapping.getFilters().put("channel", "Email");
        mappings.put("${series_1_name}", series1NameMapping);

        FieldMapping series1DataMapping = new FieldMapping("amount", "array");
        series1DataMapping.setAggregationType("list");
        series1DataMapping.getFilters().put("channel", "Email");
        mappings.put("${series_1_data}", series1DataMapping);

        // 第二个系列：Social Media
        FieldMapping series2NameMapping = new FieldMapping("channel", "string");
        series2NameMapping.setAggregationType("none");
        series2NameMapping.getFilters().put("channel", "Social Media");
        mappings.put("${series_2_name}", series2NameMapping);

        FieldMapping series2DataMapping = new FieldMapping("amount", "array");
        series2DataMapping.setAggregationType("list");
        series2DataMapping.getFilters().put("channel", "Social Media");
        mappings.put("${series_2_data}", series2DataMapping);

        // 第三个系列：Direct访问
        FieldMapping series3NameMapping = new FieldMapping("channel", "string");
        series3NameMapping.setAggregationType("none");
        series3NameMapping.getFilters().put("channel", "Direct");
        mappings.put("${series_3_name}", series3NameMapping);

        FieldMapping series3DataMapping = new FieldMapping("amount", "array");
        series3DataMapping.setAggregationType("list");
        series3DataMapping.getFilters().put("channel", "Direct");
        mappings.put("${series_3_data}", series3DataMapping);

        // 堆叠组映射
        FieldMapping stackGroupMapping = new FieldMapping("category", "string");
        stackGroupMapping.setAggregationType("none");
        stackGroupMapping.getFilters().put("category", "营销渠道");
        mappings.put("${stack_group}", stackGroupMapping);

        configureMappings(chartId, mappings);
        System.out.println("✅ [映射管理] 堆叠折线图映射关系配置完成");
    }

    /**
     * 获取所有映射配置列表
     */
    public List<Map<String, Object>> getAllMappingConfigurations() {
        System.out.println("📋 [映射管理] 开始获取所有映射配置列表...");
        System.out.println("📊 [映射管理] 当前映射存储中的图表数量: " + mappingStore.size());

        List<Map<String, Object>> configList = new ArrayList<>();
        int instanceId = 1; // 自增实例ID

        for (Map.Entry<String, Map<String, FieldMapping>> entry : mappingStore.entrySet()) {
            String chartId = entry.getKey();
            Map<String, FieldMapping> mappings = entry.getValue();

            System.out.println("🔍 [映射管理] 处理图表: " + chartId + ", 映射数量: " + mappings.size());

            String chartType = getChartTypeFromId(chartId);
            String chartName = getChartNameFromId(chartId);
            // 使用基于图表分类的模板类型获取方法
            String universalTemplate = getUniversalTemplateByCategory(chartType);
            String joltSpecFile = getJoltSpecFileFromId(chartId);
            int placeholderCount = getPlaceholderCountForChart(chartId);

            System.out.println("📝 [映射管理] 图表详情 - ID: " + chartId +
                    ", 类型: " + chartType +
                    ", 名称: " + chartName +
                    ", 模板: " + universalTemplate +
                    ", 规范: " + joltSpecFile +
                    ", 占位符数: " + placeholderCount);

            // 生成实例名称
            String instanceName = generateInstanceName(chartType, chartName);

            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("instanceId", instanceId); // 使用自增数字作为实例ID
            configInfo.put("chartId", chartId); // 保留原始chartId作为图表ID
            configInfo.put("instanceName", instanceName);
            configInfo.put("chartType", chartType);
            configInfo.put("chartName", chartName);
            configInfo.put("universalTemplate", universalTemplate);
            configInfo.put("joltSpecFile", joltSpecFile);
            configInfo.put("mappingCount", mappings.size());
            configInfo.put("placeholderCount", placeholderCount);
            configInfo.put("createdTime", new java.util.Date());
            configInfo.put("hasConfig", !mappings.isEmpty());

            // 添加映射详情
            List<Map<String, Object>> mappingDetails = new ArrayList<>();
            for (Map.Entry<String, FieldMapping> mappingEntry : mappings.entrySet()) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("placeholder", mappingEntry.getKey());
                detail.put("fieldName", mappingEntry.getValue().getFieldName());
                detail.put("dataType", mappingEntry.getValue().getDataType());
                detail.put("aggregationType", mappingEntry.getValue().getAggregationType());
                mappingDetails.add(detail);
            }
            configInfo.put("mappingDetails", mappingDetails);

            configList.add(configInfo);
            instanceId++; // 递增实例ID
        }

        System.out.println("✅ [映射管理] 映射配置列表生成完成，共 " + configList.size() + " 条记录");
        return configList;
    }

    /**
     * 生成实例名称
     * 根据图表类型和图表名称生成易于理解的业务描述名称
     */
    private String generateInstanceName(String chartType, String chartName) {
        // 预定义的前三条数据样例
        String[] predefinedNames = {
                "2025年销售业绩排行",
                "2024年销售渠道分布",
                "2025年销售产品占比"
        };

        // 根据图表类型确定使用哪个预定义名称
        int index = getInstanceIndex(chartType);
        if (index < predefinedNames.length) {
            return predefinedNames[index];
        }

        // 如果超出预定义范围，使用原有逻辑
        int currentYear = java.time.LocalDate.now().getYear();
        switch (chartType) {
            case "折线图":
                return currentYear + "年" + getBusinessContext(chartName) + "趋势分析";
            case "柱状图":
                return currentYear + "年" + getBusinessContext(chartName) + "对比分析";
            case "饼图":
                return currentYear + "年" + getBusinessContext(chartName) + "占比分析";
            case "雷达图":
                return currentYear + "年" + getBusinessContext(chartName) + "综合评估";
            case "仪表盘":
                return currentYear + "年" + getBusinessContext(chartName) + "指标监控";
            default:
                return currentYear + "年" + getBusinessContext(chartName) + "数据分析";
        }
    }

    /**
     * 根据图表类型获取实例索引，用于确定使用哪个预定义名称
     */
    private int getInstanceIndex(String chartType) {
        switch (chartType) {
            case "折线图":
                return 0; // 2025年销售业绩排行
            case "柱状图":
                return 1; // 2024年销售渠道分布
            case "饼图":
                return 2; // 2025年销售产品占比
            default:
                return 3; // 超出预定义范围
        }
    }

    /**
     * 根据图表名称提取业务上下文
     */
    private String getBusinessContext(String chartName) {
        if (chartName == null || chartName.isEmpty()) {
            return "业务数据";
        }

        // 移除常见的图表类型后缀
        String context = chartName
                .replace("基础", "")
                .replace("平滑", "")
                .replace("堆叠", "")
                .replace("富文本标签", "")
                .replace("圆角环形", "")
                .replace("进度", "")
                .replace("等级", "")
                .replace("折线图", "")
                .replace("柱状图", "")
                .replace("饼图", "")
                .replace("雷达图", "")
                .replace("仪表盘", "")
                .trim();

        // 如果处理后为空，使用默认值
        if (context.isEmpty()) {
            return "销售业绩";
        }

        return context;
    }

    /**
     * 复制映射配置到其他图表类型
     */
    public boolean copyMappings(String sourceChartId, String targetChartId) {
        Map<String, FieldMapping> sourceMappings = mappingStore.get(sourceChartId);

        if (sourceMappings == null || sourceMappings.isEmpty()) {
            return false;
        }

        // 深拷贝映射配置
        Map<String, FieldMapping> targetMappings = new HashMap<>();
        for (Map.Entry<String, FieldMapping> entry : sourceMappings.entrySet()) {
            FieldMapping sourceMapping = entry.getValue();
            FieldMapping targetMapping = new FieldMapping(sourceMapping.getFieldName(), sourceMapping.getDataType());
            targetMapping.setAggregationType(sourceMapping.getAggregationType());
            targetMapping.setFilters(new HashMap<>(sourceMapping.getFilters()));
            targetMapping.setTransformExpression(sourceMapping.getTransformExpression());

            targetMappings.put(entry.getKey(), targetMapping);
        }

        configureMappings(targetChartId, targetMappings);
        System.out.println("✅ [映射管理] 成功复制映射配置: " + sourceChartId + " -> " + targetChartId);
        return true;
    }

    /**
     * 批量删除映射配置
     */
    public int batchDeleteMappings(List<String> chartIds) {
        int deletedCount = 0;

        for (String chartId : chartIds) {
            if (mappingStore.containsKey(chartId)) {
                mappingStore.remove(chartId);
                deletedCount++;
                System.out.println("✅ [映射管理] 删除映射配置: " + chartId);
            }
        }

        System.out.println("✅ [映射管理] 批量删除完成，共删除 " + deletedCount + " 个配置");
        return deletedCount;
    }

    /**
     * 根据图表ID获取图表类型
     */
    private String getChartTypeFromId(String chartId) {
        if (chartId.contains("line")) {
            return "折线图";
        } else if (chartId.contains("bar")) {
            return "柱状图";
        } else if (chartId.contains("pie")) {
            return "饼图";
        } else if (chartId.contains("radar")) {
            return "雷达图";
        } else if (chartId.contains("gauge")) {
            return "仪表盘";
        } else if (chartId.contains("area")) {
            return "面积图";
        } else if (chartId.contains("scatter")) {
            return "散点图";
        } else if (chartId.contains("heatmap")) {
            return "热力图";
        }
        return "未知类型";
    }

    /**
     * 根据图表ID获取图表名称
     */
    private String getChartNameFromId(String chartId) {
        Map<String, String> chartNames = new HashMap<>();
        chartNames.put("basic_line_chart", "基础折线图");
        chartNames.put("smooth_line_chart", "平滑折线图");
        chartNames.put("stacked_line_chart", "堆叠折线图");
        chartNames.put("basic_bar_chart", "基础柱状图");
        chartNames.put("stacked_bar_chart", "堆叠柱状图");
        chartNames.put("basic_pie_chart", "基础饼图");
        chartNames.put("ring_chart", "环形图");
        chartNames.put("nested_pie_chart", "嵌套饼图");
        chartNames.put("basic_radar_chart", "基础雷达图");
        chartNames.put("basic_gauge_chart", "基础仪表盘");
        chartNames.put("basic_area_chart", "基础面积图");
        chartNames.put("basic_scatter_chart", "基础散点图");
        chartNames.put("basic_heatmap_chart", "基础热力图");

        return chartNames.getOrDefault(chartId, "未知图表");
    }

    /**
     * 根据图表ID获取通用模板类型 - 基于系统设计的四大模板类型
     */
    private String getUniversalTemplateFromId(String chartId) {
        // 系统设计的四大模板类型
        final String CARTESIAN = "CARTESIAN（直角坐标系）";
        final String PIE = "PIE（饼图类）";
        final String RADAR = "RADAR（雷达图类）";
        final String GAUGE = "GAUGE（仪表盘类）";

        // 根据图表ID确定所属的模板类型
        if (chartId.contains("line") || chartId.contains("bar")) {
            // 折线图和柱状图都属于直角坐标系
            return CARTESIAN;
        } else if (chartId.contains("pie") || chartId.contains("doughnut")) {
            // 饼图和环形图属于饼图类
            return PIE;
        } else if (chartId.contains("radar")) {
            // 雷达图属于雷达图类
            return RADAR;
        } else if (chartId.contains("gauge")) {
            // 仪表盘属于仪表盘类
            return GAUGE;
        }

        return "UNKNOWN（未知类型）";
    }

    /**
     * 根据图表分类获取通用模板类型 - 推荐使用此方法
     * 基于一级下拉框的图表分类来确定模板类型，符合系统设计
     */
    public String getUniversalTemplateByCategory(String chartCategory) {
        // 系统设计的四大模板类型
        final String CARTESIAN = "CARTESIAN（直角坐标系）";
        final String PIE = "PIE（饼图类）";
        final String RADAR = "RADAR（雷达图类）";
        final String GAUGE = "GAUGE（仪表盘类）";

        if (chartCategory == null) {
            return "UNKNOWN（未知类型）";
        }

        switch (chartCategory) {
            case "折线图":
            case "柱状图":
            case "面积图":
            case "散点图":
                return CARTESIAN;
            case "饼图":
                return PIE;
            case "雷达图":
                return RADAR;
            case "仪表盘":
                return GAUGE;
            case "热力图":
                return "HEATMAP（热力图类）";
            default:
                return "UNKNOWN（未知类型）";
        }
    }

    /**
     * 根据图表ID获取模板名称
     */
    private String getTemplateNameFromId(String chartId) {
        Map<String, String> templateNames = new HashMap<>();
        templateNames.put("basic_line_chart", "基础折线图模板");
        templateNames.put("smooth_line_chart", "平滑折线图模板");
        templateNames.put("stacked_line_chart", "堆叠折线图模板");
        templateNames.put("basic_bar_chart", "基础柱状图模板");
        templateNames.put("stacked_bar_chart", "堆叠柱状图模板");
        templateNames.put("basic_pie_chart", "基础饼图模板");
        templateNames.put("ring_chart", "环形图模板");
        templateNames.put("nested_pie_chart", "嵌套饼图模板");
        templateNames.put("basic_radar_chart", "基础雷达图模板");
        templateNames.put("basic_gauge_chart", "基础仪表盘模板");

        return templateNames.getOrDefault(chartId, "通用模板");
    }

    /**
     * 根据图表ID获取JOLT规范文件名
     */
    private String getJoltSpecFileFromId(String chartId) {
        Map<String, String> joltSpecs = new HashMap<>();
        joltSpecs.put("basic_line_chart", "line-chart-placeholder.json");
        joltSpecs.put("smooth_line_chart", "line-chart-placeholder.json");
        joltSpecs.put("stacked_line_chart", "line-chart-stacked.json");
        joltSpecs.put("basic_bar_chart", "bar-chart-placeholder.json");
        joltSpecs.put("stacked_bar_chart", "bar-chart-placeholder.json");
        joltSpecs.put("basic_pie_chart", "pie-chart-placeholder.json");
        joltSpecs.put("ring_chart", "pie-chart-placeholder.json");
        joltSpecs.put("nested_pie_chart", "pie-chart-placeholder.json");
        joltSpecs.put("basic_radar_chart", "radar-chart-placeholder.json");
        joltSpecs.put("basic_gauge_chart", "gauge-chart-placeholder.json");

        return joltSpecs.getOrDefault(chartId, "default-placeholder.json");
    }

    /**
     * 根据图表ID获取占位符数量
     */
    private int getPlaceholderCountForChart(String chartId) {
        try {
            // 从实际的模板中提取占位符数量
            Map<String, Object> template = templateService.getCategoryTemplateByChartId(chartId);
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(template);
            int actualCount = placeholders.size();

            System.out.println("📊 [占位符计数] 图表 " + chartId + " 实际占位符数量: " + actualCount);
            System.out.println("📊 [占位符计数] 占位符列表: " + placeholders);

            return actualCount;
        } catch (Exception e) {
            System.err.println("❌ [占位符计数] 获取图表 " + chartId + " 占位符数量失败: " + e.getMessage());
            // 回退到默认值
            return getDefaultPlaceholderCount(chartId);
        }
    }

    /**
     * 获取默认占位符数量（回退方案）
     */
    private int getDefaultPlaceholderCount(String chartId) {
        Map<String, Integer> placeholderCounts = new HashMap<>();
        placeholderCounts.put("basic_line_chart", 4);
        placeholderCounts.put("smooth_line_chart", 4);
        placeholderCounts.put("stacked_line_chart", 5);
        placeholderCounts.put("basic_bar_chart", 4);
        placeholderCounts.put("stacked_bar_chart", 5);
        placeholderCounts.put("basic_pie_chart", 2);
        placeholderCounts.put("ring_chart", 2);
        placeholderCounts.put("nested_pie_chart", 3);
        placeholderCounts.put("basic_radar_chart", 3);
        placeholderCounts.put("basic_gauge_chart", 3);
        placeholderCounts.put("basic_area_chart", 4);
        placeholderCounts.put("basic_scatter_chart", 3);
        placeholderCounts.put("basic_heatmap_chart", 4);

        return placeholderCounts.getOrDefault(chartId, 3);
    }

    /**
     * 初始化基础柱状图的映射关系
     */
    private void initializeBasicBarChartMappings() {
        String chartId = "basic_bar_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 类别数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 系列数据映射
        FieldMapping seriesDataMapping = new FieldMapping("amount", "array");
        seriesDataMapping.setAggregationType("list");
        mappings.put("${series_1_data}", seriesDataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化基础柱状图映射关系完成");
    }

    /**
     * 初始化堆叠柱状图的映射关系
     */
    private void initializeStackedBarChartMappings() {
        String chartId = "stacked_bar_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 类别数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 系列1数据映射
        FieldMapping series1DataMapping = new FieldMapping("amount", "array");
        series1DataMapping.setAggregationType("list");
        mappings.put("${series_1_data}", series1DataMapping);

        // 系列2数据映射
        FieldMapping series2DataMapping = new FieldMapping("quantity", "array");
        series2DataMapping.setAggregationType("list");
        mappings.put("${series_2_data}", series2DataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化堆叠柱状图映射关系完成");
    }

    /**
     * 初始化基础饼图的映射关系
     */
    private void initializeBasicPieChartMappings() {
        String chartId = "basic_pie_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 饼图数据映射
        FieldMapping dataMapping = new FieldMapping("channel", "array");
        dataMapping.setAggregationType("list");
        mappings.put("${pie_data}", dataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化基础饼图映射关系完成");
    }

    /**
     * 初始化环形图的映射关系
     */
    private void initializeRingChartMappings() {
        String chartId = "ring_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 环形图数据映射
        FieldMapping dataMapping = new FieldMapping("product", "array");
        dataMapping.setAggregationType("list");
        mappings.put("${ring_data}", dataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化环形图映射关系完成");
    }

    /**
     * 初始化嵌套饼图的映射关系
     */
    private void initializeNestedPieChartMappings() {
        String chartId = "nested_pie_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 外层数据映射
        FieldMapping outerDataMapping = new FieldMapping("product", "array");
        outerDataMapping.setAggregationType("list");
        mappings.put("${outer_data}", outerDataMapping);

        // 内层数据映射
        FieldMapping innerDataMapping = new FieldMapping("region", "array");
        innerDataMapping.setAggregationType("list");
        mappings.put("${inner_data}", innerDataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化嵌套饼图映射关系完成");
    }

    /**
     * 初始化基础雷达图的映射关系
     */
    private void initializeBasicRadarChartMappings() {
        String chartId = "basic_radar_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 雷达图指标映射
        FieldMapping indicatorMapping = new FieldMapping("region", "array");
        indicatorMapping.setAggregationType("list");
        mappings.put("${radar_indicators}", indicatorMapping);

        // 雷达图数据映射
        FieldMapping dataMapping = new FieldMapping("percentage", "array");
        dataMapping.setAggregationType("list");
        mappings.put("${radar_data}", dataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化基础雷达图映射关系完成");
    }

    /**
     * 初始化基础仪表盘的映射关系
     */
    private void initializeBasicGaugeChartMappings() {
        String chartId = "basic_gauge_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 仪表盘数值映射
        FieldMapping valueMapping = new FieldMapping("percentage", "number");
        valueMapping.setAggregationType("avg");
        mappings.put("${gauge_value}", valueMapping);

        // 仪表盘最大值映射
        FieldMapping maxMapping = new FieldMapping("quantity", "number");
        maxMapping.setAggregationType("max");
        mappings.put("${gauge_max}", maxMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化基础仪表盘映射关系完成");
    }

    /**
     * 初始化面积图的映射关系
     */
    private void initializeAreaChartMappings() {
        String chartId = "basic_area_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // 类别数据映射
        FieldMapping categoriesMapping = new FieldMapping("month", "array");
        categoriesMapping.setAggregationType("list");
        mappings.put("${categories}", categoriesMapping);

        // 系列数据映射
        FieldMapping seriesDataMapping = new FieldMapping("amount", "array");
        seriesDataMapping.setAggregationType("list");
        mappings.put("${series_1_data}", seriesDataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化面积图映射关系完成");
    }

    /**
     * 初始化散点图的映射关系
     */
    private void initializeScatterChartMappings() {
        String chartId = "basic_scatter_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // X轴数据映射
        FieldMapping xDataMapping = new FieldMapping("amount", "array");
        xDataMapping.setAggregationType("list");
        mappings.put("${x_data}", xDataMapping);

        // Y轴数据映射
        FieldMapping yDataMapping = new FieldMapping("quantity", "array");
        yDataMapping.setAggregationType("list");
        mappings.put("${y_data}", yDataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化散点图映射关系完成");
    }

    /**
     * 初始化热力图的映射关系
     */
    private void initializeHeatmapChartMappings() {
        String chartId = "basic_heatmap_chart";
        Map<String, FieldMapping> mappings = new HashMap<>();

        // 图表标题映射
        FieldMapping titleMapping = new FieldMapping("category", "string");
        titleMapping.setAggregationType("none");
        mappings.put("${chart_title}", titleMapping);

        // X轴类别映射
        FieldMapping xAxisMapping = new FieldMapping("month", "array");
        xAxisMapping.setAggregationType("list");
        mappings.put("${x_axis_data}", xAxisMapping);

        // Y轴类别映射
        FieldMapping yAxisMapping = new FieldMapping("channel", "array");
        yAxisMapping.setAggregationType("list");
        mappings.put("${y_axis_data}", yAxisMapping);

        // 热力图数据映射
        FieldMapping heatDataMapping = new FieldMapping("amount", "array");
        heatDataMapping.setAggregationType("list");
        mappings.put("${heat_data}", heatDataMapping);

        mappingStore.put(chartId, mappings);
        System.out.println("✅ [映射管理] 初始化热力图映射关系完成");
    }
}
