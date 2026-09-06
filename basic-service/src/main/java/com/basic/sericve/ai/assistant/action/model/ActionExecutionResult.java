package com.basic.sericve.ai.assistant.action.model;

/**
 * Action 执行产生的业务主键和用户回复。
 */
public record ActionExecutionResult(Long resultId, String reply) {
}
