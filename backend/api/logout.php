<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$headers    = getallheaders();
$authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';
if (empty($authHeader)) {
    sendResponse(401, false, 'Authorization token required');
}

$token = str_replace('Bearer ', '', $authHeader);
if ($token === '') {
    sendResponse(401, false, 'Authorization token required');
}

$stmt = $pdo->prepare('UPDATE users SET auth_token = NULL, token_expiry = NULL WHERE auth_token = ?');
$stmt->execute([$token]);
if ($stmt->rowCount() > 0) {
    sendResponse(200, true, 'Logged out successfully');
}

$stmt = $pdo->prepare('UPDATE admin SET auth_token = NULL, token_expiry = NULL WHERE auth_token = ?');
$stmt->execute([$token]);
if ($stmt->rowCount() > 0) {
    sendResponse(200, true, 'Logged out successfully');
}

// Idempotent: token already cleared or unknown
sendResponse(200, true, 'Logged out successfully');
