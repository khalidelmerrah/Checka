<?php
/**
 * COMPREHENSIVE PASSWORD HASH TEST
 * This will test every aspect of password hashing and verification
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<!DOCTYPE html><html><head><title>Password Hash Deep Test</title>";
echo "<style>
body { font-family: monospace; padding: 20px; background: #000; color: #0f0; }
.success { color: #0f0; }
.error { color: #f00; }
.info { color: #ff0; }
h2 { color: #0ff; border-bottom: 1px solid #0ff; }
pre { background: #111; padding: 10px; border: 1px solid #333; }
</style></head><body>";

echo "<h1>🔬 PASSWORD HASH DEEP DIAGNOSTIC</h1>";

require_once __DIR__ . '/config/env.php';
require_once __DIR__ . '/config/db.php';

// Test password
$username = 'admin_9a505ab8';
$password = '5f3870e8f8c6b213e804';

echo "<h2>TEST 1: PHP PASSWORD FUNCTIONS</h2>";
echo "<pre>";
echo "PHP Version: " . PHP_VERSION . "\n";
echo "password_hash available: " . (function_exists('password_hash') ? 'YES' : 'NO') . "\n";
echo "password_verify available: " . (function_exists('password_verify') ? 'YES' : 'NO') . "\n";
echo "</pre>";

echo "<h2>TEST 2: GENERATE FRESH HASH</h2>";
$freshHash = password_hash($password, PASSWORD_DEFAULT);
echo "<pre>";
echo "Password: $password\n";
echo "Fresh Hash: $freshHash\n";
echo "Hash Length: " . strlen($freshHash) . "\n";
echo "</pre>";

echo "<h2>TEST 3: VERIFY FRESH HASH</h2>";
$verifyFresh = password_verify($password, $freshHash);
echo "<pre>";
echo "Verify Result: " . ($verifyFresh ? '<span class="success">✅ PASS</span>' : '<span class="error">❌ FAIL</span>') . "\n";
echo "</pre>";

if (!$verifyFresh) {
    echo "<div class='error'>CRITICAL ERROR: PHP password functions are broken!</div>";
    exit;
}

echo "<h2>TEST 4: DATABASE CONNECTION</h2>";
try {
    $pdo = get_db_connection();
    echo "<pre class='success'>✅ Connected to database</pre>";
} catch (Exception $e) {
    echo "<pre class='error'>❌ Database error: " . $e->getMessage() . "</pre>";
    exit;
}

echo "<h2>TEST 5: CHECK ADMIN IN DATABASE</h2>";
$stmt = $pdo->prepare("SELECT * FROM admins WHERE username = ?");
$stmt->execute([$username]);
$admin = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$admin) {
    echo "<pre class='error'>❌ Admin '$username' not found in database!</pre>";

    // Show all admins
    $stmt = $pdo->query("SELECT id, username FROM admins");
    $all = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "<pre class='info'>Admins in database:\n";
    foreach ($all as $a) {
        echo "  - ID: {$a['id']}, Username: {$a['username']}\n";
    }
    echo "</pre>";

    echo "<h2>FIX: Create admin now</h2>";
    echo "<pre>";
    try {
        $stmt = $pdo->prepare("INSERT INTO admins (username, password_hash) VALUES (?, ?)");
        $stmt->execute([$username, $freshHash]);
        echo "<span class='success'>✅ Admin created with correct hash!</span>\n";
        echo "Username: $username\n";
        echo "Password: $password\n";
    } catch (Exception $e) {
        echo "<span class='error'>❌ Error: " . $e->getMessage() . "</span>\n";
    }
    echo "</pre>";

} else {
    echo "<pre class='success'>✅ Admin found in database</pre>";

    echo "<h2>TEST 6: EXAMINE STORED HASH</h2>";
    $storedHash = $admin['password_hash'];
    echo "<pre>";
    echo "Stored Hash: $storedHash\n";
    echo "Hash Length: " . strlen($storedHash) . "\n";
    echo "First 10 chars: " . substr($storedHash, 0, 10) . "\n";

    // Check if it's a valid bcrypt hash
    if (substr($storedHash, 0, 4) === '$2y$') {
        echo "<span class='success'>✅ Hash format looks valid (bcrypt)</span>\n";
    } else {
        echo "<span class='error'>❌ Hash format is WRONG! Should start with \$2y\$</span>\n";
    }
    echo "</pre>";

    echo "<h2>TEST 7: VERIFY PASSWORD AGAINST STORED HASH</h2>";
    $verifyStored = password_verify($password, $storedHash);
    echo "<pre>";
    echo "Password: $password\n";
    echo "Stored Hash: $storedHash\n";
    echo "Result: " . ($verifyStored ? '<span class="success">✅ MATCH!</span>' : '<span class="error">❌ NO MATCH</span>') . "\n";
    echo "</pre>";

    if (!$verifyStored) {
        echo "<h2>TEST 8: TRY COMMON PASSWORDS</h2>";
        $testPwds = ['admin123', 'password', 'admin', '123456', '5f3870e8f8c6b213e804'];
        echo "<pre>";
        foreach ($testPwds as $pwd) {
            $match = password_verify($pwd, $storedHash);
            echo "Testing '$pwd': " . ($match ? '<span class="success">✅ MATCH</span>' : '❌ no') . "\n";
        }
        echo "</pre>";

        echo "<h2>FIX: UPDATE WITH CORRECT HASH</h2>";
        echo "<pre>";
        try {
            $stmt = $pdo->prepare("UPDATE admins SET password_hash = ? WHERE username = ?");
            $stmt->execute([$freshHash, $username]);
            echo "<span class='success'>✅ Password hash UPDATED!</span>\n";
            echo "Username: $username\n";
            echo "Password: $password\n";

            // Verify the update worked
            $stmt = $pdo->prepare("SELECT password_hash FROM admins WHERE username = ?");
            $stmt->execute([$username]);
            $newHash = $stmt->fetchColumn();
            $verifyNew = password_verify($password, $newHash);
            echo "\nVerification after update: " . ($verifyNew ? '<span class="success">✅ WORKS!</span>' : '<span class="error">❌ STILL BROKEN</span>') . "\n";
        } catch (Exception $e) {
            echo "<span class='error'>❌ Error: " . $e->getMessage() . "</span>\n";
        }
        echo "</pre>";
    } else {
        echo "<div class='success'><h2>✅ EVERYTHING IS WORKING!</h2>";
        echo "<p>Login should work with:</p>";
        echo "<p>Username: <strong>$username</strong></p>";
        echo "<p>Password: <strong>$password</strong></p>";
        echo "</div>";
    }
}

echo "<h2>TEST 9: SIMULATE ADMIN LOGIN</h2>";
echo "<pre>";

// Simulate exact login logic from admin/index.php
$stmt = $pdo->prepare("SELECT * FROM admins WHERE username = ?");
$stmt->execute([$username]);
$testAdmin = $stmt->fetch(PDO::FETCH_ASSOC);

if ($testAdmin && password_verify($password, $testAdmin['password_hash'])) {
    echo "<span class='success'>✅ LOGIN SIMULATION: SUCCESS</span>\n";
    echo "Admin ID: {$testAdmin['id']}\n";
    echo "Username: {$testAdmin['username']}\n";
} else {
    echo "<span class='error'>❌ LOGIN SIMULATION: FAILED</span>\n";
    if (!$testAdmin) {
        echo "Reason: Admin not found\n";
    } else {
        echo "Reason: Password verification failed\n";
    }
}
echo "</pre>";

echo "<hr><p class='error'><strong>DELETE THIS FILE AFTER USE!</strong></p>";
echo "</body></html>";
?>