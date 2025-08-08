package com.example.chart.controller;

import com.example.chart.repository.model.UniversalTemplateEntity;
import com.example.chart.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<UniversalTemplateEntity>> listAll() {
        return ResponseEntity.ok(templateService.listAll());
    }

    @GetMapping("/{chartId}")
    public ResponseEntity<Map<String, Object>> getByChartId(@PathVariable String chartId) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("chartId", chartId);
        resp.put("template", templateService.getTemplateByChartId(chartId));
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<UniversalTemplateEntity> create(@RequestBody UniversalTemplateEntity entity) {
        return ResponseEntity.ok(templateService.save(entity));
    }

    @PutMapping("/{chartId}")
    public ResponseEntity<UniversalTemplateEntity> update(@PathVariable String chartId,
                                                          @RequestBody UniversalTemplateEntity entity) {
        entity.setChartId(chartId);
        return ResponseEntity.ok(templateService.save(entity));
    }

    @DeleteMapping("/{chartId}")
    public ResponseEntity<Void> delete(@PathVariable String chartId) {
        templateService.delete(chartId);
        return ResponseEntity.noContent().build();
    }
}

