<template>
  <div class="module-container">
    <div v-if="showIndexFlag">
      <div class="page-title">科目管理</div>
      <!-- 查询条件 -->
      <div class="filter-container">
        <div class="filter-item-group">
          <el-input v-model="params.indexName" placeholder="请输入科目名称" class="filter-item" clearable @keyup.enter="handleSearch"/>

        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="operation-container">
        <el-button v-if="utils.isAuth('subject','新增')" class="operation-btn" type="primary" @click="handleAdd"> 新增</el-button>
        <el-button v-if="utils.isAuth('subject','删除')" class="operation-btn" type="primary" :disabled="multipleSelection.length === 0"
                   @click="handleBatchDelete"> 批量删除
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-scroll-x">
        <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe>
          <el-table-column type="selection" width="55" header-align="center" align="center" fixed="left" />
          <el-table-column type="index" label="序号" width="65" header-align="center" align="center" fixed="left" />
          <el-table-column prop="indexName" label="科目名称" min-width="140" header-align="center" align="center" />
          <el-table-column prop="codeIndex" label="科目代码" min-width="120" header-align="center" align="center" />
          <el-table-column prop="createTime" label="创建时间" min-width="100" header-align="center" align="center"/>
          <el-table-column label="操作" width="250" header-align="center" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
                <el-button v-if="utils.isAuth('subject','删除')" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                <el-button v-if="utils.isAuth('subject','编辑')" type="success" size="small" @click="handleEdit(row)">编辑</el-button>
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
import {ref, onMounted, nextTick} from "vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import AddOrUpdate from "./add-or-update.vue";
import utils from "@/utils/utils.js";

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
})
// 查询参数
const params = ref({
  page: 1,
  limit: 10,
  dicCode: '',
  indexName: ''
});

// 加载科目接口
const getDataList = () => {
  // 构建查询参数
  const queryParams = {
    page: params.value.page,
    limit: params.value.limit,
    dicCode: 'kemu_types'
  };
  
  // 只添加非空的查询条件
  if (params.value.indexName && params.value.indexName.trim()) {
    queryParams.indexName = params.value.indexName.trim();
  }
  
  request.post('/dictionary/page', queryParams).then(res => {
    if (res.code === 200) {
       dataList.value = res.data.list;
       total.value = res.data.total;
    } else {
       ElMessage.error(res.msg || '获取失败');
    }
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
    dicCode: '',
    indexName: ''
  }
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
        request.post("dictionary/del/batch", ids).then(() => {
          ElMessage.success("删除成功");
          getDataList();
        });
      })
      .catch(() => {
      });
};

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该信息吗?`, "提示", {
    type: "warning",
  })
      .then(() => {
        request.delete(`dictionary/del/${row.id}`).then(() => {
          ElMessage.success("删除成功");
          getDataList();
        });
      })
      .catch(() => {
      });
};

const addOrUpdateFlag = ref(false);
const addOrUpdateRef = ref();
const isReadonly = ref(false);

// 新增
const handleAdd = () => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  
  // 获取所有科目数据来计算最大代码
  request.post('/dictionary/page', { page: 1, limit: 1000, dicCode: 'kemu_types' }).then(res => {
    if (res.code === 200) {
      const allSubjects = res.data.list;
      nextTick(() => {
        addOrUpdateRef.value?.init(null, allSubjects);
      });
    }
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

// 成功回调
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  showIndexFlag.value = true;
  isReadonly.value = false;
  getDataList();
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

:deep(.el-button) {
  font-weight: 500;

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
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;

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

      .el-table__row--striped {
        td {
          background: var(--table-row-stripe-bg, #fafafa);
        }
      }
    }
  }

  &.el-table--border {
    border: 1px solid #ebeef5;

    td,
    th {
      border-right: 1px solid #ebeef5;
    }
  }
}

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

:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20px 0;
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

.status-tag {
  font-weight: 500;
  border: none;
}

.no-data {
  color: #909399;
  font-size: 13px;
}

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

