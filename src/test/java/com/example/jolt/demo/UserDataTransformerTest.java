package com.example.jolt.demo;

import com.bazaarvoice.jolt.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户数据转换器的单元测试
 * 
 * @author Demo Author
 * @version 1.0.0
 */
@DisplayName("用户数据转换器测试")
class UserDataTransformerTest {
    
    private UserDataTransformer transformer;
    private Object inputData;
    
    @BeforeEach
    void setUp() throws Exception {
        transformer = new UserDataTransformer();
        
        // 加载测试数据
        try (InputStream inputStream = getClass().getResourceAsStream("/input-data.json")) {
            assertNotNull(inputStream, "输入数据文件不存在");
            inputData = JsonUtils.jsonToObject(inputStream);
        }
    }
    
    @Test
    @DisplayName("测试单个用户数据转换")
    void testTransformUserData() {
        Object result = transformer.transformUserData(inputData);
        
        assertNotNull(result, "转换结果不应为空");
        assertTrue(result instanceof Map, "转换结果应该是Map类型");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        // 验证基本结构
        assertTrue(resultMap.containsKey("profile"), "应包含profile");
        assertTrue(resultMap.containsKey("employment"), "应包含employment");
        assertTrue(resultMap.containsKey("skills"), "应包含skills");
        assertTrue(resultMap.containsKey("preferences"), "应包含preferences");
        assertTrue(resultMap.containsKey("settings"), "应包含settings");
        assertTrue(resultMap.containsKey("account"), "应包含account");
    }
    
    @Test
    @DisplayName("测试批量用户数据转换")
    void testTransformUserDataBatch() {
        Object[] inputArray = {inputData, inputData, inputData};
        Object[] results = transformer.transformUserDataBatch(inputArray);
        
        assertNotNull(results, "批量转换结果不应为空");
        assertEquals(3, results.length, "结果数组长度应该正确");
        
        for (Object result : results) {
            assertNotNull(result, "每个转换结果都不应为空");
            assertTrue(result instanceof Map, "每个转换结果都应该是Map类型");
        }
    }
    
    @Test
    @DisplayName("测试空数组批量转换")
    void testTransformUserDataBatchEmpty() {
        Object[] emptyArray = {};
        Object[] results = transformer.transformUserDataBatch(emptyArray);
        
        assertNotNull(results, "空数组转换结果不应为空");
        assertEquals(0, results.length, "空数组转换结果长度应为0");
    }
    
    @Test
    @DisplayName("测试null数组批量转换")
    void testTransformUserDataBatchNull() {
        Object[] results = transformer.transformUserDataBatch(null);
        
        assertNotNull(results, "null数组转换结果不应为空");
        assertEquals(0, results.length, "null数组转换结果长度应为0");
    }
    
    @Test
    @DisplayName("测试转换结果验证")
    void testValidateTransformedData() {
        Object transformedData = transformer.transformUserData(inputData);
        UserDataTransformer.ValidationResult result = 
            transformer.validateTransformedData(transformedData);
        
        assertNotNull(result, "验证结果不应为空");
        assertTrue(result.isValid(), "转换结果应该通过验证，错误: " + result.getErrors());
        assertTrue(result.getErrors().isEmpty(), "不应该有验证错误");
    }
    
    @Test
    @DisplayName("测试null数据验证")
    void testValidateNullData() {
        UserDataTransformer.ValidationResult result = 
            transformer.validateTransformedData(null);
        
        assertNotNull(result, "验证结果不应为空");
        assertFalse(result.isValid(), "null数据应该验证失败");
        assertFalse(result.getErrors().isEmpty(), "应该有验证错误");
        assertTrue(result.getErrors().get(0).contains("转换结果为空"), 
            "错误信息应该指出结果为空");
    }
    
    @Test
    @DisplayName("测试转换分析")
    void testAnalyzeTransformation() {
        Object transformedData = transformer.transformUserData(inputData);
        UserDataTransformer.TransformationAnalysis analysis = 
            transformer.analyzeTransformation(inputData, transformedData);
        
        assertNotNull(analysis, "分析结果不应为空");
        
        // 验证基本统计信息
        assertTrue(analysis.getInputSize() > 0, "输入大小应该大于0");
        assertTrue(analysis.getOutputSize() > 0, "输出大小应该大于0");
        assertTrue(analysis.getSizeChangeRatio() > 0, "大小变化比应该大于0");
        
        assertTrue(analysis.getInputFieldCount() > 0, "输入字段数应该大于0");
        assertTrue(analysis.getOutputFieldCount() > 0, "输出字段数应该大于0");
        assertTrue(analysis.getFieldChangeRatio() > 0, "字段变化比应该大于0");
        
        assertTrue(analysis.getInputDepth() >= 0, "输入深度应该大于等于0");
        assertTrue(analysis.getOutputDepth() > analysis.getInputDepth(), 
            "输出深度应该大于输入深度（因为创建了嵌套结构）");
        
        // 验证toString方法
        String analysisString = analysis.toString();
        assertNotNull(analysisString, "分析结果字符串不应为空");
        assertTrue(analysisString.contains("TransformationAnalysis"), 
            "分析结果字符串应该包含类名");
    }
    
    @Test
    @DisplayName("测试空输入转换异常")
    void testTransformNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            transformer.transformUserData(null);
        }, "null输入应该抛出IllegalArgumentException");
    }
    
    @Test
    @DisplayName("测试转换器构造函数")
    void testTransformerConstructor() {
        assertDoesNotThrow(() -> {
            new UserDataTransformer();
        }, "默认构造函数不应该抛出异常");
    }
    
    @Test
    @DisplayName("测试验证结果类")
    void testValidationResultClass() {
        UserDataTransformer.ValidationResult result = 
            new UserDataTransformer.ValidationResult();
        
        assertTrue(result.isValid(), "新创建的验证结果应该是有效的");
        assertTrue(result.getErrors().isEmpty(), "新创建的验证结果不应该有错误");
        
        result.addError("测试错误");
        assertFalse(result.isValid(), "添加错误后应该变为无效");
        assertEquals(1, result.getErrors().size(), "应该有一个错误");
        assertEquals("测试错误", result.getErrors().get(0), "错误信息应该正确");
    }
    
    @Test
    @DisplayName("测试分析结果类的getter和setter")
    void testTransformationAnalysisGettersSetters() {
        UserDataTransformer.TransformationAnalysis analysis = 
            new UserDataTransformer.TransformationAnalysis();
        
        // 测试所有的setter和getter
        analysis.setInputSize(100);
        assertEquals(100, analysis.getInputSize());
        
        analysis.setOutputSize(200);
        assertEquals(200, analysis.getOutputSize());
        
        analysis.setSizeChangeRatio(2.0);
        assertEquals(2.0, analysis.getSizeChangeRatio(), 0.001);
        
        analysis.setInputFieldCount(10);
        assertEquals(10, analysis.getInputFieldCount());
        
        analysis.setOutputFieldCount(20);
        assertEquals(20, analysis.getOutputFieldCount());
        
        analysis.setFieldChangeRatio(2.0);
        assertEquals(2.0, analysis.getFieldChangeRatio(), 0.001);
        
        analysis.setInputDepth(1);
        assertEquals(1, analysis.getInputDepth());
        
        analysis.setOutputDepth(3);
        assertEquals(3, analysis.getOutputDepth());
    }
    
    @Test
    @DisplayName("测试批量转换中的错误处理")
    void testBatchTransformationErrorHandling() {
        // 创建包含无效数据的数组
        Object[] mixedArray = {inputData, "invalid_data", inputData};
        Object[] results = transformer.transformUserDataBatch(mixedArray);
        
        assertNotNull(results, "批量转换结果不应为空");
        assertEquals(3, results.length, "结果数组长度应该正确");
        
        // 第一个和第三个应该成功，第二个应该失败（为null）
        assertNotNull(results[0], "第一个结果应该成功");
        assertNull(results[1], "第二个结果应该失败（为null）");
        assertNotNull(results[2], "第三个结果应该成功");
    }
}
