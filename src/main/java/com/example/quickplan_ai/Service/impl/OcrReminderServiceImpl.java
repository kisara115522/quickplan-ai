package com.example.quickplan_ai.Service.impl;

import com.example.quickplan_ai.entity.OcrReminder;
import com.example.quickplan_ai.mapper.OcrReminderMapper;
import com.example.quickplan_ai.Service.OcrReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OCR提醒Service实现类
 */
@Service
public class OcrReminderServiceImpl implements OcrReminderService {

    @Autowired
    private OcrReminderMapper ocrReminderMapper;

    @Override
    public OcrReminder createReminder(OcrReminder reminder) {
        if (reminder.getId() == null || reminder.getId().isBlank()) {
            reminder.setId(UUID.randomUUID().toString());
        }
        reminder.setIsCompleted(0);
        reminder.setIsDeleted(0);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        ocrReminderMapper.insert(reminder);
        return reminder;
    }

    @Override
    public OcrReminder getReminderById(String id) {
        return ocrReminderMapper.selectById(id);
    }

    @Override
    public List<OcrReminder> getUserReminders(String userId) {
        return ocrReminderMapper.selectByUserId(userId);
    }

    @Override
    public List<OcrReminder> getUncompletedReminders(String userId) {
        return ocrReminderMapper.selectUncompletedByUserId(userId);
    }

    @Override
    public List<OcrReminder> getConversationReminders(String conversationId) {
        return ocrReminderMapper.selectByConversationId(conversationId);
    }

    @Override
    public boolean updateReminder(OcrReminder reminder) {
        reminder.setUpdatedAt(LocalDateTime.now());
        return ocrReminderMapper.updateById(reminder) > 0;
    }

    @Override
    public boolean markAsCompleted(String id) {
        return ocrReminderMapper.markAsCompleted(id) > 0;
    }

    @Override
    public boolean deleteReminder(String id) {
        return ocrReminderMapper.deleteById(id) > 0;
    }
}
