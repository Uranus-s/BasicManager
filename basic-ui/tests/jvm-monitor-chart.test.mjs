import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import vm from "node:vm";
import { parse } from "@vue/compiler-sfc";

const componentPath = new URL(
  "../src/views/system/monitor/components/JvmMonitorChart.vue",
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
  filename: "JvmMonitorChart.vue",
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
  formatPercent: utils.formatPercent,
};
vm.runInNewContext(executableScript, sandbox);
const component = sandbox.component;

test("JVM 摘要格式化非堆已用并稳定处理非法值", () => {
  assert.equal(
    typeof component.computed.nonHeapUsedText,
    "function",
    "应提供非堆已用摘要"
  );
  assert.equal(
    component.computed.nonHeapUsedText.call({
      jvm: { nonHeapUsedBytes: 1536 },
    }),
    "1.5 KB"
  );
  for (const nonHeapUsedBytes of [null, "", -1, "invalid"]) {
    assert.equal(
      component.computed.nonHeapUsedText.call({
        jvm: { nonHeapUsedBytes },
      }),
      "—"
    );
  }
  assert.match(descriptor.template.content, /nonHeapUsedText/);
});

test("JVM 趋势图显示双系列图例和左右轴名称", () => {
  const option = component.computed.chartOption.call({
    reducedMotion: false,
    trend: [
      {
        time: "2026-07-25 14:32:18",
        heapPercent: 50,
        threadCount: 12,
      },
    ],
  });

  assert.ok(option.legend, "双轴趋势图应有可见图例");
  assert.deepEqual(Array.from(option.legend.data), [
    "堆内存使用率",
    "线程数",
  ]);
  assert.equal(option.series[0].name, "堆内存使用率");
  assert.equal(option.series[1].name, "线程数");
  assert.equal(option.yAxis[0].name, "堆内存使用率");
  assert.equal(option.yAxis[1].name, "线程数");
  assert.ok(option.grid.top >= 48);
  assert.ok(option.grid.left >= 50);
  assert.ok(option.grid.right >= 46);
});
