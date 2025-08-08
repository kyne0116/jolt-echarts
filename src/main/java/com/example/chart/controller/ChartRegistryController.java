package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.example.chart.model.ChartRegistry;
import com.example.chart.service.ChartRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charts")
public class ChartRegistryController {

    @Autowired
    private ChartRegistryService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChartRegistry>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(service.list()));
    }

    @GetMapping("/{chartId}/registry")
    public ResponseEntity<ApiResponse<ChartRegistry>> get(@PathVariable String chartId) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(chartId).orElse(null)));
    }

    @PutMapping("/{chartId}/registry/activate")
    public ResponseEntity<ApiResponse<ChartRegistry>> activate(@PathVariable String chartId,
                                                               @RequestBody Map<String,String> body) {
        String t = body.getOrDefault("templateVersion", "");
        String s = body.getOrDefault("specVersion", "");
        String m = body.getOrDefault("mappingVersion", "");
        return ResponseEntity.ok(ApiResponse.ok(service.activate(chartId, t, s, m)));
    }
}

