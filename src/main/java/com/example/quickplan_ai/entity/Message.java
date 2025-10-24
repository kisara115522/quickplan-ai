package com.example.quickplan_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息实体类
 * 用于存储对话中的每一条消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 消息ID,主键,自增
     */
    private Long id;

    /**
     * 所属会话ID
     */
    private String conversationId;

    /**
     * 消息角色: user-用户消息, assistant-AI助手消息
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer isDeleted;
}
