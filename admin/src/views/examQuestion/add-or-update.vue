<template>
  <div class="form-container">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">

      <!-- 试题名称（题干） -->
      <el-col :span="24">
        <el-form-item label="试题题干" prop="examQuestionName">
          <RichEditor 
            v-if="!readonly" 
            :initValue="formData.examQuestionName" 
            @change="(html) => formData.examQuestionName = html"
            placeholder="请输入试题题干，支持插入图片、公式等富文本内容"
          />
          <div v-else class="rich-text-preview" v-html="formData.examQuestionName"></div>
        </el-form-item>
      </el-col>

      <!-- 科目 -->
      <el-col :span="12">
        <el-form-item label="科目" prop="kemuTypes">
          <el-select v-model="formData.kemuTypes" :disabled="readonly || isTeacherRole" placeholder="请选择科目">
            <el-option v-for="item in kemuOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>

      <!-- 试题类型 -->
      <el-col :span="12">
        <el-form-item label="试题类型" prop="examQuestionTypes">
          <el-select v-model="formData.examQuestionTypes" :disabled="readonly" placeholder="请选择类型">
            <el-option v-for="item in examQuestionTypeOptions" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
      </el-col>

      <!-- 难度等级 -->
      <el-col :span="12">
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="formData.difficultyLevel" :disabled="readonly" placeholder="请选择难度等级">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
      </el-col>

      <!-- 知识点 -->
      <el-col :span="12">
        <el-form-item label="知识点" prop="knowledgePoint">
          <el-input v-model="formData.knowledgePoint" :disabled="readonly" placeholder="请输入知识点标签（如：二次函数、光合作用）" />
        </el-form-item>
      </el-col>

      <!-- 填空/简答：学生端公式快捷栏类型（与 exam_question.formula_type 一致） -->
      <el-col :span="12" v-if="formData.examQuestionTypes == 4 || formData.examQuestionTypes == 5">
        <el-form-item label="公式快捷栏" prop="formulaType">
          <el-select v-model="formData.formulaType" :disabled="readonly" placeholder="学生作答时输入框上方的 LaTeX 片段按钮">
            <el-option v-for="opt in formulaTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <div v-if="!readonly" class="form-tip" style="margin-top: 6px; color: #909399; font-size: 12px;">
            语/英/政/史多为「无」；数/物/化、生、地会随科目自动推荐，可自行修改。
          </div>
        </el-form-item>
      </el-col>

      <!-- 选项（单选、多选、判断） - 填空题(4)和简答题(5)时隐藏 -->
      <el-col :span="12" v-if="formData.examQuestionTypes >= 1 && formData.examQuestionTypes <= 3">
        <el-form-item label="选项">
          <div class="options-container">
            <!-- 选项列表 -->
            <div class="option-item" v-for="(opt, idx) in options" :key="idx">
              <div class="option-content">
                <span class="option-code">{{ opt.code }}</span>
                <span class="option-text">{{ opt.text }}</span>
              </div>
              <!-- 判断题时不显示删除按钮 -->
              <el-button style="padding: 7px;" v-if="!readonly && formData.examQuestionTypes != 3" type="danger" link
                @click="deleteOption(idx)"><el-icon>
                  <Delete />
                </el-icon></el-button>
            </div>

            <!-- 判断题说明 -->
            <div v-if="formData.examQuestionTypes == 3" class="option-tip">
              判断题固定选项：对（A）、错（B）
            </div>

            <!-- 添加选项按钮 - 判断题时不显示 -->
            <el-button v-if="!readonly && formData.examQuestionTypes != 3" type="primary" size="small" plain
              @click="handleAddOption" class="add-option-btn">
              + 添加选项
            </el-button>
          </div>
        </el-form-item>
      </el-col>

      <!-- 正确答案/参考答案 -->
      <el-col :span="12">
        <template v-if="!readonly">

          <!-- 单选答案 -->
          <el-form-item v-if="formData.examQuestionTypes == 1 && options.length > 0" label="正确答案"
            prop="examQuestionAnswer">
            <el-select v-model="formData.examQuestionAnswer" placeholder="请选择答案">
              <el-option v-for="(item, index) in options" :key="index" :label="item.code" :value="item.code" />
            </el-select>
          </el-form-item>

          <!-- 多选答案 -->
          <el-form-item v-if="formData.examQuestionTypes == 2 && options.length > 0" label="正确答案"
            prop="examQuestionAnswer">
            <el-select v-model="formData.examQuestionAnswer" multiple placeholder="请选择答案">
              <el-option v-for="(item, index) in options" :key="index" :label="item.code" :value="item.code" />
            </el-select>
          </el-form-item>

          <!-- 判断答案 - 改为下拉框 -->
          <el-form-item v-if="formData.examQuestionTypes == 3" label="正确答案" prop="examQuestionAnswer">
            <el-select v-model="formData.examQuestionAnswer" placeholder="请选择正确答案">
              <el-option label="A - 对" value="A" />
              <el-option label="B - 错" value="B" />
            </el-select>
          </el-form-item>

          <!-- 填空答案 -->
          <el-form-item v-if="formData.examQuestionTypes == 4" label="正确答案" prop="examQuestionAnswer">
            <el-input v-model="formData.examQuestionAnswer" placeholder="请输入正确答案" />
          </el-form-item>

          <!-- 简答题参考答案（主观题，选填） -->
          <el-form-item v-if="formData.examQuestionTypes == 5" label="参考答案">
            <el-input v-model="formData.examQuestionAnswer" type="textarea" :rows="3"
              placeholder="请输入参考答案（选填，主观题由教师人工批改）" />
            <div class="form-tip" style="margin-top: 8px; color: #e6a23c; font-size: 12px;">
              <el-icon>
                <WarningFilled />
              </el-icon> 简答题为人工批改题目，参考答案仅用于教师阅卷时对照
            </div>
          </el-form-item>

        </template>

        <!-- 查看模式 -->
        <template v-else>
          <el-form-item :label="formData.examQuestionTypes == 5 ? '参考答案' : '正确答案'">
            <el-input v-model="formData.examQuestionAnswer" readonly type="textarea"
              :rows="formData.examQuestionTypes == 5 ? 3 : 1" />
            <div v-if="formData.examQuestionTypes == 5" class="form-tip"
              style="margin-top: 8px; color: #e6a23c; font-size: 12px;">
              <el-icon>
                <WarningFilled />
              </el-icon> 本题为简答题，需人工批改
            </div>
          </el-form-item>
        </template>
      </el-col>

      <!-- 答案解析 -->
      <el-col :span="12">
        <el-form-item label="答案解析">
          <el-input type="textarea" v-model="formData.examQuestionAnalysis" :readonly="readonly" placeholder="请输入答案解析" />
        </el-form-item>
      </el-col>

      <!-- 功能按钮 -->
      <el-form-item v-if="!readonly">
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>

      <el-form-item v-else>
        <el-button @click="handleCancel">关闭</el-button>
      </el-form-item>

    </el-form>

    <!-- 添加选项弹窗 -->
    <el-dialog title="添加选项" v-model="addOptionDialog" width="500px">
      <el-form :model="newOptionForm" label-width="80px">
        <el-form-item label="选项内容" required>
          <el-input v-model="newOptionForm.text" placeholder="请输入选项内容" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addOptionDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddOption">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { WarningFilled } from '@element-plus/icons-vue';
import request from "@/utils/request";
import { getKemuOptions, getExamQuestionTypeOptions } from "@/utils/dictionary.js";
import storage from '@/utils/storage';
import config from '@/config/config';
import { useUserStore } from '@/stores/user';
import RichEditor from '@/components/RichEditor/index.vue'; // 引入富文本编辑器
import { suggestFormulaTypeFromKemuLabel } from "@/utils/examFormulaDefaults.js";

const props = defineProps({ readonly: Boolean });
const emit = defineEmits(["handleSuccess"]);
const formRef = ref(null);

// 获取用户信息
const userStore = useUserStore();

// 修复：从 localStorage (storage) 中读取角色信息，而不是从 userStore.userInfo
const storedRole = storage.get(config.CURRENT_SESSION_ROLE_KEY); // 如 'teachers'
const storedRoleName = storage.get(config.CURRENT_SESSION_ROLE_NAME_KEY); // 如 '教师'
const isTeacherRole = ref(
  storedRole === 'teachers' || storedRoleName === '教师'
);

console.log('[试题表单] 存储的角色:', storedRole, '角色名称:', storedRoleName, '是否教师:', isTeacherRole.value);

const formulaTypeOptions = [
  { label: "无", value: "none" },
  { label: "数学", value: "math" },
  { label: "物理", value: "physics" },
  { label: "化学", value: "chemistry" },
  { label: "生物", value: "biology" },
  { label: "地理", value: "geography" },
];

const formData = ref({
  id: null,
  examQuestionName: "",
  kemuTypes: null,
  examQuestionTypes: null,
  difficultyLevel: 2, // 默认中等难度
  knowledgePoint: "",
  formulaType: "none",
  examQuestionOptions: "[]",
  examQuestionAnswer: "",
  examQuestionAnalysis: "",
});

// 选项（存储 JSON 数组）
const options = ref([]);

// 科目选项
const kemuOptions = ref([]);

// 试题类型选项
const examQuestionTypeOptions = ref([]);

/** 编辑回填期间为 true，避免 watch 用科目推断覆盖库里的 formulaType */
const skipFormulaAutoOnce = ref(false);

const getKemuLabelByValue = (kemuVal) => {
  const hit = kemuOptions.value.find((o) => String(o.value) === String(kemuVal ?? ""));
  return hit?.label ?? "";
};

/** 填空(4)/简答(5) 时按当前科目名称刷新 formulaType */
const applyFillBlankFormulaDefault = () => {
  const type = formData.value.examQuestionTypes;
  if (type != 4 && type != 5) return;
  const label = getKemuLabelByValue(formData.value.kemuTypes);
  formData.value.formulaType = suggestFormulaTypeFromKemuLabel(label);
};

// 获取字典选项（从字典接口获取）
const getDictOptions = async () => {
  try {
    const [kemuList, typeList] = await Promise.all([
      getKemuOptions(),
      getExamQuestionTypeOptions()
    ]);
    kemuOptions.value = kemuList;
    examQuestionTypeOptions.value = typeList;
  } catch (error) {
    console.error('获取字典选项失败:', error);
  }
};

// 监听试题类型变化
watch(
  () => formData.value.examQuestionTypes,
  (newType, oldType) => {
    // 如果切换到判断题类型，自动设置对错选项
    if (newType == 3) {
      options.value = [
        { code: "A", text: "对" },
        { code: "B", text: "错" },
      ];
    }
    // 如果切换到填空题或简答题，清空选项数据和答案
    // 注意：只有当 oldType 不为 null 时才清空（即用户主动切换题型），初始化加载数据时不应清空
    else if ((newType == 4 || newType == 5) && oldType != null) {
      options.value = [];
      formData.value.examQuestionOptions = "[]";
      // 简答题的答案为选填，切换时清空
      formData.value.examQuestionAnswer = "";
    }
    if (newType != 4 && newType != 5) {
      formData.value.formulaType = "none";
    } else if (
      (newType == 4 || newType == 5) &&
      !skipFormulaAutoOnce.value &&
      oldType != 4 &&
      oldType != 5
    ) {
      // 从客观题切到填空/简答：按科目自动推荐公式栏
      applyFillBlankFormulaDefault();
    }
    // 如果从判断题切换到其他类型，清空选项（但保留用户已手动添加的自定义选项）
    else if (oldType == 3 && newType != 3 && newType != 4 && newType != 5) {
      // 只有当选项是对错的时候才清空，其他情况保持不变
      const hasTrueFalseOnly = options.value.length === 2 &&
        options.value[0].code === 'A' && options.value[0].text === '对' &&
        options.value[1].code === 'B' && options.value[1].text === '错';

      if (hasTrueFalseOnly) {
        options.value = [];
      }
    }
  }
);

// 初始化选项
watch(
  () => formData.value.examQuestionOptions,
  (json) => {
    try {
      options.value = json ? JSON.parse(json) : [];
    } catch { }
  },
  { immediate: true }
);

watch(
  () => formData.value.kemuTypes,
  () => {
    if (skipFormulaAutoOnce.value) return;
    if (formData.value.examQuestionTypes == 4 || formData.value.examQuestionTypes == 5) {
      applyFillBlankFormulaDefault();
    }
  }
);

// 添加选项弹窗
const addOptionDialog = ref(false);
const newOptionForm = ref({
  code: "",
  text: ""
});

const handleAddOption = () => {
  // 检查是否已选择试题类型
  if (!formData.value.examQuestionTypes) {
    ElMessage.warning("请先选择试题类型");
    return;
  }

  // 检查是否为判断题
  if (formData.value.examQuestionTypes == 3) {
    ElMessage.info("判断题自动提供选项：对（A）、错（B），无需手动添加");
    return;
  }

  // 检查是否为填空题或简答题
  if (formData.value.examQuestionTypes == 4 || formData.value.examQuestionTypes == 5) {
    ElMessage.info("填空题和简答题无需添加选项，请直接填写答案");
    return;
  }

  // 单选或多选题型，打开添加选项弹窗
  openAddOption();
};

const openAddOption = () => {
  // 找到下一个可用的选项代码
  const optionCodes = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  const usedCodes = options.value.map(option => option.code);
  const nextAvailableCode = optionCodes.find(code => !usedCodes.includes(code)) || '';

  newOptionForm.value = {
    code: nextAvailableCode,
    text: ""
  };
  addOptionDialog.value = true;
};

const confirmAddOption = () => {
  // 检查是否已选择选项代码和输入选项内容
  if (!newOptionForm.value.code) {
    ElMessage.warning("请选择选项代码");
    return;
  }

  if (!newOptionForm.value.text.trim()) {
    ElMessage.warning("请输入选项内容");
    return;
  }

  // 检查选项代码是否已存在
  const existingCode = options.value.find(option => option.code === newOptionForm.value.code);
  if (existingCode) {
    ElMessage.warning(`选项代码 ${newOptionForm.value.code} 已存在，请选择其他代码`);
    return;
  }

  options.value.push({
    code: newOptionForm.value.code,
    text: newOptionForm.value.text.trim()
  });

  addOptionDialog.value = false;
  ElMessage.success("添加选项成功");
};

const deleteOption = (idx) => {
  // 删除选项
  options.value.splice(idx, 1);

  // 重新排序剩余选项的代码
  const reorderOptions = () => {
    const optionCodes = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
    options.value.forEach((option, index) => {
      option.code = optionCodes[index];
    });
  };

  reorderOptions();
};

const formRules = {
  examQuestionName: [{ required: true, message: "请输入试题名称", trigger: "blur" }],
  kemuTypes: [{ required: true, message: "请选择科目", trigger: "change" }],
  examQuestionTypes: [{ required: true, message: "请选择类型", trigger: "change" }],
  difficultyLevel: [{ required: true, message: "请选择难度等级", trigger: "change" }],
  knowledgePoint: [{ required: true, message: "请输入知识点标签", trigger: "blur" }],
  examQuestionAnswer: [
    {
      validator: (rule, value, callback) => {
        const type = formData.value.examQuestionTypes;
        const opts = options.value;

        // === 简答题（类型5）：参考答案选填 ===
        if (type == 5) {
          callback();
          return;
        }

        // === 单选题（类型1）：必须选择且仅选择一个答案 ===
        if (type == 1) {
          if (!value) {
            callback(new Error("请选择正确答案"));
          } else if (typeof value !== 'string' || value.length !== 1) {
            callback(new Error("单选题只能选择一个答案"));
          } else {
            callback();
          }
          return;
        }

        // === 多选题（类型2）：必须选择至少一个答案 ===
        if (type == 2) {
          if (!value || (Array.isArray(value) && value.length === 0)) {
            callback(new Error("请至少选择一个正确答案"));
          } else {
            callback();
          }
          return;
        }

        // === 判断题（类型3）：答案必须是A或B ===
        if (type == 3) {
          if (!value) {
            callback(new Error("请选择正确答案"));
          } else if (value !== "A" && value !== "B") {
            callback(new Error("判断题答案只能是'A'或'B'"));
          } else {
            callback();
          }
          return;
        }

        // === 填空题（类型4）：答案必填 ===
        if (type == 4) {
          if (!value || (typeof value === 'string' && value.trim() === '')) {
            callback(new Error("请输入正确答案"));
          } else {
            callback();
          }
          return;
        }

        // 默认通过
        callback();
      },
      trigger: "change"
    }
  ],
};

// 初始化（编辑）
const init = (id) => {
  if (id) {
    skipFormulaAutoOnce.value = true;
    request
      .get(`examQuestion/info/${id}`)
      .then(({ data }) => {
        formData.value = data.data || data;
        if (!formData.value.formulaType) {
          formData.value.formulaType = "none";
        }
        options.value = formData.value.examQuestionOptions ? JSON.parse(formData.value.examQuestionOptions) : [];

        // 如果是多选题，将逗号分隔的字符串转换为数组用于回显
        if (formData.value.examQuestionTypes == 2 && formData.value.examQuestionAnswer) {
          formData.value.examQuestionAnswer = formData.value.examQuestionAnswer.split(',');
        }
      })
      .finally(() => {
        nextTick(() => {
          skipFormulaAutoOnce.value = false;
        });
      });
  } else {
    // 新增模式
    formData.value = {
      id: null,
      examQuestionName: "",
      kemuTypes: isTeacherRole.value ? userStore.userInfo?.kemuTypes : null,
      examQuestionTypes: null,
      difficultyLevel: 2, // 默认中等难度
      knowledgePoint: "",
      formulaType: "none",
      examQuestionOptions: "[]",
      examQuestionAnswer: "",
      examQuestionAnalysis: "",
    };
    options.value = [];

    // 教师新增时，自动填充并锁定任教科目
    if (isTeacherRole.value) {
      console.log('[试题新增] 教师角色 - 自动设置科目:', formData.value.kemuTypes);
    }

    // 教师新增且已带任教科目：若随后直接选填空/简答，需能根据科目推断公式栏
    nextTick(() => {
      if (
        (formData.value.examQuestionTypes == 4 || formData.value.examQuestionTypes == 5) &&
        formData.value.kemuTypes != null &&
        formData.value.kemuTypes !== ""
      ) {
        applyFillBlankFormulaDefault();
      }
    });
  }
};

// 提交
const handleSubmit = async () => {
  // 1. 选项数量校验
  const type = formData.value.examQuestionTypes;
  const opts = options.value;
  
  // 单选题和多选题至少需要2个选项
  if ((type == 1 || type == 2) && opts.length < 2) {
    ElMessage.warning(`${type == 1 ? '单选题' : '多选题'}至少需要添加 2 个选项`);
    return;
  }
  
  // 判断题固定为2个选项，无需校验

  await formRef.value.validate();

  formData.value.examQuestionOptions = JSON.stringify(options.value);

  // 准备提交的数据
  const submitData = { ...formData.value };

  // 如果是多选题，将数组转换为逗号分隔的字符串
  if (submitData.examQuestionTypes == 2 && Array.isArray(submitData.examQuestionAnswer)) {
    submitData.examQuestionAnswer = submitData.examQuestionAnswer.join(',');
  }

  const api = formData.value.id ? "examQuestion/update" : "examQuestion/save";

  request.post(api, submitData).then(() => {
    ElMessage.success("保存成功");
    emit("handleSuccess");
  });
};

const handleCancel = () => emit("handleSuccess");

// 初始化时获取字典选项
onMounted(() => {
  getDictOptions();
});

defineExpose({ init });
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

  &:hover {
    background: #f5f5f5;
    border-color: #d9d9d9;
  }

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

.option-tip {
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f0f9ff;
  border: 1px solid #bae7ff;
  border-radius: 4px;
  color: #0958d9;
  font-size: 13px;
  line-height: 1.5;
}

.add-option-btn {
  width: 100%;
  margin-top: 4px;
  border-style: dashed;
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

:deep(.el-select) {
  width: 100%;
}

// 富文本预览样式
.rich-text-preview {
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f7fa;
  min-height: 100px;
  line-height: 1.6;
  
  :deep(img) {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 10px 0;
  }
  
  :deep(p) {
    margin: 8px 0;
  }
}
</style>
