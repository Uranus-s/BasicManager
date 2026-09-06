package com.basic.sericve.ai.assistant.action.model;

import java.util.List;

/**
 * 业务无关的待确认预览。
 *
 * @param title 预览标题
 * @param summary 仅供审批分类使用的安全摘要
 * @param fields 前端展示的短字段
 * @param content 可选长文本正文
 */
public record ActionPreview(
        String title,
        String summary,
        List<ActionPreviewField> fields,
        String content) {

    public ActionPreview {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
