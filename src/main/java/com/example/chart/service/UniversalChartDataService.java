package com.example.chart.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.chart.model.UniversalChartDataView;

/**
 * 统一图表数据服务
 * 提供模拟数据，支持所有图表类型的演示需求
 * 
 * @author Chart System
 * @version 1.0
 */
@Service
public class UniversalChartDataService {

    private final Random random = new Random();

    // 渠道名称列表
    private final List<String> channels = Arrays.asList(
            "Email", "Union Ads", "Video Ads", "Direct", "Search Engine");

    // 日期名称列表
    private final List<String> dayNames = Arrays.asList(
            "周一", "周二", "周三", "周四", "周五", "周六", "周日");

    // 月份名称列表
    private final List<String> monthNames = Arrays.asList(
            "一月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "十二月");

    // 产品类型列表
    private final List<String> productTypes = Arrays.asList(
            "电子产品", "服装", "食品", "图书", "家居");

    // 地区列表
    private final List<String> regions = Arrays.asList(
            "北京", "上海", "广州", "深圳", "杭州", "成都");

    /**
     * 获取所有模拟数据
     */
    public List<UniversalChartDataView> getAllData() {
        List<UniversalChartDataView> dataList = new ArrayList<>();

        // 生成时间序列数据（用于折线图、柱状图）
        dataList.addAll(generateTimeSeriesData());

        // 生成分类数据（用于饼图）
        dataList.addAll(generateCategoryData());

        // 生成雷达图数据
        dataList.addAll(generateRadarData());

        // 生成仪表盘数据
        dataList.addAll(generateGaugeData());

        return dataList;
    }

    /**
     * 根据图表类型获取数据
     */
    public List<UniversalChartDataView> getDataByChartType(String chartType) {
        List<UniversalChartDataView> allData = getAllData();

        // 根据图表类型过滤数据
        return allData.stream()
                .filter(data -> isDataSuitableForChartType(data, chartType))
                .collect(Collectors.toList());
    }

    /**
     * 生成时间序列数据（用于CARTESIAN类型图表）
     */
    private List<UniversalChartDataView> generateTimeSeriesData() {
        List<UniversalChartDataView> dataList = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        for (int day = 0; day < 7; day++) {
            for (String channel : channels) {
                UniversalChartDataView data = new UniversalChartDataView();

                // 基础信息
                data.setId((long) (day * channels.size() + channels.indexOf(channel) + 1));
                data.setTitle("营销数据分析");
                data.setChartType("time_series");
                data.setTheme("default");
                data.setDescription("营销渠道转换数据");
                data.setDataSource("marketing_system");
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());

                // 时间维度
                LocalDate currentDate = startDate.plusDays(day);
                data.setDate(currentDate);
                data.setDayName(dayNames.get(day));
                data.setMonth(currentDate.getMonthValue());
                data.setMonthName(monthNames.get(currentDate.getMonthValue() - 1));
                data.setYear(currentDate.getYear());
                data.setQuarter((currentDate.getMonthValue() - 1) / 3 + 1);
                data.setWeekNumber(day + 1);
                data.setTimestamp(System.currentTimeMillis() + day * 86400000L);

                // 分类数据
                data.setCategory("营销渠道");
                data.setSubCategory("数字营销");
                data.setChannelName(channel);
                data.setChannelType(getChannelType(channel));
                data.setProductName("产品" + (channels.indexOf(channel) + 1));
                data.setProductType(productTypes.get(channels.indexOf(channel) % productTypes.size()));
                data.setRegion(regions.get(channels.indexOf(channel) % regions.size()));
                data.setDepartment("营销部");

                // 数值数据
                data.setValue(generateRandomValue(channel, day));
                data.setConversionCount(generateConversionCount(channel, day));
                data.setClickCount(data.getConversionCount() * (5 + random.nextInt(10)));
                data.setViewCount(data.getClickCount() * (3 + random.nextInt(5)));
                data.setPercentage(data.getConversionCount() / 100.0);
                data.setRatio(data.getConversionCount() / 50.0);
                data.setAmount((double) (data.getConversionCount() * (100 + random.nextInt(200))));
                data.setQuantity(data.getConversionCount());

                // 配置字段
                data.setColor(getChannelColor(channel));
                data.setStyle("normal");
                data.setRadius("50%");
                data.setCenter("['50%', '50%']");
                data.setStackGroup(isStackableChart(channel) ? "Total" : null);
                data.setSmoothStyle(channel.contains("Search") || channel.contains("Email"));
                data.setBoundaryGap(false);
                data.setExtraConfig("{}");

                dataList.add(data);
            }
        }

        return dataList;
    }

    /**
     * 生成分类数据（用于PIE类型图表）
     */
    private List<UniversalChartDataView> generateCategoryData() {
        List<UniversalChartDataView> dataList = new ArrayList<>();

        for (int i = 0; i < channels.size(); i++) {
            String channel = channels.get(i);
            UniversalChartDataView data = new UniversalChartDataView();

            // 基础信息
            data.setId((long) (100 + i));
            data.setTitle("渠道分布分析");
            data.setChartType("category");
            data.setTheme("default");
            data.setDescription("各渠道占比数据");
            data.setDataSource("analytics_system");
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());

            // 分类数据
            data.setCategory("渠道分布");
            data.setChannelName(channel);
            data.setChannelType(getChannelType(channel));

            // 数值数据
            int totalValue = 150 + random.nextInt(100);
            data.setValue((double) totalValue);
            data.setConversionCount(totalValue);
            data.setPercentage(totalValue / 500.0 * 100);

            // 配置字段
            data.setColor(getChannelColor(channel));
            data.setRadius(i == 0 ? "60%" : "50%"); // 第一个扇区突出显示
            data.setCenter("['50%', '50%']");

            dataList.add(data);
        }

        return dataList;
    }

    /**
     * 生成雷达图数据
     */
    private List<UniversalChartDataView> generateRadarData() {
        List<UniversalChartDataView> dataList = new ArrayList<>();
        List<String> indicators = Arrays.asList("销售", "管理", "信息技术", "客服", "研发", "市场");

        for (int i = 0; i < 2; i++) { // 生成两组雷达数据
            UniversalChartDataView data = new UniversalChartDataView();

            data.setId((long) (200 + i));
            data.setTitle("能力雷达图");
            data.setChartType("radar");
            data.setTheme("default");
            data.setCategory("能力评估");
            data.setChannelName("团队" + (i + 1));

            // 为雷达图生成多维度数据
            StringBuilder radarValues = new StringBuilder("[");
            for (int j = 0; j < indicators.size(); j++) {
                int value = 60 + random.nextInt(40);
                radarValues.append(value);
                if (j < indicators.size() - 1)
                    radarValues.append(",");
            }
            radarValues.append("]");

            data.setExtraConfig(radarValues.toString());
            data.setValue(80.0 + random.nextInt(20));

            dataList.add(data);
        }

        return dataList;
    }

    /**
     * 生成仪表盘数据
     */
    private List<UniversalChartDataView> generateGaugeData() {
        List<UniversalChartDataView> dataList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            UniversalChartDataView data = new UniversalChartDataView();

            data.setId((long) (300 + i));
            data.setTitle("性能指标");
            data.setChartType("gauge");
            data.setTheme("default");
            data.setCategory("性能监控");
            data.setChannelName("指标" + (i + 1));

            // 仪表盘数值
            double gaugeValue = 60 + random.nextInt(40);
            data.setValue(gaugeValue);
            data.setPercentage(gaugeValue);

            dataList.add(data);
        }

        return dataList;
    }

    // 辅助方法
    private String getChannelType(String channel) {
        switch (channel) {
            case "Email":
                return "邮件营销";
            case "Union Ads":
                return "联盟广告";
            case "Video Ads":
                return "视频广告";
            case "Direct":
                return "直接访问";
            case "Search Engine":
                return "搜索引擎";
            default:
                return "其他";
        }
    }

    private String getChannelColor(String channel) {
        switch (channel) {
            case "Email":
                return "#5470c6";
            case "Union Ads":
                return "#91cc75";
            case "Video Ads":
                return "#fac858";
            case "Direct":
                return "#ee6666";
            case "Search Engine":
                return "#73c0de";
            default:
                return "#9a60b4";
        }
    }

    private double generateRandomValue(String channel, int day) {
        int base;
        switch (channel) {
            case "Email":
                base = 120;
                break;
            case "Union Ads":
                base = 200;
                break;
            case "Video Ads":
                base = 150;
                break;
            case "Direct":
                base = 320;
                break;
            case "Search Engine":
                base = 820;
                break;
            default:
                base = 100;
                break;
        }
        return base + random.nextInt(100) + day * 10;
    }

    private int generateConversionCount(String channel, int day) {
        return (int) (generateRandomValue(channel, day) * 0.1);
    }

    private boolean isStackableChart(String channel) {
        return Arrays.asList("Email", "Union Ads", "Video Ads").contains(channel);
    }

    private boolean isDataSuitableForChartType(UniversalChartDataView data, String chartType) {
        if (chartType.contains("line") || chartType.contains("bar")) {
            return "time_series".equals(data.getChartType());
        } else if (chartType.contains("pie")) {
            return "category".equals(data.getChartType());
        } else if (chartType.contains("radar")) {
            return "radar".equals(data.getChartType());
        } else if (chartType.contains("gauge")) {
            return "gauge".equals(data.getChartType());
        }
        return true;
    }
}
