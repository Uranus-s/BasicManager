const assert = require("node:assert/strict")
const fs = require("node:fs")
const path = require("node:path")

const projectRoot = path.resolve(__dirname, "..")

function read(relativePath) {
  const fullPath = path.join(projectRoot, relativePath)
  assert.ok(fs.existsSync(fullPath), `${relativePath}: 文件不存在`)
  return fs.readFileSync(fullPath, "utf8")
}

function assertIncludes(relativePath, expected) {
  assert.ok(
    read(relativePath).includes(expected),
    `${relativePath}: 缺少 ${JSON.stringify(expected)}`
  )
}

function assertExcludes(relativePath, unexpected) {
  assert.ok(
    !read(relativePath).includes(unexpected),
    `${relativePath}: 不应再包含 ${JSON.stringify(unexpected)}`
  )
}

const noticePage = "src/views/system/notice/index.vue"
for (const fragment of [
  "pageRegistry",
  "data-ai-target",
  "noticeCapabilities",
  "noticeActions",
  "unregisterAiPage",
  "agentConfirmedOperation",
  "consumeAgentTrace",
  "aiAgentTrace",
]) {
  assertExcludes(noticePage, fragment)
}

const dictPage = "src/views/system/dict/index.vue"
for (const fragment of [
  "pageRegistry",
  "data-ai-target",
  "dictCapabilities",
  "dictActions",
  "unregisterAiPage",
  "agentConfirmedOperation",
  "consumeAgentTrace",
  "aiAgentTrace",
]) {
  assertExcludes(dictPage, fragment)
}

const configPage = "src/views/system/config/index.vue"
for (const fragment of [
  'label="AI 网页代理"',
  '<el-switch',
  'v-model="aiForm.agentEnabled"',
  'const defaultAiForm = () => ({ apiKey: "", agentEnabled: false });',
  'agentEnabled: Boolean(data?.agentEnabled)',
  'savedAi: defaultAiForm()',
  'this.savedAi = { ...ai };',
  'this.savedAi.agentEnabled !== this.aiForm.agentEnabled',
  'this.aiForm = { ...this.savedAi };',
  'apiKey: this.aiForm.apiKey.trim()',
  'agentEnabled: this.aiForm.agentEnabled',
  'aiApiKeyDirty() {',
  '!canEdit || aiApiKeyDirty || !deepSeekApiKeyConfigured',
  'if (!this.canEdit || this.aiApiKeyDirty || !this.deepSeekApiKeyConfigured) return;',
]) {
  assertIncludes(configPage, fragment)
}

console.log("AI 网页代理页面检查通过")
