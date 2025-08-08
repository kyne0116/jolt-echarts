package com.example.chart.service;

import com.example.chart.repository.InMemoryUniversalTemplateRepository;
import com.example.chart.repository.model.UniversalTemplateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 模板服务层：封装Repository访问，模拟真实Service风格
 */
@Service
public class TemplateService {

    @Autowired
    private InMemoryUniversalTemplateRepository repository;

    @Autowired
    private PlaceholderManager placeholderManager;

    @PostConstruct
    public void init() {
        // 预置三类图表模板（从旧的 createUniversalTemplateWithPlaceholders 迁移）
        repository.save(new UniversalTemplateEntity(
                "stacked_line_chart",
                "堆叠折线图通用模板",
                "用于两阶段转换演示的折线图模板",
                createBaseTemplate("stacked_line_chart")
        ));
        repository.save(new UniversalTemplateEntity(
                "basic_bar_chart",
                "基础柱状图通用模板",
                "用于两阶段转换演示的柱状图模板",
                createBaseTemplate("basic_bar_chart")
        ));
        repository.save(new UniversalTemplateEntity(
                "pie_chart",
                "饼图通用模板",
                "用于两阶段转换演示的饼图模板",
                createBaseTemplate("pie_chart")
        ));
    }

    public Map<String, Object> getTemplateByChartId(String chartId) {
        return repository.findById(chartId)
                .map(UniversalTemplateEntity::getTemplate)
                .orElseThrow(() -> new NoSuchElementException("模板不存在: " + chartId));
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

