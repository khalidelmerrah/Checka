<?php
require_once '../config/db.php';
require_once 'elo.php';

header('Content-Type: application/json');

// Optional: Limit
$limit = isset($_GET['limit']) ? (int) $_GET['limit'] : 50;
if ($limit > 100)
    $limit = 100;

$userId = isset($_GET['user_id']) ? $_GET['user_id'] : null;

try {
    // 1. Fetch Top Leaderboard
    $pdo = get_db_connection();

    // FIX: Use display_name as username (DB column is display_name)
    $stmt = $pdo->prepare("SELECT id, display_name as username, avatar_url, elo_rating, total_wins, total_matches, xp, level FROM users ORDER BY elo_rating DESC LIMIT ?");
    $stmt->bindValue(1, $limit, PDO::PARAM_INT);
    $stmt->execute();

    $players = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Process titles
    foreach ($players as &$p) {
        $p['rank_title'] = EloRating::getRankTitle($p['elo_rating']);
        $p['win_rate'] = ($p['total_matches'] > 0) ? round(($p['total_wins'] / $p['total_matches']) * 100, 1) . '%' : '0%';
    }

    $response = ['leaderboard' => $players];

    // 2. Fetch User Specific Rank (if requested and exists)
    if ($userId) {
        // Find user stats first
        $userStmt = $pdo->prepare("SELECT id, display_name as username, avatar_url, elo_rating, total_wins, total_matches, xp, level FROM users WHERE id = ?");
        $userStmt->execute([$userId]);
        $userStats = $userStmt->fetch(PDO::FETCH_ASSOC);

        if ($userStats) {
            // Count people with higher ELO
            $rankStmt = $pdo->prepare("SELECT COUNT(*) as rank_above FROM users WHERE elo_rating > ?");
            $rankStmt->execute([$userStats['elo_rating']]);
            $rankAbove = $rankStmt->fetch(PDO::FETCH_ASSOC)['rank_above'];

            $userStats['rank'] = $rankAbove + 1;
            $userStats['rank_title'] = EloRating::getRankTitle($userStats['elo_rating']);
            $userStats['win_rate'] = ($userStats['total_matches'] > 0) ? round(($userStats['total_wins'] / $userStats['total_matches']) * 100, 1) . '%' : '0%';

            $response['user_rank'] = $userStats;
        }
    }

    echo json_encode($response);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}
