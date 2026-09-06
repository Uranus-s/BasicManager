package com.basic.sericve.ai.assistant.operation;

import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 助手操作注册表，负责汇总各业务能力声明的查询和写操作。
 *
 * <p>注册表在应用启动时完成定义校验和索引构建，确保能力标识、模型工具名称以及
 * 写操作的 Action Type 全局唯一。注册完成后的集合均为不可变集合，可供后续请求安全复用。</p>
 */
@Component
public class AssistantOperationRegistry {

    /** 全部已注册操作，保留 Spring 注入能力及其内部定义的声明顺序。 */
    private final List<AssistantOperationSpec<?, ?>> operations;

    /** 按稳定 Action Type 索引的写操作定义，用于待确认操作的恢复和执行路由。 */
    private final Map<String, RegisteredAssistantActionSpec<?, ?>> actions;

    /**
     * 收集所有业务能力并建立操作注册表。
     *
     * <p>构造阶段采用快速失败策略：任何空白或重复的能力标识、重复的工具名称、
     * 重复的 Action Type 都会阻止应用启动，避免运行期间发生工具覆盖或错误路由。</p>
     *
     * @param capabilities Spring 注入的业务能力列表；无可用能力时允许为空
     * @throws IllegalStateException 能力或操作定义违反全局唯一性约束时抛出
     */
    public AssistantOperationRegistry(List<AssistantCapability> capabilities) {
        List<AssistantOperationSpec<?, ?>> collected = new ArrayList<>();
        Map<String, RegisteredAssistantActionSpec<?, ?>> indexedActions = new LinkedHashMap<>();
        Set<String> capabilityIds = new HashSet<>();
        Set<String> toolNames = new HashSet<>();

        if (capabilities != null) {
            for (AssistantCapability capability : capabilities) {
                // 能力标识会随写操作一同持久化和审计，因此必须稳定且全局唯一。
                String capabilityId = capability.capabilityId();
                if (!StringUtils.hasText(capabilityId) || !capabilityIds.add(capabilityId)) {
                    throw new IllegalStateException("AI 助手能力标识为空或重复：" + capabilityId);
                }
                List<AssistantOperationSpec<?, ?>> definitions = capability.operations();
                if (definitions == null) {
                    continue;
                }
                for (AssistantOperationSpec<?, ?> operation : definitions) {
                    if (operation == null || !toolNames.add(operation.toolName())) {
                        throw new IllegalStateException("AI 助手工具为空或名称重复："
                                + (operation == null ? null : operation.toolName()));
                    }
                    collected.add(operation);
                    if (operation instanceof AssistantActionSpec<?, ?> action) {
                        // 只有写操作需要按 Action Type 建立索引，查询操作直接通过工具列表暴露。
                        RegisteredAssistantActionSpec<?, ?> registered =
                                new RegisteredAssistantActionSpec<>(capabilityId, action);
                        if (indexedActions.putIfAbsent(action.actionType(), registered) != null) {
                            throw new IllegalStateException("AI Action 类型重复：" + action.actionType());
                        }
                    }
                }
            }
        }
        // 冻结注册结果，避免运行期间被能力实现或调用方意外修改。
        this.operations = List.copyOf(collected);
        this.actions = Map.copyOf(indexedActions);
    }

    /**
     * 返回当前用户有权调用的操作定义。
     *
     * <p>用户必须同时具备操作声明的全部权限；权限集合为空时，仅保留无需权限的操作。</p>
     *
     * @param permissions 当前用户拥有的权限标识；传入 {@code null} 时按空权限集合处理
     * @return 按注册顺序排列的不可变操作列表
     */
    public List<AssistantOperationSpec<?, ?>> permittedOperations(Set<String> permissions) {
        Set<String> actual = permissions == null ? Set.of() : permissions;
        return operations.stream()
                .filter(operation -> actual.containsAll(operation.requiredPermissions()))
                .toList();
    }

    /**
     * 按持久化记录中的稳定 Action Type 查找写操作定义。
     *
     * @param actionType 待确认操作保存的 Action Type
     * @return 同时包含能力归属和具体 Spec 的注册信息
     * @throws BusinessException Action Type 未注册或已失效时抛出
     */
    public RegisteredAssistantActionSpec<?, ?> requireAction(String actionType) {
        RegisteredAssistantActionSpec<?, ?> registered = actions.get(actionType);
        if (registered == null) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }
        return registered;
    }
}
