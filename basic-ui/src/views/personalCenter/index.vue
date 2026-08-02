<template>
  <div class="personal-center-page">
    <div class="profile-header">
      <div
        class="profile-avatar-uploader"
        :class="{ 'is-uploading': avatarUploading }"
        role="button"
        tabindex="0"
        title="更换头像"
        @click="openAvatarEditor"
        @keydown.enter.prevent="openAvatarEditor"
        @keydown.space.prevent="openAvatarEditor"
      >
        <el-avatar :size="72" :src="avatar">
          {{ avatarText }}
        </el-avatar>
        <div class="avatar-mask">
          <span>{{ avatarUploading ? "上传中" : "更换头像" }}</span>
        </div>
      </div>
      <div class="profile-title">
        <h1>{{ displayName }}</h1>
        <p>{{ username || "未设置登录账号" }}</p>
      </div>
    </div>

    <div class="profile-grid">
      <section class="profile-section">
        <div class="section-title">基础信息</div>
        <div class="info-list">
          <div class="info-row">
            <span>登录账号</span>
            <strong>{{ username || "-" }}</strong>
          </div>
          <div class="info-row">
            <span>昵称</span>
            <strong>{{ nickname || "-" }}</strong>
          </div>
          <div class="info-row">
            <span>邮箱</span>
            <strong>{{ email || "未绑定邮箱" }}</strong>
          </div>
        </div>
      </section>

      <section class="profile-section">
        <div class="section-title">权限信息</div>
        <div class="summary-grid">
          <div class="summary-item">
            <span>角色数量</span>
            <strong>{{ roles.length }}</strong>
          </div>
          <div class="summary-item">
            <span>权限数量</span>
            <strong>{{ permissions.length }}</strong>
          </div>
        </div>

        <div class="tag-group">
          <el-tag
            v-for="role in roles"
            :key="role"
            class="profile-tag"
            effect="light"
          >
            {{ role }}
          </el-tag>
          <span v-if="!roles.length" class="empty-text">未配置角色</span>
        </div>
      </section>
    </div>

    <section class="profile-section permission-section">
      <div class="section-title">权限标识</div>
      <div class="tag-group">
        <el-tag
          v-for="permission in permissions"
          :key="permission"
          class="profile-tag"
          type="info"
          effect="plain"
        >
          {{ permission }}
        </el-tag>
        <span v-if="!permissions.length" class="empty-text">
          当前账号暂无权限，请联系管理员配置。
        </span>
      </div>
    </section>

    <el-dialog
      v-model="avatarEditorVisible"
      title="编辑头像"
      width="420px"
      class="avatar-editor-dialog"
      :close-on-click-modal="!avatarUploading"
      :close-on-press-escape="!avatarUploading"
      @closed="resetAvatarEditor"
    >
      <div class="avatar-editor">
        <div class="avatar-preview">
          <el-avatar :size="128" :src="avatarPreview">
            {{ avatarText }}
          </el-avatar>
        </div>
        <div class="avatar-editor-actions">
          <el-button @click="chooseAvatarFile">选择图片</el-button>
          <input
            ref="avatarInput"
            class="avatar-input"
            type="file"
            accept="image/*"
            @change="handleAvatarChange"
          />
        </div>
        <p class="avatar-editor-tip">请选择清晰的图片作为头像。</p>
      </div>
      <template #footer>
        <el-button :disabled="avatarUploading" @click="closeAvatarEditor">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="avatarUploading"
          :disabled="!avatarFile"
          @click="saveAvatar"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage } from "element-plus";
import { mapActions, mapGetters } from "vuex";
import { uploadUserAvatar } from "@/api/user";

export default {
  name: "PersonalCenter",
  data() {
    return {
      avatarEditorVisible: false,
      avatarFile: null,
      avatarPreviewUrl: "",
      avatarUploading: false,
    };
  },
  computed: {
    ...mapGetters({
      username: "user/username",
      nickname: "user/nickname",
      email: "user/email",
      avatar: "user/avatar",
      roles: "user/roles",
      permissions: "user/permissions",
    }),
    displayName() {
      return this.nickname || this.username || "未命名用户";
    },
    avatarText() {
      return this.displayName.slice(0, 1).toUpperCase();
    },
    avatarPreview() {
      return this.avatarPreviewUrl || this.avatar;
    },
  },
  methods: {
    ...mapActions({
      refreshAvatar: "user/refreshAvatar",
    }),
    openAvatarEditor() {
      if (this.avatarUploading) return;
      this.avatarEditorVisible = true;
    },
    closeAvatarEditor() {
      if (this.avatarUploading) return;
      this.avatarEditorVisible = false;
    },
    chooseAvatarFile() {
      if (this.avatarUploading) return;
      this.$refs.avatarInput.click();
    },
    handleAvatarChange(event) {
      const [file] = event.target.files || [];
      event.target.value = "";

      if (!file) return;
      if (!file.type || !file.type.startsWith("image/")) {
        ElMessage.warning("请选择图片文件");
        return;
      }

      this.clearAvatarPreviewUrl();
      this.avatarFile = file;
      this.avatarPreviewUrl = URL.createObjectURL(file);
    },
    async saveAvatar() {
      if (!this.avatarFile) return;

      this.avatarUploading = true;
      try {
        await uploadUserAvatar(this.avatarFile);
        await this.refreshAvatar();
        ElMessage.success("头像已更新");
        this.avatarEditorVisible = false;
      } finally {
        this.avatarUploading = false;
      }
    },
    resetAvatarEditor() {
      if (this.avatarUploading) return;
      this.avatarFile = null;
      this.clearAvatarPreviewUrl();
    },
    clearAvatarPreviewUrl() {
      if (!this.avatarPreviewUrl) return;
      URL.revokeObjectURL(this.avatarPreviewUrl);
      this.avatarPreviewUrl = "";
    },
  },
  mounted() {
    this.refreshAvatar().catch((error) => {
      console.error("获取头像失败:", error);
    });
  },
  beforeUnmount() {
    this.clearAvatarPreviewUrl();
  },
};
</script>

<style lang="scss" scoped>
.personal-center-page {
  min-height: $base-app-main-height;
  padding: 20px;
  color: #1f2937;
  background: #f4f7fb;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
}

.profile-avatar-uploader {
  position: relative;
  flex: 0 0 72px;
  width: 72px;
  height: 72px;
  overflow: hidden;
  border-radius: 50%;
  cursor: pointer;

  :deep(.el-avatar) {
    display: block;
  }

  &:hover,
  &:focus-visible,
  &.is-uploading {
    .avatar-mask {
      opacity: 1;
    }
  }

  &.is-uploading {
    cursor: wait;
  }
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.2;
  text-align: center;
  background: rgba(15, 23, 42, 0.62);
  opacity: 0;
  transition: opacity 0.18s ease;
  pointer-events: none;
}

.avatar-input {
  display: none;
}

.avatar-editor {
  display: grid;
  justify-items: center;
  gap: 16px;
}

.avatar-preview {
  width: 144px;
  height: 144px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
}

.avatar-editor-actions {
  display: flex;
  justify-content: center;
}

.avatar-editor-tip {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.profile-title {
  min-width: 0;

  h1 {
    margin: 0 0 8px;
    font-size: 24px;
    font-weight: 700;
    color: #111827;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 14px;
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.profile-section {
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e5eaf3;
  border-radius: 8px;
}

.section-title {
  margin-bottom: 16px;
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.info-list {
  display: grid;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #edf2f7;

  &:last-child {
    padding-bottom: 0;
    border-bottom: 0;
  }

  span {
    flex: 0 0 auto;
    color: #64748b;
  }

  strong {
    min-width: 0;
    color: #1f2937;
    font-weight: 600;
    text-align: right;
    overflow-wrap: anywhere;
  }
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  padding: 14px;
  background: #f8fafc;
  border-radius: 8px;

  span {
    display: block;
    margin-bottom: 8px;
    color: #64748b;
    font-size: 13px;
  }

  strong {
    color: #111827;
    font-size: 24px;
    line-height: 1;
  }
}

.permission-section {
  margin-top: 16px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-tag {
  max-width: 100%;
}

.empty-text {
  color: #94a3b8;
  font-size: 14px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .personal-center-page {
    padding: 12px;
  }

  .profile-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .info-row {
    flex-direction: column;
    gap: 6px;

    strong {
      text-align: left;
    }
  }
}
</style>
