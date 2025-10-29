package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户ID查询用户
     */
    @Select("SELECT * FROM users WHERE user_id = #{userId} AND deleted = 0")
    User findByUserId(String userId);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User findByEmail(String email);

    /**
     * 插入新用户
     */
    @Insert("INSERT INTO users (user_id, phone, email, password, nickname, avatar, gender, " +
            "status, login_type, created_at, updated_at) " +
            "VALUES (#{userId}, #{phone}, #{email}, #{password}, #{nickname}, #{avatar}, #{gender}, " +
            "#{status}, #{loginType}, #{createdAt}, #{updatedAt})")
    int insert(User user);

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET phone = #{phone}, email = #{email}, password = #{password}, " +
            "nickname = #{nickname}, avatar = #{avatar}, gender = #{gender}, birthday = #{birthday}, " +
            "bio = #{bio}, status = #{status}, last_login_time = #{lastLoginTime}, " +
            "last_login_ip = #{lastLoginIp}, updated_at = #{updatedAt} " +
            "WHERE user_id = #{userId}")
    int update(User user);

    /**
     * 更新最后登录信息
     */
    @Update("UPDATE users SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp}, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int updateLastLogin(@Param("userId") String userId,
            @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime,
            @Param("lastLoginIp") String lastLoginIp);

    /**
     * 软删除用户
     */
    @Update("UPDATE users SET deleted = 1, updated_at = NOW() WHERE user_id = #{userId}")
    int softDelete(String userId);

    /**
     * 检查手机号是否已存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone} AND deleted = 0")
    int existsByPhone(String phone);

    /**
     * 检查邮箱是否已存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND deleted = 0")
    int existsByEmail(String email);
}
