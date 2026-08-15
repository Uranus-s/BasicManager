import { pulseAgentTarget } from "@/utils/aiAgent/visualEffects"

const interactiveSelector = [
  "button",
  "input",
  "textarea",
  ".el-select",
  ".el-radio",
  ".el-checkbox",
  ".el-switch",
  ".el-segmented__item",
  ".el-menu-item",
  ".el-sub-menu__title",
  ".el-pager li",
  ".el-pagination button",
  ".el-tree-node__content",
  "[role='button']",
].join(",")

const sensitivePattern = /(password|passwd|api\s*key|token|secret|密码|密钥|令牌)/i
const highRiskPattern = /(保存|提交|确定|确认|删除|发布|撤回|重置密码|分配|移除|清空|停用|启用|解锁|上传|导入)/
const lowRiskPattern = /(查询|搜索|刷新|重置|新增|编辑|查看|详情|打开|返回|取消|关闭|展开|收起|上一页|下一页|首页|末页|更多|选择|切换|进入)/

function cleanText(value, maxLength = 240) {
  return String(value || "").replace(/\s+/g, " ").trim().slice(0, maxLength)
}

function matches(element, selector) {
  try {
    return Boolean(element?.matches?.(selector))
  } catch (_error) {
    return false
  }
}

function closest(element, selector) {
  try {
    return element?.closest?.(selector) || null
  } catch (_error) {
    return null
  }
}

function attribute(element, name) {
  return cleanText(element?.getAttribute?.(name))
}

function formLabel(element) {
  return cleanText(
    closest(element, ".el-form-item")?.querySelector?.(".el-form-item__label")
      ?.textContent
  )
}

function elementLabel(element) {
  return (
    attribute(element, "aria-label") ||
    attribute(element, "title") ||
    formLabel(element) ||
    attribute(element, "placeholder") ||
    cleanText(element?.textContent) ||
    cleanText(element?.value)
  )
}

function elementContext(element) {
  const row = closest(element, ".el-table__row, tr")
  if (row) return cleanText(row.innerText || row.textContent, 500)
  const dialog = closest(element, ".el-dialog, .el-drawer")
  if (dialog) {
    const title = dialog.querySelector?.(
      ".el-dialog__title, .el-drawer__title"
    )
    return cleanText(title?.textContent)
  }
  return ""
}

function isVisible(element, document) {
  if (!element || element.hidden || element.disabled) return false
  if (attribute(element, "aria-hidden") === "true") return false
  if (typeof element.getClientRects === "function" && element.getClientRects().length === 0) {
    return false
  }
  const style = document?.defaultView?.getComputedStyle?.(element)
  return !style || (style.display !== "none" && style.visibility !== "hidden")
}

function isAgentControl(element) {
  return Boolean(
    closest(element, ".agent-panel, .agent-observer, .ai-chat-float, [data-ai-agent-root]")
  )
}

function isSensitive(element, label) {
  const type = (attribute(element, "type") || element?.type || "").toLowerCase()
  if (["password", "file", "hidden"].includes(type)) return true
  return sensitivePattern.test([
    label,
    attribute(element, "name"),
    attribute(element, "id"),
    attribute(element, "autocomplete"),
  ].join(" "))
}

function elementKind(element) {
  if (matches(element, ".el-select")) return "select"
  if (matches(element, "input, textarea")) {
    if (closest(element, ".el-select")) return null
    const type = (attribute(element, "type") || element?.type || "").toLowerCase()
    if (["checkbox", "radio"].includes(type)) {
      // Element Plus 包装器本身已经是可点击语义目标，忽略内部值通常为 on 的原生 input。
      if (closest(element, ".el-checkbox, .el-radio, .el-switch, .el-segmented")) {
        return null
      }
      return "click"
    }
    if (["button", "submit"].includes(type)) return "click"
    return "fill"
  }
  return "click"
}

function clickRisk(element, label) {
  if (
    matches(element, ".el-menu-item, .el-sub-menu__title, .el-pager li, .el-pagination button, .el-radio, .el-checkbox, .el-switch, .el-segmented__item, .el-tree-node__content") ||
    closest(element, ".el-menu, .el-pagination")
  ) {
    return "low"
  }
  if (highRiskPattern.test(label)) return "high"
  if (lowRiskPattern.test(label)) return "low"
  // 无法确认用途的按钮保守地进入用户确认流程。
  return "high"
}

function parameterSchema(kind, label) {
  if (kind === "click") {
    return { type: "object", additionalProperties: false }
  }
  return {
    type: "object",
    properties: {
      value: { type: "string", description: label },
    },
    required: ["value"],
    additionalProperties: false,
  }
}

function hashText(value) {
  let hash = 2166136261
  for (let index = 0; index < value.length; index += 1) {
    hash ^= value.charCodeAt(index)
    hash = Math.imul(hash, 16777619)
  }
  return (hash >>> 0).toString(36)
}

function setNativeValue(element, value) {
  let prototype = Object.getPrototypeOf(element)
  while (prototype) {
    const descriptor = Object.getOwnPropertyDescriptor(prototype, "value")
    if (descriptor?.set) {
      descriptor.set.call(element, value)
      return
    }
    prototype = Object.getPrototypeOf(prototype)
  }
  element.value = value
}

function dispatchValueEvents(element) {
  const EventType = element.ownerDocument?.defaultView?.Event || globalThis.Event
  element.dispatchEvent(new EventType("input", { bubbles: true }))
  element.dispatchEvent(new EventType("change", { bubbles: true }))
}

function highlight(element) {
  element.scrollIntoView?.({ block: "center", inline: "nearest" })
  pulseAgentTarget(element)
}

function currentValue(element, kind) {
  if (kind === "fill") return cleanText(element.value)
  if (kind === "select") {
    return cleanText(
      element.querySelector?.(".el-select__selected-item, .el-select__placeholder")
        ?.textContent
    )
  }
  return ""
}

function collectTables(document) {
  return [...document.querySelectorAll(".el-table")].map((table) => {
    const allColumns = [...table.querySelectorAll(".el-table__header th")]
      .map((cell, index) => ({ index, label: cleanText(cell.textContent) }))
    // 操作列已经通过独立按钮暴露；敏感列不得作为页面状态交给模型。
    const columns = allColumns.filter(
      ({ label }) => label && label !== "操作" && !sensitivePattern.test(label)
    )
    const rows = [...table.querySelectorAll(".el-table__body .el-table__row")]
      .slice(0, 50)
      .map((row) => {
        const cells = [...row.querySelectorAll("td")]
        return columns.map(({ index }) => cleanText(cells[index]?.textContent, 300))
      })
    return {
      columns: columns.map(({ label }) => label),
      rows,
    }
  }).filter((table) => table.columns.length || table.rows.length)
}

/**
 * 为当前项目的 Element Plus 页面创建通用扫描与执行适配器。
 * 临时 target 只存在于当前标签页内，模型不能借此提交选择器或脚本。
 */
export function createElementPlusAdapter({ document = globalThis.document } = {}) {
  const elementIds = new WeakMap()
  let nextElementId = 1
  let activeRouteName = null
  let activeTargets = new Map()

  function idOf(element) {
    if (!elementIds.has(element)) {
      elementIds.set(element, String(nextElementId))
      nextElementId += 1
    }
    return elementIds.get(element)
  }

  function scan(routeName) {
    if (!document?.querySelectorAll) {
      throw new Error("当前环境无法扫描页面控件")
    }
    const targets = new Map()
    const elements = []

    for (const element of document.querySelectorAll(interactiveSelector)) {
      const label = elementLabel(element)
      const kind = elementKind(element)
      if (
        !kind ||
        !label ||
        !isVisible(element, document) ||
        isAgentControl(element) ||
        isSensitive(element, label)
      ) {
        continue
      }
      const risk = kind === "click" ? clickRisk(element, label) : "low"
      const actionType = `ui.${kind}`
      const target = `auto.${kind}.${risk}.${idOf(element)}`
      const descriptor = {
        element,
        actionType,
        target,
        label,
        kind,
        risk,
        context: elementContext(element),
      }
      targets.set(target, descriptor)
      elements.push({
        target,
        role: kind,
        label,
        risk: risk.toUpperCase(),
        context: descriptor.context,
        value: currentValue(element, kind),
      })
    }

    activeRouteName = routeName
    activeTargets = targets
    const state = {
      title: cleanText(document.title),
      elements,
      tables: collectTables(document),
    }
    const pageVersion = `auto-${hashText(JSON.stringify({ routeName, state }))}`
    return {
      routeName,
      pageVersion,
      state,
      capabilities: elements.map(({ target, role, label }) => ({
        actionType: `ui.${role}`,
        target,
        parameterSchema: parameterSchema(role, label),
      })),
    }
  }

  async function snapshot(routeName) {
    const result = scan(routeName)
    if (!result.capabilities.length) {
      throw new Error("当前页面没有可供 AI 操作的 Element Plus 控件")
    }
    return result
  }

  async function selectValue(descriptor, value) {
    highlight(descriptor.element)
    descriptor.element.click()
    await Promise.resolve()
    const expected = cleanText(value)
    const options = [...document.querySelectorAll(
      ".el-select-dropdown__item:not(.is-disabled)"
    )].filter((option) => isVisible(option, document))
    const matches = options.filter(
      (option) => cleanText(option.textContent) === expected
    )
    if (matches.length !== 1) {
      throw new Error(`下拉选项“${expected}”不存在或不唯一`)
    }
    matches[0].click()
    return { status: "SUCCEEDED", summary: `已选择${descriptor.label}` }
  }

  async function acceptPageConfirmation(descriptor) {
    if (descriptor.risk !== "high") return
    // 服务端已经确认该动作，只接管本次点击随后产生的 Element Plus MessageBox，
    // 避免业务页面的二次确认让通用代理停在半完成状态。
    await Promise.resolve()
    await new Promise((resolve) => setTimeout(resolve, 0))
    const buttons = [...document.querySelectorAll(
      ".el-message-box__btns .el-button--primary:not(.is-disabled)"
    )].filter((button) => isVisible(button, document))
    if (buttons.length === 1) {
      highlight(buttons[0])
      buttons[0].click()
    }
  }

  async function execute(routeName, action) {
    if (activeRouteName !== routeName) scan(routeName)
    const descriptor = activeTargets.get(action?.target)
    if (!descriptor || descriptor.actionType !== action?.actionType) {
      throw new Error("AI 动作目标不属于当前页面快照")
    }
    if (!isVisible(descriptor.element, document)) {
      throw new Error("AI 动作目标当前不可见或不可用")
    }
    if (descriptor.kind === "click") {
      highlight(descriptor.element)
      descriptor.element.click()
      await acceptPageConfirmation(descriptor)
      return {
        status: "SUCCEEDED",
        summary: `已点击${descriptor.label}`,
        // 只有菜单项通常会触发路由导航，运行时据此等待目标页面挂载后再采集结果快照。
        routeChangeExpected: matches(descriptor.element, ".el-menu-item"),
      }
    }
    if (descriptor.kind === "select") {
      return selectValue(descriptor, action.arguments?.value)
    }

    const input = matches(descriptor.element, "input, textarea")
      ? descriptor.element
      : descriptor.element.querySelector?.("input, textarea")
    if (!input) throw new Error("AI 填写目标中不存在输入控件")
    highlight(descriptor.element)
    input.focus?.()
    setNativeValue(input, String(action.arguments?.value ?? ""))
    dispatchValueEvents(input)
    return { status: "SUCCEEDED", summary: `已填写${descriptor.label}` }
  }

  return Object.freeze({ snapshot, execute })
}

export const elementPlusAdapter = createElementPlusAdapter()
