package com.basic.sericve.log;

import com.basic.core.log.model.OperateLogRecord;
import com.basic.core.log.spi.LogHandler;
import com.basic.sericve.sysOperLog.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 数据库操作日志处理器。
 */
@Component
@RequiredArgsConstructor
public class DbOperateLogHandler implements LogHandler<OperateLogRecord> {

    private final ISysOperLogService sysOperLogService;

    @Override
    public void handle(OperateLogRecord record) {
        sysOperLogService.saveOperLog(
                record.getModule(),
                record.getMethod(),
                record.getRequestUrl(),
                record.getRequestMethod(),
                record.getRequestParams(),
                record.getResponseResult(),
                record.getStatus(),
                record.getCostTime()
        );
    }
}
