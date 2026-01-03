<?php
/**
 * ADMIN ACCOUNT SETUP SCRIPT
 * Run this ONCE to create or reset your admin account
 * Delete this file after use for security!
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<!DOCTYPE html><html><head><title>Admin Setup</title>";
echo "<style>
body { font-family: Arial; padding: 20px; background: #1e1e1e; color: #fff; }
.success { color: #4CAF50; padding: 10px; background: rgba(76,175,80,0.1); margin: 10px 0; }
.error { color: #f44336; padding: 10px; background: rgba(244,67,54,0.1); margin: 10px 0; }
code { background: #333; padding: 2px 5px; border-radius: 3px; }
</style></head><body>";

echo "<h1>🔧 Admin Account Setup</h1>";

// Load configuration
require_once __DIR__ . '/config/env.php';
require_once __DIR__ . '/config/db.php';

try {
    $pdo = get_db_connection();
    echo "<div class='success'>✅ Database connected</div>";

    // Check if admins table exists
    $stmt = $pdo->query("SHOW TABLES LIKE 'admins'");
    if ($stmt->rowCount() == 0) {
        echo "<div class='error'>❌ Admins table doesn't exist!</div>";
        echo "<div class='error'>You need to import the fresh_database_schema.sql first</div>";
        exit;
    }

    // Set your credentials here
    $username = 'admin';
    $password = 'admin123'; // Change this to your desired password

    // Generate password hash
    $passwordHash = password_hash($password, PASSWORD_DEFAULT);

    // Check if admin already exists
    $stmt = $pdo->prepare("SELECT id FROM admins WHERE username = ?");
    $stmt->execute([$username]);

    if ($stmt->rowCount() > 0) {
        // Update existing admin
        $stmt = $pdo->prepare("UPDATE admins SET password_hash = ? WHERE username = ?");
        $stmt->execute([$passwordHash, $username]);
        echo "<div class='success'>✅ Admin password UPDATED</div>";
    } else {
        // Create new admin
        $stmt = $pdo->prepare("INSERT INTO admins (username, password_hash) VALUES (?, ?)");
        $stmt->execute([$username, $passwordHash]);
        echo "<div class='success'>✅ Admin account CREATED</div>";
    }

    echo "<div class='success'>";
    echo "<h2>Your Admin Credentials:</h2>";
    echo "<p><strong>Username:</strong> <code>$username</code></p>";
    echo "<p><strong>Password:</strong> <code>$password</code></p>";
    echo "<p><strong>Login URL:</strong> <code>https://admin.checka.top/admin/</code></p>";
    echo "</div>";

    echo "<div class='error'>";
    echo "<h3>⚠️ IMPORTANT SECURITY STEPS:</h3>";
    echo "<ol>";
    echo "<li>DELETE THIS FILE (<code>create_admin.php</code>) immediately after use!</li>";
    echo "<li>Change your password after first login</li>";
    echo "</ol>";
    echo "</div>";

    // Verify admin exists
    $stmt = $pdo->prepare("SELECT id, username, created_at FROM admins WHERE username = ?");
    $stmt->execute([$username]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);

    echo "<div class='success'>";
    echo "<h3>Verification:</h3>";
    echo "<p>Admin ID: {$admin['id']}</p>";
    echo "<p>Username: {$admin['username']}</p>";
    echo "<p>Created: {$admin['created_at']}</p>";
    echo "</div>";

} catch (PDOException $e) {
    echo "<div class='error'>❌ Database error: " . htmlspecialchars($e->getMessage()) . "</div>";
}

echo "</body></html>";
?>