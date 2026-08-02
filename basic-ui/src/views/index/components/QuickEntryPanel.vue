<template>
  <section class="dashboard-panel quick-panel">
    <div class="dashboard-panel-header">
      <h3>快捷入口</h3>
    </div>
    <div class="quick-grid">
      <button
        v-for="item in items"
        :key="item.label"
        type="button"
        class="quick-item"
        @click="$emit('open', item.path)"
      >
        <span class="quick-icon" :class="item.theme">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <span>{{ item.label }}</span>
      </button>
    </div>
  </section>
</template>

<script>
export default {
  name: "QuickEntryPanel",
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  emits: ["open"],
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

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 34px 30px;
  padding: 18px 26px 22px;
}

.quick-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-width: 0;
  min-height: 86px;
  padding: 0;
  color: #1f2937;
  cursor: pointer;
  background: transparent;
  border: 0;

  &:hover .quick-icon {
    transform: translateY(-2px);
  }
}

.quick-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin-bottom: 12px;
  color: #fff;
  background: #2f7cf6;
  border-radius: 8px;
  box-shadow: 0 12px 22px rgba(47, 124, 246, 0.24);
  font-size: 30px;
  transition: transform 0.2s ease;

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

  &.slate {
    background: linear-gradient(135deg, #8b9bb0, #66788f);
  }
}

@media (max-width: 640px) {
  .dashboard-panel {
    padding: 18px 16px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
