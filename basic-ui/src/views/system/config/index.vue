<template>
  <main class="config-page" aria-labelledby="config-page-title">
    <header class="page-header">
      <div>
        <h1 id="config-page-title">系统设置</h1>
        <p>集中维护系统展示信息与登录凭证策略。</p>
      </div>
      <el-tag v-if="!canEdit" type="info" effect="plain">只读</el-tag>
    </header>

    <div class="settings-shell">
      <nav class="settings-nav" aria-label="设置分类">
        <button
          v-for="item in sections"
          :key="item.key"
          :class="['nav-item', { active: activeSection === item.key }]"
          :aria-current="activeSection === item.key ? 'page' : undefined"
          type="button"
          @click="switchSection(item.key)"
        >
          <el-icon aria-hidden="true"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <span v-if="isSectionDirty(item.key)" class="dirty-indicator">未保存</span>
        </button>
      </nav>

      <section v-loading="loading" class="settings-content">
        <div v-if="loadFailed" class="load-error">
          <el-empty description="系统设置加载失败">
            <el-button type="primary" @click="loadSettings">重新加载</el-button>
          </el-empty>
        </div>

        <template v-else-if="activeSection === 'basic'">
          <div class="section-heading">
            <div>
              <h2>基础设置</h2>
              <p>系统名称将显示在登录页、导航区域和浏览器页签。</p>
            </div>
          </div>

          <el-form
            ref="basicFormRef"
            class="settings-form"
            label-position="top"
            :model="basicForm"
            :rules="basicRules"
          >
            <el-form-item label="系统名称" prop="systemName">
              <el-input
                v-model.trim="basicForm.systemName"
                :disabled="!canEdit"
                maxlength="50"
                show-word-limit
                placeholder="请输入系统名称"
              />
            </el-form-item>
          </el-form>

          <div class="section-actions">
            <el-button
              type="primary"
              :disabled="!canEdit || !basicDirty || !basicValid"
              :loading="basicSaving"
              @click="saveBasicSettings"
            >
              <el-icon><Check /></el-icon>
              保存基础设置
            </el-button>
          </div>
        </template>

        <template v-else>
          <div class="section-heading">
            <div>
              <h2>账号安全</h2>
              <p>设置新签发登录凭证的有效时长。</p>
            </div>
          </div>

          <el-form
            ref="securityFormRef"
            class="settings-form"
            label-position="top"
            :model="securityForm"
            :rules="securityRules"
          >
            <el-form-item label="Token 有效期" prop="tokenExpireHours">
              <div class="number-field">
                <el-input-number
                  v-model="securityForm.tokenExpireHours"
                  :disabled="!canEdit"
                  :min="1"
                  :max="168"
                  :step="1"
                  controls-position="right"
                />
                <span>小时</span>
              </div>
              <p class="field-help">允许范围为 1 至 168 小时，仅影响之后签发的登录凭证。</p>
            </el-form-item>
          </el-form>

          <div class="section-actions">
            <el-button
              type="primary"
              :disabled="!canEdit || !securityDirty || !securityValid"
              :loading="securitySaving"
              @click="saveSecuritySettings"
            >
              <el-icon><Check /></el-icon>
              保存账号安全设置
            </el-button>
          </div>
        </template>
      </section>
    </div>
  </main>
</template>

<script>
import { Check, Lock, Setting } from "@element-plus/icons-vue";
import {
  getSystemSettings,
  updateBasicSettings,
  updateSecuritySettings,
} from "@/api/system/config";
import getPageTitle from "@/utils/pageTitle";
import {
  canManage,
  hasSettingChanges,
  isBasicSettingsValid,
  isSecuritySettingsValid,
} from "@/utils/systemSettings";

const defaultBasicForm = () => ({ systemName: "" });
const defaultSecurityForm = () => ({ tokenExpireHours: 24 });

export default {
  name: "SystemConfig",
  components: { Check, Lock, Setting },
  data() {
    return {
      sections: [
        { key: "basic", label: "基础设置", icon: "Setting" },
        { key: "security", label: "账号安全", icon: "Lock" },
      ],
      activeSection: "basic",
      loading: false,
      loadFailed: false,
      basicSaving: false,
      securitySaving: false,
      basicForm: defaultBasicForm(),
      securityForm: defaultSecurityForm(),
      savedBasic: defaultBasicForm(),
      savedSecurity: defaultSecurityForm(),
      basicRules: {
        systemName: [
          { required: true, message: "请输入系统名称", trigger: "blur" },
          { max: 50, message: "系统名称不能超过 50 个字符", trigger: "blur" },
        ],
      },
      securityRules: {
        tokenExpireHours: [
          { required: true, message: "请输入 Token 有效期", trigger: "change" },
          {
            type: "number",
            min: 1,
            max: 168,
            message: "Token 有效期必须在 1 至 168 小时之间",
            trigger: "change",
          },
        ],
      },
    };
  },
  computed: {
    canEdit() {
      return canManage(
        this.$store.getters["user/permissions"],
        this.$store.getters["user/roles"],
        "system:config:edit"
      );
    },
    basicDirty() {
      return hasSettingChanges(this.savedBasic, this.basicForm);
    },
    basicValid() {
      return isBasicSettingsValid(this.basicForm.systemName);
    },
    securityDirty() {
      return hasSettingChanges(this.savedSecurity, this.securityForm);
    },
    securityValid() {
      return isSecuritySettingsValid(this.securityForm.tokenExpireHours);
    },
    hasUnsavedChanges() {
      return this.basicDirty || this.securityDirty;
    },
  },
  created() {
    this.loadSettings();
    window.addEventListener("beforeunload", this.handleBeforeUnload);
  },
  beforeUnmount() {
    window.removeEventListener("beforeunload", this.handleBeforeUnload);
  },
  beforeRouteLeave(to, from, next) {
    if (!this.hasUnsavedChanges) {
      next();
      return;
    }

    this.confirmDiscard().then(() => next()).catch(() => next(false));
  },
  methods: {
    async loadSettings() {
      this.loading = true;
      this.loadFailed = false;
      try {
        const { data } = await getSystemSettings();
        const basic = { systemName: data?.systemName || "" };
        const security = {
          tokenExpireHours: Number(data?.tokenExpireHours) || 24,
        };
        this.basicForm = { ...basic };
        this.savedBasic = { ...basic };
        this.securityForm = { ...security };
        this.savedSecurity = { ...security };
      } catch (error) {
        this.loadFailed = true;
      } finally {
        this.loading = false;
      }
    },
    async switchSection(section) {
      if (section === this.activeSection) return;

      if (this.isSectionDirty(this.activeSection)) {
        try {
          await this.confirmDiscard();
          this.restoreSection(this.activeSection);
        } catch (error) {
          return;
        }
      }
      this.activeSection = section;
    },
    isSectionDirty(section) {
      return section === "basic" ? this.basicDirty : this.securityDirty;
    },
    restoreSection(section) {
      if (section === "basic") {
        this.basicForm = { ...this.savedBasic };
      } else {
        this.securityForm = { ...this.savedSecurity };
      }
    },
    confirmDiscard() {
      return this.$confirm("当前分类有未保存的修改，确认放弃吗？", "未保存的修改", {
        confirmButtonText: "放弃修改",
        cancelButtonText: "继续编辑",
        type: "warning",
      });
    },
    handleBeforeUnload(event) {
      if (!this.hasUnsavedChanges) return;
      event.preventDefault();
      event.returnValue = "";
    },
    async saveBasicSettings() {
      if (!this.canEdit || !this.basicDirty) return;
      const valid = await this.$refs.basicFormRef.validate().catch(() => false);
      if (!valid) return;

      this.basicSaving = true;
      try {
        const systemName = this.basicForm.systemName.trim();
        await updateBasicSettings({ systemName });
        this.basicForm.systemName = systemName;
        this.savedBasic = { ...this.basicForm };
        this.$store.commit("settings/setSystemName", systemName);
        document.title = getPageTitle(this.$route.meta.title);
        this.$message.success("基础设置已保存");
      } finally {
        this.basicSaving = false;
      }
    },
    async saveSecuritySettings() {
      if (!this.canEdit || !this.securityDirty) return;
      const valid = await this.$refs.securityFormRef.validate().catch(() => false);
      if (!valid) return;

      this.securitySaving = true;
      try {
        await updateSecuritySettings({
          tokenExpireHours: this.securityForm.tokenExpireHours,
        });
        this.savedSecurity = { ...this.securityForm };
        this.$message.success("账号安全设置已保存，将应用于新签发的登录凭证");
      } finally {
        this.securitySaving = false;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.config-page {
  min-height: $base-app-main-height;
  padding: 24px;
  color: #303133;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  max-width: 1120px;
  margin: 0 auto 16px;

  h1 {
    margin: 0;
    font-size: 24px;
    line-height: 32px;
    font-weight: 600;
    letter-spacing: 0;
  }

  p {
    margin: 6px 0 0;
    color: #606266;
    line-height: 22px;
  }
}

.settings-shell {
  display: grid;
  grid-template-columns: 216px minmax(0, 1fr);
  max-width: 1120px;
  min-height: 520px;
  margin: 0 auto;
  overflow: hidden;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
}

.settings-nav {
  padding: 12px;
  background: #fafafa;
  border-right: 1px solid #e4e7ed;
}

.nav-item {
  position: relative;
  display: flex;
  width: 100%;
  min-height: 44px;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  color: #606266;
  font: inherit;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 4px;
  transition: color 180ms ease, background-color 180ms ease;

  & + & {
    margin-top: 4px;
  }

  &:hover {
    color: #303133;
    background: #ebeef5;
  }

  &:focus-visible {
    outline: 2px solid var(--el-color-primary);
    outline-offset: 2px;
  }

  &.active {
    color: var(--el-color-primary);
    font-weight: 600;
    background: var(--el-color-primary-light-9);
  }
}

.dirty-indicator {
  margin-left: auto;
  color: #b54708;
  font-size: 12px;
  font-weight: 500;
}

.settings-content {
  min-width: 0;
  padding: 32px 40px;
}

.section-heading {
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;

  h2 {
    margin: 0;
    font-size: 20px;
    line-height: 28px;
    font-weight: 600;
    letter-spacing: 0;
  }

  p {
    margin: 6px 0 0;
    color: #606266;
    line-height: 22px;
  }
}

.settings-form {
  max-width: 520px;
  padding-top: 28px;
}

.number-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-help {
  width: 100%;
  margin: 8px 0 0;
  color: #606266;
  font-size: 13px;
  line-height: 20px;
}

.section-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.load-error {
  display: grid;
  min-height: 420px;
  place-items: center;
}

@media (max-width: 768px) {
  .config-page {
    padding: 16px;
  }

  .settings-shell {
    display: block;
    min-height: 0;
  }

  .settings-nav {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    border-right: 0;
    border-bottom: 1px solid #e4e7ed;
  }

  .nav-item {
    width: auto;
    min-width: max-content;
    margin-top: 0 !important;
  }

  .settings-content {
    padding: 24px 20px;
  }

  .section-actions {
    justify-content: stretch;

    .el-button {
      width: 100%;
      min-height: 44px;
    }
  }
}

@media (prefers-reduced-motion: reduce) {
  .nav-item {
    transition: none;
  }
}
</style>
