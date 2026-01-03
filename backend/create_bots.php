<?php
require_once __DIR__ . '/config/db.php';

// List of Bot Names
$botNames = [
    "CheckaMaster",
    "RookieOne",
    "AlphaZero_v1",
    "DeepBlue_Mini",
    "KasparovAI",
    "QueenGambit",
    "PawnStar",
    "KnightRider",
    "BishopTakesQueen",
    "EndgamePro"
];

$count = 0;

try {
    $stmt = $pdo->prepare("INSERT IGNORE INTO users (google_subject_id, display_name, email, elo_rating, xp, level, is_bot) VALUES (?, ?, ?, ?, ?, ?, 1)");

    foreach ($botNames as $name) {
        // Fake Google ID for uniqueness
        $googleId = "bot_" . md5($name);
        $elo = rand(800, 1600);
        $xp = rand(100, 5000);
        $level = floor($xp / 1000) + 1;

        $stmt->execute([$googleId, $name, "bot@checka.top", $elo, $xp, $level]);
        if ($stmt->rowCount() > 0) {
            $count++;
        }
    }

    echo "Successfully created $count new Bots!<br>";
    echo "<a href='admin_logs.php'>Return to Logs</a>";

} catch (PDOException $e) {
    echo "Error creating bots: " . $e->getMessage();
}
?>