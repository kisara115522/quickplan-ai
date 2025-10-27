package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.AiChatService;
import com.example.quickplan_ai.Service.ConversationService;
import com.example.quickplan_ai.Service.MessageService;
import com.example.quickplan_ai.domian.ChatRequest;
import com.example.quickplan_ai.entity.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI对话Controller
 * 负责处理用户与AI的实时对话
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class Aicontroller {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    /**
     * AI对话接口 - 流式返回
     */
    @PostMapping("/chat")
    public Flux<String> sendMessage(@RequestBody ChatRequest request) {
        // 1. 验证请求参数
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Flux.just("消息为空");
        }

        if (request.getMemoryId() == null || request.getMemoryId().isBlank()) {
            return Flux.just("会话ID不能为空");
        }

        // 2. 保存用户消息到数据库
        messageService.saveUserMessage(request.getMemoryId(), request.getMessage());

        // 3. 调用 AI 获取流式回复
        AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

        return aiChatService.chat(request.getMemoryId(), request.getMessage())
                .doOnNext(chunk -> {
                    // 累积每一块响应内容
                    fullResponse.get().append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式响应完成后,保存完整的AI消息到数据库
                    String completeResponse = fullResponse.get().toString();
                    if (!completeResponse.isBlank()) {
                        messageService.saveAssistantMessage(request.getMemoryId(), completeResponse);

                        // 更新会话的更新时间
                        Conversation conversation = conversationService.getConversationById(request.getMemoryId());
                        if (conversation != null) {
                            // 如果会话标题还是"新对话",则自动生成标题
                            if ("新对话".equals(conversation.getTitle())) {
                                String autoTitle = request.getMessage().length() > 30
                                        ? request.getMessage().substring(0, 30) + "..."
                                        : request.getMessage();
                                conversationService.updateTitle(request.getMemoryId(), autoTitle);
                            }
                        }
                    }
                })
                .doOnError(error -> {
                    // 详细记录错误日志
                    System.err.println("AI服务错误: " + error.getMessage());
                    error.printStackTrace();
                })
                .onErrorResume(error -> {
                    // 当发生错误时，返回友好的错误消息给前端
                    String errorMsg = "抱歉，AI服务暂时不可用";
                    if (error.getMessage() != null) {
                        if (error.getMessage().contains("Connection reset")) {
                            errorMsg = "AI服务连接中断，请重试";
                        } else if (error.getMessage().contains("timeout")) {
                            errorMsg = "AI服务响应超时，请重试";
                        }
                    }
                    return Flux.just(errorMsg);
                });
    }

    /**
     * 创建新会话
     * POST /api/ai/chat/new
     */
    @PostMapping("/chat/new")
    public Map<String, Object> startNewConversation(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String userId = request.get("userId");
        String title = request.getOrDefault("title", "新对话");

        // 验证用户ID
        if (userId == null || userId.isBlank()) {
            response.put("success", false);
            response.put("message", "用户ID不能为空");
            response.put("data", null);
            return response;
        }

        // 创建新会话
        Conversation conversation = conversationService.createConversation(userId, title);

        response.put("success", true);
        response.put("data", conversation);
        response.put("message", "创建成功");
        return response;
    }
}
