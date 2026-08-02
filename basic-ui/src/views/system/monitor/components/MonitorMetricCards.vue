<template>
  <section class="metric-grid" aria-label="服务核心指标">
    <article
      v-for="item in items"
      :key="item.label"
      class="metric-card"
      :class="`is-${item.tone || 'primary'}`"
    >
      <span class="metric-icon" aria-hidden="true">
        <el-icon><component :is="item.icon" /></el-icon>
      </span>
      <div class="metric-content">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.detail }}</small>
      </div>
    </article>
  </section>
</template>

<script>
export default {
  name: "MonitorMetricCards",
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
};
</script>

<style lang="scss" scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 20px;
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.metric-icon {
  display: inline-flex;
  flex: 0 0 44px;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-right: 16px;
  color: #2563eb;
  background: #eff6ff;
  border-radius: 10px;
  font-size: 22px;
}

.metric-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;

  span {
    color: #475569;
    font-size: 13px;
    line-height: 20px;
  }

  strong {
    margin-top: 3px;
    overflow: hidden;
    color: #0f172a;
    font-size: 24px;
    font-variant-numeric: tabular-nums;
    font-weight: 700;
    line-height: 32px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  small {
    margin-top: 3px;
    overflow-wrap: anywhere;
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }
}

.metric-card.is-success {
  .metric-icon {
    color: #15803d;
    background: #f0fdf4;
  }
}

.metric-card.is-warning {
  .metric-icon {
    color: #b45309;
    background: #fff7ed;
  }
}

.metric-card.is-danger {
  .metric-icon {
    color: #dc2626;
    background: #fef2f2;
  }
}

@media (max-width: 960px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
