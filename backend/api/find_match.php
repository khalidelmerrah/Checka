<?php
require_once '../config/db.php';
require_once 'elo.php';

header('Content-Type: application/json');

// NOTE: In production, verify AUTH TOKEN here.
// Parse JSON body
$data = json_decode(file_get_contents('php://input'), true);
$userId = $data['user_id'] ?? $_REQUEST['user_id'] ?? null;
$userElo = $data['elo'] ?? $_REQUEST['elo'] ?? 1200;

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

    // 3. Search for REAL human opponent (Long Polling: 10 seconds)
    $opponent = false;
    $maxRetries = 10; // Wait up to 10 seconds

    for ($i = 0; $i < $maxRetries; $i++) {
        if (!$isGuest) {
            $stmt = $pdo->prepare("SELECT user_id, elo_rating FROM matchmaking_queue WHERE user_id != ? AND elo_rating BETWEEN ? AND ? ORDER BY joined_at ASC LIMIT 1");
            $stmt->execute([$userId, $userElo - 1000, $userElo + 1000]);
            $opponent = $stmt->fetch(PDO::FETCH_ASSOC);
        } else {
            // Guest Logic: Can match with other Guests? 
            // Logic: Check for ANY other user (guest or real) in queue
            $stmt = $pdo->prepare("SELECT user_id, elo_rating FROM matchmaking_queue WHERE user_id != ? ORDER BY joined_at ASC LIMIT 1");
            $stmt->execute([$userId]);
            $opponent = $stmt->fetch(PDO::FETCH_ASSOC);
        }

        if ($opponent) {
            break; // Found one!
        }
        sleep(1); // Wait 1 second before retrying
    }

    if ($opponent) {
        // REAL HUMAN FOUND!
        $oppId = $opponent['user_id'];
        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] MATCH FOUND: $userId vs $oppId\n", FILE_APPEND);

        // Remove both from queue
        $pdo->prepare("DELETE FROM matchmaking_queue WHERE user_id IN (?, ?)")->execute([$userId, $oppId]);

        // Fetch details
        $stmtUser = $pdo->prepare("SELECT id, display_name AS username, elo_rating, avatar_url, xp FROM users WHERE id = ?");
        $stmtUser->execute([$oppId]);
        $oppData = $stmtUser->fetch(PDO::FETCH_ASSOC);

        // Handle Guest Opponent (Has no DB record)
        if (!$oppData && strpos($oppId, 'guest') !== false) {
            $oppData = [
                'username' => 'Guest ' . substr($oppId, -4),
                'elo_rating' => 1200,
                'avatar_url' => null,
                'xp' => 0
            ];
        }

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
        // 4. NO HUMAN FOUND - CHECK BOT SETTINGS
        $stmtSettings = $pdo->query("SELECT setting_value FROM app_settings WHERE setting_key = 'bots_enabled'");
        $botsEnabled = $stmtSettings->fetchColumn();

        // Only use bot if explicitly enabled (default is true if not set, but treating 'false' string as false)
        $useBot = ($botsEnabled === 'true' || $botsEnabled === false); // Default true if missing

        if (!$useBot) {
            // Bots Disabled -> Return "No Match" so client keeps spinning/waiting
            echo json_encode(['success' => true, 'match_found' => false]);
            exit;
        }

        file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] NO MATCH for $userId. Checking for Bot...\n", FILE_APPEND);

        // ... (Ghost Protocol Logic)
        // GHOST PROTOCOL INTIATED
        // Find a bot with similar Elo (+/- 150)
        $minElo = $userElo - 150;
        $maxElo = $userElo + 150;

        $stmtBot = $pdo->prepare("SELECT id, display_name AS username, elo_rating, avatar_url, xp FROM users WHERE is_bot = 1 AND elo_rating BETWEEN ? AND ? ORDER BY RAND() LIMIT 1");
        $stmtBot->execute([$minElo, $maxElo]);
        $bot = $stmtBot->fetch(PDO::FETCH_ASSOC);

        if (!$bot) {
            // Fallback 2: Try any bot
            $stmtBot = $pdo->query("SELECT id, display_name AS username, elo_rating, avatar_url, xp FROM users WHERE is_bot = 1 ORDER BY RAND() LIMIT 1");
            $bot = $stmtBot->fetch(PDO::FETCH_ASSOC);
        }

        // EMERGENCY FALLBACK: If DB is empty of bots, create an in-memory one
        if (!$bot) {
            $bot = [
                'id' => 'bot_emergency_' . rand(100, 999),
                'username' => 'Emergency Bot',
                'elo_rating' => 1000,
                'avatar_url' => null,
                'xp' => 100
            ];
            file_put_contents(__DIR__ . '/../logs/app.log', "[" . date('Y-m-d H:i:s') . "] WARNING: No bots in DB. Using Emergency Bot.\n", FILE_APPEND);
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
