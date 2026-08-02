import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import vm from "node:vm";
import { parse } from "@vue/compiler-sfc";
import * as echarts from "echarts";

const componentPath = new URL(
  "../src/views/system/monitor/components/ThreadPoolPanel.vue",
  import.meta.url
);
const utilsPath = new URL(
  "../src/views/system/monitor/monitorUtils.js",
  import.meta.url
);

const utilsSource = await readFile(utilsPath, "utf8");
const utils = await import(
  `data:text/javascript;charset=utf-8,${encodeURIComponent(utilsSource)}`
);
const componentSource = await readFile(componentPath, "utf8");
const { descriptor, errors } = parse(componentSource, {
  filename: "ThreadPoolPanel.vue",
});

assert.deepEqual(errors, []);

const executableScript = descriptor.script.content
  .replace(/^import VChart.*$/m, "")
  .replace(
    /import\s*\{[\s\S]*?\}\s*from\s*["']\.\.\/monitorUtils["'];/,
    ""
  )
  .replace("export default", "globalThis.component =");
const sandbox = {
  VChart: {},
  getQueueLoad: utils.getQueueLoad,
  getThreadPoolLoad: utils.getThreadPoolLoad,
};
vm.runInNewContext(executableScript, sandbox);
const component = sandbox.component;

const findElements = (node, tag, matches = []) => {
  if (node?.type === 1 && node.tag === tag) matches.push(node);
  for (const child of node?.children || []) {
    findElements(child, tag, matches);
  }
  return matches;
};

const getStaticAttribute = (node, name) =>
  node.props.find((prop) => prop.type === 6 && prop.name === name)?.value
    ?.content;

const longExecutorName = "async-notification-background-executor";
const context = {
  executors: [
    {
      name: longExecutorName,
      activeCount: 4,
      maximumPoolSize: 8,
      queueSize: 2,
      queueCapacity: 10,
    },
  ],
  trend: [
    {
      time: "2026-07-25 14:32:18",
      executors: { [longExecutorName]: 4 },
    },
  ],
  reducedMotion: false,
};

test("线程池明细展示队列剩余容量并复用空值格式化", () => {
  const columns = findElements(
    descriptor.template.ast,
    "el-table-column"
  );
  const remainingCapacityColumn = columns.find(
    (column) =>
      getStaticAttribute(column, "prop") === "queueRemainingCapacity"
  );

  assert.ok(remainingCapacityColumn, "应渲染队列剩余容量列");
  assert.equal(
    getStaticAttribute(remainingCapacityColumn, "label"),
    "剩余容量"
  );
  assert.equal(component.methods.formatNullable({}, {}, 500), 500);
  assert.equal(component.methods.formatNullable({}, {}, null), "—");
});

test("线程池图表在窄容器中使用紧凑坐标轴配置", () => {
  const loadOption = component.computed.loadOption.call(context);
  const trendOption = component.computed.trendOption.call(context);
  const loadMobile = loadOption.media?.find(
    ({ query }) => query?.maxWidth <= 480
  )?.option;
  const trendMobile = trendOption.media?.find(
    ({ query }) => query?.maxWidth <= 480
  )?.option;

  assert.ok(loadMobile, "实时负载图应提供 480px 以下的 ECharts media 配置");
  assert.ok(loadMobile.grid.left <= 88);
  assert.ok(loadMobile.grid.right <= 16);
  assert.ok(loadMobile.xAxis.splitNumber <= 2);
  assert.equal(loadMobile.xAxis.axisLabel.hideOverlap, true);

  assert.ok(trendMobile, "趋势图应提供 480px 以下的 ECharts media 配置");
  assert.equal(trendMobile.xAxis.axisLabel.hideOverlap, true);
  assert.equal(
    trendMobile.xAxis.axisLabel.formatter("2026-07-25 14:32:18"),
    "14:32:18"
  );
});

test("移动端轴标签可省略，但 tooltip 与 ARIA 保留执行器全名", () => {
  const loadOption = component.computed.loadOption.call(context);
  const loadMobile = loadOption.media?.find(
    ({ query }) => query?.maxWidth <= 480
  )?.option;
  assert.ok(loadMobile, "实时负载图应提供移动端轴标签配置");
  const abbreviatedName =
    loadMobile.yAxis.axisLabel.formatter(longExecutorName);
  const tooltip = loadOption.baseOption.tooltip.formatter([
    {
      seriesName: "线程负载",
      value: 50,
      name: longExecutorName,
      axisValueLabel: abbreviatedName,
    },
  ]);
  const ariaLabel = component.computed.loadChartAriaLabel.call(context);

  assert.notEqual(abbreviatedName, longExecutorName);
  assert.match(tooltip, new RegExp(longExecutorName));
  assert.match(ariaLabel, new RegExp(longExecutorName));
});

test("线程池图表从窄屏扩到桌面宽度时恢复桌面坐标轴", () => {
  const cases = [
    {
      option: component.computed.loadOption.call(context),
      assertMobile(option) {
        assert.equal(option.grid[0].left, 82);
        assert.equal(option.xAxis[0].splitNumber, 2);
        assert.equal(option.xAxis[0].axisLabel.hideOverlap, true);
      },
      assertDesktop(option) {
        assert.equal(option.grid[0].left, 150);
        assert.equal(option.grid[0].right, 42);
        assert.equal(option.xAxis[0].splitNumber, 5);
        assert.equal(option.xAxis[0].axisLabel.hideOverlap, false);
        assert.equal(
          option.yAxis[0].axisLabel.formatter(longExecutorName),
          longExecutorName
        );
      },
    },
    {
      option: component.computed.trendOption.call(context),
      assertMobile(option) {
        assert.equal(option.grid[0].left, 38);
        assert.equal(
          option.xAxis[0].axisLabel.formatter("2026-07-25 14:32:18"),
          "14:32:18"
        );
      },
      assertDesktop(option) {
        assert.equal(option.grid[0].left, 48);
        assert.equal(option.grid[0].right, 20);
        assert.equal(option.xAxis[0].axisLabel.hideOverlap, false);
        assert.equal(
          option.xAxis[0].axisLabel.formatter("2026-07-25 14:32:18"),
          "2026-07-25 14:32:18"
        );
      },
    },
  ];

  for (const chartCase of cases) {
    const chart = echarts.init(null, null, {
      renderer: "svg",
      ssr: true,
      width: 248,
      height: 260,
    });
    try {
      chart.setOption(chartCase.option);
      chartCase.assertMobile(chart.getOption());
      chart.resize({ width: 800, height: 260 });
      chartCase.assertDesktop(chart.getOption());
    } finally {
      chart.dispose();
    }
  }
});
