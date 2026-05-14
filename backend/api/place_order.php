<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
$user = authenticateUser($pdo);
$data = getJsonInput();
validateRequired($data, ['shipping_name', 'shipping_phone', 'shipping_address', 'payment_method']);

$paymentMethod = sanitize($data['payment_method']);
if (!in_array($paymentMethod, ['gcash', 'credit_card', 'cod'])) {
    sendResponse(400, false, 'Invalid payment method');
}

// Get cart items
$stmt = $pdo->prepare("SELECT c.*, p.name, p.price, p.image_url, p.stock FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ? AND p.is_active = 1");
$stmt->execute([$user['id']]);
$cartItems = $stmt->fetchAll();

if (empty($cartItems)) {
    sendResponse(400, false, 'Cart is empty');
}

// Validate stock
foreach ($cartItems as $item) {
    if ($item['stock'] < $item['quantity']) {
        sendResponse(400, false, "Insufficient stock for: {$item['name']}");
    }
}

$subtotal = 0;
foreach ($cartItems as $item) {
    $subtotal += $item['price'] * $item['quantity'];
}

$shippingFee = isset($data['shipping_fee']) ? (float)$data['shipping_fee'] : ($subtotal >= 50 ? 0 : 80);
$discount = isset($data['discount']) ? (float)$data['discount'] : 0;
$total = $subtotal + $shippingFee - $discount;

$pdo->beginTransaction();

try {
    $orderNumber = generateOrderNumber();

    $stmt = $pdo->prepare("INSERT INTO orders (user_id, order_number, subtotal, shipping_fee, discount, total, payment_method, shipping_name, shipping_phone, shipping_address, shipping_city, shipping_province, shipping_zip, voucher_code, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([
        $user['id'], $orderNumber, $subtotal, $shippingFee, $discount, $total, $paymentMethod,
        sanitize($data['shipping_name']), sanitize($data['shipping_phone']),
        sanitize($data['shipping_address']),
        isset($data['shipping_city']) ? sanitize($data['shipping_city']) : null,
        isset($data['shipping_province']) ? sanitize($data['shipping_province']) : null,
        isset($data['shipping_zip']) ? sanitize($data['shipping_zip']) : null,
        isset($data['voucher_code']) ? sanitize($data['voucher_code']) : null,
        isset($data['notes']) ? sanitize($data['notes']) : null
    ]);

    $orderId = $pdo->lastInsertId();

    $insertItem = $pdo->prepare("INSERT INTO order_items (order_id, product_id, product_name, product_image, variation, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $updateStock = $pdo->prepare("UPDATE products SET stock = stock - ?, sold_count = sold_count + ? WHERE id = ?");

    foreach ($cartItems as $item) {
        $lineTotal = $item['price'] * $item['quantity'];
        $insertItem->execute([$orderId, $item['product_id'], $item['name'], $item['image_url'], $item['variation'], $item['quantity'], $item['price'], $lineTotal]);
        $updateStock->execute([$item['quantity'], $item['quantity'], $item['product_id']]);
    }

    // Clear cart
    $stmt = $pdo->prepare("DELETE FROM cart WHERE user_id = ?");
    $stmt->execute([$user['id']]);

    // Add notification
    $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message, type, reference_id) VALUES (?, ?, ?, 'order', ?)");
    $stmt->execute([$user['id'], 'Order Placed!', "Your order $orderNumber has been placed successfully.", $orderId]);

    $pdo->commit();

    sendResponse(201, true, 'Order placed successfully', [
        'order_id' => (int)$orderId,
        'order_number' => $orderNumber,
        'total' => round($total, 2),
        'payment_method' => $paymentMethod,
        'status' => 'pending'
    ]);
} catch (Exception $e) {
    $pdo->rollBack();
    sendResponse(500, false, 'Failed to place order');
}
