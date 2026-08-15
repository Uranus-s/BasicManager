import { pulseAgentTarget } from "@/utils/aiAgent/visualEffects"

const semanticTargetPattern = /^[A-Za-z0-9][A-Za-z0-9._:-]*$/

function requireSemanticTarget(target) {
  if (typeof target !== "string" || !semanticTargetPattern.test(target)) {
    throw new Error("AI 页面动作只接受 data-ai-target 语义目标")
  }
  return target
}

function targetElements(target, root = document) {
  // CSS.escape 只负责安全构造选择器；前置格式校验进一步限制模型只能使用约定的语义目标。
  const escaped = CSS.escape(requireSemanticTarget(target))
  return root.querySelectorAll(`[data-ai-target="${escaped}"]`)
}

function requireTarget(target, root) {
  const elements = targetElements(target, root)
  if (elements.length === 0) {
    throw new Error(`未找到 AI 页面目标 ${target}`)
  }
  if (elements.length > 1) {
    // 多个匹配项意味着页面语义标记存在歧义，宁可停止也不猜测用户想操作哪个元素。
    throw new Error(`AI 页面目标 ${target} 不唯一`)
  }
  return elements[0]
}

/** 短暂标出即将操作的元素，帮助用户辨认代理当前动作。 */
export function highlightTarget(element) {
  element.scrollIntoView?.({ block: "center", inline: "nearest" })
  pulseAgentTarget(element)
  return element
}

export function clickTarget(target, { root = document } = {}) {
  const element = highlightTarget(requireTarget(target, root))
  element.click()
  return element
}

function editableControl(element) {
  if (element.matches?.("input, textarea")) return element
  return element.querySelector?.("input, textarea") || null
}

function setNativeValue(element, value) {
  // 调用原生 setter 并在外层派发 input/change，确保 Vue 等受控表单能接收到代理输入。
  const descriptor = Object.getOwnPropertyDescriptor(
    Object.getPrototypeOf(element),
    "value"
  )
  if (!descriptor?.set) {
    throw new Error("目标输入控件不支持原生 value setter")
  }
  descriptor.set.call(element, value)
}

export function fillTarget(target, value, { root = document } = {}) {
  const container = highlightTarget(requireTarget(target, root))
  const input = editableControl(container)
  if (!input) {
    throw new Error(`AI 页面目标 ${target} 内不存在 input 或 textarea`)
  }
  input.focus()
  setNativeValue(input, value == null ? "" : String(value))
  input.dispatchEvent(new Event("input", { bubbles: true }))
  input.dispatchEvent(new Event("change", { bubbles: true }))
  return input
}

export function clearTarget(target, options) {
  return fillTarget(target, "", options)
}

/** 等待页面显式条件成立，不开放自由 DOM 查询能力。 */
export function waitFor(
  condition,
  { timeout = 5000, interval = 50, errorMessage = "等待页面状态超时" } = {}
) {
  if (typeof condition !== "function") {
    return Promise.reject(new Error("waitFor 必须接收状态检查函数"))
  }
  const startedAt = Date.now()

  return new Promise((resolve, reject) => {
    const check = async () => {
      try {
        const value = await condition()
        if (value) {
          resolve(value)
          return
        }
        if (Date.now() - startedAt >= timeout) {
          reject(new Error(errorMessage))
          return
        }
        setTimeout(check, interval)
      } catch (error) {
        reject(error)
      }
    }
    check()
  })
}

export async function submitAndWait(targetOrSubmit, condition, options = {}) {
  // 提交后等待页面显式业务条件，而不是依赖固定延时，降低接口和渲染速度波动造成的误判。
  if (typeof targetOrSubmit === "function") {
    await targetOrSubmit()
  } else {
    clickTarget(targetOrSubmit, options)
  }
  return waitFor(condition, options)
}

/** 要求业务数据中只有一条匹配记录，避免误操作相似行。 */
export function requireUniqueRow(rows, matcher, label = "目标记录") {
  if (!Array.isArray(rows)) {
    throw new Error("页面记录必须是数组")
  }
  const predicate =
    typeof matcher === "function"
      ? matcher
      : matcher && typeof matcher === "object"
        ? (row) =>
            Object.entries(matcher).every(([key, value]) => row?.[key] === value)
        : (row) => String(row?.id) === String(matcher)
  // 执行写操作前必须把业务条件收敛到唯一记录，零条或多条都视为不安全。
  const matches = rows.filter(predicate)
  if (matches.length === 0) throw new Error(`未找到${label}`)
  if (matches.length > 1) throw new Error(`${label}不唯一，已停止操作`)
  return matches[0]
}
