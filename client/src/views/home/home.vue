<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="hero-banner">
      <span class="hero-glow glow-a"></span>
      <span class="hero-glow glow-b"></span>
      <div class="hero-left">
        <div class="hero-main">欢迎回来，{{ userName }}！</div>
        <div class="hero-sub">认真备考，诚信应考，取得优异成绩！</div>
      </div>
      <div class="hero-right">
        <div class="hero-float-card card-a"></div>
        <div class="hero-float-card card-b"></div>
        <div class="hero-illustration">
          <div class="illustration-clipboard">
            <div class="clipboard-header"></div>
            <div class="clipboard-line" v-for="i in 4" :key="i"></div>
          </div>
          <div class="illustration-books">
            <div class="book book-1"></div>
            <div class="book book-2"></div>
          </div>
          <div class="illustration-plant">
            <div class="plant-pot"></div>
            <div class="plant-leaf"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="router.push('/index/examCenter')">
        <div class="stat-left">
          <div class="stat-icon" style="background: #4f8efc;">
            <el-icon :size="24">
              <Document />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待参加考试</div>
            <div class="stat-value">{{ stats.pendingCount }}<span class="stat-unit">场</span></div>
          </div>
        </div>
        <div class="stat-link">
          <span>点击查看</span>
          <el-icon>
            <ArrowRight />
          </el-icon>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/index/studentExamRecord')">
        <div class="stat-left">
          <div class="stat-icon" style="background: #52c41a;">
            <el-icon :size="24">
              <CircleCheck />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">已完成考试</div>
            <div class="stat-value">{{ stats.completedCount }}<span class="stat-unit">场</span></div>
          </div>
        </div>
        <div class="stat-link">
          <span>点击查看</span>
          <el-icon>
            <ArrowRight />
          </el-icon>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/index/wrongQuestion')">
        <div class="stat-left">
          <div class="stat-icon" style="background: #fa8c16;">
            <el-icon :size="24">
              <TrendCharts />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">掌握程度</div>
            <div class="stat-value">{{ stats.masteryPercent }}<span class="stat-unit">%</span></div>
          </div>
        </div>
        <div class="stat-link">
          <span>点击查看</span>
          <el-icon>
            <ArrowRight />
          </el-icon>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/index/studentExamRecord')">
        <div class="stat-left">
          <div class="stat-icon" style="background: #722ed1;">
            <el-icon :size="24">
              <Star />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">平均成绩</div>
            <div class="stat-value">{{ stats.avgScore }}<span class="stat-unit">分</span></div>
          </div>
        </div>
        <div class="stat-link">
          <span>点击查看</span>
          <el-icon>
            <ArrowRight />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 待参加考试 -->
    <div class="section-header">
      <h3 class="section-title">待参加考试</h3>
      <router-link to="/index/examCenter" class="view-all">查看全部 <el-icon>
          <ArrowRight />
        </el-icon></router-link>
    </div>
    <div class="board">
      <div class="exam-list" v-if="upcomingExams.length > 0">
        <div class="exam-item" v-for="item in upcomingExams" :key="item.id">
          <div class="exam-icon" :class="getExamIconClass(item)">
            <el-icon :size="28">
              <component :is="getExamIcon(item)" />
            </el-icon>
          </div>
          <div class="exam-info">
            <div class="exam-name">{{ item.examName }}</div>
            <div class="exam-meta">
              <span><el-icon>
                  <Clock />
                </el-icon> 考试时间：{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
            </div>
            <div class="exam-meta">
              <span><el-icon>
                  <Timer />
                </el-icon> 考试时长：{{ item.duration || "-" }}分钟</span>
              <span class="meta-divider">|</span>
              <span><el-icon>
                  <Coin />
                </el-icon> 总分：{{ item.examPaperScore || "-" }}分</span>
            </div>
          </div>
          <div class="exam-action">
            <div class="status-tag" :class="getStatusClass(item)">{{ getStatusText(item) }}</div>
            <el-button v-if="getActionType(item) === 'enter_exam'" type="primary" class="enter-btn"
              @click="handleEnterExam(item)">
              进入考试
            </el-button>
            <el-button v-else disabled class="enter-btn">
              {{ getActionText(item) }}
            </el-button>
            <div class="countdown-text" v-if="showCountdown(item)">
              距开始 {{ getCountdown(item) }}
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-text">暂无待参加的考试</div>
      </div>
    </div>

    <!-- 学习建议 -->
    <div class="section-header">
      <h3 class="section-title">学习建议</h3>
    </div>
    <div class="tips-grid">
      <div class="tip-card" style="background: #f0f5ff;">
        <div class="tip-icon" style="color: #4f8efc;">
          <el-icon :size="32">
            <Calendar />
          </el-icon>
        </div>
        <div class="tip-title">制定学习计划</div>
        <div class="tip-desc">合理安排时间，制定学习计划，有效提高学习效率。</div>
      </div>
      <div class="tip-card" style="background: #f6ffed;">
        <div class="tip-icon" style="color: #52c41a;">
          <el-icon :size="32">
            <Reading />
          </el-icon>
        </div>
        <div class="tip-title">多做练习题</div>
        <div class="tip-desc">通过大量练习，巩固知识点，熟悉考试题型。</div>
      </div>
      <div class="tip-card" style="background: #fff7e6;">
        <div class="tip-icon" style="color: #fa8c16;">
          <el-icon :size="32">
            <Notebook />
          </el-icon>
        </div>
        <div class="tip-title">错题及时复习</div>
        <div class="tip-desc">整理错题，定期复习，避免同样错误再次发生。</div>
      </div>
      <div class="tip-card" style="background: #f9f0ff;">
        <div class="tip-icon" style="color: #722ed1;">
          <el-icon :size="32">
            <CircleCheck />
          </el-icon>
        </div>
        <div class="tip-title">诚信考试</div>
        <div class="tip-desc">遵守考试规则，诚信应考，展现真实水平。</div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Document,
  CircleCheck,
  TrendCharts,
  Star,
  ArrowRight,
  Clock,
  Timer,
  Coin,
  Calendar,
  Reading,
  Notebook,
  EditPen,
  DataLine
} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const nowTs = ref(Date.now());
let timer = null;

const userName = ref("张同学");
const userStore = useUserStore();
const stats = reactive({
  pendingCount: 0,
  completedCount: 0,
  /** 错题掌握度：已掌握条数 / 错题总数，无错题时为 100（与错题集统计一致） */
  masteryPercent: 100,
  avgScore: "0"
});
const upcomingExams = ref([]);

const syncUserName = () => {
  const info = userStore.getUserInfo?.() || userStore.userInfo || {};
  const raw = String(info.nickname || info.realName || info.name || info.userName || info.username || "").trim();
  if (!raw) {
    userName.value = "同学";
    return;
  }
  // 已包含称呼则直接用（如“张同学”）
  if (raw.includes("同学")) {
    userName.value = raw;
    return;
  }
  // 中文姓名：只取姓（首字）+ 同学，例如“张三” -> “张同学”
  if (/^[\u4e00-\u9fa5]{2,4}$/.test(raw)) {
    userName.value = `${raw.slice(0, 1)}同学`;
    return;
  }
  // 其他：取首个词 + 同学
  const first = raw.split(/\s+/).filter(Boolean)[0] || "";
  userName.value = first ? `${first}同学` : "同学";
};

const isExamAttemptFinished = (row) => [1, 2, 3].includes(Number(row?.status));

/** 成绩已发布（可展示分数），与「我的成绩」页一致 */
const isScorePublishedRecord = (row) =>
  isExamAttemptFinished(row) && !(Number(row?.pendingReviewCount) > 0);

const getRecordTotalScore = (row) => {
  const v = row?.totalScore ?? row?.total_score;
  if (v === null || v === undefined || v === "") return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
};

// 加载统计数据（待参加仍按考试安排；已完成 / 平均成绩按个人考试记录，与「我的成绩」一致）
const loadStats = async () => {
  try {
    const { data } = await request.post("/examInfo/studentPage", { page: 1, limit: 100 });
    const list = data.list || [];
    const now = Date.now();

    let pending = 0;

    list.forEach((item) => {
      const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
      if (startTime > now) pending++;
    });

    stats.pendingCount = pending;
  } catch (error) {
    console.error("加载待考统计失败:", error);
  }

  try {
    const { data } = await request.post("/examRecord/page", { page: 1, limit: 2000, onlyLatest: 1 });
    const records = data?.list || [];
    stats.completedCount = records.filter(isExamAttemptFinished).length;

    const publishedScores = records
      .filter(isScorePublishedRecord)
      .map(getRecordTotalScore)
      .filter((n) => n !== null);
    if (publishedScores.length > 0) {
      const avg = publishedScores.reduce((a, b) => a + b, 0) / publishedScores.length;
      stats.avgScore = String(Math.round(avg * 10) / 10);
    } else {
      stats.avgScore = "0";
    }
  } catch (error) {
    console.error("加载已完成考试场数失败:", error);
    stats.completedCount = 0;
    stats.avgScore = "0";
  }
};

/** 首页「待参加考试」区块默认展示的条数（在「未开始」的考试中按开始时间升序取前 N 条） */
const HOME_UPCOMING_EXAMS_DISPLAY_LIMIT = 2;

const WRONG_PAGE_LIMIT = 500;

/** 与错题集页一致：拉取个人错题并计算掌握度百分比 */
const loadWrongMasteryPercent = async () => {
  try {
    const usersId = userStore.userInfo?.id;
    let pageNum = 1;
    const merged = [];
    while (pageNum < 500) {
      const { data } = await request.post("/examWrongQuestion/page", {
        page: pageNum,
        limit: WRONG_PAGE_LIMIT,
        usersId,
      });
      const batch = data?.list || [];
      merged.push(...batch);
      const totalRows = data?.total != null ? Number(data.total) : merged.length;
      if (batch.length < WRONG_PAGE_LIMIT || merged.length >= totalRows) break;
      pageNum += 1;
    }
    let mastered = 0;
    merged.forEach((item) => {
      const v = item?.masteryStatus;
      if (v == null || v === "") return;
      const n = Number(v);
      if (Number.isFinite(n) && n === 1) mastered += 1;
    });
    const total = merged.length;
    stats.masteryPercent =
      total === 0 ? 100 : Math.min(100, Math.round((mastered / total) * 100));
  } catch (error) {
    console.error("加载错题掌握度失败:", error);
    stats.masteryPercent = 0;
  }
};

// 加载待参加考试
const loadUpcomingExams = async () => {
  try {
    const { data } = await request.post("/examInfo/studentPage", { page: 1, limit: 10 });
    const list = data.list || [];
    const now = Date.now();

    upcomingExams.value = list
      .filter((item) => {
        const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
        return startTime > now;
      })
      .sort((a, b) => {
        const ta = a.startTime ? new Date(a.startTime).getTime() : 0;
        const tb = b.startTime ? new Date(b.startTime).getTime() : 0;
        return ta - tb;
      })
      .slice(0, HOME_UPCOMING_EXAMS_DISPLAY_LIMIT);
  } catch (error) {
    console.error("加载待参加考试失败:", error);
  }
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return "";
  return timeStr.replace("T", " ").substring(0, 16);
};

// 获取考试图标
const getExamIcon = (item) => {
  const subjectText = `${item.kemuValue || ""}${item.examName || ""}`.toLowerCase();
  if (subjectText.includes("英语") || subjectText.includes("english")) return Reading;
  if (subjectText.includes("计算机") || subjectText.includes("软件")) return DataLine;
  if (subjectText.includes("数学") || subjectText.includes("统计")) return DataLine;
  return EditPen;
};

// 获取图标样式类
const getExamIconClass = (item) => {
  const subjectText = `${item.kemuValue || ""}${item.examName || ""}`.toLowerCase();
  if (subjectText.includes("英语")) return "icon-blue";
  if (subjectText.includes("数学")) return "icon-green";
  return "icon-blue";
};

// 获取状态文本
const getStatusText = (item) => {
  const now = nowTs.value;
  const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
  const endTime = item.endTime ? new Date(item.endTime).getTime() : 0;

  if (startTime > now) return "未开始";
  if (endTime > 0 && now > endTime) return "已结束";
  return "进行中";
};

// 获取状态样式类
const getStatusClass = (item) => {
  const text = getStatusText(item);
  if (text === "未开始") return "tag-pending";
  if (text === "进行中") return "tag-active";
  return "tag-ended";
};

// 获取操作类型
const getActionType = (item) => {
  const now = nowTs.value;
  const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
  const endTime = item.endTime ? new Date(item.endTime).getTime() : 0;

  if (startTime > now) return "not_started";
  if (endTime > 0 && now > endTime) return "ended";
  return "enter_exam";
};

// 获取操作文本
const getActionText = (item) => {
  const type = getActionType(item);
  if (type === "not_started") return "未开始";
  if (type === "ended") return "已结束";
  return "进入考试";
};

// 是否显示倒计时
const showCountdown = (item) => {
  return getActionType(item) === "not_started";
};

// 获取倒计时
const getCountdown = (item) => {
  const now = nowTs.value;
  const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
  const diff = startTime - now;

  if (diff <= 0) return "0天0小时";
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  return `${days}天${hours}小时`;
};

// 进入考试
const handleEnterExam = async (item) => {
  if (!item.examPaperId) {
    ElMessage.error("获取试卷ID失败");
    return;
  }

  try {
    const { data } = await request.get(`/examInfo/enter/${item.id}`);
    let examRecordUuid = data.examRecordUuid || data;
    router.push({
      name: "examPaperExam",
      query: {
        examInfoId: item.id,
        examPaperId: item.examPaperId,
        examRecordUuid
      }
    });
  } catch (error) {
    ElMessage.error(error.message || "进入考试失败");
  }
};

onMounted(() => {
  syncUserName();
  loadStats();
  loadWrongMasteryPercent();
  loadUpcomingExams();
  timer = setInterval(() => {
    nowTs.value = Date.now();
  }, 1000);
});

watch(
  () => userStore.userInfo,
  () => {
    syncUserName();
    loadWrongMasteryPercent();
  },
  { deep: true }
);

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style lang="scss" scoped>
.home-container {
  width: 100%;
  min-height: 100%;
  margin: 0;
  padding: 0;
  --content-inline-gap: 2px;
}

/* 欢迎横幅 */
.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 16px;
  background: #4f46e5;
  border: 1px solid rgba(255, 255, 255, 0.18);
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #ffffff;
  margin-left: var(--content-inline-gap);
  margin-right: var(--content-inline-gap);
}

.hero-banner::before,
.hero-banner::after {
  display: none;
}

.hero-banner::before {
  width: 260px;
  height: 260px;
  right: 220px;
  top: -150px;
}

.hero-banner::after {
  width: 380px;
  height: 380px;
  right: -120px;
  top: -140px;
}

.hero-glow {
  display: none;
}

.hero-glow.glow-a {
  width: 180px;
  height: 180px;
  right: 290px;
  top: 14px;
}

.hero-glow.glow-b {
  width: 240px;
  height: 240px;
  right: 56px;
  bottom: -120px;
}

.hero-left {
  position: relative;
  z-index: 1;
}

.hero-main {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #ffffff;
}

.hero-sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
  margin-top: 15px;
}

.hero-right {
  position: relative;
  z-index: 1;
}

.hero-float-card {
  display: none;
}

.hero-float-card.card-a {
  width: 82px;
  height: 44px;
  top: -14px;
  right: 32px;
  transform: rotate(8deg);
}

.hero-float-card.card-b {
  width: 70px;
  height: 36px;
  right: -8px;
  bottom: -2px;
  transform: rotate(-10deg);
}

.hero-illustration {
  position: relative;
  width: 200px;
  height: 140px;
}

.illustration-clipboard {
  position: absolute;
  right: 40px;
  top: 10px;
  width: 80px;
  height: 100px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px 8px;

  .clipboard-header {
    width: 24px;
    height: 4px;
    background: #4f8efc;
    border-radius: 2px;
    margin: 0 auto 12px;
  }

  .clipboard-line {
    width: 100%;
    height: 3px;
    background: #e8e8e8;
    border-radius: 2px;
    margin-bottom: 8px;

    &:nth-child(3) {
      width: 80%;
    }

    &:nth-child(4) {
      width: 60%;
    }

    &:nth-child(5) {
      width: 90%;
    }
  }
}

.illustration-books {
  position: absolute;
  right: 0;
  bottom: 10px;

  .book {
    width: 50px;
    height: 14px;
    border-radius: 2px;
    margin-bottom: 2px;
  }

  .book-1 {
    background: #4f8efc;
  }

  .book-2 {
    background: #818cf8;
    margin-left: 4px;
  }
}

.illustration-plant {
  position: absolute;
  left: 20px;
  bottom: 20px;

  .plant-pot {
    width: 20px;
    height: 16px;
    background: #5a7ba8;
    border-radius: 2px 2px 4px 4px;
  }

  .plant-leaf {
    width: 12px;
    height: 24px;
    background: #52c41a;
    border-radius: 50% 50% 0 0;
    margin: 0 auto -2px;
  }
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
  margin-left: var(--content-inline-gap);
  margin-right: var(--content-inline-gap);
}

.stat-card {
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
    transform: translateY(-5px);

    .stat-link {
      opacity: 1;
      transform: translateY(0);
      max-height: 28px;
    }
  }
}

.stat-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #8b93a5;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #202733;

  .stat-unit {
    font-size: 13px;
    font-weight: 400;
    color: #8b93a5;
    margin-left: 4px;
  }
}

.stat-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f0f2f5;
  font-size: 13px;
  color: #8b93a5;
  opacity: 0;
  transform: translateY(6px);
  max-height: 0;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    color: #4f46e5;
  }
}

/* 区块标题 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  margin-left: var(--content-inline-gap);
  margin-right: var(--content-inline-gap);
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #202733;
  padding-bottom: 6px;
}

.view-all {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8b93a5;
  text-decoration: none;
  transition: color 0.3s;

  &:hover {
    color: #4f46e5;
  }
}

/* 考试列表容器 */
.board {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  margin-left: var(--content-inline-gap);
  margin-right: var(--content-inline-gap);
}

.empty-state {
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.exam-list {
  display: flex;
  flex-direction: column;
}

.exam-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eff3f8;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-child {
    padding-top: 0;
  }
}

.exam-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.icon-blue {
    background: #ecf5ff;
    color: #4f8efc;
  }

  &.icon-green {
    background: #f0f9eb;
    color: #52c41a;
  }
}

.exam-info {
  flex: 1;
  min-width: 0;
}

.exam-name {
  font-size: 15px;
  font-weight: 600;
  color: #202733;
  margin-bottom: 8px;
}

.exam-meta {
  font-size: 13px;
  color: #8b93a5;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;

  .el-icon {
    margin-right: 4px;
  }

  .meta-divider {
    margin: 0 8px;
    color: #d0d5dd;
  }
}

.exam-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.status-tag {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 4px;

  &.tag-pending {
    background: #ecf5ff;
    color: #4f8efc;
  }

  &.tag-active {
    background: #f0f9eb;
    color: #52c41a;
  }

  &.tag-ended {
    background: #f5f5f5;
    color: #8b93a5;
  }
}

.enter-btn {
  padding: 8px 20px;
  font-size: 14px;
  border-radius: 6px;
}

.countdown-text {
  font-size: 12px;
  color: #fa8c16;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.empty-text {
  font-size: 14px;
  color: #8b93a5;
}

/* 学习建议 */
.tips-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
  margin-left: var(--content-inline-gap);
  margin-right: var(--content-inline-gap);
}

.tip-card {
  border-radius: 14px;
  padding: 24px 20px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid rgba(148, 163, 184, 0.16);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 18px rgba(15, 23, 42, 0.08);
  }
}

.tip-icon {
  margin-bottom: 12px;
  width: 56px;
  height: 56px;
  margin-left: auto;
  margin-right: auto;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.72);
  position: relative;

  &::before {
    content: "";
    position: absolute;
    inset: 8px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.6);
  }

  .el-icon {
    position: relative;
    z-index: 1;
  }
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #202733;
  margin-bottom: 8px;
}

.tip-desc {
  font-size: 12px;
  color: #8b93a5;
  line-height: 1.6;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 20px 0;
  font-size: 13px;
  color: #8b93a5;
  border-top: 1px solid #eef2f7;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tips-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-container {
    --content-inline-gap: 0px;
  }

  .hero-banner {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }

  .hero-right {
    display: none;
  }

  .hero-main {
    font-size: 22px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 22px;
  }

  .exam-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .exam-action {
    flex-direction: row;
    align-items: center;
    width: 100%;
    justify-content: space-between;
  }

  .tips-grid {
    grid-template-columns: 1fr;
  }
}
</style>
