# jolt-echarts 技术规格书（Architecture & Implementation Spec)

## 目录
- 概要
- 一、项目背景与核心需求
  - 1.1 业务目标
  - 1.2 技术约束（不可变前提）
  - 1.3 需解决的核心问题
- 二、架构设计决策
  - 2.1 方案对比：A（单一通用模板）vs B（按 chartId 专用模板）
  - 2.2 选择结论与理由
  - 2.3 一对一绑定：chartId → 专用模板 + JOLT spec + 占位符目录
  - 2.4 占位符映射管理整体方案
- 三、具体实现方案
  - 3.1 总体架构与流程
  - 3.2 后端设计（服务分层 / 数据模型 / API / 关键算法）
  - 3.3 前端设计（UI/UX / 交互流程 / 状态管理）
  - 3.4 数据库模型设计
- 四、技术规范
  - 4.1 代码与配置约定
  - 4.2 校验规则
  - 4.3 错误处理与标准响应
  - 4.4 版本管理与激活策略
  - 4.5 安全、日志与监控
  - 4.6 测试策略
  - 4.7 实施步骤与配置示例
- 五、实施优先级与开发路线图

---

## 概要
本规范文件面向 jolt-echarts 项目，定义“ECharts 动态数据流架构”的端到端最优实现：采用两阶段转换（JOLT 结构转换 + 数据回填）与按图表类型（chartId）拆分的专用模板策略。文档覆盖架构决策、服务与数据模型、API、前端 UI/UX、校验/错误/版本策略与路线图，可作为开发团队的权威技术标准。

---

## 一、项目背景与核心需求

### 1.1 业务目标
- 将扁平数据（数据库）转换为 ECharts 可渲染配置（结构+数据）。
- 支持多图表类型，新增图表边际成本低。
- 通过可视化界面完成占位符与数据列映射、聚合/分组/过滤/透视，并可 Dry-run 预览。

### 1.2 技术约束（不可变前提）
- ECharts 目标结构与数据格式固定。
- 必须使用 JOLT 完成静态结构转换（阶段一）。
- 动态数据以占位符（${var}）表达，于阶段二回填替换。

### 1.3 需解决的核心问题
- 扁平数据如何生产出 ECharts 所需数组/对象（legend/xAxis/series 等差异大）。
- 结构与数据解耦：JOLT 只做结构，数据经占位符与映射回填。
- 模板/spec/映射一致性保障、可维护性与扩展性。

---

## 二、架构设计决策

### 2.1 方案对比：A vs B
- A 单一通用模板（包含全部占位符）
  - 优：看似“一处维护”。
  - 劣：模板臃肿、JOLT 条件复杂、映射集合过大、变更影响面广、回归成本高。
- B 按 chartId 专用模板 + 对应 JOLT spec
  - 优：边界清晰、集合小而稳定、回归范围可控、扩展友好。
  - 劣：文件数量增加（可用模板继承/生成器缓解）。

### 2.2 选择结论与理由
- 选择方案 B。其维护性、可行性、扩展性显著优于 A，天然契合“两阶段 + 配置驱动”。

### 2.3 一对一绑定（按版本）
- 定义：Template[vT] + Spec[vS] + PlaceholderCatalog[vC] 作为 chartId 的强绑定“契约集合”。
- 对外：ChartRegistry 维护激活版本（active 组合）。
- Catalog 来源：对“专用模板 + JOLT spec”执行阶段一产出后，从输出结构中自动抽取占位符（含 JSONPath 与类型）。

### 2.4 占位符映射管理整体方案
- 以 Catalog 为单一事实来源（SSOT）驱动 UI 与校验。
- 用户针对每个占位符配置：数据列映射 + 聚合/分组/过滤 + 透视（seriesKey/categoryKey）。
- 映射版本化，绑定 Template/Spec 版本；保存前执行覆盖率/类型/长度校验与 Dry-run。

---

## 三、具体实现方案

### 3.1 总体架构与流程
- 阶段一：专用模板（含占位符）→ JOLT（Spec）→ ECharts 结构（占位符保留）。
- 阶段二：依据映射执行查询编排（聚合/分组/透视）→ 生成占位符键值对 → 占位符回填 → 最终 ECharts 配置。
- 增强：ChartRegistry、PlaceholderCatalogService、QueryOrchestrator、Mapping validate & Dry-run。

### 3.2 后端设计

#### 3.2.1 服务分层与职责
- controller
  - ChartRegistryController（激活/版本查询）
  - TemplateController（模板 CRUD/版本）
  - JoltSpecController（spec 管理/版本）
  - PlaceholderCatalogController（目录生成/查询）
  - MappingController（映射 CRUD/校验/Dry-run）
  - TwoStageTransformationController（阶段一/二，升级 DTO）
  - DataSourceController（schema/表预览/查询预览）
- service
  - ChartRegistryService：维护 chartId 激活组合与历史版本
  - TemplateService：专用模板版本化 CRUD
  - JoltSpecRegistryService：spec 元数据/路径/版本
  - PlaceholderCatalogService：阶段一运行 + 输出扫描 → 目录
  - MappingService：映射 CRUD、validate、dry-run
  - QueryOrchestrator：聚合/分组/透视标准化，输出占位符值
  - PlaceholderManager：占位符提取/替换，类型保持
  - TwoStageTransformationService：编排阶段一/二
- repository
  - TemplateRepository / SpecRepository / CatalogRepository / MappingRepository / RegistryRepository（先内存、后 DB）

#### 3.2.2 数据模型（JSON Schema 摘要）
- ApiResponse（统一响应）
```json
{ "type": "object", "properties": { "code": {"type":"string"}, "message": {"type":"string"}, "data": {}, "traceId": {"type":"string"} }, "required": ["code"] }
```
- PlaceholderCatalog
```json
{ "type":"object","properties":{ "chartId":{"type":"string"},"templateVersion":{"type":"string"},"specVersion":{"type":"string"},"generatedAt":{"type":"integer"},"placeholders":{"type":"array","items":{ "type":"object","properties":{ "name":{"type":"string"},"variable":{"type":"string"},"type":{"type":"string","enum":["string","number","array","object"]},"required":{"type":"boolean"},"group":{"type":"string"},"targetPath":{"type":"string"},"description":{"type":"string"},"example":{} },"required":["name","variable","type","required","targetPath"] }},"checksum":{"type":"string"} },"required":["chartId","templateVersion","specVersion","placeholders"] }
```
- Mapping
```json
{ "type":"object","properties":{ "chartId":{"type":"string"},"mappingVersion":{"type":"string"},"templateVersion":{"type":"string"},"specVersion":{"type":"string"},"status":{"type":"string","enum":["active","draft","deprecated"]},"updatedBy":{"type":"string"},"updatedAt":{"type":"integer"},"items":{"type":"array","items":{ "type":"object","properties":{ "placeholder":{"type":"string"},"dataType":{"type":"string","enum":["string","number","array","object"]},"source":{"type":"object","properties":{ "table":{"type":"string"},"column":{"type":"string"} },"required":["table","column"]},"transform":{"type":"object","properties":{ "aggregation":{"type":"string","enum":["sum","avg","min","max","count",null]},"groupBy":{"type":"array","items":{"type":"string"}},"filters":{"type":"array","items":{ "type":"object","properties":{ "field":{"type":"string"},"op":{"type":"string","enum":["eq","neq","gt","gte","lt","lte","in","notIn","like"]},"value":{} },"required":["field","op","value"] }},"orderBy":{"type":"array","items":{ "type":"object","properties":{ "field":{"type":"string"},"dir":{"type":"string","enum":["asc","desc"]} },"required":["field","dir"] }},"limit":{"type":["integer","null"]},"nullHandling":{"type":"string","enum":["skip","zero","fill"]} }},"pivot":{"type":"object","properties":{ "categoryKey":{"type":["string","null"]},"seriesKey":{"type":["string","null"]} }},"notes":{"type":"string"} },"required":["placeholder","dataType","source"] }} },"required":["chartId","items","templateVersion","specVersion"] }
```
- ChartRegistry
```json
{ "type":"object","properties":{ "chartId":{"type":"string"},"active":{"type":"object","properties":{ "templateVersion":{"type":"string"},"specVersion":{"type":"string"},"catalogVersion":{"type":"string"},"mappingVersion":{"type":"string"} }},"versions":{"type":"array","items":{"type":"object"}} },"required":["chartId"] }
```

#### 3.2.3 API 设计（接口与示例）
- Chart Registry
  - GET `/api/charts`
  - GET `/api/charts/{chartId}/registry`
  - PUT `/api/charts/{chartId}/registry/activate`
    - Body: `{ templateVersion, specVersion, catalogVersion, mappingVersion }`
- Templates
  - GET `/api/charts/{chartId}/templates[?version=]`
  - PUT `/api/charts/{chartId}/templates`
    - Body: `{ version, name, description, templateJson }`
- Specs
  - GET `/api/charts/{chartId}/specs[?version=]`
  - PUT `/api/charts/{chartId}/specs`
    - Body: `{ version, name, description, specFilePath|specJson }`
- Placeholder Catalog
  - GET `/api/charts/{chartId}/placeholder-catalog[?version=]`
  - POST `/api/charts/{chartId}/placeholder-catalog/generate`
    - Body: `{ templateVersion, specVersion }`
- Mappings
  - GET `/api/charts/{chartId}/mappings[?version=]`
  - PUT `/api/charts/{chartId}/mappings`（创建或新版本）
    - Body: `Mapping`
  - POST `/api/charts/{chartId}/mappings/validate`
    - Body: `{ mapping, templateVersion, specVersion, catalogVersion }`
  - POST `/api/charts/{chartId}/dry-run`
    - Body: `{ mappingVersion? | mapping?, templateVersion?, specVersion?, params? }`
- Two-stage（升级 DTO）
  - POST `/api/chart/two-stage/stage1`
    - Body: `{ chartId, templateVersion? }`
  - POST `/api/chart/two-stage/stage2`
    - Body: `{ chartId, echartsTemplate, mappingVersion? | mapping? }`
- Data Source
  - GET `/api/datasource/schema`
  - GET `/api/datasource/preview?table=...&limit=20`
  - POST `/api/datasource/query/preview`

#### 3.2.4 关键算法与业务逻辑
- PlaceholderCatalogService（生成）
  1) 阶段一：模板 → JOLT → 结构（占位符保留）
  2) 扫描输出：提取 ${...}，记录 JSONPath 与类型，推断 group
  3) 生成目录：placeholders[] + checksum；与上版差异校验
- MappingService.validate
  - 覆盖率（required=100%）、类型匹配、数组长度与 xAxis.data 对齐、透视一致性
- MappingService.dryRun
  - QueryOrchestrator：聚合/分组/透视 → Map<placeholder,value>
  - PlaceholderManager：替换占位符，输出 finalEChartsConfig

---

### 3.3 前端设计（UI/UX）

#### 3.3.1 三栏布局
- 顶栏：选择 chartId + 显示激活版本；按钮：生成目录、校验、Dry-run、保存、激活
- 左栏（目录树）：变量名、类型图标、必填标识、JSONPath、映射状态
- 中栏（映射编辑器）：表/列选择、聚合/分组/过滤/排序/limit/null、pivot 配置；即时查询预览
- 右栏（数据源浏览）：schema 树与搜索，支持拖拽至表单
- 底部：Dry-run 后 ECharts 预览

#### 3.3.2 交互流程与状态管理（Pinia）
- chartStore：chartId、激活版本、目录与映射状态
- mappingStore：映射草稿、校验结果、覆盖率与错误清单
- datasourceStore：schema/预览/查询预览
- transformationStore：阶段一/二运行与预览

#### 3.3.3 关键组件
- PlaceholderTree / MappingForm / QueryPreview / CoverageProgress / EChartsPreview / VersionSelector / VersionDiff

---

### 3.4 数据库模型设计（建议）
- charts(chart_id PK, name, description, created_at, updated_at)
- templates(id PK, chart_id FK, version UK, name, description, content_json, created_by, created_at)
- specs(id PK, chart_id FK, version UK, name, description, content_path|content_json, created_by, created_at)
- placeholder_catalogs(id PK, chart_id FK, version UK, template_version, spec_version, content_json, checksum, generated_at)
- mappings(id PK, chart_id FK, version UK, template_version, spec_version, status, content_json, updated_by, updated_at)
- chart_registry(chart_id PK, active_template_version, active_spec_version, active_catalog_version, active_mapping_version, updated_at)
- audit_logs(id PK, entity_type, entity_id, action, before_json, after_json, operator, created_at)

---

## 四、技术规范

### 4.1 代码与配置约定
- Controller 仅编排与入参校验，核心逻辑在 Service；DTO/VO 替代 Map。
- 统一日志（SLF4J），关键链路打点 traceId。
- 配置化注册表，禁止硬编码 chartId→specFile。

示例（application.yml 摘要）：
```yaml
charts:
  stacked_line_chart:
    specs:
      - version: "1.0.3"
        path: "classpath:/jolt-specs/line-chart-placeholder.json"
    templates:
      - version: "1.1.0"
        path: "db:templates/stacked_line_chart/1.1.0"
```

### 4.2 校验规则
- 目录一致性：阶段一输出占位符集合 == 目录集合（required 必须存在）。
- 覆盖率：required 覆盖率=100%。
- 类型匹配：映射输出类型 == 目录声明类型。
- 数组长度：array 与 xAxis.data 对齐（或按 nullHandling 策略）。
- 版本绑定：mapping 的 templateVersion/specVersion 必须与激活一致，否则“需复核”。

### 4.3 错误处理与标准响应
- ApiResponse：code/message/data/traceId。
- 常见 code：OK / VALIDATION_ERROR / VERSION_MISMATCH / NOT_FOUND / INTERNAL_ERROR。
- Service 抛业务异常（含错误码），Controller 统一转换响应。

### 4.4 版本管理与激活策略
- Template/Spec/Catalog/Mapping 全量版本化。
- 新版本保存不自动激活；经校验+Dry-run 通过后 `/registry/activate` 激活。
- 激活动作写审计，支持版本 diff 与快速回滚（切换 registry 指向）。

### 4.5 安全、日志与监控
- 预留鉴权；对模板/spec/目录/映射的新增/更新/激活写审计。
- 指标：阶段一/二耗时、校验失败率、Dry-run 使用率。

### 4.6 测试策略
- 参数化集成测试：遍历 chartId 组合 → 生成目录 → 校验映射 → Dry-run → 断言 title/xAxis/series 关键字段。
- 单元：PlaceholderManager、QueryOrchestrator（聚合/透视）。
- API 契约测试：请求/响应按 JSON Schema 校验。

### 4.7 实施步骤与配置示例
后端优先：
1) JoltSpecRegistryService & ChartRegistryService
2) PlaceholderCatalogService（阶段一运行+输出扫描）
3) MappingService（validate/dry-run）+ QueryOrchestrator（先内存）
4) TwoStage API 升级 DTO + 统一响应/日志
5) Controller 与 Repository 打通

前端：
1) 三栏页面骨架 → 目录树/映射表单/schema 浏览
2) 预览/校验/Dry-run 流程与联动
3) 激活版本与转换演示联动（“一键用激活版本渲染”）

---

## 五、实施优先级与开发路线图

### 5.1 里程碑（建议）
- M1（后端基础，2 周）：Registry/Spec/Template 服务与 API；Catalog 生成；TwoStage DTO；统一响应
- M2（映射与编排，2–3 周）：Mapping validate/dry-run；QueryOrchestrator v1；数据源 schema/预览 API
- M3（前端页面，2–3 周）：三栏映射管理页；版本管理与激活；与转换演示联动
- M4（工程化，2–4 周）：DB 持久化与缓存；审计/监控完善；参数化测试矩阵；模板/spec 生成器（可选）

### 5.2 风险与缓解
- 真实数据源差异大 → 以 Orchestrator 标准化查询/透视。
- 多图表版本膨胀 → 版本注册表 + 审计 + diff/回滚。
- 前端复杂度高 → 目录驱动 UI、JSON Schema 生成表单、分步校验与 Dry-run。

