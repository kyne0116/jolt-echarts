package com.example.chart.repository;

import com.example.chart.model.Mapping;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryMappingRepository {
    private final Map<String, Mapping> activeByChart = new HashMap<>();
    private final Map<String, Map<String, Mapping>> versionsByChart = new HashMap<>();

    public Optional<Mapping> getActive(String chartId) {
        return Optional.ofNullable(activeByChart.get(chartId));
    }

    public Optional<Mapping> getByVersion(String chartId, String version) {
        return Optional.ofNullable(versionsByChart
                .getOrDefault(chartId, Collections.emptyMap())
                .get(version));
    }

    public Mapping saveNewVersion(Mapping m) {
        versionsByChart.computeIfAbsent(m.getChartId(), k -> new LinkedHashMap<>())
                .put(m.getMappingVersion(), m);
        // 默认不激活
        return m;
    }

    public void activate(String chartId, String version) {
        Mapping m = versionsByChart.getOrDefault(chartId, Collections.emptyMap()).get(version);
        if (m != null) {
            activeByChart.put(chartId, m);
        }
    }

    public List<String> listVersions(String chartId) {
        return new ArrayList<>(versionsByChart.getOrDefault(chartId, Collections.emptyMap()).keySet());
    }
}

