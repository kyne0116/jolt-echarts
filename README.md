# Jolt JSON转换演示项目

这是一个完整的Jolt JSON转换演示项目，展示了如何使用Jolt库将扁平化的用户数据转换为层级化的复杂结构。

## 项目概述

本项目演示了以下核心功能：

1. **数据重组**: 将扁平化的用户信息重新组织为层级化结构
2. **字段重命名**: 将数据库风格的字段名转换为更友好的API格式
3. **结构重构**: 创建嵌套的JSON对象，提高数据的可读性和组织性
4. **默认值应用**: 为缺失的字段添加合理的默认值
5. **数据清理**: 移除空值和不需要的数据
6. **结果排序**: 对最终结果进行排序，确保输出的一致性

## 技术栈

- **Java**: 8+
- **Maven**: 3.6+
- **Jolt**: 0.1.7 (JSON转换库)
- **Jackson**: 2.13.4 (JSON处理)
- **JUnit 5**: 5.8.2 (单元测试)
- **SLF4J**: 1.7.36 (日志)

## 项目结构

```
jolt-demo/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/com/example/jolt/demo/
│   │   │   ├── JoltTransformationDemo.java   # 主演示类
│   │   │   └── UserDataTransformer.java      # 用户数据转换器
│   │   └── resources/
│   │       ├── input-data.json               # 输入数据样例
│   │       └── transformation-spec.json      # Jolt转换规范
│   └── test/
│       └── java/com/example/jolt/demo/
│           ├── JoltTransformationDemoTest.java    # 主演示类测试
│           └── UserDataTransformerTest.java       # 转换器测试
```

## 快速开始

### 1. 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本

### 2. 克隆和构建项目

```bash
# 进入项目目录
cd jolt-demo

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn package
```

### 3. 运行演示

```bash
# 方式1: 使用Maven运行
mvn exec:java -Dexec.mainClass="com.example.jolt.demo.JoltTransformationDemo"

# 方式2: 运行打包后的JAR
java -jar target/jolt-demo-1.0.0.jar
```

## 数据转换示例

### 输入数据（扁平化结构）

```json
{
  "user_id": "12345",
  "first_name": "张",
  "last_name": "三",
  "age": 28,
  "gender": "M",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "country": "中国",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "street": "建国路88号",
  "postal_code": "100020",
  "company_name": "科技有限公司",
  "department": "研发部",
  "position": "高级工程师",
  "salary": 25000,
  "hire_date": "2020-03-15",
  "language_chinese": true,
  "language_english": true,
  "language_japanese": false,
  "skill_java": 5,
  "skill_python": 4,
  "skill_javascript": 3,
  "skill_sql": 4,
  "hobby_reading": true,
  "hobby_gaming": true,
  "hobby_sports": false,
  "hobby_music": true,
  "notification_email": true,
  "notification_sms": false,
  "notification_push": true,
  "privacy_profile_public": false,
  "privacy_contact_visible": true,
  "privacy_activity_tracking": true,
  "account_status": "active",
  "account_type": "premium",
  "last_login": "2024-01-15T10:30:00Z",
  "created_at": "2020-03-01T09:00:00Z",
  "updated_at": "2024-01-15T10:30:00Z"
}
```

### 输出数据（层级化结构）

```json
{
  "account": {
    "status": "active",
    "timestamps": {
      "createdAt": "2020-03-01T09:00:00Z",
      "lastLogin": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    "twoFactorEnabled": false,
    "type": "premium",
    "verified": true
  },
  "employment": {
    "company": {
      "department": "研发部",
      "name": "科技有限公司"
    },
    "compensation": {
      "salary": 25000
    },
    "hireDate": "2020-03-15",
    "position": "高级工程师"
  },
  "preferences": {
    "hobbies": ["reading", "gaming", "music"],
    "language": "zh-CN",
    "theme": "light",
    "timezone": "Asia/Shanghai"
  },
  "profile": {
    "address": {
      "city": "北京市",
      "country": "中国",
      "district": "朝阳区",
      "postalCode": "100020",
      "province": "北京市",
      "street": "建国路88号"
    },
    "contactInfo": {
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "preferredContact": "email"
    },
    "personalInfo": {
      "age": 28,
      "gender": "M",
      "name": {
        "displayName": "",
        "firstName": "张",
        "lastName": "三"
      }
    },
    "userId": "12345"
  },
  "settings": {
    "notifications": {
      "email": true,
      "push": true,
      "sms": false
    },
    "privacy": {
      "activityTracking": true,
      "contactVisible": true,
      "profilePublic": false
    }
  },
  "skills": {
    "languages": [
      {
        "name": "Chinese",
        "proficiency": "fluent"
      },
      {
        "name": "English",
        "proficiency": "fluent"
      }
    ],
    "technical": {
      "java": {
        "category": "Programming Language",
        "level": 5,
        "name": "Java"
      },
      "javascript": {
        "category": "Programming Language",
        "level": 3,
        "name": "JavaScript"
      },
      "python": {
        "category": "Programming Language",
        "level": 4,
        "name": "Python"
      },
      "sql": {
        "category": "Database",
        "level": 4,
        "name": "SQL"
      }
    }
  }
}
```

## 转换规范说明

项目使用的Jolt转换规范包含以下步骤：

### 1. Shift转换 (数据移动)
- 将扁平化字段重新组织为层级结构
- 重命名字段以符合API标准
- 条件性地包含数据（如只包含掌握的语言和感兴趣的爱好）

### 2. Default转换 (默认值应用)
- 为技术技能添加名称和分类信息
- 为语言技能添加熟练度信息
- 添加用户偏好的默认设置
- 添加账户的默认配置

### 3. Remove转换 (数据清理)
- 移除空的数组元素
- 清理不需要的临时数据

### 4. Sort转换 (排序)
- 对最终结果进行字母排序
- 确保输出的一致性和可读性

## 核心类说明

### JoltTransformationDemo
主演示类，提供以下功能：
- 加载输入数据和转换规范
- 执行完整的转换流程
- 演示分步转换过程
- 展示转换统计信息

### UserDataTransformer
用户数据转换器，提供以下功能：
- 单个和批量数据转换
- 转换结果验证
- 转换过程分析
- 错误处理和日志记录

## 测试说明

项目包含全面的单元测试，覆盖以下方面：

1. **基本转换功能测试**
2. **数据结构验证测试**
3. **字段映射正确性测试**
4. **边界条件和错误处理测试**
5. **批量转换测试**
6. **数据验证和分析测试**

运行测试：
```bash
mvn test
```

查看测试报告：
```bash
mvn surefire-report:report
open target/site/surefire-report.html
```

## 扩展和定制

### 添加新的转换规则
1. 修改 `src/main/resources/transformation-spec.json`
2. 添加相应的测试用例
3. 更新文档

### 自定义转换器
1. 实现 `Transform` 或 `ContextualTransform` 接口
2. 在转换规范中使用完全限定类名引用
3. 添加到Chainr转换链中

### 性能优化
- 转换器实例可以重用（线程安全）
- 对于大量数据，考虑使用批量转换
- 监控内存使用情况，必要时调整JVM参数

## 常见问题

### Q: 如何处理缺失的字段？
A: 使用Default转换器添加默认值，或在Shift转换中使用条件映射。

### Q: 如何处理数组数据？
A: 使用数组索引语法 `[0]`, `[1]` 或通配符 `*` 进行处理。

### Q: 如何调试转换过程？
A: 使用分步转换功能，逐步查看每个转换器的输出结果。

### Q: 转换性能如何优化？
A: 重用转换器实例，避免重复创建Chainr对象，对大数据集使用流式处理。

## 许可证

本项目仅用于演示目的，基于Apache 2.0许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 创建Issue
- 提交Pull Request
- 发送邮件至项目维护者
