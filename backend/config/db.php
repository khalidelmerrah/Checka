<?php
/**
 * Database Connection Manager
 * Uses environment variables for secure configuration
 */

require_once __DIR__ . '/env.php';

// Load environment variables
Env::load();

function get_db_connection()
{
    try {
        $dsn = "mysql:host=" . Env::getDbHost() . ";dbname=" . Env::getDbName() . ";charset=utf8mb4";
        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ];
        return new PDO($dsn, Env::getDbUser(), Env::getDbPass(), $options);
    } catch (PDOException $e) {
        // In production, log this instead of displaying to user
        error_log("Database Connection Failed: " . $e->getMessage());

        if (Env::isDebug()) {
            die("Database Connection Failed: " . $e->getMessage());
        } else {
            die("Database service temporarily unavailable");
        }
    }
}

// Global PDO instance for legacy compatibility
$pdo = get_db_connection();
?>