package com.example.chart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 动态Jolt规范选择测试
 */
@SpringBootTest
public class DynamicJoltSpecTest {

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private MappingRelationshipService mappingService;

    private Map<String, Object> universalTemplate;

    @BeforeEach
    public void setUp() {
        // 初始化映射关系
        mappingService.initializeSampleMappings();
        
        // 创建通用模板
        universalTemplate = createUniversalTemplate();
    }

    @Test
    public void testStackedLineChartTransformation() {
        System.out.println("=== 测试堆叠折线图转换 ===");
        
        TwoStageTransformationService.TransformationResult result = 
            transformationService.executeStage1Transformation("stacked_line_chart", universalTemplate);
        
        assertTrue(result.isSuccess(), "堆叠折线图转换应该成功");
        assertEquals("line-chart-placeholder.json", result.getUsedJoltSpec(), "应该使用折线图规范");
        assertNotNull(result.getResult(), "转换结果不应为空");
        
        System.out.println("✅ 堆叠折线图转换测试通过");
        System.out.println("使用的Jolt规范: " + result.getUsedJoltSpec());
    }

    @Test
    public void testBarChartTransformation() {
        System.out.println("=== 测试基础柱状图转换 ===");
        
        TwoStageTransformationService.TransformationResult result = 
            transformationService.executeStage1Transformation("basic_bar_chart", universalTemplate);
        
        assertTrue(result.isSuccess(), "基础柱状图转换应该成功");
        assertEquals("bar-chart-placeholder.json", result.getUsedJoltSpec(), "应该使用柱状图规范");
        assertNotNull(result.getResult(), "转换结果不应为空");
        
        System.out.println("✅ 基础柱状图转换测试通过");
        System.out.println("使用的Jolt规范: " + result.getUsedJoltSpec());
    }

    @Test
    public void testPieChartTransformation() {
        System.out.println("=== 测试饼图转换 ===");
        
        TwoStageTransformationService.TransformationResult result = 
            transformationService.executeStage1Transformation("pie_chart", universalTemplate);
        
        assertTrue(result.isSuccess(), "饼图转换应该成功");
        assertEquals("pie-chart-placeholder.json", result.getUsedJoltSpec(), "应该使用饼图规范");
        assertNotNull(result.getResult(), "转换结果不应为空");
        
        System.out.println("✅ 饼图转换测试通过");
        System.out.println("使用的Jolt规范: " + result.getUsedJoltSpec());
    }

    @Test
    public void testUnknownChartTypeTransformation() {
        System.out.println("=== 测试未知图表类型转换 ===");
        
        TwoStageTransformationService.TransformationResult result = 
            transformationService.executeStage1Transformation("unknown_chart", universalTemplate);
        
        assertTrue(result.isSuccess(), "未知图表类型应该使用默认规范");
        assertEquals("line-chart-placeholder.json", result.getUsedJoltSpec(), "应该使用默认的折线图规范");
        assertNotNull(result.getResult(), "转换结果不应为空");
        
        System.out.println("✅ 未知图表类型转换测试通过");
        System.out.println("使用的Jolt规范: " + result.getUsedJoltSpec());
    }

    @Test
    public void testPlaceholderPreservation() {
        System.out.println("=== 测试占位符保持功能 ===");
        
        // 测试不同图表类型的占位符保持
        String[] chartTypes = {"stacked_line_chart", "basic_bar_chart", "pie_chart"};
        
        for (String chartType : chartTypes) {
            TwoStageTransformationService.TransformationResult result = 
                transformationService.executeStage1Transformation(chartType, universalTemplate);
            
            assertTrue(result.isSuccess(), chartType + " 转换应该成功");
            assertNotNull(result.getPlaceholders(), "应该保留占位符信息");
            assertFalse(result.getPlaceholders().isEmpty(), "应该有占位符被保留");
            
            System.out.println(chartType + " 保留的占位符: " + result.getPlaceholders());
        }
        
        System.out.println("✅ 占位符保持功能测试通过");
    }

    private Map<String, Object> createUniversalTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        // 图表元数据
        Map<String, Object> chartMeta = new HashMap<>();
        chartMeta.put("chartId", "test_chart");
        chartMeta.put("chartType", "${chart_type}");
        chartMeta.put("title", "${chart_title}");
        chartMeta.put("dataSource", "test_db");
        template.put("chartMeta", chartMeta);
        
        // 类别数据
        template.put("categories", "${category_field}");
        
        // 系列数据
        Map<String, Object> series1 = new HashMap<>();
        series1.put("seriesId", "series_1");
        series1.put("seriesName", "${series_name_1}");
        series1.put("seriesType", "${chart_type}");
        series1.put("stackGroup", "${stack_group}");
        series1.put("values", "${series_data_1}");
        
        Map<String, Object> series2 = new HashMap<>();
        series2.put("seriesId", "series_2");
        series2.put("seriesName", "${series_name_2}");
        series2.put("seriesType", "${chart_type}");
        series2.put("stackGroup", "${stack_group}");
        series2.put("values", "${series_data_2}");
        
        template.put("series", new Object[]{series1, series2});
        
        // 样式配置
        Map<String, Object> styleConfig = new HashMap<>();
        styleConfig.put("showLegend", true);
        styleConfig.put("showTooltip", true);
        styleConfig.put("showGrid", true);
        template.put("styleConfig", styleConfig);
        
        return template;
    }
}
