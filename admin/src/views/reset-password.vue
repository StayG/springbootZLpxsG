<template>
  <div class="reset-password-page">
    <!-- 表单编辑区 -->
    <div class="form-panel">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <!-- 密码修改板块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <div class="section-content">
            <!-- 旧密码 -->
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input 
                v-model="formData.oldPassword" 
                type="password" 
                placeholder="请输入旧密码" 
                show-password 
                clearable
              />
            </el-form-item>

            <!-- 新密码 -->
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="formData.newPassword" 
                type="password" 
                placeholder="请输入新密码（至少6位）" 
                show-password 
                clearable
              />
            </el-form-item>

            <!-- 确认新密码 -->
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="formData.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码" 
                show-password 
                clearable
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
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Lock, Check, RefreshLeft } from '@element-plus/icons-vue';

import storage from "@/utils/storage";
import config from "@/config/config";
import request from "@/utils/request";

const formRef = ref(null);
const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
const formData = ref({
  oldPassword: "", // 旧密码
  newPassword: "", // 新密码
  confirmPassword: "", // 确认新密码
});

// 表单验证规则
const formRules = ref({
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度至少6位字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== formData.value.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
});

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value.validate();

    // 调用修改密码接口
    const api = `${tableName}/updatePassword?oldPassword=${formData.value.oldPassword}&newPassword=${formData.value.newPassword}`;

    await request.post(api);

    // 提示成功
    ElMessage.success("密码修改成功,下次登录系统生效");

    // 清空表单
    handleReset();
  } catch (error) {
    console.error("密码修改失败:", error);
    ElMessage.error(error.response?.data?.message || "密码修改失败");
  }
};

// 重置表单
const handleReset = () => {
  formRef.value.resetFields();
};
</script>

<style lang="scss" scoped>
.reset-password-page {
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
