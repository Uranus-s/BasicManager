package com.basic.ai.assistant.runtime;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 AI 助手运行时配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AssistantRuntimeProperties.class)
public class AssistantRuntimeConfiguration {
}
