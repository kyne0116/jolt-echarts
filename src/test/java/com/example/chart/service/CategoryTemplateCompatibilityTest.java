package com.example.chart.service;

import com.example.chart.model.TemplateType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类模板兼容性测试
 * 验证分类模板系统对11种图表的兼容性
 */
@SpringBootTest
public class CategoryTemplateCompatibilityTest {

    @Autowired
    private CategoryTemplateFactory templateFactory;

    @Autowired
    private SmartTransformationEngine transformationEngine;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private MappingRelationshipService mappingService;

    @Test
    public void testTemplateTypeInference() {
        System.out.println("=== 测试模板类型推断 ===");
        
        // 测试直角坐标系类型推断
        assertEquals(TemplateType.CARTESIAN, TemplateType.inferFromChartType("stacked_line_chart"));
        assertEquals(TemplateType.CARTESIAN, TemplateType.inferFromChartType("basic_bar_chart"));
        assertEquals(TemplateType.CARTESIAN, TemplateType.inferFromChartType("smooth_line_chart"));
        
        // 测试饼图类型推断
        assertEquals(TemplateType.PIE, TemplateType.inferFromChartType("basic_pie_chart"));
        assertEquals(TemplateType.PIE, TemplateType.inferFromChartType("doughnut_chart"));
        
        // 测试雷达图类型推断
        assertEquals(TemplateType.RADAR, TemplateType.inferFromChartType("basic_radar_chart"));
        
        // 测试仪表盘类型推断
        assertEquals(TemplateType.GAUGE, TemplateType.inferFromChartType("basic_gauge_chart"));
        assertEquals(TemplateType.GAUGE, TemplateType.inferFromChartType("progress_gauge_chart"));
        
        System.out.println("✅ 模板类型推断测试通过");
    }

    @Test
    public void testCategoryTemplateCreation() {
        System.out.println("=== 测试分类模板创建 ===");
        
        // 测试直角坐标系模板
        Map<String, Object> cartesianTemplate = templateFactory.createTemplate(TemplateType.CARTESIAN);
        assertNotNull(cartesianTemplate);
        assertTrue(cartesianTemplate.containsKey("coordinates"));
        assertTrue(cartesianTemplate.containsKey("data"));
        
        // 测试饼图模板
        Map<String, Object> pieTemplate = templateFactory.createTemplate(TemplateType.PIE);
        assertNotNull(pieTemplate);
        assertTrue(pieTemplate.containsKey("data"));
        assertFalse(pieTemplate.containsKey("coordinates")); // 饼图不需要坐标系
        
        // 测试雷达图模板
        Map<String, Object> radarTemplate = templateFactory.createTemplate(TemplateType.RADAR);
        assertNotNull(radarTemplate);
        assertTrue(radarTemplate.containsKey("coordinates"));
        
        // 测试仪表盘模板
        Map<String, Object> gaugeTemplate = templateFactory.createTemplate(TemplateType.GAUGE);
        assertNotNull(gaugeTemplate);
        assertTrue(gaugeTemplate.containsKey("data"));
        
        System.out.println("✅ 分类模板创建测试通过");
    }

    @Test
    public void testPlaceholderExtraction() {
        System.out.println("=== 测试占位符提取 ===");
        
        for (TemplateType templateType : TemplateType.values()) {
            Set<String> placeholders = templateFactory.getTemplatePlaceholders(templateType);
            assertFalse(placeholders.isEmpty(), "模板类型 " + templateType + " 应该包含占位符");
            
            System.out.println(templateType + " 占位符数量: " + placeholders.size());
            System.out.println(templateType + " 占位符: " + placeholders);
        }
        
        System.out.println("✅ 占位符提取测试通过");
    }

    @Test
    public void testSemanticTransformation() {
        System.out.println("=== 测试语义转换 ===");
        
        String[] testChartTypes = {
            "stacked_line_chart", "basic_bar_chart", "basic_pie_chart", 
            "basic_radar_chart", "basic_gauge_chart"
        };
        
        for (String chartType : testChartTypes) {
            try {
                // 获取分类模板
                Map<String, Object> template = templateService.getCategoryTemplateByChartId(chartType);
                assertNotNull(template, "模板不应为空: " + chartType);
                
                // 执行语义转换
                Map<String, Object> echartsConfig = transformationEngine.semanticTransformWithCategory(chartType, template);
                assertNotNull(echartsConfig, "转换结果不应为空: " + chartType);
                
                // 验证基本结构
                if (chartType.contains("line") || chartType.contains("bar")) {
                    assertTrue(echartsConfig.containsKey("xAxis"), "直角坐标系图表应包含xAxis: " + chartType);
                    assertTrue(echartsConfig.containsKey("yAxis"), "直角坐标系图表应包含yAxis: " + chartType);
                }
                
                assertTrue(echartsConfig.containsKey("series"), "所有图表都应包含series: " + chartType);
                
                System.out.println("✅ " + chartType + " 语义转换成功");
                
            } catch (Exception e) {
                fail("语义转换失败: " + chartType + ", 错误: " + e.getMessage());
            }
        }
        
        System.out.println("✅ 语义转换测试通过");
    }

    @Test
    public void testEndToEndCompatibility() {
        System.out.println("=== 测试端到端兼容性 ===");
        
        // 初始化映射关系
        mappingService.initializeSampleMappings();
        
        String[] testChartTypes = {
            "stacked_line_chart", "basic_bar_chart", "basic_pie_chart"
        };
        
        for (String chartType : testChartTypes) {
            try {
                System.out.println("🧪 测试图表类型: " + chartType);
                
                // 1. 获取分类模板
                Map<String, Object> template = templateService.getCategoryTemplateByChartId(chartType);
                Set<String> originalPlaceholders = placeholderManager.extractPlaceholdersFromJson(template);
                System.out.println("1️⃣ 原始占位符: " + originalPlaceholders.size() + " 个");
                
                // 2. 语义转换
                Map<String, Object> echartsStructure = transformationEngine.semanticTransformWithCategory(chartType, template);
                Set<String> transformedPlaceholders = placeholderManager.extractPlaceholdersFromJson(echartsStructure);
                System.out.println("2️⃣ 转换后占位符: " + transformedPlaceholders.size() + " 个");
                
                // 3. 验证映射关系
                java.util.List<String> missingMappings = mappingService.validateMappings(chartType, transformedPlaceholders);
                System.out.println("3️⃣ 缺失映射: " + missingMappings.size() + " 个");
                
                // 4. 模拟数据查询
                Map<String, Object> queryResults = mappingService.simulateDataQuery(chartType, transformedPlaceholders);
                System.out.println("4️⃣ 查询结果: " + queryResults.size() + " 个");
                
                // 5. 占位符替换
                Object finalResult = placeholderManager.replacePlaceholdersInJson(echartsStructure, queryResults);
                Set<String> remainingPlaceholders = placeholderManager.extractPlaceholdersFromJson(finalResult);
                System.out.println("5️⃣ 剩余占位符: " + remainingPlaceholders.size() + " 个");
                
                // 验证结果
                assertTrue(missingMappings.isEmpty(), "不应该有缺失的映射关系: " + chartType);
                assertFalse(queryResults.isEmpty(), "应该有查询结果: " + chartType);
                assertTrue(remainingPlaceholders.isEmpty(), "不应该有剩余占位符: " + chartType);
                
                System.out.println("✅ " + chartType + " 端到端测试通过");
                
            } catch (Exception e) {
                System.err.println("❌ " + chartType + " 端到端测试失败: " + e.getMessage());
                e.printStackTrace();
                fail("端到端测试失败: " + chartType + ", 错误: " + e.getMessage());
            }
        }
        
        System.out.println("🎉 端到端兼容性测试全部通过！");
    }

    @Test
    public void testPerformance() {
        System.out.println("=== 测试性能指标 ===");
        
        String chartType = "stacked_line_chart";
        int iterations = 100;
        
        // 模板创建性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            templateService.getCategoryTemplateByChartId(chartType);
        }
        long templateTime = System.currentTimeMillis() - startTime;
        System.out.println("模板创建平均时间: " + (templateTime / iterations) + "ms");
        
        // 语义转换性能
        Map<String, Object> template = templateService.getCategoryTemplateByChartId(chartType);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            transformationEngine.semanticTransformWithCategory(chartType, template);
        }
        long transformTime = System.currentTimeMillis() - startTime;
        System.out.println("语义转换平均时间: " + (transformTime / iterations) + "ms");
        
        // 验证性能指标
        assertTrue(templateTime / iterations < 10, "模板创建时间应小于10ms");
        assertTrue(transformTime / iterations < 50, "语义转换时间应小于50ms");
        
        System.out.println("✅ 性能测试通过");
    }
}
