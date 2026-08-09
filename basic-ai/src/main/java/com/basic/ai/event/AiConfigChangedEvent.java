package com.basic.ai.event;

import org.springframework.context.ApplicationEvent;

/**
 * AI 配置变更事件，用于在事务提交后使已缓存的模型客户端失效。
 */
public final class AiConfigChangedEvent extends ApplicationEvent {

    public AiConfigChangedEvent(Object source) {
        super(source);
    }
}
