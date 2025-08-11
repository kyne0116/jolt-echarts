package com.example.chart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用模板管理控制器
 * 提供8个通用模板文件的完整CRUD操作和文件管理功能
 */
@RestController
@RequestMapping("/api/universal-templates")
@CrossOrigin(origins = "*")
public class UniversalTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(UniversalTemplateController.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 通用模板文件存储路径
    private static final String TEMPLATE_BASE_PATH = "src/main/resources/universal-templates/";
    private static final String CLASSPATH_TEMPLATE_BASE = "universal-templates/";
    
    // 8个通用模板的映射关系
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
        // 折线图系列 -> line-chart-template
        put("basic_line_chart", "line-chart-template");
        put("smooth_line_chart", "line-chart-template");
        put("stacked_line_chart", "line-chart-template");
        put("step_line_chart", "line-chart-template");
        
        // 柱状图系列 -> bar-chart-template
        put("basic_bar_chart", "bar-chart-template");
        put("stacked_bar_chart", "bar-chart-template");
        put("horizontal_bar_chart", "bar-chart-template");
        put("grouped_bar_chart", "bar-chart-template");
        
        // 面积图系列 -> area-chart-template
        put("basic_area_chart", "area-chart-template");
        put("stacked_area_chart", "area-chart-template");
        
        // 散点图系列 -> scatter-chart-template
        put("scatter_chart", "scatter-chart-template");
        put("bubble_chart", "scatter-chart-template");
        
        // 饼图系列 -> pie-chart-template
        put("basic_pie_chart", "pie-chart-template");
        put("doughnut_chart", "pie-chart-template");
        put("rose_chart", "pie-chart-template");
        put("pie_chart", "pie-chart-template");
        
        // 层次图系列 -> treemap-chart-template
        put("nested_pie_chart", "treemap-chart-template");
        put("sunburst_chart", "treemap-chart-template");
        put("treemap_chart", "treemap-chart-template");
        put("funnel_chart", "treemap-chart-template");
        
        // 雷达图系列 -> radar-chart-template
        put("basic_radar_chart", "radar-chart-template");
        put("filled_radar_chart", "radar-chart-template");
        put("polar_chart", "radar-chart-template");
        put("radar_multiple_chart", "radar-chart-template");
        
        // 仪表盘系列 -> gauge-chart-template
        put("basic_gauge_chart", "gauge-chart-template");
        put("progress_gauge_chart", "gauge-chart-template");
        put("grade_gauge_chart", "gauge-chart-template");
        put("speedometer_chart", "gauge-chart-template");
        put("thermometer_chart", "gauge-chart-template");
        put("ring_progress_chart", "gauge-chart-template");
    }};

    /**
     * 获取所有通用模板的管理表格数据
     */
    @GetMapping("/table")
    public ResponseEntity<Map<String, Object>> getUniversalTemplateTable() {
        try {
            List<Map<String, Object>> tableData = new ArrayList<>();
            
            for (Map.Entry<String, String> entry : TEMPLATE_MAPPINGS.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                String templateKey = entry.getKey();
                String templateFile = entry.getValue();
                
                row.put("templateKey", templateKey);
                row.put("templateName", getTemplateDisplayName(templateKey));
                row.put("templateCategory", getTemplateCategory(templateKey));
                row.put("fileName", templateFile);
                
                // 检查文件是否存在
                boolean exists = checkTemplateExists(templateFile);
                row.put("fileExists", exists);
                row.put("status", exists ? "可用" : "缺失");
                
                if (exists) {
                    // 获取文件信息
                    Map<String, Object> fileInfo = getTemplateFileInfo(templateFile);
                    row.put("fileInfo", fileInfo);
                }
                
                // 获取支持的图表类型数量
                long supportedChartCount = CHART_TYPE_TO_TEMPLATE.entrySet().stream()
                    .filter(chartEntry -> chartEntry.getValue().equals(templateKey))
                    .count();
                row.put("supportedChartCount", supportedChartCount);
                
                tableData.add(row);
            }
            
            // 添加统计信息
            Map<String, Object> response = new HashMap<>();
            response.put("data", tableData);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalTemplates", TEMPLATE_MAPPINGS.size());
            statistics.put("availableTemplates", tableData.stream().mapToLong(row -> 
                (Boolean) row.get("fileExists") ? 1L : 0L).sum());
            statistics.put("missingTemplates", tableData.stream().mapToLong(row -> 
                (Boolean) row.get("fileExists") ? 0L : 1L).sum());
            statistics.put("totalSupportedChartTypes", CHART_TYPE_TO_TEMPLATE.size());
            
            response.put("statistics", statistics);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取通用模板表格数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取通用模板表格数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据模板键获取通用模板内容
     */
    @GetMapping("/{templateKey}")
    public ResponseEntity<Map<String, Object>> getUniversalTemplate(@PathVariable String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "未知的模板键: " + templateKey);
                return ResponseEntity.badRequest().body(error);
            }
            
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            if (!resource.exists()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模板文件不存在: " + templateFile);
                return ResponseEntity.notFound().build();
            }
            
            InputStream inputStream = resource.getInputStream();
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            Map<String, Object> template = objectMapper.readValue(content, Map.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("templateKey", templateKey);
            response.put("fileName", templateFile);
            response.put("content", template);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取通用模板失败: " + templateKey, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取通用模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据图表类型获取对应的通用模板
     */
    @GetMapping("/by-chart-type/{chartType}")
    public ResponseEntity<Map<String, Object>> getTemplateByChartType(@PathVariable String chartType) {
        try {
            String templateKey = CHART_TYPE_TO_TEMPLATE.get(chartType);
            if (templateKey == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "图表类型 '" + chartType + "' 没有对应的通用模板");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 重定向到通用的获取模板方法
            return getUniversalTemplate(templateKey);
            
        } catch (Exception e) {
            logger.error("根据图表类型获取通用模板失败: " + chartType, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "根据图表类型获取通用模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 上传通用模板文件
     */
    @PostMapping("/upload/{templateKey}")
    public ResponseEntity<Map<String, Object>> uploadUniversalTemplate(
            @PathVariable String templateKey,
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (!TEMPLATE_MAPPINGS.containsKey(templateKey)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "未知的模板键: " + templateKey);
                return ResponseEntity.badRequest().body(error);
            }
            
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "上传文件为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件是否为有效的JSON
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            try {
                objectMapper.readValue(content, Map.class);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不是有效的JSON格式: " + e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            
            // 保存文件到对应位置
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            Path targetPath = Paths.get(TEMPLATE_BASE_PATH + templateFile);
            
            // 确保目录存在
            Files.createDirectories(targetPath.getParent());
            
            // 写入文件
            Files.write(targetPath, file.getBytes(), StandardOpenOption.CREATE, 
                       StandardOpenOption.TRUNCATE_EXISTING);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "通用模板上传成功");
            response.put("templateKey", templateKey);
            response.put("fileName", templateFile);
            response.put("fileSize", file.getSize());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("通用模板上传成功: {} -> {}", templateKey, templateFile);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("上传通用模板失败: " + templateKey, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传通用模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 下载通用模板文件
     */
    @GetMapping("/download/{templateKey}")
    public ResponseEntity<Resource> downloadUniversalTemplate(@PathVariable String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateFile + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new InputStreamResource(resource.getInputStream()));
                
        } catch (Exception e) {
            logger.error("下载通用模板失败: " + templateKey, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除通用模板文件
     */
    @DeleteMapping("/{templateKey}")
    public ResponseEntity<Map<String, Object>> deleteUniversalTemplate(@PathVariable String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "未知的模板键: " + templateKey);
                return ResponseEntity.badRequest().body(error);
            }
            
            Path targetPath = Paths.get(TEMPLATE_BASE_PATH + templateFile);
            if (!Files.exists(targetPath)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模板文件不存在: " + templateFile);
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(targetPath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "通用模板删除成功");
            response.put("templateKey", templateKey);
            response.put("fileName", templateFile);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("通用模板删除成功: {} -> {}", templateKey, templateFile);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("删除通用模板失败: " + templateKey, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除通用模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 预览通用模板内容
     */
    @GetMapping("/preview/{templateKey}")
    public ResponseEntity<Map<String, Object>> previewUniversalTemplate(@PathVariable String templateKey) {
        try {
            String templateFile = TEMPLATE_MAPPINGS.get(templateKey);
            if (templateFile == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "未知的模板键: " + templateKey);
                return ResponseEntity.badRequest().body(error);
            }
            
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            if (!resource.exists()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模板文件不存在: " + templateFile);
                return ResponseEntity.notFound().build();
            }
            
            InputStream inputStream = resource.getInputStream();
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            
            Map<String, Object> response = new HashMap<>();
            response.put("templateKey", templateKey);
            response.put("fileName", templateFile);
            response.put("content", content);
            response.put("displayName", getTemplateDisplayName(templateKey));
            response.put("category", getTemplateCategory(templateKey));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("预览通用模板失败: " + templateKey, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "预览通用模板失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取支持的图表类型列表
     */
    @GetMapping("/supported-chart-types")
    public ResponseEntity<Map<String, Object>> getSupportedChartTypes() {
        try {
            Map<String, List<String>> templateToCharts = new HashMap<>();
            
            for (Map.Entry<String, String> entry : CHART_TYPE_TO_TEMPLATE.entrySet()) {
                String chartType = entry.getKey();
                String templateKey = entry.getValue();
                
                templateToCharts.computeIfAbsent(templateKey, k -> new ArrayList<>()).add(chartType);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("templateMappings", templateToCharts);
            response.put("totalChartTypes", CHART_TYPE_TO_TEMPLATE.size());
            response.put("totalTemplates", TEMPLATE_MAPPINGS.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取支持的图表类型失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取支持的图表类型失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "UniversalTemplateController");
        response.put("timestamp", System.currentTimeMillis());
        response.put("totalTemplates", TEMPLATE_MAPPINGS.size());
        response.put("totalSupportedChartTypes", CHART_TYPE_TO_TEMPLATE.size());
        
        return ResponseEntity.ok(response);
    }

    // 私有辅助方法

    private boolean checkTemplateExists(String templateFile) {
        try {
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> getTemplateFileInfo(String templateFile) {
        Map<String, Object> info = new HashMap<>();
        try {
            Resource resource = new ClassPathResource(CLASSPATH_TEMPLATE_BASE + templateFile);
            if (resource.exists()) {
                info.put("size", resource.contentLength());
                info.put("lastModified", resource.lastModified());
                info.put("readable", resource.isReadable());
            }
        } catch (Exception e) {
            logger.warn("获取模板文件信息失败: " + templateFile, e);
        }
        return info;
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