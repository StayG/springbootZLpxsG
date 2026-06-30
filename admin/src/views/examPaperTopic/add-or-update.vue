<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form ref="formRef" :model="formData" label-width="100px" :rules="formRules">

      <el-row :gutter="22">
        <!-- 试卷名称 -->
      <el-col :span="12">
        <el-form-item label="试卷名称" prop="examPaperName">
          <el-input v-model="formData.examPaperName" placeholder="请输入试卷名称" :disabled="true" />
        </el-form-item>
      </el-col>

      <!-- 考试时长 -->
      <el-col :span="12">
        <el-form-item label="考试时长" prop="examPaperDate">
          <el-input v-model="formData.examPaperDate" placeholder="考试时长" :disabled="true"/>
        </el-form-item>
      </el-col>
      </el-row>

      <el-row :gutter="22">
         <!-- 试卷总分数 -->
      <el-col :span="12">
        <el-form-item label="试卷总分数" prop="examPaperMyscore">
          <el-input v-model="formData.examPaperMyscore" placeholder="试卷总分数" :disabled="true" />
        </el-form-item>
      </el-col>

      <!-- 试题名称 -->
      <el-col :span="12">
        <el-form-item label="试题名称" prop="examQuestionName">
          <el-input v-model="formData.examQuestionName" placeholder="试题名称" :disabled="true" />
        </el-form-item>
      </el-col> 
     </el-row>

     <el-row :gutter="22">
         <!-- 正确答案 -->
      <el-col :span="12">
        <el-form-item label="正确答案" prop="examQuestionAnswer">
          <el-input v-model="formData.examQuestionAnswer" placeholder="正确答案" :disabled="true" />
        </el-form-item>
      </el-col>

      <!-- 答案解析 -->
      <el-col :span="12">
        <el-form-item label="答案解析" prop="examQuestionAnalysis">
          <el-input v-model="formData.examQuestionAnalysis" placeholder="答案解析" :disabled="true" />
        </el-form-item>
      </el-col> 
     </el-row>

     <el-row :gutter="22">
         <!-- 试题类型 -->
      <el-col :span="12">
        <el-form-item label="试题类型" prop="examQuestionTypesName">
          <el-input v-model="formData.examQuestionTypesName" placeholder="试题类型" :disabled="true" />
        </el-form-item>
      </el-col>

      <!-- 试题分数 -->
      <el-col :span="12">
        <el-form-item label="试题分数" prop="examPaperTopicNumber">
          <el-input 
            v-model.number="formData.examPaperTopicNumber" 
            placeholder="请输入试题分数" 
            :disabled="readonly"
            type="number"
            @keypress="handleNumberInput"
          />
        </el-form-item>
      </el-col> 
     </el-row>
      

      

      <!-- 操作按钮 -->
      <el-form-item v-if="!readonly">
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
      <el-form-item v-else>
        <el-button @click="handleCancel">关闭</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import '@/styles/common-form.scss';

// Props
const props = defineProps({
  readonly: {
    type: Boolean,
    default: false
  }
});

//事件
const emit = defineEmits(["handleSuccess"]);
// 表单数据
const formData = ref({
  id: null,
  examPaperName: "",
  examPaperDate: 0,
  examPaperMyscore: 0,
  examQuestionName: "",
  examQuestionAnswer: "",
  examQuestionAnalysis: "",
  examQuestionTypes: '',
  examPaperTopicNumber: 0
});
const formRef = ref(null);

// 表单验证规则
const formRules = ref({
  examPaperTopicNumber: [
    { required: true, message: "请输入试题分数", trigger: "blur" },
    { type: 'number', message: "试题分数必须为数字", trigger: "blur" },
    { 
      validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('试题分数不能为负数'));
        } else if (!Number.isInteger(value)) {
          callback(new Error('试题分数必须为整数'));
        } else {
          callback();
        }
      }, 
      trigger: "blur" 
    }
  ],
});

//初始化
const init = (id) => {
  console.log("🚀 ~ init ~ id:", id);
  if (id) {
    request.get(`examPaperTopic/info/${id}`).then(({ data }) => {
      formData.value = data;
    });
  }
};

// 限制只能输入数字
const handleNumberInput = (event) => {
  const charCode = event.which ? event.which : event.keyCode;
  // 只允许数字 0-9
  if (charCode < 48 || charCode > 57) {
    event.preventDefault();
  }
};

// 提交表单
const handleSubmit = async () => {

  await formRef?.value.validate();

  const api = formData.value.id ? "examPaperTopic/update" : "examPaperTopic/save";

  request.post(api, formData.value).then(() => {
    ElMessage.success("保存成功");
    emit("handleSuccess");
  });
};

// 取消
const handleCancel = () => {
  emit("handleSuccess");
};

defineExpose({
  init,
});
</script>


