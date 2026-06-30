<template>
  <div class="home-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="greeting">
          <h1>{{ greeting }}，{{ userName }}！</h1>
          <p class="welcome-text">
            <el-icon><Calendar /></el-icon>
            {{ currentDate }} {{ currentTime }}
          </p>
        </div>
        <div class="user-avatar">
          <image-view :src="userAvatar" />
        </div>
      </div>
    </div>

    <!-- 公告区域 -->
    <div class="notice-section">
      <div class="notice-panel">
        <div class="section-header">
          <h3>
            <el-icon><Bell /></el-icon>
            系统公告
          </h3>
          <el-button link type="primary" @click="handleMoreNotices">
            查看更多
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="notice-list">
          <div
            v-if="noticeList.length > 0"
            v-for="notice in noticeList"
            :key="notice.id"
            class="notice-item"
            @click="handleNoticeClick(notice)"
          >
            <div class="notice-dot"></div>
            <div class="notice-content">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-time">
                <el-icon><Clock /></el-icon>
                {{ notice.publishTime }}
              </div>
            </div>
            <el-icon class="notice-arrow"><ArrowRight /></el-icon>
          </div>
          <div v-else class="empty-notice">
            <el-icon :size="64"><DocumentCopy /></el-icon>
            <p>暂无公告信息</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import {
  Calendar,
  Bell,
  Clock,
  ArrowRight,
  DocumentCopy,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import request from "@/utils/request";

const router = useRouter();
const userStore = useUserStore();

// 用户信息
const userName = ref("管理员");
const userAvatar = ref(
  "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
);

// 当前时间
const currentDate = ref("");
const currentTime = ref("");
let timer = null;

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return "夜深了";
  if (hour < 9) return "早上好";
  if (hour < 12) return "上午好";
  if (hour < 14) return "中午好";
  if (hour < 18) return "下午好";
  if (hour < 22) return "晚上好";
  return "夜深了";
});

// 最新公告（从接口获取）
const noticeList = ref([]);

// 获取最新公告
const getNoticeList = () => {
  request
    .post("notices/page", {
      page: 1,
      limit: 8,
    })
    .then(({ data }) => {
      noticeList.value = data.list.map((item) => ({
        id: item.id,
        title: item.title,
        publishTime: item.createTime || item.updateTime,
      }));
    })
    .catch(() => {
      // 如果接口失败，保持为空数组
      noticeList.value = [];
    });
};

// 更新时间
const updateTime = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  const weekDay = [
    "星期日",
    "星期一",
    "星期二",
    "星期三",
    "星期四",
    "星期五",
    "星期六",
  ][now.getDay()];
  const hour = String(now.getHours()).padStart(2, "0");
  const minute = String(now.getMinutes()).padStart(2, "0");
  const second = String(now.getSeconds()).padStart(2, "0");

  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`;
  currentTime.value = `${hour}:${minute}:${second}`;
};

// 公告点击
const handleNoticeClick = (notice) => {
  router.push("/notices");
};

// 查看更多公告
const handleMoreNotices = () => {
  router.push("/notices");
};

// 初始化
const initData = () => {
  // 获取用户信息
  const userInfo = userStore.userInfo || {};
  userName.value = userInfo.userName || userInfo.realName || "管理员";
  userAvatar.value =
    userInfo.avatar ||
    "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

  // 获取公告列表
  getNoticeList();

  // 更新时间
  updateTime();
  timer = setInterval(updateTime, 1000);
};

onMounted(() => {
  initData();
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style lang="scss" scoped>
.home-dashboard {
  padding: 8px;
  min-height: calc(100vh - 140px);

  // 欢迎区域
  .welcome-section {
    max-width: 100%;
    margin: 0 auto 24px;
    background: var(--card-bg, #ffffff);
    border-radius: 14px;
    padding: 32px 40px;
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
    border: 1px solid #ebeef5;
    background: linear-gradient(115deg, #e0ecff 0%, #dbeafe 45%, #c7e7ff 100%);
    color: #0f172a;

    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .greeting {
        h1 {
          margin: 0 0 12px 0;
          font-size: 2rem;
          font-weight: 600;
          color: #0f172a;
        }

        .welcome-text {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 1rem;
          margin: 0;
          color: #334155;

          .el-icon {
            font-size: 18px;
            color: #1890ff;
          }
        }
      }

      .user-avatar {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        border: 3px solid rgba(255, 255, 255, 0.9);
        overflow: hidden;
        box-shadow: 0 8px 20px rgba(59, 130, 246, 0.18);

        :deep(img) {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  // 公告区域
  .notice-section {
    max-width: 100%;
    margin: 0 auto;

    .notice-panel {
      background: var(--card-bg, #ffffff);
      border-radius: 14px;
      padding: 32px;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
      border: 1px solid #ebeef5;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #e2e8f0;

        h3 {
          display: flex;
          align-items: center;
          gap: 10px;
          margin: 0;
          font-size: 1.25rem;
          font-weight: 600;
          color: #0f172a;

          .el-icon {
            font-size: 22px;
            color: #1890ff;
          }
        }
      }

      .notice-list {
        .notice-item {
          display: flex;
          align-items: center;
          gap: 16px;
          padding: 18px 16px;
          border-radius: 10px;
          margin-bottom: 10px;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 1px solid transparent;
          background: #f8fafc;

          &:hover {
            background: #f0f9ff;
            border-color: #91d5ff;
            transform: translateX(4px);

            .notice-dot {
              background: #1890ff;
              transform: scale(1.2);
            }

            .notice-arrow {
              transform: translateX(4px);
              color: #1890ff;
            }
          }

          .notice-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #c0c4cc;
            flex-shrink: 0;
            transition: all 0.3s ease;
          }

          .notice-content {
            flex: 1;
            min-width: 0;

            .notice-title {
              font-size: 15px;
              color: #0f172a;
              margin-bottom: 8px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              font-weight: 500;
            }

            .notice-time {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #909399;

              .el-icon {
                font-size: 13px;
              }
            }
          }

          .notice-arrow {
            color: #c0c4cc;
            font-size: 16px;
            transition: all 0.3s ease;
            flex-shrink: 0;
          }
        }

        .empty-notice {
          text-align: center;
          padding: 80px 20px;
          color: #c0c4cc;

          .el-icon {
            margin-bottom: 16px;
            opacity: 0.5;
          }

          p {
            margin: 0;
            font-size: 15px;
          }
        }
      }
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    padding: 12px;

    .welcome-section {
      padding: 20px;

      .welcome-content {
        .greeting h1 {
          font-size: 1.5rem;
        }

        .user-avatar {
          width: 60px;
          height: 60px;
        }
      }
    }

    .notice-section {
      .notice-panel {
        padding: 20px;

        .notice-list .notice-item {
          padding: 14px 12px;

          .notice-content .notice-title {
            font-size: 14px;
          }
        }
      }
    }
  }
}
</style>
