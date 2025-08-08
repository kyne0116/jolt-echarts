import type { RouteRecordRaw } from "vue-router";
import { createRouter, createWebHistory } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "Layout",
    component: () => import("@/layouts/MainLayout.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("@/views/Dashboard.vue"),
        meta: {
          title: "仪表板",
          icon: "DashboardOutlined",
        },
      },
      {
        path: "/chart-config",
        name: "ChartConfig",
        component: () => import("@/views/ChartConfig/index.vue"),
        meta: {
          title: "图表配置管理",
          icon: "BarChartOutlined",
        },
      },
      {
        path: "/jolt-spec",
        name: "JoltSpec",
        component: () => import("@/views/JoltSpec/index.vue"),
        meta: {
          title: "Jolt规范管理",
          icon: "CodeOutlined",
        },
      },
      {
        path: "/mapping",
        name: "Mapping",
        component: () => import("@/views/Mapping/index.vue"),
        meta: {
          title: "占位符映射管理",
          icon: "LinkOutlined",
        },
      },
      {
        path: "/template",
        name: "Template",
        component: () => import("@/views/Template/index.vue"),
        meta: {
          title: "通用JSON模板",
          icon: "FileTextOutlined",
        },
      },
      {
        path: "/transformation",
        name: "Transformation",
        component: () => import("@/views/Transformation/index.vue"),
        meta: {
          title: "两阶段转换演示",
          icon: "SwapOutlined",
        },
      },
      {
        path: "/virtual-database",
        name: "VirtualDatabase",
        component: () => import("@/views/VirtualDatabase/index.vue"),
        meta: {
          title: "虚拟数据库管理",
          icon: "DatabaseOutlined",
        },
      },
      {
        path: "/system",
        name: "System",
        component: () => import("@/views/System/index.vue"),
        meta: {
          title: "系统设置",
          icon: "SettingOutlined",
        },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - ECharts动态数据流管理平台`;
  } else {
    document.title = "ECharts动态数据流管理平台";
  }

  next();
});

export default router;
