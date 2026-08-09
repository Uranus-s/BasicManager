<template>
  <main class="notice-center-container">
    <header class="page-heading">
      <div>
        <h1>公告中心</h1>
        <p>共 {{ total }} 条可见公告</p>
      </div>
      <el-button :loading="listLoading" @click="getList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </header>

    <el-form class="query-form" :inline="true" :model="listQuery" @submit.prevent>
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
          style="width: 160px"
        >
          <el-option
            v-for="option in noticeTypeOptions"
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
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="listLoading" :data="list" row-key="id" style="width: 100%">
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag effect="light" type="warning">{{ typeLabel(row.noticeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="公告标题" min-width="260">
        <template #default="{ row }">
          <el-button class="title-link" link type="primary" @click="openDetail(row.id)">
            {{ row.title }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" min-width="180" prop="publishTime" />
      <template #empty>
        <el-empty description="暂无可见公告" />
      </template>
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
  </main>
</template>

<script>
import { Refresh, Search } from "@element-plus/icons-vue";
import { getVisibleNoticeList } from "@/api/notice";

export default {
  name: "NoticeCenter",
  components: { Refresh, Search },
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        pageNum: 1,
        pageSize: 10,
        title: "",
        noticeType: "",
      },
    };
  },
  computed: {
    noticeTypeOptions() {
      return this.$dictOptions("sys_notice_type");
    },
  },
  created() {
    this.$loadDict("sys_notice_type");
    this.getList();
  },
  methods: {
    /** 用户端列表只调用可见公告接口，不展示管理状态或接收目标。 */
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getVisibleNoticeList(this.listQuery);
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
      };
      this.getList();
    },
    typeLabel(value) {
      return this.$dictLabel("sys_notice_type", value) || value || "公告";
    },
    openDetail(id) {
      this.$router.push(`/notice/${id}`);
    },
  },
};
</script>

<style lang="scss" scoped>
.notice-center-container {
  box-sizing: border-box;
  min-height: $base-app-main-height;
  padding: 24px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;

  h1 {
    margin: 0;
    color: var(--el-text-color-primary);
    font-size: 24px;
    font-weight: 600;
  }

  p {
    margin: 6px 0 0;
    color: var(--el-text-color-secondary);
  }
}

.query-form {
  margin-bottom: 12px;
}

.title-link {
  max-width: 100%;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 640px) {
  .notice-center-container {
    padding: 16px 12px;
  }

  .page-heading {
    align-items: flex-start;
  }

  :deep(.el-form--inline .el-form-item) {
    width: 100%;
    margin-right: 0;

    .el-form-item__content,
    .el-input,
    .el-select {
      width: 100% !important;
    }
  }
}
</style>
