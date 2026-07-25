package com.basic.core.threadpool.config;

import com.basic.core.threadpool.constant.ThreadPoolNames;
import com.basic.core.threadpool.context.ContextCopyingTaskDecorator;
import com.basic.core.threadpool.executor.MonitoredThreadPoolTaskExecutor;
import com.basic.core.threadpool.executor.MonitoredThreadPoolTaskScheduler;
import com.basic.core.threadpool.executor.MonitoredVirtualTaskExecutor;
import com.basic.core.threadpool.handler.ThreadPoolAsyncExceptionHandler;
import com.basic.core.threadpool.monitor.ManagedExecutorMonitor;
import com.basic.core.threadpool.monitor.ThreadPoolMonitorRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 统一创建受监控执行器、上下文装饰器和异常指标注册表。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfiguration {

    @Bean
    ContextCopyingTaskDecorator contextCopyingTaskDecorator() {
        return new ContextCopyingTaskDecorator();
    }

    /**
     * CPU 与虚拟线程任务复制提交方上下文；调度器不装饰任务，以保留取消移除策略。
     */
    @Bean(name = ThreadPoolNames.CPU)
    MonitoredThreadPoolTaskExecutor cpuTaskExecutor(
            ThreadPoolProperties properties,
            ContextCopyingTaskDecorator decorator) {
        return new MonitoredThreadPoolTaskExecutor(
                ThreadPoolNames.CPU,
                properties.getCpu(),
                properties.getShutdown().getAwaitTermination(),
                decorator);
    }

    @Bean(name = ThreadPoolNames.VIRTUAL)
    MonitoredVirtualTaskExecutor virtualTaskExecutor(
            ThreadPoolProperties properties,
            ContextCopyingTaskDecorator decorator) {
        return new MonitoredVirtualTaskExecutor(
                ThreadPoolNames.VIRTUAL,
                properties.getVirtual().getConcurrencyLimit(),
                properties.getShutdown().getAwaitTermination(),
                decorator);
    }

    @Bean(name = ThreadPoolNames.SCHEDULED)
    MonitoredThreadPoolTaskScheduler scheduledTaskScheduler(ThreadPoolProperties properties) {
        return new MonitoredThreadPoolTaskScheduler(
                ThreadPoolNames.SCHEDULED,
                properties.getScheduled().getPoolSize(),
                properties.getShutdown().getAwaitTermination());
    }

    @Bean
    ThreadPoolMonitorRegistry threadPoolMonitorRegistry(List<ManagedExecutorMonitor> monitors) {
        return new ThreadPoolMonitorRegistry(monitors);
    }

    @Bean
    ThreadPoolAsyncExceptionHandler threadPoolAsyncExceptionHandler(
            ThreadPoolMonitorRegistry registry) {
        return new ThreadPoolAsyncExceptionHandler(registry);
    }
}
