<template>
  <div class="form-container">
    <!-- 表单 -->
    <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
      <el-row>
        <!-- 科目 -->
        <el-col :span="13">
          <el-form-item label="科目" prop="kemuTypes">
            <el-select v-model="formData.kemuTypes" placeholder="请选择科目" :disabled="readonly || isTeacherRole">
              <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>

        <!-- 试卷名称 -->
        <el-col :span="13">
          <el-form-item label="试卷名称" prop="examPaperName">
            <el-input v-model="formData.examPaperName" placeholder="请输入试卷名称" :disabled="readonly" />
          </el-form-item>
        </el-col>

        <!-- 组卷方式 -->
        <el-col :span="13">
          <el-form-item label="组卷方式" prop="zujuanTypes">
            <el-select v-model="formData.zujuanTypes" placeholder="请选择组卷方式" :disabled="readonly" v-if="!readonly">
              <el-option v-for="item in zujuanOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-input v-else :model-value="formData.zujuanTypesName || formatZujuanTypes(formData.zujuanTypes)"
              readonly />
          </el-form-item>
        </el-col>

        <!-- 试卷状态 -->
        <el-col v-if="readonly" :span="12">
          <el-form-item label="试卷状态" prop="examPaperTypes">
            <el-input :model-value="formData.examPaperTypesName || formatExamPaperStatus(formData.examPaperTypes)"
              readonly />
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
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import "@/styles/common-form.scss";
import { getKemuOptions, getZujuanOptions, getExamPaperStatusOptions } from "@/utils/dictionary.js";
import storage from '@/utils/storage';
import config from '@/config/config';
import { useUserStore } from '@/stores/user';

// Props
const props = defineProps({
  readonly: {
    type: Boolean,
    default: false,
  },
});

// 事件
const emit = defineEmits(["handleSuccess"]);

// 获取用户信息
const userStore = useUserStore();

// 判断是否为教师角色（从 storage 中读取角色信息）
const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY); // 如 'teachers'
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY); // 如 '教师'
const isTeacherRole = ref(
  storedRole === 'teachers' || storedRoleName === '教师'
);

console.log('[试卷表单] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

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

// 科目选项
const kemuOptions = ref([]);

// 组卷方式选项
const zujuanOptions = ref([]);

// 试卷状态选项
const examPaperStatusOptions = ref([]);

// 获取字典选项（从字典接口获取）
const getDictOptions = async () => {
  try {
    const [kemuList, zujuanList, statusList] = await Promise.all([
      getKemuOptions(),
      getZujuanOptions(),
      getExamPaperStatusOptions()
    ]);
    kemuOptions.value = kemuList;
    zujuanOptions.value = zujuanList;
    examPaperStatusOptions.value = statusList;
  } catch (error) {
    console.error('获取字典选项失败:', error);
  }
};

// 科目 - 使用后端字典转换后的值
const formatKemu = (value) => {
  if (!value) return "";
  const option = kemuOptions.value.find(opt => opt.value == value);
  return option ? option.label : String(value);
};

// 组卷方式 - 使用后端字典转换后的值
const formatZujuanTypes = (value) => {
  if (!value) return "";
  const option = zujuanOptions.value.find(opt => opt.value == value);
  return option ? option.label : String(value);
};

// 试卷状态 - 使用后端字典转换后的值
const formatExamPaperStatus = (value) => {
  if (!value) return "";
  const option = examPaperStatusOptions.value.find(opt => opt.value == value);
  return option ? option.label : String(value);
};

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
    { required: true, message: "请选择科目", trigger: "change" },
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
    // 编辑模式
    request.get(`examPaper/info/${id}`).then(({ data }) => {
      // 根据你的后端返回结构调整
      formData.value = data.data || data;
      
      console.log('[试卷编辑] 教师角色:', isTeacherRole.value, '当前科目:', formData.value.kemuTypes);
    });
  } else {
    // 新增模式
    formData.value = {
      id: null,
      examPaperName: "",
      examPaperMyscore: "",
      kemuTypes: "",
      examPaperTypes: "",
      zujuanTypes: "",
      examPaperDelete: "",
    };
    
    // 教师新增时，自动填充并锁定任教科目
    if (isTeacherRole.value) {
      // 从 userStore.userInfo 中获取教师的任教科目
      const teacherKemuTypes = userStore.userInfo?.kemuTypes;
      if (teacherKemuTypes) {
        formData.value.kemuTypes = teacherKemuTypes;
        console.log('[试卷新增] 教师角色 - 自动设置科目:', teacherKemuTypes);
      } else {
        console.warn('[试卷新增] 教师角色 - 未找到kemuTypes信息，请检查登录状态');
      }
    }
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

// 初始化时获取字典选项
onMounted(() => {
  getDictOptions();
});

defineExpose({
  init,
});
</script>