package com.basic.core.threadpool.monitor;

import com.basic.core.threadpool.model.ThreadPoolSnapshot;

/**
 * 统一暴露受管理执行器的名称、状态快照及失败记录能力。
 */
public interface ManagedExecutorMonitor {

    String getName();

    ThreadPoolSnapshot snapshot();

    boolean ownsThread(String threadName);

    void recordExternalFailure();
}
