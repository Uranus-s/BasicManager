// 仅接受后端约定的事件名，未知事件不进入聊天组件状态机。
const ASSISTANT_STREAM_EVENTS = new Set([
  "start",
  "delta",
  "tool_start",
  "tool_result",
  "approval_required",
  "done",
  "error",
])

export const isAssistantStreamEvent = (type) => ASSISTANT_STREAM_EVENTS.has(type)

/**
 * 归约 AI 助手 SSE 事件。工具进度和审批预览使用独立状态，禁止混入助手正文。
 *
 * @param {object} state 当前生成状态、工具提示和待审批预览
 * @param {string} type 后端 SSE 事件名
 * @param {object} data 当前事件的结构化数据
 * @returns {object} 不修改原对象的新状态；未知事件原样返回
 */
export const reduceAssistantEvent = (state, type, data = {}) => {
  if (type === "start") {
    return { ...state, generating: true, toolStatus: "" }
  }
  if (type === "tool_start") {
    return {
      ...state,
      toolStatus: data.toolName ? `正在${data.toolName}` : "正在执行工具",
    }
  }
  if (type === "tool_result") {
    return {
      ...state,
      toolStatus: data.summary || (data.toolName ? `${data.toolName}已完成` : "工具执行完成"),
    }
  }
  if (type === "approval_required") {
    // 写工具只生成预览；收到该事件即结束生成态，等待用户用下一条消息确认。
    return {
      ...state,
      generating: false,
      toolStatus: "",
      pendingAction: data.pendingAction || null,
    }
  }
  if (type === "done" || type === "error") {
    return { ...state, generating: false, toolStatus: "" }
  }
  if (type === "delta") {
    // 模型恢复输出正文时清除上一工具的完成提示，正文由组件单独追加。
    return { ...state, toolStatus: "" }
  }
  return state
}
