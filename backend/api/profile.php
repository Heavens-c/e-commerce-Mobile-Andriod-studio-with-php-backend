<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST' || $method === 'PUT') {
    $data = getJsonInput();
    $updates = [];
    $params = [];

    $allowedFields = ['full_name', 'phone', 'address', 'city', 'province', 'zip_code'];
    foreach ($allowedFields as $field) {
        if (isset($data[$field])) {
            $updates[] = "$field = ?";
            $params[] = sanitize($data[$field]);
        }
    }

    if (empty($updates)) {
        sendResponse(400, false, 'No fields to update');
    }

    $params[] = $user['id'];
    $sql = "UPDATE users SET " . implode(', ', $updates) . " WHERE id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    // Return updated profile
    $stmt = $pdo->prepare("SELECT id, full_name, email, phone, avatar_url, address, city, province, zip_code, country, membership, coins, points FROM users WHERE id = ?");
    $stmt->execute([$user['id']]);
    $updatedUser = $stmt->fetch();

    sendResponse(200, true, 'Profile updated', ['user' => $updatedUser]);
} else {
    sendResponse(200, true, 'Profile retrieved', ['user' => $user]);
}
