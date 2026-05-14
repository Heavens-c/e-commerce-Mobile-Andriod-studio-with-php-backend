<?php
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();

$categoryId = isset($_GET['category_id']) ? (int)$_GET['category_id'] : 0;
$search = isset($_GET['search']) ? sanitize($_GET['search']) : '';
$featured = isset($_GET['featured']) ? (int)$_GET['featured'] : 0;
$flashSale = isset($_GET['flash_sale']) ? (int)$_GET['flash_sale'] : 0;
$page = isset($_GET['page']) ? max(1, (int)$_GET['page']) : 1;
$limit = isset($_GET['limit']) ? min(50, max(1, (int)$_GET['limit'])) : 20;
$offset = ($page - 1) * $limit;
$sortBy = isset($_GET['sort']) ? sanitize($_GET['sort']) : 'created_at';

$allowedSorts = ['created_at', 'price', 'rating', 'sold_count', 'name'];
if (!in_array($sortBy, $allowedSorts)) $sortBy = 'created_at';
$sortOrder = (isset($_GET['order']) && strtolower($_GET['order']) === 'asc') ? 'ASC' : 'DESC';

$where = ["p.is_active = 1"];
$params = [];

if ($categoryId > 0) {
    $where[] = "p.category_id = ?";
    $params[] = $categoryId;
}
if (!empty($search)) {
    $where[] = "(p.name LIKE ? OR p.description LIKE ? OR p.brand LIKE ? OR c.name LIKE ? OR (p.tags IS NOT NULL AND p.tags LIKE ?) OR (p.variations IS NOT NULL AND CAST(p.variations AS CHAR) LIKE ?))";
    $searchParam = "%$search%";
    $params[] = $searchParam;
    $params[] = $searchParam;
    $params[] = $searchParam;
    $params[] = $searchParam;
    $params[] = $searchParam;
    $params[] = $searchParam;
}
if ($featured) {
    $where[] = "p.is_featured = 1";
}
if ($flashSale) {
    $where[] = "p.is_flash_sale = 1";
}

$whereClause = implode(' AND ', $where);

$countStmt = $pdo->prepare("SELECT COUNT(*) FROM products p WHERE $whereClause");
$countStmt->execute($params);
$total = $countStmt->fetchColumn();

$sql = "SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE $whereClause ORDER BY p.$sortBy $sortOrder LIMIT $limit OFFSET $offset";

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$products = $stmt->fetchAll();

foreach ($products as &$product) {
    $product['id'] = (int)$product['id'];
    $product['price'] = (float)$product['price'];
    $product['original_price'] = $product['original_price'] ? (float)$product['original_price'] : null;
    $product['discount_percent'] = (int)$product['discount_percent'];
    $product['stock'] = (int)$product['stock'];
    $product['sold_count'] = (int)$product['sold_count'];
    $product['rating'] = (float)$product['rating'];
    $product['review_count'] = (int)$product['review_count'];
    $product['is_featured'] = (bool)$product['is_featured'];
    $product['is_flash_sale'] = (bool)$product['is_flash_sale'];
    if ($product['variations']) {
        $product['variations'] = json_decode($product['variations']);
    }
    $images = [$product['image_url']];
    for ($i = 2; $i <= 5; $i++) {
        if ($product["image_url_$i"]) $images[] = $product["image_url_$i"];
    }
    $product['images'] = $images;
}

sendResponse(200, true, 'Products retrieved', [
    'products' => $products,
    'pagination' => [
        'total' => (int)$total,
        'page' => $page,
        'limit' => $limit,
        'total_pages' => ceil($total / $limit)
    ]
]);
