# ECharts动态数据流架构前端管理界面

基于Vue 3 + TypeScript + Ant Design Vue构建的ECharts动态数据流架构管理平台前端界面。

## 🚀 功能特性

### 核心功能模块

1. **📊 仪表板**
   - 系统状态概览
   - 统计数据展示
   - 快速操作入口
   - 示例图表预览

2. **🔄 两阶段转换演示**
   - 可视化展示完整的两阶段转换流程
   - 实时显示每个阶段的输入输出数据
   - 转换过程的步骤跟踪和调试功能
   - 图表预览和下载功能

3. **🗺️ 占位符映射管理**
   - 可视化展示占位符与虚拟数据库表列的映射关系
   - 映射关系的创建、编辑、删除功能
   - 映射关系的导入导出
   - 映射测试和验证

4. **📋 图表配置管理**
   - 展示所有可用的ECharts图表类型
   - 图表配置的CRUD操作界面
   - 图表配置的预览和编辑

5. **📝 Jolt规范管理**
   - 管理每个图表类型对应的Jolt转换规范文件
   - Jolt规范的在线编辑器
   - Jolt规范的测试和验证功能

6. **📄 通用JSON模板管理**
   - 管理带占位符的通用JSON模板
   - 模板的可视化编辑器
   - 占位符的自动识别和高亮显示

7. **🗄️ 虚拟数据库管理**
   - 模拟数据库表结构的管理界面
   - 虚拟数据的CRUD操作
   - 数据的批量导入和导出

8. **⚙️ 系统设置**
   - 系统状态监控
   - 配置管理
   - 日志查看

## 🛠️ 技术栈

- **前端框架**: Vue 3.4+ with Composition API
- **类型系统**: TypeScript 5.3+
- **构建工具**: Vite 5.0+
- **UI组件库**: Ant Design Vue 4.0+
- **图表库**: ECharts 5.4+
- **状态管理**: Pinia 2.1+
- **路由管理**: Vue Router 4.2+
- **HTTP客户端**: Axios 1.6+
- **代码编辑器**: Monaco Editor 0.45+
- **JSON展示**: Vue JSON Pretty 2.2+

## 📦 安装和运行

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
cd frontend
npm install
```

### 开发环境运行

```bash
npm run dev
```

应用将在 http://localhost:3000 启动

### 生产环境构建

```bash
npm run build
```

构建产物将输出到 `dist` 目录

### 类型检查

```bash
npm run type-check
```

### 代码检查和修复

```bash
npm run lint
```

## 🏗️ 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口定义
│   │   └── index.ts       # API客户端配置
│   ├── components/        # 公共组件
│   ├── layouts/           # 布局组件
│   │   └── MainLayout.vue # 主布局
│   ├── router/            # 路由配置
│   │   └── index.ts       # 路由定义
│   ├── stores/            # Pinia状态管理
│   │   ├── index.ts       # Store入口
│   │   ├── system.ts      # 系统状态
│   │   ├── transformation.ts # 转换流程状态
│   │   ├── mapping.ts     # 映射关系状态
│   │   └── ...           # 其他Store
│   ├── styles/            # 全局样式
│   │   └── global.css     # 全局CSS
│   ├── types/             # TypeScript类型定义
│   │   └── index.ts       # 类型定义
│   ├── views/             # 页面组件
│   │   ├── Dashboard.vue  # 仪表板
│   │   ├── Transformation/ # 两阶段转换演示
│   │   ├── Mapping/       # 映射关系管理
│   │   ├── ChartConfig/   # 图表配置管理
│   │   └── ...           # 其他页面
│   ├── App.vue            # 根组件
│   └── main.ts            # 应用入口
├── index.html             # HTML模板
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript配置
├── vite.config.ts         # Vite配置
└── README.md              # 项目文档
```

## 🔌 API集成

### 后端API接口

前端通过以下API与后端服务集成：

```typescript
// 两阶段转换API
/api/chart/two-stage/health              # 健康检查
/api/chart/two-stage/validate/{chartId}  # 完整转换验证
/api/chart/two-stage/template/{chartId}  # 获取通用模板
/api/chart/two-stage/stage1              # 第一阶段转换
/api/chart/two-stage/stage2/{chartId}    # 第二阶段转换
/api/chart/two-stage/mappings/{chartId}  # 获取映射关系
/api/chart/two-stage/placeholder/test    # 占位符测试
```

### 代理配置

开发环境下，Vite配置了API代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
    },
  },
}
```

## 🎨 主要功能使用说明

### 1. 两阶段转换演示

1. 选择图表类型（堆叠折线图、基础柱状图、饼图）
2. 点击"执行完整转换"按钮
3. 观察转换进度和每个阶段的数据变化
4. 查看最终生成的ECharts配置和图表预览
5. 可以下载生成的图表图片

### 2. 占位符映射管理

1. 选择图表类型
2. 查看当前的映射关系列表
3. 点击"新增映射"创建新的映射关系
4. 填写占位符名称、数据表、列名等信息
5. 可以测试映射关系的有效性
6. 支持批量编辑和导出功能

### 3. 系统监控

- 实时显示后端连接状态
- 自动心跳检测（每30秒）
- 系统版本和功能信息展示
- 错误信息提示和处理

## 🔧 开发指南

### 添加新页面

1. 在 `src/views/` 目录下创建新的Vue组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 在主布局的菜单中添加导航项

### 添加新的API接口

1. 在 `src/api/index.ts` 中添加API方法
2. 在对应的Store中调用API方法
3. 在组件中使用Store的状态和方法

### 添加新的状态管理

1. 在 `src/stores/` 目录下创建新的Store文件
2. 在 `src/stores/index.ts` 中导出新的Store
3. 在组件中使用 `useXxxStore()` 访问状态

## 🚀 部署说明

### 开发环境部署

1. 确保后端服务在 http://localhost:8080 运行
2. 运行 `npm run dev` 启动前端开发服务器
3. 访问 http://localhost:3000

### 生产环境部署

1. 运行 `npm run build` 构建生产版本
2. 将 `dist` 目录部署到Web服务器
3. 配置反向代理将 `/api` 请求转发到后端服务

### Docker部署

```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: your-email@example.com

---

**ECharts动态数据流架构管理平台** - 让图表数据管理更简单、更高效！🚀
