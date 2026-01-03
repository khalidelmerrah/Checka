-- Session Management Table
-- Stores cryptographically secure session tokens for authenticated users

CREATE TABLE IF NOT EXISTS sessions (
    token VARCHAR(64) PRIMARY KEY COMMENT 'Hex-encoded 32-byte random token',
    user_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cleanup procedure for expired sessions
DELIMITER $$
CREATE EVENT IF NOT EXISTS cleanup_expired_sessions
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    DELETE FROM sessions WHERE expires_at < NOW();
END$$
DELIMITER ;
