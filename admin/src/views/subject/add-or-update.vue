<template>
  <div class="form-container">
    <!-- 表单 -->
  <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
    <!-- 科目代码 - 新增时自动生成，只读显示 -->
    <el-col :span="12">
      <el-form-item label="科目代码" prop="codeIndex">
        <el-input v-model="formData.codeIndex" placeholder="自动生成" disabled />
      </el-form-item>
    </el-col>

    <!-- 科目名称 -->
    <el-col :span="12">
      <el-form-item label="科目名称" prop="indexName">
        <el-input v-model="formData.indexName" placeholder="请输入科目名称" :disabled="readonly" />
      </el-form-item>
    </el-col>

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
  dicCode: 'kemu_types', // 科目类型
  codeIndex: '',
  indexName: '',
});
const formRef = ref(null);

// 表单验证规则
const formRules = ref({
  codeIndex: [{ required: true, message: "请输入科目代码", trigger: "blur" }],
  indexName: [{ required: true, message: "请输入科目名称", trigger: "blur" }],
});

//初始化
const init = (id, dataList = []) => {
  // 重置表单数据
  formData.value = {
    id: null,
    dicCode: 'kemu_types',
    codeIndex: '',
    indexName: '',
  };
  
  if (id) {
    // 编辑模式 - 获取现有数据
    request.get(`dictionary/info/${id}`).then(({ data }) => {
      formData.value = data;
    });
  } else {
    // 新增模式 - 自动生成科目代码
    // 获取现有最大代码值
    let maxCode = 0;
    dataList.forEach(item => {
      const code = Number(item.codeIndex);
      if (!isNaN(code) && code > maxCode) {
        maxCode = code;
      }
    });
    // 生成新代码（最大代码+1）
    formData.value.codeIndex = String(maxCode + 1);
  }
};

// 提交表单
const handleSubmit = async () => {

  await formRef?.value.validate();

  // 前端验证科目名称是否为空
  if (!formData.value.indexName || !formData.value.indexName.trim()) {
    ElMessage.error("科目名称不能为空");
    return;
  }

  const api = formData.value.id ? "dictionary/update" : "dictionary/save";

  request.post(api, formData.value).then(() => {
    ElMessage.success("保存成功");
    emit("handleSuccess");
  }).catch(error => {
    // 后端返回的错误信息会自动显示
    console.error("保存失败:", error);
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


