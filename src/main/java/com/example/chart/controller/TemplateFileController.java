package com.example.chart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
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
 * 模板文件管理控制器
 * 支持JSON模板文件和JOLT规范文件的上传、下载、删除操作
 */
@RestController
@RequestMapping("/api/template-files")
public class TemplateFileController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 文件存储路径配置
    private static final String TEMPLATE_BASE_PATH = "src/main/resources";
    private static final String JSON_TEMPLATE_PATH = TEMPLATE_BASE_PATH + "/universal-templates";
    private static final String JOLT_SPEC_PATH = TEMPLATE_BASE_PATH + "/jolt-specs";
    private static final String ECHARTS_EXAMPLES_PATH = TEMPLATE_BASE_PATH + "/echarts";

    /**
     * 获取模板管理数据表格
     */
    @GetMapping("/table")
    public ResponseEntity<Map<String, Object>> getTemplateTable() {
        try {
            List<Map<String, Object>> tableData = new ArrayList<>();
            
            // 获取所有支持的图表类型
            List<String> chartTypes = getSupportedChartTypes();
            
            for (String chartType : chartTypes) {
                Map<String, Object> row = new HashMap<>();
                row.put("chartType", chartType);
                row.put("chartName", getChartTypeName(chartType));
                row.put("category", getTemplateCategory(chartType));
                row.put("status", getImplementationStatus(chartType));
                
                // 获取关联的JSON模板文件信息
                Map<String, Object> jsonTemplate = getJsonTemplateInfo(chartType);
                row.put("jsonTemplate", jsonTemplate);
                
                // 获取关联的JOLT规范文件信息
                Map<String, Object> joltSpec = getJoltSpecInfo(chartType);
                row.put("joltSpec", joltSpec);
                
                tableData.add(row);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", tableData);
            response.put("total", tableData.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取模板表格数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 上传JSON模板文件
     */
    @PostMapping("/json-template/upload")
    public ResponseEntity<Map<String, Object>> uploadJsonTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chartType") String chartType) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件格式
            if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "只支持JSON格式文件");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证JSON内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            objectMapper.readValue(content, Map.class); // 验证JSON格式
            
            // 保存文件
            String fileName = getJsonTemplateFileName(chartType);
            Path filePath = Paths.get(JSON_TEMPLATE_PATH, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("filePath", filePath.toString());
            response.put("size", file.getSize());
            response.put("uploadTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传JSON模板文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 上传JOLT规范文件
     */
    @PostMapping("/jolt-spec/upload")
    public ResponseEntity<Map<String, Object>> uploadJoltSpec(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chartType") String chartType) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件格式
            if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "只支持JSON格式文件");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证JSON内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            objectMapper.readValue(content, Object.class); // 验证JSON格式
            
            // 保存文件
            String fileName = getJoltSpecFileName(chartType);
            Path filePath = Paths.get(JOLT_SPEC_PATH, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("filePath", filePath.toString());
            response.put("size", file.getSize());
            response.put("uploadTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传JOLT规范文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 下载JSON模板文件
     */
    @GetMapping("/json-template/download")
    public ResponseEntity<Resource> downloadJsonTemplate(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJsonTemplateFileName(chartType);
            Path filePath = Paths.get(JSON_TEMPLATE_PATH, fileName);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new org.springframework.core.io.FileSystemResource(filePath.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 下载JOLT规范文件
     */
    @GetMapping("/jolt-spec/download")
    public ResponseEntity<Resource> downloadJoltSpec(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJoltSpecFileName(chartType);
            Path filePath = Paths.get(JOLT_SPEC_PATH, fileName);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new org.springframework.core.io.FileSystemResource(filePath.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除JSON模板文件
     */
    @DeleteMapping("/json-template")
    public ResponseEntity<Map<String, Object>> deleteJsonTemplate(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJsonTemplateFileName(chartType);
            Path filePath = Paths.get(JSON_TEMPLATE_PATH, fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JSON模板文件删除成功");
            response.put("fileName", fileName);
            response.put("deleteTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除JSON模板文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 删除JOLT规范文件
     */
    @DeleteMapping("/jolt-spec")
    public ResponseEntity<Map<String, Object>> deleteJoltSpec(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJoltSpecFileName(chartType);
            Path filePath = Paths.get(JOLT_SPEC_PATH, fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JOLT规范文件删除成功");
            response.put("fileName", fileName);
            response.put("deleteTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除JOLT规范文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 预览JSON模板文件内容
     */
    @GetMapping("/json-template/preview")
    public ResponseEntity<Map<String, Object>> previewJsonTemplate(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJsonTemplateFileName(chartType);
            Path filePath = Paths.get(JSON_TEMPLATE_PATH, fileName);
            
            if (!Files.exists(filePath)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不存在: " + fileName);
                return ResponseEntity.notFound().build();
            }
            
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            Map<String, Object> jsonContent = objectMapper.readValue(content, Map.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("content", jsonContent);
            response.put("rawContent", content);
            response.put("size", Files.size(filePath));
            response.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "预览JSON模板文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 预览JOLT规范文件内容
     */
    @GetMapping("/jolt-spec/preview")
    public ResponseEntity<Map<String, Object>> previewJoltSpec(@RequestParam("chartType") String chartType) {
        try {
            String fileName = getJoltSpecFileName(chartType);
            Path filePath = Paths.get(JOLT_SPEC_PATH, fileName);
            
            if (!Files.exists(filePath)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不存在: " + fileName);
                return ResponseEntity.notFound().build();
            }
            
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            Object jsonContent = objectMapper.readValue(content, Object.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("content", jsonContent);
            response.put("rawContent", content);
            response.put("size", Files.size(filePath));
            response.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "预览JOLT规范文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // 私有辅助方法
    
    private List<String> getSupportedChartTypes() {
        return Arrays.asList(
            "basic_line_chart", "smooth_line_chart", "stacked_line_chart",
            "basic_bar_chart", "stacked_bar_chart", "basic_area_chart",
            "basic_pie_chart", "doughnut_chart", "rose_chart", "pie_chart",
            "basic_radar_chart", "filled_radar_chart",
            "basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart"
        );
    }
    
    private String getChartTypeName(String chartType) {
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("basic_line_chart", "基础折线图");
        nameMap.put("smooth_line_chart", "平滑折线图");
        nameMap.put("stacked_line_chart", "堆叠折线图");
        nameMap.put("basic_bar_chart", "基础柱状图");
        nameMap.put("stacked_bar_chart", "堆叠柱状图");
        nameMap.put("basic_area_chart", "基础面积图");
        nameMap.put("basic_pie_chart", "基础饼图");
        nameMap.put("doughnut_chart", "环形图");
        nameMap.put("rose_chart", "玫瑰图");
        nameMap.put("pie_chart", "饼图");
        nameMap.put("basic_radar_chart", "基础雷达图");
        nameMap.put("filled_radar_chart", "填充雷达图");
        nameMap.put("basic_gauge_chart", "基础仪表盘");
        nameMap.put("progress_gauge_chart", "进度仪表盘");
        nameMap.put("grade_gauge_chart", "等级仪表盘");
        
        return nameMap.getOrDefault(chartType, chartType);
    }
    
    private String getTemplateCategory(String chartType) {
        if (Arrays.asList("basic_line_chart", "smooth_line_chart", "stacked_line_chart", 
                         "basic_bar_chart", "stacked_bar_chart", "basic_area_chart").contains(chartType)) {
            return "CARTESIAN";
        } else if (Arrays.asList("basic_pie_chart", "doughnut_chart", "rose_chart", "pie_chart").contains(chartType)) {
            return "PIE";
        } else if (Arrays.asList("basic_radar_chart", "filled_radar_chart").contains(chartType)) {
            return "RADAR";
        } else if (Arrays.asList("basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart").contains(chartType)) {
            return "GAUGE";
        }
        return "UNKNOWN";
    }
    
    private String getImplementationStatus(String chartType) {
        Set<String> completed = new HashSet<>(Arrays.asList(
            "basic_line_chart", "smooth_line_chart", "stacked_line_chart",
            "basic_bar_chart", "stacked_bar_chart", "pie_chart"
        ));
        Set<String> development = new HashSet<>(Arrays.asList(
            "basic_pie_chart", "doughnut_chart", "basic_radar_chart",
            "basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart"
        ));
        
        if (completed.contains(chartType)) {
            return "completed";
        } else if (development.contains(chartType)) {
            return "development";
        } else {
            return "pending";
        }
    }
    
    private Map<String, Object> getJsonTemplateInfo(String chartType) {
        Map<String, Object> info = new HashMap<>();
        String fileName = getJsonTemplateFileName(chartType);
        Path filePath = Paths.get(JSON_TEMPLATE_PATH, fileName);
        
        info.put("fileName", fileName);
        info.put("exists", Files.exists(filePath));
        
        if (Files.exists(filePath)) {
            try {
                info.put("size", Files.size(filePath));
                info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            } catch (IOException e) {
                info.put("error", e.getMessage());
            }
        }
        
        return info;
    }
    
    private Map<String, Object> getJoltSpecInfo(String chartType) {
        Map<String, Object> info = new HashMap<>();
        String fileName = getJoltSpecFileName(chartType);
        Path filePath = Paths.get(JOLT_SPEC_PATH, fileName);
        
        info.put("fileName", fileName);
        info.put("exists", Files.exists(filePath));
        
        if (Files.exists(filePath)) {
            try {
                info.put("size", Files.size(filePath));
                info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            } catch (IOException e) {
                info.put("error", e.getMessage());
            }
        }
        
        return info;
    }
    
    private String getJsonTemplateFileName(String chartType) {
        return chartType + "-template.json";
    }
    
    private String getJoltSpecFileName(String chartType) {
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("basic_line_chart", "line-chart-placeholder.json");
        fileMap.put("smooth_line_chart", "line-chart-placeholder.json");
        fileMap.put("stacked_line_chart", "line-chart-stacked.json");
        fileMap.put("basic_bar_chart", "bar-chart-placeholder.json");
        fileMap.put("stacked_bar_chart", "bar-chart-placeholder.json");
        fileMap.put("basic_pie_chart", "pie-chart-placeholder.json");
        fileMap.put("doughnut_chart", "pie-chart-placeholder.json");
        fileMap.put("pie_chart", "pie-chart-placeholder.json");
        fileMap.put("basic_radar_chart", "radar-chart-placeholder.json");
        fileMap.put("filled_radar_chart", "radar-chart-placeholder.json");
        fileMap.put("basic_gauge_chart", "gauge-chart-placeholder.json");
        fileMap.put("progress_gauge_chart", "gauge-chart-placeholder.json");
        fileMap.put("grade_gauge_chart", "gauge-chart-placeholder.json");
        
        return fileMap.getOrDefault(chartType, chartType + "-spec.json");
    }
}