package com.example.chart.service;

import com.example.chart.model.ChartRegistry;
import com.example.chart.repository.InMemoryChartRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChartRegistryService {

    @Autowired
    private InMemoryChartRegistryRepository repository;

    public List<ChartRegistry> list() { return repository.listAll(); }

    public Optional<ChartRegistry> get(String chartId) { return repository.get(chartId); }

    public ChartRegistry activate(String chartId, String templateVersion, String specVersion, String mappingVersion) {
        ChartRegistry reg = repository.get(chartId).orElseGet(() -> { ChartRegistry r = new ChartRegistry(); r.setChartId(chartId); return r; });
        reg.setActiveTemplateVersion(templateVersion);
        reg.setActiveSpecVersion(specVersion);
        reg.setActiveMappingVersion(mappingVersion);
        return repository.upsert(reg);
    }
}

