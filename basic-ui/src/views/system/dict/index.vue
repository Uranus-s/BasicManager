<template>
  <div class="dict-container">
    <div class="dict-layout">
      <el-card class="dict-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">字典列表</span>
            <el-button :loading="dictLoading" type="primary" @click="getDictListData">
              刷新
            </el-button>
          </div>
        </template>

        <el-form
          ref="dictQueryForm"
          class="query-form"
          :inline="true"
          :model="dictListQuery"
          @submit.prevent
        >
          <el-form-item label="字典编码">
            <el-input
              v-model.trim="dictListQuery.dictCode"
              clearable
              placeholder="请输入字典编码"
              @keyup.enter="handleDictQuery"
            />
          </el-form-item>
          <el-form-item label="字典名称">
            <el-input
              v-model.trim="dictListQuery.dictName"
              clearable
              placeholder="请输入字典名称"
              @keyup.enter="handleDictQuery"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="dictListQuery.status"
              clearable
              placeholder="请选择状态"
              style="width: 120px"
            >
              <el-option
                v-for="option in $dictOptions('user_status')"
                :key="option.value"
                :label="option.label"
                :value="Number(option.value)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleDictQuery">查询</el-button>
            <el-button @click="resetDictQuery">重置</el-button>
            <el-button type="success" @click="handleCreateDict">新增</el-button>
          </el-form-item>
        </el-form>

        <el-table
          ref="dictTableRef"
          v-loading="dictLoading"
          :data="dictList"
          highlight-current-row
          style="width: 100%"
          @row-click="handleDictSelect"
        >
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column
            label="字典编码"
            min-width="150"
            prop="dictCode"
            show-overflow-tooltip
          />
          <el-table-column
            label="字典名称"
            min-width="150"
            prop="dictName"
            show-overflow-tooltip
          />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="$dictTagType('user_status', row.status)">
                {{ $dictLabel("user_status", row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="创建时间"
            min-width="170"
            prop="createTime"
            show-overflow-tooltip
          />
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button type="text" @click.stop="handleEditDict(row)">
                编辑
              </el-button>
              <el-button type="text" @click.stop="handleDeleteDict(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:currentPage="dictListQuery.pageNum"
            v-model:page-size="dictListQuery.pageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 30, 50]"
            :total="dictTotal"
            @current-change="getDictListData"
            @size-change="getDictListData"
          />
        </div>
      </el-card>

      <el-card class="dict-item-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <span class="card-title">
                {{ currentDict.dictName || "字典项列表" }}
              </span>
              <span v-if="currentDict.id" class="card-subtitle">
                {{ currentDict.dictCode }}
              </span>
            </div>
            <div class="card-actions">
              <el-button
                :disabled="!currentDict.id"
                type="success"
                @click="handleCreateDictItem"
              >
                新增字典项
              </el-button>
              <el-button
                :disabled="!currentDict.id"
                :loading="itemLoading"
                type="primary"
                @click="getDictItemListData"
              >
                刷新
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-if="!currentDict.id" description="请选择左侧字典" />
        <template v-else>
          <el-form
            ref="itemQueryForm"
            class="query-form"
            :inline="true"
            :model="itemListQuery"
            @submit.prevent
          >
            <el-form-item label="字典项值">
              <el-input
                v-model.trim="itemListQuery.itemValue"
                clearable
                placeholder="请输入字典项值"
                @keyup.enter="handleItemQuery"
              />
            </el-form-item>
            <el-form-item label="字典项标签">
              <el-input
                v-model.trim="itemListQuery.itemLabel"
                clearable
                placeholder="请输入字典项标签"
                @keyup.enter="handleItemQuery"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="itemListQuery.status"
                clearable
                placeholder="请选择状态"
                style="width: 120px"
              >
                <el-option
                  v-for="option in $dictOptions('user_status')"
                  :key="option.value"
                  :label="option.label"
                  :value="Number(option.value)"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleItemQuery">查询</el-button>
              <el-button @click="resetItemQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="itemLoading" :data="itemList" style="width: 100%">
            <el-table-column label="ID" prop="id" width="80" />
            <el-table-column
              label="字典项值"
              min-width="150"
              prop="itemValue"
              show-overflow-tooltip
            />
            <el-table-column
              label="字典项标签"
              min-width="150"
              prop="itemLabel"
              show-overflow-tooltip
            />
            <el-table-column label="排序" prop="sort" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="$dictTagType('user_status', row.status)">
                  {{ $dictLabel("user_status", row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="创建时间"
              min-width="170"
              prop="createTime"
              show-overflow-tooltip
            />
            <el-table-column fixed="right" label="操作" width="150">
              <template #default="{ row }">
                <el-button type="text" @click="handleEditDictItem(row)">
                  编辑
                </el-button>
                <el-button type="text" @click="handleDeleteDictItem(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:currentPage="itemListQuery.pageNum"
              v-model:page-size="itemListQuery.pageSize"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 30, 50]"
              :total="itemTotal"
              @current-change="getDictItemListData"
              @size-change="getDictItemListData"
            />
          </div>
        </template>
      </el-card>
    </div>

    <el-dialog
      v-model="dictDialogVisible"
      :title="dictDialogTitle"
      width="560px"
      @closed="resetDictForm"
    >
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="90px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input
            v-model.trim="dictForm.dictCode"
            :disabled="dictDialogType === 'edit'"
            maxlength="100"
            placeholder="请输入字典编码"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input
            v-model.trim="dictForm.dictName"
            maxlength="100"
            placeholder="请输入字典名称"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictForm.status">
            <el-radio
              v-for="option in $dictOptions('user_status')"
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
          <el-button @click="dictDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dictSubmitLoading" @click="submitDictForm">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      width="560px"
      @closed="resetItemForm"
    >
      <div class="dict-summary">
        当前字典：
        <strong>{{ currentDict.dictName || "-" }}</strong>
      </div>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="字典项值" prop="itemValue">
          <el-input
            v-model.trim="itemForm.itemValue"
            maxlength="100"
            placeholder="请输入字典项值"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="字典项标签" prop="itemLabel">
          <el-input
            v-model.trim="itemForm.itemLabel"
            maxlength="100"
            placeholder="请输入字典项标签"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="itemForm.sort" :min="0" style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="itemForm.status">
            <el-radio
              v-for="option in $dictOptions('user_status')"
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
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="itemSubmitLoading" @click="submitItemForm">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  getDictDetail,
  getDictItemDetail,
  getDictItemList,
  getDictList,
  updateDict,
  updateDictItem,
} from "@/api/system/dict";

const defaultDictForm = () => ({
  id: undefined,
  dictCode: "",
  dictName: "",
  status: 1,
});

const defaultItemForm = () => ({
  id: undefined,
  dictId: undefined,
  itemValue: "",
  itemLabel: "",
  sort: 0,
  status: 1,
});

const defaultItemListQuery = (pageSize = 10) => ({
  pageNum: 1,
  pageSize,
  dictId: undefined,
  itemValue: "",
  itemLabel: "",
  status: undefined,
});

export default {
  name: "SystemDict",
  data() {
    return {
      dictList: [],
      itemList: [],
      currentDict: {},
      dictTotal: 0,
      itemTotal: 0,
      dictLoading: false,
      itemLoading: false,
      dictSubmitLoading: false,
      itemSubmitLoading: false,
      dictDialogVisible: false,
      itemDialogVisible: false,
      dictDialogType: "create",
      itemDialogType: "create",
      dictListQuery: {
        pageNum: 1,
        pageSize: 10,
        dictCode: "",
        dictName: "",
        status: undefined,
      },
      itemListQuery: defaultItemListQuery(),
      dictForm: defaultDictForm(),
      itemForm: defaultItemForm(),
      dictRules: {
        dictCode: [
          { required: true, message: "请输入字典编码", trigger: "blur" },
          { max: 100, message: "长度不能超过 100 个字符", trigger: "blur" },
        ],
        dictName: [
          { required: true, message: "请输入字典名称", trigger: "blur" },
          { max: 100, message: "长度不能超过 100 个字符", trigger: "blur" },
        ],
      },
      itemRules: {
        itemValue: [
          { required: true, message: "请输入字典项值", trigger: "blur" },
          { max: 100, message: "长度不能超过 100 个字符", trigger: "blur" },
        ],
        itemLabel: [
          { required: true, message: "请输入字典项标签", trigger: "blur" },
          { max: 100, message: "长度不能超过 100 个字符", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    dictDialogTitle() {
      return this.dictDialogType === "create" ? "新增字典" : "编辑字典";
    },
    itemDialogTitle() {
      return this.itemDialogType === "create" ? "新增字典项" : "编辑字典项";
    },
  },
  created() {
    this.$loadDict("user_status");
    this.getDictListData();
  },
  methods: {
    async getDictListData() {
      this.dictLoading = true;
      try {
        const { data } = await getDictList(this.dictListQuery);
        this.dictList = data?.list || [];
        this.dictTotal = data?.total || 0;
        this.restoreCurrentDict();
      } finally {
        this.dictLoading = false;
      }
    },
    async getDictItemListData() {
      if (!this.currentDict.id) return;

      this.itemLoading = true;
      try {
        const { data } = await getDictItemList({
          ...this.itemListQuery,
          dictId: this.currentDict.id,
        });
        this.itemList = data?.list || [];
        this.itemTotal = data?.total || 0;
      } finally {
        this.itemLoading = false;
      }
    },
    handleDictQuery() {
      this.dictListQuery.pageNum = 1;
      this.getDictListData();
    },
    resetDictQuery() {
      this.dictListQuery = {
        pageNum: 1,
        pageSize: this.dictListQuery.pageSize,
        dictCode: "",
        dictName: "",
        status: undefined,
      };
      this.getDictListData();
    },
    handleItemQuery() {
      this.itemListQuery.pageNum = 1;
      this.getDictItemListData();
    },
    resetItemQuery() {
      this.itemListQuery = defaultItemListQuery(this.itemListQuery.pageSize);
      this.getDictItemListData();
    },
    handleDictSelect(row) {
      this.currentDict = row || {};
      this.itemListQuery = defaultItemListQuery(this.itemListQuery.pageSize);
      this.itemList = [];
      this.itemTotal = 0;
      this.$nextTick(() => {
        this.$refs.dictTableRef &&
          this.$refs.dictTableRef.setCurrentRow(this.currentDict);
      });
      this.getDictItemListData();
    },
    handleCreateDict() {
      this.dictDialogType = "create";
      this.dictForm = defaultDictForm();
      this.dictDialogVisible = true;
    },
    async handleEditDict(row) {
      this.dictDialogType = "edit";
      this.dictDialogVisible = true;
      const { data } = await getDictDetail(row.id);
      this.dictForm = {
        ...defaultDictForm(),
        ...data,
        status: data?.status ?? 1,
      };
    },
    handleDeleteDict(row) {
      this.$confirm(`确认删除字典「${row.dictName}」吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteDict(row.id);
          this.$message.success("删除成功");
          if (this.currentDict.id === row.id) {
            this.currentDict = {};
            this.itemList = [];
            this.itemTotal = 0;
          }
          await this.getDictListData();
        })
        .catch(() => {});
    },
    submitDictForm() {
      this.$refs.dictFormRef.validate(async (valid) => {
        if (!valid) return;

        this.dictSubmitLoading = true;
        try {
          const payload = { ...this.dictForm };
          if (this.dictDialogType === "edit") {
            delete payload.dictCode;
            await updateDict(payload);
          } else {
            delete payload.id;
            await createDict(payload);
          }
          this.$message.success("保存成功");
          this.dictDialogVisible = false;
          await this.getDictListData();
        } finally {
          this.dictSubmitLoading = false;
        }
      });
    },
    resetDictForm() {
      this.dictForm = defaultDictForm();
      this.dictSubmitLoading = false;
      this.$refs.dictFormRef && this.$refs.dictFormRef.clearValidate();
    },
    handleCreateDictItem() {
      if (!this.currentDict.id) return;

      this.itemDialogType = "create";
      this.itemForm = {
        ...defaultItemForm(),
        dictId: this.currentDict.id,
      };
      this.itemDialogVisible = true;
    },
    async handleEditDictItem(row) {
      this.itemDialogType = "edit";
      this.itemDialogVisible = true;
      const { data } = await getDictItemDetail(row.id);
      this.itemForm = {
        ...defaultItemForm(),
        ...data,
        dictId: data?.dictId ?? this.currentDict.id,
        sort: data?.sort ?? 0,
        status: data?.status ?? 1,
      };
    },
    handleDeleteDictItem(row) {
      this.$confirm(`确认删除字典项「${row.itemLabel}」吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteDictItem(row.id);
          this.$message.success("删除成功");
          await this.getDictItemListData();
        })
        .catch(() => {});
    },
    submitItemForm() {
      if (!this.currentDict.id) return;

      this.$refs.itemFormRef.validate(async (valid) => {
        if (!valid) return;

        this.itemSubmitLoading = true;
        try {
          const payload = {
            ...this.itemForm,
            dictId: this.currentDict.id,
          };
          if (this.itemDialogType === "edit") {
            await updateDictItem(payload);
          } else {
            delete payload.id;
            await createDictItem(payload);
          }
          this.$message.success("保存成功");
          this.itemDialogVisible = false;
          await this.getDictItemListData();
        } finally {
          this.itemSubmitLoading = false;
        }
      });
    },
    resetItemForm() {
      this.itemForm = defaultItemForm();
      this.itemSubmitLoading = false;
      this.$refs.itemFormRef && this.$refs.itemFormRef.clearValidate();
    },
    restoreCurrentDict() {
      if (!this.currentDict.id) return;

      const matched = this.dictList.find((item) => item.id === this.currentDict.id);
      if (!matched) {
        this.currentDict = {};
        this.itemList = [];
        this.itemTotal = 0;
        return;
      }

      this.currentDict = matched;
      this.$nextTick(() => {
        this.$refs.dictTableRef &&
          this.$refs.dictTableRef.setCurrentRow(matched);
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.dict-container {
  padding: 20px;

  .dict-layout {
    display: grid;
    grid-template-columns: minmax(420px, 0.9fr) minmax(0, 1.1fr);
    gap: 16px;
  }

  .dict-card,
  .dict-item-card {
    min-width: 0;
  }

  .card-header,
  .card-actions {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .card-header {
    justify-content: space-between;
  }

  .card-title {
    font-weight: 600;
  }

  .card-subtitle {
    margin-left: 8px;
    color: #909399;
    font-size: 13px;
  }

  .query-form {
    margin-bottom: 12px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }

  .dict-summary {
    margin-bottom: 12px;
    color: #606266;
  }
}

@media (max-width: 1200px) {
  .dict-container {
    .dict-layout {
      grid-template-columns: 1fr;
    }
  }
}
</style>
