<template>
  <div class="monitor-container">
    <section class="monitor-hero">
      <div class="hero-summary">
        <div class="title-row">
          <h1>服务监控</h1>
          <el-tag
            :aria-label="`总体健康状态：${overallHealth.label}`"
            :type="healthTagType"
            effect="light"
          >
            {{ overallHealth.label }}
          </el-tag>
        </div>
        <p>
          <span>{{ healthDescription }}</span>
          <span v-if="lastCollectTime" class="collect-time">
            最近采集：{{ lastCollectTime }}
          </span>
        </p>
      </div>
      <div class="hero-actions">
        <span class="auto-refresh">每 10 秒自动刷新</span>
        <el-button
          type="primary"
          :loading="refreshing"
          :disabled="refreshing"
          @click="refreshAll"
        >
          <el-icon><Refresh /></el-icon>
          立即刷新
        </el-button>
      </div>
    </section>

    <monitor-metric-cards :items="metricItems" />

    <div class="primary-grid">
      <jvm-monitor-chart
        :jvm="statusData?.jvm"
        :trend="jvmTrend"
        :loading="statusLoading"
        :error="statusError"
        @retry="refreshAll"
      />
      <dependency-health-panel
        :database="statusData?.database"
        :redis="statusData?.redis"
        :system="statusData?.system"
        :loading="statusLoading"
        :error="statusError"
        @retry="refreshAll"
      />
    </div>

    <disk-usage-chart
      :disks="disks"
      :loading="statusLoading"
      :error="statusError"
      @retry="refreshAll"
    />

    <thread-pool-panel
      :executors="executors"
      :trend="threadTrend"
      :loading="threadLoading"
      :error="threadError"
      @retry="refreshAll"
    />
  </div>
</template>

<script>
import {
  getSystemMonitorStatus,
  getThreadPoolMonitorStatus,
} from "@/api/system/monitor";
import DependencyHealthPanel from "./components/DependencyHealthPanel.vue";
import DiskUsageChart from "./components/DiskUsageChart.vue";
import JvmMonitorChart from "./components/JvmMonitorChart.vue";
import MonitorMetricCards from "./components/MonitorMetricCards.vue";
import ThreadPoolPanel from "./components/ThreadPoolPanel.vue";
import {
  appendTrendPoint,
  formatDuration,
  getLatestCollectTime,
  getOverallHealth,
} from "./monitorUtils";

const isRecord = (value) =>
  value !== null && typeof value === "object" && !Array.isArray(value);

const toRecordArray = (value) =>
  Array.isArray(value) ? value.filter(isRecord) : [];

const hasNumericValue = (value) =>
  value !== null &&
  value !== undefined &&
  value !== "" &&
  Number.isFinite(Number(value));

const toTrendValue = (value) => {
  if (
    typeof value !== "number" &&
    (typeof value !== "string" || !value.trim())
  ) {
    return null;
  }
  const number = Number(
    typeof value === "string" ? value.trim() : value
  );
  return Number.isFinite(number) && number >= 0 ? number : null;
};

export default {
  name: "SystemMonitor",
  components: {
    DependencyHealthPanel,
    DiskUsageChart,
    JvmMonitorChart,
    MonitorMetricCards,
    ThreadPoolPanel,
  },
  data() {
    return {
      statusData: null,
      threadPoolData: null,
      previousExecutors: [],
      statusLoading: true,
      threadLoading: true,
      statusError: "",
      threadError: "",
      refreshing: false,
      refreshPending: false,
      unmounted: false,
      pollTimer: null,
      jvmTrend: [],
      threadTrend: [],
    };
  },
  computed: {
    disks() {
      return toRecordArray(this.statusData?.disks);
    },
    executors() {
      return toRecordArray(this.threadPoolData?.executors);
    },
    overallHealth() {
      return getOverallHealth({
        status: this.statusData,
        executors: this.executors,
        previousExecutors: this.previousExecutors,
      });
    },
    healthTagType() {
      return (
        {
          normal: "success",
          warning: "warning",
          danger: "danger",
          unknown: "info",
        }[this.overallHealth.level] || "info"
      );
    },
    healthDescription() {
      const reasons = Array.isArray(this.overallHealth.reasons)
        ? this.overallHealth.reasons
        : [];
      return reasons.length
        ? reasons.slice(0, 2).join("；")
        : "系统、依赖服务与执行器运行状态概览。";
    },
    lastCollectTime() {
      return getLatestCollectTime(
        this.statusData?.collectTime,
        this.threadPoolData?.collectTime
      );
    },
    metricItems() {
      const system = this.statusData?.system || {};
      const jvm = this.statusData?.jvm || {};
      return [
        {
          label: "应用状态",
          value: this.overallHealth.label,
          detail: system.applicationName || "BasicManage",
          icon: "CircleCheckFilled",
          tone: this.overallHealth.level,
        },
        {
          label: "JVM 运行",
          value: formatDuration(jvm.uptimeMs),
          detail: jvm.javaVersion ? `Java ${jvm.javaVersion}` : "Java —",
          icon: "Timer",
          tone: "primary",
        },
        {
          label: "当前线程",
          value: hasNumericValue(jvm.threadCount)
            ? String(jvm.threadCount)
            : "—",
          detail: `守护线程 ${
            hasNumericValue(jvm.daemonThreadCount)
              ? jvm.daemonThreadCount
              : "—"
          }`,
          icon: "Connection",
          tone: "success",
        },
        {
          label: "处理器",
          value: hasNumericValue(system.availableProcessors)
            ? String(system.availableProcessors)
            : "—",
          detail: system.osArch || "架构未知",
          icon: "Cpu",
          tone: "warning",
        },
      ];
    },
  },
  created() {
    this.refreshAll();
  },
  mounted() {
    if (typeof document !== "undefined") {
      document.addEventListener(
        "visibilitychange",
        this.handleVisibilityChange
      );
    }
    this.startPolling();
  },
  beforeUnmount() {
    this.unmounted = true;
    this.refreshPending = false;
    this.stopPolling();
    if (typeof document !== "undefined") {
      document.removeEventListener(
        "visibilitychange",
        this.handleVisibilityChange
      );
    }
  },
  methods: {
    normalizeStatusData(response) {
      const data = response?.data;
      if (!isRecord(data)) return null;
      return {
        ...data,
        system: isRecord(data.system) ? data.system : null,
        jvm: isRecord(data.jvm) ? data.jvm : null,
        disks: toRecordArray(data.disks),
        database: isRecord(data.database) ? data.database : null,
        redis: isRecord(data.redis) ? data.redis : null,
      };
    },
    normalizeThreadPoolData(response) {
      const data = response?.data;
      if (!isRecord(data)) return null;
      return {
        ...data,
        executors: toRecordArray(data.executors),
      };
    },
    async refreshAll() {
      if (this.unmounted) return;
      if (this.refreshing) return;
      this.refreshing = true;
      const firstStatusLoad = !this.statusData;
      const firstThreadLoad = !this.threadPoolData;
      if (firstStatusLoad) this.statusLoading = true;
      if (firstThreadLoad) this.threadLoading = true;

      try {
        const [statusResult, threadResult] = await Promise.allSettled([
          Promise.resolve().then(() => getSystemMonitorStatus()),
          Promise.resolve().then(() => getThreadPoolMonitorStatus()),
        ]);

        if (this.unmounted) return;

        if (statusResult.status === "fulfilled") {
          const nextStatusData = this.normalizeStatusData(statusResult.value);
          if (nextStatusData) {
            this.statusData = nextStatusData;
            this.statusError = "";
            this.captureJvmTrend();
          } else {
            this.statusError = "系统状态暂时无法获取，请稍后重试";
          }
        } else {
          this.statusError = "系统状态暂时无法获取，请稍后重试";
        }

        if (threadResult.status === "fulfilled") {
          const nextThreadPoolData = this.normalizeThreadPoolData(
            threadResult.value
          );
          if (nextThreadPoolData) {
            this.previousExecutors = this.executors;
            this.threadPoolData = nextThreadPoolData;
            this.threadError = "";
            this.captureThreadTrend();
          } else {
            this.threadError = "线程池状态暂时无法获取，请稍后重试";
          }
        } else {
          this.threadError = "线程池状态暂时无法获取，请稍后重试";
        }
      } finally {
        if (!this.unmounted) {
          this.statusLoading = false;
          this.threadLoading = false;
          this.refreshing = false;
          if (this.refreshPending) {
            this.refreshPending = false;
            if (
              typeof document === "undefined" ||
              !document.hidden
            ) {
              this.refreshAll();
            }
          }
        }
      }
    },
    captureJvmTrend() {
      const jvm = this.statusData?.jvm;
      if (!isRecord(jvm)) return;
      const used = toTrendValue(jvm.heapUsedBytes);
      const max = toTrendValue(jvm.heapMaxBytes);
      const heapPercent =
        used !== null && max !== null && max > 0
          ? Number(((used / max) * 100).toFixed(2))
          : null;
      this.jvmTrend = appendTrendPoint(this.jvmTrend, {
        time:
          this.statusData.collectTime || new Date().toLocaleTimeString(),
        heapPercent,
        threadCount: toTrendValue(jvm.threadCount),
      });
    },
    captureThreadTrend() {
      const executors = toRecordArray(this.threadPoolData?.executors);
      this.threadTrend = appendTrendPoint(this.threadTrend, {
        time:
          this.threadPoolData?.collectTime ||
          new Date().toLocaleTimeString(),
        executors: Object.fromEntries(
          executors
            .filter(
              (item) =>
                typeof item.name === "string" && item.name.trim().length
            )
            .map((item) => [item.name, toTrendValue(item.activeCount)])
        ),
      });
    },
    startPolling() {
      this.stopPolling();
      if (
        typeof window === "undefined" ||
        (typeof document !== "undefined" && document.hidden)
      ) {
        return;
      }
      this.pollTimer = window.setInterval(this.refreshAll, 10000);
    },
    stopPolling() {
      if (this.pollTimer !== null) {
        window.clearInterval(this.pollTimer);
        this.pollTimer = null;
      }
    },
    handleVisibilityChange() {
      if (typeof document !== "undefined" && document.hidden) {
        this.refreshPending = false;
        this.stopPolling();
        return;
      }
      if (this.refreshing) {
        this.refreshPending = true;
      } else {
        this.refreshAll();
      }
      this.startPolling();
    },
  },
};
</script>

<style lang="scss" scoped>
.monitor-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  min-height: $base-app-main-height;
  padding: 20px;
  overflow-x: hidden;
  background: #f4f7fb;
}

.monitor-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-width: 0;
  padding: 20px 24px;
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.hero-summary {
  min-width: 0;
}

.title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;

  h1 {
    margin: 0;
    color: #0f172a;
    font-size: 22px;
    font-weight: 800;
    line-height: 32px;
  }
}

.hero-summary > p {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
  overflow-wrap: anywhere;
}

.collect-time {
  color: #475569;
  font-variant-numeric: tabular-nums;
}

.hero-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 16px;

  :deep(.el-button) {
    min-height: 44px;
  }
}

.auto-refresh {
  color: #64748b;
  font-size: 13px;
  line-height: 20px;
  white-space: nowrap;
}

.primary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 16px;
  min-width: 0;

  > * {
    min-width: 0;
  }
}

:deep(.el-button:focus-visible) {
  outline: 3px solid rgba(47, 124, 246, 0.28);
  outline-offset: 2px;
}

@media (max-width: 1180px) {
  .primary-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .monitor-container {
    padding: 12px;
  }

  .monitor-hero {
    align-items: stretch;
    padding: 18px 16px;
    flex-direction: column;
    gap: 16px;
  }

  .hero-actions {
    align-items: stretch;
    flex-direction: column;
    gap: 8px;
  }

  .auto-refresh {
    white-space: normal;
  }

  .hero-actions :deep(.el-button) {
    width: 100%;
    margin: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .monitor-container,
  .monitor-container :deep(*),
  .monitor-container :deep(*::before),
  .monitor-container :deep(*::after) {
    animation: none !important;
    scroll-behavior: auto !important;
    transition: none !important;
  }
}
</style>
