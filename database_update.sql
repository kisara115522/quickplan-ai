-- ============================================
-- QuickPlan AI 数据库更新脚本
-- 用于更新现有数据库以支持新功能
-- ============================================

USE `quickplan_ai`;

-- ============================================
-- 1. 重命名现有表(如果存在)
-- ============================================

-- 检查并重命名 message 表为 conversation_message
DROP TABLE IF EXISTS conversation_message_backup;
CREATE TABLE conversation_message_backup SELECT * FROM message WHERE 1=0;

-- 重命名表
RENAME TABLE message TO conversation_message;

-- ============================================
-- 2. 为 conversation 表添加 status 字段(如果不存在)
-- ============================================

-- 添加status字段
ALTER TABLE conversation 
ADD COLUMN IF NOT EXISTS `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '会话状态: 0-正常, 1-归档' 
AFTER `title`;

-- 添加status索引
ALTER TABLE conversation 
ADD INDEX IF NOT EXISTS `idx_status` (`status`);

-- ============================================
-- 3. 创建 OCR 提醒表
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
-- 4. 创建日程表
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
-- 5. 验证表结构
-- ============================================

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC conversation;
DESC conversation_message;
DESC ocr_reminder;
DESC schedule;

-- ============================================
-- 6. 插入测试数据(可选)
-- ============================================

-- 测试会话
INSERT IGNORE INTO `conversation` (`id`, `user_id`, `title`, `status`, `created_at`, `updated_at`, `is_deleted`) 
VALUES 
('test-conv-001', 'default_user_001', '测试会话', 0, NOW(), NOW(), 0);

-- 测试消息
INSERT IGNORE INTO `conversation_message` (`conversation_id`, `role`, `content`, `created_at`, `is_deleted`) 
VALUES 
('test-conv-001', 'user', '你好,请帮我规划明天的日程', NOW(), 0),
('test-conv-001', 'assistant', '好的!我可以帮你规划明天的日程。', NOW(), 0);

-- 测试日程
INSERT IGNORE INTO `schedule` (`id`, `user_id`, `title`, `location`, `date`, `time`, `description`, `is_deleted`) 
VALUES 
('test-schedule-001', 'default_user_001', '团队周会', '会议室A', '2025-10-27', '09:30:00', '讨论项目进度', 0);

-- 测试OCR提醒
INSERT IGNORE INTO `ocr_reminder` (`id`, `user_id`, `title`, `description`, `remind_time`, `is_completed`, `is_deleted`) 
VALUES 
('test-reminder-001', 'default_user_001', '明早开会', '由OCR自动生成', '2025-10-27 09:00:00', 0, 0);

-- ============================================
-- 完成!
-- ============================================

SELECT '数据库更新完成!' AS Status;
SELECT COUNT(*) AS conversation_count FROM conversation;
SELECT COUNT(*) AS message_count FROM conversation_message;
SELECT COUNT(*) AS reminder_count FROM ocr_reminder;
SELECT COUNT(*) AS schedule_count FROM schedule;
