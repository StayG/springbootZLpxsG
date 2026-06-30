<template>
  <div class="personal-center-page">
    <!-- 表单编辑区 -->
    <div class="form-panel">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <!-- 基本信息板块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
          <div class="section-content">
            <!-- 头像上传 -->
            <el-form-item label="头像" prop="avatar">
              <div class="avatar-upload-area">
                <div class="upload-controls">
                  <file-upload 
                    action="file/upload" 
                    :fileUrls="formData.avatar" 
                    :limit="1" 
                    :show-file-list="false" 
                    @change="handleAvatarChange" 
                  >
                    <el-button type="primary" size="small">
                      <el-icon><Camera /></el-icon>
                      更换头像
                    </el-button>
                  </file-upload>
                  <p class="upload-tip">建议尺寸：200×200像素</p>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="用户名" prop="userName">
              <el-input v-model="formData.userName" disabled />
            </el-form-item>
            
            <!-- 管理员和教师使用真实姓名 -->
            <template v-if="isManager || isTeacher">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </template>
            
            <!-- 学生使用昵称/姓名 -->
            <template v-else>
              <el-form-item label="姓名" prop="nickname">
                <el-input v-model="formData.nickname" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-select v-model="formData.gender" placeholder="请选择性别">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </template>
          </div>
        </div>

        <!-- 联系方式板块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Phone /></el-icon>
            <span>联系方式</span>
          </div>
          <div class="section-content">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </div>
        </div>

        <!-- 教师专属信息板块 -->
        <div class="form-section" v-if="isTeacher">
          <div class="section-title">
            <el-icon><Briefcase /></el-icon>
            <span>教师信息</span>
          </div>
          <div class="section-content">
            <el-form-item label="任教科目" prop="kemuTypes">
              <el-select v-model="formData.kemuTypes" placeholder="请选择任教科目">
                <el-option 
                  v-for="item in kemuOptions" 
                  :key="item.codeIndex" 
                  :label="item.indexName" 
                  :value="item.codeIndex" 
                />
              </el-select>
            </el-form-item>
            <el-form-item label="职称" prop="title">
              <el-input v-model="formData.title" placeholder="请输入职称" />
            </el-form-item>
          </div>
        </div>

        <!-- 医生专属信息板块 -->
        <div class="form-section" v-if="!isManager && !isTeacher">
          <div class="section-title">
            <el-icon><Briefcase /></el-icon>
            <span>专业信息</span>
          </div>
          <div class="section-content">
            <el-form-item label="所属科室" prop="departmentId">
              <module-select
                v-model="formData.departmentId"
                titleName="deptName"
                url="department/loadAll"
                placeholder="请选择所属科室"
              ></module-select>
            </el-form-item>
            <el-form-item label="职称" prop="title">
              <el-input v-model="formData.title" placeholder="请输入职称" />
            </el-form-item>
            <el-form-item label="从业年限" prop="experience">
              <el-input-number 
                v-model="formData.experience" 
                :min="0" 
                :max="50"
                placeholder="请输入从业年限"
              />
              <span class="field-suffix">年</span>
            </el-form-item>
            <el-form-item label="挂号费" prop="registrationFee">
              <el-input-number 
                v-model="formData.registrationFee" 
                :min="0" 
                :precision="2"
                :step="0.01"
                placeholder="请输入挂号费"
              />
              <span class="field-suffix">元</span>
            </el-form-item>
            <el-form-item label="擅长领域" prop="specialties">
              <div class="specialties-input">
                <el-tag
                  v-for="tag in specialtiesTags"
                  :key="tag"
                  closable
                  :disable-transitions="false"
                  @close="handleTagClose(tag)"
                  class="specialty-tag"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="inputVisible"
                  ref="inputRef"
                  v-model="inputValue"
                  class="tag-input"
                  size="small"
                  @keyup.enter="handleInputConfirm"
                  @blur="handleInputConfirm"
                />
                <el-button
                  v-else
                  class="button-new-tag"
                  size="small"
                  @click="showInput"
                >
                  + 添加领域
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="简介" prop="introduction">
              <el-input 
                type="textarea" 
                :rows="4" 
                v-model="formData.introduction" 
                placeholder="请输入简介" 
              />
            </el-form-item>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button type="primary" size="large" class="save-btn" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
          <el-button type="default" size="large" @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { Calendar, Clock, User, Phone, Briefcase, Check, RefreshLeft, Camera } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import storage from "@/utils/storage";
import config from "@/config/config";
import request from "@/utils/request";
import ModuleSelect from "@/components/ModuleSelect/index.vue";

const userStore = useUserStore();
const formRef = ref(null);
const formData = ref({});
const currentUserType = computed(() => storage.get(config.CURRENT_SESSION_TABLE_KEY));
const kemuOptions = ref([]);
console.log("🚀 ~ currentUserType:", currentUserType);

// 判断是否是管理员
const isManager = computed(() => currentUserType.value === "managers");

// 判断是否是教师
const isTeacher = computed(() => currentUserType.value === "teachers");

// 擅长领域标签相关
const specialtiesTags = ref([]);
const inputVisible = ref(false);
const inputValue = ref('');
const inputRef = ref(null);

// 表单验证规则
const formRules = ref({
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "邮箱格式不正确", trigger: "blur" },
  ],
  kemuTypes: [
    { required: true, message: "请选择任教科目", trigger: "change" },
  ],
  title: [
    { required: true, message: "请输入职称", trigger: "blur" },
  ],
  departmentId: [
    { required: true, message: "请选择所属科室", trigger: "blur" },
  ],
  experience: [
    { required: true, message: "请输入从业年限", trigger: "blur" },
  ],
  registrationFee: [
    { required: true, message: "请输入挂号费", trigger: "blur" },
  ],
});

// 初始化表单数据
const initFormData = async () => {
  try {
    let api;
    if (isManager.value) {
      api = "managers/session";
    } else if (isTeacher.value) {
      api = "teachers/session";
    } else {
      api = "doctor/session";
    }
    const { data } = await request.get(api);
    formData.value = data;
    
    // 初始化擅长领域标签（仅医生）
    if (!isManager.value && !isTeacher.value && data.specialties) {
      specialtiesTags.value = data.specialties.split(',').filter(tag => tag.trim() !== '');
    } else {
      specialtiesTags.value = [];
    }
    
    console.log("🚀 ~ initFormData ~ data:", data);
  } catch (error) {
    ElMessage.error("获取用户信息失败");
  }
};

// 加载科目选项
const loadKemuOptions = async () => {
  try {
    const { data } = await request.post("dictionary/list", { dicCode: "kemu_types" });
    kemuOptions.value = data || [];
  } catch (error) {
    console.error("加载科目选项失败:", error);
  }
};

// 头像上传处理
const handleAvatarChange = (fileUrls) => {
  formData.value.avatar = fileUrls;
};

// 擅长领域标签处理
const handleTagClose = (tag) => {
  specialtiesTags.value = specialtiesTags.value.filter(t => t !== tag);
  formData.value.specialties = specialtiesTags.value.join(',');
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    inputRef.value?.focus();
  });
};

const handleInputConfirm = () => {
  if (inputValue.value) {
    const trimmedValue = inputValue.value.trim();
    if (trimmedValue && !specialtiesTags.value.includes(trimmedValue)) {
      specialtiesTags.value.push(trimmedValue);
      formData.value.specialties = specialtiesTags.value.join(',');
    }
  }
  inputVisible.value = false;
  inputValue.value = '';
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    // 确保擅长领域数据同步（仅医生）
    if (!isManager.value && !isTeacher.value) {
      formData.value.specialties = specialtiesTags.value.join(',');
    }

    let api;
    if (isManager.value) {
      api = "managers/update";
    } else if (isTeacher.value) {
      api = "teachers/update";
    } else {
      api = "doctor/update";
    }
    await request.post(api, formData.value);

    // 更新store中的数据
    userStore.userInfo = formData.value;

    ElMessage.success("修改成功");
  } catch (error) {
    console.error("保存失败:", error);
  }
};

// 重置表单
const handleReset = () => {
  initFormData();
};

onMounted(() => {
  initFormData();
  loadKemuOptions();
});
</script>

<style lang="scss" scoped>
.personal-center-page {
  padding: 20px;
  min-height: calc(100vh - 140px);

  // 表单编辑区
  .form-panel {
    max-width: 1200px;
    margin: 0 auto;
    background: var(--card-bg);
    border-radius: var(--card-border-radius);
    padding: var(--card-padding);
    box-shadow: var(--card-shadow);
    border-top: var(--card-border-top);
    border-right: var(--card-border-right);
    border-bottom: var(--card-border-bottom);
    border-left: var(--card-border-left);

    .form-section {
      margin-bottom: 32px;

      &:last-of-type {
        margin-bottom: 24px;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 2px solid #f0f0f0;
        position: relative;

        &::after {
          content: '';
          position: absolute;
          bottom: -2px;
          left: 0;
          width: 60px;
          height: 2px;
          background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
        }

        .el-icon {
          font-size: 20px;
          color: #1890ff;
        }

        span {
          font-size: 1.1rem;
          font-weight: 600;
          color: #303133;
        }
      }

      .section-content {
        // 头像上传区域
        .avatar-upload-area {
          display: flex;
          align-items: center;
          gap: 20px;

          .current-avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            overflow: hidden;
            border: 3px solid #f0f0f0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            flex-shrink: 0;
            transition: all 0.3s ease;

            &:hover {
              border-color: #1890ff;
              box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
            }

            :deep(img) {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .upload-controls {
            flex: 1;

            .el-button {
              border-radius: 6px;
              
              .el-icon {
                margin-right: 4px;
              }
            }

            .upload-tip {
              margin: 8px 0 0 0;
              font-size: 12px;
              color: #909399;
              line-height: 1.5;
            }
          }
        }

        :deep(.el-form-item) {
          margin-bottom: 20px;

          .el-form-item__label {
            font-weight: 500;
            color: #606266;
          }

          .el-input,
          .el-select,
          .el-textarea {
            width: 100%;
          }

          .el-input__wrapper,
          .el-textarea__inner {
            border-radius: 8px;
            transition: all 0.3s ease;

            &:hover,
            &.is-focus {
              box-shadow: 0 0 0 1px #1890ff inset;
            }
          }
        }

        // 字段后缀（单位）
        .field-suffix {
          margin-left: 8px;
          color: #909399;
          font-size: 14px;
        }

        // 擅长领域标签输入
        .specialties-input {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          align-items: center;
          padding: 8px 12px;
          border: 1px solid #dcdfe6;
          border-radius: 8px;
          min-height: 40px;
          background: #fff;
          transition: all 0.3s ease;

          &:hover {
            border-color: #c0c4cc;
          }

          &:focus-within {
            border-color: #1890ff;
            box-shadow: 0 0 0 1px #1890ff inset;
          }

          .specialty-tag {
            background: linear-gradient(135deg, #1890ff 0%, #0066cc 100%);
            border: none;
            color: #fff;
            border-radius: 4px;
            padding: 4px 12px;
            font-size: 13px;

            :deep(.el-tag__close) {
              color: #fff;
              
              &:hover {
                background-color: rgba(255, 255, 255, 0.2);
              }
            }
          }

          .tag-input {
            width: 120px;
            
            :deep(.el-input__wrapper) {
              box-shadow: none;
              border: 1px dashed #1890ff;
            }
          }

          .button-new-tag {
            border: 1px dashed #1890ff;
            color: #1890ff;
            background: #f0f9ff;
            border-radius: 4px;
            
            &:hover {
              background: #e6f4ff;
              border-color: #0066cc;
              color: #0066cc;
            }
          }
        }
      }
    }

    .form-actions {
      display: flex;
      gap: 12px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;

      .el-button {
        padding: 12px 32px;
        border-radius: 8px;
        font-weight: 500;
      }

      .save-btn {
        background: var(--btn-primary-bg, #1890ff);
        border-width: var(--btn-primary-border-width, 1px);
        border-style: var(--btn-primary-border-style, solid);
        border-color: var(--btn-primary-border-color, #1890ff);
        border-radius: var(--btn-primary-border-radius, 8px);
        color: var(--btn-primary-color, #fff);

        &:hover {
          background: var(--btn-primary-hover-bg, #40a9ff);
          border-color: var(--btn-primary-hover-border-color, #40a9ff);
          color: var(--btn-primary-hover-color, #fff);
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
        }
      }
    }
  }
}
</style>
