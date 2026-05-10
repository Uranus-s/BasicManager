package com.basic.core.log.aspect;

import com.basic.core.log.model.LoginLogRecord;
import com.basic.core.log.spi.LogHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 登录日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoginLogAspect {

    private final ObjectProvider<LogHandler<LoginLogRecord>> loginLogHandlerProvider;

    @Around("execution(* com.basic.web.controller.AuthController.login(..))")
    public Object aroundLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            saveLoginLog(joinPoint, error);
        }
    }

    private void saveLoginLog(ProceedingJoinPoint joinPoint, Throwable error) {
        LogHandler<LoginLogRecord> handler = loginLogHandlerProvider.getIfAvailable();
        if (handler == null) {
            return;
        }

        try {
            HttpServletRequest request = LogRequestUtils.currentRequest();
            LoginLogRecord record = new LoginLogRecord();
            record.setUsername(LogRequestUtils.extractUsername(joinPoint.getArgs()));
            record.setIp(LogRequestUtils.getIp(request));
            record.setBrowser(LogRequestUtils.getBrowser(request));
            record.setOs(LogRequestUtils.getOs(request));
            record.setStatus((byte) (error == null ? 1 : 0));
            record.setMsg(error == null ? "登录成功" : error.getMessage());
            handler.handle(record);
        } catch (Exception ex) {
            log.warn("记录登录日志失败", ex);
        }
    }
}
