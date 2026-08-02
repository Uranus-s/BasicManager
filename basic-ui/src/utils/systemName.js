/**
 * 规范化后端返回的系统名称，接口异常或空值时使用静态配置兜底。
 *
 * @param {unknown} value 接口返回值
 * @param {unknown} fallback 静态兜底名称
 * @returns {string} 可直接展示的系统名称
 */
export function normalizeSystemName(value, fallback) {
  const normalized = typeof value === "string" ? value.trim() : "";
  if (normalized) return normalized;

  return typeof fallback === "string" ? fallback.trim() : "";
}

/**
 * 统一生成浏览器页签标题，避免各页面自行拼接导致格式不一致。
 *
 * @param {string} pageTitle 当前页面名称
 * @param {string} systemName 运行时系统名称
 * @returns {string} 浏览器页签标题
 */
export function buildPageTitle(pageTitle, systemName) {
  return pageTitle ? `${pageTitle}-${systemName}` : systemName;
}
