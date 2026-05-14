<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(405, false, 'Method not allowed');
}

$pdo  = getDBConnection();
authenticateAdmin($pdo);

$data = getJsonInput();
if (!isset($data['id'])) {
    sendResponse(400, false, 'Missing product id');
}

$id = (int) $data['id'];
if ($id <= 0) {
    sendResponse(400, false, 'Invalid product id');
}

$exists = $pdo->prepare('SELECT id FROM products WHERE id = ?');
$exists->execute([$id]);
if (!$exists->fetch()) {
    sendResponse(404, false, 'Product not found');
}

$fields = [];
$params = [];

if (isset($data['category_id'])) {
    $cid = (int) $data['category_id'];
    if ($cid <= 0) {
        sendResponse(400, false, 'Invalid category_id');
    }
    $cat = $pdo->prepare('SELECT id FROM categories WHERE id = ? AND is_active = 1');
    $cat->execute([$cid]);
    if (!$cat->fetch()) {
        sendResponse(400, false, 'Invalid category');
    }
    $fields[] = 'category_id = ?';
    $params[] = $cid;
}
if (isset($data['name'])) {
    $name = sanitize(trim((string) $data['name']));
    if ($name === '') {
        sendResponse(400, false, 'Invalid name');
    }
    $fields[] = 'name = ?';
    $params[] = $name;
}
if (isset($data['description'])) {
    $desc = trim((string) $data['description']);
    if ($desc === '') {
        sendResponse(400, false, 'Invalid description');
    }
    $fields[] = 'description = ?';
    $params[] = $desc;
}
if (isset($data['price'])) {
    $price = (float) $data['price'];
    if (!is_finite($price) || $price <= 0) {
        sendResponse(400, false, 'Invalid price');
    }
    $fields[] = 'price = ?';
    $params[] = $price;
}
if (isset($data['stock'])) {
    $stock = (int) $data['stock'];
    if ($stock < 0) {
        sendResponse(400, false, 'Invalid stock');
    }
    $fields[] = 'stock = ?';
    $params[] = $stock;
}
if (isset($data['image_url'])) {
    $img = sanitize(trim((string) $data['image_url']));
    if ($img === '') {
        sendResponse(400, false, 'Invalid image_url');
    }
    $fields[] = 'image_url = ?';
    $params[] = $img;
}
if (isset($data['original_price'])) {
    $fields[] = 'original_price = ?';
    $params[] = (float) $data['original_price'];
}
if (isset($data['discount_percent'])) {
    $fields[] = 'discount_percent = ?';
    $params[] = max(0, min(100, (int) $data['discount_percent']));
}
if (isset($data['brand'])) {
    $fields[] = 'brand = ?';
    $params[] = sanitize(trim((string) $data['brand']));
}
if (array_key_exists('tags', $data)) {
    $fields[] = 'tags = ?';
    $params[] = $data['tags'] === null || $data['tags'] === ''
        ? null
        : sanitize(trim((string) $data['tags']));
}
if (isset($data['is_featured'])) {
    $fields[] = 'is_featured = ?';
    $params[] = !empty($data['is_featured']) ? 1 : 0;
}
if (isset($data['is_flash_sale'])) {
    $fields[] = 'is_flash_sale = ?';
    $params[] = !empty($data['is_flash_sale']) ? 1 : 0;
}
if (isset($data['is_active'])) {
    $fields[] = 'is_active = ?';
    $params[] = !empty($data['is_active']) ? 1 : 0;
}

if (empty($fields)) {
    sendResponse(400, false, 'No fields to update');
}

$params[] = $id;
$sql = 'UPDATE products SET ' . implode(', ', $fields) . ' WHERE id = ?';
$upd = $pdo->prepare($sql);
$upd->execute($params);

sendResponse(200, true, 'Product updated', ['id' => $id]);
