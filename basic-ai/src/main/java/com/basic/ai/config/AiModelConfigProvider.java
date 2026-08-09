package com.basic.ai.config;

/**
 * AI 模型运行时配置读取端口，由上层业务模块提供具体配置来源。
 */
public interface AiModelConfigProvider {

    /**
     * 获取 DeepSeek API Key；调用方不得将返回值写入响应、异常或日志。
     */
    String getDeepSeekApiKey();
}
