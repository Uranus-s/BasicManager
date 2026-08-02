<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="forgot-form-container">
        <div class="logo-container">
          <h2 class="welcome-text">重置密码</h2>
          <h3 class="system-title">请输入账号绑定信息完成密码重置</h3>
        </div>

        <el-form
          ref="forgotForm"
          class="forgot-form"
          :model="form"
          :rules="rules"
        >
          <el-form-item prop="username">
            <el-input
              v-model.trim="form.username"
              v-focus
              autocomplete="username"
              placeholder="请输入用户名"
              type="text"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="contact">
            <el-input
              v-model.trim="form.contact"
              autocomplete="email"
              placeholder="请输入绑定手机号或邮箱"
              type="text"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input
              v-model.trim="form.newPassword"
              autocomplete="new-password"
              placeholder="请输入新密码"
              show-password
              type="password"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model.trim="form.confirmPassword"
              autocomplete="new-password"
              placeholder="请再次输入新密码"
              show-password
              type="password"
              @keyup.enter="handleReset"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            class="reset-button"
            type="primary"
            :loading="loading"
            @click.prevent="handleReset"
          >
            提交重置
          </el-button>

          <div class="login-link">
            <span>想起密码了?</span>
            <router-link to="/login" class="back-to-login">
              返回登录
            </router-link>
          </div>
        </el-form>
      </div>

      <div class="forgot-image">
        <div class="overlay">
          <h2 class="slogan">安全找回访问权限</h2>
          <p class="description">使用已绑定的手机号或邮箱验证身份</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, toRefs } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Lock, Message, User } from "@element-plus/icons-vue";
import { forgotPasswordReset } from "@/api/user";
import { isPassword } from "@/utils/validate";

const vFocus = {
  mounted: (el) => el.querySelector("input").focus(),
};

const router = useRouter();
const forgotForm = ref(null);

const validateNewPassword = (rule, value, callback) => {
  if (!isPassword(value)) {
    callback(new Error("密码长度必须大于等于6位"));
    return;
  }

  callback();
};

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== state.form.newPassword) {
    callback(new Error("两次输入的密码不一致"));
    return;
  }

  callback();
};

const state = reactive({
  form: {
    username: "",
    contact: "",
    newPassword: "",
    confirmPassword: "",
  },
  rules: {
    username: [
      { required: true, trigger: "blur", message: "请输入用户名" },
      { min: 3, max: 20, trigger: "blur", message: "用户名长度为3到20位" },
    ],
    contact: [
      { required: true, trigger: "blur", message: "请输入绑定手机号或邮箱" },
    ],
    newPassword: [
      { required: true, trigger: "blur", message: "请输入新密码" },
      { validator: validateNewPassword, trigger: "blur" },
    ],
    confirmPassword: [
      { required: true, trigger: "blur", message: "请再次输入新密码" },
      { validator: validateConfirmPassword, trigger: "blur" },
    ],
  },
  loading: false,
});

const handleReset = () => {
  forgotForm.value?.validate(async (valid) => {
    if (!valid) return;

    state.loading = true;
    try {
      const { msg, message } = await forgotPasswordReset({
        username: state.form.username,
        contact: state.form.contact,
        newPassword: state.form.newPassword,
      });

      ElMessage.success(msg || message || "密码重置成功，请重新登录");
      router.push("/login");
    } catch (error) {
      ElMessage.error(error.message || "密码重置失败，请确认账号信息");
    } finally {
      state.loading = false;
    }
  });
};

const { form, rules, loading } = toRefs(state);
</script>

<style lang="scss" scoped>
.forgot-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.forgot-box {
  width: 80%;
  max-width: 1000px;
  height: 700px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  background-color: #fff;
}

.forgot-form-container {
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

.forgot-form {
  flex: 1;

  .el-form-item {
    margin-bottom: 22px;
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

.reset-button {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #409eff 0%, #007aff 100%);
  border: none;
  margin-top: 4px;

  &:hover {
    background: linear-gradient(90deg, #007aff 0%, #409eff 100%);
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

.forgot-image {
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

@media screen and (max-width: 992px) {
  .forgot-box {
    width: 100%;
    max-width: 100%;
    flex-direction: column;
    height: auto;
    max-height: 90vh;
    overflow-y: auto;
  }

  .forgot-form-container,
  .forgot-image {
    width: 100%;
  }

  .forgot-image {
    height: 200px;
    order: -1;
  }
}

@media screen and (max-width: 576px) {
  .forgot-container {
    padding: 0;
    height: 100%;
    background: #fff;
  }

  .forgot-box {
    width: 100%;
    max-width: 100%;
    height: 100%;
    border-radius: 0;
    box-shadow: none;
  }

  .forgot-form-container {
    padding: 20px;
    width: 100%;
    box-sizing: border-box;
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

  .forgot-form {
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

  .reset-button {
    height: 45px;
    font-size: 15px;
    width: 100%;
  }
}

@media screen and (max-width: 375px) {
  .forgot-form-container {
    padding: 15px 10px;
  }

  .forgot-image {
    height: 150px;
  }
}
</style>
