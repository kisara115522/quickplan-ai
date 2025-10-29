package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.UserToken;
import org.apache.ibatis.annotations.*;

/**
 * 用户TokenMapper接口
 */
@Mapper
public interface UserTokenMapper {

    /**
     * 根据访问令牌查询
     */
    @Select("SELECT * FROM user_tokens WHERE access_token = #{accessToken} AND status = 1")
    UserToken findByAccessToken(String accessToken);

    /**
     * 根据刷新令牌查询
     */
    @Select("SELECT * FROM user_tokens WHERE refresh_token = #{refreshToken} AND status = 1")
    UserToken findByRefreshToken(String refreshToken);

    /**
     * 根据用户ID查询有效的Token
     */
    @Select("SELECT * FROM user_tokens WHERE user_id = #{userId} AND status = 1 " +
            "ORDER BY created_at DESC LIMIT 1")
    UserToken findValidByUserId(String userId);

    /**
     * 插入Token
     */
    @Insert("INSERT INTO user_tokens (user_id, access_token, refresh_token, " +
            "access_token_expires_at, refresh_token_expires_at, device_type, device_id, " +
            "ip_address, user_agent, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{accessToken}, #{refreshToken}, #{accessTokenExpiresAt}, " +
            "#{refreshTokenExpiresAt}, #{deviceType}, #{deviceId}, #{ipAddress}, " +
            "#{userAgent}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserToken userToken);

    /**
     * 使指定用户的所有Token失效
     */
    @Update("UPDATE user_tokens SET status = 0, updated_at = NOW() WHERE user_id = #{userId}")
    int invalidateByUserId(String userId);

    /**
     * 使指定Token失效
     */
    @Update("UPDATE user_tokens SET status = 0, updated_at = NOW() WHERE access_token = #{accessToken}")
    int invalidateByAccessToken(String accessToken);

    /**
     * 清理过期的Token
     */
    @Delete("DELETE FROM user_tokens WHERE status = 0 AND updated_at < DATE_SUB(NOW(), INTERVAL 7 DAY)")
    int cleanExpiredTokens();
}
