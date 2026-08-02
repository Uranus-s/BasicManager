<template>
  <div class="dept-container">
    <div class="dept-layout">
      <el-card class="dept-tree-card" shadow="never">
        <div class="dept-tree-header">
          <el-button type="success" @click="handleCreate">新增部门</el-button>
          <el-button :loading="treeLoading" @click="getTree">刷新</el-button>
        </div>
        <el-input
          v-model.trim="treeKeyword"
          class="dept-tree-filter"
          clearable
          placeholder="请输入部门名称、负责人或电话"
        />
        <el-tree
          ref="deptTreeRef"
          v-loading="treeLoading"
          class="dept-tree"
          :data="filteredTree"
          default-expand-all
          highlight-current
          node-key="id"
          :props="treeProps"
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="dept-tree-node">
              <span>{{ data.deptName }}</span>
              <span v-if="data.leader" class="dept-tree-node-leader">
                {{ data.leader }}
              </span>
            </span>
          </template>
        </el-tree>
        <div class="dept-tree-actions">
          <el-button
            :disabled="!currentDept.id"
            type="primary"
            @click="handleCreateChild"
          >
            新增下级
          </el-button>
          <el-button :disabled="!currentDept.id" @click="handleEdit">
            编辑
          </el-button>
          <el-button
            :disabled="!currentDept.id"
            type="danger"
            @click="handleDelete"
          >
            删除
          </el-button>
        </div>
      </el-card>

      <el-card class="dept-user-card" shadow="never">
        <template #header>
          <div class="dept-user-header">
            <div>
              <span class="dept-user-title">
                {{ currentDept.deptName || "部门用户" }}
              </span>
              <span v-if="currentDept.id" class="dept-user-subtitle">
                共 {{ filteredUsers.length }} 人
              </span>
            </div>
            <div class="dept-user-actions">
              <el-button
                :disabled="!currentDept.id"
                type="success"
                @click="handleAddUsers"
              >
                新增用户
              </el-button>
              <el-button
                :disabled="!currentDept.id"
                :loading="userLoading"
                type="primary"
                @click="getUsers"
              >
                刷新用户
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-if="!currentDept.id" description="请选择左侧部门" />
        <template v-else>
          <el-form class="query-form" :inline="true" @submit.prevent>
            <el-form-item label="用户关键字">
              <el-input
                v-model.trim="userKeyword"
                clearable
                placeholder="账号、昵称、手机号、邮箱、部门或角色"
                style="width: 300px"
              />
            </el-form-item>
          </el-form>

          <el-table
            v-loading="userLoading"
            :data="filteredUsers"
            style="width: 100%"
          >
            <el-table-column label="ID" prop="id" width="80" />
            <el-table-column
              label="登录账号"
              min-width="120"
              prop="username"
              show-overflow-tooltip
            />
            <el-table-column
              label="昵称"
              min-width="120"
              prop="nickname"
              show-overflow-tooltip
            />
            <el-table-column
              label="手机号"
              min-width="130"
              prop="phone"
              show-overflow-tooltip
            />
            <el-table-column
              label="邮箱"
              min-width="180"
              prop="email"
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
              min-width="170"
              prop="createTime"
              show-overflow-tooltip
            />
            <el-table-column fixed="right" label="操作" width="110">
              <template #default="{ row }">
                <el-button type="text" @click="handleRemoveUser(row)">
                  踢出部门
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            check-strictly
            :data="parentOptions"
            default-expand-all
            node-key="id"
            placeholder="请选择上级部门"
            :props="{ value: 'id', label: 'deptName', children: 'children' }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input
            v-model.trim="form.deptName"
            maxlength="50"
            placeholder="请输入部门名称"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input
            v-model.trim="form.leader"
            maxlength="50"
            placeholder="请输入负责人"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model.trim="form.phone"
            maxlength="20"
            placeholder="请输入联系电话"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
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
      v-model="addUserDialogVisible"
      title="新增用户"
      width="820px"
      @closed="resetAddUserDialog"
    >
      <div class="dept-user-summary">
        当前部门：
        <strong>{{ currentDept.deptName || "-" }}</strong>
      </div>
      <el-transfer
        v-model="selectedAddUserIds"
        v-loading="addUserLoading"
        class="user-transfer"
        filterable
        filter-placeholder="请输入账号、昵称、手机号或邮箱"
        :data="addUserTransferData"
        :filter-method="filterUserTransfer"
        :titles="['可选用户', '待添加用户']"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :disabled="!selectedAddUserIds.length"
            :loading="addUserSubmitLoading"
            @click="submitAddUsers"
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
  addDeptUsers,
  createDept,
  deleteDept,
  getDeptDetail,
  getDeptTree,
  getDeptUsers,
  removeDeptUsers,
  updateDept,
} from "@/api/system/dept";
import { getUserList } from "@/api/system/user";

const rootOption = {
  id: 0,
  deptName: "顶级部门",
  children: [],
};

const defaultForm = () => ({
  id: undefined,
  parentId: 0,
  deptName: "",
  leader: "",
  phone: "",
  sort: 0,
});

export default {
  name: "SystemDept",
  data() {
    return {
      tree: [],
      users: [],
      addUserTransferData: [],
      selectedAddUserIds: [],
      currentDept: {},
      parentOptions: [{ ...rootOption }],
      treeKeyword: "",
      userKeyword: "",
      treeLoading: false,
      userLoading: false,
      submitLoading: false,
      addUserLoading: false,
      addUserSubmitLoading: false,
      dialogVisible: false,
      addUserDialogVisible: false,
      dialogType: "create",
      treeProps: {
        children: "children",
        label: "deptName",
      },
      form: defaultForm(),
      rules: {
        parentId: [
          { required: true, message: "请选择上级部门", trigger: "change" },
        ],
        deptName: [
          { required: true, message: "请输入部门名称", trigger: "blur" },
          { max: 50, message: "长度不能超过 50 个字符", trigger: "blur" },
        ],
        leader: [
          { max: 50, message: "长度不能超过 50 个字符", trigger: "blur" },
        ],
        phone: [
          { max: 20, message: "长度不能超过 20 个字符", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    dialogTitle() {
      return this.dialogType === "create" ? "新增部门" : "编辑部门";
    },
    filteredTree() {
      return this.filterDeptTree(this.tree);
    },
    filteredUsers() {
      const keyword = this.userKeyword.trim().toLowerCase();
      if (!keyword) return this.users;

      return this.users.filter((user) =>
        [
          user.username,
          user.nickname,
          user.phone,
          user.email,
          ...(user.deptNames || []),
          ...(user.roleNames || []),
        ].some((value) =>
          String(value || "")
            .toLowerCase()
            .includes(keyword)
        )
      );
    },
  },
  created() {
    this.$loadDict("sys_user_status");
    this.getTree();
  },
  methods: {
    async getTree() {
      this.treeLoading = true;
      const currentId = this.currentDept.id;
      try {
        const { data } = await getDeptTree();
        this.tree = Array.isArray(data) ? data : [];
        this.parentOptions = this.buildParentTree();
        this.restoreCurrentDept(currentId);
      } finally {
        this.treeLoading = false;
      }
    },
    async getUsers() {
      if (!this.currentDept.id) return;

      this.userLoading = true;
      try {
        const { data } = await getDeptUsers(this.currentDept.id);
        this.users = Array.isArray(data) ? data : [];
      } finally {
        this.userLoading = false;
      }
    },
    async handleAddUsers() {
      if (!this.currentDept.id) return;

      this.addUserDialogVisible = true;
      this.addUserLoading = true;
      try {
        const { data } = await getUserList({ pageNum: 1, pageSize: -1 });
        const currentUserIds = new Set(this.users.map((user) => user.id));
        this.addUserTransferData = (data?.list || data || [])
          .filter((user) => !currentUserIds.has(user.id))
          .map((user) => ({
            key: user.id,
            label: this.formatUserLabel(user),
            username: user.username,
            nickname: user.nickname,
            phone: user.phone,
            email: user.email,
            disabled: user.status === 0,
          }));
      } finally {
        this.addUserLoading = false;
      }
    },
    async submitAddUsers() {
      if (!this.currentDept.id || !this.selectedAddUserIds.length) return;

      this.addUserSubmitLoading = true;
      try {
        await addDeptUsers(this.currentDept.id, this.selectedAddUserIds);
        this.$message.success("用户添加成功");
        this.addUserDialogVisible = false;
        await this.getUsers();
      } finally {
        this.addUserSubmitLoading = false;
      }
    },
    handleRemoveUser(row) {
      if (!this.currentDept.id || !row?.id) return;

      const userName = row.nickname || row.username || row.id;
      this.$confirm(
        `确认将用户「${userName}」踢出部门「${this.currentDept.deptName}」吗？`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(async () => {
          await removeDeptUsers(this.currentDept.id, [row.id]);
          this.$message.success("已踢出部门");
          await this.getUsers();
        })
        .catch(() => {});
    },
    handleNodeClick(data) {
      this.currentDept = data || {};
      this.userKeyword = "";
      this.getUsers();
    },
    handleCreate() {
      this.dialogType = "create";
      this.form = defaultForm();
      this.parentOptions = this.buildParentTree();
      this.dialogVisible = true;
    },
    handleCreateChild() {
      if (!this.currentDept.id) return;

      this.dialogType = "create";
      this.form = {
        ...defaultForm(),
        parentId: this.currentDept.id,
      };
      this.parentOptions = this.buildParentTree();
      this.dialogVisible = true;
    },
    async handleEdit() {
      if (!this.currentDept.id) return;

      this.dialogType = "edit";
      this.dialogVisible = true;
      this.parentOptions = this.buildParentTree(this.currentDept.id);
      const { data } = await getDeptDetail(this.currentDept.id);
      this.form = {
        ...defaultForm(),
        ...data,
        parentId: data?.parentId ?? 0,
        sort: data?.sort ?? 0,
      };
    },
    handleDelete() {
      if (!this.currentDept.id) return;

      this.$confirm(
        `确认删除部门「${this.currentDept.deptName}」吗？`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(async () => {
          const deletedId = this.currentDept.id;
          await deleteDept(deletedId);
          this.$message.success("删除成功");
          this.currentDept = {};
          this.users = [];
          await this.getTree();
        })
        .catch(() => {});
    },
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          const payload = { ...this.form };
          if (this.dialogType === "edit") {
            await updateDept(payload);
          } else {
            delete payload.id;
            await createDept(payload);
          }
          this.$message.success("保存成功");
          this.dialogVisible = false;
          await this.getTree();
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      this.form = defaultForm();
      this.parentOptions = this.buildParentTree();
      this.$refs.formRef && this.$refs.formRef.clearValidate();
    },
    resetAddUserDialog() {
      this.addUserTransferData = [];
      this.selectedAddUserIds = [];
      this.addUserLoading = false;
      this.addUserSubmitLoading = false;
    },
    restoreCurrentDept(currentId) {
      if (!currentId) return;

      const matched = this.findDeptById(this.tree, currentId);
      if (!matched) {
        this.currentDept = {};
        this.users = [];
        return;
      }

      this.currentDept = matched;
      this.$nextTick(() => {
        this.$refs.deptTreeRef &&
          this.$refs.deptTreeRef.setCurrentKey(currentId);
      });
      this.getUsers();
    },
    filterDeptTree(tree) {
      const keyword = this.treeKeyword.trim().toLowerCase();
      if (!keyword) return tree || [];

      return (tree || [])
        .map((item) => {
          const children = this.filterDeptTree(item.children || []);
          const matched = [item.deptName, item.leader, item.phone].some((value) =>
            String(value || "")
              .toLowerCase()
              .includes(keyword)
          );

          if (!matched && !children.length) return null;

          return {
            ...item,
            children,
          };
        })
        .filter(Boolean);
    },
    buildParentTree(excludeId) {
      return [
        {
          ...rootOption,
          children: this.removeNodeAndChildren(this.tree, excludeId),
        },
      ];
    },
    removeNodeAndChildren(tree, excludeId) {
      return (tree || [])
        .filter((item) => item.id !== excludeId)
        .map((item) => ({
          ...item,
          children: this.removeNodeAndChildren(item.children || [], excludeId),
        }));
    },
    findDeptById(tree, id) {
      for (const item of tree || []) {
        if (item.id === id) return item;
        const matched = this.findDeptById(item.children || [], id);
        if (matched) return matched;
      }
      return null;
    },
    formatNames(names) {
      return names && names.length ? names.join("、") : "-";
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
.dept-container {
  padding: 20px;

  .dept-layout {
    display: grid;
    grid-template-columns: 320px minmax(0, 1fr);
    gap: 16px;
  }

  .dept-tree-card,
  .dept-user-card {
    min-width: 0;
  }

  .dept-tree-header,
  .dept-tree-actions,
  .dept-user-header,
  .dept-user-actions {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .dept-tree-header,
  .dept-user-header {
    justify-content: space-between;
  }

  .dept-tree-filter {
    margin: 12px 0;
  }

  .dept-tree {
    min-height: 360px;
    max-height: calc(100vh - 300px);
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 8px;
  }

  .dept-tree-node {
    display: inline-flex;
    gap: 8px;
    align-items: center;
    min-width: 0;
  }

  .dept-tree-node-leader,
  .dept-user-subtitle {
    color: #909399;
    font-size: 13px;
  }

  .dept-tree-actions {
    margin-top: 12px;
    justify-content: center;
  }

  .dept-user-title {
    font-weight: 600;
  }

  .dept-user-summary {
    margin-bottom: 12px;
    color: #606266;
  }

  .query-form {
    margin-bottom: 12px;
  }

  .user-tag {
    margin-right: 6px;
    margin-bottom: 4px;
  }

  .user-transfer {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 960px) {
  .dept-container {
    .dept-layout {
      grid-template-columns: 1fr;
    }

    .dept-tree {
      max-height: 420px;
    }
  }
}
</style>
