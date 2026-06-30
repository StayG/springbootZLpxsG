<template>
  <div class="module-container">
    <div v-if="showIndexFlag">
      <div class="page-title">试卷管理</div>
      <!-- 查询条件 -->
      <div class="filter-container">
        <div class="filter-item-group">
          <!--试卷名称-->
          <el-input v-model="params.examPaperName" placeholder="请输入试卷名称" class="filter-item" clearable />

          <!-- 科目筛选：教师角色隐藏，因为数据已自动隔离 -->
          <el-select v-if="!isTeacherRole" v-model="params.kemuTypes" placeholder="请选择科目" class="filter-item" clearable>
            <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          
          <!-- 组卷方式 -->
          <el-select v-model="params.zujuanTypes" placeholder="请选择组卷方式" class="filter-item" clearable>
            <el-option v-for="item in zujuanOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>

          <!-- 创建时间 -->
          <el-date-picker
            v-model="params.createTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择创建时间"
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
        <el-button v-if="utils.isAuth('examPaper', '新增')" class="operation-btn" type="primary" @click="handleAdd">
          新增</el-button>
        <el-button v-if="utils.isAuth('examPaper', '删除')" class="operation-btn" type="primary"
          :disabled="multipleSelection.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-scroll-x">
        <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe>
          <el-table-column type="selection" width="55" header-align="center" align="center" />
          <el-table-column type="index" label="序号" width="65" header-align="center" align="center" />
          <el-table-column prop="examPaperName" label="试卷名称" min-width="100" header-align="center" align="center" />
          <el-table-column prop="examPaperMyscore" label="考试总分数" min-width="100" header-align="center" align="center" />
          <el-table-column prop="kemuTypes" label="科目" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ formatKemu(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="zujuanTypes" label="组卷方式" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ formatZujuanTypes(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="examPaperTypes" label="试卷状态" min-width="150" header-align="center" align="center">
            <template #default="{ row }">
              <el-switch :model-value="row.examPaperTypes" :active-value="1" :inactive-value="2" active-text="启用"
                inactive-text="禁用" @click.stop="changeStatus(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" header-align="center" align="center" />
          <el-table-column label="操作" width="300" header-align="center" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
                <el-button :disabled="row.examPaperTypes == 1" v-if="utils.isAuth('examPaper', '删除')" type="danger"
                  size="small" @click="handleDelete(row)">删除</el-button>
                <el-button :disabled="row.examPaperTypes == 1" v-if="utils.isAuth('examPaper', '编辑')" type="success"
                  size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button :disabled="row.examPaperTypes == 1" type="primary" size="small"
                  @click="setKaoti(row)">设置考题</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <el-pagination v-model:current-page="params.page" v-model:page-size="params.limit" :total="total"
        :page-sizes="[5, 10, 15, 20]" layout="total, sizes, prev, pager, next, jumper" @size-change="getDataList"
        @current-change="getDataList" />
    </div>

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
import { useRouter } from "vue-router";
import { getKemuOptions, getZujuanOptions, getExamPaperStatusOptions } from "@/utils/dictionary.js";
import storage from '@/utils/storage';
import config from '@/config/config';

const router = useRouter();

// 判断是否为教师角色（从 storage 中读取角色信息）
const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY); // 如 'teachers'
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY); // 如 '教师'
const isTeacherRole = ref(
  storedRole === 'teachers' || storedRoleName === '教师'
);

console.log('[试卷列表] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

// 数据列表
const dataList = ref([]);
const total = ref(0);
// 多选数据
const multipleSelection = ref([]);
//默认展示表格页面
const showIndexFlag = ref(true);
// 分页属性
const pageParam = ref({
  page: 1,
  limit: 5,
});
// 查询参数
const params = ref({
  ...pageParam.value,
});

// 获取字典选项（从字典接口获取）
const getDictOptions = async () => {
  try {
    const [kemuList, zujuanList, statusList] = await Promise.all([getKemuOptions(), getZujuanOptions(), getExamPaperStatusOptions()]);

    kemuOptions.value = kemuList || [];
    zujuanOptions.value = zujuanList || [];
    examPaperStatusOptions.value = statusList || [];

    // 如果选项为空，使用默认选项（静默处理，不显示警告）
    if (kemuOptions.value.length === 0) {
      kemuOptions.value = [
        { label: "语文", value: 1 },
        { label: "数学", value: 2 },
      ];
    }
    if (zujuanOptions.value.length === 0) {
      zujuanOptions.value = [
        { label: "自动组卷", value: 1 },
        { label: "手动组卷", value: 2 },
      ];
    }
    if (examPaperStatusOptions.value.length === 0) {
      examPaperStatusOptions.value = [
        { label: "启用", value: 1 },
        { label: "禁用", value: 2 },
      ];
    }
  } catch (error) {
    console.error("获取字典选项失败:", error);
    ElMessage.warning("获取字典选项失败，请检查网络连接或联系管理员");
    // 设置默认空数组，避免报错
    kemuOptions.value = [];
    zujuanOptions.value = [];
    examPaperStatusOptions.value = [];
  }
};

// 获取数据列表
const getDataList = () => {
  const payload = {
    ...params.value,
    page: 1,
    limit: 2000,
  };
  request.post("examPaper/page", payload).then(({ data }) => {
    const sourceList = data?.list || [];
    const filteredList = sourceList.filter((item) => {
      if (params.value.createTime) {
        const rowDate = String(item.createTime || "").slice(0, 10);
        if (rowDate !== params.value.createTime) return false;
      }
      return true;
    });
    total.value = filteredList.length;
    const start = (params.value.page - 1) * params.value.limit;
    const end = start + params.value.limit;
    dataList.value = filteredList.slice(start, end);
  });
};

// 科目选项 - 从后端数据中动态获取
const kemuOptions = ref([]);

// 组卷方式选项 - 从后端数据中动态获取
const zujuanOptions = ref([]);

// 试卷状态选项 - 从后端数据中动态获取
const examPaperStatusOptions = ref([]);

//科目 - 使用后端字典转换后的值
const formatKemu = (row) => {
  return row.kemuTypesName || row.kemuTypes || "";
};

//组卷方式 - 使用后端字典转换后的值
const formatZujuanTypes = (row) => {
  return row.zujuanTypesName || row.zujuanTypes || "";
};

//启用或禁用
const changeStatus = (row) => {
  // 计算新状态（切换状态）
  const newStatus = row.examPaperTypes === 1 ? 2 : 1;

  console.log("changeStatus 被调用", {
    rowId: row.id,
    rowName: row.examPaperName,
    oldStatus: row.examPaperTypes,
    newStatus: newStatus,
  });

  // 从后端数据中获取状态文本
  const statusOption = examPaperStatusOptions.value.find((opt) => opt.value === newStatus);
  const statusText = statusOption ? statusOption.label : newStatus === 1 ? "启用" : "禁用";

  ElMessageBox.confirm(`确定要将【${row.examPaperName}】状态修改为【${statusText}】吗？`, "提示", { type: "warning" })
    .then(() => {
      // 构造要提交的数据：保留原来的字段，只把状态改掉
      const payload = {
        ...row,
        examPaperTypes: newStatus,
      };

      // 调用你现有的 /update 接口
      return request.post("examPaper/update", payload);
    })
    .then(() => {
      ElMessage.success("状态修改成功");
      getDataList(); // 刷新列表
    })
    .catch(() => {
      // 用户取消时不做任何操作
      console.log("用户取消了状态修改");
    });
};

// 查询
const handleSearch = () => {
  params.value.page = 1;
  getDataList();
};

// 新增重置方法
const handleReset = () => {
  params.value = {
    ...pageParam.value,
  };
  handleSearch();
};

// 多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

// 批量删除
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请选择要删除的信息");
    return;
  }
  ElMessageBox.confirm("确定要删除选中的信息吗?", "提示", {
    type: "warning",
  })
    .then(() => {
      const ids = multipleSelection.value.map((item) => Number(item.id));
      console.log("🚀 ~ .then ~ ids:", ids);
      request.post("examPaper/del/batch", ids).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => { });
};

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该信息吗?`, "提示", {
    type: "warning",
  })
    .then(() => {
      request.delete(`examPaper/del/${row.id}`).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => { });
};

const addOrUpdateFlag = ref(false);
const addOrUpdateRef = ref();
const isReadonly = ref(false);

// 新增
const handleAdd = () => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init();
  });
};

// 编辑
const handleEdit = (row) => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init(row.id);
  });
};

// 查看（只读模式）
const handleView = (row) => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = true;
  nextTick(() => {
    addOrUpdateRef.value?.init(row.id);
  });
};

const setKaoti = (row) => {
  router.push({
    path: "/examPaperTopic", // 路由名称
    query: {
      id: row.id,
      kemuTypes: row.kemuTypes,
      zujuanTypes: row.zujuanTypes,
    },
  });
};

// 成功回调
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  showIndexFlag.value = true;
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

  /* 创建时间与左侧下拉同一水平线对齐 */
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
  margin-bottom: 20px;
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  padding: var(--card-padding);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
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
