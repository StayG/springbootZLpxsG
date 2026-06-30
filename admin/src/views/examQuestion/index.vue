<template>
  <div class="module-container">
    <div v-if="showIndexFlag">
      <div class="page-title">试题管理</div>
      <!-- 查询条件 -->
      <div class="filter-container">
        <div class="filter-item-group">
          <el-input v-model="params.examQuestionName" placeholder="请输入题干（或关键词）" class="filter-item" clearable />
          <!-- 选择科目（管理员可见，教师自动过滤） -->
          <el-select v-if="!isTeacherRole" v-model="params.kemuTypes" placeholder="请选择科目" class="filter-item" clearable>
            <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>

          <!-- 选择试题类型 -->
          <el-select v-model="params.examQuestionTypes" placeholder="请选择试题类型" class="filter-item" clearable>
            <el-option v-for="item in examQuestionTypeOptions" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
          <el-select v-model="params.difficultyLevel" placeholder="请选择难度" class="filter-item" clearable>
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
          <el-input v-model="params.knowledgePoint" placeholder="请输入知识点" class="filter-item" clearable />
        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="operation-container">
        <el-button v-if="utils.isAuth('examQuestion', '新增')" class="operation-btn" type="primary" @click="handleAdd">
          新增</el-button>
        <el-button class="operation-btn" type="success" @click="handleImportExcel"> 批量导入</el-button>
        <el-button class="operation-btn" type="warning" @click="handleExportExcel"> 批量导出</el-button>
        <el-button v-if="utils.isAuth('examQuestion', '删除')" class="operation-btn" type="primary"
          :disabled="multipleSelection.length === 0" @click="handleBatchDelete"> 批量删除
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-scroll-x">
        <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe>
          <el-table-column type="selection" width="55" header-align="center" align="center" />
          <el-table-column type="index" label="序号" width="65" header-align="center" align="center" />
          <el-table-column label="试题名称" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ truncateText(row.examQuestionName) }}
            </template>
          </el-table-column>
          <el-table-column prop="kemuTypes" label="科目" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ getKemuLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column label="正确答案" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ truncateText(row.examQuestionAnswer) }}
            </template>
          </el-table-column>
          <el-table-column label="答案解析" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ truncateText(row.examQuestionAnalysis) }}
            </template>
          </el-table-column>
          <el-table-column prop="examQuestionTypesName" label="试题类型" min-width="150" header-align="center" align="center">
            <template #default="{ row }">
              {{ getExamQuestionTypeLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column label="难度等级" width="100" header-align="center" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.difficultyLevel === 1" type="success" size="small">简单</el-tag>
              <el-tag v-else-if="row.difficultyLevel === 2" type="warning" size="small">中等</el-tag>
              <el-tag v-else-if="row.difficultyLevel === 3" type="danger" size="small">困难</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="知识点" min-width="120" header-align="center" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.knowledgePoint" type="info" size="small">{{ row.knowledgePoint }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" header-align="center" align="center">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" header-align="center" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
                <el-button v-if="utils.isAuth('examQuestion', '删除')" type="danger" size="small"
                  @click="handleDelete(row)">删除</el-button>
                <el-button v-if="utils.isAuth('examQuestion', '编辑')" type="success" size="small"
                  @click="handleEdit(row)">修改</el-button>
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

    <!-- Excel 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入试题" width="600px" :close-on-click-modal="false">
      <el-alert title="导入说明" type="info" :closable="false" show-icon style="margin-bottom: 20px;">
        <div>1. 请先下载模板，按照模板格式填写试题数据</div>
        <div>2. 选择题的选项必须为 JSON 格式，如：{"A":"选项 A","B":"选项 B","C":"选项 C","D":"选项 D"}</div>
        <div>3. 试题类型：1-单选题 2-多选题 3-判断题 4-填空题 5-简答题</div>
        <div>4. 科目类型：1-语文 2-数学 3-英语 4-物理 5-化学 6-生物 7-政治 8-历史 9-地理</div>
        <div>5. 简答题无需填写选项字段，参考答案可选填（用于人工批改）</div>
      </el-alert>

      <el-form label-width="100px">
        <el-form-item label="下载模板">
          <el-button type="primary" @click="downloadTemplate">
            <el-icon>
              <Download />
            </el-icon>
            下载 Excel 模板
          </el-button>
        </el-form-item>

        <el-form-item label="上传文件" required>
          <el-upload ref="uploadRef" drag action="#" :http-request="handleFileUpload" :before-upload="beforeFileUpload"
            :on-success="handleUploadSuccess" :on-error="handleUploadError" :limit="1" accept=".xlsx,.xls">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 xlsx/xls 文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { Download, UploadFilled } from '@element-plus/icons-vue';
import AddOrUpdate from "./add-or-update.vue";
import utils from "@/utils/utils.js";
import { getKemuOptions, getExamQuestionTypeOptions } from "@/utils/dictionary.js";
import storage from '@/utils/storage';
import config from '@/config/config';

// 判断是否为教师角色（从 storage 中读取角色信息）
const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY); // 如 'teachers'
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY); // 如 '教师'
const isTeacherRole = ref(
  storedRole === 'teachers' || storedRoleName === '教师'
);

console.log('[试题管理] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

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
  ...pageParam.value,
});

// 获取字典选项（从字典接口获取）
const getDictOptions = async () => {
  try {
    const [kemuList, typeList] = await Promise.all([
      getKemuOptions(),
      getExamQuestionTypeOptions()
    ]);

    kemuOptions.value = kemuList || [];
    examQuestionTypeOptions.value = typeList || [];

    // 如果选项为空，使用默认选项（静默处理）
    if (kemuOptions.value.length === 0) {
      kemuOptions.value = [
        { label: '语文', value: 1 },
        { label: '数学', value: 2 }
      ];
    }
    if (examQuestionTypeOptions.value.length === 0) {
      examQuestionTypeOptions.value = [
        { label: '单选题', value: 1 },
        { label: '多选题', value: 2 },
        { label: '判断题', value: 3 },
        { label: '填空题', value: 4 },
        { label: '简答题', value: 5 }
      ];
    }
  } catch (error) {
    console.error('获取字典选项失败:', error);
    // 设置默认选项，避免报错
    kemuOptions.value = [
      { label: '语文', value: 1 },
      { label: '数学', value: 2 }
    ];
    examQuestionTypeOptions.value = [
      { label: '单选题', value: 1 },
      { label: '多选题', value: 2 },
      { label: '判断题', value: 3 },
      { label: '填空题', value: 4 },
      { label: '简答题', value: 5 }
    ];
  }
};

// 获取数据列表
const getDataList = () => {
  const payload = {
    ...params.value,
    page: 1,
    limit: 2000,
  };
  request.post("examQuestion/page", payload).then(({ data }) => {
    const sourceList = data?.list || [];
    const keyword = String(params.value.knowledgePoint || "").trim().toLowerCase();
    const filteredList = sourceList.filter((item) => {
      if (params.value.difficultyLevel !== undefined && params.value.difficultyLevel !== null && params.value.difficultyLevel !== "") {
        if (Number(item.difficultyLevel) !== Number(params.value.difficultyLevel)) return false;
      }
      if (keyword) {
        const rowKnowledgePoint = String(item.knowledgePoint || "").toLowerCase();
        if (!rowKnowledgePoint.includes(keyword)) return false;
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

// 科目类型标签转换 - 使用后端字典转换后的值
const getKemuLabel = (row) => {
  return row.kemuTypesName || row.kemuTypes || "";
};

// 科目选项 - 从字典接口获取
const kemuOptions = ref([]);

// 题目类型标签转换 - 使用后端字典转换后的值
const getExamQuestionTypeLabel = (row) => {
  return row.examQuestionTypesName || row.examQuestionTypes || "";
};

// 题目类型选项 - 从字典接口获取
const examQuestionTypeOptions = ref([]);

// 新增重置方法
const handleReset = () => {
  params.value = {
    ...pageParam.value,
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
      request.post("examQuestion/del/batch", ids).then(() => {
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
      request.delete(`examQuestion/del/${row.id}`).then(() => {
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

// 成功回调
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  showIndexFlag.value = true;
  isReadonly.value = false;
  getDataList();
};

// 文本中间省略号函数
const truncateText = (text, maxLength = 15) => {
  if (!text || typeof text !== 'string') return text;
  if (text.length <= maxLength) return text;
  const ellipsis = '...';
  const half = Math.floor((maxLength - ellipsis.length) / 2);
  return text.slice(0, half) + ellipsis + text.slice(text.length - half);
};

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

// ========== Excel 批量导入相关 ==========
const importDialogVisible = ref(false);
const uploadRef = ref(null);

// 打开导入对话框
const handleImportExcel = () => {
  importDialogVisible.value = true;
};

// 下载模板
const downloadTemplate = () => {
  // 使用原生方式下载，避免被 axios 拦截器处理
  const baseUrl = request.defaults.baseURL || '/springbootZLpxsG/';
  // 确保 baseUrl 以 / 结尾
  const normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl : baseUrl + '/';
  const url = `${normalizedBaseUrl}examQuestion/downloadTemplate`;
  window.open(url, '_blank');
};

// 批量导出 Excel
const handleExportExcel = async () => {
  try {
    const confirmed = await ElMessageBox.confirm(
      '确定要导出当前条件下的所有试题吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).catch(() => false);

    if (!confirmed) return;

    // 构建导出参数（包含当前筛选条件）
    const exportParams = {
      examQuestionName: params.value.examQuestionName,
      kemuTypes: params.value.kemuTypes,
      examQuestionTypes: params.value.examQuestionTypes,
      difficultyLevel: params.value.difficultyLevel,
      knowledgePoint: params.value.knowledgePoint,
    };

    // 直接使用 axios 实例，绕过响应拦截器的 JSON 解析
    const response = await request.post('examQuestion/exportExcel', exportParams, {
      responseType: 'blob',
      // 添加一个标记，让拦截器知道这是文件下载
      headers: {
        'X-Download-File': 'true'
      }
    });

    console.log('导出响应:', response);

    // 创建 Blob 对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.ms-excel',
    });

    console.log('Blob 创建成功:', blob);

    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;

    // 生成文件名（带时间戳）
    const timestamp = new Date().getTime();
    link.download = `试题数据_${timestamp}.xls`;

    // 触发下载
    document.body.appendChild(link);
    link.click();

    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    ElMessage.success('导出成功，文件正在下载');
  } catch (error) {
    console.error('导出失败:', error);
    // 如果是 Blob 数据被错误拦截器捕获，尝试从 error 中提取 blob
    if (error instanceof Blob) {
      const reader = new FileReader();
      reader.onload = function (event) {
        try {
          const errorData = JSON.parse(event.target.result);
          ElMessage.error(errorData.msg || '导出失败');
        } catch (e) {
          ElMessage.error('导出失败：' + error.message);
        }
      };
      reader.readAsText(error);
    } else {
      ElMessage.error('导出失败：' + (error.message || '请稍后重试'));
    }
  }
};

// 文件上传前校验
const beforeFileUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
    file.type === 'application/vnd.ms-excel';
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件！');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！');
    return false;
  }
  return true;
};

// 自定义文件上传处理
const handleFileUpload = async (options) => {
  const { file } = options;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await request.post('examQuestion/importExcel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (response.code === 200) {
      const result = response.data;
      if (result.success) {
        ElMessage.success(result.message);
        importDialogVisible.value = false;
        getDataList();

        // 如果有错误信息，显示详细对话框
        if (result.errors && result.errors.length > 0) {
          showImportErrors(result.errors);
        }
      } else {
        ElMessage.error(result.message || '导入失败');
        if (result.errors && result.errors.length > 0) {
          showImportErrors(result.errors);
        }
      }
    } else {
      ElMessage.error(response.msg || '导入失败');
    }
  } catch (error) {
    console.error('导入失败:', error);
    ElMessage.error('导入失败，请稍后重试');
  }
};

// 显示导入错误信息
const showImportErrors = (errors) => {
  ElMessageBox.alert(
    errors.join('<br/>'),
    '导入错误详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定',
      customStyle: {
        maxWidth: '600px',
      },
    }
  );
};

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  console.log('上传成功', response, file);
};

// 上传失败回调
const handleUploadError = (error) => {
  console.error('上传失败', error);
  ElMessage.error('上传失败，请稍后重试');
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

