<?php
/**
 * Generate password hash for new admin
 * Run this once to get the hash, then use it in SQL
 */

$newPassword = '5f3870e8f8c6b213e804';
$hash = password_hash($newPassword, PASSWORD_DEFAULT);

echo "<!DOCTYPE html><html><head><title>Admin Hash Generator</title>";
echo "<style>body{font-family:Arial;padding:20px;background:#1e1e1e;color:#fff;}</style></head><body>";
echo "<h1>Admin Password Hash Generator</h1>";
echo "<div style='background:#333;padding:20px;margin:20px 0;'>";
echo "<p><strong>Password:</strong> <code>$newPassword</code></p>";
echo "<p><strong>Hash:</strong><br><textarea style='width:100%;height:80px;background:#222;color:#0f0;padding:10px;border:1px solid #555;' readonly>$hash</textarea></p>";
echo "</div>";

echo "<h2>SQL to run in phpMyAdmin:</h2>";
echo "<textarea style='width:100%;height:150px;background:#222;color:#fff;padding:10px;border:1px solid #555;' readonly>";
echo "-- Delete old admin\n";
echo "DELETE FROM admins WHERE username = 'admin';\n\n";
echo "-- Create new admin\n";
echo "INSERT INTO admins (username, password_hash) VALUES\n";
echo "('admin_9a505ab8', '$hash');\n\n";
echo "-- Verify\n";
echo "SELECT * FROM admins;";
echo "</textarea>";

echo "<p><strong>Delete this file after use!</strong></p>";
echo "</body></html>";
?>