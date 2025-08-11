package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板测试控制器
 * 提供模板文件与JOLT规范文件的转换测试功能
 */
@RestController
@RequestMapping("/api/file-template-tests")
public class TemplateTestController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTestController.class);
    
    // 模板文件存储路径
    private static final String TEMPLATE_BASE_PATH = "src/main/resources/echarts";
    // JOLT规范文件存储路径
    private static final String JOLT_SPEC_BASE_PATH = "src/main/resources/jolt-specs";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取模板文件内容
     */
    @GetMapping("/templates/{category}/{filename}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplateContent(
            @PathVariable String category,
            @PathVariable String filename) {
        logger.info("📄 [模板内容] 获取模板文件内容: {}/{}", category, filename);
        
        try {
            Path filePath = Paths.get(TEMPLATE_BASE_PATH, category, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [模板内容] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 读取文件内容
            String content = new String(Files.readAllBytes(filePath));
            Object template = objectMapper.readValue(content, Object.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("path", category + "/" + filename);
            result.put("content", template);
            
            logger.info("✅ [模板内容] 获取成功: {}", filename);
            return ResponseEntity.ok(ApiResponse.ok(result));
            
        } catch (Exception e) {
            logger.error("❌ [模板内容] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("READ_FAILED", e.getMessage()));
        }
    }

    /**
     * 获取JOLT规范文件内容
     */
    @GetMapping("/jolt-specs/{filename}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJoltSpecContent(@PathVariable String filename) {
        logger.info("📋 [规范内容] 获取JOLT规范文件内容: {}", filename);
        
        try {
            Path filePath = Paths.get(JOLT_SPEC_BASE_PATH, filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("⚠️ [规范内容] 文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 读取文件内容
            String content = new String(Files.readAllBytes(filePath));
            Object spec = objectMapper.readValue(content, Object.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("path", filename);
            result.put("content", spec);
            
            logger.info("✅ [规范内容] 获取成功: {}", filename);
            return ResponseEntity.ok(ApiResponse.ok(result));
            
        } catch (Exception e) {
            logger.error("❌ [规范内容] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("READ_FAILED", e.getMessage()));
        }
    }

    /**
     * 测试模板与JOLT规范的关联转换
     */
    @PostMapping("/test-conversion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConversion(
            @RequestBody Map<String, String> request) {
        logger.info("🔄 [转换测试] 测试模板与JOLT规范的转换");
        
        try {
            String templatePath = request.get("templatePath");
            String joltSpecName = request.get("joltSpec");
            
            if (templatePath == null || joltSpecName == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", "缺少必要的参数"));
            }
            
            // 读取模板文件内容
            Path templateFilePath = Paths.get(TEMPLATE_BASE_PATH, templatePath);
            if (!Files.exists(templateFilePath)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("TEMPLATE_NOT_FOUND", "模板文件不存在"));
            }
            
            String templateContent = new String(Files.readAllBytes(templateFilePath));
            Object template = objectMapper.readValue(templateContent, Object.class);
            
            // 读取JOLT规范文件内容
            Path joltSpecPath = Paths.get(JOLT_SPEC_BASE_PATH, joltSpecName);
            if (!Files.exists(joltSpecPath)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("JOLT_SPEC_NOT_FOUND", "JOLT规范文件不存在"));
            }
            
            String joltSpecContent = new String(Files.readAllBytes(joltSpecPath));
            Object joltSpec = objectMapper.readValue(joltSpecContent, Object.class);
            
            // 构建测试结果
            Map<String, Object> result = new HashMap<>();
            result.put("templatePath", templatePath);
            result.put("joltSpec", joltSpecName);
            result.put("templateContent", template);
            result.put("joltSpecContent", joltSpec);
            result.put("testResult", "测试功能待实现"); // 实际项目中这里会执行JOLT转换
            
            logger.info("✅ [转换测试] 测试完成: {} -> {}", templatePath, joltSpecName);
            return ResponseEntity.ok(ApiResponse.ok(result));
            
        } catch (Exception e) {
            logger.error("❌ [转换测试] 测试失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("TEST_FAILED", e.getMessage()));
        }
    }

    /**
     * 提取模板中的占位符
     */
    @PostMapping("/extract-placeholders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractPlaceholders(
            @RequestBody Map<String, String> request) {
        logger.info("🏷️ [占位符提取] 提取模板中的占位符");
        
        try {
            String templatePath = request.get("templatePath");
            
            if (templatePath == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", "缺少模板路径参数"));
            }
            
            // 读取模板文件内容
            Path filePath = Paths.get(TEMPLATE_BASE_PATH, templatePath);
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("FILE_NOT_FOUND", "模板文件不存在"));
            }
            
            String content = new String(Files.readAllBytes(filePath));
            Set<String> placeholders = extractPlaceholdersFromContent(content);
            
            Map<String, Object> result = new HashMap<>();
            result.put("templatePath", templatePath);
            result.put("placeholders", new ArrayList<>(placeholders));
            result.put("count", placeholders.size());
            
            logger.info("✅ [占位符提取] 提取完成，共 {} 个占位符", placeholders.size());
            return ResponseEntity.ok(ApiResponse.ok(result));
            
        } catch (Exception e) {
            logger.error("❌ [占位符提取] 提取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("EXTRACTION_FAILED", e.getMessage()));
        }
    }

    /**
     * 从内容中提取占位符
     */
    private Set<String> extractPlaceholdersFromContent(String content) {
        Set<String> placeholders = new HashSet<>();
        // 简单的占位符提取正则表达式
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            placeholders.add(matcher.group(0));
        }
        
        return placeholders;
    }
}