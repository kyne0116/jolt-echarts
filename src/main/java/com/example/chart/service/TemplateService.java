package com.example.chart.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chart.model.TemplateType;
import com.example.chart.model.UniversalTemplate;
import com.example.chart.repository.InMemoryUniversalTemplateRepository;
import com.example.chart.repository.model.UniversalTemplateEntity;

/**
 * 模板服务层：封装Repository访问，模拟真实Service风格
 */
@Service
public class TemplateService {

    @Autowired
    private InMemoryUniversalTemplateRepository repository;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private CategoryTemplateFactory categoryTemplateFactory;

    @PostConstruct
    public void init() {
        // 新设计：只有一个真正通用的模板
        UniversalTemplate universalTemplate = UniversalTemplate.createDefault();

        repository.save(new UniversalTemplateEntity(
                "universal",
                "通用图表模板",
                "真正通用的语义化图表模板，支持所有图表类型",
                universalTemplate.toMap()));

        System.out.println("✅ 初始化通用模板完成");
    }

    public Map<String, Object> getTemplateByChartId(String chartId) {
        // 新设计：所有图表类型都使用同一个通用模板
        return repository.findById("universal")
                .map(UniversalTemplateEntity::getTemplate)
                .orElseThrow(() -> new NoSuchElementException("通用模板不存在"));
    }

    /**
     * 根据图表类型获取分类模板（新版本）
     */
    public Map<String, Object> getCategoryTemplateByChartId(String chartId) {
        System.out.println("📋 获取分类模板，图表类型: " + chartId);

        // 使用分类模板工厂创建对应的模板
        Map<String, Object> template = categoryTemplateFactory.createTemplateForChartType(chartId);

        // 推断模板类型
        TemplateType templateType = TemplateType.inferFromChartType(chartId);
        System.out.println("📋 使用模板类型: " + templateType);

        return template;
    }

    public List<UniversalTemplateEntity> listAll() {
        return repository.findAll();
    }

    public UniversalTemplateEntity save(UniversalTemplateEntity entity) {
        return repository.save(entity);
    }

    public void delete(String chartId) {
        repository.deleteById(chartId);
    }

    // 生成基础模板（与旧服务中的模板结构一致，含占位符）
    private Map<String, Object> createBaseTemplate(String chartId) {
        Map<String, Object> template = new HashMap<>();

        Map<String, Object> chartMeta = new HashMap<>();
        chartMeta.put("chartId", chartId);
        chartMeta.put("chartType", "${chart_type}");
        chartMeta.put("title", "${chart_title}");
        chartMeta.put("dataSource", "marketing_db");
        template.put("chartMeta", chartMeta);

        template.put("categories", "${category_field}");

        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("seriesId", "series_" + i);
            seriesItem.put("seriesName", "${series_name_" + i + "}");
            seriesItem.put("seriesType", "${chart_type}");
            seriesItem.put("stackGroup", "${stack_group}");
            seriesItem.put("values", "${series_data_" + i + "}");
            series.add(seriesItem);
        }
        template.put("series", series);

        Map<String, Object> styleConfig = new HashMap<>();
        styleConfig.put("showLegend", true);
        styleConfig.put("showTooltip", true);
        styleConfig.put("showGrid", true);
        template.put("styleConfig", styleConfig);

        return template;
    }
}
