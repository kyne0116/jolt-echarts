# 配置重构测试指南

## 🎯 测试目标
验证从前端硬编码到后端接口的重构是否成功

## 📋 测试步骤

### 1. 后端服务测试
```bash
# 重启后端服务
mvn spring-boot:run

# 测试配置接口
curl http://localhost:8080/api/chart/config/chart-types
curl http://localhost:8080/api/chart/config/chart-specific/basic_line_chart
curl http://localhost:8080/api/chart/config/preprocessing-rules
```

### 2. 前端功能测试

#### 2.1 基础功能测试
- [ ] 访问主页面，确认图表列表正常加载
- [ ] 选择不同的图表类型，确认切换正常
- [ ] 执行两阶段转换，确认配置正确应用

#### 2.2 图表类型特定测试
- [ ] **基础折线图**: 确认显示直线连接 (`smooth: false`)
- [ ] **平滑折线图**: 确认显示平滑曲线 (`smooth: true`)
- [ ] **堆叠折线图**: 确认显示堆叠区域效果 (`areaStyle: {}`)

#### 2.3 配置服务测试
- [ ] 打开浏览器开发者工具
- [ ] 查看网络请求，确认调用了配置接口
- [ ] 查看控制台日志，确认配置服务正常工作

### 3. 错误处理测试
- [ ] 断开网络连接，测试回退机制
- [ ] 输入无效的图表ID，测试错误处理
- [ ] 清除浏览器缓存，测试缓存机制

## ✅ 预期结果

### 后端接口响应
```json
// GET /api/chart/config/chart-types
{
  "code": 200,
  "message": "success",
  "data": {
    "pathToChartId": {
      "折线图/基础折线图.json": "basic_line_chart",
      "折线图/基础平滑折线图.json": "smooth_line_chart",
      "折线图/折线图堆叠.json": "stacked_line_chart"
    },
    "chartIdToPath": {
      "basic_line_chart": "折线图/基础折线图.json",
      "smooth_line_chart": "折线图/基础平滑折线图.json",
      "stacked_line_chart": "折线图/折线图堆叠.json"
    },
    "categories": {
      "折线图": ["basic_line_chart", "smooth_line_chart", "stacked_line_chart"]
    }
  }
}
```

### 前端控制台日志
```
📋 [配置服务] 从后端获取图表类型映射
✅ [配置服务] 图表类型映射获取成功
🔧 [配置服务] 开始预处理图表数据: smooth_line_chart
✅ [配置服务] 图表数据预处理完成: smooth_line_chart
```

### 图表渲染效果
- **基础折线图**: 尖锐的折线连接
- **平滑折线图**: 平滑的曲线连接  
- **堆叠折线图**: 彩色区域垂直堆叠

## 🔧 故障排除

### 问题1: 配置接口404错误
**解决方案**: 确认后端服务已重启，配置控制器已加载

### 问题2: 前端配置服务调用失败
**解决方案**: 检查API路径是否正确，网络连接是否正常

### 问题3: 图表渲染效果不正确
**解决方案**: 检查控制台日志，确认配置是否正确应用

### 问题4: 异步函数调用错误
**解决方案**: 确认所有异步函数调用都使用了 await 或 .catch()

## 📊 成功标准

- [ ] 所有配置接口正常响应
- [ ] 前端配置服务正常工作
- [ ] 图表类型映射正确
- [ ] 图表渲染效果符合预期
- [ ] 错误处理机制正常
- [ ] 缓存机制正常工作

## 🚀 下一步

重构成功后，可以考虑：
1. 添加更多图表类型配置
2. 实现配置的动态更新
3. 添加配置版本管理
4. 优化缓存策略
