package com.example.chart.service;

import com.example.chart.model.PlaceholderCatalog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 目录生成：运行阶段一（模板+spec）后，对输出JSON扫描占位符，记录JSONPath和类型/分组
 */
@Service
public class PlaceholderCatalogService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{[^}]+}" );

    @Autowired
    private TwoStageTransformationService twoStageTransformationService;

    @Autowired
    private TemplateService templateService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 生成占位符目录（不持久化版本，仅返回）
     */
    public PlaceholderCatalog generateCatalog(String chartId, String templateVersion, String specVersion) {
        // 1) 取模板（若提供版本则可扩展从版本仓取，这里先直接按chartId）
        Map<String, Object> template = templateService.getTemplateByChartId(chartId);
        // 2) 阶段一：结构转换，保留占位符
        var stage1 = twoStageTransformationService.executeStage1Transformation(chartId, template);
        Object echartsStructure = stage1.getResult();

        // 3) 扫描输出，提取占位符，记录JSONPath与类型
        PlaceholderCatalog catalog = new PlaceholderCatalog();
        catalog.setChartId(chartId);
        catalog.setTemplateVersion(templateVersion);
        catalog.setSpecVersion(specVersion);
        catalog.setGeneratedAt(Instant.now().toEpochMilli());

        JsonNode root = mapper.valueToTree(echartsStructure);
        List<PlaceholderCatalog.Item> items = new ArrayList<>();
        scanNode(root, "$.", items);
        catalog.setPlaceholders(items);
        catalog.setChecksum(UUID.randomUUID().toString());
        return catalog;
    }

    private void scanNode(JsonNode node, String path, List<PlaceholderCatalog.Item> out) {
        if (node == null) return;
        switch (node.getNodeType()) {
            case OBJECT:
                var fields = node.fields();
                while (fields.hasNext()) {
                    var e = fields.next();
                    String childPath = path + e.getKey();
                    scanNode(e.getValue(), childPath + ".", out);
                }
                break;
            case ARRAY:
                ArrayNode arr = (ArrayNode) node;
                for (int i = 0; i < arr.size(); i++) {
                    scanNode(arr.get(i), path + "[" + i + "].", out);
                }
                break;
            case STRING:
                String text = node.asText();
                Matcher m = PLACEHOLDER.matcher(text);
                if (m.matches()) {
                    // 整个值是一个占位符，保持原始类型
                    addItem(out, text, inferGroup(path), inferTypeByParent(node, path), trimDot(path));
                } else {
                    // 文本中包含多个占位符（如 formatter），逐个登记为string
                    m.reset();
                    while (m.find()) {
                        addItem(out, m.group(), inferGroup(path), "string", trimDot(path));
                    }
                }
                break;
            default:
                // number/boolean/null 不作为占位符
                break;
        }
    }

    private void addItem(List<PlaceholderCatalog.Item> out, String name, String group, String type, String jsonPath) {
        PlaceholderCatalog.Item it = new PlaceholderCatalog.Item();
        it.setName(name);
        it.setVariable(name.substring(2, name.length()-1));
        it.setType(type);
        it.setRequired(true);
        it.setGroup(group);
        it.setTargetPath(jsonPath);
        out.add(it);
    }

    private String inferTypeByParent(JsonNode node, String path) {
        // 简化：若父级为数组的 data 字段，视为 array，否则 string
        if (path.contains("series[") && path.endsWith("data.")) return "array";
        if (path.contains("xAxis.") && path.endsWith("data.")) return "array";
        return "string";
    }

    private String inferGroup(String path) {
        if (path.contains("title.")) return "title";
        if (path.contains("legend.")) return "legend";
        if (path.contains("xAxis.")) return "xAxis";
        if (path.contains("yAxis.")) return "yAxis";
        if (path.contains("series[")) return "series";
        return "general";
    }

    private String trimDot(String p) {
        if (p.endsWith(".")) return p.substring(0, p.length()-1);
        return p;
    }
}

