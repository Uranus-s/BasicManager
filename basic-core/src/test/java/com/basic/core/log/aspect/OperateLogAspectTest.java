package com.basic.core.log.aspect;

import com.basic.core.log.annotation.OperateLog;
import com.basic.core.log.model.OperateLogRecord;
import com.basic.core.log.spi.LogHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperateLogAspectTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void recordsSuccessOperationThroughHandler() throws Throwable {
        CapturingOperateLogHandler handler = new CapturingOperateLogHandler();
        OperateLogAspect aspect = new OperateLogAspect(provider(handler));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/system/user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ProceedingJoinPoint joinPoint = joinPoint("addUser", new Object[]{"admin"}, "ok");

        Object result = aspect.around(joinPoint, operateLog("用户管理", "新增用户"));

        assertEquals("ok", result);
        assertEquals(1, handler.records.size());
        OperateLogRecord record = handler.records.getFirst();
        assertEquals("用户管理", record.getModule());
        assertEquals("新增用户", record.getMethod());
        assertEquals("/system/user", record.getRequestUrl());
        assertEquals("POST", record.getRequestMethod());
        assertEquals((byte) 1, record.getStatus());
        assertTrue(record.getCostTime() >= 0);
    }

    @Test
    void recordsFailureOperationAndRethrowsBusinessException() throws Throwable {
        CapturingOperateLogHandler handler = new CapturingOperateLogHandler();
        OperateLogAspect aspect = new OperateLogAspect(provider(handler));
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/system/user/1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RuntimeException error = new RuntimeException("删除失败");
        ProceedingJoinPoint joinPoint = joinPoint("deleteUser", new Object[]{1L}, error);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> aspect.around(joinPoint, operateLog("用户管理", "删除用户")));

        assertSame(error, thrown);
        assertEquals(1, handler.records.size());
        OperateLogRecord record = handler.records.getFirst();
        assertEquals("用户管理", record.getModule());
        assertEquals("删除用户", record.getMethod());
        assertEquals("/system/user/1", record.getRequestUrl());
        assertEquals("DELETE", record.getRequestMethod());
        assertEquals((byte) 0, record.getStatus());
        assertTrue(record.getResponseResult().contains("删除失败"));
    }

    private static ProceedingJoinPoint joinPoint(String methodName, Object[] args, Object resultOrThrowable) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        if (resultOrThrowable instanceof Throwable throwable) {
            when(joinPoint.proceed()).thenThrow(throwable);
        } else {
            when(joinPoint.proceed()).thenReturn(resultOrThrowable);
        }
        return joinPoint;
    }

    private static OperateLog operateLog(String module, String method) {
        return new OperateLog() {
            @Override
            public String module() {
                return module;
            }

            @Override
            public String method() {
                return method;
            }

            @Override
            public Class<OperateLog> annotationType() {
                return OperateLog.class;
            }
        };
    }

    private static ObjectProvider<LogHandler<OperateLogRecord>> provider(LogHandler<OperateLogRecord> handler) {
        return new ObjectProvider<>() {
            @Override
            public LogHandler<OperateLogRecord> getObject(Object... args) {
                return handler;
            }

            @Override
            public LogHandler<OperateLogRecord> getIfAvailable() {
                return handler;
            }

            @Override
            public LogHandler<OperateLogRecord> getIfUnique() {
                return handler;
            }

            @Override
            public LogHandler<OperateLogRecord> getObject() {
                return handler;
            }
        };
    }

    private static class CapturingOperateLogHandler implements LogHandler<OperateLogRecord> {
        private final List<OperateLogRecord> records = new ArrayList<>();

        @Override
        public void handle(OperateLogRecord record) {
            records.add(record);
        }
    }
}
