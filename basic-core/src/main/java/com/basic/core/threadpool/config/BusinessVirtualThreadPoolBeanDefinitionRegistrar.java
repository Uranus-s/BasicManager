package com.basic.core.threadpool.config;

import com.basic.core.threadpool.executor.MonitoredVirtualTaskExecutor;
import com.basic.core.threadpool.support.BusinessVirtualThreadPoolNames;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * 根据配置在 BeanDefinition 阶段注册业务虚拟线程执行器，确保其接受完整的 Spring 生命周期管理。
 */
public final class BusinessVirtualThreadPoolBeanDefinitionRegistrar
        implements BeanDefinitionRegistryPostProcessor {

    private final ThreadPoolProperties properties;

    public BusinessVirtualThreadPoolBeanDefinitionRegistrar(Environment environment) {
        this.properties = Binder.get(environment)
                .bind("basic.thread-pool", Bindable.of(ThreadPoolProperties.class))
                .orElseGet(ThreadPoolProperties::new);
    }

    /**
     * 在普通 Bean 实例化之前注册业务执行器，使监控聚合和销毁回调均能识别动态 Bean。
     *
     * @param registry 当前应用上下文的 BeanDefinition 注册表
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        validateConfiguration();
        properties.getVirtual().getBusinesses().forEach((businessName, business) -> {
            String beanName = BusinessVirtualThreadPoolNames.beanName(businessName);
            if (registry.isBeanNameInUse(beanName)) {
                throw new BeanDefinitionStoreException(
                        "业务虚拟线程池 Bean 名称冲突: " + beanName);
            }
            registry.registerBeanDefinition(
                    beanName,
                    BeanDefinitionBuilder
                            .genericBeanDefinition(MonitoredVirtualTaskExecutor.class)
                            .addConstructorArgValue(beanName)
                            .addConstructorArgValue(business.getConcurrencyLimit())
                            .addConstructorArgValue(
                                    properties.getShutdown().getAwaitTermination())
                            .addConstructorArgReference("contextCopyingTaskDecorator")
                            .addConstructorArgValue(
                                    BusinessVirtualThreadPoolNames.threadNamePrefix(
                                            businessName))
                            .getBeanDefinition());
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 本注册器只负责补充 BeanDefinition，无需修改已创建的 BeanFactory。
    }

    /**
     * 独立校验动态注册边界，避免仅依赖后续配置属性 Bean 的校验时序。
     */
    private void validateConfiguration() {
        ThreadPoolProperties.Virtual virtual = properties.getVirtual();
        Map<String, ThreadPoolProperties.BusinessVirtual> businesses =
                virtual.getBusinesses();
        if (businesses == null) {
            throw new BeanDefinitionStoreException("业务虚拟线程池配置不能为空");
        }

        long configuredTotal = virtual.getConcurrencyLimit();
        for (Map.Entry<String, ThreadPoolProperties.BusinessVirtual> entry
                : businesses.entrySet()) {
            String businessName = entry.getKey();
            try {
                BusinessVirtualThreadPoolNames.validate(businessName);
            } catch (IllegalArgumentException exception) {
                throw new BeanDefinitionStoreException(
                        "业务虚拟线程池标识不合法: " + businessName, exception);
            }

            ThreadPoolProperties.BusinessVirtual business = entry.getValue();
            if (business == null) {
                throw new BeanDefinitionStoreException(
                        "业务虚拟线程池配置不能为空: " + businessName);
            }
            if (business.getConcurrencyLimit() < 1) {
                throw new BeanDefinitionStoreException(
                        "业务虚拟线程池并发上限必须大于等于 1，业务名: "
                                + businessName
                                + "，实际值: "
                                + business.getConcurrencyLimit());
            }
            configuredTotal += business.getConcurrencyLimit();
        }

        if (configuredTotal > virtual.getTotalConcurrencyLimit()) {
            throw new BeanDefinitionStoreException(
                    "业务虚拟线程池总并发超出预算，实际总和: "
                            + configuredTotal
                            + "，预算值: "
                            + virtual.getTotalConcurrencyLimit());
        }
    }
}
