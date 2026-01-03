<?php
/**
 * Secure Authentication Endpoint
 * Verifies Google ID tokens cryptographically and issues session tokens
 */

require_once __DIR__ . '/../vendor/autoload.php';
require_once __DIR__ . '/../config/env.php';
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../middleware/auth.php';

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

// Load environment variables
Env::load();

// Get POST Data
$data = json_decode(file_get_contents("php://input"), true);
$idToken = $data['id_token'] ?? '';

if (!$idToken) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Missing id_token']);
    exit;
}

try {
    // Initialize Google Client
    $client = new Google_Client([
        'client_id' => Env::getGoogleClientId()
    ]);

    // Verify ID Token cryptographically
    $payload = $client->verifyIdToken($idToken);

    if (!$payload) {
        http_response_code(401);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid ID token',
            'hint' => 'Token verification failed'
        ]);
        exit;
    }

    // Extract verified user information
    $googleId = $payload['sub'];
    $email = $payload['email'] ?? '';
    $name = $payload['name'] ?? 'Unknown Player';
    $picture = $payload['picture'] ?? '';

    // Validate essential fields
    if (!$googleId) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Invalid token payload']);
        exit;
    }

    $pdo = get_db_connection();

    // Check if user exists
    $stmt = $pdo->prepare("SELECT * FROM users WHERE google_subject_id = ?");
    $stmt->execute([$googleId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        // Create new user - let database auto-generate INT ID
        $stmt = $pdo->prepare("
            INSERT INTO users (google_subject_id, display_name, email, avatar_url, elo_rating, xp, level, total_wins, total_matches) 
            VALUES (?, ?, ?, ?, 1200, 0, 1, 0, 0)
        ");
        $stmt->execute([$googleId, $name, $email, $picture]);

        // Get the auto-generated ID
        $userId = $pdo->lastInsertId();

        // LOGGING
        $logEntry = "[" . date('Y-m-d H:i:s') . "] NEW USER REGISTERED: $name ($email) - ID: $userId\n";
        @file_put_contents(__DIR__ . '/../logs/app.log', $logEntry, FILE_APPEND);

        // Fetch created user
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$userId]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
    } else {
        // Update last login timestamp
        $stmt = $pdo->prepare("UPDATE users SET last_login = NOW() WHERE id = ?");
        $stmt->execute([$user['id']]);

        // Optionally update profile info if changed
        if ($picture && $picture !== $user['avatar_url']) {
            $stmt = $pdo->prepare("UPDATE users SET avatar_url = ? WHERE id = ?");
            $stmt->execute([$picture, $user['id']]);
        }
    }

    // Check if user is banned
    if ($user['is_banned']) {
        http_response_code(403);
        echo json_encode([
            'success' => false,
            'message' => 'Account suspended',
            'banned' => true
        ]);
        exit;
    }

    // Generate secure session token
    $sessionToken = AuthMiddleware::createSession(
        $user['id'],
        Env::getSessionExpiryHours()
    );

    // Return success with session token
    echo json_encode([
        'success' => true,
        'session_token' => $sessionToken,
        'user_id' => $user['id'],
        'username' => $user['display_name'],
        'elo' => (int) $user['elo_rating'],
        'xp' => (int) $user['xp'],
        'level' => (int) $user['level'],
        'avatar_url' => $user['avatar_url'],
        'total_wins' => (int) ($user['total_wins'] ?? 0),
        'total_matches' => (int) ($user['total_matches'] ?? 0)
    ]);

} catch (Google_Exception $e) {
    http_response_code(401);
    echo json_encode([
        'success' => false,
        'message' => 'Token verification failed',
        'error' => Env::isDebug() ? $e->getMessage() : 'Authentication error'
    ]);
    error_log("Google Auth Error: " . $e->getMessage());
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database error'
    ]);
    error_log("Database Error in auth.php: " . $e->getMessage());
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Authentication service error'
    ]);
    error_log("Unexpected Error in auth.php: " . $e->getMessage());
}
?>