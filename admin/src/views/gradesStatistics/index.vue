<template>
  <div class="module-container">
    <div class="page-title">成绩与统计</div>

    <div class="filter-card">
      <div class="filter-grid">
        <div class="filter-item">
          <div class="label">考试</div>
          <el-select v-model="filters.examInfoId" placeholder="全部考试" clearable filterable>
            <el-option label="全部考试" :value="null" />
            <el-option
              v-for="ex in examOptions"
              :key="ex.id"
              :label="ex.examName"
              :value="ex.id"
            />
          </el-select>
        </div>
        <div class="filter-item filter-item--daterange">
          <div class="label">考试日期</div>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="—"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
          />
        </div>
        <div class="filter-item">
          <div class="label">考试记录状态</div>
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="全部状态" :value="''" />
            <el-option label="考试中" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="强制交卷" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </div>
      </div>
      <div class="filter-actions filter-actions--split">
        <div class="filter-actions-left">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <el-button
          v-if="utils.isAuth('gradesStatistics', '导出')"
          class="export-report-btn"
          @click="handleExportReport"
        >
          <el-icon class="m-r-4"><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <div class="grades-dashboard">
    <div class="summary-cards">
      <div v-for="(c, idx) in summaryCards" :key="idx" class="summary-card">
        <div class="summary-card-icon" :class="c.iconClass">
          <el-icon :size="summaryIconSize"><component :is="c.icon" /></el-icon>
        </div>
        <div class="summary-card-body">
          <div class="summary-card-label">{{ c.label }}</div>
          <div class="summary-card-value">{{ c.value }}</div>
          <div class="summary-card-trend" :class="{ up: c.trendUp, down: !c.trendUp && c.hasTrend }">
            {{ c.trendText }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表行 1 -->
    <div class="chart-row">
      <div class="chart-panel">
        <div class="chart-panel-title">成绩分布</div>
        <div ref="donutRef" class="chart-box" />
      </div>
      <div class="chart-panel">
        <div class="chart-panel-head">
          <span class="chart-panel-title">平均分趋势</span>
          <div class="chart-tabs">
            <el-radio-group v-model="trendTab" size="small" @change="onTrendTabChange">
              <el-radio-button label="7">近7天</el-radio-button>
              <el-radio-button label="30">近30天</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-if="trendTab === 'custom'"
              v-model="customTrendRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="—"
              start-placeholder="开始"
              end-placeholder="结束"
              size="small"
              class="chart-custom-range"
              @change="handleTrendRangeCustomChange"
            />
          </div>
        </div>
        <div ref="avgAreaRef" class="chart-box" />
      </div>
    </div>

    <!-- 图表行 2 -->
    <div class="chart-row">
      <div class="chart-panel">
        <div class="chart-panel-title">各考试平均分对比</div>
        <div ref="barRef" class="chart-box" />
      </div>
      <div class="chart-panel">
        <div class="chart-panel-head">
          <span class="chart-panel-title">及格率趋势</span>
          <div class="chart-tabs">
            <el-radio-group v-model="trendTab" size="small" @change="onTrendTabChange">
              <el-radio-button label="7">近7天</el-radio-button>
              <el-radio-button label="30">近30天</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-if="trendTab === 'custom'"
              v-model="customTrendRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="—"
              start-placeholder="开始"
              end-placeholder="结束"
              size="small"
              class="chart-custom-range"
              @change="handleTrendRangeCustomChange"
            />
          </div>
        </div>
        <div ref="passAreaRef" class="chart-box" />
      </div>
    </div>
    </div>

    <div class="table-scroll-x">
      <div class="toolbar">
        <div class="toolbar-left table-section-title">考试成绩列表</div>
        <el-button class="table-toolbar-refresh" @click="fetchData" aria-label="刷新">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      <el-table :data="tableList" border stripe header-align="center">
        <el-table-column
          type="index"
          label="序号"
          width="70"
          align="center"
          header-align="center"
          :index="tableIndexMethod"
        />
        <el-table-column prop="examName" label="考试名称" min-width="200" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column label="考试时间" min-width="340" align="center" header-align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.examTimeDisplay || "—" }}</template>
        </el-table-column>
        <el-table-column prop="participants" label="参考人数" width="110" align="center" header-align="center" />
        <el-table-column label="平均分" width="100" align="center" header-align="center">
          <template #default="{ row }">{{ formatFixed(row.avgScore, 2) }}</template>
        </el-table-column>
        <el-table-column prop="maxScore" label="最高分" width="90" align="center" header-align="center" />
        <el-table-column label="及格率" width="100" align="center" header-align="center">
          <template #default="{ row }">{{ formatPassRate(row.passRate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" header-align="center" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn detail-btn" @click="goExamRecord(row)">查看成绩</el-button>
            <el-button v-if="utils.isAuth('gradesStatistics', '导出')" class="gs-row-export" @click="exportRow(row)">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page.page"
        v-model:page-size="page.limit"
        :total="tableTotal"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
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
import {
  Download,
  Refresh,
  UserFilled,
  Odometer,
  StarFilled,
  CircleCheck,
  Calendar,
} from "@element-plus/icons-vue";
import { ElMessage, ElLoading } from "element-plus";
import * as XLSX from "xlsx";
import request from "@/utils/request";
import utils from "@/utils/utils";
import { scrollAdminLayoutToTop } from "@/utils/adminScrollLayout.js";

const router = useRouter();
const route = useRoute();

const donutRef = ref(null);
const avgAreaRef = ref(null);
const barRef = ref(null);
const passAreaRef = ref(null);
let donutChart;
let avgChart;
let barChart;
let passChart;

const examOptions = ref([]);
const dateRange = ref([]);
const filters = ref({
  examInfoId: null,
  status: "",
});

/** 汇总卡片内图标尺寸（与容器同步放大） */
const summaryIconSize = 34;

const trendTab = ref("7");
const customTrendRange = ref([]);
let resizeHandler = null;

const summary = ref(null);
const scoreDistribution = ref([]);
const avgScoreTrend = ref([]);
const passRateTrend = ref([]);
const examAvgCompare = ref([]);
const tableList = ref([]);
const tableTotal = ref(0);
const page = ref({ page: 1, limit: 10 });

const summaryCards = computed(() => {
  const s = summary.value;
  if (!s) {
    return [
      { label: "参考人数", value: "—", trendText: "较上期 —", trendUp: true, hasTrend: false, icon: UserFilled, iconClass: "icon-blue" },
      { label: "平均分", value: "—", trendText: "较上期 —", trendUp: true, hasTrend: false, icon: Odometer, iconClass: "icon-green" },
      { label: "最高分", value: "—", trendText: "较上期 —", trendUp: true, hasTrend: false, icon: StarFilled, iconClass: "icon-purple" },
      { label: "及格率", value: "—", trendText: "较上期 —", trendUp: true, hasTrend: false, icon: CircleCheck, iconClass: "icon-orange" },
      { label: "考试次数", value: "—", trendText: "较上期 —", trendUp: true, hasTrend: false, icon: Calendar, iconClass: "icon-teal" },
    ];
  }
  const pick = (m) => ({
    value: m?.value ?? "—",
    trendText: m?.trendText ?? "较上期 —",
    trendUp: m?.trendUp !== false,
    hasTrend: String(m?.trendText ?? "").includes("%") || String(m?.trendText ?? "").match(/\d/),
  });
  const pa = pick(s.participants);
  const av = pick(s.avgScore);
  const mx = pick(s.maxScore);
  const pr = pick(s.passRate);
  const ex = pick(s.examCount);
  return [
    { label: "参考人数", ...pa, icon: UserFilled, iconClass: "icon-blue" },
    { label: "平均分", ...av, icon: Odometer, iconClass: "icon-green" },
    { label: "最高分", ...mx, icon: StarFilled, iconClass: "icon-purple" },
    { label: "及格率", ...pr, icon: CircleCheck, iconClass: "icon-orange" },
    { label: "考试次数", ...ex, icon: Calendar, iconClass: "icon-teal" },
  ];
});

function buildPayload(tr, ts, te) {
  return {
    examInfoId: filters.value.examInfoId ?? undefined,
    dateStart: dateRange.value?.[0],
    dateEnd: dateRange.value?.[1],
    status: filters.value.status === "" ? undefined : filters.value.status,
    trendRange: tr,
    trendDateStart: ts,
    trendDateEnd: te,
    page: page.value.page,
    limit: page.value.limit,
  };
}

/** 导出接口用：与列表相同筛选，不传分页 */
function buildExportPayload() {
  let tr = trendTab.value;
  let ts;
  let te;
  if (tr === "custom") {
    ts = customTrendRange.value?.[0];
    te = customTrendRange.value?.[1];
    if (!ts || !te) {
      tr = "7";
    }
  }
  return {
    examInfoId: filters.value.examInfoId ?? undefined,
    dateStart: dateRange.value?.[0],
    dateEnd: dateRange.value?.[1],
    status: filters.value.status === "" ? undefined : filters.value.status,
    trendRange: tr,
    trendDateStart: ts,
    trendDateEnd: te,
  };
}

function buildSummaryExportRows() {
  const s = summary.value;
  if (!s) return [{ 指标: "—", 当期值: "—", 较上期: "—" }];
  const row = (label, card) => ({
    指标: label,
    当期值: card?.value ?? "—",
    较上期: card?.trendText ?? "—",
  });
  return [
    row("参考人数", s.participants),
    row("平均分", s.avgScore),
    row("最高分", s.maxScore),
    row("及格率", s.passRate),
    row("考试次数", s.examCount),
  ];
}

function buildDistributionExportRows() {
  return (scoreDistribution.value ?? []).map((d) => ({
    分数段: d.label,
    人数: d.count,
    占比: d.percent != null ? `${Number(d.percent).toFixed(2)}%` : "—",
  }));
}

function buildTrendExportRows(points) {
  return (points ?? []).map((p) => ({
    日期: p.date,
    数值: p.value != null ? Number(p.value).toFixed(2) : "—",
  }));
}

function buildCompareExportRows() {
  return (examAvgCompare.value ?? []).map((x) => ({
    考试: x.name,
    平均分: x.avgScore != null ? Number(x.avgScore).toFixed(2) : "—",
  }));
}

function buildTableExportRows(list) {
  return list.map((r, i) => ({
    序号: i + 1,
    考试名称: r.examName,
    考试时间: r.examTimeDisplay || "—",
    参考人数: r.participants,
    平均分: formatFixed(r.avgScore, 2),
    最高分: r.maxScore ?? "—",
    及格率: formatPassRate(r.passRate),
  }));
}

async function fetchData() {
  let tr = trendTab.value;
  let ts;
  let te;
  if (tr === "custom") {
    ts = customTrendRange.value?.[0];
    te = customTrendRange.value?.[1];
    if (!ts || !te) {
      tr = "7";
    }
  }
  try {
    const { data } = await request.post("gradesStatistics", buildPayload(tr, ts, te));
    applyResponse(data);
    await nextTick();
    renderCharts();
  } catch (e) {
    console.error(e);
  }
}

function applyResponse(data) {
  summary.value = data.summary ?? null;
  scoreDistribution.value = data.scoreDistribution ?? [];
  avgScoreTrend.value = data.avgScoreTrend ?? [];
  passRateTrend.value = data.passRateTrend ?? [];
  examAvgCompare.value = data.examAvgCompare ?? [];
  const t = data.examTable;
  tableList.value = t?.list ?? [];
  tableTotal.value = Number(t?.total ?? 0);
}

function onTrendTabChange() {
  page.value.page = 1;
  fetchData();
}

function handleTrendRangeCustomChange() {
  page.value.page = 1;
  fetchData();
}

function handleSearch() {
  page.value.page = 1;
  fetchData();
}

function handleSizeChange() {
  page.value.page = 1;
  fetchData();
}

function handleCurrentChange() {
  fetchData();
}

function handleReset() {
  filters.value = { examInfoId: null, status: "" };
  dateRange.value = [];
  trendTab.value = "7";
  customTrendRange.value = [];
  page.value.page = 1;
  page.value.limit = 10;
  fetchData();
}

function formatFixed(v, n) {
  if (v === undefined || v === null) return "—";
  const x = Number(v);
  return Number.isFinite(x) ? x.toFixed(n) : "—";
}

function formatPassRate(v) {
  if (v === undefined || v === null) return "—";
  const x = Number(v);
  return Number.isFinite(x) ? `${x.toFixed(2)}%` : "—";
}

/** 跨页连续序号，与后端分页一致 */
function tableIndexMethod(index) {
  return (page.value.page - 1) * page.value.limit + index + 1;
}

function disposeCharts() {
  if (resizeHandler) {
    window.removeEventListener("resize", resizeHandler);
    resizeHandler = null;
  }
  donutChart?.dispose();
  avgChart?.dispose();
  barChart?.dispose();
  passChart?.dispose();
  donutChart = avgChart = barChart = passChart = null;
}

function renderCharts() {
  disposeCharts();
  const donutEl = donutRef.value;
  const avgEl = avgAreaRef.value;
  const barEl = barRef.value;
  const passEl = passAreaRef.value;
  if (!donutEl || !avgEl || !barEl || !passEl) return;

  const dist = scoreDistribution.value;
  const pieData = dist.map((d, i) => ({
    name: d.label,
    value: d.count,
    pct: d.percent,
    itemStyle: { color: DONUT_COLORS[i % DONUT_COLORS.length] },
  }));

  donutChart = echarts.init(donutEl);
  donutChart.setOption({
    tooltip: {
      trigger: "item",
      formatter: (p) => {
        const pct = p.data?.pct != null ? `${Number(p.data.pct).toFixed(2)}%` : "";
        return `${p.name}<br/>${pct} (${p.value}人)`;
      },
    },
    legend: {
      orient: "vertical",
      right: "2%",
      top: "middle",
      itemGap: 12,
      formatter: (name) => {
        const item = pieData.find((x) => x.name === name);
        const pct = dist.find((x) => x.label === name)?.percent;
        const pstr = pct != null ? `${Number(pct).toFixed(2)}%` : "";
        const cnt = item?.value ?? 0;
        return `${name}    ${pstr} (${cnt}人)`;
      },
      textStyle: { fontSize: 12, color: "#606266", width: 200, overflow: "truncate" },
    },
    series: [
      {
        type: "pie",
        radius: ["48%", "72%"],
        center: ["38%", "50%"],
        avoidLabelOverlap: true,
        label: { show: false },
        data: pieData,
      },
    ],
  });

  const avgPts = avgScoreTrend.value;
  avgChart = echarts.init(avgEl);
  avgChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "3%", top: "12%", containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: avgPts.map((x) => x.date),
      axisLine: { lineStyle: { color: "#dcdfe6" } },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      splitLine: { lineStyle: { type: "dashed", color: "#ebeef5" } },
    },
    series: [
      {
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        lineStyle: { width: 2, color: "#409EFF" },
        areaStyle: { color: "rgba(64, 158, 255, 0.18)" },
        itemStyle: { color: "#409EFF" },
        label: {
          show: true,
          position: "top",
          formatter: (p) => (p.value != null ? Number(p.value).toFixed(1) : ""),
          fontSize: 11,
          color: "#409EFF",
        },
        data: avgPts.map((x) => x.value),
      },
    ],
  });

  const bars = examAvgCompare.value;
  barChart = echarts.init(barEl);
  barChart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "20%", top: "12%", containLabel: true },
    xAxis: {
      type: "category",
      data: bars.map((x) => x.name),
      axisLabel: { rotate: 28, fontSize: 11, interval: 0, color: "#606266" },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      splitLine: { lineStyle: { type: "dashed", color: "#ebeef5" } },
    },
    series: [
      {
        type: "bar",
        barMaxWidth: 36,
        itemStyle: { color: "#409EFF", borderRadius: [4, 4, 0, 0] },
        label: {
          show: true,
          position: "top",
          formatter: (p) => (p.value != null ? Number(p.value).toFixed(1) : ""),
          fontSize: 11,
          color: "#409EFF",
        },
        data: bars.map((x) => x.avgScore),
      },
    ],
  });

  const passPts = passRateTrend.value;
  passChart = echarts.init(passEl);
  passChart.setOption({
    tooltip: { trigger: "axis", valueFormatter: (v) => (v != null ? `${Number(v).toFixed(2)}%` : "") },
    grid: { left: "3%", right: "4%", bottom: "3%", top: "12%", containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: passPts.map((x) => x.date),
      axisLine: { lineStyle: { color: "#dcdfe6" } },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      axisLabel: { formatter: "{value}%" },
      splitLine: { lineStyle: { type: "dashed", color: "#ebeef5" } },
    },
    series: [
      {
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        lineStyle: { width: 2, color: "#67C23A" },
        areaStyle: { color: "rgba(103, 194, 58, 0.15)" },
        itemStyle: { color: "#67C23A" },
        label: {
          show: true,
          position: "top",
          formatter: (p) => (p.value != null ? `${Number(p.value).toFixed(2)}%` : ""),
          fontSize: 11,
          color: "#67C23A",
        },
        data: passPts.map((x) => x.value),
      },
    ],
  });

  resizeHandler = () => {
    donutChart?.resize();
    avgChart?.resize();
    barChart?.resize();
    passChart?.resize();
  };
  window.addEventListener("resize", resizeHandler);
}

/** 成绩分布环形图：与后端分段顺序一致 90-100、80-89、70-79、60-69、60分以下 */
const DONUT_COLORS = ["#22C55E", "#3B82F6", "#8B5CF6", "#F59E0B", "#EF4444"];

async function loadExamOptions() {
  try {
    /** 仅「已结束」考试（exam_info.status = 3） */
    const { data } = await request.post("examInfo/page", { page: 1, limit: 500, status: 3 });
    examOptions.value = data.list ?? [];
    const ids = new Set(examOptions.value.map((x) => x.id));
    if (filters.value.examInfoId != null && !ids.has(filters.value.examInfoId)) {
      filters.value.examInfoId = null;
    }
  } catch {
    examOptions.value = [];
  }
}

function appendSheet(wb, sheetName, rows) {
  const name = String(sheetName).slice(0, 31);
  const body = rows?.length ? rows : [{ 说明: "暂无数据" }];
  XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(body), name);
}

async function handleExportReport() {
  let loading;
  try {
    loading = ElLoading.service({
      lock: true,
      text: "正在生成报表…",
      background: "rgba(0, 0, 0, 0.12)",
    });
    const { data: list } = await request.post("gradesStatistics/exportTable", buildExportPayload(), { timeout: 120000 });
    const tableRows = Array.isArray(list) ? list : [];
    if (!tableRows.length) {
      ElMessage.warning("当前筛选条件下暂无考试成绩可导出");
      return;
    }
    const wb = XLSX.utils.book_new();
    appendSheet(wb, "汇总", buildSummaryExportRows());
    appendSheet(wb, "成绩分布", buildDistributionExportRows());
    appendSheet(wb, "平均分趋势", buildTrendExportRows(avgScoreTrend.value));
    appendSheet(wb, "及格率趋势", buildTrendExportRows(passRateTrend.value));
    appendSheet(wb, "平均分对比", buildCompareExportRows());
    appendSheet(wb, "考试成绩列表", buildTableExportRows(tableRows));
    XLSX.writeFile(wb, `成绩与统计报表_${new Date().toISOString().slice(0, 10)}.xlsx`);
    ElMessage.success("导出成功");
  } catch (e) {
    console.error(e);
    ElMessage.error(e?.msg || "导出失败，请稍后重试");
  } finally {
    loading?.close();
  }
}

function exportRow(row) {
  if (!row) {
    ElMessage.warning("无数据可导出");
    return;
  }
  try {
    const wb = XLSX.utils.book_new();
    appendSheet(wb, "成绩", buildTableExportRows([row]));
    const safe = String(row.examName ?? "考试").replace(/[\\/:*?"<>|]/g, "_");
    XLSX.writeFile(wb, `${safe}_成绩.xlsx`);
    ElMessage.success("导出成功");
  } catch (e) {
    console.error(e);
    ElMessage.error("导出失败");
  }
}

function goExamRecord(row) {
  if (row?.examInfoId != null) {
    router.push({
      path: "/gradesStatisticsDetail",
      query: {
        examInfoId: String(row.examInfoId),
        examName: row.examName != null ? String(row.examName) : "",
      },
    });
  } else {
    router.push("/gradesStatistics");
  }
}

onMounted(async () => {
  scrollAdminLayoutToTop();
  await loadExamOptions();
  await fetchData();
});

watch(
  () => route.path,
  async (newPath, oldPath) => {
    scrollAdminLayoutToTop();
    // 当路由切换到成绩统计页面时，重新加载数据
    if (newPath === '/gradesStatistics' && oldPath && oldPath !== newPath) {
      await loadExamOptions();
      await fetchData();
    }
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
  margin-bottom: 16px;
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

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 14px 16px;
}

.filter-item {
  min-width: 0;
}

.filter-item .label {
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.filter-item :deep(.el-input),
.filter-item :deep(.el-select),
.filter-item :deep(.el-date-editor) {
  width: 100%;
}

.filter-actions {
  margin-top: 0;
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-actions--split {
  justify-content: space-between;
  flex-wrap: wrap;
}

.filter-actions-left {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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

.export-report-btn {
  min-width: 108px;
  height: 36px;
  border-radius: 8px;
  font-weight: 600;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.export-report-btn:hover {
  border-color: #2386ea;
  color: #2386ea;
}

.m-r-4 {
  margin-right: 4px;
}

.grades-dashboard {
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

.toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 考试成绩列表刷新：白底、细灰边、圆角方形的图标按钮（与参考图一致） */
.table-scroll-x .toolbar :deep(.table-toolbar-refresh.el-button) {
  width: 32px;
  height: 32px;
  min-width: 32px;
  padding: 0;
  margin: 0;
  border-radius: 4px;
  font-weight: 400;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.table-scroll-x .toolbar :deep(.table-toolbar-refresh.el-button .el-icon) {
  font-size: 16px;
}

.table-scroll-x .toolbar :deep(.table-toolbar-refresh.el-button:hover),
.table-scroll-x .toolbar :deep(.table-toolbar-refresh.el-button:focus) {
  background: #fff;
  border-color: #c0c4cc;
  color: #303133;
}

.table-scroll-x .toolbar :deep(.table-toolbar-refresh.el-button:active) {
  background: #f5f7fa;
  border-color: #c0c4cc;
  color: #303133;
}

/* 与考试记录页「详情」按钮一致 */
.action-btn {
  min-width: 56px;
  height: 30px;
  border-radius: 6px;
  font-weight: 600;
  padding: 0 12px;
}

.detail-btn {
  color: #fff;
  background: #409eff;
  border: 1px solid #409eff;
}

.detail-btn:hover {
  background: #66b1ff;
  border-color: #66b1ff;
  color: #fff;
}

.table-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

@media (max-width: 1400px) {
  .summary-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .summary-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

.summary-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 14px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}

.summary-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.summary-card-icon.icon-blue {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}
.summary-card-icon.icon-green {
  background: linear-gradient(135deg, #67c23a, #95d475);
}
.summary-card-icon.icon-purple {
  background: linear-gradient(135deg, #9b59b6, #b07cc8);
}
.summary-card-icon.icon-orange {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
}
.summary-card-icon.icon-teal {
  background: linear-gradient(135deg, #13c2c2, #5cdbd3);
}

.summary-card-body {
  min-width: 0;
}

.summary-card-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.summary-card-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.summary-card-trend {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.summary-card-trend.up {
  color: #f56c6c;
}

.summary-card-trend.down {
  color: #67c23a;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}

.chart-row:last-child {
  margin-bottom: 0;
}

@media (max-width: 1100px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

.chart-panel {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  padding: 14px 14px 8px;
  min-height: 320px;
}

.chart-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.chart-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.chart-panel-head .chart-panel-title {
  margin-bottom: 0;
}

.chart-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.chart-custom-range {
  width: 240px !important;
}

.chart-box {
  width: 100%;
  height: 280px;
}

.gs-row-export {
  margin-left: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-date-editor .el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-date-editor .el-input__wrapper:hover) {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused),
:deep(.el-date-editor .el-input__wrapper.is-focus) {
  border-color: #1890ff;
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-button.is-link) {
  background: transparent;
  border-color: transparent;
  color: #1890ff;
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

:deep(.gs-page-backtop.el-backtop) {
  z-index: 2000;
}
</style>
