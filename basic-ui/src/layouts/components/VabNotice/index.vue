<template>
  <section v-if="notices.length" class="vab-notice" aria-label="通知公告">
    <el-carousel
      :autoplay="!reducedMotion"
      :interval="3000"
      direction="vertical"
      height="30px"
      indicator-position="none"
      pause-on-hover
    >
      <el-carousel-item v-for="item in notices" :key="item.id">
        <button class="notice-link" type="button" @click="openDetail(item.id)">
          <el-tag effect="light" size="small" type="warning">
            {{ typeLabel(item.noticeType) }}
          </el-tag>
          <span class="notice-title">{{ item.title }}</span>
          <time :datetime="item.publishTime">{{ formatPublishTime(item.publishTime) }}</time>
        </button>
      </el-carousel-item>
    </el-carousel>
  </section>
</template>

<script>
import { getLatestNotices } from "@/api/notice";
import eventBus from "@/utils/eventBus";

export default {
  name: "VabNotice",
  emits: ["visibility-change"],
  data() {
    return {
      notices: [],
      reducedMotion: false,
    };
  },
  created() {
    this.$loadDict("sys_notice_type");
    this.loadNotices();
    eventBus.on("notice-updated", this.loadNotices);
  },
  mounted() {
    this.reducedMotion = window.matchMedia?.("(prefers-reduced-motion: reduce)").matches || false;
  },
  beforeUnmount() {
    eventBus.off("notice-updated", this.loadNotices);
    this.$emit("visibility-change", false);
  },
  methods: {
    /** 读取当前用户最新可见公告；失败时不保留旧数据或占位高度。 */
    async loadNotices() {
      try {
        const { data } = await getLatestNotices({ limit: 5 });
        this.setNotices(Array.isArray(data) ? data : []);
      } catch (_error) {
        this.setNotices([]);
      }
    },
    /** 统一更新公告与布局占位状态，避免无数据时残留空白。 */
    setNotices(notices) {
      this.notices = notices;
      this.$emit("visibility-change", notices.length > 0);
    },
    typeLabel(value) {
      return this.$dictLabel("sys_notice_type", value) || value || "公告";
    },
    /** 在紧凑公告条中保留月日和时分，完整时间仍写入 datetime。 */
    formatPublishTime(value) {
      const text = String(value || "").replace("T", " ");
      return text.length >= 16 ? text.slice(5, 16) : text;
    },
    openDetail(id) {
      if (this.$route.path !== `/notice/${id}`) this.$router.push(`/notice/${id}`);
    },
  },
};
</script>

<style lang="scss" scoped>
.vab-notice {
  box-sizing: border-box;
  display: block;
  height: 30px;
  min-height: 30px;
  padding: 0 8px 0 12px;
  overflow: hidden;
  background: $base-color-white;
  border-radius: 4px;

  :deep(.el-carousel) {
    min-width: 0;
  }
}

.notice-link {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  width: 100%;
  height: 30px;
  padding: 0 8px 0 0;
  font-size: 13px;
  color: var(--el-text-color-primary);
  cursor: pointer;
  background: transparent;
  border: 0;

  &:focus-visible {
    outline: 2px solid var(--el-color-primary);
    outline-offset: -2px;
  }
}

.notice-title {
  min-width: 0;
  margin: 0 10px;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .vab-notice {
    padding: 0 4px 0 6px;
  }

  .notice-link {
    grid-template-columns: auto minmax(0, 1fr) auto;
    padding-right: 2px;
  }

  .notice-title {
    margin: 0 4px;
  }

  time {
    font-size: 11px;
  }

  :deep(.el-tag) {
    height: 20px;
    padding: 0 4px;
    font-size: 11px;
  }
}

@media (prefers-reduced-motion: reduce) {
  :deep(.el-carousel__item) {
    transition: none !important;
  }
}
</style>
