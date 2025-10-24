-- ============================================
-- AI对话助手数据库建表SQL脚本
-- 数据库: quickplan_ai
-- 创建时间: 2025-10-24
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `quickplan_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `quickplan_ai`;

-- ============================================
-- 表1: conversation (会话表)
-- 用途: 存储用户的对话会话信息,类似ChatGPT的会话列表
-- ============================================
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` VARCHAR(64) NOT NULL COMMENT '会话ID,使用UUID作为唯一标识',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID,标识会话所属用户',
  `title` VARCHAR(255) DEFAULT '新对话' COMMENT '会话标题,默认为"新对话",可根据首条消息自动生成',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_updated_at` (`updated_at`),
  INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表-存储用户的对话会话';

-- ============================================
-- 表2: message (消息表)
-- 用途: 存储会话中的每一条消息记录,包括用户输入和AI回复
-- ============================================
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID,自增主键',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '所属会话ID,外键关联conversation表',
  `role` VARCHAR(20) NOT NULL COMMENT '消息角色: user-用户消息, assistant-AI助手消息',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id` (`conversation_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表-存储会话中的每条消息';

-- ============================================
-- 初始化数据(可选)
-- ============================================
-- 插入示例会话
INSERT INTO `conversation` (`id`, `user_id`, `title`, `created_at`, `updated_at`, `is_deleted`) 
VALUES 
('example-conversation-001', 'user-001', '如何使用Spring Boot?', NOW(), NOW(), 0);

-- 插入示例消息
INSERT INTO `message` (`conversation_id`, `role`, `content`, `created_at`, `is_deleted`) 
VALUES 
('example-conversation-001', 'user', '你好,请问如何使用Spring Boot创建一个REST API?', NOW(), 0),
('example-conversation-001', 'assistant', '你好!创建Spring Boot REST API很简单。首先需要添加spring-boot-starter-web依赖,然后创建一个Controller类...', NOW(), 0);

-- ============================================
-- 查询示例
-- ============================================
-- 查询某个用户的所有会话(按更新时间倒序)
-- SELECT * FROM conversation WHERE user_id = 'user-001' AND is_deleted = 0 ORDER BY updated_at DESC;

-- 查询某个会话的所有消息(按创建时间正序)
-- SELECT * FROM message WHERE conversation_id = 'example-conversation-001' AND is_deleted = 0 ORDER BY created_at ASC;

-- 查询会话及其消息总数
-- SELECT c.*, COUNT(m.id) as message_count 
-- FROM conversation c 
-- LEFT JOIN message m ON c.id = m.conversation_id AND m.is_deleted = 0 
-- WHERE c.user_id = 'user-001' AND c.is_deleted = 0 
-- GROUP BY c.id 
-- ORDER BY c.updated_at DESC;
