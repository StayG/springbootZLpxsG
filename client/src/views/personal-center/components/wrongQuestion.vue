<template>
  <div class="wrongquestion-container" v-loading="loading">
    <el-alert
      v-if="filters.examPaperId"
      type="info"
      show-icon
      closable
      class="paper-filter-alert"
      title="当前仅显示本场考试关联错题"
      @close="clearExamPaperFilter"
    />
    <div class="overview-grid">
      <div class="overview-card">
        <div class="icon blue"><el-icon><Document /></el-icon></div>
        <div>
          <div class="label">错题总数</div>
          <div class="value">{{ summary.totalWrong }} <span>题</span></div>
          <div v-if="summary.repeatedWrong > 0" class="sub-info">其中重复错题：{{ summary.repeatedWrong }} 题</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon red"><el-icon><Tickets /></el-icon></div>
        <div>
          <div class="label">涉及考试</div>
          <div class="value">{{ summary.paperCount }} <span>份</span></div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon orange"><el-icon><TrendCharts /></el-icon></div>
        <div>
          <div class="label">掌握度</div>
          <div class="value">{{ summary.masteryPercent }} <span>%</span></div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon green"><el-icon><Grid /></el-icon></div>
        <div>
          <div class="label">涉及题型</div>
          <div class="value">{{ summary.questionTypesCount }} <span>种</span></div>
        </div>
      </div>
    </div>

    <div class="board">
      <div class="filter-panel">
        <el-select v-model="filters.masteryStatus" placeholder="掌握状态" clearable class="filter-item mastery-item" @change="handleFilterChange">
          <el-option label="全部状态" value="" />
          <el-option label="已掌握" value="1" />
          <el-option label="未掌握" value="0" />
        </el-select>
        <el-checkbox v-model="filters.repeatedWrong" class="filter-item repeated-checkbox" @change="handleFilterChange">
          仅重复错题
        </el-checkbox>
        <el-select v-model="filters.questionType" placeholder="全部题型" clearable class="filter-item select-item" @change="handleFilterChange">
          <el-option label="全部题型" value="" />
          <el-option v-for="item in questionTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.source" placeholder="全部来源" clearable class="filter-item source-item" @change="handleFilterChange">
          <el-option label="全部来源" value="" />
          <el-option v-for="item in sourceOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-date-picker
          v-model="filters.date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="全部时间"
          clearable
          class="filter-item date-item"
          @change="handleFilterChange"
        />
        <el-input
          v-model="filters.keyword"
          class="filter-item keyword-input"
          placeholder="搜索题目内容"
          clearable
          :suffix-icon="Search"
          @keyup.enter="handleFilterChange"
          @clear="handleFilterChange"
        />
      </div>

      <div class="list-header">
        <div class="tabs">
          <span :class="['tab-item', { active: groupBy === 'question' }]" @click="changeGroupBy('question')">按题目</span>
          <span :class="['tab-item', { active: groupBy === 'paper' }]" @click="changeGroupBy('paper')">按考试</span>
        </div>
        <el-select v-model="sortType" class="sort-select" @change="handleSortChange">
          <el-option label="最新错题" value="latest" />
          <el-option label="最早错题" value="oldest" />
        </el-select>
      </div>

      <div class="board-body">
        <div v-if="dataList.length" ref="listScrollEl" class="board-list">
          <div class="question-list">
            <template v-for="(row, idx) in dataList" :key="row.id || `${idx}-${row.examQuestionName}`">
              <div
                v-if="groupBy === 'paper' && (idx === 0 || getSourceLabel(dataList[idx - 1]) !== getSourceLabel(row))"
                class="paper-group-heading"
              >
                {{ getSourceLabel(row) }}
              </div>
              <div class="question-card">
                <div class="card-main">
                  <div class="question-layout">
                    <div class="question-index">
                      <span class="index">{{ getQuestionIndex(idx) }}.</span>
                    </div>
                    <div class="question-body">
                      <div class="question-stem">{{ extractPlainText(row.examQuestionName) || "暂无题目内容" }}</div>
                      <div v-show="isAnswerExpanded(row.id)" class="answer-analysis-block">
                        <div v-if="expandLoading[row.id]" class="expand-loading">加载中…</div>
                        <template v-else-if="expandDetail[row.id]">
                          <div class="answer-row correct-answer-row">
                            <span class="title">正确答案：</span>
                            <span class="correct-val" :class="{ 'answer-missing': !expandDetail[row.id].examQuestionAnswer }">
                              {{ expandDetail[row.id].examQuestionAnswer || "（答案数据缺失，请联系管理员补充）" }}
                            </span>
                          </div>
                          <div class="analysis-row">
                            <span class="title analysis-label">解析：</span>
                            <div class="analysis-body rich-text-content" v-html="getRowAnalysisHtml(expandDetail[row.id])" />
                          </div>
                        </template>
                      </div>
                      
                      <!-- 历史错误记录 -->
                      <div v-if="shouldShowHistory(row)" class="history-section">
                        <div class="history-toggle" @click="toggleHistoryExpand(row)">
                          <span class="history-toggle-text">
                            <el-icon class="history-icon"><Clock /></el-icon>
                            <template v-if="groupBy === 'question'">
                              查看历史错误记录（共 {{ row.wrongCount }} 次）
                            </template>
                            <template v-else>
                              查看该考试下的错误记录
                            </template>
                          </span>
                          <el-icon class="toggle-arrow" :class="{ expanded: historyExpanded[row.id] }">
                            <ArrowRight />
                          </el-icon>
                        </div>
                        <div v-show="historyExpanded[row.id]" class="history-content">
                          <div v-if="historyLoading[row.id]" class="history-loading">加载中…</div>
                          <template v-else-if="getFilteredHistory(row).length">
                            <div v-for="(record, hIdx) in getFilteredHistory(row)" :key="record.id" class="history-item">
                              <div class="history-item-header">
                                <span class="history-index">第 {{ hIdx + 1 }} 次</span>
                                <span class="history-time">{{ formatHistoryTime(record.insertTime) }}</span>
                              </div>
                              <div class="history-item-body">
                                <div v-if="groupBy === 'question'" class="history-row">
                                  <span class="history-label">来源：</span>
                                  <span class="history-val">{{ record.examName || record.examPaperName || "-" }}</span>
                                </div>
                                <div class="history-row">
                                  <span class="history-label">错误答案：</span>
                                  <span class="history-val wrong">{{ record.examDetailsMyanswer || "-" }}</span>
                                </div>
                                <div v-if="record.examDetailsMyscore != null" class="history-row">
                                  <span class="history-label">得分：</span>
                                  <span class="history-val">{{ record.examDetailsMyscore }} 分</span>
                                </div>
                              </div>
                            </div>
                          </template>
                          <div v-else class="history-empty">暂无历史记录</div>
                        </div>
                      </div>
                      
                      <div class="meta-row">
                        <span>来源：{{ getSourceLabel(row) }}</span>
                        <span>题型：{{ row.examQuestionTypesName || "-" }}</span>
                        <span>错误时间：{{ formatWrongTimeBeijing(row.insertTime) }}</span>
                      </div>
                    </div>
                    <div class="question-tags">
                      <span v-if="Number(row.masteryStatus) === 1" class="mastery-pill mastered">已掌握</span>
                      <span v-else class="mastery-pill not-mastered">未掌握</span>
                      <span v-if="shouldShowRepeatedTag(row)" class="repeated-pill">⚠️ 重复错 ×{{ getRepeatedCount(row) }}</span>
                    </div>
                  </div>
                </div>
                <div class="card-actions">
                  <el-button class="action-btn" type="default" plain @click="toggleAnswerExpand(row)">
                    {{ isAnswerExpanded(row.id) ? "收起解析" : "解析" }}
                  </el-button>
                  <el-button class="action-btn" type="primary" plain @click="handleRetry(row)">重做</el-button>
                </div>
              </div>
            </template>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="暂无错题数据" />
        </div>

        <div class="pagination-section" v-if="dataList.length > 0">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
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
import { computed, onMounted, ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { useUserStore } from "@/stores/user";
import { useRoute, useRouter } from "vue-router";
import { Document, Grid, Search, Tickets, TrendCharts } from "@element-plus/icons-vue";
import { ArrowRight, Clock } from "@element-plus/icons-vue";

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const dataList = ref([]);
const allList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const listScrollEl = ref(null);
const loading = ref(false);
const groupBy = ref("question");
const sortType = ref("latest");

const filters = ref({
  masteryStatus: "",
  questionType: "",
  source: "",
  date: "",
  keyword: "",
  /** 来自「我的成绩」等页：仅展示某试卷错题 */
  examPaperId: "",
  /** 重复错题筛选 */
  repeatedWrong: false,
});

/** 列表项「解析」按需拉取详情 */
const expandDetail = ref({});
const expandLoading = ref({});
const expandOpenIds = ref({});

/** 历史记录展开状态和数据 */
const historyExpanded = ref({});
const historyLoading = ref({});
const historyData = ref({});

const rowMasteryNum = (row) => {
  const v = row?.masteryStatus;
  if (v == null || v === "") return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
};

const isAnswerExpanded = (id) => !!(id != null && expandOpenIds.value[id]);

const formatWrongScore = (row) => {
  const s = row?.examDetailsMyscore;
  if (s == null || s === "") return "—";
  return `${s} 分`;
};

const toggleAnswerExpand = async (row) => {
  const id = row?.id;
  if (id == null) return;
  if (expandOpenIds.value[id]) {
    expandOpenIds.value = { ...expandOpenIds.value, [id]: false };
    return;
  }
  expandOpenIds.value = { ...expandOpenIds.value, [id]: true };
  if (expandDetail.value[id]) return;
  expandLoading.value = { ...expandLoading.value, [id]: true };
  try {
    const { data } = await request.get(`/examWrongQuestion/info/${id}`);
    console.log('错题详情数据:', data);
    console.log('正确答案字段:', data?.examQuestionAnswer);
    expandDetail.value = { ...expandDetail.value, [id]: data || {} };
  } catch (e) {
    console.error(e);
    ElMessage.error("加载答案失败");
    expandOpenIds.value = { ...expandOpenIds.value, [id]: false };
  } finally {
    expandLoading.value = { ...expandLoading.value, [id]: false };
  }
};

const toggleHistoryExpand = async (row) => {
  const id = row?.id;
  if (id == null) return;
  
  if (historyExpanded.value[id]) {
    historyExpanded.value = { ...historyExpanded.value, [id]: false };
    return;
  }
  
  historyExpanded.value = { ...historyExpanded.value, [id]: true };
  
  // 如果已经加载过，直接返回
  if (historyData.value[id]) return;
  
  historyLoading.value = { ...historyLoading.value, [id]: true };
  try {
    const { data } = await request.get(`/examWrongQuestion/history/${id}`);
    historyData.value = { ...historyData.value, [id]: data || [] };
  } catch (e) {
    console.error(e);
    ElMessage.error("加载历史记录失败");
    historyExpanded.value = { ...historyExpanded.value, [id]: false };
  } finally {
    historyLoading.value = { ...historyLoading.value, [id]: false };
  }
};

const formatHistoryTime = (time) => {
  if (!time) return "-";
  const d = parseWrongQuestionInstant(time);
  if (!d) return String(time);
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(d);
};

/** 计算题目序号：按题目分组时全局编号，按考试分组时每个考试内部编号 */
const getQuestionIndex = (idx) => {
  if (groupBy.value === "question") {
    // 按题目分组：全局编号
    return (currentPage.value - 1) * pageSize.value + idx + 1;
  } else {
    // 按考试分组：每个考试内部编号
    let count = 1;
    const currentSource = getSourceLabel(dataList.value[idx]);
    for (let i = 0; i < idx; i++) {
      if (getSourceLabel(dataList.value[i]) === currentSource) {
        count++;
      }
    }
    return count;
  }
};

/** 判断是否显示历史记录入口 */
const shouldShowHistory = (row) => {
  if (groupBy.value === "question") {
    // 按题目分组：显示该题的所有历史（错误次数>=2）
    return row.wrongCount >= 2;
  } else {
    // 按考试分组：始终显示（用于查看该考试下的历史）
    return true;
  }
};

/** 判断是否显示重复错标签 */
const shouldShowRepeatedTag = (row) => {
  if (groupBy.value === "question") {
    // 按题目分组：显示总的错误次数
    return row.wrongCount >= 2;
  } else {
    // 按考试分组：不显示（因为按考试看，每条都是独立的）
    return false;
  }
};

/** 获取重复错误次数 */
const getRepeatedCount = (row) => {
  return row.wrongCount || 0;
};

/** 获取过滤后的历史记录（按考试分组时只显示该考试的记录） */
const getFilteredHistory = (row) => {
  const history = historyData.value[row.id] || [];
  
  if (groupBy.value === "question") {
    // 按题目分组：显示所有历史
    return history;
  } else {
    // 按考试分组：只显示该考试的记录
    const currentSource = getSourceLabel(row);
    return history.filter(record => {
      const recordSource = record.examName || record.examPaperName || "";
      return recordSource === currentSource;
    });
  }
};

const safeString = (value) => String(value || "").trim();

/** 从HTML中提取纯文本 */
const extractPlainText = (html) => {
  if (!html) return "";
  const text = String(html);
  // 移除所有HTML标签
  const withoutTags = text.replace(/<[^>]+>/g, " ");
  // 移除多余空格
  const cleaned = withoutTags.replace(/\s+/g, " ").trim();
  return cleaned;
};

/** 无偏移量时按东八区墙钟解析（与后端 Jackson 常用 GMT+8 一致），避免在非中国本地时区被误解析 */
const parseWrongQuestionInstant = (value) => {
  if (value == null || value === "") return null;
  if (typeof value === "number" && Number.isFinite(value)) {
    const ms = value < 1e12 ? value * 1000 : value;
    const d = new Date(ms);
    return Number.isNaN(d.getTime()) ? null : d;
  }
  const raw = String(value).trim();
  if (!raw) return null;
  if (/^\d{10}$/.test(raw) || /^\d{13}$/.test(raw)) {
    const n = Number(raw);
    const ms = raw.length <= 10 ? n * 1000 : n;
    const d = new Date(ms);
    return Number.isNaN(d.getTime()) ? null : d;
  }
  if (/[zZ]|[+-]\d{2}:?\d{2}$/.test(raw)) {
    const d = new Date(raw);
    return Number.isNaN(d.getTime()) ? null : d;
  }
  const m = raw.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2})(?:\.\d+)?)?/);
  if (m) {
    const [, y, mo, d, h, mi, sec = "00"] = m;
    const inst = new Date(`${y}-${mo}-${d}T${h}:${mi}:${sec}+08:00`);
    return Number.isNaN(inst.getTime()) ? null : inst;
  }
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
};

const toDate = (value) => parseWrongQuestionInstant(value);

/** 列表「错误时间」统一按北京时间展示 */
const formatWrongTimeBeijing = (value) => {
  const d = parseWrongQuestionInstant(value);
  if (!d) return value ? String(value) : "-";
  return new Intl.DateTimeFormat("sv-SE", {
    timeZone: "Asia/Shanghai",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(d);
};

/** 北京时间日历日 yyyy-mm-dd，用于筛选「全部时间」 */
const beijingCalendarDayKey = (instant) => {
  if (!instant) return "";
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Shanghai",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(instant);
};

const getSourceLabel = (row) => {
  // 优先使用考试名称，如果没有则使用试卷名称
  const examName = safeString(row.examName);
  if (examName) return examName;
  return safeString(row.examPaperName || row.sourceName || row.source || "-");
};

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "");
};

const getRowAnalysisHtml = (row) => {
  const raw = row?.examQuestionAnalysis ?? row?.exam_question_analysis;
  const sanitized = formatRichText(raw);
  const plain = String(sanitized).replace(/<[^>]+>/g, "").trim();
  if (!plain) {
    return '<span class="analysis-empty">暂无解析</span>';
  }
  return sanitized;
};

const matchDate = (row) => {
  if (!filters.value.date) return true;
  const target = toDate(row.insertTime);
  if (!target) return false;
  return beijingCalendarDayKey(target) === filters.value.date;
};

const questionTypeOptions = computed(() => {
  return [...new Set(allList.value.map((item) => safeString(item.examQuestionTypesName)).filter(Boolean))];
});

const sourceOptions = computed(() => {
  return [...new Set(allList.value.map((item) => getSourceLabel(item)).filter((item) => item && item !== "-"))];
});

const summary = computed(() => {
  // 统计基于原始数据（不去重）
  const examSet = new Set();
  const typeSet = new Set();
  const questionSet = new Set(); // 用于统计不重复的题目数（用于重复错题统计）
  let mastered = 0;
  let repeatedWrong = 0;
  
  allList.value.forEach((item) => {
    // 统计考试
    const examInfoId = item.examInfoId;
    if (examInfoId != null && examInfoId !== "") {
      examSet.add(`exam_${examInfoId}`);
    } else {
      const paperId = item.examPaperId;
      if (paperId != null && paperId !== "") {
        examSet.add(`paper_${paperId}`);
      }
    }
    
    // 统计题型
    const typeName = safeString(item.examQuestionTypesName);
    if (typeName) typeSet.add(typeName);
    
    // 统计掌握情况（基于记录数）
    if (rowMasteryNum(item) === 1) mastered += 1;
    
    // 统计不重复的题目（用于重复错题统计）
    const qid = item.examQuestionId;
    if (qid && item.wrongCount >= 2) {
      questionSet.add(qid);
    }
  });
  
  repeatedWrong = questionSet.size;
  
  const totalWrong = allList.value.length; // 原始记录数
  const masteryPercent =
    totalWrong === 0 ? 100 : Math.min(100, Math.round((mastered / totalWrong) * 100));
  
  return {
    totalWrong, // 原始记录数
    paperCount: examSet.size,
    masteryPercent,
    questionTypesCount: typeSet.size,
    repeatedWrong, // 不重复的重复错题数
  };
});

const buildFilteredList = () => {
  let list = [...allList.value];

  // 只有在"按题目"分组时才去重
  if (groupBy.value === "question") {
    const questionMap = new Map();
    list.forEach((item) => {
      const qid = item.examQuestionId;
      if (!qid) return;
      
      if (!questionMap.has(qid)) {
        // 第一次遇到这道题，直接添加
        questionMap.set(qid, item);
      } else {
        // 已存在，比较时间，保留最新的
        const existing = questionMap.get(qid);
        const existingTime = toDate(existing.insertTime)?.getTime() || 0;
        const currentTime = toDate(item.insertTime)?.getTime() || 0;
        if (currentTime > existingTime) {
          questionMap.set(qid, item);
        }
      }
    });
    
    // 转换为数组
    list = Array.from(questionMap.values());
  }
  // 按考试分组时不去重，保留所有记录

  list = list.filter((item) => {
    const keyword = safeString(filters.value.keyword);
    const questionName = safeString(item.examQuestionName);
    const questionType = safeString(item.examQuestionTypesName);
    const source = getSourceLabel(item);
    const hitKeyword = !keyword || questionName.includes(keyword);
    const hitType = !filters.value.questionType || filters.value.questionType === questionType;
    const hitSource = !filters.value.source || filters.value.source === source;
    const pid = safeString(filters.value.examPaperId);
    const hitPaper = !pid || safeString(item.examPaperId) === pid;
    const ms = filters.value.masteryStatus;
    const hitMastery =
      ms === "" || ms === null || ms === undefined ? true : Number(ms) === rowMasteryNum(item);
    const hitRepeated = !filters.value.repeatedWrong || (item.wrongCount >= 2);
    return hitKeyword && hitType && hitSource && hitPaper && matchDate(item) && hitMastery && hitRepeated;
  });

  list.sort((a, b) => {
    const dateA = toDate(a.insertTime)?.getTime() || 0;
    const dateB = toDate(b.insertTime)?.getTime() || 0;
    if (sortType.value === "oldest") return dateA - dateB;
    return dateB - dateA;
  });

  if (groupBy.value === "paper") {
    list.sort((a, b) => {
      const sourceA = getSourceLabel(a);
      const sourceB = getSourceLabel(b);
      return sourceA.localeCompare(sourceB, "zh-CN");
    });
  }

  total.value = list.length;
  const start = (currentPage.value - 1) * pageSize.value;
  dataList.value = list.slice(start, start + pageSize.value);
};

const scrollListToTop = async () => {
  await nextTick();
  const el = listScrollEl.value;
  if (el) el.scrollTop = 0;
};

const FETCH_LIMIT = 500;
/** 与 wrongQuestionRedo 页约定一致 */
const WRONG_REDO_STORAGE_KEY = "studentWrongRedoIds";

const loadExamWrongQuestion = async () => {
  loading.value = true;
  try {
    const usersId = userStore.userInfo?.id;
    let pageNum = 1;
    const merged = [];
    while (pageNum < 500) {
      const { data } = await request.post("/examWrongQuestion/page", {
        page: pageNum,
        limit: FETCH_LIMIT,
        usersId,
      });
      const batch = data?.list || [];
      merged.push(...batch);
      const totalRows = data?.total != null ? Number(data.total) : merged.length;
      if (batch.length < FETCH_LIMIT || merged.length >= totalRows) break;
      pageNum += 1;
    }
    allList.value = merged;
    buildFilteredList();
    scrollListToTop();
  } catch (error) {
    console.error("获取错题列表失败:", error);
    ElMessage.error("网络错误，获取错题失败");
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = async () => {
  currentPage.value = 1;
  buildFilteredList();
  await scrollListToTop();
};

const handleCurrentChange = async (val) => {
  currentPage.value = val;
  buildFilteredList();
  await scrollListToTop();
};

const changeGroupBy = (value) => {
  groupBy.value = value;
  handleFilterChange();
};

const handleSortChange = () => {
  handleFilterChange();
};

const handleRetry = (row) => {
  if (row?.id == null) {
    ElMessage.warning("无法识别该错题记录");
    return;
  }
  try {
    sessionStorage.setItem(WRONG_REDO_STORAGE_KEY, JSON.stringify([row.id]));
  } catch {
    ElMessage.error("无法开启重做，请检查浏览器存储权限");
    return;
  }
  router.push({ path: "/index/wrongQuestionRedo" });
};

const syncExamPaperFilterFromRoute = () => {
  const q = route.query?.examPaperId;
  filters.value.examPaperId = q != null && String(q).trim() !== "" ? String(q).trim() : "";
  currentPage.value = 1;
  buildFilteredList();
  scrollListToTop();
};

const clearExamPaperFilter = () => {
  filters.value.examPaperId = "";
  const q = { ...route.query };
  delete q.examPaperId;
  router.replace({ path: route.path, query: q });
};

watch(
  () => route.query.examPaperId,
  () => {
    syncExamPaperFilterFromRoute();
  }
);

onMounted(() => {
  syncExamPaperFilterFromRoute();
  loadExamWrongQuestion();
});
</script>

<style lang="scss" scoped>
.wrongquestion-container {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.paper-filter-alert {
  flex-shrink: 0;
  margin-bottom: 14px;
  border-radius: 10px;
}

.overview-grid {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
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
  padding: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
}

.board-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.board-list {
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
    display: none;
  }
}

.empty-state {
  flex: 1;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 12px;
  padding: 26px 20px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  .icon {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28px;
    font-weight: 700;

    &.blue {
      background: #4f46e5;
    }
    &.red {
      background: #ff6b6b;
    }
    &.orange {
      background: #ffb020;
    }
    &.green {
      background: #28c191;
    }
  }

  .label {
    font-size: 22px;
    color: #666;
    font-weight: 500;
    line-height: 1;
    margin-bottom: 8px;
  }

  .value {
    font-size: 44px;
    font-weight: 700;
    color: #202733;
    line-height: 1;

    span {
      font-size: 20px;
      color: #666;
      margin-left: 4px;
      font-weight: 500;
    }
  }

  .sub-info {
    margin-top: 6px;
    font-size: 13px;
    color: #94a3b8;
    line-height: 1.4;
  }
}

.filter-panel {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;

  .filter-item {
    height: 40px;
  }

  .select-item {
    width: 150px;
  }

  .mastery-item {
    width: 140px;
  }

  .repeated-checkbox {
    height: 40px;
    padding: 0 12px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    background: #fff;
    display: flex;
    align-items: center;

    :deep(.el-checkbox__label) {
      font-size: 14px;
      color: #334155;
      font-weight: 500;
    }

    &:hover {
      border-color: #c7d2fe;
      background: #fafbff;
    }
  }

  .source-item {
    width: 150px;
  }

  .date-item {
    width: 180px;
  }

  .keyword-input {
    margin-left: auto;
    width: 240px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    height: 40px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
  }

  :deep(.el-date-editor.el-input),
  :deep(.el-date-editor.el-input__wrapper) {
    height: 40px;
  }

  :deep(.el-date-editor .el-input__wrapper) {
    padding: 1px 11px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
  }

  :deep(.el-input__inner),
  :deep(.el-select__selected-item) {
    font-size: 14px;
  }
}

.list-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .tabs {
    display: flex;
    gap: 18px;

    .tab-item {
      cursor: pointer;
      padding-bottom: 8px;
      color: #606266;
      border-bottom: 2px solid transparent;

      &.active {
        color: #4f46e5;
        border-bottom-color: #4f46e5;
        font-weight: 600;
      }
    }
  }

  .sort-select {
    width: 130px;
  }
}

.question-list {
  display: grid;
  gap: 12px;
}

.paper-group-heading {
  grid-column: 1 / -1;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  padding: 10px 4px 4px;
  border-bottom: 1px solid #e7ecf4;

  &:first-child {
    padding-top: 0;
  }
}

.question-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid #e7ecf4;
  border-radius: 10px;
  background: #fff;
  transition: all 0.24s ease;

  &:hover {
    border-color: #c7d2fe;
    box-shadow: 0 10px 20px rgba(79, 70, 229, 0.1);
    transform: translateY(-2px);
  }

  .card-main {
    flex: 1;
  }

  .question-layout {
    display: grid;
    grid-template-columns: auto 1fr auto;
    column-gap: 12px;
    row-gap: 0;
    align-items: start;
  }

  .question-index {
    display: flex;
    align-items: flex-start;
    padding-top: 2px;

    .index {
      font-size: 16px;
      font-weight: 600;
      line-height: 1.6;
      color: #4f46e5;
    }
  }

  .question-tags {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
    padding-top: 2px;

    .mastery-pill {
      flex-shrink: 0;
      font-size: 12px;
      font-weight: 700;
      line-height: 1.4;
      padding: 3px 10px;
      border-radius: 6px;
      letter-spacing: 0.02em;
    }

    .mastery-pill.not-mastered {
      color: #dc2626;
      background: #fef2f2;
      border: 1px solid #fecaca;
    }

    .mastery-pill.mastered {
      color: #15803d;
      background: #ecfdf5;
      border: 1px solid #bbf7d0;
    }

    .repeated-pill {
      flex-shrink: 0;
      font-size: 12px;
      font-weight: 700;
      line-height: 1.4;
      padding: 3px 10px;
      border-radius: 6px;
      letter-spacing: 0.02em;
      color: #ea580c;
      background: #fff7ed;
      border: 1px solid #fed7aa;
    }
  }

  .question-body {
    min-width: 0;
  }

  .question-stem {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    line-height: 1.6;
    margin-bottom: 10px;
  }

  .answer-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
    color: #606266;

    .title {
      color: #606266;
    }
    .mine {
      color: #f56c6c;
      font-weight: 600;
    }
  }

  .score-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
    color: #606266;

    .score-val {
      color: #334155;
      font-weight: 600;
    }
  }

  .answer-analysis-block {
    margin-bottom: 10px;
  }

  .expand-loading {
    font-size: 13px;
    color: #909399;
    padding: 4px 0;
  }

  .correct-answer-row .correct-val {
    color: #16a34a;
    font-weight: 600;
  }

  .analysis-row {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 10px;
    color: #606266;

    .analysis-label {
      flex-shrink: 0;
      padding-top: 2px;
    }

    .analysis-body {
      flex: 1;
      min-width: 0;
      font-size: 14px;
      line-height: 1.6;
      color: #334155;
      font-weight: 500;

      :deep(img) {
        max-width: 100%;
        height: auto;
      }
    }

    :deep(.analysis-empty) {
      color: #909399;
      font-weight: 500;
    }
  }

  .meta-row {
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    color: #909399;
    font-size: 13px;
  }

  .card-actions {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
    flex-shrink: 0;

    .action-btn {
      margin: 0;
      min-width: 88px;
      padding: 8px 16px;
      border-radius: 8px;
      font-weight: 600;
    }
  }
}

.pagination-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding: 8px 4px 4px;
  border-top: 1px solid #eff3f8;
}

.total-text {
  color: #8b94a3;
  font-size: 13px;
}

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

.history-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.history-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.history-toggle:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.history-toggle-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.history-icon {
  font-size: 16px;
  color: #64748b;
}

.toggle-arrow {
  font-size: 14px;
  color: #94a3b8;
  transition: transform 0.2s ease;
}

.toggle-arrow.expanded {
  transform: rotate(90deg);
}

.history-content {
  margin-top: 10px;
  padding: 0 4px;
}

.history-loading,
.history-empty {
  padding: 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.history-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e2e8f0;
}

.history-index {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.history-time {
  font-size: 12px;
  color: #94a3b8;
}

.history-item-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
}

.history-label {
  flex-shrink: 0;
  color: #64748b;
  font-weight: 500;
  min-width: 70px;
}

.history-val {
  flex: 1;
  color: #334155;
  font-weight: 500;
}

.history-val.wrong {
  color: #dc2626;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-panel {
    .keyword-input {
      margin-left: 0;
      width: 100%;
    }
  }

  .question-card {
    flex-direction: column;
    align-items: stretch;

    .card-actions {
      justify-content: center;
      width: 100%;
      padding-top: 14px;
    }
  }
}
</style>
