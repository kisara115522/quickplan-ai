package com.example.quickplan_ai.Service.impl;

import com.example.quickplan_ai.Service.MessageService;
import com.example.quickplan_ai.entity.Message;
import com.example.quickplan_ai.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Message saveUserMessage(String conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .createdAt(LocalDateTime.now())
                .isDeleted(0)
                .build();

        messageMapper.insert(message);
        return message;
    }

    @Override
    public Message saveAssistantMessage(String conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .createdAt(LocalDateTime.now())
                .isDeleted(0)
                .build();

        messageMapper.insert(message);
        return message;
    }

    @Override
    public List<Message> getConversationMessages(String conversationId) {
        return messageMapper.selectByConversationId(conversationId);
    }

    @Override
    public List<Message> getRecentMessages(String conversationId, Integer limit) {
        return messageMapper.selectRecentMessages(conversationId, limit);
    }

    @Override
    public Integer countMessages(String conversationId) {
        return messageMapper.countByConversationId(conversationId);
    }

    @Override
    public boolean deleteConversationMessages(String conversationId) {
        return messageMapper.deleteByConversationId(conversationId) > 0;
    }
}
