<template>
  <div class="reset-password-container">
    <el-card class="reset-password-box">
      <!-- Logo 区域 -->
      <div class="system-logo">
        <div class="logo-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <h1 class="system-title">重置密码</h1>
      </div>

      <el-form :model="resetForm" :rules="rules" ref="resetFormRef">
        <el-form-item prop="userName">
          <div class="form-label">用户名</div>
          <el-input v-model="resetForm.userName" placeholder="请输入您的用户名" clearable class="custom-input">
          </el-input>
        </el-form-item>

        <el-form-item prop="newPassword">
          <div class="form-label">新密码</div>
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password clearable
            class="custom-input">
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <div class="form-label">确认密码</div>
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password clearable
            class="custom-input">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleResetPassword" :loading="isSubmitting" class="reset-button">
            确定重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="reset-footer">
        <el-button text @click="goToLogin" class="back-btn">
          返回登录
        </el-button>
      </div>

      <!-- 版权信息 -->
      <div class="copyright">
        © 2026 在线考试系统 v1.0.0
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Monitor } from "@element-plus/icons-vue";
import request from "@/utils/request";
import menu from "@/utils/menu";
import { useThemeStore } from "@/stores/theme.js";
import { useUserStore } from "@/stores/user";
import storage from "@/utils/storage";
import config from "@/config/config";

const themeStore = useThemeStore();
const userStore = useUserStore();
const router = useRouter();

const resetForm = ref({
  userName: "",
  newPassword: "",
  confirmPassword: "",
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== resetForm.value.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于 6 位", trigger: "blur" }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: "blur" }],
};

const resetFormRef = ref(null);
const isSubmitting = ref(false);

const handleResetPassword = async () => {
  if (!resetFormRef.value || isSubmitting.value) return;

  const valid = await resetFormRef.value.validate().catch(() => false);

  if (!valid) {
    ElMessage.error("请填写完整的信息");
    return;
  }

  isSubmitting.value = true;

  try {
    let menus = menu.list();
    let targetTable = null;
    for (let i = 0; i < menus.length; i++) {
      if (menus[i].hasFrontLogin === "是") {
        targetTable = menus[i].tableName;
        break;
      }
    }

    if (!targetTable) {
      ElMessage.error("未找到可用的用户表");
      isSubmitting.value = false;
      return;
    }

    const response = await request.post(`/${targetTable}/resetPassword`, {
      userName: resetForm.value.userName,
      password: resetForm.value.newPassword
    });

    console.log("密码重置响应:", response);

    if (response.code === 0 || response.code === 200) {
      ElMessage({
        message: "密码重置成功，请登录",
        type: "success",
        duration: 2000,
      });
      // 跳转到登录页面
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } else {
      ElMessage.error(response.msg || "密码重置失败");
      isSubmitting.value = false;
    }
  } catch (error) {
    console.error("密码重置失败:", error);
    ElMessage.error("密码重置失败，请检查用户名是否正确或联系管理员");
    isSubmitting.value = false;
  }
};

const goToLogin = () => {
  router.push("/login");
};

// 检查是否已登录，如果已登录则跳转到首页
const checkLoginStatus = async () => {
  const token = userStore.getToken();
  const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
  
  if (token && tableName) {
    try {
      // 验证token是否有效
      await request.get(`/${tableName}/session`);
      // token有效，跳转到首页
      router.replace("/");
    } catch (error) {
      // token无效，清除登录信息
      userStore.reset();
      storage.clear();
    }
  }
};

onMounted(() => {
  checkLoginStatus();
});
</script>

<style lang="scss" scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f8f8 100%);
  position: relative;
  overflow: hidden;

  .reset-password-box {
    position: relative;
    z-index: 10;
    width: 420px;
    max-width: 96vw;
    padding: 50px 40px 40px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    background: #ffffff;
    border: none;

    @media (max-width: 600px) {
      padding: 40px 30px 30px;
      border-radius: 12px;
    }

    .system-logo {
      text-align: center;
      margin-bottom: 40px;

      .logo-icon {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        margin: 0 auto 16px;
        background: #4f46e5;
        border-radius: 12px;
        color: #ffffff;
      }

      .system-title {
        margin: 0;
        font-size: 1.6rem;
        font-weight: 600;
        color: #2c3e50;
        letter-spacing: 1px;

        @media (max-width: 600px) {
          font-size: 1.4rem;
        }
      }
    }

    .el-form {
      .el-form-item {
        margin-bottom: 24px;

        .form-label {
          margin-bottom: 8px;
          font-size: 0.9rem;
          font-weight: 500;
          color: #333;
        }

        .custom-input {
          :deep(.el-input__wrapper) {
            border-radius: 6px;
            border: 1px solid #e0e0e0;
            box-shadow: none;
            background: #ffffff;
            min-height: 48px;
            padding: 0 15px;
            transition: all 0.3s ease;

            &:hover {
              border-color: #4f46e5;
            }

            &.is-focus {
              border-color: #4f46e5;
              box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
            }
          }

          :deep(.el-input__inner) {
            color: #333;
            font-size: 0.95rem;

            &::placeholder {
              color: #bbb;
            }
          }
        }
      }
    }

    .reset-button {
      width: 100%;
      height: 48px;
      margin-top: 10px;
      background: #4f46e5;
      border: none;
      border-radius: 6px;
      font-size: 1rem;
      font-weight: 600;
      color: #ffffff;
      letter-spacing: 1px;
      transition: all 0.3s ease;

      &:hover {
        background: #4338ca;
      }

      &:active {
        transform: translateY(1px);
      }
    }

    .reset-footer {
      display: flex;
      justify-content: center;
      margin-top: 24px;

      .back-btn {
        font-size: 0.9rem;
        color: #4f46e5;
        padding: 0;
        height: auto;

        &:hover {
          color: #4338ca;
          background: transparent;
        }
      }
    }

    .copyright {
      margin-top: 24px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
      text-align: center;
      font-size: 0.85rem;
      color: #999;
    }
  }
}
</style>
