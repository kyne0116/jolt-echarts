package com.example.jolt.demo;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 用户数据转换器
 * 
 * 提供用户数据转换的核心功能，包括：
 * 1. 扁平化数据到层级化数据的转换
 * 2. 数据验证和清理
 * 3. 转换结果分析
 * 
 * @author Demo Author
 * @version 1.0.0
 */
public class UserDataTransformer {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDataTransformer.class);
    
    private final Chainr chainr;
    
    /**
     * 构造函数 - 使用默认转换规范
     */
    public UserDataTransformer() throws Exception {
        this(loadDefaultTransformationSpec());
    }
    
    /**
     * 构造函数 - 使用自定义转换规范
     */
    public UserDataTransformer(List<Object> transformSpec) {
        this.chainr = Chainr.fromSpec(transformSpec);
        logger.info("用户数据转换器初始化完成");
    }
    
    /**
     * 转换用户数据
     * 
     * @param inputData 输入的扁平化用户数据
     * @return 转换后的层级化用户数据
     */
    public Object transformUserData(Object inputData) {
        if (inputData == null) {
            throw new IllegalArgumentException("输入数据不能为空");
        }
        
        try {
            logger.debug("开始转换用户数据");
            Object result = chainr.transform(inputData);
            logger.debug("用户数据转换完成");
            return result;
            
        } catch (Exception e) {
            logger.error("用户数据转换失败", e);
            throw new RuntimeException("数据转换过程中发生错误", e);
        }
    }
    
    /**
     * 批量转换用户数据
     * 
     * @param inputDataList 输入的用户数据列表
     * @return 转换后的用户数据列表
     */
    public Object[] transformUserDataBatch(Object[] inputDataList) {
        if (inputDataList == null || inputDataList.length == 0) {
            return new Object[0];
        }
        
        Object[] results = new Object[inputDataList.length];
        
        for (int i = 0; i < inputDataList.length; i++) {
            try {
                results[i] = transformUserData(inputDataList[i]);
                logger.debug("批量转换进度: {}/{}", i + 1, inputDataList.length);
                
            } catch (Exception e) {
                logger.error("批量转换第{}条数据失败", i + 1, e);
                results[i] = null; // 或者可以选择跳过失败的数据
            }
        }
        
        logger.info("批量转换完成，成功转换 {}/{} 条数据", 
            countNonNullElements(results), inputDataList.length);
        
        return results;
    }
    
    /**
     * 验证转换结果
     * 
     * @param transformedData 转换后的数据
     * @return 验证结果
     */
    public ValidationResult validateTransformedData(Object transformedData) {
        ValidationResult result = new ValidationResult();
        
        if (transformedData == null) {
            result.addError("转换结果为空");
            return result;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) transformedData;
            
            // 验证必需的顶级字段
            validateRequiredField(data, "profile", result);
            validateRequiredField(data, "employment", result);
            validateRequiredField(data, "skills", result);
            validateRequiredField(data, "preferences", result);
            validateRequiredField(data, "settings", result);
            validateRequiredField(data, "account", result);
            
            // 验证profile结构
            if (data.containsKey("profile")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) data.get("profile");
                validateRequiredField(profile, "userId", result);
                validateRequiredField(profile, "personalInfo", result);
                validateRequiredField(profile, "contactInfo", result);
                validateRequiredField(profile, "address", result);
            }
            
            logger.debug("数据验证完成，发现 {} 个错误", result.getErrors().size());
            
        } catch (ClassCastException e) {
            result.addError("转换结果格式不正确：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析转换结果
     * 
     * @param inputData 原始输入数据
     * @param transformedData 转换后数据
     * @return 分析结果
     */
    public TransformationAnalysis analyzeTransformation(Object inputData, Object transformedData) {
        TransformationAnalysis analysis = new TransformationAnalysis();
        
        try {
            String inputJson = JsonUtils.toJsonString(inputData);
            String outputJson = JsonUtils.toJsonString(transformedData);
            
            analysis.setInputSize(inputJson.length());
            analysis.setOutputSize(outputJson.length());
            analysis.setSizeChangeRatio((double) outputJson.length() / inputJson.length());
            
            // 简单的字段计数（基于逗号分隔）
            int inputFields = inputJson.split(",").length;
            int outputFields = outputJson.split(",").length;
            
            analysis.setInputFieldCount(inputFields);
            analysis.setOutputFieldCount(outputFields);
            analysis.setFieldChangeRatio((double) outputFields / inputFields);
            
            // 计算嵌套层级深度
            analysis.setInputDepth(calculateJsonDepth(inputData));
            analysis.setOutputDepth(calculateJsonDepth(transformedData));
            
            logger.debug("转换分析完成");
            
        } catch (Exception e) {
            logger.error("转换分析失败", e);
        }
        
        return analysis;
    }
    
    /**
     * 加载默认转换规范
     */
    private static List<Object> loadDefaultTransformationSpec() throws Exception {
        try (InputStream inputStream = UserDataTransformer.class
                .getResourceAsStream("/transformation-spec.json")) {
            if (inputStream == null) {
                throw new RuntimeException("无法找到默认转换规范文件");
            }
            return JsonUtils.jsonToList(inputStream);
        }
    }
    
    /**
     * 验证必需字段
     */
    private void validateRequiredField(Map<String, Object> data, String fieldName, 
                                     ValidationResult result) {
        if (!data.containsKey(fieldName) || data.get(fieldName) == null) {
            result.addError("缺少必需字段: " + fieldName);
        }
    }
    
    /**
     * 计算非空元素数量
     */
    private int countNonNullElements(Object[] array) {
        int count = 0;
        for (Object element : array) {
            if (element != null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 计算JSON深度
     */
    private int calculateJsonDepth(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            int maxDepth = 0;
            for (Object value : map.values()) {
                maxDepth = Math.max(maxDepth, calculateJsonDepth(value));
            }
            return 1 + maxDepth;
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            int maxDepth = 0;
            for (Object item : list) {
                maxDepth = Math.max(maxDepth, calculateJsonDepth(item));
            }
            return 1 + maxDepth;
        } else {
            return 0;
        }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public java.util.List<String> getErrors() {
            return errors;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
    }
    
    /**
     * 转换分析结果类
     */
    public static class TransformationAnalysis {
        private int inputSize;
        private int outputSize;
        private double sizeChangeRatio;
        private int inputFieldCount;
        private int outputFieldCount;
        private double fieldChangeRatio;
        private int inputDepth;
        private int outputDepth;
        
        // Getters and Setters
        public int getInputSize() { return inputSize; }
        public void setInputSize(int inputSize) { this.inputSize = inputSize; }
        
        public int getOutputSize() { return outputSize; }
        public void setOutputSize(int outputSize) { this.outputSize = outputSize; }
        
        public double getSizeChangeRatio() { return sizeChangeRatio; }
        public void setSizeChangeRatio(double sizeChangeRatio) { this.sizeChangeRatio = sizeChangeRatio; }
        
        public int getInputFieldCount() { return inputFieldCount; }
        public void setInputFieldCount(int inputFieldCount) { this.inputFieldCount = inputFieldCount; }
        
        public int getOutputFieldCount() { return outputFieldCount; }
        public void setOutputFieldCount(int outputFieldCount) { this.outputFieldCount = outputFieldCount; }
        
        public double getFieldChangeRatio() { return fieldChangeRatio; }
        public void setFieldChangeRatio(double fieldChangeRatio) { this.fieldChangeRatio = fieldChangeRatio; }
        
        public int getInputDepth() { return inputDepth; }
        public void setInputDepth(int inputDepth) { this.inputDepth = inputDepth; }
        
        public int getOutputDepth() { return outputDepth; }
        public void setOutputDepth(int outputDepth) { this.outputDepth = outputDepth; }
        
        @Override
        public String toString() {
            return String.format(
                "TransformationAnalysis{输入大小=%d, 输出大小=%d, 大小变化比=%.2f, " +
                "输入字段数=%d, 输出字段数=%d, 字段变化比=%.2f, " +
                "输入深度=%d, 输出深度=%d}",
                inputSize, outputSize, sizeChangeRatio,
                inputFieldCount, outputFieldCount, fieldChangeRatio,
                inputDepth, outputDepth
            );
        }
    }
}
