package com.example.chart;

import com.example.chart.service.UniversalTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通用模板集成测试
 * 验证8个通用模板文件的正确性和API功能
 */
@SpringBootTest
public class UniversalTemplateIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UniversalTemplateIntegrationTest.class);

    @Autowired
    private UniversalTemplateService universalTemplateService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAllUniversalTemplatesExist() {
        logger.info("=== 测试所有通用模板是否存在 ===");
        
        String[] templateKeys = {
            "line-chart-template", "bar-chart-template", "area-chart-template",
            "scatter-chart-template", "pie-chart-template", "treemap-chart-template", 
            "radar-chart-template", "gauge-chart-template"
        };
        
        for (String templateKey : templateKeys) {
            try {
                Map<String, Object> template = universalTemplateService.getUniversalTemplate(templateKey);
                assertNotNull(template, "模板不应该为空: " + templateKey);
                
                // 验证基本结构
                assertTrue(template.containsKey("chartMeta"), "应该包含chartMeta: " + templateKey);
                assertTrue(template.containsKey("dataStructure"), "应该包含dataStructure: " + templateKey);
                assertTrue(template.containsKey("styleConfig"), "应该包含styleConfig: " + templateKey);
                
                logger.info("✅ 模板验证成功: {}", templateKey);
                
            } catch (Exception e) {
                fail("获取通用模板失败: " + templateKey + " - " + e.getMessage());
            }
        }
        
        logger.info("✅ 所有8个通用模板都存在且格式正确");
    }

    @Test 
    public void testChartTypeToTemplateMapping() {
        logger.info("=== 测试图表类型到通用模板的映射关系 ===");
        
        String[] chartTypes = {
            "basic_line_chart", "smooth_line_chart", "stacked_line_chart",
            "basic_bar_chart", "stacked_bar_chart", 
            "basic_area_chart", "scatter_chart", "bubble_chart",
            "basic_pie_chart", "doughnut_chart", "rose_chart",
            "treemap_chart", "sunburst_chart", "funnel_chart",
            "basic_radar_chart", "filled_radar_chart", "polar_chart",
            "basic_gauge_chart", "progress_gauge_chart", "grade_gauge_chart"
        };
        
        for (String chartType : chartTypes) {
            try {
                Map<String, Object> template = universalTemplateService.getUniversalTemplateByChartType(chartType);
                assertNotNull(template, "应该能获取到模板: " + chartType);
                
                // 验证模板结构
                Map<String, Object> chartMeta = (Map<String, Object>) template.get("chartMeta");
                assertNotNull(chartMeta, "chartMeta不应该为空: " + chartType);
                assertTrue(chartMeta.containsKey("templateCategory"), "应该包含templateCategory: " + chartType);
                
                logger.info("✅ 图表类型映射成功: {} -> 模板分类: {}", 
                    chartType, chartMeta.get("templateCategory"));
                
            } catch (Exception e) {
                fail("根据图表类型获取通用模板失败: " + chartType + " - " + e.getMessage());
            }
        }
        
        logger.info("✅ 所有图表类型都能正确映射到通用模板");
    }

    @Test
    public void testPlaceholderExtraction() {
        logger.info("=== 测试占位符提取功能 ===");
        
        String[] templateKeys = {
            "line-chart-template", "bar-chart-template", "pie-chart-template", "radar-chart-template"
        };
        
        for (String templateKey : templateKeys) {
            try {
                List<String> placeholders = universalTemplateService.extractPlaceholdersFromTemplate(templateKey);
                
                assertNotNull(placeholders, "占位符列表不应该为空: " + templateKey);
                assertFalse(placeholders.isEmpty(), "应该包含占位符: " + templateKey);
                
                // 验证占位符格式
                for (String placeholder : placeholders) {
                    assertTrue(placeholder.startsWith("${") && placeholder.endsWith("}"), 
                        "占位符格式应该正确: " + placeholder + " in " + templateKey);
                }
                
                logger.info("✅ 模板 {} 包含 {} 个占位符", templateKey, placeholders.size());
                logger.info("   前5个占位符: {}", 
                    placeholders.subList(0, Math.min(5, placeholders.size())));
                
            } catch (Exception e) {
                fail("提取占位符失败: " + templateKey + " - " + e.getMessage());
            }
        }
        
        logger.info("✅ 占位符提取功能正常工作");
    }

    @Test
    public void testTemplateValidation() {
        logger.info("=== 测试通用模板验证功能 ===");
        
        String[] templateKeys = {"line-chart-template", "pie-chart-template", "radar-chart-template"};
        
        for (String templateKey : templateKeys) {
            try {
                Map<String, Object> validationResult = universalTemplateService.validateUniversalTemplate(templateKey);
                
                assertNotNull(validationResult, "验证结果不应该为空: " + templateKey);
                assertTrue((Boolean) validationResult.get("valid"), "模板应该有效: " + templateKey);
                
                List<String> missingKeys = (List<String>) validationResult.get("missingKeys");
                assertTrue(missingKeys.isEmpty(), "不应该有缺失的键: " + templateKey + " - " + missingKeys);
                
                Integer placeholderCount = (Integer) validationResult.get("placeholderCount");
                assertTrue(placeholderCount > 0, "应该包含占位符: " + templateKey);
                
                logger.info("✅ 模板验证通过: {} (包含{}个占位符)", templateKey, placeholderCount);
                
            } catch (Exception e) {
                fail("模板验证失败: " + templateKey + " - " + e.getMessage());
            }
        }
        
        logger.info("✅ 通用模板验证功能正常工作");
    }

    @Test
    public void testGetAllUniversalTemplates() {
        logger.info("=== 测试获取所有通用模板功能 ===");
        
        try {
            List<Map<String, Object>> allTemplates = universalTemplateService.getAllUniversalTemplates();
            
            assertNotNull(allTemplates, "模板列表不应该为空");
            assertEquals(8, allTemplates.size(), "应该有8个通用模板");
            
            // 验证每个模板的信息完整性
            for (Map<String, Object> template : allTemplates) {
                assertTrue(template.containsKey("templateKey"), "应该包含templateKey");
                assertTrue(template.containsKey("displayName"), "应该包含displayName");
                assertTrue(template.containsKey("category"), "应该包含category");
                assertTrue(template.containsKey("supportedChartTypes"), "应该包含supportedChartTypes");
                assertTrue(template.containsKey("supportedCount"), "应该包含supportedCount");
                
                List<String> supportedTypes = (List<String>) template.get("supportedChartTypes");
                Integer supportedCount = (Integer) template.get("supportedCount");
                assertEquals(supportedTypes.size(), supportedCount.intValue(), 
                    "支持的图表类型数量应该一致: " + template.get("templateKey"));
                
                logger.info("✅ 模板信息完整: {} - {} (支持{}种图表)", 
                    template.get("templateKey"), template.get("displayName"), supportedCount);
            }
            
            logger.info("✅ 获取所有通用模板功能正常工作");
            
        } catch (Exception e) {
            fail("获取所有通用模板失败: " + e.getMessage());
        }
    }

    @Test
    public void testTemplateCategoryGrouping() {
        logger.info("=== 测试按分类获取通用模板功能 ===");
        
        String[] categories = {"CARTESIAN", "PIE", "TREE", "RADAR", "GAUGE"};
        
        for (String category : categories) {
            try {
                List<Map<String, Object>> templates = universalTemplateService.getUniversalTemplatesByCategory(category);
                
                assertNotNull(templates, "分类模板列表不应该为空: " + category);
                
                for (Map<String, Object> template : templates) {
                    assertEquals(category, template.get("category"), 
                        "模板分类应该正确: " + template.get("templateKey"));
                }
                
                logger.info("✅ 分类 {} 包含 {} 个模板", category, templates.size());
                
            } catch (Exception e) {
                fail("按分类获取模板失败: " + category + " - " + e.getMessage());
            }
        }
        
        logger.info("✅ 按分类获取通用模板功能正常工作");
    }
}