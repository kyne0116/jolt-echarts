package com.example.chart.controller;

import com.example.api.ApiResponse;
import com.example.chart.model.PlaceholderCatalog;
import com.example.chart.service.PlaceholderCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/charts")
public class PlaceholderCatalogController {

    @Autowired
    private PlaceholderCatalogService catalogService;

    /**
     * 生成占位符目录（不持久化），用于前端“自动发现/校验”按钮
     */
    @PostMapping("/{chartId}/placeholder-catalog/generate")
    public ResponseEntity<ApiResponse<PlaceholderCatalog>> generate(@PathVariable String chartId,
                                                                    @RequestBody(required = false) Map<String, String> body) {
        String templateVersion = body != null ? body.getOrDefault("templateVersion", "") : "";
        String specVersion = body != null ? body.getOrDefault("specVersion", "") : "";
        PlaceholderCatalog catalog = catalogService.generateCatalog(chartId, templateVersion, specVersion);
        return ResponseEntity.ok(ApiResponse.ok(catalog));
    }
}

