-- ============================================
-- QuickPlan AI对话助手 - 完整数据库建表SQL脚本
-- 数据库: quickplan_ai
-- 创建时间: 2025-10-26
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `quickplan_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `quickplan_ai`;

-- ============================================
-- 表1: conversation (会话表)
-- 用途: 存储用户的对话会话信息
-- ============================================
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` VARCHAR(64) NOT NULL COMMENT '会话ID,使用UUID作为唯一标识',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID,标识会话所属用户',
  `title` VARCHAR(128) DEFAULT '新对话' COMMENT '会话标题',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '会话状态: 0-正常, 1-归档',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_updated_at` (`updated_at`),
  INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- ============================================
-- 表2: conversation_message (消息表)
-- 用途: 存储会话中的每一条消息(用户消息和AI回复)
-- ============================================
CREATE TABLE IF NOT EXISTS `conversation_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID,自增主键',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '所属会话ID',
  `role` VARCHAR(16) NOT NULL COMMENT '角色: user / assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id` (`conversation_id`),
  INDEX `idx_role` (`role`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) 
    REFERENCES `conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================
-- 表3: ocr_reminder (OCR提醒表)
-- 用途: 存储通过OCR识别创建的提醒事项
-- ============================================
CREATE TABLE IF NOT EXISTS `ocr_reminder` (
  `id` VARCHAR(64) NOT NULL COMMENT '提醒ID,使用UUID',
  `conversation_id` VARCHAR(64) DEFAULT NULL COMMENT '来源会话ID(可选)',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '提醒标题',
  `description` TEXT COMMENT '备注描述',
  `remind_time` DATETIME DEFAULT NULL COMMENT '提醒时间(可选)',
  `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成: 0-未完成, 1-已完成',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id` (`conversation_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_remind_time` (`remind_time`),
  INDEX `idx_is_completed` (`is_completed`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR提醒表';

-- ============================================
-- 表4: schedule (日程表)
-- 用途: 存储用户的日程安排
-- ============================================
CREATE TABLE IF NOT EXISTS `schedule` (
  `id` VARCHAR(64) NOT NULL COMMENT '日程ID,使用UUID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '日程标题',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '地点',
  `date` DATE NOT NULL COMMENT '日期',
  `time` TIME DEFAULT NULL COMMENT '时间',
  `description` TEXT COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_date` (`date`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- ============================================
-- 示例数据(可选 - 用于测试)
-- ============================================
-- 默认用户
INSERT INTO `conversation` (`id`, `user_id`, `title`, `status`, `created_at`, `updated_at`, `is_deleted`) 
VALUES 
('example-conversation-001', 'default_user_001', '如何使用AI助手', 0, NOW(), NOW(), 0);

INSERT INTO `conversation_message` (`conversation_id`, `role`, `content`, `created_at`, `is_deleted`) 
VALUES 
('example-conversation-001', 'user', '你好,请帮我规划明天的日程', NOW(), 0),
('example-conversation-001', 'assistant', '好的!我可以帮你规划日程。请告诉我明天你需要做什么事情?', NOW(), 0);

-- ============================================
-- 常用查询SQL示例
-- ============================================

-- 1. 查询用户所有会话(按更新时间倒序)
-- SELECT * FROM conversation 
-- WHERE user_id = 'default_user_001' AND is_deleted = 0 
-- ORDER BY updated_at DESC;

-- 2. 查询会话的所有消息(按时间升序)
-- SELECT * FROM conversation_message 
-- WHERE conversation_id = 'xxx' AND is_deleted = 0 
-- ORDER BY created_at ASC;

-- 3. 查询用户某日期范围的日程
-- SELECT * FROM schedule 
-- WHERE user_id = 'default_user_001' 
--   AND date BETWEEN '2025-10-26' AND '2025-10-31' 
--   AND is_deleted = 0 
-- ORDER BY date ASC, time ASC;

-- 4. 查询用户未完成的提醒
-- SELECT * FROM ocr_reminder 
-- WHERE user_id = 'default_user_001' 
--   AND is_completed = 0 
--   AND is_deleted = 0 
-- ORDER BY remind_time ASC;

-- ============================================
-- 数据库维护SQL
-- ============================================

-- 清理软删除的旧数据(30天前)
-- DELETE FROM conversation WHERE is_deleted = 1 AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- DELETE FROM conversation_message WHERE is_deleted = 1 AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- DELETE FROM ocr_reminder WHERE is_deleted = 1 AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
-- DELETE FROM schedule WHERE is_deleted = 1 AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
