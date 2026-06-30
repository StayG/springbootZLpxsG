<template>
  <div class="exam-center-container">
    <h2 class="page-title">考试中心</h2>

    <div class="hero-banner">
      <div class="hero-left">
        <div class="hero-main">诚信考试，从我做起</div>
        <div class="hero-sub">遵守考试规则，维护公平公正，展现真实水平</div>
      </div>
      <div class="hero-right">
        <div class="hero-illustration">
          <div class="illus-card"></div>
          <div class="illus-clip"></div>
          <div class="illus-pen"></div>
          <div class="illus-clock"></div>
        </div>
      </div>
    </div>

    <div class="board">
      <div class="board-header">
        <div class="status-tabs">
          <span :class="{ active: activeTab === 'all' }" @click="handleTabChange('all')">全部考试</span>
          <span :class="{ active: activeTab === 'not_started' }" @click="handleTabChange('not_started')">未开始</span>
          <span :class="{ active: activeTab === 'ongoing' }" @click="handleTabChange('ongoing')">进行中</span>
          <span :class="{ active: activeTab === 'ended' }" @click="handleTabChange('ended')">已结束</span>
        </div>
        <el-select
          v-model="searchParams.kemuTypes"
          placeholder="全部课程"
          clearable
          style="width: 120px"
          @change="handleSubjectChange"
        >
          <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <div class="board-body">
        <div ref="listScrollEl" class="board-list" v-if="tabFilteredList.length > 0">
        <div class="exam-row" v-for="item in tabFilteredList" :key="item.id">
          <div class="row-icon" :class="getIconClass(item.status)">
            <el-icon>
              <component :is="getSubjectIcon(item)" />
            </el-icon>
          </div>
          <div class="row-main">
            <div class="exam-name">{{ item.examName }}</div>
            <div class="meta-grid">
              <div class="meta-item">
                <el-icon class="meta-icon"><Clock /></el-icon>
                <span class="meta-label">考试时间：</span>
                <span class="meta-value">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon class="meta-icon"><Reading /></el-icon>
                <span class="meta-label">考试课程：</span>
                <span class="meta-value">{{ item.kemuValue || "待定" }}</span>
              </div>
              <div class="meta-item">
                <el-icon class="meta-icon"><Clock /></el-icon>
                <span class="meta-label">考试时长：</span>
                <span class="meta-value">{{ item.duration || "-" }}分钟</span>
              </div>
              <div class="meta-item">
                <el-icon class="meta-icon"><User /></el-icon>
                <span class="meta-label">发布教师：</span>
                <span class="meta-value">{{ getTeacherText(item) }}</span>
              </div>
              <div class="meta-item">
                <el-icon class="meta-icon"><DataLine /></el-icon>
                <span class="meta-label">试卷总分：</span>
                <span class="meta-value">{{ item.examPaperScore || "-" }}分</span>
              </div>
              <div v-if="shouldShowRetake(item)" class="meta-item">
                <el-icon class="meta-icon"><EditPen /></el-icon>
                <span class="meta-label">重考：</span>
                <span class="meta-value">{{ getRetakeText(item) }}</span>
              </div>
            </div>
          </div>
          <div class="row-action">
            <!-- 按钮现在依赖动态计算的 actionType，会随 nowTs 实时更新 -->
            <div class="state-tag" :class="getStatusClass(item)">{{ getStatusText(item) }}</div>
            <div class="tip-text" :class="getStatusTextClass(item)" v-if="showStatusCountdown(item)">{{ getStatusTip(item) }}</div>
            <div class="countdown" :class="getStatusTextClass(item)" v-if="showStatusCountdown(item)">
              {{ getStatusCountdown(item) }}
            </div>
            <div class="status-note" :class="getStatusTextClass(item)" v-else>{{ getStatusNote(item) }}</div>

            <el-button
              v-if="getDynamicActionType(item) === 'enter_exam' || getDynamicActionType(item) === 'resume_exam' || getDynamicActionType(item) === 'retake_exam'"
              type="primary"
              class="action-btn primary"
              @click="handleEnterExam(item)"
            >
              {{ getDynamicActionText(item) }}
            </el-button>
            <el-button
              v-else-if="getDynamicActionType(item) === 'view_answer_sheet'"
              class="action-btn"
              @click="handleViewAnswerSheet(item)"
            >
              {{ getDynamicActionText(item) }}
            </el-button>
            <el-button
              v-else-if="getDynamicActionType(item) === 'view_result'"
              class="action-btn"
              @click="handleViewResult(item)"
            >
              {{ getDynamicActionText(item) }}
            </el-button>
            <el-button v-else disabled class="action-btn">
              {{ getDynamicActionText(item) }}
            </el-button>
          </div>
        </div>
      </div>

        <div v-else class="empty-state">
          <div class="empty-text">暂无可参加的考试</div>
        </div>

        <div class="pagination-section" v-if="dataList.length > 0">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="searchParams.page"
            v-model:page-size="searchParams.limit"
            :total="total"
            layout="prev, pager, next, jumper"
            background
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Reading, Monitor, EditPen, DataLine, Clock, User } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { getKemuOptions } from "@/utils/dictionary.js";
import { formatCountdownHmsFromDiff, computeStudentAnswerDeadlineMs } from "@/utils/examTime.js";

const router = useRouter();
const listScrollEl = ref(null);
const dataList = ref([]);
const total = ref(0);
const kemuOptions = ref([]);
const activeTab = ref("all");
const nowTs = ref(Date.now());
let timer = null;

// 获取字典选项
const getDictOptions = async () => {
  try {
    const [kemuList] = await Promise.all([getKemuOptions()]);
    kemuOptions.value = kemuList;
  } catch (error) {
    console.error('获取字典选项失败:', error);
  }
};

// 搜索参数
const searchParams = reactive({
  page: 1,
  limit: 5,
  examName: "",
  kemuTypes: null,
  status: null,
});

const tabToStatusParam = (tab) => {
  if (tab === "not_started") return 0;
  if (tab === "ongoing") return 1;
  if (tab === "ended") return 2;
  return null;
};

// 加载数据列表
const loadDataList = async () => {
  try {
    const { data } = await request.post("/examInfo/studentPage", searchParams);
    dataList.value = data.list || [];
    total.value = data.total || 0;
  } catch (error) {
    console.error("加载考试列表失败:", error);
  }
};

const scrollListToTop = async () => {
  await nextTick();
  const el = listScrollEl.value;
  if (el) el.scrollTop = 0;
};

const handleTabChange = async (tab) => {
  activeTab.value = tab;
  searchParams.page = 1;
  searchParams.status = tabToStatusParam(tab);
  await loadDataList();
  scrollListToTop();
};

// 进入考试
const handleEnterExam = async (item) => {
  console.log('=== 进入考试调试信息 ===');
  console.log('考试信息:', item);
  console.log('examPaperId:', item.examPaperId);
  console.log('考试ID:', item.id);
  
  // 检查必要参数
  if (!item.examPaperId) {
    ElMessage.error('获取试卷ID失败，请刷新页面重试');
    console.error('examPaperId 为空:', item);
    return;
  }

  // 到达考试结束时间后一律不可再进入/重考（仅可查看答卷或成绩）
  if (isItemExamEnded(item)) {
    ElMessage.warning("考试已结束，无法进入");
    return;
  }

  const actionType = getDynamicActionType(item);
  if (!(actionType === "enter_exam" || actionType === "resume_exam" || actionType === "retake_exam")) {
    ElMessage.warning("当前状态不可进入考试");
    return;
  }
  
  try {
    // 1. 仅断点续考豁免密码校验；重新考试应视为新一次作答
    const isResume = actionType === 'resume_exam';
    
    if (!isResume) {
      const checkRes = await request.get(`/examInfo/info/${item.id}`);
      if (checkRes.data?.examPassword) {
        const { value } = await ElMessageBox.prompt('请输入考试密码', '密码校验', {
          inputPattern: /\S+/,
          inputErrorMessage: '密码不能为空'
        });
        // 临时存储密码，供答题页刷新时断点续考使用
        sessionStorage.setItem(`exam_pwd_${item.id}`, value);
        
        // 2. 携带密码进入考试
        const { data } = await request.get(`/examInfo/enter/${item.id}`, {
          params: { password: value }
        });
        handleEnterSuccess(data, item);
        return;
      }
    }
    
    // 3. 无需密码或断点续考直接进入
    const { data } = await request.get(`/examInfo/enter/${item.id}`);
    handleEnterSuccess(data, item);
  } catch (error) {
    if (error !== 'cancel') {
      console.error("进入考试失败:", error);
      ElMessage.error(error.message || '进入考试失败');
    }
  }
};

const handleEnterSuccess = (data, item) => {
  let examRecordUuid = null;
  if (typeof data === 'object' && data.examRecordUuid) {
    examRecordUuid = data.examRecordUuid;
  } else {
    examRecordUuid = data;
  }
  
  router.push({ 
    name: 'examPaperExam', 
    query: { 
      examInfoId: item.id,
      examPaperId: item.examPaperId,
      examRecordUuid: examRecordUuid 
    } 
  });
};

// 查看成绩
const handleViewResult = (item) => {
  if (item.examRecordUuidNumber) {
    router.push({ 
      path: '/index/examRecord',
      query: { uuid: item.examRecordUuidNumber }
    });
  }
};

const hasPendingReview = (item) =>
  Number(item?.pendingReviewCount ?? item?.pending_review_count) > 0;

// 待出分：仅查看答卷（与我的成绩、个人中心一致）
const handleViewAnswerSheet = (item) => {
  if (item.examRecordUuidNumber) {
    router.push({
      path: '/index/examRecord',
      query: { uuid: item.examRecordUuidNumber, view: 'answerSheet' }
    });
  }
};

// 搜索
const handleSearch = () => {
  searchParams.page = 1;
  loadDataList();
};

// 当前页码改变
const handleCurrentChange = async (page) => {
  searchParams.page = page;
  await loadDataList();
  scrollListToTop();
};

// 科目筛选改变
const handleSubjectChange = async () => {
  searchParams.page = 1;
  await loadDataList();
  scrollListToTop();
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ').substring(0, 16);
};

// 状态文本
const getStatusText = (item) => {
  const scene = getStatusScene(item);
  if (scene === "retake_allowed") return "可重考";
  const map = {
    not_started: "未开始",
    ongoing: "进行中",
    ended: "已结束",
    finished: "已完成",
    forced: "强制交卷",
  };
  return map[scene] || "未知";
};

// 状态样式类
const getStatusClass = (item) => {
  const scene = getStatusScene(item);
  if (scene === "retake_allowed") return "status-active";
  const map = {
    not_started: "status-pending",
    ongoing: "status-active",
    ended: "status-ended",
    finished: "status-finished",
    forced: "status-forced",
    retake_allowed: "status-finished",
  };
  return map[scene] || "";
};

const getIconClass = (status) => {
  const map = { 0: 'icon-pending', 1: 'icon-active', 2: 'icon-ended', 3: 'icon-ended' };
  return map[status] || 'icon-pending';
};

const getSubjectIcon = (item) => {
  const subjectText = `${item.kemuValue || ""}${item.examName || ""}`.toLowerCase();
  if (subjectText.includes("英语") || subjectText.includes("english")) return Reading;
  if (subjectText.includes("计算机") || subjectText.includes("软件") || subjectText.includes("程序") || subjectText.includes("代码")) return Monitor;
  if (subjectText.includes("数学") || subjectText.includes("统计") || subjectText.includes("物理")) return DataLine;
  if (subjectText.includes("思想") || subjectText.includes("政治") || subjectText.includes("语文") || subjectText.includes("写作")) return EditPen;
  return Document;
};

const getTimeText = (item, scene) => {
  const now = nowTs.value;
  const start = item.startTime ? new Date(item.startTime).getTime() : 0;
  const end = item.endTime ? new Date(item.endTime).getTime() : 0;
  if (scene === "not_started") {
    const diff = start - now;
    if (!diff || diff <= 0) return "0天0时0分";
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    return `${days}天${hours}时${minutes}分`;
  }
  // 进行中：与答题页 enter 返回的 endTimestamp 规则一致（含断点续考：按记录 insertTime + 单次时长上限）
  const recordStatus = getExamRecordStatus(item);
  const recStart = parseTs(item.examRecordInsertTime ?? item.exam_record_insert_time);
  const inProgressSession = recordStatus === 0 || item.actionType === "resume_exam";
  const deadlineMs = computeStudentAnswerDeadlineMs({
    nowMs: now,
    endMs: end,
    durationMin: toNumberOrNull(item.duration),
    recordStartMs: inProgressSession && recStart != null ? recStart : null,
  });
  const diff = deadlineMs - now;
  if (!diff || diff <= 0) return "00 : 00 : 00";
  return formatCountdownHmsFromDiff(diff);
};

const getTeacherText = (item) => {
  return item.publishTeacherName || item.teacherName || item.createByName || item.nickname || "管理员";
};

const isTruthyRetake = (val) => {
  if (val === true || val === 1 || val === "1") return true;
  const text = String(val ?? "").toLowerCase();
  return text === "true" || text === "y" || text === "yes";
};

const toNumberOrNull = (val) => {
  const num = Number(val);
  return Number.isFinite(num) ? num : null;
};

const getRetakeMaxCount = (item) => {
  return toNumberOrNull(item.max_retake_count ?? item.maxRetakeCount);
};

/** 显式「已用重考次数」字段；不含 attemptCount（总次数与已用重考槽位语义不同，见 getRetakeRemainCount） */
const getRetakeUsedCount = (item) => {
  return toNumberOrNull(
    item.retake_used_count ??
    item.used_retake_count ??
    item.retakeUsedCount ??
    item.usedRetakeCount ??
    item.usedCount ??
    item.retake_count ??
    item.retakeCount ??
    item.retakeTimes ??
    item.redoCount
  );
};

/** 与后端 ExamInfoServiceImpl 一致：remaining = max(0, maxRetake - max(0, attemptCount - 1)) */
const getRetakeRemainCount = (item) => {
  const directRemain = toNumberOrNull(
    item.retake_remain_count ??
    item.remain_retake_count ??
    item.remainingRetakeCount ??
    item.retakeRemainCount ??
    item.remainRetakeCount
  );
  if (directRemain !== null) return directRemain;
  const max = getRetakeMaxCount(item);
  const attempts = toNumberOrNull(item.attemptCount);
  if (max !== null && attempts !== null) {
    const usedRetakeSlots = Math.max(0, attempts - 1);
    return Math.max(0, max - usedRetakeSlots);
  }
  const used = getRetakeUsedCount(item);
  if (max !== null && used !== null) return Math.max(max - used, 0);
  return null;
};

/** 考试配置是否允许重考（仅用于结束时间之前的场景判断） */
const allowRetakeByExamConfig = (item) =>
  item.allowRetake === 1 ||
  item.allow_retake === 1 ||
  isTruthyRetake(item.allowRetake);

const hasRetakeRemainPositive = (item) => {
  const n = getRetakeRemainCount(item);
  return n !== null && n > 0;
};

const isItemExamEnded = (item) => {
  const endTime = item.endTime ? new Date(item.endTime).getTime() : 0;
  return endTime > 0 && nowTs.value >= endTime;
};

const shouldShowRetake = (item) => {
  const max = getRetakeMaxCount(item);
  return max !== null && max > 0;
};

const getRetakeText = (item) => {
  const max = getRetakeMaxCount(item);
  const remain = getRetakeRemainCount(item);
  return `最多${max !== null ? max : "-"}次，剩余${remain !== null ? remain : "-"}次`;
};

const getExamRecordStatus = (item) => {
  return toNumberOrNull(item.examRecordStatus ?? item.recordStatus ?? item.exam_record_status);
};

const getStatusScene = (item) => {
  const now = nowTs.value;
  const startTime = item.startTime ? new Date(item.startTime).getTime() : 0;
  const endTime = item.endTime ? new Date(item.endTime).getTime() : 0;
  const isExamEnded = endTime > 0 && now >= endTime;
  const recordStatus = getExamRecordStatus(item);
  const backendActionType = item.actionType; // 后端返回的actionType作为参考
  
  // 优先级1：考试已过期（到达结束时间）— 即使有重考剩余次数也不再允许重考，与后端列表态一致
  if (isExamEnded) {
    if (item.examRecordUuidNumber || recordStatus !== null) {
      return "finished";
    }
    return "ended";
  }
  
  // 优先级2：根据考试记录状态判断（窗口内：已交卷/出分但若仍有重考次数 → retake_allowed）
  if (recordStatus === 2) {
    if (allowRetakeByExamConfig(item) && hasRetakeRemainPositive(item)) return 'retake_allowed';
    return 'forced';
  }
  if (recordStatus === 3) {
    if (allowRetakeByExamConfig(item) && hasRetakeRemainPositive(item)) return 'retake_allowed';
    return 'finished';
  }
  if (recordStatus === 1) {
    if (allowRetakeByExamConfig(item) && hasRetakeRemainPositive(item)) return 'retake_allowed';
    return 'finished';
  }
  if (recordStatus === 0) return 'ongoing'; // 考试中
  
  // 优先级3：根据后端actionType判断（作为补充）
  if (backendActionType === 'view_answer_sheet' || backendActionType === 'view_result') return 'finished';
  if (backendActionType === 'ended') return 'ended';
  if (backendActionType === 'not_started') return 'not_started';
  if (backendActionType === 'retake_exam') return 'retake_allowed';
  if (backendActionType === 'enter_exam' || backendActionType === 'resume_exam') return 'ongoing';
  
  // 优先级4：当后端未提供明确状态时，按时间自动推导（保证排序/筛选“先看状态”正确）
  if (startTime > 0 && now < startTime) return "not_started";
  if (startTime > 0 && now >= startTime) {
    // endTime 为空时，也视为已经开始
    if (!(endTime > 0 && now >= endTime)) return "ongoing";
  }

  // 默认：未开始
  return "not_started";
};

const getStatusGroup = (item) => {
  const scene = getStatusScene(item);
  if (scene === "not_started") return "not_started";
  if (scene === "ongoing") return "ongoing";
  if (scene === "retake_allowed") return "ongoing";
  return "ended";
};

const parseTs = (v) => {
  if (!v) return null;
  const ts = new Date(v).getTime();
  return Number.isFinite(ts) ? ts : null;
};

const sortedList = computed(() => {
  // 需求：先按状态（进行中→待开始→已结束），再按开始时间倒序
  const order = { ongoing: 0, not_started: 1, ended: 2 };
  const list = Array.isArray(dataList.value) ? [...dataList.value] : [];
  return list.sort((a, b) => {
    const ga = getStatusGroup(a);
    const gb = getStatusGroup(b);
    const diffGroup = (order[ga] ?? 99) - (order[gb] ?? 99);
    if (diffGroup !== 0) return diffGroup;

    const aStart = parseTs(a.startTime);
    const bStart = parseTs(b.startTime);

    // 同组内：按开始时间倒序（越晚越靠前）
    if (aStart !== null && bStart !== null) return bStart - aStart;
    if (aStart !== null) return -1;
    if (bStart !== null) return 1;
    // 兜底：保持稳定（再按 id 倒序）
    const aId = Number(a?.id);
    const bId = Number(b?.id);
    if (Number.isFinite(aId) && Number.isFinite(bId)) return bId - aId;
    return 0;
  });
});

// 新增：动态获取按钮的 actionType（用于按钮渲染）
const getDynamicActionType = (item) => {
  const scene = getStatusScene(item);
  if (scene === "not_started") return "not_started";
  if (scene === "ended") return "ended";
  if (scene === "finished" || scene === "forced") {
    return hasPendingReview(item) ? "view_answer_sheet" : "view_result";
  }
  if (scene === "retake_allowed") return "retake_exam";
  if (scene === "ongoing") {
    if (item?.actionType === "retake_exam") return "retake_exam";
    if (item?.actionType === "resume_exam" || item.examRecordUuidNumber) return "resume_exam";
    return "enter_exam";
  }
  return "not_started";
};

// 新增：动态获取按钮文本
const getDynamicActionText = (item) => {
  const actionType = getDynamicActionType(item);
  const map = {
    not_started: '未开始',
    enter_exam: '进入考试',
    resume_exam: '继续考试',
    retake_exam: '重新考试',
    ended: '已结束',
    view_answer_sheet: '查看答卷',
    view_result: '查看成绩'
  };
  return map[actionType] || '查看详情';
};

const getStatusTip = (item) => {
  const scene = getStatusScene(item);
  if (scene === "retake_allowed") return "剩余时间";
  const map = {
    not_started: "距离开始还有",
    ongoing: "剩余时间",
    ended: "考试已结束",
    finished: "考试已完成",
    forced: "考试已强制交卷",
  };
  return map[getStatusScene(item)];
};

const showStatusCountdown = (item) => {
  const scene = getStatusScene(item);
  return scene === "not_started" || scene === "ongoing" || scene === "retake_allowed";
};

const getStatusCountdown = (item) => {
  const scene = getStatusScene(item);
  if (scene === "not_started") return getTimeText(item, "not_started");
  if (scene === "ongoing" || scene === "retake_allowed") {
    return getTimeText(item, "ongoing");
  }
  return "";
};

const getStatusNote = (item) => getStatusTip(item);

const getStatusTextClass = (item) => {
  const scene = getStatusScene(item);
  if (scene === "retake_allowed") return "text-active";
  const map = {
    not_started: "text-pending",
    ongoing: "text-active",
    ended: "text-ended",
    finished: "text-finished",
    forced: "text-forced",
  };
  return map[scene] || "text-pending";
};

const tabFilteredList = computed(() => {
  const base = sortedList.value;
  if (activeTab.value === "all") return base;
  if (activeTab.value === "not_started") return base.filter((i) => getStatusGroup(i) === "not_started");
  if (activeTab.value === "ongoing") return base.filter((i) => getStatusGroup(i) === "ongoing");
  if (activeTab.value === "ended") return base.filter((i) => getStatusGroup(i) === "ended");
  return base;
});

onMounted(() => {
  getDictOptions();
  loadDataList();
  timer = setInterval(() => {
    nowTs.value = Date.now();
  }, 1000);
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
</script>

<style lang="scss" scoped>
.exam-center-container {
  width: 100%;
  height: 100%;
  min-height: 0;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-title {
  flex-shrink: 0;
  margin: 0 0 14px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #0f172a;
}

.hero-banner {
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  padding: 22px 28px;
  margin-bottom: 14px;
  background: #4f46e5;
  border: 1px solid rgba(255, 255, 255, 0.18);
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #ffffff;
}

.hero-banner::before,
.hero-banner::after {
  content: "";
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  pointer-events: none;
}

.hero-banner::before {
  width: 260px;
  height: 260px;
  right: 180px;
  top: -130px;
}

.hero-banner::after {
  width: 380px;
  height: 380px;
  right: -120px;
  top: -130px;
}

.hero-glow {
  display: none;
}

.hero-glow.glow-a {
  width: 180px;
  height: 180px;
  right: 280px;
  top: 0;
}

.hero-glow.glow-b {
  width: 240px;
  height: 240px;
  right: 40px;
  bottom: -120px;
}

.hero-main { font-size: 32px; font-weight: 700; margin-bottom: 10px; position: relative; z-index: 1; color: #ffffff; }
.hero-sub { font-size: 14px; opacity: 0.92; position: relative; z-index: 1; color: rgba(255, 255, 255, 0.92); }
.hero-right { position: relative; z-index: 1; padding-right: 6px; }

.hero-illustration {
  position: relative;
  width: 210px;
  height: 120px;
}

.hero-illustration .illus-card {
  position: absolute;
  right: 18px;
  top: 10px;
  width: 86px;
  height: 92px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 22px rgba(15, 23, 42, 0.16);
}

.hero-illustration .illus-clip {
  position: absolute;
  right: 44px;
  top: 4px;
  width: 34px;
  height: 8px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.85);
}

.hero-illustration .illus-pen {
  position: absolute;
  right: 6px;
  top: 54px;
  width: 46px;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  transform: rotate(-24deg);
  box-shadow: 0 8px 14px rgba(15, 23, 42, 0.12);
}

.hero-illustration .illus-clock {
  position: absolute;
  right: 92px;
  bottom: 8px;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.board {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  padding: 12px 16px 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
}

.board-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.board-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 4px 12px;
  border-bottom: 1px solid #eff3f8;
}

.status-tabs {
  display: flex;
  gap: 28px;
  font-size: 14px;
  color: #8b93a5;
  font-weight: 500;
}

.status-tabs span {
  cursor: pointer;
  position: relative;
  padding: 4px 0;
}

.status-tabs span.active {
  color: #4f46e5;
  font-weight: 600;
}

.status-tabs span.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -13px;
  height: 3px;
  border-radius: 2px;
  background: #4f46e5;
}

.board-list {
  flex: 1;
  min-height: 0;
  margin-top: 12px;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge legacy */

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
    display: none; /* Chrome, Safari, Edge */
  }
}

.exam-row {
  border: 1px solid #e5eaf4;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 16px;
  align-items: stretch;
  margin-bottom: 12px;
  transition: all 0.24s ease;

  &:hover {
    border-color: #c7d2fe;
    box-shadow: 0 10px 18px rgba(79, 70, 229, 0.1);
    transform: translateY(-2px);
  }
}

.row-icon {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  flex-shrink: 0;
  align-self: center;
}

.row-icon :deep(svg) {
  font-size: 34px;
}

.icon-pending { background: #eef2ff; color: #4f46e5; }
.icon-active { background: #ecfdf3; color: #16a34a; }
.icon-ended { background: #fff7ed; color: #ea580c; }

.row-main {
  flex: 1;
  min-width: 0;
  padding-right: 12px;
}
.exam-name { font-size: 24px; font-weight: 700; margin-bottom: 10px; color: #1e293b; line-height: 1.25; }
.meta-grid {
  display: grid;
  grid-template-columns: minmax(460px, 1.15fr) minmax(300px, 0.85fr);
  column-gap: 28px;
  row-gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  color: #707987;
  font-size: 13px;
  line-height: 1.6;
}

.meta-icon {
  color: #99a2b1;
  font-size: 14px;
  flex-shrink: 0;
}

.meta-label {
  color: #7f8899;
  flex-shrink: 0;
}

.meta-value {
  color: #4f5869;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-action {
  width: 240px;
  min-width: 240px;
  border-left: 1px solid #edf1f7;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.state-tag {
  border-radius: 4px;
  padding: 5px 14px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}
.state-tag.status-pending { background: #eef2f7; color: #7f8da3; }
.state-tag.status-active { background: #eef2ff; color: #4f46e5; }
.state-tag.status-ended { background: #fff7ed; color: #ea580c; }
.state-tag.status-finished { background: #e8f8ef; color: #22b573; }
.state-tag.status-forced { background: #fdebec; color: #e34d59; }

.tip-text { font-size: 14px; margin-bottom: 10px; }
.countdown { font-size: 30px; line-height: 1; font-weight: 700; margin-bottom: 16px; letter-spacing: 1px; white-space: nowrap; }
.status-note { font-size: 14px; margin-bottom: 16px; }

.text-pending { color: #7f8da3; }
.text-active { color: #4f46e5; }
.text-ended { color: #ea580c; }
.text-finished { color: #22b573; }
.text-forced { color: #e34d59; }

.action-btn {
  width: 136px;
  height: 40px;
  border-radius: 6px;
  font-size: 15px;
}

.action-btn.primary {
  background: #4f46e5;
  border-color: #4f46e5;
}

.empty-state {
  flex: 1;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
}
.empty-text { color: #8b94a3; font-size: 14px; }

.pagination-section {
  flex-shrink: 0;
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 4px;
  border-top: 1px solid #eff3f8;
}

.total-text { color: #8b94a3; font-size: 13px; }

:deep(.el-select .el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #e7ecf2 inset;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  width: 26px;
  height: 26px;
  line-height: 26px;
  min-width: 26px;
  border-radius: 4px;
  margin: 0 3px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #4f46e5;
  color: #fff;
}

:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #4f46e5;
}
</style>
