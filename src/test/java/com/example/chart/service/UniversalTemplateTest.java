package com.example.chart.service;

import com.example.chart.model.UniversalTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UniversalTemplateTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SmartTransformationEngine smartEngine;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private MappingRelationshipService mappingService;

    @Test
    public void testUniversalTemplateCreation() {
        System.out.println("=== 测试通用模板创建 ===");
        
        UniversalTemplate template = UniversalTemplate.createDefault();
        assertNotNull(template);
        
        Map<String, Object> templateMap = template.toMap();
        assertNotNull(templateMap);
        
        // 验证结构
        assertTrue(templateMap.containsKey("chart"));
        assertTrue(templateMap.containsKey("data"));
        assertTrue(templateMap.containsKey("layout"));
        assertTrue(templateMap.containsKey("interaction"));
        
        System.out.println("✅ 通用模板创建测试通过");
    }

    @Test
    public void testTemplateServiceWithUniversalTemplate() {
        System.out.println("=== 测试模板服务 ===");
        
        // 获取通用模板
        Map<String, Object> template = templateService.getTemplateByChartId("stacked_line_chart");
        assertNotNull(template);
        
        // 验证占位符
        Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(template);
        assertFalse(placeholders.isEmpty());
        
        System.out.println("发现占位符: " + placeholders);
        System.out.println("✅ 模板服务测试通过");
    }

    @Test
    public void testSmartTransformationEngine() {
        System.out.println("=== 测试智能转换引擎 ===");
        
        // 创建通用模板
        UniversalTemplate template = UniversalTemplate.createDefault();
        Map<String, Object> templateMap = template.toMap();
        
        // 测试折线图转换
        Map<String, Object> lineChart = smartEngine.semanticTransform(templateMap);
        assertNotNull(lineChart);
        assertTrue(lineChart.containsKey("title"));
        assertTrue(lineChart.containsKey("xAxis"));
        assertTrue(lineChart.containsKey("yAxis"));
        assertTrue(lineChart.containsKey("series"));
        
        System.out.println("折线图转换结果: " + lineChart.keySet());
        System.out.println("✅ 智能转换引擎测试通过");
    }

    @Test
    public void testNewMappingSystem() {
        System.out.println("=== 测试新映射系统 ===");
        
        // 初始化映射关系
        mappingService.initializeSampleMappings();
        
        // 测试新格式占位符的映射
        Set<String> testPlaceholders = Set.of(
            "${chart_title}", 
            "${categories}", 
            "${series_1_name}", 
            "${series_1_data}"
        );
        
        Map<String, Object> queryResults = mappingService.simulateDataQuery("stacked_line_chart", testPlaceholders);
        
        assertEquals(testPlaceholders.size(), queryResults.size(), "查询结果数量应该匹配");
        
        // 验证具体数据
        assertTrue(queryResults.containsKey("${chart_title}"));
        assertTrue(queryResults.containsKey("${categories}"));
        assertTrue(queryResults.containsKey("${series_1_name}"));
        assertTrue(queryResults.containsKey("${series_1_data}"));
        
        System.out.println("查询结果: " + queryResults.keySet());
        System.out.println("✅ 新映射系统测试通过");
    }

    @Test
    public void testCompleteNewArchitecture() {
        System.out.println("=== 测试完整新架构 ===");
        
        try {
            // 1. 获取通用模板
            Map<String, Object> template = templateService.getTemplateByChartId("stacked_line_chart");
            System.out.println("1️⃣ 获取通用模板成功");
            
            // 2. 智能转换
            Map<String, Object> echartsStructure = smartEngine.semanticTransform(template);
            System.out.println("2️⃣ 智能转换成功");
            
            // 3. 提取占位符
            Set<String> placeholders = placeholderManager.extractPlaceholdersFromJson(echartsStructure);
            System.out.println("3️⃣ 提取占位符: " + placeholders);
            
            // 4. 初始化映射关系
            mappingService.initializeSampleMappings();
            System.out.println("4️⃣ 初始化映射关系成功");
            
            // 5. 验证映射关系
            java.util.List<String> missingMappings = mappingService.validateMappings("stacked_line_chart", placeholders);
            System.out.println("5️⃣ 缺失映射: " + missingMappings);
            
            // 6. 模拟数据查询
            Map<String, Object> queryResults = mappingService.simulateDataQuery("stacked_line_chart", placeholders);
            System.out.println("6️⃣ 查询结果数量: " + queryResults.size());
            
            // 7. 替换占位符
            Object finalResult = placeholderManager.replacePlaceholdersInJson(echartsStructure, queryResults);
            System.out.println("7️⃣ 占位符替换成功");
            
            // 8. 验证最终结果
            Set<String> remainingPlaceholders = placeholderManager.extractPlaceholdersFromJson(finalResult);
            System.out.println("8️⃣ 剩余占位符: " + remainingPlaceholders);
            
            // 验证
            assertTrue(missingMappings.isEmpty(), "不应该有缺失的映射关系");
            assertFalse(queryResults.isEmpty(), "应该有查询结果");
            
            System.out.println("🎉 完整新架构测试通过！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("完整架构测试失败: " + e.getMessage());
        }
    }
}
