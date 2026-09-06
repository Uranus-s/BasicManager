package com.basic.sericve.ai.assistant.approval;

import com.basic.ai.assistant.gateway.ApprovalIntentGateway;
import com.basic.ai.assistant.model.ApprovalIntent;
import com.basic.sericve.ai.assistant.action.model.PendingAssistantAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 通用 Action 自然语言审批路由器，含糊或分类失败时绝不执行写操作。
 */
@Component
@RequiredArgsConstructor
public class ActionApprovalRouter {

    private static final List<String> REVISE_WORDS = List.of(
            "改", "调整", "换成", "但", "不过", "只发给");
    private static final List<String> CANCEL_WORDS = List.of(
            "取消", "算了", "不要", "别", "不可以", "不能", "暂不", "先不");
    private static final List<String> CONFIRM_WORDS = List.of(
            "确认", "执行吧", "可以执行", "同意执行", "发布吧", "可以发布");
    private static final List<String> QUESTION_WORDS = List.of(
            "吗", "么", "是否", "能否", "怎么", "如何", "多久", "什么", "？", "?");

    private final ApprovalIntentGateway remoteClassifier;

    /** 按修改、问句、取消、确认的保守优先级识别用户回复。 */
    public Mono<ApprovalIntent> classify(String message, PendingAssistantAction pendingAction) {
        if (!StringUtils.hasText(message) || pendingAction == null || pendingAction.preview() == null) {
            //不清楚
            return Mono.just(ApprovalIntent.UNKNOWN);
        }
        String normalized = message.trim();
        if (containsAny(normalized, REVISE_WORDS)) {
            //修改
            return Mono.just(ApprovalIntent.REVISE);
        }
        if (containsAny(normalized, QUESTION_WORDS)) {
            //问句
            return Mono.just(ApprovalIntent.UNKNOWN);
        }
        if (containsAny(normalized, CANCEL_WORDS)) {
            //取消
            return Mono.just(ApprovalIntent.CANCEL);
        }
        if (containsAny(normalized, CONFIRM_WORDS)) {
            //确认
            return Mono.just(ApprovalIntent.CONFIRM);
        }
        //大模型判断意图
        return Mono.defer(() -> remoteClassifier.classify(
                        pendingAction.preview().summary(), normalized))
                .defaultIfEmpty(ApprovalIntent.UNKNOWN)
                .onErrorReturn(ApprovalIntent.UNKNOWN);
    }

    private static boolean containsAny(String message, List<String> words) {
        return words.stream().anyMatch(message::contains);
    }
}
