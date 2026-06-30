import { createRouter, createWebHashHistory } from "vue-router";
import { ElMessageBox } from "element-plus";
import {
  isActiveStrictScreenSwitchSession,
  tryReportExamScreenSwitch,
} from "@/utils/examScreenSwitchSession.js";

const routes = [
  {
    path: "/",
    redirect: "/index/home",
  },
  {
    path: "/index",
    component: () => import("@/views/index.vue"),
    children: [
      {
        path: "home",
        name: "home",
        component: () => import("@/views/home/home.vue"),
      },
      {
        path: "personal-center",
        name: "personalCenter",
        component: () => import("@/views/personal-center/personal-center.vue"),
      },
      {
        path: "notices",
        name: "notices",
        component: () => import("@/views/notices/list.vue"),
      },
      {
        path: "noticesDetail",
        name: "noticesDetail",
        component: () => import("@/views/notices/detail.vue"),
      },
      {
        path: "helpCenter",
        name: "helpCenter",
        component: () => import("@/views/helpCenter/index.vue"),
      },
      {
        path: "helpArticle/:slug",
        name: "helpArticle",
        component: () => import("@/views/helpCenter/detail.vue"),
      },
      {
        path: "examCenter",
        name: "examCenter",
        component: () => import("@/views/examCenter/index.vue"),
      },
      {
        path: "examPaperExam",
        name: "examPaperExam",
        component: () => import("@/views/examPaper/exam.vue"),
      },
      {
        path: "examRecord",
        name: "examRecord",
        component: () => import("@/views/examRecord/list.vue"),
      },
      {
        path: "studentExamRecord",
        name: "studentExamRecord",
        component: () => import("@/views/studentExamRecord/index.vue"),
      },
      {
        path: "wrongQuestion",
        name: "wrongQuestion",
        component: () => import("@/views/wrongQuestion/index.vue"),
      },
      {
        path: "wrongQuestionRedo",
        name: "wrongQuestionRedo",
        component: () => import("@/views/wrongQuestionRedo/index.vue"),
      },
    ],
  },
  {
    path: "/login",
    component: () => import("@/views/login.vue"),
  },
  {
    path: "/register",
    component: () => import("@/views/register.vue"),
  },
  {
    path: "/reset-password-page",
    component: () => import("@/views/reset-password-page.vue"),
  },
  {
    path: "/:pathMatch(.*)",
    component: () => import("@/views/404.vue"),
  },
];

function isExamPaperExamRoute(route) {
  return (
    route?.name === "examPaperExam" ||
    route?.path === "/index/examPaperExam" ||
    (typeof route?.path === "string" && route.path.includes("examPaperExam"))
  );
}

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 延迟执行，确保DOM更新完成
    return new Promise((resolve) => {
      setTimeout(() => {
        // 查找app-container并滚动到顶部
        const appContainer = document.getElementById("app-container");
        if (appContainer) {
          appContainer.scrollTo({
            top: 0,
            behavior: "smooth",
          });
        } else {
          // 如果没有找到容器，使用默认的window滚动
          window.scrollTo({
            top: 0,
            behavior: "smooth",
          });
        }
        resolve();
      }, 100);
    });
  },
});

/**
 * 系统内从考试页跳转到非考试页：先确认，用户仍要离开则计一次切屏并上报（与 visibility / 退出全屏共用去抖）
 */
router.beforeEach(async (to, from, next) => {
  // 检查用户登录状态
  const userDataStr = localStorage.getItem('frontUser');
  let hasValidToken = false;
  
  if (userDataStr) {
    try {
      const userData = JSON.parse(userDataStr);
      hasValidToken = !!(userData && userData.token);
    } catch (e) {
      console.error('解析用户数据失败:', e);
    }
  }
  
  const isLoginPage = to.path === '/login' || to.path === '/register' || to.path === '/reset-password-page';
  
  // 如果没有有效token且不是登录相关页面，跳转到登录页
  if (!hasValidToken && !isLoginPage) {
    console.log('未登录，跳转到登录页');
    return next('/login');
  }
  
  // 如果有token且是登录页面，跳转到首页
  if (hasValidToken && isLoginPage) {
    return next('/index/home');
  }
  
  if (
    isExamPaperExamRoute(from) &&
    !isExamPaperExamRoute(to) &&
    isActiveStrictScreenSwitchSession()
  ) {
    try {
      await ElMessageBox.confirm(
        "离开考试页面将计为一次切屏并可能被限制考试，是否确认离开？",
        "离开考试",
        {
          confirmButtonText: "确认离开",
          cancelButtonText: "继续考试",
          type: "warning",
          distinguishCancelAndClose: true,
        }
      );
    } catch {
      return next(false);
    }
    await tryReportExamScreenSwitch("route");
  }
  next();
});

export default router;
