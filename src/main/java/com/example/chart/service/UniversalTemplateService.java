package com.example.chart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用模板服务类
 * 提供通用模板的业务逻辑处理和占位符管理功能
 */
@Service
public class UniversalTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(UniversalTemplateService.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String CLASSPATH_TEMPLATE_BASE = "universal-templates/";
    
    // 通用模板文件映射
    private static final Map<String, String> TEMPLATE_MAPPINGS = new HashMap<String, String>() {{
        put("line-chart-template", "line-chart-template.json");
        put("bar-chart-template", "bar-chart-template.json");
        put("area-chart-template", "area-chart-template.json");
        put("scatter-chart-template", "scatter-chart-template.json");
        put("pie-chart-template", "pie-chart-template.json");
        put("treemap-chart-template", "treemap-chart-template.json");
        put("radar-chart-template", "radar-chart-template.json");
        put("gauge-chart-template", "gauge-chart-template.json");
    }};
    
    // 图表类型到通用模板的映射关系
    private static final Map<String, String> CHART_TYPE_TO_TEMPLATE = new HashMap<String, String>() {{
        // 折线图系列
        put("basic_line_chart", "line-chart-template");
        put("smooth_line_chart", "line-chart-template");
        put("stacked_line_chart", "line-chart-template");
        put("step_line_chart", "line-chart-template");
        
        // 柱状图系列
        put("basic_bar_chart", "bar-chart-template");
        put("stacked_bar_chart", "bar-chart-template");
        put("horizontal_bar_chart", "bar-chart-template");
        put("grouped_bar_chart", "bar-chart-template");
        
        // 面积图系列
        put("basic_area_chart", "area-chart-template");
        put("stacked_area_chart", "area-chart-template");
        
        // 散点图系列
        put("scatter_chart", "scatter-chart-template");
        put("bubble_chart", "scatter-chart-template");
        
        // 饼图系列
        put("basic_pie_chart", "pie-chart-template");
        put("doughnut_chart", "pie-chart-template");
        put("rose_chart", "pie-chart-template");
        put("pie_chart", "pie-chart-template");
        
        // 层次图系列
        put("nested_pie_chart", "treemap-chart-template");
        put("sunburst_chart", "treemap-chart-template");
        put("treemap_chart", "treemap-chart-template");
        put("funnel_chart", "treemap-chart-template");
        
        // 雷达图系列
        put("basic_radar_chart", "radar-chart-template");
        put("filled_radar_chart", "radar-chart-template");
        put("polar_chart", "radar-chart-template");
        put("radar_multiple_chart", "radar-chart-template");
        
        // 仪表盘系列
        put("basic_gauge_chart", "gauge-chart-template");
        put("progress_gauge_chart", "gauge-chart-template");
        put("grade_gauge_chart", "gauge-chart-template");
        put("speedometer_chart", "gauge-chart-template");
        put("thermometer_chart", "gauge-chart-template");
        put("ring_progress_chart", "gauge-chart-template");
    }};

    /**
     * 根据图表类型获取对应的通用模板
     */
    public Map<String, Object> getUniversalTemplateByChartType(String chartType) {
        try {
            String templateKey = CHART_TYPE_TO_TEMPLATE.get(chartType);
            if (templateKey == null) {
                throw new RuntimeException("图表类型 '" + chartType + "' 没有对应的通用模板");
            }
            
            return getUniversalTemplate(templateKey);
            
        } catch (Exception e) {
            logger.error("根据图表类型获取通用模板失败: " + chartType, e);
            throw new RuntimeException("根据图表类型获取通用模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据模板键获取通用模板
     */
    public Map<String, Object> getUniversalTemplate(String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) {
                throw new RuntimeException("未知的模板键: " + templateKey);
            }
            
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            if (!resource.exists()) {
                throw new RuntimeException("通用模板文件不存在: " + templateFile);
            }
            
            InputStream inputStream = resource.getInputStream();
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            
            return objectMapper.readValue(content, Map.class);
            
        } catch (Exception e) {
            logger.error("获取通用模板失败: " + templateKey, e);
            throw new RuntimeException("获取通用模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从通用模板中提取所有占位符
     */
    public List<String> extractPlaceholdersFromTemplate(String templateKey) {
        try {
            Map<String, Object> template = getUniversalTemplate(templateKey);
            String jsonString = objectMapper.writeValueAsString(template);
            
            return extractPlaceholdersFromJson(jsonString);
            
        } catch (Exception e) {
            logger.error("从通用模板提取占位符失败: " + templateKey, e);
            throw new RuntimeException("从通用模板提取占位符失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从通用模板中提取占位符（根据图表类型）
     */
    public List<String> extractPlaceholdersByChartType(String chartType) {
        try {
            String templateKey = CHART_TYPE_TO_TEMPLATE.get(chartType);
            if (templateKey == null) {
                throw new RuntimeException("图表类型 '" + chartType + "' 没有对应的通用模板");
            }
            
            return extractPlaceholdersFromTemplate(templateKey);
            
        } catch (Exception e) {
            logger.error("根据图表类型提取占位符失败: " + chartType, e);
            throw new RuntimeException("根据图表类型提取占位符失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证通用模板的格式和结构
     */
    public Map<String, Object> validateUniversalTemplate(String templateKey) {
        Map<String, Object> validationResult = new HashMap<>();
        
        try {
            Map<String, Object> template = getUniversalTemplate(templateKey);
            
            // 检查必要的顶级结构
            List<String> missingKeys = new ArrayList<>();
            String[] requiredKeys = {"chartMeta", "dataStructure", "styleConfig"};
            
            for (String key : requiredKeys) {
                if (!template.containsKey(key)) {
                    missingKeys.add(key);
                }
            }
            
            // 检查chartMeta中的必要字段
            if (template.containsKey("chartMeta")) {
                Map<String, Object> chartMeta = (Map<String, Object>) template.get("chartMeta");
                String[] metaKeys = {"title", "chartType", "templateCategory"};
                
                for (String key : metaKeys) {
                    if (!chartMeta.containsKey(key)) {
                        missingKeys.add("chartMeta." + key);
                    }
                }
            }
            
            // 提取占位符
            List<String> placeholders = extractPlaceholdersFromTemplate(templateKey);
            
            // 统计占位符数量
            Map<String, Integer> placeholderStats = new HashMap<>();
            for (String placeholder : placeholders) {
                String category = categorizePlaceholder(placeholder);
                placeholderStats.put(category, placeholderStats.getOrDefault(category, 0) + 1);
            }
            
            validationResult.put("valid", missingKeys.isEmpty());
            validationResult.put("missingKeys", missingKeys);
            validationResult.put("placeholders", placeholders);
            validationResult.put("placeholderCount", placeholders.size());
            validationResult.put("placeholderStats", placeholderStats);
            validationResult.put("templateKey", templateKey);
            validationResult.put("templateCategory", getTemplateCategory(templateKey));
            
        } catch (Exception e) {
            validationResult.put("valid", false);
            validationResult.put("error", e.getMessage());
            validationResult.put("placeholders", new ArrayList<>());
            validationResult.put("placeholderCount", 0);
        }
        
        return validationResult;
    }

    /**
     * 获取所有可用的通用模板列表
     */
    public List<Map<String, Object>> getAllUniversalTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : TEMPLATE_MAPPINGS.entrySet()) {
            Map<String, Object> templateInfo = new HashMap<>();
            String templateKey = entry.getKey();
            String templateFile = entry.getValue();
            
            templateInfo.put("templateKey", templateKey);
            templateInfo.put("fileName", templateFile);
            templateInfo.put("displayName", getTemplateDisplayName(templateKey));
            templateInfo.put("category", getTemplateCategory(templateKey));
            templateInfo.put("exists", checkTemplateExists(templateKey));
            
            // 获取支持的图表类型
            List<String> supportedChartTypes = CHART_TYPE_TO_TEMPLATE.entrySet().stream()
                .filter(chartEntry -> chartEntry.getValue().equals(templateKey))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
            
            templateInfo.put("supportedChartTypes", supportedChartTypes);
            templateInfo.put("supportedCount", supportedChartTypes.size());
            
            templates.add(templateInfo);
        }
        
        return templates;
    }

    /**
     * 根据模板分类获取通用模板
     */
    public List<Map<String, Object>> getUniversalTemplatesByCategory(String category) {
        return getAllUniversalTemplates().stream()
            .filter(template -> category.equals(template.get("category")))
            .collect(java.util.stream.Collectors.toList());
    }

    // 私有辅助方法

    private List<String> extractPlaceholdersFromJson(String jsonString) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(jsonString);
        
        Set<String> uniquePlaceholders = new HashSet<>();
        while (matcher.find()) {
            uniquePlaceholders.add(matcher.group(0));
        }
        
        placeholders.addAll(uniquePlaceholders);
        Collections.sort(placeholders);
        return placeholders;
    }

    private String categorizePlaceholder(String placeholder) {
        if (placeholder.contains("chart_") || placeholder.contains("title") || placeholder.contains("description")) {
            return "元数据";
        } else if (placeholder.contains("series_") || placeholder.contains("data") || placeholder.contains("categories")) {
            return "数据";
        } else if (placeholder.contains("color") || placeholder.contains("style") || placeholder.contains("font")) {
            return "样式";
        } else if (placeholder.contains("axis") || placeholder.contains("grid") || placeholder.contains("legend")) {
            return "布局";
        } else if (placeholder.contains("animation") || placeholder.contains("tooltip") || placeholder.contains("toolbox")) {
            return "交互";
        } else {
            return "其他";
        }
    }

    private boolean checkTemplateExists(String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) return false;
            
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private String getTemplateDisplayName(String templateKey) {
        switch (templateKey) {
            case "line-chart-template": return "折线图通用模板";
            case "bar-chart-template": return "柱状图通用模板";
            case "area-chart-template": return "面积图通用模板";
            case "scatter-chart-template": return "散点图通用模板";
            case "pie-chart-template": return "饼图通用模板";
            case "treemap-chart-template": return "层次图通用模板";
            case "radar-chart-template": return "雷达图通用模板";
            case "gauge-chart-template": return "仪表盘通用模板";
            default: return templateKey;
        }
    }

    private String getTemplateCategory(String templateKey) {
        if (templateKey.contains("line") || templateKey.contains("bar") || 
            templateKey.contains("area") || templateKey.contains("scatter")) {
            return "CARTESIAN";
        } else if (templateKey.contains("pie")) {
            return "PIE";
        } else if (templateKey.contains("treemap")) {
            return "TREE";
        } else if (templateKey.contains("radar")) {
            return "RADAR";
        } else if (templateKey.contains("gauge")) {
            return "GAUGE";
        } else {
            return "UNKNOWN";
        }
    }
}