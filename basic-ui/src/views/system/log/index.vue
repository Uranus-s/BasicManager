<template>
  <div class="log-container">
    <el-card shadow="never">
      <el-form
        ref="queryForm"
        class="query-form"
        :inline="true"
        :model="listQuery"
        @submit.prevent
      >
        <el-form-item label="日志类型">
          <el-select
            v-model="listQuery.logType"
            clearable
            placeholder="请选择日志类型"
            style="width: 140px"
          >
            <el-option label="登录日志" value="LOGIN" />
            <el-option label="操作日志" value="OPER" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model.trim="listQuery.keyword"
            clearable
            placeholder="请输入关键字"
            style="width: 240px"
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
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="list" style="width: 100%">
        <el-table-column label="日志类型" width="110">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTag(row.logType)">
              {{ getLogTypeText(row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="标题"
          prop="title"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          label="内容"
          prop="content"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column
          label="方法名"
          prop="method"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="请求方式" width="110">
          <template #default="{ row }">
            <el-tag :type="getRequestMethodTag(row.requestMethod)" effect="plain">
              {{ row.requestMethod || "-" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="IP地址"
          prop="ip"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="{ row }">
            {{ formatCostTime(row.costTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          min-width="170"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
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
  </div>
</template>

<script>
import { getLogList } from "@/api/system/log";

const defaultQuery = (pageSize = 10) => ({
  pageNum: 1,
  pageSize,
  logType: "",
  keyword: "",
  status: undefined,
});

export default {
  name: "SystemLog",
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      listQuery: defaultQuery(),
    };
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.listLoading = true;
      try {
        const { data } = await getLogList(this.listQuery);
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
      this.listQuery = defaultQuery(this.listQuery.pageSize);
      this.getList();
    },
    getLogTypeText(type) {
      const typeMap = {
        LOGIN: "登录日志",
        OPER: "操作日志",
      };
      return typeMap[type] || type || "-";
    },
    getLogTypeTag(type) {
      const tagMap = {
        LOGIN: "success",
        OPER: "warning",
      };
      return tagMap[type] || "info";
    },
    getStatusText(status) {
      if (status === 1) return "成功";
      if (status === 0) return "失败";
      return "-";
    },
    getStatusTag(status) {
      if (status === 1) return "success";
      if (status === 0) return "danger";
      return "info";
    },
    getRequestMethodTag(method) {
      const methodMap = {
        GET: "success",
        POST: "primary",
        PUT: "warning",
        DELETE: "danger",
      };
      return methodMap[String(method || "").toUpperCase()] || "info";
    },
    formatCostTime(costTime) {
      if (costTime === undefined || costTime === null || costTime === "") {
        return "-";
      }
      return `${costTime}ms`;
    },
    formatDateTime(value) {
      if (!value) return "-";
      return String(value).replace("T", " ");
    },
  },
};
</script>

<style lang="scss" scoped>
.log-container {
  padding: 20px;

  .query-form {
    margin-bottom: 12px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }
}
</style>
