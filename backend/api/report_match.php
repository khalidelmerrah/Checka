<?php
require_once 'db.php';

header('Content-Type: application/json');

// Get raw POST data
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['player1_id']) || !isset($data['game_mode'])) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$p1_id = $data['player1_id'];
$p2_id = isset($data['player2_id']) ? $data['player2_id'] : null;
$winner_id = isset($data['winner_id']) && $data['winner_id'] !== "" ? $data['winner_id'] : null;
$game_mode = $data['game_mode'];
$difficulty = isset($data['difficulty']) ? $data['difficulty'] : 'Easy';
$duration = isset($data['duration']) ? $data['duration'] : 0;
$turns = isset($data['turns']) ? $data['turns'] : 0;

try {
    // 1. Record the Match
    $stmt = $pdo->prepare("INSERT INTO matches (game_mode, difficulty, player1_id, player2_id, winner_id, duration_seconds) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$game_mode, $difficulty, $p1_id, $p2_id, $winner_id, $duration]);
    $match_id = $pdo->lastInsertId();

    // 2. Update Stats (XP, Wins)
    if ($winner_id) {
        // Winner gets XP and Win count
        $xp_gain = 50; // Base XP
        if ($game_mode == 'Ranked')
            $xp_gain = 100;

        $stmt = $pdo->prepare("UPDATE users SET total_wins = total_wins + 1, total_matches = total_matches + 1, xp = xp + ? WHERE id = ?");
        $stmt->execute([$xp_gain, $winner_id]);
    }

    // Loser (if real user) gets participation XP
    if ($p2_id && $p2_id != $winner_id) {
        $stmt = $pdo->prepare("UPDATE users SET total_matches = total_matches + 1, xp = xp + 10 WHERE id = ?");
        $stmt->execute([$p2_id]);
    }

    // Also update P1 total matches if they lost
    if ($p1_id != $winner_id) {
        $stmt = $pdo->prepare("UPDATE users SET total_matches = total_matches + 1, xp = xp + 10 WHERE id = ?");
        $stmt->execute([$p1_id]);
    }

    // 3. Elo Update (Only for Ranked & Real Players)
    // Simplified Elo implementation for now
    if ($game_mode == 'Ranked' && $p2_id) {
        // Fetch current Elos... (Todo: Implement full K-factor logic)
        // For now, simple static gain/loss
        if ($winner_id == $p1_id) {
            $pdo->prepare("UPDATE users SET elo_rating = elo_rating + 15 WHERE id = ?")->execute([$p1_id]);
            $pdo->prepare("UPDATE users SET elo_rating = elo_rating - 15 WHERE id = ?")->execute([$p2_id]);
        } else {
            $pdo->prepare("UPDATE users SET elo_rating = elo_rating - 15 WHERE id = ?")->execute([$p1_id]);
            $pdo->prepare("UPDATE users SET elo_rating = elo_rating + 15 WHERE id = ?")->execute([$p2_id]);
        }
    }

    // 4. Check for Level Up (Simple logic: 1000 XP per level)
    $stmt = $pdo->prepare("UPDATE users SET level = FLOOR(xp / 1000) + 1 WHERE id IN (?, ?)");
    $stmt->execute([$p1_id, $p2_id]);

    echo json_encode(['success' => true, 'match_id' => $match_id]);

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>