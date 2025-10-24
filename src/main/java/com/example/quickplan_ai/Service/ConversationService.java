package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.entity.Conversation;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {

    /**
     * 创建新会话
     */
    Conversation createConversation(String userId, String title);

    /**
     * 更新会话标题
     */
    boolean updateTitle(String conversationId, String title);

    /**
     * 获取用户的所有会话列表
     */
    List<Conversation> getUserConversations(String userId);

    /**
     * 获取用户最近的N个会话
     */
    List<Conversation> getRecentConversations(String userId, Integer limit);

    /**
     * 删除会话(逻辑删除)
     */
    boolean deleteConversation(String conversationId);

    /**
     * 根据会话ID获取会话详情
     */
    Conversation getConversationById(String conversationId);
}
