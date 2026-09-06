package com.basic.sericve.ai.assistant.operation;

import com.basic.sericve.ai.assistant.action.model.ActionExecutionResult;
import com.basic.sericve.ai.assistant.action.model.ActionPreview;
import com.basic.sericve.ai.assistant.action.model.ActionPreviewField;
import com.basic.sericve.ai.assistant.action.model.AssistantProposalResult;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 需要自然语言确认的写操作定义，统一描述快照准备、确认预览、业务复验和最终执行。
 *
 * <p>模型调用该操作时只能根据输入 {@code I} 创建待确认快照 {@code P}，不能直接触发写入。
 * 快照持久化后将作为预览和最终执行的唯一数据来源，避免用户确认后的新对话内容改变已确认
 * 的操作语义。</p>
 *
 * <p>Spec 构建完成后不可变，可以安全地注册到 Operation Registry 并在多次助手运行间复用。</p>
 *
 * @param <I> 模型工具接收的原始输入类型
 * @param <P> 待确认操作冻结并持久化的快照类型
 */
public final class AssistantActionSpec<I, P>
        implements AssistantOperationSpec<I, AssistantProposalResult> {

    /** 限制审批摘要体积，避免过长业务字段进入事件和重新规划上下文。 */
    private static final int MAX_SUMMARY_LENGTH = 1000;

    /** 未配置业务校验器时使用的无操作实现，统一后续调用路径。 */
    private static final Consumer<Object> NO_VALIDATION = ignored -> { };

    /** 暴露给模型的工具元数据。 */
    private final String toolName;
    private final String description;
    private final Class<I> inputType;

    /** 用于注册、持久化和确认路由的稳定操作类型。 */
    private final String actionType;

    /** 用于反序列化已持久化快照的运行时类型。 */
    private final Class<P> payloadType;
    private final Set<String> requiredPermissions;

    /** 写操作从提议到执行各阶段的业务函数。 */
    private final Function<I, P> prepare;
    private final Consumer<P> validator;
    private final String previewTitle;
    private final Function<P, List<ActionPreviewField>> fieldsFactory;
    private final Function<P, String> contentFactory;
    private final Function<P, ActionExecutionResult> executor;

    /**
     * 校验构建参数并冻结集合，确保注册后的定义不会被外部修改。
     *
     * <p>{@link #NO_VALIDATION} 不读取参数，转换为 {@code Consumer<P>} 不会造成类型风险。</p>
     */
    @SuppressWarnings("unchecked")
    private AssistantActionSpec(Builder<I, P> builder) {
        this.toolName = AssistantQuerySpec.requireText(builder.toolName, "工具名称不能为空");
        this.description = AssistantQuerySpec.requireText(builder.description, "工具描述不能为空");
        this.inputType = Objects.requireNonNull(builder.inputType, "输入类型不能为空");
        this.actionType = AssistantQuerySpec.requireText(builder.actionType, "Action Type 不能为空");
        this.payloadType = Objects.requireNonNull(builder.payloadType, "快照类型不能为空");
        this.requiredPermissions = Set.copyOf(builder.requiredPermissions);
        if (requiredPermissions.isEmpty()) {
            throw new IllegalStateException("写操作权限不能为空：" + toolName);
        }
        this.prepare = Objects.requireNonNull(builder.prepare, "快照准备函数不能为空：" + toolName);
        this.validator = builder.validator == null
                ? (Consumer<P>) NO_VALIDATION : builder.validator;
        this.previewTitle = AssistantQuerySpec.requireText(builder.previewTitle, "预览标题不能为空");
        this.fieldsFactory = Objects.requireNonNull(builder.fieldsFactory, "预览字段函数不能为空：" + toolName);
        this.contentFactory = builder.contentFactory == null ? ignored -> null : builder.contentFactory;
        this.executor = Objects.requireNonNull(builder.executor, "执行函数不能为空：" + toolName);
    }

    static <I, P> Builder<I, P> builder(String toolName, Class<I> inputType, Class<P> payloadType) {
        return new Builder<>(toolName, inputType, payloadType);
    }

    @Override
    public String toolName() {
        return toolName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Class<I> inputType() {
        return inputType;
    }

    public String actionType() {
        return actionType;
    }

    /** 返回持久化快照的类型，供确认阶段进行安全反序列化。 */
    public Class<P> payloadType() {
        return payloadType;
    }

    @Override
    public Set<String> requiredPermissions() {
        return requiredPermissions;
    }

    /**
     * 根据模型输入准备待确认快照。
     *
     * <p>准备函数可读取当前业务数据并将执行所需信息冻结到快照中，但不应执行最终写操作。</p>
     */
    public P prepare(I input) {
        return prepare.apply(input);
    }

    /**
     * 执行业务约束校验。
     *
     * <p>该校验会在创建提议和用户确认时分别调用，确认阶段复验可识别等待期间发生的
     * 状态变化。未配置校验器时此方法为空操作。</p>
     */
    public void validate(P payload) {
        validator.accept(payload);
    }

    /**
     * 仅从冻结快照生成允许向用户展示的安全预览。
     *
     * <p>字段工厂返回 {@code null} 时按空字段处理；返回的字段列表会复制为不可变列表，
     * 防止预览创建后被业务代码修改。长正文与短字段分开保存，便于客户端分别展示。</p>
     */
    public ActionPreview buildPreview(P payload) {
        List<ActionPreviewField> fields = fieldsFactory.apply(payload);
        List<ActionPreviewField> safeFields = fields == null ? List.of() : List.copyOf(fields);
        return new ActionPreview(previewTitle, summary(previewTitle, safeFields),
                safeFields, contentFactory.apply(payload));
    }

    /** 用户确认并通过复验后，使用原始冻结快照执行最终业务写入。 */
    public ActionExecutionResult execute(P payload) {
        return executor.apply(payload);
    }

    /**
     * 将标题和短字段拼接为审批摘要，并硬性限制最大长度。
     *
     * <p>缺少标签的字段不进入摘要，空值按空字符串展示；达到长度上限后立即截断，
     * 后续字段不再追加。</p>
     */
    private static String summary(String title, List<ActionPreviewField> fields) {
        StringBuilder summary = new StringBuilder(title);
        for (ActionPreviewField field : fields) {
            if (field == null || field.label() == null) {
                continue;
            }
            summary.append('；').append(field.label()).append('：')
                    .append(field.value() == null ? "" : field.value());
            if (summary.length() >= MAX_SUMMARY_LENGTH) {
                return summary.substring(0, MAX_SUMMARY_LENGTH);
            }
        }
        return summary.toString();
    }

    /**
     * Action Spec 的类型安全构建器。
     *
     * <p>除 {@link #content(Function)} 和 {@link #validate(Consumer)} 外，其余业务配置均为
     * 必填项；缺失配置会在 {@link #build()} 时快速失败。</p>
     */
    public static final class Builder<I, P> {

        /** 创建构建器时即可确定、后续不可变的类型元数据。 */
        private final String toolName;
        private final Class<I> inputType;
        private final Class<P> payloadType;

        /** 在构建阶段按声明顺序收集权限并去重。 */
        private final Set<String> requiredPermissions = new java.util.LinkedHashSet<>();

        /** 由链式方法逐步补齐的写操作定义。 */
        private String description;
        private String actionType;
        private Function<I, P> prepare;
        private Consumer<P> validator;
        private String previewTitle;
        private Function<P, List<ActionPreviewField>> fieldsFactory;
        private Function<P, String> contentFactory;
        private Function<P, ActionExecutionResult> executor;

        private Builder(String toolName, Class<I> inputType, Class<P> payloadType) {
            this.toolName = toolName;
            this.inputType = inputType;
            this.payloadType = payloadType;
        }

        /** 设置持久化和确认路由使用的稳定操作类型。 */
        public Builder<I, P> actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        /** 设置提供给模型理解工具用途的描述。 */
        public Builder<I, P> description(String description) {
            this.description = description;
            return this;
        }

        /**
         * 追加调用该写操作所需的权限标识。
         *
         * <p>空白权限会被忽略，重复权限会被去重；写操作最终至少需要一个有效权限。</p>
         */
        public Builder<I, P> permissions(String... permissions) {
            AssistantQuerySpec.addPermissions(requiredPermissions, permissions);
            return this;
        }

        /**
         * 设置快照准备函数，用于将模型输入转换为可持久化、可复验的执行数据。
         */
        public Builder<I, P> prepare(Function<I, P> prepare) {
            this.prepare = prepare;
            return this;
        }

        /** 设置预览标题以及从快照中显式选择短展示字段的函数。 */
        public Builder<I, P> preview(String title,
                                     Function<P, List<ActionPreviewField>> fieldsFactory) {
            this.previewTitle = title;
            this.fieldsFactory = fieldsFactory;
            return this;
        }

        /** 设置可选的长文本正文提取函数；未设置时预览正文为 {@code null}。 */
        public Builder<I, P> content(Function<P, String> contentFactory) {
            this.contentFactory = contentFactory;
            return this;
        }

        /** 设置提议和确认阶段都会执行的业务复验函数。 */
        public Builder<I, P> validate(Consumer<P> validator) {
            this.validator = validator;
            return this;
        }

        /** 设置用户确认后执行最终业务写入的函数。 */
        public Builder<I, P> execute(Function<P, ActionExecutionResult> executor) {
            this.executor = executor;
            return this;
        }

        /** 构建并校验不可变的写操作定义。 */
        public AssistantActionSpec<I, P> build() {
            return new AssistantActionSpec<>(this);
        }
    }
}
