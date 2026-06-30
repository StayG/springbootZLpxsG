<template>
  <div class="module-container">
    <div v-if="showIndexFlag">
      <div class="page-title">用户管理</div>
      <!-- 查询条件 -->
      <div class="filter-container">
        <div class="filter-item-group">
          <el-input v-model="params.userName" placeholder="请输入用户名" class="filter-item" clearable />
          <el-select v-model="params.gender" placeholder="请选择性别" class="filter-item">
            <el-option label="全部" value="" />
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="operation-container">
        <el-button class="operation-btn" type="primary" @click="handleAdd"> 新增 </el-button>
        <el-button class="operation-btn" type="primary" :disabled="multipleSelection.length === 0" @click="handleBatchDelete"> 批量删除 </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-scroll-x">
        <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe>
          <el-table-column type="selection" width="55" header-align="center" align="center" fixed="left" />
          <el-table-column type="index" label="序号" width="65" header-align="center" align="center" fixed="left" />
          <el-table-column prop="userName" label="用户名" min-width="140" header-align="center" align="center" />
          <el-table-column prop="nickname" label="姓名" min-width="140" header-align="center" align="center" />
          <el-table-column prop="gender" label="性别" width="100" header-align="center" align="center" />
          <el-table-column prop="avatar" label="头像" min-width="180" header-align="center" align="center">
            <template #default="{ row }">
              <image-view
                  v-if="row.avatar"
                  style="width: 100px; height: 100px"
                  :src="row.avatar?.split(',')[0] || '/images/default-product.jpg'"
                  :alt="row.avatar"
                  fit="cover"
              />
              <span v-else>无图片</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="200" header-align="center" align="center" />
          <el-table-column prop="phone" label="电话" min-width="140" header-align="center" align="center" />
          <el-table-column prop="status" label="状态" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="100" header-align="center" align="center" />
          <el-table-column prop="updateTime" label="修改时间" min-width="100" header-align="center" align="center" />
          <el-table-column label="操作" width="250" header-align="center" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                <el-button type="success" size="small" @click="handleEdit(row)">编辑</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="params.page"
        v-model:page-size="params.limit"
        :total="total"
        :page-sizes="[5, 10, 15, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getDataList"
        @current-change="getDataList"
      />
    </div>

    <!-- 新增或编辑或查看 -->
    <add-or-update v-if="addOrUpdateFlag" ref="addOrUpdateRef" :readonly="isReadonly" @handleSuccess="handleSuccess"></add-or-update>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import AddOrUpdate from "./add-or-update.vue";

// 数据列表
const dataList = ref([]);
const total = ref(0);
// 多选数据
const multipleSelection = ref([]);
// 默认展示表格页面
const showIndexFlag = ref(true);
// 查询参数
const params = ref({
  page: 1,
  limit: 5,
  userName: "", // 查询条件改为用户名
  gender: "", // 新增性别查询参数
  status: null,
});

// 获取数据列表
const getDataList = () => {
  request.post("users/page", params.value).then(({ data }) => {
    dataList.value = data.list;
    total.value = data.total;
  });
};

// 查询
const handleSearch = () => {
  params.value.page = 1;
  getDataList();
};

// 多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

// 批量删除
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请选择要删除的用户");
    return;
  }
  ElMessageBox.confirm("确定要删除选中的用户吗?", "提示", {
    type: "warning",
  })
    .then(() => {
      const ids = multipleSelection.value.map((item) => Number(item.id));
      request.post("users/del/batch", ids).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => {});
};

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 ${row.userName} 吗?`, "提示", {
    type: "warning",
  })
    .then(() => {
      request.delete(`users/del/${row.id}`).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => {});
};

// 状态切换
const handleStatusChange = (row) => {
  const action = row.status === 1 ? "启用" : "禁用";
  const msg = row.status === 1 ? `确定要启用用户 ${row.userName} 吗?` : `确定要禁用用户 ${row.userName} 吗?`;
  
  ElMessageBox.confirm(msg, "提示", { type: "warning" })
    .then(() => {
      request.post("users/updateStatus", { id: row.id, status: row.status }).then(() => {
        ElMessage.success(`${action}成功`);
        getDataList();
      }).catch(() => {
        // 失败时恢复原状态
        row.status = row.status === 1 ? 0 : 1;
      });
    })
    .catch(() => {
      // 取消操作时恢复原状态
      row.status = row.status === 1 ? 0 : 1;
    });
};

const addOrUpdateFlag = ref(false);
const addOrUpdateRef = ref();
const isReadonly = ref(false);

// 新增
const handleAdd = () => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
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

// 新增或编辑成功
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  showIndexFlag.value = true;
  isReadonly.value = false;
  getDataList();
};

// 新增重置方法
const handleReset = () => {
  params.value.userName = '';
  params.value.gender = '';
  params.value.status = null;
  handleSearch();
};

// 初始化加载数据
onMounted(() => {
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
  overflow-x: auto;
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

