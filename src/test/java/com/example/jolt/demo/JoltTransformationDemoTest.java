package com.example.jolt.demo;

import com.bazaarvoice.jolt.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jolt转换演示的单元测试
 * 
 * 测试内容包括：
 * 1. 基本转换功能验证
 * 2. 转换结果结构验证
 * 3. 数据完整性验证
 * 4. 边界条件测试
 * 
 * @author Demo Author
 * @version 1.0.0
 */
@DisplayName("Jolt JSON转换演示测试")
class JoltTransformationDemoTest {
    
    private JoltTransformationDemo demo;
    private Object inputData;
    private List<Object> transformSpec;
    
    @BeforeEach
    void setUp() throws Exception {
        demo = new JoltTransformationDemo();
        
        // 加载测试数据
        try (InputStream inputStream = getClass().getResourceAsStream("/input-data.json")) {
            assertNotNull(inputStream, "输入数据文件不存在");
            inputData = JsonUtils.jsonToObject(inputStream);
        }
        
        // 加载转换规范
        try (InputStream inputStream = getClass().getResourceAsStream("/transformation-spec.json")) {
            assertNotNull(inputStream, "转换规范文件不存在");
            transformSpec = JsonUtils.jsonToList(inputStream);
        }
    }
    
    @Test
    @DisplayName("测试基本转换功能")
    void testBasicTransformation() throws Exception {
        // 执行转换
        Object result = demo.transformData(inputData, transformSpec);
        
        // 验证结果不为空
        assertNotNull(result, "转换结果不应为空");
        
        // 验证结果是Map类型
        assertTrue(result instanceof Map, "转换结果应该是Map类型");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        // 验证顶级结构
        assertTrue(resultMap.containsKey("profile"), "结果应包含profile字段");
        assertTrue(resultMap.containsKey("employment"), "结果应包含employment字段");
        assertTrue(resultMap.containsKey("skills"), "结果应包含skills字段");
        assertTrue(resultMap.containsKey("preferences"), "结果应包含preferences字段");
        assertTrue(resultMap.containsKey("settings"), "结果应包含settings字段");
        assertTrue(resultMap.containsKey("account"), "结果应包含account字段");
    }
    
    @Test
    @DisplayName("测试用户基本信息转换")
    void testProfileTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) resultMap.get("profile");
        assertNotNull(profile, "profile不应为空");
        
        // 验证用户ID
        assertEquals("12345", profile.get("userId"), "用户ID应该正确转换");
        
        // 验证个人信息结构
        assertTrue(profile.containsKey("personalInfo"), "应包含personalInfo");
        @SuppressWarnings("unchecked")
        Map<String, Object> personalInfo = (Map<String, Object>) profile.get("personalInfo");
        
        // 验证姓名信息
        assertTrue(personalInfo.containsKey("name"), "应包含name信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> name = (Map<String, Object>) personalInfo.get("name");
        assertEquals("张", name.get("firstName"), "名字应该正确转换");
        assertEquals("三", name.get("lastName"), "姓氏应该正确转换");
        
        // 验证年龄和性别
        assertEquals(28, personalInfo.get("age"), "年龄应该正确转换");
        assertEquals("M", personalInfo.get("gender"), "性别应该正确转换");
    }
    
    @Test
    @DisplayName("测试联系信息转换")
    void testContactInfoTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) resultMap.get("profile");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contactInfo = (Map<String, Object>) profile.get("contactInfo");
        assertNotNull(contactInfo, "contactInfo不应为空");
        
        assertEquals("zhangsan@example.com", contactInfo.get("email"), "邮箱应该正确转换");
        assertEquals("13800138000", contactInfo.get("phone"), "电话应该正确转换");
        assertEquals("email", contactInfo.get("preferredContact"), "应该有默认的首选联系方式");
    }
    
    @Test
    @DisplayName("测试地址信息转换")
    void testAddressTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) resultMap.get("profile");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) profile.get("address");
        assertNotNull(address, "address不应为空");
        
        assertEquals("中国", address.get("country"), "国家应该正确转换");
        assertEquals("北京市", address.get("province"), "省份应该正确转换");
        assertEquals("北京市", address.get("city"), "城市应该正确转换");
        assertEquals("朝阳区", address.get("district"), "区域应该正确转换");
        assertEquals("建国路88号", address.get("street"), "街道应该正确转换");
        assertEquals("100020", address.get("postalCode"), "邮编应该正确转换");
    }
    
    @Test
    @DisplayName("测试就业信息转换")
    void testEmploymentTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> employment = (Map<String, Object>) resultMap.get("employment");
        assertNotNull(employment, "employment不应为空");
        
        // 验证公司信息
        assertTrue(employment.containsKey("company"), "应包含company信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> company = (Map<String, Object>) employment.get("company");
        assertEquals("科技有限公司", company.get("name"), "公司名称应该正确转换");
        assertEquals("研发部", company.get("department"), "部门应该正确转换");
        
        // 验证职位和薪资
        assertEquals("高级工程师", employment.get("position"), "职位应该正确转换");
        assertTrue(employment.containsKey("compensation"), "应包含compensation信息");
        @SuppressWarnings("unchecked")
        Map<String, Object> compensation = (Map<String, Object>) employment.get("compensation");
        assertEquals(25000, compensation.get("salary"), "薪资应该正确转换");
        
        // 验证入职日期
        assertEquals("2020-03-15", employment.get("hireDate"), "入职日期应该正确转换");
    }
    
    @Test
    @DisplayName("测试技能信息转换")
    void testSkillsTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> skills = (Map<String, Object>) resultMap.get("skills");
        assertNotNull(skills, "skills不应为空");
        
        // 验证技术技能
        assertTrue(skills.containsKey("technical"), "应包含technical技能");
        @SuppressWarnings("unchecked")
        Map<String, Object> technical = (Map<String, Object>) skills.get("technical");
        
        // 验证Java技能
        assertTrue(technical.containsKey("java"), "应包含java技能");
        @SuppressWarnings("unchecked")
        Map<String, Object> javaSkill = (Map<String, Object>) technical.get("java");
        assertEquals(5, javaSkill.get("level"), "Java技能等级应该正确");
        assertEquals("Java", javaSkill.get("name"), "Java技能名称应该正确");
        assertEquals("Programming Language", javaSkill.get("category"), "Java技能分类应该正确");
        
        // 验证语言技能
        assertTrue(skills.containsKey("languages"), "应包含languages技能");
        @SuppressWarnings("unchecked")
        List<Object> languages = (List<Object>) skills.get("languages");
        assertFalse(languages.isEmpty(), "语言列表不应为空");
    }
    
    @Test
    @DisplayName("测试偏好设置转换")
    void testPreferencesTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> preferences = (Map<String, Object>) resultMap.get("preferences");
        assertNotNull(preferences, "preferences不应为空");
        
        // 验证爱好
        assertTrue(preferences.containsKey("hobbies"), "应包含hobbies");
        @SuppressWarnings("unchecked")
        List<Object> hobbies = (List<Object>) preferences.get("hobbies");
        assertFalse(hobbies.isEmpty(), "爱好列表不应为空");
        
        // 验证默认偏好设置
        assertEquals("light", preferences.get("theme"), "应该有默认主题");
        assertEquals("zh-CN", preferences.get("language"), "应该有默认语言");
        assertEquals("Asia/Shanghai", preferences.get("timezone"), "应该有默认时区");
    }
    
    @Test
    @DisplayName("测试设置信息转换")
    void testSettingsTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) resultMap.get("settings");
        assertNotNull(settings, "settings不应为空");
        
        // 验证通知设置
        assertTrue(settings.containsKey("notifications"), "应包含notifications设置");
        @SuppressWarnings("unchecked")
        Map<String, Object> notifications = (Map<String, Object>) settings.get("notifications");
        assertEquals(true, notifications.get("email"), "邮件通知设置应该正确");
        assertEquals(false, notifications.get("sms"), "短信通知设置应该正确");
        assertEquals(true, notifications.get("push"), "推送通知设置应该正确");
        
        // 验证隐私设置
        assertTrue(settings.containsKey("privacy"), "应包含privacy设置");
        @SuppressWarnings("unchecked")
        Map<String, Object> privacy = (Map<String, Object>) settings.get("privacy");
        assertEquals(false, privacy.get("profilePublic"), "个人资料公开设置应该正确");
        assertEquals(true, privacy.get("contactVisible"), "联系方式可见设置应该正确");
        assertEquals(true, privacy.get("activityTracking"), "活动跟踪设置应该正确");
    }
    
    @Test
    @DisplayName("测试账户信息转换")
    void testAccountTransformation() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> account = (Map<String, Object>) resultMap.get("account");
        assertNotNull(account, "account不应为空");
        
        // 验证账户状态和类型
        assertEquals("active", account.get("status"), "账户状态应该正确转换");
        assertEquals("premium", account.get("type"), "账户类型应该正确转换");
        
        // 验证默认账户设置
        assertEquals(true, account.get("verified"), "应该有默认的验证状态");
        assertEquals(false, account.get("twoFactorEnabled"), "应该有默认的双因子认证设置");
        
        // 验证时间戳
        assertTrue(account.containsKey("timestamps"), "应包含timestamps");
        @SuppressWarnings("unchecked")
        Map<String, Object> timestamps = (Map<String, Object>) account.get("timestamps");
        assertEquals("2024-01-15T10:30:00Z", timestamps.get("lastLogin"), "最后登录时间应该正确");
        assertEquals("2020-03-01T09:00:00Z", timestamps.get("createdAt"), "创建时间应该正确");
        assertEquals("2024-01-15T10:30:00Z", timestamps.get("updatedAt"), "更新时间应该正确");
    }
    
    @Test
    @DisplayName("测试空输入处理")
    void testNullInputHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            demo.transformData(null, transformSpec);
        }, "空输入应该抛出异常");
    }
    
    @Test
    @DisplayName("测试空转换规范处理")
    void testNullSpecHandling() {
        assertThrows(Exception.class, () -> {
            demo.transformData(inputData, null);
        }, "空转换规范应该抛出异常");
    }
    
    @Test
    @DisplayName("测试JSON结构完整性")
    void testJsonStructureIntegrity() throws Exception {
        Object result = demo.transformData(inputData, transformSpec);
        
        // 将结果转换为JSON字符串再解析回来，验证JSON结构的完整性
        String jsonString = JsonUtils.toJsonString(result);
        assertNotNull(jsonString, "JSON字符串不应为空");
        assertFalse(jsonString.trim().isEmpty(), "JSON字符串不应为空字符串");
        
        // 重新解析JSON
        Object reparsedResult = JsonUtils.jsonToObject(jsonString);
        assertNotNull(reparsedResult, "重新解析的结果不应为空");
        
        // 验证重新解析后的结构与原结果一致
        assertEquals(JsonUtils.toJsonString(result), JsonUtils.toJsonString(reparsedResult), 
            "重新解析后的结果应该与原结果一致");
    }
}
