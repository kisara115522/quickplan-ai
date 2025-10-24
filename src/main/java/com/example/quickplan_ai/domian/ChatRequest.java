package com.example.quickplan_ai.domian;

import lombok.Data;

@Data
public class ChatRequest {
    // 用户发送的消息
    private String message;
    // 会话唯一id
    private String memoryId;
    // 用户唯一id
    private String UserId;
}
