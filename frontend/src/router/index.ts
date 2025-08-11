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
          title: "系统概览",
          icon: "DashboardOutlined",
          description: "占位符映射管理系统总览",
        },
      },
      {
        path: "/mapping",
        name: "Mapping",
        component: () => import("@/views/Mapping/index.vue"),
        meta: {
          title: "占位符映射管理",
          icon: "LinkOutlined",
          description: "核心功能：智能映射配置与实时预览",
          featured: true,
        },
      },
      {
        path: "/virtual-database",
        name: "VirtualDatabase",
        component: () => import("@/views/VirtualDatabase/index.vue"),
        meta: {
          title: "虚拟数据库",
          icon: "DatabaseOutlined",
          description: "统一数据源管理与CRUD操作",
        },
      },
      {
        path: "/transformation",
        name: "Transformation",
        component: () => import("@/views/Transformation/index.vue"),
        meta: {
          title: "两阶段转换",
          icon: "SwapOutlined",
          description: "结构转换与数据回填演示",
        },
      },
      {
        path: "/template",
        name: "Template",
        component: () => import("@/views/Template/index.vue"),
        meta: {
          title: "模板管理",
          icon: "FileTextOutlined",
          description: "通用JSON模板与JOLT规范",
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
