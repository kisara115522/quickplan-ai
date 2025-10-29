package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.entity.User;
import com.example.quickplan_ai.mapper.UserMapper;
import com.example.quickplan_ai.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        logger.info("获取用户信息请求");

        try {
            // 从请求头获取Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未提供Token");
                response.put("data", null);
                return ResponseEntity.status(401).body(response);
            }

            token = token.substring(7);

            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                response.put("data", null);
                return ResponseEntity.status(401).body(response);
            }

            // 获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "无法解析用户信息");
                response.put("data", null);
                return ResponseEntity.status(401).body(response);
            }

            // 查询用户信息
            User user = userMapper.findByUserId(userId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }

            // 构造响应
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("phone", user.getPhone());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("gender", user.getGender());
            userInfo.put("birthday", user.getBirthday());
            userInfo.put("bio", user.getBio());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("loginType", user.getLoginType());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取成功");
            response.put("data", userInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器内部错误");
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }
}
