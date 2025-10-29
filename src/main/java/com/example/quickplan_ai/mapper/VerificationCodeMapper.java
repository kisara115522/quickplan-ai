package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.VerificationCode;
import org.apache.ibatis.annotations.*;

/**
 * 验证码Mapper接口
 */
@Mapper
public interface VerificationCodeMapper {

    /**
     * 根据手机号和类型查询最新的有效验证码
     */
    @Select("SELECT * FROM verification_codes WHERE phone = #{phone} AND type = #{type} " +
            "AND status = 0 AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    VerificationCode findValidByPhone(@Param("phone") String phone, @Param("type") String type);

    /**
     * 根据邮箱和类型查询最新的有效验证码
     */
    @Select("SELECT * FROM verification_codes WHERE email = #{email} AND type = #{type} " +
            "AND status = 0 AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    VerificationCode findValidByEmail(@Param("email") String email, @Param("type") String type);

    /**
     * 插入验证码
     */
    @Insert("INSERT INTO verification_codes (phone, email, code, type, status, ip_address, " +
            "expires_at, created_at) " +
            "VALUES (#{phone}, #{email}, #{code}, #{type}, #{status}, #{ipAddress}, " +
            "#{expiresAt}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VerificationCode verificationCode);

    /**
     * 标记验证码为已使用
     */
    @Update("UPDATE verification_codes SET status = 1, used_at = NOW() WHERE id = #{id}")
    int markAsUsed(Long id);

    /**
     * 标记所有过期的验证码
     */
    @Update("UPDATE verification_codes SET status = 2 WHERE status = 0 AND expires_at < NOW()")
    int markExpiredCodes();

    /**
     * 查询指定手机号今天发送的验证码数量
     */
    @Select("SELECT COUNT(*) FROM verification_codes WHERE phone = #{phone} " +
            "AND created_at >= CURDATE()")
    int countTodayByPhone(String phone);

    /**
     * 查询指定邮箱今天发送的验证码数量
     */
    @Select("SELECT COUNT(*) FROM verification_codes WHERE email = #{email} " +
            "AND created_at >= CURDATE()")
    int countTodayByEmail(String email);

    /**
     * 清理过期的验证码(超过1天)
     */
    @Delete("DELETE FROM verification_codes WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 DAY)")
    int cleanExpiredCodes();
}
