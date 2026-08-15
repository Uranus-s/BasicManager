package com.basic.api.dto.aiAgent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 当前标签页提供的脱敏页面快照及语义能力集合。
 */
@Data
public class AiAgentPageSnapshotDTO {

    /**
     * 浏览器当前业务路由，用于判断任务和待执行动作是否仍适用于此页面。
     */
    @NotBlank
    @Size(max = 100)
    private String routeName;

    /**
     * 客户端计算的页面状态版本；变更时服务端不得复用基于旧快照生成的动作。
     */
    @NotBlank
    @Size(max = 128)
    private String pageVersion;

    /**
     * 提供给模型的脱敏页面状态；调用方必须排除凭据、隐藏字段和无关原始正文。
     */
    @NotNull
    private Map<String, Object> state;

    /**
     * 此快照下客户端允许执行的全部语义动作，是模型决策的唯一可选范围。
     */
    @NotEmpty
    @Valid
    private List<AiAgentCapabilityDTO> capabilities;
}
