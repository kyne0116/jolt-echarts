import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'

// Ant Design Vue
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 全局样式
import './styles/global.css'

// 创建应用实例
const app = createApp(App)

// 注册插件
app.use(pinia)
app.use(router)
app.use(Antd)

// 挂载应用
app.mount('#app')

console.log('🚀 ECharts动态数据流管理平台启动成功')
