const assert = require("node:assert/strict")
const fs = require("node:fs")
const path = require("node:path")
const vm = require("node:vm")
const babel = require("@babel/core")

const projectRoot = path.resolve(__dirname, "..")

function read(relativePath) {
  const fullPath = path.join(projectRoot, relativePath)
  assert.ok(fs.existsSync(fullPath), `${relativePath}: 契约文件不存在`)
  return fs.readFileSync(fullPath, "utf8")
}

function loadModule(relativePath, injected = {}, globals = {}) {
  const source = read(relativePath)
  const { code } = babel.transformSync(source, {
    filename: relativePath,
    presets: [
      ["@babel/preset-env", { targets: { node: "current" }, modules: "commonjs" }],
    ],
  })
  const module = { exports: {} }
  const localRequire = (request) => {
    if (Object.prototype.hasOwnProperty.call(injected, request)) {
      return injected[request]
    }
    return require(request)
  }

  vm.runInNewContext(
    code,
    {
      module,
      exports: module.exports,
      require: localRequire,
      console,
      setTimeout,
      clearTimeout,
      AbortController,
      ...globals,
    },
    { filename: relativePath }
  )
  return module.exports
}

function plain(value) {
  return JSON.parse(JSON.stringify(value))
}

function fakeElement({
  tagName = "DIV",
  text = "",
  type = "",
  value = "",
  attributes = {},
  formLabel = "",
  componentWrapper = "",
  selectors = [],
} = {}) {
  const events = []
  const element = {
    tagName,
    textContent: text,
    type,
    value,
    disabled: false,
    hidden: false,
    events,
    clicked: 0,
    getAttribute: (name) => attributes[name] ?? null,
    matches: (selector) => selector.split(",").some((part) => {
      const normalized = part.trim()
      if (normalized === "button" && tagName === "BUTTON") return true
      if (normalized === "input" && tagName === "INPUT") return true
      if (normalized === "textarea" && tagName === "TEXTAREA") return true
      if (selectors.includes(normalized)) return true
      return false
    }),
    closest: (selector) => {
      if (componentWrapper && selector.includes(componentWrapper)) {
        return { textContent: text }
      }
      if (selector === ".el-form-item" && formLabel) {
        return {
          querySelector: (childSelector) =>
            childSelector === ".el-form-item__label"
              ? { textContent: formLabel }
              : null,
        }
      }
      return null
    },
    querySelector: () => null,
    focus: () => {},
    click: () => {
      element.clicked += 1
    },
    dispatchEvent: (event) => events.push(event.type),
    scrollIntoView: () => {},
  }
  return element
}

function assertIncludes(relativePath, expected) {
  assert.ok(
    read(relativePath).includes(expected),
    `${relativePath}: 缺少 ${JSON.stringify(expected)}`
  )
}

async function checkProtocol() {
  const { TERMINAL_TASK_STATES, isTerminalTask, requiresConfirmation } = loadModule(
    "src/utils/aiAgent/protocol.js"
  )

  assert.ok(Array.isArray(TERMINAL_TASK_STATES), "应导出 TERMINAL_TASK_STATES")
  assert.deepEqual(plain(TERMINAL_TASK_STATES), [
    "SUCCEEDED",
    "FAILED",
    "CANCELED",
  ])

  for (const status of ["SUCCEEDED", "FAILED", "CANCELED"]) {
    assert.equal(isTerminalTask(status), true, `${status} 应是任务终态`)
  }
  assert.equal(isTerminalTask("PAUSED"), false, "PAUSED 不应是任务终态")
  assert.equal(
    requiresConfirmation({ riskLevel: "HIGH" }),
    true,
    "HIGH 风险动作必须确认"
  )
  assert.equal(
    requiresConfirmation({ riskLevel: "MEDIUM" }),
    false,
    "非 HIGH 风险动作不应要求确认"
  )
}

async function checkAgentControlRequestsAreNeverRetried() {
  const requests = []
  const request = async (config) => {
    requests.push(config)
    return { data: {} }
  }
  const api = loadModule(
    "src/api/ai/agent.js",
    {
      "@microsoft/fetch-event-source": { fetchEventSource: () => {} },
      "@/config": { baseURL: "" },
      "@/utils/accessToken": { getAccessToken: () => "token" },
      "@/utils/request": request,
    },
    { URLSearchParams }
  )

  await api.createAiAgentTask({ goal: "创建公告" })
  await api.reportAiAgentActionResult(1, "action-1", { status: "SUCCEEDED" })
  await api.pauseAiAgentTask(1, "tab-1")
  await api.resumeAiAgentTask(1, { clientInstanceId: "tab-1" })
  await api.cancelAiAgentTask(1, "tab-1")
  await api.claimAiAgentTask(1, "tab-2")
  await api.confirmAiAgentAction(1, "action-1", "tab-1")

  assert.equal(requests.length, 7)
  assert.deepEqual(
    plain(requests[0].silentErrorCodes || null),
    [40008],
    "创建任务的并发冲突应由运行时恢复，不弹出全局错误"
  )
  for (const config of requests) {
    assert.equal(
      config.retry,
      0,
      `${config.method.toUpperCase()} ${config.url} 禁止自动重试`
    )
  }
}

async function checkRequestAutomaticallyConsumesAgentTrace() {
  let requestInterceptor
  let responseInterceptor
  const axiosInstance = Object.assign((config) => config, {
    defaults: {},
    interceptors: {
      request: {
        use: (handler) => {
          requestInterceptor = handler
        },
      },
      response: {
        use: (handler) => {
          responseInterceptor = handler
        },
      },
    },
  })
  const traces = [{ taskId: 12, actionId: "action-9" }, null]
  let consumeCount = 0
  const messageErrors = []

  loadModule("src/utils/request.js", {
    axios: { create: () => axiosInstance },
    "@/config": {
      baseURL: "",
      contentType: "application/json",
      debounce: [],
      invalidCode: 500,
      loginInterception: false,
      noPermissionCode: 403,
      recordRoute: false,
      resultCode: {
        unauthorized: 401,
        tokenInvalid: 402,
        tokenExpired: 403,
        accountDisabled: 404,
        accountLocked: 405,
        forbidden: 406,
      },
      requestTimeout: 1000,
      successCode: 200,
      tokenName: "Authorization",
    },
    "@/store": {
      state: { user: { accessToken: "" } },
      dispatch: () => {},
    },
    qs: { stringify: (value) => value },
    "@/router": {
      currentRoute: { value: { path: "/" } },
      replace: () => Promise.resolve(),
      push: () => Promise.resolve(),
    },
    "@/utils/validate": { isArray: Array.isArray },
    "element-plus": {
      ElLoading: { service: () => ({ close: () => {} }) },
      ElMessage: { error: (message) => messageErrors.push(message) },
    },
    "lodash-es": {
      identity: (value) => value,
      pickBy: (value) => value,
    },
    "@/utils/aiAgent/traceContext": {
      consumeAgentTrace: () => traces[consumeCount++] || null,
    },
  })

  assert.equal(typeof requestInterceptor, "function", "应注册 Axios 请求拦截器")
  const first = requestInterceptor({
    url: "/system/user",
    method: "post",
    headers: { "Content-Type": "application/json" },
  })
  assert.equal(first.headers["X-AI-Agent-Task-Id"], "12")
  assert.equal(first.headers["X-AI-Agent-Action-Id"], "action-9")
  assert.equal(first.retry, 0, "代理触发的业务请求禁止自动重试")

  const second = requestInterceptor({
    url: "/system/user/list",
    method: "get",
    headers: { "Content-Type": "application/json" },
  })
  assert.equal(second.headers["X-AI-Agent-Task-Id"], undefined)
  assert.equal(second.headers["X-AI-Agent-Action-Id"], undefined)
  assert.equal(consumeCount, 2, "每笔普通请求只尝试消费一次当前代理追踪")

  await assert.rejects(
    responseInterceptor({
      data: { code: 40008, msg: "当前已有网页代理任务运行中" },
      config: {
        url: "/ai/agent/tasks",
        silentErrorCodes: [40008],
      },
    })
  )
  assert.deepEqual(messageErrors, [], "可恢复的任务冲突不得提前弹出全局错误")
}

async function checkPageRegistry() {
  const { createPageRegistry, pageRegistry } = loadModule(
    "src/utils/aiAgent/pageRegistry.js",
    {
      "@/utils/aiAgent/elementPlusAdapter": {
        elementPlusAdapter: {
          snapshot: async () => {
            throw new Error("测试必须显式注入适配器")
          },
          execute: async () => {
            throw new Error("测试必须显式注入适配器")
          },
        },
      },
    }
  )
  assert.equal(typeof pageRegistry.register, "function", "应导出单例注册表")
  const adapterCalls = []
  const autoRegistry = createPageRegistry({
    adapter: {
      snapshot: async (routeName) => {
        adapterCalls.push(["snapshot", routeName])
        return {
          routeName,
          pageVersion: "auto-v1",
          state: { title: "用户管理" },
          capabilities: [
            { actionType: "ui.click", target: "auto.click.low.1", parameterSchema: {} },
          ],
        }
      },
      execute: async (routeName, action) => {
        adapterCalls.push(["execute", routeName, action.target])
        return "已自动点击"
      },
    },
  })
  assert.equal((await autoRegistry.snapshot("AutoPage")).pageVersion, "auto-v1")
  assert.equal(
    await autoRegistry.execute("AutoPage", {
      actionType: "ui.click",
      target: "auto.click.low.1",
    }),
    "已自动点击"
  )
  assert.deepEqual(plain(adapterCalls), [
    ["snapshot", "AutoPage"],
    ["execute", "AutoPage", "auto.click.low.1"],
  ])

  const registry = createPageRegistry()
  let executed = 0
  const disposeUsers = registry.register({
    routeName: "SysUser",
    pageVersion: "sys-user-v1",
    describe: () => ({ keyword: "张" }),
    actions: {
      "user.search": {
        actionType: "FILL",
        parameterSchema: { type: "object" },
        execute: ({ value }) => {
          executed += 1
          return `已填写 ${value}`
        },
      },
    },
  })
  registry.register({
    routeName: "SysRole",
    pageVersion: "sys-role-v1",
    describe: () => ({ keyword: "管理员" }),
    actions: {
      "role.search": {
        actionType: "FILL",
        parameterSchema: {},
        execute: () => "已填写角色",
      },
    },
  })

  assert.equal(typeof disposeUsers, "function", "注册应返回注销函数")
  assert.equal(registry.current("SysUser").routeName, "SysUser")
  assert.deepEqual(plain((await registry.snapshot("SysUser")).state), {
    keyword: "张",
  })
  assert.deepEqual(
    plain((await registry.snapshot("SysUser")).capabilities),
    [
      {
        actionType: "FILL",
        target: "user.search",
        parameterSchema: { type: "object" },
      },
    ],
    "快照只公开注册的语义能力"
  )
  assert.equal(
    await registry.execute("SysUser", {
      target: "user.search",
      arguments: { value: "李" },
    }),
    "已填写 李"
  )
  assert.equal(executed, 1)
  let functionAction
  const functionHandler = Object.assign(
    (action) => {
      functionAction = action
      return "函数动作已执行"
    },
    { actionType: "CLICK", parameterSchema: {} }
  )
  registry.register({
    routeName: "FunctionPage",
    pageVersion: "function-v1",
    describe: () => ({}),
    actions: { "function.open": functionHandler },
  })
  const fullAction = {
    actionId: "function-action",
    target: "function.open",
    arguments: { id: 7 },
  }
  assert.equal(
    await registry.execute("FunctionPage", fullAction),
    "函数动作已执行"
  )
  assert.equal(functionAction, fullAction, "函数 handler 应接收完整 action")
  await assert.rejects(
    registry.execute("SysUser", {
      target: "role.search",
      arguments: {},
    }),
    /不支持|未注册/,
    "动作必须按当前 routeName 隔离"
  )
  await assert.rejects(registry.snapshot("MissingPage"), /显式注入适配器/)

  disposeUsers()
  await assert.rejects(registry.snapshot("SysUser"), /显式注入适配器/)
}

function checkTraceContext() {
  const { setAgentTrace, getAgentTrace, consumeAgentTrace, clearAgentTrace } = loadModule(
    "src/utils/aiAgent/traceContext.js"
  )

  assert.equal(getAgentTrace(), null)
  setAgentTrace({ taskId: 12, actionId: "action-3" })
  assert.deepEqual(plain(getAgentTrace()), {
    taskId: 12,
    actionId: "action-3",
  })
  assert.deepEqual(plain(consumeAgentTrace()), {
    taskId: 12,
    actionId: "action-3",
  })
  assert.equal(getAgentTrace(), null, "追踪上下文被业务处理器捕获后必须立即清空")
  setAgentTrace({ taskId: 12, actionId: "action-4" })
  clearAgentTrace()
  assert.equal(getAgentTrace(), null)
}

function walkAst(node, visit) {
  if (!node || typeof node !== "object") return
  visit(node)
  for (const value of Object.values(node)) {
    if (Array.isArray(value)) value.forEach((item) => walkAst(item, visit))
    else if (value && typeof value === "object") walkAst(value, visit)
  }
}

function isAllowedSelectorArgument(argument) {
  if (argument?.type === "StringLiteral") {
    return argument.value === "input, textarea"
  }
  if (argument?.type !== "TemplateLiteral" || argument.expressions.length !== 1) {
    return false
  }
  return (
    argument.quasis[0]?.value?.raw === '[data-ai-target="' &&
    argument.quasis[1]?.value?.raw === '"]'
  )
}

function checkSafetyBoundaries() {
  const safetyFiles = [
    "src/api/ai/agent.js",
    "src/utils/aiAgent/protocol.js",
    "src/utils/aiAgent/traceContext.js",
    "src/utils/aiAgent/runtime.js",
    "src/utils/aiAgent/pageRegistry.js",
    "src/utils/aiAgent/uiActions.js",
    "src/utils/aiAgent/visualEffects.js",
  ]
  const request = read("src/utils/request.js")
  const sources = Object.fromEntries(safetyFiles.map((file) => [file, read(file)]))
  const api = sources["src/api/ai/agent.js"]
  const runtime = sources["src/utils/aiAgent/runtime.js"]
  const uiActions = sources["src/utils/aiAgent/uiActions.js"]
  const combined = Object.values(sources).join("\n")

  assert.doesNotMatch(combined, /\beval\s*\(/, "运行时禁止 eval")
  assert.doesNotMatch(combined, /new\s+Function\s*\(/, "运行时禁止 new Function")
  for (const [file, source] of Object.entries(sources)) {
    const ast = babel.parseSync(source, { filename: file, sourceType: "module" })
    walkAst(ast, (node) => {
      if (
        !["CallExpression", "OptionalCallExpression"].includes(node.type) ||
        !["MemberExpression", "OptionalMemberExpression"].includes(
          node.callee?.type
        ) ||
        !["querySelector", "querySelectorAll"].includes(node.callee.property?.name)
      ) {
        return
      }
      assert.equal(
        isAllowedSelectorArgument(node.arguments[0]),
        true,
        `${file}: 禁止执行任意 DOM selector`
      )
    })
  }
  assert.match(uiActions, /data-ai-target/, "UI 动作只能定位 data-ai-target")
  assert.match(uiActions, /CSS\.escape/, "语义 target 必须经过 CSS.escape")
  assert.doesNotMatch(
    runtime,
    /retry|replay|重新执行失败动作|重放失败动作/i,
    "失败动作禁止重试或重放"
  )
  for (const name of [
    "clickTarget",
    "fillTarget",
    "waitFor",
    "submitAndWait",
    "requireUniqueRow",
  ]) {
    assert.match(uiActions, new RegExp(`export (?:async )?function ${name}\\b`))
  }
  assert.match(request, /X-AI-Agent-Task-Id/)
  assert.match(request, /X-AI-Agent-Action-Id/)
  assert.match(request, /config\.aiAgentTrace/)
  assert.match(request, /consumeAgentTrace\(\)/)
  assert.doesNotMatch(request, /getAgentTrace\s*\(/)
  assert.match(request, /config\.retry\s*=\s*0/)
  for (const fragment of [
    '"/ai/agent/status"',
    '"/ai/agent/tasks"',
    '/actions/${actionId}/result',
    '/actions/${actionId}/confirm',
    "openWhenHidden: true",
    "throw error",
  ]) {
    assert.ok(api.includes(fragment), `agent API 缺少 ${fragment}`)
  }
}

async function checkUiActionCompatibility() {
  const { clickTarget, submitAndWait, requireUniqueRow } = loadModule(
    "src/utils/aiAgent/uiActions.js",
    {
      "@/utils/aiAgent/visualEffects": { pulseAgentTarget: () => {} },
    },
    { CSS: { escape: (value) => value } }
  )
  assert.throws(
    () => clickTarget("#save-button", { root: {} }),
    /data-ai-target|语义目标/,
    "完整 selector target 必须被拒绝"
  )
  let submitted = 0
  await submitAndWait(
    () => {
      submitted += 1
    },
    () => true
  )
  assert.equal(submitted, 1, "callback 形态应执行提交函数")
  assert.deepEqual(
    plain(requireUniqueRow([{ id: 7 }, { id: 8 }], 7, "用户")),
    { id: 7 }
  )
}

async function checkFailedActionIsNotReplayed() {
  let executeCount = 0
  let reportCount = 0
  let traceSetCount = 0
  let traceClearCount = 0
  let pauseCount = 0
  let reportedPayload
  const snapshot = {
    routeName: "SysUser",
    pageVersion: "sys-user-v1",
    state: {},
    capabilities: [{ actionType: "CLICK", target: "user.save", parameterSchema: {} }],
  }
  const api = {
    reportAiAgentActionResult: async (_taskId, _actionId, payload) => {
      reportCount += 1
      reportedPayload = payload
      assert.equal(payload.status, "FAILED")
    },
    pauseAiAgentTask: async () => {
      pauseCount += 1
    },
  }
  const registry = {
    snapshot: async () => snapshot,
    execute: async () => {
      executeCount += 1
      throw new Error("保存失败")
    },
  }
  const trace = {
    setAgentTrace: () => {
      traceSetCount += 1
    },
    clearAgentTrace: () => {
      traceClearCount += 1
    },
  }
  const { createAiAgentRuntime, aiAgentRuntime } = loadModule(
    "src/utils/aiAgent/runtime.js",
    {
      vue: { reactive: (value) => value },
      "@/api/ai/agent": api,
      "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
      "@/utils/aiAgent/protocol": {
        isTerminalTask: (status) =>
          ["SUCCEEDED", "FAILED", "CANCELED"].includes(status?.status || status),
      },
      "@/utils/aiAgent/traceContext": trace,
      "@/router": { currentRoute: { value: { name: "SysUser" } } },
    }
  )
  assert.ok(aiAgentRuntime, "应导出响应式运行时单例")
  const runtime = createAiAgentRuntime({
    api,
    registry,
    trace,
    clientInstanceId: "client-test",
  })
  runtime.state.task = { id: 12, status: "EXECUTING" }
  const event = {
    type: "action",
    taskId: 12,
    status: "EXECUTING",
    action: {
      actionId: "action-failed",
      routeName: "SysUser",
      pageVersion: "sys-user-v1",
      target: "user.save",
      arguments: {},
    },
  }

  await runtime.handleEvent(event)
  await runtime.handleEvent(event)

  assert.equal(executeCount, 1, "同一失败动作不得再次执行")
  assert.equal(reportCount, 1, "同一失败动作不得再次上报")
  assert.equal(reportedPayload.executionRouteName, "SysUser")
  assert.equal(reportedPayload.executionPageVersion, "sys-user-v1")
  assert.equal(traceSetCount, 1)
  assert.equal(traceClearCount, 1)
  await runtime.takeover()
  assert.equal(pauseCount, 0, "服务端已暂停时接管不得重复请求暂停")
}

async function checkCrossPageActionUsesDestinationSnapshot() {
  const router = { currentRoute: { value: { name: "SystemUser" } } }
  let reportedPayload
  const registry = {
    snapshot: async (routeName) => ({
      routeName,
      pageVersion: `${routeName}-v1`,
      state: {},
      capabilities: [],
    }),
    execute: async () => {
      setTimeout(() => {
        router.currentRoute.value = { name: "SystemRole" }
      }, 0)
      return {
        status: "SUCCEEDED",
        summary: "已进入角色管理",
        routeChangeExpected: true,
      }
    },
  }
  const api = {
    reportAiAgentActionResult: async (_taskId, _actionId, payload) => {
      reportedPayload = payload
    },
  }
  const trace = { setAgentTrace: () => {}, clearAgentTrace: () => {} }
  const { createAiAgentRuntime } = loadModule(
    "src/utils/aiAgent/runtime.js",
    {
      vue: { reactive: (value) => value },
      "@/api/ai/agent": api,
      "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
      "@/utils/aiAgent/protocol": { isTerminalTask: () => false },
      "@/utils/aiAgent/traceContext": trace,
      "@/router": router,
    }
  )
  const runtime = createAiAgentRuntime({
    api,
    registry,
    trace,
    router,
    clientInstanceId: "client-cross-page",
  })
  runtime.state.task = { id: 18, status: "EXECUTING" }

  await runtime.handleEvent({
    type: "action",
    taskId: 18,
    status: "EXECUTING",
    action: {
      actionId: "action-navigate",
      actionType: "ui.click",
      routeName: "SystemUser",
      pageVersion: "SystemUser-v1",
      target: "auto.click.low.1",
      arguments: {},
    },
  })

  assert.equal(reportedPayload.executionRouteName, "SystemUser")
  assert.equal(reportedPayload.executionPageVersion, "SystemUser-v1")
  assert.equal(
    reportedPayload.snapshot.routeName,
    "SystemRole",
    "跨页面动作完成后必须用目标页面快照继续规划"
  )
}

async function checkStartRestoresExistingTaskBeforeCreating() {
  const existingTask = {
    id: 52,
    goalSummary: "维护用户资料",
    status: "PAUSED",
    routeName: "SystemUser",
    pageVersion: "SystemUser-v1",
    currentStep: 2,
    activeActionId: null,
    activeAction: null,
  }
  let createCount = 0
  const api = {
    getAiAgentStatus: async () => ({
      data: {
        enabled: true,
        activeTask: existingTask,
        currentClient: true,
      },
    }),
    createAiAgentTask: async () => {
      createCount += 1
      throw new Error("当前已有网页代理任务运行中")
    },
  }
  const registry = {
    snapshot: async () => ({
      routeName: "SystemUser",
      pageVersion: "SystemUser-v1",
      state: {},
      capabilities: [],
    }),
  }
  const trace = { setAgentTrace: () => {}, clearAgentTrace: () => {} }
  const router = { currentRoute: { value: { name: "SystemUser" } } }
  const { createAiAgentRuntime } = loadModule(
    "src/utils/aiAgent/runtime.js",
    {
      vue: { reactive: (value) => value },
      "@/api/ai/agent": api,
      "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
      "@/utils/aiAgent/protocol": {
        isTerminalTask: (task) =>
          ["SUCCEEDED", "FAILED", "CANCELED"].includes(task?.status || task),
      },
      "@/utils/aiAgent/traceContext": trace,
      "@/router": router,
    }
  )
  const runtime = createAiAgentRuntime({
    api,
    registry,
    trace,
    router,
    clientInstanceId: "client-existing-task",
  })

  const result = await runtime.start("创建新用户")

  assert.equal(result, existingTask)
  assert.equal(runtime.state.task, existingTask)
  assert.equal(createCount, 0, "已有活动任务时不得再次调用创建接口")
}

async function checkStartRecoversConcurrentTaskAfterBusy() {
  const concurrentTask = {
    id: 53,
    goalSummary: "维护角色权限",
    status: "PAUSED",
    routeName: "SystemRole",
    pageVersion: "SystemRole-v1",
    currentStep: 1,
    activeActionId: null,
    activeAction: null,
  }
  let statusCount = 0
  const api = {
    getAiAgentStatus: async () => {
      statusCount += 1
      return {
        data: {
          enabled: true,
          activeTask: statusCount === 1 ? null : concurrentTask,
          currentClient: statusCount === 1,
        },
      }
    },
    createAiAgentTask: async () => {
      throw new Error("当前已有网页代理任务运行中")
    },
  }
  const registry = {
    snapshot: async () => ({
      routeName: "SystemRole",
      pageVersion: "SystemRole-v1",
      state: {},
      capabilities: [],
    }),
  }
  const trace = { setAgentTrace: () => {}, clearAgentTrace: () => {} }
  const router = { currentRoute: { value: { name: "SystemRole" } } }
  const { createAiAgentRuntime } = loadModule(
    "src/utils/aiAgent/runtime.js",
    {
      vue: { reactive: (value) => value },
      "@/api/ai/agent": api,
      "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
      "@/utils/aiAgent/protocol": {
        isTerminalTask: (task) =>
          ["SUCCEEDED", "FAILED", "CANCELED"].includes(task?.status || task),
      },
      "@/utils/aiAgent/traceContext": trace,
      "@/router": router,
    }
  )
  const runtime = createAiAgentRuntime({
    api,
    registry,
    trace,
    router,
    clientInstanceId: "client-concurrent-task",
  })

  const result = await runtime.start("创建角色")

  assert.equal(result, concurrentTask)
  assert.equal(runtime.state.task, concurrentTask)
  assert.equal(statusCount, 2, "创建冲突后应重新同步一次服务端活动任务")
}

async function checkSessionRouteAndDisconnect() {
  const values = new Map()
  const sessionStorage = {
    getItem: (key) => values.get(key) || null,
    setItem: (key, value) => values.set(key, value),
  }
  const router = { currentRoute: { value: { name: "SysUser" } } }
  let runtime
  let rejectStream
  let pauseCount = 0
  const snapshotRoutes = []
  const snapshot = {
    routeName: "SysUser",
    pageVersion: "sys-user-v2",
    state: {},
    capabilities: [{ actionType: "CLICK", target: "user.save", parameterSchema: {} }],
  }
  const registry = {
    current: (routeName) => ({ routeName }),
    snapshot: async (routeName) => {
      snapshotRoutes.push(routeName)
      return snapshot
    },
    execute: async () => "不应执行",
  }
  const api = {
    getAiAgentStatus: async () => ({
      data: { enabled: true, activeTask: null, currentClient: true },
    }),
    createAiAgentTask: async (payload) => ({
      data: {
        id: 21,
        routeName: payload.snapshot.routeName,
        pageVersion: payload.snapshot.pageVersion,
        status: "EXECUTING",
      },
    }),
    streamAiAgentEvents: () =>
      new Promise((_resolve, reject) => {
        rejectStream = reject
    }),
    pauseAiAgentTask: async () => {
      if (pauseCount === 0) {
        assert.notEqual(
          runtime.state.task.status,
          "PAUSED",
          "pause POST 成功前不得抢先写本地 PAUSED"
        )
      }
      pauseCount += 1
    },
    reportAiAgentActionResult: async () => {},
  }
  const trace = { setAgentTrace: () => {}, clearAgentTrace: () => {} }
  const runtimeModule = loadModule(
    "src/utils/aiAgent/runtime.js",
    {
      vue: { reactive: (value) => value },
      "@/api/ai/agent": api,
      "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
      "@/utils/aiAgent/protocol": {
        isTerminalTask: (status) =>
          ["SUCCEEDED", "FAILED", "CANCELED"].includes(status?.status || status),
      },
      "@/utils/aiAgent/traceContext": trace,
      "@/router": router,
    },
    { sessionStorage }
  )

  const firstId = runtimeModule.getOrCreateSessionId("basic-ai-agent-client")
  const secondId = runtimeModule.getOrCreateSessionId("basic-ai-agent-client")
  assert.equal(firstId, secondId, "同一标签页刷新后 clientInstanceId 应保持一致")

  runtime = runtimeModule.createAiAgentRuntime({
    api,
    registry,
    trace,
    router,
    clientInstanceId: firstId,
  })
  await runtime.start("保存用户")
  assert.equal(snapshotRoutes[0], "SysUser", "启动时必须绑定当前 router routeName")
  rejectStream(new Error("SSE disconnected"))
  await new Promise((resolve) => setImmediate(resolve))
  await new Promise((resolve) => setImmediate(resolve))
  assert.equal(pauseCount, 1, "SSE 非主动断开必须请求服务端暂停")
  assert.equal(runtime.state.task.status, "PAUSED")
  await runtime.takeover()
  assert.equal(pauseCount, 1, "服务端已真实暂停时接管不得重复请求暂停")

  const localOnlyPausedRuntime = runtimeModule.createAiAgentRuntime({
    api,
    registry,
    trace,
    router,
    clientInstanceId: firstId,
  })
  localOnlyPausedRuntime.state.task = { id: 22, status: "PAUSED" }
  await localOnlyPausedRuntime.takeover()
  assert.equal(pauseCount, 2, "仅本地暂停时接管必须补发服务端暂停请求")

  let executeCount = 0
  let failedReports = 0
  registry.execute = async () => {
    executeCount += 1
  }
  api.reportAiAgentActionResult = async (_taskId, _actionId, payload) => {
    if (payload.status === "FAILED") failedReports += 1
  }
  runtime.state.task = { id: 21, status: "EXECUTING" }
  await runtime.handleEvent({
    type: "action",
    taskId: 21,
    status: "EXECUTING",
    action: {
      actionId: "stale-page",
      routeName: "SysUser",
      pageVersion: "sys-user-v1",
      target: "user.save",
      arguments: {},
    },
  })
  assert.equal(executeCount, 0, "pageVersion 不一致时禁止执行")
  assert.equal(failedReports, 1, "页面契约不一致也必须上报失败结果")
  await runtime.handleEvent({
    type: "action",
    taskId: 21,
    status: "EXECUTING",
    action: {
      actionId: "stale-route",
      routeName: "SysRole",
      pageVersion: "sys-user-v2",
      target: "user.save",
      arguments: {},
    },
  })
  assert.equal(executeCount, 0, "routeName 不一致时禁止执行")
  assert.equal(failedReports, 2, "路由不一致也必须上报失败结果")
}

async function checkConfirmationRestoredAfterRefresh() {
  const activeAction = {
    actionId: "action-confirm",
    status: "WAITING_CONFIRMATION",
    confirmationSummary:
      "对象：公告“阶段一验收”（ID：9）；动作：发布公告；影响：公告将对目标范围可见",
  }
  const api = {
    getAiAgentStatus: async () => ({
      data: {
        enabled: true,
        activeTask: {
          id: 31,
          status: "WAITING_CONFIRMATION",
          activeAction,
        },
      },
    }),
  }
  const runtimeModule = loadModule("src/utils/aiAgent/runtime.js", {
    vue: { reactive: (value) => value },
    "@/api/ai/agent": api,
    "@/utils/aiAgent/pageRegistry": { pageRegistry: {} },
    "@/utils/aiAgent/protocol": { isTerminalTask: () => false },
    "@/utils/aiAgent/traceContext": {
      setAgentTrace: () => {},
      clearAgentTrace: () => {},
    },
    "@/router": { currentRoute: { value: { name: "SystemNotice" } } },
  })
  const runtime = runtimeModule.createAiAgentRuntime({
    api,
    registry: {},
    trace: { setAgentTrace: () => {}, clearAgentTrace: () => {} },
    router: { currentRoute: { value: { name: "SystemNotice" } } },
    clientInstanceId: "client-refresh",
  })

  await runtime.refresh()

  assert.equal(runtime.state.action, activeAction)
  assert.equal(runtime.state.confirmation?.action, activeAction)
  assert.match(runtime.state.confirmation?.summary || "", /阶段一验收/)
}

async function checkCompletedActiveActionIsNotReplayedAfterRefresh() {
  let executeCount = 0
  const activeAction = {
    actionId: "action-completed",
    status: "SUCCEEDED",
    routeName: "SystemDict",
    pageVersion: "dict-v1",
    target: "dict.toolbar.add",
    arguments: {},
  }
  const api = {
    getAiAgentStatus: async () => ({
      data: {
        enabled: true,
        activeTask: {
          id: 32,
          status: "PAUSED",
          activeAction,
        },
        currentClient: true,
      },
    }),
  }
  const registry = {
    snapshot: async () => ({
      routeName: "SystemDict",
      pageVersion: "dict-v1",
      state: {},
      capabilities: [],
    }),
    execute: async () => {
      executeCount += 1
    },
  }
  const runtimeModule = loadModule("src/utils/aiAgent/runtime.js", {
    vue: { reactive: (value) => value },
    "@/api/ai/agent": api,
    "@/utils/aiAgent/pageRegistry": { pageRegistry: registry },
    "@/utils/aiAgent/protocol": { isTerminalTask: () => false },
    "@/utils/aiAgent/traceContext": {
      setAgentTrace: () => {},
      clearAgentTrace: () => {},
    },
    "@/router": { currentRoute: { value: { name: "SystemDict" } } },
  })
  const runtime = runtimeModule.createAiAgentRuntime({
    api,
    registry,
    trace: { setAgentTrace: () => {}, clearAgentTrace: () => {} },
    router: { currentRoute: { value: { name: "SystemDict" } } },
    clientInstanceId: "client-reconnect",
  })

  await runtime.refresh()
  await runtime.handleEvent({
    type: "action",
    taskId: 32,
    status: "EXECUTING",
    action: activeAction,
  })

  assert.equal(executeCount, 0, "刷新恢复的已完成动作不得因 SSE 重放再次执行")
}

async function checkPausedTaskCanBeClaimedByNewTab() {
  let claimCount = 0
  const pausedTask = { id: 41, status: "PAUSED", goalSummary: "创建字典" }
  const api = {
    getAiAgentStatus: async (clientInstanceId) => {
      assert.equal(clientInstanceId, "tab-new")
      return { data: { enabled: true, activeTask: pausedTask, currentClient: false } }
    },
    claimAiAgentTask: async (taskId, clientInstanceId) => {
      assert.equal(taskId, 41)
      assert.equal(clientInstanceId, "tab-new")
      claimCount += 1
      return { data: pausedTask }
    },
  }
  const runtimeModule = loadModule("src/utils/aiAgent/runtime.js", {
    vue: { reactive: (value) => value },
    "@/api/ai/agent": api,
    "@/utils/aiAgent/pageRegistry": { pageRegistry: {} },
    "@/utils/aiAgent/protocol": { isTerminalTask: () => false },
    "@/utils/aiAgent/traceContext": {
      setAgentTrace: () => {},
      clearAgentTrace: () => {},
    },
    "@/router": { currentRoute: { value: { name: "SystemDict" } } },
  })
  const runtime = runtimeModule.createAiAgentRuntime({
    api,
    registry: {},
    trace: { setAgentTrace: () => {}, clearAgentTrace: () => {} },
    router: { currentRoute: { value: { name: "SystemDict" } } },
    clientInstanceId: "tab-new",
  })

  await runtime.refresh()
  assert.equal(runtime.state.requiresClaim, true)
  await runtime.takeover()
  assert.equal(claimCount, 1)
  assert.equal(runtime.state.requiresClaim, false)
  assert.equal(runtime.state.task.status, "PAUSED")
}

function checkObserverUi() {
  assertIncludes("src/layouts/index.vue", "<ai-agent-observer")
  assertIncludes("src/components/AiChatFloat/index.vue", 'value: "agent"')
  assertIncludes("src/components/AiAgentObserver/index.vue", "agent-edge-flow")
  assertIncludes("src/components/AiAgentObserver/index.vue", "prefers-reduced-motion")
  for (const label of ["暂停", "接管", "终止"]) {
    assertIncludes("src/components/AiAgentObserver/index.vue", label)
  }
  assertIncludes("src/components/AiAgentPanel/index.vue", 'aria-label="网页操作目标"')
  assertIncludes("src/components/AiAgentPanel/index.vue", "高风险操作需要确认")
  assertIncludes("src/components/AiAgentPanel/index.vue", "接管已暂停任务")
}

async function checkElementPlusAdapter() {
  const username = fakeElement({
    tagName: "INPUT",
    formLabel: "登录账号",
    attributes: { type: "text" },
  })
  const apiKey = fakeElement({
    tagName: "INPUT",
    formLabel: "DeepSeek API Key",
    attributes: { type: "text" },
  })
  const password = fakeElement({
    tagName: "INPUT",
    formLabel: "登录密码",
    attributes: { type: "password" },
  })
  const nestedCheckbox = fakeElement({
    tagName: "INPUT",
    type: "checkbox",
    value: "on",
    componentWrapper: ".el-checkbox",
  })
  const nestedSegmentInput = fakeElement({
    tagName: "INPUT",
    type: "radio",
    value: "ALL",
    componentWrapper: ".el-segmented",
  })
  const segmentOption = fakeElement({
    tagName: "LABEL",
    text: "全员",
    selectors: [".el-segmented__item"],
  })
  const menuItem = fakeElement({
    tagName: "LI",
    text: "角色管理",
    selectors: [".el-menu-item"],
  })
  const query = fakeElement({ tagName: "BUTTON", text: "查询" })
  const save = fakeElement({ tagName: "BUTTON", text: "保存" })
  const remove = fakeElement({ tagName: "BUTTON", text: "删除" })
  const confirm = fakeElement({ tagName: "BUTTON", text: "确定" })
  const headers = [{ textContent: "账号" }, { textContent: "昵称" }]
  const cells = [{ textContent: "alice" }, { textContent: "管理员" }]
  const table = {
    querySelectorAll: (selector) => {
      if (selector === ".el-table__header th") return headers
      if (selector === ".el-table__body .el-table__row") {
        return [{ querySelectorAll: () => cells }]
      }
      return []
    },
  }
  const controls = [
    username,
    apiKey,
    password,
    nestedCheckbox,
    nestedSegmentInput,
    segmentOption,
    menuItem,
    query,
    save,
    remove,
  ]
  let pageConfirmationVisible = false
  remove.click = () => {
    remove.clicked += 1
    pageConfirmationVisible = true
  }
  const document = {
    title: "用户管理",
    querySelectorAll: (selector) => {
      if (selector === ".el-table") return [table]
      if (selector.includes(".el-message-box__btns")) {
        return pageConfirmationVisible ? [confirm] : []
      }
      if (selector.includes(".el-select-dropdown__item")) return []
      return controls
    },
    defaultView: {
      Event: class Event {
        constructor(type) {
          this.type = type
        }
      },
      getComputedStyle: () => ({ display: "block", visibility: "visible" }),
    },
  }
  controls.forEach((control) => {
    control.ownerDocument = document
  })

  const { createElementPlusAdapter } = loadModule(
    "src/utils/aiAgent/elementPlusAdapter.js",
    { "@/utils/aiAgent/visualEffects": { pulseAgentTarget: () => {} } }
  )
  const adapter = createElementPlusAdapter({ document })
  const snapshot = await adapter.snapshot("SystemUser")
  const elements = plain(snapshot.state.elements)

  assert.equal(elements.some((element) => element.label === "登录账号"), true)
  assert.equal(elements.some((element) => element.label.includes("API Key")), false)
  assert.equal(elements.some((element) => element.label.includes("密码")), false)
  assert.equal(elements.some((element) => element.label === "on"), false)
  assert.equal(elements.some((element) => element.label === "ALL"), false)
  assert.match(
    elements.find((element) => element.label === "全员").target,
    /^auto\.click\.low\./
  )
  assert.ok(Array.isArray(snapshot.state.tables), "页面快照应包含可见表格")
  assert.deepEqual(plain(snapshot.state.tables), [
    { columns: ["账号", "昵称"], rows: [["alice", "管理员"]] },
  ])
  const queryElement = elements.find((element) => element.label === "查询")
  const saveElement = elements.find((element) => element.label === "保存")
  assert.match(queryElement.target, /^auto\.click\.low\./)
  assert.match(saveElement.target, /^auto\.click\.high\./)

  const usernameElement = elements.find((element) => element.label === "登录账号")
  await adapter.execute("SystemUser", {
    actionType: "ui.fill",
    target: usernameElement.target,
    arguments: { value: "alice" },
  })
  assert.equal(username.value, "alice")
  assert.deepEqual(username.events, ["input", "change"])

  await adapter.execute("SystemUser", {
    actionType: "ui.click",
    target: queryElement.target,
    arguments: {},
  })
  assert.equal(query.clicked, 1)

  const menuElement = elements.find((element) => element.label === "角色管理")
  const menuResult = await adapter.execute("SystemUser", {
    actionType: "ui.click",
    target: menuElement.target,
    arguments: {},
  })
  assert.equal(menuResult.routeChangeExpected, true, "菜单项点击应标记为跨页面候选动作")

  const removeElement = elements.find((element) => element.label === "删除")
  await adapter.execute("SystemUser", {
    actionType: "ui.click",
    target: removeElement.target,
    arguments: {},
  })
  assert.equal(remove.clicked, 1)
  assert.equal(confirm.clicked, 1, "服务端确认后应通过页面自身的确认框")
}

function checkRouterBootstrapDoesNotLoadAgentRuntimeSynchronously() {
  const router = read("src/router/index.js")
  // Layout 会加载 Agent runtime，若 router 同步导入 Layout，会形成
  // router -> Layout -> runtime -> request -> router 的初始化循环。
  assert.doesNotMatch(
    router,
    /import\s+Layout\s+from\s+["']@\/layouts\/index\.vue["']/,
    "router 初始化不得同步加载包含 Agent runtime 的 Layout"
  )
  assert.match(
    router,
    /const\s+Layout\s*=\s*\(\)\s*=>\s*import\(["']@\/layouts\/index\.vue["']\)/,
    "Layout 路由应在导航时懒加载"
  )
}

async function main() {
  await checkProtocol()
  await checkAgentControlRequestsAreNeverRetried()
  await checkRequestAutomaticallyConsumesAgentTrace()
  await checkElementPlusAdapter()
  await checkPageRegistry()
  checkTraceContext()
  checkSafetyBoundaries()
  await checkUiActionCompatibility()
  await checkFailedActionIsNotReplayed()
  await checkCrossPageActionUsesDestinationSnapshot()
  await checkStartRestoresExistingTaskBeforeCreating()
  await checkStartRecoversConcurrentTaskAfterBusy()
  await checkSessionRouteAndDisconnect()
  await checkConfirmationRestoredAfterRefresh()
  await checkCompletedActiveActionIsNotReplayedAfterRefresh()
  await checkPausedTaskCanBeClaimedByNewTab()
  checkRouterBootstrapDoesNotLoadAgentRuntimeSynchronously()
  checkObserverUi()
  console.log("AI 网页代理核心检查通过")
}

main().catch((error) => {
  console.error("AI 网页代理核心契约检查失败")
  console.error(error.stack || error)
  process.exitCode = 1
})
