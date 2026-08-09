package com.basic.dao.aiChat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.core.mybatis.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 聊天消息实体，用于按用户持久化会话中的用户提问和模型回答。
 */
@Getter
@Setter
@ToString
@TableName("ai_chat_message")
public class AiChatMessage extends BaseEntity {

    /**
     * 消息所属用户 ID，用于隔离不同用户的聊天记录。
     */
    private Long userId;

    /**
     * 消息角色，取值为 USER 或 ASSISTANT。
     */
    private String role;

    /**
     * 消息正文。
     */
    private String content;

    /**
     * 是否为用户停止生成或异常中断后保留的部分回答。
     */
    private Byte partial;
}
