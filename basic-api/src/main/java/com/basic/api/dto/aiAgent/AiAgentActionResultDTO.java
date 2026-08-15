package com.basic.api.dto.aiAgent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 浏览器标签页上报的动作结果；状态只允许成功或失败。
 */
@Data
public class AiAgentActionResultDTO {

    /**
     * 上报结果的标签页实例，服务端据此拒绝非任务绑定页面的结果回写。
     */
    @NotBlank
    @Size(max = 64)
    private String clientInstanceId;

    /**
     * 客户端实际执行结论；仅接受终态，不能通过该字段伪造等待或确认状态。
     */
    @NotBlank
    @Pattern(regexp = "SUCCEEDED|FAILED")
    private String status;

    /**
     * 客户端执行结果的脱敏摘要；失败时可为空，不能携带原始页面内容、凭据或服务端不可见数据。
     */
    @Size(max = 1000)
    private String resultSummary;

    /**
     * 动作执行前浏览器实际观察到的路由；无法读取时允许为空，由服务端暂停旧动作。
     */
    @Size(max = 100)
    private String executionRouteName;

    /**
     * 动作执行前浏览器实际观察到的页面版本，与动作生成版本比对以阻止过期执行。
     */
    @Size(max = 128)
    private String executionPageVersion;

    /**
     * 动作完成后采集的脱敏页面快照，是后续规划而非执行前历史页面的可信输入。
     */
    @NotNull
    @Valid
    private AiAgentPageSnapshotDTO snapshot;
}
