package com.example.chart.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chart.model.UniversalChartDataView;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 映射关系管理服务
 * 负责占位符与统一数据视图字段的映射关系管理
 * 重构后使用单一数据源 UniversalChartDataView
 */
@Service
public class MappingRelationshipService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 统一数据视图映射关系存储
    private final Map<String, Map<String, Object>> chartMappings = new HashMap<>();

    // 字段提取器映射 - 用于从UniversalChartDataView中提取数据
    private final Map<String, Function<UniversalChartDataView, Object>> fieldExtractors = new HashMap<>();

    @Autowired
    private UniversalChartDataService dataService;

    // 初始化状态管理
    private volatile boolean initialized = false;
    private final Object initLock = new Object();

    public MappingRelationshipService() {
        // 构造函数中不执行初始化，等待@PostConstruct
    }

    @PostConstruct
    public void initialize() {
        synchronized (initLock) {
            if (!initialized) {
                try {
                    initializeFieldExtractors();
                    initializeUniversalMappings();
                    initialized = true;
                    System.out.println("✅ 映射关系服务初始化完成，支持图表类型数量: " + chartMappings.size());
                } catch (Exception e) {
                    System.err.println("❌ 映射关系服务初始化失败: " + e.getMessage());
                    throw new RuntimeException("映射关系服务初始化失败", e);
                }
            }
        }
    }

    /**
     * 初始化字段提取器
     */
    private void initializeFieldExtractors() {
        // 基础信息字段提取器
        fieldExtractors.put("title", UniversalChartDataView::getTitle);
        fieldExtractors.put("chart_type", UniversalChartDataView::getChartType);
        fieldExtractors.put("theme", UniversalChartDataView::getTheme);
        fieldExtractors.put("description", UniversalChartDataView::getDescription);

        // 时间维度字段提取器
        fieldExtractors.put("date", data -> data.getDate() != null ? data.getDate().toString() : null);
        fieldExtractors.put("day_name", UniversalChartDataView::getDayName);
        fieldExtractors.put("month", UniversalChartDataView::getMonth);
        fieldExtractors.put("year", UniversalChartDataView::getYear);

        // 分类数据字段提取器
        fieldExtractors.put("category", UniversalChartDataView::getCategory);
        fieldExtractors.put("channel_name", UniversalChartDataView::getChannelName);
        fieldExtractors.put("product_name", UniversalChartDataView::getProductName);
        fieldExtractors.put("region", UniversalChartDataView::getRegion);

        // 数值字段提取器
        fieldExtractors.put("value", UniversalChartDataView::getValue);
        fieldExtractors.put("conversion_count", UniversalChartDataView::getConversionCount);
        fieldExtractors.put("click_count", UniversalChartDataView::getClickCount);
        fieldExtractors.put("percentage", UniversalChartDataView::getPercentage);
        fieldExtractors.put("amount", UniversalChartDataView::getAmount);

        // 配置字段提取器
        fieldExtractors.put("color", UniversalChartDataView::getColor);
        fieldExtractors.put("style", UniversalChartDataView::getStyle);
        fieldExtractors.put("radius", UniversalChartDataView::getRadius);
        fieldExtractors.put("stack_group", UniversalChartDataView::getStackGroup);
        fieldExtractors.put("smooth_style", UniversalChartDataView::getSmoothStyle);
        fieldExtractors.put("boundary_gap", UniversalChartDataView::getBoundaryGap);
    }

    /**
     * 创建统一字段映射
     */
    private Map<String, Object> createUniversalFieldMapping(String placeholderName, String viewName,
            String fieldName, String dataType) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("placeholderName", placeholderName);
        mapping.put("viewName", viewName);
        mapping.put("fieldName", fieldName);
        mapping.put("dataType", dataType);
        mapping.put("extractorKey", fieldName);
        return mapping;
    }

    /**
     * 映射关系数据结构
     */
    public static class FieldMapping {
        private String placeholderName;
        private String tableName;
        private String columnName;
        private String dataType;
        private String aggregationType;
        private Map<String, Object> queryConditions;

        // 构造函数
        public FieldMapping() {
        }

        public FieldMapping(String placeholderName, String tableName, String columnName, String dataType) {
            this.placeholderName = placeholderName;
            this.tableName = tableName;
            this.columnName = columnName;
            this.dataType = dataType;
        }

        // Getters and Setters
        public String getPlaceholderName() {
            return placeholderName;
        }

        public void setPlaceholderName(String placeholderName) {
            this.placeholderName = placeholderName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
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

        public Map<String, Object> getQueryConditions() {
            return queryConditions;
        }

        public void setQueryConditions(Map<String, Object> queryConditions) {
            this.queryConditions = queryConditions;
        }
    }

    /**
     * 初始化示例映射关系 - 保持向后兼容
     */
    public void initializeSampleMappings() {
        initializeUniversalMappings();
    }

    /**
     * 初始化统一数据视图映射关系
     */
    public void initializeUniversalMappings() {
        // 初始化字段提取器
        initializeFieldExtractors();

        // 通用模板的映射关系（基于UniversalChartDataView）
        Map<String, Object> universalMappings = new HashMap<>();

        // 图表基本信息映射
        universalMappings.put("${chart_title}", createUniversalFieldMapping(
                "${chart_title}", "universal_chart_data_view", "title", "string"));
        universalMappings.put("${chart_type}", createUniversalFieldMapping(
                "${chart_type}", "universal_chart_data_view", "chart_type", "string"));
        universalMappings.put("${chart_theme}", createUniversalFieldMapping(
                "${chart_theme}", "universal_chart_data_view", "theme", "string"));

        // 数据结构映射
        universalMappings.put("${categories}", createUniversalFieldMapping(
                "${categories}", "universal_chart_data_view", "day_name", "array"));

        // 系列名称映射（基于统一数据视图）
        universalMappings.put("${series_1_name}", createUniversalFieldMapping(
                "${series_1_name}", "universal_chart_data_view", "channel_name", "string"));
        universalMappings.put("${series_2_name}", createUniversalFieldMapping(
                "${series_2_name}", "universal_chart_data_view", "channel_name", "string"));
        universalMappings.put("${series_3_name}", createUniversalFieldMapping(
                "${series_3_name}", "universal_chart_data_view", "channel_name", "string"));
        universalMappings.put("${series_4_name}", createUniversalFieldMapping(
                "${series_4_name}", "universal_chart_data_view", "channel_name", "string"));
        universalMappings.put("${series_5_name}", createUniversalFieldMapping(
                "${series_5_name}", "universal_chart_data_view", "channel_name", "string"));

        // 系列数据映射（基于统一数据视图）
        universalMappings.put("${series_1_data}", createUniversalFieldMapping(
                "${series_1_data}", "universal_chart_data_view", "conversion_count", "array"));
        universalMappings.put("${series_2_data}", createUniversalFieldMapping(
                "${series_2_data}", "universal_chart_data_view", "conversion_count", "array"));
        universalMappings.put("${series_3_data}", createUniversalFieldMapping(
                "${series_3_data}", "universal_chart_data_view", "conversion_count", "array"));
        universalMappings.put("${series_4_data}", createUniversalFieldMapping(
                "${series_4_data}", "universal_chart_data_view", "conversion_count", "array"));
        universalMappings.put("${series_5_data}", createUniversalFieldMapping(
                "${series_5_data}", "universal_chart_data_view", "conversion_count", "array"));

        // 系列样式映射
        universalMappings.put("${series_1_style}", createUniversalFieldMapping(
                "${series_1_style}", "universal_chart_data_view", "style", "string"));
        universalMappings.put("${series_2_style}", createUniversalFieldMapping(
                "${series_2_style}", "universal_chart_data_view", "style", "string"));
        universalMappings.put("${series_3_style}", createUniversalFieldMapping(
                "${series_3_style}", "universal_chart_data_view", "style", "string"));
        universalMappings.put("${series_4_style}", createUniversalFieldMapping(
                "${series_4_style}", "universal_chart_data_view", "style", "string"));
        universalMappings.put("${series_5_style}", createUniversalFieldMapping(
                "${series_5_style}", "universal_chart_data_view", "style", "string"));

        // 分类模板专用占位符映射
        // 坐标轴配置映射
        universalMappings.put("${boundary_gap}", createUniversalFieldMapping(
                "${boundary_gap}", "universal_chart_data_view", "boundary_gap", "boolean"));

        // 系列配置映射
        universalMappings.put("${series_type}", createUniversalFieldMapping(
                "${series_type}", "universal_chart_data_view", "chart_type", "string"));
        universalMappings.put("${stack_group}", createUniversalFieldMapping(
                "${stack_group}", "universal_chart_data_view", "stack_group", "string"));
        universalMappings.put("${smooth_style}", createUniversalFieldMapping(
                "${smooth_style}", "universal_chart_data_view", "smooth_style", "boolean"));

        // 工具箱配置映射
        universalMappings.put("${toolbox_config}", createUniversalFieldMapping(
                "${toolbox_config}", "universal_chart_data_view", "extra_config", "string"));

        // 饼图专用占位符映射
        universalMappings.put("${radius_config}", createUniversalFieldMapping(
                "${radius_config}", "universal_chart_data_view", "radius", "string"));
        universalMappings.put("${center_config}", createUniversalFieldMapping(
                "${center_config}", "universal_chart_data_view", "center", "array"));
        universalMappings.put("${pie_data}", createUniversalFieldMapping(
                "${pie_data}", "universal_chart_data_view", "value", "array"));
        universalMappings.put("${rose_type}", createUniversalFieldMapping(
                "${rose_type}", "universal_chart_data_view", "extra_config", "string"));

        // 雷达图专用占位符映射
        universalMappings.put("${radar_config}", createFieldMapping(
                "${radar_config}", "chart_config", "radar_config", "object"));
        universalMappings.put("${radar_data}", createFieldMapping(
                "${radar_data}", "marketing_data", "radar_data", "array"));

        // 仪表盘专用占位符映射
        universalMappings.put("${gauge_min}", createFieldMapping(
                "${gauge_min}", "chart_config", "gauge_min", "number", "0"));
        universalMappings.put("${gauge_max}", createFieldMapping(
                "${gauge_max}", "chart_config", "gauge_max", "number", "100"));
        universalMappings.put("${gauge_data}", createFieldMapping(
                "${gauge_data}", "marketing_data", "gauge_data", "array"));
        universalMappings.put("${gauge_detail}", createFieldMapping(
                "${gauge_detail}", "chart_config", "gauge_detail", "object"));
        universalMappings.put("${gauge_pointer}", createFieldMapping(
                "${gauge_pointer}", "chart_config", "gauge_pointer", "object"));

        // 布局配置映射
        universalMappings.put("${legend_config}", createFieldMapping(
                "${legend_config}", "chart_config", "legend_config", "string"));
        universalMappings.put("${axis_config}", createFieldMapping(
                "${axis_config}", "chart_config", "axis_config", "string"));
        universalMappings.put("${grid_config}", createFieldMapping(
                "${grid_config}", "chart_config", "grid_config", "string"));

        // 交互配置映射
        universalMappings.put("${tooltip_config}", createFieldMapping(
                "${tooltip_config}", "chart_config", "tooltip_config", "string"));
        universalMappings.put("${zoom_config}", createFieldMapping(
                "${zoom_config}", "chart_config", "zoom_config", "string"));
        universalMappings.put("${brush_config}", createFieldMapping(
                "${brush_config}", "chart_config", "brush_config", "string"));

        // 为所有图表类型使用相同的通用映射
        // CARTESIAN类型图表
        chartMappings.put("basic_line_chart", universalMappings);
        chartMappings.put("smooth_line_chart", universalMappings);
        chartMappings.put("stacked_line_chart", universalMappings);
        chartMappings.put("basic_bar_chart", universalMappings);
        chartMappings.put("stacked_bar_chart", universalMappings);
        chartMappings.put("basic_area_chart", universalMappings);

        // PIE类型图表
        chartMappings.put("basic_pie_chart", universalMappings);
        chartMappings.put("doughnut_chart", universalMappings);
        chartMappings.put("rose_chart", universalMappings);
        chartMappings.put("pie_chart", universalMappings); // 兼容版本

        // RADAR类型图表
        chartMappings.put("basic_radar_chart", universalMappings);
        chartMappings.put("filled_radar_chart", universalMappings);

        // GAUGE类型图表
        chartMappings.put("basic_gauge_chart", universalMappings);
        chartMappings.put("progress_gauge_chart", universalMappings);
        chartMappings.put("grade_gauge_chart", universalMappings);

        chartMappings.put("universal", universalMappings); // 通用模板

        System.out.println("✅ 初始化通用映射关系完成，包含 " + universalMappings.size() + " 个映射项");
        System.out.println("📋 支持的图表类型: " + chartMappings.keySet());
    }

    /**
     * 创建字段映射对象
     */
    private Map<String, Object> createFieldMapping(String placeholder, String table, String column, String type) {
        return createFieldMapping(placeholder, table, column, type, null);
    }

    private Map<String, Object> createFieldMapping(String placeholder, String table, String column, String type,
            String condition) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("placeholderName", placeholder);
        mapping.put("tableName", table);
        mapping.put("columnName", column);
        mapping.put("dataType", type);

        if (condition != null) {
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("filterValue", condition);
            mapping.put("queryConditions", conditions);
        }

        return mapping;
    }

    /**
     * 从统一数据视图中提取占位符数据
     */
    public Map<String, Object> extractDataFromUniversalView(String chartId, Set<String> placeholders) {
        Map<String, Object> extractedData = new HashMap<>();

        if (dataService == null) {
            // 如果数据服务未注入，返回模拟数据
            return simulateDataQuery(chartId, placeholders);
        }

        try {
            // 获取图表类型对应的数据
            List<UniversalChartDataView> dataList = dataService.getDataByChartType(chartId);

            for (String placeholder : placeholders) {
                Object extractedValue = extractPlaceholderValue(placeholder, dataList);
                extractedData.put(placeholder, extractedValue);
            }

        } catch (Exception e) {
            System.err.println("从统一数据视图提取数据失败: " + e.getMessage());
            // 回退到模拟数据
            return simulateDataQuery(chartId, placeholders);
        }

        return extractedData;
    }

    /**
     * 提取单个占位符的值
     */
    private Object extractPlaceholderValue(String placeholder, List<UniversalChartDataView> dataList) {
        Map<String, Object> chartMapping = getChartMappings("universal");
        @SuppressWarnings("unchecked")
        Map<String, Object> mapping = (Map<String, Object>) chartMapping.get(placeholder);

        if (mapping == null || dataList.isEmpty()) {
            return null;
        }

        String fieldName = (String) mapping.get("fieldName");
        String dataType = (String) mapping.get("dataType");
        Function<UniversalChartDataView, Object> extractor = fieldExtractors.get(fieldName);

        if (extractor == null) {
            return null;
        }

        // 根据数据类型处理返回值
        if ("array".equals(dataType)) {
            // 对于数组类型，收集所有数据
            return dataList.stream()
                    .map(extractor)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            // 对于单值类型，返回第一个非空值
            return dataList.stream()
                    .map(extractor)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 重新加载映射关系
     */
    public void reloadMappings() {
        synchronized (initLock) {
            try {
                chartMappings.clear();
                fieldExtractors.clear();
                initializeFieldExtractors();
                initializeUniversalMappings();
                System.out.println("✅ 映射关系重新加载完成，支持图表类型数量: " + chartMappings.size());
            } catch (Exception e) {
                System.err.println("❌ 映射关系重新加载失败: " + e.getMessage());
                throw new RuntimeException("映射关系重新加载失败", e);
            }
        }
    }

    /**
     * 检查初始化状态
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取指定图表的映射关系
     */
    public Map<String, Object> getChartMappings(String chartId) {
        return chartMappings.getOrDefault(chartId, new HashMap<>());
    }

    /**
     * 保存图表的映射关系
     */
    public void saveChartMappings(String chartId, Map<String, Object> mappings) {
        chartMappings.put(chartId, mappings);
        System.out.println("✅ 保存图表 " + chartId + " 的映射关系，包含 " + mappings.size() + " 个映射项");
    }

    /**
     * 获取指定占位符的映射信息
     */
    public Map<String, Object> getPlaceholderMapping(String chartId, String placeholder) {
        Map<String, Object> chartMapping = getChartMappings(chartId);
        return (Map<String, Object>) chartMapping.get(placeholder);
    }

    /**
     * 验证映射关系的完整性
     */
    public List<String> validateMappings(String chartId, Set<String> requiredPlaceholders) {
        Map<String, Object> chartMapping = getChartMappings(chartId);
        List<String> missingMappings = new ArrayList<>();

        for (String placeholder : requiredPlaceholders) {
            if (!chartMapping.containsKey(placeholder)) {
                missingMappings.add(placeholder);
            }
        }

        return missingMappings;
    }

    /**
     * 根据映射关系模拟数据库查询结果
     */
    public Map<String, Object> simulateDataQuery(String chartId, Set<String> placeholders) {
        Map<String, Object> queryResults = new HashMap<>();
        Map<String, Object> chartMapping = getChartMappings(chartId);

        for (String placeholder : placeholders) {
            Map<String, Object> mapping = (Map<String, Object>) chartMapping.get(placeholder);
            if (mapping != null) {
                Object mockData = generateMockData(mapping, chartId);
                queryResults.put(placeholder, mockData);
            }
        }

        return queryResults;
    }

    /**
     * 根据映射信息生成Mock数据
     */
    private Object generateMockData(Map<String, Object> mapping, String chartId) {
        String dataType = (String) mapping.get("dataType");
        // 优先使用fieldName，如果不存在则使用columnName（向后兼容）
        String columnName = (String) mapping.get("fieldName");
        if (columnName == null) {
            columnName = (String) mapping.get("columnName");
        }
        Map<String, Object> conditions = (Map<String, Object>) mapping.get("queryConditions");

        // 添加null检查
        if (dataType == null) {
            System.err.println("⚠️ dataType为null，使用默认值");
            dataType = "string";
        }
        if (columnName == null) {
            System.err.println("⚠️ columnName为null，使用默认值");
            columnName = "default_column";
        }

        switch (dataType) {
            case "string":
                if (conditions != null && conditions.containsKey("filterValue")) {
                    return conditions.get("filterValue");
                }
                return generateStringData(columnName);

            case "array":
                return generateArrayData(columnName, conditions);

            case "number":
                return generateNumberData(columnName);

            case "boolean":
                if (conditions != null && conditions.containsKey("filterValue")) {
                    String value = (String) conditions.get("filterValue");
                    return Boolean.parseBoolean(value);
                }
                return generateBooleanData(columnName, chartId);

            case "object":
                return generateObjectData(columnName, conditions);

            default:
                return "Mock_" + columnName;
        }
    }

    /**
     * 生成字符串类型的Mock数据
     */
    private String generateStringData(String columnName) {
        switch (columnName) {
            case "title":
                return "动态营销渠道分析";
            case "chart_type":
                return "line";
            case "theme":
                return "default";
            case "stack_group":
                return "Total";
            case "series_style":
                return "smooth";
            case "legend_config":
                return "default";
            case "axis_config":
                return "default";
            case "grid_config":
                return "default";
            case "tooltip_config":
                return "default";
            case "zoom_config":
                return "default";
            case "brush_config":
                return "default";
            default:
                return "Mock_" + columnName;
        }
    }

    /**
     * 生成数组类型的Mock数据
     */
    private Object generateArrayData(String columnName, Map<String, Object> conditions) {
        if ("day_name".equals(columnName)) {
            return Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
        }

        if ("conversion_count".equals(columnName) && conditions != null) {
            String channel = (String) conditions.get("filterValue");
            return generateChannelData(channel);
        }

        // 饼图数据
        if ("pie_data".equals(columnName)) {
            return Arrays.asList(
                    Map.of("value", 335, "name", "直接访问"),
                    Map.of("value", 310, "name", "邮件营销"),
                    Map.of("value", 234, "name", "联盟广告"),
                    Map.of("value", 135, "name", "视频广告"),
                    Map.of("value", 1548, "name", "搜索引擎"));
        }

        // 雷达图数据
        if ("radar_data".equals(columnName)) {
            return Arrays.asList(
                    Map.of("value", Arrays.asList(4200, 3000, 20000, 35000, 50000, 18000), "name", "预算分配"),
                    Map.of("value", Arrays.asList(5000, 14000, 28000, 26000, 42000, 21000), "name", "实际开销"));
        }

        // 仪表盘数据
        if ("gauge_data".equals(columnName)) {
            return Arrays.asList(
                    Map.of("value", 75, "name", "完成率"));
        }

        // 中心配置（饼图）
        if ("center_config".equals(columnName)) {
            return Arrays.asList("50%", "50%");
        }

        return Arrays.asList(120, 280, 150, 320, 180, 380, 220);
    }

    /**
     * 根据渠道生成对应的数据
     */
    private List<Integer> generateChannelData(String channel) {
        switch (channel) {
            case "Email":
                return Arrays.asList(120, 132, 101, 134, 90, 230, 210);
            case "Union Ads":
                return Arrays.asList(220, 182, 191, 234, 290, 330, 310);
            case "Video Ads":
                return Arrays.asList(150, 232, 201, 154, 190, 330, 410);
            case "Direct":
                return Arrays.asList(320, 332, 301, 334, 390, 330, 320);
            case "Search Engine":
                return Arrays.asList(820, 932, 901, 934, 1290, 1330, 1320);
            default:
                return Arrays.asList(180, 350, 120, 420, 200, 380, 250);
        }
    }

    /**
     * 生成数字类型的Mock数据
     */
    private Integer generateNumberData(String columnName) {
        return new Random().nextInt(1000) + 100;
    }

    /**
     * 生成布尔类型的Mock数据
     */
    private Boolean generateBooleanData(String columnName, String chartId) {
        switch (columnName) {
            case "boundary_gap":
                return false; // 折线图通常不需要边界间隙
            case "smooth_style":
                // 🔧 根据图表类型设置平滑样式
                if ("basic_line_chart".equals(chartId)) {
                    System.out.println("🔧 [SMOOTH_STYLE] 基础折线图 -> smooth: false");
                    return false; // 基础折线图：直线连接
                } else if ("smooth_line_chart".equals(chartId)) {
                    System.out.println("🔧 [SMOOTH_STYLE] 平滑折线图 -> smooth: true");
                    return true; // 平滑折线图：曲线连接
                } else if ("stacked_line_chart".equals(chartId)) {
                    System.out.println("🔧 [SMOOTH_STYLE] 堆叠折线图 -> smooth: false");
                    return false; // 堆叠折线图：直线连接（突出堆叠效果）
                } else {
                    System.out.println("🔧 [SMOOTH_STYLE] 其他图表类型(" + chartId + ") -> smooth: true");
                    return true; // 其他图表类型默认使用平滑样式
                }
            default:
                return false;
        }
    }

    /**
     * 生成对象类型的Mock数据
     */
    private Object generateObjectData(String columnName, Map<String, Object> conditions) {
        switch (columnName) {
            case "radar_config":
                Map<String, Object> radarConfig = new HashMap<>();
                radarConfig.put("indicator", Arrays.asList(
                        Map.of("name", "销售", "max", 6500),
                        Map.of("name", "管理", "max", 16000),
                        Map.of("name", "信息技术", "max", 30000),
                        Map.of("name", "客服", "max", 38000),
                        Map.of("name", "研发", "max", 52000),
                        Map.of("name", "市场", "max", 25000)));
                return radarConfig;

            case "gauge_detail":
                Map<String, Object> detail = new HashMap<>();
                detail.put("formatter", "{value}%");
                return detail;

            case "gauge_pointer":
                Map<String, Object> pointer = new HashMap<>();
                pointer.put("itemStyle", Map.of("color", "auto"));
                return pointer;

            default:
                return new HashMap<>();
        }
    }

    /**
     * 获取所有图表的映射关系概览
     */
    public Map<String, Integer> getMappingsSummary() {
        Map<String, Integer> summary = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : chartMappings.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }
        return summary;
    }
}
