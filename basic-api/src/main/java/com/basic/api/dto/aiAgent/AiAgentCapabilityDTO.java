package com.basic.api.dto.aiAgent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 页面公开的单个语义动作能力，不接受脚本或任意选择器。
 */
@Data
public class AiAgentCapabilityDTO {

    /**
     * 客户端预置执行器支持的动作名称，不用于传入脚本或自定义命令。
     */
    @NotBlank
    @Size(max = 32)
    private String actionType;

    /**
     * 页面向模型公开的业务语义目标，避免暴露或接收底层选择器。
     */
    @NotBlank
    @Size(max = 160)
    private String target;

    /**
     * 该动作参数的结构约束，服务端将其作为模型输出的边界而非待执行内容。
     */
    @NotNull
    private Map<String, Object> parameterSchema;
}
