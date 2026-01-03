-- Database Schema for Checka

CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    google_subject_id VARCHAR(255) UNIQUE COMMENT 'The unique ID from Google Auth',
    email VARCHAR(255),
    display_name VARCHAR(100),
    avatar_url TEXT,
    is_banned TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS matches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_mode VARCHAR(50) NOT NULL, -- 'Solo', 'PassAndPlay'
    difficulty VARCHAR(20),         -- 'Easy', 'Hard'
    player1_id INT,                 -- FK to users.id
    player2_id INT,                 -- FK to users.id (nullable if AI or Guest)
    winner_id INT,                  -- FK to users.id (nullable if Draw or AI won)
    duration_seconds INT,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (player2_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT,
    description VARCHAR(255)
);

-- Default Settings
INSERT IGNORE INTO app_settings (setting_key, setting_value, description) VALUES 
('maintenance_mode', 'false', 'Set to true to block app access'),
('min_version_code', '1', 'Force update if app version is lower');

-- Default Admin should be created via install.php for security
-- VALUES removed to prevent hardcoded credentials
