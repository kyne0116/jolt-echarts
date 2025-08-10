# 🚀 ECharts 两阶段转换系统

一个基于**零代码配置理念**的 ECharts 动态数据可视化平台，通过智能转换引擎和统一数据视图架构，实现从通用模板到最终图表配置的完全自动化转换流程。

## 📋 需求背景

### 🎯 **核心业务挑战**

在企业级数据可视化场景中，开发者和业务用户面临以下关键挑战：

1. **图表配置复杂性**：ECharts 要求固定的数据结构，但需要支持动态的系列数量（3条、5条、8条等）
2. **零代码需求**：避免"一个ECharts图表一个模板"的代码膨胀，脱离零代码平台的设计初衷  
3. **通用性与专用性平衡**：既不能大而全的一刀切模板，也不能与ECharts图表结构一一对应
4. **数据源统一性**：所有图表数据必须来源于统一的数据库视图，支持任意扩展字段
5. **映射关系配置化**：数据库视图字段与图表变量的映射关系需要零代码配置

### 🎯 **需求目标**

基于对ECharts图表数据结构的深入分析，本项目制定了以下核心目标：

#### **架构设计目标**

1. **ECharts结构兼容性**：满足ECharts官方要求 - 数据结构固定，但数据动态（支持任意系列数量）
2. **JOLT转换一致性**：JOLT spec文件与ECharts转换结构一一对应，实现结构对齐的同时使用变量占位
3. **通用模板分类化**：基于坐标系差异定义四大模板类型（CARTESIAN、PIE、RADAR、GAUGE），平衡通用性和专用性
4. **统一数据视图**：所有数据来源于一张模拟数据库视图，该视图可根据业务需求任意扩展字段
5. **映射关系零代码**：通过配置化映射实现数据库视图字段与图表占位符变量的关联

#### **业务价值目标**

- **真正的零代码体验**：用户只需配置映射关系，无需编写代码
- **完全的数据驱动**：图表系列数量完全由映射关系配置决定
- **统一的数据架构**：单一数据视图支持所有图表类型，简化数据管理
- **灵活的扩展能力**：新增图表类型只需扩展映射配置，无需修改代码

### 🔄 **两阶段转换架构设计**

基于我们的深入分析，两阶段转换架构完美解决了零代码配置的核心问题：

#### **第一阶段：语义转换（结构对齐）**
- **输入**：通用模板（包含占位符变量）
- **处理**：通过JOLT规范将通用模板转换为ECharts结构
- **输出**：ECharts结构配置（保持占位符）
- **价值**：实现通用模板与ECharts结构的完美对齐，同时保持变量占位

#### **第二阶段：数据填充（变量替换）**
- **输入**：ECharts结构配置（含占位符）
- **处理**：基于映射关系从统一数据视图查询数据，替换所有占位符
- **输出**：最终的ECharts配置
- **价值**：实现数据库视图字段与图表变量的映射关系建立

#### **架构优势**
1. **完全的结构与数据分离**：第一阶段专注结构转换，第二阶段专注数据映射
2. **零代码的映射配置**：图表系列数量（3条、5条、8条）完全由映射关系决定
3. **统一的数据视图架构**：所有图表类型共享单一数据源，简化数据管理
4. **可缓存的结构转换**：第一阶段结果可复用，提高性能

## 🏗️ 系统设计

### 📊 **核心架构：零代码配置驱动**

本项目的核心价值链如下，完美解决了零代码配置的关键挑战：

```
统一数据视图 → 映射关系配置 → 通用模板 → JOLT转换 → ECharts渲染
     ↑              ↑           ↑         ↑         ↑
   零代码数据    零代码配置   零代码模板  结构对齐   动态系列
```

### 🎯 **分类模板系统：平衡通用性与专用性**

基于对ECharts图表坐标系的深入分析，系统设计了四大模板类型，避免"一刀切"和"一图一模板"的极端：

#### **四大模板类型**

| 模板类型      | 适用图表               | 坐标系特征  | 数据结构特点 | 解决的问题 |
| ------------- | ---------------------- | ----------- | -------- | ------ |
| **CARTESIAN** | 折线图、柱状图、面积图 | xAxis/yAxis | 系列数组 | 支持动态系列数量 |
| **PIE**       | 饼图、环形图、玫瑰图   | 无/polar    | 对象数组 | 支持分类数据展示 |
| **RADAR**     | 雷达图、极坐标图       | radar       | 多维数组 | 支持多维度分析 |
| **GAUGE**     | 仪表盘、进度条         | 无          | 单值对象 | 支持指标监控 |

#### **分类策略的价值**

1. **避免一刀切**：不同坐标系有不同的数据结构需求
2. **避免碎片化**：同类图表100%兼容，新增图表成本极低  
3. **支持零代码**：用户只需选择模板类型，无需理解ECharts技术细节
4. **支持扩展性**：基于坐标系的分类具有很强的稳定性和扩展性

### 🔗 **统一数据视图：单一数据源架构**

#### **UniversalChartDataView 设计理念**

系统采用40字段的统一数据视图，实现"单一数据源，多图表类型"的架构目标：

```java
// 统一数据视图包含40个字段，覆盖所有图表类型需求
public class UniversalChartDataView {
    // 基础信息字段 (1-8): id, title, chartType, theme...
    // 时间维度字段 (9-16): date, dayName, month, year...  
    // 分类数据字段 (17-24): category, channelName, productName...
    // 数值字段 (25-32): value, conversionCount, clickCount...
    // 配置字段 (33-40): color, style, stackGroup...
}
```

#### **数据视图的核心价值**

1. **零代码数据管理**：单一视图支持所有图表类型，无需创建多表
2. **字段任意扩展**：新增图表类型只需扩展视图字段，无需修改代码
3. **数据一致性保证**：统一数据源确保不同图表的数据一致性
4. **查询性能优化**：单表查询比多表关联查询性能更优

### ⚙️ **映射关系配置：问题的根源与解决**

#### **当前问题分析**

通过深入代码分析，我们发现基础折线图只显示1条线，而堆叠折线图显示5条线的**根本原因**：

```java
// 问题代码：所有系列都映射到同一个字段 (MappingRelationshipService.java:219-228)
universalMappings.put("${series_1_name}", ...fieldName: "channel_name"...);
universalMappings.put("${series_2_name}", ...fieldName: "channel_name"...);  // 相同字段!
universalMappings.put("${series_3_name}", ...fieldName: "channel_name"...);  // 相同字段!
universalMappings.put("${series_4_name}", ...fieldName: "channel_name"...);  // 相同字段!
universalMappings.put("${series_5_name}", ...fieldName: "channel_name"...);  // 相同字段!

// 导致所有系列名称相同 → ECharts自动合并 → 基础折线图显示1条线
// 但堆叠图因为有stack属性，ECharts强制保留所有系列 → 显示5条线
```

#### **解决方案：差异化映射配置**

```java
// 正确的映射配置：让不同系列映射到不同字段
universalMappings.put("${series_1_name}", ...fieldName: "channel_name"...);
universalMappings.put("${series_2_name}", ...fieldName: "product_name"...);   // 不同字段
universalMappings.put("${series_3_name}", ...fieldName: "region"...);        // 不同字段  
universalMappings.put("${series_4_name}", ...fieldName: "department"...);    // 不同字段
universalMappings.put("${series_5_name}", ...fieldName: "product_type"...);  // 不同字段

// 结果：基础折线图和堆叠折线图都能正确显示5条不同名称的折线
```

#### **映射关系的核心价值**

映射关系配置是实现**零代码图表系列数量控制**的关键：

1. **系列数量完全可配置**：想要3条线？映射3个字段。想要8条线？映射8个字段
2. **数据驱动的图表渲染**：图表的最终效果完全由数据库视图和映射配置决定
3. **ECharts兼容性保证**：确保生成的ECharts配置符合官方结构要求
4. **零代码的扩展能力**：新增图表类型无需编写代码，只需配置映射关系

### 🎯 **核心技术突破总结**

通过这次深入分析，我们实现了以下关键技术突破：

#### **问题定位的精准性**
- **发现根因**：系列名称映射配置错误导致ECharts自动合并系列
- **分析机制**：ECharts对有无stack属性的同名系列处理方式不同
- **解决路径**：通过差异化字段映射实现系列名称区分

#### **架构设计的合理性**  
- **统一数据视图**：40字段覆盖所有图表类型，真正的单一数据源
- **分类模板系统**：基于坐标系的四大分类，平衡通用性与专用性
- **两阶段转换**：完美分离结构转换与数据填充，支持零代码配置

#### **零代码理念的实现**
- **配置驱动**：图表效果完全由映射配置决定，无需编写代码
- **数据驱动**：系列数量由数据库字段映射关系控制
- **模板驱动**：通用模板支持快速图表类型扩展

## 🛠️ 技术栈

### 后端技术栈

- **Java**: 17+ (Spring Boot 2.7.5)
- **Maven**: 3.9+ (项目构建管理)
- **Spring Boot**: 2.7.5 (Web 框架和依赖注入)
- **Jackson**: 2.13.4 (JSON 处理)
- **JUnit 5**: 5.8.2 (单元测试框架)

### 前端技术栈

- **Vue 3**: 3.4+ (渐进式 JavaScript 框架)
- **TypeScript**: 5.3+ (类型安全的 JavaScript)
- **Vite**: 5.0+ (现代化构建工具)
- **Ant Design Vue**: 4.0+ (企业级 UI 组件库)
- **ECharts**: 5.4+ (数据可视化图表库)
- **Pinia**: 2.1+ (Vue 状态管理)
- **Axios**: 1.6+ (HTTP 客户端)

## 🗄️ 数据库集成指南

### 📊 **当前实现方式**

系统目前使用**内存模拟数据**的方式实现，主要特点：

#### **模拟数据结构**

```java
// 虚拟数据库表结构
Map<String, List<Map<String, Object>>> virtualDatabase = Map.of(
    "chart_config", List.of(
        Map.of("id", "1", "title", "动态营销渠道分析", "theme", "default")
    ),
    "marketing_data", List.of(
        Map.of("day_name", "周一", "channel_name", "Email", "conversion_count", 120),
        Map.of("day_name", "周二", "channel_name", "Email", "conversion_count", 132)
    )
);
```

#### **模拟查询逻辑**

- **简单查询**：直接从内存 Map 中获取数据
- **聚合查询**：使用 Java Stream API 进行分组和聚合
- **过滤查询**：基于条件过滤数据集合
- **类型转换**：自动处理数据类型转换和格式化

### 🔄 **切换到真实数据库**

#### **需要改造的核心文件**

##### **1. MappingRelationshipService.java**

**位置**: `src/main/java/com/example/chart/service/MappingRelationshipService.java`
**改造内容**:

```java
// 当前模拟实现
public Map<String, Object> simulateDataQuery(String chartId, Set<String> placeholders) {
    // 内存数据查询逻辑
}

// 改造为真实数据库实现
@Autowired
private JdbcTemplate jdbcTemplate;

public Map<String, Object> executeDataQuery(String chartId, Set<String> placeholders) {
    Map<String, Object> results = new HashMap<>();

    for (String placeholder : placeholders) {
        FieldMapping mapping = getFieldMapping(chartId, placeholder);
        String sql = buildSqlQuery(mapping);
        Object result = jdbcTemplate.queryForObject(sql, mapping.getParameters());
        results.put(placeholder, result);
    }

    return results;
}
```

##### **2. Repository 接口和实现类**

**新增文件**:

- `src/main/java/com/example/chart/repository/ChartConfigRepository.java`
- `src/main/java/com/example/chart/repository/MappingRepository.java`
- `src/main/java/com/example/chart/repository/impl/ChartConfigRepositoryImpl.java`

```java
@Repository
public interface ChartConfigRepository {
    ChartConfig findByChartId(String chartId);
    List<ChartConfig> findAll();
    void save(ChartConfig chartConfig);
}

@Repository
public interface MappingRepository {
    List<FieldMapping> findByChartId(String chartId);
    void saveMappings(String chartId, List<FieldMapping> mappings);
}
```

##### **3. 数据模型类持久化注解**

**改造文件**:

- `src/main/java/com/example/chart/model/FieldMapping.java`
- `src/main/java/com/example/chart/model/ChartConfig.java`

```java
@Entity
@Table(name = "field_mappings")
public class FieldMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chart_id")
    private String chartId;

    @Column(name = "placeholder_name")
    private String placeholderName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "column_name")
    private String columnName;

    // getters and setters
}
```

#### **数据库表结构设计**

##### **核心表结构**

```sql
-- 图表配置表
CREATE TABLE chart_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chart_id VARCHAR(100) UNIQUE NOT NULL,
    chart_name VARCHAR(200) NOT NULL,
    chart_type VARCHAR(50) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 字段映射表
CREATE TABLE field_mappings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chart_id VARCHAR(100) NOT NULL,
    placeholder_name VARCHAR(100) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    aggregation_type VARCHAR(50),
    filter_conditions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chart_id) REFERENCES chart_configs(chart_id)
);

-- 业务数据表（示例）
CREATE TABLE marketing_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    day_name VARCHAR(20) NOT NULL,
    channel_name VARCHAR(50) NOT NULL,
    conversion_count INT NOT NULL,
    revenue DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **配置变更步骤**

##### **1. 添加数据库依赖**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

##### **2. 配置数据源**

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/echarts_system
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

##### **3. 数据迁移脚本**

```java
@Component
public class DataMigrationService {

    @PostConstruct
    public void migrateFromMemoryToDatabase() {
        // 将内存中的映射关系迁移到数据库
        Map<String, Map<String, Object>> memoryMappings = getMemoryMappings();

        for (String chartId : memoryMappings.keySet()) {
            List<FieldMapping> mappings = convertToFieldMappings(chartId, memoryMappings.get(chartId));
            mappingRepository.saveMappings(chartId, mappings);
        }
    }
}
```


## 🏗️ 项目结构

```
jolt-echarts/
├── 📁 src/main/java/com/example/chart/
│   ├── controller/
│   │   └── TwoStageTransformationController.java  # REST API控制器
│   ├── service/
│   │   ├── TwoStageTransformationService.java     # 两阶段转换核心服务
│   │   ├── SmartTransformationEngine.java         # 智能转换引擎
│   │   ├── CategoryTemplateFactory.java           # 分类模板工厂
│   │   ├── TemplateService.java                   # 模板管理服务
│   │   ├── PlaceholderManager.java                # 占位符管理器
│   │   └── MappingRelationshipService.java        # 映射关系管理服务
│   ├── model/
│   │   ├── TemplateType.java                      # 模板类型枚举
│   │   ├── UniversalTemplate.java                 # 通用模板模型
│   │   └── TransformationResult.java              # 转换结果模型
│   └── repository/
│       ├── InMemoryUniversalTemplateRepository.java # 内存模板仓库
│       └── model/UniversalTemplateEntity.java     # 模板实体
│
├── 📁 src/main/resources/
│   ├── echarts/                                   # ECharts示例配置
│   │   ├── 折线图/                                # 折线图示例
│   │   ├── 柱状图/                                # 柱状图示例
│   │   ├── 饼图/                                  # 饼图示例
│   │   ├── 雷达图/                                # 雷达图示例
│   │   └── 仪表盘/                                # 仪表盘示例
│   └── application.yml                            # Spring Boot配置
│
├── 📁 frontend/
│   ├── src/
│   │   ├── api/index.ts                           # API接口定义
│   │   ├── stores/transformation.ts               # 转换流程状态管理
│   │   ├── views/Transformation/index.vue         # 两阶段转换演示页面
│   │   └── types/index.ts                         # TypeScript类型定义
│   ├── package.json                               # 前端依赖配置
│   └── vite.config.ts                             # Vite构建配置
│
├── pom.xml                                        # Maven配置文件
└── README.md                                      # 项目文档
```

## 🚀 快速开始指南

### 📋 **环境要求**

#### 后端环境

- **Java**: 17+ (推荐使用 OpenJDK 17)
- **Maven**: 3.9+
- **内存**: 最小 2GB RAM

#### 前端环境

- **Node.js**: 18.0+ (推荐使用 LTS 版本)
- **npm**: 9.0+ 或 **yarn**: 1.22+

### 🛠️ **项目启动步骤**

#### 1. 克隆项目

```bash
git clone <repository-url>
cd jolt-echarts
```

#### 2. 启动后端服务

```bash
# 清理和编译
mvn clean compile

# 启动Spring Boot应用
mvn spring-boot:run -DskipTests

# 验证后端启动成功
curl http://localhost:8080/api/chart/two-stage/health
```

#### 3. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端将在 http://localhost:3000 启动
```

### 📊 **API 使用示例**

#### 获取分类模板

```bash
# 获取堆叠折线图的分类模板
curl -X GET "http://localhost:8080/api/chart/two-stage/template/stacked_line_chart"

# 响应示例
{
  "code": "OK",
  "data": {
    "template": {
      "chart": {
        "title": "${chart_title}",
        "type": "${chart_type}"
      },
      "coordinates": {
        "xAxis": {
          "type": "category",
          "data": "${categories}"
        }
      }
    },
    "templateType": "category"
  }
}
```

#### 执行第一阶段转换

```bash
curl -X POST "http://localhost:8080/api/chart/two-stage/stage1/stacked_line_chart" \
  -H "Content-Type: application/json" \
  -d '{
    "chart": {
      "title": "${chart_title}",
      "type": "${chart_type}"
    },
    "data": {
      "series": [
        {
          "name": "${series_1_name}",
          "data": "${series_1_data}"
        }
      ]
    }
  }'
```

#### 执行第二阶段转换

```bash
curl -X POST "http://localhost:8080/api/chart/two-stage/stage2/stacked_line_chart" \
  -H "Content-Type: application/json" \
  -d '{
    "title": {"text": "${chart_title}"},
    "xAxis": {"data": "${categories}"},
    "series": [{"name": "${series_1_name}", "data": "${series_1_data}"}]
  }'
```





| 模板类型      | 图表类型 | 图表名称   | 图表 ID                | ECharts 示例文件路径                                    | JOLT SPEC 文件路径                                          | 支持的占位符变量                                                                                                                                                                                                                                                                               | 数据库表映射关系                 | 开发状态  |
| ------------- | -------- | ---------- | ---------------------- | ------------------------------------------------------- | ----------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------- | --------- |
| **CARTESIAN** | 折线图   | 基础折线图 | `basic_line_chart`     | `src/main/resources/echarts/折线图/基础折线图.json`     | `src/main/resources/jolt-specs/line-chart-placeholder.json` | `${chart_title}`, `${chart_type}`, `${categories}`, `${series_1_name}` ~ `${series_5_name}`, `${series_1_data}` ~ `${series_5_data}`, `${series_type}`, `${stack_group}`, `${smooth_style}`, `${boundary_gap}`, `${legend_config}`, `${grid_config}`, `${tooltip_config}`, `${toolbox_config}` | `universal_chart_data_view`      | ✅ 已支持 |
| **CARTESIAN** | 折线图   | 平滑折线图 | `smooth_line_chart`    | `src/main/resources/echarts/折线图/基础平滑折线图.json` | `src/main/resources/jolt-specs/line-chart-placeholder.json` | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | ✅ 已支持 |
| **CARTESIAN** | 折线图   | 堆叠折线图 | `stacked_line_chart`   | `src/main/resources/echarts/折线图/折线图堆叠.json`     | `src/main/resources/jolt-specs/line-chart-stacked.json`     | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | ✅ 已支持 |
| **CARTESIAN** | 柱状图   | 基础柱状图 | `basic_bar_chart`      | `src/main/resources/echarts/柱状图/基础柱状图.json`     | `src/main/resources/jolt-specs/bar-chart-placeholder.json`  | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | ✅ 已支持 |
| **CARTESIAN** | 柱状图   | 堆叠柱状图 | `stacked_bar_chart`    | `src/main/resources/echarts/柱状图/堆叠柱状图.json`     | `src/main/resources/jolt-specs/bar-chart-placeholder.json`  | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | ✅ 已支持 |
| **CARTESIAN** | 面积图   | 基础面积图 | `basic_area_chart`     | _待创建_                                                | _待创建_                                                    | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | 📋 计划中 |
| **PIE**       | 饼图     | 基础饼图   | `basic_pie_chart`      | `src/main/resources/echarts/饼图/富文本标签.json`       | `src/main/resources/jolt-specs/pie-chart-placeholder.json`  | `${chart_title}`, `${chart_type}`, `${chart_theme}`, `${radius_config}`, `${center_config}`, `${pie_data}`, `${rose_type}`, `${legend_config}`, `${tooltip_config}`                                                                                                                            | `universal_chart_data_view`      | 🚧 开发中 |
| **PIE**       | 饼图     | 环形图     | `doughnut_chart`       | `src/main/resources/echarts/饼图/圆角环形图.json`       | `src/main/resources/jolt-specs/pie-chart-placeholder.json`  | 同上                                                                                                                                                                                                                                                                                           | `universal_chart_data_view`      | 🚧 开发中 |
| **PIE**       | 饼图     | 玫瑰图     | `rose_chart`           | _待创建_                                                | _待创建_                                                    | 同上                                                                                                                                                                                                                                                                                           | `chart_config`, `marketing_data` | � 计划中  |
| **RADAR**     | 雷达图   | 基础雷达图 | `basic_radar_chart`    | `src/main/resources/echarts/雷达图/基础雷达图.json`     | _待创建_                                                    | `${chart_title}`, `${chart_type}`, `${chart_theme}`, `${radar_indicators}`, `${radar_shape}`, `${radar_radius}`, `${radar_data}`, `${area_style}`, `${legend_config}`, `${tooltip_config}`                                                                                                     | `chart_config`, `marketing_data` | 🚧 开发中 |
| **RADAR**     | 雷达图   | 填充雷达图 | `filled_radar_chart`   | _待创建_                                                | _待创建_                                                    | 同上                                                                                                                                                                                                                                                                                           | `chart_config`, `radar_data`     | � 计划中  |
| **GAUGE**     | 仪表盘   | 基础仪表盘 | `basic_gauge_chart`    | `src/main/resources/echarts/仪表盘/基础仪表盘.json`     | _待创建_                                                    | `${chart_title}`, `${chart_type}`, `${chart_theme}`, `${min_value}`, `${max_value}`, `${gauge_data}`, `${detail_config}`, `${pointer_config}`, `${axis_line_config}`, `${progress_config}`, `${tooltip_config}`                                                                                | `chart_config`, `marketing_data` | 🚧 开发中 |
| **GAUGE**     | 仪表盘   | 进度仪表盘 | `progress_gauge_chart` | `src/main/resources/echarts/仪表盘/进度仪表盘.json`     | _待创建_                                                    | 同上                                                                                                                                                                                                                                                                                           | `chart_config`, `marketing_data` | 🚧 开发中 |
| **GAUGE**     | 仪表盘   | 等级仪表盘 | `grade_gauge_chart`    | `src/main/resources/echarts/仪表盘/等级仪表盘.json`     | _待创建_                                                    | 同上                                                                                                                                                                                                                                                                                           | `chart_config`, `marketing_data` | 🚧 开发中 |


### � **图表类型与文件对应关系详细说明**

#### **实现状态分类**

##### **✅ 已实现 (11 种) - 有 ECharts 示例文件和 JOLT SPEC 文件**

| 图表 ID                | 图表名称   | ECharts 文件                 | JOLT SPEC 文件                 |
| ---------------------- | ---------- | ---------------------------- | ------------------------------ |
| `stacked_line_chart`   | 堆叠折线图 | `折线图/折线图堆叠.json`     | `line-chart-stacked.json`      |
| `basic_bar_chart`      | 基础柱状图 | `柱状图/基础柱状图.json`     | `bar-chart-placeholder.json`   |
| `stacked_bar_chart`    | 堆叠柱状图 | `柱状图/堆叠柱状图.json`     | `bar-chart-placeholder.json`   |
| `basic_line_chart`     | 基础折线图 | `折线图/基础折线图.json`     | `line-chart-placeholder.json`  |
| `smooth_line_chart`    | 平滑折线图 | `折线图/基础平滑折线图.json` | `line-chart-placeholder.json`  |
| `basic_pie_chart`      | 基础饼图   | `饼图/富文本标签.json`       | `pie-chart-placeholder.json`   |
| `doughnut_chart`       | 环形图     | `饼图/圆角环形图.json`       | `pie-chart-placeholder.json`   |
| `basic_radar_chart`    | 基础雷达图 | `雷达图/基础雷达图.json`     | `radar-chart-placeholder.json` |
| `basic_gauge_chart`    | 基础仪表盘 | `仪表盘/基础仪表盘.json`     | `gauge-chart-placeholder.json` |
| `progress_gauge_chart` | 进度仪表盘 | `仪表盘/进度仪表盘.json`     | `gauge-chart-placeholder.json` |
| `grade_gauge_chart`    | 等级仪表盘 | `仪表盘/等级仪表盘.json`     | `gauge-chart-placeholder.json` |

##### **📋 计划中 (3 种) - 仅在代码中定义**

| 图表 ID              | 图表名称   | ECharts 文件 | JOLT SPEC 文件 |
| -------------------- | ---------- | ------------ | -------------- |
| `basic_area_chart`   | 基础面积图 | _待创建_     | _待创建_       |
| `rose_chart`         | 玫瑰图     | _待创建_     | _待创建_       |
| `filled_radar_chart` | 填充雷达图 | _待创建_     | _待创建_       |

##### **🔄 兼容版本 (1 种) - 向后兼容**

| 图表 ID     | 图表名称 | 说明                                 |
| ----------- | -------- | ------------------------------------ |
| `pie_chart` | 饼图     | 兼容旧版本，映射到 `basic_pie_chart` |

#### **文件数量不匹配说明**

- **ECharts 示例文件 (11 个)** vs **代码定义图表类型 (14 种)**：部分图表类型共享同一个示例文件
- **JOLT SPEC 文件 (6 个)** vs **代码定义图表类型 (14 种)**：多个图表类型共享同一个 JOLT 转换规范
- **前端支持 (6 种)** vs **代码定义 (14 种)**：前端界面展示所有已实现的图表类型





