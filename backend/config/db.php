<?php
// =====================================================
// FlashShop API – Database Configuration
// =====================================================

define('DB_HOST',    'localhost');
define('DB_NAME',    'flashshop_db');
define('DB_USER',    'root');       // ← change to your MySQL user
define('DB_PASS',    '');           // ← change to your MySQL password
define('DB_CHARSET', 'utf8mb4');

// Token Settings
define('TOKEN_SECRET',       'FlashShop_S3cur3_T0k3n_K3y_2024!@#');
define('TOKEN_EXPIRY_HOURS', 720); // 30 days

// Upload / URL Settings
define('UPLOAD_DIR', __DIR__ . '/../uploads/');
define('BASE_URL',   'http://208.115.197.18');  // ← change to your real domain
define('MAX_FILE_SIZE', 5 * 1024 * 1024);        // 5 MB

// Public URL prefix for password-reset links (browser). No trailing slash.
// Example XAMPP: http://localhost/flashshop/backend
if (!defined('RESET_LINK_BASE')) {
    define('RESET_LINK_BASE', rtrim(BASE_URL, '/') . '/backend');
}

// CORS + JSON headers (API only — web pages set FLASHSHOP_SKIP_HTTP_HEADERS before including this file)
if (!defined('FLASHSHOP_SKIP_HTTP_HEADERS') || !FLASHSHOP_SKIP_HTTP_HEADERS) {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization');
    header('Content-Type: application/json; charset=UTF-8');

    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit();
    }
}

// Database Connection
function getDBConnection(): PDO {
    try {
        $dsn = sprintf(
            'mysql:host=%s;dbname=%s;charset=%s',
            DB_HOST, DB_NAME, DB_CHARSET
        );
        return new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ]);
    } catch (PDOException $e) {
        // Log the real error but never expose DB details to the client
        error_log('FlashShop DB error: ' . $e->getMessage());
        // Do not call sendResponse() here: helpers.php may not be loaded yet when db.php fails early.
        http_response_code(500);
        header('Content-Type: application/json; charset=UTF-8');
        echo json_encode(['status' => 'error', 'message' => 'Database connection failed'], JSON_UNESCAPED_UNICODE);
        exit();
    }
}
