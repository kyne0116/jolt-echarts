package com.example.chart.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 统一图表数据视图对象
 * 包含12个核心业务字段，专注于纯业务数据模拟
 * 移除所有图表配置相关字段，便于后续映射关系配置测试
 *
 * @author Chart System
 * @version 2.0 - 简化业务模型
 */
public class UniversalChartDataView {

    // ==================== 核心业务字段 (12个) ====================

    /**
     * 主键ID - 唯一标识
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 年份 - 用于时间维度分析
     * 示例: "2024", "2023"
     */
    @JsonProperty("year")
    private String year;

    /**
     * 月份 - 用于月度趋势分析
     * 示例: "01", "02", "12"
     */
    @JsonProperty("month")
    private String month;

    /**
     * 具体日期 - 用于日期维度分析
     * 示例: "2024-01-15"
     */
    @JsonProperty("date")
    private String date;

    /**
     * 业务分类 - 如产品类别、部门等
     * 示例: "电子产品", "服装", "食品"
     */
    @JsonProperty("category")
    private String category;

    /**
     * 销售渠道 - 用于渠道分析
     * 示例: "线上", "线下", "移动端", "电话销售"
     */
    @JsonProperty("channel")
    private String channel;

    /**
     * 具体产品名称
     * 示例: "iPhone 15", "MacBook Pro", "iPad"
     */
    @JsonProperty("product")
    private String product;

    /**
     * 地理区域 - 用于地域分析
     * 示例: "华北", "华东", "华南", "华中"
     */
    @JsonProperty("region")
    private String region;

    /**
     * 金额数据 - 核心财务指标
     * 示例: 12500.50, 8900.00
     */
    @JsonProperty("amount")
    private Double amount;

    /**
     * 数量数据 - 销量等计数指标
     * 示例: 100, 250, 50
     */
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * 百分比数据 - 占比分析
     * 示例: 25.5, 60.2, 15.8
     */
    @JsonProperty("percentage")
    private Double percentage;

    /**
     * 销售人员 - 用于人员绩效分析
     * 示例: "张三", "李四", "王五"
     */
    @JsonProperty("salesman")
    private String salesman;

    // ==================== 系统字段 ====================

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // ==================== 构造函数 ====================

    public UniversalChartDataView() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Getter/Setter 方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getSalesman() {
        return salesman;
    }

    public void setSalesman(String salesman) {
        this.salesman = salesman;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}