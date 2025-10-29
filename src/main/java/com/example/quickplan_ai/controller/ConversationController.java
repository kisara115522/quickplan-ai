package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.ConversationService;
import com.example.quickplan_ai.Service.MessageService;
import com.example.quickplan_ai.entity.Conversation;
import com.example.quickplan_ai.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createConversation(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String title = request.getOrDefault("title", "新对话");

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户ID不能为空"));
        }

        Conversation conversation = conversationService.createConversation(userId, title);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", conversation);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<Map<String, Object>> getConversationList(@PathVariable String userId) {
        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        List<Conversation> conversations = conversationService.getUserConversations(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", conversations);
        response.put("total", conversations.size());
        response.put("message", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<Map<String, Object>> getRecentConversations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<Conversation> conversations = conversationService.getRecentConversations(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", conversations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversationDetail(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Conversation conversation = conversationService.getConversationById(conversationId);

        if (conversation == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "会话不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!conversation.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无权访问该会话");
            errorResponse.put("data", null);
            return ResponseEntity.status(403).body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", conversation);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "会话不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!conversation.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无权访问该会话");
            errorResponse.put("data", null);
            return ResponseEntity.status(403).body(errorResponse);
        }

        List<Message> messages = messageService.getConversationMessages(conversationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", messages);
        response.put("total", messages.size());
        response.put("message", null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-title")
    public ResponseEntity<Map<String, Object>> updateConversationTitle(@RequestBody Map<String, String> request) {
        String conversationId = request.get("conversationId");
        String userId = request.get("userId");
        String title = request.get("title");

        if (conversationId == null || title == null || userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "参数不完整");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "会话不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!conversation.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无权修改该会话");
            errorResponse.put("data", null);
            return ResponseEntity.status(403).body(errorResponse);
        }

        boolean success = conversationService.updateTitle(conversationId, title);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "更新成功" : "更新失败");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "会话不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!conversation.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无权删除该会话");
            errorResponse.put("data", null);
            return ResponseEntity.status(403).body(errorResponse);
        }

        messageService.deleteConversationMessages(conversationId);
        boolean success = conversationService.deleteConversation(conversationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");
        response.put("data", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversationStats(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Conversation conversation = conversationService.getConversationById(conversationId);

        if (conversation == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "会话不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!conversation.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "无权访问该会话");
            errorResponse.put("data", null);
            return ResponseEntity.status(403).body(errorResponse);
        }

        Integer messageCount = messageService.countMessages(conversationId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("conversation", conversation);
        stats.put("messageCount", messageCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }
}
