<?php
$logFile = __DIR__ . '/../logs/app.log';

if (file_exists($logFile)) {
    // Return last 200 lines
    $lines = file($logFile);
    $lastLines = array_slice($lines, -200);
    echo implode("", $lastLines);
} else {
    echo "No logs found.";
}
?>