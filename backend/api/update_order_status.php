<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(405, false, 'Method not allowed');
}

$pdo      = getDBConnection();
authenticateAdmin($pdo);

$data = getJsonInput();
if (!isset($data['id']) || !isset($data['status'])) {
    sendResponse(400, false, 'Missing id or status');
}

$orderId   = max(0, (int) $data['id']);
$uiStatus  = strtolower(sanitize(trim((string) $data['status'])));

if ($orderId <= 0) {
    sendResponse(400, false, 'Invalid order id');
}

$allowed = ['pending', 'processing', 'completed', 'cancelled'];
if (!in_array($uiStatus, $allowed, true)) {
    sendResponse(400, false, 'Invalid status');
}

$dbStatus = adminUiStatusToDb($uiStatus);
if (!$dbStatus) {
    sendResponse(400, false, 'Invalid status mapping');
}

$check = $pdo->prepare('SELECT id FROM orders WHERE id = ?');
$check->execute([$orderId]);
if (!$check->fetch()) {
    sendResponse(404, false, 'Order not found');
}

$upd = $pdo->prepare('UPDATE orders SET order_status = ? WHERE id = ?');
$upd->execute([$dbStatus, $orderId]);

sendResponse(200, true, 'Order status updated', [
    'id'     => $orderId,
    'status' => $uiStatus,
]);
