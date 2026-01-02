-- Add Elo Rating column to users table
-- Default starting rating is 1200
ALTER TABLE users ADD COLUMN elo_rating INT DEFAULT 1200;

-- Index for fast leaderboard lookups
CREATE INDEX idx_users_elo ON users(elo_rating DESC);

-- Optional: Track wins/losses explicitly if not already in matches aggregation
ALTER TABLE users ADD COLUMN total_wins INT DEFAULT 0;
ALTER TABLE users ADD COLUMN total_matches INT DEFAULT 0;
