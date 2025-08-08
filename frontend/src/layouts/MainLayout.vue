<template>
  <a-layout class="main-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      width="250"
      class="sidebar"
    >
      <div class="logo">
        <img src="/vite.svg" alt="Logo" class="logo-img" />
        <span v-if="!collapsed" class="logo-text">ECharts管理平台</span>
      </div>
      
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        class="sidebar-menu"
        @click="handleMenuClick"
      >
        <a-menu-item key="/dashboard">
          <template #icon>
            <DashboardOutlined />
          </template>
          仪表板
        </a-menu-item>

        <a-menu-item key="/chart-config">
          <template #icon>
            <BarChartOutlined />
          </template>
          图表配置管理
        </a-menu-item>

        <a-menu-item key="/jolt-spec">
          <template #icon>
            <CodeOutlined />
          </template>
          Jolt规范管理
        </a-menu-item>

        <a-menu-item key="/mapping">
          <template #icon>
            <LinkOutlined />
          </template>
          占位符映射管理
        </a-menu-item>

        <a-menu-item key="/template">
          <template #icon>
            <FileTextOutlined />
          </template>
          通用JSON模板
        </a-menu-item>

        <a-menu-item key="/transformation">
          <template #icon>
            <SwapOutlined />
          </template>
          两阶段转换演示
        </a-menu-item>

        <a-menu-item key="/virtual-database">
          <template #icon>
            <DatabaseOutlined />
          </template>
          虚拟数据库管理
        </a-menu-item>

        <a-menu-item key="/system">
          <template #icon>
            <SettingOutlined />
          </template>
          系统设置
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <!-- 主内容区 -->
    <a-layout class="main-content">
      <!-- 顶部导航栏 -->
      <a-layout-header class="header">
        <div class="header-left">
          <a-button
            type="text"
            class="trigger"
            @click="collapsed = !collapsed"
          >
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>
              <HomeOutlined />
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 系统状态指示器 -->
          <div class="status-indicator">
            <a-badge
              :status="systemStore.isHealthy ? 'success' : 'error'"
              :text="systemStore.connectionStatus"
            />
          </div>
          
          <!-- 刷新按钮 -->
          <a-button
            type="text"
            :loading="systemStore.loading"
            @click="systemStore.checkHealth"
          >
            <ReloadOutlined />
          </a-button>
          
          <!-- 全屏按钮 -->
          <a-button type="text" @click="toggleFullscreen">
            <FullscreenOutlined v-if="!isFullscreen" />
            <FullscreenExitOutlined v-else />
          </a-button>
        </div>
      </a-layout-header>
      
      <!-- 内容区域 -->
      <a-layout-content class="content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>
      
      <!-- 底部状态栏 -->
      <a-layout-footer class="footer">
        <div class="footer-content">
          <span>ECharts动态数据流架构管理平台 v{{ systemStore.status.version }}</span>
          <span>最后心跳: {{ lastHeartbeatText }}</span>
        </div>
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { useSystemStore } from '@/stores'
import {
    BarChartOutlined,
    CodeOutlined,
    DashboardOutlined,
    DatabaseOutlined,
    FileTextOutlined,
    FullscreenExitOutlined,
    FullscreenOutlined,
    HomeOutlined,
    LinkOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    ReloadOutlined,
    SettingOutlined,
    SwapOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const systemStore = useSystemStore()

// 响应式状态
const collapsed = ref(false)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])
const isFullscreen = ref(false)

// 计算属性
const currentPageTitle = computed(() => {
  return route.meta?.title as string || '未知页面'
})

const lastHeartbeatText = computed(() => {
  if (!systemStore.status.lastHeartbeat) return '无'
  return dayjs(systemStore.status.lastHeartbeat).format('HH:mm:ss')
})

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
  },
  { immediate: true }
)

// 方法
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 生命周期
onMounted(() => {
  // 启动系统心跳检测
  systemStore.startHeartbeat()
  
  // 监听全屏状态变化
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  position: fixed;
  height: 100vh;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.1);
  margin-bottom: 1px;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-text {
  color: white;
  font-size: 16px;
  font-weight: bold;
  margin-left: 12px;
  white-space: nowrap;
}

.sidebar-menu {
  height: calc(100vh - 65px);
  border-right: 0;
}

.main-content {
  margin-left: 250px;
  transition: margin-left 0.2s;
}

.main-content.collapsed {
  margin-left: 80px;
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger {
  font-size: 18px;
  line-height: 64px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.breadcrumb {
  margin-left: 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-indicator {
  font-size: 14px;
}

.content {
  margin: 24px;
  padding: 0;
  min-height: calc(100vh - 64px - 48px - 48px);
}

.content-wrapper {
  background: #fff;
  border-radius: 8px;
  min-height: 100%;
}

.footer {
  text-align: center;
  background: #f0f2f5;
  padding: 12px 24px;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .sidebar.mobile-open {
    transform: translateX(0);
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
