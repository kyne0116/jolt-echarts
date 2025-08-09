# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an ECharts dynamic data visualization system with a two-stage transformation architecture. It converts universal semantic templates to final ECharts configurations through an intelligent transformation engine.

### Key Architecture

- **Two-Stage Transformation**: Stage 1 performs semantic transformation (universal template → ECharts structure), Stage 2 performs data filling (placeholder replacement)
- **Template Classification System**: Charts are categorized into 4 template types based on coordinate systems:
  - `CARTESIAN`: Line charts, bar charts, area charts (xAxis/yAxis)
  - `PIE`: Pie charts, doughnut charts, rose charts (radius/center)
  - `RADAR`: Radar charts, polar charts (radar.indicator)
  - `GAUGE`: Gauge charts, progress charts (min/max)
- **Smart Transformation Engine**: Automatically infers chart template type and applies appropriate conversion logic
- **Placeholder Management**: Maintains placeholders through transformation stages and replaces them with real data

## Development Commands

### Backend (Spring Boot)
```bash
# Clean and compile
mvn clean compile

# Run application
mvn spring-boot:run -DskipTests

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=CategoryTemplateCompatibilityTest

# Health check
curl http://localhost:8080/api/chart/two-stage/health
```

### Frontend (Vue 3 + Vite)
```bash
cd frontend

# Install dependencies
npm install

# Start development server (runs on port 3000)
npm run dev

# Build for production
npm run build

# Type checking
npm run type-check

# Lint code
npm run lint
```

## Core Service Classes

### TwoStageTransformationService
- **Location**: `src/main/java/com/example/chart/service/TwoStageTransformationService.java`
- **Purpose**: Orchestrates the complete two-stage transformation pipeline
- **Key Methods**:
  - `executeFullTransformation(String chartId, Map<String, Object> universalTemplate)`
  - `executeStage1Transformation()` - Structural conversion with placeholder preservation
  - `executeStage2Transformation()` - Data filling and placeholder replacement

### SmartTransformationEngine
- **Location**: `src/main/java/com/example/chart/service/SmartTransformationEngine.java`
- **Purpose**: Performs semantic transformation based on chart template types
- **Key Method**: `semanticTransformWithCategory(String chartType, Map<String, Object> universalTemplate)`
- **Template-specific transformations**:
  - `transformCartesianChart()` - For line/bar charts
  - `transformPieChart()` - For pie/doughnut charts  
  - `transformRadarChart()` - For radar charts
  - `transformGaugeChart()` - For gauge charts

### PlaceholderManager
- **Purpose**: Extracts and replaces placeholders in JSON structures
- **Pattern**: Placeholders use `${placeholder_name}` format
- **Key functionality**: Preserves placeholders during stage 1, replaces them in stage 2

### MappingRelationshipService
- **Purpose**: Manages field mapping relationships and simulates database queries
- **Current implementation**: Uses in-memory virtual database for demonstration
- **Extension point**: Ready to be replaced with real database integration

## File Structure and Resources

### ECharts Example Files
- **Location**: `src/main/resources/echarts/`
- **Structure**: Organized by chart category (折线图, 柱状图, 饼图, 雷达图, 仪表盘)
- **Usage**: Reference templates for final chart rendering

### JOLT Transformation Specs  
- **Location**: `src/main/resources/jolt-specs/`
- **Purpose**: Define transformation rules from universal templates to ECharts structures
- **Key files**:
  - `line-chart-placeholder.json` - Basic line charts
  - `line-chart-stacked.json` - Stacked line charts  
  - `bar-chart-placeholder.json` - Bar charts
  - `pie-chart-placeholder.json` - Pie charts
  - `radar-chart-placeholder.json` - Radar charts
  - `gauge-chart-placeholder.json` - Gauge charts

## Chart Type to File Mapping

The system supports 14+ chart types mapped to appropriate JOLT specs:

```java
// Chart ID → JOLT Spec mapping
"stacked_line_chart" → "line-chart-stacked.json"
"basic_line_chart" → "line-chart-placeholder.json" 
"smooth_line_chart" → "line-chart-placeholder.json"
"basic_bar_chart" → "bar-chart-placeholder.json"
"stacked_bar_chart" → "bar-chart-placeholder.json"
"basic_pie_chart" → "pie-chart-placeholder.json"
```

## API Endpoints

### Two-Stage Transformation
- `GET /api/chart/two-stage/template/{chartId}` - Get template by chart ID
- `POST /api/chart/two-stage/stage1/{chartId}` - Execute stage 1 transformation
- `POST /api/chart/two-stage/stage2/{chartId}` - Execute stage 2 transformation  
- `GET /api/chart/two-stage/echarts-directory` - Scan ECharts directory structure

### Testing and Validation
- `GET /api/chart/two-stage/health` - Health check
- `POST /api/chart/two-stage/validate/{chartId}` - Validate complete process

## Frontend Architecture

### Technology Stack
- **Vue 3** with Composition API
- **TypeScript** for type safety
- **Ant Design Vue** for UI components
- **ECharts** for chart rendering
- **Pinia** for state management
- **Vite** for build tooling

### Key Components
- **Transformation View** (`frontend/src/views/Transformation/index.vue`): Main demo interface
- **API Layer** (`frontend/src/api/index.ts`): Backend communication
- **Stores** (`frontend/src/stores/`): State management for transformation flow

### Proxy Configuration
Frontend development server proxies `/api` requests to backend at `http://localhost:8080`

## Database Integration Notes

Currently uses in-memory simulation via `UniversalChartDataView` with 40 fields covering all chart types. For production database integration:

1. **Extend MappingRelationshipService**: Replace `simulateDataQuery()` with real database queries
2. **Add JPA repositories**: Create repository interfaces for chart configs and mappings  
3. **Update application.yml**: Add database connection configuration
4. **Migration path**: The universal data view design supports seamless database integration

## Testing

### Test Coverage
- **Unit tests**: Core transformation logic with JUnit 5
- **Integration tests**: End-to-end transformation pipeline  
- **Compatibility tests**: Template type inference and chart classification

### Key Test Classes
- `CategoryTemplateCompatibilityTest` - Template system validation
- `TwoStageTransformationTest` - Pipeline testing
- `UniversalTemplateTest` - Template creation testing

## Common Development Tasks

When adding new chart types:
1. Add chart ID to appropriate `TemplateType` in `getSupportedChartTypes()`
2. Create/update corresponding JOLT spec file in `src/main/resources/jolt-specs/`
3. Update chart-to-spec mapping in `TwoStageTransformationService.getJoltSpecFileByChartId()`
4. Add ECharts example file in `src/main/resources/echarts/` if needed
5. Update frontend dropdown options and file path mappings

When modifying transformation logic:
1. Update the appropriate transform method in `SmartTransformationEngine`
2. Ensure placeholder preservation during stage 1
3. Test with complete transformation pipeline
4. Validate with both simulated and real data scenarios