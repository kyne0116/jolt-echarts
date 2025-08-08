package com.example.chart.model;

/**
 * 每个 chartId 的激活版本注册表
 */
public class ChartRegistry {
    private String chartId;
    private String activeTemplateVersion;
    private String activeSpecVersion;
    private String activeCatalogVersion; // 目前目录为计算产物，可留空
    private String activeMappingVersion;

    public String getChartId() { return chartId; }
    public void setChartId(String chartId) { this.chartId = chartId; }
    public String getActiveTemplateVersion() { return activeTemplateVersion; }
    public void setActiveTemplateVersion(String activeTemplateVersion) { this.activeTemplateVersion = activeTemplateVersion; }
    public String getActiveSpecVersion() { return activeSpecVersion; }
    public void setActiveSpecVersion(String activeSpecVersion) { this.activeSpecVersion = activeSpecVersion; }
    public String getActiveCatalogVersion() { return activeCatalogVersion; }
    public void setActiveCatalogVersion(String activeCatalogVersion) { this.activeCatalogVersion = activeCatalogVersion; }
    public String getActiveMappingVersion() { return activeMappingVersion; }
    public void setActiveMappingVersion(String activeMappingVersion) { this.activeMappingVersion = activeMappingVersion; }
}

