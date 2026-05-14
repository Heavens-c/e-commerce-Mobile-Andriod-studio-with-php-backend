<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$productId = isset($_GET['product_id']) ? (int)$_GET['product_id'] : 0;
if ($productId <= 0) sendResponse(400, false, 'Product ID required');

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    $user = authenticateUser($pdo);
    $data = getJsonInput();
    validateRequired($data, ['rating']);

    $rating = max(1, min(5, (int)$data['rating']));
    $comment = isset($data['comment']) ? sanitize($data['comment']) : null;

    $stmt = $pdo->prepare("INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)");
    $stmt->execute([$user['id'], $productId, $rating, $comment]);

    // Update product average rating
    $stmt = $pdo->prepare("UPDATE products SET rating = (SELECT AVG(rating) FROM reviews WHERE product_id = ?), review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = ?) WHERE id = ?");
    $stmt->execute([$productId, $productId, $productId]);

    sendResponse(201, true, 'Review added');
} else {
    $stmt = $pdo->prepare("SELECT r.*, u.full_name, u.avatar_url FROM reviews r JOIN users u ON r.user_id = u.id WHERE r.product_id = ? AND r.is_approved = 1 ORDER BY r.created_at DESC");
    $stmt->execute([$productId]);
    $reviews = $stmt->fetchAll();

    foreach ($reviews as &$r) {
        $r['rating'] = (int)$r['rating'];
    }

    sendResponse(200, true, 'Reviews retrieved', ['reviews' => $reviews]);
}
