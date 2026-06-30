<template>
  <el-footer class="app-footer">

    <!-- 页脚内容 -->
    <div class="footer-content">
      <!-- 左侧信息 -->
      <div class="footer-left">
        <span class="copyright">
          © {{ currentYear }} {{ themeStore.layout.logoText || '管理系统' }}
        </span>
        <span class="divider">|</span>
        <span class="version">{{ themeStore.layout.logoSubtext || 'v1.0.0' }}</span>
      </div>

    </div>
  </el-footer>
</template>

<script setup>
import { ref } from "vue";
import { useThemeStore } from "@/stores/theme";

const themeStore = useThemeStore();
const currentYear = ref(new Date().getFullYear());
</script>

<style lang="scss" scoped>
.app-footer {
  background: #ffffff;
  height: 56px;
  padding: 0 24px;
  margin: 0;
  border-radius: 0;
  border-top: 1px solid #e7ecf4;
  border-right: 0;
  border-bottom: 0;
  border-left: 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: 13px;
  font-weight: 400;
  color: #606266;


  // 内容区域
  .footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 2;

    // 左侧信息
    .footer-left {
      display: flex;
      align-items: center;
      gap: 10px;

      .copyright {
        color: #606266;
        font-weight: 500;
      }

      .version {
        color: #606266;
        opacity: 0.8;
        font-size: 12px;
      }
    }

    // 分割线
    .divider {
      color: #606266;
      opacity: 0.3;
      font-weight: 300;
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    height: auto;
    min-height: 56px;
    padding: 12px 16px;

    .footer-content {
      flex-direction: column;
      gap: 8px;
      font-size: 12px;

      .footer-left {
        flex-wrap: wrap;
        justify-content: center;
      }
    }
  }
}
</style>
