<?php
require_once '../config/db.php';
require_once '../middleware/auth.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method Not Allowed']);
    exit;
}

// AUTHENTICATE USER
$userId = AuthMiddleware::authenticate();

$input = json_decode(file_get_contents('php://input'), true);

$avatarUrl = isset($input['avatar_url']) ? $input['avatar_url'] : null;
$displayName = isset($input['display_name']) ? trim($input['display_name']) : null;

if (!$avatarUrl && !$displayName) {
    http_response_code(400);
    echo json_encode(['error' => 'Nothing to update']);
    exit;
}

// STRICT VALIDATION - Prevent XSS and offensive content
if ($avatarUrl && strlen($avatarUrl) > 500) {
    http_response_code(400);
    echo json_encode(['error' => 'Avatar URL too long']);
    exit;
}

if ($displayName) {
    // Length validation
    if (strlen($displayName) < 3 || strlen($displayName) > 20) {
        http_response_code(400);
        echo json_encode(['error' => 'Display name must be between 3 and 20 characters']);
        exit;
    }

    // Character whitelist: alphanumeric, spaces, hyphens, underscores
    if (!preg_match('/^[a-zA-Z0-9_\- ]+$/', $displayName)) {
        http_response_code(400);
        echo json_encode(['error' => 'Display name contains invalid characters. Use only letters, numbers, spaces, hyphens, and underscores.']);
        exit;
    }

    // Prevent excessive whitespace
    if (preg_match('/\s{2,}/', $displayName)) {
        http_response_code(400);
        echo json_encode(['error' => 'Display name cannot contain consecutive spaces']);
        exit;
    }
}

try {
    $pdo = get_db_connection();

    if ($avatarUrl && $displayName) {
        $stmt = $pdo->prepare("UPDATE users SET avatar_url = ?, display_name = ? WHERE id = ?");
        $result = $stmt->execute([$avatarUrl, $displayName, $userId]);
    } else if ($avatarUrl) {
        $stmt = $pdo->prepare("UPDATE users SET avatar_url = ? WHERE id = ?");
        $result = $stmt->execute([$avatarUrl, $userId]);
    } else if ($displayName) {
        $stmt = $pdo->prepare("UPDATE users SET display_name = ? WHERE id = ?");
        $result = $stmt->execute([$displayName, $userId]);
    }

    if ($result) {
        echo json_encode(['success' => true, 'message' => 'Profile updated']);
    } else {
        http_response_code(500);
        echo json_encode(['error' => 'Failed to update profile']);
    }

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => 'Database error: ' . $e->getMessage()]);
}
?>