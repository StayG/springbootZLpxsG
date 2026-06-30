<template>
  <div class="module-container">
    <div class="page-title">考试管理</div>
    <!-- 查询条件 -->
    <div class="filter-container">
      <div class="filter-item-group">
        <!-- 考试名称 -->
        <el-input v-model="params.examName" placeholder="请输入考试名称" class="filter-item" clearable
          @keyup.enter="handleSearch" />

        <!-- 科目筛选：教师角色隐藏，因为数据已自动隔离 -->
        <el-select v-if="!isTeacherRole" v-model="params.kemuTypes" placeholder="请选择科目" class="filter-item" clearable>
          <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <!-- 状态 -->
        <el-select v-model="params.status" placeholder="请选择状态" class="filter-item" clearable>
          <el-option label="未发布" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="进行中" :value="2" />
          <el-option label="已结束" :value="3" />
        </el-select>
        <el-date-picker
          v-model="params.examDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择考试时间"
          class="filter-item"
          clearable
        />
      </div>
      <div class="filter-btn-group">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-container">
      <el-button v-if="utils.isAuth('examInfo', '新增')" class="operation-btn" type="primary"
        @click="handleAdd">新增考试</el-button>
      <el-button v-if="utils.isAuth('examInfo', '删除')" class="operation-btn" type="primary"
        :disabled="multipleSelection.length === 0" @click="handleBatchDelete">批量删除
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="table-scroll-x">
      <div class="toolbar">
        <div class="toolbar-left"></div>
        <el-button class="refresh-btn" @click="getDataList">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe>
        <el-table-column type="selection" width="55" header-align="center" align="center" :selectable="checkSelectable" />
        <el-table-column type="index" label="序号" width="65" header-align="center" align="center" />
        <el-table-column prop="examName" label="考试名称" min-width="180" header-align="center" align="center"
          show-overflow-tooltip />
        <el-table-column prop="kemuValue" label="科目" width="100" header-align="center" align="center" />
        <el-table-column prop="examPaperName" label="关联试卷" min-width="150" header-align="center" align="center"
          show-overflow-tooltip />
        <el-table-column label="考试时长" width="120" header-align="center" align="center">
          <template #default="{ row }">
            {{ formatDuration(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="passingScore" label="及格分" width="100" header-align="center" align="center">
          <template #default="{ row }">
            <span v-if="row.passingScore !== null && row.passingScore !== undefined">{{ row.passingScore }} 分</span>
            <span v-else style="color: #909399;">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="160" header-align="center" align="center">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" min-width="160" header-align="center" align="center">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" header-align="center" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">未发布</el-tag>
            <el-tag v-else-if="row.status === 1" type="primary">已发布</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">进行中</el-tag>
            <el-tag v-else-if="row.status === 3" type="warning">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" header-align="center" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <!-- 未发布状态显示发布按钮 -->
              <el-button v-if="utils.isAuth('examInfo', '发布') && row.status === 0" type="success" size="small"
                @click="publishHandle(row)">发布</el-button>

              <!-- 已发布状态显示取消发布按钮 -->
              <el-button v-if="utils.isAuth('examInfo', '取消发布') && row.status === 1" type="warning" size="small"
                @click="unpublishHandle(row)">取消发布</el-button>

              <el-button v-if="utils.isAuth('examInfo', '编辑') && (row.status === 0 || row.status === 1)" type="primary"
                size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="utils.isAuth('examInfo', '删除') && row.status !== 2" type="danger" size="small"
                @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <el-pagination v-model:current-page="params.page" v-model:page-size="params.limit" :total="total"
      :page-sizes="[5, 10, 15, 20]" layout="total, sizes, prev, pager, next, jumper" @size-change="getDataList"
      @current-change="getDataList" />

    <!-- 新增或编辑或查看 -->
    <add-or-update v-if="addOrUpdateFlag" ref="addOrUpdateRef" :readonly="isReadonly"
      @handleSuccess="handleSuccess"></add-or-update>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import AddOrUpdate from "./add-or-update.vue";
import utils from "@/utils/utils.js";
import { getKemuOptions } from "@/utils/dictionary.js";
import storage from '@/utils/storage';
import config from '@/config/config';
import { Refresh } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";

// 判断是否为教师角色（从 storage 中读取角色信息）
const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY); // 如 'teachers'
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY); // 如 '教师'
const isTeacherRole = ref(
  storedRole === 'teachers' || storedRoleName === '教师'
);

console.log('[考试列表] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

const userStore = useUserStore();
const teacherKemuTypes = ref(null);
if (isTeacherRole.value) {
  const v = Number(userStore.userInfo?.kemuTypes);
  teacherKemuTypes.value = Number.isFinite(v) && v > 0 ? v : null;
}

// 数据列表
const dataList = ref([]);
const total = ref(0);
// 多选数据
const multipleSelection = ref([]);
// 分页属性
const pageParam = ref({
  page: 1,
  limit: 5,
});
// 查询参数
const params = ref({
  ...pageParam.value,
});

// 获取字典选项
const getDictOptions = async () => {
  try {
    const kemuList = await getKemuOptions();
    kemuOptions.value = kemuList || [];

    // 如果选项为空，使用默认选项（静默处理）
    if (kemuOptions.value.length === 0) {
      kemuOptions.value = [
        { label: "语文", value: 1 },
        { label: "数学", value: 2 },
      ];
    }
  } catch (error) {
    console.error("获取字典选项失败:", error);
    kemuOptions.value = [];
  }
};

// 科目选项 - 从后端数据中动态获取
const kemuOptions = ref([]);

// 时间格式化函数
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 考试时长格式化函数
const formatDuration = (row) => {
  // 优先显示 duration 字段
  if (row.duration) {
    return `${row.duration}分钟`;
  }
  
  // 如果 duration 为空，根据 startTime 和 endTime 计算
  if (row.startTime && row.endTime) {
    const start = new Date(row.startTime).getTime();
    const end = new Date(row.endTime).getTime();
    const diffMinutes = Math.round((end - start) / 60000);
    return diffMinutes > 0 ? `${diffMinutes}分钟` : '-';
  }
  
  return '-';
};

// 获取数据列表
const getDataList = () => {
  const payload = {
    ...params.value,
    page: 1,
    limit: 2000,
  };
  // 教师角色：后端已通过 SQL 实现数据隔离（自己创建的 OR 同科目的），前端无需额外过滤
  request.post("examInfo/page", payload).then(({ data }) => {
    const sourceList = data?.list || [];
    const filteredList = sourceList.filter((item) => {
      // 仅保留日期过滤逻辑
      if (params.value.examDate) {
        const rowDate = String(item.startTime || "").slice(0, 10);
        if (rowDate !== params.value.examDate) return false;
      }
      return true;
    });
    total.value = filteredList.length;
    const start = (params.value.page - 1) * params.value.limit;
    const end = start + params.value.limit;
    dataList.value = filteredList.slice(start, end);
  });
};

// 查询
const handleSearch = () => {
  params.value.page = 1;
  getDataList();
};

// 重置
const handleReset = () => {
  params.value = {
    ...pageParam.value,
  };
  handleSearch();
};

const addOrUpdateFlag = ref(false);
const addOrUpdateRef = ref();
const isReadonly = ref(false);

// 新增
const handleAdd = () => {
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init();
  });
};

// 编辑
const handleEdit = (row) => {
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init(row.id);
  });
};

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该信息吗?`, "提示", {
    type: "warning",
  })
    .then(() => {
      request.delete(`examInfo/del/${row.id}`).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => { });
};

// 批量删除
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请选择要删除的信息");
    return;
  }

  // 检查选中的记录中是否包含进行中的考试
  const hasActiveExam = multipleSelection.value.some(item => item.status === 2);
  if (hasActiveExam) {
    ElMessage.warning("选中的记录中包含进行中的考试，无法删除");
    return;
  }

  ElMessageBox.confirm("确定要删除选中的信息吗?", "提示", {
    type: "warning",
  })
    .then(() => {
      const ids = multipleSelection.value.map((item) => Number(item.id));
      console.log("🚀 ~ .then ~ ids:", ids);
      request.post("examInfo/del/batch", ids).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => { });
};

// 表格选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 检查行是否可选（进行中的考试不可选）
const checkSelectable = (row) => {
  return row.status !== 2;
};

// 发布考试
const publishHandle = (row) => {
  ElMessageBox.confirm("发布后学生端将可见此考试，确定要发布吗？", "提示", {
    type: "warning",
  })
    .then(() => {
      request.post(`examInfo/publish/${row.id}`).then(() => {
        ElMessage.success("发布成功");
        getDataList();
      });
    })
    .catch(() => { });
};

// 取消发布考试
const unpublishHandle = (row) => {
  ElMessageBox.confirm("取消发布后学生端将不可见此考试，确定要取消吗？", "提示", {
    type: "warning",
  })
    .then(() => {
      request.post(`examInfo/unpublish/${row.id}`).then(() => {
        ElMessage.success("取消发布成功");
        getDataList();
      });
    })
    .catch(() => { });
};

// 成功回调
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  isReadonly.value = false;
  getDataList();
};

// 初始化加载数据
onMounted(() => {
  getDictOptions(); // 先获取字典选项
  getDataList();
});
</script>

<style lang="scss" scoped>
.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 18px;
}

/* ==================== 查询条件区域 ==================== */
.filter-container {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);

  .filter-item-group {
    flex: 1;
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    align-items: center;
  }

  .filter-btn-group {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-shrink: 0;
  }

  /* 考试时间与左侧下拉同一水平线对齐 */
  .filter-item-group :deep(.el-date-editor.el-input) {
    margin: 0;
    line-height: 1;
  }

  .filter-item-group :deep(.el-date-editor .el-input__wrapper) {
    display: flex;
    align-items: center;
    margin-top:-3px;
  }

  .filter-item-group :deep(.el-date-editor .el-input__prefix) {
    display: flex;
    align-items: center;
  }
}

/* ==================== 操作按钮区域 ==================== */
.operation-container {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

/* ==================== 表格容器 ==================== */
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
  overflow-x: auto;
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

.refresh-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.refresh-btn:hover {
  color: #1890ff;
  border-color: #1890ff;
  background: #f0f9ff;
}

/* ==================== Element Plus 输入框样式覆盖 ==================== */
:deep(.el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.is-focus {
    border-color: #1890ff;
    box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
  }
}

:deep(.el-input__inner) {
  color: #303133;
  font-size: 14px;

  &::placeholder {
    color: #a8abb2;
  }
}

/* ==================== Element Plus 选择器样式覆盖 ==================== */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.is-focused {
    border-color: #1890ff;
    box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
  }
}

/* ==================== Element Plus 按钮样式覆盖 ==================== */
:deep(.el-button) {
  font-weight: 500;

  // 主要按钮
  &.el-button--primary {
    background: var(--btn-primary-bg, #1890ff);
    border-width: var(--btn-primary-border-width, 1px);
    border-style: var(--btn-primary-border-style, solid);
    border-color: var(--btn-primary-border-color, #1890ff);
    border-radius: var(--btn-primary-border-radius, 6px);
    color: var(--btn-primary-color, #fff);

    &:hover {
      background: var(--btn-primary-hover-bg, #40a9ff);
      border-color: var(--btn-primary-hover-border-color, #40a9ff);
      color: var(--btn-primary-hover-color, #fff);
    }

    &.is-disabled {
      background: #a0cfff;
    }
  }

  // 默认按钮
  &.el-button--default {
    background: var(--btn-default-bg, #fff);
    border-width: var(--btn-default-border-width, 1px);
    border-style: var(--btn-default-border-style, solid);
    border-color: var(--btn-default-border-color, #dcdfe6);
    border-radius: var(--btn-default-border-radius, 6px);
    color: var(--btn-default-color, #606266);

    &:hover {
      background: var(--btn-default-hover-bg, #f0f9ff);
      border-color: var(--btn-default-hover-border-color, #1890ff);
      color: var(--btn-default-hover-color, #1890ff);
    }
  }

  // 危险按钮
  &.el-button--danger {
    background: var(--btn-danger-bg, #f56c6c);
    border-width: var(--btn-danger-border-width, 1px);
    border-style: var(--btn-danger-border-style, solid);
    border-color: var(--btn-danger-border-color, #f56c6c);
    border-radius: var(--btn-danger-border-radius, 6px);
    color: var(--btn-danger-color, #fff);

    &:hover {
      background: var(--btn-danger-hover-bg, #f78989);
      border-color: var(--btn-danger-hover-border-color, #f78989);
      color: var(--btn-danger-hover-color, #fff);
    }
  }

  // 成功按钮
  &.el-button--success {
    background: var(--btn-success-bg, #67c23a);
    border-width: var(--btn-success-border-width, 1px);
    border-style: var(--btn-success-border-style, solid);
    border-color: var(--btn-success-border-color, #67c23a);
    border-radius: var(--btn-success-border-radius, 6px);
    color: var(--btn-success-color, #fff);

    &:hover {
      background: var(--btn-success-hover-bg, #85ce61);
      border-color: var(--btn-success-hover-border-color, #85ce61);
      color: var(--btn-success-hover-color, #fff);
    }
  }

  // 警告按钮
  &.el-button--warning {
    background: var(--btn-warning-bg, #e6a23c);
    border-width: var(--btn-warning-border-width, 1px);
    border-style: var(--btn-warning-border-style, solid);
    border-color: var(--btn-warning-border-color, #e6a23c);
    border-radius: var(--btn-warning-border-radius, 6px);
    color: var(--btn-warning-color, #fff);

    &:hover {
      background: var(--btn-warning-hover-bg, #ebb563);
      border-color: var(--btn-warning-hover-border-color, #ebb563);
      color: var(--btn-warning-hover-color, #fff);
    }
  }

  // 信息按钮
  &.el-button--info {
    background: var(--btn-info-bg, #909399);
    border-width: var(--btn-info-border-width, 1px);
    border-style: var(--btn-info-border-style, solid);
    border-color: var(--btn-info-border-color, #909399);
    border-radius: var(--btn-info-border-radius, 6px);
    color: var(--btn-info-color, #fff);

    &:hover {
      background: var(--btn-info-hover-bg, #a6a9ad);
      border-color: var(--btn-info-hover-border-color, #a6a9ad);
      color: var(--btn-info-hover-color, #fff);
    }
  }
}

/* ==================== Element Plus 表格样式覆盖 ==================== */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;

  // 表头样式
  .el-table__header-wrapper {
    .el-table__header {
      th {
        background: var(--table-header-bg, #f5f7fa);
        color: var(--table-header-text-color, #303133);
        font-weight: 600;
        font-size: 14px;
        border-bottom: 2px solid var(--table-header-border-color, #e4e7ed);

        .cell {
          padding: 12px 0;
        }
      }
    }
  }

  // 表体样式
  .el-table__body-wrapper {
    .el-table__body {
      tr {
        transition: all 0.3s;

        &:hover {
          background: var(--table-row-hover-bg, #f0f9ff);

          td {
            background: transparent;
          }
        }

        td {
          color: #303133;
          font-size: 14px;
          border-bottom: 1px solid #ebeef5;

          .cell {
            padding: 12px 0;
          }
        }
      }

      // 斑马纹
      .el-table__row--striped {
        td {
          background: var(--table-row-stripe-bg, #fafafa);
        }
      }
    }
  }

  // 边框
  &.el-table--border {
    border: 1px solid #ebeef5;

    td,
    th {
      border-right: 1px solid #ebeef5;
    }
  }
}

/* ==================== 表格内操作按钮 ==================== */
.table-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    margin: 0;
    padding: 6px 12px;
    font-size: 13px;
  }
}

/* ==================== Element Plus 分页样式覆盖 ==================== */
:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20px 0;
  gap: 8px;

  // 总数
  .el-pagination__total {
    color: #606266;
    font-weight: 500;
  }

  // 每页条数选择器
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

  // 上一页/下一页按钮
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

  // 页码按钮
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

  // 跳转
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

/* ==================== 通用状态标签 ==================== */
.status-tag {
  font-weight: 500;
  border: none;
}

/* ==================== 暂无数据样式 ==================== */
.no-data {
  color: #909399;
  font-size: 13px;
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    padding: 16px;

    .filter-item-group {
      width: 100%;

      .filter-item {
        width: 100%;
      }
    }

    .filter-btn-group {
      width: 100%;
      justify-content: flex-start;
    }
  }

  .table-scroll-x {
    padding: 12px;
    overflow-x: auto;
  }

  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
