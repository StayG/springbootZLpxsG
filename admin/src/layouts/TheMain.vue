<template>
  <el-container class="layout-container" :class="layoutClass">
    <!-- 侧边栏布局 -->
    <the-aside></the-aside>
    <el-container class="main-container">
      <the-header></the-header>
      <el-main class="content-main">
        <el-scrollbar>
          <router-view></router-view>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useThemeStore } from "@/stores/theme";
import TheAside from "@/layouts/TheAside.vue";
import TheHeader from "@/layouts/TheHeader.vue";

const themeStore = useThemeStore();

// 布局类名
const layoutClass = computed(() => {
  return {
    'layout-side': true,
    'fixed-header': themeStore.layout.fixedHeader,
  };
});
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  flex-direction: row;
  background: #f8fafc;

  .main-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
    background: #f8fafc;
  }

  .content-main {
    flex: 1;
    background: #f8fafc;
    overflow: hidden;
    padding: 14px 18px 16px;

    :deep(.el-scrollbar__view) {
      height: 100%;
    }

    /* 主内容区仍可滚动，只隐藏纵向滚动条，保留横向滚动条 */
    :deep(.el-scrollbar__bar.is-vertical) {
      display: none;
    }
    :deep(.el-scrollbar__wrap) {
      /* Firefox: 隐藏纵向，显示横向 */
      overflow-y: scroll;
      overflow-x: auto;
      scrollbar-width: none; /* 隐藏 Firefox 滚动条 */
      -ms-overflow-style: none; /* 隐藏 IE/Edge 滚动条 */
    }
    /* Webkit浏览器：完全隐藏纵向滚动条 */
    :deep(.el-scrollbar__wrap::-webkit-scrollbar) {
      width: 0; /* 隐藏纵向滚动条 */
      height: 0; /* 先隐藏所有 */
    }
    /* 只为横向滚动显示滚动条 */
    :deep(.el-scrollbar__wrap::-webkit-scrollbar:horizontal) {
      height: 8px; /* 显示横向滚动条 */
    }
    :deep(.el-scrollbar__wrap::-webkit-scrollbar-thumb) {
      background: #c1c1c1;
      border-radius: 4px;
    }
    :deep(.el-scrollbar__wrap::-webkit-scrollbar-thumb:hover) {
      background: #a8a8a8;
    }
    :deep(.el-scrollbar__wrap::-webkit-scrollbar-track) {
      background: #f1f1f1;
      border-radius: 4px;
    }
  }
}
</style>
