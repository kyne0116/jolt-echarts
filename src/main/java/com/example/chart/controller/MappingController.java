package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.example.chart.model.Mapping;
import com.example.chart.model.PlaceholderCatalog;
import com.example.chart.service.MappingService;
import com.example.chart.service.PlaceholderCatalogService;
import com.example.chart.service.PlaceholderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/charts")
public class MappingController {

    @Autowired
    private MappingService mappingService;

    @Autowired
    private PlaceholderCatalogService catalogService;

    @Autowired
    private PlaceholderManager placeholderManager;

    @GetMapping("/{chartId}/mappings/active")
    public ResponseEntity<ApiResponse<Mapping>> getActive(@PathVariable String chartId) {
        return ResponseEntity.ok(ApiResponse.ok(mappingService.getActive(chartId).orElse(null)));
    }

    @PutMapping("/{chartId}/mappings")
    public ResponseEntity<ApiResponse<Map<String,String>>> saveDraft(@PathVariable String chartId,
                                                                     @RequestBody Mapping mapping,
                                                                     @RequestHeader(value = "X-Operator", required = false) String operator) {
        mapping.setChartId(chartId);
        Mapping saved = mappingService.saveDraft(operator != null ? operator : "system", mapping);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("mappingVersion", saved.getMappingVersion())));
    }

    @PostMapping("/{chartId}/mappings/validate")
    public ResponseEntity<ApiResponse<MappingService.ValidationResult>> validate(@PathVariable String chartId,
                                                                                 @RequestBody Map<String,Object> body) {
        // 生成目录，提取必填占位符集合
        PlaceholderCatalog catalog = catalogService.generateCatalog(chartId, String.valueOf(body.getOrDefault("templateVersion","")), String.valueOf(body.getOrDefault("specVersion","")));
        Set<String> required = new HashSet<>();
        for (PlaceholderCatalog.Item it : catalog.getPlaceholders()) {
            if (it.isRequired()) required.add(it.getName());
        }
        Mapping mapping = toMapping(body.get("mapping"));
        var result = mappingService.validate(chartId, mapping, required);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{chartId}/dry-run")
    public ResponseEntity<ApiResponse<MappingService.DryRunResult>> dryRun(@PathVariable String chartId,
                                                                           @RequestBody Mapping mapping) {
        mapping.setChartId(chartId);
        var result = mappingService.dryRun(chartId, mapping);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @SuppressWarnings("unchecked")
    private Mapping toMapping(Object obj) {
        // 允许直接反序列化为 Mapping（若 body 已是 mapping 对象则忽略）
        if (obj instanceof Mapping) return (Mapping) obj;
        // 由 Jackson 自动处理更合适，这里兜底返回空映射
        Mapping m = new Mapping();
        m.setItems(new ArrayList<>());
        return m;
    }
}

