<template>
  <section class="monitor-panel thread-pool-panel" :aria-busy="loading">
    <header class="panel-header">
      <div>
        <h2>线程池监控</h2>
        <p>执行器负载、活动会话趋势与任务明细</p>
      </div>
    </header>

    <el-skeleton
      v-if="loading && !hasData"
      :rows="9"
      :animated="!reducedMotion"
    />
    <el-result
      v-else-if="error && !hasData"
      icon="warning"
      title="线程池数据加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button
          class="retry-button"
          type="primary"
          @click="$emit('retry')"
        >
          重新加载
        </el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!hasData" description="暂无受管线程池" />
    <div v-else>
      <el-alert
        v-if="error"
        class="stale-alert"
        :closable="false"
        :title="error"
        type="warning"
        show-icon
      />

      <div class="chart-grid">
        <article class="chart-card">
          <header class="chart-heading">
            <h3>实时负载</h3>
            <p>线程与任务队列当前占用比例</p>
          </header>
          <div
            class="chart-container"
            role="img"
            :aria-label="loadChartAriaLabel"
          >
            <v-chart autoresize :option="loadOption" />
          </div>
        </article>

        <article class="chart-card">
          <header class="chart-heading">
            <h3>活动会话趋势</h3>
            <p>各执行器活动任务数随时间变化</p>
          </header>
          <div
            class="chart-container"
            role="img"
            :aria-label="trendChartAriaLabel"
          >
            <v-chart autoresize :option="trendOption" />
          </div>
        </article>
      </div>

      <div class="table-heading">
        <div>
          <h3>执行器明细</h3>
          <p>线程、队列与任务累计计数</p>
        </div>
        <span>{{ executors.length }} 个执行器</span>
      </div>
      <div
        class="table-scroll"
        role="region"
        tabindex="0"
        aria-label="线程池执行器明细表"
      >
        <el-table class="thread-table" :data="executors">
          <el-table-column
            prop="name"
            label="执行器"
            min-width="190"
            fixed="left"
          />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 'RUNNING' ? 'success' : 'danger'"
                effect="light"
              >
                {{ row.status || "UNKNOWN" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="type"
            label="类型"
            width="110"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="threadNamePrefix"
            label="线程前缀"
            min-width="150"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="corePoolSize"
            label="核心线程"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="maximumPoolSize"
            label="最大线程"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="poolSize"
            label="当前线程"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="largestPoolSize"
            label="历史最大"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="activeCount"
            label="活动任务"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="queueSize"
            label="队列任务"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="queueCapacity"
            label="队列容量"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="queueRemainingCapacity"
            label="剩余容量"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="submittedTaskCount"
            label="已提交"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="completedTaskCount"
            label="已完成"
            width="105"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="failedTaskCount"
            label="失败"
            width="90"
            :formatter="formatNullable"
          />
          <el-table-column
            prop="rejectedTaskCount"
            label="拒绝"
            width="90"
            :formatter="formatNullable"
          />
        </el-table>
      </div>
    </div>
  </section>
</template>

<script>
import VChart from "@/plugins/echarts";
import { getQueueLoad, getThreadPoolLoad } from "../monitorUtils";

const escapeHtml = (value) =>
  String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");

const formatLoadValue = (value) => {
  if (value === null || value === undefined || value === "") return "不适用";
  const number = Number(value);
  return Number.isFinite(number) ? `${number.toFixed(1)}%` : "不适用";
};

const abbreviateExecutorName = (value) => {
  const name = String(value || "");
  return name.length > 10 ? `${name.slice(0, 9)}…` : name;
};

const formatCompactTime = (value) => {
  const label = String(value || "");
  const match = label.match(/(?:T|\s)(\d{2}:\d{2}(?::\d{2})?)/);
  return match?.[1] || label;
};

const keepAxisLabel = (value) => value;

export default {
  name: "ThreadPoolPanel",
  components: { VChart },
  props: {
    executors: {
      type: Array,
      default: () => [],
    },
    trend: {
      type: Array,
      default: () => [],
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
  data() {
    return {
      reducedMotion: false,
      motionMedia: null,
    };
  },
  computed: {
    hasData() {
      return this.executors.length > 0;
    },
    loadChartAriaLabel() {
      const values = this.executors.flatMap((executor) => {
        const name = executor?.name || "未命名执行器";
        return [
          { name, type: "线程", value: getThreadPoolLoad(executor).percent },
          { name, type: "队列", value: getQueueLoad(executor).percent },
        ];
      });
      const available = values.filter((item) => item.value !== null);
      if (!available.length) {
        return `线程池实时负载图，共 ${this.executors.length} 个执行器，当前负载均不适用`;
      }
      const highest = available.reduce((max, item) =>
        item.value > max.value ? item : max
      );
      return `线程池实时负载图，共 ${this.executors.length} 个执行器，最高为 ${highest.name}${highest.type}负载 ${highest.value.toFixed(1)}%`;
    },
    trendChartAriaLabel() {
      if (!this.trend.length) {
        return `线程池活动会话趋势图，共 ${this.executors.length} 个执行器，正在积累趋势数据`;
      }
      return `线程池活动会话趋势图，共 ${this.executors.length} 个执行器、${this.trend.length} 个时间点`;
    },
    loadOption() {
      const items = this.executors.map((executor) => ({
        name: executor?.name || "未命名执行器",
        thread: getThreadPoolLoad(executor),
        queue: getQueueLoad(executor),
      }));

      const baseOption = {
        animation: !this.reducedMotion,
        animationDuration: this.reducedMotion ? 0 : 250,
        grid: { left: 150, right: 42, top: 20, bottom: 44 },
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          formatter: (params) => {
            const rows = params.map(
              (item) =>
                `${item.marker || ""}${escapeHtml(item.seriesName)}：${formatLoadValue(item.value)}`
            );
            const name = params[0]?.name || params[0]?.axisValueLabel || "";
            return [
              `<strong>${escapeHtml(name || "未命名执行器")}</strong>`,
              ...rows,
            ].join("<br>");
          },
        },
        legend: { bottom: 0, data: ["线程负载", "队列负载"] },
        xAxis: {
          type: "value",
          min: 0,
          max: 100,
          axisLabel: { formatter: "{value}%" },
          splitLine: { lineStyle: { color: "#edf1f7" } },
        },
        yAxis: {
          type: "category",
          data: items.map((item) => item.name),
          axisTick: { show: false },
        },
        series: [
          {
            name: "线程负载",
            type: "bar",
            barMaxWidth: 12,
            data: items.map((item) => item.thread.percent),
            itemStyle: { color: "#2f7cf6", borderRadius: 6 },
          },
          {
            name: "队列负载",
            type: "bar",
            barMaxWidth: 12,
            data: items.map((item) => item.queue.percent),
            itemStyle: { color: "#20bf8f", borderRadius: 6 },
          },
        ],
      };

      return {
        baseOption,
        media: [
          {
            query: { maxWidth: 480 },
            option: {
              grid: { left: 82, right: 12, top: 20, bottom: 44 },
              xAxis: {
                splitNumber: 2,
                axisLabel: { formatter: "{value}%", hideOverlap: true },
              },
              yAxis: {
                axisLabel: { formatter: abbreviateExecutorName },
              },
            },
          },
          {
            option: {
              grid: { left: 150, right: 42, top: 20, bottom: 44 },
              xAxis: {
                splitNumber: 5,
                axisLabel: {
                  formatter: "{value}%",
                  hideOverlap: false,
                },
              },
              yAxis: {
                axisLabel: { formatter: keepAxisLabel },
              },
            },
          },
        ],
      };
    },
    trendOption() {
      const names = [
        ...new Set(
          this.executors.map((item) => item?.name || "未命名执行器")
        ),
      ];

      const baseOption = {
        animation: !this.reducedMotion,
        animationDuration: this.reducedMotion ? 0 : 250,
        tooltip: { trigger: "axis" },
        legend: { type: "scroll", top: 0, data: names },
        grid: { left: 48, right: 20, top: 44, bottom: 30 },
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: this.trend.map((item) => item?.time || ""),
        },
        yAxis: {
          type: "value",
          min: 0,
          minInterval: 1,
          splitLine: { lineStyle: { color: "#edf1f7" } },
        },
        series: names.map((name) => ({
          name,
          type: "line",
          smooth: true,
          showSymbol: this.trend.length <= 1,
          data: this.trend.map((item) => item?.executors?.[name] ?? null),
        })),
      };

      return {
        baseOption,
        media: [
          {
            query: { maxWidth: 480 },
            option: {
              grid: { left: 38, right: 12, top: 44, bottom: 30 },
              xAxis: {
                axisLabel: {
                  formatter: formatCompactTime,
                  hideOverlap: true,
                },
              },
            },
          },
          {
            option: {
              grid: { left: 48, right: 20, top: 44, bottom: 30 },
              xAxis: {
                axisLabel: {
                  formatter: keepAxisLabel,
                  hideOverlap: false,
                },
              },
            },
          },
        ],
      };
    },
  },
  mounted() {
    if (typeof window === "undefined" || !window.matchMedia) return;
    this.motionMedia = window.matchMedia("(prefers-reduced-motion: reduce)");
    this.handleMotionPreference(this.motionMedia);
    if (this.motionMedia.addEventListener) {
      this.motionMedia.addEventListener("change", this.handleMotionPreference);
    } else {
      this.motionMedia.addListener(this.handleMotionPreference);
    }
  },
  beforeUnmount() {
    if (!this.motionMedia) return;
    if (this.motionMedia.removeEventListener) {
      this.motionMedia.removeEventListener(
        "change",
        this.handleMotionPreference
      );
    } else {
      this.motionMedia.removeListener(this.handleMotionPreference);
    }
  },
  methods: {
    formatNullable(row, column, cellValue) {
      return cellValue === null ||
        cellValue === undefined ||
        cellValue === ""
        ? "—"
        : cellValue;
    },
    handleMotionPreference(event) {
      this.reducedMotion = Boolean(event.matches);
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

.stale-alert {
  margin-bottom: 16px;
}

.retry-button {
  min-width: 96px;
  min-height: 44px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-width: 0;
}

.chart-card {
  min-width: 0;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e8edf5;
  border-radius: 8px;
}

.chart-heading,
.table-heading {
  h3 {
    margin: 0;
    color: #0f172a;
    font-size: 15px;
    font-weight: 700;
    line-height: 22px;
  }

  p {
    margin: 2px 0 0;
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }
}

.chart-container {
  width: 100%;
  height: 260px;
  min-width: 0;
  margin-top: 8px;
}

.table-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin: 24px 0 12px;

  > span {
    flex: 0 0 auto;
    color: #475569;
    font-size: 12px;
    font-variant-numeric: tabular-nums;
    line-height: 18px;
  }
}

.table-scroll {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  outline: none;
  overscroll-behavior-inline: contain;

  &:focus-visible {
    box-shadow: 0 0 0 3px rgba(47, 124, 246, 0.2);
  }
}

.thread-table {
  width: 100%;
  min-width: 1580px;

  :deep(.el-table__cell) {
    font-variant-numeric: tabular-nums;
  }

  :deep(.el-table__header-wrapper th) {
    color: #475569;
    font-weight: 600;
  }
}

@media (max-width: 900px) {
  .chart-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 480px) {
  .monitor-panel {
    padding: 16px;
  }

  .chart-card {
    padding: 12px;
  }
}
</style>
