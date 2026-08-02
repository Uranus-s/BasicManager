<template>
  <section class="dashboard-panel trend-panel">
    <div class="dashboard-panel-header">
      <h3>访问趋势</h3>
      <div class="chart-actions">
        <span class="legend uv"><i></i>访问量（UV）</span>
        <span class="legend pv"><i></i>访问量（PV）</span>
        <button type="button">
          {{ period }}
          <el-icon><ArrowDown /></el-icon>
        </button>
      </div>
    </div>

    <div class="trend-chart" role="img" aria-label="最近7天访问趋势">
      <svg viewBox="0 0 620 260" preserveAspectRatio="none">
        <defs>
          <linearGradient id="uvGradient" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#2f7cf6" stop-opacity="0.18" />
            <stop offset="100%" stop-color="#2f7cf6" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="pvGradient" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#20bf8f" stop-opacity="0.16" />
            <stop offset="100%" stop-color="#20bf8f" stop-opacity="0" />
          </linearGradient>
        </defs>
        <g class="grid-lines">
          <line
            v-for="(_, index) in yAxisLabels"
            :key="index"
            x1="48"
            x2="588"
            :y1="40 + index * 38"
            :y2="40 + index * 38"
          />
        </g>
        <g class="axis-labels">
          <text
            v-for="(label, index) in yAxisLabels"
            :key="label"
            x="6"
            :y="44 + index * 38"
          >
            {{ label.toLocaleString() }}
          </text>
        </g>
        <polygon :points="areaUv" fill="url(#uvGradient)" />
        <polygon :points="areaPv" fill="url(#pvGradient)" />
        <polyline :points="pointsUv" class="uv-line" />
        <polyline :points="pointsPv" class="pv-line" />
        <g>
          <circle
            v-for="item in pointListUv"
            :key="'uv-' + item"
            :cx="item.split(',')[0]"
            :cy="item.split(',')[1]"
            r="4"
            class="uv-dot"
          />
          <circle
            v-for="item in pointListPv"
            :key="'pv-' + item"
            :cx="item.split(',')[0]"
            :cy="item.split(',')[1]"
            r="4"
            class="pv-dot"
          />
        </g>
      </svg>
      <div class="x-axis">
        <span v-for="item in series" :key="item.day">{{ item.day }}</span>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "VisitTrendPanel",
  props: {
    period: {
      type: String,
      required: true,
    },
    series: {
      type: Array,
      required: true,
    },
  },
  computed: {
    maxValue() {
      return Math.max(
        1,
        ...this.series.flatMap((item) => [item.uv, item.pv])
      );
    },
    pointsUv() {
      return this.buildPoints("uv");
    },
    pointsPv() {
      return this.buildPoints("pv");
    },
    pointListUv() {
      return this.pointsUv.split(" ");
    },
    pointListPv() {
      return this.pointsPv.split(" ");
    },
    areaUv() {
      return `48,230 ${this.pointsUv} 588,230`;
    },
    areaPv() {
      return `48,230 ${this.pointsPv} 588,230`;
    },
    yAxisLabels() {
      return [15000, 12000, 9000, 6000, 3000, 0];
    },
  },
  methods: {
    buildPoints(key) {
      const count = this.series.length - 1 || 1;
      return this.series
        .map((item, index) => {
          const x = 48 + (540 / count) * index;
          const y = 230 - (item[key] / this.maxValue) * 190;
          return `${x.toFixed(1)},${y.toFixed(1)}`;
        })
        .join(" ");
    },
  },
};
</script>

<style lang="scss" scoped>
.dashboard-panel {
  min-width: 0;
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #edf1f7;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(31, 45, 61, 0.06);
}

.dashboard-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 34px;
  margin-bottom: 18px;

  h3 {
    margin: 0;
    color: #111827;
    font-size: 16px;
    font-weight: 800;
  }

  button {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 32px;
    padding: 0 12px;
    color: #2f7cf6;
    cursor: pointer;
    background: #fff;
    border: 1px solid #dfe7f2;
    border-radius: 6px;
  }
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  color: #334155;
  font-size: 13px;
}

.legend {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  white-space: nowrap;

  i {
    width: 18px;
    height: 3px;
    border-radius: 999px;
  }

  &.uv i {
    background: #2f7cf6;
  }

  &.pv i {
    background: #20bf8f;
  }
}

.trend-chart {
  height: 260px;

  svg {
    display: block;
    width: 100%;
    height: 228px;
  }
}

.grid-lines line {
  stroke: #e8edf5;
  stroke-width: 1;
}

.axis-labels {
  fill: #60708a;
  font-size: 12px;
}

.uv-line,
.pv-line {
  fill: none;
  stroke-width: 2.5;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.uv-line {
  stroke: #2f7cf6;
}

.pv-line {
  stroke: #20bf8f;
}

.uv-dot,
.pv-dot {
  stroke: #fff;
  stroke-width: 2;
}

.uv-dot {
  fill: #2f7cf6;
}

.pv-dot {
  fill: #20bf8f;
}

.x-axis {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding-left: 48px;
  color: #60708a;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 640px) {
  .dashboard-panel {
    padding: 18px 16px;
  }

  .dashboard-panel-header,
  .chart-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .x-axis {
    padding-left: 34px;
    font-size: 11px;
  }
}
</style>
