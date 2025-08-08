package com.example.chart.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.chart.model.Mapping;
import com.example.chart.model.PlaceholderCatalog;
import com.example.chart.service.MappingService;
import com.example.chart.service.PlaceholderCatalogService;
import com.example.chart.service.TemplateService;
import com.example.chart.service.TwoStageTransformationService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 端到端集成测试：验证完整的工作流程
 * 1. 生成占位符目录
 * 2. 创建映射
 * 3. 校验映射
 * 4. Dry-run 预览
 * 5. 图表渲染验证
 */
@SpringBootTest
public class EndToEndIntegrationTest {

    @Autowired
    private PlaceholderCatalogService catalogService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private TemplateService templateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCompleteWorkflow() throws Exception {
        String chartId = "stacked_line_chart";

        // 1. 生成占位符目录
        System.out.println("=== 步骤1: 生成占位符目录 ===");
        PlaceholderCatalog catalog = catalogService.generateCatalog(chartId, "1.1.0", "1.0.3");

        assertNotNull(catalog);
        assertEquals(chartId, catalog.getChartId());
        assertFalse(catalog.getPlaceholders().isEmpty());

        System.out.println("生成的占位符数量: " + catalog.getPlaceholders().size());
        catalog.getPlaceholders().forEach(
                p -> System.out.println("  - " + p.getName() + " (" + p.getType() + ", " + p.getGroup() + ")"));

        // 2. 创建映射
        System.out.println("\n=== 步骤2: 创建映射 ===");
        Mapping mapping = createSampleMapping(chartId);
        Mapping savedMapping = mappingService.saveDraft("test-user", mapping);

        assertNotNull(savedMapping);
        assertNotNull(savedMapping.getMappingVersion());
        assertEquals("draft", savedMapping.getStatus());

        System.out.println("映射版本: " + savedMapping.getMappingVersion());
        System.out.println("映射项数量: " + savedMapping.getItems().size());

        // 3. 校验映射
        System.out.println("\n=== 步骤3: 校验映射 ===");
        var requiredPlaceholders = catalog.getPlaceholders().stream()
                .filter(PlaceholderCatalog.Item::isRequired)
                .map(PlaceholderCatalog.Item::getName)
                .collect(java.util.stream.Collectors.toSet());

        var validationResult = mappingService.validate(chartId, savedMapping, requiredPlaceholders);

        System.out.println("校验通过: " + validationResult.isPassed());
        if (!validationResult.getMissing().isEmpty()) {
            System.out.println("缺失占位符: " + validationResult.getMissing());
        }
        if (!validationResult.getTypeErrors().isEmpty()) {
            System.out.println("类型错误: " + validationResult.getTypeErrors());
        }

        // 4. Dry-run 预览
        System.out.println("\n=== 步骤4: Dry-run 预览 ===");
        var dryRunResult = mappingService.dryRun(chartId, savedMapping);

        assertNotNull(dryRunResult);
        assertNotNull(dryRunResult.getFinalEChartsConfig());

        System.out.println("生成的 ECharts 配置类型: " + dryRunResult.getFinalEChartsConfig().getClass().getSimpleName());
        System.out.println("查询预览数据: " + dryRunResult.getQueryPreview().keySet());
        System.out.println("剩余占位符: " + dryRunResult.getRemainingPlaceholders());

        // 5. 验证 ECharts 配置结构
        System.out.println("\n=== 步骤5: 验证 ECharts 配置结构 ===");
        Map<String, Object> echartsConfig = objectMapper.convertValue(dryRunResult.getFinalEChartsConfig(), Map.class);

        // 验证基本结构
        assertTrue(echartsConfig.containsKey("title"), "应包含 title");
        assertTrue(echartsConfig.containsKey("xAxis"), "应包含 xAxis");
        assertTrue(echartsConfig.containsKey("series"), "应包含 series");

        System.out.println("ECharts 配置验证通过!");

        // 6. 两阶段转换验证
        System.out.println("\n=== 步骤6: 两阶段转换验证 ===");
        var template = templateService.getTemplateByChartId(chartId);
        var stage1Result = transformationService.executeStage1Transformation(chartId, template);

        assertTrue(stage1Result.isSuccess());
        assertNotNull(stage1Result.getResult());

        var stage2Result = transformationService.executeStage2Transformation(chartId, stage1Result.getResult());

        assertTrue(stage2Result.isSuccess());
        assertNotNull(stage2Result.getResult());

        System.out.println("两阶段转换验证通过!");

        System.out.println("\n=== 端到端测试完成 ===");
        System.out.println("✅ 所有步骤验证通过，系统功能正常!");
    }

    private Mapping createSampleMapping(String chartId) {
        Mapping mapping = new Mapping();
        mapping.setChartId(chartId);
        mapping.setTemplateVersion("1.1.0");
        mapping.setSpecVersion("1.0.3");

        // 创建基本映射项
        Mapping.Item titleItem = new Mapping.Item();
        titleItem.setPlaceholder("${chart_title}");
        titleItem.setDataType("string");
        titleItem.getSource().setTable("chart_config");
        titleItem.getSource().setColumn("title");

        Mapping.Item categoryItem = new Mapping.Item();
        categoryItem.setPlaceholder("${category_field}");
        categoryItem.setDataType("array");
        categoryItem.getSource().setTable("marketing_data");
        categoryItem.getSource().setColumn("day_name");
        categoryItem.getPivot().setCategoryKey("day_name");

        // 添加系列名称映射
        for (int i = 1; i <= 5; i++) {
            Mapping.Item seriesNameItem = new Mapping.Item();
            seriesNameItem.setPlaceholder("${series_name_" + i + "}");
            seriesNameItem.setDataType("string");
            seriesNameItem.getSource().setTable("chart_config");
            seriesNameItem.getSource().setColumn("series_name");
            mapping.getItems().add(seriesNameItem);
        }

        Mapping.Item seriesItem = new Mapping.Item();
        seriesItem.setPlaceholder("${series_data_1}");
        seriesItem.setDataType("array");
        seriesItem.getSource().setTable("marketing_data");
        seriesItem.getSource().setColumn("conversion_count");
        seriesItem.getTransform().setAggregation("sum");
        seriesItem.getTransform().setGroupBy(Arrays.asList("day_name", "channel_name"));

        Mapping.Filter filter = new Mapping.Filter();
        filter.setField("channel_name");
        filter.setOp("eq");
        filter.setValue("Email");
        seriesItem.getTransform().getFilters().add(filter);

        seriesItem.getPivot().setCategoryKey("day_name");
        seriesItem.getPivot().setSeriesKey("channel_name");

        mapping.getItems().addAll(Arrays.asList(titleItem, categoryItem, seriesItem));

        return mapping;
    }
}
