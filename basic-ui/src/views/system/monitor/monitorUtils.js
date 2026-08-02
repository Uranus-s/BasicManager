const UNBOUNDED_POOL_SIZE = 2147483647;

const isNonEmptyRecord = (value) =>
  value !== null &&
  typeof value === "object" &&
  !Array.isArray(value) &&
  Object.keys(value).length > 0;

const hasUsableStatus = (status) =>
  isNonEmptyRecord(status) &&
  ([
    status.system,
    status.jvm,
    status.database,
    status.redis,
  ].some(isNonEmptyRecord) ||
    (Array.isArray(status.disks) && status.disks.length > 0));

const toNonNegativeNumber = (value) => {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) && number >= 0 ? number : null;
};

const toPositiveNumber = (value) => {
  const number = toNonNegativeNumber(value);
  return number !== null && number > 0 ? number : null;
};

const toPercent = (value, capacity) => {
  if (!capacity) return null;
  return Math.min(100, Math.max(0, (value / capacity) * 100));
};

export function formatBytes(value) {
  const bytes = toNonNegativeNumber(value);
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const index = Math.min(
    Math.floor(Math.log(bytes) / Math.log(1024)),
    units.length - 1
  );
  const amount = bytes / 1024 ** index;
  const digits = Number.isInteger(amount) || amount >= 10 || index === 0 ? 0 : 1;
  return `${amount.toFixed(digits)} ${units[index]}`;
}

export function formatDuration(milliseconds) {
  const value = toNonNegativeNumber(milliseconds);
  if (value === null) return "—";
  const totalMinutes = Math.floor(value / 60000);
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const minutes = totalMinutes % 60;
  return `${days}天 ${hours}时 ${minutes}分`;
}

export function formatPercent(value, digits = 1) {
  const number = toNonNegativeNumber(value);
  if (number === null) return "—";
  return `${Math.min(100, number).toFixed(digits)}%`;
}

export function getLatestCollectTime(...values) {
  return values.reduce((latest, value) => {
    if (typeof value !== "string" || !value.trim()) return latest;
    const current = value.trim();
    return current > latest ? current : latest;
  }, "");
}

export function getThreadPoolCapacity(executor = {}) {
  const concurrency = toPositiveNumber(executor.concurrencyLimit);
  if (concurrency) return concurrency;

  const maximum = toPositiveNumber(executor.maximumPoolSize);
  if (maximum && maximum < UNBOUNDED_POOL_SIZE) return maximum;

  return toPositiveNumber(executor.corePoolSize);
}

export function getThreadPoolLoad(executor = {}) {
  const value = toNonNegativeNumber(executor.activeCount) || 0;
  const capacity = getThreadPoolCapacity(executor);
  return { value, capacity, percent: toPercent(value, capacity) };
}

export function getQueueLoad(executor = {}) {
  const value = toNonNegativeNumber(executor.queueSize) || 0;
  const capacity = toPositiveNumber(executor.queueCapacity);
  return { value, capacity, percent: toPercent(value, capacity) };
}

export function appendTrendPoint(points = [], point, limit = 30) {
  const safeLimit = Math.min(30, Math.max(1, Number(limit) || 30));
  return [...points, point].slice(-safeLimit);
}

export function getOverallHealth({
  status,
  executors = [],
  previousExecutors = [],
} = {}) {
  const statusAvailable = hasUsableStatus(status);
  if (!statusAvailable && executors.length === 0) {
    return { level: "unknown", label: "暂无数据", reasons: [] };
  }

  const reasons = [];
  const dangerReasons = [];
  const dependencies = [
    ["数据库", status?.database?.status],
    ["Redis", status?.redis?.status],
  ];

  dependencies.forEach(([label, value]) => {
    if (value && String(value).toUpperCase() !== "UP") {
      dangerReasons.push(`${label}状态异常`);
    }
  });

  executors.forEach((executor) => {
    if (
      executor.status &&
      String(executor.status).toUpperCase() !== "RUNNING"
    ) {
      dangerReasons.push(`${executor.name || "线程池"}未运行`);
    }
  });

  if (dangerReasons.length) {
    return { level: "danger", label: "存在异常", reasons: dangerReasons };
  }

  (status?.disks || []).forEach((disk) => {
    if ((toNonNegativeNumber(disk.usedPercent) || 0) >= 80) {
      reasons.push(`${disk.name || "磁盘"}使用率较高`);
    }
  });

  const previousMap = new Map(
    previousExecutors.map((item) => [item.name, item])
  );
  executors.forEach((executor) => {
    const threadLoad = getThreadPoolLoad(executor);
    const queueLoad = getQueueLoad(executor);
    if ((threadLoad.percent || 0) >= 80) {
      reasons.push(`${executor.name || "线程池"}负载较高`);
    }
    if ((queueLoad.percent || 0) >= 80) {
      reasons.push(`${executor.name || "线程池"}队列接近容量`);
    }

    const hasPrevious = previousMap.has(executor.name);
    const previous = previousMap.get(executor.name) || {};
    const failed = toNonNegativeNumber(executor.failedTaskCount) || 0;
    const previousFailed =
      toNonNegativeNumber(previous.failedTaskCount) || 0;
    const rejected = toNonNegativeNumber(executor.rejectedTaskCount) || 0;
    const previousRejected =
      toNonNegativeNumber(previous.rejectedTaskCount) || 0;
    if (hasPrevious && failed > previousFailed) {
      reasons.push(`${executor.name || "线程池"}失败任务增加`);
    } else if (failed > 0) {
      reasons.push(`${executor.name || "线程池"}存在累计失败任务`);
    }
    if (hasPrevious && rejected > previousRejected) {
      reasons.push(`${executor.name || "线程池"}拒绝任务增加`);
    } else if (rejected > 0) {
      reasons.push(`${executor.name || "线程池"}存在累计拒绝任务`);
    }
  });

  if (reasons.length) {
    return { level: "warning", label: "需要关注", reasons };
  }

  if (!statusAvailable) {
    return { level: "unknown", label: "暂无数据", reasons: [] };
  }

  return { level: "normal", label: "运行正常", reasons: [] };
}
