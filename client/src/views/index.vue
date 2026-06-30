<template>
  <div class="student-shell">
    <aside class="sidebar">
      <div class="logo-wrap">
        <div class="logo-icon">
          <el-icon :size="22"><Monitor /></el-icon>
        </div>
        <div class="logo-text">
          <div class="title">在线考试系统</div>
          <div class="sub">学生端</div>
        </div>
      </div>

      <div class="menu-list">
        <div class="menu-item" :class="{ active: isActive('/index/home') }" @click="go('/index/home')"><el-icon><House /></el-icon><span>首页</span></div>
        <div class="menu-item" :class="{ active: isExamSection }" @click="go('/index/examCenter')"><el-icon><EditPen /></el-icon><span>我的考试</span></div>
        <div class="menu-item" :class="{ active: isWrongQuestionSection }" @click="go('/index/wrongQuestion')"><el-icon><Collection /></el-icon><span>错题集</span></div>
        <div class="menu-item" :class="{ active: isExamRecordSection }" @click="go('/index/studentExamRecord')"><el-icon><Histogram /></el-icon><span>我的成绩</span></div>
        <div class="menu-item" :class="{ active: isActive('/index/personal-center') }" @click="go('/index/personal-center')"><el-icon><User /></el-icon><span>个人中心</span></div>
        <div class="menu-item" :class="{ active: isNoticeSection }" @click="go('/index/notices')"><el-icon><Bell /></el-icon><span>通知公告</span></div>
        <div class="menu-item" :class="{ active: isHelpCenterSection }" @click="go('/index/helpCenter')"><el-icon><QuestionFilled /></el-icon><span>帮助中心</span></div>
      </div>

      <div class="sidebar-footer">
        <div class="footer-illus">🖥️</div>
        <div class="footer-copy">© 2026 在线考试系统</div>
        <div class="footer-copy en">All rights reserved.</div>
      </div>
    </aside>

    <section class="main">
      <header class="topbar">
        <div class="crumb">首页 <span>/</span> <strong>{{ currentPageName }}</strong></div>
        <div class="user">
          <el-badge
            :value="unreadNoticeCount"
            :max="99"
            :hidden="unreadNoticeCount === 0"
            class="notice-badge"
            role="button"
            tabindex="0"
            @click="go('/index/notices')"
            @keyup.enter="go('/index/notices')"
          >
            <el-icon class="bell"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-trigger">
              <el-avatar :size="36" :src="avatarUrl">{{ (userName || "学生").slice(0, 1) }}</el-avatar>
              <span class="name">{{ userName || "张同学" }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <span class="user-dropdown-item"><el-icon><User /></el-icon>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <span class="user-dropdown-item"><el-icon><Edit /></el-icon>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <span class="user-dropdown-item danger"><el-icon><SwitchButton /></el-icon>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div
        class="page-content"
        :class="{
          'page-content--exam': isExamPaperRoute,
          'page-content--exam-center': isExamCenterRoute,
          'page-content--answer-sheet': isAnswerSheetRoute,
          'page-content--exam-record': isExamRecordRoute,
          'page-content--wrong-redo': isWrongRedoRoute,
          'page-content--wrong-question': isWrongQuestionListRoute,
          'page-content--notices': isNoticesListRoute,
          'page-content--help-center': isHelpCenterSection,
          'page-content--student-exam-record': isStudentExamRecordRoute,
        }"
      >
        <router-view />
      </div>
    </section>

    <el-dialog
      v-model="modals.changePasswordVisible"
      width="520px"
      class="student-change-pwd-dialog"
      :close-on-click-modal="true"
      :show-close="true"
      destroy-on-close
      @closed="modals.closeChangePassword()"
    >
      <template #header>
        <div class="pwd-header">
          <div class="pwd-title">修改密码</div>
          <div class="pwd-sub">为保障账号安全，请设置高强度密码</div>
        </div>
      </template>

      <div class="pwd-body" :style="{ '--personal-right-theme-color': '#4f46e5' }">
        <reset-password :key="modals.changePasswordKey" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { isNoticeUnread } from "@/utils/studentNoticeRead.js";
import { ElMessage } from "element-plus";
import request, { setLoggingOut } from "@/utils/request";
import storage from "@/utils/storage";
import config from "@/config/config";
import { House, EditPen, Collection, Histogram, User, Bell, QuestionFilled, ArrowDown, Monitor, Edit, SwitchButton } from "@element-plus/icons-vue";
import ResetPassword from "@/views/reset-password/reset-password.vue";
import { useModalsStore } from "@/stores/modals";
import { getHelpArticle } from "@/views/helpCenter/helpArticles.js";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const modals = useModalsStore();
const userName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.userName || "");
const unreadNoticeCount = ref(0);

// 处理头像 URL（添加 baseUrl 前缀）
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar;
  if (!avatar) return '';
  
  // 如果已经是完整的 URL（http 或 https 开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  
  // 如果是相对路径，拼接 baseUrl
  // 确保路径以 / 开头
  const path = avatar.startsWith('/') ? avatar : `/${avatar}`;
  return `${config.baseUrl}${path}`;
});

const isActive = (path) => route.path === path;
const isExamPaperRoute = computed(() => route.path === "/index/examPaperExam");
const isExamCenterRoute = computed(() => route.path === "/index/examCenter");
/** 查看答卷：主区域铺满可视高度，内部滚动 */
const isAnswerSheetRoute = computed(
  () => route.path === "/index/examRecord" && String(route.query.view || "") === "answerSheet"
);
/** 成绩详情 / 查看答卷：主内容区铺满，由页面内部滚动 */
const isExamRecordRoute = computed(() => route.path === "/index/examRecord");
const isExamSection = computed(() => route.path === "/index/examCenter" || isExamPaperRoute.value);
const isExamRecordSection = computed(() => route.path === "/index/studentExamRecord" || route.path === "/index/examRecord");
const isStudentExamRecordRoute = computed(() => route.path === "/index/studentExamRecord");
const isNoticeSection = computed(() => route.path === "/index/notices" || route.path === "/index/noticesDetail");
const isHelpCenterSection = computed(
  () => route.path === "/index/helpCenter" || route.name === "helpArticle"
);
const isWrongRedoRoute = computed(() => route.path === "/index/wrongQuestionRedo");
const isWrongQuestionListRoute = computed(() => route.path === "/index/wrongQuestion");
const isNoticesListRoute = computed(() => route.path === "/index/notices");
const isWrongQuestionSection = computed(() => route.path === "/index/wrongQuestion" || isWrongRedoRoute.value);

const go = (path) => router.push(path);

const loadUnreadNoticeCount = async () => {
  try {
    const uid = userStore.userInfo?.id;
    let total = 0;
    let collected = [];
    let page = 1;
    const pageSize = 200;
    // 分页拉取公告 ID，用于统计未读条数（公告总量通常不大）
    for (;;) {
      const { data } = await request.post("/notices/list", { page, limit: pageSize, title: "" });
      total = data?.total ?? 0;
      const chunk = data?.list || [];
      collected = collected.concat(chunk);
      if (collected.length >= total || chunk.length === 0) break;
      page += 1;
      if (page > 50) break;
    }
    unreadNoticeCount.value = collected.filter((n) => n && isNoticeUnread(uid, n.id)).length;
  } catch (e) {
    unreadNoticeCount.value = 0;
  }
};

const handleUserCommand = async (command) => {
  if (command === "profile") {
    router.push("/index/personal-center");
    return;
  }
  if (command === "password") {
    modals.openChangePassword();
    return;
  }
  if (command === "logout") {
    // 设置退出登录标志，避免其他请求失败时显示错误提示
    setLoggingOut(true);
    
    try {
      const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
      if (tableName) {
        await request.post(`/${tableName}/logout`);
      }
    } catch (e) {
      // ignore backend logout error and continue local cleanup
    } finally {
      userStore.reset();
      ElMessage.success("退出登录成功");
      router.push("/login");
      // 重置标志
      setTimeout(() => setLoggingOut(false), 1000);
    }
  }
};

const crumbText = computed(() => {
  const map = {
    "/index/home": "首页",
    "/index/examCenter": "我的考试",
    "/index/studentExamRecord": "我的成绩",
    "/index/wrongQuestion": "错题集",
    "/index/wrongQuestionRedo": "重做错题",
    "/index/notices": "通知公告",
    "/index/helpCenter": "帮助中心",
    "/index/personal-center": "个人中心",
    "/index/examRecord": "成绩详情",
    "/index/examPaperExam": "在线考试",
  };
  let leaf = map[route.path] || "页面";
  if (route.path === "/index/examRecord" && route.query?.view === "answerSheet") {
    leaf = "查看答卷";
  }
  if (route.name === "helpArticle") {
    const art = getHelpArticle(String(route.params.slug || ""));
    return `首页 / 帮助中心 / ${art?.title || "帮助说明"}`;
  }
  return `首页 / ${leaf}`;
});
const currentPageName = computed(() => crumbText.value.replace("首页 / ", ""));

// 刷新用户信息（解决管理端更新用户信息后，学生端不更新的问题）
const refreshUserInfo = async () => {
  try {
    const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
    if (tableName) {
      const { data } = await request.get(`/${tableName}/session`);
      if (data) {
        userStore.setUserInfo(data);
      }
    }
  } catch (e) {
    // 如果获取失败，保持原有信息
    console.warn('刷新用户信息失败:', e);
  }
};

onMounted(() => {
  loadUnreadNoticeCount();
  // 页面加载时刷新用户信息
  refreshUserInfo();
});

watch(
  () => route.path,
  () => {
    // 从公告页返回/切换页面后刷新一次未读数
    loadUnreadNoticeCount();
    // 切换页面时也刷新用户信息（可选，如果觉得频繁可以去掉）
    if (route.path === '/index/personal-center') {
      refreshUserInfo();
    }
  }
);
</script>

<style lang="scss" scoped>
.student-shell {
  display: flex;
  height: 100vh;
  background: #f8fafc;
  overflow: hidden;
  gap: 0;
  padding: 0;
}
.sidebar {
  width: 240px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid #e7ecf4;
  border-radius: 0;
  box-shadow: none;
  padding: 14px 0 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.logo-wrap { display: flex; flex-direction: column; align-items: center; gap: 8px; margin-bottom: 18px; }
.logo-icon {
  width: 46px; height: 46px; border-radius: 10px; background: #4f46e5; color: #ffffff;
  display: flex; align-items: center; justify-content: center; font-size: 22px;
}
.logo-text { display: flex; flex-direction: column; align-items: center; }
.logo-text .title { font-size: 18px; font-weight: 700; letter-spacing: 0.2px; line-height: 1.2; }
.logo-text .sub { margin-top: 2px; color: #4f46e5; font-size: 12px; background: #eef2ff; border-radius: 999px; padding: 2px 10px; line-height: 18px; }
.menu-list { display: flex; flex-direction: column; gap: 1px; margin-top: 8px; }
.menu-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
.menu-item {
  height: 48px; display: flex; align-items: center; gap: 10px; padding: 0 16px;
  color: #334155; cursor: pointer; font-size: 16px; border-radius: 12px; margin: 2px 10px;
}
.menu-item :deep(svg) { font-size: 18px; color: #64748b; }
.menu-item:hover { background: #f3f6ff; }
.menu-item.active { color: #ffffff; background: #4f46e5; font-weight: 700; position: relative; box-shadow: 0 10px 18px rgba(79, 70, 229, 0.22); }
.menu-item.active :deep(svg) { color: #ffffff; }
.sidebar-footer {
  margin-top: 10px;
  padding: 8px 10px 26px;
  text-align: center;
  color: #a3a8b3;
  flex-shrink: 0;
}
.footer-illus { font-size: 40px; margin-bottom: 6px; color: #7fb1ff; }
.footer-copy { font-size: 12px; line-height: 1.5; }
.footer-copy.en { color: #b8bdc7; }
.main {
  flex: 1;
  height: 100vh;
  padding: 0;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 16px;
  padding: 0 20px;
  margin: 14px 14px 0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}
.crumb { color: #9aa1ad; font-size: 14px; font-weight: 600; }
.crumb span { margin: 0 8px; color: #c2c8d2; }
.crumb :deep(strong) { color: #2a3140; font-weight: 700; }
.user { display: flex; align-items: center; gap: 14px; color: #111827; font-weight: 500; }
.bell { color: #6b7280; }
.notice-badge {
  display: inline-flex;
  margin-right: 10px;
  cursor: pointer;
  padding: 6px;
  border-radius: 10px;
  transition: background 0.2s ease;
}
.notice-badge:hover { background: #eef2ff; }
:deep(.notice-badge .el-badge__content) {
  font-size: 11px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
}
.user-dropdown-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.user-dropdown-item.danger { color: #dc2626; }
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #1f2937;
  outline: none;
}
.user-trigger .name { font-size: 14px; font-weight: 600; }
.user-trigger .arrow { color: #6b7280; font-size: 12px; }
.page-content {
  flex: 1;
  min-height: 0;
  padding: 14px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 我的成绩：内容过长时仍可滚动，隐藏滚动条 */
.page-content--student-exam-record {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.page-content--student-exam-record::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

/* 在线考试：禁止整页滚动，由考试页内部仅答题区滚动 */
.page-content--exam {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

/* 考试中心：禁止主内容区整页下滚，列表在卡片内滚动 + 底部分页 */
.page-content--exam-center {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 错题集列表、通知公告列表：与考试中心相同，页内滚动 */
.page-content--wrong-question,
.page-content--notices {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 帮助中心：铺满主内容区宽高，由页面内部滚动 */
.page-content--help-center {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 查看答卷：占满主内容区宽高，由答卷页内部滚动 */
.page-content--answer-sheet {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

/* 成绩详情（含普通成绩与查看答卷）：主区域不整页外滚，由内层滚动 */
.page-content--exam-record {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.page-content--wrong-redo {
  display: flex;
  flex-direction: column;
  padding: 14px;
  overflow: hidden;
}

/* 全局修改密码弹窗（学生端风格） */
:deep(.student-change-pwd-dialog) {
  border-radius: 14px;
}
:deep(.student-change-pwd-dialog .el-dialog__header) {
  padding: 18px 20px 8px;
  margin-right: 0;
}
:deep(.student-change-pwd-dialog .el-dialog__body) {
  padding: 10px 20px 18px;
}
.pwd-header .pwd-title {
  font-size: 18px;
  font-weight: 800;
  color: #202733;
  line-height: 1.1;
}
.pwd-header .pwd-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #8a8f99;
}
.pwd-body {
  background: #fff;
}
:deep(.student-change-pwd-dialog .el-dialog__headerbtn) {
  top: 14px;
  right: 14px;
}
</style>
