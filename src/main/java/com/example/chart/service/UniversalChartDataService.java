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

        // 根据图表类型过滤数据并调整配置
        return allData.stream()
                .filter(data -> isDataSuitableForChartType(data, chartType))
                .map(data -> adjustDataForChartType(data, chartType))
                .collect(Collectors.toList());
    }

    /**
     * 根据图表类型调整数据配置
     */
    private UniversalChartDataView adjustDataForChartType(UniversalChartDataView data, String chartType) {
        // 创建数据副本以避免修改原始数据
        UniversalChartDataView adjustedData = new UniversalChartDataView();

        // 复制所有基础属性
        copyBasicProperties(adjustedData, data);

        // 新的12字段模型不包含图表配置字段
        // 数据已经通过 copyBasicProperties 复制完成

        return adjustedData;
    }

    /**
     * 复制基础属性
     */
    private void copyBasicProperties(UniversalChartDataView target, UniversalChartDataView source) {
        // 复制12个核心业务字段
        target.setId(source.getId());
        target.setYear(source.getYear());
        target.setMonth(source.getMonth());
        target.setDate(source.getDate());
        target.setCategory(source.getCategory());
        target.setChannel(source.getChannel());
        target.setProduct(source.getProduct());
        target.setRegion(source.getRegion());
        target.setAmount(source.getAmount());
        target.setQuantity(source.getQuantity());
        target.setPercentage(source.getPercentage());
        target.setSalesman(source.getSalesman());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
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

                // 设置12个核心业务字段
                data.setId((long) (day * channels.size() + channels.indexOf(channel) + 1));

                // 时间维度
                LocalDate currentDate = startDate.plusDays(day);
                data.setYear(String.valueOf(currentDate.getYear()));
                data.setMonth(String.format("%02d", currentDate.getMonthValue()));
                data.setDate(currentDate.toString());

                // 业务分类
                String[] categories = { "电子产品", "服装", "食品", "家居", "图书" };
                String[] products = { "iPhone 15", "MacBook Pro", "iPad", "AirPods", "Apple Watch",
                        "Nike运动鞋", "Adidas外套", "优衣库T恤", "星巴克咖啡", "可口可乐" };
                String[] regions = { "华北", "华东", "华南", "华中", "西北", "西南", "东北" };
                String[] salesmen = { "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十" };

                data.setCategory(categories[channels.indexOf(channel) % categories.length]);
                data.setChannel(channel);
                data.setProduct(products[channels.indexOf(channel) % products.length]);
                data.setRegion(regions[channels.indexOf(channel) % regions.length]);
                data.setSalesman(salesmen[day % salesmen.length]);

                // 数值数据
                int conversionCount = generateConversionCount(channel, day);
                data.setAmount((double) (conversionCount * (100 + random.nextInt(200))));
                data.setQuantity(conversionCount);
                data.setPercentage(conversionCount / 100.0);

                // 系统字段
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());

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

            // 设置12个核心业务字段
            data.setId((long) (100 + i));

            // 时间维度
            LocalDate currentDate = LocalDate.now();
            data.setYear(String.valueOf(currentDate.getYear()));
            data.setMonth(String.format("%02d", currentDate.getMonthValue()));
            data.setDate(currentDate.toString());

            // 业务分类
            data.setCategory("渠道分布");
            data.setChannel(channel);
            data.setProduct("产品" + (i + 1));
            data.setRegion("华东");
            data.setSalesman("分析师" + (i + 1));

            // 数值数据
            int totalValue = 150 + random.nextInt(100);
            data.setAmount((double) totalValue * 100);
            data.setQuantity(totalValue);
            data.setPercentage(totalValue / 500.0 * 100);

            // 系统字段
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());

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

            // 设置12个核心业务字段
            data.setId((long) (200 + i));

            // 时间维度
            LocalDate currentDate = LocalDate.now();
            data.setYear(String.valueOf(currentDate.getYear()));
            data.setMonth(String.format("%02d", currentDate.getMonthValue()));
            data.setDate(currentDate.toString());

            // 业务分类
            data.setCategory("能力评估");
            data.setChannel("团队" + (i + 1));
            data.setProduct("评估报告" + (i + 1));
            data.setRegion("总部");
            data.setSalesman("评估师" + (i + 1));

            // 数值数据
            int avgValue = 80 + random.nextInt(20);
            data.setAmount((double) avgValue * 10);
            data.setQuantity(avgValue);
            data.setPercentage((double) avgValue);

            // 系统字段
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());

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

            // 设置12个核心业务字段
            data.setId((long) (300 + i));

            // 时间维度
            LocalDate currentDate = LocalDate.now();
            data.setYear(String.valueOf(currentDate.getYear()));
            data.setMonth(String.format("%02d", currentDate.getMonthValue()));
            data.setDate(currentDate.toString());

            // 业务分类
            data.setCategory("性能监控");
            data.setChannel("指标" + (i + 1));
            data.setProduct("监控系统" + (i + 1));
            data.setRegion("数据中心");
            data.setSalesman("系统管理员");

            // 数值数据
            double gaugeValue = 60 + random.nextInt(40);
            data.setAmount(gaugeValue * 100);
            data.setQuantity((int) gaugeValue);
            data.setPercentage(gaugeValue);

            // 系统字段
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());

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
        // 新的12字段模型不包含chartType字段，所有数据都适用于所有图表类型
        // 通过业务分类来判断数据适用性
        if (chartType.contains("line") || chartType.contains("bar")) {
            return data.getCategory() != null;
        } else if (chartType.contains("pie")) {
            return data.getCategory() != null;
        } else if (chartType.contains("radar")) {
            return "能力评估".equals(data.getCategory());
        } else if (chartType.contains("gauge")) {
            return "性能监控".equals(data.getCategory());
        }
        return true;
    }
}
