package com.basic.web.interceptor;

import com.basic.core.security.model.LoginUser;
import com.basic.sericve.ai.agent.model.AiAgentUserContext;
import com.basic.sericve.ai.agent.service.IAiAgentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * AI 代理业务请求关联拦截器，将客户端候选头校验为可信请求属性。
 */
@Component
@RequiredArgsConstructor
public class AiAgentTraceInterceptor implements HandlerInterceptor {

    public static final String TASK_HEADER = "X-AI-Agent-Task-Id";
    public static final String ACTION_HEADER = "X-AI-Agent-Action-Id";
    public static final String TASK_ATTRIBUTE = "aiAgentTaskId";
    public static final String ACTION_ATTRIBUTE = "aiAgentActionId";

    private final IAiAgentService aiAgentService;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        String taskHeader = request.getHeader(TASK_HEADER);
        String actionHeader = request.getHeader(ACTION_HEADER);
        // 普通人工操作无需携带代理关联头；一旦携带则两个头必须成对出现，禁止形成半可信上下文。
        if (taskHeader == null && actionHeader == null) {
            return true;
        }
        if (taskHeader == null || actionHeader == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
        Long taskId;
        try {
            taskId = Long.valueOf(taskHeader);
        } catch (NumberFormatException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
        AiAgentUserContext user = currentUser();
        // 客户端头只是候选关联信息，必须同时匹配当前用户、活动任务和执行中动作后才写入请求属性。
        if (user == null || !aiAgentService.validateTrace(user, taskId, actionHeader)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        request.setAttribute(TASK_ATTRIBUTE, taskId);
        request.setAttribute(ACTION_ATTRIBUTE, actionHeader);
        // 后续操作日志只读取服务端确认后的属性，不直接信任原始请求头。
        return true;
    }

    private AiAgentUserContext currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.getUserId() == null) {
            return null;
        }
        Set<String> permissions = loginUser.getPermissions() == null
                ? Set.of() : Set.copyOf(loginUser.getPermissions());
        return new AiAgentUserContext(loginUser.getUserId(), permissions);
    }
}
