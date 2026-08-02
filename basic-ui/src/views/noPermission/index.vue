<template>
  <div class="no-permission-page">
    <div class="no-permission-panel">
      <el-icon class="panel-icon">
        <Lock />
      </el-icon>
      <h1>暂无访问权限</h1>
      <p>
        当前账号还没有配置任何菜单或操作权限，请联系管理员或相关负责人完成权限配置后再访问系统。
      </p>
      <div class="panel-actions">
        <el-button type="primary" @click="handleRetry">重新加载权限</el-button>
        <el-button @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { Lock } from "@element-plus/icons-vue";

export default {
  name: "NoPermission",
  components: {
    Lock,
  },
  methods: {
    handleRetry() {
      window.location.reload();
    },
    handleLogout() {
      this.$store.dispatch("user/resetAccessToken").then(() => {
        this.$router.replace("/login");
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.no-permission-page {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  padding: 24px;
  overflow: hidden;
  background: #f4f7fb;
}

.no-permission-panel {
  width: min(480px, 100%);
  padding: 36px 32px;
  text-align: center;
  background: #ffffff;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.panel-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 18px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 50%;
  font-size: 32px;
}

h1 {
  margin: 0 0 12px;
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
}

p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.8;
}

.panel-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

@media (max-width: 520px) {
  .no-permission-panel {
    padding: 30px 22px;
  }

  .panel-actions {
    flex-direction: column;
  }
}
</style>
