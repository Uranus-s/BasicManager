package com.basic.sericve.ai.assistant.capability.online;

import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.auth.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/** 在线用户只读 AI 能力，仅返回当前登录会话的安全摘要。 */
@Component
@RequiredArgsConstructor
public class OnlineUserAssistantCapability implements AssistantCapability {

    private static final String ONLINE_QUERY = "auth:online:list";
    private static final int MAX_RESULTS = 20;

    private final IAuthService authService;

    @Override
    public String capabilityId() {
        return "online";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        return List.of(query("online_list", OnlineListResult.class)
                .description("查询当前在线用户，最多返回20条安全摘要")
                .permissions(ONLINE_QUERY)
                .execute(this::listOnlineUsers)
                .build());
    }

    private OnlineListResult listOnlineUsers() {
        List<OnlineUserVO> users = authService.getOnlineUsers();
        int total = users == null ? 0 : users.size();
        List<OnlineUserSummary> items = users == null ? List.of() : users.stream()
                .limit(MAX_RESULTS)
                .map(OnlineUserAssistantCapability::toSummary)
                .toList();
        return new OnlineListResult((long) total, items, total > MAX_RESULTS);
    }

    private static OnlineUserSummary toSummary(OnlineUserVO user) {
        return new OnlineUserSummary(user.getUserId(), user.getUsername(), user.getNickname(),
                user.getLoginTime(), user.getLoginIp(), user.getBrowser(), user.getOs());
    }

    public record OnlineListResult(Long total, List<OnlineUserSummary> items, boolean truncated) {
    }

    /** 不暴露头像内容或令牌，仅保留会话识别和登录环境摘要。 */
    public record OnlineUserSummary(Long userId, String username, String nickname,
                                    LocalDateTime loginTime, String loginIp,
                                    String browser, String os) {
    }
}
