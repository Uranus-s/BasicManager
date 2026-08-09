<template>
  <main v-loading="loading" class="notice-detail-container">
    <template v-if="notice">
      <header class="detail-heading">
        <el-button text type="primary" @click="backToCenter">
          <el-icon><ArrowLeft /></el-icon>
          返回公告中心
        </el-button>
        <h1>{{ notice.title }}</h1>
        <div class="notice-meta">
          <el-tag effect="light" type="warning">{{ typeLabel(notice.noticeType) }}</el-tag>
          <time>{{ notice.publishTime }}</time>
        </div>
      </header>
      <article class="markdown-body" v-html="renderedContent" />
    </template>
    <el-empty v-else-if="!loading" description="公告不存在或已失效">
      <el-button type="primary" @click="backToCenter">返回公告中心</el-button>
    </el-empty>
  </main>
</template>

<script>
import MarkdownIt from "markdown-it";
import { ArrowLeft } from "@element-plus/icons-vue";
import { getVisibleNoticeDetail } from "@/api/notice";

const markdown = new MarkdownIt({ html: false, linkify: true, breaks: true });
const defaultLinkOpen =
  markdown.renderer.rules.link_open ||
  ((tokens, index, options, env, self) => self.renderToken(tokens, index, options));
markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  const href = tokens[index].attrGet("href") || "";
  if (/^https?:\/\//i.test(href)) {
    tokens[index].attrSet("target", "_blank");
    tokens[index].attrSet("rel", "noopener noreferrer");
  }
  return defaultLinkOpen(tokens, index, options, env, self);
};

export default {
  name: "NoticeDetail",
  components: { ArrowLeft },
  data() {
    return {
      notice: null,
      loading: false,
    };
  },
  computed: {
    renderedContent() {
      return markdown.render(this.notice?.content || "");
    },
  },
  watch: {
    "$route.params.id": {
      handler() {
        this.loadDetail();
      },
      immediate: true,
    },
  },
  created() {
    this.$loadDict("sys_notice_type");
  },
  methods: {
    /** 每次路由 ID 变化都清空旧正文，避免失败请求残留上一条公告。 */
    async loadDetail() {
      this.notice = null;
      this.loading = true;
      try {
        const { data } = await getVisibleNoticeDetail(this.$route.params.id);
        this.notice = data || null;
      } catch (_error) {
        this.notice = null;
      } finally {
        this.loading = false;
      }
    },
    typeLabel(value) {
      return this.$dictLabel("sys_notice_type", value) || value || "公告";
    },
    backToCenter() {
      this.$router.push("/notice");
    },
  },
};
</script>

<style lang="scss" scoped>
.notice-detail-container {
  box-sizing: border-box;
  min-height: $base-app-main-height;
  padding: 28px clamp(16px, 5vw, 72px) 48px;
}

.detail-heading,
.markdown-body {
  width: min(100%, 860px);
  margin-right: auto;
  margin-left: auto;
}

.detail-heading {
  padding-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  h1 {
    margin: 20px 0 14px;
    overflow-wrap: anywhere;
    color: var(--el-text-color-primary);
    font-size: 28px;
    font-weight: 600;
    line-height: 1.35;
  }
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}

.markdown-body {
  box-sizing: border-box;
  padding-top: 28px;
  overflow-wrap: anywhere;
  color: var(--el-text-color-primary);
  font-size: 16px;
  line-height: 1.75;

  :deep(img) {
    max-width: 100%;
    height: auto;
  }

  :deep(pre) {
    max-width: 100%;
    padding: 16px;
    overflow-x: auto;
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }

  :deep(code) {
    overflow-wrap: normal;
  }

  :deep(a) {
    overflow-wrap: anywhere;
    color: var(--el-color-primary);
  }

  :deep(blockquote) {
    margin-left: 0;
    padding-left: 16px;
    color: var(--el-text-color-regular);
    border-left: 4px solid var(--el-border-color);
  }
}

@media (max-width: 640px) {
  .notice-detail-container {
    padding: 20px 16px 36px;
  }

  .detail-heading h1 {
    font-size: 22px;
  }

  .notice-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}
</style>
