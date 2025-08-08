package com.example.chart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 真正通用的图表模板
 * 基于语义化结构，支持所有图表类型
 */
public class UniversalTemplate {
    
    // 图表基本信息
    private ChartInfo chart;
    
    // 数据结构
    private DataStructure data;
    
    // 布局配置
    private LayoutConfig layout;
    
    // 交互配置
    private InteractionConfig interaction;
    
    public static class ChartInfo {
        private String title = "${chart_title}";
        private String type = "${chart_type}";
        private String theme = "${chart_theme}";
        
        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
    }
    
    public static class DataStructure {
        private String categories = "${categories}";
        private List<SeriesItem> series = new ArrayList<>();
        
        public DataStructure() {
            // 默认创建5个系列，支持大部分图表需求
            for (int i = 1; i <= 5; i++) {
                SeriesItem item = new SeriesItem();
                item.setName("${series_" + i + "_name}");
                item.setValues("${series_" + i + "_data}");
                item.setStyle("${series_" + i + "_style}");
                series.add(item);
            }
        }
        
        public String getCategories() { return categories; }
        public void setCategories(String categories) { this.categories = categories; }
        public List<SeriesItem> getSeries() { return series; }
        public void setSeries(List<SeriesItem> series) { this.series = series; }
    }
    
    public static class SeriesItem {
        private String name;
        private String values;
        private String style;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValues() { return values; }
        public void setValues(String values) { this.values = values; }
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
    }
    
    public static class LayoutConfig {
        private String legend = "${legend_config}";
        private String axis = "${axis_config}";
        private String grid = "${grid_config}";
        
        public String getLegend() { return legend; }
        public void setLegend(String legend) { this.legend = legend; }
        public String getAxis() { return axis; }
        public void setAxis(String axis) { this.axis = axis; }
        public String getGrid() { return grid; }
        public void setGrid(String grid) { this.grid = grid; }
    }
    
    public static class InteractionConfig {
        private String tooltip = "${tooltip_config}";
        private String zoom = "${zoom_config}";
        private String brush = "${brush_config}";
        
        public String getTooltip() { return tooltip; }
        public void setTooltip(String tooltip) { this.tooltip = tooltip; }
        public String getZoom() { return zoom; }
        public void setZoom(String zoom) { this.zoom = zoom; }
        public String getBrush() { return brush; }
        public void setBrush(String brush) { this.brush = brush; }
    }
    
    // 主类的getters and setters
    public ChartInfo getChart() { return chart; }
    public void setChart(ChartInfo chart) { this.chart = chart; }
    public DataStructure getData() { return data; }
    public void setData(DataStructure data) { this.data = data; }
    public LayoutConfig getLayout() { return layout; }
    public void setLayout(LayoutConfig layout) { this.layout = layout; }
    public InteractionConfig getInteraction() { return interaction; }
    public void setInteraction(InteractionConfig interaction) { this.interaction = interaction; }
    
    /**
     * 创建默认的通用模板实例
     */
    public static UniversalTemplate createDefault() {
        UniversalTemplate template = new UniversalTemplate();
        template.chart = new ChartInfo();
        template.data = new DataStructure();
        template.layout = new LayoutConfig();
        template.interaction = new InteractionConfig();
        return template;
    }
    
    /**
     * 转换为Map格式，便于JSON序列化
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        
        // 图表信息
        Map<String, Object> chartMap = new HashMap<>();
        chartMap.put("title", chart.getTitle());
        chartMap.put("type", chart.getType());
        chartMap.put("theme", chart.getTheme());
        result.put("chart", chartMap);
        
        // 数据结构
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("categories", data.getCategories());
        
        List<Map<String, Object>> seriesList = new ArrayList<>();
        for (SeriesItem item : data.getSeries()) {
            Map<String, Object> seriesMap = new HashMap<>();
            seriesMap.put("name", item.getName());
            seriesMap.put("values", item.getValues());
            seriesMap.put("style", item.getStyle());
            seriesList.add(seriesMap);
        }
        dataMap.put("series", seriesList);
        result.put("data", dataMap);
        
        // 布局配置
        Map<String, Object> layoutMap = new HashMap<>();
        layoutMap.put("legend", layout.getLegend());
        layoutMap.put("axis", layout.getAxis());
        layoutMap.put("grid", layout.getGrid());
        result.put("layout", layoutMap);
        
        // 交互配置
        Map<String, Object> interactionMap = new HashMap<>();
        interactionMap.put("tooltip", interaction.getTooltip());
        interactionMap.put("zoom", interaction.getZoom());
        interactionMap.put("brush", interaction.getBrush());
        result.put("interaction", interactionMap);
        
        return result;
    }
}
