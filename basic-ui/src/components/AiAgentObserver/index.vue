<template>
  <div v-if="visible" :class="['agent-observer', `is-${visualState}`]" aria-live="polite">
    <div class="agent-edge" aria-hidden="true" />
    <div class="agent-control-bar">
      <span class="agent-state">
        <el-icon aria-hidden="true"><Connection /></el-icon>
        {{ stateLabel }}
      </span>
      <div class="agent-controls">
        <el-button
          v-if="runtime.state.requiresClaim"
          size="small"
          type="primary"
          :loading="busy"
          @click="run(runtime.takeover)"
        >
          <el-icon><User /></el-icon>
          接管
        </el-button>
        <el-button
          v-else-if="visualState === 'paused'"
          size="small"
          type="primary"
          :loading="busy"
          @click="run(runtime.resume)"
        >
          继续
        </el-button>
        <el-button v-else size="small" :loading="busy" @click="run(runtime.pause)">
          <el-icon><VideoPause /></el-icon>
          暂停
        </el-button>
        <el-button
          v-if="!runtime.state.requiresClaim"
          size="small"
          :loading="busy"
          @click="run(runtime.takeover)"
        >
          <el-icon><User /></el-icon>
          接管
        </el-button>
        <el-button
          v-if="!runtime.state.requiresClaim"
          size="small"
          type="danger"
          plain
          :loading="busy"
          @click="run(runtime.cancel)"
        >
          <el-icon><CloseBold /></el-icon>
          终止
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CloseBold, Connection, User, VideoPause } from "@element-plus/icons-vue"
import { computed, ref } from "vue"

import { observerVisualState } from "@/utils/aiAgent/visualEffects"

const props = defineProps({
  runtime: {
    type: Object,
    required: true,
  },
})

const busy = ref(false)
const visualState = computed(() =>
  observerVisualState(props.runtime.state.task, props.runtime.state.confirmation)
)
const visible = computed(() => visualState.value !== "idle")
const stateLabel = computed(() => ({
  active: "AI 正在操作",
  paused: "AI 已暂停",
  confirmation: "等待高风险确认",
})[props.runtime.state.requiresClaim ? "claim" : visualState.value] || (
  props.runtime.state.requiresClaim ? "等待接管" : "AI 正在操作"
))

const run = async (operation) => {
  // 旁观层与侧边面板共享运行时，局部互斥可防止该浮层自身重复发送控制请求。
  if (busy.value) return
  busy.value = true
  try {
    await operation()
  } finally {
    busy.value = false
  }
}
</script>

<style lang="scss" scoped>
@property --agent-glow-angle {
  syntax: "<angle>";
  inherits: false;
  initial-value: 0deg;
}

.agent-observer,
.agent-edge {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.agent-observer {
  z-index: $base-z-index + 1;
}

.agent-edge {
  box-sizing: border-box;
  padding: 3px;

  &::before {
    position: absolute;
    inset: 0;
    padding: 3px;
    content: "";
    background: conic-gradient(
      from var(--agent-glow-angle),
      #ff5ea8,
      #a970ff,
      #5c8dff,
      #38d9e8,
      #79e39b,
      #ffd166,
      #ff7a70,
      #ff5ea8
    );
    mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
    mask-composite: exclude;
  }
}

.is-active .agent-edge::before {
  animation: agent-edge-flow 5s linear infinite;
}

.is-paused .agent-edge::before {
  filter: saturate(0.35);
}

.is-confirmation .agent-edge::before {
  background: var(--el-color-warning);
}

.agent-control-bar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  display: flex;
  box-sizing: border-box;
  width: min(520px, calc(100vw - 48px));
  min-height: 52px;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px 6px 14px;
  pointer-events: auto;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 8px 28px rgb(0 0 0 / 18%);
  transform: translateX(-50%);
  gap: 12px;
}

.agent-state,
.agent-controls {
  display: flex;
  align-items: center;
}

.agent-state {
  min-width: 0;
  font-size: 13px;
  font-weight: 600;
  gap: 6px;
}

.agent-controls {
  flex: 0 0 auto;
  gap: 4px;

  .el-button + .el-button {
    margin-left: 0;
  }
}

@keyframes agent-edge-flow {
  to {
    --agent-glow-angle: 360deg;
  }
}

:global(.agent-target-active) {
  position: relative;
  z-index: 1;
  outline: 2px solid var(--el-color-primary) !important;
  outline-offset: 3px;
  animation: agent-target-pulse 900ms ease-out;
}

@keyframes agent-target-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgb(64 158 255 / 0%);
  }

  35% {
    box-shadow: 0 0 0 6px rgb(64 158 255 / 22%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .is-active .agent-edge::before,
  :global(.agent-target-active) {
    animation: none;
  }
}
</style>
