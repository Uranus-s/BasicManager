package com.basic.sericve.ai.assistant.operation;

import com.basic.sericve.ai.assistant.AssistantRequestContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 只读助手工具定义，支持有输入和无输入两种调用形式。
 *
 * <p>有输入查询通过 {@link Function} 执行，无输入查询通过 {@link Supplier} 执行；
 * 构建完成后，两种执行方式互斥且定义内容不可变。</p>
 *
 * @param <I> 模型传入的查询参数类型；无输入查询固定为 {@link Void}
 * @param <O> 查询结果类型
 */
public final class AssistantQuerySpec<I, O> implements AssistantOperationSpec<I, O> {

    /** 暴露给模型的工具名称、用途描述和输入输出类型。 */
    private final String toolName;
    private final String description;
    private final Class<I> inputType;
    private final Class<?> outputType;

    /** 调用查询工具所需的权限标识，构建后不可变。 */
    private final Set<String> requiredPermissions;

    /** 标识当前查询是否不接收模型输入，用于约束执行函数和调用入口。 */
    private final boolean noInput;

    /** 标识当前查询是否依赖本次请求的用户上下文。 */
    private final boolean contextRequired;

    /** 两种查询形式对应的执行函数，任一实例只会配置其中一个。 */
    private final Function<I, O> function;
    private final Supplier<O> supplier;
    private final BiFunction<I, AssistantRequestContext, O> contextFunction;
    private final Function<AssistantRequestContext, O> contextSupplier;

    /**
     * 校验构建参数并冻结权限集合，确保注册后的查询定义不会被外部修改。
     */
    private AssistantQuerySpec(Builder<I, O> builder) {
        this.toolName = requireText(builder.toolName, "工具名称不能为空");
        this.description = requireText(builder.description, "工具描述不能为空");
        this.inputType = java.util.Objects.requireNonNull(builder.inputType, "输入类型不能为空");
        this.outputType = java.util.Objects.requireNonNull(builder.outputType, "输出类型不能为空");
        this.requiredPermissions = Set.copyOf(builder.requiredPermissions);
        this.noInput = builder.noInput;
        this.contextRequired = builder.contextRequired;
        this.function = builder.function;
        this.supplier = builder.supplier;
        this.contextFunction = builder.contextFunction;
        this.contextSupplier = builder.contextSupplier;
        boolean configured = contextRequired
                ? (noInput ? contextSupplier != null : contextFunction != null)
                : (noInput ? supplier != null : function != null);
        if (!configured) {
            throw new IllegalStateException("查询工具执行函数不能为空：" + toolName);
        }
    }

    /** 创建需要模型输入的查询构建器。 */
    static <I, O> Builder<I, O> withInput(String toolName, Class<I> inputType, Class<?> outputType) {
        return new Builder<>(toolName, inputType, outputType, false);
    }

    /** 创建不需要模型输入的查询构建器，并以 {@link Void} 作为统一输入类型。 */
    static <O> Builder<Void, O> withoutInput(String toolName, Class<?> outputType) {
        return new Builder<>(toolName, Void.class, outputType, true);
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

    /** 返回查询结果的运行时类型，供 Spring AI 构建工具返回值定义。 */
    public Class<?> outputType() {
        return outputType;
    }

    @Override
    public Set<String> requiredPermissions() {
        return requiredPermissions;
    }

    /** 返回当前查询是否采用无输入调用形式。 */
    public boolean noInput() {
        return noInput;
    }

    /** 返回当前查询是否必须使用请求上下文执行。 */
    public boolean contextRequired() {
        return contextRequired;
    }

    /**
     * 使用模型输入执行查询。
     *
     * @throws IllegalStateException 当前定义为无输入查询时抛出
     */
    public O execute(I input) {
        if (contextRequired) {
            throw new IllegalStateException("上下文查询必须使用 AssistantRequestContext 执行");
        }
        if (noInput) {
            throw new IllegalStateException("无输入查询不能使用输入参数执行");
        }
        return function.apply(input);
    }

    /**
     * 不传入模型参数执行查询。
     *
     * @throws IllegalStateException 当前定义为有输入查询时抛出
     */
    public O execute() {
        if (contextRequired) {
            throw new IllegalStateException("上下文查询必须使用 AssistantRequestContext 执行");
        }
        if (!noInput) {
            throw new IllegalStateException("有输入查询不能按无参数形式执行");
        }
        return supplier.get();
    }

    /** 使用模型输入和请求上下文执行有输入查询。 */
    public O executeWithContext(I input, AssistantRequestContext context) {
        if (!contextRequired) {
            throw new IllegalStateException("旧查询不能使用上下文执行");
        }
        java.util.Objects.requireNonNull(context, "请求上下文不能为空");
        if (noInput) {
            throw new IllegalStateException("无输入查询不能使用输入参数执行");
        }
        return contextFunction.apply(input, context);
    }

    /** 使用请求上下文执行无输入查询。 */
    public O executeWithContext(AssistantRequestContext context) {
        if (!contextRequired) {
            throw new IllegalStateException("旧查询不能使用上下文执行");
        }
        java.util.Objects.requireNonNull(context, "请求上下文不能为空");
        if (!noInput) {
            throw new IllegalStateException("有输入查询必须提供输入参数执行");
        }
        return contextSupplier.apply(context);
    }

    /**
     * Query Spec 的类型安全构建器，在构建阶段约束输入类型与执行函数形式。
     *
     * @param <I> 模型传入的查询参数类型
     * @param <O> 查询结果类型
     */
    public static final class Builder<I, O> {

        /** 创建构建器时确定、后续不可变的工具类型元数据和调用形式。 */
        private final String toolName;
        private final Class<I> inputType;
        private final Class<?> outputType;
        private final boolean noInput;

        /** 在构建阶段按声明顺序收集权限并去重。 */
        private final Set<String> requiredPermissions = new LinkedHashSet<>();

        /** 由链式方法补齐的工具描述和执行函数。 */
        private String description;
        private Function<I, O> function;
        private Supplier<O> supplier;
        private boolean contextRequired;
        private BiFunction<I, AssistantRequestContext, O> contextFunction;
        private Function<AssistantRequestContext, O> contextSupplier;

        private Builder(String toolName, Class<I> inputType, Class<?> outputType, boolean noInput) {
            this.toolName = toolName;
            this.inputType = inputType;
            this.outputType = outputType;
            this.noInput = noInput;
        }

        /** 设置提供给模型理解查询用途的工具描述。 */
        public Builder<I, O> description(String description) {
            this.description = description;
            return this;
        }

        /**
         * 追加调用该查询所需的权限标识。
         *
         * <p>空数组、空白权限会被忽略，重复权限只保留一份。</p>
         */
        public Builder<I, O> permissions(String... permissions) {
            addPermissions(requiredPermissions, permissions);
            return this;
        }

        /** 设置有输入查询的执行函数。 */
        public Builder<I, O> execute(Function<I, O> function) {
            if (noInput) {
                throw new IllegalStateException("无输入查询必须使用 Supplier");
            }
            this.function = function;
            return this;
        }

        /** 设置无输入查询的执行函数。 */
        public Builder<I, O> execute(Supplier<O> supplier) {
            if (!noInput) {
                throw new IllegalStateException("有输入查询必须使用 Function");
            }
            this.supplier = supplier;
            return this;
        }

        /** 设置需要请求上下文的有输入查询执行函数。 */
        public Builder<I, O> executeWithContext(BiFunction<I, AssistantRequestContext, O> function) {
            if (noInput) {
                throw new IllegalStateException("无输入查询必须使用 Function");
            }
            this.contextRequired = true;
            this.contextFunction = function;
            return this;
        }

        /** 设置需要请求上下文的无输入查询执行函数。 */
        public Builder<I, O> executeWithContext(Function<AssistantRequestContext, O> function) {
            if (!noInput) {
                throw new IllegalStateException("有输入查询必须使用 BiFunction");
            }
            this.contextRequired = true;
            this.contextSupplier = function;
            return this;
        }

        /** 构建并校验不可变的只读查询定义。 */
        public AssistantQuerySpec<I, O> build() {
            return new AssistantQuerySpec<>(this);
        }
    }

    /** 将非空权限标识去除首尾空白后追加到目标集合。 */
    static void addPermissions(Set<String> target, String... permissions) {
        if (permissions == null) {
            return;
        }
        Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .forEach(target::add);
    }

    /** 校验必填文本并返回去除首尾空白后的值。 */
    static String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
        return value.trim();
    }
}
