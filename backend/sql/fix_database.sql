-- Complete Database Fix Script
-- Run this to fix both critical issues
-- Database: reyagency_chekadmin

-- ============================================
-- PART 1: Create Sessions Table (CRITICAL)
-- ============================================

CREATE TABLE IF NOT EXISTS sessions (
    token VARCHAR(64) PRIMARY KEY COMMENT 'Hex-encoded 32-byte random token',
    user_id INT NOT NULL COMMENT 'References users.id',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_expires (expires_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- PART 2: Add Auto-Cleanup Event (Optional)
-- ============================================

-- Enable event scheduler (if not already enabled)
SET GLOBAL event_scheduler = ON;

-- Create cleanup event
DELIMITER $$
CREATE EVENT IF NOT EXISTS cleanup_expired_sessions
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    DELETE FROM sessions WHERE expires_at < NOW();
END$$
DELIMITER ;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check sessions table exists
SELECT 
    'Sessions table created' as status,
    COUNT(*) as row_count 
FROM sessions;

-- Show table structure
DESCRIBE sessions;

-- Verify event scheduler is running
SHOW VARIABLES LIKE 'event_scheduler';

-- List scheduled events
SHOW EVENTS WHERE db = DATABASE();

SELECT 'Database fix completed successfully!' as result;
