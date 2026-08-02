<template>
  <section class="monitor-panel disk-panel" :aria-busy="loading">
    <header class="panel-header">
      <div>
        <h2>磁盘使用率</h2>
        <p>按使用率从高到低展示文件系统容量</p>
      </div>
    </header>

    <el-skeleton v-if="loading && !hasData" :rows="6" animated />
    <el-result
      v-else-if="error && !hasData"
      icon="warning"
      title="磁盘状态加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重新加载</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!hasData" description="暂无磁盘数据" />
    <div v-else>
      <el-alert
        v-if="error"
        class="stale-alert"
        :closable="false"
        :title="error"
        type="warning"
        show-icon
      />
      <div
        class="chart-container"
        :style="chartStyle"
        role="img"
        :aria-label="chartAriaLabel"
      >
        <v-chart autoresize :option="chartOption" />
      </div>
      <ul class="threshold-legend" aria-label="磁盘使用率阈值说明">
        <li class="normal">
          <span aria-hidden="true"></span>
          <strong>正常</strong>
          <small>&lt;70%</small>
        </li>
        <li class="attention">
          <span aria-hidden="true"></span>
          <strong>关注</strong>
          <small>70–79.9%</small>
        </li>
        <li class="warning">
          <span aria-hidden="true"></span>
          <strong>警告</strong>
          <small>≥80%</small>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import VChart from "@/plugins/echarts";
import { formatBytes } from "../monitorUtils";

const toPercent = (value) => {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  if (!Number.isFinite(number) || number < 0) return null;
  return Math.min(100, number);
};

const formatOptionalBytes = (value) => {
  if (value === null || value === undefined || value === "") return "—";
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? formatBytes(number) : "—";
};

const escapeHtml = (value) =>
  String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");

export default {
  name: "DiskUsageChart",
  components: { VChart },
  props: {
    disks: {
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
      return this.disks.length > 0;
    },
    chartStyle() {
      const height = Math.min(480, Math.max(240, this.disks.length * 52));
      return { height: `${height}px` };
    },
    chartAriaLabel() {
      const validDisks = this.disks
        .map((disk) => ({
          name: disk?.name || "未命名磁盘",
          percent: toPercent(disk?.usedPercent),
        }))
        .filter((disk) => disk.percent !== null)
        .sort((a, b) => b.percent - a.percent);
      if (!validDisks.length) {
        return `磁盘使用率图表，共 ${this.disks.length} 个磁盘，暂无有效使用率数据`;
      }
      const highest = validDisks[0];
      return `磁盘使用率图表，共 ${this.disks.length} 个磁盘，最高为 ${highest.name} ${highest.percent.toFixed(1)}%`;
    },
    chartOption() {
      const disks = [...this.disks].sort((a, b) => {
        const percentA = toPercent(a?.usedPercent);
        const percentB = toPercent(b?.usedPercent);
        return (percentB ?? -1) - (percentA ?? -1);
      });

      const baseOption = {
        animation: !this.reducedMotion,
        animationDuration: this.reducedMotion ? 0 : 250,
        grid: { left: 88, right: 42, top: 12, bottom: 24 },
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          formatter: (params) => {
            const index = params[0]?.dataIndex || 0;
            const disk = disks[index] || {};
            const percent = toPercent(disk.usedPercent);
            return [
              `<strong>${escapeHtml(disk.name || "未命名磁盘")}</strong>`,
              `文件系统：${escapeHtml(disk.type || "—")}`,
              `使用率：${percent === null ? "—" : `${percent.toFixed(1)}%`}`,
              `已用：${formatOptionalBytes(disk.usedSpaceBytes)}`,
              `可用：${formatOptionalBytes(disk.usableSpaceBytes)}`,
              `总容量：${formatOptionalBytes(disk.totalSpaceBytes)}`,
            ].join("<br>");
          },
        },
        xAxis: {
          type: "value",
          min: 0,
          max: 100,
          axisLabel: { formatter: "{value}%" },
          splitLine: { lineStyle: { color: "#edf1f7" } },
        },
        yAxis: {
          type: "category",
          inverse: true,
          data: disks.map((item) => item?.name || "未命名磁盘"),
          axisTick: { show: false },
        },
        series: [
          {
            type: "bar",
            barWidth: 14,
            showBackground: true,
            backgroundStyle: {
              color: "#edf2f8",
              borderRadius: 8,
            },
            data: disks.map((item) => {
              const percent = toPercent(item?.usedPercent);
              const data = {
                value: percent,
                valid: percent !== null,
              };
              if (percent !== null) {
                data.itemStyle = {
                  color:
                    percent >= 80
                      ? "#ef5656"
                      : percent >= 70
                        ? "#e6a23c"
                        : "#2f7cf6",
                  borderRadius: 8,
                };
              }
              return data;
            }),
            label: {
              show: true,
              position: "right",
              formatter: ({ data, value }) =>
                data.valid ? `${Number(value).toFixed(1)}%` : "—",
            },
          },
        ],
      };

      return {
        baseOption,
        media: [
          {
            query: { maxWidth: 480 },
            option: {
              grid: { left: 62, right: 30, top: 12, bottom: 24 },
              xAxis: {
                splitNumber: 2,
                axisLabel: { formatter: "{value}%", hideOverlap: true },
              },
            },
          },
          {
            option: {
              grid: { left: 88, right: 42, top: 12, bottom: 24 },
              xAxis: {
                splitNumber: 5,
                axisLabel: {
                  formatter: "{value}%",
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
    formatBytes,
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

.chart-container {
  width: 100%;
  min-width: 0;
  min-height: 240px;
  max-height: 480px;
}

.threshold-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px 24px;
  margin: 10px 0 0;
  padding: 12px 0 0;
  border-top: 1px solid #edf1f7;
  list-style: none;

  li {
    display: inline-flex;
    align-items: center;
    color: #475569;
    font-size: 12px;
    line-height: 18px;
  }

  li > span {
    width: 8px;
    height: 8px;
    margin-right: 7px;
    background: #2f7cf6;
    border-radius: 50%;
  }

  strong {
    margin-right: 5px;
    color: #1e293b;
    font-weight: 600;
  }

  small {
    color: #64748b;
    font-size: inherit;
  }

  .attention > span {
    background: #e6a23c;
  }

  .warning > span {
    background: #ef5656;
  }
}

@media (max-width: 480px) {
  .monitor-panel {
    padding: 16px;
  }
}
</style>
