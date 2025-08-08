package com.example.chart.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.chart.model.Mapping;

/**
 * 查询编排层：基于映射定义，将扁平数据聚合/分组/透视为占位符值。
 * 现阶段使用内存模拟：从 MappingRelationshipService 的模拟数据生成器推导样例。
 */
@Service
public class QueryOrchestrator {

    /**
     * 简化：根据 mapping 的 transform/pivot 模拟返回值
     * 实际接DB时：编译SQL/ORM → rows → 聚合/分组 → 透视
     */
    public Map<String, Object> run(String chartId, Mapping mapping, Map<String, Object> sampleUniverse) {
        Map<String, Object> values = new HashMap<>();

        for (Mapping.Item item : mapping.getItems()) {
            String ph = item.getPlaceholder();
            switch (item.getDataType()) {
                case "string":
                case "number":
                    // 简化：若 filters 指定了 channel_name 等，则取该过滤值
                    Object v = extractScalar(sampleUniverse, item);
                    values.put(ph, v);
                    break;
                case "array":
                    List<?> arr = extractArray(sampleUniverse, item);
                    values.put(ph, arr);
                    break;
                case "object":
                default:
                    values.put(ph, Collections.emptyMap());
            }
        }

        return values;
    }

    private Object extractScalar(Map<String, Object> sample, Mapping.Item item) {
        // 规则：若 filters 有固定值，则返回该值；否则返回列名
        Optional<Mapping.Filter> f = item.getTransform().getFilters().stream().findFirst();
        if (f.isPresent()) {
            return f.get().getValue();
        }
        return item.getSource().getColumn();
    }

    @SuppressWarnings("unchecked")
    private List<?> extractArray(Map<String, Object> sample, Mapping.Item item) {
        // 规则：若是 category 列（如 day_name），返回 categories；否则返回 series 对应的数值数组
        String catKey = item.getPivot() != null ? item.getPivot().getCategoryKey() : null;
        String serKey = item.getPivot() != null ? item.getPivot().getSeriesKey() : null;
        String column = item.getSource().getColumn();

        // 允许 sample 提供：categories、seriesValues(channel)->list
        List<String> categories = (List<String>) sample.getOrDefault("categories",
                Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        Map<String, List<?>> seriesValues = (Map<String, List<?>>) sample.getOrDefault("series",
                defaultSeries());

        if (Objects.equals(column, catKey)) {
            return categories;
        }

        // 查找过滤指定的 series
        String targetSeries = item.getTransform().getFilters().stream()
                .filter(f -> Objects.equals(f.getField(), serKey))
                .map(f -> String.valueOf(f.getValue()))
                .findFirst().orElse("Email");

        return seriesValues.getOrDefault(targetSeries, seriesValues.values().stream().findFirst().orElse(categories));
    }

    private Map<String, List<?>> defaultSeries() {
        Map<String, List<?>> m = new LinkedHashMap<>();
        m.put("Email", Arrays.asList(120, 132, 101, 134, 90, 230, 210));
        m.put("Union Ads", Arrays.asList(220, 182, 191, 234, 290, 330, 310));
        m.put("Video Ads", Arrays.asList(150, 232, 201, 154, 190, 330, 410));
        return m;
    }
}
