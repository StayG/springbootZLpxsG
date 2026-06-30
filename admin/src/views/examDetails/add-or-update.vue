<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
      
      <!-- 基本信息 -->
      <el-row :gutter="20">
        <!-- 试卷编号 -->
        <el-col :span="12">
          <el-form-item label="试卷编号">
            <el-input v-model="formData.examDetailsUuidNumber" readonly />
          </el-form-item>
        </el-col>

        <!-- 用户姓名 -->
        <el-col :span="12">
          <el-form-item label="用户姓名">
            <el-input v-model="formData.nickname" readonly />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 试题信息 -->
      <el-row :gutter="20">
        <!-- 试题名称 -->
        <el-col :span="12">
          <el-form-item label="试题名称">
            <el-input v-model="formData.examQuestionName" readonly />
          </el-form-item>
        </el-col>

        <!-- 试题类型 -->
        <el-col :span="12">
          <el-form-item label="试题类型">
            <el-input :value="getExamQuestionTypeLabel()" readonly />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 选项（填空题时不显示） -->
      <el-row v-if="formData.examQuestionTypes != 4">
        <el-col :span="24">
          <el-form-item label="选项">
            <div class="options-container">
              <div class="option-item" v-for="(item, index) in options" :key="index">
                <div class="option-content">
                  <span class="option-code">{{ item.code }}</span>
                  <span class="option-text">{{ item.text }}</span>
                </div>
              </div>
              <div v-if="options.length === 0" class="no-options">暂无选项</div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 答案信息 - 填空题 -->
      <el-row v-if="formData.examQuestionTypes == 4" :gutter="20">
        <!-- 正确答案 -->
        <el-col :span="12">
          <el-form-item label="正确答案">
            <el-input 
              type="textarea"
              :rows="3"
              v-model="formData.examQuestionAnswer" 
              readonly
              placeholder="暂无答案"
            />
          </el-form-item>
        </el-col>

        <!-- 考生答案 -->
        <el-col :span="12">
          <el-form-item label="考生答案">
            <el-input 
              type="textarea"
              :rows="3"
              v-model="formData.examDetailsMyanswer" 
              readonly
              placeholder="考生未作答"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 答案信息 - 其他题型 -->
      <el-row v-else :gutter="20">
        <!-- 正确答案 -->
        <el-col :span="12">
          <el-form-item label="正确答案">
            <el-input v-model="formData.examQuestionAnswer" readonly />
          </el-form-item>
        </el-col>

        <!-- 考生答案 -->
        <el-col :span="12">
          <el-form-item label="考生答案">
            <el-input v-model="formData.examDetailsMyanswer" readonly />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 得分 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="试题得分">
            <el-input v-model="formData.examDetailsMyscore" readonly>
              <template #append>分</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 简答题批阅区（仅简答题显示） -->
      <el-row v-if="formData.examQuestionTypes == 5" :gutter="20">
        <el-col :span="24">
          <el-divider content-position="left">教师批阅</el-divider>
        </el-col>
        
        <!-- 教师评分 -->
        <el-col :span="12">
          <el-form-item label="教师评分" prop="teacherScore">
            <el-input-number 
              v-model="formData.teacherScore" 
              :min="0" 
              :max="formData.examPaperTopicNumber || 100"
              :disabled="!readonly"
              placeholder="请输入分数"
              style="width: 100%;"
            >
              <template #append>分</template>
            </el-input-number>
            <div class="score-hint" v-if="formData.examPaperTopicNumber">
              （满分：{{ formData.examPaperTopicNumber }} 分）
            </div>
          </el-form-item>
        </el-col>

        <!-- 批阅状态 -->
        <el-col :span="12">
          <el-form-item label="批阅状态">
            <el-tag :type="formData.reviewStatus === 1 ? 'success' : 'warning'" size="large">
              {{ formData.reviewStatus === 1 ? '已批阅' : '待批阅' }}
            </el-tag>
          </el-form-item>
        </el-col>

        <!-- 教师评语 -->
        <el-col :span="24">
          <el-form-item label="教师评语" prop="teacherComment">
            <el-input 
              v-model="formData.teacherComment" 
              type="textarea"
              :rows="4"
              :disabled="!readonly"
              placeholder="请输入评语（可选）"
            />
          </el-form-item>
        </el-col>

        <!-- 批阅按钮 -->
        <el-col :span="24" v-if="!readonly && formData.examQuestionTypes == 5">
          <el-form-item>
            <el-button type="primary" @click="submitGrading">提交批阅</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 答案解析 -->
      <el-row>
        <el-col :span="24">
          <el-form-item label="答案解析">
            <el-input 
              type="textarea"
              :rows="4"
              v-model="formData.examQuestionAnalysis" 
              readonly
              placeholder="暂无解析"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button @click="handleCancel">关闭</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import "@/styles/common-form.scss";

// Props
const props = defineProps({
  readonly: {
    type: Boolean,
    default: true, // 详情页默认只读
  },
});

// 事件
const emit = defineEmits(["handleSuccess"]);

// 表单数据
const formData = ref({
  id: null,
  examDetailsUuidNumber: "",
  usersId: null,
  examQuestionId: null,
  examDetailsMyanswer: "",
  examDetailsMyscore: 0,
  teacherScore: 0, // 教师评分
  teacherComment: "", // 教师评语
  reviewStatus: 0, // 批阅状态：0-待批阅 1-已批阅
  createTime: "",
  examQuestionName: "",
  nickname: "",
  examQuestionTypes: null,
  examQuestionTypesName: "",
  kemuTypes: null,
  kemuTypesName: "",
  examQuestionOptions: "[]",
  examQuestionAnswer: "",
  examQuestionAnalysis: "",
  examPaperName: "",
  examPaperTopicNumber: "", // 题目满分
});

const formRef = ref(null);

// 选项列表
const options = computed(() => {
  try {
    const parsed = JSON.parse(formData.value.examQuestionOptions || '[]');
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    return [];
  }
});

// 获取试题类型标签
const getExamQuestionTypeLabel = () => {
  if (formData.value.examQuestionTypesName) {
    return formData.value.examQuestionTypesName;
  }
  
  // 如果没有名称，根据类型值返回默认名称
  const typeMap = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题'
  };
  return typeMap[formData.value.examQuestionTypes] || '未知类型';
};

// 表单验证规则
const formRules = {};

// 初始化
const init = (id) => {
  if (id) {
    request.get(`examDetails/info/${id}`).then(({ data }) => {
      // 根据后端返回结构调整
      const responseData = data.data || data;
      formData.value = {
        ...formData.value,
        ...responseData
      };
    });
  }
};

// 提交批阅
const submitGrading = async () => {
  // 验证分数
  if (formData.value.teacherScore === null || formData.value.teacherScore === undefined) {
    ElMessage.warning('请输入分数');
    return;
  }

  const maxScore = formData.value.examPaperTopicNumber || 100;
  if (formData.value.teacherScore > maxScore) {
    ElMessage.warning(`分数不能超过满分 ${maxScore} 分`);
    return;
  }

  try {
    const response = await request.post('/examDetails/gradeSubjective', {
      examDetailsId: formData.value.id,
      teacherScore: formData.value.teacherScore,
      teacherComment: formData.value.teacherComment
    });

    if (response.code === 200) {
      ElMessage.success('批阅成功');
      formData.value.reviewStatus = 1; // 更新本地状态
      formData.value.examDetailsMyscore = formData.value.teacherScore; // 同步得分
      emit("handleSuccess");
    } else {
      ElMessage.error(response.msg || '批阅失败');
    }
  } catch (error) {
    console.error('批阅失败:', error);
    ElMessage.error('批阅失败：' + (error.message || '请稍后重试'));
  }
};

// 取消/关闭
const handleCancel = () => {
  emit("handleSuccess");
};

defineExpose({
  init,
});
</script>


<style lang="scss" scoped>
.form-container {
  padding: 20px;
  background: var(--card-bg, #fff);
  border-radius: 8px;
}

.options-container {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  transition: all 0.2s;

  .option-content {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;

    .option-code {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 28px;
      height: 28px;
      background: #fff;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      font-size: 14px;
      font-weight: 500;
      color: #595959;
    }

    .option-text {
      flex: 1;
      font-size: 14px;
      color: #262626;
      line-height: 1.5;
    }
  }
}

.no-options {
  padding: 12px;
  text-align: center;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border: 1px dashed #e8e8e8;
  border-radius: 4px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 6px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.is-focus {
    box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
  }
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
}

:deep(.el-textarea.is-disabled .el-textarea__inner) {
  background-color: #f5f7fa;
  color: #606266;
}

:deep(.el-select) {
  width: 100%;
}

.score-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}
</style>
