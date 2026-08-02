import assert from "node:assert/strict";
import test from "node:test";
import * as monitorUtils from "../src/views/system/monitor/monitorUtils.js";

const {
  appendTrendPoint,
  formatBytes,
  formatDuration,
  formatPercent,
  getOverallHealth,
  getQueueLoad,
  getThreadPoolCapacity,
  getThreadPoolLoad,
} = monitorUtils;

test("formatBytes handles invalid values and selects binary units", () => {
  assert.equal(formatBytes(null), "0 B");
  assert.equal(formatBytes(-1), "0 B");
  assert.equal(formatBytes(1024), "1 KB");
  assert.equal(formatBytes(1536), "1.5 KB");
  assert.equal(formatBytes(4 * 1024 ** 3), "4 GB");
});

test("formatDuration formats milliseconds without invalid output", () => {
  assert.equal(formatDuration(undefined), "—");
  assert.equal(formatDuration(-1), "—");
  assert.equal(formatDuration(90_061_000), "1天 1时 1分");
});

test("formatPercent clamps finite values and rejects unsupported values", () => {
  assert.equal(formatPercent(73.0418), "73.0%");
  assert.equal(formatPercent(120), "100.0%");
  assert.equal(formatPercent(-1), "—");
});

test("thread pool capacity prefers concurrency, bounded maximum, then core", () => {
  assert.equal(getThreadPoolCapacity({ concurrencyLimit: 200 }), 200);
  assert.equal(
    getThreadPoolCapacity({
      maximumPoolSize: 24,
      corePoolSize: 12,
    }),
    24
  );
  assert.equal(
    getThreadPoolCapacity({
      maximumPoolSize: 2147483647,
      corePoolSize: 2,
    }),
    2
  );
  assert.equal(getThreadPoolCapacity({}), null);
});

test("thread and queue loads preserve not-applicable values", () => {
  assert.deepEqual(getThreadPoolLoad({ activeCount: 3, maximumPoolSize: 12 }), {
    value: 3,
    capacity: 12,
    percent: 25,
  });
  assert.deepEqual(getThreadPoolLoad({ activeCount: 2 }), {
    value: 2,
    capacity: null,
    percent: null,
  });
  assert.deepEqual(getQueueLoad({ queueSize: null, queueCapacity: null }), {
    value: 0,
    capacity: null,
    percent: null,
  });
});

test("appendTrendPoint returns a new array capped at the requested limit", () => {
  const original = [{ time: "1" }, { time: "2" }];
  const result = appendTrendPoint(original, { time: "3" }, 2);
  assert.deepEqual(result, [{ time: "2" }, { time: "3" }]);
  assert.deepEqual(original, [{ time: "1" }, { time: "2" }]);
  const capped = appendTrendPoint(
    Array.from({ length: 30 }, (_, index) => ({ time: String(index) })),
    { time: "30" },
    100
  );
  assert.equal(capped.length, 30);
  assert.equal(capped[0].time, "1");
});

test("overall health prioritizes danger, warning, normal and unknown", () => {
  assert.equal(getOverallHealth({}).level, "unknown");
  assert.equal(
    getOverallHealth({
      status: { database: { status: "DOWN" }, redis: { status: "UP" } },
      executors: [],
    }).level,
    "danger"
  );
  assert.equal(
    getOverallHealth({
      status: {
        database: { status: "UP" },
        redis: { status: "UP" },
        disks: [{ name: "D:\\", usedPercent: 82 }],
      },
      executors: [],
    }).level,
    "warning"
  );
  assert.equal(
    getOverallHealth({
      status: {
        database: { status: "UP" },
        redis: { status: "UP" },
        disks: [{ name: "D:\\", usedPercent: 50 }],
      },
      executors: [{ name: "cpu", status: "RUNNING" }],
    }).level,
    "normal"
  );
});

test("overall health warns when failures or rejections increase", () => {
  const health = getOverallHealth({
    status: {
      database: { status: "UP" },
      redis: { status: "UP" },
      disks: [],
    },
    executors: [
      {
        name: "cpu",
        status: "RUNNING",
        failedTaskCount: 2,
        rejectedTaskCount: 0,
      },
    ],
    previousExecutors: [
      {
        name: "cpu",
        failedTaskCount: 1,
        rejectedTaskCount: 0,
      },
    ],
  });
  assert.equal(health.level, "warning");
  assert.match(health.reasons.join("、"), /失败任务增加/);
});

test("overall health keeps cumulative failures visible", () => {
  const executor = {
    name: "cpu",
    status: "RUNNING",
    failedTaskCount: 2,
    rejectedTaskCount: 1,
  };
  const health = getOverallHealth({
    status: {
      database: { status: "UP" },
      redis: { status: "UP" },
      disks: [],
    },
    executors: [executor],
    previousExecutors: [executor],
  });
  assert.equal(health.level, "warning");
  assert.match(health.reasons.join("、"), /累计失败任务/);
  assert.match(health.reasons.join("、"), /累计拒绝任务/);
});

test("overall health reports cumulative tasks without a previous sample", () => {
  const health = getOverallHealth({
    status: {
      database: { status: "UP" },
      redis: { status: "UP" },
      disks: [],
    },
    executors: [
      {
        name: "cpu",
        status: "RUNNING",
        failedTaskCount: 2,
        rejectedTaskCount: 1,
      },
    ],
    previousExecutors: [],
  });
  const reasons = health.reasons.join("、");
  assert.equal(health.level, "warning");
  assert.match(reasons, /累计失败任务/);
  assert.match(reasons, /累计拒绝任务/);
  assert.doesNotMatch(reasons, /任务增加/);
});

test("empty or malformed system status cannot report healthy", () => {
  for (const status of [{}, { data: {} }, {
    system: null,
    jvm: null,
    disks: [],
    database: null,
    redis: null,
  }]) {
    const health = getOverallHealth({
      status,
      executors: [{ name: "cpu", status: "RUNNING" }],
    });
    assert.equal(health.level, "unknown");
    assert.equal(health.label, "暂无数据");
  }
});

test("latest collection time selects the newer valid timestamp", () => {
  assert.equal(typeof monitorUtils.getLatestCollectTime, "function");
  assert.equal(
    monitorUtils.getLatestCollectTime(
      "2026-07-25 14:32:18",
      "2026-07-25 14:32:27"
    ),
    "2026-07-25 14:32:27"
  );
  assert.equal(
    monitorUtils.getLatestCollectTime(
      "2026-07-25 14:32:18",
      null,
      "  "
    ),
    "2026-07-25 14:32:18"
  );
});
