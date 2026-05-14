<?php
// =====================================================
// FlashShop API - Helper Functions
// =====================================================

/**
 * Send JSON API response
 */
function sendResponse($statusCode, $success, $message, $data = null) {
    http_response_code($statusCode);
    $response = [
        'success' => $success,
        'message' => $message
    ];
    if ($data !== null) {
        $response['data'] = $data;
    }
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
}

/**
 * Generate secure auth token
 */
function generateToken($userId) {
    $payload = $userId . '|' . time() . '|' . bin2hex(random_bytes(16));
    return hash('sha256', $payload . TOKEN_SECRET);
}

/**
 * Validate auth token and return user
 */
function authenticateUser($pdo) {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

    if (empty($authHeader)) {
        sendResponse(401, false, 'Authorization token required');
    }

    $token = str_replace('Bearer ', '', $authHeader);

    $stmt = $pdo->prepare("SELECT id, full_name, email, phone, avatar_url, address, city, province, zip_code, country, membership, coins, points FROM users WHERE auth_token = ? AND token_expiry > NOW() AND is_active = 1");
    $stmt->execute([$token]);
    $user = $stmt->fetch();

    if (!$user) {
        sendResponse(401, false, 'Invalid or expired token');
    }

    return $user;
}

/**
 * Validate admin Bearer token and return admin row (password excluded).
 */
function authenticateAdmin($pdo) {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

    if (empty($authHeader)) {
        sendResponse(401, false, 'Authorization token required');
    }

    $token = str_replace('Bearer ', '', $authHeader);

    $stmt = $pdo->prepare('SELECT id, username, email, full_name, role, avatar_url, auth_token, token_expiry, is_active FROM admin WHERE auth_token = ? AND token_expiry > NOW() AND is_active = 1');
    $stmt->execute([$token]);
    $admin = $stmt->fetch();

    if (!$admin) {
        sendResponse(401, false, 'Invalid or expired token');
    }

    return $admin;
}

/**
 * Read email/password for login from form-urlencoded (POST) or JSON body.
 */
function getLoginCredentials() {
    if (!empty($_POST['email'])) {
        return [
            'email'    => $_POST['email'],
            'password' => isset($_POST['password']) ? (string) $_POST['password'] : '',
        ];
    }
    $json = getJsonInput();
    return [
        'email'    => $json['email'] ?? '',
        'password' => isset($json['password']) ? (string) $json['password'] : '',
    ];
}

/**
 * Map admin UI order status to DB order_status ENUM value.
 */
function adminUiStatusToDb($uiStatus) {
    $map = [
        'pending'    => 'pending',
        'processing' => 'processing',
        'completed'  => 'delivered',
        'cancelled'  => 'cancelled',
    ];
    return $map[$uiStatus] ?? null;
}

/**
 * Map DB order_status to admin UI label (pending, processing, completed, cancelled).
 */
function dbOrderStatusToAdminUi($dbStatus) {
    $dbStatus = strtolower((string) $dbStatus);
    if ($dbStatus === 'delivered' || $dbStatus === 'shipped' || $dbStatus === 'confirmed') {
        return 'completed';
    }
    if ($dbStatus === 'returned') {
        return 'cancelled';
    }
    if (in_array($dbStatus, ['pending', 'processing', 'cancelled'], true)) {
        return $dbStatus;
    }
    return 'processing';
}

/**
 * Generate unique order number
 */
function generateOrderNumber() {
    return 'FS-' . date('Ymd') . '-' . strtoupper(substr(bin2hex(random_bytes(4)), 0, 8));
}

/**
 * Get POST JSON body
 */
function getJsonInput() {
    $input = json_decode(file_get_contents('php://input'), true);
    return $input ?? [];
}

/**
 * Validate required fields
 */
function validateRequired($data, $fields) {
    $missing = [];
    foreach ($fields as $field) {
        if (!isset($data[$field]) || trim($data[$field]) === '') {
            $missing[] = $field;
        }
    }
    if (!empty($missing)) {
        sendResponse(400, false, 'Missing required fields: ' . implode(', ', $missing));
    }
}

/**
 * Sanitize input string
 */
function sanitize($value) {
    return htmlspecialchars(strip_tags(trim($value)), ENT_QUOTES, 'UTF-8');
}

/**
 * Handle file upload
 */
function uploadFile($file, $subdir = 'products') {
    $allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    
    if (!in_array($file['type'], $allowedTypes)) {
        return ['error' => 'Invalid file type. Allowed: JPG, PNG, WebP, GIF'];
    }
    if ($file['size'] > MAX_FILE_SIZE) {
        return ['error' => 'File too large. Max: 5MB'];
    }

    $baseDir = rtrim(UPLOAD_DIR, '/\\') . DIRECTORY_SEPARATOR . $subdir;
    if (!is_dir($baseDir)) {
        if (!@mkdir($baseDir, 0755, true)) {
            return ['error' => 'Could not create upload directory'];
        }
    }

    $ext = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename = uniqid('img_') . '_' . time() . '.' . $ext;
    $uploadPath = $baseDir . DIRECTORY_SEPARATOR . $filename;

    if (move_uploaded_file($file['tmp_name'], $uploadPath)) {
        return ['url' => BASE_URL . '/uploads/' . $subdir . '/' . $filename];
    }
    return ['error' => 'Upload failed'];
}
