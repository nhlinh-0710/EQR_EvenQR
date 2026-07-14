SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- ============================================
-- DATABASE even_qr
-- ============================================
CREATE DATABASE IF NOT EXISTS `even_qr`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `even_qr`;

-- ============================================
-- BẢNG account
-- ============================================
CREATE TABLE IF NOT EXISTS `account` (
  `user_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20),
  `role` ENUM('user','organizer','admin') DEFAULT 'user',
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG chat_logs
-- ============================================
CREATE TABLE IF NOT EXISTS `chat_logs` (
  `chat_id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `message` TEXT NOT NULL,
  `sender` ENUM('user','bot') NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`chat_id`),
  KEY `idx_chat_user` (`user_id`),
  CONSTRAINT `chat_logs_fk_user`
    FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG event
-- ============================================
CREATE TABLE IF NOT EXISTS `event` (
  `event_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(255),
  `location` VARCHAR(255),
  `start_time` DATETIME(6),
  `end_time` DATETIME(6),
  `status` VARCHAR(50) DEFAULT 'DRAFT',
  `max_participants` INT(11),
  `organizer_id` INT(11) NOT NULL,
  `image_url` VARCHAR(500),  -- Tăng từ 255 lên 500 để lưu đường dẫn tuyệt đối dài
  `created_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`event_id`),
  KEY `idx_event_organizer` (`organizer_id`),
  CONSTRAINT `event_fk_organizer`
      FOREIGN KEY (`organizer_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG event_ticket
-- ============================================
CREATE TABLE IF NOT EXISTS `event_ticket` (
  `ticket_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `event_id` BIGINT(20) NOT NULL,
  `user_id` INT(11) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `phone` VARCHAR(20),
  `occupation` VARCHAR(100),
  `ticket_type` VARCHAR(50) NOT NULL,
  `note` TEXT,
  `registered_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  `cancelled` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`ticket_id`),
  KEY `idx_ticket_event` (`event_id`),
  KEY `idx_ticket_user` (`user_id`),
  CONSTRAINT `event_ticket_fk_event`
      FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`) ON DELETE CASCADE,
  CONSTRAINT `event_ticket_fk_user`
      FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG checkin_history
-- ============================================
CREATE TABLE IF NOT EXISTS `checkin_history` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `ticket_id` BIGINT(20) NOT NULL,
  `user_id` INT(11) NOT NULL,
  `event_id` BIGINT(20) NOT NULL,
  `checkin_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_checkin_ticket` (`ticket_id`),
  KEY `idx_checkin_event` (`event_id`),
  KEY `idx_checkin_user` (`user_id`),
  CONSTRAINT `checkin_history_fk_ticket`
      FOREIGN KEY (`ticket_id`) REFERENCES `event_ticket` (`ticket_id`) ON DELETE CASCADE,
  CONSTRAINT `checkin_history_fk_event`
      FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`) ON DELETE CASCADE,
  CONSTRAINT `checkin_history_fk_user`
      FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG feedbacks (có feedback + reply)
-- ============================================
CREATE TABLE IF NOT EXISTS `feedbacks` (
  `feedback_id` INT(11) NOT NULL AUTO_INCREMENT,
  `event_id` BIGINT(20) NOT NULL,
  `user_id` INT(11) NOT NULL,
  `rating` TINYINT(4) DEFAULT NULL CHECK (`rating` BETWEEN 1 AND 5),
  `comment` TEXT,
  `organizer_reply` TEXT DEFAULT NULL,
  `organizer_reply_at` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`feedback_id`),
  UNIQUE KEY `uniq_event_user_feedback` (`event_id`, `user_id`),
  KEY `idx_fb_event` (`event_id`),
  KEY `idx_fb_user` (`user_id`),
  CONSTRAINT `feedbacks_fk_event`
      FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`) ON DELETE CASCADE,
  CONSTRAINT `feedbacks_fk_user`
      FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG notifications
-- ============================================
CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `event_id` BIGINT(20) DEFAULT NULL,
  `title` VARCHAR(200),
  `message` TEXT NOT NULL,
  `status` ENUM('unread','read') DEFAULT 'unread',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_notif_user` (`user_id`),
  KEY `idx_notif_event` (`event_id`),
  CONSTRAINT `notifications_fk_user`
      FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `notifications_fk_event`
      FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- BẢNG user_event_behavior
-- ============================================
CREATE TABLE IF NOT EXISTS `user_event_behavior` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `event_id` BIGINT(20) NOT NULL,
  `behavior_type` ENUM('VIEW','CLICK') NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_behavior_user` (`user_id`),
  KEY `idx_behavior_event` (`event_id`),
  CONSTRAINT `behavior_fk_user`
      FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `behavior_fk_event`
      FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- MIGRATION: Đảm bảo unique constraint feedbacks
-- ============================================
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
    AND table_name = 'feedbacks'
    AND constraint_name = 'uniq_event_user_feedback'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE `feedbacks` ADD UNIQUE KEY `uniq_event_user_feedback` (`event_id`, `user_id`);',
    'SELECT "Unique constraint uniq_event_user_feedback đã tồn tại" AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- DONE
-- ============================================
SELECT '✅ Hoàn tất tạo database + bảng + ràng buộc + feedback!' AS message;

COMMIT;
