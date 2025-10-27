package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.entity.OcrReminder;

import java.util.List;

/**
 * OCR提醒Service接口
 */
public interface OcrReminderService {

    /**
     * 创建OCR提醒
     */
    OcrReminder createReminder(OcrReminder reminder);

    /**
     * 根据ID获取提醒
     */
    OcrReminder getReminderById(String id);

    /**
     * 获取用户所有提醒
     */
    List<OcrReminder> getUserReminders(String userId);

    /**
     * 获取用户未完成的提醒
     */
    List<OcrReminder> getUncompletedReminders(String userId);

    /**
     * 获取会话的提醒
     */
    List<OcrReminder> getConversationReminders(String conversationId);

    /**
     * 更新提醒
     */
    boolean updateReminder(OcrReminder reminder);

    /**
     * 标记为完成
     */
    boolean markAsCompleted(String id);

    /**
     * 删除提醒
     */
    boolean deleteReminder(String id);
}
