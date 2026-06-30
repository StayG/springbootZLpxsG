<template>
  <el-header class="top-header">
    <!-- 左侧面包屑导航 -->
    <div class="header-left">
      <el-breadcrumb separator="/" v-if="themeStore.layout.showBreadcrumb">
        <el-breadcrumb-item :to="{ path: '/index/home' }">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
          {{ item }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧操作区 -->
    <div class="header-right">
      <!-- 通知图标：跳转公告信息 -->
      <div
        class="action-item notification"
        role="button"
        tabindex="0"
        title="公告信息"
        @click="handleGoNotices"
        @keydown.enter.prevent="handleGoNotices"
        @keydown.space.prevent="handleGoNotices"
      >
        <el-badge :value="notificationCount" :max="99" :hidden="notificationCount === 0">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
      </div>

      <!-- 分割线 -->
      <el-divider direction="vertical" />

      <!-- 用户信息下拉菜单 -->
      <el-dropdown placement="bottom-end" trigger="hover">
        <div class="user-info">
          <div class="avatar-wrapper">
            <image-view
              :src="avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            />
          </div>
          <div class="user-details">
            <span class="user-name">{{ loginName }}</span>
            <span class="user-role">{{ roleName }}</span>
          </div>
          <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown">
            <el-dropdown-item @click="handlePersonalCenter">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item @click="handleResetPassword">
              <el-icon><Edit /></el-icon>
              <span>修改密码</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { useUserStore } from "@/stores/user";
import { useThemeStore } from "@/stores/theme";
import { useRouter, useRoute } from "vue-router";
import config from "@/config/config";
import request from "@/utils/request.js";
import storage from "@/utils/storage";
import { ElMessage } from "element-plus";
import { onMounted, ref, watch, computed } from "vue";
import { User, SwitchButton, ArrowDown, Edit, Bell, HomeFilled } from "@element-plus/icons-vue";

const userStore = useUserStore();
const themeStore = useThemeStore();
const router = useRouter();
const route = useRoute();
const loginName = ref(null);
const roleName = ref(null);
const avatarUrl = ref(null);
const notificationCount = ref(0); // 通知数量

// 动态生成面包屑（从路由 meta 中读取）
const breadcrumbList = computed(() => {
  return route.meta?.breadcrumb || [];
});

// 监听 userStore.userInfo 的变化
watch(
  () => userStore.userInfo,
  (newUserInfo) => {
    if (newUserInfo.avatar) {
      avatarUrl.value = newUserInfo.avatar;
    } else {
      avatarUrl.value = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    }
  },
  { immediate: true }
);

// 退出登录
const handleLogout = () => {
  let tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
  request.post(`/${tableName}/logout`).then(() => {
    userStore.reset();
    ElMessage.success("退出登录成功");
    router.push("/login");
  });
};

// 跳转个人中心
const handlePersonalCenter = () => {
  router.push("/personal-center");
};

// 跳转修改密码
const handleResetPassword = () => {
  router.push("/reset-password");
};

// 跳转公告信息
const handleGoNotices = () => {
  router.push("/notices");
};

onMounted(() => {
  loginName.value = storage.get(config.CURRENT_LOGIN_NAME);
  roleName.value = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY);
  avatarUrl.value = userStore.userInfo.avatar || "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
  // 可以在这里加载通知数量
  // loadNotifications();
});
</script>

<style lang="scss" scoped>
.top-header {
  background: #ffffff;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 12px;
  border: 1px solid #e7ecf4;
  padding: 0 24px;
  margin: 12px 18px 0;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  position: relative;
  z-index: 100;

  // 左侧面包屑区域
  .header-left {
    flex: 1;

    :deep(.el-breadcrumb) {
      background: #f8fafc;
      padding: 8px 16px;
      margin: 0;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      font-size: 14px;

      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          color: #64748b;
          font-weight: 500;
          transition: color 0.3s ease;

          .el-icon {
            color: #94a3b8;
          }

          &:hover {
            color: #1890ff;
          }

          &.is-link {
            color: #1890ff;
            font-weight: 500;
          }
        }

        .el-breadcrumb__separator {
          color: #cbd5e1;
          margin: 0 8px;
        }

        &:last-child {
          .el-breadcrumb__inner {
            color: #1e293b;
            font-weight: 600;
          }
        }
      }
    }
  }

  // 右侧操作区
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    // 通知图标
    .action-item {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      color: #475569;

      &:hover {
        background: #f0f9ff;
        color: #1890ff;
      }

      :deep(.el-badge__content) {
        background: #67C23A;
        border: none;
      }
    }

    // 分割线
    :deep(.el-divider--vertical) {
      height: 24px;
      margin: 0;
      border-left: 1px solid #e2e8f0;
    }

    // 用户信息区域
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #f0f9ff;

        .user-name {
          color: #1890ff;
        }
      }

      .avatar-wrapper {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 2px solid #e2e8f0;
        overflow: hidden;
        transition: all 0.3s ease;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.14);
        }

        :deep(img) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .user-details {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .user-name {
          font-size: 14px;
          font-weight: 600;
          color: #1e293b;
          line-height: 1.4;
          transition: color 0.3s ease;
        }

        .user-role {
          font-size: 12px;
          color: #94a3b8;
          line-height: 1.2;
        }
      }

      .dropdown-icon {
        color: #94a3b8;
        font-size: 14px;
        transition: transform 0.3s ease;
      }

      &:hover .dropdown-icon {
        transform: translateY(2px);
      }
    }
  }
}

// 下拉菜单样式
:deep(.user-dropdown) {
  margin-top: 8px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    font-size: 14px;
    color: #475569;
    transition: all 0.3s ease;

    .el-icon {
      font-size: 16px;
      color: #94a3b8;
    }

    &:hover {
      background: #f0f9ff;
      color: #1890ff;

      .el-icon {
        color: #1890ff;
      }
    }

    &.is-divided {
      margin-top: 6px;
      border-top: 1px solid #e2e8f0;
    }
  }
}
</style>
