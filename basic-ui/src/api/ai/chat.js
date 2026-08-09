import { fetchEventSource } from "@microsoft/fetch-event-source"

import { baseURL } from "@/config"
import { getAccessToken } from "@/utils/accessToken"
import request from "@/utils/request"

const CHAT_STREAM_EVENTS = new Set(["start", "delta", "done", "error"])

export function getAiChatMessages(params) {
  return request({
    baseURL: "",
    url: "/ai/chat/messages",
    method: "get",
    params,
    // 历史刷新由聊天组件控制，认证或网络失败时禁止公共拦截器放大请求次数。
    retry: 0,
  })
}

export function clearAiChatMemory() {
  return request({
    baseURL: "",
    url: "/ai/chat/memory",
    method: "delete",
  })
}

/**
 * 使用 POST SSE 消费模型增量。错误直接向调用方抛出，明确禁止库自动重连，
 * 避免重复提交用户问题和重复持久化回答。
 */
export function streamAiChat(message, { signal, onEvent } = {}) {
  return fetchEventSource(`${baseURL || ""}/ai/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getAccessToken()}`,
    },
    body: JSON.stringify({ message }),
    signal,
    // 标签页隐藏不应触发库的断开重建，同一用户并发由后端统一控制。
    openWhenHidden: true,
    onmessage(event) {
      if (!CHAT_STREAM_EVENTS.has(event.event)) return
      const data = JSON.parse(event.data || "{}")
      onEvent?.(event.event, data)
    },
    onerror(error) {
      throw error
    },
  })
}
