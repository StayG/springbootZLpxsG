<template>
  <div class="module-container">
    <div class="page-title">考试记录</div>

    <div class="filter-card">
      <div class="filter-grid">
        <div class="filter-item">
          <div class="label">考试名称</div>
          <el-input v-model="filters.examPaperName" placeholder="请输入考试名称" clearable />
        </div>
        <div class="filter-item">
          <div class="label">考生姓名</div>
          <el-input v-model="filters.nickname" placeholder="请输入考生姓名" clearable />
        </div>
        <div class="filter-item">
          <div class="label">考试状态</div>
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="全部状态" value="" />
            <el-option label="考试中" value="in_progress" />
            <el-option label="已提交" value="submitted" />
            <el-option label="强制交卷" value="forced_submit" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">考试时间</div>
          <el-date-picker
            v-model="filters.examDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择考试日期"
            clearable
          />
        </div>
        <div class="filter-item">
          <div class="label">成绩范围</div>
          <div class="score-range">
            <el-input-number v-model="filters.minScore" :min="0" :max="100" :controls="false" placeholder="最低分" />
            <span class="split-line">~</span>
            <el-input-number v-model="filters.maxScore" :min="0" :max="100" :controls="false" placeholder="最高分" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="operation-container">
      <div class="operation-left">
      <el-button
        class="op-btn"
        type="primary"
        :disabled="!hasCompletedSelection"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
      <el-button
        class="op-btn"
        type="primary"
        :disabled="!hasCompletedSelection"
        @click="handleBatchExportScore"
      >
        批量导出成绩
      </el-button>
      </div>
    </div>

    <div class="table-scroll-x">
      <div class="toolbar">
        <div class="toolbar-left"></div>
        <el-button circle @click="getDataList">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table :data="pageList" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" header-align="center" :selectable="rowSelectable" />
        <el-table-column label="序号" type="index" width="70" align="center" header-align="center" />
        <el-table-column label="考试名称" min-width="220" align="center" header-align="center">
          <template #default="{ row }">
            {{ getExamName(row) }}
          </template>
        </el-table-column>
        <el-table-column label="科目" width="110" align="center" header-align="center">
          <template #default="{ row }">
            {{ getSubjectLabel(row) }}
          </template>
        </el-table-column>
        <el-table-column label="考生姓名" min-width="120" prop="nickname" align="center" header-align="center" />
        <el-table-column label="考生账号" min-width="130" align="center" header-align="center">
          <template #default="{ row }">
            {{ getUserAccount(row) }}
          </template>
        </el-table-column>
        <el-table-column label="考生进入时间" min-width="180" prop="insertTime" align="center" header-align="center" />
        <el-table-column label="用时" width="100" align="center" header-align="center">
          <template #default="{ row }">
            {{ getDuration(row) }}
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <span :class="['score-text', getScoreClass(row.totalScore)]">
              {{ row.totalScore ?? "-" }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag class="status-tag" :class="`status-${getStatusKey(row)}`" effect="light">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" header-align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="showRowDetail(row)">
              <el-button class="action-btn detail-btn" @click="goToDetails(row)">详情</el-button>
            </template>
            <template v-if="showRowDelete(row)">
              <el-button class="action-btn delete-btn" @click="handleDelete(row)">删除</el-button>
            </template>
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
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import * as XLSX from "xlsx";
import storage from "@/utils/storage";
import config from "@/config/config";
import { useUserStore } from "@/stores/user";
import { getKemuOptions } from "@/utils/dictionary";

const router = useRouter();
const rawList = ref([]);
const examInfoList = ref([]);
const usersList = ref([]);
const multipleSelection = ref([]);
const kemuOptions = ref([]);

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
  examPaperName: "",
  nickname: "",
  status: "",
  minScore: null,
  maxScore: null,
  timeRange: [],
  examDate: "",
});

const examInfoMap = computed(() => {
  const map = new Map();
  examInfoList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) map.set(item.id, item);
  });
  return map;
});

const usersMap = computed(() => {
  const map = new Map();
  usersList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) map.set(item.id, item);
  });
  return map;
});

const toTime = (value) => {
  if (!value) return null;
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? null : time;
};

const getStatusText = (row) => {
  if (row.status === null || row.status === undefined) return "缺考";
  if (row.status === 0) return "考试中";
  if (row.status === 1) return "已提交";
  if (row.status === 2) return "强制交卷";
  if (row.status === 3) return "已完成";
  return "未知";
};

const getStatusKey = (row) => {
  const status = getStatusText(row);
  if (status === "缺考") return "absent";
  if (status === "考试中") return "in-progress";
  if (status === "已提交") return "submitted";
  if (status === "强制交卷") return "forced-submit";
  if (status === "已完成") return "completed";
  return "unknown";
};

/** 仅「已完成」可勾选批量删除/导出成绩 */
const rowSelectable = (row) => Number(row?.status) === 3;

/** 考试中（status 为 0）不显示详情；缺考（null/undefined）仍显示详情 */
const showRowDetail = (row) => !(row?.status === 0 || row?.status === "0");

const showRowDelete = (row) => Number(row?.status) === 3;

/** 仅「已完成」记录可勾选，故非空即全部为已完成 */
const hasCompletedSelection = computed(() => multipleSelection.value.length > 0);

const getScoreClass = (score) => {
  if (score === null || score === undefined) return "";
  if (score >= 90) return "score-high";
  if (score >= 60) return "score-medium";
  return "score-low";
};

const getDuration = (row) => {
  const start = toTime(row.insertTime);
  const end = toTime(row.endTime);
  if (!start || !end || end <= start) return "-";
  return `${Math.round((end - start) / 60000)}分钟`;
};

const getExamName = (row) => {
  if (row.examName) return row.examName;
  return examInfoMap.value.get(row.examInfoId)?.examName || row.examPaperName || "-";
};

const getExamKemuTypes = (row) => {
  const info = examInfoMap.value.get(row.examInfoId);
  const kemu = Number(row?.kemuTypes ?? info?.kemuTypes ?? info?.kemuValue ?? 0);
  return Number.isFinite(kemu) && kemu > 0 ? kemu : null;
};

const getSubjectLabel = (row) => {
  const kemu = getExamKemuTypes(row);
  if (!kemu) return "-";
  const hit = kemuOptions.value.find((it) => Number(it.value) === Number(kemu));
  return hit?.label || examInfoMap.value.get(row.examInfoId)?.kemuTypesName || "-";
};

const getUserAccount = (row) => {
  if (row.username || row.account || row.userNumber) {
    return row.username || row.account || row.userNumber;
  }
  return usersMap.value.get(row.usersId)?.userName || (row.usersId ? `user${row.usersId}` : "-");
};

const examNameKeyword = (kw) => {
  const s = String(kw || "").trim();
  return s;
};

/** 仅考试安排名称（与后端 ei.exam_name 一致），不含试卷名 */
const getExamArrangementNameOnly = (row) => {
  if (!row) return "";
  if (row.examName) return String(row.examName).trim();
  const info = examInfoMap.value.get(row.examInfoId);
  return info?.examName ? String(info.examName).trim() : "";
};

const rowMatchesExamNameKeyword = (row, kw) => {
  if (!kw) return true;
  return getExamArrangementNameOnly(row).includes(kw);
};

const filteredList = computed(() => {
  const nameKw = examNameKeyword(filters.value.examPaperName);
  return rawList.value.filter((row) => {
    // 后端已通过 SQL 实现教师数据隔离（按科目过滤），前端无需再次过滤科目
    // if (isTeacherRole.value && teacherKemuTypes.value) {
    //   const kemu = getExamKemuTypes(row);
    //   if (!kemu || Number(kemu) !== Number(teacherKemuTypes.value)) return false;
    // }

    if (!rowMatchesExamNameKeyword(row, nameKw)) return false;

    if (filters.value.status && filters.value.status !== ({
      "考试中": "in_progress",
      "已提交": "submitted",
      "强制交卷": "forced_submit",
      "已完成": "completed",
      "缺考": "absent",
    }[getStatusText(row)] || "")) {
      return false;
    }

    if (filters.value.minScore !== null && filters.value.minScore !== undefined) {
      if ((row.totalScore ?? -1) < filters.value.minScore) return false;
    }
    if (filters.value.maxScore !== null && filters.value.maxScore !== undefined) {
      if ((row.totalScore ?? 101) > filters.value.maxScore) return false;
    }

    if (filters.value.examDate) {
      const rowTime = toTime(row.insertTime);
      const start = toTime(`${filters.value.examDate} 00:00:00`);
      const end = toTime(`${filters.value.examDate} 23:59:59`);
      if (!rowTime || !start || !end || rowTime < start || rowTime > end) return false;
    }
    return true;
  });
});

const total = computed(() => filteredList.value.length);

const pageList = computed(() => {
  const start = (page.value.page - 1) * page.value.limit;
  const end = start + page.value.limit;
  return filteredList.value.slice(start, end);
});

const getDataList = async () => {
  try {
    const { data } = await request.post("examRecord/page", {
      page: 1,
      limit: 1000,
      onlyLatest: 1,
      examPaperName: filters.value.examPaperName,
      examName: filters.value.examPaperName,
      nickname: filters.value.nickname,
    });
    rawList.value = data?.list || [];
  } catch (error) {
    console.error("获取考试记录失败:", error);
    ElMessage.error("获取考试记录失败");
  }
};

const handleSearch = async () => {
  page.value.page = 1;
  await getDataList();
};

const handleReset = async () => {
  filters.value = {
    examPaperName: "",
    nickname: "",
    status: "",
    minScore: null,
    maxScore: null,
    timeRange: [],
    examDate: "",
  };
  page.value.page = 1;
  await getDataList();
};

const handleSizeChange = () => {
  page.value.page = 1;
};

const handleCurrentChange = () => {};

const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

const goToDetails = (row) => {
  router.push({
    path: "/examDetails",
    query: {
      examRecordId: row.id,
      examDetailsUuidNumber: row.examRecordUuidNumber,
      examName: getExamName(row),
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
};

const handleBatchDelete = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要删除的记录（仅已完成状态可批量删除）");
    return;
  }
  const nonCompleted = multipleSelection.value.filter((r) => Number(r?.status) !== 3);
  if (nonCompleted.length) {
    ElMessage.warning("只能删除已完成状态的考试记录");
    return;
  }
  try {
    await ElMessageBox.confirm("确定删除选中的考试记录吗？", "提示", { type: "warning" });
    const ids = multipleSelection.value.map((item) => Number(item.id));
    await request.post("examRecord/del/batch", ids);
    ElMessage.success("批量删除成功");
    multipleSelection.value = [];
    await getDataList();
  } catch (error) {
    if (error === "cancel") return;
    console.error("批量删除失败:", error);
    ElMessage.error("批量删除失败");
  }
};

const handleBatchExportScore = () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要导出的记录（仅已完成状态可导出成绩）");
    return;
  }
  const nonCompleted = multipleSelection.value.filter((r) => Number(r?.status) !== 3);
  if (nonCompleted.length) {
    ElMessage.warning("只能导出已完成状态的考试记录");
    return;
  }
  const headers = ["考试名称", "科目", "考生姓名", "考生账号", "考生进入时间", "用时", "成绩", "状态"];
  const rows = multipleSelection.value.map((row) => [
    getExamName(row),
    getSubjectLabel(row),
    row.nickname || "-",
    getUserAccount(row),
    row.insertTime || "-",
    getDuration(row),
    row.totalScore ?? "-",
    getStatusText(row),
  ]);
  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, sheet, "考试记录");
  const buffer = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
  const blob = new Blob([buffer], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `考试记录_${new Date().toISOString().slice(0, 10)}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success("导出成功");
};

const handleDelete = async (row) => {
  if (!row?.id) return;
  if (Number(row?.status) !== 3) {
    ElMessage.warning("只能删除已完成状态的考试记录");
    return;
  }
  try {
    await ElMessageBox.confirm("确定删除该考试记录吗？", "提示", { type: "warning" });
    await request.delete(`examRecord/del/${row.id}`);
    ElMessage.success("删除成功");
    await getDataList();
  } catch (error) {
    if (error === "cancel") return;
    console.error("删除失败:", error);
    ElMessage.error("删除失败");
  }
};

const loadExamInfoList = async () => {
  try {
    const { data } = await request.post("examInfo/page", { page: 1, limit: 2000 });
    examInfoList.value = data?.list || [];
  } catch (error) {
    console.error("加载考试列表失败:", error);
  }
};

const loadUsersList = async () => {
  try {
    const { data } = await request.post("users/page", { page: 1, limit: 3000 });
    usersList.value = data?.list || [];
  } catch (error) {
    console.error("加载用户列表失败:", error);
  }
};

const loadKemuOptions = async () => {
  try {
    kemuOptions.value = (await getKemuOptions()) || [];
  } catch {
    kemuOptions.value = [];
  }
};

const route = useRoute();

onMounted(() => {
  Promise.all([loadExamInfoList(), loadUsersList(), loadKemuOptions()]).finally(() => {
    getDataList();
  });
});

// 监听路由变化，当从其他页面返回时重新加载数据
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 当路由切换到考试记录页面时，重新加载数据
    if (newPath === '/examRecord' && oldPath && oldPath !== newPath) {
      getDataList();
    }
  }
);
</script>

<style lang="scss" scoped>
.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
}

/* 筛选区排版与错题本一致：filter-card + filter-grid + label + filter-actions */
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

.operation-container,
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

.score-range {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;

  :deep(.el-input-number) {
    flex: 1;
    min-width: 0;
  }
}

.split-line {
  color: #909399;
  flex-shrink: 0;
}

.expand-btn {
  margin-left: 8px;
}

.operation-container {
  display: flex;
  justify-content: flex-start;
}

.operation-left {
  display: flex;
  gap: 10px;
}

.op-btn {
  min-width: 124px;
  height: 36px;
  border-radius: 8px;
  font-weight: 600;
  padding: 0 14px;
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
}

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

.delete-btn {
  color: #fff;
  background: #f56c6c;
  border: 1px solid #f56c6c;
}

.delete-btn:hover {
  background: #f78989;
  border-color: #f78989;
  color: #fff;
}

.score-text {
  font-weight: 600;
}

.score-high {
  color: #36b37e;
}

.score-medium {
  color: #e6a23c;
}

.score-low {
  color: #f56c6c;
}

.status-tag {
  border-width: 1px;
}

.status-absent {
  color: #909399;
  border-color: #d3d4d6;
  background: #f4f4f5;
}

.status-in-progress {
  color: #409eff;
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.status-submitted {
  color: #e6a23c;
  border-color: #f3d19e;
  background: #fdf6ec;
}

.status-forced-submit {
  color: #f56c6c;
  border-color: #fab6b6;
  background: #fef0f0;
}

.status-completed {
  color: #67c23a;
  border-color: #c2e7b0;
  background: #f0f9eb;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-date-editor .el-input__wrapper),
:deep(.el-input-number .el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-date-editor .el-input__wrapper:hover),
:deep(.el-input-number .el-input__wrapper:hover) {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused),
:deep(.el-date-editor .el-input__wrapper.is-focus),
:deep(.el-input-number .el-input__wrapper.is-focus) {
  border-color: #1890ff;
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
}

:deep(.el-date-editor) {
  width: 100%;
}

/* 按钮统一蓝色 */
:deep(.el-button) {
  font-weight: 500;
  border-radius: 6px;
}

:deep(.el-button--primary),
:deep(.el-button--default) {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

:deep(.el-button--primary:hover),
:deep(.el-button--default:hover) {
  background: #40a9ff;
  border-color: #40a9ff;
  color: #fff;
}

:deep(.el-button--primary.is-disabled),
:deep(.el-button--default.is-disabled) {
  background: #a0cfff;
  border-color: #a0cfff;
  color: #fff;
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
  .filter-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }

  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>

