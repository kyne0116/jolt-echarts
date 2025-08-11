package com.example.chart.controller;

import com.example.chart.repository.model.UniversalTemplateEntity;
import com.example.chart.service.TemplateService;
import com.example.chart.service.PlaceholderManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;
    
    @Autowired
    private PlaceholderManager placeholderManager;
    
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<UniversalTemplateEntity>> listAll() {
        return ResponseEntity.ok(templateService.listAll());
    }

    @GetMapping("/{chartId}")
    public ResponseEntity<Map<String, Object>> getByChartId(@PathVariable String chartId) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("chartId", chartId);
        resp.put("template", templateService.getTemplateByChartId(chartId));
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<UniversalTemplateEntity> create(@RequestBody UniversalTemplateEntity entity) {
        return ResponseEntity.ok(templateService.save(entity));
    }

    @PutMapping("/{chartId}")
    public ResponseEntity<UniversalTemplateEntity> update(@PathVariable String chartId,
                                                          @RequestBody UniversalTemplateEntity entity) {
        entity.setChartId(chartId);
        return ResponseEntity.ok(templateService.save(entity));
    }

    @DeleteMapping("/{chartId}")
    public ResponseEntity<Void> delete(@PathVariable String chartId) {
        templateService.delete(chartId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 从JOLT规范导入模板
     */
    @PostMapping("/import-jolt/{chartId}")
    public ResponseEntity<Map<String, Object>> importFromJolt(@PathVariable String chartId) {
        try {
            // 构建JOLT文件路径
            String joltFilePath = getJoltSpecFile(chartId);
            Resource resource = new ClassPathResource(joltFilePath);
            
            if (!resource.exists()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "JOLT规范文件不存在: " + joltFilePath);
                return ResponseEntity.badRequest().body(error);
            }
            
            InputStream inputStream = resource.getInputStream();
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            Map<String, Object> joltSpec = objectMapper.readValue(content, Map.class);
            
            return ResponseEntity.ok(joltSpec);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "导入JOLT规范失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 从ECharts示例导入模板
     */
    @PostMapping("/import-example/{chartId}")
    public ResponseEntity<Map<String, Object>> importFromExample(@PathVariable String chartId) {
        try {
            // 构建ECharts示例文件路径
            String exampleFilePath = getEChartsExampleFile(chartId);
            Resource resource = new ClassPathResource(exampleFilePath);
            
            if (!resource.exists()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "ECharts示例文件不存在: " + exampleFilePath);
                return ResponseEntity.badRequest().body(error);
            }
            
            InputStream inputStream = resource.getInputStream();
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            Map<String, Object> example = objectMapper.readValue(content, Map.class);
            
            // 将ECharts配置转换为模板格式（添加占位符）
            Map<String, Object> template = convertEChartsToTemplate(example, chartId);
            
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "导入ECharts示例失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 导出模板
     */
    @GetMapping("/{chartId}/export")
    public ResponseEntity<Map<String, Object>> exportTemplate(@PathVariable String chartId) {
        try {
            Map<String, Object> template = templateService.getTemplateByChartId(chartId);
            
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("chartId", chartId);
            exportData.put("template", template);
            exportData.put("exportTime", System.currentTimeMillis());
            exportData.put("version", "1.0");
            
            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "导出模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 验证模板格式
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(@RequestBody Map<String, Object> template) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查JSON格式有效性
            String jsonString = objectMapper.writeValueAsString(template);
            objectMapper.readValue(jsonString, Map.class);
            
            // 提取占位符
            List<String> placeholders = extractPlaceholders(jsonString);
            
            // 检查必要字段
            List<String> missingFields = new ArrayList<>();
            if (!template.containsKey("chartMeta")) {
                missingFields.add("chartMeta");
            }
            
            result.put("valid", true);
            result.put("placeholders", placeholders);
            result.put("placeholderCount", placeholders.size());
            result.put("missingFields", missingFields);
            result.put("hasRequiredFields", missingFields.isEmpty());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            result.put("placeholders", new ArrayList<>());
            result.put("placeholderCount", 0);
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 提取占位符
     */
    @PostMapping("/extract-placeholders")
    public ResponseEntity<Map<String, Object>> extractPlaceholders(@RequestBody Map<String, Object> template) {
        try {
            String jsonString = objectMapper.writeValueAsString(template);
            List<String> placeholders = extractPlaceholders(jsonString);
            
            Map<String, Object> result = new HashMap<>();
            result.put("placeholders", placeholders);
            result.put("count", placeholders.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "提取占位符失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取支持的图表类型
     */
    @GetMapping("/chart-types")
    public ResponseEntity<List<Map<String, Object>>> getSupportedChartTypes() {
        List<Map<String, Object>> chartTypes = new ArrayList<>();
        
        // CARTESIAN 类型
        Map<String, Object> cartesian = new HashMap<>();
        cartesian.put("type", "cartesian");
        cartesian.put("name", "CARTESIAN (直角坐标系)");
        cartesian.put("description", "折线图、柱状图、面积图等");
        List<Map<String, Object>> cartesianCharts = Arrays.asList(
            createChartInfo("basic_line_chart", "基础折线图", "completed"),
            createChartInfo("smooth_line_chart", "平滑折线图", "completed"),
            createChartInfo("stacked_line_chart", "堆叠折线图", "completed"),
            createChartInfo("basic_bar_chart", "基础柱状图", "completed"),
            createChartInfo("stacked_bar_chart", "堆叠柱状图", "completed"),
            createChartInfo("basic_area_chart", "基础面积图", "pending")
        );
        cartesian.put("charts", cartesianCharts);
        chartTypes.add(cartesian);
        
        // PIE 类型
        Map<String, Object> pie = new HashMap<>();
        pie.put("type", "pie");
        pie.put("name", "PIE (饼图类)");
        pie.put("description", "饼图、环形图、玫瑰图等");
        List<Map<String, Object>> pieCharts = Arrays.asList(
            createChartInfo("basic_pie_chart", "基础饼图", "development"),
            createChartInfo("doughnut_chart", "环形图", "development"),
            createChartInfo("rose_chart", "玫瑰图", "pending"),
            createChartInfo("pie_chart", "饼图(兼容)", "completed")
        );
        pie.put("charts", pieCharts);
        chartTypes.add(pie);
        
        // RADAR 类型
        Map<String, Object> radar = new HashMap<>();
        radar.put("type", "radar");
        radar.put("name", "RADAR (雷达图类)");
        radar.put("description", "雷达图、极坐标图等");
        List<Map<String, Object>> radarCharts = Arrays.asList(
            createChartInfo("basic_radar_chart", "基础雷达图", "development"),
            createChartInfo("filled_radar_chart", "填充雷达图", "pending")
        );
        radar.put("charts", radarCharts);
        chartTypes.add(radar);
        
        // GAUGE 类型
        Map<String, Object> gauge = new HashMap<>();
        gauge.put("type", "gauge");
        gauge.put("name", "GAUGE (仪表盘类)");
        gauge.put("description", "仪表盘、进度条等");
        List<Map<String, Object>> gaugeCharts = Arrays.asList(
            createChartInfo("basic_gauge_chart", "基础仪表盘", "development"),
            createChartInfo("progress_gauge_chart", "进度仪表盘", "development"),
            createChartInfo("grade_gauge_chart", "等级仪表盘", "development")
        );
        gauge.put("charts", gaugeCharts);
        chartTypes.add(gauge);
        
        return ResponseEntity.ok(chartTypes);
    }
    
    /**
     * 获取模板分类信息
     */
    @GetMapping("/template-categories")
    public ResponseEntity<Map<String, Object>> getTemplateCategories() {
        Map<String, Object> categories = new HashMap<>();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTypes", 14);
        stats.put("completedTypes", 6);
        stats.put("developmentTypes", 6);
        stats.put("pendingTypes", 2);
        
        List<Map<String, Object>> categoryDetails = new ArrayList<>();
        categoryDetails.add(createCategoryInfo("cartesian", "CARTESIAN", 6, 5, 1));
        categoryDetails.add(createCategoryInfo("pie", "PIE", 4, 1, 3));
        categoryDetails.add(createCategoryInfo("radar", "RADAR", 2, 0, 2));
        categoryDetails.add(createCategoryInfo("gauge", "GAUGE", 3, 0, 3));
        
        categories.put("stats", stats);
        categories.put("categories", categoryDetails);
        
        return ResponseEntity.ok(categories);
    }
    
    // 私有辅助方法
    
    private String getJoltSpecFile(String chartId) {
        Map<String, String> joltFileMap = new HashMap<>();
        joltFileMap.put("basic_line_chart", "jolt-specs/line-chart-placeholder.json");
        joltFileMap.put("smooth_line_chart", "jolt-specs/line-chart-placeholder.json");
        joltFileMap.put("stacked_line_chart", "jolt-specs/line-chart-stacked.json");
        joltFileMap.put("basic_bar_chart", "jolt-specs/bar-chart-placeholder.json");
        joltFileMap.put("stacked_bar_chart", "jolt-specs/bar-chart-placeholder.json");
        joltFileMap.put("basic_pie_chart", "jolt-specs/pie-chart-placeholder.json");
        joltFileMap.put("doughnut_chart", "jolt-specs/pie-chart-placeholder.json");
        joltFileMap.put("basic_radar_chart", "jolt-specs/radar-chart-placeholder.json");
        joltFileMap.put("basic_gauge_chart", "jolt-specs/gauge-chart-placeholder.json");
        joltFileMap.put("progress_gauge_chart", "jolt-specs/gauge-chart-placeholder.json");
        joltFileMap.put("grade_gauge_chart", "jolt-specs/gauge-chart-placeholder.json");
        
        return joltFileMap.getOrDefault(chartId, "jolt-specs/line-chart-placeholder.json");
    }
    
    private String getEChartsExampleFile(String chartId) {
        Map<String, String> exampleFileMap = new HashMap<>();
        exampleFileMap.put("basic_line_chart", "echarts/折线图/基础折线图.json");
        exampleFileMap.put("smooth_line_chart", "echarts/折线图/基础平滑折线图.json");
        exampleFileMap.put("stacked_line_chart", "echarts/折线图/折线图堆叠.json");
        exampleFileMap.put("basic_bar_chart", "echarts/柱状图/基础柱状图.json");
        exampleFileMap.put("stacked_bar_chart", "echarts/柱状图/堆叠柱状图.json");
        exampleFileMap.put("basic_pie_chart", "echarts/饼图/富文本标签.json");
        exampleFileMap.put("doughnut_chart", "echarts/饼图/圆角环形图.json");
        exampleFileMap.put("basic_radar_chart", "echarts/雷达图/基础雷达图.json");
        exampleFileMap.put("basic_gauge_chart", "echarts/仪表盘/基础仪表盘.json");
        exampleFileMap.put("progress_gauge_chart", "echarts/仪表盘/进度仪表盘.json");
        exampleFileMap.put("grade_gauge_chart", "echarts/仪表盘/等级仪表盘.json");
        
        return exampleFileMap.getOrDefault(chartId, "echarts/折线图/基础折线图.json");
    }
    
    private Map<String, Object> convertEChartsToTemplate(Map<String, Object> echartsConfig, String chartId) {
        Map<String, Object> template = new HashMap<>();
        
        // 基本元数据
        Map<String, Object> chartMeta = new HashMap<>();
        chartMeta.put("title", "${chart_title}");
        chartMeta.put("type", "${chart_type}");
        chartMeta.put("chartId", chartId);
        template.put("chartMeta", chartMeta);
        
        // 根据图表类型转换
        if (chartId.contains("line") || chartId.contains("bar") || chartId.contains("area")) {
            // 直角坐标系图表
            template.put("categories", "${categories}");
            
            List<Map<String, Object>> series = new ArrayList<>();
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", "${series_1_name}");
            seriesItem.put("data", "${series_1_data}");
            seriesItem.put("type", chartId.contains("line") ? "line" : "bar");
            
            if (chartId.contains("stacked")) {
                seriesItem.put("stack", "${stack_group}");
            }
            
            series.add(seriesItem);
            template.put("series", series);
            
        } else if (chartId.contains("pie")) {
            // 饼图
            template.put("data", "${pie_data}");
            
        } else if (chartId.contains("radar")) {
            // 雷达图
            template.put("indicator", "${radar_indicators}");
            template.put("data", "${radar_data}");
            
        } else if (chartId.contains("gauge")) {
            // 仪表盘
            template.put("data", "${gauge_data}");
            template.put("min", "${gauge_min}");
            template.put("max", "${gauge_max}");
        }
        
        // 样式配置
        Map<String, Object> styleConfig = new HashMap<>();
        styleConfig.put("showLegend", true);
        styleConfig.put("showTooltip", true);
        template.put("styleConfig", styleConfig);
        
        return template;
    }
    
    private List<String> extractPlaceholders(String content) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(content);
        
        Set<String> uniquePlaceholders = new HashSet<>();
        while (matcher.find()) {
            uniquePlaceholders.add(matcher.group(0));
        }
        
        placeholders.addAll(uniquePlaceholders);
        Collections.sort(placeholders);
        return placeholders;
    }
    
    private Map<String, Object> createChartInfo(String id, String name, String status) {
        Map<String, Object> chart = new HashMap<>();
        chart.put("id", id);
        chart.put("name", name);
        chart.put("status", status);
        return chart;
    }
    
    private Map<String, Object> createCategoryInfo(String type, String name, int total, int completed, int pending) {
        Map<String, Object> category = new HashMap<>();
        category.put("type", type);
        category.put("name", name);
        category.put("total", total);
        category.put("completed", completed);
        category.put("development", total - completed - pending);
        category.put("pending", pending);
        return category;
    }
}
