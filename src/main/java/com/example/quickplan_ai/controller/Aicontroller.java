package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.AiChatService;
import com.example.quickplan_ai.Service.ConversationService;
import com.example.quickplan_ai.Service.MessageService;
import com.example.quickplan_ai.Service.ScheduleTools;
import com.example.quickplan_ai.domian.ChatRequest;
import com.example.quickplan_ai.entity.Conversation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI对话Controller
 * 负责处理用户与AI的实时对话
 */
@RestController
@RequestMapping("/api/ai")
public class Aicontroller {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ScheduleTools scheduleTools;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI对话接口 - 非流式返回
     * 返回格式: {
     * "success": true/false,
     * "message": "AI的回复内容",
     * "data": null
     * }
     */
    @PostMapping("/chat")
    public Map<String, Object> sendMessage(@RequestBody ChatRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 验证请求参数
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                response.put("success", false);
                response.put("message", "消息为空");
                response.put("data", null);
                return response;
            }

            if (request.getMemoryId() == null || request.getMemoryId().isBlank()) {
                response.put("success", false);
                response.put("message", "会话ID不能为空");
                response.put("data", null);
                return response;
            }

            if (request.getUserId() == null || request.getUserId().isBlank()) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                response.put("data", null);
                return response;
            }

            // 2. 保存用户消息到数据库
            messageService.saveUserMessage(request.getMemoryId(), request.getMessage());

            // 3. 获取当前日期
            String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            // 4. 调用AI服务(非流式,支持工具调用)
            String aiResponse = aiChatService.chat(
                    request.getMemoryId(),
                    request.getMessage(),
                    request.getUserId(),
                    currentDate);

            System.out.println("========== AI响应内容 ==========");
            System.out.println(aiResponse);
            System.out.println("================================");

            // 5. 检查并手动执行工具调用
            // 因为 Qwen2.5 模型不支持标准的 function calling,
            // 它会将工具调用以文本形式返回,需要手动解析和执行
            String finalResponse = aiResponse;

            // 更宽松的检测条件:只要包含大括号和关键字段
            boolean containsJson = aiResponse != null &&
                    (aiResponse.contains("{") && aiResponse.contains("}"));
            boolean containsToolName = aiResponse != null &&
                    (aiResponse.contains("addSchedule") ||
                            aiResponse.contains("getSchedulesByDate") ||
                            aiResponse.contains("deleteSchedule"));

            System.out.println("工具调用检测: containsJson=" + containsJson + ", containsToolName=" + containsToolName);

            if (containsJson && containsToolName) {
                System.out.println("✓ 检测到工具调用,尝试手动解析并执行...");
                finalResponse = executeToolCallManually(aiResponse, request.getUserId(), request.getMemoryId(),
                        currentDate);
            } else {
                System.out.println("✗ 未检测到工具调用,直接返回AI响应");
            }

            // 6. 保存AI回复到数据库
            if (finalResponse != null && !finalResponse.isBlank()) {
                messageService.saveAssistantMessage(request.getMemoryId(), finalResponse);

                // 更新会话的更新时间和标题
                Conversation conversation = conversationService.getConversationById(request.getMemoryId());
                if (conversation != null && "新对话".equals(conversation.getTitle())) {
                    String autoTitle = request.getMessage().length() > 30
                            ? request.getMessage().substring(0, 30) + "..."
                            : request.getMessage();
                    conversationService.updateTitle(request.getMemoryId(), autoTitle);
                }
            }

            // 6. 返回成功响应
            response.put("success", true);
            response.put("message", finalResponse);
            response.put("data", null);

        } catch (Exception e) {
            // 详细记录错误日志
            System.err.println("AI服务错误: " + e.getMessage());
            e.printStackTrace();

            // 返回错误响应
            String errorMsg = "抱歉，AI服务暂时不可用";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Connection reset")) {
                    errorMsg = "AI服务连接中断，请重试";
                } else if (e.getMessage().contains("timeout")) {
                    errorMsg = "AI服务响应超时，请重试";
                }
            }

            response.put("success", false);
            response.put("message", errorMsg);
            response.put("data", null);
        }

        return response;
    }

    /**
     * 手动解析并执行工具调用
     * 因为 Qwen2.5 模型不支持标准的 OpenAI function calling
     * 需要从文本中提取 JSON 格式的工具调用并手动执行
     */
    private String executeToolCallManually(String aiResponse, String userId, String memoryId, String currentDate) {
        try {
            System.out.println("开始解析工具调用...");

            // 尝试多种 JSON 提取模式
            String jsonStr = null;

            // 模式1: 标准的 {"name": "xxx", "arguments": {...}}
            Pattern pattern1 = Pattern.compile("\\{[\\s\\S]*?\"name\"[\\s\\S]*?\"arguments\"[\\s\\S]*?\\}\\s*\\}",
                    Pattern.DOTALL);
            Matcher matcher1 = pattern1.matcher(aiResponse);
            if (matcher1.find()) {
                jsonStr = matcher1.group();
                System.out.println("使用模式1提取");
            }

            // 模式2: 可能缺少外层大括号,只有 "name": "xxx", "arguments": {...}
            if (jsonStr == null) {
                Pattern pattern2 = Pattern.compile(
                        "\"name\"\\s*:\\s*\"(\\w+)\"[\\s\\S]*?\"arguments\"\\s*:\\s*\\{[\\s\\S]*?\\}", Pattern.DOTALL);
                Matcher matcher2 = pattern2.matcher(aiResponse);
                if (matcher2.find()) {
                    String matched = matcher2.group();
                    jsonStr = "{" + matched + "}";
                    System.out.println("使用模式2提取");
                }
            }

            // 模式3: 最宽松的匹配,提取任何包含 name 和 arguments 的 JSON
            if (jsonStr == null) {
                Pattern pattern3 = Pattern.compile("\\{[^}]*\"name\"[^}]*\\{[^}]*\\}[^}]*\\}", Pattern.DOTALL);
                Matcher matcher3 = pattern3.matcher(aiResponse);
                if (matcher3.find()) {
                    jsonStr = matcher3.group();
                    System.out.println("使用模式3提取");
                }
            }

            if (jsonStr == null) {
                System.out.println("✗ 无法提取JSON,返回原始响应");
                return aiResponse;
            }

            System.out.println("提取到的工具调用JSON:");
            System.out.println(jsonStr);

            // 解析 JSON
            JsonNode toolCall = objectMapper.readTree(jsonStr);

            if (!toolCall.has("name")) {
                System.out.println("✗ JSON中没有name字段");
                return aiResponse;
            }

            String toolName = toolCall.get("name").asText();
            System.out.println("工具名称: " + toolName);

            if (!toolCall.has("arguments")) {
                System.out.println("✗ JSON中没有arguments字段");
                return aiResponse;
            }

            JsonNode arguments = toolCall.get("arguments");
            System.out.println("参数内容: " + arguments.toString());

            String toolResult = null;

            // 根据工具名称执行相应操作
            switch (toolName) {
                case "addSchedule":
                    System.out.println("执行 addSchedule 工具...");
                    toolResult = scheduleTools.addSchedule(
                            arguments.has("userId") ? arguments.get("userId").asText() : userId,
                            arguments.has("title") ? arguments.get("title").asText() : "",
                            arguments.has("date") ? arguments.get("date").asText() : "",
                            arguments.has("time") ? arguments.get("time").asText() : "",
                            arguments.has("location") ? arguments.get("location").asText() : "未指定",
                            arguments.has("description") ? arguments.get("description").asText() : "");
                    break;

                case "getSchedulesByDate":
                    toolResult = scheduleTools.getSchedulesByDate(
                            arguments.get("userId").asText(),
                            arguments.get("date").asText());
                    break;

                case "deleteSchedule":
                    toolResult = scheduleTools.deleteSchedule(
                            arguments.get("userId").asText(),
                            arguments.get("scheduleId").asText());
                    break;

                default:
                    System.err.println("未知的工具名称: " + toolName);
                    return aiResponse;
            }

            System.out.println("工具执行结果: " + toolResult);

            // 如果工具执行成功,只返回工具的结果
            // 否则返回完整的 AI 响应
            if (toolResult != null && toolResult.contains("✅")) {
                return toolResult;
            } else {
                return toolResult != null ? toolResult : aiResponse;
            }

        } catch (Exception e) {
            System.err.println("手动执行工具调用失败: " + e.getMessage());
            e.printStackTrace();
            return aiResponse;
        }
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
