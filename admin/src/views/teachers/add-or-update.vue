<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form ref="formRef" :model="formData" label-width="100px" :rules="formRules">
      <el-row :gutter="24">
        <!-- 用户名 -->
        <el-col :span="12">
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="formData.userName" placeholder="请输入用户名" :disabled="readonly || !!formData.id" />
          </el-form-item>
        </el-col>
        <!-- 真实姓名 -->
        <el-col :span="12">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="formData.realName" placeholder="请输入真实姓名" :disabled="readonly" />
          </el-form-item>
        </el-col>
        <!-- 性别 -->
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="formData.gender" placeholder="请选择性别" :disabled="readonly">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </el-col>
        <!-- 任教科目 -->
        <el-col :span="12">
          <el-form-item label="任教科目" prop="kemuTypes">
            <module-select
              v-model="formData.kemuTypes"
              titleName="indexName"
              optionKey="codeIndex"
              url="dictionary/page?dicCode=kemu_types"
              placeholder="请选择任教科目"
              :disabled="readonly"
            />
          </el-form-item>
        </el-col>
        <!-- 职称 -->
        <el-col :span="12">
          <el-form-item label="职称" prop="title">
            <el-input v-model="formData.title" placeholder="请输入职称" :disabled="readonly" />
          </el-form-item>
        </el-col>

        <!-- 头像 -->
        <el-col :span="24">
          <el-form-item label="头像" prop="avatar">
            <file-upload 
              action="file/upload" 
              :fileUrls="formData.avatar" 
              :limit="1" 
              :multiple="false" 
              :disabled="readonly"
              @change="avatarUploadChange"
            ></file-upload>
          </el-form-item>
        </el-col>

        <!-- 邮箱 -->
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" :disabled="readonly" />
          </el-form-item>
        </el-col>

        <!-- 电话 -->
        <el-col :span="12">
          <el-form-item label="电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入电话" :disabled="readonly" />
          </el-form-item>
        </el-col>

        <!-- 密码（仅新增时显示） -->
        <el-col :span="12" v-if="!formData.id && !readonly">
          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码（默认123456）" show-password />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <el-form-item v-if="!readonly">
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button type="default" @click="handleCancel">取消</el-button>
      </el-form-item>
      <el-form-item v-else>
        <el-button type="default" @click="handleCancel">关闭</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import ModuleSelect from "@/components/ModuleSelect/index.vue";
import '@/styles/common-form.scss';

// Props
const props = defineProps({
  readonly: {
    type: Boolean,
    default: false
  }
});

// 事件
const emit = defineEmits(["handleSuccess"]);

// 表单数据
const formData = ref({
  id: null,
  userName: "",
  password: "",
  realName: "",
  avatar: "",
  email: "",
  phone: "",
  gender: "",
  kemuTypes: null,
  title: "",
});

// 表单验证规则
const formRules = ref({
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  phone: [
    { required: true, message: "请输入电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" },
  ],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  kemuTypes: [{ required: true, message: "请选择任教科目", trigger: "change" }],
});
const formRef = ref(null);

// 初始化
const init = (id) => {
  if (id) {
    request.get(`teachers/info/${id}`).then(({ data }) => {
      formData.value = data;
    });
  } else {
    formData.value = {
      id: null,
      userName: "",
      password: "123456",
      realName: "",
      avatar: "",
      email: "",
      phone: "",
      gender: "",
      kemuTypes: null,
      title: "",
    };
  }
};

// 头像上传
const avatarUploadChange = (value) => {
  formData.value.avatar = value;
};

// 提交表单
const handleSubmit = async() => {
  
  await formRef?.value.validate();

  const api = formData.value.id ? "teachers/update" : "teachers/save";

  request.post(api, formData.value).then(() => {
    ElMessage.success("保存成功");
    emit("handleSuccess");
  });
};

// 取消
const handleCancel = () => {
  emit("handleSuccess");
};

// 暴露方法
defineExpose({
  init
});
</script>
