<template>
  <div class="module-container">
    <div class="grading-title">阅卷管理</div>

    <div class="filter-card">
      <div class="filter-grid">
        <div class="filter-item">
          <div class="label">考试名称</div>
          <el-input v-model="params.examPaperName" placeholder="请输入考试名称" clearable />
        </div>
        <div class="filter-item">
          <div class="label">考试科目</div>
          <el-select v-model="params.examType" placeholder="请选择考试科目" clearable :disabled="isTeacherRole">
            <el-option label="全部" value="" />
            <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">阅卷状态</div>
          <el-select v-model="params.gradingStatus" placeholder="请选择阅卷状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待批阅" value="pending" />
            <el-option label="批阅中" value="reviewing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">提交时间</div>
          <el-date-picker
            v-model="submitDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择提交日期"
            clearable
            :popper-options="{ strategy: 'fixed' }"
          />
        </div>
        <div class="filter-item">
          <div class="label">考生姓名</div>
          <el-input v-model="params.nickname" placeholder="请输入考生姓名" clearable />
        </div>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-card">
      <div class="status-tabs">
        <div
          v-for="item in tabList"
          :key="item.key"
          class="tab-item"
          :class="{ active: activeTab === item.key }"
          @click="handleTabChange(item.key)"
        >
          {{ item.label }} ({{ item.count }})
        </div>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-button
            class="op-btn batch-delete-btn"
            :disabled="multipleSelection.length === 0"
            @click="handleBatchDelete"
          >批量删除</el-button>
          <el-tooltip content="仅导出当前筛选条件下状态为「已完成」的阅卷记录" placement="top">
            <el-button class="op-btn" @click="handleExport">导出列表</el-button>
          </el-tooltip>
        </div>
        <el-button circle @click="getDataList">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table :data="tableList" style="width: 100%" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" header-align="center" align="center" :selectable="rowSelectableForDelete" />
        <el-table-column label="考试名称" min-width="180" header-align="center" align="center">
          <template #default="{ row }">
            {{ getExamName(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="examPaperName" label="试卷名称" min-width="180" header-align="center" align="center" />
        <el-table-column label="考试科目" min-width="120" header-align="center" align="center">
          <template #default="{ row }">
            {{ getExamSubject(row) }}
          </template>
        </el-table-column>
        <el-table-column label="考生姓名" min-width="140" header-align="center" align="center">
          <template #default="{ row }">
            <div>{{ row.nickname || "-" }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="insertTime" label="提交时间" min-width="170" header-align="center" align="center" />
        <el-table-column label="教师评分" width="100" header-align="center" align="center">
          <template #default="{ row }">
            {{ row.teacherScore ?? row.totalScore ?? "-" }}
          </template>
        </el-table-column>
        <el-table-column label="阅卷状态" width="120" header-align="center" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(resolveStatus(row))" effect="light">
              {{ getStatusLabel(resolveStatus(row)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" header-align="center" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="resolveStatus(row) !== 'completed'"
              class="action-btn start-btn"
              @click="goToGrading(row)"
            >开始阅卷</el-button>
            <el-button class="action-btn detail-btn" @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="resolveStatus(row) === 'completed'"
              class="action-btn delete-btn"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="params.page"
        v-model:page-size="params.limit"
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
import { computed, ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import { getKemuOptions } from "@/utils/dictionary";
import * as XLSX from "xlsx";
import storage from "@/utils/storage";
import config from "@/config/config";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const route = useRoute();
const fullList = ref([]);
const examPaperList = ref([]);
const examInfoList = ref([]);
const usersList = ref([]);
const kemuOptions = ref([]);
const submitDate = ref(null);
const activeTab = ref("pending");
const multipleSelection = ref([]);

const pageParam = ref({
  page: 1,
  limit: 10,
});

const params = ref({
  ...pageParam.value,
  examType: "",
  gradingStatus: "",
});

const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY);
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY);
const isTeacherRole = ref(storedRole === "teachers" || storedRoleName === "教师");
const userStore = useUserStore();
const teacherKemuTypes = ref(null);
if (isTeacherRole.value) {
  const v = Number(userStore.userInfo?.kemuTypes);
  teacherKemuTypes.value = Number.isFinite(v) && v > 0 ? v : null;
  if (teacherKemuTypes.value) params.value.examType = teacherKemuTypes.value;
}

const examPaperMap = computed(() => {
  const map = new Map();
  examPaperList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) {
      map.set(item.id, item);
    }
  });
  return map;
});

const examInfoMap = computed(() => {
  const map = new Map();
  examInfoList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) {
      map.set(item.id, item);
    }
  });
  return map;
});

const usersMap = computed(() => {
  const map = new Map();
  usersList.value.forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) {
      map.set(item.id, item);
    }
  });
  return map;
});

const getPaperByRecord = (row) => {
  if (!row) return null;
  return examPaperMap.value.get(row.examPaperId) || null;
};

const getExamInfoByRecord = (row) => {
  if (!row) return null;
  return examInfoMap.value.get(row.examInfoId) || null;
};

/** 科目以考试安排为准，其次试卷/行上字段（与考试记录页一致） */
const getRecordKemuTypes = (row) => {
  if (!row) return null;
  const info = getExamInfoByRecord(row);
  const paper = getPaperByRecord(row);
  const candidates = [row.kemuTypes, info?.kemuTypes, paper?.kemuTypes];
  for (const c of candidates) {
    const n = Number(c);
    if (Number.isFinite(n) && n > 0) return n;
  }
  return null;
};

const getExamName = (row) => {
  if (row?.examName) return row.examName;
  const examInfo = getExamInfoByRecord(row);
  return examInfo?.examName || "-";
};

const getExamSubject = (row) => {
  const kemuType = getRecordKemuTypes(row);
  if (kemuType == null) return "-";
  const hit = kemuOptions.value.find((item) => Number(item.value) === Number(kemuType));
  const info = getExamInfoByRecord(row);
  return hit?.label || info?.kemuTypesName || "-";
};

const getUserAccount = (row) => {
  if (!row) return "-";
  if (row.username || row.account || row.userNumber || row.userName) {
    return row.username || row.account || row.userNumber || row.userName;
  }
  return usersMap.value.get(row.usersId)?.userName || (row.usersId ? `user${row.usersId}` : "-");
};

const toTime = (value) => {
  if (!value) return null;
  const t = new Date(value).getTime();
  return Number.isNaN(t) ? null : t;
};

const matchesSubmitDate = (row) => {
  if (!submitDate.value) return true;
  const rowTime = toTime(row.insertTime);
  if (!rowTime) return false;
  const start = toTime(`${submitDate.value} 00:00:00`);
  const end = toTime(`${submitDate.value} 23:59:59`);
  if (!start || !end) return true;
  return rowTime >= start && rowTime <= end;
};

const resolveStatus = (row) => {
  // 只要批过任意一道主观题（哪怕给 0 分），就认为“阅卷中”
  if (row.pendingReviewCount > 0 && (row.reviewedSubjectiveCount || 0) > 0) return "reviewing";
  if (row.pendingReviewCount > 0) return "pending";
  if ((row.pendingReviewCount === 0 && row.totalScore !== null && row.totalScore !== undefined) || row.reviewStatus === "completed" || row.reviewStatus === 2) return "completed";
  if (row.reviewStatus === "reviewing" || row.reviewStatus === 1) return "reviewing";
  return "pending";
};

const examNameKeyword = (kw) => String(kw || "").trim();

/** 仅考试安排名称（与后端 ei.exam_name 一致），不匹配试卷名 */
const rowMatchesExamNameKeyword = (row, kw) => {
  if (!kw) return true;
  const n = row?.examName
    ? String(row.examName)
    : String(getExamInfoByRecord(row)?.examName || "");
  return n.includes(kw);
};

const filteredList = computed(() => {
  const nameKw = examNameKeyword(params.value.examPaperName);
  return fullList.value.filter((item) => {
    if (!rowMatchesExamNameKeyword(item, nameKw)) return false;

    const recordKemu = getRecordKemuTypes(item);
    if (isTeacherRole.value && teacherKemuTypes.value) {
      if (Number(recordKemu) !== Number(teacherKemuTypes.value)) return false;
    }
    if (params.value.examType !== "" && params.value.examType != null) {
      if (Number(params.value.examType) !== Number(recordKemu)) return false;
    }

    const rowStatus = resolveStatus(item);
    if (params.value.gradingStatus && params.value.gradingStatus !== rowStatus) {
      return false;
    }
    if (!matchesSubmitDate(item)) {
      return false;
    }
    return true;
  });
});

const tabList = computed(() => {
  const list = filteredList.value;
  const countBy = (status) => list.filter((item) => resolveStatus(item) === status).length;
  return [
    { key: "pending", label: "待阅卷", count: countBy("pending") },
    { key: "reviewing", label: "阅卷中", count: countBy("reviewing") },
    { key: "completed", label: "已完成", count: countBy("completed") },
  ];
});

const tabFilteredList = computed(() => {
  return filteredList.value.filter((item) => resolveStatus(item) === activeTab.value);
});

const total = computed(() => tabFilteredList.value.length);

const tableList = computed(() => {
  const start = (params.value.page - 1) * params.value.limit;
  const end = start + params.value.limit;
  return tabFilteredList.value.slice(start, end);
});

const getStatusTagType = (status) => {
  if (status === "pending") return "warning";
  if (status === "reviewing") return "primary";
  return "success";
};

const getStatusLabel = (status) => {
  if (status === "pending") return "待阅卷";
  if (status === "reviewing") return "阅卷中";
  return "已完成";
};

const getDataList = () => {
  const payload = {
    page: 1,
    limit: 1000,
    onlyLatest: 1,
    examPaperName: params.value.examPaperName,
    nickname: params.value.nickname,
  };
  // 管理员按考试科目筛选：走库表 exam_info.kemu_types（教师端由后端强制 kemu，勿传以免歧义）
  if (!isTeacherRole.value) {
    const kt = params.value.examType;
    if (kt !== "" && kt != null && kt !== undefined) {
      const n = Number(kt);
      if (Number.isFinite(n) && n > 0) payload.kemuTypes = n;
    }
  }

  request
    .post("examRecord/page", payload)
    .then(({ data }) => {
      fullList.value = data?.list || [];
      updateTotal();
    })
    .catch((err) => {
      console.error("获取列表失败:", err);
      ElMessage.error("获取列表失败");
    });
};

const loadExamPaperList = async () => {
  try {
    const { data } = await request.post("examPaper/page", { page: 1, limit: 2000 });
    examPaperList.value = data?.list || [];
  } catch (error) {
    console.error("加载试卷列表失败:", error);
  }
};

const loadKemuOptions = async () => {
  const options = await getKemuOptions();
  kemuOptions.value = options || [];
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

const updateTotal = () => {
  const maxPage = Math.max(1, Math.ceil((total.value || 0) / params.value.limit));
  if (params.value.page > maxPage) {
    params.value.page = maxPage;
  }
};

const handleSearch = () => {
  params.value.page = 1;
  getDataList();
};

const handleReset = () => {
  params.value = {
    ...pageParam.value,
    examPaperName: "",
    nickname: "",
    examType: isTeacherRole.value && teacherKemuTypes.value ? teacherKemuTypes.value : "",
    gradingStatus: "",
  };
  submitDate.value = null;
  activeTab.value = "pending";
  handleSearch();
};

const handleTabChange = (key) => {
  activeTab.value = key;
  params.value.page = 1;
  updateTotal();
};

const handleSizeChange = () => {
  params.value.page = 1;
  updateTotal();
};

const handleCurrentChange = () => {
  updateTotal();
};

const rowSelectableForDelete = (row) => resolveStatus(row) === "completed";

const handleExport = () => {
  const exportRows = filteredList.value.filter((item) => resolveStatus(item) === "completed");
  if (!exportRows.length) {
    ElMessage.warning("当前没有「已完成」状态的阅卷记录可导出");
    return;
  }
  const headers = ["考试名称", "试卷名称", "考试科目", "考生姓名", "考生账号", "提交时间", "教师评分", "阅卷状态"];
  const rows = exportRows.map((row) => [
    getExamName(row),
    row.examPaperName || "-",
    getExamSubject(row),
    row.nickname || "-",
    getUserAccount(row),
    row.insertTime || "-",
    row.teacherScore ?? row.totalScore ?? "-",
    getStatusLabel(resolveStatus(row)),
  ]);
  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, sheet, "阅卷管理");
  const buffer = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
  const blob = new Blob([buffer], {
    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `阅卷管理_${new Date().toISOString().slice(0, 10)}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success("导出成功");
};

const handleSelectionChange = (selection) => {
  multipleSelection.value = selection || [];
};

const handleDelete = async (row) => {
  if (!row?.id) return;
  try {
    await ElMessageBox.confirm("确定删除该阅卷记录吗？", "提示", { type: "warning" });
    await request.delete(`examRecord/del/${row.id}`);
    ElMessage.success("删除成功");
    await getDataList();
  } catch (error) {
    if (error === "cancel") return;
    console.error("删除失败:", error);
    ElMessage.error("删除失败");
  }
};

const handleBatchDelete = async () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning("请先选择要删除的阅卷记录");
    return;
  }
  const allSelected = [...multipleSelection.value];
  const nonCompletedRows = allSelected.filter((item) => resolveStatus(item) !== "completed");
  if (nonCompletedRows.length) {
    ElMessage.warning("仅“已完成”状态记录支持删除，请调整勾选后重试");
    return;
  }
  const ids = allSelected.map((item) => Number(item.id)).filter(Boolean);
  if (!ids.length) {
    ElMessage.warning("未获取到可删除记录");
    return;
  }
  try {
    await ElMessageBox.confirm(`确定批量删除选中的 ${ids.length} 条阅卷记录吗？`, "提示", { type: "warning" });
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

const handleDetail = (row) => {
  router.push({
    path: "/examGradingView",
    query: {
      examRecordId: row.id,
      examDetailsUuidNumber: row.examRecordUuidNumber,
      examName: getExamName(row),
      examPaperName: row.examPaperName,
      reviewStatus: getStatusLabel(resolveStatus(row)),
      nickname: row.nickname,
      userName: getUserAccount(row),
      totalScore: row.totalScore,
      insertTime: row.insertTime,
    },
  });
};

const goToGrading = (row) => {
  router.push({
    path: "/examGradingDetails",
    query: {
      examRecordId: row.id,
      examDetailsUuidNumber: row.examRecordUuidNumber,
      examName: getExamName(row),
      examPaperName: row.examPaperName,
      reviewTab: resolveStatus(row),
      nickname: row.nickname,
      totalScore: row.totalScore,
      insertTime: row.insertTime,
    },
  });
};

onMounted(() => {
  if (route.query.tab && ["pending", "reviewing", "completed"].includes(route.query.tab)) {
    activeTab.value = route.query.tab;
  }
  Promise.all([loadKemuOptions(), loadExamPaperList(), loadExamInfoList(), loadUsersList()]).finally(() => {
    getDataList();
  });
});

// 监听路由变化，当从其他页面返回时重新加载数据
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 当路由切换到阅卷管理页面时，重新加载数据
    if (newPath === '/examGrading' && oldPath && oldPath !== newPath) {
      getDataList();
    }
  }
);
</script>

<style lang="scss" scoped>
.grading-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
}

/* ==================== 筛选区排版与错题本一致 ==================== */
.filter-card {
  margin-bottom: 20px;
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

@media (max-width: 1400px) {
  .filter-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1100px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }

}

/* ==================== 按钮统一蓝色 ==================== */
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

:deep(.el-button.is-link) {
  background: transparent;
  border-color: transparent;
  color: #1890ff;
}

/* 筛选区按钮：与错题本一致（覆盖上方全局 primary/default） */
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

.filter-actions :deep(.el-button--primary:hover) {
  background: #40a9ff;
  border-color: #40a9ff;
  color: #fff;
}

.filter-actions :deep(.el-button--default) {
  background: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.filter-actions :deep(.el-button--default:hover) {
  background: #f2f6fc;
  border-color: #c0c4cc;
  color: #606266;
}

/* ==================== 状态栏/工具栏 ==================== */
.table-card {
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  padding: var(--card-padding);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.status-tabs {
  display: flex;
  gap: 26px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 14px;
}

.tab-item {
  position: relative;
  padding: 12px 0;
  color: #606266;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.tab-item.active {
  color: #1890ff;
  font-weight: 600;
}

.tab-item.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: #1890ff;
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

.op-btn {
  min-width: 124px;
  height: 36px;
  border-radius: 8px;
  font-weight: 600;
  padding: 0 14px;
}

.batch-delete-btn {
  color: #fff;
  background: #1890ff;
  border: 1px solid #1890ff;
}

.batch-delete-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  color: #fff;
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
  background: #1890ff;
  border: 1px solid #1890ff;
}

.detail-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
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

.start-btn {
  color: #fff;
  background: #67c23a;
  border: 1px solid #67c23a;
}

.start-btn:hover {
  background: #85ce61;
  border-color: #85ce61;
  color: #fff;
}

/* ==================== 表格统一样式 ==================== */
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

@media (max-width: 900px) {
  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
