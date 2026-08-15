import { elementPlusAdapter } from "@/utils/aiAgent/elementPlusAdapter"

function requireDefinition(definition) {
  if (!definition || typeof definition.routeName !== "string" || !definition.routeName.trim()) {
    throw new Error("AI 页面定义必须提供 routeName")
  }
  if (typeof definition.describe !== "function") {
    throw new Error(`AI 页面 ${definition.routeName} 必须提供 describe`)
  }
}

function actionEntries(actions) {
  if (actions instanceof Map) return [...actions.entries()]
  return Object.entries(actions || {})
}

function findAction(definition, target) {
  if (definition.actions instanceof Map) return definition.actions.get(target)
  return definition.actions?.[target]
}

function capabilitiesOf(definition) {
  // 只把存在本地执行器的动作暴露给服务端；单独声明 capability 不能凭空获得执行能力。
  const declaredCapabilities = new Map(
    (definition.capabilities || []).map((capability) => [capability.target, capability])
  )
  return actionEntries(definition.actions).map(([target, action]) => {
    const declared = declaredCapabilities.get(target) || {}
    return {
      actionType: action.actionType || declared.actionType,
      target,
      parameterSchema: action.parameterSchema || declared.parameterSchema || {},
    }
  })
}

/** 创建相互隔离的页面能力注册表，便于运行时与契约检查独立使用。 */
export function createPageRegistry({ adapter = elementPlusAdapter } = {}) {
  const definitions = new Map()

  function register(definition) {
    requireDefinition(definition)
    const routeName = definition.routeName.trim()
    const registered = { ...definition, routeName }
    definitions.set(routeName, registered)

    // 只注销本次注册，避免旧组件卸载时删掉同路由的新实例。
    return () => {
      if (definitions.get(routeName) === registered) {
        definitions.delete(routeName)
      }
    }
  }

  function current(routeName) {
    if (routeName) return definitions.get(routeName) || null
    if (definitions.size === 1) return definitions.values().next().value
    return null
  }

  async function snapshot(routeName) {
    const definition = current(routeName)
    if (!definition) {
      return adapter.snapshot(routeName)
    }
    const description = (await definition.describe()) || {}
    const state = Object.prototype.hasOwnProperty.call(description, "state")
      ? description.state
      : description

    // 快照同时携带页面版本和当前可执行能力，服务端会再与权限策略取交集后才交给模型。
    return {
      routeName: definition.routeName,
      pageVersion:
        description.pageVersion || definition.pageVersion || "1",
      state: state || {},
      capabilities: capabilitiesOf(definition),
    }
  }

  async function execute(routeName, action) {
    const definition = current(routeName)
    if (!definition) {
      return adapter.execute(routeName, action)
    }
    const handler = findAction(definition, action?.target)
    // 仅按语义 target 查找已注册处理器，不接受模型提供选择器或任意脚本。
    if (!handler || (typeof handler !== "function" && typeof handler.execute !== "function")) {
      throw new Error(
        `页面 ${definition.routeName} 不支持动作目标 ${action?.target || "未知"}`
      )
    }
    if (typeof handler === "function") return handler(action)
    return handler.execute(action.arguments || {}, action)
  }

  return Object.freeze({ register, current, snapshot, execute })
}

export const pageRegistry = createPageRegistry()
