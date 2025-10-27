package com.example.quickplan_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日程实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    /**
     * 日程ID (UUID)
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 日程标题
     */
    private String title;

    /**
     * 地点
     */
    private String location;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 时间
     */
    private LocalTime time;

    /**
     * 备注描述
     */
    private String description;

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
