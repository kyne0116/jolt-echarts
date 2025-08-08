package com.example.chart.service;

import com.example.chart.model.TemplateType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 分类模板工厂
 * 根据模板类型创建对应的通用模板
 */
@Service
public class CategoryTemplateFactory {
    
    /**
     * 根据模板类型创建对应的通用模板
     */
    public Map<String, Object> createTemplate(TemplateType templateType) {
        switch (templateType) {
            case CARTESIAN:
                return createCartesianTemplate();
            case PIE:
                return createPieTemplate();
            case RADAR:
                return createRadarTemplate();
            case GAUGE:
                return createGaugeTemplate();
            default:
                return createCartesianTemplate(); // 默认模板
        }
    }
    
    /**
     * 创建直角坐标系通用模板
     * 适用于：折线图、柱状图、面积图
     */
    private Map<String, Object> createCartesianTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        // 图表基本信息
        Map<String, Object> chart = new HashMap<>();
        chart.put("title", "${chart_title}");
        chart.put("type", "${chart_type}");
        chart.put("theme", "${chart_theme}");
        template.put("chart", chart);
        
        // 坐标系配置
        Map<String, Object> coordinates = new HashMap<>();
        
        Map<String, Object> xAxis = new HashMap<>();
        xAxis.put("type", "category");
        xAxis.put("data", "${categories}");
        xAxis.put("boundaryGap", "${boundary_gap}");
        coordinates.put("xAxis", xAxis);
        
        Map<String, Object> yAxis = new HashMap<>();
        yAxis.put("type", "value");
        coordinates.put("yAxis", yAxis);
        
        template.put("coordinates", coordinates);
        
        // 数据结构
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        
        // 创建5个系列（支持大部分用例）
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", "${series_" + i + "_name}");
            seriesItem.put("type", "${series_type}");
            seriesItem.put("data", "${series_" + i + "_data}");
            seriesItem.put("stack", "${stack_group}");
            seriesItem.put("smooth", "${smooth_style}");
            series.add(seriesItem);
        }
        
        data.put("series", series);
        template.put("data", data);
        
        // 布局配置
        Map<String, Object> layout = new HashMap<>();
        layout.put("legend", "${legend_config}");
        layout.put("grid", "${grid_config}");
        layout.put("tooltip", "${tooltip_config}");
        layout.put("toolbox", "${toolbox_config}");
        template.put("layout", layout);
        
        return template;
    }
    
    /**
     * 创建饼图通用模板
     * 适用于：饼图、环形图、玫瑰图
     */
    private Map<String, Object> createPieTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        // 图表基本信息
        Map<String, Object> chart = new HashMap<>();
        chart.put("title", "${chart_title}");
        chart.put("type", "pie");
        chart.put("theme", "${chart_theme}");
        template.put("chart", chart);
        
        // 数据结构
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        
        Map<String, Object> seriesItem = new HashMap<>();
        seriesItem.put("type", "pie");
        seriesItem.put("radius", "${radius_config}");
        seriesItem.put("center", "${center_config}");
        seriesItem.put("data", "${pie_data}");
        seriesItem.put("roseType", "${rose_type}");
        series.add(seriesItem);
        
        data.put("series", series);
        template.put("data", data);
        
        // 布局配置
        Map<String, Object> layout = new HashMap<>();
        layout.put("legend", "${legend_config}");
        layout.put("tooltip", "${tooltip_config}");
        template.put("layout", layout);
        
        return template;
    }
    
    /**
     * 创建雷达图通用模板
     * 适用于：雷达图、极坐标图
     */
    private Map<String, Object> createRadarTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        // 图表基本信息
        Map<String, Object> chart = new HashMap<>();
        chart.put("title", "${chart_title}");
        chart.put("type", "radar");
        chart.put("theme", "${chart_theme}");
        template.put("chart", chart);
        
        // 坐标系配置
        Map<String, Object> coordinates = new HashMap<>();
        Map<String, Object> radar = new HashMap<>();
        radar.put("indicator", "${radar_indicators}");
        radar.put("shape", "${radar_shape}");
        radar.put("radius", "${radar_radius}");
        coordinates.put("radar", radar);
        template.put("coordinates", coordinates);
        
        // 数据结构
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        
        Map<String, Object> seriesItem = new HashMap<>();
        seriesItem.put("type", "radar");
        seriesItem.put("data", "${radar_data}");
        seriesItem.put("areaStyle", "${area_style}");
        series.add(seriesItem);
        
        data.put("series", series);
        template.put("data", data);
        
        // 布局配置
        Map<String, Object> layout = new HashMap<>();
        layout.put("legend", "${legend_config}");
        layout.put("tooltip", "${tooltip_config}");
        template.put("layout", layout);
        
        return template;
    }
    
    /**
     * 创建仪表盘通用模板
     * 适用于：仪表盘、进度条
     */
    private Map<String, Object> createGaugeTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        // 图表基本信息
        Map<String, Object> chart = new HashMap<>();
        chart.put("title", "${chart_title}");
        chart.put("type", "gauge");
        chart.put("theme", "${chart_theme}");
        template.put("chart", chart);
        
        // 数据结构
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        
        Map<String, Object> seriesItem = new HashMap<>();
        seriesItem.put("type", "gauge");
        seriesItem.put("min", "${min_value}");
        seriesItem.put("max", "${max_value}");
        seriesItem.put("data", "${gauge_data}");
        seriesItem.put("detail", "${detail_config}");
        seriesItem.put("pointer", "${pointer_config}");
        seriesItem.put("axisLine", "${axis_line_config}");
        seriesItem.put("progress", "${progress_config}");
        series.add(seriesItem);
        
        data.put("series", series);
        template.put("data", data);
        
        // 布局配置
        Map<String, Object> layout = new HashMap<>();
        layout.put("tooltip", "${tooltip_config}");
        template.put("layout", layout);
        
        return template;
    }
    
    /**
     * 获取模板的所有占位符
     */
    public Set<String> getTemplatePlaceholders(TemplateType templateType) {
        Map<String, Object> template = createTemplate(templateType);
        return extractPlaceholders(template);
    }
    
    /**
     * 递归提取占位符
     */
    private Set<String> extractPlaceholders(Object obj) {
        Set<String> placeholders = new HashSet<>();
        
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.startsWith("${") && str.endsWith("}")) {
                placeholders.add(str);
            }
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                placeholders.addAll(extractPlaceholders(value));
            }
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (Object item : list) {
                placeholders.addAll(extractPlaceholders(item));
            }
        }
        
        return placeholders;
    }
    
    /**
     * 根据图表类型自动选择模板
     */
    public Map<String, Object> createTemplateForChartType(String chartType) {
        TemplateType templateType = TemplateType.inferFromChartType(chartType);
        return createTemplate(templateType);
    }
}
