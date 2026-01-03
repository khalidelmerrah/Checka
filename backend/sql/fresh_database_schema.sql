-- ============================================
-- CHECKA GAME - COMPLETE FRESH DATABASE SCHEMA
-- ============================================
-- This is a complete rebuild with all fixes applied
-- Run this after DROP DATABASE to start fresh

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

-- ============================================
-- TABLE: admins
-- ============================================
CREATE TABLE `admins` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: users
-- ============================================
CREATE TABLE `users` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `google_subject_id` VARCHAR(255) DEFAULT NULL UNIQUE COMMENT 'The unique ID from Google Auth',
    `email` VARCHAR(255) DEFAULT NULL,
    `display_name` VARCHAR(100) DEFAULT NULL,
    `avatar_url` TEXT DEFAULT NULL,
    `is_banned` TINYINT(1) DEFAULT 0,
    `is_bot` TINYINT(1) DEFAULT 0,
    `elo_rating` INT(11) DEFAULT 1200,
    `xp` INT(11) DEFAULT 0,
    `level` INT(11) DEFAULT 1,
    `total_wins` INT(11) DEFAULT 0,
    `total_matches` INT(11) DEFAULT 0,
    `best_turns` INT(11) DEFAULT 999,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_login` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_users_elo` (`elo_rating`),
    INDEX `idx_users_google_id` (`google_subject_id`),
    INDEX `idx_users_bot` (`is_bot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: sessions
-- ============================================
CREATE TABLE `sessions` (
    `token` VARCHAR(64) NOT NULL COMMENT 'Hex-encoded 32-byte random token',
    `user_id` INT(11) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` TIMESTAMP NOT NULL,
    `last_activity` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`token`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expires` (`expires_at`),
    CONSTRAINT `fk_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: matches
-- ============================================
CREATE TABLE `matches` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `game_mode` VARCHAR(50) NOT NULL COMMENT 'Solo, PassAndPlay, Ranked',
    `difficulty` VARCHAR(20) DEFAULT NULL COMMENT 'Easy, Hard, Master',
    `player1_id` INT(11) DEFAULT NULL,
    `player2_id` INT(11) DEFAULT NULL,
    `winner_id` INT(11) DEFAULT NULL,
    `duration_seconds` INT(11) DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'waiting' COMMENT 'waiting, active, finished',
    `current_turn_player_id` INT(11) DEFAULT NULL,
    `last_move_at` TIMESTAMP(3) NULL DEFAULT NULL,
    `played_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_player1` (`player1_id`),
    INDEX `idx_player2` (`player2_id`),
    INDEX `idx_winner` (`winner_id`),
    INDEX `idx_status_players` (`status`, `player1_id`, `player2_id`),
    CONSTRAINT `fk_matches_player1` FOREIGN KEY (`player1_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_matches_player2` FOREIGN KEY (`player2_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_matches_winner` FOREIGN KEY (`winner_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: moves
-- ============================================
CREATE TABLE `moves` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `match_id` INT(11) NOT NULL,
    `player_id` INT(11) NOT NULL,
    `move_number` INT(11) NOT NULL,
    `move_data` LONGTEXT NOT NULL COMMENT 'JSON: {from: "e2", to: "e4", type: "move|wall"}',
    `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    INDEX `idx_match_move` (`match_id`, `move_number`),
    CONSTRAINT `fk_moves_match` FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_move_data_json` CHECK (JSON_VALID(`move_data`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: matchmaking_queue
-- ============================================
CREATE TABLE `matchmaking_queue` (
    `user_id` INT(11) NOT NULL,
    `elo_rating` INT(11) NOT NULL,
    `joined_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    INDEX `idx_elo` (`elo_rating`),
    CONSTRAINT `fk_queue_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE: app_settings
-- ============================================
CREATE TABLE `app_settings` (
    `setting_key` VARCHAR(50) NOT NULL,
    `setting_value` TEXT DEFAULT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- DEFAULT APP SETTINGS
-- ============================================
INSERT INTO `app_settings` (`setting_key`, `setting_value`, `description`) VALUES
('maintenance_mode', 'false', 'Set to true to block app access'),
('min_version_code', '1', 'Force update if app version is lower'),
('bots_enabled', 'true', 'Enable bot matchmaking when no human opponents found');

-- ============================================
-- CREATE ADMIN ACCOUNT
-- ============================================
-- IMPORTANT: Change the username and password below!
-- Generate password hash in PHP using: password_hash('your_password', PASSWORD_DEFAULT)

-- Default admin (CHANGE THIS!)
-- Username: admin
-- Password: admin123 (CHANGE THIS IMMEDIATELY!)
INSERT INTO `admins` (`username`, `password_hash`) VALUES
('admin', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- To create your own admin with custom password, run this PHP snippet:
-- <?php echo password_hash('YOUR_SECURE_PASSWORD', PASSWORD_DEFAULT); ?>
-- Then update the INSERT above with your username and generated hash

-- ============================================
-- AUTO-CLEANUP EVENT FOR SESSIONS
-- ============================================
SET GLOBAL event_scheduler = ON;

DELIMITER $$
CREATE EVENT IF NOT EXISTS `cleanup_expired_sessions`
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    DELETE FROM `sessions` WHERE `expires_at` < NOW();
END$$
DELIMITER ;

-- ============================================
-- VERIFICATION
-- ============================================
SELECT 'Database schema created successfully!' as status;
SELECT COUNT(*) as admin_count FROM admins;
SELECT COUNT(*) as settings_count FROM app_settings;

SHOW TABLES;
