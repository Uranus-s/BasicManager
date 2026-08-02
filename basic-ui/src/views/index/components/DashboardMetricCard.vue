<template>
  <section class="metric-card">
    <div class="metric-icon" :class="item.theme">
      <el-icon><component :is="item.icon" /></el-icon>
    </div>
    <div class="metric-content">
      <div class="metric-label">{{ item.label }}</div>
      <div class="metric-value">{{ item.value }}</div>
      <div class="metric-change">
        <span>较昨日</span>
        <strong :class="item.increase ? 'is-up' : 'is-down'">
          {{ item.increase ? "↑" : "↓" }} {{ item.rate }}
        </strong>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "DashboardMetricCard",
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
};
</script>

<style lang="scss" scoped>
.metric-card {
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 124px;
  padding: 24px 28px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #edf1f7;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(31, 45, 61, 0.06);
}

.metric-icon {
  display: inline-flex;
  flex: 0 0 58px;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  margin-right: 22px;
  color: #fff;
  background: #2f7cf6;
  border-radius: 50%;
  box-shadow: 0 12px 22px rgba(47, 124, 246, 0.24);
  font-size: 28px;

  &.blue {
    background: linear-gradient(135deg, #409eff, #1f6ff2);
  }

  &.green {
    background: linear-gradient(135deg, #35d39c, #16b981);
  }

  &.purple {
    background: linear-gradient(135deg, #8b68ff, #6947e8);
  }

  &.orange {
    background: linear-gradient(135deg, #ffaf27, #f08500);
  }
}

.metric-content {
  min-width: 0;
}

.metric-label {
  margin-bottom: 8px;
  color: #253044;
  font-size: 14px;
}

.metric-value {
  color: #0f172a;
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
}

.metric-change {
  margin-top: 14px;
  color: #6b7a90;
  font-size: 13px;

  strong {
    margin-left: 6px;
    font-weight: 700;
  }

  .is-up {
    color: #ff2435;
  }

  .is-down {
    color: #16a34a;
  }
}

@media (max-width: 1380px) {
  .metric-card {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .metric-card {
    min-height: 104px;
  }
}
</style>
