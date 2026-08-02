const fs = require("fs");
const path = require("path");
const assert = require("assert");
const vm = require("vm");
const { parse } = require("@vue/compiler-sfc");

const root = path.resolve(__dirname, "..");
const files = [
  "src/views/index/index.vue",
  "src/views/index/components/DashboardMetricCard.vue",
  "src/views/index/components/NoticePanel.vue",
  "src/views/index/components/QuickEntryPanel.vue",
  "src/views/index/components/SystemOverviewPanel.vue",
  "src/views/index/components/VisitTrendPanel.vue",
];

let hasFailure = false;

for (const file of files) {
  const fullPath = path.join(root, file);
  if (!fs.existsSync(fullPath)) {
    console.error(`Missing file: ${file}`);
    hasFailure = true;
    continue;
  }

  const source = fs.readFileSync(fullPath, "utf8");
  const result = parse(source, { filename: file });
  if (result.errors.length > 0) {
    console.error(`SFC parse failed: ${file}`);
    for (const error of result.errors) console.error(error.message);
    hasFailure = true;
  }
}

const indexSource = fs.readFileSync(
  path.join(root, "src/views/index/index.vue"),
  "utf8"
);

const requiredIndexSnippets = [
  "import DashboardMetricCard from \"./components/DashboardMetricCard.vue\";",
  "import VisitTrendPanel from \"./components/VisitTrendPanel.vue\";",
  "<dashboard-metric-card",
  "<visit-trend-panel",
  "<quick-entry-panel",
  "grid-template-columns: repeat(4, minmax(0, 1fr));",
  'import { getSystemMonitorStatus } from "@/api/system/monitor";',
  "created() {",
  "this.loadSystemOverview();",
  "async loadSystemOverview()",
  "getSystemMonitorStatus()",
  "formatBytes(bytes)",
  "formatUptime(milliseconds)",
  "buildSystemOverview(data)",
  ':loading="systemOverviewLoading"',
  "systemOverviewLoading: true",
  "this.systemOverviewLoading = false;",
  "finally {",
];

for (const snippet of requiredIndexSnippets) {
  if (!indexSource.includes(snippet)) {
    console.error(`Missing index.vue snippet: ${snippet}`);
    hasFailure = true;
  }
}

const visitTrendSource = fs.readFileSync(
  path.join(root, "src/views/index/components/VisitTrendPanel.vue"),
  "utf8"
);

for (const snippet of [
  "<svg viewBox=\"0 0 620 260\"",
  "buildPoints(key)",
  "preserveAspectRatio=\"none\"",
]) {
  if (!visitTrendSource.includes(snippet)) {
    console.error(`Missing visit trend snippet: ${snippet}`);
    hasFailure = true;
  }
}

const systemOverviewPanelSource = fs.readFileSync(
  path.join(root, "src/views/index/components/SystemOverviewPanel.vue"),
  "utf8"
);

for (const snippet of [
  'v-if="loading"',
  'class="overview-body overview-skeleton"',
  'class="skeleton-block skeleton-circle"',
  "prefers-reduced-motion: reduce",
  "loading: {",
]) {
  if (!systemOverviewPanelSource.includes(snippet)) {
    console.error(`Missing system overview loading snippet: ${snippet}`);
    hasFailure = true;
  }
}

function hasPanelMinHeight(selector) {
  const escapedSelector = selector.replace(".", "\\.");
  const pattern = new RegExp(
    `${escapedSelector}\\s*\\{[^}]*\\bmin-height:\\s*246px;`
  );
  return pattern.test(systemOverviewPanelSource);
}

for (const selector of [".skeleton-storage", ".storage-card"]) {
  if (!hasPanelMinHeight(selector)) {
    console.error(`Missing shared storage min-height on: ${selector}`);
    hasFailure = true;
  }
}

const monitorApiPath = path.join(root, "src/api/system/monitor.js");

if (!fs.existsSync(monitorApiPath)) {
  console.error("Missing file: src/api/system/monitor.js");
  hasFailure = true;
} else {
  const monitorApiSource = fs.readFileSync(monitorApiPath, "utf8");

  for (const snippet of [
    'url: "/system/monitor/status"',
    'method: "get"',
  ]) {
    if (!monitorApiSource.includes(snippet)) {
      console.error(`Missing monitor API snippet: ${snippet}`);
      hasFailure = true;
    }
  }
}

function toPlain(value) {
  return JSON.parse(JSON.stringify(value));
}

function loadIndexComponent() {
  const { descriptor } = parse(indexSource, {
    filename: "src/views/index/index.vue",
  });

  assert.ok(descriptor.script, "首页必须包含普通 script 区块");

  const script = descriptor.script.content
    .replace(
      /import\s+\{\s*getSystemMonitorStatus\s*\}\s+from\s+["'][^"']+["'];?\s*/,
      ""
    )
    .replace(
      /import\s+([A-Za-z_$][\w$]*)\s+from\s+["'][^"']+["'];?\s*/g,
      "const $1 = {};\n"
    )
    .replace("export default", "module.exports =");
  const context = {
    module: { exports: {} },
    getSystemMonitorStatus: () => {
      throw new Error("未注入监控接口响应");
    },
  };

  vm.runInNewContext(script, context, {
    filename: "src/views/index/index.vue",
  });

  return { component: context.module.exports, context };
}

function createIndexVm(component) {
  const instance = component.data();

  for (const [name, method] of Object.entries(component.methods)) {
    instance[name] = method.bind(instance);
  }

  return instance;
}

async function runMonitorBehaviorChecks() {
  const { component, context } = loadIndexComponent();
  const formatterVm = createIndexVm(component);

  assert.strictEqual(formatterVm.systemOverviewLoading, true);

  assert.strictEqual(formatterVm.formatBytes(0), "0 B");
  assert.strictEqual(formatterVm.formatBytes(1), "1 B");
  assert.strictEqual(formatterVm.formatBytes(1024), "1.0 KB");
  assert.strictEqual(formatterVm.formatBytes(1024 ** 2), "1.0 MB");
  assert.strictEqual(formatterVm.formatBytes(1024 ** 3), "1.0 GB");
  assert.strictEqual(formatterVm.formatBytes(1024 ** 4), "1.0 TB");

  const missingOverview = formatterVm.buildSystemOverview({});
  assert.deepStrictEqual(toPlain(missingOverview.rows), [
    { label: "操作系统", value: "--" },
    { label: "系统架构", value: "--" },
    { label: "Java版本", value: "--" },
    { label: "数据库", value: "--" },
    { label: "采集时间", value: "--" },
    { label: "已运行时间", value: "--" },
  ]);

  const monitorData = {
    collectTime: "2026-07-23 12:00:00",
    system: { osName: "Linux", osVersion: "6.8", osArch: "amd64" },
    jvm: { javaVersion: "21", uptimeMs: 90060000 },
    database: {
      databaseProductName: "MySQL",
      databaseProductVersion: "8.4",
    },
    disks: [
      {
        totalSpaceBytes: 10 * 1024,
        usedSpaceBytes: 2 * 1024,
        usableSpaceBytes: 8 * 1024,
      },
      {
        totalSpaceBytes: 30 * 1024,
        usedSpaceBytes: 18 * 1024,
        usableSpaceBytes: 12 * 1024,
      },
    ],
  };
  const expectedRows = [
    { label: "操作系统", value: "Linux 6.8" },
    { label: "系统架构", value: "amd64" },
    { label: "Java版本", value: "21" },
    { label: "数据库", value: "MySQL 8.4" },
    { label: "采集时间", value: "2026-07-23 12:00:00" },
    { label: "已运行时间", value: "1天 1时 1分" },
  ];
  const expectedStorage = {
    percent: 50,
    used: "20 KB",
    total: "40 KB",
    free: "20 KB",
  };
  const overview = formatterVm.buildSystemOverview(monitorData);

  assert.deepStrictEqual(toPlain(overview.rows), expectedRows);
  assert.deepStrictEqual(toPlain(overview.storage), expectedStorage);

  const successVm = createIndexVm(component);
  let requestCount = 0;
  let resolveSuccess;
  context.getSystemMonitorStatus = () => {
    requestCount += 1;
    return new Promise((resolve) => {
      resolveSuccess = resolve;
    });
  };
  const successRequest = successVm.loadSystemOverview();

  assert.strictEqual(requestCount, 1);
  assert.strictEqual(successVm.systemOverviewLoading, true);

  resolveSuccess({ data: monitorData });
  await successRequest;

  assert.strictEqual(successVm.systemOverviewLoading, false);
  assert.deepStrictEqual(toPlain(successVm.systemOverview), expectedRows);
  assert.deepStrictEqual(toPlain(successVm.storageUsage), expectedStorage);

  const failureVm = createIndexVm(component);
  const initialRows = toPlain(failureVm.systemOverview);
  const initialStorage = toPlain(failureVm.storageUsage);
  let rejectFailure;
  context.getSystemMonitorStatus = () => {
    return new Promise((resolve, reject) => {
      rejectFailure = reject;
    });
  };
  const failureRequest = failureVm.loadSystemOverview();

  assert.strictEqual(failureVm.systemOverviewLoading, true);

  rejectFailure(new Error("监控请求失败"));
  await failureRequest;

  assert.strictEqual(failureVm.systemOverviewLoading, false);
  assert.deepStrictEqual(toPlain(failureVm.systemOverview), initialRows);
  assert.deepStrictEqual(toPlain(failureVm.storageUsage), initialStorage);
}

async function main() {
  if (hasFailure) {
    process.exitCode = 1;
    return;
  }

  await runMonitorBehaviorChecks();
  console.log("Home dashboard SFC checks passed");
}

main().catch((error) => {
  console.error("Home dashboard monitor behavior checks failed");
  console.error(error.stack || error);
  process.exitCode = 1;
});
