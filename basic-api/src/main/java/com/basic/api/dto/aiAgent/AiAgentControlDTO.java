package com.basic.api.dto.aiAgent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 暂停、终止和确认操作的标签页绑定参数。
 */
@Data
public class AiAgentControlDTO {

    /**
     * 发起控制操作的标签页实例，防止其他已登录页面接管或确认当前任务。
     */
    @NotBlank
    @Size(max = 64)
    private String clientInstanceId;
}
