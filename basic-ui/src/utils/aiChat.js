import MarkdownIt from "markdown-it"

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
})

const defaultLinkOpen =
  markdown.renderer.rules.link_open ||
  ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))

// 外部链接与管理系统主页面隔离，避免新页面访问原窗口对象。
markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  const href = tokens[index].attrGet("href") || ""
  if (/^https?:\/\//i.test(href)) {
    tokens[index].attrSet("target", "_blank")
    tokens[index].attrSet("rel", "noopener noreferrer")
  }
  return defaultLinkOpen(tokens, index, options, env, self)
}

export const shouldRenderAiChat = (device) => device === "desktop"

export const appendDelta = (content = "", event = {}) =>
  `${content}${event.content || ""}`

/**
 * 窗口聚焦可能在短时间内连续触发，只在刷新间隔到期后重新读取历史。
 */
export const shouldRefreshHistory = (
  lastRefreshAt,
  now = Date.now(),
  interval = 5_000
) => !Number.isFinite(lastRefreshAt) || now - lastRefreshAt >= interval

/**
 * 多个自动刷新信号同时到达时复用在途请求，避免对同一历史页并发查询。
 */
export const createRequestCoalescer = () => {
  let pending = null
  return (requestFactory) => {
    if (pending) return pending
    pending = Promise.resolve()
      .then(requestFactory)
      .finally(() => {
        pending = null
      })
    return pending
  }
}

/**
 * 停止生成后只接受本轮开始之后的新记录，避免同文旧消息造成错误确认。
 */
export const hasPersistedRound = (messages = [], question, answer, afterId = 0) => {
  for (let index = 0; index < messages.length; index += 1) {
    const user = messages[index]
    if (
      user?.role !== "USER" ||
      user?.content !== question ||
      !Number.isFinite(Number(user?.id)) ||
      Number(user.id) <= Number(afterId || 0)
    ) {
      continue
    }
    if (!answer) return true
    const assistant = messages[index + 1]
    if (
      assistant?.role === "ASSISTANT" &&
      assistant?.content === answer &&
      assistant?.partial === true &&
      Number(assistant.id) > Number(user.id)
    ) {
      return true
    }
  }
  return false
}

/**
 * 合并游标历史并按消息 ID 升序排列，同一条持久化消息只展示一次。
 */
export const mergeHistory = (current = [], older = []) => {
  const merged = new Map()
  ;[...older, ...current].forEach((item) => {
    if (item?.id !== undefined && item?.id !== null) merged.set(String(item.id), item)
  })
  return [...merged.values()].sort((left, right) => Number(left.id) - Number(right.id))
}

/**
 * 使用统一边距计算默认位置，避免首次打开时依赖尚未写入的本地状态。
 */
export const defaultBottomRightRect = (viewport, margin = 16) =>
  clampWindowRect(
    {
      x: viewport.width - 420 - margin,
      y: viewport.height - 600 - margin,
      width: 420,
      height: 600,
    },
    viewport,
    margin
  )

/**
 * 读取浮窗位置时严格校验数值；旧版本或损坏数据不得导致窗口移出屏幕。
 */
export const loadStoredRect = (serialized, fallback, viewport) => {
  try {
    const parsed = JSON.parse(serialized)
    const values = [parsed?.x, parsed?.y, parsed?.width, parsed?.height]
    if (!values.every(Number.isFinite)) return fallback
    return clampWindowRect(parsed, viewport)
  } catch (_error) {
    return fallback
  }
}

/**
 * 将浮窗尺寸和位置限制在当前视口内，拖动、缩放和窗口变化均复用该边界。
 */
export const clampWindowRect = (rect, viewport, margin = 16) => {
  const width = Math.min(
    Math.max(rect.width, 360),
    viewport.width - margin * 2
  )
  const height = Math.min(
    Math.max(rect.height, 480),
    viewport.height - margin * 2
  )
  return {
    x: Math.min(Math.max(rect.x, margin), viewport.width - width - margin),
    y: Math.min(Math.max(rect.y, margin), viewport.height - height - margin),
    width,
    height,
  }
}

/**
 * 渲染模型 Markdown。原始 HTML 始终作为文本处理，防止聊天内容注入脚本。
 */
export const renderMarkdown = (content = "") => markdown.render(String(content))
