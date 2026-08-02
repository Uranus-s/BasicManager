<template>
  <section class="dashboard-panel overview-panel" :aria-busy="loading">
    <div class="dashboard-panel-header">
      <h3>系统概览</h3>
    </div>
    <div v-if="loading" class="overview-body overview-skeleton" aria-hidden="true">
      <div class="skeleton-table">
        <div v-for="index in 6" :key="index" class="skeleton-row">
          <span class="skeleton-block skeleton-label"></span>
          <span class="skeleton-block skeleton-value"></span>
        </div>
      </div>
      <div class="skeleton-storage">
        <span class="skeleton-block skeleton-title"></span>
        <span class="skeleton-block skeleton-circle"></span>
        <span class="skeleton-block skeleton-line skeleton-line-wide"></span>
        <span class="skeleton-block skeleton-line"></span>
      </div>
    </div>
    <div v-else class="overview-body">
      <div class="system-table">
        <div v-for="row in rows" :key="row.label" class="system-row">
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </div>
      </div>
      <div class="storage-card">
        <h4>存储使用情况</h4>
        <div
          class="storage-ring"
          :style="{ '--percent': storage.percent + '%' }"
        >
          <div>
            <strong>{{ storage.percent }}%</strong>
          </div>
        </div>
        <div class="storage-text">
          已使用 {{ storage.used }} / 总容量 {{ storage.total }}
        </div>
        <div class="storage-legend">
          <span><i class="used"></i>已用空间 {{ storage.used }}</span>
          <span><i class="free"></i>可用空间 {{ storage.free }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "SystemOverviewPanel",
  props: {
    loading: {
      type: Boolean,
      default: false,
    },
    rows: {
      type: Array,
      required: true,
    },
    storage: {
      type: Object,
      required: true,
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
}

.overview-body {
  display: grid;
  grid-template-columns: minmax(260px, 0.9fr) minmax(260px, 1.1fr);
  gap: 20px;
}

.overview-skeleton {
  min-height: 236px;
}

.skeleton-table {
  overflow: hidden;
  border-radius: 6px;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  background: #fbfcfe;
  border-bottom: 1px solid #edf1f7;
}

.skeleton-block {
  display: block;
  background: linear-gradient(90deg, #edf2f8 25%, #f7f9fc 50%, #edf2f8 75%);
  background-size: 200% 100%;
  border-radius: 6px;
  animation: overview-skeleton-shimmer 1.4s ease-in-out infinite;
}

.skeleton-label {
  width: 52%;
  height: 12px;
}

.skeleton-value {
  justify-self: end;
  width: 68%;
  height: 12px;
}

.skeleton-storage {
  min-height: 246px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 0;
}

.skeleton-title {
  width: 92px;
  height: 14px;
  margin-bottom: 18px;
}

.skeleton-circle {
  width: 150px;
  height: 150px;
  margin-bottom: 12px;
  border-radius: 50%;
}

.skeleton-line {
  width: 56%;
  height: 11px;
  margin-top: 12px;
}

.skeleton-line-wide {
  width: 72%;
  margin-top: 0;
}

@keyframes overview-skeleton-shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -200% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .skeleton-block {
    animation: none;
  }
}

.system-table {
  overflow: hidden;
  border-radius: 6px;
}

.system-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 38px;
  background: #fbfcfe;
  border-bottom: 1px solid #edf1f7;

  span,
  strong {
    display: flex;
    align-items: center;
    padding: 0 14px;
    font-size: 13px;
  }

  span {
    color: #111827;
  }

  strong {
    justify-content: flex-end;
    color: #334155;
    font-weight: 500;
  }
}

.storage-card {
  min-width: 0;
  min-height: 246px;
  text-align: center;

  h4 {
    margin: 0 0 18px;
    color: #1f2937;
    font-size: 14px;
    font-weight: 800;
  }
}

.storage-ring {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 150px;
  height: 150px;
  margin: 0 auto 12px;
  background:
    radial-gradient(circle, #fff 0 54%, transparent 55%),
    conic-gradient(#2f7cf6 var(--percent), #dbe6f8 var(--percent) 100%);
  border-radius: 50%;

  strong {
    color: #111827;
    font-size: 26px;
    font-weight: 800;
  }
}

.storage-text {
  color: #6b7a90;
  font-size: 13px;
}

.storage-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 18px;
  margin-top: 18px;
  color: #334155;
  font-size: 13px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  i {
    width: 10px;
    height: 10px;
    border-radius: 3px;
  }

  .used {
    background: #2f7cf6;
  }

  .free {
    background: #dbe6f8;
  }
}

@media (max-width: 960px) {
  .overview-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .dashboard-panel {
    padding: 18px 16px;
  }
}
</style>
