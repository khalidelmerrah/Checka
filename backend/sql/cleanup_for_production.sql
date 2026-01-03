-- Database Cleanup Script
-- Removes all bot/test users and starts fresh for production
-- IMPORTANT: Run this ONLY when you're ready to launch with real users!

-- Step 1: Remove all bot users
DELETE FROM users WHERE is_bot = 1;

-- Step 2: Remove all guest/test users (optional - only if you want to remove ALL non-Google users)
-- Uncomment the line below if you want to remove users without Google authentication
-- DELETE FROM users WHERE google_subject_id IS NULL;

-- Step 3: Clean up orphaned sessions (sessions for deleted users)
DELETE FROM sessions 
WHERE user_id NOT IN (SELECT id FROM users);

-- Step 4: Clean up orphaned matches (matches with deleted players)
DELETE FROM matches 
WHERE player1_id NOT IN (SELECT id FROM users) 
   OR player2_id NOT IN (SELECT id FROM users);

-- Step 5: Clean up matchmaking queue
TRUNCATE TABLE matchmaking_queue;

-- Step 6: Reset statistics for remaining users (optional)
-- Uncomment if you want all real users to start fresh
-- UPDATE users SET 
--     total_wins = 0,
--     total_matches = 0,
--     elo_rating = 1200,
--     xp = 0,
--     level = 1
-- WHERE is_bot = 0;

-- Step 7: Verify cleanup
SELECT 'Remaining users:' as info, COUNT(*) as count FROM users;
SELECT 'Bots remaining (should be 0):' as info, COUNT(*) as count FROM users WHERE is_bot = 1;
SELECT 'Total matches:' as info, COUNT(*) as count FROM matches;
SELECT 'Active sessions:' as info, COUNT(*) as count FROM sessions;

-- OPTIONAL: If you want to add back a few strategic bots for matchmaking
-- You can run fix_ghost_users.sql again after this cleanup
