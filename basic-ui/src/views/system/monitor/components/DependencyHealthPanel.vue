<template>
  <section class="monitor-panel dependency-panel" :aria-busy="loading">
    <header class="panel-header">
      <div>
        <h2>依赖服务</h2>
        <p>数据库与 Redis 实时健康状态</p>
      </div>
    </header>

    <el-skeleton v-if="loading && !hasData" :rows="5" animated />
    <el-result
      v-else-if="error && !hasData"
      icon="warning"
      title="依赖状态加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重新加载</el-button>
      </template>
    </el-result>
    <div v-else class="service-list">
      <article v-for="service in services" :key="service.key" class="service-card">
        <div class="service-heading">
          <div>
            <span
              class="status-dot"
              :class="service.level"
              aria-hidden="true"
            ></span>
            <strong :title="service.name">{{ service.name }}</strong>
          </div>
          <el-tag :type="service.tagType" effect="light">
            {{ service.statusText }}
          </el-tag>
        </div>
        <dl>
          <template v-for="item in service.details" :key="item.label">
            <dt>{{ item.label }}</dt>
            <dd :title="item.value">{{ item.value }}</dd>
          </template>
        </dl>
        <el-alert
          v-if="service.errorMessage"
          :closable="false"
          :title="service.errorMessage"
          type="error"
          show-icon
        />
      </article>
      <div class="directory-detail">
        <span>应用工作目录</span>
        <strong :title="userDir">{{ userDir }}</strong>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "DependencyHealthPanel",
  props: {
    database: {
      type: Object,
      default: null,
    },
    redis: {
      type: Object,
      default: null,
    },
    system: {
      type: Object,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    error: {
      type: String,
      default: "",
    },
  },
  emits: ["retry"],
  computed: {
    hasData() {
      return Boolean(this.database || this.redis);
    },
    services() {
      return [
        {
          key: "database",
          name: this.database?.databaseProductName || "数据库",
          statusText: this.database?.status || "UNKNOWN",
          level: this.getLevel(this.database?.status),
          tagType: this.getTagType(this.database?.status),
          errorMessage: this.database?.errorMessage || "",
          details: [
            {
              label: "版本",
              value: this.database?.databaseProductVersion || "—",
            },
            {
              label: "响应耗时",
              value: this.formatLatency(this.database?.responseTimeMs),
            },
            { label: "JDBC 地址", value: this.database?.jdbcUrl || "—" },
          ],
        },
        {
          key: "redis",
          name: "Redis",
          statusText: this.redis?.status || "UNKNOWN",
          level: this.getLevel(this.redis?.status),
          tagType: this.getTagType(this.redis?.status),
          errorMessage: this.redis?.errorMessage || "",
          details: [
            { label: "运行模式", value: this.redis?.mode || "—" },
            { label: "PING", value: this.redis?.ping || "—" },
            {
              label: "响应耗时",
              value: this.formatLatency(this.redis?.responseTimeMs),
            },
          ],
        },
      ];
    },
    userDir() {
      return this.system?.userDir || "—";
    },
  },
  methods: {
    getLevel(status) {
      const value = String(status || "").toUpperCase();
      if (value === "UP") return "normal";
      if (value === "DOWN") return "danger";
      return "unknown";
    },
    getTagType(status) {
      const value = String(status || "").toUpperCase();
      if (value === "UP") return "success";
      if (value === "DOWN") return "danger";
      return "info";
    },
    formatLatency(value) {
      const latency = Number(value);
      return Number.isFinite(latency) && latency >= 0 ? `${latency} ms` : "—";
    },
  },
};
</script>

<style lang="scss" scoped>
.monitor-panel {
  min-width: 0;
  padding: 20px;
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
    font-weight: 700;
    line-height: 26px;
  }

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 20px;
  }
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-width: 0;
}

.service-card {
  min-width: 0;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e8edf5;
  border-radius: 8px;
}

.service-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  > div {
    display: flex;
    align-items: center;
    min-width: 0;
  }

  strong {
    overflow: hidden;
    color: #0f172a;
    font-size: 15px;
    font-weight: 700;
    line-height: 22px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .el-tag {
    flex: 0 0 auto;
  }
}

.status-dot {
  flex: 0 0 8px;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  background: #64748b;
  border-radius: 50%;

  &.normal {
    background: #16a34a;
  }

  &.danger {
    background: #dc2626;
  }
}

dl {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  row-gap: 10px;
  margin: 0;
  min-width: 0;
}

dt,
dd {
  min-width: 0;
  font-size: 13px;
  line-height: 20px;
}

dt {
  color: #475569;
}

dd {
  margin: 0;
  overflow: hidden;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.service-card .el-alert {
  margin-top: 14px;
}

.directory-detail {
  display: grid;
  grid-column: 1 / -1;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 16px;
  min-width: 0;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e8edf5;
  border-radius: 8px;

  span,
  strong {
    min-width: 0;
    font-size: 13px;
    line-height: 20px;
  }

  span {
    color: #475569;
  }

  strong {
    overflow: hidden;
    color: #1e293b;
    font-family: Consolas, "Courier New", monospace;
    font-weight: 500;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 640px) {
  .monitor-panel {
    padding: 16px;
  }

  .service-list {
    grid-template-columns: minmax(0, 1fr);
  }

  .directory-detail {
    grid-column: auto;
  }
}
</style>
