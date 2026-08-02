<template>
  <div class="user-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="登录账号">
          <el-input
            v-model.trim="listQuery.username"
            clearable
            placeholder="请输入登录账号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model.trim="listQuery.nickname"
            clearable
            placeholder="请输入昵称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="listQuery.status"
            clearable
            placeholder="请选择状态"
            style="width: 120px"
          >
            <el-option
              v-for="option in $dictOptions('sys_user_status')"
              :key="option.value"
              :label="option.label"
              :value="Number(option.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleCreate">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="list" style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column
          label="登录账号"
          prop="username"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          label="昵称"
          prop="nickname"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          label="手机号"
          prop="phone"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column
          label="邮箱"
          prop="email"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="部门" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatNames(row.deptNames) }}
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag
              v-for="roleName in row.roleNames || []"
              :key="roleName"
              class="user-tag"
              size="small"
            >
              {{ roleName }}
            </el-tag>
            <span v-if="!row.roleNames || !row.roleNames.length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="$dictTagType('sys_user_status', row.status)">
              {{ $dictLabel("sys_user_status", row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          prop="createTime"
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleAssignRoles(row)">
              分配角色
            </el-button>
            <el-button
              v-if="canResetPassword"
              type="text"
              @click="handleResetPassword(row)"
            >
              <el-icon><Key /></el-icon>
              重置密码
            </el-button>
            <el-button type="text" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:currentPage="listQuery.pageNum"
          v-model:page-size="listQuery.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
      >
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model.trim="form.username"
            :disabled="dialogType === 'edit'"
            maxlength="20"
            placeholder="请输入登录账号"
          />
        </el-form-item>
        <el-form-item v-if="dialogType === 'create'" label="登录密码" prop="password">
          <el-input
            v-model.trim="form.password"
            maxlength="20"
            placeholder="请输入登录密码"
            show-password
            type="password"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model.trim="form.nickname"
            maxlength="50"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item
          label="手机号"
          prop="phone"
          :required="dialogType === 'create'"
        >
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input v-model.trim="form.avatar" placeholder="请输入头像地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="option in $dictOptions('sys_user_status')"
              :key="option.value"
              :label="Number(option.value)"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="roleDialogVisible"
      :title="roleDialogTitle"
      width="560px"
      @closed="resetRoleForm"
    >
      <el-form label-width="90px">
        <el-form-item label="登录账号">
          <el-input :model-value="currentRoleUser.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="checkedRoleIds"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            :loading="roleLoading"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :disabled="role.status === 0"
              :label="formatRoleLabel(role)"
              :value="role.id"
            >
              <span>{{ role.roleName }}</span>
              <span class="role-code">{{ role.roleCode }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="roleSubmitLoading"
            @click="submitRoleForm"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="passwordDialogVisible"
      destroy-on-close
      :title="passwordDialogTitle"
      width="480px"
      @closed="resetPasswordForm"
    >
      <el-alert
        class="password-alert"
        :closable="false"
        show-icon
        title="保存后该用户的现有会话将立即失效"
        type="warning"
      />
      <el-form
        ref="passwordFormRef"
        autocomplete="off"
        label-position="top"
        :model="passwordForm"
        :rules="passwordRules"
      >
        <el-form-item label="临时密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            autocomplete="new-password"
            maxlength="20"
            placeholder="请输入 6 至 20 个字符"
            show-password
            type="password"
          />
        </el-form-item>
        <el-form-item label="确认临时密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            autocomplete="new-password"
            maxlength="20"
            placeholder="请再次输入临时密码"
            show-password
            type="password"
            @keyup.enter="submitPasswordReset"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="passwordSubmitLoading"
            @click="submitPasswordReset"
          >
            <el-icon><Check /></el-icon>
            确认重置
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Check, Key } from "@element-plus/icons-vue";
import { getAllRoles } from "@/api/system/role";
import {
  assignUserRoles,
  createUser,
  deleteUser,
  getUserDetail,
  getUserList,
  getUserRoles,
  resetUserPassword,
  updateUser,
} from "@/api/system/user";
import {
  canManage,
  validateTemporaryPasswords,
} from "@/utils/systemSettings";

const defaultForm = () => ({
  id: undefined,
  username: "",
  password: "",
  nickname: "",
  phone: "",
  email: "",
  avatar: "",
  status: 1,
});

const defaultPasswordForm = () => ({
  newPassword: "",
  confirmPassword: "",
});

export default {
  name: "SystemUser",
  components: { Check, Key },
  data() {
    const validatePhone = (rule, value, callback) => {
      if (this.dialogType === "create" && !value) {
        callback(new Error("请输入手机号"));
        return;
      }
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error("请输入正确的手机号"));
        return;
      }
      callback();
    };
    const validateEmail = (rule, value, callback) => {
      if (
        value &&
        !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value)
      ) {
        callback(new Error("请输入正确的邮箱地址"));
        return;
      }
      callback();
    };
    const validateConfirmPassword = (rule, value, callback) => {
      const message = validateTemporaryPasswords(
        this.passwordForm.newPassword,
        value
      );
      if (message) {
        callback(new Error(message));
        return;
      }
      callback();
    };

    return {
      list: [],
      total: 0,
      listLoading: false,
      submitLoading: false,
      roleLoading: false,
      roleSubmitLoading: false,
      passwordSubmitLoading: false,
      dialogVisible: false,
      roleDialogVisible: false,
      passwordDialogVisible: false,
      dialogType: "create",
      currentRoleUser: {},
      passwordUser: {},
      checkedRoleIds: [],
      roleOptions: [],
      listQuery: {
        pageNum: 1,
        pageSize: 10,
        username: "",
        nickname: "",
        status: undefined,
      },
      form: defaultForm(),
      passwordForm: defaultPasswordForm(),
      rules: {
        username: [
          { required: true, message: "请输入登录账号", trigger: "blur" },
          { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入登录密码", trigger: "blur" },
          { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
        ],
        phone: [{ validator: validatePhone, trigger: "blur" }],
        email: [{ validator: validateEmail, trigger: "blur" }],
      },
      passwordRules: {
        newPassword: [
          { required: true, message: "请输入临时密码", trigger: "blur" },
          {
            min: 6,
            max: 20,
            message: "临时密码长度必须在 6 到 20 个字符之间",
            trigger: "blur",
          },
        ],
        confirmPassword: [
          { required: true, message: "请再次输入临时密码", trigger: "blur" },
          { validator: validateConfirmPassword, trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    canResetPassword() {
      return canManage(
        this.$store.getters["user/permissions"],
        this.$store.getters["user/roles"],
        "system:user:resetPwd"
      );
    },
    dialogTitle() {
      return this.dialogType === "create" ? "新增用户" : "编辑用户";
    },
    roleDialogTitle() {
      return `分配角色${this.currentRoleUser.nickname ? ` - ${this.currentRoleUser.nickname}` : ""}`;
    },
    passwordDialogTitle() {
      const name = this.passwordUser.nickname || this.passwordUser.username;
      return name ? `重置密码 - ${name}` : "重置密码";
    },
  },
  created() {
    this.$loadDict("sys_user_status");
    this.getList();
  },
  methods: {
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getUserList(this.listQuery);
        this.list = data?.list || [];
        this.total = data?.total || 0;
      } finally {
        this.listLoading = false;
      }
    },
    handleQuery() {
      this.listQuery.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.listQuery = {
        pageNum: 1,
        pageSize: this.listQuery.pageSize,
        username: "",
        nickname: "",
        status: undefined,
      };
      this.getList();
    },
    handleCreate() {
      this.dialogType = "create";
      this.form = defaultForm();
      this.dialogVisible = true;
    },
    async handleEdit(row) {
      this.dialogType = "edit";
      this.dialogVisible = true;
      const { data } = await getUserDetail(row.id);
      this.form = {
        ...defaultForm(),
        ...data,
        password: "",
        status: data?.status ?? 1,
      };
    },
    handleDelete(row) {
      this.$confirm(`确认删除用户「${row.username}」吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteUser(row.id);
          this.$message.success("删除成功");
          this.getList();
        })
        .catch(() => {});
    },
    async handleAssignRoles(row) {
      this.currentRoleUser = { ...row };
      this.roleDialogVisible = true;
      this.roleLoading = true;
      try {
        const [{ data: roles }, { data: roleIds }] = await Promise.all([
          getAllRoles(),
          getUserRoles(row.id),
        ]);
        this.roleOptions = roles || [];
        this.checkedRoleIds = roleIds || [];
      } finally {
        this.roleLoading = false;
      }
    },
    handleResetPassword(row) {
      this.passwordUser = { ...row };
      this.passwordForm = defaultPasswordForm();
      this.passwordDialogVisible = true;
    },
    async submitRoleForm() {
      this.roleSubmitLoading = true;
      try {
        await assignUserRoles(this.currentRoleUser.id, this.checkedRoleIds);
        this.$message.success("角色分配成功");
        this.roleDialogVisible = false;
        this.getList();
      } finally {
        this.roleSubmitLoading = false;
      }
    },
    async submitPasswordReset() {
      if (!this.canResetPassword || !this.passwordUser.id) return;
      const valid = await this.$refs.passwordFormRef.validate().catch(() => false);
      if (!valid) return;

      this.passwordSubmitLoading = true;
      try {
        await resetUserPassword(this.passwordUser.id, {
          newPassword: this.passwordForm.newPassword,
        });
        this.$message.success("密码已重置，该用户的现有会话已失效");
        this.passwordDialogVisible = false;
      } finally {
        this.passwordSubmitLoading = false;
      }
    },
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          const payload = { ...this.form };
          if (this.dialogType === "edit") {
            delete payload.password;
            await updateUser(payload);
          } else {
            await createUser(payload);
          }
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.getList();
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      this.form = defaultForm();
      this.$refs.formRef && this.$refs.formRef.clearValidate();
    },
    resetRoleForm() {
      this.currentRoleUser = {};
      this.checkedRoleIds = [];
      this.roleOptions = [];
    },
    resetPasswordForm() {
      // 对话框关闭后立即清空敏感信息，避免密码留存在页面状态中。
      this.passwordUser = {};
      this.passwordForm = defaultPasswordForm();
      this.passwordSubmitLoading = false;
      this.$refs.passwordFormRef && this.$refs.passwordFormRef.clearValidate();
    },
    formatNames(names) {
      return names && names.length ? names.join("、") : "-";
    },
    formatRoleLabel(role) {
      return role.roleCode ? `${role.roleName} (${role.roleCode})` : role.roleName;
    },
  },
};
</script>

<style lang="scss" scoped>
.user-container {
  padding: 20px;

  .query-form {
    margin-bottom: 12px;
  }

  .user-tag {
    margin-right: 6px;
    margin-bottom: 4px;
  }

  .role-code {
    float: right;
    color: #909399;
    font-size: 13px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }

  .password-alert {
    margin-bottom: 20px;
  }
}

@media screen and (max-width: 576px) {
  :deep(.el-dialog) {
    width: calc(100% - 32px) !important;
  }
}
</style>
