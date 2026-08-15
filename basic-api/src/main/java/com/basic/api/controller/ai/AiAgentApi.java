package com.basic.api.controller.ai;

import com.basic.api.dto.aiAgent.AiAgentActionResultDTO;
import com.basic.api.dto.aiAgent.AiAgentControlDTO;
import com.basic.api.dto.aiAgent.AiAgentResumeDTO;
import com.basic.api.dto.aiAgent.AiAgentTaskCreateDTO;
import com.basic.api.vo.aiAgent.AiAgentEventVO;
import com.basic.api.vo.aiAgent.AiAgentStatusVO;
import com.basic.api.vo.aiAgent.AiAgentTaskVO;
import com.basic.common.result.Result;
import com.basic.common.web.annotation.IgnoreResponseAdvice;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

/**
 * AI 网页代理的 REST 与 SSE 接口契约。
 *
 * <p>该接口只声明浏览器标签页与服务端的协作协议；身份、任务状态机、模型调用和
 * 页面快照校验由实现层及业务层处理。所有带 {@code clientInstanceId} 的请求均用于
 * 约束操作只能发生在任务绑定的浏览器实例中。路径中的 {@code taskId} 和
 * {@code actionId} 只关联任务及幂等动作，不能替代请求级 traceId；业务请求携带的代理关联头
 * 由 Web 拦截器校验后转换为可信请求属性，客户端传值本身不作为身份或授权依据。</p>
 */
public interface AiAgentApi {

    /**
     * 查询当前用户在指定浏览器实例中的代理可用状态及活动任务。
     *
     * @param clientInstanceId 请求页面的实例标识，用于判断其是否为活动任务绑定页面
     */
    @GetMapping("/status")
    Result<AiAgentStatusVO> getStatus(@RequestParam String clientInstanceId);

    /**
     * 基于标签页提供的脱敏快照创建代理任务；用户身份不得由请求体传入。
     *
     * @param dto 目标、标签页绑定标识及首个可信页面快照
     */
    @PostMapping("/tasks")
    Result<AiAgentTaskVO> createTask(@Valid @RequestBody AiAgentTaskCreateDTO dto);

    /**
     * 查询任务当前状态，具体访问范围由服务端按登录态校验。
     *
     * @param taskId 需要查询的任务追踪主键
     */
    @GetMapping("/tasks/{taskId}")
    Result<AiAgentTaskVO> getTask(@PathVariable Long taskId);

    /**
     * 建立任务事件流，用于异步接收状态变化、待执行动作及失败提示。
     * SSE 响应跳过统一包装，避免破坏事件流协议。
     *
     * @param taskId 需要订阅的任务追踪主键
     * @param clientInstanceId 建立订阅的页面实例，必须与任务绑定关系匹配
     */
    @GetMapping(value = "/tasks/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @IgnoreResponseAdvice
    Flux<ServerSentEvent<AiAgentEventVO>> events(
            @PathVariable Long taskId, @RequestParam String clientInstanceId);

    /**
     * 上报指定动作的执行结果及执行后的页面快照，供服务端校验并规划后续步骤。
     *
     * @param taskId 动作所属任务追踪主键
     * @param actionId 动作幂等标识，重复上报必须收敛到同一状态迁移
     * @param dto 执行终态、标签页绑定标识及执行后的脱敏快照
     */
    @PostMapping("/tasks/{taskId}/actions/{actionId}/result")
    Result<?> reportResult(
            @PathVariable Long taskId,
            @PathVariable String actionId,
            @Valid @RequestBody AiAgentActionResultDTO dto);

    /**
     * 暂停任务，防止已下发但尚未完成的动作继续推进状态。
     *
     * @param taskId 需要暂停的任务追踪主键
     * @param dto 发起暂停的标签页绑定标识
     */
    @PostMapping("/tasks/{taskId}/pause")
    Result<?> pause(@PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto);

    /**
     * 以同一标签页的新快照恢复暂停任务，避免在旧页面上下文继续执行。
     *
     * @param taskId 需要恢复的任务追踪主键
     * @param dto 标签页绑定标识及替换旧上下文的新快照
     */
    @PostMapping("/tasks/{taskId}/resume")
    Result<?> resume(@PathVariable Long taskId, @Valid @RequestBody AiAgentResumeDTO dto);

    /**
     * 终止任务；终止后的动作结果不应再次驱动该任务规划。
     *
     * @param taskId 需要终止的任务追踪主键
     * @param dto 发起终止的标签页绑定标识
     */
    @PostMapping("/tasks/{taskId}/cancel")
    Result<?> cancel(@PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto);

    /**
     * 由指定标签页认领任务，用于在刷新或事件流重连后恢复唯一执行归属。
     *
     * @param taskId 需要认领的任务追踪主键
     * @param dto 请求认领的标签页绑定标识
     */
    @PostMapping("/tasks/{taskId}/claim")
    Result<AiAgentTaskVO> claim(
            @PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto);

    /**
     * 确认指定高风险动作；确认只作用于当前任务中仍有效的待确认动作。
     *
     * @param taskId 动作所属任务追踪主键
     * @param actionId 待确认动作的幂等标识，不能确认历史或已终结动作
     * @param dto 发起确认的标签页绑定标识
     */
    @PostMapping("/tasks/{taskId}/actions/{actionId}/confirm")
    Result<?> confirm(
            @PathVariable Long taskId,
            @PathVariable String actionId,
            @Valid @RequestBody AiAgentControlDTO dto);
}
