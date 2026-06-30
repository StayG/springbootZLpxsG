import { createRouter, createWebHashHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
const routes = [
  {
    path: "/",
    redirect: "/index/home",
  },
  {
    path: "/index",
    name: "/",
    component: () => import("@/views/index.vue"),
    children: [
      {
        path: "home",
        component: () => import("@/views/home/home.vue"),
        meta: { breadcrumb: ['首页'] }
      },
      {
        path: "/personal-center",
        component: () => import("@/views/personal-center.vue"),
        meta: { breadcrumb: ['个人中心'] }
      },
      {
        path: "/reset-password",
        component: () => import("@/views/reset-password.vue"),
        meta: { breadcrumb: ['修改密码'] }
      },
      {
        path: "/notices",
        component: () => import("@/views/notices/index.vue"),
        meta: { breadcrumb: ['公告信息'] }
      },
      {
        path: "/subject",
        component: () => import("@/views/subject/index.vue"),
        meta: { breadcrumb: ['科目管理'] }
      },
      {
        path: "/examPaper",
        component: () => import("@/views/examPaper/index.vue"),
        meta: { breadcrumb: ['试卷管理'] }
      },
      {
        path: "/examInfo",
        component: () => import("@/views/examInfo/index.vue"),
        meta: { breadcrumb: ['考试管理'] }
      },
      {
        path: "/examQuestion",
        component: () => import("@/views/examQuestion/index.vue"),
        meta: { breadcrumb: ['试题管理'] }
      },
      {
        path: "/examRecord",
        component: () => import("@/views/examRecord/index.vue"),
        meta: { breadcrumb: ['考试记录'] }
      },
      {
        path: "/gradesStatistics",
        component: () => import("@/views/gradesStatistics/index.vue"),
        meta: { breadcrumb: ['成绩与统计'] }
      },
      {
        path: "/gradesStatisticsDetail",
        component: () => import("@/views/gradesStatistics/detail.vue"),
        meta: { breadcrumb: ['成绩与统计', '查看成绩'] }
      },
      {
        path: "/examGrading",
        component: () => import("@/views/examGrading/index.vue"),
        meta: { breadcrumb: ['阅卷管理'] }
      },
      {
        path: "/examDetails",
        component: () => import("@/views/examDetails/index.vue"),
        meta: { breadcrumb: ['考试记录', '考试详情'] }
      },
      {
        path: "/examGradingDetails",
        component: () => import("@/views/examGradingDetails/index.vue"),
        meta: { breadcrumb: ['阅卷管理', '批改试卷'] }
      },
      {
        path: "/examGradingView",
        component: () => import("@/views/examGradingView/index.vue"),
        meta: { breadcrumb: ['阅卷管理', '阅卷详情'] }
      },
      {
        path: "/examWrongQuestion",
        component: () => import("@/views/examWrongQuestion/index.vue"),
        meta: { breadcrumb: ['错题本'] }
      },
      {
        path: "/examPaperTopic",
        component: () => import("@/views/examPaperTopic/index.vue"),
        meta: { breadcrumb: ['试卷管理', '考题设置'] }
      },
      {
        path: "/users",
        component: () => import("@/views/users/index.vue"),
        meta: { breadcrumb: ['用户管理'] }
      },
      {
        path: "/teachers",
        component: () => import("@/views/teachers/index.vue"),
        meta: { breadcrumb: ['教师管理'] }
      },
      {
        path: "/operationLog",
        component: () => import("@/views/operationLog/index.vue"),
        meta: { breadcrumb: ['系统设置', '操作日志'] }
      },
    ],
  },
  {
    path: "/login",
    component: () => import("@/views/login.vue"),
  },
  {
    path: "/:pathMatch(.*)",
    component: () => import("@/views/404.vue"),
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

// 前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  let token = userStore.getToken();

  // 如果用户访问的是登录页面，直接放行
  if (to.path === "/login") {
    // 如果用户已经登录，跳转到首页
    if (token) {
      next("/index/home");
    } else {
      next();
    }
  } else {
    if (!token) {
      // 如果没有 token，跳转到登录页面
      next("/login");
    } else {
      // 如果有 token，允许访问
      next();
    }
  }
});

export default router;
