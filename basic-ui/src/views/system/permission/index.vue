<template>
  <div class="permission-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="名称">
          <el-input
            v-model.trim="listQuery.name"
            clearable
            placeholder="请输入名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="listQuery.type"
            clearable
            placeholder="请选择类型"
            style="width: 120px"
          >
            <el-option
              v-for="option in permissionTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
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

      <el-table
        v-loading="listLoading"
        :data="filteredTree"
        row-key="id"
        style="width: 100%"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column
          label="名称"
          min-width="180"
          prop="name"
          show-overflow-tooltip
        />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{
                $dictLabel(
                  "sys_permission_type",
                  row.type,
                  getTypeText(row.type)
                )
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="路径"
          min-width="180"
          prop="path"
          show-overflow-tooltip
        />
        <el-table-column
          label="组件"
          min-width="200"
          prop="component"
          show-overflow-tooltip
        />
        <el-table-column
          label="权限标识"
          min-width="180"
          prop="permission"
          show-overflow-tooltip
        />
        <el-table-column
          label="图标"
          min-width="100"
          prop="icon"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span v-if="row.icon" class="icon-preview">
              <el-icon>
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.icon }}</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="显示" width="90">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'">
              {{ row.visible === 1 ? "显示" : "隐藏" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
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
        <el-table-column fixed="right" label="操作" width="190">
          <template #default="{ row }">
            <el-button type="text" @click="handleCreateChild(row)">
              新增下级
            </el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            check-strictly
            clearable
            :data="parentOptions"
            default-expand-all
            node-key="id"
            placeholder="请选择上级菜单"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input
            v-model.trim="form.name"
            maxlength="50"
            placeholder="请输入名称"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio
              v-for="option in permissionTypeOptions"
              :key="option.value"
              :label="option.value"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input
            v-model.trim="form.path"
            maxlength="200"
            placeholder="请输入路由路径或接口路径"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-input
            v-model.trim="form.component"
            maxlength="200"
            placeholder="请输入前端组件路径"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input
            v-model.trim="form.permission"
            maxlength="100"
            placeholder="请输入权限标识"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-popover
            placement="bottom-start"
            trigger="click"
            width="460px"
          >
            <template #reference>
              <el-button class="icon-select-button">
                <el-icon v-if="form.icon">
                  <component :is="form.icon" />
                </el-icon>
                <span>{{ form.icon || "请选择图标" }}</span>
              </el-button>
            </template>
            <div class="icon-picker">
              <el-input
                v-model.trim="iconKeyword"
                clearable
                placeholder="搜索图标"
              />
              <div class="icon-grid">
                <button
                  v-for="icon in filteredIconList"
                  :key="icon"
                  class="icon-option"
                  :class="{ active: form.icon === icon }"
                  type="button"
                  @click="selectIcon(icon)"
                >
                  <el-icon>
                    <component :is="icon" />
                  </el-icon>
                  <span>{{ icon }}</span>
                </button>
              </div>
              <div v-if="!filteredIconList.length" class="icon-empty">
                暂无匹配图标
              </div>
              <div class="icon-actions">
                <el-button text type="primary" @click="form.icon = ''">
                  清空图标
                </el-button>
              </div>
            </div>
          </el-popover>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
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
  </div>
</template>

<script>
import {
  createPermission,
  deletePermission,
  getPermissionDetail,
  getPermissionTree,
  updatePermission,
} from "@/api/system/permission";
import * as ElIcons from "@element-plus/icons-vue";

const rootOption = {
  id: 0,
  name: "顶级菜单",
  children: [],
};

const defaultForm = () => ({
  id: undefined,
  parentId: 0,
  name: "",
  type: "MENU",
  path: "",
  component: "",
  permission: "",
  icon: "",
  sort: 0,
  visible: 1,
  status: 1,
});

const permissionTypeFallbackOptions = [
  { label: "菜单", value: "MENU" },
  { label: "按钮", value: "BUTTON" },
  { label: "接口", value: "API" },
];

export default {
  name: "SystemPermission",
  components: {
    ...ElIcons,
  },
  data() {
    return {
      tree: [],
      filteredTree: [],
      parentOptions: [{ ...rootOption }],
      iconKeyword: "",
      iconList: Object.keys(ElIcons),
      listLoading: false,
      submitLoading: false,
      dialogVisible: false,
      dialogType: "create",
      listQuery: {
        name: "",
        type: "",
        status: undefined,
      },
      form: defaultForm(),
      rules: {
        parentId: [
          { required: true, message: "请选择上级菜单", trigger: "change" },
        ],
        name: [
          { required: true, message: "请输入名称", trigger: "blur" },
          { max: 50, message: "长度不能超过 50 个字符", trigger: "blur" },
        ],
        type: [
          { required: true, message: "请选择类型", trigger: "change" },
        ],
        path: [
          { max: 200, message: "长度不能超过 200 个字符", trigger: "blur" },
        ],
        component: [
          { max: 200, message: "长度不能超过 200 个字符", trigger: "blur" },
        ],
        permission: [
          { max: 100, message: "长度不能超过 100 个字符", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    dialogTitle() {
      return this.dialogType === "create" ? "新增权限" : "编辑权限";
    },
    filteredIconList() {
      const keyword = this.iconKeyword.toLowerCase();
      if (!keyword) return this.iconList;
      return this.iconList.filter((icon) =>
        icon.toLowerCase().includes(keyword)
      );
    },
    permissionTypeOptions() {
      const options = this.$dictOptions(
        "sys_permission_type",
        permissionTypeFallbackOptions
      );
      const optionValues = new Set(options.map((option) => option.value));
      const fallbackMissing = permissionTypeFallbackOptions.filter(
        (option) => !optionValues.has(option.value)
      );
      return options.concat(fallbackMissing);
    },
  },
  created() {
    this.$loadDicts(["sys_user_status", "sys_permission_type"]);
    this.getTree();
  },
  methods: {
    async getTree() {
      this.listLoading = true;
      try {
        const { data } = await getPermissionTree();
        this.tree = Array.isArray(data) ? data : [];
        this.handleQuery();
        this.parentOptions = this.buildParentTree();
      } finally {
        this.listLoading = false;
      }
    },
    handleQuery() {
      this.filteredTree = this.filterTree(this.tree);
    },
    resetQuery() {
      this.listQuery = {
        name: "",
        type: "",
        status: undefined,
      };
      this.handleQuery();
    },
    handleCreate() {
      this.dialogType = "create";
      this.form = defaultForm();
      this.parentOptions = this.buildParentTree();
      this.dialogVisible = true;
    },
    handleCreateChild(row) {
      this.dialogType = "create";
      this.form = {
        ...defaultForm(),
        parentId: row.id,
      };
      this.parentOptions = this.buildParentTree();
      this.dialogVisible = true;
    },
    async handleEdit(row) {
      this.dialogType = "edit";
      this.dialogVisible = true;
      this.parentOptions = this.buildParentTree(row.id);
      const { data } = await getPermissionDetail(row.id);
      this.form = {
        ...defaultForm(),
        ...data,
        parentId: data?.parentId ?? 0,
        sort: data?.sort ?? 0,
        visible: data?.visible ?? 1,
        status: data?.status ?? 1,
      };
    },
    handleDelete(row) {
      const nodeName = this.getNodeName(row);
      this.$confirm(`确认删除权限「${nodeName}」吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deletePermission(row.id);
          this.$message.success("删除成功");
          this.getTree();
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
            await updatePermission(payload);
          } else {
            delete payload.id;
            await createPermission(payload);
          }
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.getTree();
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      this.form = defaultForm();
      this.parentOptions = this.buildParentTree();
      this.iconKeyword = "";
      this.$refs.formRef && this.$refs.formRef.clearValidate();
    },
    selectIcon(icon) {
      this.form.icon = icon;
    },
    filterTree(tree) {
      const { name, type, status } = this.listQuery;
      const keyword = name ? name.toLowerCase() : "";

      return (tree || [])
        .map((item) => {
          const children = this.filterTree(item.children || []);
          const itemName = item.name ? item.name.toLowerCase() : "";
          const matchedName = !keyword || itemName.includes(keyword);
          const matchedType = !type || item.type === type;
          const matchedStatus =
            status === undefined || status === "" || item.status === status;
          const matched = matchedName && matchedType && matchedStatus;

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
    getTypeText(type) {
      const typeMap = {
        MENU: "菜单",
        BUTTON: "按钮",
        API: "接口",
      };
      return typeMap[type] || type || "-";
    },
    getTypeTag(type) {
      const tagMap = {
        MENU: "success",
        BUTTON: "warning",
        API: "info",
      };
      return tagMap[type] || "";
    },
    getNodeName(row) {
      return row?.name || row?.permission || row?.path || row?.id;
    },
  },
};
</script>

<style lang="scss" scoped>
.permission-container {
  padding: 20px;

  .query-form {
    margin-bottom: 12px;
  }

  .icon-preview {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .icon-select-button {
    justify-content: flex-start;
    width: 100%;
  }
}

.icon-picker {
  .icon-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    max-height: 300px;
    margin-top: 12px;
    overflow-x: hidden;
    overflow-y: auto;
  }

  .icon-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 72px;
    min-width: 0;
    padding: 8px;
    cursor: pointer;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    box-sizing: border-box;

    .el-icon {
      margin-bottom: 6px;
      font-size: 20px;
    }

    span {
      width: 100%;
      overflow: hidden;
      font-size: 12px;
      line-height: 16px;
      text-align: center;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &:hover,
    &.active {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
    }
  }

  .icon-empty {
    padding: 24px 0;
    color: #909399;
    text-align: center;
  }

  .icon-actions {
    margin-top: 10px;
    text-align: right;
  }
}
</style>
