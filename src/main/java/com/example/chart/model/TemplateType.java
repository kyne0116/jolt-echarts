package com.example.chart.model;

/**
 * 图表模板类型枚举
 * 基于坐标系和数据结构的差异进行分类
 */
public enum TemplateType {
    
    /**
     * 直角坐标系图表
     * 包括：折线图、柱状图、面积图等
     * 特点：使用 xAxis/yAxis，数据格式为数组
     */
    CARTESIAN("cartesian", "直角坐标系图表", "折线图、柱状图、面积图"),
    
    /**
     * 饼图类图表  
     * 包括：饼图、环形图、玫瑰图等
     * 特点：使用 radius/center，数据格式为对象数组
     */
    PIE("pie", "饼图类图表", "饼图、环形图、玫瑰图"),
    
    /**
     * 雷达图类图表
     * 包括：雷达图、极坐标图等
     * 特点：使用 radar.indicator，数据格式为多维数组
     */
    RADAR("radar", "雷达图类图表", "雷达图、极坐标图"),
    
    /**
     * 仪表盘类图表
     * 包括：仪表盘、进度条等
     * 特点：使用 min/max，数据格式为单值对象
     */
    GAUGE("gauge", "仪表盘类图表", "仪表盘、进度条");
    
    private final String code;
    private final String name;
    private final String description;
    
    TemplateType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据图表类型推断模板类型
     */
    public static TemplateType inferFromChartType(String chartType) {
        if (chartType == null) {
            return CARTESIAN; // 默认类型
        }
        
        String type = chartType.toLowerCase();
        
        // 直角坐标系类型
        if (type.contains("line") || type.contains("bar") || type.contains("area") || 
            type.contains("column") || type.contains("stack")) {
            return CARTESIAN;
        }
        
        // 饼图类型
        if (type.contains("pie") || type.contains("doughnut") || type.contains("ring")) {
            return PIE;
        }
        
        // 雷达图类型
        if (type.contains("radar") || type.contains("polar")) {
            return RADAR;
        }
        
        // 仪表盘类型
        if (type.contains("gauge") || type.contains("meter") || type.contains("speedometer")) {
            return GAUGE;
        }
        
        // 默认返回直角坐标系（最常用）
        return CARTESIAN;
    }
    
    /**
     * 获取支持的图表类型列表
     */
    public String[] getSupportedChartTypes() {
        switch (this) {
            case CARTESIAN:
                return new String[]{
                    "basic_line_chart", "smooth_line_chart", "stacked_line_chart",
                    "basic_bar_chart", "stacked_bar_chart", "basic_area_chart"
                };
            case PIE:
                return new String[]{
                    "basic_pie_chart", "doughnut_chart", "rose_chart"
                };
            case RADAR:
                return new String[]{
                    "basic_radar_chart", "filled_radar_chart"
                };
            case GAUGE:
                return new String[]{
                    "basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart"
                };
            default:
                return new String[]{};
        }
    }
    
    /**
     * 检查图表类型是否被此模板类型支持
     */
    public boolean supports(String chartType) {
        if (chartType == null) return false;
        
        for (String supportedType : getSupportedChartTypes()) {
            if (supportedType.equals(chartType)) {
                return true;
            }
        }
        
        // 如果不在预定义列表中，使用推断逻辑
        return inferFromChartType(chartType) == this;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s): %s", name, code, description);
    }
}
