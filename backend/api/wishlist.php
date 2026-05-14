<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);
$data = getJsonInput();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    validateRequired($data, ['product_id']);
    $productId = (int)$data['product_id'];

    $stmt = $pdo->prepare("SELECT id FROM wishlist WHERE user_id = ? AND product_id = ?");
    $stmt->execute([$user['id'], $productId]);

    if ($stmt->fetch()) {
        $stmt = $pdo->prepare("DELETE FROM wishlist WHERE user_id = ? AND product_id = ?");
        $stmt->execute([$user['id'], $productId]);
        sendResponse(200, true, 'Removed from wishlist', ['wishlisted' => false]);
    } else {
        $stmt = $pdo->prepare("INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)");
        $stmt->execute([$user['id'], $productId]);
        sendResponse(200, true, 'Added to wishlist', ['wishlisted' => true]);
    }
} else {
    $stmt = $pdo->prepare("SELECT w.id, w.product_id, p.name, p.price, p.original_price, p.image_url, p.rating, p.sold_count FROM wishlist w JOIN products p ON w.product_id = p.id WHERE w.user_id = ? AND p.is_active = 1 ORDER BY w.created_at DESC");
    $stmt->execute([$user['id']]);
    $items = $stmt->fetchAll();

    foreach ($items as &$item) {
        $item['product_id'] = (int)$item['product_id'];
        $item['price'] = (float)$item['price'];
        $item['original_price'] = $item['original_price'] ? (float)$item['original_price'] : null;
        $item['rating'] = (float)$item['rating'];
    }

    sendResponse(200, true, 'Wishlist retrieved', ['items' => $items]);
}
