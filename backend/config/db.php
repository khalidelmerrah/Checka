<?php
// backend/config/db.php

// cPanel Database Credentials
// CHANGE THESE TO YOUR REAL CPANEL DETAILS
define('DB_HOST', 'localhost');
define('DB_NAME', 'reyagency_chekadmin');
define('DB_USER', 'reyagency_chkadmin');
define('DB_PASS', 'QtLuG+SQ[?4.Dl-u');

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
?>