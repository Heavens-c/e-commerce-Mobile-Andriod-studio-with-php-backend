<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(405, false, 'Method not allowed');
}

$pdo = getDBConnection();
authenticateAdmin($pdo);

$data = [];
if (!empty($_FILES['image']['tmp_name'])) {
    $data = $_POST;
} else {
    $data = getJsonInput();
}

validateRequired($data, ['category_id', 'name', 'description', 'price', 'stock']);

$categoryId = (int) $data['category_id'];
$name       = sanitize(trim((string) $data['name']));
$desc       = trim((string) $data['description']);
$price      = (float) $data['price'];
$stock      = (int) $data['stock'];
$tags       = isset($data['tags']) ? sanitize(trim((string) $data['tags'])) : null;

$imageUrl = '';
if (!empty($_FILES['image']['tmp_name'])) {
    $up = uploadFile($_FILES['image'], 'products');
    if (isset($up['error'])) {
        sendResponse(400, false, $up['error']);
    }
    $imageUrl = $up['url'];
} else {
    validateRequired($data, ['image_url']);
    $imageUrl = sanitize(trim((string) $data['image_url']));
}

if ($categoryId <= 0) {
    sendResponse(400, false, 'Invalid category_id');
}
if ($name === '' || $desc === '' || $imageUrl === '') {
    sendResponse(400, false, 'Empty fields are not allowed');
}
if (!is_finite($price) || $price <= 0) {
    sendResponse(400, false, 'Invalid price');
}
if ($stock < 0) {
    sendResponse(400, false, 'Invalid stock');
}

$cat = $pdo->prepare('SELECT id FROM categories WHERE id = ? AND is_active = 1');
$cat->execute([$categoryId]);
if (!$cat->fetch()) {
    sendResponse(400, false, 'Invalid category');
}

$original = isset($data['original_price']) ? (float) $data['original_price'] : null;
$discount = isset($data['discount_percent']) ? max(0, min(100, (int) $data['discount_percent'])) : 0;
$brand    = isset($data['brand']) ? sanitize(trim((string) $data['brand'])) : null;
$featured = !empty($data['is_featured']) ? 1 : 0;
$flash    = !empty($data['is_flash_sale']) ? 1 : 0;

$hasTags = false;
try {
    $pdo->query('SELECT tags FROM products LIMIT 1');
    $hasTags = true;
} catch (Throwable $e) {
    $hasTags = false;
}

if ($hasTags) {
    $stmt = $pdo->prepare(
        'INSERT INTO products (category_id, name, description, price, original_price, discount_percent, image_url, stock, brand, tags, is_featured, is_flash_sale) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)'
    );
    $stmt->execute([
        $categoryId, $name, $desc, $price, $original, $discount, $imageUrl, $stock, $brand, $tags,
        $featured, $flash,
    ]);
} else {
    $stmt = $pdo->prepare(
        'INSERT INTO products (category_id, name, description, price, original_price, discount_percent, image_url, stock, brand, is_featured, is_flash_sale) VALUES (?,?,?,?,?,?,?,?,?,?,?)'
    );
    $stmt->execute([
        $categoryId, $name, $desc, $price, $original, $discount, $imageUrl, $stock, $brand,
        $featured, $flash,
    ]);
}

sendResponse(201, true, 'Product created', ['id' => (int) $pdo->lastInsertId()]);
