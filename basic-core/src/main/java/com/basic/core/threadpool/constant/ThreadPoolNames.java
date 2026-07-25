package com.basic.core.threadpool.constant;

/**
 * 统一线程池在 Spring 容器中的固定名称。
 */
public final class ThreadPoolNames {

    public static final String CPU = "cpuTaskExecutor";
    public static final String VIRTUAL = "virtualTaskExecutor";
    public static final String SCHEDULED = "scheduledTaskScheduler";

    private ThreadPoolNames() {
    }
}
