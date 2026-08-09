<template>
  <div class="index-container">
    <div class="dashboard-page">
      <div class="metric-grid">
        <dashboard-metric-card
          v-for="item in metricCards"
          :key="item.label"
          :item="item"
        />
      </div>

      <div class="dashboard-main">
        <visit-trend-panel :period="trendPeriod" :series="visitTrend" />
        <quick-entry-panel :items="quickEntries" @open="handleQuickOpen" />
      </div>

      <div class="dashboard-bottom">
        <system-overview-panel
          :loading="systemOverviewLoading"
          :rows="systemOverview"
          :storage="storageUsage"
        />
        <notice-panel
          :items="notices"
          :loading="noticesLoading"
          @more="openNoticeCenter"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { getVisibleNoticeList } from "@/api/notice";
import { getSystemMonitorStatus } from "@/api/system/monitor";
import eventBus from "@/utils/eventBus";
import DashboardMetricCard from "./components/DashboardMetricCard.vue";
import NoticePanel from "./components/NoticePanel.vue";
import QuickEntryPanel from "./components/QuickEntryPanel.vue";
import SystemOverviewPanel from "./components/SystemOverviewPanel.vue";
import VisitTrendPanel from "./components/VisitTrendPanel.vue";

export default {
  name: "Index",
  components: {
    DashboardMetricCard,
    VisitTrendPanel,
    QuickEntryPanel,
    SystemOverviewPanel,
    NoticePanel,
  },
  data() {
    return {
      trendPeriod: "近7天",
      metricCards: [
        {
          label: "用户总数",
          value: "2,345",
          rate: "12.5%",
          increase: true,
          icon: "UserFilled",
          theme: "blue",
        },
        {
          label: "内容总数",
          value: "1,568",
          rate: "8.3%",
          increase: true,
          icon: "Tickets",
          theme: "green",
        },
        {
          label: "系统访问量",
          value: "12,689",
          rate: "15.6%",
          increase: true,
          icon: "Box",
          theme: "purple",
        },
        {
          label: "在线用户数",
          value: "156",
          rate: "5.1%",
          increase: true,
          icon: "PieChart",
          theme: "orange",
        },
      ],
      visitTrend: [
        { day: "05-10", uv: 9000, pv: 3800 },
        { day: "05-11", uv: 10700, pv: 5200 },
        { day: "05-12", uv: 12300, pv: 5300 },
        { day: "05-13", uv: 10300, pv: 4700 },
        { day: "05-14", uv: 11500, pv: 5000 },
        { day: "05-15", uv: 9900, pv: 4500 },
        { day: "05-16", uv: 10900, pv: 5100 },
      ],
      quickEntries: [
        {
          label: "用户管理",
          icon: "UserFilled",
          theme: "blue",
          path: "/system/user",
        },
        {
          label: "角色管理",
          icon: "Avatar",
          theme: "green",
          path: "/system/role",
        },
        {
          label: "日志管理",
          icon: "Tickets",
          theme: "orange",
          path: "/system/log",
        },
        {
          label: "部门管理",
          icon: "OfficeBuilding",
          theme: "purple",
          path: "/system/dept",
        },
        {
          label: "权限管理",
          icon: "Lock",
          theme: "slate",
          path: "/system/permission",
        },
        {
          label: "字典管理",
          icon: "Collection",
          theme: "blue",
          path: "/system/dict",
        },
        {
          label: "在线用户",
          icon: "Monitor",
          theme: "green",
          path: "/system/online",
        },
      ],
      systemOverview: [
        { label: "操作系统", value: "--" },
        { label: "系统架构", value: "--" },
        { label: "Java版本", value: "--" },
        { label: "数据库", value: "--" },
        { label: "采集时间", value: "--" },
        { label: "已运行时间", value: "--" },
      ],
      storageUsage: {
        percent: 0,
        used: "0 B",
        total: "0 B",
        free: "0 B",
      },
      systemOverviewLoading: true,
      notices: [],
      noticesLoading: true,
    };
  },
  created() {
    this.$loadDict("sys_notice_type");
    this.loadLatestNotices();
    this.loadSystemOverview();
    eventBus.on("notice-updated", this.loadLatestNotices);
  },
  beforeUnmount() {
    eventBus.off("notice-updated", this.loadLatestNotices);
  },
  methods: {
    formatBytes(bytes) {
      const value = Number(bytes);
      if (!Number.isFinite(value) || value <= 0) return "0 B";
      const units = ["B", "KB", "MB", "GB", "TB"];
      const unitIndex = Math.min(
        Math.floor(Math.log(value) / Math.log(1024)),
        units.length - 1
      );
      const formatted = value / 1024 ** unitIndex;
      return `${formatted.toFixed(formatted >= 10 || unitIndex === 0 ? 0 : 1)} ${
        units[unitIndex]
      }`;
    },
    formatUptime(milliseconds) {
      const totalMinutes = Math.floor(Number(milliseconds) / 60000);
      if (!Number.isFinite(totalMinutes) || totalMinutes < 0) return "--";
      const days = Math.floor(totalMinutes / 1440);
      const hours = Math.floor((totalMinutes % 1440) / 60);
      const minutes = totalMinutes % 60;
      return `${days}天 ${hours}时 ${minutes}分`;
    },
    buildSystemOverview(data) {
      data = data || {};
      const system = data.system || {};
      const jvm = data.jvm || {};
      const database = data.database || {};
      const disks = Array.isArray(data.disks) ? data.disks : [];
      const totals = disks.reduce(
        (result, disk) => ({
          total: result.total + (Number(disk.totalSpaceBytes) || 0),
          used: result.used + (Number(disk.usedSpaceBytes) || 0),
          free: result.free + (Number(disk.usableSpaceBytes) || 0),
        }),
        { total: 0, used: 0, free: 0 }
      );
      const percent = totals.total
        ? Math.min(
            100,
            Math.max(0, Math.round((totals.used / totals.total) * 100))
          )
        : 0;
      const joinValues = (...values) => values.filter(Boolean).join(" ") || "--";

      return {
        rows: [
          { label: "操作系统", value: joinValues(system.osName, system.osVersion) },
          { label: "系统架构", value: system.osArch || "--" },
          { label: "Java版本", value: jvm.javaVersion || "--" },
          {
            label: "数据库",
            value: joinValues(
              database.databaseProductName,
              database.databaseProductVersion
            ),
          },
          { label: "采集时间", value: data.collectTime || "--" },
          { label: "已运行时间", value: this.formatUptime(jvm.uptimeMs) },
        ],
        storage: {
          percent,
          used: this.formatBytes(totals.used),
          total: this.formatBytes(totals.total),
          free: this.formatBytes(totals.free),
        },
      };
    },
    async loadSystemOverview() {
      try {
        const response = await getSystemMonitorStatus();
        const overview = this.buildSystemOverview(response?.data || {});
        this.systemOverview = overview.rows;
        this.storageUsage = overview.storage;
      } catch (error) {
        // 公共请求层统一处理错误，首页保留占位数据。
      } finally {
        this.systemOverviewLoading = false;
      }
    },
    /** 获取当前用户最新可见公告，失败时由公告面板显示空状态。 */
    async loadLatestNotices() {
      this.noticesLoading = true;
      try {
        const { data } = await getVisibleNoticeList({ pageNum: 1, pageSize: 5 });
        this.notices = Array.isArray(data?.list) ? data.list : [];
      } catch (_error) {
        this.notices = [];
      } finally {
        this.noticesLoading = false;
      }
    },
    openNoticeCenter() {
      if (this.$route.path !== "/notice") this.$router.push("/notice");
    },
    handleQuickOpen(path) {
      if (!path || path === this.$route.path) return;
      this.$router.push(path);
    },
  },
};
</script>

<style lang="scss" scoped>
.index-container {
  min-height: $base-app-main-height;
  padding: 0 !important;
  margin: 0 !important;
  overflow: auto;
  color: #1f2937;
  background: #f4f7fb !important;
}

.dashboard-page {
  box-sizing: border-box;
  min-height: $base-app-main-height;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.dashboard-main,
.dashboard-bottom {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.dashboard-main {
  grid-template-columns: minmax(0, 1.24fr) minmax(360px, 0.76fr);
}

.dashboard-bottom {
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
}

.dashboard-footer {
  padding: 26px 0 2px;
  color: #8a98ad;
  font-size: 14px;
  text-align: center;
}

@media (max-width: 1380px) {
  .dashboard-page {
    padding: 18px;
  }
}

@media (max-width: 1180px) {
  .dashboard-main,
  .dashboard-bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard-page {
    padding: 12px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
