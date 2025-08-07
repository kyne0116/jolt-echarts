package com.example.chart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占位符管理器
 * 负责占位符的解析、提取、验证和替换
 */
@Service
public class PlaceholderManager {

    private static final String PLACEHOLDER_PATTERN = "\\$\\{([^}]+)\\}";
    private static final Pattern PATTERN = Pattern.compile(PLACEHOLDER_PATTERN);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从字符串中提取所有占位符
     */
    public Set<String> extractPlaceholders(String content) {
        Set<String> placeholders = new HashSet<>();
        if (content == null) return placeholders;
        
        Matcher matcher = PATTERN.matcher(content);
        while (matcher.find()) {
            placeholders.add(matcher.group(0)); // 包含 ${} 的完整占位符
        }
        return placeholders;
    }

    /**
     * 从JSON对象中递归提取所有占位符
     */
    public Set<String> extractPlaceholdersFromJson(Object jsonObj) {
        Set<String> placeholders = new HashSet<>();
        try {
            String jsonString = objectMapper.writeValueAsString(jsonObj);
            return extractPlaceholders(jsonString);
        } catch (Exception e) {
            System.err.println("提取占位符时出错: " + e.getMessage());
            return placeholders;
        }
    }

    /**
     * 验证占位符格式是否正确
     */
    public boolean isValidPlaceholder(String placeholder) {
        return placeholder != null && PATTERN.matcher(placeholder).matches();
    }

    /**
     * 获取占位符的变量名（去掉 ${} 包装）
     */
    public String getVariableName(String placeholder) {
        if (!isValidPlaceholder(placeholder)) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(placeholder);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 创建占位符（添加 ${} 包装）
     */
    public String createPlaceholder(String variableName) {
        if (variableName == null || variableName.trim().isEmpty()) {
            return null;
        }
        return "${" + variableName.trim() + "}";
    }

    /**
     * 在字符串中替换占位符
     */
    public String replacePlaceholders(String template, Map<String, Object> values) {
        if (template == null || values == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String placeholder = entry.getKey();
            Object value = entry.getValue();
            
            if (isValidPlaceholder(placeholder)) {
                String valueStr = convertValueToString(value);
                result = result.replace(placeholder, valueStr);
            }
        }
        return result;
    }

    /**
     * 在JSON对象中递归替换占位符
     */
    public Object replacePlaceholdersInJson(Object jsonObj, Map<String, Object> values) {
        try {
            JsonNode jsonNode = objectMapper.valueToTree(jsonObj);
            JsonNode replacedNode = replacePlaceholdersInJsonNode(jsonNode, values);
            return objectMapper.treeToValue(replacedNode, Object.class);
        } catch (Exception e) {
            System.err.println("替换JSON占位符时出错: " + e.getMessage());
            return jsonObj;
        }
    }

    /**
     * 递归替换JsonNode中的占位符
     */
    private JsonNode replacePlaceholdersInJsonNode(JsonNode node, Map<String, Object> values) {
        if (node.isTextual()) {
            // 处理文本节点中的占位符
            String text = node.asText();
            String replacedText = replacePlaceholders(text, values);
            
            // 如果整个字符串就是一个占位符，尝试保持原始数据类型
            if (isValidPlaceholder(text) && values.containsKey(text)) {
                Object value = values.get(text);
                return objectMapper.valueToTree(value);
            }
            
            return new TextNode(replacedText);
        } else if (node.isArray()) {
            // 处理数组节点
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                arrayNode.add(replacePlaceholdersInJsonNode(item, values));
            }
            return arrayNode;
        } else if (node.isObject()) {
            // 处理对象节点
            ObjectNode objectNode = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                objectNode.set(key, replacePlaceholdersInJsonNode(value, values));
            });
            return objectNode;
        }
        
        return node; // 其他类型节点直接返回
    }

    /**
     * 将值转换为字符串
     */
    private String convertValueToString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List || value instanceof Map) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                return value.toString();
            }
        } else {
            return value.toString();
        }
    }

    /**
     * 验证所有占位符是否都有对应的值
     */
    public List<String> validatePlaceholders(Object jsonObj, Map<String, Object> values) {
        Set<String> placeholders = extractPlaceholdersFromJson(jsonObj);
        List<String> missingPlaceholders = new ArrayList<>();
        
        for (String placeholder : placeholders) {
            if (!values.containsKey(placeholder)) {
                missingPlaceholders.add(placeholder);
            }
        }
        
        return missingPlaceholders;
    }

    /**
     * 创建示例占位符映射
     */
    public Map<String, Object> createSamplePlaceholderValues() {
        Map<String, Object> values = new HashMap<>();
        
        // 基础数据占位符
        values.put("${chart_title}", "动态营销渠道分析");
        values.put("${category_field}", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        values.put("${series_name_1}", "邮件营销");
        values.put("${series_name_2}", "联盟广告");
        values.put("${series_name_3}", "视频广告");
        values.put("${series_name_4}", "直接访问");
        values.put("${series_name_5}", "搜索引擎");
        
        // 数值数据占位符
        values.put("${series_data_1}", Arrays.asList(120, 132, 101, 134, 90, 230, 210));
        values.put("${series_data_2}", Arrays.asList(220, 182, 191, 234, 290, 330, 310));
        values.put("${series_data_3}", Arrays.asList(150, 232, 201, 154, 190, 330, 410));
        values.put("${series_data_4}", Arrays.asList(320, 332, 301, 334, 390, 330, 320));
        values.put("${series_data_5}", Arrays.asList(820, 932, 901, 934, 1290, 1330, 1320));
        
        // 配置占位符
        values.put("${stack_group}", "Total");
        values.put("${chart_type}", "line");
        
        return values;
    }
}
