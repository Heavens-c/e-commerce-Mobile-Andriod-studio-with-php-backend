<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
authenticateAdmin($pdo);

$page  = isset($_GET['page']) ? max(1, (int) $_GET['page']) : 1;
$limit = isset($_GET['limit']) ? min(100, max(1, (int) $_GET['limit'])) : 30;
$offset = ($page - 1) * $limit;

$countStmt = $pdo->query('SELECT COUNT(*) FROM products');
$total     = (int) $countStmt->fetchColumn();

$sql = <<<SQL
SELECT p.*, c.name AS category_name
FROM products p
LEFT JOIN categories c ON c.id = p.category_id
ORDER BY p.updated_at DESC, p.id DESC
LIMIT $limit OFFSET $offset
SQL;

$stmt = $pdo->prepare($sql);
$stmt->execute();
$products = $stmt->fetchAll();

foreach ($products as &$product) {
    $product['id'] = (int) $product['id'];
    $product['price'] = (float) $product['price'];
    $product['original_price'] = $product['original_price'] ? (float) $product['original_price'] : null;
    $product['discount_percent'] = (int) $product['discount_percent'];
    $product['stock'] = (int) $product['stock'];
    $product['sold_count'] = (int) $product['sold_count'];
    $product['rating'] = (float) $product['rating'];
    $product['review_count'] = (int) $product['review_count'];
    $product['is_featured'] = (bool) $product['is_featured'];
    $product['is_flash_sale'] = (bool) $product['is_flash_sale'];
    $product['is_active'] = (bool) $product['is_active'];
    if (!empty($product['variations'])) {
        $product['variations'] = json_decode((string) $product['variations']);
    }
    $images = [$product['image_url']];
    for ($i = 2; $i <= 5; $i++) {
        if (!empty($product["image_url_$i"])) {
            $images[] = $product["image_url_$i"];
        }
    }
    $product['images'] = $images;
}

sendResponse(200, true, 'Products', [
    'products' => $products,
    'pagination' => [
        'total' => $total,
        'page' => $page,
        'limit' => $limit,
        'total_pages' => $limit > 0 ? (int) ceil($total / $limit) : 0,
    ],
]);
