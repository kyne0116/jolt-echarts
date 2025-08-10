package com.example.chart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可配置的Jolt规范服务
 * 解决硬编码问题，从配置文件加载Jolt规范映射
 */
@Service
public class ConfigurableJoltSpecService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurableJoltSpecService.class);
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    // 缓存配置数据
    private Map<String, String> chartToJoltSpecMapping = new HashMap<>();
    private Map<String, String> joltSpecDescriptions = new HashMap<>();
    private String defaultJoltSpec = "line-chart-placeholder.json";
    private String fallbackMessage = "未找到对应的Jolt规范，使用默认规范";
    
    @PostConstruct
    public void loadConfigurations() {
        try {
            loadJoltSpecMappings();
            logger.info("✅ Jolt规范映射配置加载成功");
        } catch (Exception e) {
            logger.error("❌ Jolt规范映射配置加载失败", e);
            // 使用默认配置作为回退
            loadDefaultJoltSpecMappings();
        }
    }
    
    /**
     * 从配置文件加载Jolt规范映射
     */
    private void loadJoltSpecMappings() throws IOException {
        ClassPathResource resource = new ClassPathResource("config/jolt-spec-mappings.yml");
        if (!resource.exists()) {
            throw new IOException("配置文件不存在: config/jolt-spec-mappings.yml");
        }
        
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> config = yamlMapper.readValue(inputStream, Map.class);
            
            // 加载图表到Jolt规范的映射
            @SuppressWarnings("unchecked")
            Map<String, String> chartToJoltSpec = (Map<String, String>) config.get("chart-to-jolt-spec");
            if (chartToJoltSpec != null) {
                this.chartToJoltSpecMapping = new HashMap<>(chartToJoltSpec);
            }
            
            // 加载Jolt规范描述
            @SuppressWarnings("unchecked")
            Map<String, String> descriptions = (Map<String, String>) config.get("jolt-spec-descriptions");
            if (descriptions != null) {
                this.joltSpecDescriptions = new HashMap<>(descriptions);
            }
            
            // 加载默认配置
            @SuppressWarnings("unchecked")
            Map<String, String> defaults = (Map<String, String>) config.get("defaults");
            if (defaults != null) {
                this.defaultJoltSpec = defaults.getOrDefault("jolt-spec-file", "line-chart-placeholder.json");
                this.fallbackMessage = defaults.getOrDefault("fallback-message", "未找到对应的Jolt规范，使用默认规范");
            }
            
            logger.info("📋 加载了 {} 个Jolt规范映射", chartToJoltSpecMapping.size());
            logger.info("📋 默认Jolt规范: {}", defaultJoltSpec);
        }
    }
    
    /**
     * 加载默认Jolt规范映射（回退方案）
     */
    private void loadDefaultJoltSpecMappings() {
        logger.warn("⚠️ 使用默认Jolt规范映射配置");
        
        // 基本的默认映射
        chartToJoltSpecMapping.put("basic_line_chart", "line-chart-placeholder.json");
        chartToJoltSpecMapping.put("stacked_line_chart", "line-chart-stacked.json");
        chartToJoltSpecMapping.put("basic_bar_chart", "bar-chart-placeholder.json");
        chartToJoltSpecMapping.put("basic_pie_chart", "pie-chart-placeholder.json");
        chartToJoltSpecMapping.put("basic_radar_chart", "radar-chart-placeholder.json");
        chartToJoltSpecMapping.put("basic_gauge_chart", "gauge-chart-placeholder.json");
        
        defaultJoltSpec = "line-chart-placeholder.json";
        fallbackMessage = "使用默认的折线图规范";
    }
    
    /**
     * 根据图表类型ID获取对应的Jolt规范文件名
     */
    public String getJoltSpecFileByChartId(String chartId) {
        String specFile = chartToJoltSpecMapping.get(chartId);
        
        if (specFile == null) {
            logger.warn("⚠️ 未找到图表类型 {} 对应的Jolt规范，使用默认规范: {}", chartId, defaultJoltSpec);
            return defaultJoltSpec;
        }
        
        logger.info("📋 图表类型 {} 使用JOLT规范: {}", chartId, specFile);
        return specFile;
    }
    
    /**
     * 获取Jolt规范文件的描述
     */
    public String getJoltSpecDescription(String specFile) {
        return joltSpecDescriptions.getOrDefault(specFile, "未知规范文件");
    }
    
    /**
     * 获取所有支持的图表类型
     */
    public Map<String, String> getAllChartToJoltSpecMappings() {
        return new HashMap<>(chartToJoltSpecMapping);
    }
    
    /**
     * 检查图表类型是否支持
     */
    public boolean isChartTypeSupported(String chartId) {
        return chartToJoltSpecMapping.containsKey(chartId);
    }
    
    /**
     * 获取默认Jolt规范文件名
     */
    public String getDefaultJoltSpec() {
        return defaultJoltSpec;
    }
    
    /**
     * 获取回退消息
     */
    public String getFallbackMessage() {
        return fallbackMessage;
    }
    
    /**
     * 验证Jolt规范文件是否存在
     */
    public boolean validateJoltSpecFile(String specFile) {
        try {
            ClassPathResource resource = new ClassPathResource("jolt-specs/" + specFile);
            return resource.exists();
        } catch (Exception e) {
            logger.error("验证Jolt规范文件失败: {}", specFile, e);
            return false;
        }
    }
    
    /**
     * 获取所有Jolt规范文件的验证状态
     */
    public Map<String, Boolean> validateAllJoltSpecFiles() {
        Map<String, Boolean> validationResults = new HashMap<>();
        
        for (String specFile : chartToJoltSpecMapping.values()) {
            validationResults.put(specFile, validateJoltSpecFile(specFile));
        }
        
        // 验证默认规范文件
        validationResults.put(defaultJoltSpec, validateJoltSpecFile(defaultJoltSpec));
        
        return validationResults;
    }
    
    /**
     * 重新加载配置（用于运行时更新）
     */
    public void reloadConfigurations() {
        logger.info("🔄 重新加载Jolt规范映射配置");
        loadConfigurations();
    }
    
    /**
     * 获取配置统计信息
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMappings", chartToJoltSpecMapping.size());
        stats.put("defaultSpec", defaultJoltSpec);
        stats.put("supportedChartTypes", chartToJoltSpecMapping.keySet());
        stats.put("uniqueSpecFiles", chartToJoltSpecMapping.values().stream().distinct().count());
        stats.put("configSource", "YAML配置文件");
        stats.put("lastUpdated", System.currentTimeMillis());
        
        return stats;
    }
}
