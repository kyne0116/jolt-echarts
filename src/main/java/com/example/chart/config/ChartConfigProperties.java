package com.example.chart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 图表配置属性类
 * 解决硬编码问题，实现配置化管理
 */
@Component
@ConfigurationProperties(prefix = "chart")
public class ChartConfigProperties {
    
    /**
     * 模板配置
     */
    private Template template = new Template();
    
    /**
     * 通用配置
     */
    private Config config = new Config();
    
    public static class Template {
        /**
         * 系列配置
         */
        private Series series = new Series();
        
        public static class Series {
            private Cartesian cartesian = new Cartesian();
            private Pie pie = new Pie();
            private Radar radar = new Radar();
            private Gauge gauge = new Gauge();
            
            public static class Cartesian {
                private int defaultCount = 5;
                private int maxCount = 10;
                
                public int getDefaultCount() { return defaultCount; }
                public void setDefaultCount(int defaultCount) { this.defaultCount = defaultCount; }
                public int getMaxCount() { return maxCount; }
                public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
            }
            
            public static class Pie {
                private int defaultCount = 1;
                private int maxCount = 1;
                
                public int getDefaultCount() { return defaultCount; }
                public void setDefaultCount(int defaultCount) { this.defaultCount = defaultCount; }
                public int getMaxCount() { return maxCount; }
                public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
            }
            
            public static class Radar {
                private int defaultCount = 3;
                private int maxCount = 5;
                
                public int getDefaultCount() { return defaultCount; }
                public void setDefaultCount(int defaultCount) { this.defaultCount = defaultCount; }
                public int getMaxCount() { return maxCount; }
                public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
            }
            
            public static class Gauge {
                private int defaultCount = 1;
                private int maxCount = 1;
                
                public int getDefaultCount() { return defaultCount; }
                public void setDefaultCount(int defaultCount) { this.defaultCount = defaultCount; }
                public int getMaxCount() { return maxCount; }
                public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
            }
            
            public Cartesian getCartesian() { return cartesian; }
            public void setCartesian(Cartesian cartesian) { this.cartesian = cartesian; }
            public Pie getPie() { return pie; }
            public void setPie(Pie pie) { this.pie = pie; }
            public Radar getRadar() { return radar; }
            public void setRadar(Radar radar) { this.radar = radar; }
            public Gauge getGauge() { return gauge; }
            public void setGauge(Gauge gauge) { this.gauge = gauge; }
        }
        
        public Series getSeries() { return series; }
        public void setSeries(Series series) { this.series = series; }
    }
    
    public static class Config {
        private String mappingFile = "classpath:config/chart-mappings.yml";
        private String joltSpecMappingFile = "classpath:config/jolt-spec-mappings.yml";
        private String defaultJoltSpec = "line-chart-placeholder.json";
        private String defaultTemplateType = "CARTESIAN";
        
        public String getMappingFile() { return mappingFile; }
        public void setMappingFile(String mappingFile) { this.mappingFile = mappingFile; }
        public String getJoltSpecMappingFile() { return joltSpecMappingFile; }
        public void setJoltSpecMappingFile(String joltSpecMappingFile) { this.joltSpecMappingFile = joltSpecMappingFile; }
        public String getDefaultJoltSpec() { return defaultJoltSpec; }
        public void setDefaultJoltSpec(String defaultJoltSpec) { this.defaultJoltSpec = defaultJoltSpec; }
        public String getDefaultTemplateType() { return defaultTemplateType; }
        public void setDefaultTemplateType(String defaultTemplateType) { this.defaultTemplateType = defaultTemplateType; }
    }
    
    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }
    public Config getConfig() { return config; }
    public void setConfig(Config config) { this.config = config; }
    
    /**
     * 根据模板类型获取默认系列数量
     */
    public int getDefaultSeriesCount(String templateType) {
        switch (templateType.toUpperCase()) {
            case "CARTESIAN":
                return template.series.cartesian.defaultCount;
            case "PIE":
                return template.series.pie.defaultCount;
            case "RADAR":
                return template.series.radar.defaultCount;
            case "GAUGE":
                return template.series.gauge.defaultCount;
            default:
                return template.series.cartesian.defaultCount;
        }
    }
    
    /**
     * 根据模板类型获取最大系列数量
     */
    public int getMaxSeriesCount(String templateType) {
        switch (templateType.toUpperCase()) {
            case "CARTESIAN":
                return template.series.cartesian.maxCount;
            case "PIE":
                return template.series.pie.maxCount;
            case "RADAR":
                return template.series.radar.maxCount;
            case "GAUGE":
                return template.series.gauge.maxCount;
            default:
                return template.series.cartesian.maxCount;
        }
    }
}
