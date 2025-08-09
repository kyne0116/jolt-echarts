package com.example.chart.controller;

import com.example.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 图表配置管理控制器
 * 提供图表类型映射、文件路径映射等配置信息
 */
@RestController
@RequestMapping("/api/chart/config")
public class ChartConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ChartConfigController.class);

    /**
     * 获取图表类型映射配置
     */
    @GetMapping("/chart-types")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartTypes() {
        logger.info("📋 [配置接口] 获取图表类型映射配置");
        
        Map<String, Object> response = new HashMap<>();
        
        // 文件路径到图表ID的映射
        Map<String, String> pathToChartId = new HashMap<>();
        pathToChartId.put("折线图/基础折线图.json", "basic_line_chart");
        pathToChartId.put("折线图/基础平滑折线图.json", "smooth_line_chart");
        pathToChartId.put("折线图/折线图堆叠.json", "stacked_line_chart");
        pathToChartId.put("柱状图/基础柱状图.json", "basic_bar_chart");
        pathToChartId.put("柱状图/堆叠柱状图.json", "stacked_bar_chart");
        pathToChartId.put("饼图/富文本标签.json", "basic_pie_chart");
        pathToChartId.put("饼图/圆角环形图.json", "doughnut_chart");
        pathToChartId.put("雷达图/基础雷达图.json", "basic_radar_chart");
        pathToChartId.put("仪表盘/基础仪表盘.json", "basic_gauge_chart");
        pathToChartId.put("仪表盘/进度仪表盘.json", "progress_gauge_chart");
        pathToChartId.put("仪表盘/等级仪表盘.json", "grade_gauge_chart");
        
        // 图表ID到文件路径的映射
        Map<String, String> chartIdToPath = new HashMap<>();
        for (Map.Entry<String, String> entry : pathToChartId.entrySet()) {
            chartIdToPath.put(entry.getValue(), entry.getKey());
        }
        
        // 图表类型分类
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("折线图", Arrays.asList("basic_line_chart", "smooth_line_chart", "stacked_line_chart"));
        categories.put("柱状图", Arrays.asList("basic_bar_chart", "stacked_bar_chart"));
        categories.put("饼图", Arrays.asList("basic_pie_chart", "doughnut_chart"));
        categories.put("雷达图", Arrays.asList("basic_radar_chart"));
        categories.put("仪表盘", Arrays.asList("basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart"));
        
        response.put("pathToChartId", pathToChartId);
        response.put("chartIdToPath", chartIdToPath);
        response.put("categories", categories);
        response.put("totalCharts", pathToChartId.size());
        
        logger.info("✅ [配置接口] 返回 {} 个图表类型配置", pathToChartId.size());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 获取图表特定配置
     */
    @GetMapping("/chart-specific/{chartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartSpecificConfig(@PathVariable String chartId) {
        logger.info("📋 [配置接口] 获取图表特定配置: {}", chartId);
        
        Map<String, Object> config = new HashMap<>();
        
        // 根据图表类型设置特定配置
        switch (chartId) {
            case "basic_line_chart":
                config.put("smooth", false);
                config.put("stack", null);
                config.put("areaStyle", null);
                config.put("description", "基础折线图：直线连接的折线图");
                break;
                
            case "smooth_line_chart":
                config.put("smooth", true);
                config.put("stack", null);
                config.put("areaStyle", null);
                config.put("description", "平滑折线图：曲线连接的折线图");
                break;
                
            case "stacked_line_chart":
                config.put("smooth", false);
                config.put("stack", "Total");
                config.put("areaStyle", new HashMap<>());
                config.put("description", "堆叠折线图：多个数据系列垂直堆叠显示");
                break;
                
            case "basic_bar_chart":
                config.put("smooth", null);
                config.put("stack", null);
                config.put("boundaryGap", true);
                config.put("description", "基础柱状图：垂直柱状显示");
                break;
                
            case "stacked_bar_chart":
                config.put("smooth", null);
                config.put("stack", "Total");
                config.put("boundaryGap", true);
                config.put("description", "堆叠柱状图：多个数据系列垂直堆叠的柱状图");
                break;
                
            case "basic_pie_chart":
                config.put("radius", "50%");
                config.put("center", Arrays.asList("50%", "50%"));
                config.put("description", "基础饼图：圆形饼状图");
                break;
                
            case "doughnut_chart":
                config.put("radius", Arrays.asList("40%", "70%"));
                config.put("center", Arrays.asList("50%", "50%"));
                config.put("description", "圆环图：中空的环形饼图");
                break;
                
            case "basic_radar_chart":
                config.put("radius", "60%");
                config.put("description", "基础雷达图：多维数据雷达显示");
                break;
                
            case "basic_gauge_chart":
                config.put("min", 0);
                config.put("max", 100);
                config.put("description", "基础仪表盘：指针式仪表显示");
                break;
                
            default:
                config.put("description", "未知图表类型");
                break;
        }
        
        config.put("chartId", chartId);
        config.put("timestamp", System.currentTimeMillis());
        
        logger.info("✅ [配置接口] 返回图表 {} 的特定配置", chartId);
        return ResponseEntity.ok(ApiResponse.ok(config));
    }

    /**
     * 获取图表预处理规则
     */
    @GetMapping("/preprocessing-rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreprocessingRules() {
        logger.info("📋 [配置接口] 获取图表预处理规则");
        
        Map<String, Object> rules = new HashMap<>();
        
        // 堆叠图表规则
        Map<String, Object> stackedRules = new HashMap<>();
        stackedRules.put("requiresAreaStyle", true);
        stackedRules.put("stackValue", "Total");
        stackedRules.put("smooth", false);
        
        // 平滑图表规则
        Map<String, Object> smoothRules = new HashMap<>();
        smoothRules.put("smooth", true);
        smoothRules.put("requiresAreaStyle", false);
        
        // 基础图表规则
        Map<String, Object> basicRules = new HashMap<>();
        basicRules.put("smooth", false);
        basicRules.put("requiresAreaStyle", false);
        
        rules.put("stacked_line_chart", stackedRules);
        rules.put("smooth_line_chart", smoothRules);
        rules.put("basic_line_chart", basicRules);
        
        logger.info("✅ [配置接口] 返回预处理规则");
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }
}
