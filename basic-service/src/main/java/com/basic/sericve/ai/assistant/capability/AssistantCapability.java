package com.basic.sericve.ai.assistant.capability;

import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;

import java.util.List;

/**
 * AI 助手业务能力扩展点，每个实现只声明稳定的查询和写操作。
 */
public interface AssistantCapability {

    /** 返回全局唯一且稳定的能力标识。 */
    String capabilityId();

    /**
     * 返回不读取登录态和请求状态的不可变操作定义，权限过滤由公共工具工厂完成。
     */
    List<AssistantOperationSpec<?, ?>> operations();
}
