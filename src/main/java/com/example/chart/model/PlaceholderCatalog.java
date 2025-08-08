package com.example.chart.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 占位符目录：由阶段一产物自动生成，用于驱动UI与后端校验
 */
public class PlaceholderCatalog {
    private String chartId;
    private String templateVersion;
    private String specVersion;
    private long generatedAt;
    private String checksum;
    private List<Item> placeholders = new ArrayList<>();

    public static class Item {
        private String name;       // ${chart_title}
        private String variable;   // chart_title
        private String type;       // string | number | array | object
        private boolean required;  // 默认true
        private String group;      // title/xAxis/series/legend/...
        private String targetPath; // $.title.text
        private String description;
        private Object example;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVariable() { return variable; }
        public void setVariable(String variable) { this.variable = variable; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public String getTargetPath() { return targetPath; }
        public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Object getExample() { return example; }
        public void setExample(Object example) { this.example = example; }
    }

    public String getChartId() { return chartId; }
    public void setChartId(String chartId) { this.chartId = chartId; }
    public String getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }
    public String getSpecVersion() { return specVersion; }
    public void setSpecVersion(String specVersion) { this.specVersion = specVersion; }
    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public List<Item> getPlaceholders() { return placeholders; }
    public void setPlaceholders(List<Item> placeholders) { this.placeholders = placeholders; }
}

