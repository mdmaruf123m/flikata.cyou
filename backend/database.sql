-- Flikata Social App Database Schema
-- MySQL 5.7+ / 8.0+ / MariaDB compatible
-- Character set: utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `full_name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(150) DEFAULT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `profile_pic` VARCHAR(500) DEFAULT NULL,
  `cover_pic` VARCHAR(500) DEFAULT NULL,
  `bio` VARCHAR(300) DEFAULT 'Hey there! I am using Flikata.',
  `occupation` VARCHAR(150) DEFAULT 'Software Engineer',
  `location` VARCHAR(150) DEFAULT 'Dhaka, Bangladesh',
  `gender` VARCHAR(20) DEFAULT 'Male',
  `dob` DATE DEFAULT NULL,
  `is_verified` TINYINT(1) DEFAULT 0,
  `is_banned` TINYINT(1) DEFAULT 0,
  `is_online` TINYINT(1) DEFAULT 1,
  `auth_token` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_email` (`email`),
  UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Cloudflare R2 Buckets Management Table
CREATE TABLE IF NOT EXISTS `r2_buckets` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `account_label` VARCHAR(150) NOT NULL,
  `bucket_name` VARCHAR(150) NOT NULL,
  `account_id` VARCHAR(150) NOT NULL,
  `access_key_id` VARCHAR(150) NOT NULL,
  `secret_access_key` VARCHAR(255) NOT NULL,
  `public_url` VARCHAR(255) NOT NULL,
  `account_limit_gb` DECIMAL(10, 2) NOT NULL DEFAULT 10.00,
  `used_bytes` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `priority` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Posts Table
CREATE TABLE IF NOT EXISTS `posts` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `content` TEXT DEFAULT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `video_url` VARCHAR(500) DEFAULT NULL,
  `privacy` VARCHAR(30) DEFAULT 'Public',
  `bg_color` VARCHAR(50) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `comments_count` INT DEFAULT 0,
  `shares_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Post Likes Table
CREATE TABLE IF NOT EXISTS `post_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_post_user_like` (`post_id`, `user_id`),
  FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Post Reactions Table (Emoji reactions like Like, Love, Haha, Wow, Sad, Angry)
CREATE TABLE IF NOT EXISTS `post_reactions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `emoji` VARCHAR(50) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_post_user_reaction` (`post_id`, `user_id`),
  FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Comments Table
CREATE TABLE IF NOT EXISTS `comments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT DEFAULT NULL,
  `reel_id` INT DEFAULT NULL,
  `parent_id` INT DEFAULT NULL,
  `user_id` INT NOT NULL,
  `text` TEXT NOT NULL,
  `reaction_emoji` VARCHAR(50) DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Comment Likes Table
CREATE TABLE IF NOT EXISTS `comment_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `comment_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_comment_user_like` (`comment_id`, `user_id`),
  FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Stories Table
CREATE TABLE IF NOT EXISTS `stories` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `story_image` VARCHAR(500) DEFAULT NULL,
  `text_content` TEXT DEFAULT NULL,
  `background_color` VARCHAR(50) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Reels Table
CREATE TABLE IF NOT EXISTS `reels` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `media_url` VARCHAR(500) NOT NULL,
  `thumbnail_url` VARCHAR(500) DEFAULT NULL,
  `caption` TEXT DEFAULT NULL,
  `audio_title` VARCHAR(255) DEFAULT 'Original Audio',
  `bucket_id` INT DEFAULT NULL,
  `likes_count` INT DEFAULT 0,
  `comments_count` INT DEFAULT 0,
  `shares_count` INT DEFAULT 0,
  `views_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Reel Likes Table
CREATE TABLE IF NOT EXISTS `reel_likes` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `reel_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `reaction` VARCHAR(50) DEFAULT 'Like',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_reel_user_like` (`reel_id`, `user_id`),
  FOREIGN KEY (`reel_id`) REFERENCES `reels` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Conversations Table (Direct message threads between users)
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_one_id` INT NOT NULL,
  `user_two_id` INT NOT NULL,
  `last_message` TEXT DEFAULT NULL,
  `last_message_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `idx_conversation_users` (`user_one_id`, `user_two_id`),
  FOREIGN KEY (`user_one_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_two_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Messages Table
CREATE TABLE IF NOT EXISTS `messages` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `conversation_id` INT NOT NULL,
  `sender_id` INT NOT NULL,
  `receiver_id` INT NOT NULL,
  `reply_to_id` INT DEFAULT NULL,
  `text` TEXT DEFAULT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `video_url` VARCHAR(500) DEFAULT NULL,
  `bucket_id` INT DEFAULT NULL,
  `is_read` TINYINT(1) DEFAULT 0,
  `is_edited` TINYINT(1) DEFAULT 0,
  `is_call_notice` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `from_user_id` INT DEFAULT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(50) DEFAULT 'general',
  `target_id` INT DEFAULT NULL,
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Story Settings Table (Customizable expiration in hours)
CREATE TABLE IF NOT EXISTS `story_settings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `expiry_hours` INT NOT NULL DEFAULT 24,
  `description` VARCHAR(255) DEFAULT 'Story expiration duration in hours (e.g. 24 for 1 day, 12, 48, etc.)',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `story_settings` (`id`, `expiry_hours`, `description`) 
VALUES (1, 24, 'Story auto-deletion duration in hours. Change this value to adjust story lifetime for all stories.')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 15. Sample Default User
INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password_hash`, `bio`, `occupation`, `location`, `is_verified`, `is_online`)
VALUES (1, 'Maruf Hossain', 'maruf@example.com', '01700000000', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Creator & Developer of Flikata.', 'Software Engineer', 'Dhaka, Bangladesh', 1, 1)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 16. Reel Settings Table (Customizable maximum video duration in seconds)
CREATE TABLE IF NOT EXISTS `reel_settings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `max_duration_seconds` INT NOT NULL DEFAULT 60,
  `description` VARCHAR(255) DEFAULT 'Maximum allowed video duration for reels in seconds (default 60s). Change this to any limit.',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reel_settings` (`id`, `max_duration_seconds`, `description`)
VALUES (1, 60, 'Maximum allowed duration in seconds for reels/videos. Change this value to adjust the duration limit.')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 17. Admins Table
CREATE TABLE IF NOT EXISTS `admins` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(150) NOT NULL DEFAULT 'Super Admin',
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `admins` (`id`, `name`, `email`, `password_hash`)
VALUES (1, 'Super Admin', 'admin@gmail.com', '11223344')
ON DUPLICATE KEY UPDATE `email` = 'admin@gmail.com';

SET FOREIGN_KEY_CHECKS = 1;

