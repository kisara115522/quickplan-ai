package com.example.quickplan_ai.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Data
public class VerificationCode {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 验证码类型: login/register/reset_password
     */
    private String type;

    /**
     * 状态: 0-未使用, 1-已使用, 2-已过期
     */
    private Integer status;

    /**
     * 请求IP
     */
    private String ipAddress;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;
}
