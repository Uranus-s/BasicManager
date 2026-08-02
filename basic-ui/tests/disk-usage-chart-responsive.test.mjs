import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import vm from "node:vm";
import { parse } from "@vue/compiler-sfc";
import * as echarts from "echarts";

const componentPath = new URL(
  "../src/views/system/monitor/components/DiskUsageChart.vue",
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
  filename: "DiskUsageChart.vue",
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
  formatBytes: utils.formatBytes,
};
vm.runInNewContext(executableScript, sandbox);
const component = sandbox.component;

const context = {
  disks: [
    {
      name: "C:",
      type: "NTFS",
      usedPercent: 65,
      usedSpaceBytes: 65,
      usableSpaceBytes: 35,
      totalSpaceBytes: 100,
    },
    {
      name: "D:",
      type: "NTFS",
      usedPercent: 85,
      usedSpaceBytes: 85,
      usableSpaceBytes: 15,
      totalSpaceBytes: 100,
    },
  ],
  reducedMotion: false,
};

test("磁盘图在窄容器中减少横轴刻度且保留桌面图表行为", () => {
  const option = component.computed.chartOption.call(context);
  const mobile = option.media?.find(
    ({ query }) => query?.maxWidth <= 480
  )?.option;

  assert.ok(mobile, "磁盘图应提供 480px 以下的 ECharts media 配置");
  assert.ok(mobile.grid.left < 88);
  assert.ok(mobile.grid.right < 42);
  assert.ok(mobile.xAxis.splitNumber <= 2);
  assert.equal(mobile.xAxis.axisLabel.hideOverlap, true);

  assert.equal(option.baseOption.grid.left, 88);
  assert.equal(option.baseOption.grid.right, 42);
  assert.equal(option.baseOption.grid.top, 12);
  assert.equal(option.baseOption.grid.bottom, 24);
  assert.deepEqual(
    Array.from(option.baseOption.series[0].data, ({ value }) => value),
    [85, 65]
  );
  assert.match(
    option.baseOption.tooltip.formatter([{ dataIndex: 0 }]),
    /D:/
  );
  assert.match(component.computed.chartAriaLabel.call(context), /D: 85\.0%/);
});

test("磁盘图从窄屏扩到桌面宽度时恢复桌面坐标轴", () => {
  const chart = echarts.init(null, null, {
    renderer: "svg",
    ssr: true,
    width: 248,
    height: 260,
  });
  try {
    chart.setOption(component.computed.chartOption.call(context));
    const mobile = chart.getOption();
    assert.equal(mobile.grid[0].left, 62);
    assert.equal(mobile.xAxis[0].splitNumber, 2);
    assert.equal(mobile.xAxis[0].axisLabel.hideOverlap, true);

    chart.resize({ width: 800, height: 260 });
    const desktop = chart.getOption();
    assert.equal(desktop.grid[0].left, 88);
    assert.equal(desktop.grid[0].right, 42);
    assert.equal(desktop.xAxis[0].splitNumber, 5);
    assert.equal(desktop.xAxis[0].axisLabel.hideOverlap, false);
  } finally {
    chart.dispose();
  }
});
