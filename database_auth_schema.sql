-- ====================================
-- QuickPlan 用户认证系统数据库表
-- 创建日期: 2025-10-29
-- ====================================

-- 1. 用户表 (users)
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` VARCHAR(64) PRIMARY KEY COMMENT '用户唯一ID',
  `phone` VARCHAR(20) UNIQUE DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) UNIQUE DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '密码(加密存储)',
  `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
  `status` TINYINT DEFAULT 1 COMMENT '账号状态: 0-禁用, 1-正常',
  `login_type` VARCHAR(20) DEFAULT 'phone' COMMENT '注册方式: phone/email/wechat/qq',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '软删除标记: 0-未删除, 1-已删除',
  
  INDEX idx_phone (`phone`),
  INDEX idx_email (`email`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 第三方登录绑定表 (user_third_party)
CREATE TABLE IF NOT EXISTS `user_third_party` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `third_party_type` VARCHAR(20) NOT NULL COMMENT '第三方平台类型: wechat/qq/weibo',
  `open_id` VARCHAR(100) NOT NULL COMMENT '第三方平台OpenID',
  `union_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方平台UnionID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '第三方平台昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '第三方平台头像',
  `access_token` VARCHAR(500) DEFAULT NULL COMMENT '访问令牌',
  `refresh_token` VARCHAR(500) DEFAULT NULL COMMENT '刷新令牌',
  `expires_in` BIGINT DEFAULT NULL COMMENT 'Token过期时间(秒)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY uk_third_party (`third_party_type`, `open_id`),
  INDEX idx_user_id (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方登录绑定表';

-- 3. 验证码表 (verification_codes)
CREATE TABLE IF NOT EXISTS `verification_codes` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `type` VARCHAR(20) NOT NULL COMMENT '验证码类型: login/register/reset_password',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `used_at` DATETIME DEFAULT NULL COMMENT '使用时间',
  
  INDEX idx_phone (`phone`),
  INDEX idx_email (`email`),
  INDEX idx_created_at (`created_at`),
  INDEX idx_expires_at (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- 4. 用户Token表 (user_tokens)
CREATE TABLE IF NOT EXISTS `user_tokens` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `access_token` VARCHAR(500) NOT NULL COMMENT '访问令牌',
  `refresh_token` VARCHAR(500) NOT NULL COMMENT '刷新令牌',
  `access_token_expires_at` DATETIME NOT NULL COMMENT '访问令牌过期时间',
  `refresh_token_expires_at` DATETIME NOT NULL COMMENT '刷新令牌过期时间',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型: ios/android/web',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已失效, 1-有效',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY uk_access_token (`access_token`(255)),
  UNIQUE KEY uk_refresh_token (`refresh_token`(255)),
  INDEX idx_user_id (`user_id`),
  INDEX idx_expires_at (`access_token_expires_at`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Token表';

-- 5. 用户登录日志表 (user_login_logs)
CREATE TABLE IF NOT EXISTS `user_login_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `login_type` VARCHAR(20) NOT NULL COMMENT '登录方式: phone/email/wechat/qq',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `status` TINYINT DEFAULT 1 COMMENT '登录状态: 0-失败, 1-成功',
  `fail_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  
  INDEX idx_user_id (`user_id`),
  INDEX idx_created_at (`created_at`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- ====================================
-- 初始化测试数据
-- ====================================

-- 插入测试用户 (密码: 123456, 已加密)
INSERT INTO `users` (`user_id`, `phone`, `email`, `nickname`, `avatar`, `login_type`) VALUES
('default_user_001', '13800138000', 'test@example.com', '测试用户', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test', 'phone');

-- ====================================
-- 清理过期数据的定时任务建议
-- ====================================

-- 建议创建定时任务清理:
-- 1. 过期的验证码 (created_at < NOW() - INTERVAL 1 DAY)
-- 2. 过期的Token (access_token_expires_at < NOW())
-- 3. 旧的登录日志 (created_at < NOW() - INTERVAL 90 DAY)

-- 示例清理语句:
-- DELETE FROM verification_codes WHERE status = 2 AND created_at < NOW() - INTERVAL 1 DAY;
-- DELETE FROM user_tokens WHERE status = 0 AND updated_at < NOW() - INTERVAL 7 DAY;
-- DELETE FROM user_login_logs WHERE created_at < NOW() - INTERVAL 90 DAY;
