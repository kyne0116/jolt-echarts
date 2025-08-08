package com.example.chart.controller;

import com.example.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @GetMapping("/schema")
    public ResponseEntity<ApiResponse<Map<String,Object>>> schema() {
        Map<String,Object> schema = new LinkedHashMap<>();
        schema.put("tables", List.of(
                Map.of("name","chart_config","columns", List.of("title","stack_group","chart_type")),
                Map.of("name","marketing_data","columns", List.of("day_name","channel_name","conversion_count"))
        ));
        return ResponseEntity.ok(ApiResponse.ok(schema));
    }

    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<Map<String,Object>>> preview(@RequestParam String table,
                                                                   @RequestParam(defaultValue = "20") int limit) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("table", table);
        result.put("limit", limit);
        List<Map<String,Object>> rows = new ArrayList<>();
        if ("marketing_data".equals(table)) {
            String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            String[] channels = {"Email","Union Ads","Video Ads"};
            int[][] vals = { {120,132,101,134,90,230,210}, {220,182,191,234,290,330,310}, {150,232,201,154,190,330,410} };
            for (int c=0; c<channels.length; c++) {
                for (int i=0; i<days.length && rows.size()<limit; i++) {
                    rows.add(Map.of("day_name", days[i], "channel_name", channels[c], "conversion_count", vals[c][i]));
                }
            }
        } else if ("chart_config".equals(table)) {
            rows.add(Map.of("title","动态营销渠道分析","stack_group","Total","chart_type","line"));
        }
        result.put("rows", rows);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/query/preview")
    public ResponseEntity<ApiResponse<Map<String,Object>>> queryPreview(@RequestBody Map<String,Object> body) {
        // 简化：直接回显筛选条件，并返回固定聚合/透视样例
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("input", body);
        out.put("categories", List.of("Mon","Tue","Wed","Thu","Fri","Sat","Sun"));
        out.put("series", Map.of(
                "Email", List.of(120,132,101,134,90,230,210),
                "Union Ads", List.of(220,182,191,234,290,330,310)
        ));
        return ResponseEntity.ok(ApiResponse.ok(out));
    }
}

