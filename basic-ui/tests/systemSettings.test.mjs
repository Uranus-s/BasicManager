import assert from "node:assert/strict";
import test from "node:test";

import {
  buildPageTitle,
  normalizeSystemName,
} from "../src/utils/systemName.js";
import {
  canManage,
  hasSettingChanges,
  isBasicSettingsValid,
  isSecuritySettingsValid,
  validateTemporaryPasswords,
} from "../src/utils/systemSettings.js";

test("系统名称会去除首尾空白，并在空值时使用静态名称", () => {
  assert.equal(normalizeSystemName("  基础管理系统  ", "BasicManage"), "基础管理系统");
  assert.equal(normalizeSystemName("   ", "BasicManage"), "BasicManage");
  assert.equal(normalizeSystemName(undefined, "BasicManage"), "BasicManage");
});

test("页面标题由当前页面和运行时系统名称组成", () => {
  assert.equal(buildPageTitle("用户管理", "基础管理系统"), "用户管理-基础管理系统");
  assert.equal(buildPageTitle("", "基础管理系统"), "基础管理系统");
});

test("设置脏状态只比较指定业务字段", () => {
  const saved = { systemName: "基础管理系统", tokenExpireHours: 24 };
  assert.equal(hasSettingChanges(saved, { ...saved }), false);
  assert.equal(hasSettingChanges(saved, { ...saved, systemName: "新名称" }), true);
  assert.equal(hasSettingChanges(saved, { ...saved, loading: true }), false);
});

test("管理操作只按后端要求的具体权限判断", () => {
  assert.equal(canManage([], ["admin"], "system:config:edit"), false);
  assert.equal(canManage(["system:config:edit"], [], "system:config:edit"), true);
  assert.equal(canManage(["system:config:query"], ["user"], "system:config:edit"), false);
});

test("设置保存状态会拒绝空白名称和越界的 Token 有效期", () => {
  assert.equal(isBasicSettingsValid("   "), false);
  assert.equal(isBasicSettingsValid("基础管理系统"), true);
  assert.equal(isBasicSettingsValid("系".repeat(51)), false);
  assert.equal(isSecuritySettingsValid(0), false);
  assert.equal(isSecuritySettingsValid(24), true);
  assert.equal(isSecuritySettingsValid(169), false);
  assert.equal(isSecuritySettingsValid(1.5), false);
});

test("临时密码必须为 6 到 20 位且两次输入一致", () => {
  assert.equal(validateTemporaryPasswords("12345", "12345"), "临时密码长度必须在 6 到 20 个字符之间");
  assert.equal(validateTemporaryPasswords("123456", "654321"), "两次输入的密码不一致");
  assert.equal(validateTemporaryPasswords("123456", "123456"), "");
});
