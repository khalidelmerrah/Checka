<?php
require_once '../config/db.php';
require_once 'elo.php';

header('Content-Type: application/json');

// NOTE: In production, verify AUTH TOKEN here.
$userId = $_POST['user_id'] ?? $_GET['user_id'] ?? null;
$userElo = $_POST['elo'] ?? $_GET['elo'] ?? 1200;

if (!$userId) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing user_id']);
    exit;
}

try {
    // 1. Clean old queue (Remove players waiting > 30s)
    $pdo->exec("DELETE FROM matchmaking_queue WHERE joined_at < (NOW() - INTERVAL 30 SECOND)");

    // 2. Add/Update Self in Queue
    // FIX: If user is "guest", do not insert into DB (avoids Foreign Key/Type errors)
    $isGuest = strpos($userId, 'guest') !== false;

    if (!$isGuest) {
        $stmt = $pdo->prepare("REPLACE INTO matchmaking_queue (user_id, elo_rating, joined_at) VALUES (?, ?, NOW())");
        $stmt->execute([$userId, $userElo]);
    } else {
        // Log guest access
        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] GUEST ACCESS: $userId skipping queue.\n", FILE_APPEND);
    }

    // LOGGING
    file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] MATCHMAKING: User $userId ($userElo) joined queue.\n", FILE_APPEND);

    // 3. Search for REAL human opponent
    $opponent = false;

    if (!$isGuest) {
        $stmt = $pdo->prepare("SELECT user_id, elo_rating FROM matchmaking_queue WHERE user_id != ? AND elo_rating BETWEEN ? AND ? ORDER BY joined_at ASC LIMIT 1");
        $stmt->execute([$userId, $userElo - 1000, $userElo + 1000]);
        $opponent = $stmt->fetch(PDO::FETCH_ASSOC);
    }

    if ($opponent) {
        // REAL HUMAN FOUND!
        $oppId = $opponent['user_id'];
        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] MATCH FOUND: $userId vs $oppId\n", FILE_APPEND);

        // Remove both from queue
        $pdo->prepare("DELETE FROM matchmaking_queue WHERE user_id IN (?, ?)")->execute([$userId, $oppId]);

        // Fetch details
        $stmtUser = $pdo->prepare("SELECT id, username, elo_rating, avatar_url, xp FROM users WHERE id = ?");
        $stmtUser->execute([$oppId]);
        $oppData = $stmtUser->fetch(PDO::FETCH_ASSOC);

        // Get Stats (Games & Win Rate)
        $stmtStats = $pdo->prepare("SELECT 
            COUNT(*) as total, 
            SUM(CASE WHEN winner_id = ? THEN 1 ELSE 0 END) as wins 
            FROM matches WHERE player1_id = ? OR player2_id = ?");
        $stmtStats->execute([$oppId, $oppId, $oppId]);
        $stats = $stmtStats->fetch(PDO::FETCH_ASSOC);

        $totalGames = $stats['total'] ?? 0;
        $wins = $stats['wins'] ?? 0;
        $winRate = $totalGames > 0 ? round(($wins / $totalGames) * 100) . "%" : "-";
        $level = floor(($oppData['xp'] ?? 0) / 1000) + 1;

        echo json_encode([
            'success' => true,
            'match_found' => true,
            'is_bot' => false,
            'opponent' => [
                'id' => $opponent['user_id'],
                'name' => $oppData['username'],
                'elo' => $oppData['elo_rating'],
                'title' => EloRating::getRankTitle($oppData['elo_rating']),
                'avatar_url' => $oppData['avatar_url'],
                'level' => $level,
                'win_rate' => $winRate,
                'total_games' => $totalGames
            ]
        ]);
    } else {
        // 4. NO HUMAN FOUND
        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] NO MATCH for $userId. Checking for Bot...\n", FILE_APPEND);

        // ... (Ghost Protocol Logic)
        // For debugging, let's DELAY getting a bot slightly so the user sees "Searching" UI for at least a sec
        // sleep(1); 

        // GHOST PROTOCOL INTIATED
        // Find a bot with similar Elo (+/- 150)
        $minElo = $userElo - 150;
        $maxElo = $userElo + 150;

        $stmtBot = $pdo->prepare("SELECT id, username, elo_rating, avatar_url, xp FROM users WHERE is_bot = 1 AND elo_rating BETWEEN ? AND ? ORDER BY RAND() LIMIT 1");
        $stmtBot->execute([$minElo, $maxElo]);
        $bot = $stmtBot->fetch(PDO::FETCH_ASSOC);

        if (!$bot) {
            // Fallback
            $stmtBot = $pdo->query("SELECT id, username, elo_rating, avatar_url, xp FROM users WHERE is_bot = 1 ORDER BY RAND() LIMIT 1");
            $bot = $stmtBot->fetch(PDO::FETCH_ASSOC);
        }

        // Fake Stats for Bot to look real
        $level = floor(($bot['xp'] ?? 0) / 1000) + 1;
        $totalGames = rand(10, 500);
        $winRate = rand(45, 65) . "%";

        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] BOT ASSIGNED: $userId vs Bot " . $bot['username'] . "\n", FILE_APPEND);

        echo json_encode([
            'success' => true,
            'match_found' => true,
            'is_bot' => true, // CLIENT HIDDEN FLAG
            'opponent' => [
                'id' => $bot['id'],
                'name' => $bot['username'],
                'elo' => $bot['elo_rating'],
                'title' => EloRating::getRankTitle($bot['elo_rating']),
                'avatar_url' => $bot['avatar_url'],
                'level' => $level,
                'win_rate' => $winRate,
                'total_games' => $totalGames
            ]
        ]);

        // Remove self from queue since we "matched" with a bot
        $pdo->prepare("DELETE FROM matchmaking_queue WHERE user_id = ?")->execute([$userId]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}
