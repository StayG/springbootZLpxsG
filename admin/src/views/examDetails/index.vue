<template>
  <div class="module-container">
    <template v-if="isDetailMode">
      <div class="page-head">
        <div class="page-title">考试记录详情</div>
        <el-button class="back-btn" plain @click="backToList">
          <span class="back-icon">←</span>
          <span>返回</span>
        </el-button>
      </div>

      <section class="card-section">
        <div class="section-head">考试基本信息</div>
        <div class="basic-grid">
          <div class="item"><span class="k">考试名称：</span><span class="v">{{ headerInfo.examName || "-" }}</span></div>
          <div class="item"><span class="k">试卷总分：</span><span class="v">{{ paperFullScoreDisplay }} 分</span></div>
          <div class="item"><span class="k">考生姓名：</span><span class="v">{{ headerInfo.nickname || "-" }}</span></div>
          <div class="item"><span class="k">考试时间：</span><span class="v">{{ headerInfo.examTime || "-" }}</span></div>
          <div class="item"><span class="k">及格分数：</span><span class="v">{{ headerInfo.passingScoreText }}</span></div>
          <div class="item"><span class="k">考生账号：</span><span class="v">{{ headerInfo.userName || "-" }}</span></div>
          <div class="item status-row">
            <span class="k">考生状态：</span>
            <el-tag :type="statusTag.type" effect="light">{{ statusTag.text }}</el-tag>
          </div>
        </div>
      </section>

      <section class="card-section">
        <div class="section-head">答题情况</div>
        <div class="exam-result-card">
          <div class="result-grid">
            <div class="result-item">
              <div class="result-label">卷面总分</div>
              <div class="result-value blue">{{ paperFullScoreDisplay }}分</div>
            </div>
            <div class="result-item">
              <div class="result-label">考生得分</div>
              <div class="result-score-row">
                <div class="result-value green">{{ studentScore }}分</div>
                <el-tag :type="passStatusType" effect="light">{{ passStatusText }}</el-tag>
              </div>
            </div>
            <div class="result-item">
              <div class="result-label">得分率</div>
              <div class="result-value orange">{{ scoreRateDisplay }}</div>
            </div>
            <div class="result-item">
              <div class="result-label">用时</div>
              <div class="result-value purple">{{ durationText }}</div>
            </div>
            <div class="result-item">
              <div class="result-label">排名</div>
              <div class="result-value cyan">{{ rankDisplayText }}</div>
            </div>
          </div>
        </div>
        <div class="table-wrap">
          <table class="summary-table">
            <thead>
              <tr>
                <th>题型</th>
                <th v-for="s in typeStats" :key="s.type">{{ s.label }}</th>
                <th>总题数</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>题目数量</td>
                <td v-for="s in typeStats" :key="`q-${s.type}`">{{ summaryCellQuestionCount(s) }}</td>
                <td>{{ summaryTotalQuestionCountDisplay }}</td>
              </tr>
              <tr>
                <td>答对数量</td>
                <td v-for="s in typeStats" :key="`c-${s.type}`">{{ summaryCellCorrectCount(s) }}</td>
                <td>{{ summaryTotalCorrectCountDisplay }}</td>
              </tr>
              <tr>
                <td>得分情况</td>
                <td v-for="s in typeStats" :key="`s-${s.type}`">{{ summaryCellScoreText(s) }}</td>
                <td>{{ summaryTotalScoreTextDisplay }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card-section">
        <div class="section-head">考试行为记录</div>
        <div class="timeline">
          <div v-for="(item, idx) in behaviorTimeline" :key="idx" class="line-item">
            <div class="dot" :class="item.type" />
            <div class="content">
              <div class="title">{{ item.title }}</div>
              <div class="desc">{{ item.desc }}</div>
            </div>
            <div class="time">{{ item.time }}</div>
          </div>
        </div>
      </section>

      <section class="card-section">
        <div class="section-head">答题详情</div>
        
        <!-- 题型筛选标签 -->
        <div class="tabs">
          <div class="tab" :class="{ active: activeTab === 'all' }" @click="switchTab('all')">全部题目</div>
          <div class="tab" :class="{ active: activeTab === '1' }" @click="switchTab('1')">单选题</div>
          <div class="tab" :class="{ active: activeTab === '2' }" @click="switchTab('2')">多选题</div>
          <div class="tab" :class="{ active: activeTab === '3' }" @click="switchTab('3')">判断题</div>
          <div class="tab" :class="{ active: activeTab === '4' }" @click="switchTab('4')">填空题</div>
          <div class="tab" :class="{ active: activeTab === '5' }" @click="switchTab('5')">简答题</div>
        </div>

        <!-- 按题型分组的题目列表 -->
        <div class="exam-paper" v-if="groupedQuestions.length">
          <div v-for="(group, groupIdx) in groupedQuestions" :key="group.type" class="question-group">
            <!-- 分组标题 -->
            <div class="group-header">
              <span class="group-title">{{ getChineseNumber(group.globalTypeOrder) }}、{{ typeLabel(group.type) }}（总分{{ group.fullScore }}分）</span>
            </div>

            <!-- 题目列表 -->
            <div class="questions-list">
              <div v-for="(row, idx) in group.questions" :key="row.id || row.displayNo" class="question-item">
                <!-- 题号、题干和状态标签 -->
                <div class="question-stem">
                  <span class="question-number">{{ getGroupQuestionNumber(groupIdx, idx) }}.</span>
                  <div class="question-text rich-text-content" v-html="formatRichText(getQuestionStem(row) || '未命名试题')" />
                  <el-tag 
                    v-if="getAnswerStatus(row)" 
                    :type="getAnswerStatusType(row)" 
                    effect="light" 
                    size="small"
                    class="status-tag"
                  >
                    {{ getAnswerStatus(row) }}
                  </el-tag>
                </div>

                <!-- 选项（选择题） -->
                <div v-if="shouldShowOptions(row) && row.parsedOptions.length" class="options-list">
                  <div v-for="(opt, optIdx) in row.parsedOptions" :key="`${row.id}-opt-${optIdx}`" class="option-row">
                    <span class="option-key">{{ opt.key }}.</span>
                    <span class="option-content">{{ opt.text }}</span>
                  </div>
                </div>

                <!-- 答案和得分信息 -->
                <div class="answer-info-section">
                  <div class="info-row">
                    <span class="info-label">考生答案：</span>
                    <span class="info-value student-answer">{{ row.examDetailsMyanswer || "未作答" }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">正确答案：</span>
                    <span class="info-value correct-answer">{{ row.examQuestionAnswer || "-" }}</span>
                  </div>
                  <div class="info-row score-row">
                    <span class="info-label">得分：</span>
                    <span class="info-value score-value">{{ getQuestionScore(row) }} / {{ row.examPaperTopicNumber || 0 }}</span>
                  </div>
                </div>

                <!-- 解析 -->
                <div v-if="row.examQuestionAnalysis" class="analysis-section">
                  <span class="analysis-label">解析：</span>
                  <div class="analysis-content rich-text-content" v-html="formatRichText(row.examQuestionAnalysis)" />
                </div>

                <!-- 教师评语（仅简答题且已批阅时显示） -->
                <div v-if="showTeacherComment(row)" class="teacher-comment-section">
                  <span class="comment-label">教师评语：</span>
                  <span class="comment-content">{{ row.teacherComment }}</span>
                </div>

                <!-- 分隔线（最后一题除外） -->
                <div v-if="idx < group.questions.length - 1" class="question-divider"></div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无题目数据" />
      </section>
    </template>

    <template v-else>
      <div class="fallback-card">
        <el-empty description="请从考试记录进入详情页" />
        <el-button type="primary" @click="backToList">返回</el-button>
      </div>
    </template>

    <el-backtop v-if="isDetailMode" target=".content-main .el-scrollbar__wrap" :right="28" :bottom="96" />
  </div>
</template>

<script setup>
import { computed, onBeforeMount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import { scrollAdminLayoutToTop } from "@/utils/adminScrollLayout.js";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();

const examRecordId = route.query.examRecordId;
const examDetailsUuidNumber = route.query.examDetailsUuidNumber;
const isDetailMode = computed(() => !!examRecordId || !!examDetailsUuidNumber);

const detailsList = ref([]);
const paperFullScore = ref(null);
const examEndTimeText = ref("");
const examStartTimeText = ref("");
const recordStatus = ref(null);
const recordTotalScore = ref(null);
const passingScore = ref(null);
const examInfoId = ref(null);
const examInfoStartTime = ref("");
const examInfoEndTime = ref("");
const screenSwitchCount = ref(0);
/** 每次切屏的时间字符串列表（后端 JSON），与 screenSwitchCount 新数据一致 */
const screenSwitchTimes = ref([]);
const rank = ref(null);

// 题型筛选
const activeTab = ref("all");

const detailsSorted = computed(() => {
  const list = Array.isArray(detailsList.value) ? detailsList.value.slice() : [];
  // 后端已经按 exam_paper_topic_sequence ASC 排序，前端直接使用
  return list.map((item, index) => ({ ...item, displayNo: index + 1 }));
});

const paperFullScoreDisplay = computed(() => {
  const v = Number(paperFullScore.value);
  if (paperFullScore.value === null || paperFullScore.value === undefined) return 0;
  return Number.isNaN(v) ? 0 : v;
});

const studentScore = computed(() => {
  if (detailsSorted.value.length) {
    return detailsSorted.value.reduce((sum, q) => {
      const isSubjective = Number(q?.examQuestionTypes) === 5;
      const score = isSubjective ? Number(q?.teacherScore ?? q?.examDetailsMyscore ?? 0) : Number(q?.examDetailsMyscore ?? 0);
      return sum + (Number.isNaN(score) ? 0 : score);
    }, 0);
  }
  if (recordTotalScore.value !== null && recordTotalScore.value !== undefined) return Number(recordTotalScore.value);
  const routeScore = Number(route.query.totalScore);
  if (!Number.isNaN(routeScore)) return routeScore;
  return 0;
});

const scoreRate = computed(() => {
  const full = Number(paperFullScoreDisplay.value || 0);
  if (!full) return 0;
  return Number(((Number(studentScore.value) / full) * 100).toFixed(0));
});

/** 已提交(1)、强制交卷(2) 不展示得分率数值 */
const scoreRateDisplay = computed(() => {
  const st = Number(recordStatus.value);
  if (st === 1 || st === 2) return "--";
  return `${scoreRate.value}%`;
});

const rankText = computed(() => {
  if (rank.value === null || rank.value === undefined) return "-";
  return `第 ${rank.value} 名`;
});

/** 已提交、强制交卷不展示排名 */
const rankDisplayText = computed(() => {
  const st = Number(recordStatus.value);
  if (st === 1 || st === 2) return "--";
  return rankText.value;
});

const formatToMinute = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  const h = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${d} ${h}:${min}`;
};

const headerInfo = computed(() => {
  const first = detailsSorted.value[0] || {};
  const start = formatToMinute(examInfoStartTime.value);
  const end = formatToMinute(examInfoEndTime.value);
  return {
    examName: route.query.examName || first.examName || first.examPaperName,
    nickname: route.query.nickname || first.nickname,
    userName: route.query.userName || first.userName || first.user_name,
    examTime: start && end ? `${start}—${end}` : "-",
    passingScore: passingScore.value ?? "-",
    passingScoreText: Number.isFinite(Number(passingScore.value)) ? `${Number(passingScore.value)}分` : "-",
    endTime: examEndTimeText.value || route.query.endTime || route.query.insertTime || first.endTime || first.insertTime,
  };
});

const statusTag = computed(() => {
  const s = Number(recordStatus.value);
  if (s === 3) return { text: "已完成", type: "success" };
  if (s === 2) return { text: "强制交卷", type: "warning" };
  if (s === 1) return { text: "已提交", type: "warning" };
  if (s === 0) return { text: "考试中", type: "info" };
  return { text: "未知", type: "info" };
});

const pendingSubjectiveCount = computed(() =>
  detailsSorted.value.filter((q) => Number(q?.examQuestionTypes) === 5 && Number(q?.reviewStatus ?? 0) === 0).length
);

const passStatusType = computed(() => {
  if (pendingSubjectiveCount.value > 0) return "warning";
  const pass = Number(passingScore.value);
  if (!Number.isFinite(pass) || pass <= 0) return "info";
  return Number(studentScore.value) >= pass ? "success" : "danger";
});

const passStatusText = computed(() => {
  if (pendingSubjectiveCount.value > 0) return "待批阅";
  const pass = Number(passingScore.value);
  if (!Number.isFinite(pass) || pass <= 0) return "待判定";
  return Number(studentScore.value) >= pass ? "及格" : "不及格";
});

const toTime = (value) => {
  if (!value) return null;
  const t = new Date(value).getTime();
  return Number.isNaN(t) ? null : t;
};

const durationText = computed(() => {
  const start = toTime(examStartTimeText.value);
  const end = toTime(examEndTimeText.value);
  if (!start || !end || end <= start) return "-";
  const totalSeconds = Math.floor((end - start) / 1000);
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${m}分${s}秒`;
});

const typeLabel = (type) => {
  const t = Number(type);
  if (t === 1) return "单选题";
  if (t === 2) return "多选题";
  if (t === 3) return "判断题";
  if (t === 4) return "填空题";
  if (t === 5) return "简答题";
  return "其他";
};

const calcCorrectCount = (q) => {
  const ans = String(q.examDetailsMyanswer || "").trim();
  const right = String(q.examQuestionAnswer || "").trim();
  if (!ans || !right) return 0;
  
  // 标准化答案：移除空格、逗号，转大写，然后排序字符
  const normalizeAnswer = (str) => {
    return str.replaceAll(" ", "").replaceAll(",", "").toUpperCase().split("").sort().join("");
  };
  
  return normalizeAnswer(ans) === normalizeAnswer(right) ? 1 : 0;
};

const typeStats = computed(() => {
  const types = [1, 2, 3, 4, 5];
  return types.map((t) => {
    const rows = detailsSorted.value.filter((q) => Number(q.examQuestionTypes) === t);
    return {
      type: t,
      label: typeLabel(t),
      count: rows.length,
      correctCount: rows.reduce((sum, q) => sum + calcCorrectCount(q), 0),
      score: rows.reduce((sum, q) => sum + Number(q.examDetailsMyscore || 0), 0),
      fullScore: rows.reduce((sum, q) => sum + Number(q.examPaperTopicNumber || 0), 0),
    };
  });
});

const totalQuestionCount = computed(() => typeStats.value.reduce((sum, s) => sum + s.count, 0));
const totalCorrectCount = computed(() => typeStats.value.reduce((sum, s) => sum + s.correctCount, 0));

/** 仍有简答题未批阅：简答题仅隐藏答对/得分；总题数始终显示题量，答对/得分先不含简答题 */
const gradingIncomplete = computed(() => pendingSubjectiveCount.value > 0);

/** 客观题（不含简答题）汇总，用于阅卷中总题数列的答对、得分 */
const objectiveTotals = computed(() =>
  typeStats.value
    .filter((s) => Number(s.type) !== 5)
    .reduce(
      (acc, s) => ({
        correctCount: acc.correctCount + s.correctCount,
        score: acc.score + Number(s.score || 0),
        fullScore: acc.fullScore + Number(s.fullScore || 0),
      }),
      { correctCount: 0, score: 0, fullScore: 0 }
    )
);

const summaryCellQuestionCount = (s) => s.count;

const summaryCellCorrectCount = (s) => {
  if (gradingIncomplete.value && Number(s?.type) === 5) return "--";
  return s.correctCount;
};
const summaryCellScoreText = (s) => {
  if (gradingIncomplete.value && Number(s?.type) === 5) return "--";
  return `${s.score} / ${s.fullScore}`;
};

const summaryTotalQuestionCountDisplay = computed(() => totalQuestionCount.value);

const summaryTotalCorrectCountDisplay = computed(() =>
  gradingIncomplete.value ? objectiveTotals.value.correctCount : totalCorrectCount.value
);

const summaryTotalScoreTextDisplay = computed(() => {
  if (gradingIncomplete.value) {
    const { score } = objectiveTotals.value;
    // 分母为卷面总分，与阅卷完成后一致；分子仍为不含简答题的得分
    return `${score} / ${paperFullScoreDisplay.value}`;
  }
  return `${studentScore.value} / ${paperFullScoreDisplay.value}`;
});

const formatBehaviorClock = (raw) => {
  if (raw === null || raw === undefined || raw === "") return "--:--:--";
  const s = String(raw).trim();
  const tail = s.match(/(\d{1,2}:\d{2}:\d{2})$/);
  if (tail) {
    const parts = tail[1].split(":");
    return `${String(parts[0]).padStart(2, "0")}:${parts[1]}:${parts[2]}`;
  }
  const d = new Date(s);
  if (!Number.isNaN(d.getTime())) {
    return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}:${String(d.getSeconds()).padStart(2, "0")}`;
  }
  return "--:--:--";
};

const behaviorTimeline = computed(() => {
  const list = [];
  const times = Array.isArray(screenSwitchTimes.value) ? screenSwitchTimes.value : [];
  const fallbackCount = Math.max(0, Number(screenSwitchCount.value || 0));
  if (examStartTimeText.value) {
    list.push({ type: "start", title: "进入考试", desc: "考生进入考试，考试开始", time: examStartTimeText.value.slice(11) });
  }
  if (times.length > 0) {
    times.forEach((raw) => {
      list.push({
        type: "switch",
        title: "切换屏幕",
        desc: "考生切换出考试页面",
        time: formatBehaviorClock(raw),
      });
    });
  } else if (fallbackCount > 0) {
    for (let i = 0; i < fallbackCount; i++) {
      list.push({
        type: "switch",
        title: "切换屏幕",
        desc: `考生切换出考试页面（第${i + 1}次，无精确切屏时间记录）`,
        time: "--:--:--",
      });
    }
  }
  if (examEndTimeText.value) {
    const endTitle = Number(recordStatus.value) === 2 ? "强制交卷" : "提交试卷";
    const endDesc = Number(recordStatus.value) === 2 ? "系统强制结束考试" : "考生提交试卷，考试结束";
    list.push({ type: "submit", title: endTitle, desc: endDesc, time: examEndTimeText.value.slice(11) });
  }
  return list;
});

const normalizeAnswer = (value) => String(value || "").replaceAll(" ", "").trim().toUpperCase();
const shouldShowOptions = (row) => [1, 2, 3].includes(Number(row?.examQuestionTypes));

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "")
    .replace(/<img([^>]*?)style=""/gi, "<img$1");
};

const parseQuestionOptions = (raw) => {
  if (!raw) return [];
  const normalizeOption = (item, index) => {
    if (typeof item === "string") {
      const text = item.trim();
      if (!text) return null;
      const matched = text.match(/^([A-Z])[\.、:：)\s-]+(.*)$/i);
      if (matched) return { key: `${matched[1].toUpperCase()}.`, text: (matched[2] || "").trim() };
      return { key: `${String.fromCharCode(65 + index)}.`, text };
    }
    if (item && typeof item === "object") {
      const key = item.key || item.label || item.option || item.code || String.fromCharCode(65 + index);
      const text = item.value || item.text || item.content || item.name || "";
      return { key: `${String(key).replace(/[.、:：\s]+$/g, "")}.`, text: String(text).trim() };
    }
    return null;
  };

  let parsed = raw;
  if (typeof raw === "string") {
    const text = raw.trim();
    if (!text) return [];
    try {
      parsed = JSON.parse(text);
    } catch (e) {
      const pieces = text.split(/\r?\n|[;；]+/).map((s) => s.trim()).filter(Boolean);
      return pieces.map((item, index) => normalizeOption(item, index)).filter(Boolean);
    }
  }

  if (Array.isArray(parsed)) {
    return parsed.map((item, index) => normalizeOption(item, index)).filter((item) => item && item.text);
  }

  if (parsed && typeof parsed === "object") {
    return Object.keys(parsed)
      .map((key, index) => normalizeOption({ key, text: parsed[key] }, index))
      .filter((item) => item && item.text);
  }

  return [];
};

const isPendingSubjective = (row) => Number(row?.examQuestionTypes) === 5 && Number(row?.reviewStatus) === 0;
const shouldShowScore = (row) => !isPendingSubjective(row);
const getQuestionScore = (row) => {
  if (Number(row?.examQuestionTypes) === 5) return Number(row?.teacherScore ?? row?.examDetailsMyscore ?? 0);
  return Number(row?.examDetailsMyscore ?? 0);
};
const isQuestionCorrect = (row) => {
  if (Number(row?.examQuestionTypes) === 5) return getQuestionScore(row) > 0;
  
  // 客观题：优先根据得分判断（如果有得分数据）
  const score = Number(row?.examDetailsMyscore ?? 0);
  const fullScore = Number(row?.examPaperTopicNumber ?? 0);
  
  // 如果有得分数据，根据得分判断（得分等于满分则正确）
  if (fullScore > 0 && score === fullScore) {
    return true;
  }
  
  // 如果得分为0，则为错误
  if (score === 0) {
    return false;
  }
  
  // 兜底：比较答案（如果正确答案存在）
  const ans = normalizeAnswer(row?.examDetailsMyanswer);
  const right = normalizeAnswer(row?.examQuestionAnswer);
  if (!ans) return false;
  if (!right) {
    // 如果没有正确答案数据，根据得分判断
    return score > 0;
  }
  return ans === right;
};
const showTeacherComment = (row) =>
  Number(row?.examQuestionTypes) === 5 && Number(row?.reviewStatus) === 1 && String(row?.teacherComment || "").trim() !== "";

/**
 * 获取题目答题状态文本
 */
const getAnswerStatus = (row) => {
  const questionType = Number(row?.examQuestionTypes);
  
  // 简答题处理
  if (questionType === 5) {
    const reviewStatus = Number(row?.reviewStatus);
    // 待批阅状态
    if (reviewStatus === 0) {
      return "待批阅";
    }
    // 已批阅状态，根据得分判定
    if (reviewStatus === 1) {
      const score = getQuestionScore(row);
      return score > 0 ? "回答正确" : "回答错误";
    }
    return null;
  }
  
  // 客观题处理（单选、多选、判断、填空）
  const isCorrect = isQuestionCorrect(row);
  return isCorrect ? "回答正确" : "回答错误";
};

/**
 * 获取状态标签类型
 */
const getAnswerStatusType = (row) => {
  const status = getAnswerStatus(row);
  
  if (status === "回答正确") {
    return "success";
  } else if (status === "回答错误") {
    return "danger";
  } else if (status === "待批阅") {
    return "warning";
  }
  
  return "info";
};

const questionList = computed(() =>
  detailsSorted.value.map((row, index) => ({
    ...row,
    parsedOptions: parseQuestionOptions(row.examQuestionOptions),
    globalIndex: index + 1, // 存储全局序号（从1开始）
  }))
);

/**
 * 按题型分组并筛选题目
 */
const groupedQuestions = computed(() => {
  // 始终基于全量题目进行分组，保持全局顺序
  const allQuestions = questionList.value;
  
  if (!allQuestions.length) return [];
  
  // 按题型分组（基于全量题目）
  const typeGroups = {};
  allQuestions.forEach(q => {
    const type = Number(q.examQuestionTypes);
    if (!typeGroups[type]) {
      typeGroups[type] = {
        type,
        questions: [],
        fullScore: 0
      };
    }
    typeGroups[type].questions.push(q);
    typeGroups[type].fullScore += Number(q.examPaperTopicNumber || 0);
  });
  
  // 按题型顺序排列（1-单选, 2-多选, 3-判断, 4-填空, 5-简答）
  const sortedTypes = [1, 2, 3, 4, 5].filter(t => typeGroups[t]);
  
  // 根据当前筛选条件，决定显示哪些分组
  let displayTypes = sortedTypes;
  if (activeTab.value !== 'all') {
    const filterType = Number(activeTab.value);
    displayTypes = sortedTypes.filter(t => t === filterType);
  }
  
  return displayTypes.map(type => ({
    type,
    // 后端已经按 exam_paper_topic_sequence ASC 排序，前端直接使用
    questions: typeGroups[type].questions,
    fullScore: typeGroups[type].fullScore,
    // 记录该题型在全局中的顺序位置（用于显示中文序号）
    globalTypeOrder: sortedTypes.indexOf(type) + 1
  }));
});

/**
 * 获取中文序号
 */
const getChineseNumber = (num) => {
  const chineseNumbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十'];
  return chineseNumbers[num - 1] || num;
};

/**
 * 获取题目在组内的题号（始终使用全局序号）
 */
const getGroupQuestionNumber = (groupIdx, questionIdx) => {
  const group = groupedQuestions.value[groupIdx];
  if (!group || !group.questions[questionIdx]) return questionIdx + 1;
  
  // 直接使用题目数据中预存的全局序号
  return group.questions[questionIdx].globalIndex;
};

/**
 * 切换题型标签
 */
const switchTab = (tab) => {
  activeTab.value = tab;
};

const parseScreenSwitchTimes = (raw) => {
  if (raw == null) return [];
  if (Array.isArray(raw)) return raw.map((x) => String(x)).filter(Boolean);
  if (typeof raw === "string") {
    const t = raw.trim();
    if (!t) return [];
    try {
      const arr = JSON.parse(t);
      return Array.isArray(arr) ? arr.map((x) => String(x)).filter(Boolean) : [];
    } catch {
      return [];
    }
  }
  return [];
};

const getQuestionStem = (row) => {
  const r = row || {};
  return (
    r.examQuestionContent ||
    r.examQuestionTitle ||
    r.examQuestionName ||
    r.examQuestionText ||
    r.examQuestionProblem ||
    r.questionContent ||
    ""
  );
};

const loadData = async () => {
  /** 排名以服务端 examPaperInfo.fillPublishedExamRank 为准；勿用未筛选的 page 结果覆盖 */
  let rankFromBackend = false;
  try {
    if (examDetailsUuidNumber) {
      const { data } = await request.get(`examRecord/examPaperInfo/${examDetailsUuidNumber}`);
      paperFullScore.value = data?.examPaperMyscore ?? null;
      examEndTimeText.value = data?.endTime || "";
      examStartTimeText.value = data?.insertTime || "";
      recordStatus.value = data?.status ?? null;
      recordTotalScore.value = data?.totalScore ?? null;
      examInfoId.value = data?.examInfoId ?? null;
      screenSwitchCount.value = data?.screenSwitchCount ?? 0;
      screenSwitchTimes.value = parseScreenSwitchTimes(data?.screenSwitchTimes);
      detailsList.value = Array.isArray(data?.examDetailsRespList) ? data.examDetailsRespList : [];
      rank.value = data?.examRank ?? null;
      rankFromBackend = true;
    } else if (examRecordId) {
      const { data: recordInfo } = await request.get(`examRecord/info/${examRecordId}`);
      examInfoId.value = recordInfo?.examInfoId ?? null;
      screenSwitchCount.value = recordInfo?.screenSwitchCount ?? 0;
      screenSwitchTimes.value = parseScreenSwitchTimes(recordInfo?.screenSwitchTimes);
      recordStatus.value = recordInfo?.status ?? Number(route.query.status);
      const { data } = await request.post("examDetails/page", {
        page: 1,
        limit: 2000,
        examRecordId,
      });
      detailsList.value = data?.list || [];
      const first = detailsList.value?.[0];
      paperFullScore.value = first?.examPaperMyscore ?? first?.examPaper?.examPaperMyscore ?? null;
      examEndTimeText.value = first?.endTime || "";
      examStartTimeText.value = first?.insertTime || "";
      recordTotalScore.value = route.query.totalScore ?? recordInfo?.totalScore ?? null;
      const uuid = recordInfo?.examRecordUuidNumber;
      if (uuid) {
        try {
          const { data: paperData } = await request.get(`examRecord/examPaperInfo/${uuid}`);
          rank.value = paperData?.examRank ?? null;
          rankFromBackend = true;
        } catch {
          rank.value = null;
        }
      }
    }

    if (examInfoId.value) {
      const { data: examInfo } = await request.get(`examInfo/info/${examInfoId.value}`);
      passingScore.value = examInfo?.passingScore ?? null;
      examInfoStartTime.value = examInfo?.startTime || "";
      examInfoEndTime.value = examInfo?.endTime || "";
    }

    if (examInfoId.value && !rankFromBackend) {
      try {
        const { data: rankData } = await request.post("examRecord/page", {
          page: 1,
          limit: 5000,
          examInfoId: examInfoId.value,
          onlyLatest: 1,
        });
        const records = Array.isArray(rankData?.list) ? rankData.list : [];
        const currentId = Number(examRecordId);
        const currentUuid = String(examDetailsUuidNumber || "");
        const sorted = records
          .filter((item) => item && item.totalScore !== null && item.totalScore !== undefined)
          .sort((a, b) => Number(b.totalScore || 0) - Number(a.totalScore || 0));

        let lastScore = null;
        let currentRank = 0;
        sorted.forEach((item, index) => {
          const score = Number(item.totalScore || 0);
          if (lastScore === null || score < lastScore) {
            currentRank = index + 1;
            lastScore = score;
          }
          item.__rank = currentRank;
        });

        const target = sorted.find((item) => {
          if (currentId && Number(item.id) === currentId) return true;
          if (currentUuid && String(item.examRecordUuidNumber || "") === currentUuid) return true;
          return false;
        });
        rank.value = target?.__rank ?? null;
      } catch (e) {
        rank.value = null;
      }
    }
    scrollAdminLayoutToTop();
  } catch (e) {
    console.error(e);
    ElMessage.error("加载考试记录详情失败");
  }
};

const backToList = () => {
  router.go(-1);
};

onBeforeMount(() => {
  if (isDetailMode.value) loadData();
});

onMounted(() => {
  scrollAdminLayoutToTop();
});

watch(
  () => route.fullPath,
  () => {
    scrollAdminLayoutToTop();
  }
);
</script>

<style lang="scss" scoped>
.module-container {
  max-width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.back-btn {
  min-width: 104px;
  height: 36px;
  border-radius: 6px;
  border-color: #dbe6f3;
  color: #2f8cff;
  font-size: 16px;
  font-weight: 500;
  padding: 0 18px;
}

.back-icon {
  margin-right: 6px;
  font-size: 18px;
  line-height: 1;
}

.card-section,
.fallback-card {
  margin-bottom: 14px;
  padding: 16px;
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.section-head {
  border-left: 3px solid #409eff;
  padding-left: 10px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.basic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 24px;
}

.basic-grid .item {
  color: #606266;
}

.basic-grid .k {
  font-weight: 600;
}

.basic-grid .v {
  color: #303133;
}

.status-row {
  grid-column: 1 / -1;
}

.exam-result-card {
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid #e8edf4;
  border-radius: 10px;
  background: #fff;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(120px, 1fr));
  gap: 8px;
}

.result-item {
  text-align: center;
  padding: 8px 10px;
  border-right: 1px solid #ebeef5;
}

.result-item:last-child {
  border-right: none;
}

.result-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.result-value {
  font-size: 34px;
  font-weight: 700;
  line-height: 1.1;
}

.result-score-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.result-value.blue { color: #2f8cff; }
.result-value.green { color: #48a649; }
.result-value.orange { color: #e6a23c; }
.result-value.purple { color: #9b59b6; }
.result-value.cyan { color: #2f8cff; }

.result-score-row :deep(.el-tag) {
  margin-top: 4px;
}

.table-wrap {
  overflow-x: auto;
}

.summary-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
}

.summary-table th,
.summary-table td {
  border: 1px solid #ebeef5;
  padding: 13px 10px;
  font-size: 16px;
}

.summary-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #606266;
}

.summary-table tbody tr:nth-child(2) td:not(:first-child) {
  color: #48a649;
  font-weight: 500;
}

.summary-table tbody tr:nth-child(3) td:not(:first-child) {
  color: #2f8cff;
  font-weight: 600;
}

.timeline {
  position: relative;
  padding: 10px 4px 0;
}

.timeline::before {
  content: "";
  position: absolute;
  left: 12px;
  top: 18px;
  bottom: 18px;
  width: 1px;
  background: #e5eaf3;
}

.line-item {
  display: grid;
  grid-template-columns: 24px 1fr 100px;
  gap: 14px;
  align-items: start;
  margin-bottom: 18px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  margin: 5px auto 0;
  z-index: 1;
}

.dot.start { background: #409eff; }
.dot.switch { background: #e6a23c; }
.dot.submit { background: #67c23a; }

.line-item .content .title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.line-item .content .desc {
  color: #a2a8b3;
  margin-top: 6px;
  font-size: 18px;
}

.line-item .time {
  color: #748094;
  font-size: 22px;
  text-align: right;
  line-height: 1.2;
}

.question-list {
  display: grid;
  gap: 14px;
}

/* ==================== 题型筛选标签 ==================== */
.tabs {
  display: flex;
  gap: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.tab {
  padding: 10px 0;
  cursor: pointer;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
  position: relative;
}

.tab:hover {
  color: #409eff;
}

.tab.active {
  color: #409eff;
  font-weight: 700;
}

.tab.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: #409eff;
}

/* ==================== 按题型分组的试卷样式 ==================== */
.exam-paper {
  margin-top: 20px;
}

.question-group {
  margin-bottom: 30px;
}

.group-header {
  background: #f5f7fa;
  padding: 12px 16px;
  border-left: 4px solid #409eff;
  margin-bottom: 16px;
  border-radius: 4px;
}

.group-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.questions-list {
  padding: 0 16px;
}

.question-item {
  padding: 20px 0;
}

.question-stem {
  margin-bottom: 16px;
  line-height: 1.8;
  font-size: 15px;
  color: #303133;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.question-number {
  font-weight: 700;
  color: #409eff;
  margin-right: 8px;
}

.question-text {
  white-space: normal;
}

.rich-text-content {
  line-height: 1.8;
  color: #303133;
}

.rich-text-content :deep(p) {
  margin: 0 0 8px;
}

.rich-text-content :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 12px;
  border-left: 3px solid #dcdfe6;
  background: #f8f9fb;
  color: #606266;
}

.rich-text-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 6px 0;
}

.rich-text-content :deep(table) {
  width: 100% !important;
  border-collapse: collapse;
  margin: 10px 0;
  border: 1px solid #dcdfe6;
  background: #fff;
}

.rich-text-content :deep(th),
.rich-text-content :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 10px;
  text-align: left;
  font-size: 14px;
  color: #303133;
}

.rich-text-content :deep(thead tr) {
  background: #f5f7fa;
}

/* 选项列表 */
.options-list {
  margin: 12px 0 16px 24px;
  display: grid;
  gap: 8px;
}

.option-row {
  display: grid;
  grid-template-columns: 30px 1fr;
  gap: 8px;
  align-items: start;
  line-height: 1.6;
}

.option-key {
  font-weight: 600;
  color: #409eff;
}

.option-content {
  color: #303133;
  white-space: pre-wrap;
}

/* 答案信息区域 */
.answer-info-section {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f9fbff;
  border-radius: 6px;
  border: 1px solid #e8edf4;
  display: grid;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.info-value {
  color: #303133;
  flex: 1;
}

.student-answer {
  color: #f56c6c;
  font-weight: 600;
}

.correct-answer {
  color: #67c23a;
  font-weight: 600;
}

.score-row {
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
}

.score-value {
  color: #409eff;
  font-weight: 700;
  font-size: 15px;
}

/* 解析区域 */
.analysis-section {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  font-size: 14px;
}

.analysis-label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
}

.analysis-content {
  color: #303133;
  white-space: pre-wrap;
}

/* 教师评语区域 */
.teacher-comment-section {
  margin-top: 16px;
  padding: 14px 16px;
  background: #fff7e6;
  border-radius: 6px;
  border-left: 4px solid #e6a23c;
  line-height: 1.8;
  font-size: 14px;
}

.comment-label {
  font-weight: 600;
  color: #e6a23c;
  margin-right: 8px;
}

.comment-content {
  color: #606266;
  white-space: pre-wrap;
}

.status-tag {
  margin-left: 8px;
  flex-shrink: 0;
}

/* 题目分隔线 */
.question-divider {
  margin-top: 20px;
  border-bottom: 1px dashed #e4e7ed;
}

.question-box {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
}

.question-main-layout {
  display: grid;
  grid-template-columns: 1.3fr 0.9fr;
  gap: 16px;
}

.question-left {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.question-right {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #f9fbff;
  display: grid;
  gap: 10px;
  align-content: start;
}

.question-box .q-title {
  font-weight: 700;
  margin-bottom: 10px;
}

.q-stem {
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.option-list {
  display: grid;
  gap: 8px;
  margin-top: 8px;
}

.option-item {
  display: grid;
  grid-template-columns: 26px 1fr;
  gap: 8px;
  align-items: start;
  color: #606266;
}

.opt-key {
  color: #409eff;
  font-weight: 600;
}

.opt-text {
  color: #303133;
  white-space: pre-wrap;
}

.answer-info-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.answer-info-item .k {
  font-weight: 600;
  color: #606266;
}

.answer-info-item .v {
  color: #303133;
}

.answer-info-item .my-answer {
  color: #f56c6c;
  font-weight: 600;
}

.answer-info-item .right-answer {
  color: #67c23a;
  font-weight: 600;
}

.answer-info-item.score .v {
  color: #409eff;
  font-weight: 600;
}

.inline-analysis,
.extra-block {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px;
  align-items: start;
  margin-top: 12px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}

.inline-analysis .k,
.extra-block .k {
  color: #606266;
  font-weight: 600;
}

.inline-analysis .v,
.extra-block .v {
  color: #303133;
  white-space: pre-wrap;
}

.fallback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

@media (max-width: 1200px) {
  .basic-grid {
    grid-template-columns: 1fr 1fr;
  }

  .result-grid {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
    row-gap: 12px;
  }

  .result-item:nth-child(3n) {
    border-right: none;
  }
}

@media (max-width: 900px) {
  .basic-grid {
    grid-template-columns: 1fr;
  }

  .question-main-layout {
    grid-template-columns: 1fr;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  .result-item {
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }

  .result-item:last-child {
    border-bottom: none;
  }
}
</style>
