<template>
  <div class="password-form-container">
    <el-form
        ref="formRef"
        :model="formData"
        label-width="100px"
        :rules="formRules"
        class="form-wrapper"
    >
      <!-- 旧密码 -->
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
            v-model="formData.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
        />
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input
            v-model="formData.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
        />
      </el-form-item>

      <!-- 确认新密码 -->
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
        />
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item class="form-actions">
        <el-button type="primary" @click="handleSubmit" class="btn-submit">
          保存修改
        </el-button>
        <el-button @click="handleReset" class="btn-reset">
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import storage from "@/utils/storage";
import config from "@/config/config";
import request from "@/utils/request";

const formRef = ref(null);
const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);

const formData = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const formRules = ref({
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [{ required: true, message: "请输入新密码", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== formData.value.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else callback();
      },
      trigger: "blur",
    },
  ],
});

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    const api = `${tableName}/updatePassword?oldPassword=${formData.value.oldPassword}&newPassword=${formData.value.newPassword}`;
    await request.post(api);
    ElMessage.success("密码修改成功，下次登录生效");
    handleReset();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "密码修改失败");
  }
};

const handleReset = () => {
  formRef.value.resetFields();
};
</script>

<style lang="scss" scoped>
.password-form-container {
  width: 100%;
  padding: 0;

  .form-wrapper {
    padding: 0;

    /* 表单标签样式 */
    :deep(.el-form-item__label) {
      font-weight: 600;
      color: #606266;
      font-size: 14px;
    }

    /* 输入框样式 */
    :deep(.el-input__wrapper) {
      min-height: 42px;
      border-radius: 8px;
      border: 1.5px solid #dcdfe6;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--personal-right-theme-color, #4f46e5);
      }
    }

    :deep(.el-input__wrapper.is-focus) {
      border-color: var(--personal-right-theme-color, #4f46e5);
      box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1), 0 2px 8px rgba(22, 119, 255, 0.15);
    }

    /* 输入框内文字 */
    :deep(.el-input__inner) {
      color: #303133;
      font-size: 14px;
    }

    /* placeholder 样式 */
    :deep(.el-input__inner::placeholder) {
      color: #c0c4cc;
    }

    /* 提交按钮 */
    .btn-submit {
      flex: 1;
      height: 46px;
      border-radius: 8px;
      font-weight: 600;
      font-size: 15px;
      background: linear-gradient(135deg, var(--personal-right-theme-color, #4f46e5), color-mix(in srgb, var(--personal-right-theme-color, #4f46e5) 80%, #4dabf7));
      border: none;
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(22, 119, 255, 0.35);
        background: linear-gradient(135deg, color-mix(in srgb, var(--personal-right-theme-color, #4f46e5) 90%, black), var(--personal-right-theme-color, #4f46e5));
      }

      &:active {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
      }
    }

    /* 重置按钮 */
    .btn-reset {
      width: 120px;
      height: 46px;
      background: transparent;
      color: var(--personal-right-theme-color, #4f46e5);
      border: 2px solid var(--personal-right-theme-color, #4f46e5);
      border-radius: 8px;
      font-weight: 600;
      font-size: 15px;
      transition: all 0.3s ease;

      &:hover {
        background: var(--personal-right-theme-color, #4f46e5);
        color: #fff;
        box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
        transform: translateY(-2px);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .form-actions {
      margin-top: 32px;
      display: flex;
      gap: 16px;

      :deep(.el-form-item__content) {
        display: flex;
        gap: 16px;
        width: 100%;
      }
    }

    /* 表单项间距 */
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }
  }
}

/* 响应式优化 */
@media (max-width: 600px) {
  .password-form-container {
    .form-wrapper {
      .form-actions {
        :deep(.el-form-item__content) {
          flex-direction: column;
        }

        .btn-submit,
        .btn-reset {
          width: 100%;
        }
      }
    }
  }
}
</style>
