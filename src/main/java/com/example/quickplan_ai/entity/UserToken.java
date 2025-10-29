package com.example.quickplan_ai.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户Token实体类
 */
@Data
public class UserToken {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 访问令牌过期时间
     */
    private LocalDateTime accessTokenExpiresAt;

    /**
     * 刷新令牌过期时间
     */
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 设备类型: ios/android/web
     */
    private String deviceType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 登录IP
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 状态: 0-已失效, 1-有效
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
