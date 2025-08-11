package com.example.chart.controller;

import com.example.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 模板与JOLT规范关联管理控制器
 * 管理模板文件与JOLT规范文件的关联关系
 */
@RestController
@RequestMapping("/api/file-template-relations")
public class TemplateRelationController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateRelationController.class);
    
    // 模板文件存储路径
    private static final String TEMPLATE_BASE_PATH = "src/main/resources/echarts";
    // JOLT规范文件存储路径
    private static final String JOLT_SPEC_BASE_PATH = "src/main/resources/jolt-specs";
    
    // 关联关系存储（实际项目中应该使用数据库）
    private static final Map<String, String> TEMPLATE_JOLT_RELATIONS = new HashMap<>();
    
    static {
        // 初始化一些默认关联关系
        TEMPLATE_JOLT_RELATIONS.put("折线图/基础折线图.json", "line-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("折线图/基础平滑折线图.json", "line-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("折线图/折线图堆叠.json", "line-chart-stacked.json");
        TEMPLATE_JOLT_RELATIONS.put("柱状图/基础柱状图.json", "bar-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("柱状图/堆叠柱状图.json", "bar-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("饼图/富文本标签.json", "pie-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("饼图/圆角环形图.json", "pie-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("雷达图/基础雷达图.json", "radar-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("仪表盘/基础仪表盘.json", "gauge-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("仪表盘/进度仪表盘.json", "gauge-chart-placeholder.json");
        TEMPLATE_JOLT_RELATIONS.put("仪表盘/等级仪表盘.json", "gauge-chart-placeholder.json");
    }

    /**
     * 获取所有关联关系
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRelations() {
        logger.info("🔗 [关联列表] 获取所有模板与JOLT规范关联关系");
        
        try {
            List<Map<String, Object>> relations = new ArrayList<>();
            
            for (Map.Entry<String, String> entry : TEMPLATE_JOLT_RELATIONS.entrySet()) {
                Map<String, Object> relation = new HashMap<>();
                relation.put("templatePath", entry.getKey());
                relation.put("joltSpec", entry.getValue());
                relations.add(relation);
            }
            
            logger.info("✅ [关联列表] 获取成功，共 {} 个关联关系", relations.size());
            return ResponseEntity.ok(ApiResponse.ok(relations));
            
        } catch (Exception e) {
            logger.error("❌ [关联列表] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 为模板设置JOLT规范
     */
    @PostMapping("/{templatePath}/jolt-spec/{joltSpec}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setTemplateJoltSpec(
            @PathVariable String templatePath,
            @PathVariable String joltSpec) {
        logger.info("🔗 [关联设置] 设置模板 {} 的JOLT规范为 {}", templatePath, joltSpec);
        
        try {
            // 验证模板文件是否存在
            Path templateFilePath = Paths.get(TEMPLATE_BASE_PATH, templatePath);
            if (!Files.exists(templateFilePath)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("TEMPLATE_NOT_FOUND", "模板文件不存在"));
            }
            
            // 验证JOLT规范文件是否存在
            Path joltSpecPath = Paths.get(JOLT_SPEC_BASE_PATH, joltSpec);
            if (!Files.exists(joltSpecPath)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("JOLT_SPEC_NOT_FOUND", "JOLT规范文件不存在"));
            }
            
            // 设置关联关系
            TEMPLATE_JOLT_RELATIONS.put(templatePath, joltSpec);
            
            Map<String, Object> relation = new HashMap<>();
            relation.put("templatePath", templatePath);
            relation.put("joltSpec", joltSpec);
            
            logger.info("✅ [关联设置] 设置成功: {} -> {}", templatePath, joltSpec);
            return ResponseEntity.ok(ApiResponse.ok(relation));
            
        } catch (Exception e) {
            logger.error("❌ [关联设置] 设置失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("SET_RELATION_FAILED", e.getMessage()));
        }
    }

    /**
     * 获取模板关联的JOLT规范
     */
    @GetMapping("/{templatePath}/jolt-spec")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplateJoltSpec(@PathVariable String templatePath) {
        logger.info("🔍 [关联查询] 查询模板 {} 的JOLT规范", templatePath);
        
        try {
            String joltSpec = TEMPLATE_JOLT_RELATIONS.get(templatePath);
            
            if (joltSpec == null) {
                logger.warn("⚠️ [关联查询] 模板 {} 未关联JOLT规范", templatePath);
                return ResponseEntity.ok(ApiResponse.ok(new HashMap<>()));
            }
            
            Map<String, Object> relation = new HashMap<>();
            relation.put("templatePath", templatePath);
            relation.put("joltSpec", joltSpec);
            
            logger.info("✅ [关联查询] 查询成功: {} -> {}", templatePath, joltSpec);
            return ResponseEntity.ok(ApiResponse.ok(relation));
            
        } catch (Exception e) {
            logger.error("❌ [关联查询] 查询失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("QUERY_RELATION_FAILED", e.getMessage()));
        }
    }

    /**
     * 删除模板的JOLT规范关联
     */
    @DeleteMapping("/{templatePath}/jolt-spec")
    public ResponseEntity<ApiResponse<Void>> removeTemplateJoltSpec(@PathVariable String templatePath) {
        logger.info("🔗 [关联删除] 删除模板 {} 的JOLT规范关联", templatePath);
        
        try {
            String removed = TEMPLATE_JOLT_RELATIONS.remove(templatePath);
            
            if (removed == null) {
                logger.warn("⚠️ [关联删除] 模板 {} 未关联JOLT规范", templatePath);
                return ResponseEntity.ok(ApiResponse.ok(null));
            }
            
            logger.info("✅ [关联删除] 删除成功: {} -> {}", templatePath, removed);
            return ResponseEntity.ok(ApiResponse.ok(null));
            
        } catch (Exception e) {
            logger.error("❌ [关联删除] 删除失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("REMOVE_RELATION_FAILED", e.getMessage()));
        }
    }

    /**
     * 获取未关联JOLT规范的模板列表
     */
    @GetMapping("/unassociated-templates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnassociatedTemplates() {
        logger.info("📋 [未关联模板] 获取未关联JOLT规范的模板列表");
        
        try {
            List<Map<String, Object>> templates = new ArrayList<>();
            Set<String> associatedTemplates = TEMPLATE_JOLT_RELATIONS.keySet();
            
            // 获取所有模板文件
            Path basePath = Paths.get(TEMPLATE_BASE_PATH);
            if (Files.exists(basePath)) {
                try (Stream<Path> categoryPaths = Files.list(basePath)) {
                    List<Path> categoryDirs = categoryPaths
                        .filter(Files::isDirectory)
                        .collect(Collectors.toList());
                    
                    for (Path categoryDir : categoryDirs) {
                        String categoryName = categoryDir.getFileName().toString();
                        
                        try (Stream<Path> files = Files.list(categoryDir)) {
                            List<Path> jsonFiles = files
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".json"))
                                .collect(Collectors.toList());
                            
                            for (Path file : jsonFiles) {
                                String relativePath = categoryName + "/" + file.getFileName().toString();
                                
                                // 如果未关联，则添加到列表
                                if (!associatedTemplates.contains(relativePath)) {
                                    Map<String, Object> fileInfo = new HashMap<>();
                                    fileInfo.put("path", relativePath);
                                    fileInfo.put("category", categoryName);
                                    fileInfo.put("name", file.getFileName().toString());
                                    templates.add(fileInfo);
                                }
                            }
                        }
                    }
                }
            }
            
            logger.info("✅ [未关联模板] 获取成功，共 {} 个未关联模板", templates.size());
            return ResponseEntity.ok(ApiResponse.ok(templates));
            
        } catch (Exception e) {
            logger.error("❌ [未关联模板] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * 获取未被任何模板关联的JOLT规范列表
     */
    @GetMapping("/unassociated-jolt-specs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnassociatedJoltSpecs() {
        logger.info("📋 [未关联规范] 获取未被任何模板关联的JOLT规范列表");
        
        try {
            List<Map<String, Object>> specs = new ArrayList<>();
            Set<String> associatedSpecs = new HashSet<>(TEMPLATE_JOLT_RELATIONS.values());
            
            // 获取所有JOLT规范文件
            Path basePath = Paths.get(JOLT_SPEC_BASE_PATH);
            if (Files.exists(basePath)) {
                try (Stream<Path> files = Files.list(basePath)) {
                    List<Path> jsonFiles = files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());
                    
                    for (Path file : jsonFiles) {
                        String fileName = file.getFileName().toString();
                        
                        // 如果未被关联，则添加到列表
                        if (!associatedSpecs.contains(fileName)) {
                            Map<String, Object> specInfo = new HashMap<>();
                            specInfo.put("name", fileName);
                            specInfo.put("path", fileName);
                            specs.add(specInfo);
                        }
                    }
                }
            }
            
            logger.info("✅ [未关联规范] 获取成功，共 {} 个未关联规范", specs.size());
            return ResponseEntity.ok(ApiResponse.ok(specs));
            
        } catch (Exception e) {
            logger.error("❌ [未关联规范] 获取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", e.getMessage()));
        }
    }
}