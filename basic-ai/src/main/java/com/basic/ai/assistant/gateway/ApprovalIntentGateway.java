package com.basic.ai.assistant.gateway;

import com.basic.ai.assistant.model.ApprovalIntent;
import reactor.core.publisher.Mono;

/**
 * 无工具审批意图分类边界。
 */
public interface ApprovalIntentGateway {

    /**
     * 对本地规则无法确定的回复做保守分类。
     *
     * @param safePendingSummary 不含正文、目标明细和内部标识的待审批摘要
     * @param userMessage 用户本次回复，作为不可信数据交给分类模型
     * @return 分类失败或结果含糊时由实现返回 {@link ApprovalIntent#UNKNOWN}
     */
    Mono<ApprovalIntent> classify(String safePendingSummary, String userMessage);
}
