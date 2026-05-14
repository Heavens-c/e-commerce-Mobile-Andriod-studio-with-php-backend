<?php
declare(strict_types=1);

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/helpers.php';

$pdo = getDBConnection();
authenticateAdmin($pdo);

$totals = $pdo->query("SELECT COUNT(*) AS orders, COALESCE(SUM(total),0) AS revenue FROM orders")->fetch(PDO::FETCH_ASSOC);
$users  = (int) $pdo->query('SELECT COUNT(*) FROM users WHERE is_active = 1')->fetchColumn();
$prods  = (int) $pdo->query('SELECT COUNT(*) FROM products WHERE is_active = 1')->fetchColumn();

$statusRows = $pdo->query(
    "SELECT order_status AS label, COUNT(*) AS value FROM orders GROUP BY order_status"
)->fetchAll(PDO::FETCH_ASSOC);

$topProducts = $pdo->query(
    "SELECT oi.product_name AS name, SUM(oi.quantity) AS units_sold
     FROM order_items oi
     GROUP BY oi.product_id, oi.product_name
     ORDER BY units_sold DESC
     LIMIT 8"
)->fetchAll(PDO::FETCH_ASSOC);

$monthly = $pdo->query(
    "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS orders, COALESCE(SUM(total),0) AS revenue
     FROM orders
     WHERE created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
     GROUP BY DATE_FORMAT(created_at, '%Y-%m')
     ORDER BY month ASC"
)->fetchAll(PDO::FETCH_ASSOC);

sendResponse(200, true, 'Analytics', [
    'summary' => [
        'total_orders'    => (int) $totals['orders'],
        'total_revenue'   => (float) $totals['revenue'],
        'total_users'     => $users,
        'active_products' => $prods,
    ],
    'orders_by_status' => $statusRows,
    'top_products'     => $topProducts,
    'monthly'          => $monthly,
]);
