package com.basic.core.log.spi;

/**
 * 日志处理器。
 *
 * @param <T> 日志记录类型
 */
public interface LogHandler<T> {

    /**
     * 处理日志记录。
     *
     * @param record 日志记录
     */
    void handle(T record);
}
