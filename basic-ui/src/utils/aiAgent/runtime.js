import { reactive } from "vue"

import * as agentApi from "@/api/ai/agent"
import appRouter from "@/router"
import { pageRegistry } from "@/utils/aiAgent/pageRegistry"
import { isTerminalTask } from "@/utils/aiAgent/protocol"
import {
  clearAgentTrace,
  setAgentTrace,
} from "@/utils/aiAgent/traceContext"

const TERMINAL_ACTION_STATUSES = new Set(["SUCCEEDED", "FAILED"])

function unwrap(response) {
  return response?.data ?? response
}

function createClientInstanceId() {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID()
  }
  return `agent-${Date.now()}-${Math.random().toString(36).slice(2)}`
}

/** 获取当前标签页稳定的客户端 ID，刷新页面时继续绑定同一服务端任务。 */
export function getOrCreateSessionId(key) {
  if (typeof sessionStorage === "undefined") return createClientInstanceId()
  const existing = sessionStorage.getItem(key)
  if (existing) return existing
  const created = createClientInstanceId()
  sessionStorage.setItem(key, created)
  return created
}

function resultSummary(result) {
  if (typeof result === "string") return result
  return result?.summary || "页面动作已完成"
}

function errorSummary(error) {
  return error instanceof Error ? error.message : String(error || "页面动作失败")
}

/** 创建单标签页任务运行时；依赖参数只用于隔离契约检查。 */
export function createAiAgentRuntime({
  api = agentApi,
  registry = pageRegistry,
  trace = { setAgentTrace, clearAgentTrace },
  router = appRouter,
  clientInstanceId = getOrCreateSessionId("basic-ai-agent-client"),
} = {}) {
  const state = reactive({
    enabled: false,
    task: null,
    action: null,
    confirmation: null,
    requiresClaim: false,
    error: null,
  })
  // 去重集合保存在当前运行时并在创建新任务时清空；服务端仍通过动作状态保证最终幂等。
  const handledActionIds = new Set()
  let streamSession = null
  let lastSnapshot = null
  let serverPaused = false

  function currentRouteName() {
    const routeName = router.currentRoute.value.name
    if (!routeName) throw new Error("当前页面缺少可绑定的路由名称")
    return String(routeName)
  }

  async function waitForRouteChange(previousRouteName, timeoutMs = 1000) {
    const deadline = Date.now() + timeoutMs
    while (Date.now() < deadline) {
      const routeName = currentRouteName()
      if (routeName !== previousRouteName) return routeName
      // Element Plus 菜单点击不会返回 router.push 的 Promise，只能等待路由状态实际更新。
      await new Promise((resolve) => setTimeout(resolve, 20))
    }
    return currentRouteName()
  }

  function updateTaskStatus(status) {
    if (!state.task || !status) return
    state.task = { ...state.task, status }
  }

  function stopStream() {
    if (!streamSession) return
    streamSession.intentional = true
    streamSession.controller.abort()
    streamSession = null
  }

  async function executeAction(taskId, action) {
    if (!action?.actionId || handledActionIds.has(action.actionId)) return
    // 收到事件后先登记动作 ID，避免回放流或重连事件在异步执行期间再次进入同一动作。
    handledActionIds.add(action.actionId)
    state.action = action

    let status = "SUCCEEDED"
    let summary
    let snapshot
    let executionRouteName
    let executionPageVersion
    try {
      const routeName = currentRouteName()
      // 执行动作前重新采集快照，动作必须与生成时的路由和页面版本一致，防止操作已经变化的页面。
      snapshot = await registry.snapshot(routeName)
      lastSnapshot = snapshot
      executionRouteName = routeName
      executionPageVersion = snapshot.pageVersion
      if (
        action.routeName !== routeName ||
        action.pageVersion !== snapshot.pageVersion
      ) {
        throw new Error("AI 动作与当前页面路由或版本不一致")
      }
      // 只在代理实际调用页面动作期间挂载追踪上下文，使动作触发的业务请求可被后端审计关联。
      trace.setAgentTrace({ taskId, actionId: action.actionId })
      try {
        const result = await registry.execute(routeName, action)
        summary = resultSummary(result)
        const resultRouteName = result?.routeChangeExpected
          ? await waitForRouteChange(routeName)
          : currentRouteName()
        snapshot = await registry.snapshot(resultRouteName)
        lastSnapshot = snapshot
      } catch (error) {
        status = "FAILED"
        summary = errorSummary(error)
        try {
          // 失败后尽量上报最新页面状态；若页面已损坏，则退回到执行前最后一个可信快照。
          snapshot = await registry.snapshot(routeName)
          lastSnapshot = snapshot
        } catch (_snapshotError) {
          snapshot = lastSnapshot
        }
      } finally {
        trace.clearAgentTrace()
      }
    } catch (error) {
      status = "FAILED"
      summary = errorSummary(error)
      snapshot = lastSnapshot
    }

    if (!snapshot) {
      // 没有页面快照时不上报不完整结果，先在本地暂停并等待用户恢复页面上下文。
      state.error = "页面快照不可用，动作结果无法安全上报"
      updateTaskStatus("PAUSED")
      return
    }

    try {
      await api.reportAiAgentActionResult(taskId, action.actionId, {
        clientInstanceId,
        status,
        resultSummary: summary,
        executionRouteName,
        executionPageVersion,
        snapshot,
      })
      if (status === "FAILED") serverPaused = true
    } catch (error) {
      state.error = errorSummary(error)
      updateTaskStatus("PAUSED")
      return
    }

    if (status === "FAILED") {
      state.error = summary
      updateTaskStatus("PAUSED")
    }
  }

  async function handleEvent(event) {
    if (!event) return
    if (
      event.type === "action" &&
      handledActionIds.has(event.action?.actionId)
    ) {
      return
    }
    if (state.task && event.taskId === state.task.id && event.status) {
      // 服务端事件状态是事实来源，本地状态只用于立即反馈和断线期间的保守展示。
      updateTaskStatus(event.status)
    }

    if (event.type === "confirmation") {
      state.action = event.action
      state.confirmation = {
        action: event.action,
        summary: event.summary,
      }
      return
    }
    if (event.type === "action") {
      state.confirmation = null
      await executeAction(event.taskId, event.action)
      return
    }
    if (event.type === "paused") {
      serverPaused = true
      state.error = event.summary || state.error
    }
    if (isTerminalTask(event.status)) {
      state.action = null
      state.confirmation = null
      stopStream()
    }
  }

  async function pauseAfterDisconnect(session, error) {
    if (
      session.intentional ||
      isTerminalTask(state.task) ||
      serverPaused
    ) {
      return
    }
    if (error) state.error = errorSummary(error)
    try {
      // 非主动断线时显式请求暂停；即使服务端流终止钩子也会暂停，这个请求仍保持跨部署实现的一致性。
      await api.pauseAiAgentTask(state.task.id, clientInstanceId)
      serverPaused = true
      updateTaskStatus("PAUSED")
    } catch (pauseError) {
      state.error = errorSummary(pauseError)
    }
  }

  function connect(taskId) {
    stopStream()
    // 每次连接使用独立会话对象，旧连接的 finally 不能清空后来建立的新连接。
    const session = {
      controller: new AbortController(),
      intentional: false,
    }
    streamSession = session
    let streamPromise
    try {
      streamPromise = api.streamAiAgentEvents(taskId, {
        clientInstanceId,
        signal: session.controller.signal,
        onEvent: (_type, event) => handleEvent(event),
      })
    } catch (error) {
      streamPromise = Promise.reject(error)
    }
    Promise.resolve(streamPromise)
      .then(
        () => pauseAfterDisconnect(session),
        (error) => pauseAfterDisconnect(session, error)
      )
      .finally(() => {
        if (streamSession === session) streamSession = null
      })
  }

  async function refresh() {
    state.error = null
    const status = unwrap(await api.getAiAgentStatus(clientInstanceId)) || {}
    state.enabled = Boolean(status.enabled)
    state.task = status.activeTask || null
    // 服务端返回的绑定关系决定当前标签页能否执行；其他标签页只能先接管暂停任务。
    state.requiresClaim = Boolean(state.task && status.currentClient === false)
    serverPaused = state.task?.status === "PAUSED"
    state.action = state.requiresClaim ? null : state.task?.activeAction || null
    if (
      state.action?.actionId &&
      TERMINAL_ACTION_STATUSES.has(state.action.status)
    ) {
      // 刷新后把服务端已终结动作加入去重集合，避免历史 activeAction 被页面再次执行。
      handledActionIds.add(state.action.actionId)
    }
    state.confirmation =
      state.task?.status === "WAITING_CONFIRMATION" && state.action
        ? {
            action: state.action,
            summary:
              state.action.confirmationSummary ||
              "高风险操作详情不可用，请终止任务后重新发起",
          }
        : null
    return status
  }

  async function start(goal) {
    state.error = null
    // 面板挂载时的状态刷新可能尚未结束；创建前以服务端状态为准，避免重复创建活动任务。
    await refresh()
    if (state.task && !isTerminalTask(state.task)) {
      return state.task
    }
    // 创建任务时提交首个页面快照，服务端据此绑定路由、版本和允许模型看到的页面能力。
    lastSnapshot = await registry.snapshot(currentRouteName())
    let task
    try {
      task = unwrap(
        await api.createAiAgentTask({
          goal,
          clientInstanceId,
          snapshot: lastSnapshot,
        })
      )
    } catch (createError) {
      try {
        await refresh()
      } catch (_refreshError) {
        throw createError
      }
      if (state.task && !isTerminalTask(state.task)) {
        return state.task
      }
      throw createError
    }
    state.task = task
    state.requiresClaim = false
    state.action = null
    state.confirmation = null
    handledActionIds.clear()
    serverPaused = false
    connect(task.id)
    return task
  }

  async function pause() {
    if (!state.task || isTerminalTask(state.task)) return
    if (serverPaused) {
      stopStream()
      return
    }
    await api.pauseAiAgentTask(state.task.id, clientInstanceId)
    serverPaused = true
    stopStream()
    updateTaskStatus("PAUSED")
  }

  async function resume() {
    if (!state.task) return
    // 恢复前必须重新采集快照，让服务端从用户当前看到的页面重新规划，而不是重放旧动作。
    lastSnapshot = await registry.snapshot(currentRouteName())
    await api.resumeAiAgentTask(state.task.id, {
      clientInstanceId,
      snapshot: lastSnapshot,
    })
    serverPaused = false
    state.error = null
    updateTaskStatus("PLANNING")
    connect(state.task.id)
  }

  async function cancel() {
    if (!state.task || isTerminalTask(state.task)) return
    await api.cancelAiAgentTask(state.task.id, clientInstanceId)
    stopStream()
    updateTaskStatus("CANCELED")
    state.action = null
    state.confirmation = null
  }

  async function confirm(actionId = state.confirmation?.action?.actionId) {
    if (!state.task || !actionId) {
      throw new Error("当前没有待确认的 AI 动作")
    }
    // 确认接口只改变服务端动作状态；真正执行仍等待服务端随后发布的 action 事件。
    await api.confirmAiAgentAction(state.task.id, actionId, clientInstanceId)
    state.confirmation = null
  }

  async function takeover() {
    if (state.task && state.requiresClaim) {
      // 接管只转移暂停任务的标签页归属，不自动恢复；用户仍需在新页面确认快照后主动恢复。
      const claimed = unwrap(
        await api.claimAiAgentTask(state.task.id, clientInstanceId)
      )
      state.task = claimed || state.task
      state.requiresClaim = false
      serverPaused = true
      state.action = null
      state.confirmation = null
      state.error = null
      return
    }
    await pause()
    state.action = null
    state.confirmation = null
  }

  return Object.freeze({
    state,
    clientInstanceId,
    refresh,
    start,
    pause,
    resume,
    cancel,
    confirm,
    takeover,
    handleEvent,
  })
}

export const aiAgentRuntime = createAiAgentRuntime()
