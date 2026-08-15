package com.basic.sericve.ai.agent.support;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 代理上下文递归脱敏器，避免敏感字段或超长页面文本进入模型与审计记录。
 */
@Component
public class AiAgentSensitiveDataSanitizer {

    private static final int MAX_TEXT_LENGTH = 2_000;
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i).*(password|token|api.?key|secret|authorization|cookie).*");

    /**
     * 对页面状态 Map 做递归脱敏并保留原有遍历顺序。
     */
    public Map<String, Object> sanitize(Map<String, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        // 保留字段顺序能让模型输入和审计摘要更稳定，但所有嵌套值仍必须经过同一递归规则。
        source.forEach((key, value) -> result.put(key, sanitizeValue(key, value)));
        return result;
    }

    /**
     * 清理单段结果或目标文本，统一执行最大长度限制。
     */
    public String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > MAX_TEXT_LENGTH ? text.substring(0, MAX_TEXT_LENGTH) : text;
    }

    /**
     * 根据字段名和数据结构递归清理值；集合元素也会继续处理嵌套 Map。
     */
    public Object sanitizeValue(String key, Object value) {
        if (key != null && SENSITIVE_KEY.matcher(key).matches()) {
            return "***";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((nestedKey, nestedValue) -> {
                String normalizedKey = String.valueOf(nestedKey);
                result.put(normalizedKey, sanitizeValue(normalizedKey, nestedValue));
            });
            return result;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> sanitizeValue(null, item)).toList();
        }
        if (value instanceof String text && text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH);
        }
        return value;
    }
}
