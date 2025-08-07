package com.example.chart.service;

import com.example.chart.service.StackedLineChartValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化的堆叠折线图测试（不依赖Spring）
 */
public class SimpleStackedLineChartTest {

    private final StackedLineChartValidationService validationService = new StackedLineChartValidationService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testMockDataGeneration() {
        System.out.println("=== 测试Mock数据生成 ===");
        
        List<Map<String, Object>> mockResults = validationService.getMockDatabaseResults();
        
        // 验证数据量：5个渠道 × 7天 = 35条记录
        assertEquals(35, mockResults.size(), "Mock数据应该包含35条记录");
        
        // 验证数据结构
        Map<String, Object> firstRecord = mockResults.get(0);
        assertTrue(firstRecord.containsKey("day_name"), "记录应包含day_name字段");
        assertTrue(firstRecord.containsKey("channel_name"), "记录应包含channel_name字段");
        assertTrue(firstRecord.containsKey("conversion_count"), "记录应包含conversion_count字段");
        assertTrue(firstRecord.containsKey("stack_group"), "记录应包含stack_group字段");
        
        System.out.println("✅ Mock数据生成测试通过");
        System.out.println("示例记录: " + firstRecord);
    }

    @Test
    public void testUniversalFormatConversion() throws Exception {
        System.out.println("=== 测试通用格式转换 ===");
        
        List<Map<String, Object>> mockResults = validationService.getMockDatabaseResults();
        Map<String, Object> universalData = validationService.convertToUniversalFormat(mockResults);
        
        // 验证通用格式结构
        assertTrue(universalData.containsKey("chartMeta"), "应包含chartMeta字段");
        assertTrue(universalData.containsKey("categories"), "应包含categories字段");
        assertTrue(universalData.containsKey("series"), "应包含series字段");
        
        // 验证类别数据
        List<String> categories = (List<String>) universalData.get("categories");
        assertEquals(7, categories.size(), "应包含7个类别（天数）");
        
        // 验证系列数据
        List<Map<String, Object>> series = (List<Map<String, Object>>) universalData.get("series");
        assertEquals(5, series.size(), "应包含5个系列（渠道）");
        
        // 验证每个系列的数据结构
        for (Map<String, Object> s : series) {
            assertTrue(s.containsKey("seriesName"), "系列应包含seriesName");
            assertTrue(s.containsKey("values"), "系列应包含values");
            assertTrue(s.containsKey("stackGroup"), "系列应包含stackGroup");
            
            List<Integer> values = (List<Integer>) s.get("values");
            assertEquals(7, values.size(), "每个系列应包含7个数值");
        }
        
        System.out.println("通用格式数据:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(universalData));
        System.out.println("✅ 通用格式转换测试通过");
    }

    @Test
    public void testJoltTransformation() throws Exception {
        System.out.println("=== 测试Jolt转换 ===");
        
        List<Map<String, Object>> mockResults = validationService.getMockDatabaseResults();
        Map<String, Object> universalData = validationService.convertToUniversalFormat(mockResults);
        Map<String, Object> echartsConfig = validationService.transformWithJolt(universalData);
        
        // 验证ECharts配置结构
        assertTrue(echartsConfig.containsKey("title"), "应包含title字段");
        assertTrue(echartsConfig.containsKey("xAxis"), "应包含xAxis字段");
        assertTrue(echartsConfig.containsKey("yAxis"), "应包含yAxis字段");
        assertTrue(echartsConfig.containsKey("series"), "应包含series字段");
        assertTrue(echartsConfig.containsKey("legend"), "应包含legend字段");
        
        // 验证title
        Map<String, Object> title = (Map<String, Object>) echartsConfig.get("title");
        assertEquals("Stacked Line", title.get("text"), "标题应为'Stacked Line'");
        
        // 验证xAxis
        Map<String, Object> xAxis = (Map<String, Object>) echartsConfig.get("xAxis");
        assertEquals("category", xAxis.get("type"), "xAxis类型应为category");
        List<String> xAxisData = (List<String>) xAxis.get("data");
        assertEquals(7, xAxisData.size(), "xAxis数据应包含7个类别");
        
        // 验证series
        List<Map<String, Object>> series = (List<Map<String, Object>>) echartsConfig.get("series");
        assertEquals(5, series.size(), "应包含5个系列");
        
        // 验证堆叠配置
        for (Map<String, Object> s : series) {
            assertTrue(s.containsKey("stack"), "每个系列应包含stack字段");
            assertEquals("Total", s.get("stack"), "stack值应为'Total'");
            assertEquals("line", s.get("type"), "类型应为line");
        }
        
        System.out.println("ECharts配置:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(echartsConfig));
        System.out.println("✅ Jolt转换测试通过");
    }

    @Test
    public void testCompleteValidationFlow() throws Exception {
        System.out.println("=== 测试完整验证流程 ===");
        
        StackedLineChartValidationService.ValidationResult result = validationService.validateStackedLineChart();
        
        assertTrue(result.isValid(), "验证应该成功");
        assertNotNull(result.getEchartsConfig(), "应该生成ECharts配置");
        assertNotNull(result.getMessage(), "应该包含验证消息");
        
        System.out.println("验证结果:");
        System.out.println(result.getMessage());
        System.out.println("✅ 完整验证流程测试通过");
    }
}
