<?php
// backend/install.php
require_once 'config/db.php';

// ONLY RUN THIS ONCE
$secret_access_code = 'setup_checka_securely'; // Change this if you want to protect this script before upload

if (!isset($_GET['code']) || $_GET['code'] !== $secret_access_code) {
    die("Access Denied. Please provide the correct code parameter.");
}

$pdo = get_db_connection();

// 1. Run Schema
$sql = file_get_contents('sql/schema.sql');
// Remove the default insert from schema first if strictly needed, but INSERT IGNORE handles it.
// We will manually insert the strong admin here.

try {
    $pdo->exec($sql);
    echo "Tables created successfully.<br>";
} catch (PDOException $e) {
    die("Error creating tables: " . $e->getMessage());
}

// 2. Generate Strong Credentials
$username = 'admin_' . bin2hex(random_bytes(4)); // e.g., admin_a1b2c3d4
$password = bin2hex(random_bytes(10)); // 20 chars random hex

$hash = password_hash($password, PASSWORD_BCRYPT);

// 3. Insert Admin
$stmt = $pdo->prepare("INSERT INTO admins (username, password_hash) VALUES (?, ?)");
try {
    $stmt->execute([$username, $hash]);
    echo "<h1>Installation Successful</h1>";
    echo "<div style='border: 2px solid green; padding: 20px; background: #e8f5e9;'>";
    echo "<h3>SAVE THESE CREDENTIALS NOW!</h3>";
    echo "<p><strong>Username:</strong> " . htmlspecialchars($username) . "</p>";
    echo "<p><strong>Password:</strong> " . htmlspecialchars($password) . "</p>";
    echo "<p><em>This script will not show these again.</em></p>";
    echo "</div>";
    echo "<br><p><strong>SECURITY WARNING:</strong> Please delete <code>install.php</code> from your server immediately after use.</p>";
    echo "<a href='admin/index.php'>Go to Admin Login</a>";
} catch (PDOException $e) {
    echo "Could not create admin (maybe already exists?): " . $e->getMessage();
}
?>