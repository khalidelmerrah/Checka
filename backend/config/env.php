<?php
/**
 * Environment Configuration Loader
 * Loads .env file and provides secure access to environment variables
 */

require_once __DIR__ . '/../vendor/autoload.php';

use Dotenv\Dotenv;

class Env
{
    private static $loaded = false;

    public static function load()
    {
        if (self::$loaded) {
            return;
        }

        $dotenv = Dotenv::createImmutable(__DIR__ . '/..');
        $dotenv->load();

        // Validate required variables
        $dotenv->required([
            'GOOGLE_CLIENT_ID',
            'GOOGLE_CLIENT_SECRET',
            'DB_HOST',
            'DB_NAME',
            'DB_USER'
        ]);

        self::$loaded = true;
    }

    public static function get($key, $default = null)
    {
        self::load();
        return $_ENV[$key] ?? $default;
    }

    public static function getGoogleClientId()
    {
        return self::get('GOOGLE_CLIENT_ID');
    }

    public static function getGoogleClientSecret()
    {
        return self::get('GOOGLE_CLIENT_SECRET');
    }

    public static function getDbHost()
    {
        return self::get('DB_HOST', 'localhost');
    }

    public static function getDbName()
    {
        return self::get('DB_NAME');
    }

    public static function getDbUser()
    {
        return self::get('DB_USER', 'root');
    }

    public static function getDbPass()
    {
        return self::get('DB_PASS', '');
    }

    public static function getSessionExpiryHours()
    {
        return (int) self::get('SESSION_TOKEN_EXPIRY_HOURS', 24);
    }

    public static function isDebug()
    {
        return self::get('APP_DEBUG', 'false') === 'true';
    }
}
