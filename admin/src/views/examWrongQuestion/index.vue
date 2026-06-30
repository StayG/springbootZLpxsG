<template>
  <div class="module-container">
    <div
      v-if="showIndexFlag"
      v-loading="listLoading"
      class="wrong-question-index"
      element-loading-text="加载错题数据中..."
      element-loading-background="rgba(255,255,255,0.65)"
    >
    <div class="page-title">错题本</div>

    <div class="tip-card">
      错题本汇总了考生在考试中做错的所有题目，便于分析薄弱知识点，优化刷题和教学内容。
    </div>

    <div class="filter-card">
      <div class="filter-grid">
        <div class="filter-item">
          <div class="label">考试名称</div>
          <el-input v-model="filters.examName" placeholder="请输入考试名称" clearable />
        </div>
        <div class="filter-item">
          <div class="label">题目类型</div>
          <el-select v-model="filters.questionType" placeholder="请选择题目类型" clearable>
            <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">题目难度</div>
          <el-select v-model="filters.difficulty" placeholder="请选择难度" clearable>
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">知识点</div>
          <el-select v-model="filters.knowledgePoint" placeholder="请选择知识点" clearable>
            <el-option v-for="item in knowledgePointOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">开始时间</div>
          <el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
        </div>
        <div class="filter-item">
          <div class="label">结束时间</div>
          <el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
        </div>
        <div class="filter-item">
          <div class="label">关键字</div>
          <el-input v-model="filters.keyword" placeholder="请输入题干关键词" clearable />
        </div>
      </div>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div v-for="item in statCards" :key="item.key" class="stat-card">
        <div class="stat-top">
          <div class="icon-wrap" :class="item.iconClass">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-main">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div class="table-header">
        <div class="table-title">错题列表（共 {{ total }} 条）</div>
        <div class="table-actions">
          <el-button class="export-btn" @click="handleExport">导出错题</el-button>
          <el-button
            v-if="utils.isAuth('examWrongQuestion', '删除')"
            class="batch-delete-btn"
            :disabled="multipleSelection.length === 0"
            @click="handleBatchDelete"
          >
            批量移除
          </el-button>
        </div>
      </div>

      <el-table :data="pageList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" header-align="center" />
        <el-table-column label="题目内容" min-width="320" header-align="center" align="left">
          <template #default="{ row }">
            {{ truncateText(row.examQuestionName, 48) }}
          </template>
        </el-table-column>
        <el-table-column label="科目" width="110" align="center" header-align="center">
          <template #default="{ row }">
            {{ getSubjectLabel(row) }}
          </template>
        </el-table-column>
        <el-table-column label="题型" width="110" align="center" header-align="center">
          <template #default="{ row }">
            {{ getTypeLabel(row) }}
          </template>
        </el-table-column>
        <el-table-column label="知识点" min-width="160" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.knowledgePoint || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getDifficultyTagType(row.difficultyLevel)">
              {{ getDifficultyLabel(row.difficultyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属考试" min-width="160" align="center" header-align="center">
          <template #default="{ row }">
            <span v-if="(row.examInfoIds || []).length > 1">多场考试</span>
            <span v-else>{{ (row.examNames || [])[0] || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="错误人数" width="100" align="center" header-align="center" prop="wrongUserCount" />
        <el-table-column label="错误率" width="100" align="center" header-align="center">
          <template #default="{ row }">
            <span class="error-rate">{{ formatPercent(row.wrongRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" header-align="center" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn detail-btn" size="small" @click="handleView(row)">查看详情</el-button>
            <el-button
              v-if="utils.isAuth('examWrongQuestion', '删除')"
              class="action-btn delete-btn"
              size="small"
              @click="handleDelete(row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.page"
        v-model:page-size="page.limit"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
      />
    </div>

    </div>
    <question-detail
      v-if="detailFlag"
      :question="currentDetailQuestion"
      :records="currentDetailRecords"
      :global-user-count="globalUserCount"
      :exam-name-by-info-id="examNameMapByExamInfoId"
      :exam-record-to-info-map="examRecordToInfoMap"
      :kemu-options="kemuOptions"
      @close="handleCloseDetail"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { CollectionTag, InfoFilled, Tickets, UserFilled } from "@element-plus/icons-vue";
import request from "@/utils/request";
import QuestionDetail from "./question-detail.vue";
import utils from "@/utils/utils.js";
import * as XLSX from "xlsx";
import { getKemuOptions } from "@/utils/dictionary";
import storage from "@/utils/storage";
import config from "@/config/config";
import { useUserStore } from "@/stores/user";

const rawList = ref([]);
const examQuestionList = ref([]);
const examPaperList = ref([]);
const examInfoList = ref([]);
const examRecordList = ref([]); // 新增：存储考试记录数据
const kemuOptions = ref([]);
const examOptions = ref([]);
const knowledgePointOptions = ref([]);
const multipleSelection = ref([]);
const showIndexFlag = ref(true);
const listLoading = ref(false);

const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY);
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY);
const isTeacherRole = ref(storedRole === "teachers" || storedRoleName === "教师");
const userStore = useUserStore();
const teacherKemuTypes = ref(null);
if (isTeacherRole.value) {
  const v = Number(userStore.userInfo?.kemuTypes);
  teacherKemuTypes.value = Number.isFinite(v) && v > 0 ? v : null;
}

const page = ref({
  page: 1,
  limit: 10,
});

const filters = ref({
  examName: "",
  questionType: "",
  difficulty: "",
  knowledgePoint: "",
  startDate: "",
  endDate: "",
  keyword: "",
});

const detailFlag = ref(false);
const currentDetailQuestionId = ref(null);

const questionTypeOptions = [
  { label: "单选题", value: 1 },
  { label: "多选题", value: 2 },
  { label: "判断题", value: 3 },
  { label: "填空题", value: 4 },
  { label: "简答题", value: 5 },
];

const typeLabelMap = {
  1: "单选题",
  2: "多选题",
  3: "判断题",
  4: "填空题",
  5: "简答题",
};

const questionMap = computed(() => {
  const map = new Map();
  examQuestionList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) {
      map.set(Number(item.id), item);
    }
  });
  return map;
});

const examPaperMap = computed(() => {
  const map = new Map();
  examPaperList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) {
      map.set(Number(item.id), item);
    }
  });
  return map;
});

// 新增：exam_record_id 到 exam_info_id 的映射
const examRecordToInfoMap = computed(() => {
  const map = new Map();
  examRecordList.value.forEach((record) => {
    const recordId = Number(record?.id);
    const infoId = Number(record?.examInfoId ?? record?.exam_info_id);
    if (recordId && infoId) {
      map.set(recordId, infoId);
    }
  });
  return map;
});

const examNameMapByExamInfoId = computed(() => {
  const map = new Map();
  // 使用 exam_info_id 来标识考试，这是真正的考试标识
  // 同一份试卷可以被多场考试使用，exam_info_id 才能区分不同的考试
  examInfoList.value.forEach((item) => {
    const examInfoId = Number(item?.id);
    if (examInfoId && item?.examName) {
      map.set(examInfoId, item.examName);
    }
  });
  return map;
});

const normalizeDateTime = (dateStr, endOfDay = false) => {
  if (!dateStr) return null;
  const suffix = endOfDay ? " 23:59:59" : " 00:00:00";
  const time = new Date(`${dateStr}${suffix}`).getTime();
  return Number.isNaN(time) ? null : time;
};

const extractExamPaperId = (row) => Number(row.examPaperId ?? row.exam_paper_id ?? 0) || null;
const extractQuestionId = (row) => Number(row.examQuestionId ?? row.exam_question_id ?? 0) || null;
const extractUserId = (row) => Number(row.usersId ?? row.users_id ?? 0) || null;
const extractExamRecordId = (row) => Number(row.examRecordId ?? row.exam_record_id ?? 0) || null;

const getTypeLabel = (row) => {
  const questionType = row.examQuestionTypesName || typeLabelMap[Number(row.examQuestionTypes)] || row.examQuestionTypes;
  if (questionType) return questionType;
  const question = questionMap.value.get(Number(row.examQuestionId));
  return question?.examQuestionTypesName || typeLabelMap[Number(question?.examQuestionTypes)] || "-";
};

const getSubjectLabel = (row) => {
  const paperIdForMap = Number(row.examPaperId) || Number((row.examPaperIds || [])[0]) || 0;
  const paper = paperIdForMap ? examPaperMap.value.get(paperIdForMap) : null;
  const kemuValue = Number(row.kemuTypes ?? paper?.kemuTypes);
  if (!kemuValue) return "-";
  const hit = kemuOptions.value.find((item) => Number(item.value) === kemuValue);
  return hit?.label || paper?.kemuTypesName || row.kemuTypesName || "-";
};

const getExamName = (row) => {
  const names = row.examNames || [];
  if (names.length > 1) return names.join("；");
  if (names.length === 1) return names[0];
  const pid = Number(row.examPaperId) || Number((row.examPaperIds || [])[0]) || 0;
  if (pid) {
    const examName = examNameMapByPaperId.value.get(pid);
    return examName || row.examPaperName || "-";
  }
  return row.examPaperName || "-";
};

const getDifficultyLabel = (difficulty) => {
  if (Number(difficulty) === 1) return "简单";
  if (Number(difficulty) === 2) return "中等";
  if (Number(difficulty) === 3) return "困难";
  return "-";
};

const getDifficultyTagType = (difficulty) => {
  if (Number(difficulty) === 1) return "success";
  if (Number(difficulty) === 2) return "warning";
  if (Number(difficulty) === 3) return "danger";
  return "info";
};

const formatPercent = (value) => `${(Number(value || 0) * 100).toFixed(2)}%`;

const truncateText = (text, maxLength = 30) => {
  if (!text || typeof text !== "string") return text || "-";
  const plain = text.replace(/<[^>]+>/g, "").replace(/\s+/g, " ").trim();
  if (!plain) return "-";
  return plain.length > maxLength ? `${plain.slice(0, maxLength)}...` : plain;
};

const aggregatedList = computed(() => {
  const map = new Map();
  rawList.value.forEach((row) => {
    const questionId = extractQuestionId(row);
    const key = questionId || `name_${row.examQuestionName || ""}`;
    if (!map.has(key)) {
      const question = questionMap.value.get(questionId);
      map.set(key, {
        questionId,
        examQuestionName: row.examQuestionName || question?.examQuestionName || "",
        examQuestionTypes: row.examQuestionTypes || question?.examQuestionTypes || "",
        examQuestionTypesName: row.examQuestionTypesName || question?.examQuestionTypesName || "",
        difficultyLevel: row.difficultyLevel || question?.difficultyLevel || "",
        knowledgePoint: row.knowledgePoint || question?.knowledgePoint || "",
        kemuTypes: row.kemuTypes || question?.kemuTypes || "",
        kemuTypesName: row.kemuTypesName || question?.kemuTypesName || "",
        examInfoIdsSet: new Set(), // 改用 exam_info_id 集合
        examNamesSet: new Set(),
        wrongUserSet: new Set(),
        recordIds: [],
        rawRecords: [],
        lastWrongTime: row.insertTime || "",
      });
    }
    const item = map.get(key);
    const userId = extractUserId(row);
    if (userId) item.wrongUserSet.add(userId);
    item.recordIds.push(Number(row.id));
    item.rawRecords.push(row);
    
    // 通过 exam_record_id 获取 exam_info_id
    const recordId = extractExamRecordId(row);
    const examInfoId = recordId ? examRecordToInfoMap.value.get(recordId) : null;
    if (examInfoId) {
      item.examInfoIdsSet.add(examInfoId);
      // 从 examInfoList 获取考试名称
      const examName = examNameMapByExamInfoId.value.get(examInfoId);
      if (examName) item.examNamesSet.add(examName);
    }
    
    if (row.insertTime && (!item.lastWrongTime || new Date(row.insertTime).getTime() > new Date(item.lastWrongTime).getTime())) {
      item.lastWrongTime = row.insertTime;
    }
  });

  const totalUsers = new Set(rawList.value.map((row) => extractUserId(row)).filter(Boolean)).size || 1;
  return Array.from(map.values()).map((item) => ({
    ...item,
    examInfoIds: Array.from(item.examInfoIdsSet.values()), // 改用 exam_info_id 数组
    examNames: Array.from(item.examNamesSet.values()),
    wrongUserCount: item.wrongUserSet.size,
    wrongRate: item.wrongUserSet.size / totalUsers,
  }));
});

const filteredList = computed(() => {
  const start = normalizeDateTime(filters.value.startDate, false);
  const end = normalizeDateTime(filters.value.endDate, true);
  const keyword = (filters.value.keyword || "").trim().toLowerCase();
  const examNameKeyword = (filters.value.examName || "").trim().toLowerCase();

  return aggregatedList.value.filter((row) => {
    if (examNameKeyword) {
      const names = (row.examNames || []).map(name => String(name).toLowerCase());
      if (!names.some(name => name.includes(examNameKeyword))) return false;
    }
    if (filters.value.questionType && Number(filters.value.questionType) !== Number(row.examQuestionTypes)) return false;
    if (filters.value.difficulty && Number(filters.value.difficulty) !== Number(row.difficultyLevel)) return false;
    if (filters.value.knowledgePoint && filters.value.knowledgePoint !== row.knowledgePoint) return false;
    if (keyword && !String(row.examQuestionName || "").toLowerCase().includes(keyword)) return false;

    const rowTime = row.lastWrongTime ? new Date(row.lastWrongTime).getTime() : null;
    if (start && (!rowTime || rowTime < start)) return false;
    if (end && (!rowTime || rowTime > end)) return false;
    return true;
  });
});

const summary = computed(() => {
  const userSet = new Set();
  const examInfoSet = new Set(); // 使用 exam_info_id 统计考试数量
  const knowledgeSet = new Set();
  let totalQuestionCount = 0;

  filteredList.value.forEach((item) => {
    totalQuestionCount += 1; // Count unique questions
    item.wrongUserSet.forEach((userId) => userSet.add(userId));
    // 使用 exam_info_id 来统计考试数量
    (item.examInfoIds || []).forEach((examInfoId) => {
      if (examInfoId) examInfoSet.add(examInfoId);
    });
    if (item.knowledgePoint) knowledgeSet.add(item.knowledgePoint);
  });

  return {
    totalQuestionCount,
    involvedUsers: userSet.size,
    involvedExams: examInfoSet.size, // 使用 exam_info_id 统计
    knowledgePointCount: knowledgeSet.size,
  };
});

const statCards = computed(() => [
  { key: "total", label: "错题总数", value: summary.value.totalQuestionCount, icon: InfoFilled, iconClass: "icon-blue" },
  { key: "users", label: "涉及考生", value: summary.value.involvedUsers, icon: UserFilled, iconClass: "icon-green" },
  { key: "exams", label: "涉及考试", value: summary.value.involvedExams, icon: Tickets, iconClass: "icon-orange" },
  { key: "points", label: "知识点数量", value: summary.value.knowledgePointCount, icon: CollectionTag, iconClass: "icon-purple" },
]);

const total = computed(() => filteredList.value.length);

/** 筛选实时生效；任意筛选项变化时回到第 1 页，避免仍停在高页码导致列表空白 */
watch(
  filters,
  () => {
    page.value.page = 1;
  },
  { deep: true }
);

const pageList = computed(() => {
  const start = (page.value.page - 1) * page.value.limit;
  return filteredList.value.slice(start, start + page.value.limit);
});

/** 单次分页条数；通过翻页循环拉取，避免超过单页上限导致聚合不全 */
const PAGE_FETCH_LIMIT = 5000;
const PAGE_FETCH_MAX_PAGES = 5000;

const fetchAllPagedList = async (url, extraBody = {}) => {
  const all = [];
  let pageNum = 1;
  while (pageNum <= PAGE_FETCH_MAX_PAGES) {
    const { data } = await request.post(url, { ...extraBody, page: pageNum, limit: PAGE_FETCH_LIMIT });
    const list = data?.list || [];
    all.push(...list);
    if (list.length < PAGE_FETCH_LIMIT) break;
    pageNum += 1;
  }
  if (pageNum > PAGE_FETCH_MAX_PAGES) {
    console.warn(`[错题本] 分页拉取达到上限 ${PAGE_FETCH_MAX_PAGES} 页，数据可能仍不完整: ${url}`);
  }
  return all;
};

const loadWrongQuestionList = async () => {
  rawList.value = await fetchAllPagedList("examWrongQuestion/page");
};

const loadExamQuestionList = async () => {
  examQuestionList.value = await fetchAllPagedList("examQuestion/page");
};

const loadExamPaperList = async () => {
  examPaperList.value = await fetchAllPagedList("examPaper/page");
};

const loadExamInfoList = async () => {
  examInfoList.value = await fetchAllPagedList("examInfo/page");
};

const loadExamRecordList = async () => {
  examRecordList.value = await fetchAllPagedList("examRecord/page");
};

const loadKemuOptions = async () => {
  kemuOptions.value = (await getKemuOptions()) || [];
};

const buildFilterOptions = () => {
  const examMap = new Map();
  const knowledgeSet = new Set();
  rawList.value.forEach((row) => {
    const examPaperId = extractExamPaperId(row);
    const examPaperName = row.examPaperName || "-";
    if (examPaperId && !examMap.has(examPaperId)) {
      examMap.set(examPaperId, { label: examPaperName, value: examPaperId });
    }
    const knowledgePoint = row.knowledgePoint || questionMap.value.get(extractQuestionId(row))?.knowledgePoint;
    if (knowledgePoint) knowledgeSet.add(knowledgePoint);
  });

  examOptions.value = Array.from(examMap.values());
  knowledgePointOptions.value = Array.from(knowledgeSet.values());
};

const clampPageToTotal = async () => {
  await nextTick();
  const n = total.value;
  const maxPage = n === 0 ? 1 : Math.ceil(n / page.value.limit);
  if (page.value.page > maxPage) page.value.page = maxPage;
};

const refreshList = async () => {
  listLoading.value = true;
  try {
    await Promise.all([
      loadWrongQuestionList(), 
      loadExamQuestionList(), 
      loadExamPaperList(), 
      loadExamInfoList(), 
      loadExamRecordList(), // 新增：加载考试记录数据
      loadKemuOptions()
    ]);
    if (isTeacherRole.value && teacherKemuTypes.value) {
      rawList.value = rawList.value.filter((row) => {
        const kemu = Number(row?.kemuTypes ?? row?.kemu_types ?? questionMap.value.get(extractQuestionId(row))?.kemuTypes ?? 0);
        return Number(kemu) === Number(teacherKemuTypes.value);
      });
    }
    buildFilterOptions();
    await clampPageToTotal();
  } catch (error) {
    console.error("加载错题数据失败:", error);
    ElMessage.error("加载错题数据失败");
  } finally {
    listLoading.value = false;
  }
};

const handleSearch = () => {
  page.value.page = 1;
};

const handleReset = () => {
  filters.value = {
    examName: "",
    questionType: "",
    difficulty: "",
    knowledgePoint: "",
    startDate: "",
    endDate: "",
    keyword: "",
  };
  page.value.page = 1;
};

const handleSizeChange = () => {
  page.value.page = 1;
};

const handleSelectionChange = (selection) => {
  multipleSelection.value = selection || [];
};

const handleDeleteRecords = async (recordIds) => {
  const ids = (recordIds || []).map((id) => Number(id)).filter(Boolean);
  if (!ids.length) return;
  await request.post("examWrongQuestion/del/batch", ids);
};

const handleBatchDelete = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要移除的错题");
    return;
  }
  try {
    await ElMessageBox.confirm("确定移除选中的错题记录吗？", "提示", { type: "warning" });
    const allIds = multipleSelection.value.flatMap((item) => item.recordIds || []);
    await handleDeleteRecords(allIds);
    ElMessage.success("移除成功");
    multipleSelection.value = [];
    await refreshList();
  } catch (error) {
    if (error !== "cancel") {
      console.error("批量移除失败:", error);
      ElMessage.error("批量移除失败");
    }
  }
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm("确定移除该题在错题本中的所有记录吗？", "提示", { type: "warning" });
    await handleDeleteRecords(row.recordIds);
    ElMessage.success("移除成功");
    await refreshList();
  } catch (error) {
    if (error !== "cancel") {
      console.error("移除失败:", error);
      ElMessage.error("移除失败");
    }
  }
};

const handleView = (row) => {
  const qid = Number(row.questionId);
  if (!qid) {
    ElMessage.warning("该题缺少题目ID，无法查看详情");
    return;
  }
  currentDetailQuestionId.value = qid;
  showIndexFlag.value = false;
  detailFlag.value = true;
};

const handleCloseDetail = async (action) => {
  detailFlag.value = false;
  showIndexFlag.value = true;
  currentDetailQuestionId.value = null;
  if (action === "refresh") await refreshList();
};

const currentDetailQuestion = computed(() => {
  const qid = Number(currentDetailQuestionId.value);
  if (!qid) return null;
  return questionMap.value.get(qid) || null;
});

const currentDetailRecords = computed(() => {
  const qid = Number(currentDetailQuestionId.value);
  if (!qid) return [];
  return rawList.value.filter((r) => extractQuestionId(r) === qid);
});

const globalUserCount = computed(() => new Set(rawList.value.map((row) => extractUserId(row)).filter(Boolean)).size || 1);

const handleExport = () => {
  if (!filteredList.value.length) {
    ElMessage.warning("暂无可导出的错题数据");
    return;
  }
  const headers = ["题目内容", "题目类型", "科目", "所属考试", "知识点", "难度", "错误人数", "错误率"];
  const rows = filteredList.value.map((row) => [
    row.examQuestionName || "-",
    getTypeLabel(row),
    getSubjectLabel(row),
    getExamName(row),
    row.knowledgePoint || "-",
    getDifficultyLabel(row.difficultyLevel),
    row.wrongUserCount,
    formatPercent(row.wrongRate),
  ]);
  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, sheet, "错题本");
  const buffer = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
  const blob = new Blob([buffer], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `错题本_${new Date().toISOString().slice(0, 10)}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success("导出成功");
};

const route = useRoute();

onMounted(() => {
  refreshList();
});

// 监听路由变化，当从其他页面返回时重新加载数据
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 当路由切换到错题本页面时，重新加载数据
    if (newPath === '/examWrongQuestion' && oldPath && oldPath !== newPath) {
      refreshList();
    }
  }
);
</script>

<style lang="scss" scoped>
.wrong-question-index {
  position: relative;
  min-height: 200px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
}

.tip-card {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #eef6ff;
  color: #3b6ca8;
  font-size: 14px;
}

.filter-card,
.table-card {
  margin-bottom: 16px;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.filter-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 14px 16px;
}

.filter-item .label {
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.filter-actions {
  margin-top: 0;
  display: flex;
  gap: 10px;
}

.filter-actions :deep(.el-button) {
  min-width: 76px;
  height: 36px;
  border-radius: 8px;
  font-weight: 600;
}

.filter-actions :deep(.el-button--primary) {
  background: #2386ea;
  border-color: #2386ea;
  color: #fff;
}

.filter-actions :deep(.el-button--default) {
  background: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.stats-grid {
  margin-bottom: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 12px;
}

.stat-card {
  padding: 16px 18px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #edf0f5;
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrap {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.icon-blue {
  color: #3f8cff;
  background: #eaf2ff;
}

.icon-green {
  color: #19be9b;
  background: #e7faf4;
}

.icon-orange {
  color: #f2a93b;
  background: #fff5e8;
}

.icon-purple {
  color: #8d67f4;
  background: #f3edff;
}

.stat-main {
  flex: 1;
  min-width: 0;
}

.stat-label {
  color: #5f6b7a;
  font-size: 15px;
}

.stat-value {
  margin-top: 2px;
  color: #1f2d3d;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.1;
}

.table-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.table-title {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.export-btn {
  height: 34px;
  border-radius: 8px;
}

.batch-delete-btn {
  height: 34px;
  border-radius: 8px;
  background: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

.error-rate {
  color: #f56c6c;
  font-weight: 600;
}

:deep(.el-select),
:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-date-editor .el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
}

:deep(.el-table .cell) {
  line-height: 1.6;
}

.action-btn {
  min-width: 64px;
  height: 30px;
  border-radius: 8px;
  font-weight: 600;
}

.detail-btn {
  color: #fff;
  background: #5ea7f8;
  border: 1px solid #5ea7f8;
}

.detail-btn:hover {
  color: #fff;
  background: #7cb9fa;
  border-color: #7cb9fa;
}

.delete-btn {
  color: #fff;
  background: #f56c6c;
  border: 1px solid #f56c6c;
}

.delete-btn:hover {
  color: #fff;
  background: #f78989;
  border-color: #f78989;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table .el-table__header-wrapper .el-table__header th) {
  background: var(--table-header-bg, #f5f7fa);
  color: var(--table-header-text-color, #303133);
  font-weight: 600;
  font-size: 14px;
  border-bottom: 2px solid var(--table-header-border-color, #e4e7ed);
}

:deep(.el-table .el-table__header-wrapper .el-table__header th .cell) {
  padding: 12px 0;
}

:deep(.el-table .el-table__body-wrapper .el-table__body tr) {
  transition: all 0.3s;
}

:deep(.el-table .el-table__body-wrapper .el-table__body tr:hover) {
  background: var(--table-row-hover-bg, #f0f9ff);
}

:deep(.el-table .el-table__body-wrapper .el-table__body tr:hover td) {
  background: transparent;
}

:deep(.el-table .el-table__body-wrapper .el-table__body tr td) {
  color: #303133;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table .el-table__body-wrapper .el-table__body tr td .cell) {
  padding: 12px 0;
}

:deep(.el-table .el-table__body-wrapper .el-table__body .el-table__row--striped td) {
  background: var(--table-row-stripe-bg, #fafafa);
}

:deep(.el-table.el-table--border) {
  border: 1px solid #ebeef5;
}

:deep(.el-table.el-table--border td),
:deep(.el-table.el-table--border th) {
  border-right: 1px solid #ebeef5;
}

:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20px 0 0;
  gap: 8px;

  .el-pagination__total {
    color: #606266;
    font-weight: 500;
  }

  .el-pagination__sizes {
    .el-select {
      width: auto;
      min-width: 100px;

      .el-select__wrapper {
        border-radius: 6px;
        padding: 0 8px;
      }

      .el-select__selected-item {
        display: flex;
        align-items: center;
      }
    }
  }

  .btn-prev,
  .btn-next {
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    color: #606266;
    padding: 0 12px;
    min-width: 32px;
    height: 32px;
    transition: all 0.3s;

    &:hover {
      color: #1890ff;
      border-color: #1890ff;
      background: #f0f9ff;
    }

    &:disabled {
      color: #c0c4cc;
      background: #f5f7fa;
      border-color: #e4e7ed;
      cursor: not-allowed;
    }
  }

  .el-pager {
    li {
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      color: #606266;
      min-width: 32px;
      height: 32px;
      line-height: 30px;
      margin: 0 4px;
      transition: all 0.3s;

      &:hover {
        color: #1890ff;
        border-color: #1890ff;
        background: #f0f9ff;
      }

      &.is-active {
        background: #1890ff;
        border-color: transparent;
        color: #fff;
        font-weight: 600;
      }

      &.btn-quicknext,
      &.btn-quickprev {
        border: none;

        &:hover {
          color: #1890ff;
        }
      }
    }
  }

  .el-pagination__jump {
    color: #606266;
    margin-left: 8px;

    .el-input {
      width: 50px;

      .el-input__wrapper {
        border-radius: 6px;
      }
    }
  }
}

@media (max-width: 1400px) {
  .filter-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1100px) {
  .filter-grid,
  .stats-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 768px) {
  .filter-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>

