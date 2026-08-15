package com.basic.api.dto.aiAgent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建网页代理任务请求，身份和权限只能从服务端登录态获取。
 */
@Data
public class AiAgentTaskCreateDTO {

    /**
     * 用户希望完成的目标，服务端会在持久化和模型交互前按需要转为脱敏摘要。
     */
    @NotBlank
    @Size(max = 1000)
    private String goal;

    /**
     * 创建任务的浏览器实例，作为后续事件接收、认领和结果上报的绑定键。
     */
    @NotBlank
    @Size(max = 64)
    private String clientInstanceId;

    /**
     * 创建时的可信页面快照，限定首个模型决策可见的状态和动作范围。
     */
    @NotNull
    @Valid
    private AiAgentPageSnapshotDTO snapshot;
}
