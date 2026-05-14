<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);
$data = getJsonInput();
validateRequired($data, ['product_id']);

$productId = (int)$data['product_id'];
$quantity = isset($data['quantity']) ? max(1, (int)$data['quantity']) : 1;
$variation = isset($data['variation']) ? sanitize($data['variation']) : null;

$stmt = $pdo->prepare("SELECT id, stock FROM products WHERE id = ? AND is_active = 1");
$stmt->execute([$productId]);
$product = $stmt->fetch();

if (!$product) {
    sendResponse(404, false, 'Product not found');
}
if ($product['stock'] < $quantity) {
    sendResponse(400, false, 'Insufficient stock');
}

$stmt = $pdo->prepare("SELECT id, quantity FROM cart WHERE user_id = ? AND product_id = ? AND (variation = ? OR (variation IS NULL AND ? IS NULL))");
$stmt->execute([$user['id'], $productId, $variation, $variation]);
$existing = $stmt->fetch();

if ($existing) {
    $newQty = $existing['quantity'] + $quantity;
    $stmt = $pdo->prepare("UPDATE cart SET quantity = ? WHERE id = ?");
    $stmt->execute([$newQty, $existing['id']]);
} else {
    $stmt = $pdo->prepare("INSERT INTO cart (user_id, product_id, quantity, variation) VALUES (?, ?, ?, ?)");
    $stmt->execute([$user['id'], $productId, $quantity, $variation]);
}

sendResponse(200, true, 'Added to cart');
