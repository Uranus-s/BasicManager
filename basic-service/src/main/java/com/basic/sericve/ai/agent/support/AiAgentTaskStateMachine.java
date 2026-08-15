package com.basic.sericve.ai.agent.support;

import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.sericve.ai.agent.model.AiAgentTaskStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.CANCELED;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.CREATED;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.EXECUTING;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.FAILED;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.PAUSED;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.PLANNING;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.SUCCEEDED;
import static com.basic.sericve.ai.agent.model.AiAgentTaskStatus.WAITING_CONFIRMATION;

/**
 * AI 网页代理任务状态机，阻止终态恢复或绕过确认等非法流转。
 */
@Component
public class AiAgentTaskStateMachine {

    private static final Map<AiAgentTaskStatus, Set<AiAgentTaskStatus>> ALLOWED = Map.of(
            CREATED, Set.of(PLANNING, CANCELED),
            PLANNING, Set.of(EXECUTING, WAITING_CONFIRMATION, PAUSED, SUCCEEDED, FAILED, CANCELED),
            EXECUTING, Set.of(PLANNING, PAUSED, FAILED, CANCELED),
            WAITING_CONFIRMATION, Set.of(EXECUTING, PAUSED, CANCELED),
            PAUSED, Set.of(PLANNING, CANCELED),
            SUCCEEDED, Set.of(),
            FAILED, Set.of(),
            CANCELED, Set.of());

    /**
     * 检查两个状态之间是否允许流转。
     */
    public boolean canTransition(AiAgentTaskStatus source, AiAgentTaskStatus target) {
        return source != null && target != null
                && ALLOWED.getOrDefault(source, Set.of()).contains(target);
    }

    /**
     * 要求状态流转合法，否则抛出稳定业务错误。
     */
    public void requireTransition(AiAgentTaskStatus source, AiAgentTaskStatus target) {
        if (!canTransition(source, target)) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
    }
}
