package com.example.chart.repository;

import com.example.chart.model.ChartRegistry;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryChartRegistryRepository {
    private final Map<String, ChartRegistry> registries = new LinkedHashMap<>();

    public List<ChartRegistry> listAll() {
        return new ArrayList<>(registries.values());
    }

    public Optional<ChartRegistry> get(String chartId) {
        return Optional.ofNullable(registries.get(chartId));
    }

    public ChartRegistry upsert(ChartRegistry r) {
        registries.put(r.getChartId(), r);
        return r;
    }
}

