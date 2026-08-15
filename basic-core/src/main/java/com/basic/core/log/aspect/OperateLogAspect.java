package com.basic.core.log.aspect;

import com.basic.core.log.annotation.OperateLog;
import com.basic.core.log.model.OperateLogRecord;
import com.basic.core.log.spi.LogHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 操作日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final ObjectProvider<LogHandler<OperateLogRecord>> operateLogHandlerProvider;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            saveOperateLog(joinPoint, operateLog, result, error, System.currentTimeMillis() - startTime);
        }
    }

    private void saveOperateLog(ProceedingJoinPoint joinPoint, OperateLog operateLog, Object result, Throwable error, long costTime) {
        LogHandler<OperateLogRecord> handler = operateLogHandlerProvider.getIfAvailable();
        if (handler == null) {
            return;
        }

        try {
            HttpServletRequest request = LogRequestUtils.currentRequest();
            OperateLogRecord record = new OperateLogRecord();
            record.setModule(operateLog.module());
            record.setMethod(StringUtils.hasText(operateLog.method()) ? operateLog.method() : joinPoint.getSignature().getName());
            record.setRequestUrl(request == null ? "" : request.getRequestURI());
            record.setRequestMethod(request == null ? "" : request.getMethod());
            record.setRequestParams(LogRequestUtils.serializeArgs(joinPoint.getArgs()));
            record.setResponseResult(error == null ? LogRequestUtils.serializeResult(result) : error.getMessage());
            record.setStatus((byte) (error == null ? 1 : 0));
            record.setCostTime(costTime);
            // 只读取拦截器校验后写入的请求属性，原始客户端头不能直接影响操作来源和审计关联。
            Object taskId = request == null ? null : request.getAttribute("aiAgentTaskId");
            record.setAiAgentTaskId(taskId instanceof Long value ? value : null);
            record.setAiAgentActionId(request == null ? null
                    : Objects.toString(request.getAttribute("aiAgentActionId"), null));
            record.setOperationSource(record.getAiAgentTaskId() == null ? "MANUAL" : "AI_AGENT");
            handler.handle(record);
        } catch (Exception ex) {
            log.warn("记录操作日志失败", ex);
        }
    }
}
