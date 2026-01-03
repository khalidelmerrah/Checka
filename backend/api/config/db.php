<?php
// backend/config/db.php

// cPanel Database Credentials
// CHANGE THESE TO YOUR REAL CPANEL DETAILS
require_once __DIR__ . '/env_loader.php';
loadEnv(__DIR__ . '/../.env');

// cPanel Database Credentials
define('DB_HOST', getenv('DB_HOST') ?: 'localhost');
define('DB_NAME', getenv('DB_NAME') ?: 'reyagency_chekadmin');
define('DB_USER', getenv('DB_USER') ?: 'reyagency_chkadmin');
define('DB_PASS', getenv('DB_PASS') ?: 'QtLuG+SQ[?4.Dl-u'); // Fallback ONLY for dev if .env missing, usually bad practice but kept for transition


function get_db_connection()
{
    try {
        $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4";
        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ];
        return new PDO($dsn, DB_USER, DB_PASS, $options);
    } catch (PDOException $e) {
        die("Database Connection Failed: " . $e->getMessage()); // In prod, log this instead of showing user
    }
}

$pdo = get_db_connection();
?>