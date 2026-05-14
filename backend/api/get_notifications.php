<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);

$stmt = $pdo->prepare("SELECT * FROM notifications WHERE user_id = ? OR user_id IS NULL ORDER BY created_at DESC LIMIT 50");
$stmt->execute([$user['id']]);
$notifications = $stmt->fetchAll();

foreach ($notifications as &$n) {
    $n['id'] = (int)$n['id'];
    $n['is_read'] = (bool)$n['is_read'];
}

sendResponse(200, true, 'Notifications retrieved', ['notifications' => $notifications]);
