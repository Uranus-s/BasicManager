const fs = require("node:fs");
const path = require("node:path");

const projectRoot = path.resolve(__dirname, "..");
const failures = [];

function read(relativePath) {
  const fullPath = path.join(projectRoot, relativePath);
  if (!fs.existsSync(fullPath)) {
    failures.push(`${relativePath}: 文件不存在`);
    return "";
  }
  return fs.readFileSync(fullPath, "utf8");
}

function assertIncludes(relativePath, expected) {
  const content = read(relativePath);
  if (!content.includes(expected)) {
    failures.push(`${relativePath}: 缺少 ${JSON.stringify(expected)}`);
  }
}

function assertNotIncludes(relativePath, unexpected) {
  const content = read(relativePath);
  if (content.includes(unexpected)) {
    failures.push(`${relativePath}: 不应包含 ${JSON.stringify(unexpected)}`);
  }
}

assertIncludes("src/api/system/notice.js", 'url: "/system/notice/list"');
assertIncludes("src/api/system/notice.js", "`/system/notice/${id}/publish`");
assertIncludes("src/api/notice.js", 'url: "/notice/latest"');
assertIncludes("rspack.config.js", '"/notice",');
assertIncludes("src/router/index.js", 'path: "/notice"');
assertIncludes("src/layouts/index.vue", "<vab-notice");
assertIncludes("src/layouts/components/VabNotice/index.vue", 'name: "VabNotice"');
assertIncludes("src/layouts/components/VabNotice/index.vue", 'height="30px"');
assertIncludes(
  "src/layouts/components/VabNotice/index.vue",
  ".vab-notice {\n  box-sizing: border-box;\n  display: block;\n  height: 30px;"
);
assertIncludes(
  "src/layouts/components/VabNotice/index.vue",
  "padding: 0 8px 0 12px;"
);
assertIncludes("src/layouts/components/VabNotice/index.vue", 'emits: ["visibility-change"]');
assertIncludes("src/layouts/index.vue", '@visibility-change="handleNoticeVisibility"');
assertIncludes("src/layouts/index.vue", '"has-notice": noticeVisible.value');
assertIncludes(
  "src/layouts/index.vue",
  "calc(#{$base-app-main-height} - 30px - #{$base-padding})"
);
assertNotIncludes("src/layouts/components/VabNotice/index.vue", "margin-bottom: -20px");
assertNotIncludes("src/layouts/components/VabNotice/index.vue", "notice-more");
assertNotIncludes("src/layouts/components/VabNotice/index.vue", "全部公告");
assertIncludes("src/views/index/index.vue", "<notice-panel");
assertIncludes(
  "src/views/index/index.vue",
  'import { getVisibleNoticeList } from "@/api/notice"'
);
assertIncludes(
  "src/views/index/index.vue",
  "getVisibleNoticeList({ pageNum: 1, pageSize: 5 })"
);
assertIncludes("src/views/index/index.vue", "minmax(360px, 0.95fr)");
assertIncludes("src/views/index/components/NoticePanel.vue", 'name: "NoticePanel"');
assertIncludes("src/views/index/components/NoticePanel.vue", "<router-link");
assertIncludes("src/views/index/components/NoticePanel.vue", 'emits: ["more"]');
assertIncludes("src/views/index/components/NoticePanel.vue", "暂无公告");
assertIncludes("src/views/system/notice/index.vue", "system:notice:publish");
assertIncludes("src/views/system/notice/index.vue", "check-strictly");
assertIncludes(
  "src/views/system/notice/index.vue",
  '<el-table-column :fixed="operationColumnFixed" label="操作" width="300">'
);
assertIncludes("src/views/system/notice/index.vue", "operationColumnFixed() {");
assertIncludes(
  "src/views/system/notice/index.vue",
  'return this.$store.getters["settings/device"] === "mobile" ? false : "right";'
);
assertIncludes("src/views/system/notice/index.vue", "catch (_error)");
assertIncludes("src/views/system/notice/index.vue", "this.total = 0;");
assertIncludes(
  "src/views/system/notice/index.vue",
  ".pagination-container {\n  margin-top: 20px;\n  text-align: center;\n}"
);
assertNotIncludes(
  "src/views/system/notice/index.vue",
  ".pagination-container {\n  display: flex;\n  justify-content: center;"
);
assertIncludes("src/views/notice/index.vue", "catch (_error)");
assertIncludes("src/views/notice/index.vue", "this.total = 0;");
assertIncludes("src/views/notice/detail.vue", "html: false");

if (failures.length) {
  console.error("通知公告模块契约检查失败：");
  failures.forEach((failure) => console.error(`- ${failure}`));
  process.exitCode = 1;
} else {
  console.log("通知公告模块契约检查通过");
}
