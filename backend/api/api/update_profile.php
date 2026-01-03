<?php
require_once '../config/db.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method Not Allowed']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['user_id'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing user_id']);
    exit;
}

$userId = $input['user_id'];
$avatarUrl = isset($input['avatar_url']) ? $input['avatar_url'] : null;
$displayName = isset($input['display_name']) ? trim($input['display_name']) : null;

if (!$avatarUrl && !$displayName) {
    http_response_code(400);
    echo json_encode(['error' => 'Nothing to update']);
    exit;
}

// Basic validation
if ($avatarUrl && strlen($avatarUrl) > 500) {
    http_response_code(400);
    echo json_encode(['error' => 'Avatar URL too long']);
    exit;
}
if ($displayName && (strlen($displayName) < 3 || strlen($displayName) > 20)) {
    http_response_code(400);
    echo json_encode(['error' => 'Display name must be between 3 and 20 characters']);
    exit;
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