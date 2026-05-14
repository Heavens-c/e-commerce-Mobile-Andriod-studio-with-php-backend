<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);

$stmt = $pdo->prepare("SELECT c.id as cart_id, c.quantity, c.variation, p.id as product_id, p.name, p.price, p.original_price, p.discount_percent, p.image_url, p.stock, p.brand FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ? AND p.is_active = 1 ORDER BY c.created_at DESC");
$stmt->execute([$user['id']]);
$items = $stmt->fetchAll();

$subtotal = 0;
foreach ($items as &$item) {
    $item['cart_id'] = (int)$item['cart_id'];
    $item['product_id'] = (int)$item['product_id'];
    $item['quantity'] = (int)$item['quantity'];
    $item['price'] = (float)$item['price'];
    $item['original_price'] = $item['original_price'] ? (float)$item['original_price'] : null;
    $item['stock'] = (int)$item['stock'];
    $item['line_total'] = round($item['price'] * $item['quantity'], 2);
    $subtotal += $item['line_total'];
}

sendResponse(200, true, 'Cart retrieved', [
    'items' => $items,
    'item_count' => count($items),
    'subtotal' => round($subtotal, 2)
]);
