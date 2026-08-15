import { fetchEventSource } from "@microsoft/fetch-event-source"

import { baseURL } from "@/config"
import { getAccessToken } from "@/utils/accessToken"
import request from "@/utils/request"

export function getAiAgentStatus(clientInstanceId) {
  return request({
    baseURL: "",
    url: "/ai/agent/status",
    method: "get",
    params: { clientInstanceId },
  })
}

export function createAiAgentTask(data) {
  return request({
    baseURL: "",
    url: "/ai/agent/tasks",
    method: "post",
    retry: 0,
    // 同用户并发创建由运行时重新同步活动任务，不在请求层提前弹出错误。
    silentErrorCodes: [40008],
    data,
  })
}

export function getAiAgentTask(taskId) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}`,
    method: "get",
  })
}

export function reportAiAgentActionResult(taskId, actionId, data) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/actions/${actionId}/result`,
    method: "post",
    retry: 0,
    data,
  })
}

export function pauseAiAgentTask(taskId, clientInstanceId) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/pause`,
    method: "post",
    retry: 0,
    data: { clientInstanceId },
  })
}

export function resumeAiAgentTask(taskId, data) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/resume`,
    method: "post",
    retry: 0,
    data,
  })
}

export function cancelAiAgentTask(taskId, clientInstanceId) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/cancel`,
    method: "post",
    retry: 0,
    data: { clientInstanceId },
  })
}

export function claimAiAgentTask(taskId, clientInstanceId) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/claim`,
    method: "post",
    retry: 0,
    data: { clientInstanceId },
  })
}

export function confirmAiAgentAction(taskId, actionId, clientInstanceId) {
  return request({
    baseURL: "",
    url: `/ai/agent/tasks/${taskId}/actions/${actionId}/confirm`,
    method: "post",
    retry: 0,
    data: { clientInstanceId },
  })
}

/**
 * 代理事件流断开后由运行时进入暂停态；这里直接抛错，禁止底层库自动重连。
 */
export function streamAiAgentEvents(
  taskId,
  { clientInstanceId, signal, onEvent } = {}
) {
  const query = new URLSearchParams({ clientInstanceId }).toString()
  return fetchEventSource(
    `${baseURL || ""}/ai/agent/tasks/${taskId}/events?${query}`,
    {
      method: "GET",
      headers: {
        Accept: "text/event-stream",
        Authorization: `Bearer ${getAccessToken()}`,
      },
      signal,
      openWhenHidden: true,
      onmessage(event) {
        const data = JSON.parse(event.data || "{}")
        onEvent?.(event.event || data.type, data)
      },
      onerror(error) {
        throw error
      },
    }
  )
}
