<template>
  <div class="role-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="角色编码">
          <el-input
            v-model.trim="listQuery.roleCode"
            clearable
            placeholder="请输入角色编码"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input
            v-model.trim="listQuery.roleName"
            clearable
            placeholder="请输入角色名称"
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
          label="角色编码"
          prop="roleCode"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          label="角色名称"
          prop="roleName"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="$dictTagType('sys_user_status', row.status)">
              {{ $dictLabel("sys_user_status", row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="备注"
          prop="remark"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          label="创建时间"
          prop="createTime"
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column fixed="right" label="操作" width="290">
          <template #default="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleAssignPermissions(row)">
              分配权限
            </el-button>
            <el-button type="text" @click="handleManageUsers(row)">
              管理用户
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model.trim="form.roleCode"
            :disabled="dialogType === 'edit'"
            maxlength="50"
            placeholder="请输入角色编码"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model.trim="form.roleName"
            maxlength="50"
            placeholder="请输入角色名称"
          />
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
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model.trim="form.remark"
            maxlength="500"
            placeholder="请输入备注"
            show-word-limit
            type="textarea"
            :rows="3"
          />
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
      v-model="permissionDialogVisible"
      title="分配权限"
      width="640px"
      @closed="resetPermissionDialog"
    >
      <div class="permission-summary">
        当前角色：
        <strong>{{ permissionRole.roleName || "-" }}</strong>
      </div>
      <el-input
        v-model.trim="permissionFilterText"
        class="permission-filter"
        clearable
        placeholder="请输入权限名称或标识"
      />
      <el-tree
        ref="permissionTreeRef"
        v-loading="permissionTreeLoading"
        class="permission-tree"
        :check-strictly="permissionCheckStrictly"
        :data="permissionTree"
        :default-expand-all="false"
        :filter-node-method="filterPermissionNode"
        :props="permissionTreeProps"
        node-key="id"
        show-checkbox
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="permissionSubmitLoading"
            @click="submitPermissions"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="userDialogVisible"
      title="管理用户"
      width="820px"
      @closed="resetUserDialog"
    >
      <div class="permission-summary">
        当前角色：
        <strong>{{ userRole.roleName || "-" }}</strong>
      </div>
      <el-transfer
        v-model="selectedUserIds"
        v-loading="userTransferLoading"
        class="user-transfer"
        filterable
        filter-placeholder="请输入账号、昵称、手机号或邮箱"
        :data="userTransferData"
        :filter-method="filterUserTransfer"
        :titles="['可选用户', '已关联用户']"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!hasUserChanges"
            :loading="userSubmitLoading"
            @click="submitUsers"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  assignRolePermissions,
  createRole,
  deleteRole,
  getRoleDetail,
  getRoleList,
  getRolePermissions,
  getRoleUsers,
  updateRoleUsers,
  updateRole,
} from "@/api/system/role";
import { getPermissionTree } from "@/api/system/permission";
import { getUserList } from "@/api/system/user";

const defaultForm = () => ({
  id: undefined,
  roleCode: "",
  roleName: "",
  status: 1,
  remark: "",
  permissionIds: [],
});

export default {
  name: "SystemRole",
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      submitLoading: false,
      permissionTreeLoading: false,
      permissionSubmitLoading: false,
      userTransferLoading: false,
      userSubmitLoading: false,
      dialogVisible: false,
      permissionDialogVisible: false,
      userDialogVisible: false,
      dialogType: "create",
      permissionRole: {},
      permissionFilterText: "",
      permissionCheckStrictly: false,
      permissionTree: [],
      permissionTreeProps: {
        children: "children",
        label: "name",
      },
      userRole: {},
      userTransferData: [],
      selectedUserIds: [],
      originalUserIds: [],
      listQuery: {
        pageNum: 1,
        pageSize: 10,
        roleCode: "",
        roleName: "",
        status: undefined,
      },
      form: defaultForm(),
      rules: {
        roleCode: [
          { required: true, message: "请输入角色编码", trigger: "blur" },
          { max: 50, message: "长度不能超过 50 个字符", trigger: "blur" },
        ],
        roleName: [
          { required: true, message: "请输入角色名称", trigger: "blur" },
          { max: 50, message: "长度不能超过 50 个字符", trigger: "blur" },
        ],
        remark: [
          { max: 500, message: "长度不能超过 500 个字符", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    dialogTitle() {
      return this.dialogType === "create" ? "新增角色" : "编辑角色";
    },
    hasUserChanges() {
      const originalSet = new Set(this.originalUserIds);
      const selectedSet = new Set(this.selectedUserIds);

      if (originalSet.size !== selectedSet.size) return true;
      return this.selectedUserIds.some((id) => !originalSet.has(id));
    },
  },
  created() {
    this.$loadDict("sys_user_status");
    this.getList();
  },
  watch: {
    permissionFilterText(value) {
      this.$refs.permissionTreeRef &&
        this.$refs.permissionTreeRef.filter(value);
    },
  },
  methods: {
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getRoleList(this.listQuery);
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
        roleCode: "",
        roleName: "",
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
      const { data } = await getRoleDetail(row.id);
      this.form = {
        ...defaultForm(),
        ...data,
        status: data?.status ?? 1,
        permissionIds: this.getPermissionIds(data?.permissions),
      };
    },
    handleDelete(row) {
      this.$confirm(`确认删除角色「${row.roleName}」吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteRole(row.id);
          this.$message.success("删除成功");
          this.getList();
        })
        .catch(() => {});
    },
    async handleAssignPermissions(row) {
      this.permissionRole = row;
      this.permissionDialogVisible = true;
      this.permissionTreeLoading = true;
      try {
        const [treeRes, permissionRes] = await Promise.all([
          getPermissionTree(),
          getRolePermissions(row.id),
        ]);
        this.permissionTree = treeRes.data || [];
        const checkedKeys = this.normalizePermissionIds(permissionRes.data);
        this.permissionCheckStrictly = true;
        this.$nextTick(() => {
          if (this.$refs.permissionTreeRef) {
            this.$refs.permissionTreeRef.setCheckedKeys(checkedKeys);
          }
          this.$nextTick(() => {
            this.permissionCheckStrictly = false;
          });
        });
      } finally {
        this.permissionTreeLoading = false;
      }
    },
    async submitPermissions() {
      if (!this.permissionRole.id) return;

      this.permissionSubmitLoading = true;
      try {
        const permissionIds = this.getSelectedPermissionIds();
        await assignRolePermissions(this.permissionRole.id, permissionIds);
        this.$message.success("权限分配成功");
        this.permissionDialogVisible = false;
        this.getList();
      } finally {
        this.permissionSubmitLoading = false;
      }
    },
    async handleManageUsers(row) {
      this.userRole = row;
      this.userDialogVisible = true;
      this.userTransferLoading = true;
      try {
        const [userRes, roleUserRes] = await Promise.all([
          getUserList({ pageNum: 1, pageSize: 1000 }),
          getRoleUsers(row.id),
        ]);
        const users = userRes.data?.list || [];
        const roleUsers = roleUserRes.data || [];
        const userMap = new Map();
        users.concat(roleUsers).forEach((user) => {
          if (user?.id !== undefined && user?.id !== null) {
            userMap.set(user.id, user);
          }
        });
        this.userTransferData = Array.from(userMap.values()).map((user) => ({
          key: user.id,
          label: this.formatUserLabel(user),
          username: user.username,
          nickname: user.nickname,
          phone: user.phone,
          email: user.email,
        }));
        this.originalUserIds = this.getUserIds(roleUsers);
        this.selectedUserIds = [...this.originalUserIds];
      } finally {
        this.userTransferLoading = false;
      }
    },
    async submitUsers() {
      if (!this.userRole.id) return;
      if (!this.hasUserChanges) {
        this.$message.warning("请先移动用户后再保存");
        return;
      }

      this.userSubmitLoading = true;
      try {
        const payload = this.getUserChangePayload();
        await updateRoleUsers(this.userRole.id, payload);
        this.$message.success("关联用户保存成功");
        this.userDialogVisible = false;
        this.getList();
      } finally {
        this.userSubmitLoading = false;
      }
    },
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          const payload = { ...this.form };
          if (this.dialogType === "edit") {
            delete payload.roleCode;
            await updateRole(payload);
          } else {
            await createRole(payload);
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
    resetPermissionDialog() {
      this.permissionRole = {};
      this.permissionFilterText = "";
      this.permissionCheckStrictly = false;
      this.permissionTree = [];
      this.permissionTreeLoading = false;
      this.permissionSubmitLoading = false;
      this.$refs.permissionTreeRef &&
        this.$refs.permissionTreeRef.setCheckedKeys([]);
    },
    resetUserDialog() {
      this.userRole = {};
      this.userTransferData = [];
      this.selectedUserIds = [];
      this.originalUserIds = [];
      this.userTransferLoading = false;
      this.userSubmitLoading = false;
    },
    filterPermissionNode(value, data) {
      if (!value) return true;

      const keyword = value.toLowerCase();
      return [data.name, data.permission, data.path].some((item) =>
        String(item || "")
          .toLowerCase()
          .includes(keyword)
      );
    },
    getSelectedPermissionIds() {
      const tree = this.$refs.permissionTreeRef;
      if (!tree) return [];

      const checkedKeys = tree.getCheckedKeys(false);
      const halfCheckedKeys = tree.getHalfCheckedKeys();
      return Array.from(new Set([...checkedKeys, ...halfCheckedKeys]));
    },
    getPermissionIds(permissions) {
      return Array.isArray(permissions)
        ? permissions.map((item) => item.id).filter(Boolean)
        : [];
    },
    normalizePermissionIds(permissions) {
      if (!Array.isArray(permissions)) return [];

      return permissions
        .map((item) => (typeof item === "object" ? item.id : item))
        .filter((id) => id !== undefined && id !== null);
    },
    getUserIds(users) {
      return Array.isArray(users)
        ? users
            .map((item) => item.id)
            .filter((id) => id !== undefined && id !== null)
        : [];
    },
    getUserChangePayload() {
      const originalSet = new Set(this.originalUserIds);
      const selectedSet = new Set(this.selectedUserIds);
      return {
        addUserIds: this.selectedUserIds.filter((id) => !originalSet.has(id)),
        removeUserIds: this.originalUserIds.filter((id) => !selectedSet.has(id)),
      };
    },
    formatUserLabel(user) {
      const nickname = user.nickname ? `（${user.nickname}）` : "";
      return `${user.username || "-"}${nickname}`;
    },
    filterUserTransfer(query, item) {
      if (!query) return true;

      const keyword = query.toLowerCase();
      return [item.label, item.username, item.nickname, item.phone, item.email].some(
        (value) =>
          String(value || "")
            .toLowerCase()
            .includes(keyword)
      );
    },
  },
};
</script>

<style lang="scss" scoped>
.role-container {
  padding: 20px;

  .query-form {
    margin-bottom: 12px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }

  .permission-summary {
    margin-bottom: 12px;
    color: #606266;
  }

  .permission-filter {
    margin-bottom: 12px;
  }

  .permission-tree {
    min-height: 260px;
    max-height: 460px;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 12px;
  }

  .user-transfer {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}
</style>
