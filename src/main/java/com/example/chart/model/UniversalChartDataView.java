package com.example.chart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一图表数据视图对象
 * 包含40个字段，覆盖所有图表类型的数据需求
 * 
 * @author Chart System
 * @version 1.0
 */
public class UniversalChartDataView {
    
    // ==================== 基础信息字段 (1-8) ====================
    
    /**
     * 数据记录ID
     */
    @JsonProperty("id")
    private Long id;
    
    /**
     * 图表标题
     */
    @JsonProperty("title")
    private String title;
    
    /**
     * 图表类型
     */
    @JsonProperty("chart_type")
    private String chartType;
    
    /**
     * 图表主题
     */
    @JsonProperty("theme")
    private String theme;
    
    /**
     * 图表描述
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * 数据源标识
     */
    @JsonProperty("data_source")
    private String dataSource;
    
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
    
    // ==================== 时间维度字段 (9-16) ====================
    
    /**
     * 日期
     */
    @JsonProperty("date")
    private LocalDate date;
    
    /**
     * 日期名称（如：周一、周二）
     */
    @JsonProperty("day_name")
    private String dayName;
    
    /**
     * 月份
     */
    @JsonProperty("month")
    private Integer month;
    
    /**
     * 月份名称
     */
    @JsonProperty("month_name")
    private String monthName;
    
    /**
     * 年份
     */
    @JsonProperty("year")
    private Integer year;
    
    /**
     * 季度
     */
    @JsonProperty("quarter")
    private Integer quarter;
    
    /**
     * 周数
     */
    @JsonProperty("week_number")
    private Integer weekNumber;
    
    /**
     * 时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;
    
    // ==================== 分类数据字段 (17-24) ====================
    
    /**
     * 主分类
     */
    @JsonProperty("category")
    private String category;
    
    /**
     * 子分类
     */
    @JsonProperty("sub_category")
    private String subCategory;
    
    /**
     * 渠道名称
     */
    @JsonProperty("channel_name")
    private String channelName;
    
    /**
     * 渠道类型
     */
    @JsonProperty("channel_type")
    private String channelType;
    
    /**
     * 产品名称
     */
    @JsonProperty("product_name")
    private String productName;
    
    /**
     * 产品类型
     */
    @JsonProperty("product_type")
    private String productType;
    
    /**
     * 地区
     */
    @JsonProperty("region")
    private String region;
    
    /**
     * 部门
     */
    @JsonProperty("department")
    private String department;
    
    // ==================== 数值字段 (25-32) ====================
    
    /**
     * 主要数值
     */
    @JsonProperty("value")
    private Double value;
    
    /**
     * 转换数量
     */
    @JsonProperty("conversion_count")
    private Integer conversionCount;
    
    /**
     * 点击数量
     */
    @JsonProperty("click_count")
    private Integer clickCount;
    
    /**
     * 浏览数量
     */
    @JsonProperty("view_count")
    private Integer viewCount;
    
    /**
     * 百分比
     */
    @JsonProperty("percentage")
    private Double percentage;
    
    /**
     * 比率
     */
    @JsonProperty("ratio")
    private Double ratio;
    
    /**
     * 金额
     */
    @JsonProperty("amount")
    private Double amount;
    
    /**
     * 数量
     */
    @JsonProperty("quantity")
    private Integer quantity;
    
    // ==================== 配置字段 (33-40) ====================
    
    /**
     * 颜色配置
     */
    @JsonProperty("color")
    private String color;
    
    /**
     * 样式配置
     */
    @JsonProperty("style")
    private String style;
    
    /**
     * 半径配置
     */
    @JsonProperty("radius")
    private String radius;
    
    /**
     * 中心位置配置
     */
    @JsonProperty("center")
    private String center;
    
    /**
     * 堆叠组
     */
    @JsonProperty("stack_group")
    private String stackGroup;
    
    /**
     * 平滑样式
     */
    @JsonProperty("smooth_style")
    private Boolean smoothStyle;
    
    /**
     * 边界间隙
     */
    @JsonProperty("boundary_gap")
    private Boolean boundaryGap;
    
    /**
     * 扩展配置（JSON格式）
     */
    @JsonProperty("extra_config")
    private String extraConfig;
    
    // ==================== 构造函数 ====================
    
    public UniversalChartDataView() {
    }
    
    public UniversalChartDataView(Long id, String title, String chartType) {
        this.id = id;
        this.title = title;
        this.chartType = chartType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }
    
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    
    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }
    
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    
    public Integer getConversionCount() { return conversionCount; }
    public void setConversionCount(Integer conversionCount) { this.conversionCount = conversionCount; }
    
    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    
    public Double getRatio() { return ratio; }
    public void setRatio(Double ratio) { this.ratio = ratio; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    
    public String getRadius() { return radius; }
    public void setRadius(String radius) { this.radius = radius; }
    
    public String getCenter() { return center; }
    public void setCenter(String center) { this.center = center; }
    
    public String getStackGroup() { return stackGroup; }
    public void setStackGroup(String stackGroup) { this.stackGroup = stackGroup; }
    
    public Boolean getSmoothStyle() { return smoothStyle; }
    public void setSmoothStyle(Boolean smoothStyle) { this.smoothStyle = smoothStyle; }
    
    public Boolean getBoundaryGap() { return boundaryGap; }
    public void setBoundaryGap(Boolean boundaryGap) { this.boundaryGap = boundaryGap; }
    
    public String getExtraConfig() { return extraConfig; }
    public void setExtraConfig(String extraConfig) { this.extraConfig = extraConfig; }
}
