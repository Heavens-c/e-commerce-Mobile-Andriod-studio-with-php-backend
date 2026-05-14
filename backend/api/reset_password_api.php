<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(405, false, 'Method not allowed');
}

$pdo  = getDBConnection();
$data = getJsonInput();
validateRequired($data, ['token', 'password']);

$token = preg_replace('/[^a-f0-9]/i', '', (string) $data['token']);
$pass  = (string) $data['password'];

if (strlen($token) < 32) {
    sendResponse(400, false, 'Invalid token');
}
if (strlen($pass) < 6) {
    sendResponse(400, false, 'Password must be at least 6 characters');
}

$stmt = $pdo->prepare('SELECT * FROM password_resets WHERE token = ? AND expires_at > NOW() LIMIT 1');
$stmt->execute([$token]);
$row = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$row) {
    sendResponse(400, false, 'Invalid or expired token');
}

$hash = password_hash($pass, PASSWORD_DEFAULT);
if ($row['account_type'] === 'admin') {
    $pdo->prepare('UPDATE admin SET password = ?, reset_token = NULL, reset_expiry = NULL WHERE id = ?')
        ->execute([$hash, $row['user_id']]);
} else {
    $pdo->prepare('UPDATE users SET password = ?, reset_token = NULL, reset_expiry = NULL WHERE id = ?')
        ->execute([$hash, $row['user_id']]);
}
$pdo->prepare('DELETE FROM password_resets WHERE email = ?')->execute([$row['email']]);

sendResponse(200, true, 'Password updated successfully');
