package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.OcrReminder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * OCR提醒 Mapper接口
 */
@Mapper
public interface OcrReminderMapper {

    /**
     * 插入OCR提醒
     */
    @Insert("INSERT INTO ocr_reminder(id, conversation_id, user_id, title, description, remind_time, is_completed, is_deleted) "
            +
            "VALUES(#{id}, #{conversationId}, #{userId}, #{title}, #{description}, #{remindTime}, #{isCompleted}, #{isDeleted})")
    int insert(OcrReminder reminder);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM ocr_reminder WHERE id = #{id} AND is_deleted = 0")
    OcrReminder selectById(String id);

    /**
     * 根据用户ID查询所有提醒
     */
    @Select("SELECT * FROM ocr_reminder WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<OcrReminder> selectByUserId(String userId);

    /**
     * 根据会话ID查询提醒
     */
    @Select("SELECT * FROM ocr_reminder WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<OcrReminder> selectByConversationId(String conversationId);

    /**
     * 查询未完成的提醒
     */
    @Select("SELECT * FROM ocr_reminder WHERE user_id = #{userId} AND is_completed = 0 AND is_deleted = 0 ORDER BY remind_time ASC")
    List<OcrReminder> selectUncompletedByUserId(String userId);

    /**
     * 更新提醒
     */
    @Update("UPDATE ocr_reminder SET title = #{title}, description = #{description}, " +
            "remind_time = #{remindTime}, is_completed = #{isCompleted} WHERE id = #{id}")
    int updateById(OcrReminder reminder);

    /**
     * 标记为完成
     */
    @Update("UPDATE ocr_reminder SET is_completed = 1 WHERE id = #{id}")
    int markAsCompleted(String id);

    /**
     * 逻辑删除
     */
    @Update("UPDATE ocr_reminder SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(String id);
}
