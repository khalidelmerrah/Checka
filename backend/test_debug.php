<?php
// ============================================
// ADMIN PANEL 500 ERROR DEBUG SCRIPT
// ============================================
// Upload this file to your server and visit it in browser
// It will show you exactly what's broken

error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<!DOCTYPE html><html><head><title>Debug Report</title>";
echo "<style>
body { font-family: Arial; padding: 20px; background: #1e1e1e; color: #fff; }
h1 { color: #4CAF50; }
h2 { color: #2196F3; margin-top: 30px; }
.success { color: #4CAF50; padding: 10px; background: rgba(76,175,80,0.1); margin: 10px 0; }
.error { color: #f44336; padding: 10px; background: rgba(244,67,54,0.1); margin: 10px 0; }
.info { color: #FFC107; padding: 10px; background: rgba(255,193,7,0.1); margin: 10px 0; }
code { background: #333; padding: 2px 5px; border-radius: 3px; }
</style></head><body>";

echo "<h1>🔍 Admin Panel Debug Report</h1>";
echo "<p>Generated: " . date('Y-m-d H:i:s') . "</p>";

// Test 1: Basic PHP
echo "<h2>Test 1: PHP Execution</h2>";
echo "<div class='success'>✅ PHP is working! Version: " . PHP_VERSION . "</div>";

// Test 2: Current Directory
echo "<h2>Test 2: File Locations</h2>";
$currentDir = __DIR__;
echo "<div class='info'>📁 Current directory: <code>$currentDir</code></div>";

// Test 3: .env File
echo "<h2>Test 3: Environment File (.env)</h2>";
$envPath = $currentDir . '/.env';
if (file_exists($envPath)) {
    echo "<div class='success'>✅ .env file exists</div>";
    echo "<div class='info'>Location: <code>$envPath</code></div>";

    // Try to read it (first 5 lines only, for security)
    $lines = file($envPath, FILE_IGNORE_NEW_LINES);
    echo "<div class='info'>Preview (first 5 lines):<br>";
    for ($i = 0; $i < min(5, count($lines)); $i++) {
        // Hide sensitive values
        $line = $lines[$i];
        if (strpos($line, '=') !== false) {
            list($key, $val) = explode('=', $line, 2);
            echo "<code>" . htmlspecialchars($key) . "=***</code><br>";
        } else {
            echo "<code>" . htmlspecialchars($line) . "</code><br>";
        }
    }
    echo "</div>";
} else {
    echo "<div class='error'>❌ .env file NOT FOUND</div>";
    echo "<div class='error'>Expected location: <code>$envPath</code></div>";
    echo "<div class='info'>💡 Upload the .env file to this directory!</div>";
}

// Test 4: Vendor Folder
echo "<h2>Test 4: Composer Dependencies (vendor/)</h2>";
$vendorAutoload = $currentDir . '/vendor/autoload.php';
if (file_exists($vendorAutoload)) {
    echo "<div class='success'>✅ Composer vendor folder exists</div>";
    echo "<div class='info'>Autoload: <code>$vendorAutoload</code></div>";
} else {
    echo "<div class='error'>❌ Composer vendor folder NOT FOUND</div>";
    echo "<div class='error'>Expected: <code>$vendorAutoload</code></div>";
    echo "<div class='info'>💡 Run: <code>composer install</code> in this directory</div>";
}

// Test 5: Config Files
echo "<h2>Test 5: Configuration Files</h2>";

$configEnv = $currentDir . '/config/env.php';
if (file_exists($configEnv)) {
    echo "<div class='success'>✅ config/env.php exists</div>";
} else {
    echo "<div class='error'>❌ config/env.php NOT FOUND</div>";
    echo "<div class='error'>Path: <code>$configEnv</code></div>";
}

$configDb = $currentDir . '/config/db.php';
if (file_exists($configDb)) {
    echo "<div class='success'>✅ config/db.php exists</div>";
} else {
    echo "<div class='error'>❌ config/db.php NOT FOUND</div>";
    echo "<div class='error'>Path: <code>$configDb</code></div>";
}

// Test 6: Admin Folder
echo "<h2>Test 6: Admin Panel Files</h2>";
$adminIndex = $currentDir . '/admin/index.php';
if (file_exists($adminIndex)) {
    echo "<div class='success'>✅ admin/index.php exists</div>";
    echo "<div class='info'>Admin URL should be: <code>https://admin.checka.top/admin/</code></div>";
} else {
    echo "<div class='error'>❌ admin/index.php NOT FOUND</div>";
    echo "<div class='error'>Path: <code>$adminIndex</code></div>";
}

// Test 7: Load env.php
echo "<h2>Test 7: Loading env.php</h2>";
if (file_exists($configEnv) && file_exists($vendorAutoload)) {
    try {
        require_once $configEnv;
        echo "<div class='success'>✅ env.php loaded successfully</div>";

        // Test Env class
        if (class_exists('Env')) {
            echo "<div class='success'>✅ Env class is available</div>";
        } else {
            echo "<div class='error'>❌ Env class not found after loading env.php</div>";
        }
    } catch (Exception $e) {
        echo "<div class='error'>❌ Error loading env.php</div>";
        echo "<div class='error'>Error: " . htmlspecialchars($e->getMessage()) . "</div>";
        echo "<div class='error'>Stack trace:<br><pre>" . htmlspecialchars($e->getTraceAsString()) . "</pre></div>";
    }
} else {
    echo "<div class='error'>❌ Cannot test - missing env.php or vendor/</div>";
}

// Test 8: Database Connection
echo "<h2>Test 8: Database Connection</h2>";
if (file_exists($configDb) && class_exists('Env', false)) {
    try {
        require_once $configDb;
        echo "<div class='success'>✅ db.php loaded successfully</div>";

        $pdo = get_db_connection();
        echo "<div class='success'>✅ Database connected!</div>";

        // Test query
        $stmt = $pdo->query("SELECT COUNT(*) as count FROM users");
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        echo "<div class='success'>✅ Database query works! Found {$result['count']} users</div>";

    } catch (Exception $e) {
        echo "<div class='error'>❌ Database error</div>";
        echo "<div class='error'>Error: " . htmlspecialchars($e->getMessage()) . "</div>";
        echo "<div class='info'>💡 Check your .env file database credentials</div>";
    }
} else {
    echo "<div class='error'>❌ Cannot test - missing db.php or Env class</div>";
}

// Summary
echo "<h2>Summary</h2>";
echo "<div class='info'>";
echo "<strong>If all tests pass ✅, your admin panel should work!</strong><br><br>";
echo "If any test fails ❌, fix that issue first.<br><br>";
echo "<strong>Common fixes:</strong><br>";
echo "• Missing .env → Upload .env file to <code>$currentDir</code><br>";
echo "• Missing vendor/ → Run <code>composer install</code><br>";
echo "• Database error → Check credentials in .env file<br>";
echo "• Wrong paths → Make sure you uploaded files to correct directory<br>";
echo "</div>";

echo "<hr>";
echo "<h2>Directory Tree</h2>";
echo "<div class='info'><pre>";
echo "Expected structure in <code>$currentDir</code>:\n";
echo "├── .env (your credentials)\n";
echo "├── composer.json\n";
echo "├── vendor/ (Composer packages)\n";
echo "│   └── autoload.php\n";
echo "├── config/\n";
echo "│   ├── env.php\n";
echo "│   └── db.php\n";
echo "└── admin/\n";
echo "    └── index.php\n";
echo "</pre></div>";

echo "</body></html>";
?>