const SETTING_KEYS = ["systemName", "tokenExpireHours"];

/**
 * 判断系统设置表单是否包含尚未保存的业务字段变更。
 * 加载状态等页面字段不参与比较，避免产生误报。
 */
export function hasSettingChanges(saved, current) {
  return SETTING_KEYS.some((key) => saved?.[key] !== current?.[key]);
}

/**
 * 管理操作必须与后端接口使用同一个具体权限标识，角色本身不能替代接口授权。
 */
export function canManage(permissions, _roles, authority) {
  return Array.isArray(permissions) && permissions.includes(authority);
}

/**
 * 基础设置按后端保存前的 trim 结果校验，确保前后端边界一致。
 */
export function isBasicSettingsValid(systemName) {
  const normalizedName = typeof systemName === "string" ? systemName.trim() : "";
  return normalizedName.length >= 1 && normalizedName.length <= 50;
}

/**
 * Token 有效期只接受后端允许范围内的整数小时数。
 */
export function isSecuritySettingsValid(tokenExpireHours) {
  return (
    Number.isInteger(tokenExpireHours) &&
    tokenExpireHours >= 1 &&
    tokenExpireHours <= 168
  );
}

/**
 * 空 Key 代表保留服务端已有配置；只有管理员输入新值时才校验长度。
 */
export function isAiApiKeyValid(apiKey) {
  if (typeof apiKey !== "string") return false;
  return apiKey.trim().length <= 255;
}

/**
 * AI 表单不保存原 Key，因此仅新输入的非空值构成未保存修改。
 */
export function hasAiSettingChanges(form) {
  return typeof form?.apiKey === "string" && form.apiKey.trim().length > 0;
}

/**
 * 校验管理员输入的临时密码，返回空字符串表示校验通过。
 */
export function validateTemporaryPasswords(newPassword, confirmPassword) {
  if (!newPassword || newPassword.length < 6 || newPassword.length > 20) {
    return "临时密码长度必须在 6 到 20 个字符之间";
  }
  if (newPassword !== confirmPassword) return "两次输入的密码不一致";
  return "";
}
