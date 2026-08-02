import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import { parse } from "@vue/compiler-sfc";

const source = await readFile(
  new URL("../src/views/system/monitor/index.vue", import.meta.url),
  "utf8"
);
const { descriptor, errors } = parse(source, {
  filename: "SystemMonitor.vue",
});
assert.deepEqual(errors, []);

const channel = (hex, offset) =>
  Number.parseInt(hex.slice(offset, offset + 2), 16) / 255;
const luminance = (hex) =>
  [1, 3, 5]
    .map((offset) => {
      const value = channel(hex, offset);
      return value <= 0.04045
        ? value / 12.92
        : ((value + 0.055) / 1.055) ** 2.4;
    })
    .reduce(
      (sum, value, index) =>
        sum + value * [0.2126, 0.7152, 0.0722][index],
      0
    );
const contrast = (foreground, background) => {
  const lighter = Math.max(luminance(foreground), luminance(background));
  const darker = Math.min(luminance(foreground), luminance(background));
  return (lighter + 0.05) / (darker + 0.05);
};

test("hero 普通文字在白底达到 WCAG AA 对比度", () => {
  const style = descriptor.styles[0].content;
  const rule = style.match(/\.hero-summary\s*>\s*p\s*\{([\s\S]*?)\}/)?.[1];
  const color = rule?.match(/color:\s*(#[0-9a-f]{6})/i)?.[1];

  assert.ok(color, "应能解析 hero 普通文字颜色");
  assert.ok(
    contrast(color, "#ffffff") >= 4.5,
    `${color} 在白底的对比度应不低于 4.5:1`
  );
});
