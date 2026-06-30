<template>
  <div class="grade-detail-page" :class="{ 'grade-detail-page--answer-sheet': isAnswerSheetView }">
    <div class="page-inner">
      <button type="button" class="back-row" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>

      <!-- 顶部成绩概要卡片 -->
      <section class="hero-card">
        <div class="hero-icon" aria-hidden="true">
          <el-icon v-if="isAnswerSheetView" class="hero-icon-el"><Document /></el-icon>
          <el-icon v-else class="hero-icon-el"><DataAnalysis /></el-icon>
        </div>
        <div class="hero-main">
          <h1 v-if="isAnswerSheetView" class="hero-title">{{ answerSheetTitle }}</h1>
          <h1 v-else class="hero-title">{{ displayExamTitle }}</h1>
          <p v-if="isAnswerSheetView" class="hero-sheet-hint">查看答卷 · 待阅卷期间不展示分数、标准答案与解析</p>
          <p class="hero-meta">
            <span>考试时间：{{ examScheduleText }}</span>
          </p>
          <p class="hero-meta">
            <span>提交时间：{{ submitTimeText }}</span>
          </p>
        </div>
        <div v-if="!isAnswerSheetView" class="hero-score">
          <div class="hero-score-line">
            总分 <strong class="score-num">{{ userScore }}</strong>
            <span class="score-slash">/ {{ totalScore }}</span>
          </div>
          <span class="grade-badge" :class="gradeBadgeClass">{{ gradeLevelLabel }}</span>
        </div>
        <div v-else class="hero-score hero-score--sheet">
          <p class="hero-sheet-note">当前为「仅答卷」模式，您可核对本人填写内容；最终得分以教师阅卷发布后为准。</p>
        </div>
      </section>

      <!-- 关键指标 -->
      <section v-if="!isAnswerSheetView" class="metrics-row">
        <div v-for="m in metricCards" :key="m.label" class="metric-card">
          <div class="metric-label">{{ m.label }}</div>
          <div class="metric-value" :class="m.valueClass">{{ m.value }}</div>
        </div>
      </section>

      <!-- 题型得分 -->
      <section v-if="!isAnswerSheetView" class="panel">
        <h2 class="panel-title">题型得分情况</h2>
        <div class="type-analysis-grid">
          <div class="type-table-wrap">
            <table class="type-table">
              <thead>
                <tr>
                  <th>题型</th>
                  <th>题量</th>
                  <th>得分</th>
                  <th>满分</th>
                  <th>得分率</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in typeAnalysisRows" :key="row.typeName">
                  <td>{{ row.typeName }}</td>
                  <td>{{ row.count }}</td>
                  <td>{{ row.earned }}</td>
                  <td>{{ row.full }}</td>
                  <td>{{ row.rateText }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="chart-wrap">
            <div class="donut-wrap" aria-hidden="true">
              <div class="donut-ring" :style="donutRingStyle" />
              <div class="donut-center">
                <span class="donut-label">得分率</span>
                <strong class="donut-pct">{{ scoreRatePct }}%</strong>
              </div>
            </div>
            <ul class="chart-legend">
              <li v-for="(leg, idx) in chartLegendItems" :key="leg.name">
                <span class="leg-dot" :style="{ background: chartColors[idx % chartColors.length] }" />
                <span class="leg-text">
                  <span class="leg-name">{{ leg.name }}</span>
                  <span class="leg-rate">{{ leg.rateText }}</span>
                </span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 试题详情 -->
      <section class="panel">
        <h2 class="panel-title">{{ isAnswerSheetView ? "试题详情（仅本人作答）" : "试题详情（按题型顺序）" }}</h2>
        <el-collapse v-model="openSections" class="q-collapse">
          <el-collapse-item
            v-for="sec in questionSections"
            :key="sec.key"
            :title="sec.sectionTitle"
            :name="sec.key"
          >
            <div
              v-for="(item, idx) in sec.items"
              :key="item.id || `${sec.key}-${idx}`"
              class="q-card"
              :class="!isAnswerSheetView ? getQuestionStatus(item) : ''"
            >
              <span class="q-no">{{ globalIndex(sec, idx) }}</span>
              <div class="q-main">
                <div class="q-title-row">
                  <div class="q-stem-wrap">
                    <div class="q-stem rich-text-content" v-html="formatRichText(item.examQuestionName || item.examQuestionContent || '')" />
                  </div>
                  <span v-if="!isAnswerSheetView" class="q-badge" :class="badgeClass(item)">{{ shortStatusText(item) }}</span>
                </div>
                <div class="q-body">
                <div
                  v-if="[1, 2, 3].includes(parseInt(item.examQuestionTypes))"
                  class="q-options-row q-options-row--vertical"
                >
                  <div
                    v-for="opt in parseOptions(item)"
                    :key="opt.code"
                    class="q-opt"
                    :class="getOptionClass(opt, item)"
                  >
                    <span class="q-opt-code">{{ opt.code }}</span>
                    <span class="q-opt-text">{{ opt.text }}</span>
                  </div>
                </div>

                <!-- 仅答卷模式：填空/简答只展示本人作答 -->
                <div
                  v-if="isAnswerSheetView && [4, 5].includes(parseInt(item.examQuestionTypes, 10))"
                  class="q-answer-compare q-answer-compare--sheet"
                >
                  <div class="q-answer-line">
                    <span class="q-answer-label">我的作答：</span>
                    <span class="q-answer-val">{{ myAnswerSummaryPlain(item) }}</span>
                  </div>
                  <div
                    v-if="parseInt(item.examQuestionTypes, 10) === 5 && getQuestionStatus(item) === 'pending'"
                    class="q-pending-review-hint q-answer-pending"
                  >
                    <el-icon><Clock /></el-icon> 此题正在等待教师批阅
                  </div>
                </div>

                <!-- 成绩详情：在解析上方区分「我的作答」与「参考答案」 -->
                <div v-if="gradeDetailShowsSolutions" class="q-answer-compare">
                  <div class="q-answer-line">
                    <span class="q-answer-label">我的作答：</span>
                    <span class="q-answer-val" :class="myAnswerValClass(item)">{{ myAnswerSummaryPlain(item) }}</span>
                  </div>
                  <div v-if="hasReferenceAnswer(item)" class="q-answer-line">
                    <span class="q-answer-label">参考答案：</span>
                    <span
                      v-if="[4, 5].includes(parseInt(item.examQuestionTypes, 10))"
                      class="q-answer-val is-reference rich-text-content"
                      v-html="formatRichText(item.examQuestionAnswer)"
                    />
                    <span v-else class="q-answer-val is-reference">{{ referenceAnswerChoiceText(item) }}</span>
                  </div>
                  <div
                    v-if="[4, 5].includes(parseInt(item.examQuestionTypes, 10)) && getQuestionStatus(item) === 'pending'"
                    class="q-pending-review-hint q-answer-pending"
                  >
                    <el-icon><Clock /></el-icon> 此题正在等待教师批阅
                  </div>
                </div>

                <div v-if="gradeDetailShowsSolutions && item.examQuestionAnalysis" class="q-analysis">
                  <div class="q-analysis-title">答案解析</div>
                  <div class="rich-text-content" v-html="formatRichText(item.examQuestionAnalysis)" />
                </div>

                <div
                  v-if="!isAnswerSheetView && teacherCommentText(item)"
                  class="q-teacher-comment"
                >
                  <div class="q-analysis-title">教师评语</div>
                  <div class="rich-text-content" v-html="formatRichText(teacherCommentText(item))" />
                </div>

                <div v-if="!isAnswerSheetView" class="q-foot-score">
                  得分：<span class="earned">{{ item.examDetailsMyscore ?? 0 }}</span>
                  <span class="slash"> / </span>
                  <span>{{ item.examPaperTopicNumber || item.examQuestionScore || 0 }}</span>
                </div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft, Clock, DataAnalysis, Document } from "@element-plus/icons-vue";
import request from "@/utils/request.js";

const router = useRouter();
const route = useRoute();

/** 待出分：仅展示本人作答，不展示分数、标准答案、解析 */
const isAnswerSheetView = computed(() => String(route.query.view || "") === "answerSheet");

const questionList = ref([]);
const examInfo = ref({
  examPaperName: "",
  examPaperMyscore: 0,
  totalScore: 0,
  nickname: "",
  examName: "",
  examScheduleStart: null,
  examScheduleEnd: null,
  insertTime: null,
  endTime: null,
});

const openSections = ref([]);

const TYPE_ORDER = [
  { key: "1", label: "单选题", order: "一" },
  { key: "2", label: "多选题", order: "二" },
  { key: "3", label: "判断题", order: "三" },
  { key: "4", label: "填空题", order: "四" },
  { key: "5", label: "简答题", order: "五" },
  { key: "0", label: "其他题型", order: "六" },
];

const chartColors = ["#4f46e5", "#06b6d4", "#f59e0b", "#ec4899", "#8b5cf6", "#64748b"];

/** 成绩详情（非「仅答卷」模式）始终展示标准答案、解析与评语，与考试「交卷后显示答案」配置无关 */
const gradeDetailShowsSolutions = computed(() => !isAnswerSheetView.value);

const teacherCommentText = (item) => {
  const c = item?.teacherComment ?? item?.teacher_comment;
  if (c == null || c === "") return "";
  const s = String(c).trim();
  return s || "";
};

const displayExamTitle = computed(() => {
  const n = examInfo.value?.examName || examInfo.value?.examPaperName;
  return n || "成绩详情";
});

/** 查看答卷：只展示考试安排名称（exam_info.exam_name），不用试卷名（常为「数学考试」类简称） */
const answerSheetTitle = computed(() => {
  const n = examInfo.value?.examName;
  return n && String(n).trim() ? String(n).trim() : "—";
});

/** 展示为 YYYY-MM-DD HH:mm（与「2026-05-03 00:00」一致） */
const padSchedule = (d) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const h = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${day} ${h}:${min}`;
};

const formatScheduleDateTime = (v) => {
  if (v == null || v === "") return "";
  if (v instanceof Date && !Number.isNaN(v.getTime())) return padSchedule(v);
  const raw = String(v).trim();
  if (!raw) return "";
  let d = new Date(raw);
  if (Number.isNaN(d.getTime()) && raw.includes(" ") && !raw.includes("T")) {
    d = new Date(raw.replace(" ", "T"));
  }
  if (!Number.isNaN(d.getTime())) return padSchedule(d);
  const norm = raw.includes("T") ? raw.replace("T", " ") : raw;
  if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}/.test(norm)) return norm.slice(0, 16);
  return norm.length >= 16 ? norm.slice(0, 16) : norm || "";
};

const formatDateTime = (v) => {
  const t = formatScheduleDateTime(v);
  return t || "—";
};

const examScheduleText = computed(() => {
  const a = examInfo.value?.examScheduleStart ?? examInfo.value?.exam_schedule_start;
  const b = examInfo.value?.examScheduleEnd ?? examInfo.value?.exam_schedule_end;
  const fa = formatScheduleDateTime(a);
  const fb = formatScheduleDateTime(b);
  if (fa && fb) return `${fa} - ${fb}`;
  if (fa && !fb) return `${fa} - —`;
  if (!fa && fb) return `— - ${fb}`;
  return "—";
});

const submitTimeText = computed(() => formatDateTime(examInfo.value?.endTime || examInfo.value?.insertTime));

const totalScore = computed(() => Number(examInfo.value?.examPaperMyscore) || 0);
const userScore = computed(() => Number(examInfo.value?.totalScore) || 0);

const scoreRatePct = computed(() => {
  if (!totalScore.value) return 0;
  return Math.round((userScore.value / totalScore.value) * 1000) / 10;
});

const gradeLevelLabel = computed(() => {
  const p = scoreRatePct.value;
  if (p >= 90) return "优秀";
  if (p >= 80) return "良好";
  if (p >= 60) return "及格";
  return "待提高";
});

const gradeBadgeClass = computed(() => {
  const p = scoreRatePct.value;
  if (p >= 90) return "lv-excellent";
  if (p >= 80) return "lv-good";
  if (p >= 60) return "lv-pass";
  return "lv-low";
});

/** 同场考试、阅卷发布后的名次（后端 examRank / examRankTotal） */
const examRankDisplay = computed(() => {
  const total = examInfo.value?.examRankTotal;
  const rank = examInfo.value?.examRank;
  if (total == null || rank == null || Number(total) <= 0) return "—";
  return `${rank} / ${total}`;
});

/** 从进入考试到交卷的用时（insert_time → end_time） */
const answerDurationDisplay = computed(() => {
  const start = examInfo.value?.insertTime;
  const end = examInfo.value?.endTime;
  if (!start || !end) return "—";
  const t0 = new Date(start).getTime();
  const t1 = new Date(end).getTime();
  if (!Number.isFinite(t0) || !Number.isFinite(t1) || t1 < t0) return "—";
  let sec = Math.floor((t1 - t0) / 1000);
  if (sec < 0) return "—";
  if (sec < 60) return `${sec}秒`;
  const m = Math.floor(sec / 60);
  sec %= 60;
  if (m < 60) return sec > 0 ? `${m}分${sec}秒` : `${m}分钟`;
  const h = Math.floor(m / 60);
  const mm = m % 60;
  if (mm === 0 && sec === 0) return `${h}小时`;
  if (sec === 0) return `${h}小时${mm}分钟`;
  return `${h}小时${mm}分${sec}秒`;
});

const metricCards = computed(() => [
  { label: "得分（分）", value: String(userScore.value), valueClass: "" },
  { label: "总分（分）", value: String(totalScore.value), valueClass: "" },
  { label: "得分率", value: `${scoreRatePct.value}%`, valueClass: "" },
  { label: "排名", value: examRankDisplay.value, valueClass: examRankDisplay.value === "—" ? "muted" : "" },
  { label: "答题用时", value: answerDurationDisplay.value, valueClass: answerDurationDisplay.value === "—" ? "muted" : "" },
  {
    label: "成绩等级",
    value: gradeLevelLabel.value,
    valueClass: `grade-txt ${gradeBadgeClass.value}`,
  },
]);

const typeBuckets = computed(() => {
  const map = new Map();
  questionList.value.forEach((item) => {
    const t = parseInt(item.examQuestionTypes, 10);
    const key = Number.isFinite(t) && t >= 1 && t <= 5 ? String(t) : "0";
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(item);
  });
  TYPE_ORDER.forEach(({ key }) => {
    if (!map.has(key)) map.set(key, []);
  });
  return map;
});

const typeAnalysisRows = computed(() => {
  const rows = [];
  let sumCount = 0;
  let sumEarned = 0;
  let sumFull = 0;
  TYPE_ORDER.forEach(({ key, label }) => {
    const items = typeBuckets.value.get(key) || [];
    if (!items.length) return;
    const count = items.length;
    const earned = items.reduce((s, it) => s + (Number(it.examDetailsMyscore) || 0), 0);
    const full = items.reduce((s, it) => s + (Number(it.examPaperTopicNumber || it.examQuestionScore) || 0), 0);
    const rate = full > 0 ? Math.round((earned / full) * 1000) / 10 : 0;
    rows.push({
      typeName: label,
      count,
      earned,
      full,
      rateText: `${rate}%`,
    });
    sumCount += count;
    sumEarned += earned;
    sumFull += full;
  });
  const totalRate = sumFull > 0 ? Math.round((sumEarned / sumFull) * 1000) / 10 : 0;
  rows.push({
    typeName: "合计",
    count: sumCount,
    earned: sumEarned,
    full: sumFull,
    rateText: `${totalRate}%`,
  });
  return rows;
});

const chartLegendItems = computed(() =>
  typeAnalysisRows.value.filter((r) => r.typeName !== "合计").map((r) => ({ name: r.typeName, rateText: r.rateText }))
);

const donutRingStyle = computed(() => {
  const segs = typeAnalysisRows.value.filter((r) => r.typeName !== "合计");
  const totalFull = segs.reduce((s, r) => s + r.full, 0) || 1;
  let start = 0;
  const parts = [];
  segs.forEach((r, i) => {
    const deg = (r.full / totalFull) * 360;
    const c = chartColors[i % chartColors.length];
    parts.push(`${c} ${start}deg ${start + deg}deg`);
    start += deg;
  });
  if (!parts.length) {
    return { background: "#e5e7eb" };
  }
  return { background: `conic-gradient(${parts.join(", ")})` };
});

const questionSections = computed(() => {
  const sections = [];
  let offset = 0;
  const paperOnly = isAnswerSheetView.value;
  TYPE_ORDER.forEach(({ key, label, order }) => {
    const items = [...(typeBuckets.value.get(key) || [])].sort(
      (a, b) => (a.examPaperTopicSequence ?? 0) - (b.examPaperTopicSequence ?? 0)
    );
    if (!items.length) return;
    const full = items.reduce((s, it) => s + (Number(it.examPaperTopicNumber || it.examQuestionScore) || 0), 0);
    const first = Number(items[0]?.examPaperTopicNumber || items[0]?.examQuestionScore) || 0;
    const same = items.every(
      (it) => (Number(it.examPaperTopicNumber || it.examQuestionScore) || 0) === first
    );
    const scorePhrase = same ? `每题${first}分` : "各题分值不同";
    const sectionTitle = paperOnly
      ? `${order}、${label}（共${items.length}题）`
      : `${order}、${label}（共${items.length}题，${scorePhrase}，共${full}分）`;
    sections.push({
      key,
      sectionTitle,
      items,
      offsetStart: offset,
    });
    offset += items.length;
  });
  return sections;
});

const globalIndex = (sec, idx) => sec.offsetStart + idx + 1;

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "");
};

/** 单选/多选/判断：把选项代号展开为「代号. 题干」便于与参考答案对照 */
const formatChoiceAnswerDisplay = (item, codesRaw) => {
  if (codesRaw == null || codesRaw === "") return "";
  const raw = String(codesRaw).trim();
  if (!raw || raw === "未作答") return "";
  const t = parseInt(item.examQuestionTypes, 10);
  const opts = parseOptions(item);
  if (!opts.length) return raw;
  const map = new Map(opts.map((o) => [String(o.code).trim(), o.text]));
  const parts = t === 2 ? raw.split(",").map((s) => s.trim()).filter(Boolean) : [raw];
  return parts
    .map((code) => {
      const text = map.get(code);
      return text ? `${code}. ${text}` : code;
    })
    .join("；");
};

const myAnswerSummaryPlain = (item) => {
  const my = item.examDetailsMyanswer;
  if (!my || my === "未作答" || !String(my).trim()) return "未作答";
  const t = parseInt(item.examQuestionTypes, 10);
  if ([1, 2, 3].includes(t)) {
    return formatChoiceAnswerDisplay(item, my) || String(my).trim();
  }
  return String(my).trim();
};

const hasReferenceAnswer = (item) => {
  const a = item.examQuestionAnswer;
  return a != null && String(a).trim() !== "";
};

const referenceAnswerChoiceText = (item) => {
  if (!hasReferenceAnswer(item)) return "";
  const t = parseInt(item.examQuestionTypes, 10);
  const ans = String(item.examQuestionAnswer).trim();
  if ([1, 2, 3].includes(t)) {
    return formatChoiceAnswerDisplay(item, ans) || ans;
  }
  return ans;
};

const parseOptions = (item) => {
  if (!item.examQuestionOptions || item.examQuestionOptions === "[]") return [];
  try {
    const rawOptions = JSON.parse(item.examQuestionOptions);
    if (Array.isArray(rawOptions) && rawOptions.every((opt) => typeof opt === "object" && opt && "code" in opt && "text" in opt)) {
      return rawOptions;
    }
    return rawOptions.map((opt, index) => {
      const match = String(opt).match(/^([A-Za-z])\.\s*(.*)$/);
      if (match) return { code: match[1], text: match[2].trim() };
      return { code: String.fromCharCode(65 + index), text: String(opt).trim() };
    });
  } catch {
    return [];
  }
};

const getQuestionStatus = (item) => {
  if (parseInt(item.examQuestionTypes, 10) === 5) {
    if (!item.examDetailsMyanswer || item.examDetailsMyanswer === "未作答" || !String(item.examDetailsMyanswer).trim()) {
      return "unanswered";
    }
    const reviewStatus = item.reviewStatus;
    if (reviewStatus === null || reviewStatus === undefined || reviewStatus === 0) return "pending";
    const sc = parseFloat(item.examDetailsMyscore) || 0;
    return sc > 0 ? "correct" : "wrong";
  }
  const sc = parseFloat(item.examDetailsMyscore) || 0;
  if (!item.examDetailsMyanswer || item.examDetailsMyanswer === "未作答" || !String(item.examDetailsMyanswer).trim()) {
    return "unanswered";
  }
  if (sc > 0) return "correct";
  return "wrong";
};

const shortStatusText = (item) => {
  const s = getQuestionStatus(item);
  if (s === "correct") return "正确";
  if (s === "wrong") return "错误";
  if (s === "pending") return "待批阅";
  return "未作答";
};

const myAnswerValClass = (item) => {
  if (isAnswerSheetView.value) return {};
  return {
    "is-wrong": getQuestionStatus(item) === "wrong",
    "is-pending": getQuestionStatus(item) === "pending",
  };
};

const badgeClass = (item) => {
  const s = getQuestionStatus(item);
  if (s === "correct") return "b-ok";
  if (s === "wrong") return "b-bad";
  if (s === "pending") return "b-wait";
  return "b-skip";
};

const isUserAnswer = (code, item) => {
  if (!item.examDetailsMyanswer) return false;
  if (parseInt(item.examQuestionTypes, 10) === 2) {
    return item.examDetailsMyanswer.split(",").includes(code);
  }
  return item.examDetailsMyanswer === code;
};

const isCorrectAnswer = (code, item) => {
  if (!gradeDetailShowsSolutions.value || !item.examQuestionAnswer) return false;
  if (parseInt(item.examQuestionTypes, 10) === 2) {
    return item.examQuestionAnswer.split(",").includes(code);
  }
  return item.examQuestionAnswer === code;
};

const getOptionClass = (opt, item) => {
  const u = isUserAnswer(opt.code, item);
  if (isAnswerSheetView.value) {
    return u ? "picked" : "";
  }
  const c = isCorrectAnswer(opt.code, item);
  if (u && c) return "ok";
  if (u && !c) return "bad";
  if (!u && c) return "answer-only";
  return "";
};

const goBack = () => {
  router.push("/index/studentExamRecord");
};

const fetchExamDetails = async () => {
  try {
    const uuid = route.query.uuid || route.params.uuid;
    if (!uuid) {
      ElMessage.error("参数错误：未找到成绩记录");
      return;
    }
    const response = await request.get(`/examRecord/examPaperInfo/${uuid}`);
    if (response.code !== 200) {
      ElMessage.error(response.msg || "加载失败");
      return;
    }
    const data = response.data || {};
    examInfo.value = {
      ...examInfo.value,
      ...data,
    };
    questionList.value = data.examDetailsRespList || [];

    const start = examInfo.value.examScheduleStart ?? examInfo.value.exam_schedule_start;
    const end = examInfo.value.examScheduleEnd ?? examInfo.value.exam_schedule_end;
    const nameMissing = !examInfo.value.examName || !String(examInfo.value.examName).trim();
    const eid = examInfo.value.examInfoId ?? examInfo.value.exam_info_id;
    if (eid != null && (nameMissing || !start || !end)) {
      try {
        const infoRes = await request.get(`/examInfo/info/${eid}`);
        const ei = infoRes.data || {};
        if (nameMissing && ei.examName) examInfo.value.examName = ei.examName;
        if (!start && ei.startTime != null) examInfo.value.examScheduleStart = ei.startTime;
        if (!end && ei.endTime != null) examInfo.value.examScheduleEnd = ei.endTime;
      } catch (_) {
        /* 补全考试信息失败时忽略 */
      }
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("网络错误，请稍后重试");
  }
};

watch(
  questionList,
  async () => {
    await nextTick();
    openSections.value = questionSections.value.map((s) => s.key);
  },
  { deep: true }
);

watch(
  () => route.query.uuid,
  (uuid) => {
    if (uuid) fetchExamDetails();
  },
  { immediate: true }
);
</script>

<style lang="scss" scoped>
.grade-detail-page {
  box-sizing: border-box;
  flex: 1;
  min-height: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #eef1f6;
}

.page-inner {
  flex: 1;
  min-height: 0;
  max-width: none;
  width: 100%;
  margin: 0;
  box-sizing: border-box;
  padding: 16px 20px 32px;
  overflow-x: hidden;
  overflow-y: auto;
}

/* 查看答卷：隐藏滚动条、略紧凑内边距 */
.grade-detail-page--answer-sheet {
  overflow: hidden;
}

.grade-detail-page--answer-sheet .page-inner {
  padding: 12px 16px 20px;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
    display: none;
  }
}

.back-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: #4f46e5;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  padding: 0 0 16px;
  &:hover {
    color: #3730a3;
  }
}

.hero-card {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 20px;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 22px 24px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  margin-bottom: 16px;
}

@media (max-width: 640px) {
  .hero-card {
    grid-template-columns: 1fr;
    text-align: center;
  }
  .hero-score {
    text-align: center;
  }
}

.hero-icon {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: linear-gradient(145deg, #4f8efc, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
}

.hero-icon-el {
  font-size: 34px;
  color: #fff;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
}

.hero-meta {
  margin: 0 0 6px;
  font-size: 14px;
  color: #64748b;
}

.hero-sheet-hint {
  margin: 0 0 10px;
  font-size: 13px;
  color: #4f46e5;
  font-weight: 600;
}

.hero-score--sheet {
  text-align: left;
  max-width: 360px;
  justify-self: end;
}

.hero-sheet-note {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.55;
}

.hero-score {
  text-align: right;
}

.hero-score-line {
  font-size: 15px;
  color: #475569;
  margin-bottom: 8px;
}

.score-num {
  font-size: 34px;
  font-weight: 800;
  color: #e11d48;
  margin: 0 4px;
}

.score-slash {
  font-size: 18px;
  color: #94a3b8;
  font-weight: 600;
}

.grade-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.lv-excellent {
  background: #dcfce7;
  color: #15803d;
}
.lv-good {
  background: #dbeafe;
  color: #1d4ed8;
}
.lv-pass {
  background: #fef9c3;
  color: #a16207;
}
.lv-low {
  background: #fee2e2;
  color: #b91c1c;
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

@media (max-width: 960px) {
  .metrics-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

.metric-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  &.muted {
    color: #94a3b8;
    font-size: 15px;
  }
  &.grade-txt.lv-excellent {
    color: #15803d;
  }
  &.grade-txt.lv-good {
    color: #1d4ed8;
  }
  &.grade-txt.lv-pass {
    color: #a16207;
  }
  &.grade-txt.lv-low {
    color: #b91c1c;
  }
}

.panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px 22px 24px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  margin-bottom: 16px;
}

.panel-title {
  margin: 0 0 18px;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.type-analysis-grid {
  display: grid;
  grid-template-columns: 1fr minmax(300px, min(42vw, 520px));
  gap: 28px;
  align-items: center;
}

@media (max-width: 900px) {
  .type-analysis-grid {
    grid-template-columns: 1fr;
  }
}

.type-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  th,
  td {
    border: 1px solid #e2e8f0;
    padding: 10px 12px;
    text-align: center;
  }
  th {
    background: #f8fafc;
    font-weight: 600;
    color: #475569;
  }
  tbody tr:last-child td {
    font-weight: 700;
    background: #f1f5f9;
  }
}

.chart-wrap {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 20px 32px;
}

@media (max-width: 520px) {
  .chart-wrap {
    flex-direction: column;
  }
}

.donut-wrap {
  position: relative;
  width: 200px;
  height: 200px;
}

.donut-ring {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.donut-center {
  position: absolute;
  inset: 22%;
  background: #fff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 1px #e2e8f0;
}

.donut-label {
  font-size: 12px;
  color: #64748b;
}

.donut-pct {
  font-size: 22px;
  color: #0f172a;
}

.chart-legend {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1 1 200px;
  min-width: 0;
  max-width: 120px;
}

.chart-legend li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  white-space: nowrap;
}

.leg-dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  flex-shrink: 0;
}

.leg-text {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.leg-name {
  flex: 0 1 auto;
}

.leg-rate {
  font-weight: 600;
  color: #0f172a;
  flex-shrink: 0;
}

:deep(.q-collapse) {
  border: none;
}
:deep(.q-collapse .el-collapse-item__header) {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  background: #f8fafc;
  border-radius: 8px;
  padding: 0 14px;
  margin-bottom: 10px;
  border: 1px solid #e2e8f0;
}
:deep(.q-collapse .el-collapse-item__wrap) {
  border: none;
  background: transparent;
}
:deep(.q-collapse .el-collapse-item__content) {
  padding-bottom: 8px;
}

.q-card {
  display: grid;
  grid-template-columns: 28px 1fr;
  column-gap: 10px;
  align-items: start;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 12px;
  background: #fff;
  &.correct {
    border-color: #bbf7d0;
  }
  &.wrong {
    border-color: #fecaca;
  }
  &.pending {
    border-color: #fde68a;
  }
  &.unanswered {
    border-color: #e2e8f0;
  }
}

.q-main {
  min-width: 0;
}

.q-title-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}

.q-stem-wrap {
  flex: 1;
  min-width: 0;
}

.q-no {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #4f46e5;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  margin-top: 2px;
}

.q-title-row .q-badge {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}
.b-ok {
  background: #dcfce7;
  color: #15803d;
}
.b-bad {
  background: #fee2e2;
  color: #b91c1c;
}
.b-wait {
  background: #fef3c7;
  color: #b45309;
}
.b-skip {
  background: #f1f5f9;
  color: #64748b;
}

.q-stem {
  font-size: 15px;
  line-height: 1.75;
  color: #1e293b;
  margin: 0;
  :deep(img) {
    max-width: 100%;
    height: auto;
  }
}

.q-options-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
  margin-bottom: 12px;
}

/* 成绩详情 / 查看答卷：单选 / 多选 / 判断 选项纵向排列 */
.q-options-row--vertical {
  flex-direction: column;
  flex-wrap: nowrap;
  align-items: stretch;
  gap: 10px;
}

.q-options-row--vertical .q-opt {
  width: 100%;
  box-sizing: border-box;
  align-items: flex-start;
}

.q-opt {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  font-size: 14px;
  color: #334155;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  &.ok {
    border-color: #86efac;
    background: #f0fdf4;
  }
  &.bad {
    border-color: #fca5a5;
    background: #fef2f2;
  }
  &.picked {
    border-color: #93c5fd;
    background: #eff6ff;
  }
  &.answer-only {
    border-color: #93c5fd;
    background: #eff6ff;
  }
}

.q-opt-code {
  font-weight: 700;
  color: #4f46e5;
}

.q-compare {
  margin-bottom: 10px;
  font-size: 14px;
}
.q-compare-row {
  margin-bottom: 6px;
}
.q-compare .lbl {
  color: #64748b;
}
.q-compare .val.is-correct {
  color: #15803d;
  font-weight: 600;
}
.q-compare .val.is-wrong {
  color: #b91c1c;
}
.q-compare .val.is-pending {
  color: #b45309;
}

.q-muted {
  font-size: 13px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.q-answer-compare {
  margin-top: 14px;
  padding: 14px 14px 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  font-size: 14px;
  color: #334155;
}

.q-answer-compare--sheet {
  margin-top: 12px;
  padding: 12px 14px;
}

.q-answer-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
  line-height: 1.55;
}

.q-answer-line:last-of-type {
  margin-bottom: 0;
}

.q-answer-label {
  flex: 0 0 auto;
  font-weight: 700;
  color: #0f172a;
  min-width: 5em;
}

.q-answer-val {
  flex: 1 1 auto;
  min-width: 0;
  word-break: break-word;
}

.q-answer-val.is-reference {
  color: #15803d;
  font-weight: 600;
}

.q-answer-pending {
  margin-top: 10px;
  margin-bottom: 0;
}

.q-pending-review-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #92400e;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 8px;
  padding: 10px 12px;
  line-height: 1.45;
}

.q-pending-review-hint .el-icon {
  color: #d97706;
  flex-shrink: 0;
}

.q-analysis {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
  font-size: 14px;
  color: #475569;
}
.q-analysis-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #334155;
}

.q-teacher-comment {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
  font-size: 14px;
  color: #475569;
}

.q-foot-score {
  text-align: right;
  font-size: 14px;
  color: #64748b;
  margin-top: 10px;
}
.q-foot-score .earned {
  color: #15803d;
  font-weight: 700;
}
.q-foot-score .slash {
  color: #cbd5e1;
}
</style>
