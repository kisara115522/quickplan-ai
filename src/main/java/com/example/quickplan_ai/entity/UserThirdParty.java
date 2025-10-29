package com.example.quickplan_ai.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 第三方登录绑定实体类
 */
@Data
public class UserThirdParty {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 第三方平台类型: wechat/qq/weibo
     */
    private String thirdPartyType;

    /**
     * 第三方平台OpenID
     */
    private String openId;

    /**
     * 第三方平台UnionID
     */
    private String unionId;

    /**
     * 第三方平台昵称
     */
    private String nickname;

    /**
     * 第三方平台头像
     */
    private String avatar;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * Token过期时间(秒)
     */
    private Long expiresIn;

    /**
     * 绑定时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
