package com.basic.api.dto.aiAgent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 恢复任务请求，必须携带同一标签页的新页面快照。
 */
@Data
public class AiAgentResumeDTO {

    /**
     * 恢复请求所属标签页，必须与暂停任务的页面绑定关系一致。
     */
    @NotBlank
    @Size(max = 64)
    private String clientInstanceId;

    /**
     * 刷新或页面变化后重新采集的快照，恢复时以它替换旧上下文。
     */
    @NotNull
    @Valid
    private AiAgentPageSnapshotDTO snapshot;
}
