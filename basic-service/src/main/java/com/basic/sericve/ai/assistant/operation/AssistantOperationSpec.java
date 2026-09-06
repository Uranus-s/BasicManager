package com.basic.sericve.ai.assistant.operation;

import java.util.Set;

/** AI 助手查询和写操作共享的不可变元数据。 */
public sealed interface AssistantOperationSpec<I, O>
        permits AssistantQuerySpec, AssistantActionSpec {

    String toolName();

    String description();

    Class<I> inputType();

    Set<String> requiredPermissions();
}
