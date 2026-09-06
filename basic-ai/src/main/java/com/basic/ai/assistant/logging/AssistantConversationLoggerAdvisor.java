package com.basic.ai.assistant.logging;

import com.basic.ai.assistant.runtime.AssistantRunContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 记录统一 AI 助手运行中每次真实模型请求和聚合响应。
 *
 * <p>该 Advisor 位于工具循环内部，只处理携带 {@link AssistantRunContext} 的聊天请求。
 * 审批意图分类和连接测试会直接透传，不产生完整会话日志。</p>
 */
@Component
@RequiredArgsConstructor
public final class AssistantConversationLoggerAdvisor implements StreamAdvisor {

    private static final ChatClientMessageAggregator RESPONSE_AGGREGATOR =
            new ChatClientMessageAggregator();

    private final AssistantConversationLogger conversationLogger;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request,
                                                  StreamAdvisorChain advisorChain) {
        AssistantRunContext context = resolveRunContext(request);
        if (context == null) {
            return advisorChain.nextStream(request);
        }
        return Flux.defer(() -> {
            // defer 保证轮次在真实订阅时分配，未订阅的 SSE 不消耗编号。
            int round = context.nextModelRound();
            conversationLogger.logModelRequest(context.getRunId(), round, request.prompt());
            Flux<ChatClientResponse> response = advisorChain.nextStream(request);
            return RESPONSE_AGGREGATOR.aggregateChatClientResponse(
                            response,
                            aggregated -> conversationLogger.logModelResponse(
                                    context.getRunId(), round, aggregated.chatResponse()))
                    .doOnError(error -> conversationLogger.logModelFailure(
                            context.getRunId(), round, error));
        });
    }

    @Override
    public String getName() {
        return AssistantConversationLoggerAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        // ToolCallingAdvisor 复制其后续链发起递归调用，因此日志 Advisor 必须排在其后。
        return ToolCallingAdvisor.DEFAULT_ORDER + 100;
    }

    private static AssistantRunContext resolveRunContext(ChatClientRequest request) {
        if (!(request.prompt().getOptions() instanceof ToolCallingChatOptions options)) {
            return null;
        }
        Map<String, Object> toolContext = options.getToolContext();
        if (toolContext == null) {
            return null;
        }
        Object context = toolContext.get(AssistantRunContext.TOOL_CONTEXT_KEY);
        return context instanceof AssistantRunContext assistantRunContext ? assistantRunContext : null;
    }
}
