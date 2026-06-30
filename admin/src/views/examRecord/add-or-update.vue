<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      label-width="120px"
      :rules="formRules"
    >
      <el-row>
        <!-- 试卷名称 -->
        <el-col :span="12">
          <el-form-item label="试卷名称" prop="examPaperName">
            <el-input
              v-model="formData.examPaperName"
              placeholder="请输入试卷名称"
              :disabled="readonly"
            />
          </el-form-item>
        </el-col>

        <!-- 试卷总分数 -->
        <el-col :span="12">
          <el-form-item label="试卷总分数" prop="examPaperMyscore">
            <el-input
              v-model="formData.examPaperMyscore"
              placeholder="请输入试卷总分数"
              :disabled="readonly"
            />
          </el-form-item>
        </el-col>

        <!-- 科目 -->
        <el-col :span="12">
          <el-form-item label="科目" prop="kemuTypes">
            <el-input
              v-model="formData.kemuTypes"
              placeholder="请输入科目"
              :disabled="readonly"
            />
          </el-form-item>
        </el-col>

        <!-- 组卷方式 -->
        <el-col :span="12">
          <el-form-item label="组卷方式" prop="zujuanTypes">
            <el-select
              v-model="formData.zujuanTypes"
              placeholder="请选择组卷方式"
              :disabled="readonly"
            >
              <el-option label="自动组卷" :value="1" />
              <el-option label="手动组卷" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>

        <!-- 试卷状态 -->
        <el-col :span="12">
          <el-form-item label="试卷状态" prop="examPaperTypes">
            <el-select
              v-model="formData.examPaperTypes"
              placeholder="请选择试卷状态"
              :disabled="readonly"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
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
import "@/styles/common-form.scss";

// Props
const props = defineProps({
  readonly: {
    type: Boolean,
    default: false,
  },
});

// 事件
const emit = defineEmits(["handleSuccess"]);

// 表单数据
const formData = ref({
  id: null,
  examPaperName: "",
  examPaperMyscore: "",
  kemuTypes: "",
  examPaperTypes: "",
  zujuanTypes: "",
  examPaperDelete: "",
});

const formRef = ref(null);

// 表单验证规则（改成试卷字段）
const formRules = {
  examPaperName: [
    { required: true, message: "请输入试卷名称", trigger: "blur" },
  ],
  examPaperMyscore: [
    { required: true, message: "请输入试卷总分数", trigger: "blur" },
    { pattern: /^[0-9]*$/, message: "只允许输入整数", trigger: "blur" },
  ],
  kemuTypes: [
    { required: true, message: "请输入科目", trigger: "blur" },
  ],
  zujuanTypes: [
    { required: true, message: "请选择组卷方式", trigger: "change" },
  ],
  examPaperTypes: [
    { required: true, message: "请选择试卷状态", trigger: "change" },
  ],
};

// 初始化
const init = (id) => {
  if (id) {
    request.get(`examPaper/info/${id}`).then(({ data }) => {
      // 根据你的后端返回结构调整
      formData.value = data.data || data;
    });
  } else {
    // 新增时重置
    formData.value = {
      id: null,
      examPaperName: "",
      examPaperMyscore: "",
      kemuTypes: "",
      examPaperTypes: "",
      zujuanTypes: "",
      examPaperDelete: "",
    };
  }
};

// 提交表单
const handleSubmit = async () => {
  await formRef.value?.validate();

  const api = formData.value.id ? "examPaper/update" : "examPaper/save";

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