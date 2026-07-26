package com.basic.core.threadpool.config;

import com.basic.core.threadpool.support.BusinessVirtualThreadPoolNames;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
     * 默认虚拟线程执行器与按业务隔离的虚拟线程执行器配置。
     */
    @Getter
    @Setter
    public static class Virtual {

        @Min(1)
        private int concurrencyLimit = 200;

        @Min(1)
        private int totalConcurrencyLimit = 500;

        @Valid
        private Map<String, @NotNull @Valid BusinessVirtual> businesses = new LinkedHashMap<>();

        /**
         * 业务池名称必须可稳定映射为唯一 Bean 名，配置值不可为空，且默认池与业务池的并发总额不得超过全局边界。
         *
         * @return 名称和总并发配置均合法时返回 {@code true}
         */
        @AssertTrue(message = "业务虚拟线程池名称或总并发配置不合法")
        public boolean isBusinessConfigurationValid() {
            if (businesses == null || businesses.containsValue(null)) {
                return false;
            }
            Set<String> beanNames = new HashSet<>();
            boolean validNames = businesses.keySet().stream().allMatch(name -> {
                try {
                    BusinessVirtualThreadPoolNames.validate(name);
                    return beanNames.add(BusinessVirtualThreadPoolNames.beanName(name));
                } catch (IllegalArgumentException exception) {
                    return false;
                }
            });
            long configuredTotal = concurrencyLimit
                    + businesses.values().stream()
                            .mapToLong(BusinessVirtual::getConcurrencyLimit)
                            .sum();
            return validNames && configuredTotal <= totalConcurrencyLimit;
        }
    }

    /**
     * 单个业务虚拟线程池的并发上限。
     */
    @Getter
    @Setter
    public static class BusinessVirtual {

        @Min(1)
        private int concurrencyLimit = 1;
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
