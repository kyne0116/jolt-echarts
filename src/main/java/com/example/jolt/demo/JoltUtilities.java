package com.example.jolt.demo;

import com.bazaarvoice.jolt.*;
import com.bazaarvoice.jolt.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Jolt工具类
 * 
 * 提供各种Jolt转换的实用工具方法，包括：
 * 1. 单独的转换器演示
 * 2. 转换规范验证
 * 3. 性能测试工具
 * 4. 调试辅助方法
 * 
 * @author Demo Author
 * @version 1.0.0
 */
public class JoltUtilities {
    
    private static final Logger logger = LoggerFactory.getLogger(JoltUtilities.class);
    
    /**
     * 演示Shiftr转换器的使用
     */
    public static void demonstrateShiftr() {
        logger.info("=== Shiftr转换器演示 ===");
        
        // 创建简单的输入数据
        Map<String, Object> input = new HashMap<>();
        input.put("name", "张三");
        input.put("age", 25);
        input.put("email", "zhangsan@example.com");
        
        // 创建Shiftr规范
        Map<String, Object> shiftrSpec = new HashMap<>();
        shiftrSpec.put("name", "user.fullName");
        shiftrSpec.put("age", "user.age");
        shiftrSpec.put("email", "contact.email");
        
        try {
            // 执行转换
            Shiftr shiftr = new Shiftr(shiftrSpec);
            Object result = shiftr.transform(input);
            
            logger.info("输入数据: {}", JsonUtils.toPrettyJsonString(input));
            logger.info("转换规范: {}", JsonUtils.toPrettyJsonString(shiftrSpec));
            logger.info("转换结果: {}", JsonUtils.toPrettyJsonString(result));
            
        } catch (Exception e) {
            logger.error("Shiftr转换失败", e);
        }
    }
    
    /**
     * 演示Defaultr转换器的使用
     */
    public static void demonstrateDefaultr() {
        logger.info("=== Defaultr转换器演示 ===");
        
        // 创建不完整的输入数据
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "李四");
        input.put("user", user);
        
        // 创建Defaultr规范
        Map<String, Object> defaultrSpec = new HashMap<>();
        Map<String, Object> userDefaults = new HashMap<>();
        userDefaults.put("age", 18);
        userDefaults.put("status", "active");
        userDefaults.put("preferences", new HashMap<String, Object>() {{
            put("theme", "light");
            put("language", "zh-CN");
        }});
        defaultrSpec.put("user", userDefaults);
        
        try {
            // 执行转换
            Defaultr defaultr = new Defaultr(defaultrSpec);
            Object result = defaultr.transform(JsonUtils.cloneJson(input));
            
            logger.info("输入数据: {}", JsonUtils.toPrettyJsonString(input));
            logger.info("默认值规范: {}", JsonUtils.toPrettyJsonString(defaultrSpec));
            logger.info("转换结果: {}", JsonUtils.toPrettyJsonString(result));
            
        } catch (Exception e) {
            logger.error("Defaultr转换失败", e);
        }
    }
    
    /**
     * 演示Removr转换器的使用
     */
    public static void demonstrateRemovr() {
        logger.info("=== Removr转换器演示 ===");
        
        // 创建包含敏感信息的输入数据
        Map<String, Object> input = new HashMap<>();
        input.put("name", "王五");
        input.put("age", 30);
        input.put("password", "secret123");
        input.put("ssn", "123-45-6789");
        input.put("email", "wangwu@example.com");
        
        // 创建Removr规范 - 移除敏感信息
        Map<String, Object> removrSpec = new HashMap<>();
        removrSpec.put("password", "");
        removrSpec.put("ssn", "");
        
        try {
            // 执行转换
            Removr removr = new Removr(removrSpec);
            Object result = removr.transform(JsonUtils.cloneJson(input));
            
            logger.info("输入数据: {}", JsonUtils.toPrettyJsonString(input));
            logger.info("移除规范: {}", JsonUtils.toPrettyJsonString(removrSpec));
            logger.info("转换结果: {}", JsonUtils.toPrettyJsonString(result));
            
        } catch (Exception e) {
            logger.error("Removr转换失败", e);
        }
    }
    
    /**
     * 演示Sortr转换器的使用
     */
    public static void demonstrateSortr() {
        logger.info("=== Sortr转换器演示 ===");
        
        // 创建无序的输入数据
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("zebra", "动物");
        input.put("apple", "水果");
        input.put("banana", "水果");
        input.put("cat", "动物");
        
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("gamma", 3);
        nested.put("alpha", 1);
        nested.put("beta", 2);
        input.put("nested", nested);
        
        try {
            // 执行转换
            Sortr sortr = new Sortr();
            Object result = sortr.transform(input);
            
            logger.info("输入数据: {}", JsonUtils.toPrettyJsonString(input));
            logger.info("排序结果: {}", JsonUtils.toPrettyJsonString(result));
            
        } catch (Exception e) {
            logger.error("Sortr转换失败", e);
        }
    }
    
    /**
     * 演示CardinalityTransform转换器的使用
     */
    public static void demonstrateCardinalityTransform() {
        logger.info("=== CardinalityTransform转换器演示 ===");
        
        // 创建基数不一致的输入数据
        Map<String, Object> input = new HashMap<>();
        input.put("singleItem", "item1");
        input.put("multipleItems", Arrays.asList("item1", "item2", "item3"));
        
        // 创建CardinalityTransform规范
        Map<String, Object> cardinalitySpec = new HashMap<>();
        cardinalitySpec.put("singleItem", "MANY");  // 单个值转为数组
        cardinalitySpec.put("multipleItems", "ONE"); // 数组转为单个值
        
        try {
            // 执行转换
            CardinalityTransform cardinalityTransform = new CardinalityTransform(cardinalitySpec);
            Object result = cardinalityTransform.transform(JsonUtils.cloneJson(input));
            
            logger.info("输入数据: {}", JsonUtils.toPrettyJsonString(input));
            logger.info("基数转换规范: {}", JsonUtils.toPrettyJsonString(cardinalitySpec));
            logger.info("转换结果: {}", JsonUtils.toPrettyJsonString(result));
            
        } catch (Exception e) {
            logger.error("CardinalityTransform转换失败", e);
        }
    }
    
    /**
     * 验证转换规范的有效性
     */
    public static boolean validateTransformationSpec(List<Object> spec) {
        logger.info("=== 验证转换规范 ===");
        
        try {
            // 尝试创建Chainr实例
            Chainr chainr = Chainr.fromSpec(spec);
            
            // 使用空输入测试转换
            Map<String, Object> testInput = new HashMap<>();
            testInput.put("test", "value");
            
            Object result = chainr.transform(testInput);
            
            logger.info("转换规范验证通过");
            return true;
            
        } catch (Exception e) {
            logger.error("转换规范验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 性能测试工具
     */
    public static void performanceTest(Object inputData, List<Object> transformSpec, int iterations) {
        logger.info("=== 性能测试 (迭代次数: {}) ===", iterations);
        
        try {
            // 创建转换器
            Chainr chainr = Chainr.fromSpec(transformSpec);
            
            // 预热
            for (int i = 0; i < 10; i++) {
                chainr.transform(inputData);
            }
            
            // 性能测试
            long startTime = System.nanoTime();
            
            for (int i = 0; i < iterations; i++) {
                chainr.transform(inputData);
            }
            
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            
            double averageTime = (double) totalTime / iterations / 1_000_000; // 转换为毫秒
            double throughput = (double) iterations / (totalTime / 1_000_000_000.0); // 每秒处理次数
            
            logger.info("总耗时: {} ms", totalTime / 1_000_000);
            logger.info("平均耗时: {:.3f} ms", averageTime);
            logger.info("吞吐量: {:.2f} 次/秒", throughput);
            
        } catch (Exception e) {
            logger.error("性能测试失败", e);
        }
    }
    
    /**
     * 比较两个JSON对象的差异
     */
    public static void compareJsonObjects(Object obj1, Object obj2, String obj1Name, String obj2Name) {
        logger.info("=== JSON对象比较: {} vs {} ===", obj1Name, obj2Name);
        
        try {
            Diffy diffy = new Diffy();
            Diffy.Result result = diffy.diff(obj1, obj2);
            
            if (result.isEmpty()) {
                logger.info("两个对象完全相同");
            } else {
                logger.info("发现差异:");
                logger.info("{} 独有内容: {}", obj1Name, JsonUtils.toPrettyJsonString(result.expected));
                logger.info("{} 独有内容: {}", obj2Name, JsonUtils.toPrettyJsonString(result.actual));
            }
            
        } catch (Exception e) {
            logger.error("JSON对象比较失败", e);
        }
    }
    
    /**
     * 分析JSON结构复杂度
     */
    public static void analyzeJsonComplexity(Object jsonObject, String objectName) {
        logger.info("=== JSON结构复杂度分析: {} ===", objectName);
        
        try {
            String jsonString = JsonUtils.toJsonString(jsonObject);
            
            // 基本统计
            int totalSize = jsonString.length();
            int fieldCount = jsonString.split(",").length;
            int depth = calculateDepth(jsonObject);
            
            // 类型统计
            Map<String, Integer> typeCount = countTypes(jsonObject);
            
            logger.info("总大小: {} 字符", totalSize);
            logger.info("字段数量（估算）: {}", fieldCount);
            logger.info("最大深度: {}", depth);
            logger.info("类型分布: {}", typeCount);
            
        } catch (Exception e) {
            logger.error("JSON结构分析失败", e);
        }
    }
    
    /**
     * 计算JSON对象的深度
     */
    private static int calculateDepth(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            int maxDepth = 0;
            for (Object value : map.values()) {
                maxDepth = Math.max(maxDepth, calculateDepth(value));
            }
            return 1 + maxDepth;
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            int maxDepth = 0;
            for (Object item : list) {
                maxDepth = Math.max(maxDepth, calculateDepth(item));
            }
            return 1 + maxDepth;
        } else {
            return 0;
        }
    }
    
    /**
     * 统计JSON对象中各种类型的数量
     */
    private static Map<String, Integer> countTypes(Object obj) {
        Map<String, Integer> typeCount = new HashMap<>();
        countTypesRecursive(obj, typeCount);
        return typeCount;
    }
    
    private static void countTypesRecursive(Object obj, Map<String, Integer> typeCount) {
        if (obj == null) {
            typeCount.merge("null", 1, Integer::sum);
        } else if (obj instanceof String) {
            typeCount.merge("string", 1, Integer::sum);
        } else if (obj instanceof Number) {
            typeCount.merge("number", 1, Integer::sum);
        } else if (obj instanceof Boolean) {
            typeCount.merge("boolean", 1, Integer::sum);
        } else if (obj instanceof Map) {
            typeCount.merge("object", 1, Integer::sum);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Object value : map.values()) {
                countTypesRecursive(value, typeCount);
            }
        } else if (obj instanceof List) {
            typeCount.merge("array", 1, Integer::sum);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            for (Object item : list) {
                countTypesRecursive(item, typeCount);
            }
        } else {
            typeCount.merge("unknown", 1, Integer::sum);
        }
    }
    
    /**
     * 运行所有演示
     */
    public static void runAllDemonstrations() {
        logger.info("=== 运行所有Jolt转换器演示 ===");
        
        demonstrateShiftr();
        System.out.println();
        
        demonstrateDefaultr();
        System.out.println();
        
        demonstrateRemovr();
        System.out.println();
        
        demonstrateSortr();
        System.out.println();
        
        demonstrateCardinalityTransform();
        System.out.println();
        
        logger.info("=== 所有演示完成 ===");
    }
}
