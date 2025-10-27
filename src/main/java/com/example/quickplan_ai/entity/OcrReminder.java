package com.example.quickplan_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OCR提醒实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrReminder {

    /**
     * 提醒ID (UUID)
     */
    private String id;

    /**
     * 来源会话ID (可选)
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 提醒标题
     */
    private String title;

    /**
     * 备注描述
     */
    private String description;

    /**
     * 提醒时间 (可选)
     */
    private LocalDateTime remindTime;

    /**
     * 是否完成: 0-未完成, 1-已完成
     */
    private Integer isCompleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    private Integer isDeleted;
}
