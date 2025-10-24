package com.example.quickplan_ai.Service.impl;

import com.example.quickplan_ai.Service.ConversationService;
import com.example.quickplan_ai.entity.Conversation;
import com.example.quickplan_ai.mapper.ConversationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会话服务实现类
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public Conversation createConversation(String userId, String title) {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(title != null && !title.isBlank() ? title : "新对话")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(0)
                .build();

        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public boolean updateTitle(String conversationId, String title) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setTitle(title);
            conversation.setUpdatedAt(LocalDateTime.now());
            return conversationMapper.updateById(conversation) > 0;
        }
        return false;
    }

    @Override
    public List<Conversation> getUserConversations(String userId) {
        return conversationMapper.selectByUserId(userId);
    }

    @Override
    public List<Conversation> getRecentConversations(String userId, Integer limit) {
        return conversationMapper.selectRecentConversations(userId, limit);
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        return conversationMapper.deleteById(conversationId) > 0;
    }

    @Override
    public Conversation getConversationById(String conversationId) {
        return conversationMapper.selectById(conversationId);
    }
}
