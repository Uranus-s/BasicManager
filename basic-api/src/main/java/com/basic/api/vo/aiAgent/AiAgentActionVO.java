package com.basic.api.vo.aiAgent;

import lombok.Data;

import java.util.Map;

/**
 * 下发给当前标签页的单个语义动作。
 */
@Data
public class AiAgentActionVO {
    /**
     * 动作全局幂等标识；客户端重试、上报结果和确认均以此关联同一动作。
     */
    private String actionId;
    /**
     * 任务内的单调步骤序号，用于客户端在重连后识别动作的先后关系，而不是新的幂等键。
     */
    private Integer sequenceNo;
    /**
     * 动作适用的业务路由，当前页面路由不匹配时客户端必须拒绝执行。
     */
    private String routeName;
    /**
     * 动作生成时的页面版本，客户端版本不一致时不能直接执行该动作。
     */
    private String pageVersion;
    /**
     * 客户端预置执行器支持的语义动作类型，不能解释为脚本或任意网络请求。
     */
    private String actionType;
    /**
     * 动作允许作用的业务语义目标，不暴露底层页面选择器。
     */
    private String target;
    /**
     * 风险分级结果，用于决定客户端是直接执行还是先请求用户确认。
     */
    private String riskLevel;
    /**
     * 受能力参数结构约束的脱敏参数；客户端只能按预置动作语义消费，不可作为自由指令执行。
     */
    private Map<String, Object> arguments;
    /**
     * 服务端维护的动作状态，客户端据此区分待执行、待确认与已终结动作。
     */
    private String status;
    /**
     * 已执行动作的脱敏结果摘要；未执行时允许为空，不能作为客户端重新执行的输入。
     */
    private String resultSummary;
    /**
     * 高风险动作的可信确认摘要；用于向用户说明确认对象，不能依赖模型自由文本重建。
     */
    private String confirmationSummary;
}
