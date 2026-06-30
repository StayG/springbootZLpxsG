<template>
  <el-dialog
    v-model="visible"
    :title="!formData.id ? '新增考试' : '编辑考试'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="140px">
      <!-- 步骤一：基础信息 -->
      <el-divider content-position="left">基础信息</el-divider>
      
      <el-form-item label="考试名称" prop="examName">
        <el-input v-model="formData.examName" placeholder="请输入考试名称" />
      </el-form-item>

      <el-form-item label="科目" prop="kemuTypes">
        <el-select 
          v-model="formData.kemuTypes" 
          placeholder="请选择科目" 
          :disabled="readonly || isTeacherRole"
          style="width: 100%"
          @change="handleKemuChange"
        >
          <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="考试日期" prop="examDate">
        <el-date-picker
          v-model="formData.examDate"
          type="date"
          placeholder="选择考试日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTimeStr">
            <el-time-picker
              v-model="formData.startTimeStr"
              placeholder="选择开始时间"
              format="HH:00"
              value-format="HH"
              style="width: 100%"
            />
            <div class="form-tip">仅支持整点，分钟固定为 00</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="考试时长（分钟）" prop="duration">
            <el-input-number 
              v-model="formData.duration" 
              :min="1" 
              :max="300" 
              placeholder="输入时长"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="及格分数" prop="passingScore">
        <el-input-number 
          v-model="formData.passingScore" 
          :min="0" 
          :max="selectedPaperScore || 999" 
          placeholder="请输入及格分数"
          style="width: 100%"
        />
        <div class="form-tip" v-if="selectedPaperScore">
          试卷总分：{{ selectedPaperScore }} 分，建议设置为 {{ Math.round(selectedPaperScore * 0.6) }} 分（60%）
        </div>
      </el-form-item>

      <el-form-item label="结束时间">
        <el-input 
          v-model="calculatedEndTime" 
          disabled 
          placeholder="根据开始时间和时长自动计算" 
        />
      </el-form-item>

      <!-- 步骤二：关联试卷 -->
      <el-divider content-position="left">关联试卷</el-divider>

      <el-form-item label="选择试卷" prop="examPaperId">
        <el-select 
          v-model="formData.examPaperId" 
          placeholder="请选择试卷" 
          filterable
          @change="handleExamPaperChange"
          style="width: 100%"
        >
          <el-option 
            v-for="item in examPaperList" 
            :key="item.id" 
            :label="item.examPaperName" 
            :value="item.id" 
          />
        </el-select>
      </el-form-item>

      <el-form-item label="试卷总分" v-if="selectedPaperScore">
        <el-tag type="success">{{ selectedPaperScore }} 分</el-tag>
      </el-form-item>

      <!-- 步骤三：规则配置 -->
      <el-divider content-position="left">防作弊规则配置</el-divider>

      <el-form-item label="允许切屏">
        <el-switch v-model="formData.allowScreenSwitch" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="最大切屏次数" v-if="formData.allowScreenSwitch === 0">
        <el-input-number 
          v-model="formData.maxScreenSwitch" 
          :min="1" 
          :max="10" 
          placeholder="超过此次数将强制交卷"
        />
        <div class="form-tip">不允许切屏时，超过此次数将强制交卷</div>
      </el-form-item>

      <el-form-item label="允许复制粘贴">
        <el-switch v-model="formData.allowCopyPaste" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="选项乱序">
        <el-switch v-model="formData.optionShuffle" :active-value="1" :inactive-value="0" />
        <div class="form-tip">开启后，每次考试题目选项顺序随机</div>
      </el-form-item>

      <el-form-item label="考试密码">
        <el-input 
          v-model="formData.examPassword" 
          placeholder="可选，设置后学生需输入密码才能进入考试" 
          clearable
        />
      </el-form-item>

      <el-form-item label="交卷后显示答案">
        <el-switch v-model="formData.showAnswerAfterSubmit" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="允许重考">
        <el-switch v-model="formData.allowRetake" :active-value="1" :inactive-value="0" />
      </el-form-item>

      <el-form-item label="最多重考次数" prop="maxRetakeCount" v-if="formData.allowRetake === 1">
        <el-input-number
          v-model="formData.maxRetakeCount"
          :min="1"
          :max="10"
          placeholder="请输入最多重考次数"
          style="width: 100%"
        />
        <div class="form-tip">表示学生最多可重新参加考试的次数，不含首次考试</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { getKemuOptions } from "@/utils/dictionary.js";
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

console.log('[考试表单] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

// 弹窗可见性
const visible = ref(false);

// 表单引用
const formRef = ref(null);

// 提交加载状态
const submitLoading = ref(false);

// 时间段选择器绑定值（已迁移到 formData 中）
// const timeRange = ref([]);

// 试卷列表
const examPaperList = ref([]);
const selectedPaperScore = ref(null);

// 字典选项
const kemuOptions = ref([]);

// 表单数据
const formData = ref({
  id: null,
  examName: "",
  kemuTypes: null,
  teacherId: null,
  examPaperId: null,
  examDate: "", // 新增：考试日期 YYYY-MM-DD
  startTimeStr: "", // 新增：开始时间字符串 HH:mm
  duration: 90, // 默认时长
  passingScore: 0, // 及格分数，默认为0
  startTime: null, // 最终提交的 Date/String
  endTime: null,   // 最终提交的 Date/String
  allowScreenSwitch: 0,
  maxScreenSwitch: 3,
  allowCopyPaste: 0,
  optionShuffle: 0,
  examPassword: "",
  showAnswerAfterSubmit: 0,
  allowRetake: 0,
  maxRetakeCount: 0,
  status: 0,
});

// 计算属性：自动计算并展示结束时间
const calculatedEndTime = computed(() => {
  if (!formData.value.examDate || !formData.value.startTimeStr || !formData.value.duration) {
    return "";
  }
  const datePart = formData.value.examDate; // YYYY-MM-DD
  const hours = parseInt(formData.value.startTimeStr, 10); // 只取小时
  
  // 分钟默认为 00
  const startDateTime = new Date(`${datePart} ${String(hours).padStart(2, '0')}:00:00`);
  const endDateTime = new Date(startDateTime.getTime() + formData.value.duration * 60 * 1000);
  
  const year = endDateTime.getFullYear();
  const month = String(endDateTime.getMonth() + 1).padStart(2, '0');
  const day = String(endDateTime.getDate()).padStart(2, '0');
  const h = String(endDateTime.getHours()).padStart(2, '0');
  const m = String(endDateTime.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${h}:${m}`;
});

// 自定义验证器
const validateExamDate = (rule, value, callback) => {
  if (!value) return callback(new Error('请选择考试日期'));
  callback();
};

const validateStartTimeStr = (rule, value, callback) => {
  if (!value) return callback(new Error('请选择开始时间'));
  callback();
};

const validateDuration = (rule, value, callback) => {
  if (!value || value <= 0) return callback(new Error('请输入有效的考试时长'));
  callback();
};

const validateMaxRetakeCount = (rule, value, callback) => {
  if (formData.value.allowRetake === 1 && (!value || value < 1)) {
    return callback(new Error('开启允许重考后，请输入至少 1 次最多重考次数'));
  }
  callback();
};

// 表单验证规则
const rules = {
  examName: [{ required: true, message: "请输入考试名称", trigger: "blur" }],
  kemuTypes: [{ required: true, message: "请选择科目", trigger: "change" }],
  examDate: [{ required: true, validator: validateExamDate, trigger: "change" }],
  startTimeStr: [{ required: true, validator: validateStartTimeStr, trigger: "change" }],
  duration: [{ required: true, validator: validateDuration, trigger: "blur" }],
  examPaperId: [{ required: true, message: "请选择关联试卷", trigger: "change" }],
  maxRetakeCount: [{ validator: validateMaxRetakeCount, trigger: "blur" }],
};

// 初始化
const init = (id) => {
  visible.value = true;
  
  if (id) {
    // 编辑模式
    request.get(`examInfo/info/${id}`).then(({ data }) => {
      const info = data.data || data;
      formData.value = { ...formData.value, ...info };
      
      // 解析时间回显（只取小时）
      if (info.startTime) {
        const startDate = new Date(info.startTime);
        formData.value.examDate = `${startDate.getFullYear()}-${String(startDate.getMonth() + 1).padStart(2, '0')}-${String(startDate.getDate()).padStart(2, '0')}`;
        formData.value.startTimeStr = String(startDate.getHours()).padStart(2, '0'); // 只取小时，如 "09"
      }
      
      // 加载关联试卷的分数
      if (formData.value.examPaperId) {
        loadExamPaperScore(formData.value.examPaperId);
      }
    });
  } else {
    // 新增模式
    resetForm();
    
    // 教师新增时，自动填充并锁定任教科目
    if (isTeacherRole.value) {
      const teacherKemuTypes = userStore.userInfo?.kemuTypes;
      if (teacherKemuTypes) {
        formData.value.kemuTypes = teacherKemuTypes;
      }
    }
  }
  
  // 加载试卷列表
  loadExamPaperList();
};

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    examName: "",
    kemuTypes: null,
    teacherId: null,
    examPaperId: null,
    examDate: "",
    startTimeStr: "",
    duration: 90,
    passingScore: 0,
    startTime: null,
    endTime: null,
    allowScreenSwitch: 0,
    maxScreenSwitch: 3,
    allowCopyPaste: 0,
    optionShuffle: 0,
    examPassword: "",
    showAnswerAfterSubmit: 0,
    allowRetake: 0,
    maxRetakeCount: 0,
    status: 0,
  };
  selectedPaperScore.value = null;
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 关闭弹窗
const handleClose = () => {
  resetForm();
};

// 试卷选择变化处理
const handleExamPaperChange = (examPaperId) => {
  loadExamPaperScore(examPaperId);
};

// 加载试卷列表（根据科目过滤）
const loadExamPaperList = async () => {
  try {
    const params = {};
    if (isTeacherRole.value) {
      const teacherKemuTypes = userStore.userInfo?.kemuTypes || formData.value.kemuTypes;
      if (teacherKemuTypes) {
        params.kemuTypes = teacherKemuTypes;
      }
    } else if (formData.value.kemuTypes) {
      params.kemuTypes = formData.value.kemuTypes;
    }
    
    const result = await request.get("examPaper/loadAll", { params });
    if (result.code === 200) {
      examPaperList.value = result.data || [];
    }
  } catch (error) {
    console.error("加载试卷列表失败:", error);
  }
};

// 加载试卷分数
const loadExamPaperScore = async (examPaperId) => {
  if (!examPaperId) {
    selectedPaperScore.value = null;
    return;
  }
  
  try {
    const result = await request.get(`examPaper/info/${examPaperId}`);
    if (result.code === 200) {
      selectedPaperScore.value = result.data.examPaperMyscore || 0;
      
      // 如果是新增模式且尚未设置及格分，自动设置为总分的60%
      if (!formData.value.id && (!formData.value.passingScore || formData.value.passingScore === 0)) {
        formData.value.passingScore = Math.round(selectedPaperScore.value * 0.6);
      }
    }
  } catch (error) {
    console.error("加载试卷分数失败:", error);
  }
};

// 提交前同步时间字段
const syncTimeFields = () => {
  if (formData.value.examDate && formData.value.startTimeStr && formData.value.duration) {
    const hours = parseInt(formData.value.startTimeStr, 10);
    const startDateTime = new Date(`${formData.value.examDate} ${String(hours).padStart(2, '0')}:00:00`);
    const endDateTime = new Date(startDateTime.getTime() + formData.value.duration * 60 * 1000);
    
    // 格式化为后端需要的 YYYY-MM-DD HH:mm:ss
    const format = (date) => {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      const h = String(date.getHours()).padStart(2, '0');
      const min = String(date.getMinutes()).padStart(2, '0');
      const s = String(date.getSeconds()).padStart(2, '0');
      return `${y}-${m}-${d} ${h}:${min}:${s}`;
    };
    
    formData.value.startTime = format(startDateTime);
    formData.value.endTime = format(endDateTime);
  }
};

// 提交表单
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      
      try {
        syncTimeFields(); // 关键步骤：计算并赋值 startTime 和 endTime
        
        const url = formData.value.id ? "examInfo/update" : "examInfo/save";
        const result = await request.post(url, formData.value);
        
        if (result.code === 200) {
          ElMessage.success(formData.value.id ? "更新成功" : "新增成功");
          visible.value = false;
          emit("handleSuccess");
        } else {
          ElMessage.error(result.msg || "操作失败");
        }
      } catch (error) {
        console.error("提交失败:", error);
        ElMessage.error("操作失败");
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 科目变化时重新加载试卷列表
const handleKemuChange = (kemuTypes) => {
  if (!isTeacherRole.value && kemuTypes) {
    console.log('[科目变化] 重新加载试卷列表，科目:', kemuTypes);
    loadExamPaperList();
    // 清空已选择的试卷
    formData.value.examPaperId = null;
    selectedPaperScore.value = null;
  }
};

// 暴露方法给父组件
defineExpose({
  init,
});

// 初始化字典
onMounted(() => {
  getKemuOptions().then((options) => {
    kemuOptions.value = options;
  });
});
</script>

<style scoped lang="scss">
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

:deep(.el-divider__text) {
  font-weight: bold;
  color: #303133;
}
</style>
