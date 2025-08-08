package com.example.chart.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chart.model.Mapping;
import com.example.chart.repository.InMemoryMappingRepository;

@Service
public class MappingService {

    @Autowired
    private InMemoryMappingRepository repository;

    @Autowired
    private QueryOrchestrator orchestrator;

    @Autowired
    private PlaceholderManager placeholderManager;

    @Autowired
    private TwoStageTransformationService transformationService;

    @Autowired
    private TemplateService templateService;

    public Optional<Mapping> getActive(String chartId) {
        return repository.getActive(chartId);
    }

    public Mapping saveDraft(String operator, Mapping mapping) {
        mapping.setUpdatedBy(operator);
        mapping.setUpdatedAt(Instant.now().toEpochMilli());
        if (mapping.getMappingVersion() == null || mapping.getMappingVersion().isBlank()) {
            mapping.setMappingVersion(UUID.randomUUID().toString());
        }
        mapping.setStatus("draft");
        return repository.saveNewVersion(mapping);
    }

    public void activate(String chartId, String version) {
        repository.activate(chartId, version);
    }

    public List<String> listVersions(String chartId) {
        return repository.listVersions(chartId);
    }

    public ValidationResult validate(String chartId, Mapping mapping, Set<String> requiredPlaceholders) {
        List<String> missing = new ArrayList<>();
        Map<String, String> typeErrors = new HashMap<>();
        List<String> lengthErrors = new ArrayList<>();

        // 1. 覆盖率校验
        for (String ph : requiredPlaceholders) {
            boolean covered = mapping.getItems().stream().anyMatch(it -> ph.equals(it.getPlaceholder()));
            if (!covered)
                missing.add(ph);
        }

        // 2. 类型校验：通过 dry-run 获取实际值并检查类型
        try {
            DryRunResult dryResult = dryRun(chartId, mapping);
            Map<String, Object> actualValues = dryResult.getQueryPreview();

            for (Mapping.Item item : mapping.getItems()) {
                String ph = item.getPlaceholder();
                String expectedType = item.getDataType();
                Object actualValue = actualValues.get(ph);

                if (actualValue != null) {
                    String actualType = inferType(actualValue);
                    if (!expectedType.equals(actualType)) {
                        typeErrors.put(ph, String.format("Expected %s but got %s", expectedType, actualType));
                    }

                    // 3. 数组长度校验：array 类型需要与 xAxis.data 对齐
                    if ("array".equals(expectedType) && actualValue instanceof List) {
                        List<?> arr = (List<?>) actualValue;
                        List<?> categories = (List<?>) actualValues.get("${category_field}");
                        if (categories != null && arr.size() != categories.size()) {
                            lengthErrors.add(String.format("%s length (%d) doesn't match categories length (%d)",
                                    ph, arr.size(), categories.size()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Dry-run 失败时，只能做基础校验
            typeErrors.put("dry-run", "Failed to validate types: " + e.getMessage());
        }

        return new ValidationResult(missing.isEmpty() && typeErrors.isEmpty() && lengthErrors.isEmpty(),
                missing, typeErrors, lengthErrors);
    }

    private String inferType(Object value) {
        if (value instanceof String)
            return "string";
        if (value instanceof Number)
            return "number";
        if (value instanceof List)
            return "array";
        if (value instanceof Map)
            return "object";
        return "unknown";
    }

    public DryRunResult dryRun(String chartId, Mapping mapping) {
        // 准备阶段一输出
        Map<String, Object> template = templateService.getTemplateByChartId(chartId);
        var stage1 = transformationService.executeStage1Transformation(chartId, template);
        Object stage1Out = stage1.getResult();

        // 构建样例数据宇宙（可来自 MappingRelationshipService 模拟）
        Map<String, Object> sampleUniverse = new HashMap<>();
        sampleUniverse.put("categories", Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        Map<String, List<Integer>> ser = new LinkedHashMap<>();
        ser.put("Email", Arrays.asList(120, 132, 101, 134, 90, 230, 210));
        ser.put("Union Ads", Arrays.asList(220, 182, 191, 234, 290, 330, 310));
        ser.put("Video Ads", Arrays.asList(150, 232, 201, 154, 190, 330, 410));
        sampleUniverse.put("series", ser);

        Map<String, Object> values = orchestrator.run(chartId, mapping, sampleUniverse);

        Object finalConfig = placeholderManager.replacePlaceholdersInJson(stage1Out, values);
        // 校验剩余占位符
        Set<String> remain = placeholderManager.extractPlaceholdersFromJson(finalConfig);

        return new DryRunResult(finalConfig, values, remain);
    }

    // ---- DTO ----
    public static class ValidationResult {
        private boolean passed;
        private List<String> missing;
        private Map<String, String> typeErrors;
        private List<String> lengthErrors;

        public ValidationResult(boolean passed, List<String> missing, Map<String, String> typeErrors,
                List<String> lengthErrors) {
            this.passed = passed;
            this.missing = missing;
            this.typeErrors = typeErrors;
            this.lengthErrors = lengthErrors;
        }

        public boolean isPassed() {
            return passed;
        }

        public List<String> getMissing() {
            return missing;
        }

        public Map<String, String> getTypeErrors() {
            return typeErrors;
        }

        public List<String> getLengthErrors() {
            return lengthErrors;
        }
    }

    public static class DryRunResult {
        private Object finalEChartsConfig;
        private Map<String, Object> queryPreview;
        private Set<String> remainingPlaceholders;

        public DryRunResult(Object cfg, Map<String, Object> preview, Set<String> remain) {
            this.finalEChartsConfig = cfg;
            this.queryPreview = preview;
            this.remainingPlaceholders = remain;
        }

        public Object getFinalEChartsConfig() {
            return finalEChartsConfig;
        }

        public Map<String, Object> getQueryPreview() {
            return queryPreview;
        }

        public Set<String> getRemainingPlaceholders() {
            return remainingPlaceholders;
        }
    }
}
