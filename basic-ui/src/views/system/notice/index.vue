<template>
  <div class="notice-admin-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="公告标题">
          <el-input
            v-model.trim="listQuery.title"
            clearable
            placeholder="请输入公告标题"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select
            v-model="listQuery.noticeType"
            clearable
            placeholder="请选择公告类型"
            style="width: 150px"
          >
            <el-option
              v-for="option in noticeTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态">
          <el-select
            v-model="listQuery.status"
            clearable
            placeholder="请选择状态"
            style="width: 140px"
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button v-if="hasAuthority('system:notice:add')" type="success" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增公告
          </el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="list" style="width: 100%">
        <el-table-column label="标题" min-width="220" prop="title" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag effect="light" type="warning">{{ noticeTypeLabel(row.noticeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="接收范围" width="110">
          <template #default="{ row }">
            <span>{{ row.scopeType === "ALL" ? "全员" : "定向" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="170" prop="publishTime" show-overflow-tooltip />
        <el-table-column label="更新时间" min-width="170" prop="updateTime" show-overflow-tooltip />
        <el-table-column :fixed="operationColumnFixed" label="操作" min-width="300">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="hasAuthority('system:notice:edit')"
              type="text"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="hasAuthority('system:notice:publish') && canPublish(row)"
              type="text"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="hasAuthority('system:notice:withdraw') && canWithdraw(row)"
              type="text"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
            <el-button
              v-if="hasAuthority('system:notice:delete') && canDelete(row)"
              type="text"
              class="danger-action"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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
          @current-change="getList"
          @size-change="getList"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="drawerVisible"
      :before-close="handleDrawerClose"
      :size="drawerSize"
      :title="drawerTitle"
      destroy-on-close
      @closed="resetDrawer"
    >
      <div v-loading="detailLoading" class="drawer-content">
        <el-form
          ref="formRef"
          :disabled="drawerMode === 'view'"
          :model="form"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="公告标题" prop="title">
            <el-input
              v-model.trim="form.title"
              maxlength="200"
              placeholder="请输入公告标题"
              show-word-limit
            />
          </el-form-item>

          <div class="form-grid">
            <el-form-item label="公告类型" prop="noticeType">
              <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 100%">
                <el-option
                  v-for="option in noticeTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="接收范围" prop="scopeType">
              <el-segmented
                v-model="form.scopeType"
                :options="scopeOptions"
                @change="handleScopeChange"
              />
            </el-form-item>
          </div>

          <template v-if="form.scopeType === 'TARGETED'">
            <el-form-item label="接收角色" prop="roleIds">
              <el-select
                v-model="form.roleIds"
                clearable
                collapse-tags
                collapse-tags-tooltip
                multiple
                placeholder="请选择接收角色"
                style="width: 100%"
              >
                <el-option
                  v-for="role in targetOptions.roles"
                  :key="role.id"
                  :label="role.roleName"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="接收部门">
              <el-tree
                ref="deptTreeRef"
                class="dept-tree"
                 :data="targetOptions.departments"
                 :props="deptTreeProps"
                 check-strictly
                 default-expand-all
                node-key="id"
                show-checkbox
                @check="handleTargetChange"
              />
            </el-form-item>
          </template>

          <el-form-item label="公告正文" prop="content">
            <el-tabs v-model="editorTab" class="editor-tabs">
              <el-tab-pane label="Markdown 编辑" name="edit">
                <el-input
                  v-model="form.content"
                  maxlength="100000"
                  placeholder="请输入公告正文"
                  resize="vertical"
                  :rows="16"
                  type="textarea"
                />
              </el-tab-pane>
              <el-tab-pane label="预览" name="preview">
                <article class="markdown-body" v-html="renderedContent" />
              </el-tab-pane>
            </el-tabs>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="requestCloseDrawer">{{ drawerMode === "view" ? "关闭" : "取消" }}</el-button>
        <el-button
          v-if="drawerMode !== 'view'"
          type="primary"
          :loading="submitLoading"
          @click="submitForm"
        >
          保存
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script>
import MarkdownIt from "markdown-it";
import { Plus, Refresh, Search } from "@element-plus/icons-vue";
import eventBus from "@/utils/eventBus";
import {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticeList,
  getNoticeTargetOptions,
  publishNotice,
  updateNotice,
  withdrawNotice,
} from "@/api/system/notice";

const markdown = new MarkdownIt({ html: false, linkify: true, breaks: true });
const defaultLinkOpen =
  markdown.renderer.rules.link_open ||
  ((tokens, index, options, env, self) => self.renderToken(tokens, index, options));
markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  const href = tokens[index].attrGet("href") || "";
  if (/^https?:\/\//i.test(href)) {
    tokens[index].attrSet("target", "_blank");
    tokens[index].attrSet("rel", "noopener noreferrer");
  }
  return defaultLinkOpen(tokens, index, options, env, self);
};

const defaultForm = () => ({
  id: undefined,
  title: "",
  noticeType: "",
  content: "",
  scopeType: "ALL",
  roleIds: [],
  deptIds: [],
  version: undefined,
});

export default {
  name: "SystemNotice",
  components: { Plus, Refresh, Search },
  data() {
    const validateTargets = (_rule, _value, callback) => {
      if (this.form.scopeType !== "TARGETED") return callback();
      const deptIds = this.getCheckedDeptIds();
      if (this.form.roleIds.length || deptIds.length) return callback();
      callback(new Error("定向公告至少选择一个角色或部门"));
    };
    return {
      list: [],
      total: 0,
      listLoading: false,
      detailLoading: false,
      submitLoading: false,
      drawerVisible: false,
      drawerMode: "create",
      editorTab: "edit",
      savedSnapshot: "",
      listQuery: {
        pageNum: 1,
        pageSize: 10,
        title: "",
        noticeType: "",
        status: "",
      },
      form: defaultForm(),
      targetOptions: { roles: [], departments: [] },
      scopeOptions: [
        { label: "全员", value: "ALL" },
        { label: "定向", value: "TARGETED" },
      ],
      statusOptions: [
        { label: "草稿", value: "DRAFT" },
        { label: "已发布", value: "PUBLISHED" },
        { label: "已撤回", value: "WITHDRAWN" },
      ],
      deptTreeProps: { children: "children", label: "deptName" },
      rules: {
        title: [
          { required: true, message: "请输入公告标题", trigger: "blur" },
          { max: 200, message: "公告标题不能超过 200 个字符", trigger: "blur" },
        ],
        noticeType: [{ required: true, message: "请选择公告类型", trigger: "change" }],
        scopeType: [{ required: true, message: "请选择接收范围", trigger: "change" }],
        roleIds: [{ validator: validateTargets, trigger: "change" }],
        content: [{ required: true, message: "请输入公告正文", trigger: "blur" }],
      },
    };
  },
  computed: {
    permissions() {
      return this.$store.getters["user/permissions"] || [];
    },
    noticeTypeOptions() {
      return this.$dictOptions("sys_notice_type");
    },
    drawerSize() {
      return this.$store.getters["settings/device"] === "mobile" ? "100%" : "760px";
    },
    operationColumnFixed() {
      return this.$store.getters["settings/device"] === "mobile" ? false : "right";
    },
    drawerTitle() {
      if (this.drawerMode === "create") return "新增公告";
      if (this.drawerMode === "edit") return "编辑公告";
      return "公告详情";
    },
    renderedContent() {
      return markdown.render(this.form.content || "");
    },
  },
  created() {
    this.$loadDict("sys_notice_type");
    this.getList();
  },
  methods: {
    /** 加载管理端公告分页，服务端负责最终权限和筛选边界。 */
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getNoticeList(this.listQuery);
        this.list = data?.list || [];
        this.total = data?.total || 0;
      } catch (_error) {
        this.list = [];
        this.total = 0;
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
        title: "",
        noticeType: "",
        status: "",
      };
      this.getList();
    },
    hasAuthority(authority) {
      return this.permissions.includes(authority);
    },
    noticeTypeLabel(value) {
      return this.$dictLabel("sys_notice_type", value) || value || "-";
    },
    statusLabel(value) {
      return this.statusOptions.find((option) => option.value === value)?.label || value || "-";
    },
    statusTagType(value) {
      return { DRAFT: "info", PUBLISHED: "success", WITHDRAWN: "warning" }[value] || "info";
    },
    canPublish(row) {
      return ["DRAFT", "WITHDRAWN"].includes(row.status);
    },
    canWithdraw(row) {
      return row.status === "PUBLISHED";
    },
    canDelete(row) {
      return ["DRAFT", "WITHDRAWN"].includes(row.status);
    },
    async ensureTargetOptions() {
      if (this.targetOptions.roles.length || this.targetOptions.departments.length) return;
      const { data } = await getNoticeTargetOptions();
      this.targetOptions = {
        roles: data?.roles || [],
        departments: data?.departments || [],
      };
    },
    /** 新增时先加载目标选项，保证切换定向后控件立即可用。 */
    async handleCreate() {
      this.drawerMode = "create";
      this.form = defaultForm();
      this.editorTab = "edit";
      this.drawerVisible = true;
      this.detailLoading = true;
      try {
        await this.ensureTargetOptions();
        this.markSaved();
      } finally {
        this.detailLoading = false;
      }
    },
    handleView(row) {
      this.openExisting(row.id, "view");
    },
    handleEdit(row) {
      this.openExisting(row.id, "edit");
    },
    /** 详情与编辑共用一次读取，避免列表摘要被误当成完整表单数据。 */
    async openExisting(id, mode) {
      this.drawerMode = mode;
      this.form = defaultForm();
      this.drawerVisible = true;
      this.detailLoading = true;
      try {
        const [{ data }] = await Promise.all([getNoticeDetail(id), this.ensureTargetOptions()]);
        this.form = { ...defaultForm(), ...data, roleIds: data?.roleIds || [], deptIds: data?.deptIds || [] };
        await this.$nextTick();
        this.$refs.deptTreeRef?.setCheckedKeys(this.form.deptIds);
        this.markSaved();
      } finally {
        this.detailLoading = false;
      }
    },
    handleScopeChange(value) {
      if (value !== "ALL") return;
      this.form.roleIds = [];
      this.form.deptIds = [];
      this.$refs.deptTreeRef?.setCheckedKeys([]);
    },
    handleTargetChange() {
      this.form.deptIds = this.getCheckedDeptIds();
      this.$refs.formRef?.validateField("roleIds").catch(() => {});
    },
    getCheckedDeptIds() {
      return this.$refs.deptTreeRef?.getCheckedKeys(false) || this.form.deptIds || [];
    },
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const targeted = this.form.scopeType === "TARGETED";
          const payload = {
            ...this.form,
            roleIds: targeted ? [...new Set(this.form.roleIds)] : [],
            deptIds: targeted ? [...new Set(this.getCheckedDeptIds())] : [],
          };
          if (this.drawerMode === "edit") {
            await updateNotice(payload);
          } else {
            delete payload.id;
            delete payload.version;
            await createNotice(payload);
          }
          this.$message.success("保存成功");
          this.markSaved();
          this.drawerVisible = false;
          eventBus.emit("notice-updated");
          await this.getList();
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handlePublish(row) {
      this.confirmAction(`确认发布公告「${row.title}」吗？`, () => publishNotice(row.id), "发布成功");
    },
    handleWithdraw(row) {
      this.confirmAction(`确认撤回公告「${row.title}」吗？`, () => withdrawNotice(row.id), "撤回成功");
    },
    handleDelete(row) {
      this.confirmAction(`确认删除公告「${row.title}」吗？`, () => deleteNotice(row.id), "删除成功", "warning");
    },
    /** 状态命令统一确认、刷新列表并通知全局公告条。 */
    confirmAction(message, action, successMessage, type = "warning") {
      this.$confirm(message, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type,
      })
        .then(async () => {
          await action();
          this.$message.success(successMessage);
          eventBus.emit("notice-updated");
          await this.getList();
        })
        .catch(() => {});
    },
    serializeForm() {
      return JSON.stringify({ ...this.form, deptIds: this.getCheckedDeptIds() });
    },
    markSaved() {
      this.form.deptIds = this.getCheckedDeptIds();
      this.savedSnapshot = this.serializeForm();
    },
    handleDrawerClose(done) {
      if (this.drawerMode === "view" || this.serializeForm() === this.savedSnapshot) {
        done();
        return;
      }
      this.$confirm("当前公告尚未保存，确认关闭吗？", "提示", {
        confirmButtonText: "确认关闭",
        cancelButtonText: "继续编辑",
        type: "warning",
      })
        .then(() => {
          this.markSaved();
          done();
        })
        .catch(() => {});
    },
    requestCloseDrawer() {
      this.handleDrawerClose(() => {
        this.drawerVisible = false;
      });
    },
    resetDrawer() {
      this.form = defaultForm();
      this.savedSnapshot = "";
      this.editorTab = "edit";
      this.submitLoading = false;
      this.$refs.formRef?.clearValidate();
    },
  },
};
</script>

<style lang="scss" scoped>
.notice-admin-container {
  padding: 20px;
}

.query-form {
  margin-bottom: 12px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.danger-action {
  color: var(--el-color-danger);
}

.drawer-content {
  min-height: 240px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.dept-tree {
  box-sizing: border-box;
  width: 100%;
  max-height: 280px;
  padding: 8px;
  overflow: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.editor-tabs {
  width: 100%;
}

.markdown-body {
  box-sizing: border-box;
  min-height: 352px;
  max-width: 100%;
  padding: 16px;
  overflow-wrap: anywhere;
  color: var(--el-text-color-primary);
  line-height: 1.7;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;

  :deep(pre) {
    max-width: 100%;
    padding: 12px;
    overflow: auto;
    background: var(--el-fill-color-light);
  }
}

@media (max-width: 640px) {
  .notice-admin-container {
    padding: 12px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  :deep(.el-drawer__footer) {
    .el-button {
      min-height: 44px;
    }
  }
}
</style>
