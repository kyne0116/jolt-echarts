package com.example.chart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.ApiResponse;
import com.example.chart.model.UniversalChartDataView;
import com.example.chart.service.SimpleUniversalDataCrudService;

/**
 * 简化的UniversalChartDataView CRUD控制器
 * 提供40个固定字段的基本增删改查操作
 * 
 * @author Chart System
 * @version 1.0 - 简化版本
 */
@RestController
@RequestMapping("/api/chart/simple-data")
@CrossOrigin(origins = "*")
public class SimpleUniversalDataController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUniversalDataController.class);

    @Autowired
    private SimpleUniversalDataCrudService crudService;

    /**
     * 获取所有数据记录（支持年份和地区筛选）
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String region) {
        logger.info("📋 [简化CRUD] 获取数据记录列表，页码: {}, 大小: {}, 年份: {}, 地区: {}", page, size, year, region);

        try {
            List<UniversalChartDataView> allRecords;
            
            // 根据筛选条件获取数据
            if (year != null || region != null) {
                allRecords = crudService.findWithFilters(year, region);
                logger.info("📊 [筛选查询] 筛选条件 - 年份: {}, 地区: {}, 筛选后数量: {}", year, region, allRecords.size());
            } else {
                allRecords = crudService.findAll();
                logger.info("📊 [全量查询] 无筛选条件，返回全部数据: {} 条", allRecords.size());
            }

            // 计算筛选后的总数
            long totalCount = allRecords.size();

            // 分页处理
            List<UniversalChartDataView> records;
            if (page >= 0 && size > 0) {
                int start = page * size;
                int end = Math.min(start + size, allRecords.size());
                
                if (start >= allRecords.size()) {
                    records = new ArrayList<>();
                } else {
                    records = allRecords.subList(start, end);
                }
            } else {
                records = allRecords;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("records", records);
            response.put("totalCount", totalCount);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("hasFilters", year != null || region != null);
            response.put("filters", Map.of(
                "year", year != null ? year : "",
                "region", region != null ? region : ""
            ));

            logger.info("✅ [简化CRUD] 返回 {} 条数据记录（筛选后总数: {}）", records.size(), totalCount);
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 获取数据记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取数据记录失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取单条记录
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<ApiResponse<UniversalChartDataView>> getRecordById(@PathVariable Long id) {
        logger.info("📋 [简化CRUD] 获取数据记录: ID={}", id);

        try {
            UniversalChartDataView record = crudService.findById(id);

            if (record != null) {
                logger.info("✅ [简化CRUD] 找到数据记录: ID={}", id);
                return ResponseEntity.ok(ApiResponse.ok(record));
            } else {
                logger.warn("⚠️ [简化CRUD] 数据记录不存在: ID={}", id);
                return ResponseEntity.ok(ApiResponse.error("NOT_FOUND", "数据记录不存在"));
            }

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 获取数据记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取数据记录失败: " + e.getMessage()));
        }
    }

    /**
     * 创建新的数据记录
     */
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<UniversalChartDataView>> createRecord(
            @RequestBody UniversalChartDataView record) {
        logger.info("💾 [简化CRUD] 创建新数据记录");

        try {
            // 确保ID为空（新建记录）
            record.setId(null);

            UniversalChartDataView savedRecord = crudService.save(record);

            logger.info("✅ [简化CRUD] 数据记录创建成功: ID={}", savedRecord.getId());
            return ResponseEntity.ok(ApiResponse.ok(savedRecord));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 创建数据记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "创建数据记录失败: " + e.getMessage()));
        }
    }

    /**
     * 更新数据记录
     */
    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponse<UniversalChartDataView>> updateRecord(
            @PathVariable Long id, @RequestBody UniversalChartDataView record) {
        logger.info("🔄 [简化CRUD] 更新数据记录: ID={}", id);

        try {
            // 检查记录是否存在
            UniversalChartDataView existingRecord = crudService.findById(id);
            if (existingRecord == null) {
                logger.warn("⚠️ [简化CRUD] 数据记录不存在: ID={}", id);
                return ResponseEntity.ok(ApiResponse.error("NOT_FOUND", "数据记录不存在"));
            }

            // 设置ID并保存
            record.setId(id);
            UniversalChartDataView updatedRecord = crudService.save(record);

            logger.info("✅ [简化CRUD] 数据记录更新成功: ID={}", id);
            return ResponseEntity.ok(ApiResponse.ok(updatedRecord));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 更新数据记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "更新数据记录失败: " + e.getMessage()));
        }
    }

    /**
     * 删除数据记录
     */
    @DeleteMapping("/records/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteRecord(@PathVariable Long id) {
        logger.info("🗑️ [简化CRUD] 删除数据记录: ID={}", id);

        try {
            boolean deleted = crudService.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("deleted", deleted);
            response.put("id", id);

            if (deleted) {
                logger.info("✅ [简化CRUD] 数据记录删除成功: ID={}", id);
                response.put("message", "数据记录删除成功");
                return ResponseEntity.ok(ApiResponse.ok(response));
            } else {
                logger.warn("⚠️ [简化CRUD] 数据记录不存在: ID={}", id);
                response.put("message", "数据记录不存在");
                return ResponseEntity.ok(ApiResponse.error("NOT_FOUND", "数据记录不存在"));
            }

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 删除数据记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "删除数据记录失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除数据记录
     */
    @DeleteMapping("/records/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteRecords(
            @RequestBody List<Long> ids) {
        logger.info("🗑️ [简化CRUD] 批量删除数据记录: {} 条", ids.size());

        try {
            int deletedCount = crudService.deleteByIds(ids);

            Map<String, Object> response = new HashMap<>();
            response.put("requestedCount", ids.size());
            response.put("deletedCount", deletedCount);
            response.put("message", String.format("成功删除 %d 条记录", deletedCount));

            logger.info("✅ [简化CRUD] 批量删除完成: 删除了 {} 条记录", deletedCount);
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 批量删除失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "批量删除失败: " + e.getMessage()));
        }
    }

    /**
     * 根据图表类型查询数据
     */
    @GetMapping("/records/by-chart-type/{chartType}")
    public ResponseEntity<ApiResponse<List<UniversalChartDataView>>> getRecordsByChartType(
            @PathVariable String chartType) {
        logger.info("📊 [简化CRUD] 根据图表类型查询: {}", chartType);

        try {
            List<UniversalChartDataView> records = crudService.findByCategory(chartType);

            logger.info("✅ [简化CRUD] 找到 {} 条 {} 类型的记录", records.size(), chartType);
            return ResponseEntity.ok(ApiResponse.ok(records));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 根据图表类型查询失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据渠道名称查询数据
     */
    @GetMapping("/records/by-channel/{channelName}")
    public ResponseEntity<ApiResponse<List<UniversalChartDataView>>> getRecordsByChannel(
            @PathVariable String channelName) {
        logger.info("📡 [简化CRUD] 根据渠道查询: {}", channelName);

        try {
            List<UniversalChartDataView> records = crudService.findByChannel(channelName);

            logger.info("✅ [简化CRUD] 找到 {} 条 {} 渠道的记录", records.size(), channelName);
            return ResponseEntity.ok(ApiResponse.ok(records));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 根据渠道查询失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取字段信息
     */
    @GetMapping("/fields-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFieldsInfo() {
        logger.info("📋 [简化CRUD] 获取字段信息");

        try {
            Map<String, Object> fieldsInfo = crudService.getFieldsInfo();

            logger.info("✅ [简化CRUD] 返回字段信息");
            return ResponseEntity.ok(ApiResponse.ok(fieldsInfo));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 获取字段信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取字段信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        logger.info("📊 [简化CRUD] 获取统计信息");

        try {
            Map<String, Object> statistics = crudService.getFieldStatistics();

            logger.info("✅ [简化CRUD] 返回统计信息");
            return ResponseEntity.ok(ApiResponse.ok(statistics));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 获取统计信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 清空所有数据
     */
    @DeleteMapping("/records/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearAllRecords() {
        logger.info("🗑️ [简化CRUD] 清空所有数据");

        try {
            crudService.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "所有数据已清空");
            response.put("success", true);

            logger.info("✅ [简化CRUD] 所有数据清空完成");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 清空数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "清空数据失败: " + e.getMessage()));
        }
    }

    /**
     * 重新生成示例数据
     */
    @PostMapping("/regenerate-sample-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> regenerateSampleData() {
        logger.info("🔄 [简化CRUD] 重新生成示例数据");

        try {
            crudService.regenerateSampleData();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "示例数据重新生成完成");
            response.put("totalRecords", crudService.count());
            response.put("success", true);

            logger.info("✅ [简化CRUD] 示例数据重新生成完成");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [简化CRUD] 重新生成示例数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "重新生成示例数据失败: " + e.getMessage()));
        }
    }

    /**
     * 测试筛选参数接收
     */
    @GetMapping("/test-filter-params")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testFilterParams(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String region) {
        logger.info("🧪 [参数测试] 接收到筛选参数 - 年份: {}, 地区: {}", year, region);

        Map<String, Object> response = new HashMap<>();
        response.put("receivedYear", year);
        response.put("receivedRegion", region);
        response.put("timestamp", System.currentTimeMillis());
        
        // 测试筛选逻辑
        List<UniversalChartDataView> filteredData = crudService.findWithFilters(year, region);
        response.put("filteredCount", filteredData.size());
        response.put("totalCount", crudService.count());

        logger.info("🧪 [参数测试] 筛选结果：总数 {}, 筛选后 {}", crudService.count(), filteredData.size());
        
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
