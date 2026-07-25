package com.basic.core.threadpool.model;

/**
 * 线程池的实现类型枚举。
 * <p>
 * 定义当前线程池底层所使用的线程模型与调度策略：
 * <ul>
 *   <li>{@link #PLATFORM} - 基于操作系统平台线程的传统线程池</li>
 *   <li>{@link #VIRTUAL} - 基于虚拟线程（Project Loom）的轻量级线程池</li>
 *   <li>{@link #SCHEDULED} - 支持定时调度与周期性任务执行的线程池</li>
 * </ul>
 */
public enum ThreadPoolType {
    /**
     * 平台线程池：使用操作系统级别的平台线程（Platform Thread），
     * 每个线程与一个 OS 线程一一绑定，适用于 CPU 密集型及传统阻塞 I/O 场景。
     */
    PLATFORM,
    /**
     * 虚拟线程池：基于 Java 虚拟线程（Virtual Thread）实现，
     * 由 JVM 在少量载体线程上多路复用大量虚拟线程，
     * 适用于高并发、高吞吐量且以阻塞 I/O 为主的场景。
     */
    VIRTUAL,
    /**
     * 调度线程池：具备定时调度能力的线程池，
     * 支持延迟执行与固定频率/固定延迟的周期性任务，
     * 适用于需要按时间计划触发执行的业务场景。
     */
    SCHEDULED
}
