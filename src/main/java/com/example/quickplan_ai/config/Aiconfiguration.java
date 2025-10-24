package com.example.quickplan_ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Aiconfiguration {

    @Autowired
    private ChatMemoryStore redisChatMemoryStore;

    @Bean
    public ChatMemory chatMemory() {
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(100) // 设置最大消息数
                .build();
        return memory;
    }

    @Bean
    public ChatMemoryProvider redisChatMemoryStore() {
        ChatMemoryProvider memory = new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .maxMessages(100) // 设置最大消息数
                        .chatMemoryStore(redisChatMemoryStore)
                        .id(memoryId)
                        .build();
            }
        };
        return memory;
    }

    @Bean
    public EmbeddingStore<TextSegment> store() {
        // 创建一个空的内存嵌入存储
        // 如果需要 RAG 功能,可以在这里加载文档
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        return store;
    }

}
