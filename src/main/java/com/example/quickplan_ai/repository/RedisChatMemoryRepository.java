package com.example.quickplan_ai.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Redis 聊天记忆存储实现
 * Key格式: quickplan:chat:memory:{conversationId}
 * 
 * 说明:
 * - conversationId 在数据库中已关联 userId,实现了间接的用户隔离
 * - Redis key 包含业务前缀 "quickplan:chat:memory:"
 * - 支持按 conversationId 维度的增删改查
 */
@Repository
public class RedisChatMemoryRepository implements ChatMemoryStore {

    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemoryRepository.class);

    /**
     * Redis Key 前缀
     * 格式: quickplan:chat:memory:{conversationId}
     */
    private static final String KEY_PREFIX = "quickplan:chat:memory:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 构建 Redis Key
     * 
     * @param memoryId 会话ID (conversationId)
     * @return 完整的 Redis Key
     */
    private String buildKey(Object memoryId) {
        return KEY_PREFIX + memoryId.toString();
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = buildKey(memoryId);
        logger.debug("从 Redis 获取聊天记录, key: {}", key);

        String json = stringRedisTemplate.opsForValue().get(key);
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);

        logger.debug("获取到 {} 条消息", list != null ? list.size() : 0);
        return list;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String key = buildKey(memoryId);
        String json = ChatMessageSerializer.messagesToJson(list);

        stringRedisTemplate.opsForValue().set(key, json);
        logger.debug("更新 Redis 聊天记录, key: {}, 消息数: {}", key, list.size());
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = buildKey(memoryId);
        stringRedisTemplate.delete(key);
        logger.debug("删除 Redis 聊天记录, key: {}", key);
    }

    /**
     * 批量删除指定会话的聊天记录
     * 
     * @param conversationIds 会话ID列表
     */
    public void deleteMessagesByConversationIds(List<String> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return;
        }

        List<String> keys = conversationIds.stream()
                .map(this::buildKey)
                .toList();

        Long deleted = stringRedisTemplate.delete(keys);
        logger.info("批量删除 Redis 聊天记录, 删除数量: {}", deleted);
    }

    /**
     * 清理所有聊天记录 (慎用)
     * 仅用于维护或测试
     */
    public void clearAll() {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            Long deleted = stringRedisTemplate.delete(keys);
            logger.warn("清空所有 Redis 聊天记录, 删除数量: {}", deleted);
        }
    }

    /**
     * 获取 Redis 中存储的聊天记录总数
     */
    public long countAll() {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}
