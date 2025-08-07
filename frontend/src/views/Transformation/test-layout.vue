<template>
  <div class="test-layout">
    <h2>四个卡片布局测试</h2>
    
    <!-- 测试四个卡片的布局 -->
    <a-row :gutter="[16, 16]" class="test-cards">
      <!-- 卡片1 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="6">
        <a-card title="卡片1 - 通用JSON模板" class="test-card">
          <div class="card-content">
            <p>这是第一个卡片的内容</p>
            <p>屏幕尺寸：</p>
            <ul>
              <li>xs (< 576px): 24/24 (100%)</li>
              <li>sm (≥ 576px): 24/24 (100%)</li>
              <li>md (≥ 768px): 12/24 (50%)</li>
              <li>lg (≥ 992px): 12/24 (50%)</li>
              <li>xl (≥ 1200px): 6/24 (25%)</li>
            </ul>
          </div>
        </a-card>
      </a-col>
      
      <!-- 卡片2 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="6">
        <a-card title="卡片2 - 第一阶段输出" class="test-card">
          <div class="card-content">
            <p>这是第二个卡片的内容</p>
            <p>在超大屏幕(xl)上，四个卡片应该并排显示</p>
          </div>
        </a-card>
      </a-col>
      
      <!-- 卡片3 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="6">
        <a-card title="卡片3 - 第二阶段输出" class="test-card">
          <div class="card-content">
            <p>这是第三个卡片的内容</p>
            <p>在大屏幕(lg)上，每行显示两个卡片</p>
          </div>
        </a-card>
      </a-col>
      
      <!-- 卡片4 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="6">
        <a-card title="卡片4 - 图表预览" class="test-card">
          <div class="card-content">
            <p>这是第四个卡片的内容</p>
            <p>在中等屏幕(md)上，每行显示两个卡片</p>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 当前屏幕信息 -->
    <a-card title="当前屏幕信息" style="margin-top: 24px;">
      <p>当前窗口宽度: <span id="window-width">{{ windowWidth }}px</span></p>
      <p>当前断点: <span id="current-breakpoint">{{ currentBreakpoint }}</span></p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const windowWidth = ref(window.innerWidth)
const currentBreakpoint = ref('')

const updateBreakpoint = () => {
  const width = window.innerWidth
  windowWidth.value = width
  
  if (width < 576) {
    currentBreakpoint.value = 'xs (< 576px)'
  } else if (width < 768) {
    currentBreakpoint.value = 'sm (≥ 576px)'
  } else if (width < 992) {
    currentBreakpoint.value = 'md (≥ 768px)'
  } else if (width < 1200) {
    currentBreakpoint.value = 'lg (≥ 992px)'
  } else {
    currentBreakpoint.value = 'xl (≥ 1200px)'
  }
}

const handleResize = () => {
  updateBreakpoint()
}

onMounted(() => {
  updateBreakpoint()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.test-layout {
  padding: 24px;
}

.test-cards {
  margin-bottom: 24px;
}

.test-card {
  height: 300px;
  border: 2px solid #1890ff;
}

.card-content {
  height: 200px;
  overflow-y: auto;
}

.test-card .ant-card-head {
  background: #f0f9ff;
}

/* 响应式测试样式 */
@media (max-width: 575px) {
  .test-card {
    border-color: #ff4d4f;
  }
}

@media (min-width: 576px) and (max-width: 767px) {
  .test-card {
    border-color: #faad14;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .test-card {
    border-color: #52c41a;
  }
}

@media (min-width: 992px) and (max-width: 1199px) {
  .test-card {
    border-color: #722ed1;
  }
}

@media (min-width: 1200px) {
  .test-card {
    border-color: #1890ff;
  }
}
</style>
