package com.example.chart.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 映射关系管理服务
 * 负责占位符与数据库字段的映射关系管理
 */
@Service
public class MappingRelationshipService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 模拟映射关系存储（生产环境中应该是数据库）
    private final Map<String, Map<String, Object>> chartMappings = new HashMap<>();

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
     * 初始化示例映射关系 - 更新为新的通用模板格式
     */
    public void initializeSampleMappings() {
        // 通用模板的映射关系（适用于所有图表类型）
        Map<String, Object> universalMappings = new HashMap<>();

        // 图表基本信息映射
        universalMappings.put("${chart_title}", createFieldMapping(
                "${chart_title}", "chart_config", "title", "string"));
        universalMappings.put("${chart_type}", createFieldMapping(
                "${chart_type}", "chart_config", "chart_type", "string"));
        universalMappings.put("${chart_theme}", createFieldMapping(
                "${chart_theme}", "chart_config", "theme", "string"));

        // 数据结构映射
        universalMappings.put("${categories}", createFieldMapping(
                "${categories}", "marketing_data", "day_name", "array"));

        // 系列名称映射（新格式）
        universalMappings.put("${series_1_name}", createFieldMapping(
                "${series_1_name}", "marketing_data", "channel_name", "string", "Email"));
        universalMappings.put("${series_2_name}", createFieldMapping(
                "${series_2_name}", "marketing_data", "channel_name", "string", "Union Ads"));
        universalMappings.put("${series_3_name}", createFieldMapping(
                "${series_3_name}", "marketing_data", "channel_name", "string", "Video Ads"));
        universalMappings.put("${series_4_name}", createFieldMapping(
                "${series_4_name}", "marketing_data", "channel_name", "string", "Direct"));
        universalMappings.put("${series_5_name}", createFieldMapping(
                "${series_5_name}", "marketing_data", "channel_name", "string", "Search Engine"));

        // 系列数据映射（新格式）
        universalMappings.put("${series_1_data}", createFieldMapping(
                "${series_1_data}", "marketing_data", "conversion_count", "array", "Email"));
        universalMappings.put("${series_2_data}", createFieldMapping(
                "${series_2_data}", "marketing_data", "conversion_count", "array", "Union Ads"));
        universalMappings.put("${series_3_data}", createFieldMapping(
                "${series_3_data}", "marketing_data", "conversion_count", "array", "Video Ads"));
        universalMappings.put("${series_4_data}", createFieldMapping(
                "${series_4_data}", "marketing_data", "conversion_count", "array", "Direct"));
        universalMappings.put("${series_5_data}", createFieldMapping(
                "${series_5_data}", "marketing_data", "conversion_count", "array", "Search Engine"));

        // 系列样式映射
        universalMappings.put("${series_1_style}", createFieldMapping(
                "${series_1_style}", "chart_config", "series_style", "string", "smooth"));
        universalMappings.put("${series_2_style}", createFieldMapping(
                "${series_2_style}", "chart_config", "series_style", "string", "smooth"));
        universalMappings.put("${series_3_style}", createFieldMapping(
                "${series_3_style}", "chart_config", "series_style", "string", "smooth"));
        universalMappings.put("${series_4_style}", createFieldMapping(
                "${series_4_style}", "chart_config", "series_style", "string", "smooth"));
        universalMappings.put("${series_5_style}", createFieldMapping(
                "${series_5_style}", "chart_config", "series_style", "string", "smooth"));

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
        chartMappings.put("stacked_line_chart", universalMappings);
        chartMappings.put("basic_bar_chart", universalMappings);
        chartMappings.put("pie_chart", universalMappings);
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
                Object mockData = generateMockData(mapping);
                queryResults.put(placeholder, mockData);
            }
        }

        return queryResults;
    }

    /**
     * 根据映射信息生成Mock数据
     */
    private Object generateMockData(Map<String, Object> mapping) {
        String dataType = (String) mapping.get("dataType");
        String columnName = (String) mapping.get("columnName");
        Map<String, Object> conditions = (Map<String, Object>) mapping.get("queryConditions");

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

        return Arrays.asList(1, 2, 3, 4, 5);
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
                return Arrays.asList(100, 200, 300, 400, 500, 600, 700);
        }
    }

    /**
     * 生成数字类型的Mock数据
     */
    private Integer generateNumberData(String columnName) {
        return new Random().nextInt(1000) + 100;
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
