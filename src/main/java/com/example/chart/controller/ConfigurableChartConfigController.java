package com.example.chart.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.ApiResponse;
import com.example.chart.service.ConfigurableChartMappingService;

/**
 * 可配置的图表配置控制器
 * 解决硬编码问题，使用配置化服务提供图表配置信息
 */
@RestController
@RequestMapping("/api/chart/configurable")
public class ConfigurableChartConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableChartConfigController.class);

    @Autowired
    private ConfigurableChartMappingService mappingService;

    /**
     * 获取图表类型映射配置（配置化版本）
     */
    @GetMapping("/chart-types")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartTypes() {
        logger.info("📋 [配置化接口] 获取图表类型映射配置");

        Map<String, Object> response = new HashMap<>();

        try {
            // 从配置化服务获取映射关系
            Map<String, String> pathToChartId = mappingService.getPathToChartIdMapping();
            Map<String, String> chartIdToPath = mappingService.getChartIdToPathMapping();
            Map<String, List<String>> categories = mappingService.getCategoriesMapping();

            response.put("pathToChartId", pathToChartId);
            response.put("chartIdToPath", chartIdToPath);
            response.put("categories", categories);
            response.put("totalCharts", mappingService.getTotalChartCount());
            response.put("configSource", "YAML配置文件");
            response.put("lastUpdated", System.currentTimeMillis());

            logger.info("✅ [配置化接口] 返回 {} 个图表类型映射", mappingService.getTotalChartCount());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 获取图表类型映射失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取图表类型映射失败: " + e.getMessage()));
        }
    }

    /**
     * 获取图表特定配置（配置化版本）
     */
    @GetMapping("/chart-specific/{chartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartSpecificConfig(@PathVariable String chartId) {
        logger.info("📋 [配置化接口] 获取图表特定配置: {}", chartId);

        try {
            Map<String, Object> config = mappingService.getChartSpecificConfig(chartId);

            if (config.isEmpty()) {
                logger.warn("⚠️ [配置化接口] 未找到图表 {} 的特定配置", chartId);
                return ResponseEntity.ok(ApiResponse.error("NOT_FOUND", "未找到图表配置: " + chartId));
            }

            // 添加元数据
            config.put("chartId", chartId);
            config.put("configSource", "YAML配置文件");
            config.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [配置化接口] 返回图表 {} 的配置", chartId);
            return ResponseEntity.ok(ApiResponse.ok(config));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 获取图表特定配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取图表配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取预处理规则（配置化版本）
     */
    @GetMapping("/preprocessing-rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreprocessingRules() {
        logger.info("📋 [配置化接口] 获取图表预处理规则");

        try {
            Map<String, Object> rules = mappingService.getPreprocessingRules();

            Map<String, Object> response = new HashMap<>();
            response.put("rules", rules);
            response.put("configSource", "YAML配置文件");
            response.put("totalRules", rules.size());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [配置化接口] 返回 {} 个预处理规则", rules.size());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 获取预处理规则失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取预处理规则失败: " + e.getMessage()));
        }
    }

    /**
     * 根据文件路径获取图表ID（配置化版本）
     */
    @GetMapping("/chart-id-by-path")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartIdByFilePath(@RequestParam String filePath) {
        logger.info("📋 [配置化接口] 根据文件路径获取图表ID: {}", filePath);

        try {
            String chartId = mappingService.getChartIdByFilePath(filePath);

            Map<String, Object> response = new HashMap<>();
            if (chartId != null) {
                response.put("chartId", chartId);
                response.put("filePath", filePath);
                response.put("found", true);
                logger.info("✅ [配置化接口] 找到图表ID: {}", chartId);
            } else {
                response.put("found", false);
                response.put("filePath", filePath);
                logger.warn("⚠️ [配置化接口] 未找到文件路径对应的图表ID: {}", filePath);
            }

            response.put("configSource", "YAML配置文件");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 根据文件路径获取图表ID失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取图表ID失败: " + e.getMessage()));
        }
    }

    /**
     * 重新加载配置
     */
    @GetMapping("/reload-config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reloadConfig() {
        logger.info("🔄 [配置化接口] 重新加载配置");

        try {
            mappingService.reloadConfigurations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "配置重新加载成功");
            response.put("totalCharts", mappingService.getTotalChartCount());
            response.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [配置化接口] 配置重新加载成功");
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 重新加载配置失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "重新加载配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取配置状态信息
     */
    @GetMapping("/config-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfigStatus() {
        logger.info("📊 [配置化接口] 获取配置状态信息");

        try {
            Map<String, Object> status = new HashMap<>();
            status.put("totalCharts", mappingService.getTotalChartCount());
            status.put("totalCategories", mappingService.getCategoriesMapping().size());
            status.put("configSource", "YAML配置文件");
            status.put("isConfigurable", true);
            status.put("supportsHotReload", true);
            status.put("timestamp", System.currentTimeMillis());

            logger.info("✅ [配置化接口] 返回配置状态信息");
            return ResponseEntity.ok(ApiResponse.ok(status));

        } catch (Exception e) {
            logger.error("❌ [配置化接口] 获取配置状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("ERROR", "获取配置状态失败: " + e.getMessage()));
        }
    }
}
