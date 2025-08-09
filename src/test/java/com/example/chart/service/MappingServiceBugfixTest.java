package com.example.chart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试映射服务的空指针异常修复
 */
@SpringBootTest
public class MappingServiceBugfixTest {

    private MappingRelationshipService mappingService;
    private PlaceholderManager placeholderManager;

    @BeforeEach
    public void setUp() {
        mappingService = new MappingRelationshipService();
        placeholderManager = new PlaceholderManager();
    }

    @Test
    public void testNullPointerExceptionFix() {
        System.out.println("=== 测试空指针异常修复 ===");
        
        // 初始化映射关系
        mappingService.initializeSampleMappings();
        
        // 创建包含占位符的测试数据
        Map<String, Object> testTemplate = Map.of(
            "title", Map.of("text", "${chart_title}"),
            "xAxis", Map.of("data", "${categories}"),
            "series", java.util.Arrays.asList(
                Map.of("name", "${series_1_name}", "data", "${series_1_data}"),
                Map.of("name", "${series_2_name}", "data", "${series_2_data}")
            )
        );
        
        // 提取占位符
        Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(testTemplate);
        System.out.println("提取的占位符: " + placeholders);
        
        // 模拟数据查询 - 这里之前会抛出空指针异常
        Map<String, Object> queryResults = mappingService.simulateDataQuery("stacked_line_chart", placeholders);
        
        // 验证结果
        assertNotNull(queryResults, "查询结果不应为null");
        assertFalse(queryResults.isEmpty(), "查询结果不应为空");
        
        System.out.println("查询结果数量: " + queryResults.size());
        System.out.println("查询结果键: " + queryResults.keySet());
        
        // 验证每个占位符都有对应的值
        for (String placeholder : placeholders) {
            assertTrue(queryResults.containsKey(placeholder), 
                "占位符 " + placeholder + " 应该有对应的值");
            assertNotNull(queryResults.get(placeholder), 
                "占位符 " + placeholder + " 的值不应为null");
        }
        
        // 执行占位符替换
        Object finalResult = placeholderManager.replacePlaceholdersInJson(testTemplate, queryResults);
        assertNotNull(finalResult, "最终结果不应为null");
        
        // 验证替换后没有剩余占位符
        Set<String> remainingPlaceholders = placeholderManager.extractPlaceholdersFromJson(finalResult);
        assertTrue(remainingPlaceholders.isEmpty(), 
            "替换后不应该有剩余占位符，但发现: " + remainingPlaceholders);
        
        System.out.println("✅ 空指针异常修复测试通过");
    }
}
