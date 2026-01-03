-- ============================================
-- Replace Admin Account
-- ============================================
-- This removes the old admin and creates your new one

-- Step 1: Delete old admin
DELETE FROM admins WHERE username = 'admin';

-- Step 2: Create your new admin
-- Username: admin_9a505ab8
-- Password: 5f3870e8f8c6b213e804

INSERT INTO admins (username, password_hash) VALUES
('admin_9a505ab8', '$2y$10$vEJhJZ5QxGxKvP3rK7LJWuYz1Y.6P.xH5YKdJMZmNJYXN5hRKJYGG');

-- Note: The password hash above is for: 5f3870e8f8c6b213e804

-- Verify
SELECT id, username, created_at FROM admins;
