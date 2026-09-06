package com.basic.sericve.ai.assistant.operation;

import com.basic.sericve.ai.assistant.action.model.ActionExecutionResult;
import com.basic.sericve.ai.assistant.action.model.ActionPreviewField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 助手操作定义的统一工厂入口。
 *
 * <p>集中创建查询、写操作构建器以及审批预览和执行结果，避免业务注册器直接依赖
 * Spec 的包级构建方法。写操作预览字段必须由业务显式选择，防止快照中的敏感数据
 * 被默认暴露给客户端。</p>
 */
public final class AssistantOperations {

    /** 工具类不允许实例化。 */
    private AssistantOperations() {
    }

    /**
     * 创建需要模型输入的只读查询构建器。
     *
     * @param toolName 提供给模型调用的工具名称
     * @param inputType 模型输入参数的运行时类型
     * @param outputType 查询结果的运行时类型
     * @param <I> 输入参数类型
     * @param <O> 查询结果类型
     * @return 有输入查询的类型安全构建器
     */
    public static <I, O> AssistantQuerySpec.Builder<I, O> query(
            String toolName, Class<I> inputType, Class<?> outputType) {
        return AssistantQuerySpec.withInput(toolName, inputType, outputType);
    }

    /**
     * 创建不需要模型输入的只读查询构建器。
     *
     * @param toolName 提供给模型调用的工具名称
     * @param outputType 查询结果的运行时类型
     * @param <O> 查询结果类型
     * @return 无输入查询的类型安全构建器
     */
    public static <O> AssistantQuerySpec.Builder<Void, O> query(
            String toolName, Class<?> outputType) {
        return AssistantQuerySpec.withoutInput(toolName, outputType);
    }

    /**
     * 创建模型输入与待确认快照类型相同的写操作构建器。
     *
     * <p>该便捷重载默认使用原始输入作为冻结快照，适用于输入已包含最终执行所需全部数据、
     * 无需额外查询或转换的场景。</p>
     *
     * @param toolName 提供给模型调用的工具名称
     * @param payloadType 输入及持久化快照的运行时类型
     * @param <P> 输入和待确认快照类型
     * @return 已配置恒等快照准备函数的写操作构建器
     */
    public static <P> AssistantActionSpec.Builder<P, P> action(
            String toolName, Class<P> payloadType) {
        return AssistantActionSpec.<P, P>builder(toolName, payloadType, payloadType)
                .prepare(Function.identity());
    }

    /**
     * 创建模型输入与待确认快照类型不同的写操作构建器。
     *
     * <p>调用方必须继续配置快照准备函数，将模型输入转换为可持久化、可复验的执行数据。</p>
     *
     * @param toolName 提供给模型调用的工具名称
     * @param inputType 模型输入参数的运行时类型
     * @param payloadType 持久化快照的运行时类型
     * @param <I> 模型输入类型
     * @param <P> 待确认快照类型
     * @return 尚未配置快照准备函数的写操作构建器
     */
    public static <I, P> AssistantActionSpec.Builder<I, P> action(
            String toolName, Class<I> inputType, Class<P> payloadType) {
        return AssistantActionSpec.builder(toolName, inputType, payloadType);
    }

    /**
     * 按“标签、值”成对创建不可变的预览字段列表。
     *
     * <p>业务必须显式选择允许展示的数据；标签会去除首尾空白且不能为空，值为
     * {@code null} 时转换为空字符串。参数数量不是偶数时立即拒绝，避免标签和值错位。</p>
     *
     * @param labelValuePairs 依次排列的字段标签和值
     * @return 保持传入顺序的不可变预览字段列表
     */
    public static List<ActionPreviewField> fields(Object... labelValuePairs) {
        if (labelValuePairs == null || labelValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("预览字段必须按标签和值成对提供");
        }
        List<ActionPreviewField> fields = new ArrayList<>(labelValuePairs.length / 2);
        for (int index = 0; index < labelValuePairs.length; index += 2) {
            String label = Objects.toString(labelValuePairs[index], "").trim();
            if (label.isEmpty()) {
                throw new IllegalArgumentException("预览字段标签不能为空");
            }
            fields.add(new ActionPreviewField(label,
                    Objects.toString(labelValuePairs[index + 1], "")));
        }
        return List.copyOf(fields);
    }

    /**
     * 创建写操作成功后的标准执行结果。
     *
     * @param resultId 写操作产生或影响的业务数据 ID
     * @param reply 确认执行后回复用户的文本
     * @return Action 执行结果
     */
    public static ActionExecutionResult result(Long resultId, String reply) {
        return new ActionExecutionResult(resultId, reply);
    }
}
