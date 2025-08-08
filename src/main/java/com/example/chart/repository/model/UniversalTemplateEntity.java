package com.example.chart.repository.model;

import java.util.Map;

/**
 * 通用模板实体（模拟ORM实体）
 */
public class UniversalTemplateEntity {
    private String chartId;       // 如 stacked_line_chart
    private String name;          // 模板名称
    private String description;   // 描述
    private Map<String, Object> template; // 带占位符的通用JSON模板

    public UniversalTemplateEntity() {}

    public UniversalTemplateEntity(String chartId, String name, String description, Map<String, Object> template) {
        this.chartId = chartId;
        this.name = name;
        this.description = description;
        this.template = template;
    }

    public String getChartId() {
        return chartId;
    }

    public void setChartId(String chartId) {
        this.chartId = chartId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getTemplate() {
        return template;
    }

    public void setTemplate(Map<String, Object> template) {
        this.template = template;
    }
}

