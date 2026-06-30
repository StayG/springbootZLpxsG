<template>
  <div class="login-container">
    <el-card class="login-box">
      <!-- Logo区域 -->
      <div class="system-logo">
        <div class="logo-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <h1 class="system-title">在线考试系统</h1>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="userName">
          <div class="form-label">用户名</div>
          <el-input v-model="loginForm.userName" placeholder="请输入用户名" clearable class="custom-input">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <div class="form-label">密码</div>
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password clearable
            class="custom-input">
          </el-input>
        </el-form-item>
        <el-form-item v-if="roleOptions.length > 1" prop="role">
          <div class="role-selector">
            <label class="role-label">选择角色：</label>
            <el-radio-group v-model="loginForm.role">
              <el-radio v-for="(item, index) in roleOptions" :value="item.value" :key="index">
                {{ item.key }}
              </el-radio>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-button text @click="goToRegister" class="link-btn">
          注册账号
        </el-button>
        <span class="divider">|</span>
        <el-button text @click="goToResetPassword" class="link-btn">
          忘记密码
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
import { ref, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElNotification } from "element-plus";
import { Monitor } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import config from "@/config/config";
import request from "@/utils/request";
import menu from "@/utils/menu";
import storage from "@/utils/storage";
import { useThemeStore } from "@/stores/theme.js";


const themeStore = useThemeStore();
const router = useRouter();
const loginForm = ref({
  userName: "",
  password: "",
  role: "",
});

const rules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const loginFormRef = ref(null);
const tableName = ref(null);
const roleName = ref(null);
const userStore = useUserStore();
const handleLogin = () => {
  console.log("🚀 ~ handleLogin ~ roleOptions.value:", roleOptions.value);
  if (roleOptions.value.length > 1) {
    if (!loginForm.value.role) {
      ElMessage.error("请选择角色");
      return;
    } else {
      let menus = menu.list();
      for (let i = 0; i < menus.length; i++) {
        if (menus[i].tableName === loginForm.value.role) {
          tableName.value = menus[i].tableName;
          roleName.value = menus[i].roleName;
        }
      }
    }
  } else {
    console.log('------->', roleOptions.value)
    loginForm.value.role = roleOptions.value[0].value;
    tableName.value = roleOptions.value[0].value;
    roleName.value = roleOptions.value[0].key;
  }
  loginFormRef.value.validate((valid) => {
    if (valid) {
      request.post(`/${tableName.value}/login?userName=${loginForm.value.userName}&password=${loginForm.value.password}`).then(({ data }) => {
        userStore.setToken(data);
        getCurrentUser();
        storage.set(config.CURRENT_SESSION_ROLE_KEY, loginForm.value.role);
        storage.set(config.CURRENT_SESSION_ROLE_NAME_KEY, roleName.value);
        storage.set(config.CURRENT_SESSION_TABLE_KEY, tableName.value);
        storage.set(config.CURRENT_LOGIN_NAME, loginForm.value.userName);
        storage.remove(config.NAV_KEY);
        ElMessage.success("登录成功");
        router.push("/");
      }).catch((error) => {
        console.error("登录失败:", error);
        // 登录失败时清除已设置的 token 和存储
        userStore.reset();
        storage.clear();
      });
    } else {
      ElMessage.error("请填写完整的登录信息");
    }
  });
};

const getCurrentUser = () => {
  return request.get(`/${tableName.value}/session`).then(({ data }) => {
    userStore.setUserInfo(data);
  });
};

const goToRegister = () => {
  router.push("/register");
};

const goToResetPassword = () => {
  router.push("/reset-password-page");
};

const roleOptions = ref([]);
const loadRoleOptions = () => {
  let menus = menu.list();
  console.log("🚀 ~ loadRoleOptions ~ menus:", menus);
  for (let i = 0; i < menus.length; i++) {
    // 仅加载学生角色，排除教师和管理员
    if (menus[i].hasFrontLogin === "是" && menus[i].tableName === "users") {
      let menuItem = {};
      menuItem["key"] = menus[i].roleName;
      menuItem["value"] = menus[i].tableName;
      roleOptions.value.push(menuItem);
    }
  }
  
  // 如果只有一个角色选项，自动选中
  if (roleOptions.value.length === 1) {
    loginForm.value.role = roleOptions.value[0].value;
  }
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
  loadRoleOptions();
});
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f8f8 100%);
  position: relative;
  overflow: hidden;

  .login-box {
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

    // Logo区域
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
              border-color: #409eff;
            }

            &.is-focus {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
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

        .role-selector {
          .role-label {
            display: block;
            margin-bottom: 12px;
            font-size: 0.95rem;
            font-weight: 600;
            color: #555;
          }

          .el-radio-group {
            width: 100%;
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
          }

          .el-radio {
            margin-right: 0;
            padding: 10px 20px;
            border: 1px solid #dcdfe6;
            border-radius: 8px;
            background: #f5f7fa;
            transition: all 0.3s ease;

            &:hover {
              border-color: #409eff;
              background: #ffffff;
            }

            :deep(.el-radio__input.is-checked) {
              .el-radio__inner {
                border-color: #409eff;
                background: #409eff;
              }

              &~.el-radio__label {
                color: #409eff;
              }
            }

            :deep(.el-radio__label) {
              color: #606266;
              font-weight: 600;
              padding-left: 8px;
            }
          }
        }
      }
    }

    .login-button {
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

    .login-footer {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 24px;
      gap: 8px;

      .link-btn {
        font-size: 0.9rem;
        color: #4f46e5;
        padding: 0;
        height: auto;

        &:hover {
          color: #4338ca;
          background: transparent;
        }
      }

      .divider {
        color: #ddd;
        font-size: 0.9rem;
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
