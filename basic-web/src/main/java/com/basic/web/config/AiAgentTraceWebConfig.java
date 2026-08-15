package com.basic.web.config;

import com.basic.web.interceptor.AiAgentTraceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 限定 AI 请求关联头只在可能产生业务操作日志的接口范围内校验。
 */
@Configuration
@RequiredArgsConstructor
public class AiAgentTraceWebConfig implements WebMvcConfigurer {

    private final AiAgentTraceInterceptor aiAgentTraceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 只校验 AI 动作可能触发的业务接口；排除 /ai/**，避免代理自身的控制和结果上报被递归要求动作头。
        registry.addInterceptor(aiAgentTraceInterceptor)
                .addPathPatterns("/system/**", "/auth/forceLogout/**")
                .excludePathPatterns("/ai/**");
    }
}
