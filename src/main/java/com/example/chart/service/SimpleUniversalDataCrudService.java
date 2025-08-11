package com.example.chart.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.chart.model.UniversalChartDataView;

/**
 * 简化的UniversalChartDataView CRUD服务
 * 专注于40个固定字段的基本增删改查操作
 * 
 * @author Chart System
 * @version 1.0 - 简化版本
 */
@Service
public class SimpleUniversalDataCrudService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUniversalDataCrudService.class);

    // 内存存储
    private final Map<Long, UniversalChartDataView> dataStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void initialize() {
        generateSampleData();
        logger.info("✅ 简化数据CRUD服务初始化完成，生成了 {} 条示例数据", dataStore.size());
    }

    /**
     * 生成示例数据 - 优化年份和地区分布以支持筛选测试
     */
    private void generateSampleData() {
        // 生成40条示例数据 - 确保筛选条件有足够的测试数据
        String[] categories = { "电子产品", "服装", "食品", "家居", "图书" };
        String[] channels = { "线上", "线下", "移动端", "电话销售", "直销" };
        String[] products = { "iPhone 15", "MacBook Pro", "iPad", "AirPods", "Apple Watch",
                "Nike运动鞋", "Adidas外套", "优衣库T恤", "星巴克咖啡", "可口可乐" };
        String[] regions = { "华北", "华东", "华南", "华中", "西北", "西南", "东北" };
        String[] salesmen = { "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十" };
        String[] years = { "2023", "2024", "2025" }; // 固定年份选项，便于筛选测试

        Random random = new Random();

        for (int i = 0; i < 40; i++) {
            UniversalChartDataView data = new UniversalChartDataView();

            // 主键ID
            data.setId(idGenerator.getAndIncrement());

            // 时间维度数据 - 均匀分布年份
            String selectedYear = years[i % years.length];
            data.setYear(selectedYear);
            
            // 根据年份生成合理的月份和日期
            int month = random.nextInt(12) + 1;
            int day = random.nextInt(28) + 1; // 使用28天避免月份天数问题
            data.setMonth(String.format("%02d", month));
            data.setDate(String.format("%s-%02d-%02d", selectedYear, month, day));

            // 业务分类数据 - 确保地区均匀分布
            data.setCategory(categories[i % categories.length]);
            data.setChannel(channels[i % channels.length]);
            data.setProduct(products[i % products.length]);
            data.setRegion(regions[i % regions.length]); // 轮换地区，确保每个地区都有数据
            data.setSalesman(salesmen[i % salesmen.length]);

            // 数值数据 - 根据地区和年份调整数据范围
            double baseAmount = 1000.0;
            double regionMultiplier = getRegionMultiplier(data.getRegion());
            double yearMultiplier = getYearMultiplier(data.getYear());
            
            data.setAmount(baseAmount * regionMultiplier * yearMultiplier + random.nextDouble() * 20000.0);
            data.setQuantity(random.nextInt(500) + 10);
            data.setPercentage(random.nextDouble() * 100);

            // 系统字段
            data.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            data.setUpdatedAt(LocalDateTime.now());

            dataStore.put(data.getId(), data);
        }

        logger.info("✅ 生成了 {} 条示例数据，年份分布: {}, 地区分布: {} 个", 
                   dataStore.size(), years.length, regions.length);
        
        // 输出分布统计用于验证
        logDataDistribution();
    }

    /**
     * 根据地区返回金额倍数（模拟不同地区的经济水平）
     */
    private double getRegionMultiplier(String region) {
        switch (region) {
            case "华东": return 1.5;
            case "华南": return 1.3;
            case "华北": return 1.2;
            case "华中": return 1.0;
            case "西南": return 0.9;
            case "西北": return 0.8;
            case "东北": return 0.85;
            default: return 1.0;
        }
    }

    /**
     * 根据年份返回金额倍数（模拟经济增长）
     */
    private double getYearMultiplier(String year) {
        switch (year) {
            case "2023": return 0.9;
            case "2024": return 1.0;
            case "2025": return 1.1;
            default: return 1.0;
        }
    }

    /**
     * 输出数据分布统计
     */
    private void logDataDistribution() {
        // 年份分布
        Map<String, Long> yearDistribution = dataStore.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversalChartDataView::getYear,
                        java.util.stream.Collectors.counting()));
        
        // 地区分布
        Map<String, Long> regionDistribution = dataStore.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversalChartDataView::getRegion,
                        java.util.stream.Collectors.counting()));

        logger.info("📊 年份分布: {}", yearDistribution);
        logger.info("📊 地区分布: {}", regionDistribution);
    }

    /**
     * 获取星期名称
     */
    private String getDayName(int dayOfWeek) {
        String[] days = { "周一", "周二", "周三", "周四", "周五", "周六", "周日" };
        return days[dayOfWeek - 1];
    }

    /**
     * 获取随机颜色
     */
    private String getRandomColor() {
        String[] colors = { "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8" };
        return colors[new Random().nextInt(colors.length)];
    }

    // ==================== CRUD 操作 ====================

    /**
     * 获取所有数据
     */
    public List<UniversalChartDataView> findAll() {
        return new ArrayList<>(dataStore.values());
    }

    /**
     * 根据ID获取数据
     */
    public UniversalChartDataView findById(Long id) {
        return dataStore.get(id);
    }

    /**
     * 保存数据（新增或更新）
     */
    public UniversalChartDataView save(UniversalChartDataView data) {
        if (data.getId() == null) {
            // 新增
            data.setId(idGenerator.getAndIncrement());
            data.setCreatedAt(LocalDateTime.now());
        }
        data.setUpdatedAt(LocalDateTime.now());

        dataStore.put(data.getId(), data);
        logger.info("💾 保存数据记录: ID={}", data.getId());
        return data;
    }

    /**
     * 删除数据
     */
    public boolean deleteById(Long id) {
        UniversalChartDataView removed = dataStore.remove(id);
        if (removed != null) {
            logger.info("🗑️ 删除数据记录: ID={}", id);
            return true;
        }
        return false;
    }

    /**
     * 批量删除
     */
    public int deleteByIds(List<Long> ids) {
        int deletedCount = 0;
        for (Long id : ids) {
            if (deleteById(id)) {
                deletedCount++;
            }
        }
        logger.info("🗑️ 批量删除数据记录: {} 条", deletedCount);
        return deletedCount;
    }

    /**
     * 根据分类查询
     */
    public List<UniversalChartDataView> findByCategory(String category) {
        return dataStore.values().stream()
                .filter(data -> category.equals(data.getCategory()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据渠道查询
     */
    public List<UniversalChartDataView> findByChannel(String channel) {
        return dataStore.values().stream()
                .filter(data -> channel.equals(data.getChannel()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据年份查询
     */
    public List<UniversalChartDataView> findByYear(String year) {
        return dataStore.values().stream()
                .filter(data -> year.equals(data.getYear()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据地区查询
     */
    public List<UniversalChartDataView> findByRegion(String region) {
        return dataStore.values().stream()
                .filter(data -> region.equals(data.getRegion()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 组合条件查询（年份和地区筛选）
     */
    public List<UniversalChartDataView> findWithFilters(String year, String region) {
        return dataStore.values().stream()
                .filter(data -> {
                    boolean matchYear = (year == null || year.trim().isEmpty()) || year.equals(data.getYear());
                    boolean matchRegion = (region == null || region.trim().isEmpty()) || region.equals(data.getRegion());
                    return matchYear && matchRegion;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 分页查询
     */
    public List<UniversalChartDataView> findWithPagination(int page, int size) {
        List<UniversalChartDataView> allData = findAll();
        int start = page * size;
        int end = Math.min(start + size, allData.size());

        if (start >= allData.size()) {
            return new ArrayList<>();
        }

        return allData.subList(start, end);
    }

    /**
     * 获取总数量
     */
    public long count() {
        return dataStore.size();
    }

    /**
     * 清空所有数据
     */
    public void deleteAll() {
        dataStore.clear();
        idGenerator.set(1);
        logger.info("🗑️ 清空所有数据记录");
    }

    /**
     * 重新生成示例数据
     */
    public void regenerateSampleData() {
        deleteAll();
        generateSampleData();
        logger.info("🔄 重新生成示例数据: {} 条", dataStore.size());
    }

    /**
     * 获取字段统计信息
     */
    public Map<String, Object> getFieldStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 基本统计
        stats.put("totalRecords", dataStore.size());
        stats.put("totalFields", 12);

        // 分类分布
        Map<String, Long> categoryStats = dataStore.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversalChartDataView::getCategory,
                        java.util.stream.Collectors.counting()));
        stats.put("categoryDistribution", categoryStats);

        // 渠道分布
        Map<String, Long> channelStats = dataStore.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversalChartDataView::getChannel,
                        java.util.stream.Collectors.counting()));
        stats.put("channelDistribution", channelStats);

        // 地区分布
        Map<String, Long> regionStats = dataStore.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UniversalChartDataView::getRegion,
                        java.util.stream.Collectors.counting()));
        stats.put("regionDistribution", regionStats);

        return stats;
    }

    /**
     * 获取12个核心业务字段的信息
     */
    public Map<String, Object> getFieldsInfo() {
        Map<String, Object> fieldsInfo = new HashMap<>();

        // 时间维度字段 (4个)
        fieldsInfo.put("时间维度", Arrays.asList(
                "year (年份)", "month (月份)", "date (日期)", "created_at (创建时间)", "updated_at (更新时间)"));

        // 业务分类字段 (5个)
        fieldsInfo.put("业务分类", Arrays.asList(
                "category (业务分类)", "channel (销售渠道)", "product (产品名称)",
                "region (地理区域)", "salesman (销售人员)"));

        // 数值字段 (3个)
        fieldsInfo.put("数值字段", Arrays.asList(
                "amount (金额)", "quantity (数量)", "percentage (百分比)"));

        // 系统字段 (1个)
        fieldsInfo.put("系统字段", Arrays.asList(
                "id (主键ID)"));

        return fieldsInfo;
    }
}
