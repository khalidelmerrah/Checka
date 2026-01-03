<?php
/**
 * Authentication Middleware
 * Validates session tokens and provides authenticated user context
 */

require_once __DIR__ . '/../config/db.php';

class AuthMiddleware
{

    /**
     * Verify authorization header and return authenticated user ID
     * 
     * @return string User ID if valid token
     * @throws Exception if authentication fails
     */
    public static function authenticate()
    {
        // Extract Authorization header
        $headers = getallheaders();
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? null;

        if (!$authHeader) {
            http_response_code(401);
            echo json_encode(['error' => 'Missing Authorization header']);
            exit;
        }

        // Extract Bearer token
        if (!preg_match('/^Bearer\s+(.+)$/i', $authHeader, $matches)) {
            http_response_code(401);
            echo json_encode(['error' => 'Invalid Authorization header format']);
            exit;
        }

        $token = $matches[1];

        // Validate token format (64 hex characters)
        if (!preg_match('/^[a-f0-9]{64}$/i', $token)) {
            http_response_code(401);
            echo json_encode(['error' => 'Invalid token format']);
            exit;
        }

        try {
            $pdo = get_db_connection();

            // Check token exists and is not expired
            $stmt = $pdo->prepare("
                SELECT user_id, expires_at 
                FROM sessions 
                WHERE token = ? AND expires_at > NOW()
            ");
            $stmt->execute([$token]);
            $session = $stmt->fetch(PDO::FETCH_ASSOC);

            if (!$session) {
                http_response_code(401);
                echo json_encode(['error' => 'Invalid or expired token']);
                exit;
            }

            // Update last activity timestamp
            $updateStmt = $pdo->prepare("UPDATE sessions SET last_activity = NOW() WHERE token = ?");
            $updateStmt->execute([$token]);

            // Return authenticated user ID
            return $session['user_id'];

        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(['error' => 'Authentication service error']);
            error_log("Auth middleware error: " . $e->getMessage());
            exit;
        }
    }

    /**
     * Generate a cryptographically secure session token
     * 
     * @return string 64-character hex token
     */
    public static function generateToken()
    {
        return bin2hex(random_bytes(32));
    }

    /**
     * Create a new session for a user
     * 
     * @param string $userId User ID to create session for
     * @param int $expiryHours Number of hours until token expires
     * @return string Generated session token
     */
    public static function createSession($userId, $expiryHours = 24)
    {
        $token = self::generateToken();
        $pdo = get_db_connection();

        $stmt = $pdo->prepare("
            INSERT INTO sessions (token, user_id, expires_at) 
            VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? HOUR))
        ");
        $stmt->execute([$token, $userId, $expiryHours]);

        return $token;
    }

    /**
     * Revoke a session token
     * 
     * @param string $token Token to revoke
     */
    public static function revokeSession($token)
    {
        $pdo = get_db_connection();
        $stmt = $pdo->prepare("DELETE FROM sessions WHERE token = ?");
        $stmt->execute([$token]);
    }

    /**
     * Revoke all sessions for a user
     * 
     * @param string $userId User ID to revoke sessions for
     */
    public static function revokeAllUserSessions($userId)
    {
        $pdo = get_db_connection();
        $stmt = $pdo->prepare("DELETE FROM sessions WHERE user_id = ?");
        $stmt->execute([$userId]);
    }
}
