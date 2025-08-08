package com.example.chart.service;

import com.example.chart.model.UniversalTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能转换引擎
 * 根据图表类型将通用语义结构转换为ECharts标准结构
 */
@Service
public class SmartTransformationEngine {
    
    /**
     * 第一阶段：语义化转换
     * 将通用模板转换为ECharts结构，保留占位符
     */
    public Map<String, Object> semanticTransform(Map<String, Object> universalTemplate) {
        String chartType = extractChartType(universalTemplate);
        
        switch (chartType) {
            case "line":
            case "area":
                return transformToLineChart(universalTemplate);
            case "bar":
            case "column":
                return transformToBarChart(universalTemplate);
            case "pie":
            case "doughnut":
                return transformToPieChart(universalTemplate);
            case "scatter":
                return transformToScatterChart(universalTemplate);
            default:
                return transformToLineChart(universalTemplate); // 默认折线图
        }
    }
    
    /**
     * 提取图表类型
     */
    private String extractChartType(Map<String, Object> template) {
        Map<String, Object> chart = (Map<String, Object>) template.get("chart");
        if (chart != null) {
            String type = (String) chart.get("type");
            if (type != null && type.startsWith("${")) {
                // 如果是占位符，根据上下文推断类型
                return inferChartType(template);
            }
            return type;
        }
        return "line"; // 默认类型
    }
    
    /**
     * 根据上下文推断图表类型
     */
    private String inferChartType(Map<String, Object> template) {
        // 可以根据数据结构、字段名等推断图表类型
        // 这里简化处理，返回默认类型
        return "line";
    }
    
    /**
     * 转换为折线图结构
     */
    private Map<String, Object> transformToLineChart(Map<String, Object> universalTemplate) {
        Map<String, Object> echarts = new HashMap<>();
        
        // 标题
        Map<String, Object> chart = (Map<String, Object>) universalTemplate.get("chart");
        if (chart != null) {
            Map<String, Object> title = new HashMap<>();
            title.put("text", chart.get("title"));
            echarts.put("title", title);
        }
        
        // 数据结构
        Map<String, Object> data = (Map<String, Object>) universalTemplate.get("data");
        if (data != null) {
            // X轴
            Map<String, Object> xAxis = new HashMap<>();
            xAxis.put("type", "category");
            xAxis.put("data", data.get("categories"));
            xAxis.put("boundaryGap", false); // 折线图特有
            echarts.put("xAxis", xAxis);
            
            // Y轴
            Map<String, Object> yAxis = new HashMap<>();
            yAxis.put("type", "value");
            echarts.put("yAxis", yAxis);
            
            // 系列数据
            List<Map<String, Object>> seriesList = new ArrayList<>();
            List<String> legendData = new ArrayList<>();
            
            List<Map<String, Object>> universalSeries = (List<Map<String, Object>>) data.get("series");
            if (universalSeries != null) {
                for (Map<String, Object> item : universalSeries) {
                    Map<String, Object> series = new HashMap<>();
                    series.put("name", item.get("name"));
                    series.put("type", "line"); // 折线图类型
                    series.put("data", item.get("values"));
                    
                    // 处理样式
                    String style = (String) item.get("style");
                    if (style != null && !style.startsWith("${")) {
                        // 解析样式配置
                        series.putAll(parseStyle(style));
                    }
                    
                    seriesList.add(series);
                    legendData.add((String) item.get("name"));
                }
            }
            
            echarts.put("series", seriesList);
            
            // 图例
            Map<String, Object> legend = new HashMap<>();
            legend.put("data", legendData);
            legend.put("show", true);
            echarts.put("legend", legend);
        }
        
        // 布局配置
        addLayoutConfig(echarts, universalTemplate);
        
        // 交互配置
        addInteractionConfig(echarts, universalTemplate);
        
        return echarts;
    }
    
    /**
     * 转换为柱状图结构
     */
    private Map<String, Object> transformToBarChart(Map<String, Object> universalTemplate) {
        Map<String, Object> echarts = transformToLineChart(universalTemplate);
        
        // 修改系列类型为bar
        List<Map<String, Object>> series = (List<Map<String, Object>>) echarts.get("series");
        if (series != null) {
            for (Map<String, Object> item : series) {
                item.put("type", "bar");
            }
        }
        
        // 修改X轴配置
        Map<String, Object> xAxis = (Map<String, Object>) echarts.get("xAxis");
        if (xAxis != null) {
            xAxis.put("boundaryGap", true); // 柱状图特有
        }
        
        return echarts;
    }
    
    /**
     * 转换为饼图结构
     */
    private Map<String, Object> transformToPieChart(Map<String, Object> universalTemplate) {
        Map<String, Object> echarts = new HashMap<>();
        
        // 标题
        Map<String, Object> chart = (Map<String, Object>) universalTemplate.get("chart");
        if (chart != null) {
            Map<String, Object> title = new HashMap<>();
            title.put("text", chart.get("title"));
            echarts.put("title", title);
        }
        
        // 饼图特殊处理：只使用第一个系列的数据
        Map<String, Object> data = (Map<String, Object>) universalTemplate.get("data");
        if (data != null) {
            List<Map<String, Object>> universalSeries = (List<Map<String, Object>>) data.get("series");
            if (universalSeries != null && !universalSeries.isEmpty()) {
                Map<String, Object> firstSeries = universalSeries.get(0);
                
                Map<String, Object> series = new HashMap<>();
                series.put("name", firstSeries.get("name"));
                series.put("type", "pie");
                series.put("radius", "50%");
                
                // 饼图数据结构特殊处理
                List<Map<String, Object>> pieData = new ArrayList<>();
                // 这里需要将categories和values组合成饼图数据
                series.put("data", "${pie_data}"); // 特殊占位符，需要在第二阶段特殊处理
                
                echarts.put("series", Arrays.asList(series));
            }
        }
        
        // 添加配置
        addLayoutConfig(echarts, universalTemplate);
        addInteractionConfig(echarts, universalTemplate);
        
        return echarts;
    }
    
    /**
     * 转换为散点图结构
     */
    private Map<String, Object> transformToScatterChart(Map<String, Object> universalTemplate) {
        Map<String, Object> echarts = transformToLineChart(universalTemplate);
        
        // 修改系列类型为scatter
        List<Map<String, Object>> series = (List<Map<String, Object>>) echarts.get("series");
        if (series != null) {
            for (Map<String, Object> item : series) {
                item.put("type", "scatter");
            }
        }
        
        return echarts;
    }
    
    /**
     * 添加布局配置
     */
    private void addLayoutConfig(Map<String, Object> echarts, Map<String, Object> universalTemplate) {
        Map<String, Object> layout = (Map<String, Object>) universalTemplate.get("layout");
        if (layout != null) {
            // 网格配置
            String gridConfig = (String) layout.get("grid");
            if (gridConfig != null && !gridConfig.startsWith("${")) {
                echarts.put("grid", parseGridConfig(gridConfig));
            } else {
                // 默认网格配置
                Map<String, Object> grid = new HashMap<>();
                grid.put("left", "3%");
                grid.put("right", "4%");
                grid.put("bottom", "3%");
                grid.put("containLabel", true);
                echarts.put("grid", grid);
            }
        }
    }
    
    /**
     * 添加交互配置
     */
    private void addInteractionConfig(Map<String, Object> echarts, Map<String, Object> universalTemplate) {
        Map<String, Object> interaction = (Map<String, Object>) universalTemplate.get("interaction");
        if (interaction != null) {
            // 提示框配置
            String tooltipConfig = (String) interaction.get("tooltip");
            if (tooltipConfig != null && !tooltipConfig.startsWith("${")) {
                echarts.put("tooltip", parseTooltipConfig(tooltipConfig));
            } else {
                // 默认提示框配置
                Map<String, Object> tooltip = new HashMap<>();
                tooltip.put("trigger", "axis");
                echarts.put("tooltip", tooltip);
            }
            
            // 工具箱配置
            Map<String, Object> toolbox = new HashMap<>();
            Map<String, Object> feature = new HashMap<>();
            feature.put("saveAsImage", new HashMap<>());
            toolbox.put("feature", feature);
            echarts.put("toolbox", toolbox);
        }
    }
    
    /**
     * 解析样式配置
     */
    private Map<String, Object> parseStyle(String style) {
        // 简化实现，实际可以支持更复杂的样式解析
        Map<String, Object> styleMap = new HashMap<>();
        if (style.contains("smooth")) {
            styleMap.put("smooth", true);
        }
        if (style.contains("stack")) {
            styleMap.put("stack", "Total");
        }
        return styleMap;
    }
    
    /**
     * 解析网格配置
     */
    private Map<String, Object> parseGridConfig(String config) {
        // 简化实现
        Map<String, Object> grid = new HashMap<>();
        grid.put("left", "3%");
        grid.put("right", "4%");
        grid.put("bottom", "3%");
        grid.put("containLabel", true);
        return grid;
    }
    
    /**
     * 解析提示框配置
     */
    private Map<String, Object> parseTooltipConfig(String config) {
        // 简化实现
        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("trigger", "axis");
        return tooltip;
    }
}
