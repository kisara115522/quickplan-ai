package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {

    /**
     * 插入消息
     */
    @Insert("INSERT INTO message(conversation_id, role, content, created_at, is_deleted) " +
            "VALUES(#{conversationId}, #{role}, #{content}, #{createdAt}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    /**
     * 根据ID删除(逻辑删除)
     */
    @Update("UPDATE message SET is_deleted=1 WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 根据会话ID删除所有消息(逻辑删除)
     */
    @Update("UPDATE message SET is_deleted=1 WHERE conversation_id=#{conversationId}")
    int deleteByConversationId(String conversationId);

    /**
     * 根据会话ID查询消息列表(按创建时间正序)
     */
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY created_at ASC")
    List<Message> selectByConversationId(String conversationId);

    /**
     * 查询会话的最新N条消息
     */
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<Message> selectRecentMessages(@Param("conversationId") String conversationId, @Param("limit") Integer limit);

    /**
     * 统计会话的消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId} AND is_deleted = 0")
    Integer countByConversationId(String conversationId);
}
