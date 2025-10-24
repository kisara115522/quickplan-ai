package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 保存用户消息
     */
    Message saveUserMessage(String conversationId, String content);

    /**
     * 保存AI助手消息
     */
    Message saveAssistantMessage(String conversationId, String content);

    /**
     * 获取会话的所有消息
     */
    List<Message> getConversationMessages(String conversationId);

    /**
     * 获取会话的最近N条消息
     */
    List<Message> getRecentMessages(String conversationId, Integer limit);

    /**
     * 统计会话的消息数量
     */
    Integer countMessages(String conversationId);

    /**
     * 删除会话的所有消息
     */
    boolean deleteConversationMessages(String conversationId);
}
