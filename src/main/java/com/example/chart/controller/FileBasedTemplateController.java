package com.example.chart.controller;

import com.example.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于文件的模板管理控制器
 * 提供模板文件和JOLT规范文件的上传、下载、列表查询等管理功能
 */
@RestController
@RequestMapping("/api/file-templates")
public class FileBasedTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedTemplateController.class);
    
    // 模板文件存储路径
    private static final String TEMPLATE_BASE_PATH = "src/main/resources/echarts";
    // JOLT规范文件存储路径
    private static final String JOLT_SPEC_BASE_PATH = "src/main/resources/jolt-specs";
    
    // 支持的图表类型目录
    private static final List<String> CHART_CATEGORIES = Arrays.asList(
        "折线图", "柱状图", "饼图", "雷达图", "仪表盘"
    );

    /**
     * 获取模板文件列表
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listTemplates(
            @RequestParam(required = false) String category) {
        logger.info("📁 [模板列表] 获取模板文件列表，分类: {}", category);
        
        try {
            List<Map<String, Object>> templates = new ArrayList<>();
            Path basePath = Paths.get(TEMPLATE_BASE_PATH);
            
            if (!Files.exists(basePath)) {
                logger.warn("⚠️ [模板列表] 模板基础路径不存在: {}", basePath);
                return ResponseEntity.ok(ApiResponse.ok(templates));
            }
            
            // 遍历所有分类目录
            try (Stream<Path> paths = Files.list(basePath)) {
                List<Path> categoryDirs = paths
                    .filter(Files::isDirectory)
                    .filter(path -> category == null || path.getFileName().toString().equals(category))
                    .collect(Collectors.toList());
                
                for (Path categoryDir : categoryDirs) {
                    String categoryName = categoryDir.getFileName().toString();
                    
                    try (Stream<Path> files = Files.list(categoryDir)) {
                        List<Path> jsonFiles = files
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".json"))
                            .collect(Collectors.toList());
                        
                        for (Path file : jsonFiles) {
                            Map<String, Object> fileInfo = createFileInfo(file, categoryName, "template");
                            templates.add(fileInfo);
                        }
                    }
                }
            }
            
            logger.info("✅ [模板列表] 获取成功，共 {} 个文件", templates.size());
            return ResponseEntity.ok(ApiResponse.ok(templates));
            
        } catch (Exception e) {
            logger.error("❌ [模板列表] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 获取JOLT规范文件列表
     */
    @GetMapping("/jolt-specs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listJoltSpecs() {
        logger.info("📋 [规范列表] 获取JOLT规范文件列表");
        
        try {
            List<Map<String, Object>> specs = new ArrayList<>();
            Path basePath = Paths.get(JOLT_SPEC_BASE_PATH);
            
            if (!Files.exists(basePath)) {
                logger.warn("⚠️ [规范列表] JOLT规范基础路径不存在: {}", basePath);
                return ResponseEntity.ok(ApiResponse.ok(specs));
            }
            
            try (Stream<Path> paths = Files.list(basePath)) {
                List<Path> jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
                
                for (Path file : jsonFiles) {
                    Map<String, Object> fileInfo = createFileInfo(file, null, "jolt-spec");
                    specs.add(fileInfo);
                }
            }
            
            logger.info("✅ [规范列表] 获取成功，共 {} 个文件", specs.size());
            return ResponseEntity.ok(ApiResponse.ok(specs));
            
        } catch (Exception e) {
            logger.error("❌ [规范列表] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 上传模板文件
     */
    @PostMapping("/templates/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(required = false) String description) {
        logger.info("📤 [模板上传] 上传模板文件，分类: {}", category);
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("FILE_EMPTY", "上传文件为空"));
            }
            
            if (!file.getOriginalFilename().endsWith(".json")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_FILE_TYPE", "只支持JSON文件"));
            }
            
            // 验证分类
            if (!CHART_CATEGORIES.contains(category)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_CATEGORY", "无效的图表分类"));
            }
            
            // 创建分类目录（如果不存在）
            Path categoryPath = Paths.get(TEMPLATE_BASE_PATH, category);
            if (!Files.exists(categoryPath)) {
                Files.createDirectories(categoryPath);
            }
            
            // 保存文件
            Path filePath = categoryPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回文件信息
            Map<String, Object> fileInfo = createFileInfo(filePath, category, "template");
            fileInfo.put("description", description);
            
            logger.info("✅ [模板上传] 上传成功: {}", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.ok(fileInfo));
            
        } catch (Exception e) {
            logger.error("❌ [模板上传] 上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }

    /**
     * 上传JOLT规范文件
     */
    @PostMapping("/jolt-specs/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadJoltSpec(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        logger.info("📤 [规范上传] 上传JOLT规范文件");
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("FILE_EMPTY", "上传文件为空"));
            }
            
            if (!file.getOriginalFilename().endsWith(".json")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_FILE_TYPE", "只支持JSON文件"));
            }
            
            // 创建规范目录（如果不存在）
            Path basePath = Paths.get(JOLT_SPEC_BASE_PATH);
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            
            // 保存文件
            Path filePath = basePath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回文件信息
            Map<String, Object> fileInfo = createFileInfo(filePath, null, "jolt-spec");
            fileInfo.put("description", description);
            
            logger.info("✅ [规范上传] 上传成功: {}", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.ok(fileInfo));
            
        } catch (Exception e) {
            logger.error("❌ [规范上传] 上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }

    /**
     * 下载模板文件
     */
    @GetMapping("/templates/download/{category}/{filename}")
    public ResponseEntity<Resource> downloadTemplate(
            @PathVariable String category,
            @PathVariable String filename) {
        logger.info("📥 [模板下载] 下载模板文件: {}/{}", category, filename);
        
        try {
            Path filePath = Paths.get(TEMPLATE_BASE_PATH, category, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [模板下载] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            
            logger.info("✅ [模板下载] 下载成功: {}", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(filePath.toFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("❌ [模板下载] 下载失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 下载JOLT规范文件
     */
    @GetMapping("/jolt-specs/download/{filename}")
    public ResponseEntity<Resource> downloadJoltSpec(@PathVariable String filename) {
        logger.info("📥 [规范下载] 下载JOLT规范文件: {}", filename);
        
        try {
            Path filePath = Paths.get(JOLT_SPEC_BASE_PATH, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [规范下载] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            
            logger.info("✅ [规范下载] 下载成功: {}", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(filePath.toFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("❌ [规范下载] 下载失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除模板文件
     */
    @DeleteMapping("/templates/{category}/{filename}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable String category,
            @PathVariable String filename) {
        logger.info("🗑️ [模板删除] 删除模板文件: {}/{}", category, filename);
        
        try {
            Path filePath = Paths.get(TEMPLATE_BASE_PATH, category, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [模板删除] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(filePath);
            
            logger.info("✅ [模板删除] 删除成功: {}", filename);
            return ResponseEntity.ok(ApiResponse.ok(null));
            
        } catch (Exception e) {
            logger.error("❌ [模板删除] 删除失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("DELETE_FAILED", e.getMessage()));
        }
    }

    /**
     * 删除JOLT规范文件
     */
    @DeleteMapping("/jolt-specs/{filename}")
    public ResponseEntity<ApiResponse<Void>> deleteJoltSpec(@PathVariable String filename) {
        logger.info("🗑️ [规范删除] 删除JOLT规范文件: {}", filename);
        
        try {
            Path filePath = Paths.get(JOLT_SPEC_BASE_PATH, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [规范删除] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(filePath);
            
            logger.info("✅ [规范删除] 删除成功: {}", filename);
            return ResponseEntity.ok(ApiResponse.ok(null));
            
        } catch (Exception e) {
            logger.error("❌ [规范删除] 删除失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("DELETE_FAILED", e.getMessage()));
        }
    }

    /**
     * 获取支持的图表分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        logger.info("📁 [分类列表] 获取支持的图表分类列表");
        
        try {
            logger.info("✅ [分类列表] 获取成功，共 {} 个分类", CHART_CATEGORIES.size());
            return ResponseEntity.ok(ApiResponse.ok(CHART_CATEGORIES));
        } catch (Exception e) {
            logger.error("❌ [分类列表] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 创建文件信息对象
     */
    private Map<String, Object> createFileInfo(Path filePath, String category, String type) throws IOException {
        Map<String, Object> fileInfo = new HashMap<>();
        
        fileInfo.put("name", filePath.getFileName().toString());
        fileInfo.put("path", filePath.toString());
        fileInfo.put("size", Files.size(filePath));
        fileInfo.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
        fileInfo.put("type", type);
        
        if (category != null) {
            fileInfo.put("category", category);
        }
        
        return fileInfo;
    }
}