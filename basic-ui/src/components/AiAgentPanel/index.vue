<template>
  <section class="agent-panel" aria-label="网页操作">
    <div v-if="isIdle" class="agent-start">
      <label class="field-label" for="ai-agent-goal">操作目标</label>
      <el-input
        id="ai-agent-goal"
        v-model="goal"
        aria-label="网页操作目标"
        type="textarea"
        :autosize="{ minRows: 5, maxRows: 9 }"
        maxlength="1000"
        show-word-limit
        resize="none"
        placeholder="输入要在当前页面完成的目标"
        @keydown.ctrl.enter.prevent="start"
      />
      <el-button
        class="start-button"
        type="primary"
        :loading="submitting"
        :disabled="!goal.trim() || !runtime.state.enabled"
        @click="start"
      >
        开始操作
      </el-button>
      <p v-if="!runtime.state.enabled" class="disabled-message" role="status">
        网页操作当前未启用
      </p>
    </div>

    <div v-else class="agent-progress" aria-live="polite">
      <dl class="task-summary">
        <div>
          <dt>目标</dt>
          <dd>{{ runtime.state.task.goalSummary }}</dd>
        </div>
        <div class="task-meta">
          <span>步骤 {{ runtime.state.task.currentStep || 0 }}</span>
          <el-tag :type="statusTone" effect="plain" size="small">
            {{ statusLabel }}
          </el-tag>
        </div>
      </dl>

      <div v-if="runtime.state.action && !runtime.state.requiresClaim" class="current-action">
        <span>当前动作</span>
        <strong>{{ runtime.state.action.target }}</strong>
      </div>

      <div v-if="runtime.state.requiresClaim" class="claim-block">
        <strong>任务已在原页面安全暂停</strong>
        <p>接管后任务仍保持暂停，你可以核对当前页面后再继续。</p>
        <el-button type="primary" :loading="controlling" @click="takeover">
          接管已暂停任务
        </el-button>
      </div>

      <div v-else-if="runtime.state.confirmation" class="confirmation-block">
        <strong>高风险操作需要确认</strong>
        <p>{{ runtime.state.confirmation.summary }}</p>
        <div class="confirmation-actions">
          <el-button :loading="controlling" @click="cancel">终止任务</el-button>
          <el-button type="primary" :loading="controlling" @click="confirm">
            确认并继续
          </el-button>
        </div>
      </div>

      <div v-else class="task-actions">
        <el-button
          v-if="runtime.state.task.status === 'PAUSED'"
          type="primary"
          :loading="controlling"
          @click="resume"
        >
          继续
        </el-button>
        <el-button v-else :loading="controlling" @click="pause">暂停</el-button>
        <el-button type="danger" plain :loading="controlling" @click="cancel">
          终止
        </el-button>
      </div>

      <p v-if="runtime.state.error" class="agent-error" role="alert">
        {{ runtime.state.error }}
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue"

import { isTerminalTask } from "@/utils/aiAgent/protocol"

const props = defineProps({
  runtime: {
    type: Object,
    required: true,
  },
})

const goal = ref("")
const submitting = ref(false)
const controlling = ref(false)

const isIdle = computed(
  () => !props.runtime.state.task || isTerminalTask(props.runtime.state.task)
)

const statusLabels = {
  CREATED: "已创建",
  PLANNING: "规划中",
  EXECUTING: "执行中",
  WAITING_CONFIRMATION: "等待确认",
  PAUSED: "已暂停",
  SUCCEEDED: "已完成",
  FAILED: "失败",
  CANCELED: "已终止",
}

const statusLabel = computed(
  () => statusLabels[props.runtime.state.task?.status] || "处理中"
)
const statusTone = computed(() => {
  const status = props.runtime.state.task?.status
  if (status === "PAUSED" || status === "WAITING_CONFIRMATION") return "warning"
  if (status === "FAILED" || status === "CANCELED") return "danger"
  if (status === "SUCCEEDED") return "success"
  return "primary"
})

const runControl = async (operation) => {
  // 所有任务控制共用同一互斥标记，避免用户连续点击产生暂停、恢复、确认之间的竞态请求。
  if (controlling.value) return
  controlling.value = true
  try {
    await operation()
  } finally {
    controlling.value = false
  }
}

const start = async () => {
  const value = goal.value.trim()
  if (!value || submitting.value) return
  submitting.value = true
  try {
    await props.runtime.start(value)
  } finally {
    submitting.value = false
  }
}

const pause = () => runControl(() => props.runtime.pause())
const resume = () => runControl(() => props.runtime.resume())
const cancel = () => runControl(() => props.runtime.cancel())
const confirm = () => runControl(() => props.runtime.confirm())
const takeover = () => runControl(() => props.runtime.takeover())

onMounted(() => {
  // 首次刷新失败由运行时状态和后续用户操作呈现，面板挂载不能因初始化请求失败而产生未处理 Promise。
  props.runtime.refresh().catch(() => {})
})
</script>

<style lang="scss" scoped>
.agent-panel {
  min-height: 0;
  padding: 16px;
  overflow: auto;
  background: var(--el-fill-color-extra-light);
  flex: 1;
}

.agent-start,
.agent-progress {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-label,
.current-action span,
.task-summary dt {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 500;
}

.start-button {
  width: 100%;
  min-height: 44px;
}

.disabled-message,
.agent-error {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
}

.disabled-message {
  color: var(--el-text-color-secondary);
  text-align: center;
}

.task-summary {
  display: flex;
  margin: 0;
  flex-direction: column;
  gap: 12px;

  div:first-child {
    display: grid;
    gap: 4px;
  }

  dd {
    margin: 0;
    overflow-wrap: anywhere;
    color: var(--el-text-color-primary);
    font-size: 14px;
    line-height: 1.6;
  }
}

.task-meta,
.task-actions,
.confirmation-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.task-meta {
  color: var(--el-text-color-regular);
  font-variant-numeric: tabular-nums;
  font-size: 13px;
}

.current-action,
.confirmation-block,
.claim-block {
  padding: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.current-action {
  display: grid;
  gap: 4px;

  strong {
    overflow-wrap: anywhere;
    font-size: 13px;
  }
}

.confirmation-block {
  border-color: var(--el-color-warning-light-5);

  > strong {
    color: var(--el-color-warning-dark-2);
    font-size: 14px;
  }

  p {
    margin: 8px 0 12px;
    overflow-wrap: anywhere;
    line-height: 1.6;
  }
}

.claim-block {
  display: grid;
  gap: 10px;

  p {
    margin: 0;
    color: var(--el-text-color-regular);
    font-size: 13px;
    line-height: 1.6;
  }
}

.task-actions .el-button,
.confirmation-actions .el-button {
  min-height: 40px;
  flex: 1;
}

.agent-error {
  padding: 10px 12px;
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  border-radius: 4px;
}
</style>
