package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.UserThirdParty;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 第三方登录绑定Mapper接口
 */
@Mapper
public interface UserThirdPartyMapper {

    /**
     * 根据第三方类型和OpenID查询绑定信息
     */
    @Select("SELECT * FROM user_third_party WHERE third_party_type = #{thirdPartyType} " +
            "AND open_id = #{openId}")
    UserThirdParty findByThirdPartyAndOpenId(@Param("thirdPartyType") String thirdPartyType,
            @Param("openId") String openId);

    /**
     * 根据用户ID查询所有第三方绑定
     */
    @Select("SELECT * FROM user_third_party WHERE user_id = #{userId}")
    List<UserThirdParty> findByUserId(String userId);

    /**
     * 插入第三方绑定
     */
    @Insert("INSERT INTO user_third_party (user_id, third_party_type, open_id, union_id, " +
            "nickname, avatar, access_token, refresh_token, expires_in, created_at, updated_at) " +
            "VALUES (#{userId}, #{thirdPartyType}, #{openId}, #{unionId}, #{nickname}, " +
            "#{avatar}, #{accessToken}, #{refreshToken}, #{expiresIn}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserThirdParty userThirdParty);

    /**
     * 更新第三方绑定信息
     */
    @Update("UPDATE user_third_party SET nickname = #{nickname}, avatar = #{avatar}, " +
            "access_token = #{accessToken}, refresh_token = #{refreshToken}, " +
            "expires_in = #{expiresIn}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND third_party_type = #{thirdPartyType}")
    int update(UserThirdParty userThirdParty);

    /**
     * 删除第三方绑定
     */
    @Delete("DELETE FROM user_third_party WHERE user_id = #{userId} AND third_party_type = #{thirdPartyType}")
    int delete(@Param("userId") String userId, @Param("thirdPartyType") String thirdPartyType);
}
