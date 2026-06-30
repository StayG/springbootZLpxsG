<template>
  <el-aside class="the-aside">
    <!-- 系统Logo和标题区域 -->
    <div class="aside-header" v-if="themeStore.sidebar.showLogo">
      <div class="logo-container" v-if="themeStore.sidebar.showLogoIcon">
        <el-icon class="logo-icon" :size="20"><Monitor /></el-icon>
      </div>
      <div class="system-title">
        <h3>在线考试系统</h3>
        <p class="system-version">管理后台</p>
      </div>
    </div>

    <!-- 菜单区域 -->
    <el-scrollbar class="menu-scrollbar">
      <el-menu 
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds" 
        :unique-opened="true" 
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <!-- 首页菜单 -->
        <el-menu-item 
          index="home" 
          class="menu-item home-menu-item"
          :data-indicator="themeStore.sidebar.homeMenuActiveIndicatorStyle"
        >
          <el-icon v-if="themeStore.sidebar.showMenuIcon"><House /></el-icon>
          <span>系统首页</span>
        </el-menu-item>

        <!-- 其他菜单组 -->
        <el-sub-menu 
          v-for="(item, index) in backMenuList" 
          :key="index" 
          :index="`menu-${index}`"
        >
          <template #title>
            <el-icon v-if="themeStore.sidebar.showMenuIcon"><component :is="getMenuIcon(item.menu)" /></el-icon>
            <span>{{ item.menu }}</span>
          </template>
          <el-menu-item 
            v-for="(child, sort) in item.child" 
            :key="sort" 
            :index="child.tableName"
            class="sub-menu-item"
            :data-indicator="themeStore.sidebar.subMenuActiveIndicatorStyle"
          >
            <span>{{ child.menu }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>

    <div class="aside-footer">© 2026 在线考试系统 v1.0.0</div>
  </el-aside>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useThemeStore } from "@/stores/theme";
import { 
  Grid,
  HomeFilled,
  House,
  User, 
  UserFilled, 
  OfficeBuilding, 
  Bell, 
  Setting, 
  Calendar,
  Document,
  DataAnalysis,
  Folder,
  Management,
  Monitor,
  ShoppingCart,
  ChatDotRound,
  TrendCharts,
  Files,
  Tickets,
  MessageBox,
  PieChart,
  Operation,
  Promotion,
  Star,
  Trophy,
  Medal,
  Flag,
  Location,
  Phone,
  Briefcase,
  Notebook,
  Reading,
  School,
  VideoCamera,
  Picture,
  Headset,
  Service,
  Tools,
  Box,
  Present,
  Coin,
  Wallet,
  CreditCard,
  ShoppingBag
} from "@element-plus/icons-vue";
import storage from "@/utils/storage.js";
import config from "@/config/config.js";
import menu from "@/utils/menu.js";

const router = useRouter();
const route = useRoute();
const themeStore = useThemeStore();
const roleName = ref(null);
const defaultOpeneds = ref([]);
const activeMenu = ref("home");

// 可用的图标池
const availableIcons = [
  User, UserFilled, OfficeBuilding, Bell, Setting, Calendar,
  Document, DataAnalysis, Folder, Management, Monitor, ShoppingCart,
  ChatDotRound, TrendCharts, Files, Tickets, MessageBox, PieChart,
  Operation, Promotion, Star, Trophy, Medal, Flag, Location, Phone,
  Briefcase, Notebook, Reading, School, VideoCamera, Picture,
  Headset, Service, Tools, Box, Present, Coin, Wallet, CreditCard, ShoppingBag
];

const menuIconMap = {
  首页: House,
  公告信息: Bell,
  用户管理: User,
  教师管理: User,
  科目管理: School,
  试题管理: Notebook,
  试卷管理: Tickets,
  考试管理: Calendar,
  考试记录: DataAnalysis,
  成绩与统计: TrendCharts,
  阅卷管理: Files,
  错题本: Document,
  系统设置: Setting,
};

// 简单的字符串哈希函数
const hashString = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // 转换为32位整数
  }
  return Math.abs(hash);
};

// 根据菜单名称获取随机但固定的图标
const getMenuIcon = (menuName) => {
  if (menuIconMap[menuName]) {
    return menuIconMap[menuName];
  }
  const hash = hashString(menuName);
  const iconIndex = hash % availableIcons.length;
  return availableIcons[iconIndex];
};

// 后台菜单列表
const backMenuList = computed(() => {
  const menus = menu.list();
  const currentMenu = menus.find(item => item.roleName === roleName.value);
  return currentMenu?.backMenu || [];
});

/**
 * 根据当前路由更新选中的菜单项
 */
const updateActiveMenu = () => {
  const path = route.path;
  
  // 判断是否是首页
  if (path === '/index/home' || path === '/index' || path === '/') {
    activeMenu.value = 'home';
    return;
  }
  
  // 提取路由的第一层路径作为菜单标识
  const pathParts = path.split('/').filter(part => part);
  if (pathParts.length > 0) {
    activeMenu.value = pathParts[0];
  }
};

/**
 * 加载菜单
 */
const loadMenu = () => {
  roleName.value = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY);
  // 加载菜单后立即更新选中状态
  updateActiveMenu();
};

/**
 * 菜单选择事件
 */
const handleMenuSelect = (index) => {
  activeMenu.value = index;
  if (index === 'home') {
    router.push("/index/home");
  } else {
    router.push("/" + index);
  }
};

// 监听路由变化，更新选中菜单
const unwatchRoute = router.afterEach(() => {
  updateActiveMenu();
});

onMounted(() => {
  loadMenu();
});

// 组件卸载时清理监听
onBeforeUnmount(() => {
  if (unwatchRoute) {
    unwatchRoute();
  }
});
</script>

<style lang="scss" scoped>
.the-aside {
  width: var(--aside-width, 240px);
  height: 100vh;
  background: linear-gradient(180deg, #243b67 0%, #1f4f8a 56%, #1f3f74 100%);
  box-shadow: 10px 0 24px rgba(30, 64, 112, 0.28);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(147, 197, 253, 0.3);

  // 头部Logo区域
  .aside-header {
    padding: 16px 14px;
    margin: var(--sidebar-header-margin, 0);
    display: flex;
    align-items: center;
    gap: 12px;
    background: rgba(15, 23, 42, 0.16);
    border-top-style: var(--sidebar-header-border-top-style, none);
    border-top-width: var(--sidebar-header-border-top-width, 1px);
    border-top-color: var(--sidebar-header-border-top-color, #e4e7ed);
    border-right-style: var(--sidebar-header-border-right-style, none);
    border-right-width: var(--sidebar-header-border-right-width, 1px);
    border-right-color: var(--sidebar-header-border-right-color, #e4e7ed);
    border-bottom-style: var(--sidebar-header-border-bottom-style, solid);
    border-bottom-width: var(--sidebar-header-border-bottom-width, 1px);
    border-bottom-color: rgba(255, 255, 255, 0.1);
    border-left-style: var(--sidebar-header-border-left-style, none);
    border-left-width: var(--sidebar-header-border-left-width, 1px);
    border-left-color: var(--sidebar-header-border-left-color, #e4e7ed);

    .logo-container {
      width: 34px;
      height: 34px;
      background: linear-gradient(145deg, #7dd3fc, #38bdf8);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all 0.3s ease;
      position: relative;

      .logo-icon {
        color: #ffffff;
      }

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 8px 24px rgba(56, 189, 248, 0.32);
      }
    }

    .system-title {
      flex: 1;
      min-width: 0;

      h3 {
        margin: 0;
        font-size: 15px;
        font-weight: 700;
        color: #ffffff;
        line-height: 1.4;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .system-version {
        margin: 4px 0 0 0;
        font-size: 12px;
        color: rgba(191, 219, 254, 0.92);
        line-height: 1;
      }
    }
  }

  // 菜单滚动区域
  .menu-scrollbar {
    flex: 1;
    padding: 10px 10px 12px;
    margin: var(--sidebar-menu-area-margin, 0);
    background: transparent;
    border-top-style: var(--sidebar-menu-area-border-top-style, none);
    border-top-width: var(--sidebar-menu-area-border-top-width, 1px);
    border-top-color: var(--sidebar-menu-area-border-top-color, #e4e7ed);
    border-right-style: var(--sidebar-menu-area-border-right-style, none);
    border-right-width: var(--sidebar-menu-area-border-right-width, 1px);
    border-right-color: var(--sidebar-menu-area-border-right-color, #e4e7ed);
    border-bottom-style: var(--sidebar-menu-area-border-bottom-style, none);
    border-bottom-width: var(--sidebar-menu-area-border-bottom-width, 1px);
    border-bottom-color: var(--sidebar-menu-area-border-bottom-color, #e4e7ed);
    border-left-style: var(--sidebar-menu-area-border-left-style, none);
    border-left-width: var(--sidebar-menu-area-border-left-width, 1px);
    border-left-color: var(--sidebar-menu-area-border-left-color, #e4e7ed);

    .sidebar-menu {
      background: transparent;
      border: none;
      padding: 0 6px;

      // 菜单项基础样式
      .menu-item {
        height: 42px;
        line-height: 42px;
        border-radius: var(--menu-item-border-radius, 8px);
        padding: 0 14px;
        margin: var(--menu-item-margin, 0 0 4px 0);
        background: var(--menu-item-bg, transparent);
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          transition: all 0.25s ease;
        }

        :deep(.el-icon) {
          color: #bfdbfe;
          font-size: 16px;
          transition: color 0.25s ease;
        }

        span {
          color: #e0f2fe;
          font-weight: 500;
          font-size: 14px;
          transition: color 0.25s ease;
        }

        &:hover {
          background: rgba(125, 211, 252, 0.16);

          :deep(.el-icon) {
            color: #ffffff;
          }

          span {
            color: #ffffff;
          }
        }

        &.is-active {
          background: linear-gradient(135deg, #38bdf8, #2563eb);
          box-shadow: 0 10px 16px rgba(37, 99, 235, 0.3);

          :deep(.el-icon) {
            color: #ffffff;
          }

          span {
            color: #ffffff;
            font-weight: 600;
          }
        }
      }

      // 系统首页菜单样式（使用一级菜单参数）
      .home-menu-item {
        background: var(--menu-item-bg, transparent);
        padding: var(--menu-item-padding, 0 16px);
        margin: var(--menu-item-margin, 0 0 4px 0);
        
        // 默认状态边框 - 分别设置上下左右
        border-top-style: var(--menu-item-border-top-style, none);
        border-top-width: var(--menu-item-border-top-width, 1px);
        border-top-color: var(--menu-item-border-top-color, transparent);
        border-right-style: var(--menu-item-border-right-style, none);
        border-right-width: var(--menu-item-border-right-width, 1px);
        border-right-color: var(--menu-item-border-right-color, transparent);
        border-bottom-style: var(--menu-item-border-bottom-style, none);
        border-bottom-width: var(--menu-item-border-bottom-width, 1px);
        border-bottom-color: var(--menu-item-border-bottom-color, transparent);
        border-left-style: var(--menu-item-border-left-style, none);
        border-left-width: var(--menu-item-border-left-width, 1px);
        border-left-color: var(--menu-item-border-left-color, transparent);
        border-radius: var(--menu-item-border-radius, 8px);

        :deep(.el-icon) {
          color: #bfdbfe;
        }

        span {
          color: #e0f2fe;
          font-size: var(--menu-item-font-size, 14px);
          font-weight: var(--menu-item-font-weight, 500);
        }

        &:hover {
          background: rgba(125, 211, 252, 0.16);

          :deep(.el-icon) {
            color: #ffffff;
          }

          span {
            color: #ffffff;
          }
        }

        &.is-active {
          background: linear-gradient(135deg, #38bdf8, #2563eb);
          position: relative;
          
          // 激活状态边框 - 分别设置上下左右
          border-top-style: var(--menu-item-active-border-top-style, none);
          border-top-width: var(--menu-item-active-border-top-width, 2px);
          border-top-color: transparent;
          border-right-style: var(--menu-item-active-border-right-style, none);
          border-right-width: var(--menu-item-active-border-right-width, 2px);
          border-right-color: transparent;
          border-bottom-style: var(--menu-item-active-border-bottom-style, none);
          border-bottom-width: var(--menu-item-active-border-bottom-width, 2px);
          border-bottom-color: transparent;
          border-left-style: var(--menu-item-active-border-left-style, none);
          border-left-width: var(--menu-item-active-border-left-width, 2px);
          border-left-color: transparent;
          border-radius: var(--menu-item-active-border-radius, 8px);

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            background: #dbeafe;
          }

          // line样式：竖线
          &[data-indicator="line"]::before,
          &::before {
            width: 3px;
            height: 20px;
            border-radius: 0 2px 2px 0;
          }

          // dot样式：圆点
          &[data-indicator="dot"]::before {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            left: 8px;
          }

          // arrow样式：箭头
          &[data-indicator="arrow"]::before {
            width: 0;
            height: 0;
            border-style: solid;
            border-width: 6px 0 6px 6px;
            border-color: transparent transparent transparent var(--menu-active-text-color, #409EFF);
            background: transparent;
            left: 0;
          }

          // block样式：方块
          &[data-indicator="block"]::before {
            width: 4px;
            height: 16px;
            border-radius: 2px;
            left: 4px;
          }

          // none样式：无指示器
          &[data-indicator="none"]::before {
            display: none;
          }

          :deep(.el-icon) {
            color: var(--menu-active-text-color, #409EFF);
          }

          span {
            color: #ffffff;
            font-size: var(--menu-item-font-size, 14px);
            font-weight: var(--active-menu-item-font-weight, 600);
          }
        }
      }

      // 子菜单
      :deep(.el-sub-menu) {
        margin: var(--menu-item-margin, 0 0 4px 0);

        .el-sub-menu__title {
          height: 42px;
          line-height: 42px;
          border-radius: var(--menu-item-border-radius, 8px);
          padding: 0 14px;
          background: var(--menu-item-bg, transparent);
          transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
          border-top-style: var(--menu-item-border-top-style, none);
          border-top-width: var(--menu-item-border-top-width, 1px);
          border-top-color: var(--menu-item-border-top-color, transparent);
          border-right-style: var(--menu-item-border-right-style, none);
          border-right-width: var(--menu-item-border-right-width, 1px);
          border-right-color: var(--menu-item-border-right-color, transparent);
          border-bottom-style: var(--menu-item-border-bottom-style, none);
          border-bottom-width: var(--menu-item-border-bottom-width, 1px);
          border-bottom-color: var(--menu-item-border-bottom-color, transparent);
          border-left-style: var(--menu-item-border-left-style, none);
          border-left-width: var(--menu-item-border-left-width, 1px);
          border-left-color: var(--menu-item-border-left-color, transparent);

          .el-icon {
            color: #bfdbfe;
            font-size: 16px;
            transition: color 0.25s ease;
          }

          span {
            color: #e0f2fe;
            font-weight: var(--menu-item-font-weight, 500);
            font-size: var(--menu-item-font-size, 14px);
            transition: color 0.25s ease;
          }

          .el-sub-menu__icon-arrow {
            color: #bfdbfe;
            transition: color 0.25s ease;
            transform: rotate(-90deg);
            transition: transform 0.25s ease, color 0.25s ease;
          }

          &:hover {
            background: rgba(125, 211, 252, 0.16);

            .el-icon,
            span,
            .el-sub-menu__icon-arrow {
              color: #ffffff;
            }
          }
        }

        // 展开状态
        &.is-opened {
          .el-sub-menu__title {
            .el-icon,
            span {
              color: #ffffff;
            }

            .el-sub-menu__icon-arrow {
              transform: rotate(0deg);
            }
          }
        }

        .el-menu {
          background: transparent;
          padding: 4px 0;
          margin: 0;
        }

        .el-menu-item {
          height: 36px;
          line-height: 36px;
          border-radius: var(--sub-menu-item-border-radius, 6px);
          padding: 0 16px 0 48px !important;
          margin: var(--sub-menu-item-margin, 0 8px 2px 8px);
          background: var(--sub-menu-item-bg, transparent);
          transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;
          border-top-style: var(--sub-menu-item-border-top-style, none);
          border-top-width: var(--sub-menu-item-border-top-width, 1px);
          border-top-color: var(--sub-menu-item-border-top-color, transparent);
          border-right-style: var(--sub-menu-item-border-right-style, none);
          border-right-width: var(--sub-menu-item-border-right-width, 1px);
          border-right-color: var(--sub-menu-item-border-right-color, transparent);
          border-bottom-style: var(--sub-menu-item-border-bottom-style, none);
          border-bottom-width: var(--sub-menu-item-border-bottom-width, 1px);
          border-bottom-color: var(--sub-menu-item-border-bottom-color, transparent);
          border-left-style: var(--sub-menu-item-border-left-style, none);
          border-left-width: var(--sub-menu-item-border-left-width, 1px);
          border-left-color: var(--sub-menu-item-border-left-color, transparent);

          &::before {
            content: '';
            position: absolute;
            left: 30px;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background: var(--sub-menu-text-color, #c0c4cc);
            transition: all 0.25s ease;
          }

          // none样式：无指示器时不显示默认小圆点
          &[data-indicator="none"]::before {
            display: none;
          }

          span {
            color: #e0f2fe;
            font-size: var(--sub-menu-item-font-size, 13px);
            font-weight: var(--sub-menu-item-font-weight, 500);
            transition: color 0.25s ease;
          }

          &:hover {
            background: rgba(125, 211, 252, 0.16);

            &::before {
              background: #7dd3fc;
              transform: translateY(-50%) scale(1.5);
            }

            span {
              color: #ffffff;
            }
          }

          // none样式：hover状态下也不显示
          &[data-indicator="none"]:hover::before {
            display: none;
          }

          &.is-active {
            background: linear-gradient(135deg, #38bdf8, #2563eb);
            border-top-style: var(--sub-menu-item-active-border-top-style, none);
            border-top-width: var(--sub-menu-item-active-border-top-width, 2px);
            border-top-color: var(--sub-menu-item-active-border-top-color, #409EFF);
            border-right-style: var(--sub-menu-item-active-border-right-style, none);
            border-right-width: var(--sub-menu-item-active-border-right-width, 2px);
            border-right-color: var(--sub-menu-item-active-border-right-color, #409EFF);
            border-bottom-style: var(--sub-menu-item-active-border-bottom-style, none);
            border-bottom-width: var(--sub-menu-item-active-border-bottom-width, 2px);
            border-bottom-color: var(--sub-menu-item-active-border-bottom-color, #409EFF);
            border-left-style: var(--sub-menu-item-active-border-left-style, none);
            border-left-width: var(--sub-menu-item-active-border-left-width, 2px);
            border-left-color: var(--sub-menu-item-active-border-left-color, #409EFF);
            border-radius: var(--sub-menu-item-active-border-radius, 6px);

            &::before {
              background: var(--sub-menu-active-text-color, #409EFF);
            }

            // line样式：竖线（默认）
            &[data-indicator="line"]::before,
            &::before {
              width: 3px;
              height: 16px;
              border-radius: 2px;
              left: 30px;
              transform: translateY(-50%);
            }

            // dot样式：圆点（默认）
            &[data-indicator="dot"]::before {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              left: 32px;
            }

            // horizontal样式：横线
            &[data-indicator="horizontal"]::before {
              width: 12px;
              height: 6px;
              border-radius: 6px;
              left: 32px;
            }

            // arrow样式：箭头
            &[data-indicator="arrow"]::before {
              width: 0;
              height: 0;
              border-style: solid;
              border-width: 5px 0 5px 5px;
              border-color: transparent transparent transparent var(--sub-menu-active-text-color, #409EFF);
              background: transparent;
              left: 28px;
            }

            // block样式：方块
            &[data-indicator="block"]::before {
              width: 12px;
              height: 12px;
              border-radius: 2px;
              left: 28px;
            }

            // none样式：无指示器
            &[data-indicator="none"]::before {
              display: none;
            }

            span {
              color: #ffffff;
              font-weight: var(--active-sub-menu-item-font-weight, 600);
            }
          }
        }
      }
    }
  }

  .aside-footer {
    flex-shrink: 0;
    height: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    color: rgba(191, 219, 254, 0.95);
    border-top: 1px solid rgba(147, 197, 253, 0.24);
    background: rgba(15, 23, 42, 0.14);
    letter-spacing: 0.1px;
  }
}

/* 强制箭头方向：收起=横向，展开=向下 */
:deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  transform: rotate(-90deg) !important;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  transform: rotate(0deg) !important;
}
</style>
