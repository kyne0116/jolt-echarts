# 通用 JSON 模板设计方案

## 核心设计理念

### 1. 真正的通用模板结构

```json
{
  "chart": {
    "title": "${chart_title}",
    "type": "${chart_type}",
    "theme": "${chart_theme}"
  },
  "data": {
    "categories": "${categories}",
    "series": [
      {
        "name": "${series_1_name}",
        "values": "${series_1_data}",
        "style": "${series_1_style}"
      },
      {
        "name": "${series_2_name}",
        "values": "${series_2_data}",
        "style": "${series_2_style}"
      }
    ]
  },
  "layout": {
    "legend": "${legend_config}",
    "axis": "${axis_config}",
    "grid": "${grid_config}"
  },
  "interaction": {
    "tooltip": "${tooltip_config}",
    "zoom": "${zoom_config}",
    "brush": "${brush_config}"
  }
}
```

### 2. 设计原则

- **语义化结构**: 按功能分组，用户容易理解
- **扁平化映射**: 每个占位符对应一个数据库字段或计算结果
- **类型无关**: 同一结构支持所有图表类型
- **渐进增强**: 基础功能简单，高级功能可选

### 3. 占位符分类体系

```
数据类占位符:
- ${categories} - 分类轴数据
- ${series_N_name} - 系列名称
- ${series_N_data} - 系列数值
- ${series_N_style} - 系列样式

配置类占位符:
- ${chart_title} - 图表标题
- ${chart_type} - 图表类型
- ${legend_config} - 图例配置
- ${axis_config} - 坐标轴配置

交互类占位符:
- ${tooltip_config} - 提示框配置
- ${zoom_config} - 缩放配置
```

## 两阶段转换的重新定义

### 第一阶段：语义化转换

**目标**: 将通用语义结构转换为 ECharts 标准结构
**价值**:

- 用户只需理解业务语义，不需要了解 ECharts 技术细节
- 统一的映射配置界面
- 类型安全的数据验证

### 第二阶段：数据填充

**目标**: 将占位符替换为真实数据
**价值**:

- 数据库查询优化
- 数据转换和聚合
- 缓存和性能优化

## 映射配置界面设计

### 可视化映射配置

```
[通用模板字段] ←→ [数据库表.字段]
     ↓                    ↓
${chart_title}  ←→  chart_config.title
${categories}   ←→  sales_data.month
${series_1_name} ←→ "销售额" (常量)
${series_1_data} ←→ sales_data.amount (聚合:SUM, 分组:month)
```

### 配置数据结构

```json
{
  "mappings": [
    {
      "placeholder": "${chart_title}",
      "type": "database_field",
      "source": {
        "table": "chart_config",
        "column": "title"
      }
    },
    {
      "placeholder": "${series_1_name}",
      "type": "constant",
      "value": "销售额"
    },
    {
      "placeholder": "${series_1_data}",
      "type": "aggregated_query",
      "source": {
        "table": "sales_data",
        "column": "amount",
        "aggregation": "SUM",
        "groupBy": ["month"],
        "filters": [{ "field": "year", "operator": "=", "value": "2024" }]
      }
    }
  ]
}
```

## 实现路径和技术方案

### 阶段 1：核心架构重构（已完成）

1. ✅ 创建 UniversalTemplate 模型类
2. ✅ 实现 SmartTransformationEngine 智能转换引擎
3. ✅ 重构 TemplateService 使用通用模板
4. ✅ 更新 TwoStageTransformationService 使用智能引擎

### 阶段 2：前端界面优化

1. 更新前端显示通用模板结构
2. 优化映射配置界面
3. 添加图表类型选择逻辑

### 阶段 3：映射系统增强

1. 实现可视化映射配置界面
2. 支持复杂查询和聚合
3. 添加数据验证和预览

### 阶段 4：性能和扩展性优化

1. 缓存机制
2. 异步处理
3. 插件化架构

## 核心价值体现

### 第一阶段转换价值

- **语义化抽象**: 用户只需理解业务概念，不需要了解 ECharts 技术细节
- **类型安全**: 智能引擎根据图表类型自动适配最佳结构
- **扩展性**: 新增图表类型只需在引擎中添加转换逻辑

### 第二阶段转换价值

- **数据优化**: 智能查询优化和缓存
- **业务逻辑**: 复杂的数据聚合和计算
- **性能提升**: 批量处理和异步执行

## 用户体验改进

### 简化的工作流程

1. 用户选择图表类型
2. 系统显示通用语义模板
3. 用户建立简单的一对一映射关系
4. 系统自动生成最优的 ECharts 配置

### 技术细节隐藏

- 用户不需要了解 ECharts API
- 不需要理解复杂的 JSON 结构
- 映射关系直观易懂
