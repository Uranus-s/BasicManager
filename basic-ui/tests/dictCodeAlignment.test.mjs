import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

const readProjectFile = (relativePath) =>
  readFileSync(new URL(relativePath, import.meta.url), "utf8");

const frontendSources = [
  "../src/store/modules/dict.js",
  "../src/views/system/user/index.vue",
  "../src/views/system/role/index.vue",
  "../src/views/system/dept/index.vue",
  "../src/views/system/dict/index.vue",
  "../src/views/system/permission/index.vue",
];

test("初始化脚本定义前端使用的正式字典编码", () => {
  const initSql = readProjectFile("../../initSql.sql");

  assert.match(initSql, /'sys_user_status'/);
  assert.match(initSql, /'sys_permission_type'/);
});

test("系统管理页面和字典 store 不再引用旧字典编码", () => {
  for (const sourcePath of frontendSources) {
    const source = readProjectFile(sourcePath);

    assert.doesNotMatch(source, /(["'])user_status\1/, `${sourcePath} 仍使用 user_status`);
    assert.doesNotMatch(source, /(["'])menu_type\1/, `${sourcePath} 仍使用 menu_type`);
  }
});

test("状态与权限类型使用初始化脚本中的正式字典编码", () => {
  for (const sourcePath of frontendSources) {
    assert.match(
      readProjectFile(sourcePath),
      /(["'])sys_user_status\1/,
      `${sourcePath} 未使用 sys_user_status`
    );
  }

  assert.match(
    readProjectFile("../src/views/system/permission/index.vue"),
    /(["'])sys_permission_type\1/
  );
});
