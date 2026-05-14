<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);

$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$limit = 20;
$offset = ($page - 1) * $limit;
$status = isset($_GET['status']) ? sanitize($_GET['status']) : '';

$where = "user_id = ?";
$params = [$user['id']];
if (!empty($status) && $status !== 'all') {
    $where .= " AND order_status = ?";
    $params[] = $status;
}

$stmt = $pdo->prepare("SELECT * FROM orders WHERE $where ORDER BY created_at DESC LIMIT $limit OFFSET $offset");
$stmt->execute($params);
$orders = $stmt->fetchAll();

foreach ($orders as &$order) {
    $order['id'] = (int)$order['id'];
    $order['total'] = (float)$order['total'];
    $stmt2 = $pdo->prepare("SELECT * FROM order_items WHERE order_id = ?");
    $stmt2->execute([$order['id']]);
    $order['items'] = $stmt2->fetchAll();
    foreach ($order['items'] as &$item) {
        $item['quantity'] = (int)$item['quantity'];
        $item['unit_price'] = (float)$item['unit_price'];
        $item['total_price'] = (float)$item['total_price'];
    }
}

sendResponse(200, true, 'Orders retrieved', ['orders' => $orders]);
