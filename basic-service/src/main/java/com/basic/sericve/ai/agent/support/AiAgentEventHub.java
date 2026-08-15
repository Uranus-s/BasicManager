package com.basic.sericve.ai.agent.support;

import com.basic.api.vo.aiAgent.AiAgentEventVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务级进程内 SSE 事件中心，仅重放最近 20 条事件以支持短暂断线后的状态同步。
 * 数据库仍是任务状态的事实来源；进程重启或跨实例恢复不能依赖这里的内存事件。
 */
@Component
public class AiAgentEventHub {

    private static final int REPLAY_LIMIT = 20;
    private static final Duration TERMINAL_REPLAY_TTL = Duration.ofMinutes(5);
    private final Map<Long, Sinks.Many<AiAgentEventVO>> sinks = new ConcurrentHashMap<>();
    private final Map<Long, AiAgentEventVO> terminalEvents = new ConcurrentHashMap<>();

    public Flux<AiAgentEventVO> flux(Long taskId) {
        return Flux.defer(() -> {
            // 前后两次终态检查封住“检查终态”和“创建 Sink”之间完成任务的竞态窗口。
            AiAgentEventVO terminal = terminalEvents.get(taskId);
            if (terminal != null) {
                return Flux.just(terminal);
            }
            Sinks.Many<AiAgentEventVO> current = sink(taskId);
            terminal = terminalEvents.get(taskId);
            if (terminal != null) {
                sinks.remove(taskId, current);
                return Flux.just(terminal);
            }
            return current.asFlux();
        });
    }

    public void emit(Long taskId, AiAgentEventVO event) {
        Sinks.EmitResult result = sink(taskId).tryEmitNext(event);
        // 暂无订阅者是页面尚未连上或正在重连的正常情况，回放 Sink 会保留事件；其他失败需要暴露。
        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
    }

    public void complete(Long taskId, AiAgentEventVO terminalEvent) {
        // 先缓存终态再移除活动 Sink，保证并发新订阅者至少能从终态缓存读取最终结果。
        terminalEvents.put(taskId, terminalEvent);
        Sinks.Many<AiAgentEventVO> sink = sinks.remove(taskId);
        if (sink != null) {
            sink.tryEmitNext(terminalEvent);
            sink.tryEmitComplete();
        }
        Mono.delay(TERMINAL_REPLAY_TTL)
                // 仅删除仍是同一对象的终态，避免延迟清理误删后来写入的新终态。
                .subscribe(ignored -> terminalEvents.remove(taskId, terminalEvent));
    }

    private Sinks.Many<AiAgentEventVO> sink(Long taskId) {
        return sinks.computeIfAbsent(taskId,
                ignored -> Sinks.many().replay().limit(REPLAY_LIMIT));
    }
}
