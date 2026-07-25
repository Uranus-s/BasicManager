package com.basic.core.threadpool.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 统一线程池的可配置参数及其边界校验。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "basic.thread-pool")
public class ThreadPoolProperties {

    @Valid
    private Cpu cpu = new Cpu();

    @Valid
    private Virtual virtual = new Virtual();

    @Valid
    private Scheduled scheduled = new Scheduled();

    @Valid
    private Shutdown shutdown = new Shutdown();

    /**
     * 平台线程执行器的参数；最大线程数必须覆盖核心线程数。
     */
    @Getter
    @Setter
    public static class Cpu {

        @Min(1)
        private int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors());

        @Min(1)
        private int maximumPoolSize = corePoolSize * 2;

        @Min(0)
        private int queueCapacity = 500;

        @NotNull
        private Duration keepAlive = Duration.ofSeconds(60);

        /**
         * 防止线程池因最大线程数小于核心线程数而无法创建。
         *
         * @return 最大线程数不小于核心线程数时返回 {@code true}
         */
        @AssertTrue(message = "maximumPoolSize 不能小于 corePoolSize")
        public boolean isMaximumPoolSizeValid() {
            return maximumPoolSize >= corePoolSize;
        }

        /**
         * 空闲存活时间允许为零，但不能为负数。
         *
         * @return 存活时间合法时返回 {@code true}
         */
        @AssertTrue(message = "keepAlive 不能为负数")
        public boolean isKeepAliveValid() {
            return keepAlive == null || !keepAlive.isNegative();
        }
    }

    /**
     * 虚拟线程执行器的并发上限。
     */
    @Getter
    @Setter
    public static class Virtual {

        @Min(1)
        private int concurrencyLimit = 200;
    }

    /**
     * 定时任务调度器的核心线程数量。
     */
    @Getter
    @Setter
    public static class Scheduled {

        @Min(1)
        private int poolSize = 2;
    }

    /**
     * 应用关闭线程池时的等待时间。
     */
    @Getter
    @Setter
    public static class Shutdown {

        @NotNull
        private Duration awaitTermination = Duration.ofSeconds(30);

        /**
         * 关闭等待时间允许为零，但不能为负数。
         *
         * @return 等待时间合法时返回 {@code true}
         */
        @AssertTrue(message = "awaitTermination 不能为负数")
        public boolean isAwaitTerminationValid() {
            return awaitTermination == null || !awaitTermination.isNegative();
        }
    }
}
