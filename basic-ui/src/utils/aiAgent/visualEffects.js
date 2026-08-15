import { isTerminalTask } from "@/utils/aiAgent/protocol"

const targetTimers = new WeakMap()

/** 将服务端任务状态收敛为旁观层需要的四种视觉状态。 */
export function observerVisualState(task, confirmation) {
  if (!task || isTerminalTask(task)) return "idle"
  if (confirmation || task.status === "WAITING_CONFIRMATION") return "confirmation"
  if (task.status === "PAUSED") return "paused"
  return "active"
}

/** 短暂标记当前语义目标；样式由桌面旁观层统一提供。 */
export function pulseAgentTarget(element) {
  if (!element?.classList) return
  // 同一元素连续触发时复用单个生命周期，避免旧定时器提前移除新一轮高亮。
  const previousTimer = targetTimers.get(element)
  if (previousTimer) clearTimeout(previousTimer)
  element.classList.remove("agent-target-active")
  void element.offsetWidth
  element.classList.add("agent-target-active")
  const timer = setTimeout(() => {
    element.classList.remove("agent-target-active")
    targetTimers.delete(element)
  }, 900)
  targetTimers.set(element, timer)
}
