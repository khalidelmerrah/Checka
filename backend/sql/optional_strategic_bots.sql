-- ============================================
-- OPTIONAL: Strategic Bot Users for Matchmaking
-- ============================================
-- Run this AFTER fresh_database_schema.sql if you want some bots
-- This creates 10 strategic bots across different skill levels

INSERT INTO `users` (`google_subject_id`, `display_name`, `avatar_url`, `is_bot`, `elo_rating`, `xp`, `level`, `total_wins`, `total_matches`) VALUES
-- Beginner Bots
(NULL, 'NoviceBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 1, 900, 200, 1, 3, 10),
(NULL, 'BeginnerOne', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Bear.png', 1, 1000, 500, 1, 8, 20),

-- Intermediate Bots
(NULL, 'IntermediateBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Cat%20Face.png', 1, 1150, 1500, 2, 20, 45),
(NULL, 'AveragePlayer', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Panda.png', 1, 1200, 2000, 3, 25, 50),
(NULL, 'SolidBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 1, 1300, 2800, 3, 35, 68),

-- Advanced Bots
(NULL, 'AdvancedBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 1, 1400, 4000, 5, 45, 85),
(NULL, 'StrategyMaster', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 1, 1450, 4500, 5, 50, 95),

-- Expert Bots
(NULL, 'ExpertBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Alien%20Monster.png', 1, 1550, 6000, 7, 70, 120),
(NULL, 'CheckaMaster', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Robot.png', 1, 1600, 8000, 9, 95, 150),
(NULL, 'GrandmasterAI', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Unicorn.png', 1, 1650, 10000, 11, 110, 175);

SELECT 'Strategic bots added!' as status;
SELECT COUNT(*) as bot_count FROM users WHERE is_bot = 1;
SELECT display_name, elo_rating, level FROM users WHERE is_bot = 1 ORDER BY elo_rating;
