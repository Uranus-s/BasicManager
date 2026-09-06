package com.basic.sericve.ai.assistant.capability.log;

import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.result.PageResult;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysLog.service.ISysLogService;
import com.basic.sericve.sysLoginLog.service.ISysLoginLogService;
import com.basic.sericve.sysOperLog.service.ISysOperLogService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/** 日志只读 AI 能力；返回安全摘要，避免把请求参数和响应内容暴露给模型。 */
@Component
@RequiredArgsConstructor
public class LogAssistantCapability implements AssistantCapability {

    private static final String LOG_QUERY = "system:log:query";
    private static final String LOGIN_LOG_QUERY = "system:loginLog:query";
    private static final String OPER_LOG_QUERY = "system:operLog:query";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int MAX_MESSAGE_LENGTH = 200;

    private final ISysLogService logService;
    private final ISysLoginLogService loginLogService;
    private final ISysOperLogService operLogService;

    @Override
    public String capabilityId() { return "log"; }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("system_log_search", SysLogSearchInput.class, SysLogSearchResult.class)
                .description("查询统一系统日志，分页默认10条且最多20条，内容最多返回200字")
                .permissions(LOG_QUERY).execute(this::searchSystemLog).build());
        operations.add(query("login_log_search", LoginLogSearchInput.class, LoginLogSearchResult.class)
                .description("查询登录日志，分页默认10条且最多20条，消息最多返回200字")
                .permissions(LOGIN_LOG_QUERY).execute(this::searchLoginLog).build());
        operations.add(query("login_log_get", LogIdInput.class, LoginLogDetail.class)
                .description("按ID读取登录日志安全详情，消息最多返回200字")
                .permissions(LOGIN_LOG_QUERY).execute(input -> toLoginDetail(loginLogService.getLoginLogById(input.id()))).build());
        operations.add(query("oper_log_search", OperLogSearchInput.class, OperLogSearchResult.class)
                .description("查询操作日志，分页默认10条且最多20条；不返回请求参数和响应结果")
                .permissions(OPER_LOG_QUERY).execute(this::searchOperLog).build());
        operations.add(query("oper_log_get", LogIdInput.class, OperLogDetail.class)
                .description("按ID读取操作日志安全详情；不返回请求参数和响应结果")
                .permissions(OPER_LOG_QUERY).execute(input -> toOperDetail(operLogService.getOperLogById(input.id()))).build());
        return List.copyOf(operations);
    }

    private SysLogSearchResult searchSystemLog(SysLogSearchInput input) {
        SysLogQueryDTO dto = new SysLogQueryDTO();
        dto.setLogType(trimToNull(input.logType())); dto.setKeyword(trimToNull(input.keyword())); dto.setStatus(input.status());
        setPage(dto, input.pageNum(), input.pageSize());
        PageResult<SysLogVO> page = logService.getLogList(dto);
        List<SysLogSummary> items = page.getList() == null ? List.of() : page.getList().stream().limit(MAX_PAGE_SIZE).map(LogAssistantCapability::toSystemSummary).toList();
        return new SysLogSearchResult(page.getTotal(), items);
    }

    private LoginLogSearchResult searchLoginLog(LoginLogSearchInput input) {
        LoginLogQueryDTO dto = new LoginLogQueryDTO();
        dto.setUsername(trimToNull(input.username())); dto.setIp(trimToNull(input.ip())); dto.setStatus(input.status());
        setPage(dto, input.pageNum(), input.pageSize());
        PageResult<LoginLogVO> page = loginLogService.getLoginLogList(dto);
        List<LoginLogSummary> items = page.getList() == null ? List.of() : page.getList().stream().limit(MAX_PAGE_SIZE).map(LogAssistantCapability::toLoginSummary).toList();
        return new LoginLogSearchResult(page.getTotal(), items);
    }

    private OperLogSearchResult searchOperLog(OperLogSearchInput input) {
        OperLogQueryDTO dto = new OperLogQueryDTO();
        dto.setModule(trimToNull(input.module())); dto.setOperationUser(trimToNull(input.operationUser()));
        dto.setRequestMethod(trimToNull(input.requestMethod())); dto.setStatus(input.status());
        setPage(dto, input.pageNum(), input.pageSize());
        PageResult<OperLogVO> page = operLogService.getOperLogList(dto);
        List<OperLogSummary> items = page.getList() == null ? List.of() : page.getList().stream().limit(MAX_PAGE_SIZE).map(LogAssistantCapability::toOperSummary).toList();
        return new OperLogSearchResult(page.getTotal(), items);
    }

    private static SysLogSummary toSystemSummary(SysLogVO vo) { return new SysLogSummary(vo.getId(), vo.getLogType(), vo.getTitle(), truncate(vo.getContent()), vo.getMethod(), vo.getRequestMethod(), vo.getIp(), vo.getStatus(), vo.getCostTime(), vo.getCreateTime()); }
    private static LoginLogSummary toLoginSummary(LoginLogVO vo) { return new LoginLogSummary(vo.getId(), vo.getUsername(), vo.getIp(), vo.getBrowser(), vo.getOs(), vo.getStatus(), truncate(vo.getMsg()), vo.getCreateTime()); }
    private static OperLogSummary toOperSummary(OperLogVO vo) { return new OperLogSummary(vo.getId(), vo.getModule(), vo.getMethod(), vo.getRequestUrl(), vo.getRequestMethod(), vo.getStatus(), vo.getCostTime(), vo.getCreateTime()); }
    private static LoginLogDetail toLoginDetail(LoginLogVO vo) { return new LoginLogDetail(vo.getId(), vo.getUsername(), vo.getIp(), vo.getBrowser(), vo.getOs(), vo.getStatus(), truncate(vo.getMsg()), vo.getCreateTime()); }
    private static OperLogDetail toOperDetail(OperLogVO vo) { return new OperLogDetail(vo.getId(), vo.getModule(), vo.getMethod(), vo.getRequestUrl(), vo.getRequestMethod(), vo.getStatus(), vo.getCostTime(), vo.getCreateTime()); }

    private static void setPage(SysLogQueryDTO dto, Integer pageNum, Integer pageSize) { dto.setPageNum(pageNum(pageNum)); dto.setPageSize(pageSize(pageSize)); }
    private static void setPage(LoginLogQueryDTO dto, Integer pageNum, Integer pageSize) { dto.setPageNum(pageNum(pageNum)); dto.setPageSize(pageSize(pageSize)); }
    private static void setPage(OperLogQueryDTO dto, Integer pageNum, Integer pageSize) { dto.setPageNum(pageNum(pageNum)); dto.setPageSize(pageSize(pageSize)); }
    private static int pageNum(Integer value) { return value == null || value < 1 ? 1 : value; }
    private static int pageSize(Integer value) { return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE)); }
    private static String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private static String truncate(String value) { return value == null || value.length() <= MAX_MESSAGE_LENGTH ? value : value.substring(0, MAX_MESSAGE_LENGTH) + "..."; }

    public record SysLogSearchInput(String logType, String keyword, Byte status, Integer pageNum, Integer pageSize) { }
    public record LoginLogSearchInput(String username, String ip, Byte status, Integer pageNum, Integer pageSize) { }
    public record OperLogSearchInput(String module, String operationUser, String requestMethod, Byte status, Integer pageNum, Integer pageSize) { }
    public record LogIdInput(@NotNull(message = "日志ID不能为空") Long id) { }
    public record SysLogSearchResult(Long total, List<SysLogSummary> items) { }
    public record LoginLogSearchResult(Long total, List<LoginLogSummary> items) { }
    public record OperLogSearchResult(Long total, List<OperLogSummary> items) { }
    public record SysLogSummary(Long id, String logType, String title, String content, String method, String requestMethod, String ip, Byte status, Long costTime, LocalDateTime createTime) { }
    public record LoginLogSummary(Long id, String username, String ip, String browser, String os, Byte status, String msg, LocalDateTime createTime) { }
    public record LoginLogDetail(Long id, String username, String ip, String browser, String os, Byte status, String msg, LocalDateTime createTime) { }
    public record OperLogSummary(Long id, String module, String method, String requestUrl, String requestMethod, Byte status, Long costTime, LocalDateTime createTime) { }
    public record OperLogDetail(Long id, String module, String method, String requestUrl, String requestMethod, Byte status, Long costTime, LocalDateTime createTime) { }
}
