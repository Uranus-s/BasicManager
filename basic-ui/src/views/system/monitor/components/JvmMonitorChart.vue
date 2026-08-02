<template>
  <section class="monitor-panel jvm-panel" :aria-busy="loading">
    <header class="panel-header">
      <div>
        <h2>JVM 运行状态</h2>
        <p>堆内存使用率与线程数量趋势</p>
      </div>
    </header>

    <el-skeleton v-if="loading && !hasData" :rows="7" animated />
    <el-result
      v-else-if="error && !hasData"
      icon="warning"
      title="JVM 状态加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重新加载</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!hasData" description="暂无 JVM 数据" />
    <div v-else>
      <el-alert
        v-if="error"
        class="stale-alert"
        :closable="false"
        :title="error"
        type="warning"
        show-icon
      />
      <div class="jvm-content">
        <div class="heap-summary">
          <div
            class="heap-gauge"
            :style="{ '--heap-percent': heapPercent }"
            role="img"
            :aria-label="heapAriaLabel"
          >
            <div class="heap-gauge__center">
              <strong>{{ heapPercentText }}</strong>
              <span>堆内存</span>
            </div>
          </div>
          <dl>
            <div>
              <dt>已用</dt>
              <dd>{{ heapUsedText }}</dd>
            </div>
            <div>
              <dt>最大</dt>
              <dd>{{ heapMaxText }}</dd>
            </div>
            <div>
              <dt>非堆已用</dt>
              <dd>{{ nonHeapUsedText }}</dd>
            </div>
            <div>
              <dt>当前线程</dt>
              <dd>{{ threadCountText }}</dd>
            </div>
          </dl>
        </div>

        <div class="trend-area">
          <div
            class="chart-container"
            role="img"
            :aria-label="chartAriaLabel"
          >
            <v-chart autoresize :option="chartOption" />
          </div>
          <p v-if="trend.length < 2" class="trend-hint">
            正在积累趋势数据
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import VChart from "@/plugins/echarts";
import { formatBytes, formatPercent } from "../monitorUtils";

const toFiniteNonNegative = (value) => {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? number : null;
};

export default {
  name: "JvmMonitorChart",
  components: { VChart },
  props: {
    jvm: {
      type: Object,
      default: null,
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
      return Boolean(this.jvm);
    },
    heapValues() {
      return {
        used: toFiniteNonNegative(this.jvm?.heapUsedBytes),
        max: toFiniteNonNegative(this.jvm?.heapMaxBytes),
      };
    },
    hasValidHeap() {
      return (
        this.heapValues.used !== null &&
        this.heapValues.max !== null &&
        this.heapValues.max > 0
      );
    },
    heapPercent() {
      if (!this.hasValidHeap) return 0;
      return Math.min(
        100,
        Math.max(0, (this.heapValues.used / this.heapValues.max) * 100)
      );
    },
    heapPercentText() {
      return this.hasValidHeap ? formatPercent(this.heapPercent) : "—";
    },
    heapUsedText() {
      return this.heapValues.used === null
        ? "—"
        : formatBytes(this.heapValues.used);
    },
    heapMaxText() {
      return this.heapValues.max === null
        ? "—"
        : formatBytes(this.heapValues.max);
    },
    nonHeapUsedText() {
      const value = toFiniteNonNegative(this.jvm?.nonHeapUsedBytes);
      return value === null ? "—" : formatBytes(value);
    },
    threadCount() {
      return toFiniteNonNegative(this.jvm?.threadCount);
    },
    threadCountText() {
      return this.threadCount === null ? "—" : String(this.threadCount);
    },
    heapAriaLabel() {
      if (!this.hasValidHeap) return "堆内存使用率暂无有效数据";
      return `堆内存使用率 ${this.heapPercentText}，已用 ${this.heapUsedText}，最大 ${this.heapMaxText}`;
    },
    chartAriaLabel() {
      const heapText = this.hasValidHeap
        ? this.heapPercentText
        : "暂无有效数据";
      return `JVM 趋势图，当前堆内存使用率 ${heapText}，当前线程数 ${this.threadCountText}`;
    },
    chartOption() {
      const heapData = this.trend.map((item) => {
        const value = toFiniteNonNegative(item?.heapPercent);
        return value === null ? null : Math.min(100, value);
      });
      const threadData = this.trend.map((item) =>
        toFiniteNonNegative(item?.threadCount)
      );

      return {
        animation: !this.reducedMotion,
        animationDuration: this.reducedMotion ? 0 : 250,
        tooltip: { trigger: "axis" },
        legend: {
          top: 0,
          data: ["堆内存使用率", "线程数"],
        },
        grid: { left: 58, right: 54, top: 52, bottom: 30 },
        xAxis: {
          type: "category",
          boundaryGap: false,
          data: this.trend.map((item) => item?.time || ""),
        },
        yAxis: [
          {
            type: "value",
            name: "堆内存使用率",
            min: 0,
            max: 100,
            axisLabel: { formatter: "{value}%" },
            splitLine: { lineStyle: { color: "#edf1f7" } },
          },
          {
            type: "value",
            name: "线程数",
            minInterval: 1,
            splitLine: { show: false },
          },
        ],
        series: [
          {
            name: "堆内存使用率",
            type: "line",
            smooth: true,
            showSymbol: this.trend.length <= 1,
            data: heapData,
            lineStyle: { width: 3, color: "#2f7cf6" },
            areaStyle: { color: "rgba(47,124,246,.12)" },
          },
          {
            name: "线程数",
            type: "line",
            smooth: true,
            showSymbol: this.trend.length <= 1,
            yAxisIndex: 1,
            data: threadData,
            lineStyle: { width: 2, color: "#20bf8f" },
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
    formatBytes,
    formatPercent,
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

.jvm-content {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
  min-width: 0;
}

.heap-summary {
  min-width: 0;
}

.heap-gauge {
  display: grid;
  width: 152px;
  height: 152px;
  margin: 0 auto 20px;
  background:
    radial-gradient(circle at center, #fff 0 58%, transparent 59%),
    conic-gradient(
      #2f7cf6 calc(var(--heap-percent) * 1%),
      #edf2f8 0
    );
  border-radius: 50%;
  place-items: center;
}

.heap-gauge__center {
  display: flex;
  flex-direction: column;
  align-items: center;

  strong {
    color: #0f172a;
    font-size: 28px;
    font-variant-numeric: tabular-nums;
    font-weight: 700;
    line-height: 36px;
  }

  span {
    margin-top: 2px;
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }
}

.heap-summary dl {
  display: grid;
  gap: 8px;
  margin: 0;

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  dt,
  dd {
    font-size: 13px;
    line-height: 20px;
  }

  dt {
    color: #64748b;
  }

  dd {
    margin: 0;
    color: #1e293b;
    font-variant-numeric: tabular-nums;
    font-weight: 600;
    text-align: right;
  }
}

.trend-area {
  min-width: 0;
}

.chart-container {
  width: 100%;
  height: 260px;
  min-width: 0;
}

.trend-hint {
  margin: -22px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}

@media (max-width: 760px) {
  .jvm-content {
    grid-template-columns: minmax(0, 1fr);
  }

  .heap-summary {
    display: grid;
    grid-template-columns: 152px minmax(0, 220px);
    justify-content: center;
    gap: 24px;
    align-items: center;
  }

  .heap-gauge {
    margin-bottom: 0;
  }
}

@media (max-width: 480px) {
  .monitor-panel {
    padding: 16px;
  }

  .heap-summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .heap-gauge {
    margin-bottom: 4px;
  }
}
</style>
