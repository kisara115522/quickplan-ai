package com.example.quickplan_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话实体类
 * 用于存储用户的对话会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    /**
     * 会话ID,主键,使用UUID作为会话唯一标识
     */
    private String id;

    /**
     * 用户ID,关联到用户表
     */
    private String userId;

    /**
     * 会话标题,默认为第一条消息的前30个字符
     */
    private String title;

    /**
     * 会话创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 会话最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer isDeleted;
}
