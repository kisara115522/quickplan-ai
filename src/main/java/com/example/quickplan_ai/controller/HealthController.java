package com.example.quickplan_ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基本健康检查
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Application is running");

        return ResponseEntity.ok(response);
    }

    /**
     * 详细健康检查(包含数据库和Redis)
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> healthDetail() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();

        // 检查数据库连接
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            checks.put("database", Map.of("status", "UP", "type", "MySQL"));
            logger.info("数据库连接检查: 正常");
        } catch (Exception e) {
            checks.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
            logger.error("数据库连接检查: 失败", e);
        }

        // 检查Redis连接
        if (redisTemplate != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                checks.put("redis", Map.of("status", "UP"));
                logger.info("Redis连接检查: 正常");
            } catch (Exception e) {
                checks.put("redis", Map.of("status", "DOWN", "error", e.getMessage()));
                logger.error("Redis连接检查: 失败", e);
            }
        } else {
            checks.put("redis", Map.of("status", "NOT_CONFIGURED"));
        }

        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("checks", checks);

        return ResponseEntity.ok(response);
    }
}
