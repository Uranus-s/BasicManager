<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-form-container">
        <div class="logo-container">
          <h2 class="welcome-text">创建账号</h2>
          <h3 class="system-title">加入我们的平台</h3>
        </div>

        <el-form
          ref="registerForm"
          class="register-form"
          :model="form"
          :rules="registerRules"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="form.username"
              v-focus
              placeholder="请输入用户名"
              type="text"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="phone">
            <el-input
              v-model.trim="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
              show-word-limit
              type="text"
            >
              <template #prefix>
                <el-icon><Cellphone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model.trim="form.password"
              placeholder="设置密码"
              type="password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="agreement">
            <el-checkbox v-model="agreeTerms">我已阅读并同意</el-checkbox>
            <a href="javascript:;" class="terms-link">服务条款和隐私政策</a>
          </div>

          <el-button
            class="register-btn"
            type="primary"
            :loading="loading"
            :disabled="!agreeTerms"
            @click.prevent="handleRegister"
          >
            立即注册
          </el-button>

          <div class="login-link">
            <span>已有账号?</span>
            <router-link to="/login" class="back-to-login">
              返回登录
            </router-link>
          </div>
        </el-form>
      </div>

      <div class="register-image">
        <div class="overlay">
          <h2 class="slogan">开启您的管理之旅</h2>
          <p class="description">注册账号，体验全新的管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, toRefs } from "vue";
import { isPassword, isPhone } from "@/utils/validate";
import { register } from "@/api/user";
import { ElMessage } from "element-plus";
import { User, Cellphone, Lock } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";

// 聚焦指令
const vFocus = {
  mounted: (el) => el.querySelector("input").focus(),
};

// 响应式状态
const state = reactive({
  form: {},
  registerRules: {
    username: [
      { required: true, trigger: "blur", message: "请输入用户名" },
      { min: 3, trigger: "blur", message: "用户名不能少于3个字" },
      { max: 20, trigger: "blur", message: "最多不能超过20个字" },
      {
        validator: (rule, value, callback) => {
          if (value === "") {
            callback(new Error("用户名不能为空"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    phone: [
      { required: true, trigger: "blur", message: "请输入手机号码" },
      {
        validator: (rule, value, callback) => {
          if (!isPhone(value)) {
            callback(new Error("请输入正确的手机号"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
    password: [
      { required: true, trigger: "blur", message: "请输入密码" },
      {
        validator: (rule, value, callback) => {
          if (!isPassword(value)) {
            callback(new Error("密码不能少于6位"));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ],
  },
  loading: false,
});

const registerForm = ref(null);
const agreeTerms = ref(false);
const router = useRouter();

// 处理注册
const handleRegister = () => {
  registerForm.value?.validate(async (valid) => {
    if (valid) {
      const param = {
        username: state.form.username,
        phone: state.form.phone,
        password: state.form.password,
      };
      try {
        state.loading = true;
        const { message, msg } = await register(param);
        ElMessage.success(message || msg || "注册成功");
        // 注册成功后跳转到登录页
        setTimeout(() => {
          router.push("/login");
        }, 1500);
      } catch (error) {
        ElMessage.error(error.message || "注册失败");
      } finally {
        state.loading = false;
      }
    }
  });
};

// 暴露响应式变量
const { form, registerRules, loading } = toRefs(state);
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.register-box {
  width: 80%;
  max-width: 1000px;
  height: 700px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  background-color: #fff;
}

.register-form-container {
  width: 50%;
  padding: 50px;
  display: flex;
  flex-direction: column;
}

.logo-container {
  margin-bottom: 30px;
  text-align: center;

  .welcome-text {
    font-size: 28px;
    color: #333;
    margin-bottom: 10px;
    font-weight: 600;
  }

  .system-title {
    font-size: 18px;
    color: #666;
    font-weight: 400;
  }
}

.register-form {
  flex: 1;

  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input {
    height: 50px;

    :deep(.el-input__wrapper) {
      padding-left: 15px;
      box-shadow: 0 0 0 1px #dcdfe6 inset;
    }

    :deep(.el-input__prefix) {
      color: #909399;
      font-size: 18px;
    }
  }
}

.agreement {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;

  .terms-link {
    color: #409eff;
    text-decoration: none;
    margin-left: 5px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.register-btn {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #409eff 0%, #007aff 100%);
  border: none;

  &:hover {
    background: linear-gradient(90deg, #007aff 0%, #409eff 100%);
  }

  &:disabled {
    background: #a0cfff;
  }
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;

  .back-to-login {
    color: #409eff;
    text-decoration: none;
    margin-left: 5px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.register-image {
  width: 50%;
  position: relative;
  background: url("~@/assets/login_images/background.jpg") center center
    no-repeat;
  background-size: cover;

  .overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 40px;

    .slogan {
      color: #fff;
      font-size: 32px;
      font-weight: 600;
      margin-bottom: 20px;
      text-align: center;
    }

    .description {
      color: rgba(255, 255, 255, 0.9);
      font-size: 16px;
      text-align: center;
    }
  }
}

// 响应式设计
@media screen and (max-width: 992px) {
  .register-box {
    width: 100%;
    max-width: 100%;
    flex-direction: column;
    height: auto;
    max-height: 90vh;
    overflow-y: auto;
  }

  .register-form-container,
  .register-image {
    width: 100%;
  }

  .register-image {
    height: 200px;
    order: -1;
  }
}

@media screen and (max-width: 576px) {
  .register-container {
    padding: 0;
    height: 100%;
    background: #fff;
  }

  .register-box {
    width: 100%;
    max-width: 100%;
    height: 100%;
    border-radius: 0;
    box-shadow: none;
  }

  .register-form-container {
    padding: 20px;
    width: 100%;
    box-sizing: border-box;
  }

  .register-form {
    .el-form-item {
      margin-bottom: 15px;
      width: 100%;
    }

    :deep(.el-input) {
      width: 100%;

      .el-input__wrapper {
        width: 100%;
        box-sizing: border-box;
      }
    }
  }

  .logo-container {
    margin-bottom: 20px;

    .welcome-text {
      font-size: 24px;
    }

    .system-title {
      font-size: 16px;
    }
  }

  .register-btn {
    height: 45px;
    font-size: 15px;
  }

  .agreement {
    font-size: 12px;
  }
}

// 添加额外的小屏幕适配
@media screen and (max-width: 375px) {
  .register-form-container {
    padding: 15px 10px;
  }

  .register-image {
    height: 150px;
  }

}
</style>
