package com.basic.sericve.ai.agent.service;

import com.basic.api.dto.aiAgent.AiAgentActionResultDTO;
import com.basic.api.dto.aiAgent.AiAgentPageSnapshotDTO;
import com.basic.api.dto.aiAgent.AiAgentTaskCreateDTO;
import com.basic.api.vo.aiAgent.AiAgentEventVO;
import com.basic.api.vo.aiAgent.AiAgentStatusVO;
import com.basic.api.vo.aiAgent.AiAgentTaskVO;
import com.basic.sericve.ai.agent.model.AiAgentUserContext;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * AI 网页代理业务编排接口，身份、权限、状态和风险判断均由服务端负责。
 */
public interface IAiAgentService {

    /** 查询功能开关及当前用户唯一活动任务，并判断请求标签页是否拥有执行权。 */
    AiAgentStatusVO getStatus(AiAgentUserContext user, String clientInstanceId);

    /** 创建并绑定任务，在事务提交后异步启动第一轮规划。 */
    AiAgentTaskVO createTask(AiAgentUserContext user, AiAgentTaskCreateDTO dto);

    /** 按登录用户校验任务归属后返回任务快照。 */
    AiAgentTaskVO getTask(AiAgentUserContext user, Long taskId);

    /** 订阅任务事件；订阅结束时对仍可执行的任务执行断线保护。 */
    Flux<ServerSentEvent<AiAgentEventVO>> events(
            AiAgentUserContext user, Long taskId, String clientInstanceId);

    /** 幂等接收动作结果和新页面快照，并在成功时驱动下一轮规划。 */
    void reportResult(
            AiAgentUserContext user, Long taskId, String actionId, AiAgentActionResultDTO dto);

    /** 暂停绑定标签页上的活动任务。 */
    void pause(AiAgentUserContext user, Long taskId, String clientInstanceId);

    /** 使用标签页重新采集的页面快照恢复暂停任务。 */
    void resume(
            AiAgentUserContext user,
            Long taskId,
            String clientInstanceId,
            AiAgentPageSnapshotDTO snapshot);

    /** 将任务推进到不可恢复的取消终态。 */
    void cancel(AiAgentUserContext user, Long taskId, String clientInstanceId);

    /** 将暂停任务的唯一执行权转移到指定标签页。 */
    AiAgentTaskVO claim(AiAgentUserContext user, Long taskId, String clientInstanceId);

    /** 确认当前活动的高风险动作并允许客户端执行。 */
    void confirm(AiAgentUserContext user, Long taskId, String actionId, String clientInstanceId);

    /** 校验任务和动作是否可作为当前请求的可信代理追踪上下文。 */
    boolean validateTrace(AiAgentUserContext user, Long taskId, String actionId);
}
