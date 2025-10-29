package com.example.quickplan_ai.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * Token有效期(秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private String userId;
        private String phone;
        private String email;
        private String nickname;
        private String avatar;
        private String createdAt;
        private String loginType;
    }
}
