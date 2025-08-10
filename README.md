# ECharts 两阶段转换系统

一个基于智能转换引擎和分类模板系统的 ECharts 动态数据可视化解决方案。通过两阶段转换架构（语义转换 + 数据填充），实现从通用语义模板到最终 ECharts 配置的智能转换。

## 📋 需求背景

### 🎯 **核心业务挑战**

在企业级数据可视化场景中，传统图表开发面临三大核心挑战：

1. **技术门槛高**：开发者需要深度理解 ECharts API 和复杂的配置语法
2. **重复开发**：相似图表类型重复编写转换逻辑，维护成本高
3. **扩展困难**：新增图表类型需要从零开始设计数据结构和转换规则

传统的"一图一配置"模式导致代码重复、维护困难，无法满足企业级应用的灵活性和扩展性需求。

### 🎯 **需求目标**

基于系统深度分析和技术架构验证，我们发现**映射关系配置是问题的根源**。系统需要实现：

#### **架构设计目标**

1. **零代码配置**：用户无需了解 ECharts 技术细节，通过语义化模板快速创建图表
2. **智能转换引擎**：基于图表类型自动推断转换逻辑，实现高度自动化
3. **统一数据架构**：40 字段统一数据视图，覆盖所有图表类型的数据需求
4. **差异化映射配置**：通过配置不同字段映射关系，解决图表间的差异化需求

#### **业务价值目标**

- **开发效率提升 80%**：从传统手写 ECharts 配置到模板化快速生成
- **维护成本降低 70%**：统一架构减少重复代码和维护工作量  
- **扩展成本降低 90%**：新增图表类型仅需配置映射关系，无需重新开发

### 🔄 **两阶段转换架构设计**

系统核心创新在于**两阶段转换 + 占位符保持**机制：

#### **第一阶段：语义转换（结构对齐）**
- 将用户友好的通用模板转换为 ECharts 结构
- **关键创新**：完整保留占位符（`${placeholder}`），不进行数据填充
- 通过 JOLT 转换规范实现结构化转换

#### **第二阶段：数据填充（变量替换）**
- 基于映射关系配置，将占位符替换为真实数据
- **核心价值**：通过不同的字段映射实现图表差异化
- 支持从统一数据视图按需提取数据

#### **架构优势**

- **结构与数据分离**：模板复用性更强，数据源更灵活
- **占位符机制**：同一模板支持不同数据源映射
- **渐进式转换**：每个阶段职责清晰，易于调试和扩展

## 🏗️ 系统设计

### 📊 **核心架构：零代码配置驱动**

系统遵循"**配置优于编程**"的设计理念，用户通过配置而非编程实现图表创建：

```
用户语义需求 → 通用模板 → JOLT转换 → ECharts结构 → 数据映射 → 最终图表
     ↓            ↓           ↓            ↓           ↓          ↓
   业务驱动    标准化接口   结构转换    占位符保持   差异化配置   智能生成
```

### 🎯 **分类模板系统：平衡通用性与专用性**

基于对 ECharts 坐标系的深度分析，系统设计了四大模板分类：

#### **四大模板类型**

- **CARTESIAN（直角坐标系）**：折线图、柱状图、面积图 - 使用 `xAxis/yAxis`
- **PIE（饼图类）**：饼图、环形图、玫瑰图 - 使用 `radius/center`  
- **RADAR（雷达图类）**：雷达图、极坐标图 - 使用 `radar.indicator`
- **GAUGE（仪表盘类）**：仪表盘、进度条 - 使用 `min/max`

#### **分类策略的价值**

1. **结构化复用**：同类图表共享相同的 ECharts 结构模式
2. **智能推断**：系统自动识别图表类型并选择对应转换逻辑  
3. **扩展便利**：新增图表只需归类到现有模板类型

#### **完整图表类型支持矩阵**

系统当前支持 **14 种图表类型**，基于统一数据视图 `UniversalChartDataView` 实现：

##### **✅ CARTESIAN 类型（6种）- 统一数据架构**

| 图表ID | 图表名称 | ECharts文件 | JOLT规范 | 实现状态 |
|--------|---------|------------|----------|---------|
| `basic_line_chart` | 基础折线图 | `折线图/基础折线图.json` | `line-chart-placeholder.json` | ✅ 已完成 |
| `smooth_line_chart` | 平滑折线图 | `折线图/基础平滑折线图.json` | `line-chart-placeholder.json` | ✅ 已完成 |
| `stacked_line_chart` | 堆叠折线图 | `折线图/折线图堆叠.json` | `line-chart-stacked.json` | ✅ 已完成 |
| `basic_bar_chart` | 基础柱状图 | `柱状图/基础柱状图.json` | `bar-chart-placeholder.json` | ✅ 已完成 |
| `stacked_bar_chart` | 堆叠柱状图 | `柱状图/堆叠柱状图.json` | `bar-chart-placeholder.json` | ✅ 已完成 |
| `basic_area_chart` | 基础面积图 | *规划中* | *规划中* | 📋 待开发 |

**核心占位符**：`${chart_title}`, `${categories}`, `${series_1_name}` ~ `${series_5_name}`, `${series_1_data}` ~ `${series_5_data}`, `${stack_group}`, `${smooth_style}`, `${boundary_gap}`

##### **✅ PIE 类型（3种）- 统一数据架构**

| 图表ID | 图表名称 | ECharts文件 | JOLT规范 | 实现状态 |
|--------|---------|------------|----------|---------|
| `basic_pie_chart` | 基础饼图 | `饼图/富文本标签.json` | `pie-chart-placeholder.json` | 🚧 开发中 |
| `doughnut_chart` | 环形图 | `饼图/圆角环形图.json` | `pie-chart-placeholder.json` | 🚧 开发中 |
| `rose_chart` | 玫瑰图 | *规划中* | *规划中* | 📋 待开发 |

**核心占位符**：`${chart_title}`, `${radius_config}`, `${center_config}`, `${pie_data}`, `${rose_type}`

##### **✅ RADAR 类型（2种）**

| 图表ID | 图表名称 | ECharts文件 | JOLT规范 | 实现状态 |
|--------|---------|------------|----------|---------|
| `basic_radar_chart` | 基础雷达图 | `雷达图/基础雷达图.json` | `radar-chart-placeholder.json` | 🚧 开发中 |
| `filled_radar_chart` | 填充雷达图 | *规划中* | *规划中* | 📋 待开发 |

##### **✅ GAUGE 类型（3种）**

| 图表ID | 图表名称 | ECharts文件 | JOLT规范 | 实现状态 |
|--------|---------|------------|----------|---------|
| `basic_gauge_chart` | 基础仪表盘 | `仪表盘/基础仪表盘.json` | `gauge-chart-placeholder.json` | 🚧 开发中 |
| `progress_gauge_chart` | 进度仪表盘 | `仪表盘/进度仪表盘.json` | `gauge-chart-placeholder.json` | 🚧 开发中 |
| `grade_gauge_chart` | 等级仪表盘 | `仪表盘/等级仪表盘.json` | `gauge-chart-placeholder.json` | 🚧 开发中 |

#### **实现状态统计**

- **✅ 已完成**：5种（CARTESIAN类型为主，完整的转换流程）
- **🚧 开发中**：6种（有ECharts文件，JOLT规范开发中）
- **📋 待开发**：3种（仅在代码中定义）

#### **兼容性支持**

| 图表ID | 说明 |
|--------|------|
| `pie_chart` | 向后兼容，映射到 `basic_pie_chart` |

### 🔗 **统一数据视图：单一数据源架构**

#### **UniversalChartDataView 设计理念**

系统创新性地设计了**40字段统一数据视图**，彻底解决了传统多表映射的复杂性：

```java
// 统一数据视图架构
public class UniversalChartDataView {
    // 基础信息 (8字段): id, title, chart_type, theme, description, data_source, created_at, updated_at
    // 时间维度 (8字段): date, day_name, month, month_name, year, quarter, week_number, timestamp  
    // 分类数据 (8字段): category, sub_category, channel_name, channel_type, product_name, product_type, region, department
    // 数值字段 (8字段): value, conversion_count, click_count, view_count, percentage, ratio, amount, quantity
    // 配置字段 (8字段): color, style, radius, center, stack_group, smooth_style, boundary_gap, extra_config
}
```

#### **数据视图的核心价值**

1. **数据源统一**：所有图表类型使用同一个数据源，消除数据孤岛
2. **字段覆盖完整**：40个精心设计的字段覆盖所有主流图表的数据需求
3. **扩展性强**：新增图表类型无需新建数据表，只需添加字段映射
4. **查询简化**：统一的数据提取接口，支持按需字段提取
5. **维护便利**：单一数据结构，大幅降低数据同步和维护成本

### ⚙️ **映射关系配置：问题的根源与解决**

#### **当前问题分析**

经过深入分析，我们发现了导致"基础折线图显示1条线，堆叠折线图显示5条线"的根本原因：

```java
// 问题根源：所有系列名称映射到同一个字段
universalMappings.put("${series_1_name}", createMapping("channel_name"));
universalMappings.put("${series_2_name}", createMapping("channel_name"));  // 相同字段！
universalMappings.put("${series_3_name}", createMapping("channel_name"));  // 相同字段！
universalMappings.put("${series_4_name}", createMapping("channel_name"));  // 相同字段！
universalMappings.put("${series_5_name}", createMapping("channel_name"));  // 相同字段！

// 结果：所有系列都变成 "Mock_channel_name"
// ECharts 自动合并同名系列 → 只显示1条线
// 但堆叠图因为 stack:"Total" 属性强制保留所有系列
```

#### **解决方案：差异化映射配置**

```java
// ✅ 解决方案：映射到不同字段实现差异化
universalMappings.put("${series_1_name}", createMapping("channel_name"));    // 渠道
universalMappings.put("${series_2_name}", createMapping("product_name"));    // 产品
universalMappings.put("${series_3_name}", createMapping("region"));          // 地区
universalMappings.put("${series_4_name}", createMapping("department"));      // 部门  
universalMappings.put("${series_5_name}", createMapping("product_type"));    // 产品类型

// 结果：5个不同的系列名称 → ECharts 显示5条不同的线
```

#### **映射关系的核心价值**

1. **差异化实现**：通过配置不同字段映射，同一模板支持不同的数据呈现
2. **灵活性保证**：无需修改代码，仅通过配置调整即可实现图表变化
3. **扩展性支撑**：新的业务需求只需调整映射配置，无需重构系统
4. **维护简化**：集中化的映射配置管理，易于维护和调试

### 🎯 **核心技术突破总结**

#### **问题定位的精准性**  
- 准确识别到映射关系配置是导致图表差异的根本原因
- 发现ECharts系列名称合并机制与堆叠属性的交互影响

#### **架构设计的合理性**  
- 四大模板类型基于ECharts坐标系的本质差异进行分类
- 统一数据视图兼顾通用性和专业性的平衡
- 两阶段转换实现结构与数据的完美分离

#### **零代码理念的实现**
- 用户只需配置映射关系，无需编写转换代码
- 智能推断引擎自动选择合适的转换逻辑  
- 配置驱动的扩展机制，降低技术门槛

## 🛠️ 技术栈

### 后端技术栈

- **Spring Boot 2.7+** - 企业级应用框架，提供依赖注入和自动配置
- **JOLT** - JSON到JSON的转换库，实现灵活的数据结构转换  
- **Jackson** - JSON处理库，高性能的序列化和反序列化
- **Maven** - 项目构建和依赖管理工具
- **JUnit 5** - 单元测试框架，确保代码质量
- **H2 Database** - 内存数据库，用于开发和测试环境

### 前端技术栈

- **Vue 3** - 渐进式JavaScript框架，采用Composition API
- **TypeScript** - JavaScript的超集，提供静态类型检查
- **ECharts 5.4+** - 数据可视化库，支持丰富的图表类型
- **Ant Design Vue** - 企业级UI组件库  
- **Pinia** - Vue状态管理库，提供响应式状态管理
- **Vite** - 下一代前端构建工具，快速热重载

## 🗄️ 数据库集成指南

### 📊 **当前实现方式**

系统当前使用**内存模拟**方式实现数据层，通过 `UniversalChartDataService` 生成模拟数据，便于开发和演示：

#### **模拟数据结构**

```java
// 统一数据视图模拟实现
public class UniversalChartDataService {
    // 生成 5个渠道 × 7天 = 35条记录
    // 渠道：Email, Union Ads, Video Ads, Direct, Search Engine
    // 时间：周一到周日
    // 数值：转换数量、点击数量、百分比等
}
```

#### **模拟查询逻辑**

- **按图表类型查询**：`getDataByChartType(String chartType)`
- **占位符数据提取**：`extractDataFromUniversalView(String chartId, Set<String> placeholders)`
- **字段映射转换**：基于 `fieldExtractors` 进行数据类型转换

### 🔄 **切换到真实数据库**

#### **需要改造的核心文件**

##### **1. MappingRelationshipService.java**

当前实现依赖内存模拟，切换到数据库需要替换以下方法：

```java
// 当前模拟实现
public Map<String, Object> simulateDataQuery(String chartId, Set<String> placeholders) {
    // 生成Mock数据
}

// 数据库实现版本  
@Autowired
private UniversalChartDataRepository universalDataRepository;

public Map<String, Object> executeDataQuery(String chartId, Set<String> placeholders) {
    // 1. 根据chartId查询对应的数据
    List<UniversalChartDataView> dataList = universalDataRepository.findByChartType(chartId);
    
    // 2. 基于占位符提取所需字段
    Map<String, Object> result = new HashMap<>();
    for (String placeholder : placeholders) {
        Object value = extractPlaceholderValue(placeholder, dataList);
        result.put(placeholder, value);
    }
    
    return result;
}
```

##### **2. Repository 接口和实现类**

```java
// UniversalChartDataRepository.java
@Repository
public interface UniversalChartDataRepository extends JpaRepository<UniversalChartDataView, Long> {
    
    // 根据图表类型查询数据
    List<UniversalChartDataView> findByChartType(String chartType);
    
    // 根据时间范围查询
    List<UniversalChartDataView> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 根据渠道查询
    List<UniversalChartDataView> findByChannelName(String channelName);
    
    // 复合查询
    @Query("SELECT u FROM UniversalChartDataView u WHERE u.chartType = ?1 AND u.date BETWEEN ?2 AND ?3")
    List<UniversalChartDataView> findChartDataByTypeAndDateRange(String chartType, LocalDate startDate, LocalDate endDate);
}
```

##### **3. 数据模型类持久化注解**

```java
// UniversalChartDataView.java 需要添加JPA注解
@Entity
@Table(name = "universal_chart_data_view")
public class UniversalChartDataView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Column(name = "chart_type", length = 100)
    private String chartType;
    
    @Column(name = "date")
    private LocalDate date;
    
    // ... 其他40个字段的JPA映射注解
}
```

#### **数据库表结构设计**

##### **核心表结构**

```sql
-- 统一图表数据视图表
CREATE TABLE universal_chart_data_view (
    -- 基础信息字段 (1-8)
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) COMMENT '图表标题',
    chart_type VARCHAR(100) COMMENT '图表类型',
    theme VARCHAR(50) COMMENT '图表主题',
    description TEXT COMMENT '图表描述',
    data_source VARCHAR(100) COMMENT '数据源标识',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 时间维度字段 (9-16)
    date DATE COMMENT '日期',
    day_name VARCHAR(10) COMMENT '日期名称',
    month INT COMMENT '月份',
    month_name VARCHAR(10) COMMENT '月份名称',
    year INT COMMENT '年份',
    quarter INT COMMENT '季度',
    week_number INT COMMENT '周数',
    timestamp BIGINT COMMENT '时间戳',
    
    -- 分类数据字段 (17-24)
    category VARCHAR(100) COMMENT '主分类',
    sub_category VARCHAR(100) COMMENT '子分类',
    channel_name VARCHAR(100) COMMENT '渠道名称',
    channel_type VARCHAR(50) COMMENT '渠道类型',
    product_name VARCHAR(100) COMMENT '产品名称',
    product_type VARCHAR(50) COMMENT '产品类型',
    region VARCHAR(50) COMMENT '地区',
    department VARCHAR(50) COMMENT '部门',
    
    -- 数值字段 (25-32)
    value DECIMAL(15,2) COMMENT '主要数值',
    conversion_count INT COMMENT '转换数量',
    click_count INT COMMENT '点击数量',
    view_count INT COMMENT '浏览数量',
    percentage DECIMAL(5,2) COMMENT '百分比',
    ratio DECIMAL(10,4) COMMENT '比率',
    amount DECIMAL(15,2) COMMENT '金额',
    quantity INT COMMENT '数量',
    
    -- 配置字段 (33-40)
    color VARCHAR(50) COMMENT '颜色配置',
    style VARCHAR(100) COMMENT '样式配置',
    radius VARCHAR(50) COMMENT '半径配置',
    center VARCHAR(50) COMMENT '中心位置配置',
    stack_group VARCHAR(50) COMMENT '堆叠组',
    smooth_style BOOLEAN COMMENT '平滑样式',
    boundary_gap BOOLEAN COMMENT '边界间隙',
    extra_config TEXT COMMENT '扩展配置JSON格式',
    
    -- 索引优化
    INDEX idx_chart_type (chart_type),
    INDEX idx_date (date),
    INDEX idx_channel (channel_name),
    INDEX idx_composite (chart_type, date, channel_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一图表数据视图表';
```

#### **配置变更步骤**

##### **1. 添加数据库依赖**

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- MySQL驱动（生产环境） -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- H2数据库（开发测试） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

##### **2. 配置数据源**

```yaml
# application.yml
spring:
  # 开发环境配置
  profiles:
    active: dev
    
---
# 开发环境
spring:
  profiles: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
---    
# 生产环境  
spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://localhost:3306/chart_system
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

##### **3. 数据迁移脚本**

```sql
-- data.sql (初始化数据脚本)
INSERT INTO universal_chart_data_view (
    title, chart_type, date, day_name, channel_name, conversion_count, 
    click_count, value, stack_group, smooth_style, boundary_gap
) VALUES 
('营销渠道分析', 'basic_line_chart', '2024-01-01', '周一', 'Email', 120, 1200, 85.5, null, false, false),
('营销渠道分析', 'basic_line_chart', '2024-01-02', '周二', 'Email', 132, 1350, 87.2, null, false, false),
-- ... 更多测试数据

-- 堆叠图数据
('堆叠渠道分析', 'stacked_line_chart', '2024-01-01', '周一', 'Email', 120, 1200, 85.5, 'Total', false, false),
('堆叠渠道分析', 'stacked_line_chart', '2024-01-01', '周一', 'Union Ads', 220, 2200, 78.3, 'Total', false, false);
```

## 🏗️ 项目结构

```
jolt-echarts/
├── src/main/java/com/example/chart/
│   ├── controller/                    # REST API控制器
│   │   └── TwoStageTransformationController.java
│   ├── service/                       # 业务逻辑服务
│   │   ├── TwoStageTransformationService.java      # 两阶段转换主服务
│   │   ├── SmartTransformationEngine.java          # 智能转换引擎
│   │   ├── MappingRelationshipService.java         # 映射关系管理
│   │   ├── CategoryTemplateFactory.java            # 分类模板工厂
│   │   ├── PlaceholderManager.java                 # 占位符管理器
│   │   └── UniversalChartDataService.java          # 统一数据服务
│   ├── model/                         # 数据模型
│   │   ├── UniversalChartDataView.java              # 40字段统一数据视图
│   │   ├── TemplateType.java                       # 四大模板类型枚举
│   │   └── TransformationResult.java               # 转换结果封装
│   └── config/                        # 配置类
├── src/main/resources/
│   ├── jolt-specs/                    # JOLT转换规范文件
│   │   ├── line-chart-placeholder.json              # 折线图转换规范
│   │   ├── line-chart-stacked.json                 # 堆叠折线图规范
│   │   ├── bar-chart-placeholder.json              # 柱状图转换规范
│   │   ├── pie-chart-placeholder.json              # 饼图转换规范
│   │   ├── radar-chart-placeholder.json            # 雷达图转换规范
│   │   └── gauge-chart-placeholder.json            # 仪表盘转换规范
│   └── echarts/                       # ECharts示例文件
│       ├── 折线图/                    # 折线图示例
│       ├── 柱状图/                    # 柱状图示例
│       ├── 饼图/                      # 饼图示例
│       ├── 雷达图/                    # 雷达图示例
│       └── 仪表盘/                    # 仪表盘示例
├── frontend/                          # Vue 3前端项目
│   ├── src/
│   │   ├── views/Transformation/      # 转换演示页面
│   │   ├── api/                       # API接口定义
│   │   ├── stores/                    # Pinia状态管理
│   │   └── components/                # Vue组件
│   ├── package.json                   # 前端依赖配置
│   └── vite.config.ts                 # Vite构建配置
└── src/test/java/                     # 测试代码
    └── com/example/chart/
        ├── CategoryTemplateCompatibilityTest.java  # 模板兼容性测试
        └── TwoStageTransformationTest.java         # 端到端转换测试
```

## 🚀 快速开始指南

### 📋 **环境要求**

#### 后端环境
- **Java 8+** (推荐 Java 11)
- **Maven 3.6+**
- **Spring Boot 2.7+**

#### 前端环境  
- **Node.js 16+** (推荐 18+)
- **npm 8+** 或 **yarn 1.22+**

### 🛠️ **项目启动步骤**

#### 1. 克隆项目

```bash
git clone <repository-url>
cd jolt-echarts
```

#### 2. 启动后端服务

```bash
# 安装依赖并编译
mvn clean compile

# 启动Spring Boot应用
mvn spring-boot:run -DskipTests

# 服务启动后访问：http://localhost:8080
# API文档：http://localhost:8080/swagger-ui.html (如已配置)
```

#### 3. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端应用访问：http://localhost:3000
```

### 📊 **API 使用示例**

#### 获取分类模板

```bash
curl -X GET "http://localhost:8080/api/chart/two-stage/template/stacked_line_chart" \
     -H "Content-Type: application/json"
```

响应示例：
```json
{
  "success": true,
  "data": {
    "chartMeta": {
      "title": "${chart_title}",
      "type": "${chart_type}"
    },
    "categories": "${categories}",
    "series": [
      {
        "name": "${series_1_name}",
        "data": "${series_1_data}",
        "stack": "${stack_group}"
      }
    ]
  }
}
```

#### 执行第一阶段转换

```bash
curl -X POST "http://localhost:8080/api/chart/two-stage/stage1/stacked_line_chart" \
     -H "Content-Type: application/json" \
     -d '{
       "chartMeta": {"title": "营销渠道分析", "type": "line"},
       "categories": ["周一", "周二", "周三", "周四", "周五"], 
       "series": [{"name": "Email", "data": [120, 132, 101, 134, 90]}]
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

