<?php
require_once __DIR__ . '/config/db.php';

try {
    // 1. Check current column type (Optional, just force alter)
    echo "Attempting to modify matchmaking_queue.user_id to VARCHAR(255)...<br>";

    // We need to drop primary key first if it exists? 
    // Usually modifying the column type keeps the PK status but checks constraints.
    // Let's try direct modification.

    $pdo->exec("ALTER TABLE matchmaking_queue MODIFY user_id VARCHAR(255) NOT NULL");

    echo "SUCCESS: matchmaking_queue.user_id is now VARCHAR(255).<br>";
    echo "Guests can now join the queue!<br>";

} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}
?>