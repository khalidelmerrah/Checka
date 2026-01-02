-- Create Moves table for turn-based history
CREATE TABLE IF NOT EXISTS moves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id INT NOT NULL,
    player_id INT NOT NULL,
    move_number INT NOT NULL,
    move_data JSON NOT NULL COMMENT 'Coordinates: {from: "e2", to: "e4", type: "move|wall", wall_orientation: "h|v"}',
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
);

-- Optimization: Index for fast polling
-- "Get all moves for match M starting from move N"
CREATE INDEX idx_moves_polling ON moves(match_id, move_number);

-- Add current turn tracking to matches to allow quick "Is it my turn?" checks
ALTER TABLE matches 
ADD COLUMN current_turn_player_id INT DEFAULT NULL,
ADD COLUMN status VARCHAR(20) DEFAULT 'waiting' COMMENT 'waiting, active, finished',
ADD COLUMN last_move_at TIMESTAMP(3) NULL;

-- Index for finding active games for a user quickly
CREATE INDEX idx_matches_status_player ON matches(status, player1_id, player2_id);
