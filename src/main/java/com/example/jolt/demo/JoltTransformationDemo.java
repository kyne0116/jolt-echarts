package com.example.jolt.demo;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Jolt JSON转换演示主类
 * 
 * 这个演示展示了如何使用Jolt库将扁平化的用户数据转换为层级化的复杂结构。
 * 演示包括：
 * 1. 数据重组和字段重命名
 * 2. 结构重构和嵌套层级创建
 * 3. 默认值应用
 * 4. 数据清理和排序
 * 
 * @author Demo Author
 * @version 1.0.0
 */
public class JoltTransformationDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(JoltTransformationDemo.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        try {
            logger.info("=== Jolt JSON转换演示开始 ===");

            // 创建演示实例并运行主要演示
            JoltTransformationDemo demo = new JoltTransformationDemo();
            demo.runDemo();

            // 运行额外的工具演示
            System.out.println("\n" + "=".repeat(60));
            logger.info("=== 额外的Jolt转换器演示 ===");
            JoltUtilities.runAllDemonstrations();

            logger.info("=== Jolt JSON转换演示完成 ===");

        } catch (Exception e) {
            logger.error("演示运行失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 运行完整的转换演示
     */
    public void runDemo() throws Exception {
        // 1. 加载输入数据
        Object inputData = loadInputData();
        logger.info("1. 输入数据加载完成");
        printJson("原始输入数据", inputData);
        
        // 2. 加载转换规范
        List<Object> transformSpec = loadTransformationSpec();
        logger.info("2. 转换规范加载完成");
        
        // 3. 创建Chainr转换器
        Chainr chainr = Chainr.fromSpec(transformSpec);
        logger.info("3. Chainr转换器创建完成");
        
        // 4. 执行转换
        Object transformedData = chainr.transform(inputData);
        logger.info("4. 数据转换执行完成");
        printJson("转换后数据", transformedData);
        
        // 5. 演示分步转换（用于调试）
        demonstrateStepByStepTransformation(chainr, inputData);
        
        // 6. 展示转换统计信息
        showTransformationStatistics(inputData, transformedData);

        // 7. 保存转换结果到文件
        saveTransformationResults(inputData, transformedData);

        // 8. 使用工具类进行额外分析
        performAdditionalAnalysis(inputData, transformedData, transformSpec);
    }
    
    /**
     * 从资源文件加载输入数据
     */
    private Object loadInputData() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/input-data.json")) {
            if (inputStream == null) {
                throw new RuntimeException("无法找到输入数据文件: input-data.json");
            }
            return JsonUtils.jsonToObject(inputStream);
        }
    }
    
    /**
     * 从资源文件加载转换规范
     */
    private List<Object> loadTransformationSpec() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/transformation-spec.json")) {
            if (inputStream == null) {
                throw new RuntimeException("无法找到转换规范文件: transformation-spec.json");
            }
            return JsonUtils.jsonToList(inputStream);
        }
    }
    
    /**
     * 演示分步转换过程（用于调试和理解）
     */
    private void demonstrateStepByStepTransformation(Chainr chainr, Object inputData) throws Exception {
        logger.info("\n=== 分步转换演示 ===");
        
        // 只执行第一步：Shift转换
        Object step1Result = chainr.transform(0, 1, inputData);
        printJson("步骤1 - Shift转换结果", step1Result);
        
        // 执行前两步：Shift + Default
        Object step2Result = chainr.transform(0, 2, inputData);
        printJson("步骤2 - Shift + Default转换结果", step2Result);
        
        // 执行前三步：Shift + Default + Sort
        Object step3Result = chainr.transform(0, 3, inputData);
        printJson("步骤3 - Shift + Default + Sort转换结果", step3Result);
    }
    
    /**
     * 展示转换统计信息
     */
    private void showTransformationStatistics(Object inputData, Object transformedData) throws Exception {
        logger.info("\n=== 转换统计信息 ===");
        
        String inputJson = JsonUtils.toJsonString(inputData);
        String outputJson = JsonUtils.toJsonString(transformedData);
        
        logger.info("输入数据字符数: {}", inputJson.length());
        logger.info("输出数据字符数: {}", outputJson.length());
        
        // 计算字段数量（简单统计）
        int inputFields = inputJson.split(",").length;
        int outputFields = outputJson.split(",").length;
        
        logger.info("输入字段数量（估算）: {}", inputFields);
        logger.info("输出字段数量（估算）: {}", outputFields);
        
        logger.info("数据结构复杂度提升: {}%", 
            Math.round(((double) outputFields / inputFields - 1) * 100));
    }
    
    /**
     * 格式化打印JSON数据
     */
    private void printJson(String title, Object data) throws Exception {
        logger.info("\n--- {} ---", title);
        String prettyJson = JsonUtils.toPrettyJsonString(data);
        System.out.println(prettyJson);
    }
    
    /**
     * 保存转换结果到文件
     */
    private void saveTransformationResults(Object inputData, Object transformedData) {
        logger.info("\n=== 保存转换结果到文件 ===");

        try {
            // 创建输出目录
            String outputDir = "output";
            Files.createDirectories(Paths.get(outputDir));

            // 保存原始输入数据
            String inputJson = JsonUtils.toPrettyJsonString(inputData);
            try (FileWriter writer = new FileWriter(outputDir + "/original-input.json")) {
                writer.write(inputJson);
            }
            logger.info("原始输入数据已保存到: {}/original-input.json", outputDir);

            // 保存转换后的数据
            String outputJson = JsonUtils.toPrettyJsonString(transformedData);
            try (FileWriter writer = new FileWriter(outputDir + "/transformed-output.json")) {
                writer.write(outputJson);
            }
            logger.info("转换后数据已保存到: {}/transformed-output.json", outputDir);

            // 保存转换对比报告
            saveTransformationReport(inputData, transformedData, outputDir);

        } catch (IOException e) {
            logger.error("保存文件时发生错误", e);
        }
    }

    /**
     * 保存转换对比报告
     */
    private void saveTransformationReport(Object inputData, Object transformedData, String outputDir) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Jolt JSON转换报告\n\n");
        report.append("## 转换概要\n");
        report.append("- 转换时间: ").append(java.time.LocalDateTime.now()).append("\n");

        String inputJson = JsonUtils.toJsonString(inputData);
        String outputJson = JsonUtils.toJsonString(transformedData);

        report.append("- 输入数据大小: ").append(inputJson.length()).append(" 字符\n");
        report.append("- 输出数据大小: ").append(outputJson.length()).append(" 字符\n");
        report.append("- 大小变化: ").append(String.format("%.1f%%",
            ((double) outputJson.length() / inputJson.length() - 1) * 100)).append("\n\n");

        report.append("## 结构变化\n");
        report.append("- 输入深度: 1层（扁平结构）\n");
        report.append("- 输出深度: 4-5层（层级结构）\n");
        report.append("- 主要分组: profile, employment, skills, preferences, settings, account\n\n");

        report.append("## 文件说明\n");
        report.append("- `original-input.json`: 原始输入数据\n");
        report.append("- `transformed-output.json`: 转换后的输出数据\n");
        report.append("- `transformation-report.md`: 本报告文件\n");

        try (FileWriter writer = new FileWriter(outputDir + "/transformation-report.md")) {
            writer.write(report.toString());
        }
        logger.info("转换报告已保存到: {}/transformation-report.md", outputDir);
    }

    /**
     * 使用工具类进行额外分析
     */
    private void performAdditionalAnalysis(Object inputData, Object transformedData, List<Object> transformSpec) {
        logger.info("\n=== 额外分析 ===");

        // 验证转换规范
        boolean isValid = JoltUtilities.validateTransformationSpec(transformSpec);
        logger.info("转换规范有效性: {}", isValid ? "有效" : "无效");

        // 分析输入和输出的复杂度
        JoltUtilities.analyzeJsonComplexity(inputData, "输入数据");
        JoltUtilities.analyzeJsonComplexity(transformedData, "输出数据");

        // 比较输入和输出
        JoltUtilities.compareJsonObjects(inputData, transformedData, "输入数据", "输出数据");

        // 性能测试（小规模）
        JoltUtilities.performanceTest(inputData, transformSpec, 100);
    }

    /**
     * 获取转换后的数据（供测试使用）
     */
    public Object transformData(Object inputData, List<Object> transformSpec) throws Exception {
        Chainr chainr = Chainr.fromSpec(transformSpec);
        return chainr.transform(inputData);
    }
}
