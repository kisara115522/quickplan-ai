package com.example.quickplan_ai.Service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface AiChatService {

    //TODO: 系统消息设置
    @SystemMessage("你是一个日历小助手，专门提醒用户今天要做什么")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
