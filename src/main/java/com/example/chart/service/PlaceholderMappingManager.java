package com.example.chart.service;

import com.example.chart.model.UniversalChartDataView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 占位符映射管理器
 * 负责虚拟数据库与转换模块之间的动态映射关系管理
 */
@Service
public class PlaceholderMappingManager {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    @Autowired
    private SimpleUniversalDataCrudService dataService;
    
    @Autowired
    private PlaceholderManager placeholderManager;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 映射关系存储：chartId -> (placeholder -> fieldMapping)
    private final Map<String, Map<String, FieldMapping>> mappingStore = new ConcurrentHashMap<>();
    
    /**
     * 字段映射配置
     */
    public static class FieldMapping {
        private String fieldName;          // 数据库字段名
        private String dataType;           // 数据类型
        private String aggregationType;    // 聚合类型：sum, avg, count, max, min
        private Map<String, Object> filters; // 过滤条件
        private String transformExpression; // 转换表达式
        
        public FieldMapping() {}
        
        public FieldMapping(String fieldName, String dataType) {
            this.fieldName = fieldName;
            this.dataType = dataType;
            this.aggregationType = "none";
            this.filters = new HashMap<>();
        }
        
        // Getters and Setters
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public String getAggregationType() { return aggregationType; }
        public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }
        
        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
        
        public String getTransformExpression() { return transformExpression; }
        public void setTransformExpression(String transformExpression) { this.transformExpression = transformExpression; }
    }
    
    /**
     * 映射结果
     */
    public static class MappingResult {
        private boolean success;
        private String message;
        private Map<String, Object> data;
        private List<String> unmappedPlaceholders;
        
        public MappingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.data = new HashMap<>();
            this.unmappedPlaceholders = new ArrayList<>();
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        
        public List<String> getUnmappedPlaceholders() { return unmappedPlaceholders; }
        public void setUnmappedPlaceholders(List<String> unmappedPlaceholders) { this.unmappedPlaceholders = unmappedPlaceholders; }
    }
    
    /**
     * 配置图表的占位符映射关系
     */
    public void configureMappings(String chartId, Map<String, FieldMapping> mappings) {
        mappingStore.put(chartId, new HashMap<>(mappings));
        System.out.println("✅ [映射管理] 配置图表 " + chartId + " 的映射关系，共 " + mappings.size() + " 个映射");
    }
    
    /**
     * 获取图表的映射配置
     */
    public Map<String, FieldMapping> getMappings(String chartId) {
        return mappingStore.getOrDefault(chartId, new HashMap<>());
    }
    
    /**
     * 根据映射关系动态查询数据并替换占位符
     */
    public MappingResult executeMapping(String chartId, Object templateWithPlaceholders) {
        try {
            // 1. 提取模板中的占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(templateWithPlaceholders);
            System.out.println("🔍 [映射执行] 发现占位符: " + placeholders);
            
            // 2. 获取映射配置
            Map<String, FieldMapping> mappings = getMappings(chartId);
            if (mappings.isEmpty()) {
                return new MappingResult(false, "图表 " + chartId + " 未配置映射关系");
            }
            
            // 3. 执行数据查询和映射
            Map<String, Object> mappedData = new HashMap<>();
            List<String> unmappedPlaceholders = new ArrayList<>();
            
            for (String placeholder : placeholders) {
                FieldMapping mapping = mappings.get(placeholder);
                
                if (mapping != null) {
                    Object value = queryDataByMapping(mapping);
                    mappedData.put(placeholder, value);
                    System.out.println("✅ [映射执行] " + placeholder + " -> " + value);
                } else {
                    unmappedPlaceholders.add(placeholder);
                    System.out.println("⚠️ [映射执行] 未找到映射: " + placeholder);
                }
            }
            
            // 4. 替换占位符
            Object result = placeholderManager.replacePlaceholdersInJson(templateWithPlaceholders, mappedData);
            
            MappingResult mappingResult = new MappingResult(true, "映射执行成功");
            mappingResult.setData(Map.of("result", result, "mappedData", mappedData));
            mappingResult.setUnmappedPlaceholders(unmappedPlaceholders);
            
            return mappingResult;
            
        } catch (Exception e) {
            System.err.println("❌ [映射执行] 执行失败: " + e.getMessage());
            return new MappingResult(false, "映射执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据字段映射查询数据
     */
    private Object queryDataByMapping(FieldMapping mapping) {
        try {
            List<UniversalChartDataView> allData = dataService.findAll();
            
            // 应用过滤条件
            List<UniversalChartDataView> filteredData = applyFilters(allData, mapping.getFilters());
            
            // 提取字段值
            List<Object> fieldValues = extractFieldValues(filteredData, mapping.getFieldName());
            
            // 应用聚合
            return applyAggregation(fieldValues, mapping.getAggregationType(), mapping.getDataType());
            
        } catch (Exception e) {
            System.err.println("❌ [数据查询] 查询失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 应用过滤条件
     */
    private List<UniversalChartDataView> applyFilters(List<UniversalChartDataView> data, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return data;
        }
        
        return data.stream().filter(item -> {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                Object fieldValue = getFieldValue(item, filter.getKey());
                if (!Objects.equals(fieldValue, filter.getValue())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
    
    /**
     * 提取字段值
     */
    private List<Object> extractFieldValues(List<UniversalChartDataView> data, String fieldName) {
        return data.stream()
                .map(item -> getFieldValue(item, fieldName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取对象字段值（反射方式）
     */
    private Object getFieldValue(UniversalChartDataView item, String fieldName) {
        try {
            switch (fieldName.toLowerCase()) {
                case "id": return item.getId();
                case "year": return item.getYear();
                case "month": return item.getMonth();
                case "date": return item.getDate();
                case "category": return item.getCategory();
                case "channel": return item.getChannel();
                case "product": return item.getProduct();
                case "region": return item.getRegion();
                case "amount": return item.getAmount();
                case "quantity": return item.getQuantity();
                case "percentage": return item.getPercentage();
                case "salesman": return item.getSalesman();
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 应用聚合函数
     */
    private Object applyAggregation(List<Object> values, String aggregationType, String dataType) {
        if (values.isEmpty()) {
            return getDefaultValue(dataType);
        }
        
        switch (aggregationType.toLowerCase()) {
            case "sum":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .sum();
                        
            case "avg":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .average()
                        .orElse(0.0);
                        
            case "count":
                return values.size();
                
            case "max":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .max()
                        .orElse(0.0);
                        
            case "min":
                return values.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .min()
                        .orElse(0.0);
                        
            case "list":
                return values;
                
            case "none":
            default:
                return values.isEmpty() ? getDefaultValue(dataType) : values.get(0);
        }
    }
    
    /**
     * 获取数据类型的默认值
     */
    private Object getDefaultValue(String dataType) {
        switch (dataType.toLowerCase()) {
            case "number": return 0;
            case "string": return "";
            case "boolean": return false;
            case "array": return new ArrayList<>();
            case "object": return new HashMap<>();
            default: return null;
        }
    }
    
    /**
     * 生成默认映射配置
     */
    public Map<String, FieldMapping> generateDefaultMappings(String chartId, Set<String> placeholders) {
        Map<String, FieldMapping> defaultMappings = new HashMap<>();
        
        for (String placeholder : placeholders) {
            String variableName = placeholderManager.getVariableName(placeholder);
            if (variableName != null) {
                FieldMapping mapping = inferMappingFromPlaceholder(variableName);
                defaultMappings.put(placeholder, mapping);
            }
        }
        
        return defaultMappings;
    }
    
    /**
     * 根据占位符名称推断映射配置
     */
    private FieldMapping inferMappingFromPlaceholder(String variableName) {
        String lowerName = variableName.toLowerCase();
        
        if (lowerName.contains("category") || lowerName.contains("categories")) {
            FieldMapping mapping = new FieldMapping("category", "array");
            mapping.setAggregationType("list");
            return mapping;
        } else if (lowerName.contains("amount") || lowerName.contains("value")) {
            return new FieldMapping("amount", "number");
        } else if (lowerName.contains("quantity") || lowerName.contains("count")) {
            return new FieldMapping("quantity", "number");
        } else if (lowerName.contains("region")) {
            return new FieldMapping("region", "string");
        } else if (lowerName.contains("product")) {
            return new FieldMapping("product", "string");
        } else if (lowerName.contains("channel")) {
            return new FieldMapping("channel", "string");
        } else {
            // 默认映射到第一个字符串字段
            return new FieldMapping("category", "string");
        }
    }
}
