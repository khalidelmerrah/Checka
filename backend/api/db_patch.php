<?php
require_once __DIR__ . '/config/db.php';

try {
    // 1. Add email column to users if not exists
    $columns = $pdo->query("SHOW COLUMNS FROM users LIKE 'email'")->fetchAll();
    if (empty($columns)) {
        $pdo->exec("ALTER TABLE users ADD COLUMN email VARCHAR(255) NULL AFTER username");
        echo "Added 'email' column to users table.<br>";
    } else {
        echo "'email' column already exists.<br>";
    }

    // 2. Add 'is_bot' column if not exists (for Ghost Protocol)
    $columnsBot = $pdo->query("SHOW COLUMNS FROM users LIKE 'is_bot'")->fetchAll();
    if (empty($columnsBot)) {
        $pdo->exec("ALTER TABLE users ADD COLUMN is_bot TINYINT(1) DEFAULT 0");
        echo "Added 'is_bot' column to users table.<br>";
    } else {
        echo "'is_bot' column already exists.<br>";
    }

    // 3. Create Logs Directory if it doesn't exist
    $logDir = __DIR__ . '/logs';
    if (!file_exists($logDir)) {
        mkdir($logDir, 0777, true);
        echo "Created logs directory.<br>";
    }

    // Test write
    file_put_contents($logDir . '/app.log', "[" . date('Y-m-d H:i:s') . "] DB Patch Executed\n", FILE_APPEND);
    echo "Log file initialized.<br>";

} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}
?>