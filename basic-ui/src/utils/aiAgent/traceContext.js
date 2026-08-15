let activeTrace = null

/**
 * 标记当前由代理动作触发的业务请求。上下文仅保存在当前标签页内，
 * 不写入持久化存储，避免刷新后错误关联到旧动作。
 */
export function setAgentTrace(trace) {
  if (!trace?.taskId || !trace?.actionId) {
    throw new Error("AI 代理链路必须包含任务 ID 和动作 ID")
  }
  activeTrace = Object.freeze({
    taskId: trace.taskId,
    actionId: trace.actionId,
  })
}

export function getAgentTrace() {
  return activeTrace
}

/**
 * Axios 请求拦截器消费一次追踪信息，将代理动作关联到它触发的第一笔业务请求。
 */
export function consumeAgentTrace() {
  // 读取即清除，防止一次代理交互意外关联到同一事件循环之后的人工请求。
  const trace = activeTrace
  activeTrace = null
  return trace
}

export function clearAgentTrace() {
  // 动作执行的 finally 必须调用清理；失败路径也不能把追踪信息泄漏给后续请求。
  activeTrace = null
}
