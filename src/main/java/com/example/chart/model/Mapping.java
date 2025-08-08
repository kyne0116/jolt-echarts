package com.example.chart.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 映射定义（简化版，可直接按JSON对接前端）
 */
public class Mapping {
    private String chartId;
    private String mappingVersion;
    private String templateVersion;
    private String specVersion;
    private String status; // active/draft/deprecated
    private String updatedBy;
    private long updatedAt;
    private List<Item> items = new ArrayList<>();

    public static class Item {
        private String placeholder; // ${series_data_1}
        private String dataType;    // string|number|array|object
        private Source source = new Source();
        private Transform transform = new Transform();
        private Pivot pivot = new Pivot();
        private String notes;
        public String getPlaceholder() { return placeholder; }
        public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public Source getSource() { return source; }
        public void setSource(Source source) { this.source = source; }
        public Transform getTransform() { return transform; }
        public void setTransform(Transform transform) { this.transform = transform; }
        public Pivot getPivot() { return pivot; }
        public void setPivot(Pivot pivot) { this.pivot = pivot; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Source {
        private String table;
        private String column;
        public String getTable() { return table; }
        public void setTable(String table) { this.table = table; }
        public String getColumn() { return column; }
        public void setColumn(String column) { this.column = column; }
    }

    public static class Transform {
        private String aggregation; // sum/avg/min/max/count/null
        private List<String> groupBy = new ArrayList<>();
        private List<Filter> filters = new ArrayList<>();
        private List<Order> orderBy = new ArrayList<>();
        private Integer limit;
        private String nullHandling; // skip/zero/fill
        public String getAggregation() { return aggregation; }
        public void setAggregation(String aggregation) { this.aggregation = aggregation; }
        public List<String> getGroupBy() { return groupBy; }
        public void setGroupBy(List<String> groupBy) { this.groupBy = groupBy; }
        public List<Filter> getFilters() { return filters; }
        public void setFilters(List<Filter> filters) { this.filters = filters; }
        public List<Order> getOrderBy() { return orderBy; }
        public void setOrderBy(List<Order> orderBy) { this.orderBy = orderBy; }
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
        public String getNullHandling() { return nullHandling; }
        public void setNullHandling(String nullHandling) { this.nullHandling = nullHandling; }
    }

    public static class Filter {
        private String field;
        private String op; // eq/neq/gt/gte/lt/lte/in/notIn/like
        private Object value;
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getOp() { return op; }
        public void setOp(String op) { this.op = op; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class Order {
        private String field;
        private String dir; // asc/desc
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }

    public static class Pivot {
        private String categoryKey; // day_name
        private String seriesKey;   // channel_name
        public String getCategoryKey() { return categoryKey; }
        public void setCategoryKey(String categoryKey) { this.categoryKey = categoryKey; }
        public String getSeriesKey() { return seriesKey; }
        public void setSeriesKey(String seriesKey) { this.seriesKey = seriesKey; }
    }

    public String getChartId() { return chartId; }
    public void setChartId(String chartId) { this.chartId = chartId; }
    public String getMappingVersion() { return mappingVersion; }
    public void setMappingVersion(String mappingVersion) { this.mappingVersion = mappingVersion; }
    public String getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }
    public String getSpecVersion() { return specVersion; }
    public void setSpecVersion(String specVersion) { this.specVersion = specVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}

