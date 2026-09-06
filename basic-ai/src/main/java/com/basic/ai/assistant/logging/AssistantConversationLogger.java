package com.basic.ai.assistant.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 统一输出 AI 助手模型轮次和工具结果日志。
 *
 * <p>完整会话只写入 DEBUG 日志；写出前会遮蔽凭据和身份证号。
 * 日志详情延迟构造，任何日志异常都不能影响 AI 助手主流程。</p>
 */
@Component
public final class AssistantConversationLogger {

    public static final String LOGGER_NAME = "com.basic.ai.assistant.conversation";

    private static final int MAX_EMBEDDED_JSON_DEPTH = 16;
    private static final String SENSITIVE_TEXT_KEY_PATTERN =
            "[A-Za-z0-9_-]*(?:password|passwd|token|authorization|secret|api[_-]?key)"
                    + "[A-Za-z0-9_-]*|密码|口令|令牌|身份证号";
    private static final Logger LOG = LoggerFactory.getLogger(LOGGER_NAME);
    private static final JsonMapper JSON_MAPPER = JacksonUtils.getDefaultJsonMapper();
    private static final Pattern TEXT_SENSITIVE_FIELD = Pattern.compile(
            "(?i)((?:" + SENSITIVE_TEXT_KEY_PATTERN + ")\\s*(?:是|为|[:=])\\s*)"
                    + "(.*?)(?=\\r?\\n|\\\\[rn]|[,;，；]|(?<!\\\\)\"(?=\\s*[,}\\]]|$)|$)");
    private static final Pattern BEARER_CREDENTIAL = Pattern.compile(
            "(?i)(Bearer\\s+)[A-Za-z0-9._~+/-]+=*");
    private static final Pattern JWT = Pattern.compile(
            "(?<![A-Za-z0-9_-])eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"
                    + "(?![A-Za-z0-9_-])");
    private static final Pattern CHINESE_ID_CARD = Pattern.compile(
            "(?<!\\d)\\d{17}[0-9Xx](?!\\d)");

    /** 记录一次真实模型调用收到的完整消息列表、模型参数和工具定义。 */
    public void logModelRequest(Long runId, int round, Prompt prompt) {
        write("MODEL_REQUEST", runId, round, () -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("messages", prompt.getInstructions());
            detail.put("options", snapshotModelOptions(prompt));
            return detail;
        });
    }

    /** 记录一次真实模型调用聚合后的完整响应。 */
    public void logModelResponse(Long runId, int round, ChatResponse response) {
        write("MODEL_RESPONSE", runId, round, () -> response);
    }

    /** 只记录稳定的异常类型，不把第三方异常正文写入完整会话日志。 */
    public void logModelFailure(Long runId, int round, Throwable error) {
        write("MODEL_FAILED", runId, round,
                () -> Map.of("errorType", error.getClass().getName()));
    }

    /** 记录实际返回给工具调用循环的完整工具对话历史。 */
    public void logToolResult(Long runId, int round, ToolExecutionResult result) {
        write("TOOL_RESULT", runId, round, () -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("returnDirect", result.returnDirect());
            detail.put("conversationHistory", result.conversationHistory());
            return detail;
        });
    }

    private void write(String event,
                       Long runId,
                       Integer round,
                       Supplier<Object> detailSupplier) {
        try {
            if (!LOG.isDebugEnabled()) {
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event", event);
            payload.put("runId", runId);
            if (round != null) {
                payload.put("round", round);
            }
            payload.put("detail", detailSupplier.get());
            LOG.debug("{}", serializeForLog(payload));
        }
        catch (RuntimeException ignored) {
            // 降级日志只包含原始标量，不再次访问可能抛错或携带敏感信息的详情对象。
            logFallback(event, runId, round);
        }
    }

    private static void logFallback(String event, Long runId, Integer round) {
        try {
            LOG.debug("event={} runId={} round={} detail=<unavailable>", event, runId, round);
        }
        catch (RuntimeException ignored) {
            // 日志后端自身异常也不得改变模型或工具调用结果。
        }
    }

    /** 将模型选项投影为不含运行上下文和回调实现对象的稳定快照。 */
    static Map<String, Object> snapshotModelOptions(Prompt prompt) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        ChatOptions options = prompt.getOptions();
        if (options == null) {
            return snapshot;
        }
        snapshot.put("model", options.getModel());
        snapshot.put("maxTokens", options.getMaxTokens());
        snapshot.put("temperature", options.getTemperature());
        snapshot.put("topP", options.getTopP());
        snapshot.put("topK", options.getTopK());
        snapshot.put("frequencyPenalty", options.getFrequencyPenalty());
        snapshot.put("presencePenalty", options.getPresencePenalty());
        snapshot.put("stopSequences", options.getStopSequences());
        if (options instanceof DeepSeekChatOptions deepSeekOptions) {
            snapshot.put("toolChoice", deepSeekOptions.getToolChoice());
            snapshot.put("responseFormat", deepSeekOptions.getResponseFormat());
            snapshot.put("logprobs", deepSeekOptions.getLogprobs());
            snapshot.put("topLogprobs", deepSeekOptions.getTopLogprobs());
        }
        if (options instanceof ToolCallingChatOptions toolOptions) {
            snapshot.put("tools", snapshotToolDefinitions(toolOptions.getToolCallbacks()));
        }
        return snapshot;
    }

    private static List<Map<String, Object>> snapshotToolDefinitions(List<ToolCallback> callbacks) {
        if (callbacks == null || callbacks.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> definitions = new ArrayList<>(callbacks.size());
        for (ToolCallback callback : callbacks) {
            ToolDefinition definition = callback.getToolDefinition();
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("name", definition.name());
            snapshot.put("description", definition.description());
            snapshot.put("inputSchema", definition.inputSchema());
            definitions.add(snapshot);
        }
        return definitions;
    }

    /** 把任意日志对象转为递归脱敏后的单行 JSON。 */
    static String serializeForLog(Object value) {
        JsonNode tree = JSON_MAPPER.valueToTree(value);
        JsonNode redacted = redactNode(tree, 0);
        return sanitizeText(JSON_MAPPER.writeValueAsString(redacted));
    }

    private static JsonNode redactNode(JsonNode node, int depth) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            // 替换字段值时使用快照，避免依赖底层 Map 的迭代修改行为。
            for (Map.Entry<String, JsonNode> field : new ArrayList<>(object.properties())) {
                if (isSensitiveFieldName(field.getKey())) {
                    object.set(field.getKey(), StringNode.valueOf("***"));
                }
                else {
                    object.set(field.getKey(), redactNode(field.getValue(), depth));
                }
            }
            return object;
        }
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            for (int index = 0; index < array.size(); index++) {
                array.set(index, redactNode(array.get(index), depth));
            }
            return array;
        }
        if (node.isTextual()) {
            return StringNode.valueOf(redactTextValue(node.textValue(), depth));
        }
        return node;
    }

    private static String redactTextValue(String value, int depth) {
        String redacted = value;
        if (depth < MAX_EMBEDDED_JSON_DEPTH) {
            redacted = redactEmbeddedJsonFragments(value, depth);
        }
        return sanitizeText(redacted);
    }

    /** 对混在普通说明文本中的 JSON 对象或数组逐段执行结构化脱敏。 */
    private static String redactEmbeddedJsonFragments(String value, int depth) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(value.length());
        int copiedUntil = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '{' && current != '[') {
                continue;
            }
            int end = findJsonFragmentEnd(value, index);
            if (end < 0) {
                continue;
            }
            try {
                JsonNode embedded = JSON_MAPPER.readTree(value.substring(index, end + 1));
                if (embedded == null || (!embedded.isObject() && !embedded.isArray())) {
                    continue;
                }
                result.append(value, copiedUntil, index);
                result.append(JSON_MAPPER.writeValueAsString(redactNode(embedded, depth + 1)));
                copiedUntil = end + 1;
                index = end;
            }
            catch (RuntimeException ignored) {
                // 不是合法 JSON 时保留原文，后续仍执行文本级凭据遮蔽。
            }
        }
        if (copiedUntil == 0) {
            return value;
        }
        return result.append(value, copiedUntil, value.length()).toString();
    }

    /** 在识别字符串转义的前提下查找一个 JSON 片段的配对结束位置。 */
    private static int findJsonFragmentEnd(String value, int start) {
        Deque<Character> closings = new ArrayDeque<>();
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                }
                else if (current == '\\') {
                    escaped = true;
                }
                else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            }
            else if (current == '{') {
                closings.push('}');
            }
            else if (current == '[') {
                closings.push(']');
            }
            else if (current == '}' || current == ']') {
                if (closings.isEmpty() || closings.pop() != current) {
                    return -1;
                }
                if (closings.isEmpty()) {
                    return index;
                }
            }
        }
        return -1;
    }

    private static boolean isSensitiveFieldName(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("authorization")
                || normalized.contains("secret")
                || normalized.contains("apikey")
                || normalized.contains("accesstoken")
                || normalized.contains("refreshtoken")
                || normalized.contains("token")
                || normalized.contains("密码")
                || normalized.contains("口令")
                || normalized.contains("令牌")
                || normalized.contains("身份证");
    }

    private static String sanitizeText(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = BEARER_CREDENTIAL.matcher(value).replaceAll("$1***");
        sanitized = JWT.matcher(sanitized).replaceAll("***");
        sanitized = TEXT_SENSITIVE_FIELD.matcher(sanitized).replaceAll("$1***");
        return CHINESE_ID_CARD.matcher(sanitized).replaceAll("***");
    }
}
