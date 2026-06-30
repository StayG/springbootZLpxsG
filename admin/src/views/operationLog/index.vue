<template>
  <div class="module-container">
    <div class="page-title">操作日志</div>

    <el-alert
      class="tip-alert"
      type="info"
      :closable="false"
      show-icon
      title="记录管理员与教师在管理后台的关键操作，便于审计与追溯。教师仅可查看本人相关日志。"
    />

    <div class="filter-card">
      <!-- 同一四列网格：第二行「关键词」与第一列「操作时间」同宽 -->
      <div class="filter-grid-main">
        <div class="filter-item">
          <div class="label">操作时间</div>
          <el-date-picker
            v-model="pickDate"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            clearable
            :popper-options="{ strategy: 'fixed' }"
            @change="onFilterDimensionChange"
          />
        </div>
        <div class="filter-item">
          <div class="label">操作人</div>
          <el-select
            v-model="params.adminName"
            placeholder="请选择操作人"
            clearable
            filterable
            @change="onAdminChange"
          >
            <el-option v-for="n in adminOptions" :key="n" :label="n" :value="n" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">操作模块</div>
          <el-select
            v-model="params.operationModule"
            placeholder="请选择操作模块"
            clearable
            filterable
            @change="onFilterDimensionChange"
          >
            <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">操作类型</div>
          <el-select
            v-model="params.actionType"
            placeholder="请选择操作类型"
            clearable
            @change="onFilterDimensionChange"
          >
            <el-option v-for="t in actionTypeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </div>
        <div class="filter-item">
          <div class="label">关键词</div>
          <el-input
            v-model="params.keyword"
            placeholder="请输入操作内容关键词"
            clearable
            @keyup.enter="handleKeywordEnter"
          >
            <template #prefix>
              <el-icon class="search-ico"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button
            v-if="utils.isAuth('operationLog', '导出')"
            class="op-btn"
            @click="handleExport"
          >
            <el-icon class="btn-ico"><Download /></el-icon>
            导出日志
          </el-button>
        </div>
        <el-button circle title="刷新列表" @click="handleToolbarRefresh">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table :data="dataList" style="width: 100%" border stripe>
        <el-table-column prop="operationTime" label="操作时间" min-width="168" header-align="center" align="center" />
        <el-table-column prop="adminName" label="操作人" width="100" header-align="center" align="center" />
        <el-table-column prop="operationModule" label="操作模块" min-width="120" header-align="center" align="center" />
        <el-table-column label="操作类型" width="100" header-align="center" align="center">
          <template #default="{ row }">
            <span class="action-type-text" :style="{ color: actionTypeColor(row.actionType) }">{{ row.actionType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" min-width="220" header-align="center" align="left" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="130" header-align="center" align="center" />
        <el-table-column label="操作结果" width="110" header-align="center" align="center">
          <template #default="{ row }">
            <span class="result-cell" :class="row.success === 1 ? 'ok' : 'fail'">
              <span class="result-dot" />
              {{ row.success === 1 ? '成功' : '失败' }}
            </span>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { scrollAdminLayoutToTop } from '@/utils/adminScrollLayout.js'
import { ElMessage } from 'element-plus'
import { Search, Download, Refresh } from '@element-plus/icons-vue'
import utils from '@/utils/utils.js'

const route = useRoute()

const dataList = ref([])
const total = ref(0)
/** 操作时间：单日筛选 */
const pickDate = ref(null)
const adminOptions = ref([])
const moduleOptions = ref([])
const actionTypeOptions = ref([])

const pageParam = ref({
  page: 1,
  limit: 10,
})

const params = ref({
  ...pageParam.value,
  beginTime: '',
  endTime: '',
  adminName: '',
  operationModule: '',
  actionType: '',
  keyword: '',
})

const syncPickDateToParams = () => {
  if (pickDate.value) {
    params.value.beginTime = `${pickDate.value} 00:00:00`
    params.value.endTime = `${pickDate.value} 23:59:59`
  } else {
    params.value.beginTime = ''
    params.value.endTime = ''
  }
}

const getDataList = () => {
  syncPickDateToParams()
  request.post('operationLog/page', { ...params.value }).then(({ data }) => {
    dataList.value = data.list || []
    total.value = data.total ?? 0
  })
}

/** 按当前时间/模块/类型联动「操作人」下拉（不含操作人、关键词） */
const loadOptions = () => {
  syncPickDateToParams()
  return request
    .get('operationLog/options', {
      params: {
        beginTime: params.value.beginTime || undefined,
        endTime: params.value.endTime || undefined,
        operationModule: params.value.operationModule || undefined,
        actionType: params.value.actionType || undefined,
      },
    })
    .then(({ data }) => {
      adminOptions.value = data.adminNames || []
      moduleOptions.value = data.modules || []
      actionTypeOptions.value = data.actionTypes || []
    })
    .catch(() => {
      adminOptions.value = []
    })
}

/** 日期 / 模块 / 类型变更：重置页码、刷新操作人选项与表格 */
const onFilterDimensionChange = () => {
  params.value.page = 1
  loadOptions().finally(() => {
    getDataList()
  })
}

/** 仅操作人变更：不重算下拉，只刷新表格 */
const onAdminChange = () => {
  params.value.page = 1
  getDataList()
}

let keywordDebounceTimer = null
const KEYWORD_DEBOUNCE_MS = 400
/** 重置参数时避免 keyword 的 watch 再发起防抖查询 */
let suppressKeywordWatch = false

const scheduleKeywordSearch = () => {
  if (keywordDebounceTimer) {
    clearTimeout(keywordDebounceTimer)
  }
  keywordDebounceTimer = setTimeout(() => {
    keywordDebounceTimer = null
    params.value.page = 1
    getDataList()
  }, KEYWORD_DEBOUNCE_MS)
}

const handleKeywordEnter = () => {
  if (keywordDebounceTimer) {
    clearTimeout(keywordDebounceTimer)
    keywordDebounceTimer = null
  }
  params.value.page = 1
  getDataList()
}

/** 表格工具栏刷新：保持当前筛选与页码，重新拉取列表 */
const handleToolbarRefresh = () => {
  getDataList()
}

const handleSearch = () => {
  if (keywordDebounceTimer) {
    clearTimeout(keywordDebounceTimer)
    keywordDebounceTimer = null
  }
  params.value.page = 1
  getDataList()
}

const handleReset = () => {
  if (keywordDebounceTimer) {
    clearTimeout(keywordDebounceTimer)
    keywordDebounceTimer = null
  }
  suppressKeywordWatch = true
  pickDate.value = null
  params.value = {
    ...pageParam.value,
    beginTime: '',
    endTime: '',
    adminName: '',
    operationModule: '',
    actionType: '',
    keyword: '',
  }
  loadOptions().finally(() => {
    params.value.page = 1
    getDataList()
    suppressKeywordWatch = false
  })
}

watch(
  () => params.value.keyword,
  () => {
    if (suppressKeywordWatch) {
      return
    }
    scheduleKeywordSearch()
  },
)

const handleSizeChange = () => {
  params.value.page = 1
  getDataList()
}

const handleCurrentChange = () => {
  getDataList()
}

const actionTypeColor = (t) => {
  const map = {
    新增: '#16a34a',
    修改: '#2563eb',
    删除: '#dc2626',
    发布: '#7c3aed',
    导入: '#ea580c',
    启用: '#16a34a',
    禁用: '#ea580c',
    阅卷: '#0891b2',
    导出: '#ea580c',
  }
  return map[t] || '#606266'
}

const handleExport = async () => {
  try {
    syncPickDateToParams()
    const body = {
      beginTime: params.value.beginTime,
      endTime: params.value.endTime,
      adminName: params.value.adminName,
      operationModule: params.value.operationModule,
      actionType: params.value.actionType,
      keyword: params.value.keyword,
      page: 1,
      limit: 5000,
    }
    const response = await request.post('operationLog/export', body, { responseType: 'blob' })
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功，文件正在下载')
  } catch (e) {
    ElMessage.error('导出失败，请稍后重试')
  }
}

onMounted(() => {
  scrollAdminLayoutToTop()
  loadOptions().finally(() => {
    getDataList()
  })
})

watch(
  () => route.fullPath,
  () => {
    scrollAdminLayoutToTop()
  },
)
</script>

<style lang="scss" scoped>
.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 18px;
}

.tip-alert {
  margin-bottom: 18px;
  border-radius: 8px;
  border: 1px solid #bfdbfe;
  background: #eff6ff;

  :deep(.el-alert__title) {
    font-size: 14px;
    color: #1e40af;
    line-height: 1.5;
  }
}

/* ==================== 筛选区排版与阅卷管理一致 ==================== */
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
  overflow-x: auto;
}

/* 四列网格：第一行四个筛选项；第二行「关键词」占第一列，与「操作时间」同宽 */
.filter-grid-main {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 14px 16px;
  min-width: min(100%, 680px);
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

/* ==================== 表格卡片与阅卷管理一致 ==================== */
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

.btn-ico {
  margin-right: 4px;
  vertical-align: middle;
}

.search-ico {
  color: #a8abb2;
}

.action-type-text {
  font-weight: 600;
  font-size: 14px;
}

.result-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;

  &.ok {
    color: #16a34a;
  }
  &.fail {
    color: #dc2626;
  }
}

.result-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.result-cell.ok .result-dot {
  background: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.25);
}

.result-cell.fail .result-dot {
  background: #ef4444;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.22);
}

/* ==================== 表格统一样式（与阅卷管理一致） ==================== */
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

/* 回到顶部：与成绩与统计页一致，避免被表格/分页遮挡 */
:deep(.gs-page-backtop.el-backtop) {
  z-index: 2000;
}
</style>
