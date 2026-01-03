<?php
require_once '../config/db.php';

header("Content-Type: application/json");

// 1. Get POST Data
$data = json_decode(file_get_contents("php://input"), true);
$authCode = $data['code'] ?? '';

if (!$authCode) {
    echo json_encode(['success' => false, 'message' => 'No auth code provided']);
    exit;
}

// 2. Exchange Auth Code for Tokens (Manual CURL to avoid composer deps for now)
$clientId = "460882543734-1krncmoh72rnchosf1j0dc50bru0eidd.apps.googleusercontent.com";
$clientSecret = "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";
// For Android Play Games code exchange, redirect_uri is usually empty string or standard callback
$redirectUri = "";

$tokenUrl = "https://oauth2.googleapis.com/token";
$postFields = [
    'code' => $authCode,
    'client_id' => $clientId,
    'client_secret' => $clientSecret,
    'redirect_uri' => $redirectUri,
    'grant_type' => 'authorization_code'
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $tokenUrl);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postFields));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

$tokenData = json_decode($response, true);

if (!isset($tokenData['id_token'])) {
    // Fallback: Code might be invalid or used.
    error_log("Google Token Exchange Failed: " . $response);
    echo json_encode(['success' => false, 'message' => 'Invalid auth code', 'debug' => $tokenData]);
    exit;
}

// 3. Decode ID Token to get User Info
// Simple split decode (In produciton verify signature!)
$parts = explode('.', $tokenData['id_token']);
$payload = json_decode(base64_decode(str_replace(['-', '_'], ['+', '/'], $parts[1])), true);

$googleId = $payload['sub'];
$email = $payload['email'] ?? '';
$name = $payload['name'] ?? 'Unknown Player';
$picture = $payload['picture'] ?? '';

// 4. Update/Insert DB
try {
    // Check if user exists
    $stmt = $pdo->prepare("SELECT * FROM users WHERE google_subject_id = ?");
    $stmt->execute([$googleId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        // Create new user
        $userId = uniqid('u_');
        $stmt = $pdo->prepare("INSERT INTO users (id, google_subject_id, display_name, email, elo_rating, xp, level) VALUES (?, ?, ?, ?, 1200, 0, 1)");
        $stmt->execute([$userId, $googleId, $name, $email]);

        // LOGGING
        $logEntry = "[" . date('Y-m-d H:i:s') . "] NEW USER REGISTERED: $name ($email) - ID: $userId\n";
        file_put_contents(__DIR__ . '/../logs/app.log', $logEntry, FILE_APPEND);

        // Fetch again
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$userId]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
    } else {
        // Update basic info if changed? (Optional)
    }

    echo json_encode([
        'success' => true,
        'user_id' => $user['id'],
        'username' => $user['display_name'],
        'elo' => (int) $user['elo_rating'],
        'xp' => (int) $user['xp'],
        'level' => (int) $user['level']
    ]);

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>