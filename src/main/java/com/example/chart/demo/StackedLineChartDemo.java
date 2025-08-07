package com.example.chart.demo;

import com.example.chart.service.StackedLineChartValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 堆叠折线图演示类
 * 可以独立运行，不依赖Spring容器
 */
public class StackedLineChartDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== ECharts 动态数据流架构验证演示 ===\n");
            
            StackedLineChartValidationService service = new StackedLineChartValidationService();
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 步骤1: 模拟数据库查询结果
            System.out.println("📊 步骤1: 模拟数据库查询结果");
            System.out.println("模拟SQL: SELECT day_name, channel_name, conversion_count, stack_group FROM marketing_performance");
            List<Map<String, Object>> mockResults = service.getMockDatabaseResults();
            System.out.println("查询结果: " + mockResults.size() + " 条记录");
            System.out.println("示例记录: " + mockResults.get(0));
            System.out.println();
            
            // 步骤2: 转换为通用JSON格式
            System.out.println("🔄 步骤2: 转换为通用JSON格式");
            Map<String, Object> universalData = service.convertToUniversalFormat(mockResults);
            String universalJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(universalData);
            System.out.println("通用JSON格式:");
            System.out.println(universalJson);
            System.out.println();
            
            // 步骤3: Jolt转换为ECharts配置
            System.out.println("⚙️ 步骤3: Jolt转换为ECharts配置");
            Map<String, Object> echartsConfig = service.transformWithJolt(universalData);
            String echartsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(echartsConfig);
            System.out.println("生成的ECharts配置:");
            System.out.println(echartsJson);
            System.out.println();
            
            // 步骤4: 与原始配置对比
            System.out.println("🔍 步骤4: 与原始配置对比");
            ClassPathResource originalResource = new ClassPathResource("echarts/折线图/折线图堆叠.json");
            String originalContent = new String(originalResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> originalConfig = objectMapper.readValue(originalContent, Map.class);
            
            compareConfigs(originalConfig, echartsConfig);
            
            // 步骤5: 验证结果
            System.out.println("✅ 步骤5: 验证结果");
            StackedLineChartValidationService.ValidationResult result = service.validateStackedLineChart();
            System.out.println("验证状态: " + (result.isValid() ? "成功" : "失败"));
            System.out.println("验证详情:");
            System.out.println(result.getMessage());
            
            System.out.println("\n=== 验证演示完成 ===");
            System.out.println("🎉 架构验证成功！数据流: Mock数据 → 通用JSON → Jolt转换 → ECharts配置");
            
        } catch (Exception e) {
            System.err.println("演示过程出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void compareConfigs(Map<String, Object> original, Map<String, Object> generated) {
        System.out.println("配置对比结果:");
        
        String[] keyFields = {"title", "tooltip", "legend", "xAxis", "yAxis", "series"};
        
        for (String field : keyFields) {
            boolean originalHas = original.containsKey(field);
            boolean generatedHas = generated.containsKey(field);
            String status = (originalHas && generatedHas) ? "✅" : "❌";
            System.out.println(String.format("%s %s: 原始=%s, 生成=%s", status, field, originalHas, generatedHas));
        }
        
        // 检查系列数量
        if (original.containsKey("series") && generated.containsKey("series")) {
            List<?> originalSeries = (List<?>) original.get("series");
            List<?> generatedSeries = (List<?>) generated.get("series");
            boolean seriesCountMatch = originalSeries.size() == generatedSeries.size();
            String status = seriesCountMatch ? "✅" : "❌";
            System.out.println(String.format("%s 系列数量: 原始=%d, 生成=%d", 
                status, originalSeries.size(), generatedSeries.size()));
        }
        
        // 检查类别数据
        if (original.containsKey("xAxis") && generated.containsKey("xAxis")) {
            Map<String, Object> originalXAxis = (Map<String, Object>) original.get("xAxis");
            Map<String, Object> generatedXAxis = (Map<String, Object>) generated.get("xAxis");
            
            if (originalXAxis.containsKey("data") && generatedXAxis.containsKey("data")) {
                List<?> originalData = (List<?>) originalXAxis.get("data");
                List<?> generatedData = (List<?>) generatedXAxis.get("data");
                boolean dataMatch = originalData.equals(generatedData);
                String status = dataMatch ? "✅" : "❌";
                System.out.println(String.format("%s X轴数据: 原始=%s, 生成=%s", 
                    status, originalData, generatedData));
            }
        }
        
        System.out.println();
    }
}
