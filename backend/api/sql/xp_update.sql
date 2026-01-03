-- Add XP and Level columns
ALTER TABLE users ADD COLUMN xp INT DEFAULT 0;
ALTER TABLE users ADD COLUMN level INT DEFAULT 1;

-- (Optional) If you want to track "Fastest Win" or "Most Efficient Win" later
ALTER TABLE users ADD COLUMN best_turns INT DEFAULT 999;
