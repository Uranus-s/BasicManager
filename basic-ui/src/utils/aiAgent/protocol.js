export const TERMINAL_TASK_STATES = Object.freeze([
  "SUCCEEDED",
  "FAILED",
  "CANCELED",
])

export const TERMINAL_TASK_STATUSES = TERMINAL_TASK_STATES

const terminalTaskStatuses = new Set(TERMINAL_TASK_STATES)

/** 判断任务是否已经进入服务端定义的不可恢复终态。 */
export function isTerminalTask(taskOrStatus) {
  const status =
    typeof taskOrStatus === "string" ? taskOrStatus : taskOrStatus?.status
  return terminalTaskStatuses.has(status)
}

/** HIGH 风险动作必须由用户明确确认后才可执行。 */
export function requiresConfirmation(actionOrRiskLevel) {
  const riskLevel =
    typeof actionOrRiskLevel === "string"
      ? actionOrRiskLevel
      : actionOrRiskLevel?.riskLevel
  return riskLevel === "HIGH"
}
