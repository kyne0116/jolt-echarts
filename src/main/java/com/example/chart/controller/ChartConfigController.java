package com.example.chart.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.ApiResponse;
import com.example.chart.service.ConfigurableChartMappingService;

/**
 * 图表配置管理控制器
 * 提供图表类型映射、文件路径映射等配置信息
 * 重构后使用配置化服务，解决硬编码问题
 */
@RestController
@RequestMapping("/api/chart/config")
public class ChartConfigController {

        private static final Logger logger = LoggerFactory.getLogger(ChartConfigController.class);

        @Autowired
        private ConfigurableChartMappingService mappingService;

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
                pathToChartId.put("饼图/圆角环形图.json", "ring_chart");
                pathToChartId.put("饼图/嵌套饼图.json", "nested_pie_chart");
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
                categories.put("饼图", Arrays.asList("basic_pie_chart", "ring_chart", "nested_pie_chart"));
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

                        case "ring_chart":
                        case "nested_pie_chart":
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

                // 饼图规则
                Map<String, Object> pieRules = new HashMap<>();
                pieRules.put("type", "pie");
                pieRules.put("radius", "50%");
                pieRules.put("center", Arrays.asList("50%", "50%"));
                pieRules.put("requiresValueName", true);

                // 圆环图规则
                Map<String, Object> doughnutRules = new HashMap<>();
                doughnutRules.put("type", "pie");
                doughnutRules.put("radius", Arrays.asList("40%", "70%"));
                doughnutRules.put("center", Arrays.asList("50%", "50%"));
                doughnutRules.put("requiresValueName", true);

                rules.put("stacked_line_chart", stackedRules);
                rules.put("smooth_line_chart", smoothRules);
                rules.put("basic_line_chart", basicRules);
                rules.put("basic_pie_chart", pieRules);
                rules.put("ring_chart", doughnutRules);
                rules.put("nested_pie_chart", doughnutRules);

                logger.info("✅ [配置接口] 返回预处理规则");
                return ResponseEntity.ok(ApiResponse.ok(rules));
        }

        /**
         * 获取文件路径到图表ID的映射关系
         */
        @GetMapping("/file-path-mappings")
        public ResponseEntity<ApiResponse<Map<String, String>>> getFilePathMappings() {
                logger.info("📋 [配置接口] 获取文件路径映射关系");

                Map<String, String> pathToChartId = new HashMap<>();
                pathToChartId.put("折线图/基础折线图.json", "basic_line_chart");
                pathToChartId.put("折线图/基础平滑折线图.json", "smooth_line_chart");
                pathToChartId.put("折线图/折线图堆叠.json", "stacked_line_chart");
                pathToChartId.put("柱状图/基础柱状图.json", "basic_bar_chart");
                pathToChartId.put("柱状图/堆叠柱状图.json", "stacked_bar_chart");
                pathToChartId.put("饼图/富文本标签.json", "basic_pie_chart");
                pathToChartId.put("饼图/圆角环形图.json", "ring_chart");
                pathToChartId.put("饼图/嵌套饼图.json", "nested_pie_chart");
                pathToChartId.put("雷达图/基础雷达图.json", "basic_radar_chart");
                pathToChartId.put("仪表盘/基础仪表盘.json", "basic_gauge_chart");
                pathToChartId.put("仪表盘/进度仪表盘.json", "progress_gauge_chart");
                pathToChartId.put("仪表盘/等级仪表盘.json", "grade_gauge_chart");

                logger.info("✅ [配置接口] 返回 {} 个文件路径映射", pathToChartId.size());
                return ResponseEntity.ok(ApiResponse.ok(pathToChartId));
        }

        /**
         * 根据文件路径获取图表ID
         */
        @GetMapping("/chart-id-by-path")
        public ResponseEntity<ApiResponse<String>> getChartIdByFilePath(@RequestParam String filePath) {
                logger.info("📋 [配置接口] 根据文件路径获取图表ID: {}", filePath);

                Map<String, String> pathToChartId = new HashMap<>();
                pathToChartId.put("折线图/基础折线图.json", "basic_line_chart");
                pathToChartId.put("折线图/基础平滑折线图.json", "smooth_line_chart");
                pathToChartId.put("折线图/折线图堆叠.json", "stacked_line_chart");
                pathToChartId.put("柱状图/基础柱状图.json", "basic_bar_chart");
                pathToChartId.put("柱状图/堆叠柱状图.json", "stacked_bar_chart");
                pathToChartId.put("饼图/富文本标签.json", "basic_pie_chart");
                pathToChartId.put("饼图/圆角环形图.json", "ring_chart");
                pathToChartId.put("饼图/嵌套饼图.json", "nested_pie_chart");
                pathToChartId.put("雷达图/基础雷达图.json", "basic_radar_chart");
                pathToChartId.put("仪表盘/基础仪表盘.json", "basic_gauge_chart");
                pathToChartId.put("仪表盘/进度仪表盘.json", "progress_gauge_chart");
                pathToChartId.put("仪表盘/等级仪表盘.json", "grade_gauge_chart");

                String chartId = pathToChartId.get(filePath);
                if (chartId != null) {
                        logger.info("✅ [配置接口] 找到对应图表ID: {} -> {}", filePath, chartId);
                        return ResponseEntity.ok(ApiResponse.ok(chartId));
                } else {
                        logger.warn("⚠️ [配置接口] 未找到对应的图表ID: {}", filePath);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("CHART_ID_NOT_FOUND", "未找到对应的图表ID: " + filePath));
                }
        }

        /**
         * 获取图表测试数据
         */
        @GetMapping("/test-data/{chartType}")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getTestData(@PathVariable String chartType) {
                logger.info("📋 [配置接口] 获取图表测试数据: {}", chartType);

                Map<String, Object> testData = new HashMap<>();

                switch (chartType) {
                        case "line":
                        case "basic_line_chart":
                        case "smooth_line_chart":
                                // 折线图测试数据
                                testData.put("title", Map.of("text", "折线图测试"));
                                testData.put("tooltip", Map.of("trigger", "axis"));
                                testData.put("xAxis", Map.of(
                                                "type", "category",
                                                "data", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日")));
                                testData.put("yAxis", Map.of("type", "value"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "Email",
                                                                "type", "line",
                                                                "data",
                                                                Arrays.asList(120, 132, 101, 134, 90, 230, 210)),
                                                Map.of(
                                                                "name", "Union Ads",
                                                                "type", "line",
                                                                "data",
                                                                Arrays.asList(220, 182, 191, 234, 290, 330, 310)),
                                                Map.of(
                                                                "name", "Video Ads",
                                                                "type", "line",
                                                                "data",
                                                                Arrays.asList(150, 232, 201, 154, 190, 330, 410))));
                                break;

                        case "stacked_line_chart":
                                // 堆叠折线图测试数据
                                testData.put("title", Map.of("text", "堆叠折线图测试"));
                                testData.put("tooltip", Map.of("trigger", "axis"));
                                testData.put("xAxis", Map.of(
                                                "type", "category",
                                                "data", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日")));
                                testData.put("yAxis", Map.of("type", "value"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "Email",
                                                                "type", "line",
                                                                "stack", "Total",
                                                                "areaStyle", new HashMap<>(),
                                                                "data",
                                                                Arrays.asList(120, 132, 101, 134, 90, 230, 210)),
                                                Map.of(
                                                                "name", "Union Ads",
                                                                "type", "line",
                                                                "stack", "Total",
                                                                "areaStyle", new HashMap<>(),
                                                                "data",
                                                                Arrays.asList(220, 182, 191, 234, 290, 330, 310)),
                                                Map.of(
                                                                "name", "Video Ads",
                                                                "type", "line",
                                                                "stack", "Total",
                                                                "areaStyle", new HashMap<>(),
                                                                "data",
                                                                Arrays.asList(150, 232, 201, 154, 190, 330, 410))));
                                break;

                        case "basic_pie_chart":
                                // 基础饼图测试数据
                                testData.put("title", Map.of("text", "基础饼图测试"));
                                testData.put("tooltip", Map.of(
                                                "trigger", "item",
                                                "formatter", "{a} <br/>{b}: {c} ({d}%)"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "访问来源",
                                                                "type", "pie",
                                                                "radius", "50%",
                                                                "center", Arrays.asList("50%", "50%"),
                                                                "data", Arrays.asList(
                                                                                Map.of("value", 335, "name", "直接访问"),
                                                                                Map.of("value", 310, "name", "邮件营销"),
                                                                                Map.of("value", 234, "name", "联盟广告"),
                                                                                Map.of("value", 135, "name", "视频广告"),
                                                                                Map.of("value", 1548, "name",
                                                                                                "搜索引擎")))));
                                break;

                        case "ring_chart":
                        case "nested_pie_chart":
                                // 圆环图测试数据 - 修复颜色图例问题
                                testData.put("title", Map.of("text", "动态营销渠道分析"));
                                testData.put("tooltip", Map.of(
                                                "trigger", "item",
                                                "formatter", "{a} <br/>{b}: {c} ({d}%)"));
                                // 添加图例配置，包含颜色信息
                                testData.put("legend", Map.of(
                                                "orient", "vertical",
                                                "left", "left",
                                                "data", Arrays.asList("直接访问", "邮件营销", "联盟广告", "视频广告", "搜索引擎")));
                                // 添加颜色配置
                                testData.put("color", Arrays.asList(
                                                "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "访问来源",
                                                                "type", "pie",
                                                                "radius", Arrays.asList("40%", "70%"),
                                                                "center", Arrays.asList("50%", "50%"),
                                                                "avoidLabelOverlap", false,
                                                                "itemStyle", Map.of(
                                                                                "borderRadius", 10,
                                                                                "borderColor", "#fff",
                                                                                "borderWidth", 2),
                                                                "label", Map.of(
                                                                                "show", false,
                                                                                "position", "center"),
                                                                "emphasis", Map.of(
                                                                                "label", Map.of(
                                                                                                "show", true,
                                                                                                "fontSize", 30,
                                                                                                "fontWeight", "bold")),
                                                                "labelLine", Map.of("show", false),
                                                                "data", Arrays.asList(
                                                                                Map.of("value", 335, "name", "直接访问"),
                                                                                Map.of("value", 310, "name", "邮件营销"),
                                                                                Map.of("value", 234, "name", "联盟广告"),
                                                                                Map.of("value", 135, "name", "视频广告"),
                                                                                Map.of("value", 1548, "name",
                                                                                                "搜索引擎")))));
                                break;

                        case "bar_chart":
                                // 基础柱状图测试数据
                                testData.put("title", Map.of("text", "月度销售数据分析"));
                                testData.put("tooltip", Map.of(
                                                "trigger", "axis",
                                                "axisPointer", Map.of("type", "shadow")));
                                testData.put("legend", Map.of(
                                                "data", Arrays.asList("销售额"),
                                                "top", "8%"));
                                testData.put("grid", Map.of(
                                                "left", "3%",
                                                "right", "4%",
                                                "bottom", "3%",
                                                "containLabel", true));
                                testData.put("xAxis", Map.of(
                                                "type", "category",
                                                "data", Arrays.asList("一月", "二月", "三月", "四月", "五月", "六月")));
                                testData.put("yAxis", Map.of("type", "value"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "销售额",
                                                                "type", "bar",
                                                                "data", Arrays.asList(120, 200, 150, 80, 70, 110),
                                                                "itemStyle", Map.of("color", "#5470c6"))));
                                break;

                        case "stacked_bar_chart":
                                // 堆叠柱状图测试数据
                                testData.put("title", Map.of("text", "周度访问来源统计"));
                                testData.put("tooltip", Map.of(
                                                "trigger", "axis",
                                                "axisPointer", Map.of("type", "shadow")));
                                testData.put("legend", Map.of(
                                                "data", Arrays.asList("直接访问", "邮件营销", "联盟广告", "视频广告", "搜索引擎"),
                                                "top", "8%"));
                                testData.put("grid", Map.of(
                                                "left", "3%",
                                                "right", "4%",
                                                "bottom", "3%",
                                                "containLabel", true));
                                testData.put("xAxis", Map.of(
                                                "type", "category",
                                                "data", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日")));
                                testData.put("yAxis", Map.of("type", "value"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "直接访问",
                                                                "type", "bar",
                                                                "stack", "总量",
                                                                "data",
                                                                Arrays.asList(320, 302, 301, 334, 390, 330, 320)),
                                                Map.of(
                                                                "name", "邮件营销",
                                                                "type", "bar",
                                                                "stack", "总量",
                                                                "data",
                                                                Arrays.asList(120, 132, 101, 134, 90, 230, 210)),
                                                Map.of(
                                                                "name", "联盟广告",
                                                                "type", "bar",
                                                                "stack", "总量",
                                                                "data",
                                                                Arrays.asList(220, 182, 191, 234, 290, 330, 310)),
                                                Map.of(
                                                                "name", "视频广告",
                                                                "type", "bar",
                                                                "stack", "总量",
                                                                "data",
                                                                Arrays.asList(150, 212, 201, 154, 190, 330, 410)),
                                                Map.of(
                                                                "name", "搜索引擎",
                                                                "type", "bar",
                                                                "stack", "总量",
                                                                "data",
                                                                Arrays.asList(820, 832, 901, 934, 1290, 1330, 1320))));
                                break;

                        default:
                                // 默认测试数据
                                testData.put("title", Map.of("text", "默认测试数据"));
                                testData.put("xAxis", Map.of(
                                                "type", "category",
                                                "data", Arrays.asList("A", "B", "C", "D", "E")));
                                testData.put("yAxis", Map.of("type", "value"));
                                testData.put("series", Arrays.asList(
                                                Map.of(
                                                                "name", "测试数据",
                                                                "type", "line",
                                                                "data", Arrays.asList(120, 280, 150, 320, 180))));
                                break;
                }

                testData.put("legend", Map.of("data",
                                ((List<Map<String, Object>>) testData.get("series")).stream()
                                                .map(s -> s.get("name"))
                                                .toArray()));

                logger.info("✅ [配置接口] 返回图表 {} 的测试数据", chartType);
                return ResponseEntity.ok(ApiResponse.ok(testData));
        }
        
        /**
         * 获取模板分类信息
         */
        @GetMapping("/template-categories")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplateCategories() {
                logger.info("📋 [配置接口] 获取模板分类信息");
                
                Map<String, Object> categories = new HashMap<>();
                
                // 统计信息
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalTypes", 14);
                stats.put("completedTypes", 6);
                stats.put("developmentTypes", 6);
                stats.put("pendingTypes", 2);
                
                // 分类详细信息
                List<Map<String, Object>> categoryDetails = new ArrayList<>();
                
                // CARTESIAN 类型详情
                Map<String, Object> cartesian = new HashMap<>();
                cartesian.put("type", "cartesian");
                cartesian.put("name", "CARTESIAN (直角坐标系)");
                cartesian.put("description", "折线图、柱状图、面积图等");
                cartesian.put("total", 6);
                cartesian.put("completed", 5);
                cartesian.put("development", 0);
                cartesian.put("pending", 1);
                List<Map<String, Object>> cartesianCharts = Arrays.asList(
                    createChartStatus("basic_line_chart", "基础折线图", "completed"),
                    createChartStatus("smooth_line_chart", "平滑折线图", "completed"),
                    createChartStatus("stacked_line_chart", "堆叠折线图", "completed"),
                    createChartStatus("basic_bar_chart", "基础柱状图", "completed"),
                    createChartStatus("stacked_bar_chart", "堆叠柱状图", "completed"),
                    createChartStatus("basic_area_chart", "基础面积图", "pending")
                );
                cartesian.put("charts", cartesianCharts);
                categoryDetails.add(cartesian);
                
                // PIE 类型详情
                Map<String, Object> pie = new HashMap<>();
                pie.put("type", "pie");
                pie.put("name", "PIE (饼图类)");
                pie.put("description", "饼图、环形图、玫瑰图等");
                pie.put("total", 4);
                pie.put("completed", 1);
                pie.put("development", 2);
                pie.put("pending", 1);
                List<Map<String, Object>> pieCharts = Arrays.asList(
                    createChartStatus("basic_pie_chart", "基础饼图", "development"),
                    createChartStatus("doughnut_chart", "环形图", "development"),
                    createChartStatus("rose_chart", "玫瑰图", "pending"),
                    createChartStatus("pie_chart", "饼图(兼容)", "completed")
                );
                pie.put("charts", pieCharts);
                categoryDetails.add(pie);
                
                // RADAR 类型详情
                Map<String, Object> radar = new HashMap<>();
                radar.put("type", "radar");
                radar.put("name", "RADAR (雷达图类)");
                radar.put("description", "雷达图、极坐标图等");
                radar.put("total", 2);
                radar.put("completed", 0);
                radar.put("development", 1);
                radar.put("pending", 1);
                List<Map<String, Object>> radarCharts = Arrays.asList(
                    createChartStatus("basic_radar_chart", "基础雷达图", "development"),
                    createChartStatus("filled_radar_chart", "填充雷达图", "pending")
                );
                radar.put("charts", radarCharts);
                categoryDetails.add(radar);
                
                // GAUGE 类型详情
                Map<String, Object> gauge = new HashMap<>();
                gauge.put("type", "gauge");
                gauge.put("name", "GAUGE (仪表盘类)");
                gauge.put("description", "仪表盘、进度条等");
                gauge.put("total", 3);
                gauge.put("completed", 0);
                gauge.put("development", 3);
                gauge.put("pending", 0);
                List<Map<String, Object>> gaugeCharts = Arrays.asList(
                    createChartStatus("basic_gauge_chart", "基础仪表盘", "development"),
                    createChartStatus("progress_gauge_chart", "进度仪表盘", "development"),
                    createChartStatus("grade_gauge_chart", "等级仪表盘", "development")
                );
                gauge.put("charts", gaugeCharts);
                categoryDetails.add(gauge);
                
                categories.put("stats", stats);
                categories.put("categories", categoryDetails);
                
                logger.info("✅ [配置接口] 返回模板分类信息");
                return ResponseEntity.ok(ApiResponse.ok(categories));
        }
        
        /**
         * 创建图表状态信息
         */
        private Map<String, Object> createChartStatus(String id, String name, String status) {
                Map<String, Object> chart = new HashMap<>();
                chart.put("id", id);
                chart.put("name", name);
                chart.put("status", status);
                return chart;
        }
}
