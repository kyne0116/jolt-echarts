package com.example.chart.service;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 堆叠折线图验证服务
 * 验证从Mock数据到ECharts配置的完整数据流
 */
@Service
public class StackedLineChartValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 模拟数据库查询结果
     * 模拟ORM框架已经完成数据提取的场景
     */
    public List<Map<String, Object>> getMockDatabaseResults() {
        List<Map<String, Object>> mockResults = new ArrayList<>();
        
        // 模拟营销渠道数据 - 一周的数据
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        String[] channels = {"Email", "Union Ads", "Video Ads", "Direct", "Search Engine"};
        
        // Email 数据
        int[] emailData = {120, 132, 101, 134, 90, 230, 210};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("day_name", days[i]);
            record.put("channel_name", "Email");
            record.put("conversion_count", emailData[i]);
            record.put("stack_group", "Total");
            mockResults.add(record);
        }
        
        // Union Ads 数据
        int[] unionAdsData = {220, 182, 191, 234, 290, 330, 310};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("day_name", days[i]);
            record.put("channel_name", "Union Ads");
            record.put("conversion_count", unionAdsData[i]);
            record.put("stack_group", "Total");
            mockResults.add(record);
        }
        
        // Video Ads 数据
        int[] videoAdsData = {150, 232, 201, 154, 190, 330, 410};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("day_name", days[i]);
            record.put("channel_name", "Video Ads");
            record.put("conversion_count", videoAdsData[i]);
            record.put("stack_group", "Total");
            mockResults.add(record);
        }
        
        // Direct 数据
        int[] directData = {320, 332, 301, 334, 390, 330, 320};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("day_name", days[i]);
            record.put("channel_name", "Direct");
            record.put("conversion_count", directData[i]);
            record.put("stack_group", "Total");
            mockResults.add(record);
        }
        
        // Search Engine 数据
        int[] searchEngineData = {820, 932, 901, 934, 1290, 1330, 1320};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("day_name", days[i]);
            record.put("channel_name", "Search Engine");
            record.put("conversion_count", searchEngineData[i]);
            record.put("stack_group", "Total");
            mockResults.add(record);
        }
        
        return mockResults;
    }

    /**
     * 将Mock数据转换为通用JSON格式
     */
    public Map<String, Object> convertToUniversalFormat(List<Map<String, Object>> mockResults) {
        Map<String, Object> universalData = new HashMap<>();
        
        // 设置图表元数据
        Map<String, Object> chartMeta = new HashMap<>();
        chartMeta.put("chartId", "stacked_line_validation_001");
        chartMeta.put("chartType", "line");
        chartMeta.put("title", "Stacked Line");
        chartMeta.put("dataSource", "marketing_db");
        universalData.put("chartMeta", chartMeta);
        
        // 提取类别数据（天数）
        Set<String> categorySet = new LinkedHashSet<>();
        Map<String, List<Integer>> seriesDataMap = new LinkedHashMap<>();
        Map<String, String> stackGroupMap = new HashMap<>();
        
        for (Map<String, Object> row : mockResults) {
            String day = (String) row.get("day_name");
            String channel = (String) row.get("channel_name");
            Integer count = (Integer) row.get("conversion_count");
            String stackGroup = (String) row.get("stack_group");
            
            categorySet.add(day);
            seriesDataMap.computeIfAbsent(channel, k -> new ArrayList<>()).add(count);
            stackGroupMap.put(channel, stackGroup);
        }
        
        universalData.put("categories", new ArrayList<>(categorySet));
        
        // 构建系列数据
        List<Map<String, Object>> seriesList = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : seriesDataMap.entrySet()) {
            Map<String, Object> series = new HashMap<>();
            series.put("seriesId", entry.getKey().toLowerCase().replace(" ", "_"));
            series.put("seriesName", entry.getKey());
            series.put("seriesType", "line");
            series.put("stackGroup", stackGroupMap.get(entry.getKey()));
            series.put("values", entry.getValue());
            seriesList.add(series);
        }
        
        universalData.put("series", seriesList);
        
        // 样式配置
        Map<String, Object> styleConfig = new HashMap<>();
        styleConfig.put("showLegend", true);
        styleConfig.put("showTooltip", true);
        styleConfig.put("showGrid", true);
        universalData.put("styleConfig", styleConfig);
        
        return universalData;
    }

    /**
     * 使用Jolt转换为ECharts配置
     */
    public Map<String, Object> transformWithJolt(Map<String, Object> universalData) throws IOException {
        // 加载Jolt规范文件
        ClassPathResource resource = new ClassPathResource("jolt-specs/line-chart-stacked.json");
        String joltSpecContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        
        // 解析Jolt规范
        List<Object> joltSpec = JsonUtils.jsonToList(joltSpecContent);
        Chainr chainr = Chainr.fromSpec(joltSpec);
        
        // 执行Jolt转换
        Object transformedObj = chainr.transform(universalData);
        
        return (Map<String, Object>) transformedObj;
    }

    /**
     * 完整的验证流程
     */
    public ValidationResult validateStackedLineChart() {
        try {
            System.out.println("=== 堆叠折线图验证开始 ===");
            
            // 步骤1: 获取Mock数据
            System.out.println("步骤1: 获取Mock数据库查询结果...");
            List<Map<String, Object>> mockResults = getMockDatabaseResults();
            System.out.println("Mock数据记录数: " + mockResults.size());
            
            // 步骤2: 转换为通用JSON格式
            System.out.println("步骤2: 转换为通用JSON格式...");
            Map<String, Object> universalData = convertToUniversalFormat(mockResults);
            String universalJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(universalData);
            System.out.println("通用JSON格式:\n" + universalJson);
            
            // 步骤3: Jolt转换
            System.out.println("步骤3: 执行Jolt转换...");
            Map<String, Object> echartsConfig = transformWithJolt(universalData);
            String echartsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(echartsConfig);
            System.out.println("ECharts配置:\n" + echartsJson);
            
            // 步骤4: 验证结果
            System.out.println("步骤4: 验证转换结果...");
            ValidationResult result = validateResult(echartsConfig);
            
            System.out.println("=== 验证完成 ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("验证过程出错: " + e.getMessage());
            e.printStackTrace();
            return new ValidationResult(false, "验证失败: " + e.getMessage(), null);
        }
    }

    /**
     * 验证转换结果的正确性
     */
    private ValidationResult validateResult(Map<String, Object> echartsConfig) {
        List<String> validationMessages = new ArrayList<>();
        boolean isValid = true;
        
        // 验证基本结构
        if (!echartsConfig.containsKey("title")) {
            validationMessages.add("❌ 缺少title字段");
            isValid = false;
        } else {
            validationMessages.add("✅ title字段存在");
        }
        
        if (!echartsConfig.containsKey("xAxis")) {
            validationMessages.add("❌ 缺少xAxis字段");
            isValid = false;
        } else {
            validationMessages.add("✅ xAxis字段存在");
        }
        
        if (!echartsConfig.containsKey("series")) {
            validationMessages.add("❌ 缺少series字段");
            isValid = false;
        } else {
            List<Map<String, Object>> series = (List<Map<String, Object>>) echartsConfig.get("series");
            validationMessages.add("✅ series字段存在，包含 " + series.size() + " 个系列");
            
            // 验证堆叠配置
            boolean hasStackConfig = false;
            for (Map<String, Object> s : series) {
                if (s.containsKey("stack")) {
                    hasStackConfig = true;
                    break;
                }
            }
            
            if (hasStackConfig) {
                validationMessages.add("✅ 系列包含堆叠配置");
            } else {
                validationMessages.add("❌ 系列缺少堆叠配置");
                isValid = false;
            }
        }
        
        // 验证图例数据
        if (echartsConfig.containsKey("legend")) {
            Map<String, Object> legend = (Map<String, Object>) echartsConfig.get("legend");
            if (legend.containsKey("data")) {
                List<String> legendData = (List<String>) legend.get("data");
                validationMessages.add("✅ 图例数据存在，包含 " + legendData.size() + " 项");
            }
        }
        
        return new ValidationResult(isValid, String.join("\n", validationMessages), echartsConfig);
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final Map<String, Object> echartsConfig;
        
        public ValidationResult(boolean valid, String message, Map<String, Object> echartsConfig) {
            this.valid = valid;
            this.message = message;
            this.echartsConfig = echartsConfig;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Map<String, Object> getEchartsConfig() { return echartsConfig; }
    }
}
