<template>
  <section class="dashboard-panel notice-panel">
    <div class="dashboard-panel-header">
      <h3>最新公告</h3>
      <button type="button">查看更多</button>
    </div>
    <div class="notice-list">
      <div v-for="item in items" :key="item.title" class="notice-item">
        <el-tag size="small" type="primary" effect="light">
          {{ item.type }}
        </el-tag>
        <span class="notice-title">{{ item.title }}</span>
        <time>{{ item.date }}</time>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "NoticePanel",
  props: {
    items: {
      type: Array,
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

.notice-list {
  display: grid;
}

.notice-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  min-height: 44px;
  border-bottom: 1px solid #edf1f7;

  &:last-child {
    border-bottom: 0;
  }
}

.notice-title {
  overflow: hidden;
  color: #253044;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

time {
  color: #7b8ba3;
  font-size: 13px;
}

@media (max-width: 640px) {
  .dashboard-panel {
    padding: 18px 16px;
  }

  .notice-item {
    grid-template-columns: auto minmax(0, 1fr);

    time {
      grid-column: 2;
    }
  }
}
</style>
