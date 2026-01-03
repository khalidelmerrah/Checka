<?php
require_once __DIR__ . '/../config/db.php';

$mode = $_GET['mode'] ?? 'status'; // 'on', 'off', 'status'

try {
    // 1. Ensure setting exists
    $stmt = $pdo->prepare("INSERT IGNORE INTO app_settings (setting_key, setting_value, description) VALUES ('bots_enabled', 'true', 'Enable/Disable Bot Matchmaking')");
    $stmt->execute();

    // 2. Update if requested
    if ($mode === 'on') {
        $pdo->prepare("UPDATE app_settings SET setting_value = 'true' WHERE setting_key = 'bots_enabled'")->execute();
        echo "Bots ENABLED.";
    } elseif ($mode === 'off') {
        $pdo->prepare("UPDATE app_settings SET setting_value = 'false' WHERE setting_key = 'bots_enabled'")->execute();
        echo "Bots DISABLED.";
    }

    // 3. Show Status
    $stmt = $pdo->query("SELECT setting_value FROM app_settings WHERE setting_key = 'bots_enabled'");
    $status = $stmt->fetchColumn();

    echo "<h1>Bot Matchmaking is: " . ($status === 'true' ? "ON" : "OFF") . "</h1>";
    echo "<a href='?mode=on'>[Enable Bots]</a> | <a href='?mode=off'>[Disable Bots]</a>";

} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}
?>