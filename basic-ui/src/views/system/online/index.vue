<template>
  <div class="online-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="关键字">
          <el-input
            v-model.trim="listQuery.keyword"
            clearable
            placeholder="用户名 / 昵称 / IP"
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button :loading="listLoading" type="success" @click="getList">
            刷新
          </el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="filteredList" style="width: 100%">
        <el-table-column label="用户ID" prop="userId" width="100" />
        <el-table-column
          label="用户名"
          prop="username"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column
          label="昵称"
          prop="nickname"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column label="头像" width="90">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              {{ getAvatarText(row) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          label="登录IP"
          prop="loginIp"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          label="浏览器"
          prop="browser"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作系统"
          prop="os"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          label="登录时间"
          min-width="170"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ formatDateTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="130">
          <template #default="{ row }">
            <el-button
              :loading="forceLogoutLoadingId === row.userId"
              type="text"
              @click="handleForceLogout(row)"
            >
              强制下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        当前在线 {{ filteredList.length }} 人
      </div>
    </el-card>
  </div>
</template>

<script>
import { forceLogout, getOnlineUsers } from "@/api/system/online";

const defaultQuery = () => ({
  keyword: "",
});

export default {
  name: "SystemOnline",
  data() {
    return {
      list: [],
      listLoading: false,
      forceLogoutLoadingId: undefined,
      listQuery: defaultQuery(),
      activeKeyword: "",
    };
  },
  computed: {
    filteredList() {
      const keyword = this.activeKeyword.toLowerCase();
      if (!keyword) return this.list;

      return this.list.filter((item) => {
        return [item.username, item.nickname, item.loginIp]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(keyword));
      });
    },
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getOnlineUsers();
        this.list = Array.isArray(data) ? data : [];
      } finally {
        this.listLoading = false;
      }
    },
    handleQuery() {
      this.activeKeyword = this.listQuery.keyword;
    },
    resetQuery() {
      this.listQuery = defaultQuery();
      this.activeKeyword = "";
    },
    handleForceLogout(row) {
      const userName = row.nickname || row.username || row.userId;
      this.$confirm(`确认强制用户「${userName}」下线吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          this.forceLogoutLoadingId = row.userId;
          try {
            await forceLogout(row.userId);
            this.$message.success("强制下线成功");
            await this.getList();
          } finally {
            this.forceLogoutLoadingId = undefined;
          }
        })
        .catch(() => {});
    },
    getAvatarText(row) {
      const name = row.nickname || row.username || "";
      return name ? name.slice(0, 1).toUpperCase() : "U";
    },
    formatDateTime(value) {
      if (!value) return "-";
      return String(value).replace("T", " ");
    },
  },
};
</script>

<style lang="scss" scoped>
.online-container {
  padding: 20px;

  .query-form {
    margin-bottom: 12px;
  }

  .table-footer {
    margin-top: 16px;
    color: #606266;
    font-size: 13px;
    text-align: right;
  }
}
</style>
