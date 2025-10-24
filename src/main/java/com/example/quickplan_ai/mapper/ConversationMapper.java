package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper {

    /**
     * 插入会话
     */
    @Insert("INSERT INTO conversation(id, user_id, title, created_at, updated_at, is_deleted) " +
            "VALUES(#{id}, #{userId}, #{title}, #{createdAt}, #{updatedAt}, #{isDeleted})")
    int insert(Conversation conversation);

    /**
     * 根据ID更新会话
     */
    @Update("UPDATE conversation SET title=#{title}, updated_at=#{updatedAt} WHERE id=#{id}")
    int updateById(Conversation conversation);

    /**
     * 根据ID删除(逻辑删除)
     */
    @Update("UPDATE conversation SET is_deleted=1 WHERE id=#{id}")
    int deleteById(String id);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM conversation WHERE id=#{id} AND is_deleted=0")
    Conversation selectById(String id);

    /**
     * 根据用户ID查询会话列表(按更新时间倒序)
     */
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY updated_at DESC")
    List<Conversation> selectByUserId(String userId);

    /**
     * 查询最近的N个会话
     */
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY updated_at DESC LIMIT #{limit}")
    List<Conversation> selectRecentConversations(@Param("userId") String userId, @Param("limit") Integer limit);
}
