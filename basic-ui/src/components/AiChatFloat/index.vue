<template>
  <el-tooltip v-if="!open" content="打开 AI 助手" placement="left">
    <button class="ai-launcher" type="button" aria-label="打开 AI 助手" @click="openChat">
      <el-icon><ChatDotRound /></el-icon>
    </button>
  </el-tooltip>

  <section
    v-else
    :class="['ai-chat-window', { minimized }]"
    :style="windowStyle"
    aria-label="AI 助手"
  >
    <header class="chat-header" @pointerdown="startDrag">
      <div class="chat-title">
        <span class="status-dot" aria-hidden="true" />
        <div>
          <strong>DeepSeek 助手</strong>
          <span>{{ generating ? "正在生成" : "随时可用" }}</span>
        </div>
      </div>
      <div class="header-actions" @pointerdown.stop>
        <el-tooltip content="清空聊天记录" placement="bottom">
          <button
            class="icon-button"
            type="button"
            aria-label="清空聊天记录"
            :disabled="generating"
            @click="clearMessages"
          >
            <el-icon><Delete /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip :content="minimized ? '展开' : '最小化'" placement="bottom">
          <button
            class="icon-button"
            type="button"
            :aria-label="minimized ? '展开 AI 助手' : '最小化 AI 助手'"
            @click="minimized = !minimized"
          >
            <el-icon><FullScreen v-if="minimized" /><Minus v-else /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip content="关闭" placement="bottom">
          <button class="icon-button" type="button" aria-label="关闭 AI 助手" @click="closeChat">
            <el-icon><Close /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </header>

    <template v-if="!minimized">
      <div ref="messageListRef" v-loading="loadingHistory" class="message-list">
        <button
          v-if="hasMore && messages.length"
          class="load-more"
          type="button"
          :disabled="loadingHistory"
          @click="loadOlder"
        >
          加载更早记录
        </button>

        <div v-if="!loadingHistory && !messages.length" class="empty-state">
          <el-icon><ChatLineRound /></el-icon>
          <strong>开始一段新对话</strong>
        </div>

        <article
          v-for="message in messages"
          :key="message.id"
          :class="['message-row', message.role === 'USER' ? 'user' : 'assistant']"
        >
          <div class="message-bubble">
            <div
              v-if="message.role === 'ASSISTANT'"
              class="markdown-body"
              v-html="renderMarkdown(message.content)"
            />
            <p v-else>{{ message.content }}</p>
            <span v-if="message.partial" class="partial-label">回答未完成</span>
          </div>
        </article>

        <div v-if="toolStatus" class="tool-status" role="status" aria-live="polite">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>{{ toolStatus }}</span>
        </div>

        <section v-if="pendingAction" class="pending-action" aria-label="待确认操作预览">
          <header>
            <el-icon><DocumentChecked /></el-icon>
            <div>
              <strong>{{ pendingAction.title || "操作待确认" }}</strong>
              <span>{{ pendingExpiresText }}</span>
            </div>
          </header>
          <dl v-if="pendingAction.fields?.length">
            <div v-for="(field, index) in pendingAction.fields" :key="`${field.label}:${index}`">
              <dt>{{ field.label }}</dt>
              <dd>{{ field.value }}</dd>
            </div>
          </dl>
          <div
            v-if="pendingAction.content"
            class="pending-content markdown-body"
            v-html="renderMarkdown(pendingAction.content)"
          />
        </section>

        <div v-if="errorMessage" class="stream-error" role="status">
          <span>{{ errorMessage }}</span>
          <el-button v-if="lastFailedQuestion" text type="primary" @click="retryLastMessage">
            <el-icon><RefreshRight /></el-icon>
            重试
          </el-button>
        </div>
      </div>

      <footer class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          maxlength="4000"
          resize="none"
          placeholder="输入消息"
          aria-label="聊天消息"
          @keydown.enter.exact.prevent="sendMessage()"
        />
        <el-tooltip
          :content="generating ? '停止生成' : pendingStoppedRound ? '正在保存部分回答' : '发送消息'"
          placement="top"
        >
          <button
            :class="['send-button', { stopping: generating }]"
            type="button"
            :aria-label="generating ? '停止生成' : '发送消息'"
            :disabled="!generating && (!input.trim() || Boolean(pendingStoppedRound))"
            @click="generating ? stopGenerating() : sendMessage()"
          >
            <el-icon><VideoPause v-if="generating" /><Promotion v-else /></el-icon>
          </button>
        </el-tooltip>
      </footer>

      <div
        class="resize-handle"
        role="separator"
        aria-label="调整 AI 助手窗口大小"
        @pointerdown="startResize"
      />
    </template>
  </section>
</template>

<script setup>
import {
  ChatDotRound,
  ChatLineRound,
  Close,
  Delete,
  DocumentChecked,
  FullScreen,
  Loading,
  Minus,
  Promotion,
  RefreshRight,
  VideoPause,
} from "@element-plus/icons-vue"
import dayjs from "dayjs"
import { ElMessage, ElMessageBox } from "element-plus"
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef } from "vue"

import { clearAiChatMemory, getAiChatMessages, streamAiChat } from "@/api/ai/chat"
import { reduceAssistantEvent } from "@/utils/aiAssistant"
import {
  appendDelta,
  clampWindowRect,
  createRequestCoalescer,
  defaultBottomRightRect,
  dockBottomRightRect,
  hasPersistedRound,
  loadStoredRect,
  mergeHistory,
  renderMarkdown,
  shouldRefreshHistory,
} from "@/utils/aiChat"

const OPEN_STORAGE_KEY = "basic-ai-chat-open"
const RECT_STORAGE_KEY = "basic-ai-chat-rect"
const CHANNEL_NAME = "basic-ai-chat"
const STOP_SYNC_MAX_ATTEMPTS = 6
const HISTORY_FOCUS_REFRESH_INTERVAL = 5_000

const viewport = () => ({ width: window.innerWidth, height: window.innerHeight })
const initialRect = defaultBottomRightRect(viewport())
const storedRect = loadStoredRect(localStorage.getItem(RECT_STORAGE_KEY), initialRect, viewport())

const open = ref(localStorage.getItem(OPEN_STORAGE_KEY) !== "false")
const minimized = ref(false)
const generating = ref(false)
const loadingHistory = ref(false)
const hasMore = ref(true)
const nextBeforeId = ref(null)
const messages = ref([])
const input = ref("")
const errorMessage = ref("")
const lastFailedQuestion = ref("")
// 工具进度与助手正文分离，避免状态提示被保存为聊天消息。
const toolStatus = ref("")
// 后端返回的唯一有效操作预览，确认动作仍通过自然语言消息发送。
const pendingAction = ref(null)
const rect = ref(dockBottomRightRect(storedRect, viewport()))
const messageListRef = ref(null)
const abortController = shallowRef(null)
const activeRound = shallowRef(null)
const pendingStoppedRound = shallowRef(null)

let interaction = null
let broadcastChannel = null
let refreshTimer = null
let temporaryId = 0
let lastHistoryRefreshAt = 0

const coalesceLatestRefresh = createRequestCoalescer()

const windowStyle = computed(() => ({
  left: `${rect.value.x}px`,
  top: `${rect.value.y}px`,
  width: `${rect.value.width}px`,
  height: minimized.value ? "56px" : `${rect.value.height}px`,
}))

const pendingExpiresText = computed(() => {
  if (!pendingAction.value?.expiresAt) return "等待确认"
  return `有效期至 ${dayjs(pendingAction.value.expiresAt).format("YYYY-MM-DD HH:mm")}`
})

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) messageListRef.value.scrollTop = messageListRef.value.scrollHeight
}

const historyPayload = (response) => response?.data || {}

const applyHistory = (history) => {
  messages.value = history.messages || []
  nextBeforeId.value = history.nextBeforeId ?? null
  hasMore.value = Boolean(history.hasMore)
  pendingAction.value = history.pendingAction || null
}

/**
 * 将助手事件归约结果同步回 Vue 状态；助手正文仍由 sendMessage 单独处理。
 */
const applyAssistantEvent = (type, data) => {
  const next = reduceAssistantEvent(
    {
      generating: generating.value,
      toolStatus: toolStatus.value,
      pendingAction: pendingAction.value,
    },
    type,
    data
  )
  generating.value = next.generating
  toolStatus.value = next.toolStatus
  pendingAction.value = next.pendingAction
}

/**
 * 最新页以数据库为准；发生错误时不调用本方法，从而保留尚未落库的问题和部分回答。
 */
const loadLatest = async ({ quiet = false } = {}) => {
  lastHistoryRefreshAt = Date.now()
  if (!quiet) loadingHistory.value = true
  try {
    const response = await getAiChatMessages({ limit: 50 })
    const history = historyPayload(response)
    applyHistory(history)
    if (!quiet) await scrollToBottom()
  } finally {
    if (!quiet) loadingHistory.value = false
  }
}

const refreshLatestQuietly = () =>
  coalesceLatestRefresh(() => loadLatest({ quiet: true }))

const loadOlder = async () => {
  if (loadingHistory.value || !hasMore.value || !nextBeforeId.value) return
  const list = messageListRef.value
  const previousHeight = list?.scrollHeight || 0
  loadingHistory.value = true
  try {
    const response = await getAiChatMessages({ beforeId: nextBeforeId.value, limit: 50 })
    const history = historyPayload(response)
    messages.value = mergeHistory(messages.value, history.messages || [])
    nextBeforeId.value = history.nextBeforeId ?? null
    hasMore.value = Boolean(history.hasMore)
    await nextTick()
    if (list) list.scrollTop += list.scrollHeight - previousHeight
  } finally {
    loadingHistory.value = false
  }
}

const broadcast = (type) => broadcastChannel?.postMessage({ type })

const finishStream = async () => {
  // 比较刷新前后的 Action ID，通知其他标签页是否需要清除旧审批预览。
  const previousPendingId = pendingAction.value?.id ?? null
  generating.value = false
  toolStatus.value = ""
  abortController.value = null
  activeRound.value = null
  try {
    await loadLatest({ quiet: true })
  } catch (_error) {
    // 模型已完成时不得因历史刷新失败进入重试，否则会重复提交同一问题。
    ElMessage.warning("回答已完成，聊天历史暂未刷新")
  }
  const nextPendingId = pendingAction.value?.id ?? null
  broadcast(previousPendingId !== nextPendingId ? "approval-updated" : "updated")
  await scrollToBottom()
}

const sendMessage = async (overrideMessage) => {
  if (generating.value || pendingStoppedRound.value) return
  const question = String(overrideMessage ?? input.value).trim()
  if (!question) return

  input.value = ""
  errorMessage.value = ""
  lastFailedQuestion.value = ""
  generating.value = true
  toolStatus.value = ""
  const userId = `temp-user-${++temporaryId}`
  const assistantId = `temp-assistant-${temporaryId}`
  const afterId = messages.value.reduce((max, item) => {
    const id = Number(item.id)
    return Number.isFinite(id) ? Math.max(max, id) : max
  }, 0)
  activeRound.value = { question, assistantId, afterId }
  messages.value.push({ id: userId, role: "USER", content: question, partial: false })
  messages.value.push({ id: assistantId, role: "ASSISTANT", content: "", partial: false })
  await scrollToBottom()

  const controller = new AbortController()
  abortController.value = controller
  let streamFailed = false

  try {
    await streamAiChat(question, {
      signal: controller.signal,
      onEvent(type, data) {
        // 先更新助手辅助状态，再按事件类型处理正文或错误展示。
        applyAssistantEvent(type, data)
        const assistant = messages.value.find((item) => item.id === assistantId)
        if (type === "delta" && assistant) {
          assistant.content = appendDelta(assistant.content, data)
          scrollToBottom()
        } else if (type === "error") {
          streamFailed = true
          errorMessage.value = data.message || "AI 服务暂时不可用"
          lastFailedQuestion.value = question
          if (assistant) assistant.partial = Boolean(assistant.content)
        }
      },
    })

    if (streamFailed) {
      generating.value = false
      abortController.value = null
      activeRound.value = null
      return
    }
    await finishStream()
  } catch (error) {
    if (controller.signal.aborted) return
    streamFailed = true
    generating.value = false
    toolStatus.value = ""
    abortController.value = null
    activeRound.value = null
    errorMessage.value = "连接中断，请稍后重试"
    lastFailedQuestion.value = question
    const assistant = messages.value.find((item) => item.id === assistantId)
    if (assistant) assistant.partial = Boolean(assistant.content)
  }
}

const syncStoppedRound = async (round, attempt = 0) => {
  if (pendingStoppedRound.value !== round) return
  try {
    const response = await getAiChatMessages({ limit: 50 })
    const history = historyPayload(response)
    if (hasPersistedRound(history.messages, round.question, round.answer, round.afterId)) {
      applyHistory(history)
      pendingStoppedRound.value = null
      errorMessage.value = "已停止生成"
      broadcast("updated")
      await scrollToBottom()
      return
    }
  } catch (_error) {
    // 保存确认失败时保留乐观消息，下一次轮询或窗口聚焦继续确认。
  }

  if (attempt >= STOP_SYNC_MAX_ATTEMPTS) {
    pendingStoppedRound.value = null
    errorMessage.value = "已停止生成，聊天历史稍后同步"
    return
  }
  clearTimeout(refreshTimer)
  refreshTimer = window.setTimeout(
    () => syncStoppedRound(round, attempt + 1),
    Math.min(250 * (attempt + 1), 1000)
  )
}

const stopGenerating = () => {
  if (!abortController.value || !activeRound.value) return
  const round = activeRound.value
  abortController.value.abort()
  abortController.value = null
  activeRound.value = null
  generating.value = false
  toolStatus.value = ""
  errorMessage.value = "已停止生成，正在保存部分回答"
  const assistant = messages.value.find((item) => item.id === round.assistantId)
  if (assistant) assistant.partial = Boolean(assistant.content)
  pendingStoppedRound.value = {
    question: round.question,
    answer: assistant?.content || "",
    afterId: round.afterId,
  }
  clearTimeout(refreshTimer)
  syncStoppedRound(pendingStoppedRound.value)
}

const retryLastMessage = () => {
  const question = lastFailedQuestion.value
  if (!question) return
  messages.value = messages.value.filter((item) => !String(item.id).startsWith("temp-"))
  sendMessage(question)
}

const clearMessages = async () => {
  try {
    await ElMessageBox.confirm("清空后无法恢复，确认清空全部聊天记录吗？", "清空聊天记录", {
      confirmButtonText: "清空",
      cancelButtonText: "取消",
      type: "warning",
    })
    await clearAiChatMemory()
    messages.value = []
    nextBeforeId.value = null
    hasMore.value = false
    errorMessage.value = ""
    lastFailedQuestion.value = ""
    pendingStoppedRound.value = null
    pendingAction.value = null
    toolStatus.value = ""
    clearTimeout(refreshTimer)
    broadcast("cleared")
    ElMessage.success("聊天记录已清空")
  } catch (error) {
    if (error !== "cancel" && error !== "close") throw error
  }
}

const persistRect = () => localStorage.setItem(RECT_STORAGE_KEY, JSON.stringify(rect.value))

const handlePointerMove = (event) => {
  if (!interaction) return
  const deltaX = event.clientX - interaction.startX
  const deltaY = event.clientY - interaction.startY
  const next = interaction.type === "drag"
    ? { ...interaction.rect, x: interaction.rect.x + deltaX, y: interaction.rect.y + deltaY }
    : { ...interaction.rect, width: interaction.rect.width + deltaX, height: interaction.rect.height + deltaY }
  rect.value = clampWindowRect(next, viewport())
}

const stopInteraction = () => {
  if (!interaction) return
  interaction = null
  persistRect()
  window.removeEventListener("pointermove", handlePointerMove)
  window.removeEventListener("pointerup", stopInteraction)
}

const beginInteraction = (type, event) => {
  if (event.button !== 0) return
  event.preventDefault()
  interaction = { type, startX: event.clientX, startY: event.clientY, rect: { ...rect.value } }
  window.addEventListener("pointermove", handlePointerMove)
  window.addEventListener("pointerup", stopInteraction)
}

const startDrag = (event) => beginInteraction("drag", event)
const startResize = (event) => beginInteraction("resize", event)

const handleResize = () => {
  rect.value = clampWindowRect(rect.value, viewport())
  persistRect()
}

const openChat = async () => {
  rect.value = dockBottomRightRect(rect.value, viewport())
  persistRect()
  open.value = true
  minimized.value = false
  localStorage.setItem(OPEN_STORAGE_KEY, "true")
  if (!messages.value.length) await loadLatest().catch(() => {})
}

const closeChat = () => {
  open.value = false
  minimized.value = false
  localStorage.setItem(OPEN_STORAGE_KEY, "false")
}

const handleFocus = () => {
  if (!open.value || generating.value) return
  if (pendingStoppedRound.value) {
    syncStoppedRound(pendingStoppedRound.value)
  } else if (
    shouldRefreshHistory(
      lastHistoryRefreshAt,
      Date.now(),
      HISTORY_FOCUS_REFRESH_INTERVAL
    )
  ) {
    refreshLatestQuietly().catch(() => {})
  }
}

onMounted(() => {
  window.addEventListener("resize", handleResize)
  window.addEventListener("focus", handleFocus)
  if ("BroadcastChannel" in window) {
    broadcastChannel = new BroadcastChannel(CHANNEL_NAME)
    broadcastChannel.addEventListener("message", (event) => {
      if (event.data?.type === "cleared") {
        pendingStoppedRound.value = null
        clearTimeout(refreshTimer)
        messages.value = []
        nextBeforeId.value = null
        hasMore.value = false
        pendingAction.value = null
        toolStatus.value = ""
      } else if (event.data?.type === "approval-updated") {
        // 另一标签页可能已确认、取消或替换操作，先移除旧预览再静默拉取权威状态。
        pendingAction.value = null
        if (!generating.value && !pendingStoppedRound.value) {
          refreshLatestQuietly().catch(() => {})
        }
      } else if (event.data?.type === "updated" && !generating.value && !pendingStoppedRound.value) {
        refreshLatestQuietly().catch(() => {})
      }
    })
  }
  if (open.value) loadLatest().catch(() => {})
})

onBeforeUnmount(() => {
  abortController.value?.abort()
  clearTimeout(refreshTimer)
  stopInteraction()
  window.removeEventListener("resize", handleResize)
  window.removeEventListener("focus", handleFocus)
  broadcastChannel?.close()
})
</script>

<style lang="scss" scoped>
.ai-launcher,
.ai-chat-window {
  position: fixed;
  z-index: $base-z-index + 2;
}

.ai-launcher {
  right: 16px;
  bottom: 16px;
  display: grid;
  width: 52px;
  height: 52px;
  padding: 0;
  color: #fff;
  cursor: pointer;
  background: var(--el-color-primary);
  border: 0;
  border-radius: 50%;
  box-shadow: 0 8px 24px rgb(0 0 0 / 18%);
  place-items: center;
  transition: transform 180ms ease, opacity 180ms ease;

  .el-icon {
    font-size: 24px;
  }

  &:hover {
    transform: translateY(-2px);
  }
}

.ai-chat-window {
  display: flex;
  box-sizing: border-box;
  overflow: hidden;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 14px 40px rgb(0 0 0 / 20%);
  flex-direction: column;
  transition: opacity 180ms ease;

  &.minimized {
    min-height: 56px;
  }
}

.chat-header {
  display: flex;
  min-height: 56px;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px 0 16px;
  cursor: move;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  user-select: none;
}

.chat-title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;

  .status-dot {
    width: 9px;
    height: 9px;
    background: var(--el-color-success);
    border-radius: 50%;
    box-shadow: 0 0 0 3px var(--el-color-success-light-8);
    flex: 0 0 auto;
  }

  div {
    display: flex;
    min-width: 0;
    flex-direction: column;
  }

  strong {
    overflow: hidden;
    font-size: 14px;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span:last-child {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 18px;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.icon-button {
  display: grid;
  width: 40px;
  height: 44px;
  padding: 0;
  color: var(--el-text-color-regular);
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 4px;
  place-items: center;

  &:hover:not(:disabled) {
    color: var(--el-color-primary);
    background: var(--el-fill-color-light);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.45;
  }
}

.message-list {
  flex: 1;
  min-height: 0;
  padding: 16px;
  overflow: auto;
  background: var(--el-fill-color-extra-light);
}

.load-more {
  display: block;
  min-height: 36px;
  margin: 0 auto 12px;
  padding: 0 12px;
  color: var(--el-color-primary);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.empty-state {
  display: grid;
  min-height: 100%;
  color: var(--el-text-color-secondary);
  place-content: center;
  justify-items: center;
  gap: 10px;

  .el-icon {
    font-size: 36px;
  }

  strong {
    font-size: 14px;
    font-weight: 500;
  }
}

.message-row {
  display: flex;
  margin-bottom: 14px;

  &.user {
    justify-content: flex-end;

    .message-bubble {
      color: #fff;
      background: var(--el-color-primary);
    }
  }

  &.assistant {
    justify-content: flex-start;

    .message-bubble {
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-lighter);
    }
  }
}

.message-bubble {
  max-width: 84%;
  padding: 10px 12px;
  overflow-wrap: anywhere;
  border-radius: 8px;

  p {
    margin: 0;
    line-height: 1.65;
    white-space: pre-wrap;
  }
}

.partial-label {
  display: block;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.markdown-body {
  line-height: 1.65;

  :deep(p:first-child) {
    margin-top: 0;
  }

  :deep(p:last-child) {
    margin-bottom: 0;
  }

  :deep(pre) {
    max-width: 100%;
    padding: 10px;
    overflow: auto;
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }

  :deep(code) {
    font-family: Consolas, "Courier New", monospace;
  }

  :deep(a) {
    color: var(--el-color-primary);
  }
}

.stream-error {
  display: flex;
  min-height: 44px;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  color: var(--el-color-danger);
  font-size: 13px;
  background: var(--el-color-danger-light-9);
  border-radius: 4px;
}

.tool-status {
  display: flex;
  min-height: 36px;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  padding: 0 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  background: var(--el-bg-color);
  border-left: 3px solid var(--el-color-primary);

  .el-icon {
    color: var(--el-color-primary);
    flex: 0 0 auto;
  }
}

.pending-action {
  margin: 0 0 14px;
  padding: 12px;
  overflow: hidden;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-left: 3px solid var(--el-color-warning);
  border-radius: 6px;

  > header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    > .el-icon {
      color: var(--el-color-warning);
      font-size: 20px;
      flex: 0 0 auto;
    }

    > div {
      display: flex;
      min-width: 0;
      flex-direction: column;
    }

    strong {
      font-size: 14px;
      line-height: 20px;
    }

    span {
      color: var(--el-text-color-secondary);
      font-size: 12px;
      line-height: 18px;
    }
  }

  dl {
    display: grid;
    margin: 10px 0;
    gap: 6px;
  }

  dl > div {
    display: grid;
    min-width: 0;
    grid-template-columns: 44px minmax(0, 1fr);
    gap: 8px;
  }

  dt,
  dd {
    margin: 0;
    font-size: 13px;
    line-height: 20px;
  }

  dt {
    color: var(--el-text-color-secondary);
  }

  dd {
    overflow-wrap: anywhere;
  }
}

.pending-content {
  max-height: 220px;
  padding-top: 10px;
  overflow: auto;
  border-top: 1px solid var(--el-border-color-lighter);
  overflow-wrap: anywhere;
}

.composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 12px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
}

.send-button {
  display: grid;
  width: 44px;
  height: 44px;
  padding: 0;
  color: #fff;
  cursor: pointer;
  background: var(--el-color-primary);
  border: 0;
  border-radius: 6px;
  place-items: center;
  flex: 0 0 auto;

  &.stopping {
    background: var(--el-color-danger);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.45;
  }
}

.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 18px;
  height: 18px;
  cursor: nwse-resize;

  &::after {
    position: absolute;
    right: 4px;
    bottom: 4px;
    width: 7px;
    height: 7px;
    content: "";
    border-right: 2px solid var(--el-border-color-darker);
    border-bottom: 2px solid var(--el-border-color-darker);
  }
}

button:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .ai-launcher,
  .ai-chat-window {
    transition: none;
  }
}
</style>
