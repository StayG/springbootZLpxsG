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
            <label class="role-label">选择角色</label>
            <div class="role-cards">
              <div 
                v-for="(item, index) in roleOptions" 
                :key="index"
                class="role-card"
                :class="{ 'active': loginForm.role === item.value }"
                @click="loginForm.role = item.value"
              >
                <div class="role-icon">
                  <el-icon :size="24">
                    <component :is="item.value === 'managers' ? 'UserFilled' : 'User'" />
                  </el-icon>
                </div>
                <div class="role-name">{{ item.key }}</div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">
            登录
          </el-button>
        </el-form-item>
      </el-form>

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
import { Monitor, User, UserFilled } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import { useThemeStore } from "@/stores/theme";
import config from "@/config/config";
import request from "@/utils/request";
import menu from "@/utils/menu";
import storage from "@/utils/storage";

const router = useRouter();
const userStore = useUserStore();
const themeStore = useThemeStore();

const loginForm = ref({
  userName: "admin",
  password: "admin",
  role: "",
});

const rules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const loginFormRef = ref(null);
const tableName = ref(null);
const roleName = ref(null);

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
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const { data } = await request.post(`/${tableName.value}/login?userName=${loginForm.value.userName}&password=${loginForm.value.password}`);
        userStore.setToken(data);
        storage.set(config.CURRENT_SESSION_ROLE_KEY, loginForm.value.role);
        storage.set(config.CURRENT_SESSION_ROLE_NAME_KEY, roleName.value);
        storage.set(config.CURRENT_SESSION_TABLE_KEY, tableName.value);
        storage.set(config.CURRENT_LOGIN_NAME, loginForm.value.userName);
        storage.remove(config.NAV_KEY);

        // 等待用户信息加载完成后再跳转
        await getCurrentUser();

        ElMessage.success("登录成功");
        router.push("/");
      } catch (error) {
        console.error("登录失败:", error);
        // 登录失败时清除已设置的 token 和存储，防止脏数据
        userStore.reset();
        storage.clear();
        return; // 阻止继续执行
      }
    } else {
      ElMessage.error("请填写完整的登录信息");
    }
  });
};

const getCurrentUser = async () => {
  try {
    const { data } = await request.get(`/${tableName.value}/session`);
    userStore.setUserInfo(data);
  } catch (error) {
    console.error("获取用户信息失败:", error);
    throw error; // 重新抛出异常，让外层 catch 处理
  }
};

const roleOptions = ref([]);
const loadRoleOptions = () => {
  let menus = menu.list();
  console.log("🚀 ~ loadRoleOptions ~ menus:", menus);
  for (let i = 0; i < menus.length; i++) {
    if (menus[i].hasBackLogin === "是") {
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

onMounted(() => {
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
        background: linear-gradient(145deg, #7dd3fc, #38bdf8);
        border-radius: 12px;
        color: #ffffff;
        box-shadow: 0 4px 16px rgba(56, 189, 248, 0.32);
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
              border-color: #38bdf8;
              background: #ffffff;
            }

            &.is-focus {
              border-color: #38bdf8;
              box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
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
            margin-bottom: 8px;
            font-size: 0.9rem;
            font-weight: 500;
            color: #333;
            text-align: left;
          }

          .role-cards {
            display: flex;
            gap: 12px;
          }

          .role-card {
            flex: 1;
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: center;
            gap: 10px;
            padding: 12px 20px;
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            background: #ffffff;
            cursor: pointer;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
            min-width: 140px;

            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: linear-gradient(135deg, rgba(56, 189, 248, 0.05), rgba(37, 99, 235, 0.05));
              opacity: 0;
              transition: opacity 0.3s ease;
            }

            .role-icon {
              width: 36px;
              height: 36px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
              background: linear-gradient(135deg, #e0f2fe, #dbeafe);
              color: #38bdf8;
              flex-shrink: 0;
              transition: all 0.3s ease;
              position: relative;
              z-index: 1;

              :deep(.el-icon) {
                font-size: 20px;
              }
            }

            .role-name {
              font-size: 0.95rem;
              font-weight: 600;
              color: #606266;
              transition: all 0.3s ease;
              position: relative;
              z-index: 1;
            }

            &:hover {
              border-color: #38bdf8;
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(56, 189, 248, 0.2);

              &::before {
                opacity: 1;
              }

              .role-icon {
                background: linear-gradient(135deg, #7dd3fc, #38bdf8);
                color: #ffffff;
                transform: scale(1.05);
              }

              .role-name {
                color: #38bdf8;
              }
            }

            &.active {
              border-color: #38bdf8;
              background: linear-gradient(135deg, #38bdf8, #2563eb);
              box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);

              &::before {
                opacity: 0;
              }

              .role-icon {
                background: #ffffff;
                color: #38bdf8;
                transform: scale(1.05);
              }

              .role-name {
                color: #ffffff;
                font-weight: 700;
              }
            }
          }
        }
      }
    }

    .login-button {
      width: 100%;
      height: 48px;
      margin-top: 10px;
      background: linear-gradient(135deg, #38bdf8, #2563eb);
      border: none;
      border-radius: 6px;
      font-size: 1rem;
      font-weight: 600;
      color: #ffffff;
      letter-spacing: 1px;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);

      &:hover {
        background: linear-gradient(135deg, #7dd3fc, #3b82f6);
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
      }

      &:active {
        transform: translateY(0);
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
