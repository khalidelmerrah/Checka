<?php
/**
 * ADMIN LOGIN DEBUG SCRIPT
 * This tests the exact login flow to see what's failing
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<!DOCTYPE html><html><head><title>Login Debug</title>";
echo "<style>
body { font-family: Arial; padding: 20px; background: #1e1e1e; color: #fff; }
.success { color: #4CAF50; background: rgba(76,175,80,0.1); padding: 10px; margin: 10px 0; }
.error { color: #f44336; background: rgba(244,67,54,0.1); padding: 10px; margin: 10px 0; }
.info { color: #FFC107; background: rgba(255,193,7,0.1); padding: 10px; margin: 10px 0; }
code { background: #333; padding: 2px 5px; }
</style></head><body>";

echo "<h1>🔐 Admin Login Debug</h1>";

require_once __DIR__ . '/config/env.php';
require_once __DIR__ . '/config/db.php';

try {
    $pdo = get_db_connection();
    echo "<div class='success'>✅ Database connected</div>";

    // Test credentials
    $testUsername = 'admin';
    $testPassword = 'admin123';

    echo "<h2>Testing Login for:</h2>";
    echo "<div class='info'>Username: <code>$testUsername</code><br>Password: <code>$testPassword</code></div>";

    // Step 1: Check if admin exists
    echo "<h3>Step 1: Check if admin exists</h3>";
    $stmt = $pdo->prepare("SELECT * FROM admins WHERE username = ?");
    $stmt->execute([$testUsername]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$admin) {
        echo "<div class='error'>❌ Admin username '$testUsername' NOT FOUND in database</div>";
        echo "<div class='info'>💡 Create admin account first!</div>";

        // Show what admins exist
        $stmt = $pdo->query("SELECT id, username FROM admins");
        $allAdmins = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo "<div class='info'>Admins in database:<br>";
        if (count($allAdmins) == 0) {
            echo "No admins found - database is empty!<br>";
        } else {
            foreach ($allAdmins as $a) {
                echo "ID: {$a['id']}, Username: {$a['username']}<br>";
            }
        }
        echo "</div>";
        exit;
    }

    echo "<div class='success'>✅ Admin found in database</div>";
    echo "<div class='info'>";
    echo "ID: {$admin['id']}<br>";
    echo "Username: {$admin['username']}<br>";
    echo "Created: {$admin['created_at']}<br>";
    echo "Password Hash: <code>" . substr($admin['password_hash'], 0, 30) . "...</code><br>";
    echo "</div>";

    // Step 2: Verify password
    echo "<h3>Step 2: Verify password</h3>";
    $isValid = password_verify($testPassword, $admin['password_hash']);

    if ($isValid) {
        echo "<div class='success'>✅ Password is CORRECT!</div>";
        echo "<div class='success'>Login should work with: <code>$testUsername</code> / <code>$testPassword</code></div>";
    } else {
        echo "<div class='error'>❌ Password verification FAILED</div>";
        echo "<div class='error'>The password hash in database doesn't match '$testPassword'</div>";

        // Generate correct hash
        $correctHash = password_hash($testPassword, PASSWORD_DEFAULT);
        echo "<div class='info'>";
        echo "<h3>Fix: Run this SQL in phpMyAdmin:</h3>";
        echo "<textarea style='width:100%; height:100px; background:#333; color:#fff; padding:10px;' readonly>";
        echo "UPDATE admins SET password_hash = '$correctHash' WHERE username = '$testUsername';";
        echo "</textarea>";
        echo "</div>";

        // Also test the hash that should work
        echo "<div class='info'>";
        echo "<h3>Alternative: Use this exact SQL to reset:</h3>";
        echo "<textarea style='width:100%; height:100px; background:#333; color:#fff; padding:10px;' readonly>";
        echo "UPDATE admins SET password_hash = '\$2y\$10\$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' WHERE username = 'admin';";
        echo "</textarea>";
        echo "<p>After running either SQL, password will be: <code>admin123</code></p>";
        echo "</div>";
    }

    // Step 3: Test multiple common passwords
    echo "<h3>Step 3: Testing common passwords</h3>";
    $testPasswords = ['admin123', 'admin', 'password', '123456', 'checka'];

    foreach ($testPasswords as $pwd) {
        $match = password_verify($pwd, $admin['password_hash']);
        if ($match) {
            echo "<div class='success'>✅ Password is: <code>$pwd</code></div>";
        }
    }

    // Step 4: Show what hash is currently stored
    echo "<h3>Step 4: Current database hash</h3>";
    echo "<div class='info'>";
    echo "Full hash: <code>{$admin['password_hash']}</code><br>";
    echo "Hash length: " . strlen($admin['password_hash']) . " characters<br>";
    echo "Hash starts with: " . substr($admin['password_hash'], 0, 7) . "<br>";

    if (substr($admin['password_hash'], 0, 4) === '$2y$') {
        echo "✅ Hash format looks correct (bcrypt)<br>";
    } else {
        echo "❌ Hash format is WRONG! Should start with \$2y\$<br>";
    }
    echo "</div>";

} catch (PDOException $e) {
    echo "<div class='error'>❌ Database error: " . htmlspecialchars($e->getMessage()) . "</div>";
}

echo "<hr>";
echo "<div class='info'><strong>DELETE THIS FILE after use!</strong></div>";
echo "</body></html>";
?>