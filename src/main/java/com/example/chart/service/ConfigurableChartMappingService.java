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
 * 可配置的图表映射服务
 * 解决硬编码问题，从配置文件加载映射关系
 */
@Service
public class ConfigurableChartMappingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurableChartMappingService.class);
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    // 缓存配置数据
    private Map<String, String> pathToChartIdMapping = new HashMap<>();
    private Map<String, String> chartIdToPathMapping = new HashMap<>();
    private Map<String, List<String>> categoriesMapping = new HashMap<>();
    private Map<String, Object> chartSpecificConfigs = new HashMap<>();
    private Map<String, Object> preprocessingRules = new HashMap<>();
    
    @PostConstruct
    public void loadConfigurations() {
        try {
            loadChartMappings();
            logger.info("✅ 图表映射配置加载成功");
        } catch (Exception e) {
            logger.error("❌ 图表映射配置加载失败", e);
            // 使用默认配置作为回退
            loadDefaultMappings();
        }
    }
    
    /**
     * 从配置文件加载图表映射
     */
    private void loadChartMappings() throws IOException {
        ClassPathResource resource = new ClassPathResource("config/chart-mappings.yml");
        if (!resource.exists()) {
            throw new IOException("配置文件不存在: config/chart-mappings.yml");
        }
        
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> config = yamlMapper.readValue(inputStream, Map.class);
            
            // 加载路径映射
            @SuppressWarnings("unchecked")
            Map<String, String> pathToChartId = (Map<String, String>) config.get("path-to-chart-id");
            if (pathToChartId != null) {
                this.pathToChartIdMapping = new HashMap<>(pathToChartId);
                
                // 构建反向映射
                pathToChartId.forEach((path, chartId) -> 
                    chartIdToPathMapping.put(chartId, path));
            }
            
            // 加载分类映射
            @SuppressWarnings("unchecked")
            Map<String, List<String>> categories = (Map<String, List<String>>) config.get("categories");
            if (categories != null) {
                this.categoriesMapping = new HashMap<>(categories);
            }
            
            // 加载图表特定配置
            @SuppressWarnings("unchecked")
            Map<String, Object> chartConfigs = (Map<String, Object>) config.get("chart-specific-configs");
            if (chartConfigs != null) {
                this.chartSpecificConfigs = new HashMap<>(chartConfigs);
            }
            
            // 加载预处理规则
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = (Map<String, Object>) config.get("preprocessing-rules");
            if (rules != null) {
                this.preprocessingRules = new HashMap<>(rules);
            }
            
            logger.info("📋 加载了 {} 个路径映射", pathToChartIdMapping.size());
            logger.info("📋 加载了 {} 个图表分类", categoriesMapping.size());
            logger.info("📋 加载了 {} 个图表配置", chartSpecificConfigs.size());
        }
    }
    
    /**
     * 加载默认映射（回退方案）
     */
    private void loadDefaultMappings() {
        logger.warn("⚠️ 使用默认映射配置");
        
        // 基本的默认映射
        pathToChartIdMapping.put("折线图/基础折线图.json", "basic_line_chart");
        pathToChartIdMapping.put("折线图/折线图堆叠.json", "stacked_line_chart");
        pathToChartIdMapping.put("柱状图/基础柱状图.json", "basic_bar_chart");
        
        // 构建反向映射
        pathToChartIdMapping.forEach((path, chartId) -> 
            chartIdToPathMapping.put(chartId, path));
    }
    
    /**
     * 获取路径到图表ID的映射
     */
    public Map<String, String> getPathToChartIdMapping() {
        return new HashMap<>(pathToChartIdMapping);
    }
    
    /**
     * 获取图表ID到路径的映射
     */
    public Map<String, String> getChartIdToPathMapping() {
        return new HashMap<>(chartIdToPathMapping);
    }
    
    /**
     * 获取分类映射
     */
    public Map<String, List<String>> getCategoriesMapping() {
        return new HashMap<>(categoriesMapping);
    }
    
    /**
     * 获取图表特定配置
     */
    public Map<String, Object> getChartSpecificConfig(String chartId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) chartSpecificConfigs.get(chartId);
        return config != null ? new HashMap<>(config) : new HashMap<>();
    }
    
    /**
     * 获取预处理规则
     */
    public Map<String, Object> getPreprocessingRules() {
        return new HashMap<>(preprocessingRules);
    }
    
    /**
     * 根据文件路径获取图表ID
     */
    public String getChartIdByFilePath(String filePath) {
        return pathToChartIdMapping.get(filePath);
    }
    
    /**
     * 根据图表ID获取文件路径
     */
    public String getFilePathByChartId(String chartId) {
        return chartIdToPathMapping.get(chartId);
    }
    
    /**
     * 获取所有图表类型数量
     */
    public int getTotalChartCount() {
        return pathToChartIdMapping.size();
    }
    
    /**
     * 重新加载配置（用于运行时更新）
     */
    public void reloadConfigurations() {
        logger.info("🔄 重新加载图表映射配置");
        loadConfigurations();
    }
}
