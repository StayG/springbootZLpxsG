<template>
  <div class="score-page">
    <h2 class="page-title">我的成绩</h2>

    <div class="stat-grid">
      <div class="stat-card">
        <span class="icon blue"><el-icon><Document /></el-icon></span>
        <div class="meta">
          <div class="label">考试总数</div>
          <div class="num">{{ stats.total }} <em>次</em></div>
        </div>
      </div>
      <div class="stat-card">
        <span class="icon green"><el-icon><CircleCheckFilled /></el-icon></span>
        <div class="meta">
          <div class="label">已完成</div>
          <div class="num done">{{ stats.finished }} <em>次</em></div>
        </div>
      </div>
      <div class="stat-card">
        <span class="icon orange"><el-icon><TrendCharts /></el-icon></span>
        <div class="meta">
          <div class="label">平均分</div>
          <div class="num avg">{{ stats.avgScore }} <em>分</em></div>
        </div>
      </div>
      <div class="stat-card">
        <span class="icon purple"><el-icon><Trophy /></el-icon></span>
        <div class="meta">
          <div class="label">最高分</div>
          <div class="num max">{{ stats.maxScore }} <em>分</em></div>
        </div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-top">
        <div class="tabs">
          <span class="tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">全部成绩</span>
          <span class="tab" :class="{ active: activeTab === 'finished' }" @click="activeTab = 'finished'">已完成</span>
          <span class="tab" :class="{ active: activeTab === 'unfinished' }" @click="activeTab = 'unfinished'">未完成</span>
        </div>

        <div class="filters">
          <el-select
            v-model="filters.kemuTypes"
            placeholder="全部课程"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
      </div>

      <el-table :data="pageRows" v-loading="loading" class="score-table" stripe>
        <template #empty>
          <el-empty description="暂无考试记录，快去参加考试吧" />
        </template>
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column label="考试名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveExamName(row) }}</template>
        </el-table-column>
        <el-table-column label="考试科目" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveKemu(row) }}</template>
        </el-table-column>
        <el-table-column label="考试时间" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ formatExamScheduleRange(row) }}</template>
        </el-table-column>
        <el-table-column label="成绩 / 总分" width="120" align="center">
          <template #default="{ row }">
            <span class="score-slash" :class="{ 'is-muted': getRowKind(row) !== 'published' }">{{ formatScoreOverTotal(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :class="getResultStateTagClass(row)">
              {{ getResultStateText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排名" width="100" align="center">
          <template #default="{ row }">
            <span :class="rankCellClass(row)">{{ formatRankCell(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <template v-if="getRowKind(row) === 'in_exam'">
                <el-button type="primary" size="small" @click="enterExam(row)">继续考试</el-button>
              </template>
              <template v-else-if="getRowKind(row) === 'pending'">
                <el-button type="primary" link size="small" :disabled="!row.examRecordUuidNumber" @click="viewAnswerSheet(row)">
                  查看答卷
                </el-button>
              </template>
              <template v-else>
                <el-button type="primary" link size="small" :disabled="!row.examRecordUuidNumber" @click="viewScoreDetail(row)">
                  查看成绩
                </el-button>
                <el-button type="primary" link size="small" :disabled="!canViewWrongBook(row)" @click="viewWrongQuestions(row)">
                  查看错题
                </el-button>
                <el-button
                  type="info"
                  link
                  size="small"
                  :disabled="!row.examRecordUuidNumber || !isScorePublished(row)"
                  @click="handleDownloadFullPdf(row)"
                >
                  下载试卷
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredRows.length > 0" class="pager">
        <div class="left-total">共 {{ filteredRows.length }} 条</div>
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.limit"
          :total="filteredRows.length"
          :page-sizes="[10, 20, 30]"
          layout="prev, pager, next, sizes"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import { CircleCheckFilled, Document, TrendCharts, Trophy } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { getKemuOptions } from "@/utils/dictionary";
import { downloadFullAnswerPdf as requestFullAnswerPdf } from "@/utils/downloadExamPdf.js";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const rows = ref([]);
const examInfoMap = ref(new Map());
const kemuOptions = ref([]);

const activeTab = ref("all");

const filters = reactive({
  kemuTypes: null,
});

const pager = reactive({
  page: 1,
  limit: 10,
});

watch(activeTab, () => {
  pager.page = 1;
});

const buildExamGroupKey = (item) => {
  if (item?.examInfoId !== null && item?.examInfoId !== undefined) return `exam_${item.examInfoId}`;
  if (item?.examPaperId !== null && item?.examPaperId !== undefined) return `paper_${item.examPaperId}`;
  if (item?.examName) return `name_${item.examName}`;
  return `record_${item?.id ?? Math.random()}`;
};

const pickPreferredRecord = (current, candidate) => {
  if (!current) return candidate;
  if (Number(candidate?.isLatest) === 1 && Number(current?.isLatest) !== 1) return candidate;
  if (Number(current?.isLatest) === 1 && Number(candidate?.isLatest) !== 1) return current;
  const currentTime = new Date(current?.insertTime || 0).getTime();
  const candidateTime = new Date(candidate?.insertTime || 0).getTime();
  return candidateTime >= currentTime ? candidate : current;
};

const groupedLatestRows = computed(() => {
  const map = new Map();
  rows.value.forEach((item) => {
    const key = buildExamGroupKey(item);
    map.set(key, pickPreferredRecord(map.get(key), item));
  });
  return Array.from(map.values());
});

const isFinishedRecord = (row) => [1, 2, 3].includes(Number(row?.status));
const isUnfinishedRecord = (row) => Number(row?.status) === 0;

/** 1 考试中 2 待出分 3 已发布 */
const getRowKind = (row) => {
  if (Number(row?.status) === 0) return "in_exam";
  if (Number(row?.pendingReviewCount) > 0) return "pending";
  return "published";
};

/** 成绩已发布：可展示分数、错题、成绩详情（非答卷模式） */
const isScorePublished = (row) => getRowKind(row) === "published";

const getStudentScore = (row) => {
  const v = row?.totalScore;
  if (v === null || v === undefined || v === "") return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
};

const getPaperFullScore = (row) => {
  const v = row?.examPaperMyscore ?? row?.exam_paper_myscore;
  if (v === null || v === undefined || v === "") return null;
  const n = Number(v);
  return Number.isFinite(n) && n > 0 ? n : null;
};

const formatScoreOverTotal = (row) => {
  const k = getRowKind(row);
  if (k === "in_exam") return "--";
  if (k === "pending") return "待阅卷";
  const user = getStudentScore(row);
  const full = getPaperFullScore(row);
  if (user === null) return "—";
  if (full !== null) return `${user} / ${full}`;
  return `${user}分`;
};

const getResultStateText = (row) => {
  const k = getRowKind(row);
  if (k === "in_exam") return "考试中";
  if (k === "pending") return "待出分";
  return "已发布";
};

const getResultStateTagClass = (row) => {
  const k = getRowKind(row);
  if (k === "in_exam") return "state-ongoing";
  if (k === "pending") return "state-pending";
  return "state-published";
};

const getExamRankPair = (row) => {
  const rank = row?.examRank ?? row?.exam_rank;
  const total = row?.examRankTotal ?? row?.exam_rank_total;
  return { rank, total };
};

const formatRankCell = (row) => {
  const k = getRowKind(row);
  if (k === "in_exam" || k === "pending") return "--";
  const { rank, total } = getExamRankPair(row);
  const r = rank != null && rank !== "" ? Number(rank) : NaN;
  const t = total != null && total !== "" ? Number(total) : NaN;
  if (Number.isFinite(r) && Number.isFinite(t) && t > 0) return `${r} / ${t}`;
  if (Number.isFinite(r)) return `第 ${r} 名`;
  return "--";
};

const rankCellClass = (row) => {
  const k = getRowKind(row);
  if (k === "in_exam" || k === "pending") return "rank-cell rank-cell--muted";
  const { rank } = getExamRankPair(row);
  const r = rank != null && rank !== "" ? Number(rank) : NaN;
  if (Number.isFinite(r)) return "rank-cell rank-cell--named";
  return "rank-cell rank-cell--muted";
};

const canViewWrongBook = (row) => {
  if (Number(row?.status) === 0) return false;
  if (!row?.examPaperId) return false;
  return isScorePublished(row);
};

const getSortTimeMs = (row) => {
  const s = row?.examScheduleStart ?? row?.exam_schedule_start;
  const e = row?.examScheduleEnd ?? row?.exam_schedule_end;
  const fromRow = s || e;
  if (fromRow) {
    const t = new Date(fromRow).getTime();
    if (Number.isFinite(t)) return t;
  }
  const info = row?.examInfoId != null ? examInfoMap.value.get(Number(row.examInfoId)) : null;
  if (info?.startTime) {
    const t = new Date(info.startTime).getTime();
    if (Number.isFinite(t)) return t;
  }
  return new Date(row?.insertTime || 0).getTime();
};

const stats = computed(() => {
  const total = groupedLatestRows.value.length;
  const finishedRows = groupedLatestRows.value.filter(isFinishedRecord);
  const finished = finishedRows.length;
  const publishedRows = finishedRows.filter(isScorePublished);
  const scoreList = publishedRows.map((r) => getStudentScore(r)).filter((n) => n !== null);
  const maxScore = scoreList.length ? Math.max(...scoreList) : 0;
  const avgScore = scoreList.length ? Math.round((scoreList.reduce((a, b) => a + b, 0) / scoreList.length) * 10) / 10 : 0;
  return { total, finished, maxScore, avgScore };
});

const filteredRows = computed(() => {
  let list = groupedLatestRows.value.slice();

  if (activeTab.value === "finished") list = list.filter(isFinishedRecord);
  if (activeTab.value === "unfinished") list = list.filter(isUnfinishedRecord);

  if (filters.kemuTypes !== null && filters.kemuTypes !== undefined && filters.kemuTypes !== "") {
    list = list.filter((row) => {
      const code = getRowKemuTypeCode(row);
      return code !== null && Number(code) === Number(filters.kemuTypes);
    });
  }

  list.sort((a, b) => getSortTimeMs(b) - getSortTimeMs(a));
  return list;
});

const pageRows = computed(() => {
  const start = (pager.page - 1) * pager.limit;
  return filteredRows.value.slice(start, start + pager.limit);
});

const handleSearch = () => {
  pager.page = 1;
};

const loadRows = async () => {
  loading.value = true;
  try {
    const { data } = await request.post("/examRecord/page", { page: 1, limit: 2000, onlyLatest: 0 });
    rows.value = data?.list || [];
  } catch (e) {
    ElMessage.error("加载成绩失败");
  } finally {
    loading.value = false;
  }
};

const loadExamInfoMap = async () => {
  try {
    const { data } = await request.post("/examInfo/page", { page: 1, limit: 2000 });
    const list = data?.list || [];
    const map = new Map();
    list.forEach((item) => {
      if (item?.id !== null && item?.id !== undefined) {
        map.set(Number(item.id), item);
      }
    });
    examInfoMap.value = map;
  } catch (e) {
    examInfoMap.value = new Map();
  }
};

const resolveExamName = (row) => {
  if (row?.examName) return row.examName;
  if (row?.examInfoId !== null && row?.examInfoId !== undefined) {
    const hit = examInfoMap.value.get(Number(row.examInfoId));
    if (hit?.examName) return hit.examName;
  }
  return row?.examPaperName || "—";
};

/** 与表格「考试科目」一致：优先考试安排的科目代码，便于下拉按科目筛选 */
const getRowKemuTypeCode = (row) => {
  if (row?.examInfoId != null && row?.examInfoId !== undefined) {
    const info = examInfoMap.value.get(Number(row.examInfoId));
    const v = info?.kemuTypes;
    if (v !== null && v !== undefined && v !== "") {
      const n = Number(v);
      if (Number.isFinite(n)) return n;
    }
  }
  const fromRow = row?.kemuTypes ?? row?.kemu_types;
  if (fromRow !== null && fromRow !== undefined && fromRow !== "") {
    const n = Number(fromRow);
    if (Number.isFinite(n)) return n;
  }
  return null;
};

const resolveKemu = (row) => {
  const raw = row?.kemuValue ?? row?.kemu_value;
  if (raw && String(raw).trim()) return String(raw).trim();
  if (row?.examInfoId !== null && row?.examInfoId !== undefined) {
    const hit = examInfoMap.value.get(Number(row.examInfoId));
    if (hit?.kemuValue && String(hit.kemuValue).trim()) return String(hit.kemuValue).trim();
    const label = kemuOptions.value.find((i) => Number(i.value) === Number(hit?.kemuTypes))?.label;
    if (label) return label;
  }
  const fromPaper = kemuOptions.value.find((i) => Number(i.value) === Number(row?.kemuTypes))?.label;
  if (fromPaper) return fromPaper;
  return "—";
};

const padTimePart = (n) => String(n).padStart(2, "0");

const formatDateTimeShort = (value) => {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    const s = String(value).replace("T", " ");
    return s.length >= 16 ? s.slice(0, 16) : s;
  }
  return `${d.getFullYear()}-${padTimePart(d.getMonth() + 1)}-${padTimePart(d.getDate())} ${padTimePart(d.getHours())}:${padTimePart(d.getMinutes())}`;
};

const formatExamScheduleRange = (row) => {
  let start = row?.examScheduleStart ?? row?.exam_schedule_start;
  let end = row?.examScheduleEnd ?? row?.exam_schedule_end;
  if ((!start || !end) && row?.examInfoId != null) {
    const info = examInfoMap.value.get(Number(row.examInfoId));
    if (info) {
      if (!start) start = info.startTime;
      if (!end) end = info.endTime;
    }
  }
  const a = formatDateTimeShort(start);
  const b = formatDateTimeShort(end);
  if (a && b) return `${a}—${b}`;
  if (a) return `${a}—`;
  if (b) return `—${b}`;
  return "—";
};

const enterExam = async (item) => {
  if (!item?.examInfoId || !item?.examPaperId) {
    ElMessage.warning("缺少考试信息，无法继续考试");
    return;
  }
  try {
    const checkRes = await request.get(`/examInfo/info/${item.examInfoId}`);
    if (checkRes.data?.examPassword) {
      const { value } = await ElMessageBox.prompt("请输入考试密码", "密码校验", {
        inputPattern: /\S+/,
        inputErrorMessage: "密码不能为空",
      });
      sessionStorage.setItem(`exam_pwd_${item.examInfoId}`, value);
      const { data } = await request.get(`/examInfo/enter/${item.examInfoId}`, { params: { password: value } });
      jumpToExam(item, data);
      return;
    }
    const { data } = await request.get(`/examInfo/enter/${item.examInfoId}`);
    jumpToExam(item, data);
  } catch (error) {
    if (error !== "cancel") ElMessage.error(error?.message || "进入考试失败");
  }
};

const jumpToExam = (item, data) => {
  const examRecordUuid = typeof data === "object" ? data.examRecordUuid : data;
  router.push({
    name: "examPaperExam",
    query: { examInfoId: item.examInfoId, examPaperId: item.examPaperId, examRecordUuid },
  });
};

/** 已发布：完整成绩详情（分数、解析按考试规则） */
const viewScoreDetail = (item) => {
  if (!item?.examRecordUuidNumber) return;
  router.push({ path: "/index/examRecord", query: { uuid: item.examRecordUuidNumber } });
};

/** 待出分：仅本人作答，无分数、无标准答案与解析 */
const viewAnswerSheet = (item) => {
  if (!item?.examRecordUuidNumber) return;
  router.push({ path: "/index/examRecord", query: { uuid: item.examRecordUuidNumber, view: "answerSheet" } });
};

const viewWrongQuestions = (item) => {
  if (!canViewWrongBook(item)) return;
  router.push({ path: "/index/wrongQuestion", query: { examPaperId: String(item.examPaperId) } });
};

const sanitizeFilePart = (s) => String(s || "").replace(/[\\/:*?"<>|]/g, "_").trim() || "—";

const buildPdfFallbackFilename = (row) => {
  const exam = resolveExamName(row);
  const nick =
    userStore.userInfo?.nickname ||
    userStore.userInfo?.userName ||
    userStore.userInfo?.username ||
    "学生";
  return `【成绩导出】-${sanitizeFilePart(exam)}-${sanitizeFilePart(nick)}.pdf`;
};

const handleDownloadFullPdf = async (row) => {
  if (!isScorePublished(row) || !row?.examRecordUuidNumber) return;
  const loading = ElLoading.service({ lock: true, text: "正在生成 PDF，请稍候…", background: "rgba(255,255,255,0.65)" });
  try {
    await requestFullAnswerPdf(row.examRecordUuidNumber, buildPdfFallbackFilename(row));
    ElMessage.success("下载已开始");
  } catch (e) {
    ElMessage.error(e?.message || "导出失败");
  } finally {
    loading.close();
  }
};

onMounted(() => {
  getKemuOptions()
    .then((list) => {
      kemuOptions.value = list || [];
    })
    .catch(() => {
      kemuOptions.value = [];
    });
  loadExamInfoMap();
  loadRows();
});
</script>

<style scoped lang="scss">
.score-page {
  width: 100%;
}
.page-title {
  margin: 0 0 14px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #0f172a;
}
.panel {
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  padding: 14px 16px 10px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 12px;
  padding: 26px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-card .icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  flex-shrink: 0;
}
.icon.blue {
  background: #4f46e5;
}
.icon.green {
  background: #28c191;
}
.icon.orange {
  background: #ffb020;
}
.icon.purple {
  background: #7d5fff;
}
.stat-card .meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-card .label {
  font-size: 22px;
  color: #666;
  font-weight: 500;
  line-height: 1;
  margin-bottom: 8px;
}
.stat-card .num {
  font-size: 44px;
  font-weight: 700;
  color: #202733;
  line-height: 1;
}
.stat-card .num em {
  font-style: normal;
  font-size: 20px;
  color: #666;
  margin-left: 4px;
  font-weight: 500;
}
.num.done,
.num.avg,
.num.max {
  color: #202733;
}
.panel-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f7;
}
.tabs {
  display: flex;
  align-items: center;
  gap: 18px;
}
.tab {
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  color: #6b7280;
  padding: 10px 2px;
  position: relative;
}
.tab.active {
  color: #4f46e5;
}
.tab.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: #4f46e5;
  border-radius: 2px;
}
.filters :deep(.el-select__wrapper) {
  height: 36px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}
.score-table {
  margin-top: 12px;
}
/* 列表区域出现滚动条时隐藏轨道，仍可用滚轮/触控滑动 */
.score-table :deep(.el-table__body-wrapper),
.score-table :deep(.el-table__header-wrapper),
.score-table :deep(.el-table__fixed-body-wrapper) {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.score-table :deep(.el-table__body-wrapper)::-webkit-scrollbar,
.score-table :deep(.el-table__header-wrapper)::-webkit-scrollbar,
.score-table :deep(.el-table__fixed-body-wrapper)::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}
.score-table :deep(th) {
  background: #f7f9fd;
  color: #5f6776;
  font-weight: 700;
  height: 46px;
}
.score-table :deep(td) {
  font-size: 14px;
  color: #2f3642;
}
.score-slash {
  font-weight: 700;
  color: #0f172a;
}
.score-slash.is-muted {
  color: #94a3b8;
  font-weight: 600;
}
.rank-cell {
  font-variant-numeric: tabular-nums;
}
.rank-cell--muted {
  color: #94a3b8;
  font-weight: 600;
}
.rank-cell--named {
  color: #0f172a;
  font-weight: 600;
}
.action-btns {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 2px 6px;
}
.state-ongoing {
  background: #fff7ed;
  border-color: #ffe3b0;
  color: #c98500;
}
.state-pending {
  background: #eef2f7;
  border-color: #dce4ef;
  color: #64748b;
}
.state-published {
  background: #e8f8ef;
  border-color: #bdebd1;
  color: #16a34a;
}
.pager {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.left-total {
  color: #8b92a1;
  font-size: 14px;
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
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
