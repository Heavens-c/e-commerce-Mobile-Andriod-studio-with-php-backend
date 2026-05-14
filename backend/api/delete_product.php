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

$upd = $pdo->prepare('UPDATE products SET is_active = 0 WHERE id = ?');
$upd->execute([$id]);
if ($upd->rowCount() === 0) {
    sendResponse(404, false, 'Product not found');
}

sendResponse(200, true, 'Product deactivated', ['id' => $id]);
