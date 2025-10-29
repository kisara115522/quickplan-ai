package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.AuthService;
import com.example.quickplan_ai.dto.*;
import com.example.quickplan_ai.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 发送验证码
     */
    @PostMapping("/phone/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(
            @Valid @RequestBody SendCodeRequest request,
            HttpServletRequest httpRequest) {

        logger.info("发送验证码请求: phone={}, type={}", request.getPhone(), request.getType());

        try {
            String ipAddress = IpUtil.getClientIp(httpRequest);
            authService.sendVerificationCode(request.getPhone(), request.getType(), ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "验证码已发送");
            response.put("data", Map.of("expiresIn", 300));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("发送验证码失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 手机号注册
     */
    @PostMapping("/phone/register")
    public ResponseEntity<Map<String, Object>> registerByPhone(
            @Valid @RequestBody PhoneRegisterRequest request,
            HttpServletRequest httpRequest) {

        logger.info("手机号注册请求: phone={}", request.getPhone());

        try {
            LoginResponse loginResponse = authService.registerByPhone(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("手机号注册失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 邮箱注册
     */
    @PostMapping("/email/register")
    public ResponseEntity<Map<String, Object>> registerByEmail(
            @Valid @RequestBody EmailRegisterRequest request,
            HttpServletRequest httpRequest) {

        logger.info("邮箱注册请求: email={}", request.getEmail());

        try {
            LoginResponse loginResponse = authService.registerByEmail(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("邮箱注册失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 手机号登录
     */
    @PostMapping("/phone/login")
    public ResponseEntity<Map<String, Object>> loginByPhone(
            @Valid @RequestBody PhoneLoginRequest request,
            HttpServletRequest httpRequest) {

        logger.info("手机号登录请求: phone={}", request.getPhone());

        try {
            LoginResponse loginResponse = authService.loginByPhone(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("手机号登录失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 邮箱登录
     */
    @PostMapping("/email/login")
    public ResponseEntity<Map<String, Object>> loginByEmail(
            @Valid @RequestBody EmailLoginRequest request,
            HttpServletRequest httpRequest) {

        logger.info("邮箱登录请求: email={}", request.getEmail());

        try {
            LoginResponse loginResponse = authService.loginByEmail(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("邮箱登录失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest httpRequest) {

        String refreshToken = requestBody.get("refreshToken");
        logger.info("刷新Token请求");

        try {
            LoginResponse loginResponse = authService.refreshToken(refreshToken, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token刷新成功");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("刷新Token失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        logger.info("登出请求");

        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登出成功");
            response.put("data", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("登出失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", null);

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 微信登录
     * POST /api/auth/wechat/login
     */
    // TODO: 实现微信登录功能
    // 需要集成微信开放平台SDK
    // 参考文档:
    // https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html
    @PostMapping("/wechat/login")
    public ResponseEntity<Map<String, Object>> loginByWechat(@RequestBody Map<String, Object> request) {
        logger.info("微信登录请求");

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "微信登录功能待实现");
        response.put("data", null);

        return ResponseEntity.status(501).body(response);
    }

    /**
     * QQ登录
     * POST /api/auth/qq/login
     */
    // TODO: 实现QQ登录功能
    // 需要集成QQ互联SDK
    // 参考文档: https://wiki.connect.qq.com/
    @PostMapping("/qq/login")
    public ResponseEntity<Map<String, Object>> loginByQQ(@RequestBody Map<String, Object> request) {
        logger.info("QQ登录请求");

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "QQ登录功能待实现");
        response.put("data", null);

        return ResponseEntity.status(501).body(response);
    }
}
