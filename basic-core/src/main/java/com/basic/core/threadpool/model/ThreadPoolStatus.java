package com.basic.core.threadpool.model;

/**
 * 线程池的生命周期状态枚举。
 * <p>
 * 表示线程池在其完整生命周期中所处的各个阶段，
 * 状态转换遵循严格的单向顺序：
 * {@code RUNNING → SHUTTING_DOWN → TERMINATED}。
 * <ul>
 *   <li>{@link #RUNNING} - 池处于正常工作状态，可接收并处理任务</li>
 *   <li>{@link #SHUTTING_DOWN} - 池正在关闭，不再接受新任务但会继续执行已提交的任务</li>
 *   <li>{@link #TERMINATED} - 池已完全终止，所有任务执行完毕且工作线程已销毁</li>
 * </ul>
 */
public enum ThreadPoolStatus {
    /**
     * 运行状态：线程池处于正常工作模式，
     * 可接收新任务并调度工作线程执行。
     */
    RUNNING,
    /**
     * 正在关闭状态：线程池已发起关闭指令，
     * 拒绝新任务的提交，但仍会继续执行队列中已存在的任务，
     * 直至所有剩余任务执行完成。
     */
    SHUTTING_DOWN,
    /**
     * 已终止状态：线程池已完全停止运行，
     * 所有任务均已执行完毕且所有工作线程已被销毁，
     * 线程池生命周期到此结束。
     */
    TERMINATED
}
