package com.basic.sericve.log;

import com.basic.core.log.model.LoginLogRecord;
import com.basic.core.log.spi.LogHandler;
import com.basic.sericve.sysLoginLog.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 数据库登录日志处理器。
 */
@Component
@RequiredArgsConstructor
public class DbLoginLogHandler implements LogHandler<LoginLogRecord> {

    private final ISysLoginLogService sysLoginLogService;

    @Override
    public void handle(LoginLogRecord record) {
        sysLoginLogService.saveLoginLog(
                record.getUsername(),
                record.getIp(),
                record.getBrowser(),
                record.getOs(),
                record.getStatus(),
                record.getMsg()
        );
    }
}
