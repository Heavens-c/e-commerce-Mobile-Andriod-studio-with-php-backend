<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);
$data = getJsonInput();
validateRequired($data, ['cart_id']);

$cartId = (int)$data['cart_id'];
$action = isset($data['action']) ? $data['action'] : 'set';
$quantity = isset($data['quantity']) ? max(0, (int)$data['quantity']) : 0;

$stmt = $pdo->prepare("SELECT * FROM cart WHERE id = ? AND user_id = ?");
$stmt->execute([$cartId, $user['id']]);
$cartItem = $stmt->fetch();

if (!$cartItem) {
    sendResponse(404, false, 'Cart item not found');
}

if ($action === 'remove' || $quantity === 0) {
    $stmt = $pdo->prepare("DELETE FROM cart WHERE id = ? AND user_id = ?");
    $stmt->execute([$cartId, $user['id']]);
    sendResponse(200, true, 'Item removed from cart');
}

if ($action === 'increment') {
    $quantity = $cartItem['quantity'] + 1;
} elseif ($action === 'decrement') {
    $quantity = max(1, $cartItem['quantity'] - 1);
}

$stmt = $pdo->prepare("UPDATE cart SET quantity = ? WHERE id = ? AND user_id = ?");
$stmt->execute([$quantity, $cartId, $user['id']]);

sendResponse(200, true, 'Cart updated', ['quantity' => $quantity]);
