<template>
  <div class="register-container">
    <el-card class="register-box">
      <!-- Logo区域 -->
      <div class="system-logo">
        <div class="logo-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <h1 class="system-title">用户注册</h1>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
        <div class="form-row">
          <el-form-item prop="userName">
            <div class="form-label">用户名</div>
            <el-input 
              v-model="registerForm.userName" 
              placeholder="请输入用户名"
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
          <el-form-item prop="nickname">
            <div class="form-label">姓名</div>
            <el-input 
              v-model="registerForm.nickname" 
              placeholder="请输入姓名"
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
        </div>
        
        <div class="form-row">
          <el-form-item prop="password">
            <div class="form-label">密码</div>
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码"
              show-password
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <div class="form-label">确认密码</div>
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请确认密码"
              show-password
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
        </div>
        
        <div class="form-row">
          <el-form-item prop="email">
            <div class="form-label">邮箱</div>
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱"
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
          <el-form-item prop="phone">
            <div class="form-label">电话</div>
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入电话"
              clearable
              class="custom-input"
            >
            </el-input>
          </el-form-item>
        </div>
        
        <div class="form-row">
          <el-form-item prop="gender">
            <div class="form-label">性别</div>
            <el-select 
              v-model="registerForm.gender" 
              placeholder="请选择性别"
              clearable
              class="custom-select"
            >
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </div>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="register-button">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <el-button text @click="goToLogin" class="login-btn">
          已有账号？立即登录
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
import request from "@/utils/request.js";
import { useThemeStore } from "@/stores/theme.js";
import { useUserStore } from "@/stores/user";
import storage from "@/utils/storage";
import config from "@/config/config";

const themeStore = useThemeStore();
const userStore = useUserStore();
const router = useRouter();

const registerForm = ref({
  userName: "",
  nickname: "",
  password: "",
  confirmPassword: "",
  email: "",
  phone: "",
  gender: "",
});

const validatePassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = {
  userName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  nickname: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validatePassword, trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] },
  ],
  phone: [
    { required: true, message: "请输入电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" },
  ],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
};

const registerFormRef = ref(null);

const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      request.post(`/users/register`, registerForm.value).then(({ data }) => {
        ElMessage.success("注册成功");
        router.push("/login");
      });
    } else {
      ElMessage.error("请填写完整的注册信息");
    }
  });
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f8f8 100%);
  position: relative;
  overflow: hidden;
  
  .register-box {
    position: relative;
    z-index: 10;
    width: 680px;
    max-width: 96vw;
    padding: 45px 40px 40px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    background: #ffffff;
    border: none;
    
    @media (max-width: 768px) {
      width: 420px;
      padding: 40px 30px 30px;
    }
    
    @media (max-width: 600px) {
      padding: 40px 30px 30px;
      border-radius: 12px;
    }
    
    // Logo区域
    .system-logo {
      text-align: center;
      margin-bottom: 35px;
      
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
      .form-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 16px;
        
        @media (max-width: 768px) {
          grid-template-columns: 1fr;
          gap: 0;
        }
      }
      
      .el-form-item {
        margin-bottom: 18px;
        
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
            min-height: 44px;
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
        
        .custom-select {
          width: 100%;
          
          :deep(.el-input__wrapper) {
            border-radius: 6px;
            border: 1px solid #e0e0e0;
            box-shadow: none;
            background: #ffffff;
            min-height: 44px;
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
          }
        }
      }
    }
    
    .register-button {
      width: 100%;
      height: 46px;
      margin-top: 8px;
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
    
    .register-footer {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 20px;
      
      .login-btn {
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
