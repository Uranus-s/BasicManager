<template>
  <section v-loading="loading" class="dashboard-panel notice-panel">
    <div class="dashboard-panel-header">
      <h3>最新公告</h3>
      <el-button text type="primary" @click="$emit('more')">查看更多</el-button>
    </div>
    <div v-if="items.length" class="notice-list">
      <router-link
        v-for="item in items"
        :key="item.id || item.title"
        :to="`/notice/${item.id}`"
        class="notice-item"
      >
        <el-tag effect="light" size="small" type="warning">
          {{ typeLabel(item.noticeType) }}
        </el-tag>
        <span class="notice-title">{{ item.title }}</span>
        <time :datetime="item.publishTime">{{ formatPublishDate(item.publishTime) }}</time>
      </router-link>
    </div>
    <el-empty v-else-if="!loading" :image-size="56" description="暂无公告" />
  </section>
</template>

<script>
export default {
  name: "NoticePanel",
  emits: ["more"],
  props: {
    items: {
      type: Array,
      required: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    typeLabel(value) {
      return this.$dictLabel("sys_notice_type", value) || value || "公告";
    },
    /** 首页列表只展示日期，完整发布时间保留在 time 元素中。 */
    formatPublishDate(value) {
      return String(value || "").slice(0, 10);
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

.notice-list {
  display: grid;
}

.notice-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  min-height: 44px;
  padding: 0;
  color: inherit;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-bottom: 1px solid #edf1f7;
  text-decoration: none;

  &:last-child {
    border-bottom: 0;
  }

  &:focus-visible {
    outline: 2px solid var(--el-color-primary);
    outline-offset: 2px;
  }
}

.notice-title {
  min-width: 0;
  overflow: hidden;
  color: #253044;
  font-size: 14px;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

time {
  color: #7b8ba3;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

:deep(.el-empty) {
  padding: 12px 0;
}

@media (max-width: 640px) {
  .dashboard-panel {
    padding: 18px 16px;
  }

  .notice-item {
    grid-template-columns: auto minmax(0, 1fr);

    time {
      grid-column: 2;
      text-align: left;
    }
  }
}
</style>
