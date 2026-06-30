<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form ref="formRef" :model="formData" label-width="110px" :rules="formRules">
      <!-- 考试时长 -->
      <el-row :gutter="30">
        <el-col :span="13">
          <el-form-item label="考试时长(分钟)">
            <el-input v-model="formData.examPaperDate" disabled />
          </el-form-item>
        </el-col>
        <!-- 试卷总分 -->
        <el-col :span="13">
          <el-form-item label="试卷总分数">
            <el-input v-model="formData.examPaperMyscore" disabled />
          </el-form-item>
        </el-col>
      </el-row>


      <!-- 试题名称 -->
      <el-row :gutter="30">
        <el-col :span="13">
          <el-form-item label="试题名称">
            <el-input type="textarea" :rows="3" v-model="formData.examQuestionName" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 选项（仅单选题、多选题、判断题显示） -->
      <el-row :gutter="20">
        <el-col :span="13">
          <el-form-item
            v-if="formData.examQuestionTypes == 1 || formData.examQuestionTypes == 2 || formData.examQuestionTypes == 3"
            label="选项">
            <div v-for="(item, index) in JSON.parse(formData.examQuestionOptions || '[]')" :key="index">
              {{ item.code }}. {{ item.text }}
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <!--答案解析 -->
      <el-row :gutter="30">
        <el-col :span="13">
          <el-form-item label="答案解析">
            <el-input type="textarea" :rows="3" v-model="formData.examQuestionAnalysis" disabled />
          </el-form-item>
        </el-col>
      </el-row>



      <el-row :gutter="30">
        <!-- 试题排序 -->
        <el-col :span="13">
          <el-form-item label="试题排序">
            <el-input v-model="formData.examQuestionSequence" disabled />
          </el-form-item>
        </el-col>
        <!-- 考生答案 -->
        <el-col :span="13">
          <el-form-item label="考生答案">
            <el-input type="textarea" :rows="3" v-model="formData.examDetailsMyanswer" disabled />
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
  examPaperDate: "",
  examPaperMyscore: 0,
  examQuestionName: "",
  examQuestionTypes: 0,
  examQuestionAnalysis: "",
  examDetailsMyanswer: "",
  examQuestionSequence: 0,
});
const formRef = ref(null);

// 表单验证规则
const formRules = ref({
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入内容", trigger: "blur" }],
  pictures: [],
});

//初始化
const init = (id) => {
  console.log("🚀 ~ init ~ id:", id);
  if (id) {
    request.get(`examWrongQuestion/info/${id}`).then(({ data }) => {
      formData.value = data;
    });
  }
};

//富文本编辑器文本变化
const editorChange = (value) => {
  formData.value.content = value;
};
//图片上传
const pictureUploadChange = (value) => {
  formData.value.pictures = value;
};
// 提交表单
const handleSubmit = async () => {

  await formRef?.value.validate();

  const api = formData.value.id ? "examWrongQuestion/update" : "examWrongQuestion/save";

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
