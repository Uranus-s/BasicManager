package com.basic.ai.assistant.model;

/**
 * 用户对当前待审批操作的意图；UNKNOWN 永远不能触发写操作。
 */
public enum ApprovalIntent {
    /** 用户无附加条件地明确同意执行已保存快照。 */
    CONFIRM,
    /** 用户明确放弃当前待审批操作。 */
    CANCEL,
    /** 用户要求修改内容，旧快照必须先失效再重新规划。 */
    REVISE,
    /** 无法可靠判定，必须继续追问且不得执行写操作。 */
    UNKNOWN
}
