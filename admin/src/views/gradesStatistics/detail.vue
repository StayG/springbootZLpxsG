<template>
  <div class="module-container gs-detail">
    <div class="gs-detail-header">
      <div class="gs-detail-title-row">
        <el-button text class="gs-back-icon" @click="goBack" aria-label="返回">
          <el-icon :size="22"><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title gs-detail-title">{{ pageHeading }}</h1>
      </div>
      <div class="gs-detail-actions">
        <el-button v-if="utils.isAuth('gradesStatistics', '导出')" class="gs-export-btn" @click="handleExportScores">
          <el-icon class="m-r-4"><Download /></el-icon>
          导出成绩
        </el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <div class="gs-detail-dashboard">
      <div class="summary-cards gs-detail-cards">
        <div v-for="(c, idx) in detailSummaryCards" :key="idx" class="summary-card">
          <div class="summary-card-icon" :class="c.iconClass">
            <el-icon :size="36"><component :is="c.icon" /></el-icon>
          </div>
          <div class="summary-card-body">
            <div class="summary-card-label">{{ c.label }}</div>
            <div class="summary-card-value">{{ c.value }}</div>
            <div class="summary-card-sub">{{ c.sub }}</div>
          </div>
        </div>
      </div>

      <div class="chart-row gs-detail-charts">
        <div class="chart-panel">
          <div class="chart-panel-title">成绩分布</div>
          <div ref="barRef" class="chart-box" />
        </div>
        <div class="chart-panel">
          <div class="chart-panel-title">及格情况分布</div>
          <div ref="donutRef" class="chart-box" />
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-item filter-item--search">
        <div class="label">筛选</div>
        <el-input v-model="keyword" clearable placeholder="请输入考生姓名/账号" />
      </div>
    </div>

    <div class="table-scroll-x">
      <el-table :data="pageList" border stripe header-align="center">
        <el-table-column type="index" label="序号" width="70" align="center" :index="tableIndexMethod" />
        <el-table-column prop="nickname" label="考生姓名" min-width="120" align="center" show-overflow-tooltip />
        <el-table-column label="账号" min-width="130" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserAccount(row) }}</template>
        </el-table-column>
        <el-table-column label="考生进入时间" min-width="175" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDateTime(row.insertTime) }}</template>
        </el-table-column>
        <el-table-column label="得分（分）" width="110" align="center">
          <template #default="{ row }">
            <span :class="['gs-score', scoreToneClass(row.totalScore)]">{{ row.totalScore ?? "—" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用时" width="130" align="center">
          <template #default="{ row }">{{ formatDuration(row) }}</template>
        </el-table-column>
        <el-table-column label="交卷时间" min-width="175" align="center">
          <template #default="{ row }">{{ formatDateTime(row.endTime || row.insertTime) }}</template>
        </el-table-column>
        <el-table-column label="考试状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag class="status-tag" :class="`status-${statusKey(row)}`" effect="light">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="showDetail(row)" class="action-btn detail-btn" @click="goToDetails(row)">查看详情</el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page.page"
        v-model:page-size="page.limit"
        :total="filteredTotal"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
      />
    </div>

    <el-backtop
      class="gs-page-backtop"
      target=".content-main .el-scrollbar__wrap"
      :right="28"
      :bottom="96"
      :visibility-height="80"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import * as echarts from "echarts";
import { ArrowLeft, Download, UserFilled, TrendCharts, PieChart, Trophy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import * as XLSX from "xlsx";
import request from "@/utils/request";
import utils from "@/utils/utils";
import { scrollAdminLayoutToTop } from "@/utils/adminScrollLayout.js";

const route = useRoute();
const router = useRouter();

const examInfoId = computed(() => {
  const v = route.query.examInfoId;
  if (v === undefined || v === null || v === "") return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
});

const examTitle = ref(String(route.query.examName || "").trim());

const summary = ref(null);
const scoreDistribution = ref([]);

const rawList = ref([]);
const usersList = ref([]);

const keyword = ref("");

const page = ref({ page: 1, limit: 10 });

const barRef = ref(null);
const donutRef = ref(null);
let barChart;
let donutChart;
let resizeHandler = null;

const STAT_DATE_START = "2000-01-01";
const STAT_DATE_END = "2099-12-31";

const usersMap = computed(() => {
  const map = new Map();
  usersList.value.forEach((item) => {
    if (item?.id != null) map.set(item.id, item);
  });
  return map;
});

const pageHeading = computed(() => {
  const name = examTitle.value || "考试";
  return `${name}（查看成绩）`;
});

/** 分布按分数段顺序（与示意图一致） */
function orderedBarSeries(dist) {
  const keys = ["0-59", "60-69", "70-79", "80-89", "90-100"];
  const labels = ["0-59", "60-69", "70-79", "80-89", "90-100"];
  const byKey = Object.fromEntries((dist || []).map((d) => [d.rangeKey, d.count]));
  return {
    labels,
    counts: keys.map((k) => Number(byKey[k] ?? 0)),
  };
}

function parseMetricCount(val) {
  if (val == null || val === "") return null;
  const s = String(val).replace(/,/g, "").trim();
  const n = Number(s);
  return Number.isFinite(n) ? n : null;
}

function parseMetricDecimal(val) {
  if (val == null || val === "") return null;
  const n = Number(String(val).replace(/%/g, "").trim());
  return Number.isFinite(n) ? n : null;
}

const detailSummaryCards = computed(() => {
  const s = summary.value;
  const dist = scoreDistribution.value || [];
  const ob = orderedBarSeries(dist);
  const totalGraded = ob.counts.reduce((a, b) => a + b, 0);
  const fail = ob.counts[0] ?? 0;
  const pass = Math.max(0, totalGraded - fail);
  const excellent = ob.counts[4] ?? 0;

  const partVal = s?.participants?.value;
  const refCount = parseMetricCount(partVal) ?? totalGraded;

  const avgStr = s?.avgScore?.value ?? "—";
  const maxStr = s?.maxScore?.value ?? "—";
  const passRateStr = s?.passRate?.value ?? "—";

  const minFromRecords = minScoreFromRecords();
  const minStr = minFromRecords != null ? minFromRecords.toFixed(2) : "—";

  const passPct =
    parseMetricDecimal(passRateStr) ??
    (totalGraded > 0 ? (pass * 100.0) / totalGraded : null);
  const passPctDisp = passPct != null ? `${passPct.toFixed(2)}%` : "—";

  const excPct = totalGraded > 0 ? (excellent * 100.0) / totalGraded : null;
  const excPctDisp = excPct != null ? `${excPct.toFixed(2)}%` : "—";

  return [
    {
      label: "参考人数",
      value: `${refCount} 人`,
      sub: `已参考 ${refCount} 人 | 未参考 0 人`,
      icon: UserFilled,
      iconClass: "icon-blue",
    },
    {
      label: "平均分",
      value: `${avgStr} 分`,
      sub: `最高分 ${maxStr} 分 | 最低分 ${minStr} 分`,
      icon: TrendCharts,
      iconClass: "icon-green",
    },
    {
      label: "及格率",
      value: passPctDisp,
      sub: `及格人数 ${pass} 人 | 不及格 ${fail} 人`,
      icon: PieChart,
      iconClass: "icon-orange",
    },
    {
      label: "优秀率",
      value: excPctDisp,
      sub: `优秀人数 ${excellent} 人（≥90分）`,
      icon: Trophy,
      iconClass: "icon-purple",
    },
  ];
});

function minScoreFromRecords() {
  const scores = rawList.value
    .map((r) => r.totalScore)
    .filter((x) => x !== null && x !== undefined && Number.isFinite(Number(x)));
  if (!scores.length) return null;
  return Math.min(...scores.map(Number));
}

const filteredList = computed(() => {
  const kw = String(keyword.value || "")
    .trim()
    .toLowerCase();
  return rawList.value.filter((row) => {
    if (!kw) return true;
    const name = String(row.nickname || "").toLowerCase();
    const acc = String(getUserAccount(row) || "").toLowerCase();
    return name.includes(kw) || acc.includes(kw);
  });
});

const filteredTotal = computed(() => filteredList.value.length);

const pageList = computed(() => {
  const start = (page.value.page - 1) * page.value.limit;
  return filteredList.value.slice(start, start + page.value.limit);
});

function tableIndexMethod(index) {
  return (page.value.page - 1) * page.value.limit + index + 1;
}

function toTime(value) {
  if (!value) return null;
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? null : time;
}

function getUserAccount(row) {
  if (row.username || row.account || row.userNumber) {
    return row.username || row.account || row.userNumber;
  }
  return usersMap.value.get(row.usersId)?.userName || (row.usersId ? `user${row.usersId}` : "—");
}

function formatDuration(row) {
  const start = toTime(row.insertTime);
  const end = toTime(row.endTime);
  if (!start || !end || end <= start) return "—";
  const sec = Math.floor((end - start) / 1000);
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}分${String(s).padStart(2, "0")}秒`;
}

function formatDateTime(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  const p = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

function statusText(row) {
  if (row.status === null || row.status === undefined) return "缺考";
  if (row.status === 0) return "考试中";
  if (row.status === 1) return "已提交";
  if (row.status === 2) return "强制交卷";
  if (row.status === 3) return "已完成";
  return "未知";
}

function statusKey(row) {
  const t = statusText(row);
  if (t === "缺考") return "absent";
  if (t === "考试中") return "in-progress";
  if (t === "已提交") return "submitted";
  if (t === "强制交卷") return "forced-submit";
  if (t === "已完成") return "completed";
  return "unknown";
}

/** 与及格环形图一致：≥60 绿色、<60 红色（同 60 分及格线） */
function scoreToneClass(score) {
  if (score === null || score === undefined) return "";
  const n = Number(score);
  if (!Number.isFinite(n)) return "";
  if (n >= 60) return "gs-score--pass";
  return "gs-score--fail";
}

function showDetail(row) {
  return !(row?.status === 0 || row?.status === "0");
}

function goToDetails(row) {
  router.push({
    path: "/examDetails",
    query: {
      examRecordId: row.id,
      examDetailsUuidNumber: row.examRecordUuidNumber,
      examName: row.examName || examTitle.value,
      examPaperName: row.examPaperName,
      nickname: row.nickname,
      userName: getUserAccount(row),
      status: row.status,
      passingScore: row.passingScore,
      endTime: row.endTime,
      totalScore: row.totalScore,
      insertTime: row.insertTime,
    },
  });
}

function goBack() {
  router.push("/gradesStatistics");
}

function disposeCharts() {
  if (resizeHandler) {
    window.removeEventListener("resize", resizeHandler);
    resizeHandler = null;
  }
  barChart?.dispose();
  donutChart?.dispose();
  barChart = donutChart = null;
}

function renderCharts() {
  disposeCharts();
  const barEl = barRef.value;
  const donutEl = donutRef.value;
  if (!barEl || !donutEl) return;

  const ob = orderedBarSeries(scoreDistribution.value);

  barChart = echarts.init(barEl);
  barChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "10%", top: "14%", containLabel: true },
    xAxis: {
      type: "category",
      name: "分数段（分）",
      nameLocation: "middle",
      nameGap: 28,
      data: ob.labels,
      axisLabel: { fontSize: 11, color: "#606266" },
    },
    yAxis: {
      type: "value",
      name: "人数（人）",
      minInterval: 1,
      splitLine: { lineStyle: { type: "dashed", color: "#ebeef5" } },
    },
    series: [
      {
        type: "bar",
        barMaxWidth: 40,
        itemStyle: { color: "#409EFF", borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: "top", fontSize: 11, color: "#409EFF" },
        data: ob.counts,
      },
    ],
  });

  const total = ob.counts.reduce((a, b) => a + b, 0);
  const fail = ob.counts[0] ?? 0;
  const pass = Math.max(0, total - fail);

  donutChart = echarts.init(donutEl);
  donutChart.setOption({
    tooltip: {
      trigger: "item",
      formatter: (p) => `${p.name}<br/>${p.value} 人（${p.percent}%）`,
    },
    legend: {
      orient: "vertical",
      right: "4%",
      top: "middle",
      itemGap: 12,
      formatter: (name) => {
        const item = [
          { name: "及格（≥60分）", v: pass },
          { name: "不及格（<60分）", v: fail },
        ].find((x) => x.name === name);
        const pct = total > 0 && item ? ((item.v * 100) / total).toFixed(2) : "0.00";
        return `${name}  ${item?.v ?? 0}人（${pct}%）`;
      },
      textStyle: { fontSize: 12, color: "#606266" },
    },
    series: [
      {
        type: "pie",
        radius: ["48%", "72%"],
        center: ["38%", "50%"],
        avoidLabelOverlap: true,
        label: {
          show: total > 0,
          position: "center",
          formatter: `{v|${total}}\n{s|总人数}`,
          rich: {
            v: { fontSize: 22, fontWeight: 700, color: "#303133", lineHeight: 32 },
            s: { fontSize: 13, color: "#909399", lineHeight: 24 },
          },
        },
        labelLine: { show: false },
        data: [
          { name: "及格（≥60分）", value: pass, itemStyle: { color: "#22C55E" } },
          { name: "不及格（<60分）", value: fail, itemStyle: { color: "#EF4444" } },
        ],
      },
    ],
  });

  resizeHandler = () => {
    barChart?.resize();
    donutChart?.resize();
  };
  window.addEventListener("resize", resizeHandler);
}

async function loadUsersList() {
  try {
    const { data } = await request.post("users/page", { page: 1, limit: 3000 });
    usersList.value = data?.list || [];
  } catch {
    usersList.value = [];
  }
}

async function fetchStatistics() {
  const id = examInfoId.value;
  if (id == null) return;
  const { data } = await request.post("gradesStatistics", {
    examInfoId: id,
    dateStart: STAT_DATE_START,
    dateEnd: STAT_DATE_END,
    trendRange: "7",
    page: 1,
    limit: 5,
  });
  summary.value = data.summary ?? null;
  scoreDistribution.value = data.scoreDistribution ?? [];
  const rows = data.examTable?.list ?? [];
  if (!examTitle.value && rows[0]?.examName) {
    examTitle.value = String(rows[0].examName);
  }
}

async function fetchRecords() {
  const id = examInfoId.value;
  if (id == null) return;
  const { data } = await request.post("examRecord/page", {
    page: 1,
    limit: 3000,
    onlyLatest: 1,
    examInfoId: id,
  });
  rawList.value = data?.list || [];
}

async function loadPage() {
  if (examInfoId.value == null) {
    ElMessage.warning("缺少考试参数");
    goBack();
    return;
  }
  try {
    await Promise.all([fetchStatistics(), fetchRecords(), loadUsersList()]);
    await nextTick();
    renderCharts();
  } catch (e) {
    console.error(e);
    ElMessage.error(e?.msg || "加载失败");
  }
}

function handleSizeChange() {
  page.value.page = 1;
}

async function handleExportScores() {
  const rows = filteredList.value;
  if (!rows.length) {
    ElMessage.warning("暂无成绩可导出");
    return;
  }
  try {
    const headers = ["序号", "考生姓名", "账号", "考生进入时间", "得分", "用时", "交卷时间", "考试状态"];
    const body = rows.map((row, i) => [
      i + 1,
      row.nickname || "—",
      getUserAccount(row),
      formatDateTime(row.insertTime),
      row.totalScore ?? "—",
      formatDuration(row),
      formatDateTime(row.endTime || row.insertTime),
      statusText(row),
    ]);
    const sheet = XLSX.utils.aoa_to_sheet([headers, ...body]);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, sheet, "成绩");
    const safe = (examTitle.value || "考试").replace(/[\\/:*?"<>|]/g, "_");
    XLSX.writeFile(wb, `${safe}_成绩_${new Date().toISOString().slice(0, 10)}.xlsx`);
    ElMessage.success("导出成功");
  } catch (e) {
    console.error(e);
    ElMessage.error("导出失败");
  }
}

watch(keyword, () => {
  page.value.page = 1;
});

onMounted(() => {
  scrollAdminLayoutToTop();
  loadPage();
});

watch(
  () => route.fullPath,
  () => {
    scrollAdminLayoutToTop();
  }
);

watch(
  () => route.query.examInfoId,
  (to, from) => {
    if (to === from) return;
    if (to == null || to === "") {
      goBack();
      return;
    }
    examTitle.value = String(route.query.examName || "").trim();
    loadPage();
  }
);

onBeforeUnmount(() => {
  disposeCharts();
});
</script>

<style scoped lang="scss">
.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 0;
}

.gs-detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.gs-detail-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.gs-detail-title {
  word-break: break-all;
}

.gs-back-icon {
  padding: 4px;
  color: #303133;
  flex-shrink: 0;
}

.gs-detail-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.gs-export-btn {
  background: #fff;
  border: 1px solid #dcdfe6;
}

.m-r-4 {
  margin-right: 4px;
}

.gs-detail-dashboard {
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

.summary-cards {
  display: grid;
  gap: 14px;
  margin-bottom: 16px;
}

.gs-detail-cards {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

@media (max-width: 1200px) {
  .gs-detail-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.summary-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  align-items: flex-start;
}

.summary-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-card-icon.icon-blue {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}
.summary-card-icon.icon-green {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}
.summary-card-icon.icon-orange {
  background: rgba(230, 162, 60, 0.15);
  color: #e6a23c;
}
.summary-card-icon.icon-purple {
  background: rgba(155, 89, 182, 0.12);
  color: #9b59b6;
}

.summary-card-body {
  min-width: 0;
}

.summary-card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
}

.summary-card-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 6px;
}

.summary-card-sub {
  font-size: 12px;
  color: #909399;
  line-height: 1.45;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

@media (max-width: 992px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

.chart-panel {
  padding: 12px 8px 8px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.chart-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  padding-left: 4px;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.filter-card {
  margin-bottom: 16px;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-item--search {
  max-width: 360px;
}

.filter-item .label {
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.filter-item :deep(.el-input) {
  width: 100%;
}

.table-scroll-x {
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

.gs-score {
  font-weight: 600;
}
.gs-score--pass {
  color: #22c55e;
}
.gs-score--fail {
  color: #ef4444;
}

.status-tag.status-completed {
  --el-tag-bg-color: rgba(103, 194, 58, 0.12);
  --el-tag-border-color: rgba(103, 194, 58, 0.35);
  --el-tag-text-color: #67c23a;
}
.status-tag.status-submitted,
.status-tag.status-forced-submit {
  --el-tag-bg-color: rgba(64, 158, 255, 0.1);
  --el-tag-border-color: rgba(64, 158, 255, 0.35);
  --el-tag-text-color: #409eff;
}
.status-tag.status-in-progress {
  --el-tag-bg-color: rgba(230, 162, 60, 0.12);
  --el-tag-border-color: rgba(230, 162, 60, 0.35);
  --el-tag-text-color: #e6a23c;
}
.status-tag.status-absent,
.status-tag.status-unknown {
  --el-tag-bg-color: rgba(144, 147, 153, 0.12);
  --el-tag-border-color: rgba(144, 147, 153, 0.3);
  --el-tag-text-color: #909399;
}

.action-btn.detail-btn {
  min-width: 56px;
  height: 30px;
  border-radius: 6px;
  font-weight: 600;
  padding: 0 12px;
  color: #fff;
  background: #409eff;
  border: 1px solid #409eff;
}
.action-btn.detail-btn:hover {
  background: #66b1ff;
  border-color: #66b1ff;
  color: #fff;
}

/* 分页：与成绩与统计列表页一致 */
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

  .el-pagination__sizes .el-select {
    width: auto;
    min-width: 100px;

    .el-select__wrapper {
      border-radius: 6px;
      padding: 0 8px;
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

  .el-pager li {
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
    }
  }

  .el-pagination__jump {
    color: #606266;
  }

  .el-input__wrapper {
    border-radius: 6px;
  }
}

/* 回到顶部：默认 z-index 仅 5，易被表格/分页遮挡；与成绩与统计页共用 */
:deep(.gs-page-backtop.el-backtop) {
  z-index: 2000;
}
</style>
